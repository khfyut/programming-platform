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
    public CommunityPost getPostById(Long postId, Long requesterUserId) {
        CommunityPost post = communityMapper.selectPostById(postId);
        if (post != null) {
            ensurePostReadable(post, requesterUserId);
            communityMapper.incrementPostViews(postId);
        }
        return post;
    }

    @Override
    public CommunityPost createPost(CommunityPost post) {
        normalizePostForWrite(post);
        communityMapper.insertPost(post);
        return post;
    }

    @Override
    public void updatePost(CommunityPost post, Long operatorUserId) {
        CommunityPost existingPost = requireOwnedPost(post.getId(), operatorUserId);
        post.setUserId(existingPost.getUserId());
        normalizePostForWrite(post);

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
    public void likePost(Long postId, Long requesterUserId) {
        ensurePostReadable(requirePost(postId), requesterUserId);
        communityMapper.incrementPostLikes(postId);
    }

    @Override
    public List<CommunityComment> getCommentsByPostId(Long postId, Long requesterUserId) {
        ensurePostReadable(requirePost(postId), requesterUserId);
        return communityMapper.selectCommentsByPostId(postId);
    }

    @Override
    public void createComment(CommunityComment comment) {
        ensurePostReadable(requirePost(comment.getPostId()), comment.getUserId());
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
    public void likeComment(Long commentId, Long requesterUserId) {
        CommunityComment comment = communityMapper.selectCommentById(commentId);
        if (comment == null) {
            throw new BusinessException(404, "评论不存在");
        }
        ensurePostReadable(requirePost(comment.getPostId()), requesterUserId);
        communityMapper.incrementCommentLikes(commentId);
    }

    @Override
    public List<CommunityPost> getUserPosts(Long userId, Long requesterUserId, String type, int page, int size) {
        boolean includePrivate = Objects.equals(userId, requesterUserId);
        return communityMapper.selectPostsByUserId(userId, includePrivate, type, page, size);
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

    private CommunityPost requirePost(Long postId) {
        CommunityPost post = communityMapper.selectPostById(postId);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }
        return post;
    }

    private void ensurePostReadable(CommunityPost post, Long requesterUserId) {
        if (isPrivate(post) && !Objects.equals(post.getUserId(), requesterUserId)) {
            throw new BusinessException(403, "无权访问私密动态");
        }
    }

    private boolean isPrivate(CommunityPost post) {
        return "private".equalsIgnoreCase(post.getVisibility());
    }

    private void normalizePostForWrite(CommunityPost post) {
        if (post.getType() == null || post.getType().isBlank()) {
            post.setType("note");
        }
        if ("private".equalsIgnoreCase(post.getVisibility())) {
            post.setVisibility("private");
        } else {
            post.setVisibility("public");
        }
        if (post.getTags() != null) {
            post.setTags(post.getTags().trim());
        }
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
