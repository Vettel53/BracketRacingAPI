package com.example.application.run;

import com.example.application.account.model.AppUser;
import com.example.application.account.service.UserService;
import com.example.application.run.dto.CreateRunRequest;
import com.example.application.run.dto.RunResponse;
import com.example.application.run.dto.UpdateRunRequest;
import com.example.application.shared.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/runs")
public class RunController {

    private final RunService runService;
    private final RunRepo runRepo;
    private final UserService userService;

    @Value("${runs.fake-generation.enabled:false}")
    private boolean fakeGenerationEnabled;

    public RunController(RunService runService, RunRepo runRepo, UserService userService) {
        this.runService = runService;
        this.runRepo = runRepo;
        this.userService = userService;
    }

    @GetMapping
    public List<RunResponse> getMyRuns(HttpServletRequest request) {
        AppUser user = currentUser(request);
        return runService.getUserRuns(user).stream().map(RunMapper::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<RunResponse> createRun(HttpServletRequest request, @Valid @RequestBody CreateRunRequest body) {
        AppUser user = currentUser(request);
        Run created = runService.createRun(user, body);
        return ResponseEntity.ok(RunMapper.toResponse(created));
    }

    @PostMapping("/fake")
    public ResponseEntity<RunResponse> createFakeRun(HttpServletRequest request) {
        if (!fakeGenerationEnabled) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FAKE_RUN_DISABLED", "Fake run generation is disabled on this profile");
        }
        AppUser user = currentUser(request);
        Run created = runService.createFakeRun(user);
        return ResponseEntity.ok(RunMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RunResponse> updateRun(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody UpdateRunRequest body) {
        Run run = ownedRunOrThrow(request, id);
        Run updated = runService.updateRun(run, body);
        return ResponseEntity.ok(RunMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRun(HttpServletRequest request, @PathVariable Long id) {
        Run run = ownedRunOrThrow(request, id);
        runService.deleteRun(run);
        return ResponseEntity.noContent().build();
    }

    private AppUser currentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AppUser user = userService.getById(userId);
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "Authenticated user no longer exists");
        }
        return user;
    }

    // The old Vaadin dashboard resolved "your runs" purely from the session's
    // logged-in user with no per-run ownership check on edit/delete. Any
    // authenticated user hitting these endpoints directly could touch another
    // user's run by guessing an id. This closes that gap.
    private Run ownedRunOrThrow(HttpServletRequest request, Long runId) {
        AppUser user = currentUser(request);
        Run run = runRepo.findById(runId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "RUN_NOT_FOUND", "Run " + runId + " not found"));

        if (!run.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "RUN_ACCESS_DENIED", "You do not have access to this run");
        }
        return run;
    }
}
