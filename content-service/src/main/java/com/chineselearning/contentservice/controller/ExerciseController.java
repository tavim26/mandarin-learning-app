package com.chineselearning.contentservice.controller;

import com.chineselearning.contentservice.dto.ExerciseDto;
import com.chineselearning.contentservice.dto.ExerciseRequest;
import com.chineselearning.contentservice.dto.LessonExerciseTypesDto;
import com.chineselearning.contentservice.security.CurrentUser;
import com.chineselearning.contentservice.service.ExerciseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/content")
@Tag(name = "Exercises")
public class ExerciseController
{
    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService)
    {
        this.exerciseService = exerciseService;
    }

    @Operation(summary = "Get an exercise (answers hidden from students)")
    @GetMapping("/exercises/{id}")
    public ExerciseDto get(@PathVariable Long id, @Parameter(hidden = true) CurrentUser user)
    {
        ExerciseDto exercise = exerciseService.get(id);
        return user.canEditContent() ? exercise : exercise.withoutAnswers();
    }

    @Operation(summary = "Get exercises of a lesson (answers hidden from students)")
    @GetMapping("/lessons/{lessonId}/exercises")
    public List<ExerciseDto> getByLesson(@PathVariable Long lessonId, @Parameter(hidden = true) CurrentUser user)
    {
        List<ExerciseDto> exercises = exerciseService.findByLesson(lessonId);
        return user.canEditContent() ? exercises : exercises.stream().map(ExerciseDto::withoutAnswers).toList();
    }

    @Operation(summary = "Add an exercise to an owned lesson")
    @PostMapping("/exercises")
    public ResponseEntity<ExerciseDto> add(@Valid @RequestBody ExerciseRequest request,
                                           @Parameter(hidden = true) CurrentUser user)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseService.add(request, user));
    }

    @Operation(summary = "Update an exercise (owner or admin)")
    @PutMapping("/exercises/{id}")
    public ExerciseDto update(@PathVariable Long id, @Valid @RequestBody ExerciseRequest request,
                              @Parameter(hidden = true) CurrentUser user)
    {
        return exerciseService.update(id, request, user);
    }

    @Operation(summary = "Delete an exercise (owner or admin)")
    @DeleteMapping("/exercises/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @Parameter(hidden = true) CurrentUser user)
    {
        exerciseService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get exercise type distribution of a lesson")
    @GetMapping("/lessons/{id}/stats/exercise-types")
    public LessonExerciseTypesDto getTypeDistribution(@PathVariable Long id)
    {
        return exerciseService.getTypeDistribution(id);
    }
}