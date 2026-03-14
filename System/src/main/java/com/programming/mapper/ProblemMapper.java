package com.programming.mapper;

import com.programming.entity.Problem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProblemMapper {
    List<Problem> findByPage(@Param("page") int page, @Param("size") int size, 
                              @Param("difficulty") Integer difficulty, @Param("language") String language);
    Problem findById(@Param("id") Long id);
    Problem findByTitle(@Param("title") String title);
    int count(@Param("difficulty") Integer difficulty, @Param("language") String language);
    int insert(Problem problem);
    int update(Problem problem);
    int deleteById(@Param("id") Long id);
    List<Problem> findByTags(@Param("tag") String tag);
    List<Problem> findByDifficulty(@Param("difficulty") Integer difficulty);
}
