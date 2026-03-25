package com.programming.mapper;

import com.programming.entity.KnowledgePoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface KnowledgePointMapper {
    List<KnowledgePoint> findAll();
    KnowledgePoint findById(Long id);
    List<KnowledgePoint> findByParentId(Long parentId);
    List<KnowledgePoint> findRootPoints();
    List<KnowledgePoint> findByLevel(Integer level);
    List<KnowledgePoint> findKnowledgeTree();
    void insert(KnowledgePoint knowledgePoint);
    void update(KnowledgePoint knowledgePoint);
    void delete(Long id);
}
