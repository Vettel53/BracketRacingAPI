package com.example.application.statistics;

import com.example.application.account.model.AppUser;
import com.example.application.run.Run;
import com.example.application.run.RunRepo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class StatisticsService {

    private final RunRepo runRepo;

    public StatisticsService(RunRepo runRepo) {
        this.runRepo = runRepo;
    }

    public BigDecimal getBreakoutPercentage(AppUser appUser) {
        List<Run> runs = runRepo.findByAppUser(appUser);
        if (runs.isEmpty()) {
            return null;
        }

        long breakoutCount = runs.stream()
                .filter(run -> run.getDial().compareTo(run.getFullTrack()) > 0)
                .count();

        return percentage(breakoutCount, runs.size());
    }

    public BigDecimal getOverPercentage(AppUser appUser) {
        List<Run> runs = runRepo.findByAppUser(appUser);
        if (runs.isEmpty()) {
            return null;
        }

        long overCount = runs.stream()
                .filter(run -> run.getDial().compareTo(run.getFullTrack()) <= 0)
                .count();

        return percentage(overCount, runs.size());
    }

    public BigDecimal getReactionAverage(AppUser appUser) {
        List<Run> runs = runRepo.findByAppUser(appUser);
        if (runs.isEmpty()) {
            return null;
        }

        BigDecimal total = runs.stream()
                .map(Run::getReaction)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(BigDecimal.valueOf(runs.size()), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal percentage(long count, int total) {
        double pct = ((double) count / total) * 100;
        // NOTE: the original code used `new BigDecimal(double)` here, which captures
        // binary floating-point representation error directly (e.g. 33.33 becomes
        // 33.32999999999999971...). BigDecimal.valueOf(double) goes through
        // Double.toString() first and avoids that.
        return BigDecimal.valueOf(pct).setScale(2, RoundingMode.HALF_UP);
    }
}
