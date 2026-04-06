package com.programming.service.impl;

import com.programming.entity.Problem;
import com.programming.entity.ReviewPlan;
import com.programming.entity.Submit;
import com.programming.entity.WrongBook;
import com.programming.entity.WrongBookItem;
import com.programming.mapper.WrongBookMapper;
import com.programming.service.WrongBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public WrongBookItem getWrongBookItemById(Long userId, Long id) {
        return requireOwnedWrongBookItem(userId, id);
    }

    @Override
    public void addWrongBookItem(Long userId, Long problemId, Long submitId, String code, String language,
                                 String errorMessage, String knowledgePoints) {
        WrongBook wrongBook = getUserWrongBook(userId);

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

            createReviewPlan(userId, item.getId());
        }
    }

    @Override
    public void updateWrongBookItemStatus(Long userId, Long id, Integer status) {
        WrongBookItem item = requireOwnedWrongBookItem(userId, id);
        item.setReviewStatus(status);
        item.setLastReviewTime(LocalDateTime.now());
        wrongBookMapper.updateWrongBookItem(item);
    }

    @Override
    public void removeWrongBookItem(Long userId, Long id) {
        requireOwnedWrongBookItem(userId, id);
        ReviewPlan plan = wrongBookMapper.selectReviewPlanByWrongItemId(id);
        if (plan != null) {
            wrongBookMapper.deleteReviewPlan(plan.getId());
        }
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
        ReviewPlan existingPlan = wrongBookMapper.selectReviewPlanByWrongItemId(wrongItemId);
        if (existingPlan == null) {
            ReviewPlan plan = new ReviewPlan();
            plan.setUserId(userId);
            plan.setWrongItemId(wrongItemId);
            plan.setNextReviewTime(LocalDateTime.now().plusDays(1));
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
                long nextReviewInterval;
                switch (plan.getReviewCount()) {
                    case 1:
                        nextReviewInterval = 2L * 24 * 60 * 60 * 1000;
                        break;
                    case 2:
                        nextReviewInterval = 4L * 24 * 60 * 60 * 1000;
                        break;
                    case 3:
                        nextReviewInterval = 7L * 24 * 60 * 60 * 1000;
                        break;
                    case 4:
                        nextReviewInterval = 15L * 24 * 60 * 60 * 1000;
                        break;
                    default:
                        nextReviewInterval = 30L * 24 * 60 * 60 * 1000;
                }
                plan.setNextReviewTime(LocalDateTime.now().plusDays(nextReviewInterval / (24 * 60 * 60 * 1000)));
            } else {
                plan.setNextReviewTime(LocalDateTime.now().plusHours(12));
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
        WrongBookItem item = requireOwnedWrongBookItem(userId, wrongItemId);
        Integer difficulty = 1;
        return wrongBookMapper.selectRecommendedProblems(item.getKnowledgePoints(), difficulty, limit);
    }

    @Override
    public List<Problem> getRecommendedProblemsByKnowledgePoint(String knowledgePoints, Integer difficulty, Integer limit) {
        return wrongBookMapper.selectRecommendedProblems(knowledgePoints, difficulty, limit);
    }

    @Override
    public void autoAddWrongItem(Long userId, Submit submit, Problem problem) {
        if (submit.getResult() != 0) {
            String errorMessage;
            switch (submit.getResult()) {
                case 1:
                    errorMessage = "Wrong answer";
                    break;
                case 2:
                    errorMessage = "Compile error";
                    break;
                case 3:
                    errorMessage = "Runtime error";
                    break;
                case 4:
                    errorMessage = "Time limit exceeded";
                    break;
                default:
                    errorMessage = "Unknown error";
            }

            addWrongBookItem(
                    userId,
                    submit.getProblemId(),
                    submit.getId(),
                    submit.getCode(),
                    submit.getLanguage(),
                    errorMessage,
                    problem.getKnowledgePoints()
            );
        }
    }

    @Override
    public void autoGenerateReviewPlans(Long userId) {
        WrongBook wrongBook = getUserWrongBook(userId);
        List<WrongBookItem> items = wrongBookMapper.selectWrongBookItems(wrongBook.getId(), null, null);
        for (WrongBookItem item : items) {
            ReviewPlan plan = wrongBookMapper.selectReviewPlanByWrongItemId(item.getId());
            if (plan == null) {
                createReviewPlan(userId, item.getId());
            }
        }
    }

    private WrongBookItem requireOwnedWrongBookItem(Long userId, Long wrongItemId) {
        WrongBookItem item = wrongBookMapper.selectWrongBookItemById(wrongItemId);
        WrongBook wrongBook = wrongBookMapper.selectWrongBookByUserId(userId);
        if (item == null || wrongBook == null || !wrongBook.getId().equals(item.getWrongBookId())) {
            throw new RuntimeException("Wrong book item not found");
        }
        return item;
    }
}
