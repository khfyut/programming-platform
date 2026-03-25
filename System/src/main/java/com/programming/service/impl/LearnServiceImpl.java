package com.programming.service.impl;

import com.programming.entity.*;
import com.programming.mapper.*;
import com.programming.service.LearnService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class LearnServiceImpl implements LearnService {

    @Autowired
    private LearnRecordMapper learnRecordMapper;

    @Autowired
    private SubmitMapper submitMapper;

    @Autowired
    private ProblemMapper problemMapper;

    @Autowired
    private LearningPathMapper learningPathMapper;

    @Autowired
    private KnowledgeMasteryMapper knowledgeMasteryMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Map<String, Object> getStatistics(Long userId) {
        LearnRecord record = learnRecordMapper.findByUserId(userId);
        
        int solved = 0;
        int submitted = 0;
        int accuracy = 0;
        int streak = 0;
        
        Map<String, Object> difficultyStats = new HashMap<>();
        difficultyStats.put("easy", 0);
        difficultyStats.put("medium", 0);
        difficultyStats.put("hard", 0);
        
        if (record != null) {
            solved = record.getCorrectCount() != null ? record.getCorrectCount() : 0;
            submitted = record.getProblemCount() != null ? record.getProblemCount() : 0;
        }
        
        if (submitted > 0 && solved > 0) {
            accuracy = (int) Math.round((double) solved / submitted * 100);
        }
        
        int totalCount = submitMapper.countByUserId(userId, null);
        List<Submit> allSubmits = submitMapper.findByUserId(userId, null, 0, totalCount > 0 ? totalCount : 1);
        
        for (Submit submit : allSubmits) {
            if (submit.getProblemId() != null) {
                Problem problem = problemMapper.findById(submit.getProblemId());
                if (problem != null && submit.getResult() != null && submit.getResult() == 0) {
                    if (problem.getDifficulty() != null) {
                        if (problem.getDifficulty() == 0) {
                            difficultyStats.put("easy", (int) difficultyStats.get("easy") + 1);
                        } else if (problem.getDifficulty() == 1) {
                            difficultyStats.put("medium", (int) difficultyStats.get("medium") + 1);
                        } else if (problem.getDifficulty() == 2) {
                            difficultyStats.put("hard", (int) difficultyStats.get("hard") + 1);
                        }
                    }
                }
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("solved", solved);
        stats.put("submitted", submitted);
        stats.put("accuracy", accuracy);
        stats.put("streak", streak);

        Map<String, Object> result = new HashMap<>();
        result.put("stats", stats);
        result.put("difficultyStats", difficultyStats);

        return result;
    }

    @Override
    public Map<String, Object> getRecommend(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 获取用户学习记录
        LearnRecord record = learnRecordMapper.findByUserId(userId);
        
        // 2. 分析用户薄弱环节
        List<KnowledgePoint> weakPoints = getWeakKnowledgePoints(userId);
        
        // 3. 基于用户历史推荐题目
        List<Problem> recommendedProblems = new ArrayList<>();
        
        // 4. 基于用户能力水平推荐学习模块
        List<Map<String, Object>> recommendedModules = new ArrayList<>();
        
        // 5. 分析用户提交历史
        List<Submit> submits = submitMapper.findByUserId(userId, null, 0, 20);
        
        // 6. 确定用户偏好的编程语言
        String preferredLanguage = null;
        if (!submits.isEmpty()) {
            // 统计用户使用最多的语言
            Map<String, Integer> languageCount = new HashMap<>();
            for (Submit submit : submits) {
                if (submit.getLanguage() != null) {
                    languageCount.put(submit.getLanguage(), languageCount.getOrDefault(submit.getLanguage(), 0) + 1);
                }
            }
            
            // 找出使用最多的语言
            int maxCount = 0;
            for (Map.Entry<String, Integer> entry : languageCount.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    preferredLanguage = entry.getKey();
                }
            }
        }
        
        // 7. 推荐题目
        if (weakPoints.isEmpty()) {
            // 如果没有薄弱环节，推荐基础题目
            if (preferredLanguage != null) {
                recommendedProblems = problemMapper.findByPage(0, 5, 0, preferredLanguage, null);
            } else {
                recommendedProblems = problemMapper.findByPage(0, 5, 0, null, null);
            }
        } else {
            // 根据薄弱知识点推荐相关题目
            for (KnowledgePoint point : weakPoints) {
                List<Problem> problems = problemMapper.findByKnowledgePoint(point.getId(), 3);
                recommendedProblems.addAll(problems);
                if (recommendedProblems.size() >= 5) {
                    break;
                }
            }
            
            // 如果推荐题目不足，补充基础题目
            if (recommendedProblems.size() < 5) {
                int remaining = 5 - recommendedProblems.size();
                if (preferredLanguage != null) {
                    List<Problem> additionalProblems = problemMapper.findByPage(0, remaining, 0, preferredLanguage, null);
                    recommendedProblems.addAll(additionalProblems);
                } else {
                    List<Problem> additionalProblems = problemMapper.findByPage(0, remaining, 0, null, null);
                    recommendedProblems.addAll(additionalProblems);
                }
            }
        }
        
        // 8. 推荐学习模块
        List<LearningPath> paths = learningPathMapper.selectAllPaths();
        for (LearningPath path : paths) {
            if (preferredLanguage != null && path.getLanguage() != null && 
                path.getLanguage().toLowerCase().contains(preferredLanguage.toLowerCase())) {
                Map<String, Object> module = new HashMap<>();
                module.put("pathId", path.getId());
                module.put("pathName", path.getName());
                module.put("description", path.getDescription());
                module.put("language", path.getLanguage());
                module.put("direction", path.getDirection());
                recommendedModules.add(module);
                if (recommendedModules.size() >= 3) {
                    break;
                }
            }
        }
        
        // 9. 添加学习建议
        List<String> suggestions = new ArrayList<>();
        if (!weakPoints.isEmpty()) {
            suggestions.add("建议加强以下知识点的学习：" + weakPoints.stream().map(KnowledgePoint::getName).reduce((a, b) -> a + ", " + b).orElse(""));
        }
        suggestions.add("建议每天坚持练习编程题目，保持学习节奏");
        suggestions.add("尝试解决不同类型的问题，扩展编程思维");
        
        result.put("problems", recommendedProblems);
        result.put("modules", recommendedModules);
        result.put("suggestions", suggestions);
        result.put("weakPoints", weakPoints);
        result.put("preferredLanguage", preferredLanguage);
        
        return result;
    }

    // 个性化自适应学习路径
    @Override
    public List<AssessmentQuestion> getAssessmentQuestions() {
        // 这里需要实现获取测评题目的逻辑
        // 暂时返回空列表，实际项目中需要根据具体需求实现
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public Map<String, Object> submitAssessment(Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        List<Map<String, Object>> answers = (List<Map<String, Object>>) params.get("answers");
        
        // 这里需要实现测评结果处理和学习路径生成逻辑
        // 暂时返回空数据，实际项目中需要根据具体需求实现
        Map<String, Object> result = new HashMap<>();
        result.put("pathId", 1L);
        result.put("recommendedPath", "基础入门路径");
        return result;
    }

    @Override
    public LearningPath getLearningPath(Long userId) {
        // 这里需要实现获取用户学习路径的逻辑
        // 暂时返回空数据，实际项目中需要根据具体需求实现
        return learningPathMapper.selectPathById(1L);
    }

    @Override
    @Transactional
    public Map<String, Object> unlockLevel(Long userId, Long levelId) {
        // 这里需要实现解锁关卡的逻辑
        // 暂时返回空数据，实际项目中需要根据具体需求实现
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "关卡解锁成功");
        return result;
    }

    @Override
    public Map<String, Object> getPathProgress(Long userId) {
        // 这里需要实现获取学习路径进度的逻辑
        // 暂时返回空数据，实际项目中需要根据具体需求实现
        Map<String, Object> result = new HashMap<>();
        result.put("completedLevels", 2);
        result.put("totalLevels", 10);
        result.put("progress", 20);
        return result;
    }

    // 知识点掌握度与学情分析
    @Override
    public Map<String, Object> getKnowledgeGraph() {
        // 这里需要实现获取知识图谱的逻辑
        // 暂时返回空数据，实际项目中需要根据具体需求实现
        Map<String, Object> result = new HashMap<>();
        result.put("nodes", new ArrayList<>());
        result.put("edges", new ArrayList<>());
        return result;
    }

    @Override
    public List<UserKnowledgeMastery> getKnowledgeMastery(Long userId) {
        return knowledgeMasteryMapper.selectUserKnowledgeMasteriesByUserId(userId);
    }

    @Override
    public Map<String, Object> getWeeklyReport(Long userId) {
        // 这里需要实现获取周学习报告的逻辑
        // 暂时返回空数据，实际项目中需要根据具体需求实现
        Map<String, Object> result = new HashMap<>();
        result.put("solvedProblems", 10);
        result.put("accuracy", 85);
        result.put("studyTime", "5小时30分钟");
        result.put("weakPoints", new ArrayList<>());
        return result;
    }

    @Override
    public List<KnowledgePoint> getWeakKnowledgePoints(Long userId) {
        // 这里需要实现获取薄弱知识点的逻辑
        // 暂时返回空列表，实际项目中需要根据具体需求实现
        return new ArrayList<>();
    }
    
    @Override
    public Map<String, Integer> getDifficultyStats(Long userId) {
        Map<String, Integer> difficultyStats = new HashMap<>();
        difficultyStats.put("easy", 0);
        difficultyStats.put("medium", 0);
        difficultyStats.put("hard", 0);
        
        int totalCount = submitMapper.countByUserId(userId, null);
        List<Submit> allSubmits = submitMapper.findByUserId(userId, null, 0, totalCount > 0 ? totalCount : 1);
        
        for (Submit submit : allSubmits) {
            if (submit.getProblemId() != null) {
                Problem problem = problemMapper.findById(submit.getProblemId());
                if (problem != null && submit.getResult() != null && submit.getResult() == 0) {
                    if (problem.getDifficulty() != null) {
                        if (problem.getDifficulty() == 0) {
                            difficultyStats.put("easy", difficultyStats.get("easy") + 1);
                        } else if (problem.getDifficulty() == 1) {
                            difficultyStats.put("medium", difficultyStats.get("medium") + 1);
                        } else if (problem.getDifficulty() == 2) {
                            difficultyStats.put("hard", difficultyStats.get("hard") + 1);
                        }
                    }
                }
            }
        }
        
        return difficultyStats;
    }
}