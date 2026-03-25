package com.programming.mapper;

import com.programming.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RoleMapper {
    List<Role> findAll();
    Role findById(Long id);
    void insert(Role role);
    void update(Role role);
    void delete(Long id);
    void deleteRolePermissions(Long roleId);
    void insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
}
