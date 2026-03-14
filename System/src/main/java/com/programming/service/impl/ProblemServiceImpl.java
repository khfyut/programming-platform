package com.programming.service.impl;

import com.programming.entity.Problem;
import com.programming.mapper.ProblemMapper;
import com.programming.service.ProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProblemServiceImpl implements ProblemService {

    @Autowired
    private ProblemMapper problemMapper;

    @Override
    public Map<String, Object> getProblemList(int page, int size, Integer difficulty, String language) {
        int offset = (page - 1) * size;
        List<Problem> list = problemMapper.findByPage(offset, size, difficulty, language);
        int total = problemMapper.count(difficulty, language);
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", list);
        
        return result;
    }

    @Override
    public Problem getProblemDetail(Long id) {
        return problemMapper.findById(id);
    }

    @Override
    public List<Problem> getProblemsByTag(String tag) {
        return problemMapper.findByTags(tag);
    }

    @Override
    public List<Problem> getProblemsByDifficulty(Integer difficulty) {
        return problemMapper.findByDifficulty(difficulty);
    }
}
