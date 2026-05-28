
package com.programming.mapper;

import com.programming.entity.LearningResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface LearningResourceMapper {
    List<LearningResource> selectResourcesByLevelId(@Param("levelId") Long levelId);
    LearningResource selectResourceById(Long id);
    void insertResource(LearningResource resource);
    void updateResource(LearningResource resource);
    void deleteResource(Long id);
    void deleteByLevelIdAndTypeAndName(@Param("levelId") Long levelId,
                                       @Param("type") String type,
                                       @Param("name") String name);
}
