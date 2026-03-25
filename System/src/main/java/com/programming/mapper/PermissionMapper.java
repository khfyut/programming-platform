package com.programming.mapper;

import com.programming.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PermissionMapper {
    List<Permission> findAll();
    Permission findById(Long id);
    Permission findByCode(String code);
    List<Permission> findByRoleId(Long roleId);
    void insert(Permission permission);
    void update(Permission permission);
    void delete(Long id);
}
