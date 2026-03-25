package com.programming.controller;

import com.programming.service.KnowledgeMasteryService;
import com.programming.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge-graph")
public class KnowledgeGraphController {

    @Autowired
    private KnowledgeMasteryService knowledgeMasteryService;

    /**
     * 获取知识图谱数据
     */
    @GetMapping
    public Object getKnowledgeGraph(
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) Integer minMastery,
            @RequestParam(required = false) Integer maxMastery,
            @RequestAttribute Long userId) {

        Map<String, Object> graphData = knowledgeMasteryService.getKnowledgeGraph();
        return ResultUtil.success(graphData);
    }

    /**
     * 获取知识点详情
     */
    @GetMapping("/node/{nodeId}")
    public Object getNodeDetail(
            @PathVariable Long nodeId,
            @RequestAttribute Long userId) {

        Object nodeDetail = knowledgeMasteryService.getKnowledgePointById(nodeId);
        return ResultUtil.success(nodeDetail);
    }

    /**
     * 获取用户知识掌握度分布
     */
    @GetMapping("/mastery")
    public Object getKnowledgeMasteryDistribution(@RequestAttribute Long userId) {

        Object distribution = knowledgeMasteryService.getKnowledgeMasteryDistribution(userId);
        return ResultUtil.success(distribution);
    }

    /**
     * 获取用户薄弱知识点
     */
    @GetMapping("/weaknesses")
    public Object getWeakKnowledgePoints(
            @RequestParam(defaultValue = "2") Integer threshold,
            @RequestAttribute Long userId) {

        Object weaknesses = knowledgeMasteryService.getUserWeakKnowledgePoints(userId, threshold);
        return ResultUtil.success(weaknesses);
    }
    
    /**
     * 获取知识点统计（带题目数量）
     */
    @GetMapping("/stats")
    public Object getKnowledgePointStats() {
        List<Map<String, Object>> stats = knowledgeMasteryService.getKnowledgePointWithProblemCount();
        return ResultUtil.success(stats);
    }
}