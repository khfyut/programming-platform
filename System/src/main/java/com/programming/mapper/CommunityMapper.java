package com.programming.mapper;

import com.programming.entity.CommunityPost;
import com.programming.entity.CommunityComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface CommunityMapper {
    // 帖子相关
    List<CommunityPost> selectPosts(@Param("type") String type, @Param("pathId") Long pathId, 
                                  @Param("chapterId") Long chapterId, @Param("levelId") Long levelId, 
                                  @Param("page") int page, @Param("size") int size);
    
    CommunityPost selectPostById(@Param("id") Long id);
    
    void insertPost(CommunityPost post);
    
    void updatePost(CommunityPost post);
    
    void deletePost(@Param("id") Long id);
    
    void incrementPostLikes(@Param("id") Long id);
    
    void incrementPostViews(@Param("id") Long id);
    
    // 评论相关
    List<CommunityComment> selectCommentsByPostId(@Param("postId") Long postId);
    
    void insertComment(CommunityComment comment);
    
    void updateComment(CommunityComment comment);
    
    void deleteComment(@Param("id") Long id);
    
    void incrementCommentLikes(@Param("id") Long id);
    
    // 用户相关
    List<CommunityPost> selectPostsByUserId(@Param("userId") Long userId, @Param("page") int page, @Param("size") int size);
    
    List<CommunityComment> selectCommentsByUserId(@Param("userId") Long userId, @Param("page") int page, @Param("size") int size);
    
    int countPostsByUserId(@Param("userId") Long userId);
    int countCommentsByUserId(@Param("userId") Long userId);
    List<CommunityPost> findRecentPostsByUserId(@Param("userId") Long userId, @Param("page") int page, @Param("size") int size);
    CommunityPost findPostById(@Param("id") Long id);
    
    // 统计相关
    int countTotalPosts();
    int countTotalComments();
    int countActiveUsers();
    int countTodayPosts();
    List<Map<String, Object>> selectHotTopics(@Param("limit") int limit);
}
