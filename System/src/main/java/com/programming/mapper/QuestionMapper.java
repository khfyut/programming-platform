package com.programming.mapper;

import com.programming.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuestionMapper {
    int insert(Question question);
    List<Question> findByUserId(@Param("userId") Long userId, @Param("page") int page, @Param("size") int size);
    int countByUserId(@Param("userId") Long userId);
}
