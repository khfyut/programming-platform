package com.programming.service.impl;

import com.programming.entity.CommunityComment;
import com.programming.entity.CommunityPost;
import com.programming.mapper.CommunityMapper;
import com.programming.service.CommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommunityServiceImpl implements CommunityService {

    @Autowired
    private CommunityMapper communityMapper;

    @Override
    public List<CommunityPost> getPosts(String type, Long pathId, Long chapterId, Long levelId, int page, int size) {
        return communityMapper.selectPosts(type, pathId, chapterId, levelId, page, size);
    }

    @Override
    public CommunityPost getPostById(Long postId) {
        CommunityPost post = communityMapper.selectPostById(postId);
        if (post != null) {
            communityMapper.incrementPostViews(postId);
        }
        return post;
    }

    @Override
    public void createPost(CommunityPost post) {
        communityMapper.insertPost(post);
    }

    @Override
    public void updatePost(CommunityPost post) {
        communityMapper.updatePost(post);
    }

    @Override
    public void deletePost(Long postId) {
        communityMapper.deletePost(postId);
    }

    @Override
    public void likePost(Long postId) {
        communityMapper.incrementPostLikes(postId);
    }

    @Override
    public List<CommunityComment> getCommentsByPostId(Long postId) {
        return communityMapper.selectCommentsByPostId(postId);
    }

    @Override
    public void createComment(CommunityComment comment) {
        communityMapper.insertComment(comment);
    }

    @Override
    public void updateComment(CommunityComment comment) {
        communityMapper.updateComment(comment);
    }

    @Override
    public void deleteComment(Long commentId) {
        communityMapper.deleteComment(commentId);
    }

    @Override
    public void likeComment(Long commentId) {
        communityMapper.incrementCommentLikes(commentId);
    }

    @Override
    public List<CommunityPost> getUserPosts(Long userId, int page, int size) {
        return communityMapper.selectPostsByUserId(userId, page, size);
    }

    @Override
    public List<CommunityComment> getUserComments(Long userId, int page, int size) {
        return communityMapper.selectCommentsByUserId(userId, page, size);
    }

    @Override
    public Map<String, Object> getCommunityStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPosts", communityMapper.countTotalPosts());
        stats.put("totalComments", communityMapper.countTotalComments());
        stats.put("activeUsers", communityMapper.countActiveUsers());
        stats.put("todayPosts", communityMapper.countTodayPosts());
        return stats;
    }

    @Override
    public List<Map<String, Object>> getHotTopics(int limit) {
        int safeLimit = limit > 0 ? limit : 10;
        return communityMapper.selectHotTopics(safeLimit);
    }
}
