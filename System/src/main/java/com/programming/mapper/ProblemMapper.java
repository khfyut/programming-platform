package com.programming.mapper;

import com.programming.entity.Problem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProblemMapper {
    List<Problem> findByPage(@Param("page") int page, @Param("size") int size, 
                              @Param("difficulty") Integer difficulty, @Param("language") String language,
                              @Param("knowledge") String knowledge);
    Problem findById(@Param("id") Long id);
    Problem findByTitle(@Param("title") String title);
    int count(@Param("difficulty") Integer difficulty, @Param("language") String language,
              @Param("knowledge") String knowledge);
    int insert(Problem problem);
    int update(Problem problem);
    int updateLanguageById(@Param("id") Long id, @Param("language") String language);
    int deleteById(@Param("id") Long id);
    List<Problem> findByTags(@Param("tag") String tag);
    List<Problem> findByDifficulty(@Param("difficulty") Integer difficulty);
    List<Problem> findByKnowledgePoint(@Param("knowledgePoint") String knowledgePoint, @Param("limit") int limit);
    List<Problem> selectAll();
    List<Map<String, Object>> getLanguageStats();
}
