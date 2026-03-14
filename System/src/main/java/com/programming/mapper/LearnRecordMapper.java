package com.programming.mapper;

import com.programming.entity.LearnRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LearnRecordMapper {
    LearnRecord findByUserId(@Param("userId") Long userId);
    int insert(LearnRecord learnRecord);
    int update(LearnRecord learnRecord);
}
