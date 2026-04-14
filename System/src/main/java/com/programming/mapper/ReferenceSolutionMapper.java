package com.programming.mapper;

import com.programming.entity.ReferenceSolution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReferenceSolutionMapper {

    ReferenceSolution findByProblemIdAndLanguage(@Param("problemId") Long problemId, @Param("language") String language);

    List<ReferenceSolution> findByProblemId(@Param("problemId") Long problemId);

    void insert(ReferenceSolution referenceSolution);

    int batchInsert(@Param("items") List<ReferenceSolution> items);

    void update(ReferenceSolution referenceSolution);

    void delete(@Param("id") Long id);

    int deleteByProblemId(@Param("problemId") Long problemId);

    List<String> findLanguagesByProblemId(@Param("problemId") Long problemId);
}
