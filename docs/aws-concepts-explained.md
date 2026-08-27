# AWS Deployment Concepts — Explained for Next Time

The deployment guide (`aws-deployment-guide.md`) is the *what* — click this,
type that. This doc is the *why*, written so it's still useful on a project
that isn't BracketRacingAPI. Read this once end to end; it's written to build
on itself.

## The one-sentence version

Your app is a container image. **ECR** stores that image. **ECS** runs it.
**RDS** is your database, managed by AWS instead of by you. **Secrets
Manager** keeps passwords out of your code. Everything else (security
groups, IAM roles, task definitions) exists to answer one of two questions:
*"is this thing allowed to talk to that thing?"* or *"where does this
container get its configuration from?"*

## Why not just use `docker-compose.yml`?

Compose is an **orchestration tool for one machine** — it starts multiple
containers and wires them together (`app` waits for `db`'s healthcheck, they
share a network, etc.) on whatever machine you run `docker compose up` on.
That machine is your laptop.

None of that job exists in the cloud version of this deployment:

- The "run multiple containers together" job doesn't need solving, because
  the database isn't a container anymore — **RDS is a fully managed database
  service**, not something you run yourself. There is no `db` container to
  orchestrate against.
- ECS doesn't read `docker-compose.yml` at all. It has its own equivalent
  concept (the **task definition** — more below), which does a similar job
  but is AWS's own format, not Docker's.

So compose's `db:` service is replaced by RDS, and compose's `bracket-racing-api:`
service's *build instructions* — the `Dockerfile` — are the one part that
carries forward, because ECS still needs to know how to build/run your app's
image. Compose itself stays useful for **local development only**: spin up
your app plus a throwaway MySQL container on your own machine, without
touching any real AWS resource.

## The core services, and how they're named confusingly

| Term | What it actually is | Common confusion |
|---|---|---|
| **ECR** (Elastic Container **Registry**) | Stores Docker images. Like a private Docker Hub. | Confused with ECS constantly — R for Registry (storage), S for Service (running). |
| **ECS** (Elastic Container **Service**) | Runs containers. | See above. |
| **Fargate** | A *mode* of running ECS tasks where AWS manages the underlying server for you. | Not a separate service — it's a launch type you pick inside ECS. The alternative (EC2 launch type) means you manage your own fleet of servers for ECS to schedule containers onto; Fargate means you never see a server at all. |
| **Cluster** | A logical grouping/namespace for ECS resources. | With Fargate, a cluster is *not* a set of physical machines — that's the whole point of Fargate. It's closer to a folder. |
| **Task definition** | A JSON blueprint: "run this image, on this port, with these env vars/secrets, this much CPU/memory, using this IAM role." | Not itself running anything — it's a *template*. |
| **Task** | One actual running instance of a task definition — the real container(s), actually executing. | People say "task definition" when they mean "task" and vice versa constantly. Definition = blueprint, Task = the live thing built from it. |
| **Service** | Keeps a desired number of tasks running from a task definition, restarting them if they crash, optionally load-balancing across them. | The layer that makes ECS self-healing — without a service, a task that dies just stays dead. |

Reading order for how these nest: a **cluster** contains **services**, a
**service** is configured with one **task definition** and a desired count,
and running that produces actual **tasks**.

## RDS

RDS = "Relational Database Service." You pick an engine (MySQL, in this
project), and AWS handles installing it, patching it, backing it up, and
giving you a network endpoint to connect to. You never SSH into a database
server or manage MySQL itself — you interact with it exactly like any other
MySQL instance (JDBC URL, username, password), the management layer is just
invisible.

The two settings that mattered most in this project:
- **Public access (Yes/No)**: whether the database gets a network path
  reachable from outside its VPC at all. This is separate from and evaluated
  *before* security groups — if this is "No," no security group rule can make
  it reachable from the public internet, full stop.
- **VPC security group**: *given* that it's reachable at all (or reachable
  from inside the VPC), this decides *who specifically* is allowed to connect.

## Security groups — the actual mental model

A security group is a **stateful virtual firewall** attached to a resource
(an RDS instance, an ECS task, an EC2 instance, etc.). "Stateful" means if
you allow outbound traffic, the response is automatically allowed back in
without a separate inbound rule — you almost never think about outbound
rules, only inbound.

The important trick used in this project: **a security group's source can be
another security group, not just an IP range.** Instead of writing "allow
port 3306 from `10.0.1.5`" (one specific machine's IP, which changes), you
write "allow port 3306 from anything wearing `ecs-sg`." Now *any* task that
gets attached to `ecs-sg` can reach the database, automatically, with no
rule changes needed as tasks come and go, restart, or get new IPs. This is
the standard pattern for "let my compute layer talk to my database layer"
in AWS and is worth reusing on every future project with this shape.

