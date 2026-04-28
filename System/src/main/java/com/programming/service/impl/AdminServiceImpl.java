package com.programming.service.impl;

import com.alibaba.fastjson2.JSON;
import com.programming.entity.*;
import com.programming.mapper.*;
import com.programming.service.AdminService;
import com.programming.service.runtime.RuntimeCatalogService;
import com.programming.vo.BatchImportProblemsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProblemMapper problemMapper;

    @Autowired
    private ProblemSupportedLanguageMapper problemSupportedLanguageMapper;

    @Autowired
    private ReferenceSolutionMapper referenceSolutionMapper;

    @Autowired
    private SubmitMapper submitMapper;

    @Autowired
    private LearnRecordMapper learnRecordMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private ClassInfoMapper classInfoMapper;

    @Autowired
    private KnowledgePointMapper knowledgePointMapper;

    @Autowired
    private LearningPathMapper learningPathMapper;

    @Autowired
    private SystemMonitorMapper systemMonitorMapper;

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Autowired
    private RuntimeCatalogService runtimeCatalogService;

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
    @Transactional
    public Long addProblem(Map<String, Object> params) {
        Problem problem = buildProblem(params, false);
        problemMapper.insert(problem);
        syncProblemResourcesIfPresent(problem.getId(), params, problem.getLanguage());
        return problem.getId();
    }

    @Override
    @Transactional
    public void addProblemWithCheck(Map<String, Object> params) {
        String title = (String) params.get("title");
        
        Problem existingProblem = problemMapper.findByTitle(title);
        if (existingProblem != null) {
            throw new RuntimeException("题目已存在：" + title);
        }
        
        Problem problem = buildProblem(params, false);
        problemMapper.insert(problem);
        syncProblemResourcesIfPresent(problem.getId(), params, problem.getLanguage());
        
        log.info("添加题目成功：{}", title);
    }

    @Override
    @Transactional
    public Long updateProblem(Map<String, Object> params) {
        Problem problem = buildProblem(params, true);
        problemMapper.update(problem);
        syncProblemResourcesIfPresent(problem.getId(), params, problem.getLanguage());
        return problem.getId();
    }

    @Override
    @Transactional
    public void deleteProblem(Long id) {
        referenceSolutionMapper.deleteByProblemId(id);
        problemSupportedLanguageMapper.deleteByProblemId(id);
        problemMapper.deleteById(id);
    }

    @Override
    public List<ProblemSupportedLanguage> getProblemLanguages(Long problemId) {
        Problem problem = requireProblem(problemId);
        return getExistingOrFallbackLanguages(problemId, problem.getLanguage());
    }

    @Override
    @Transactional
    public void saveProblemLanguages(Long problemId, List<Map<String, Object>> params) {
        Problem problem = requireProblem(problemId);
        List<ProblemSupportedLanguage> languages = parseProblemLanguages(problemId, params);
        List<ReferenceSolution> currentSolutions = referenceSolutionMapper.findByProblemId(problemId);
        validateLanguageAndSolutionSet(problem.getLanguage(), languages, currentSolutions);
        replaceProblemLanguages(problemId, languages);
        problemMapper.updateLanguageById(problemId, resolveDefaultLanguage(languages, problem.getLanguage()));
    }

    @Override
    public List<ReferenceSolution> getProblemReferenceSolutions(Long problemId) {
        return referenceSolutionMapper.findByProblemId(problemId);
    }

    @Override
    @Transactional
    public void saveProblemReferenceSolutions(Long problemId, List<Map<String, Object>> params) {
        Problem problem = requireProblem(problemId);
        List<ProblemSupportedLanguage> currentLanguages = getExistingOrFallbackLanguages(problemId, problem.getLanguage());
        List<ReferenceSolution> solutions = parseReferenceSolutions(problemId, params);
        validateLanguageAndSolutionSet(problem.getLanguage(), currentLanguages, solutions);
        replaceReferenceSolutions(problemId, solutions);
    }

    private Problem buildProblem(Map<String, Object> params, boolean update) {
        Problem problem = new Problem();
        if (update) {
            Long id = toLong(params.get("id"));
            if (id == null) {
                throw new RuntimeException("题目ID不能为空");
            }
            requireProblem(id);
            problem.setId(id);
        }

        String title = stringValue(params.get("title"));
        if (title == null || title.isBlank()) {
            throw new RuntimeException("题目标题不能为空");
        }

        String content = stringValue(params.get("content"));
        if (content == null || content.isBlank()) {
            throw new RuntimeException("题目描述不能为空");
        }

        String rawLanguage = stringValue(params.get("defaultLanguage"));
        if (rawLanguage == null || rawLanguage.isBlank()) {
            rawLanguage = stringValue(params.get("language"));
        }
        String defaultLanguage = runtimeCatalogService.normalizeLanguageCode(rawLanguage);
        if (!runtimeCatalogService.isJudgeLanguageSupported(defaultLanguage)) {
            throw new RuntimeException("不支持的默认语言: " + rawLanguage);
        }

        problem.setTitle(title.trim());
        problem.setContent(content.trim());
        problem.setDifficulty(toInteger(params.get("difficulty"), 0));
        problem.setLanguage(defaultLanguage);
        problem.setInput(trimToNull(stringValue(params.get("input"))));
        problem.setOutput(trimToNull(stringValue(params.get("output"))));
        problem.setTimeLimit(toIntegerObject(params.get("timeLimit")));
        problem.setMemoryLimit(toIntegerObject(params.get("memoryLimit")));
        problem.setTags(trimToNull(stringValue(params.get("tags"))));
        problem.setKnowledgePoints(trimToNull(stringValue(params.get("knowledgePoints"))));
        problem.setHints(trimToNull(stringValue(params.get("hints"))));
        problem.setSampleExplanation(trimToNull(stringValue(params.get("sampleExplanation"))));
        problem.setChapterId(toLong(params.get("chapterId")));
        problem.setLevelId(toLong(params.get("levelId")));
        return problem;
    }

    private void syncProblemResourcesIfPresent(Long problemId, Map<String, Object> params, String fallbackLanguage) {
        boolean hasLanguagePayload = params.containsKey("supportedLanguages");
        boolean hasSolutionPayload = params.containsKey("referenceSolutions");
        if (!hasLanguagePayload && !hasSolutionPayload) {
            return;
        }

        List<ProblemSupportedLanguage> languages = hasLanguagePayload
                ? parseProblemLanguages(problemId, castListPayload(params.get("supportedLanguages")))
                : getExistingOrFallbackLanguages(problemId, fallbackLanguage);
        List<ReferenceSolution> solutions = hasSolutionPayload
                ? parseReferenceSolutions(problemId, castListPayload(params.get("referenceSolutions")))
                : referenceSolutionMapper.findByProblemId(problemId);

        validateLanguageAndSolutionSet(fallbackLanguage, languages, solutions);
        replaceProblemLanguages(problemId, languages);
        replaceReferenceSolutions(problemId, solutions);
        problemMapper.updateLanguageById(problemId, resolveDefaultLanguage(languages, fallbackLanguage));
    }

    private List<Map<String, Object>> castListPayload(Object payload) {
        if (payload == null) {
            return List.of();
        }
        if (payload instanceof List<?> rawList) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : rawList) {
                if (item instanceof Map<?, ?> rawMap) {
                    Map<String, Object> normalizedMap = new LinkedHashMap<>();
                    for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                        if (entry.getKey() != null) {
                            normalizedMap.put(String.valueOf(entry.getKey()), entry.getValue());
                        }
                    }
                    result.add(normalizedMap);
                    continue;
                }
                if (item != null) {
                    result.add(JSON.parseObject(JSON.toJSONString(item), Map.class));
                }
            }
            return result;
        }
        if (payload instanceof String rawJson && !rawJson.isBlank()) {
            List<Map> parsed = JSON.parseArray(rawJson, Map.class);
            List<Map<String, Object>> result = new ArrayList<>();
            if (parsed != null) {
                for (Map item : parsed) {
                    Map<String, Object> normalizedMap = new LinkedHashMap<>();
                    for (Object key : item.keySet()) {
                        if (key != null) {
                            normalizedMap.put(String.valueOf(key), item.get(key));
                        }
                    }
                    result.add(normalizedMap);
                }
            }
            return result;
        }
        throw new RuntimeException("列表参数格式不正确");
    }

    private List<ProblemSupportedLanguage> getExistingOrFallbackLanguages(Long problemId, String fallbackLanguage) {
        List<ProblemSupportedLanguage> existing = problemSupportedLanguageMapper.findByProblemId(problemId);
        if (existing != null && !existing.isEmpty()) {
            existing.forEach(this::fillLanguageFallbacks);
            return existing;
        }

        String normalizedFallback = runtimeCatalogService.normalizeLanguageCode(fallbackLanguage);
        if (!runtimeCatalogService.isJudgeLanguageSupported(normalizedFallback)) {
            return List.of();
        }

        ProblemSupportedLanguage fallback = new ProblemSupportedLanguage();
        fallback.setProblemId(problemId);
        fallback.setLanguageCode(normalizedFallback);
        fallback.setIsDefault(1);
        fallback.setStatus("ACTIVE");
        fallback.setSortOrder(0);
        fillLanguageFallbacks(fallback);
        return List.of(fallback);
    }

    private List<ProblemSupportedLanguage> parseProblemLanguages(Long problemId, List<Map<String, Object>> params) {
        if (params == null || params.isEmpty()) {
            return List.of();
        }

        List<ProblemSupportedLanguage> result = new ArrayList<>();
        int index = 0;
        for (Map<String, Object> item : params) {
            String rawLanguage = stringValue(item.get("languageCode"));
            if (rawLanguage == null || rawLanguage.isBlank()) {
                rawLanguage = stringValue(item.get("language"));
            }
            String languageCode = runtimeCatalogService.normalizeLanguageCode(rawLanguage);
            if (!runtimeCatalogService.isJudgeLanguageSupported(languageCode)) {
                throw new RuntimeException("不支持的语言配置: " + rawLanguage);
            }

            ProblemSupportedLanguage config = new ProblemSupportedLanguage();
            config.setProblemId(problemId);
            config.setLanguageCode(languageCode);
            config.setIsDefault(asBooleanFlag(item.get("isDefault")) ? 1 : 0);
            config.setStarterCode(stringValue(item.get("starterCode")));
            config.setStarterFilename(stringValue(item.get("starterFilename")));
            config.setStatus(normalizeStatus(stringValue(item.get("status"))));
            config.setSortOrder(toInteger(item.get("sortOrder"), index));
            fillLanguageFallbacks(config);
            result.add(config);
            index++;
        }
        return result;
    }

    private List<ReferenceSolution> parseReferenceSolutions(Long problemId, List<Map<String, Object>> params) {
        if (params == null || params.isEmpty()) {
            return List.of();
        }

        List<ReferenceSolution> result = new ArrayList<>();
        for (Map<String, Object> item : params) {
            String rawLanguage = stringValue(item.get("language"));
            if (rawLanguage == null || rawLanguage.isBlank()) {
                rawLanguage = stringValue(item.get("languageCode"));
            }
            String language = runtimeCatalogService.normalizeLanguageCode(rawLanguage);
            if (!runtimeCatalogService.isJudgeLanguageSupported(language)) {
                throw new RuntimeException("不支持的参考答案语言: " + rawLanguage);
            }

            String solutionCode = stringValue(item.get("solutionCode"));
            if (solutionCode == null || solutionCode.isBlank()) {
                throw new RuntimeException("语言 " + language + " 缺少参考答案代码");
            }

            ReferenceSolution solution = new ReferenceSolution();
            solution.setProblemId(problemId);
            solution.setLanguage(language);
            solution.setSolutionCode(solutionCode);
            solution.setTimeComplexity(trimToNull(stringValue(item.get("timeComplexity"))));
            solution.setSpaceComplexity(trimToNull(stringValue(item.get("spaceComplexity"))));
            solution.setExplanation(trimToNull(stringValue(item.get("explanation"))));
            solution.setHints(normalizeHintsPayload(item.get("hints")));
            result.add(solution);
        }
        return result;
    }

    private void validateLanguageAndSolutionSet(String fallbackLanguage,
                                                List<ProblemSupportedLanguage> languages,
                                                List<ReferenceSolution> solutions) {
        List<ProblemSupportedLanguage> activeLanguages = new ArrayList<>();
        if (languages != null) {
            for (ProblemSupportedLanguage item : languages) {
                if (item != null && !"INACTIVE".equalsIgnoreCase(item.getStatus())) {
                    activeLanguages.add(item);
                }
            }
        }

        if (activeLanguages.isEmpty()) {
            throw new RuntimeException("至少需要启用一种语言");
        }

        Set<String> languageSet = new HashSet<>();
        int defaultCount = 0;
        for (ProblemSupportedLanguage item : activeLanguages) {
            String languageCode = runtimeCatalogService.normalizeLanguageCode(item.getLanguageCode());
            if (!languageSet.add(languageCode)) {
                throw new RuntimeException("语言配置重复: " + languageCode);
            }
            if (item.getIsDefault() != null && item.getIsDefault() == 1) {
                defaultCount++;
            }
        }

        if (defaultCount != 1) {
            throw new RuntimeException("必须且只能设置一个默认语言");
        }

        String resolvedDefaultLanguage = resolveDefaultLanguage(activeLanguages, fallbackLanguage);
        if (!languageSet.contains(resolvedDefaultLanguage)) {
            throw new RuntimeException("默认语言必须在启用语言列表中");
        }

        Map<String, ReferenceSolution> solutionMap = new LinkedHashMap<>();
        if (solutions != null) {
            for (ReferenceSolution solution : solutions) {
                if (solution == null) {
                    continue;
                }
                String language = runtimeCatalogService.normalizeLanguageCode(solution.getLanguage());
                if (solutionMap.containsKey(language)) {
                    throw new RuntimeException("参考答案重复: " + language);
                }
                if (solution.getSolutionCode() == null || solution.getSolutionCode().isBlank()) {
                    throw new RuntimeException("语言 " + language + " 缺少参考答案代码");
                }
                solutionMap.put(language, solution);
            }
        }

        for (String language : languageSet) {
            if (!solutionMap.containsKey(language)) {
                throw new RuntimeException("启用语言 " + language + " 缺少对应参考答案");
            }
        }
    }

    private void replaceProblemLanguages(Long problemId, List<ProblemSupportedLanguage> languages) {
        problemSupportedLanguageMapper.deleteByProblemId(problemId);
        if (languages == null || languages.isEmpty()) {
            return;
        }
        problemSupportedLanguageMapper.batchInsert(languages);
    }

    private void replaceReferenceSolutions(Long problemId, List<ReferenceSolution> solutions) {
        referenceSolutionMapper.deleteByProblemId(problemId);
        if (solutions == null || solutions.isEmpty()) {
            return;
        }
        referenceSolutionMapper.batchInsert(solutions);
    }

    private void fillLanguageFallbacks(ProblemSupportedLanguage item) {
        if (item == null) {
            return;
        }
        String normalizedLanguage = runtimeCatalogService.normalizeLanguageCode(item.getLanguageCode());
        item.setLanguageCode(normalizedLanguage);
        if (item.getStarterCode() == null || item.getStarterCode().isBlank()) {
            item.setStarterCode(runtimeCatalogService.resolveDefaultStarterCode(normalizedLanguage));
        }
        if (item.getStarterFilename() == null || item.getStarterFilename().isBlank()) {
            item.setStarterFilename(runtimeCatalogService.resolveDefaultFileName(normalizedLanguage));
        }
        if (item.getStatus() == null || item.getStatus().isBlank()) {
            item.setStatus("ACTIVE");
        } else {
            item.setStatus(item.getStatus().trim().toUpperCase(Locale.ROOT));
        }
        if (item.getSortOrder() == null) {
            item.setSortOrder(0);
        }
        if (item.getIsDefault() == null) {
            item.setIsDefault(0);
        }
    }

    private String resolveDefaultLanguage(List<ProblemSupportedLanguage> languages, String fallbackLanguage) {
        if (languages != null) {
            for (ProblemSupportedLanguage item : languages) {
                if (item != null
                        && !"INACTIVE".equalsIgnoreCase(item.getStatus())
                        && item.getIsDefault() != null
                        && item.getIsDefault() == 1) {
                    return runtimeCatalogService.normalizeLanguageCode(item.getLanguageCode());
                }
            }
        }
        return runtimeCatalogService.normalizeLanguageCode(fallbackLanguage);
    }

    private boolean asBooleanFlag(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.intValue() == 1;
        }
        String text = String.valueOf(value).trim();
        return "1".equals(text) || "true".equalsIgnoreCase(text) || "yes".equalsIgnoreCase(text);
    }

    private Problem requireProblem(Long problemId) {
        Problem problem = problemMapper.findById(problemId);
        if (problem == null) {
            throw new RuntimeException("题目不存在: " + problemId);
        }
        return problem;
    }

    private String normalizeHintsPayload(Object rawHints) {
        if (rawHints == null) {
            return "{}";
        }
        if (rawHints instanceof String text) {
            return text.isBlank() ? "{}" : text;
        }
        try {
            return JSON.toJSONString(rawHints);
        } catch (Exception e) {
            throw new RuntimeException("参考答案提示格式不正确");
        }
    }

    private String normalizeStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return "ACTIVE";
        }
        String normalized = rawStatus.trim().toUpperCase(Locale.ROOT);
        return "INACTIVE".equals(normalized) ? "INACTIVE" : "ACTIVE";
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Integer toInteger(Object value, int defaultValue) {
        Integer parsed = toIntegerObject(value);
        return parsed == null ? defaultValue : parsed;
    }

    private Integer toIntegerObject(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        return Integer.valueOf(text);
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        return Long.valueOf(text);
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
        int problemCount = problemMapper.count(null, null, null, null);
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

    // 角色和权限管理
    @Override
    public List<Role> getRoleList() {
        return roleMapper.findAll();
    }

    @Override
    public Role getRoleById(Long id) {
        return roleMapper.findById(id);
    }

    @Override
    public void addRole(Role role) {
        roleMapper.insert(role);
    }

    @Override
    public void updateRole(Role role) {
        roleMapper.update(role);
    }

    @Override
    public void deleteRole(Long id) {
        roleMapper.delete(id);
    }

    @Override
    public List<Permission> getPermissionList() {
        return permissionMapper.findAll();
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        // 先删除原有的权限关联
        roleMapper.deleteRolePermissions(roleId);
        // 添加新的权限关联
        for (Long permissionId : permissionIds) {
            roleMapper.insertRolePermission(roleId, permissionId);
        }
    }

    // 用户账号状态管理
    @Override
    public void updateUserStatus(Long userId, Integer status) {
        userMapper.updateStatus(userId, status);
    }

    @Override
    public void updateUserRole(Long userId, Long roleId) {
        userMapper.updateRole(userId, roleId);
    }

    // 班级和学员管理
    @Override
    public List<ClassInfo> getClassList() {
        return classInfoMapper.findAll();
    }

    @Override
    public void addClass(ClassInfo classInfo) {
        classInfoMapper.insert(classInfo);
    }

    @Override
    public void updateClass(ClassInfo classInfo) {
        classInfoMapper.update(classInfo);
    }

    @Override
    public void deleteClass(Long id) {
        classInfoMapper.delete(id);
    }

    @Override
    public void addUserToClass(Long userId, Long classId, String role) {
        classInfoMapper.insertUserClass(userId, classId, role);
    }

    @Override
    public void removeUserFromClass(Long userId, Long classId) {
        classInfoMapper.deleteUserClass(userId, classId);
    }

    @Override
    public List<User> getClassUsers(Long classId) {
        return classInfoMapper.findUsersByClassId(classId);
    }

    // 操作审计日志
    @Override
    public Map<String, Object> getAuditLogs(int page, int size) {
        int offset = (page - 1) * size;
        List<AuditLog> logs = auditLogMapper.findPage(offset, size);
        int total = auditLogMapper.count();

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", logs);
        return result;
    }

    // 系统监控
    @Override
    public Map<String, Object> getSystemStatus() {
        SystemMonitor monitor = systemMonitorMapper.getLatest();
        Map<String, Object> result = new HashMap<>();
        if (monitor != null) {
            result.put("metricName", monitor.getMetricName());
            result.put("metricValue", monitor.getMetricValue());
            result.put("metricUnit", monitor.getMetricUnit());
            result.put("createTime", monitor.getCreateTime());
        }
        return result;
    }

    @Override
    public Map<String, Object> getApiMetrics() {
        List<ApiMetric> metrics = systemMonitorMapper.getApiMetrics();
        Map<String, Object> result = new HashMap<>();
        result.put("metrics", metrics);
        return result;
    }

    @Override
    public Map<String, Object> getSandboxStatus() {
        List<SandboxStatus> statuses = systemMonitorMapper.getSandboxStatus();
        Map<String, Object> result = new HashMap<>();
        result.put("statuses", statuses);
        return result;
    }

    @Override
    public List<SystemLog> getSystemLogs(int page, int size) {
        int offset = (page - 1) * size;
        return systemMonitorMapper.getSystemLogs(offset, size);
    }

    // 内容管理
    @Override
    public void addKnowledgePoint(KnowledgePoint knowledgePoint) {
        knowledgePointMapper.insert(knowledgePoint);
    }

    @Override
    public void updateKnowledgePoint(KnowledgePoint knowledgePoint) {
        knowledgePointMapper.update(knowledgePoint);
    }

    @Override
    public void deleteKnowledgePoint(Long id) {
        knowledgePointMapper.delete(id);
    }

    @Override
    public List<KnowledgePoint> getKnowledgePoints() {
        return knowledgePointMapper.findAll();
    }

    @Override
    public void addLearningPath(LearningPath learningPath) {
        learningPathMapper.insertPath(learningPath);
    }

    @Override
    public void updateLearningPath(LearningPath learningPath) {
        learningPathMapper.updatePath(learningPath);
    }

    @Override
    public void deleteLearningPath(Long id) {
        learningPathMapper.deletePath(id);
    }

    @Override
    public List<LearningPath> getLearningPaths() {
        return learningPathMapper.selectAllPaths();
    }

    // 数据分析
    @Override
    public Map<String, Object> getDashboardData() {
        Map<String, Object> result = new HashMap<>();
        result.put("userCount", userMapper.count());
        result.put("problemCount", problemMapper.count(null, null, null, null));
        result.put("submitCount", submitMapper.countAll());
        result.put("activeUsers", userMapper.countActiveUsers());
        return result;
    }

    @Override
    public Map<String, Object> getHomeStatistics() {
        Map<String, Object> result = new HashMap<>();
        result.put("userCount", userMapper.count());
        result.put("problemCount", problemMapper.count(null, null, null, null));
        result.put("pathCount", learningPathMapper.selectAllPaths().size());
        return result;
    }

    @Override
    public Map<String, Object> getUserRetentionData() {
        // 这里需要实现用户留存数据的查询逻辑
        Map<String, Object> result = new HashMap<>();
        // 暂时返回空数据，实际项目中需要根据具体需求实现
        return result;
    }

    @Override
    public Map<String, Object> getKnowledgeDistribution() {
        // 这里需要实现知识点分布数据的查询逻辑
        Map<String, Object> result = new HashMap<>();
        // 暂时返回空数据，实际项目中需要根据具体需求实现
        return result;
    }

    @Override
    public void exportStatistics(String type, String format) {
        // 这里需要实现数据导出逻辑
        log.info("导出统计数据，类型：{}，格式：{}", type, format);
        // 暂时只是记录日志，实际项目中需要根据具体需求实现
    }
}
