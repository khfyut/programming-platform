package com.programming.service.impl;

import com.programming.entity.ReferenceSolution;
import com.programming.entity.UserBehavior;
import com.programming.mapper.ReferenceSolutionMapper;
import com.programming.mapper.UserBehaviorMapper;
import com.programming.service.ReferenceSolutionService;
import com.programming.vo.ReferenceSolutionVO;
import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ReferenceSolutionServiceImpl implements ReferenceSolutionService {

    @Autowired
    private ReferenceSolutionMapper referenceSolutionMapper;

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    @Override
    public PermissionCheckResult checkViewPermission(Long userId, Long problemId) {
        // 检查用户是否有提交记录
        Integer submitCount = userBehaviorMapper.countSubmitRecords(userId, problemId);
        if (submitCount == null || submitCount == 0) {
            return new PermissionCheckResult(false, "请先尝试提交代码后再查看参考答案");
        }
        return new PermissionCheckResult(true, "");
    }

    @Override
    public ReferenceSolutionVO getSolution(Long problemId, String language) {
        // 获取参考答案
        ReferenceSolution solution = null;
        
        // 如果指定了语言，先尝试获取该语言的答案
        if (language != null && !language.isEmpty()) {
            solution = referenceSolutionMapper.findByProblemIdAndLanguage(problemId, language);
        }
        
        // 如果指定语言不存在或未指定语言，获取第一个可用的语言
        if (solution == null) {
            List<String> languages = referenceSolutionMapper.findLanguagesByProblemId(problemId);
            if (languages != null && !languages.isEmpty()) {
                solution = referenceSolutionMapper.findByProblemIdAndLanguage(problemId, languages.get(0));
            }
        }

        if (solution == null) {
            return null;
        }

        // 转换为VO
        ReferenceSolutionVO vo = new ReferenceSolutionVO();
        vo.setProblemId(solution.getProblemId());
        vo.setLanguage(solution.getLanguage());
        vo.setSolutionCode(solution.getSolutionCode());
        vo.setTimeComplexity(solution.getTimeComplexity());
        vo.setSpaceComplexity(solution.getSpaceComplexity());
        vo.setExplanation(solution.getExplanation());

        // 解析hints JSON
        if (solution.getHints() != null) {
            try {
                Map<String, String> hints = JSON.parseObject(solution.getHints(), Map.class);
                vo.setHints(hints);
            } catch (Exception e) {
                // 解析失败，设置为空
                vo.setHints(null);
            }
        }

        // 获取可用语言列表
        List<String> languages = referenceSolutionMapper.findLanguagesByProblemId(problemId);
        if (languages != null) {
            vo.setAvailableLanguages(languages.toArray(new String[0]));
        }

        return vo;
    }

    @Override
    public void recordViewBehavior(Long userId, Long problemId) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setBehaviorType("VIEW_REFERENCE_SOLUTION");
        behavior.setTargetType("PROBLEM");
        behavior.setTargetId(problemId);
        behavior.setDetails("{}");
        userBehaviorMapper.insert(behavior);
    }

    @Override
    public String getHint(Long problemId, Integer hintLevel) {
        // 获取第一个可用的参考答案
        List<String> languages = referenceSolutionMapper.findLanguagesByProblemId(problemId);
        if (languages == null || languages.isEmpty()) {
            return "暂无提示";
        }

        ReferenceSolution solution = referenceSolutionMapper.findByProblemIdAndLanguage(problemId, languages.get(0));
        if (solution == null || solution.getHints() == null) {
            return "暂无提示";
        }

        try {
            Map<String, String> hints = JSON.parseObject(solution.getHints(), Map.class);
            return hints.getOrDefault(hintLevel.toString(), "暂无提示");
        } catch (Exception e) {
            return "暂无提示";
        }
    }

    @Override
    public String[] getAvailableLanguages(Long problemId) {
        List<String> languages = referenceSolutionMapper.findLanguagesByProblemId(problemId);
        if (languages == null || languages.isEmpty()) {
            return new String[0];
        }
        return languages.toArray(new String[0]);
    }
}