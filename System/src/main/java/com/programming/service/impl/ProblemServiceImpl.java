package com.programming.service.impl;

import com.programming.dto.ProblemListDTO;
import com.programming.entity.Problem;
import com.programming.mapper.ProblemMapper;
import com.programming.mapper.SubmitMapper;
import com.programming.mapper.FavoriteMapper;
import com.programming.service.ProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProblemServiceImpl implements ProblemService {

    @Autowired
    private ProblemMapper problemMapper;

    @Autowired
    private SubmitMapper submitMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Override
    public Map<String, Object> getProblemList(int page, int size, Integer difficulty, String language, String knowledge, Long userId) {
        int offset = (page - 1) * size;
        List<Problem> list = problemMapper.findByPage(offset, size, difficulty, language, knowledge);
        int total = problemMapper.count(difficulty, language, knowledge);

        // 获取用户已解决的题目ID
        Set<Long> solvedProblemIds = new HashSet<>();
        Set<Long> attemptedProblemIds = new HashSet<>();
        Set<Long> favoritedProblemIds = new HashSet<>();

        if (userId != null) {
            List<Long> solved = submitMapper.findPassedProblemIdsByUserId(userId);
            List<Long> attempted = submitMapper.findAttemptedProblemIdsByUserId(userId);
            List<Long> favorites = favoriteMapper.findProblemIdsByUserId(userId);
            if (solved != null) solvedProblemIds.addAll(solved);
            if (attempted != null) attemptedProblemIds.addAll(attempted);
            if (favorites != null) favoritedProblemIds.addAll(favorites);
        }

        // 转换为DTO并填充状态
        List<ProblemListDTO> dtoList = new ArrayList<>();
        for (Problem problem : list) {
            ProblemListDTO dto = new ProblemListDTO();
            dto.setId(problem.getId());
            dto.setTitle(problem.getTitle());
            dto.setDifficulty(problem.getDifficulty());
            dto.setLanguage(problem.getLanguage());
            dto.setTags(problem.getTags());
            dto.setKnowledgePoints(problem.getKnowledgePoints());
            dto.setCreateTime(problem.getCreateTime());

            // 设置用户状态
            dto.setIsSolved(solvedProblemIds.contains(problem.getId()));
            dto.setIsAttempted(attemptedProblemIds.contains(problem.getId()) && !solvedProblemIds.contains(problem.getId()));
            dto.setIsFavorited(favoritedProblemIds.contains(problem.getId()));

            // 计算通过率
            Map<String, Object> stats = submitMapper.countByProblemId(problem.getId());
            Long totalCount = stats != null && stats.get("totalCount") != null ?
                    ((Number) stats.get("totalCount")).longValue() : 0L;
            Long passedCount = stats != null && stats.get("passedCount") != null ?
                    ((Number) stats.get("passedCount")).longValue() : 0L;

            dto.setTotalSubmissions(totalCount.intValue());
            dto.setPassedSubmissions(passedCount.intValue());

            if (totalCount > 0) {
                dto.setPassRate((int) ((passedCount * 100) / totalCount));
            } else {
                dto.setPassRate(0);
            }

            dtoList.add(dto);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", dtoList);
        result.put("solvedCount", solvedProblemIds.size());

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

    @Override
    public List<Map<String, Object>> getLanguages() {
        List<Map<String, Object>> languages = new ArrayList<>();

        // 语言配置映射
        Map<String, Map<String, Object>> languageConfig = new HashMap<>();
        languageConfig.put("algorithm", Map.of("label", "算法", "icon", "Cpu", "color", "#F59E0B"));
        languageConfig.put("database", Map.of("label", "数据库", "icon", "DataLine", "color", "#3B82F6"));
        languageConfig.put("java", Map.of("label", "Java", "icon", "Document", "color", "#EA2D2E"));
        languageConfig.put("python", Map.of("label", "Python", "icon", "Coin", "color", "#3776AB"));

        // 从数据库获取语言统计
        List<Map<String, Object>> languageStats = problemMapper.getLanguageStats();

        // 构建语言列表
        for (Map<String, Object> stat : languageStats) {
            String language = (String) stat.get("language");
            Integer problemCount = ((Number) stat.get("problemCount")).intValue();

            Map<String, Object> languageItem = new HashMap<>();
            languageItem.put("value", language);
            languageItem.put("problemCount", problemCount);

            // 添加配置信息
            if (languageConfig.containsKey(language)) {
                languageItem.putAll(languageConfig.get(language));
            } else {
                languageItem.put("label", language);
                languageItem.put("icon", "Menu");
                languageItem.put("color", "#6B7280");
            }

            languages.add(languageItem);
        }

        // 按题目数量排序
        languages.sort((a, b) -> {
            int countA = (Integer) a.get("problemCount");
            int countB = (Integer) b.get("problemCount");
            return Integer.compare(countB, countA);
        });

        return languages;
    }
}
