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
}