## IAM roles — task role vs. task execution role

These sound like the same thing and are not:

- **Task execution role**: a role **ECS itself** assumes on your behalf,
  *before* your container's code even starts, to do the plumbing needed to
  launch it — pull the image from ECR, fetch secrets from Secrets Manager,
  write logs to CloudWatch. Your application code never sees this role or
  its credentials.
- **Task role**: a role **your running application** would assume to call
  AWS APIs itself (e.g., if your app needed to read/write an S3 bucket, or
  query DynamoDB). This project's app makes zero AWS API calls, so this was
  left as "None."

If a future project's app needs to talk to an AWS service directly from
application code, that's a task role permission, not a task execution role
one.

## Secrets Manager — how a secret actually becomes an env var

This was the most confusing part live, so here's the full trace of one
value, start to finish, using `DB_PASSWORD` as the example:

1. **At rest**: Secrets Manager stores a JSON blob at a given ARN:
   `{"username": "admin", "password": "...", "host": "...", ...}`. Nothing
   has used it yet — it's just data sitting there.
2. **The task definition is the only place a "reference" exists.** In the
   container's secret configuration, you write something like:
   `valueFrom = arn:aws:secretsmanager:...:secret:bracket-racing/db-credentials-XXXXXX:password::`
   The `:password::` suffix means "pull just the `password` field out of this
   secret's JSON" (the trailing `::` is an optional version qualifier, left
   blank). This ARN-based reference is the *only* syntax involved in this
   whole mechanism — nowhere else does "Secrets Manager syntax" appear.
3. **ECS resolves it at container startup**, using the task execution role's
   permission to call `secretsmanager:GetSecretValue`. This happens *before*
   your Spring Boot JAR runs.
4. **Your app receives a plain environment variable.** By the time
   `application-aws.properties`'s `spring.datasource.password=${DB_PASSWORD}`
   resolves, `DB_PASSWORD` is indistinguishable from any other env var — same
   mechanism as running `docker run -e DB_PASSWORD=... ...` by hand, or
   `export DB_PASSWORD=...` locally. Spring has no AWS SDK code and no
   awareness that Secrets Manager exists.

**The mistake actually made**: putting just `password` (the bare key name)
into that "Value from" field instead of the full ARN. ECS's secrets
mechanism supports two different sources — **SSM Parameter Store** (where a
bare string genuinely is the full identifier, a parameter name) and
**Secrets Manager** (where you need the full ARN because a secret can hold
multiple keys). Give it a bare string and it silently assumes you meant an
SSM parameter, which then fails on a permission it was never granted — the
resulting error (`ssm:GetParameters ... AccessDeniedException`) looks like an
IAM problem but is actually "you pointed at the wrong AWS service entirely."
**Rule of thumb: if a secret's "Value from" field isn't a full
`arn:aws:secretsmanager:...` string, it's wrong.**

## Region — why it kept coming up

Almost nothing in AWS is global by default. An ECR repository, an RDS
instance, a Secrets Manager secret, an ECS cluster — each one lives in
exactly one region (e.g., `us-east-2`), and by default nothing in one region
can reference something in another. This project's first real mistake was
creating the ECR repo in `us-east-1` while everything else went into
`us-east-2` — the fix was just recreating it in the matching region. The
takeaway for next time: **pick a region before creating anything, and check
the region dropdown on every single service's console page** — it's easy to
not notice it silently reset between browser tabs/sessions.

## Glossary (quick lookup)

| Term | Meaning |
|---|---|
| **ARN** | Amazon Resource Name — the globally unique "full address" of any AWS resource, e.g. `arn:aws:secretsmanager:us-east-2:123456789:secret:my-secret-AbCdEf`. |
| **VPC** | Virtual Private Cloud — an isolated virtual network inside AWS that your resources live in. Two resources generally need to be in the same VPC (or have networking set up between VPCs) to reach each other privately. |
| **Security group** | A stateful virtual firewall attached to a resource, controlling inbound/outbound traffic. Can reference IP ranges or other security groups as sources. |
| **IAM** | Identity and Access Management — AWS's permissions system. Users, roles, and policies all live here. |
| **Role vs. User** | A user is a persistent identity (a person or a long-lived credential). A role is a set of permissions something can *temporarily assume* — ECS assuming the task execution role is a role being used exactly as intended, not a person logging in. |
| **JDBC URL** | The connection string format Java database drivers use: `jdbc:mysql://<host>:<port>/<database-name>`. Not an AWS concept — just how Spring's `spring.datasource.url` expects the address. |
| **CloudWatch Logs** | Where container stdout/stderr ends up automatically when an ECS task definition has logging configured. First place to check when a task fails or misbehaves. |
