package com.programming.service.impl;

import com.programming.entity.CommunityComment;
import com.programming.entity.CommunityPost;
import com.programming.exception.BusinessException;
import com.programming.mapper.CommunityMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityServiceImplTest {

    @Mock
    private CommunityMapper communityMapper;

    @InjectMocks
    private CommunityServiceImpl communityService;

    @Test
    void updatePostRejectsOtherUsersPost() {
        CommunityPost existingPost = new CommunityPost();
        existingPost.setId(1L);
        existingPost.setUserId(2L);
        when(communityMapper.selectPostById(1L)).thenReturn(existingPost);

        CommunityPost updateRequest = new CommunityPost();
        updateRequest.setId(1L);
        updateRequest.setTitle("updated");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> communityService.updatePost(updateRequest, 1L));

        assertEquals(403, exception.getCode());
        verify(communityMapper, never()).updatePost(any());
    }

    @Test
    void updatePostUsesOwnerConstraintForOwnedPost() {
        CommunityPost existingPost = new CommunityPost();
        existingPost.setId(1L);
        existingPost.setUserId(1L);
        when(communityMapper.selectPostById(1L)).thenReturn(existingPost);
        when(communityMapper.updatePost(any(CommunityPost.class))).thenReturn(1);

        CommunityPost updateRequest = new CommunityPost();
        updateRequest.setId(1L);
        updateRequest.setTitle("updated");

        communityService.updatePost(updateRequest, 1L);

        ArgumentCaptor<CommunityPost> postCaptor = ArgumentCaptor.forClass(CommunityPost.class);
        verify(communityMapper).updatePost(postCaptor.capture());
        assertEquals(1L, postCaptor.getValue().getUserId());
    }

    @Test
    void createPostReturnsPostWithGeneratedId() {
        CommunityPost createRequest = new CommunityPost();
        createRequest.setUserId(1L);
        createRequest.setTitle("Markdown note");
        createRequest.setContent("# Markdown note");

        org.mockito.Mockito.doAnswer(invocation -> {
            CommunityPost insertedPost = invocation.getArgument(0);
            insertedPost.setId(99L);
            return null;
        }).when(communityMapper).insertPost(any(CommunityPost.class));

        CommunityPost createdPost = communityService.createPost(createRequest);

        assertEquals(99L, createdPost.getId());
        assertEquals("note", createdPost.getType());
        assertEquals("public", createdPost.getVisibility());
    }

    @Test
    void deleteCommentRejectsOtherUsersComment() {
        CommunityComment existingComment = new CommunityComment();
        existingComment.setId(3L);
        existingComment.setUserId(4L);
        when(communityMapper.selectCommentById(3L)).thenReturn(existingComment);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> communityService.deleteComment(3L, 1L));

        assertEquals(403, exception.getCode());
        verify(communityMapper, never()).deleteComment(3L, 1L);
    }

    @Test
    void deleteCommentAllowsOwner() {
        CommunityComment existingComment = new CommunityComment();
        existingComment.setId(3L);
        existingComment.setUserId(1L);
        when(communityMapper.selectCommentById(3L)).thenReturn(existingComment);
        when(communityMapper.deleteComment(3L, 1L)).thenReturn(1);

        communityService.deleteComment(3L, 1L);

        verify(communityMapper).deleteComment(3L, 1L);
    }
}
