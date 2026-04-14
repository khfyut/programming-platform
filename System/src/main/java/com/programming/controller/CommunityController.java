package com.programming.controller;

import com.programming.entity.CommunityComment;
import com.programming.entity.CommunityPost;
import com.programming.exception.BusinessException;
import com.programming.service.CommunityService;
import com.programming.util.ResultUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @GetMapping("/posts")
    public ResultUtil<List<CommunityPost>> getPosts(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long pathId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) Long levelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<CommunityPost> posts = communityService.getPosts(type, pathId, chapterId, levelId, page, size);
            return ResultUtil.success(posts);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/post/{postId}")
    public ResultUtil<CommunityPost> getPostById(HttpServletRequest request, @PathVariable Long postId) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            CommunityPost post = communityService.getPostById(postId, userId);
            return ResultUtil.success(post);
        } catch (BusinessException e) {
            return ResultUtil.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/post")
    public ResultUtil<CommunityPost> createPost(HttpServletRequest request, @RequestBody CommunityPost post) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            post.setUserId(userId);
            CommunityPost createdPost = communityService.createPost(post);
            return ResultUtil.success("帖子发布成功", createdPost);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PutMapping("/post/{postId}")
    public ResultUtil<?> updatePost(HttpServletRequest request, @PathVariable Long postId, @RequestBody CommunityPost post) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            post.setId(postId);
            communityService.updatePost(post, userId);
            return ResultUtil.success("帖子更新成功");
        } catch (BusinessException e) {
            return ResultUtil.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @DeleteMapping("/post/{postId}")
    public ResultUtil<?> deletePost(HttpServletRequest request, @PathVariable Long postId) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            communityService.deletePost(postId, userId);
            return ResultUtil.success("帖子删除成功");
        } catch (BusinessException e) {
            return ResultUtil.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/post/{postId}/like")
    public ResultUtil<?> likePost(HttpServletRequest request, @PathVariable Long postId) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            communityService.likePost(postId, userId);
            return ResultUtil.success("点赞成功");
        } catch (BusinessException e) {
            return ResultUtil.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/post/{postId}/comments")
    public ResultUtil<List<CommunityComment>> getCommentsByPostId(HttpServletRequest request, @PathVariable Long postId) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<CommunityComment> comments = communityService.getCommentsByPostId(postId, userId);
            return ResultUtil.success(comments);
        } catch (BusinessException e) {
            return ResultUtil.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/post/{postId}/comment")
    public ResultUtil<?> createComment(HttpServletRequest request, @PathVariable Long postId, @RequestBody CommunityComment comment) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            comment.setPostId(postId);
            comment.setUserId(userId);
            communityService.createComment(comment);
            return ResultUtil.success("评论发布成功");
        } catch (BusinessException e) {
            return ResultUtil.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PutMapping("/comment/{commentId}")
    public ResultUtil<?> updateComment(HttpServletRequest request, @PathVariable Long commentId, @RequestBody CommunityComment comment) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            comment.setId(commentId);
            communityService.updateComment(comment, userId);
            return ResultUtil.success("评论更新成功");
        } catch (BusinessException e) {
            return ResultUtil.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @DeleteMapping("/comment/{commentId}")
    public ResultUtil<?> deleteComment(HttpServletRequest request, @PathVariable Long commentId) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            communityService.deleteComment(commentId, userId);
            return ResultUtil.success("评论删除成功");
        } catch (BusinessException e) {
            return ResultUtil.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/comment/{commentId}/like")
    public ResultUtil<?> likeComment(HttpServletRequest request, @PathVariable Long commentId) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            communityService.likeComment(commentId, userId);
            return ResultUtil.success("点赞成功");
        } catch (BusinessException e) {
            return ResultUtil.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping({"/user/posts", "/my/posts"})
    public ResultUtil<List<CommunityPost>> getUserPosts(HttpServletRequest request,
                                                        @RequestParam(required = false) Long userId,
                                                        @RequestParam(required = false) String type,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        try {
            Long targetUserId = userId != null ? userId : (Long) request.getAttribute("userId");
            Long requesterUserId = (Long) request.getAttribute("userId");
            List<CommunityPost> posts = communityService.getUserPosts(targetUserId, requesterUserId, type, page, size);
            return ResultUtil.success(posts);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping({"/user/comments", "/my/comments"})
    public ResultUtil<List<CommunityComment>> getUserComments(HttpServletRequest request,
                                                              @RequestParam(required = false) Long userId,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size) {
        try {
            Long targetUserId = userId != null ? userId : (Long) request.getAttribute("userId");
            List<CommunityComment> comments = communityService.getUserComments(targetUserId, page, size);
            return ResultUtil.success(comments);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public ResultUtil<Map<String, Object>> getCommunityStatistics() {
        try {
            Map<String, Object> stats = communityService.getCommunityStatistics();
            return ResultUtil.success(stats);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/hot-topics")
    public ResultUtil<List<Map<String, Object>>> getHotTopics(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> hotTopics = communityService.getHotTopics(limit);
            return ResultUtil.success(hotTopics);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }
}
