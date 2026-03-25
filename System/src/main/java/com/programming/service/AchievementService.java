package com.programming.service;

import com.programming.entity.Achievement;
import com.programming.entity.UserAchievement;
import com.programming.mapper.AchievementMapper;
import com.programming.mapper.UserMapper;
import com.programming.mapper.SubmitMapper;
import com.programming.mapper.CommunityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AchievementService {
    
    @Autowired
    private AchievementMapper achievementMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private SubmitMapper submitMapper;
    
    @Autowired
    private CommunityMapper communityMapper;
    
    /**
     * 检查并解锁成就
     */
    public void checkAndUnlockAchievements(Long userId) {
        // 获取用户统计信息
        int solvedCount = submitMapper.countPassedByUserId(userId);
        int postCount = communityMapper.countPostsByUserId(userId);
        int commentCount = communityMapper.countCommentsByUserId(userId);
        int continuousDays = calculateContinuousDays(userId);
        
        // 检查解题成就
        checkSolvedAchievements(userId, solvedCount);
        
        // 检查连续学习成就
        checkContinuousDaysAchievements(userId, continuousDays);
        
        // 检查社区成就
        checkCommunityAchievements(userId, postCount, commentCount);
    }
    
    /**
     * 检查解题成就
     */
    private void checkSolvedAchievements(Long userId, int solvedCount) {
        List<Achievement> achievements = achievementMapper.findByConditionType("SOLVED_COUNT");
        
        for (Achievement achievement : achievements) {
            if (solvedCount >= achievement.getConditionValue()) {
                unlockAchievement(userId, achievement.getId());
            }
        }
    }
    
    /**
     * 检查连续学习成就
     */
    private void checkContinuousDaysAchievements(Long userId, int continuousDays) {
        List<Achievement> achievements = achievementMapper.findByConditionType("CONTINUOUS_DAYS");
        
        for (Achievement achievement : achievements) {
            if (continuousDays >= achievement.getConditionValue()) {
                unlockAchievement(userId, achievement.getId());
            }
        }
    }
    
    /**
     * 检查社区成就
     */
    private void checkCommunityAchievements(Long userId, int postCount, int commentCount) {
        // 检查发帖成就
        List<Achievement> postAchievements = achievementMapper.findByConditionType("POST_COUNT");
        for (Achievement achievement : postAchievements) {
            if (postCount >= achievement.getConditionValue()) {
                unlockAchievement(userId, achievement.getId());
            }
        }
        
        // 检查评论成就
        List<Achievement> commentAchievements = achievementMapper.findByConditionType("COMMENT_COUNT");
        for (Achievement achievement : commentAchievements) {
            if (commentCount >= achievement.getConditionValue()) {
                unlockAchievement(userId, achievement.getId());
            }
        }
    }
    
    /**
     * 解锁成就
     */
    private void unlockAchievement(Long userId, Long achievementId) {
        // 检查是否已解锁
        if (achievementMapper.hasUserAchievement(userId, achievementId)) {
            return;
        }
        
        // 解锁成就
        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setUserId(userId);
        userAchievement.setAchievementId(achievementId);
        userAchievement.setCreateTime(LocalDateTime.now());
        
        achievementMapper.insertUserAchievement(userAchievement);
        
        // 发送通知（这里简化实现，实际项目中需要实现通知功能）
    }
    
    /**
     * 计算连续学习天数
     */
    private int calculateContinuousDays(Long userId) {
        // 这里简化实现，实际项目中需要根据用户的提交记录计算连续学习天数
        return 0;
    }
}