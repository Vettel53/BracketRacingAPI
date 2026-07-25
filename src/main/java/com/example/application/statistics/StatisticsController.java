package com.example.application.statistics;

import com.example.application.account.model.AppUser;
import com.example.application.account.service.UserService;
import com.example.application.shared.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final UserService userService;

    public StatisticsController(StatisticsService statisticsService, UserService userService) {
        this.statisticsService = statisticsService;
        this.userService = userService;
    }

    @GetMapping
    public Map<String, BigDecimal> getStatistics(HttpServletRequest request) {
        AppUser user = currentUser(request);

        Map<String, BigDecimal> stats = new LinkedHashMap<>();
        stats.put("breakoutPercentage", statisticsService.getBreakoutPercentage(user));
        stats.put("overPercentage", statisticsService.getOverPercentage(user));
        stats.put("averageReactionTime", statisticsService.getReactionAverage(user));
        return stats;
    }

    private AppUser currentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AppUser user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("Authenticated user no longer exists");
        }
        return user;
    }
}
