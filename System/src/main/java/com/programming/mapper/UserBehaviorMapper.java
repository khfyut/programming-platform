package com.programming.mapper;

import com.programming.entity.UserBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserBehaviorMapper {

    /**
     * 插入用户行为
     */
    void insert(UserBehavior userBehavior);

    /**
     * 根据用户ID和行为类型获取行为记录
     */
    List<UserBehavior> findByUserIdAndType(@Param("userId") Long userId, @Param("behaviorType") String behaviorType);

    /**
     * 根据用户ID、目标类型和目标ID获取行为记录
     */
    UserBehavior findByUserIdAndTarget(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);

    /**
     * 获取用户对某题目是否有提交记录
     */
    Integer countSubmitRecords(@Param("userId") Long userId, @Param("problemId") Long problemId);

    /**
     * 获取用户对某题目的提交次数
     */
    Integer getSubmitCount(@Param("userId") Long userId, @Param("problemId") Long problemId);
}