package com.programming.controller;

import com.programming.service.RankingService;
import com.programming.vo.RankingItem;
import com.programming.vo.MyRankVO;
import com.programming.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {
    
    @Autowired
    private RankingService rankingService;
    
    /**
     * 获取解题数量排行榜
     */
    @GetMapping("/solved")
    public ResultUtil<List<RankingItem>> getSolvedRanking(
            @RequestParam(defaultValue = "week") String period,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        List<RankingItem> ranking = rankingService.getSolvedRanking(period, page, size);
        return ResultUtil.success(ranking);
    }
    
    /**
     * 获取通过率排行榜
     */
    @GetMapping("/pass-rate")
    public ResultUtil<List<RankingItem>> getPassRateRanking(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        List<RankingItem> ranking = rankingService.getPassRateRanking(page, size);
        return ResultUtil.success(ranking);
    }
    
    /**
     * 获取学习时长排行榜
     */
    @GetMapping("/study-hours")
    public ResultUtil<List<RankingItem>> getStudyHoursRanking(
            @RequestParam(defaultValue = "week") String period,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        List<RankingItem> ranking = rankingService.getStudyHoursRanking(period, page, size);
        return ResultUtil.success(ranking);
    }
    
    /**
     * 获取用户排名
     */
    @GetMapping("/my-rank")
    public ResultUtil<MyRankVO> getMyRank(@RequestAttribute Long userId) {
        MyRankVO rank = rankingService.getMyRank(userId);
        return ResultUtil.success(rank);
    }
}