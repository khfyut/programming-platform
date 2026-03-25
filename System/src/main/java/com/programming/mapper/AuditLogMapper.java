package com.programming.mapper;

import com.programming.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AuditLogMapper {
    List<AuditLog> findPage(int offset, int size);
    int count();
    AuditLog findById(Long id);
    List<AuditLog> findByUserId(@Param("userId") Long userId, @Param("page") int page, @Param("size") int size);
    List<AuditLog> findByResourceType(@Param("resourceType") String resourceType, @Param("page") int page, @Param("size") int size);
    void insert(AuditLog log);
    void deleteOldLogs(int days);
}
