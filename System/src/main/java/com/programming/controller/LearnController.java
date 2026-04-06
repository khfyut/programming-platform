package com.programming.controller;

import com.programming.entity.LearningPath;
import com.programming.entity.UserPathProgress;
import com.programming.service.KnowledgeMasteryService;
import com.programming.service.LearnService;
import com.programming.service.LearningService;
import com.programming.util.ResultUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learn")
public class LearnController {

    @Autowired
    private LearnService learnService;

    @Autowired
    private LearningService learningService;

    @Autowired
    private KnowledgeMasteryService knowledgeMasteryService;

    @GetMapping("/my")
    public ResultUtil<Map<String, Object>> getMyStatistics(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> result = learnService.getStatistics(userId);
            return ResultUtil.success(result);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/recommend")
    public ResultUtil getRecommend(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return ResultUtil.success(learnService.getRecommend(userId));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/assessment")
    public ResultUtil getAssessmentQuestions(@RequestParam String language,
                                             @RequestParam String direction,
                                             @RequestParam Integer limit) {
        try {
            return ResultUtil.success(learningService.getAssessmentQuestions(language, direction, limit));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/assessment/commit")
    public ResultUtil submitAssessment(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            String language = (String) params.get("language");
            String direction = (String) params.get("direction");

            @SuppressWarnings("unchecked")
            Map<String, Object> answersMap = (Map<String, Object>) params.get("answers");
            Map<Long, String> answers = new HashMap<>();
            for (Map.Entry<String, Object> entry : answersMap.entrySet()) {
                try {
                    Long questionId = Long.parseLong(entry.getKey());
                    String answer = entry.getValue() != null ? entry.getValue().toString() : "";
                    answers.put(questionId, answer);
                } catch (NumberFormatException ignored) {
                }
            }

            return ResultUtil.success(learningService.submitAssessment(userId, language, direction, answers));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/path")
    public ResultUtil getMyPath(HttpServletRequest request, @RequestParam(required = false) Long pathId) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (pathId != null) {
                return ResultUtil.success(learningService.getUserPathProgress(userId, pathId));
            }

            List<UserPathProgress> progresses = learningService.getUserProgressList(userId);
            if (!progresses.isEmpty()) {
                UserPathProgress progress = progresses.get(0);
                LearningPath path = learningService.getPathDetail(progress.getPathId());
                return ResultUtil.success(path);
            }

            List<LearningPath> paths = learningService.getAvailablePaths("all", "all");
            if (!paths.isEmpty()) {
                return ResultUtil.success(paths.get(0));
            }
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/path/progress")
    public ResultUtil getPathProgress(HttpServletRequest request, @RequestParam(required = false) Long pathId) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (pathId != null) {
                return ResultUtil.success(learningService.getUserPathProgressWithPercentage(userId, pathId));
            }

            List<UserPathProgress> progresses = learningService.getUserProgressList(userId);
            if (!progresses.isEmpty()) {
                return ResultUtil.success(
                        learningService.getUserPathProgressWithPercentage(userId, progresses.get(0).getPathId())
                );
            }
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/path/chapter/{chapterId}")
    public ResultUtil getChapterDetail(@PathVariable Long chapterId) {
        try {
            return ResultUtil.success(null);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/path/level/unlock")
    public ResultUtil unlockLevel(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Long levelId = Long.valueOf(params.get("levelId").toString());
            boolean result = learningService.unlockLevel(userId, levelId);
            return ResultUtil.success(result);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/path/level/complete")
    public ResultUtil completeLevel(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Long levelId = Long.valueOf(params.get("levelId").toString());
            boolean result = learningService.completeLevel(userId, levelId);
            return ResultUtil.success(result);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/paths")
    public ResultUtil getAvailablePaths(@RequestParam(required = false) String language,
                                        @RequestParam(required = false) String direction) {
        try {
            return ResultUtil.success(
                    learningService.getAvailablePaths(language != null ? language : "all", direction != null ? direction : "all")
            );
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/path/detail/{pathId}")
    public ResultUtil getPathDetail(@PathVariable Long pathId) {
        try {
            return ResultUtil.success(learningService.getPathDetail(pathId));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/knowledge/graph")
    public ResultUtil getKnowledgeGraph(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return ResultUtil.success(knowledgeMasteryService.getKnowledgeGraph(userId, null, null, null));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/knowledge/mastery")
    public ResultUtil getKnowledgeMastery(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return ResultUtil.success(knowledgeMasteryService.getUserKnowledgeMasteries(userId));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/report/weekly")
    public ResultUtil getWeeklyReport(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return ResultUtil.success(knowledgeMasteryService.getWeeklyReport(userId));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/report/monthly")
    public ResultUtil getMonthlyReport(HttpServletRequest request,
                                       @RequestParam(required = false) Integer year,
                                       @RequestParam(required = false) Integer month) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return ResultUtil.success(knowledgeMasteryService.getMonthlyReport(userId, year, month));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/weakness")
    public ResultUtil getWeakness(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return ResultUtil.success(knowledgeMasteryService.getWeaknessAnalysis(userId));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/knowledge/distribution")
    public ResultUtil getKnowledgeDistribution(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return ResultUtil.success(knowledgeMasteryService.getKnowledgeMasteryDistribution(userId));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/errors/frequency")
    public ResultUtil getHighFrequencyErrors(HttpServletRequest request, @RequestParam Integer limit) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            return ResultUtil.success(knowledgeMasteryService.getHighFrequencyErrors(userId, limit));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/level/{levelId}/problems")
    public ResultUtil getLevelProblems(@PathVariable Long levelId) {
        try {
            return ResultUtil.success(learningService.getProblemsByLevelId(levelId));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/level/{levelId}/problem/{problemId}")
    public ResultUtil bindProblemToLevel(@PathVariable Long levelId,
                                         @PathVariable Long problemId,
                                         @RequestParam(required = false) Integer orderNum) {
        try {
            learningService.bindProblemToLevel(levelId, problemId, orderNum);
            return ResultUtil.success("绑定成功");
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @DeleteMapping("/level/{levelId}/problem/{problemId}")
    public ResultUtil unbindProblemFromLevel(@PathVariable Long levelId, @PathVariable Long problemId) {
        try {
            learningService.unbindProblemFromLevel(levelId, problemId);
            return ResultUtil.success("解绑成功");
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/level/{levelId}/problems/batch")
    public ResultUtil batchBindProblemsToLevel(@PathVariable Long levelId, @RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> problemIds = (List<Long>) params.get("problemIds");
            learningService.batchBindProblemsToLevel(levelId, problemIds);
            return ResultUtil.success("批量绑定成功");
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PutMapping("/level/{levelId}/problems")
    public ResultUtil updateLevelProblems(@PathVariable Long levelId, @RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> problemIds = (List<Long>) params.get("problemIds");
            learningService.updateLevelProblems(levelId, problemIds);
            return ResultUtil.success("更新成功");
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/level/{levelId}/resources")
    public ResultUtil getLevelResources(@PathVariable Long levelId) {
        try {
            return ResultUtil.success(learningService.getResourcesByLevelId(levelId));
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/paths/active")
    public ResultUtil getActivePaths(HttpServletRequest request, @RequestParam(required = false) Long userId) {
        try {
            Long targetUserId = userId != null ? userId : (Long) request.getAttribute("userId");
            List<UserPathProgress> progresses = learningService.getUserProgressList(targetUserId);
            List<Map<String, Object>> activePaths = new ArrayList<>();

            for (UserPathProgress progress : progresses) {
                LearningPath path = learningService.getPathDetail(progress.getPathId());
                Map<String, Object> progressInfo = learningService.getUserPathProgressWithPercentage(targetUserId, progress.getPathId());
                Map<String, Object> pathInfo = new HashMap<>();
                pathInfo.put("id", path.getId());
                pathInfo.put("name", path.getName());
                pathInfo.put("description", path.getDescription());
                pathInfo.put("language", path.getLanguage());
                pathInfo.put("direction", path.getDirection());
                pathInfo.put("progress", progressInfo.getOrDefault("progress", 0));
                pathInfo.put("completedLevels", progressInfo.getOrDefault("completedLevels", 0));
                pathInfo.put("totalLevels", progressInfo.getOrDefault("totalLevels", 0));
                pathInfo.put("currentChapterId", progressInfo.get("currentChapterId"));
                pathInfo.put("currentLevelId", progressInfo.get("currentLevelId"));
                activePaths.add(pathInfo);
            }

            return ResultUtil.success(activePaths);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/difficulty/stats")
    public ResultUtil getDifficultyStats(HttpServletRequest request, @RequestParam(required = false) Long userId) {
        try {
            Long targetUserId = userId != null ? userId : (Long) request.getAttribute("userId");
            Map<String, Integer> difficultyStats = learnService.getDifficultyStats(targetUserId);
            return ResultUtil.success(difficultyStats);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }
}
