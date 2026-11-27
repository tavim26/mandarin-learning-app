package com.chineselearning.contentservice.controller;

import com.chineselearning.contentservice.dto.ExerciseDto;
import com.chineselearning.contentservice.dto.LessonDto;
import com.chineselearning.contentservice.service.ExerciseService;
import com.chineselearning.contentservice.service.LessonService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Service-to-service reads with full exercise data. Not routed by the gateway. */
@Hidden
@RestController
@RequestMapping("/internal")
public class InternalContentController
{
    private final ExerciseService exerciseService;
    private final LessonService lessonService;

    public InternalContentController(ExerciseService exerciseService, LessonService lessonService)
    {
        this.exerciseService = exerciseService;
        this.lessonService = lessonService;
    }

    @GetMapping("/exercises/{id}")
    public ExerciseDto getExercise(@PathVariable Long id)
    {
        return exerciseService.get(id);
    }

    @GetMapping("/lessons/{id}")
    public LessonDto getLesson(@PathVariable Long id)
    {
        return lessonService.get(id, true);
    }
}