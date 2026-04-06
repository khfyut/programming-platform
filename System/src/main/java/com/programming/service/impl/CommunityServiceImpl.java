package com.programming.service.impl;

import com.programming.entity.CommunityComment;
import com.programming.entity.CommunityPost;
import com.programming.exception.BusinessException;
import com.programming.mapper.CommunityMapper;
import com.programming.service.CommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    public void updatePost(CommunityPost post, Long operatorUserId) {
        CommunityPost existingPost = requireOwnedPost(post.getId(), operatorUserId);
        post.setUserId(existingPost.getUserId());

        if (communityMapper.updatePost(post) == 0) {
            throw new BusinessException(500, "帖子更新失败");
        }
    }

    @Override
    public void deletePost(Long postId, Long operatorUserId) {
        requireOwnedPost(postId, operatorUserId);

        if (communityMapper.deletePost(postId, operatorUserId) == 0) {
            throw new BusinessException(500, "帖子删除失败");
        }
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
    public void updateComment(CommunityComment comment, Long operatorUserId) {
        CommunityComment existingComment = requireOwnedComment(comment.getId(), operatorUserId);
        comment.setUserId(existingComment.getUserId());

        if (communityMapper.updateComment(comment) == 0) {
            throw new BusinessException(500, "评论更新失败");
        }
    }

    @Override
    public void deleteComment(Long commentId, Long operatorUserId) {
        requireOwnedComment(commentId, operatorUserId);

        if (communityMapper.deleteComment(commentId, operatorUserId) == 0) {
            throw new BusinessException(500, "评论删除失败");
        }
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

    private CommunityPost requireOwnedPost(Long postId, Long operatorUserId) {
        if (operatorUserId == null) {
            throw new BusinessException(401, "未登录");
        }

        CommunityPost existingPost = communityMapper.selectPostById(postId);
        if (existingPost == null) {
            throw new BusinessException(404, "帖子不存在");
        }
        if (!Objects.equals(existingPost.getUserId(), operatorUserId)) {
            throw new BusinessException(403, "无权操作他人的帖子");
        }
        return existingPost;
    }

    private CommunityComment requireOwnedComment(Long commentId, Long operatorUserId) {
        if (operatorUserId == null) {
            throw new BusinessException(401, "未登录");
        }

        CommunityComment existingComment = communityMapper.selectCommentById(commentId);
        if (existingComment == null) {
            throw new BusinessException(404, "评论不存在");
        }
        if (!Objects.equals(existingComment.getUserId(), operatorUserId)) {
            throw new BusinessException(403, "无权操作他人的评论");
        }
        return existingComment;
    }
}
