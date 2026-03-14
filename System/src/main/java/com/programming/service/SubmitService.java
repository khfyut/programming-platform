package com.programming.service;

import com.programming.entity.Submit;
import com.programming.vo.SubmitResultVO;
import com.programming.vo.SubmitWithProblemVO;
import java.util.Map;

public interface SubmitService {
    SubmitResultVO commit(Long userId, Long problemId, String code, String language);
    Map<String, Object> getMySubmits(Long userId, Long problemId, int page, int size);
    SubmitWithProblemVO getSubmitDetail(Long submitId);
}
