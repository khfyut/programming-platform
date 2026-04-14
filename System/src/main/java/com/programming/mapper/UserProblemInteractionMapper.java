package com.programming.mapper;

import com.programming.entity.UserProblemInteraction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserProblemInteractionMapper {

    UserProblemInteraction findByUserAndProblem(@Param("userId") Long userId,
                                                 @Param("problemId") Long problemId);

    int insert(UserProblemInteraction interaction);

    int update(UserProblemInteraction interaction);

    int incrementHintCount(@Param("userId") Long userId,
                           @Param("problemId") Long problemId);

    int incrementDiagnoseCount(@Param("userId") Long userId,
                               @Param("problemId") Long problemId);

    int incrementExplainCount(@Param("userId") Long userId,
                              @Param("problemId") Long problemId);

    int incrementRecommendCount(@Param("userId") Long userId,
                                @Param("problemId") Long problemId);

    int incrementReflectCount(@Param("userId") Long userId,
                              @Param("problemId") Long problemId);

    int updateConsecutiveFailures(@Param("userId") Long userId,
                                  @Param("problemId") Long problemId,
                                  @Param("failures") Integer failures);

    /**
     * 更新学习语义状态
     *
     * @param userId 用户ID
     * @param problemId 题目ID
     * @param errorTag 错误标签
     * @param weakPoints 薄弱知识点（JSON）
     * @param learningStage 学习阶段
     * @return 更新行数
     */
    int updateLearningState(@Param("userId") Long userId,
                            @Param("problemId") Long problemId,
                            @Param("errorTag") String errorTag,
                            @Param("weakPoints") String weakPoints,
                            @Param("learningStage") String learningStage);

    int updateActionSemanticState(@Param("userId") Long userId,
                                  @Param("problemId") Long problemId,
                                  @Param("lastActionType") String lastActionType,
                                  @Param("lastGoal") String lastGoal,
                                  @Param("lastGuidanceType") String lastGuidanceType,
                                  @Param("learningStage") String learningStage);

    List<UserProblemInteraction> findRecentByUser(@Param("userId") Long userId,
                                                  @Param("limit") Integer limit);
}
