package com.programming.service;

import com.programming.util.ResultUtil;
import com.programming.vo.AiAskVO;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface AiService {
    ResultUtil askQuestion(AiAskVO aiAskVO);
    Map<String, Object> getHistory(Long userId, int page, int size);
}
