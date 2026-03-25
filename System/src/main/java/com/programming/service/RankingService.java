package com.programming.service;

import com.programming.entity.User;
import com.programming.mapper.UserMapper;
import com.programming.mapper.SubmitMapper;
import com.programming.vo.RankingItem;
import com.programming.vo.MyRankVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RankingService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private SubmitMapper submitMapper;
    
    /**
     * 获取解题数量排行榜
     */
    public List<RankingItem> getSolvedRanking(String period, int page, int size) {
        // 这里简化实现，实际项目中需要根据period参数计算不同时间范围的排行榜
        List<User> users = userMapper.findByPage(page, size);
        
        return users.stream()
            .map(user -> {
                RankingItem item = new RankingItem();
                item.setUserId(user.getId());
                item.setUsername(user.getUsername());
                item.setAvatarUrl(user.getAvatarUrl());
                item.setRank(page * size + 1); // 简化排名计算
                item.setScore(user.getTotalSolved() != null ? user.getTotalSolved() : 0);
                item.setPassRate(0); // 简化实现
                item.setStudyHours(user.getStudyHours() != null ? user.getStudyHours() : 0);
                return item;
            })
            .sorted((a, b) -> b.getScore() - a.getScore())
            .collect(Collectors.toList());
    }
    
    /**
     * 获取通过率排行榜
     */
    public List<RankingItem> getPassRateRanking(int page, int size) {
        List<User> users = userMapper.findByPage(page, size);
        
        return users.stream()
            .map(user -> {
                RankingItem item = new RankingItem();
                item.setUserId(user.getId());
                item.setUsername(user.getUsername());
                item.setAvatarUrl(user.getAvatarUrl());
                item.setRank(page * size + 1); // 简化排名计算
                item.setScore(0); // 简化实现
                
                // 计算通过率
                int totalSubmissions = submitMapper.countByUserId(user.getId(), null);
                int totalSolved = submitMapper.countPassedByUserId(user.getId());
                double passRate = totalSubmissions > 0 ? (double) totalSolved / totalSubmissions : 0;
                item.setPassRate(passRate);
                
                item.setStudyHours(user.getStudyHours() != null ? user.getStudyHours() : 0);
                return item;
            })
            .sorted((a, b) -> Double.compare(b.getPassRate(), a.getPassRate()))
            .collect(Collectors.toList());
    }
    
    /**
     * 获取学习时长排行榜
     */
    public List<RankingItem> getStudyHoursRanking(String period, int page, int size) {
        List<User> users = userMapper.findByPage(page, size);
        
        return users.stream()
            .map(user -> {
                RankingItem item = new RankingItem();
                item.setUserId(user.getId());
                item.setUsername(user.getUsername());
                item.setAvatarUrl(user.getAvatarUrl());
                item.setRank(page * size + 1); // 简化排名计算
                item.setScore(0); // 简化实现
                item.setPassRate(0); // 简化实现
                item.setStudyHours(user.getStudyHours() != null ? user.getStudyHours() : 0);
                return item;
            })
            .sorted((a, b) -> b.getStudyHours() - a.getStudyHours())
            .collect(Collectors.toList());
    }
    
    /**
     * 获取用户排名
     */
    public MyRankVO getMyRank(Long userId) {
        MyRankVO rank = new MyRankVO();
        
        User user = userMapper.findById(userId);
        if (user != null) {
            rank.setTotalSolved(user.getTotalSolved() != null ? user.getTotalSolved() : 0);
            rank.setStudyHours(user.getStudyHours() != null ? user.getStudyHours() : 0);
            
            // 计算通过率
            int totalSubmissions = submitMapper.countByUserId(userId, null);
            int totalSolved = submitMapper.countPassedByUserId(userId);
            double passRate = totalSubmissions > 0 ? (double) totalSolved / totalSubmissions : 0;
            rank.setPassRate(passRate);
            
            // 简化排名计算
            rank.setSolvedRank(1);
            rank.setPassRateRank(1);
            rank.setStudyHoursRank(1);
        }
        
        return rank;
    }
}