package com.programming.service.impl;

import com.programming.entity.Problem;
import com.programming.entity.Submit;
import com.programming.entity.User;
import com.programming.mapper.*;
import com.programming.service.AdminService;
import com.programming.vo.BatchImportProblemsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProblemMapper problemMapper;

    @Autowired
    private SubmitMapper submitMapper;

    @Autowired
    private LearnRecordMapper learnRecordMapper;

    @Override
    public Map<String, Object> getUserList(int page, int size) {
        int offset = (page - 1) * size;
        List<User> users = userMapper.findByPage(offset, size);
        int total = userMapper.count();

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", users);

        return result;
    }

    @Override
    public void addProblem(Map<String, Object> params) {
        Problem problem = new Problem();
        problem.setTitle((String) params.get("title"));
        problem.setContent((String) params.get("content"));
        problem.setInput((String) params.get("input"));
        problem.setOutput((String) params.get("output"));
        problem.setDifficulty(((Number) params.get("difficulty")).intValue());
        problem.setLanguage((String) params.get("language"));
        
        if (params.get("timeLimit") != null) {
            problem.setTimeLimit(((Number) params.get("timeLimit")).intValue());
        }
        if (params.get("memoryLimit") != null) {
            problem.setMemoryLimit(((Number) params.get("memoryLimit")).intValue());
        }
        if (params.get("tags") != null) {
            problem.setTags((String) params.get("tags"));
        }
        if (params.get("knowledgePoints") != null) {
            problem.setKnowledgePoints((String) params.get("knowledgePoints"));
        }
        if (params.get("hints") != null) {
            problem.setHints((String) params.get("hints"));
        }
        if (params.get("sampleExplanation") != null) {
            problem.setSampleExplanation((String) params.get("sampleExplanation"));
        }
        
        problemMapper.insert(problem);
    }

    @Override
    @Transactional
    public void addProblemWithCheck(Map<String, Object> params) {
        String title = (String) params.get("title");
        
        Problem existingProblem = problemMapper.findByTitle(title);
        if (existingProblem != null) {
            throw new RuntimeException("题目已存在：" + title);
        }
        
        Problem problem = new Problem();
        problem.setTitle(title);
        problem.setContent((String) params.get("content"));
        problem.setInput((String) params.get("input"));
        problem.setOutput((String) params.get("output"));
        problem.setDifficulty(((Number) params.get("difficulty")).intValue());
        problem.setLanguage((String) params.get("language"));
        
        if (params.get("timeLimit") != null) {
            problem.setTimeLimit(((Number) params.get("timeLimit")).intValue());
        }
        if (params.get("memoryLimit") != null) {
            problem.setMemoryLimit(((Number) params.get("memoryLimit")).intValue());
        }
        if (params.get("tags") != null) {
            problem.setTags((String) params.get("tags"));
        }
        if (params.get("knowledgePoints") != null) {
            problem.setKnowledgePoints((String) params.get("knowledgePoints"));
        }
        if (params.get("hints") != null) {
            problem.setHints((String) params.get("hints"));
        }
        if (params.get("sampleExplanation") != null) {
            problem.setSampleExplanation((String) params.get("sampleExplanation"));
        }
        
        problemMapper.insert(problem);
        
        log.info("添加题目成功：{}", title);
    }

    @Override
    public void updateProblem(Map<String, Object> params) {
        Problem problem = new Problem();
        problem.setId(((Number) params.get("id")).longValue());
        problem.setTitle((String) params.get("title"));
        problem.setContent((String) params.get("content"));
        problem.setInput((String) params.get("input"));
        problem.setOutput((String) params.get("output"));
        problem.setDifficulty(((Number) params.get("difficulty")).intValue());
        problem.setLanguage((String) params.get("language"));
        
        if (params.get("timeLimit") != null) {
            problem.setTimeLimit(((Number) params.get("timeLimit")).intValue());
        }
        if (params.get("memoryLimit") != null) {
            problem.setMemoryLimit(((Number) params.get("memoryLimit")).intValue());
        }
        if (params.get("tags") != null) {
            problem.setTags((String) params.get("tags"));
        }
        if (params.get("knowledgePoints") != null) {
            problem.setKnowledgePoints((String) params.get("knowledgePoints"));
        }
        if (params.get("hints") != null) {
            problem.setHints((String) params.get("hints"));
        }
        if (params.get("sampleExplanation") != null) {
            problem.setSampleExplanation((String) params.get("sampleExplanation"));
        }
        
        problemMapper.update(problem);
    }

    @Override
    public void deleteProblem(Long id) {
        problemMapper.deleteById(id);
    }

    @Override
    public Map<String, Object> getSubmitList(int page, int size) {
        int offset = (page - 1) * size;
        List<Submit> submits = submitMapper.findAll(offset, size);
        int total = submitMapper.countAll();

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", submits);

        return result;
    }

    @Override
    public Map<String, Object> getStatistics() {
        int userCount = userMapper.count();
        int problemCount = problemMapper.count(null, null);
        int submitCount = submitMapper.countAll();
        
        List<Submit> allSubmits = submitMapper.findAll(0, submitCount);
        int correctCount = 0;
        for (Submit submit : allSubmits) {
            if (submit.getResult() == 0) {
                correctCount++;
            }
        }
        
        String correctRate = "0.00%";
        if (submitCount > 0) {
            correctRate = String.format("%.2f%%", (double) correctCount / submitCount * 100);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userCount", userCount);
        result.put("problemCount", problemCount);
        result.put("submitCount", submitCount);
        result.put("correctCount", correctCount);
        result.put("correctRate", correctRate);

        return result;
    }

    @Override
    @Transactional
    public void batchImportProblems(BatchImportProblemsVO vo) {
        if (vo.getProblems() == null || vo.getProblems().isEmpty()) {
            throw new RuntimeException("题目列表不能为空");
        }

        int successCount = 0;
        int skipCount = 0;

        for (BatchImportProblemsVO.ProblemItem item : vo.getProblems()) {
            Problem existingProblem = problemMapper.findByTitle(item.getTitle());
            if (existingProblem != null) {
                log.warn("题目已存在，跳过：{}", item.getTitle());
                skipCount++;
                continue;
            }

            Problem problem = new Problem();
            problem.setTitle(item.getTitle());
            problem.setContent(item.getContent());
            problem.setInput(item.getInput());
            problem.setOutput(item.getOutput());
            problem.setDifficulty(item.getDifficulty());
            problem.setLanguage(item.getLanguage());
            
            if (item.getTimeLimit() != null) {
                problem.setTimeLimit(item.getTimeLimit());
            }
            if (item.getMemoryLimit() != null) {
                problem.setMemoryLimit(item.getMemoryLimit());
            }
            if (item.getTags() != null) {
                problem.setTags(item.getTags());
            }
            if (item.getKnowledgePoints() != null) {
                problem.setKnowledgePoints(item.getKnowledgePoints());
            }
            if (item.getHints() != null) {
                problem.setHints(item.getHints());
            }
            if (item.getSampleExplanation() != null) {
                problem.setSampleExplanation(item.getSampleExplanation());
            }
            
            problemMapper.insert(problem);
            successCount++;
        }

        log.info("批量导入完成，成功{}道，跳过{}道", successCount, skipCount);
    }
}