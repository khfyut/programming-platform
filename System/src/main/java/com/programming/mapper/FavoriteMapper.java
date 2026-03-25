package com.programming.mapper;

import com.programming.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FavoriteMapper {
    void insert(Favorite favorite);
    void delete(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);
    boolean exists(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);
    List<Favorite> findByUserIdAndType(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("page") int page, @Param("size") int size);
    
    // 获取用户收藏的题目ID列表
    List<Long> findProblemIdsByUserId(@Param("userId") Long userId);
}