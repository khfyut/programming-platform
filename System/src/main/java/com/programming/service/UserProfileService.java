package com.programming.service;

import com.programming.entity.User;
import com.programming.entity.Favorite;
import com.programming.entity.Achievement;
import com.programming.entity.UserAchievement;
import com.programming.mapper.UserMapper;
import com.programming.mapper.FavoriteMapper;
import com.programming.mapper.AchievementMapper;
import com.programming.mapper.SubmitMapper;
import com.programming.mapper.CommunityMapper;
import com.programming.vo.UserProfileVO;
import com.programming.vo.StudyStats;
import com.programming.vo.UserProfileUpdateVO;
import com.programming.vo.FavoriteVO;
import com.programming.vo.UserAchievementVO;
import com.programming.vo.StudyActivity;
import com.programming.entity.Submit;
import com.programming.entity.CommunityPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserProfileService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private SubmitMapper submitMapper;
    
    @Autowired
    private CommunityMapper communityMapper;
    
    @Autowired
    private FavoriteMapper favoriteMapper;
    
    @Autowired
    private AchievementMapper achievementMapper;
    
    /**
     * 获取用户主页信息
     */
    public UserProfileVO getUserProfile(Long userId) {
        User user = userMapper.findById(userId);
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        UserProfileVO profile = new UserProfileVO();
        profile.setUserId(userId);
        profile.setUsername(user.getUsername());
        profile.setAvatarUrl(user.getAvatarUrl());
        profile.setBio(user.getBio());
        profile.setGithubUrl(user.getGithubUrl());
        profile.setBlogUrl(user.getBlogUrl());
        
        // 学习统计
        StudyStats stats = getStudyStats(userId);
        profile.setStats(stats);
        
        // 排名
        profile.setRanking(user.getRanking());
        
        // 是否为管理员
        profile.setAdmin(user.getRole() == 1);
        
        return profile;
    }
    
    /**
     * 更新个人信息
     */
    public void updateProfile(Long userId, UserProfileUpdateVO vo) {
        String username = vo.getUsername() == null ? "" : vo.getUsername().trim();
        if (username.isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }

        User existingUser = userMapper.findByUsername(username);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new RuntimeException("用户名已存在");
        }

        userMapper.updateProfile(userId, username, vo.getBio(), vo.getAvatarUrl(), vo.getGithubUrl(), vo.getBlogUrl());
    }
    
    /**
     * 获取学习统计
     */
    public StudyStats getStudyStats(Long userId) {
        StudyStats stats = new StudyStats();
        
        // 总提交数
        int totalSubmissions = submitMapper.countByUserId(userId, null);
        stats.setTotalSubmissions(totalSubmissions);
        
        // 总解题数
        int totalSolved = submitMapper.countPassedByUserId(userId);
        stats.setTotalSolved(totalSolved);
        
        // 通过率
        if (totalSubmissions > 0) {
            double passRate = (double) totalSolved / totalSubmissions;
            stats.setPassRate(passRate);
        }
        
        // 学习时长
        User user = userMapper.findById(userId);
        stats.setStudyHours(user.getStudyHours() != null ? user.getStudyHours() : 0);
        
        // 发布帖子数
        int postCount = communityMapper.countPostsByUserId(userId);
        stats.setPostCount(postCount);
        
        // 评论数
        int commentCount = communityMapper.countCommentsByUserId(userId);
        stats.setCommentCount(commentCount);
        
        // 周趋势（这里简化实现，实际项目中需要计算）
        stats.setSolvedTrend(0);
        stats.setSubmittedTrend(0);
        stats.setAccuracyTrend(0);
        stats.setStreakTrend(0);
        stats.setStudyHoursTrend(0);
        stats.setRankingTrend(0);
        
        return stats;
    }
    
    /**
     * 获取学习动态
     */
    public List<StudyActivity> getStudyActivities(Long userId, int page, int size) {
        List<StudyActivity> activities = new ArrayList<>();
        int fetchSize = Math.max((page + 1) * size, size);
        
        // 最近的解题记录
        List<Submit> recentSubmits = submitMapper.findRecentByUserId(userId, 0, fetchSize);
        for (Submit submit : recentSubmits) {
            StudyActivity activity = new StudyActivity();
            activity.setType("SOLVE_PROBLEM");
            activity.setTargetId(submit.getProblemId());
            activity.setTargetName("Problem " + submit.getProblemId());
            activity.setStatus(submit.getResult() != null ? submit.getResult().toString() : "PENDING");
            activity.setCreateTime(submit.getCreateTime());
            activities.add(activity);
        }
        
        // 最近的发帖记录
        List<CommunityPost> recentPosts = communityMapper.findRecentPostsByUserId(userId, 0, fetchSize);
        for (CommunityPost post : recentPosts) {
            StudyActivity activity = new StudyActivity();
            activity.setType("CREATE_POST");
            activity.setTargetId(post.getId());
            activity.setTargetName(post.getTitle());
            activity.setCreateTime(post.getCreateTime());
            activities.add(activity);
        }
        
        // 按时间排序
        activities.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        
        // 分页
        int start = page * size;
        int end = Math.min(start + size, activities.size());
        
        if (start >= activities.size()) {
            return new ArrayList<>();
        }
        
        return activities.subList(start, end);
    }
    
    /**
     * 获取用户成就
     */
    public List<UserAchievementVO> getUserAchievements(Long userId) {
        List<UserAchievement> userAchievements = achievementMapper.findByUserId(userId);
        
        return userAchievements.stream()
            .map(ua -> {
                UserAchievementVO vo = new UserAchievementVO();
                Achievement achievement = achievementMapper.findById(ua.getAchievementId());
                if (achievement != null) {
                    vo.setAchievementId(achievement.getId());
                    vo.setName(achievement.getName());
                    vo.setDescription(achievement.getDescription());
                    vo.setIcon(achievement.getIcon());
                    vo.setUnlockTime(ua.getCreateTime());
                }
                return vo;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 添加收藏
     */
    public void addFavorite(Long userId, String targetType, Long targetId) {
        // 检查是否已收藏
        if (favoriteMapper.exists(userId, targetType, targetId)) {
            throw new RuntimeException("已收藏");
        }
        
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setTargetType(targetType);
        favorite.setTargetId(targetId);
        favorite.setCreateTime(LocalDateTime.now());
        
        favoriteMapper.insert(favorite);
    }
    
    /**
     * 取消收藏
     */
    public void removeFavorite(Long userId, String targetType, Long targetId) {
        favoriteMapper.delete(userId, targetType, targetId);
    }
    
    /**
     * 获取收藏列表
     */
    public List<FavoriteVO> getFavorites(Long userId, String targetType, int page, int size) {
        List<Favorite> favorites = favoriteMapper.findByUserIdAndType(userId, targetType, page, size);
        
        return favorites.stream()
            .map(f -> {
                FavoriteVO vo = new FavoriteVO();
                vo.setTargetType(f.getTargetType());
                vo.setTargetId(f.getTargetId());
                vo.setCreateTime(f.getCreateTime());
                
                // 获取目标信息
                if ("POST".equals(targetType)) {
                    CommunityPost post = communityMapper.findPostById(f.getTargetId());
                    if (post != null) {
                        vo.setTargetName(post.getTitle());
                    }
                }
                
                return vo;
            })
            .collect(Collectors.toList());
    }
}
