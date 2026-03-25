package com.programming.service.impl;

import com.programming.entity.*;
import com.programming.mapper.WrongBookMapper;
import com.programming.service.WrongBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class WrongBookServiceImpl implements WrongBookService {

    @Autowired
    private WrongBookMapper wrongBookMapper;

    @Override
    public WrongBook getUserWrongBook(Long userId) {
        WrongBook wrongBook = wrongBookMapper.selectWrongBookByUserId(userId);
        if (wrongBook == null) {
            wrongBook = new WrongBook();
            wrongBook.setUserId(userId);
            wrongBookMapper.insertWrongBook(wrongBook);
        }
        return wrongBook;
    }

    @Override
    public List<WrongBookItem> getWrongBookItems(Long userId, String knowledgePoint, Integer difficulty) {
        WrongBook wrongBook = getUserWrongBook(userId);
        return wrongBookMapper.selectWrongBookItems(wrongBook.getId(), knowledgePoint, difficulty);
    }

    @Override
    public WrongBookItem getWrongBookItemById(Long id) {
        return wrongBookMapper.selectWrongBookItemById(id);
    }

    @Override
    public void addWrongBookItem(Long userId, Long problemId, Long submitId, String code, String language, String errorMessage, String knowledgePoints) {
        WrongBook wrongBook = getUserWrongBook(userId);
        
        // 检查是否已经存在该题目
        WrongBookItem existingItem = wrongBookMapper.selectWrongBookItemByProblemId(wrongBook.getId(), problemId);
        if (existingItem == null) {
            WrongBookItem item = new WrongBookItem();
            item.setWrongBookId(wrongBook.getId());
            item.setProblemId(problemId);
            item.setSubmitId(submitId);
            item.setCode(code);
            item.setLanguage(language);
            item.setErrorMessage(errorMessage);
            item.setKnowledgePoints(knowledgePoints);
            item.setReviewStatus(0);
            item.setLastReviewTime(null);
            wrongBookMapper.insertWrongBookItem(item);
            
            // 自动生成复习计划
            createReviewPlan(userId, item.getId());
        }
    }

    @Override
    public void updateWrongBookItemStatus(Long id, Integer status) {
        WrongBookItem item = wrongBookMapper.selectWrongBookItemById(id);
        if (item != null) {
            item.setReviewStatus(status);
            item.setLastReviewTime(LocalDateTime.now());
            wrongBookMapper.updateWrongBookItem(item);
        }
    }

    @Override
    public void removeWrongBookItem(Long id) {
        // 删除对应的复习计划
        ReviewPlan plan = wrongBookMapper.selectReviewPlanByWrongItemId(id);
        if (plan != null) {
            wrongBookMapper.deleteReviewPlan(plan.getId());
        }
        // 删除错题项
        wrongBookMapper.deleteWrongBookItem(id);
    }

    @Override
    public List<ReviewPlan> getReviewPlans(Long userId) {
        return wrongBookMapper.selectReviewPlansByUserId(userId);
    }

    @Override
    public List<ReviewPlan> getPendingReviewPlans(Long userId) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return wrongBookMapper.selectPendingReviewPlans(userId, currentTime);
    }

    @Override
    public void createReviewPlan(Long userId, Long wrongItemId) {
        // 检查是否已经存在复习计划
        ReviewPlan existingPlan = wrongBookMapper.selectReviewPlanByWrongItemId(wrongItemId);
        if (existingPlan == null) {
            ReviewPlan plan = new ReviewPlan();
            plan.setUserId(userId);
            plan.setWrongItemId(wrongItemId);
            // 根据艾宾浩斯遗忘曲线设置首次复习时间
            plan.setNextReviewTime(LocalDateTime.now().plusDays(1)); // 1天后
            plan.setReviewCount(0);
            plan.setStatus(0);
            wrongBookMapper.insertReviewPlan(plan);
        }
    }

    @Override
    public void updateReviewPlan(Long id, boolean reviewed) {
        ReviewPlan plan = wrongBookMapper.selectReviewPlanByWrongItemId(id);
        if (plan != null) {
            if (reviewed) {
                plan.setReviewCount(plan.getReviewCount() + 1);
                // 根据复习次数调整下次复习时间
                long nextReviewInterval;
                switch (plan.getReviewCount()) {
                    case 1:
                        nextReviewInterval = 2 * 24 * 60 * 60 * 1000; // 2天后
                        break;
                    case 2:
                        nextReviewInterval = 4 * 24 * 60 * 60 * 1000; // 4天后
                        break;
                    case 3:
                        nextReviewInterval = 7 * 24 * 60 * 60 * 1000; // 7天后
                        break;
                    case 4:
                        nextReviewInterval = 15 * 24 * 60 * 60 * 1000; // 15天后
                        break;
                    default:
                        nextReviewInterval = 30 * 24 * 60 * 60 * 1000; // 30天后
                }
                plan.setNextReviewTime(LocalDateTime.now().plusDays(nextReviewInterval / (24 * 60 * 60 * 1000)));
            } else {
                // 未掌握，缩短复习间隔
                plan.setNextReviewTime(LocalDateTime.now().plusHours(12)); // 12小时后
            }
            wrongBookMapper.updateReviewPlan(plan);
        }
    }

    @Override
    public void deleteReviewPlan(Long id) {
        wrongBookMapper.deleteReviewPlan(id);
    }

    @Override
    public Map<String, Object> getWrongBookStatistics(Long userId) {
        return wrongBookMapper.getWrongBookStatistics(userId);
    }

    @Override
    public List<Map<String, Object>> getWrongDistributionByKnowledgePoint(Long userId) {
        return wrongBookMapper.getWrongDistributionByKnowledgePoint(userId);
    }

    @Override
    public List<Map<String, Object>> getWrongDistributionByDifficulty(Long userId) {
        return wrongBookMapper.getWrongDistributionByDifficulty(userId);
    }

    @Override
    public List<Problem> getRecommendedProblems(Long userId, Long wrongItemId, Integer limit) {
        WrongBookItem item = wrongBookMapper.selectWrongBookItemById(wrongItemId);
        if (item != null) {
            // 获取题目难度
            // 这里可以通过problem表查询难度，暂时假设为中等难度
            Integer difficulty = 1;
            return wrongBookMapper.selectRecommendedProblems(item.getKnowledgePoints(), difficulty, limit);
        }
        return new ArrayList<>();
    }

    @Override
    public List<Problem> getRecommendedProblemsByKnowledgePoint(String knowledgePoints, Integer difficulty, Integer limit) {
        return wrongBookMapper.selectRecommendedProblems(knowledgePoints, difficulty, limit);
    }

    @Override
    public void autoAddWrongItem(Long userId, Submit submit, Problem problem) {
        // 只处理非AC的提交
        if (submit.getResult() != 0) {
            String errorMessage = "";
            // 构建错误信息
            switch (submit.getResult()) {
                case 1:
                    errorMessage = "答案错误";
                    break;
                case 2:
                    errorMessage = "编译错误";
                    break;
                case 3:
                    errorMessage = "运行时错误";
                    break;
                case 4:
                    errorMessage = "超时";
                    break;
                default:
                    errorMessage = "未知错误";
            }
            
            addWrongBookItem(userId, submit.getProblemId(), submit.getId(), submit.getCode(), 
                submit.getLanguage(), errorMessage, problem.getKnowledgePoints());
        }
    }

    @Override
    public void autoGenerateReviewPlans(Long userId) {
        // 获取用户的错题本
        WrongBook wrongBook = getUserWrongBook(userId);
        // 获取所有未生成复习计划的错题项
        List<WrongBookItem> items = wrongBookMapper.selectWrongBookItems(wrongBook.getId(), null, null);
        for (WrongBookItem item : items) {
            ReviewPlan plan = wrongBookMapper.selectReviewPlanByWrongItemId(item.getId());
            if (plan == null) {
                createReviewPlan(userId, item.getId());
            }
        }
    }
}