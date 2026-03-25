package com.programming.mapper;

import com.programming.entity.Achievement;
import com.programming.entity.UserAchievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AchievementMapper {
    Achievement findById(Long id);
    List<Achievement> findByConditionType(String conditionType);
    List<UserAchievement> findByUserId(Long userId);
    boolean hasUserAchievement(@Param("userId") Long userId, @Param("achievementId") Long achievementId);
    void insertUserAchievement(UserAchievement userAchievement);
}