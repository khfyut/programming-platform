
package com.programming.mapper;

import com.programming.entity.PathLevelProblem;
import com.programming.entity.Problem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PathLevelProblemMapper {
    List<PathLevelProblem> findByLevelId(@Param("levelId") Long levelId);
    List<Problem> findProblemsByLevelId(@Param("levelId") Long levelId);
    int insert(PathLevelProblem pathLevelProblem);
    int deleteByLevelIdAndProblemId(@Param("levelId") Long levelId, @Param("problemId") Long problemId);
    int deleteByLevelId(@Param("levelId") Long levelId);
    int batchInsert(@Param("list") List<PathLevelProblem> list);
}
