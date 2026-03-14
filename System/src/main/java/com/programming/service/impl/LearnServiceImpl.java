package com.programming.service.impl;

import com.programming.entity.LearnRecord;
import com.programming.entity.Problem;
import com.programming.entity.Submit;
import com.programming.mapper.LearnRecordMapper;
import com.programming.mapper.ProblemMapper;
import com.programming.mapper.SubmitMapper;
import com.programming.service.LearnService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
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
    public List<Problem> getRecommend(Long userId) {
        LearnRecord record = learnRecordMapper.findByUserId(userId);
        
        if (record == null || record.getLastProblemId() == null) {
            return problemMapper.findByPage(0, 5, 0, null);
        }

        List<Submit> submits = submitMapper.findByUserId(userId, null, 0, 10);
        if (submits.isEmpty()) {
            return problemMapper.findByPage(0, 5, 0, null);
        }

        String language = submits.get(0).getLanguage();
        return problemMapper.findByPage(0, 5, 0, language);
    }
}