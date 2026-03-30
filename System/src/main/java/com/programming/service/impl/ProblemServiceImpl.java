package com.programming.service.impl;

import com.programming.dto.ProblemListDTO;
import com.programming.entity.Problem;
import com.programming.mapper.FavoriteMapper;
import com.programming.mapper.ProblemMapper;
import com.programming.mapper.SubmitMapper;
import com.programming.service.ProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        List<Problem> problems = problemMapper.findByPage(offset, size, difficulty, language, knowledge);
        int total = problemMapper.count(difficulty, language, knowledge);

        Set<Long> solvedProblemIds = new HashSet<>();
        Set<Long> attemptedProblemIds = new HashSet<>();
        Set<Long> favoritedProblemIds = new HashSet<>();
        if (userId != null) {
            fillUserProblemStates(userId, solvedProblemIds, attemptedProblemIds, favoritedProblemIds);
        }

        Map<Long, ProblemStats> problemStatsMap = loadProblemStats(problems);
        List<ProblemListDTO> dtoList = buildProblemList(problems, solvedProblemIds, attemptedProblemIds, favoritedProblemIds, problemStatsMap);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", dtoList);
        result.put("solvedCount", solvedProblemIds.size());
        return result;
    }

    private void fillUserProblemStates(Long userId, Set<Long> solvedProblemIds, Set<Long> attemptedProblemIds, Set<Long> favoritedProblemIds) {
        List<Long> solved = submitMapper.findPassedProblemIdsByUserId(userId);
        List<Long> attempted = submitMapper.findAttemptedProblemIdsByUserId(userId);
        List<Long> favorites = favoriteMapper.findProblemIdsByUserId(userId);

        if (solved != null) {
            solvedProblemIds.addAll(solved);
        }
        if (attempted != null) {
            attemptedProblemIds.addAll(attempted);
        }
        if (favorites != null) {
            favoritedProblemIds.addAll(favorites);
        }
    }

    private Map<Long, ProblemStats> loadProblemStats(List<Problem> problems) {
        Map<Long, ProblemStats> statsMap = new HashMap<>();
        if (problems == null || problems.isEmpty()) {
            return statsMap;
        }

        List<Long> problemIds = problems.stream()
                .map(Problem::getId)
                .collect(Collectors.toList());
        List<Map<String, Object>> rawStats = submitMapper.countByProblemIds(problemIds);
        if (rawStats == null) {
            return statsMap;
        }

        for (Map<String, Object> rawStat : rawStats) {
            Long problemId = rawStat.get("problemId") == null ? null : ((Number) rawStat.get("problemId")).longValue();
            if (problemId == null) {
                continue;
            }

            int totalCount = rawStat.get("totalCount") == null ? 0 : ((Number) rawStat.get("totalCount")).intValue();
            int passedCount = rawStat.get("passedCount") == null ? 0 : ((Number) rawStat.get("passedCount")).intValue();
            statsMap.put(problemId, new ProblemStats(totalCount, passedCount));
        }
        return statsMap;
    }

    private List<ProblemListDTO> buildProblemList(List<Problem> problems, Set<Long> solvedProblemIds,
                                                  Set<Long> attemptedProblemIds, Set<Long> favoritedProblemIds,
                                                  Map<Long, ProblemStats> problemStatsMap) {
        List<ProblemListDTO> dtoList = new ArrayList<>();
        for (Problem problem : problems) {
            ProblemListDTO dto = new ProblemListDTO();
            dto.setId(problem.getId());
            dto.setTitle(problem.getTitle());
            dto.setDifficulty(problem.getDifficulty());
            dto.setLanguage(problem.getLanguage());
            dto.setTags(problem.getTags());
            dto.setKnowledgePoints(problem.getKnowledgePoints());
            dto.setCreateTime(problem.getCreateTime());

            dto.setIsSolved(solvedProblemIds.contains(problem.getId()));
            dto.setIsAttempted(attemptedProblemIds.contains(problem.getId()) && !solvedProblemIds.contains(problem.getId()));
            dto.setIsFavorited(favoritedProblemIds.contains(problem.getId()));

            ProblemStats stats = problemStatsMap.getOrDefault(problem.getId(), ProblemStats.EMPTY);
            dto.setTotalSubmissions(stats.totalCount());
            dto.setPassedSubmissions(stats.passedCount());
            dto.setPassRate(stats.totalCount() > 0 ? (stats.passedCount() * 100) / stats.totalCount() : 0);
            dtoList.add(dto);
        }
        return dtoList;
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

        Map<String, Map<String, Object>> languageConfig = new HashMap<>();
        languageConfig.put("algorithm", Map.of("label", "算法", "icon", "Cpu", "color", "#F59E0B"));
        languageConfig.put("database", Map.of("label", "数据库", "icon", "DataLine", "color", "#3B82F6"));
        languageConfig.put("java", Map.of("label", "Java", "icon", "Document", "color", "#EA2D2E"));
        languageConfig.put("python", Map.of("label", "Python", "icon", "Coin", "color", "#3776AB"));

        List<Map<String, Object>> languageStats = problemMapper.getLanguageStats();
        for (Map<String, Object> stat : languageStats) {
            String language = (String) stat.get("language");
            Integer problemCount = ((Number) stat.get("problemCount")).intValue();

            Map<String, Object> languageItem = new HashMap<>();
            languageItem.put("value", language);
            languageItem.put("problemCount", problemCount);

            if (languageConfig.containsKey(language)) {
                languageItem.putAll(languageConfig.get(language));
            } else {
                languageItem.put("label", language);
                languageItem.put("icon", "Menu");
                languageItem.put("color", "#6B7280");
            }
            languages.add(languageItem);
        }

        languages.sort((a, b) -> {
            int countA = (Integer) a.get("problemCount");
            int countB = (Integer) b.get("problemCount");
            return Integer.compare(countB, countA);
        });
        return languages;
    }

    private record ProblemStats(int totalCount, int passedCount) {
        private static final ProblemStats EMPTY = new ProblemStats(0, 0);
    }
}
