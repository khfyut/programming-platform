package com.programming.service.impl;

import com.programming.entity.PathChapter;
import com.programming.entity.PathLevel;
import com.programming.mapper.LearningPathMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningServiceImplUnlockTest {

    @Mock
    private LearningPathMapper learningPathMapper;

    @InjectMocks
    private LearningServiceImpl learningService;

    @Test
    void unlockLevelAllowsExistingLaterLevelWithoutPreviousCompletionForTesting() {
        Long userId = 7L;
        Long targetLevelId = 102L;
        Long chapterId = 10L;
        PathLevel targetLevel = new PathLevel();
        targetLevel.setId(targetLevelId);
        targetLevel.setChapterId(chapterId);

        PathChapter chapter = new PathChapter();
        chapter.setId(chapterId);
        chapter.setPathId(3L);

        when(learningPathMapper.selectLevelById(targetLevelId)).thenReturn(targetLevel);
        when(learningPathMapper.selectChapterById(chapterId)).thenReturn(chapter);

        assertTrue(learningService.unlockLevel(userId, targetLevelId));
    }
}
