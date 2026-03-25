package com.programming.controller;

import com.programming.service.UserProfileService;
import com.programming.vo.UserProfileVO;
import com.programming.vo.UserProfileUpdateVO;
import com.programming.vo.StudyStats;
import com.programming.vo.StudyActivity;
import com.programming.vo.UserAchievementVO;
import com.programming.vo.FavoriteVO;
import com.programming.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/profile")
public class UserProfileController {
    
    @Autowired
    private UserProfileService userProfileService;
    
    /**
     * 获取个人主页信息
     */
    @GetMapping("/{userId}")
    public ResultUtil<UserProfileVO> getUserProfile(@PathVariable Long userId) {
        UserProfileVO profile = userProfileService.getUserProfile(userId);
        return ResultUtil.success(profile);
    }
    
    /**
     * 更新个人信息
     */
    @PutMapping
    public ResultUtil<String> updateProfile(@RequestBody UserProfileUpdateVO vo, @RequestAttribute Long userId) {
        userProfileService.updateProfile(userId, vo);
        return ResultUtil.success("更新成功");
    }
    
    /**
     * 获取学习统计
     */
    @GetMapping("/{userId}/stats")
    public ResultUtil<StudyStats> getStudyStats(@PathVariable Long userId) {
        StudyStats stats = userProfileService.getStudyStats(userId);
        return ResultUtil.success(stats);
    }
    
    /**
     * 获取学习动态
     */
    @GetMapping("/{userId}/activities")
    public ResultUtil<List<StudyActivity>> getStudyActivities(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<StudyActivity> activities = userProfileService.getStudyActivities(userId, page, size);
        return ResultUtil.success(activities);
    }
    
    /**
     * 获取用户成就
     */
    @GetMapping("/{userId}/achievements")
    public ResultUtil<List<UserAchievementVO>> getUserAchievements(@PathVariable Long userId) {
        List<UserAchievementVO> achievements = userProfileService.getUserAchievements(userId);
        return ResultUtil.success(achievements);
    }
    
    /**
     * 收藏帖子
     */
    @PostMapping("/favorite")
    public ResultUtil<String> addFavorite(@RequestBody FavoriteVO vo, @RequestAttribute Long userId) {
        userProfileService.addFavorite(userId, vo.getTargetType(), vo.getTargetId());
        return ResultUtil.success("收藏成功");
    }
    
    /**
     * 取消收藏
     */
    @DeleteMapping("/favorite")
    public ResultUtil<String> removeFavorite(@RequestBody FavoriteVO vo, @RequestAttribute Long userId) {
        userProfileService.removeFavorite(userId, vo.getTargetType(), vo.getTargetId());
        return ResultUtil.success("取消收藏成功");
    }
    
    /**
     * 获取收藏列表
     */
    @GetMapping("/{userId}/favorites")
    public ResultUtil<List<FavoriteVO>> getFavorites(
            @PathVariable Long userId,
            @RequestParam String targetType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<FavoriteVO> favorites = userProfileService.getFavorites(userId, targetType, page, size);
        return ResultUtil.success(favorites);
    }
}