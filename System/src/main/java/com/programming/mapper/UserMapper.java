package com.programming.mapper;

import com.programming.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    User findByUsername(@Param("username") String username);
    int insert(User user);
    User findById(@Param("id") Long id);
    int updateLanguage(@Param("id") Long id, @Param("language") String language);
    List<User> findByPage(@Param("page") int page, @Param("size") int size);
    int count();
    int countActiveUsers();
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);
    void updateRole(@Param("id") Long id, @Param("roleId") Long roleId);
    void updateAbilityProfile(@Param("id") Long id, @Param("abilityProfile") String abilityProfile);
    void updateProfile(@Param("id") Long id, @Param("username") String username, @Param("bio") String bio, @Param("avatarUrl") String avatarUrl, @Param("githubUrl") String githubUrl, @Param("blogUrl") String blogUrl);
    void updatePassword(@Param("id") Long id, @Param("password") String password);
    void updateStudyStats(@Param("id") Long id, @Param("totalSolved") Integer totalSolved, @Param("totalSubmissions") Integer totalSubmissions, @Param("studyHours") Integer studyHours, @Param("ranking") Integer ranking);
}
