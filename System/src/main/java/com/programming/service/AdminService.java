package com.programming.service;

import com.programming.vo.BatchImportProblemsVO;
import java.util.Map;

public interface AdminService {
    Map<String, Object> getUserList(int page, int size);
    void addProblem(Map<String, Object> params);
    void addProblemWithCheck(Map<String, Object> params);
    void updateProblem(Map<String, Object> params);
    void deleteProblem(Long id);
    Map<String, Object> getSubmitList(int page, int size);
    Map<String, Object> getStatistics();
    void batchImportProblems(BatchImportProblemsVO vo);
}