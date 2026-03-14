package com.programming.mapper;

import com.programming.entity.Submit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SubmitMapper {
    int insert(Submit submit);
    List<Submit> findByUserId(@Param("userId") Long userId, @Param("problemId") Long problemId, 
                                  @Param("page") int page, @Param("size") int size);
    int countByUserId(@Param("userId") Long userId, @Param("problemId") Long problemId);
    List<Submit> findAll(@Param("page") int page, @Param("size") int size);
    int countAll();
    Submit findById(@Param("id") Long id);
}
