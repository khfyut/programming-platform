package com.programming.mapper;

import com.programming.entity.ClassInfo;
import com.programming.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ClassInfoMapper {
    List<ClassInfo> findAll();
    ClassInfo findById(Long id);
    void insert(ClassInfo classInfo);
    void update(ClassInfo classInfo);
    void delete(Long id);
    void insertUserClass(@Param("userId") Long userId, @Param("classId") Long classId, @Param("role") String role);
    void deleteUserClass(@Param("userId") Long userId, @Param("classId") Long classId);
    List<User> findUsersByClassId(Long classId);
    List<ClassInfo> findClassesByUserId(Long userId);
}
