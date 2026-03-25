package com.programming.service;

import com.programming.entity.CommunityPost;
import com.programming.entity.CommunityComment;
import java.util.List;
import java.util.Map;

public interface CommunityService {
    // 帖子相关
    List<CommunityPost> getPosts(String type, Long pathId, Long chapterId, Long levelId, int page, int size);
    
    CommunityPost getPostById(Long postId);
    
    void createPost(CommunityPost post);
    
    void updatePost(CommunityPost post);
    
    void deletePost(Long postId);
    
    void likePost(Long postId);
    
    // 评论相关
    List<CommunityComment> getCommentsByPostId(Long postId);
    
    void createComment(CommunityComment comment);
    
    void updateComment(CommunityComment comment);
    
    void deleteComment(Long commentId);
    
    void likeComment(Long commentId);
    
    // 用户相关
    List<CommunityPost> getUserPosts(Long userId, int page, int size);
    
    List<CommunityComment> getUserComments(Long userId, int page, int size);
    
    // 统计相关
    Map<String, Object> getCommunityStatistics();
    
    // 热门话题相关
    List<Map<String, Object>> getHotTopics(int limit);
}
