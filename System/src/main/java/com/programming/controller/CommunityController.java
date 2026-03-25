package com.programming.controller;

import com.programming.entity.CommunityPost;
import com.programming.entity.CommunityComment;
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

    // 帖子相关
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
    public ResultUtil<CommunityPost> getPostById(@PathVariable Long postId) {
        try {
            CommunityPost post = communityService.getPostById(postId);
            return ResultUtil.success(post);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/post")
    public ResultUtil createPost(HttpServletRequest request, @RequestBody CommunityPost post) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            post.setUserId(userId);
            communityService.createPost(post);
            return ResultUtil.success("帖子发布成功");
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PutMapping("/post/{postId}")
    public ResultUtil updatePost(@PathVariable Long postId, @RequestBody CommunityPost post) {
        try {
            post.setId(postId);
            communityService.updatePost(post);
            return ResultUtil.success("帖子更新成功");
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @DeleteMapping("/post/{postId}")
    public ResultUtil deletePost(@PathVariable Long postId) {
        try {
            communityService.deletePost(postId);
            return ResultUtil.success("帖子删除成功");
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/post/{postId}/like")
    public ResultUtil likePost(@PathVariable Long postId) {
        try {
            communityService.likePost(postId);
            return ResultUtil.success("点赞成功");
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    // 评论相关
    @GetMapping("/post/{postId}/comments")
    public ResultUtil<List<CommunityComment>> getCommentsByPostId(@PathVariable Long postId) {
        try {
            List<CommunityComment> comments = communityService.getCommentsByPostId(postId);
            return ResultUtil.success(comments);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/post/{postId}/comment")
    public ResultUtil createComment(HttpServletRequest request, @PathVariable Long postId, @RequestBody CommunityComment comment) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            comment.setPostId(postId);
            comment.setUserId(userId);
            communityService.createComment(comment);
            return ResultUtil.success("评论发布成功");
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PutMapping("/comment/{commentId}")
    public ResultUtil updateComment(@PathVariable Long commentId, @RequestBody CommunityComment comment) {
        try {
            comment.setId(commentId);
            communityService.updateComment(comment);
            return ResultUtil.success("评论更新成功");
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @DeleteMapping("/comment/{commentId}")
    public ResultUtil deleteComment(@PathVariable Long commentId) {
        try {
            communityService.deleteComment(commentId);
            return ResultUtil.success("评论删除成功");
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @PostMapping("/comment/{commentId}/like")
    public ResultUtil likeComment(@PathVariable Long commentId) {
        try {
            communityService.likeComment(commentId);
            return ResultUtil.success("点赞成功");
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    // 用户相关
    @GetMapping("/user/posts")
    public ResultUtil<List<CommunityPost>> getUserPosts(HttpServletRequest request,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<CommunityPost> posts = communityService.getUserPosts(userId, page, size);
            return ResultUtil.success(posts);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    @GetMapping("/user/comments")
    public ResultUtil<List<CommunityComment>> getUserComments(HttpServletRequest request,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<CommunityComment> comments = communityService.getUserComments(userId, page, size);
            return ResultUtil.success(comments);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    // 统计相关
    @GetMapping("/statistics")
    public ResultUtil<Map<String, Object>> getCommunityStatistics() {
        try {
            Map<String, Object> stats = communityService.getCommunityStatistics();
            return ResultUtil.success(stats);
        } catch (Exception e) {
            return ResultUtil.error(e.getMessage());
        }
    }

    // 热点话题
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
