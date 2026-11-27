package com.chineselearning.progressservice.controller;

import com.chineselearning.progressservice.dto.ExerciseAttemptDto;
import com.chineselearning.progressservice.dto.StudentLessonProgressDto;
import com.chineselearning.progressservice.dto.StudentSummaryDto;
import com.chineselearning.progressservice.dto.StudentUnitProgressDto;
import com.chineselearning.progressservice.dto.SubmitAttemptRequest;
import com.chineselearning.progressservice.security.CurrentUser;
import com.chineselearning.progressservice.security.ProgressAccessPolicy;
import com.chineselearning.progressservice.service.AttemptService;
import com.chineselearning.progressservice.service.ProgressQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@Tag(name = "Progress Tracking", description = "Exercise attempts and lesson progress")
public class ProgressController {

    private final AttemptService attemptService;
    private final ProgressQueryService queryService;
    private final ProgressAccessPolicy accessPolicy;

    public ProgressController(AttemptService attemptService, ProgressQueryService queryService, ProgressAccessPolicy accessPolicy) {
        this.attemptService = attemptService;
        this.queryService = queryService;
        this.accessPolicy = accessPolicy;
    }

    @PostMapping("/attempts")
    @Operation(summary = "Submit an exercise attempt")
    public ResponseEntity<ExerciseAttemptDto> submitAttempt(@Valid @RequestBody SubmitAttemptRequest request, @Parameter(hidden = true) CurrentUser user) {
        accessPolicy.assertCanSubmitAttempts(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(attemptService.submitAttempt(user.id(), request));
    }

    @GetMapping("/attempts/student/{studentId}/exercise/{exerciseId}")
    @Operation(summary = "Get all attempts of a student for an exercise")
    public List<ExerciseAttemptDto> getAttempts(@PathVariable Long studentId,
                                                @PathVariable Long exerciseId,
                                                @Parameter(hidden = true) CurrentUser user) {
        accessPolicy.assertCanViewStudent(user, studentId);
        return queryService.getAttempts(studentId, exerciseId);
    }

    @GetMapping("/lessons/student/{studentId}/lesson/{lessonId}")
    @Operation(summary = "Get a student's progress for a lesson")
    public StudentLessonProgressDto getLessonProgress(@PathVariable Long studentId,
                                                      @PathVariable Long lessonId,
                                                      @Parameter(hidden = true) CurrentUser user) {
        accessPolicy.assertCanViewStudent(user, studentId);
        return queryService.getLessonProgress(studentId, lessonId);
    }

    @GetMapping("/lessons/student/{studentId}")
    @Operation(summary = "Get all progress records of a student")
    public List<StudentLessonProgressDto> getAllProgress(@PathVariable Long studentId,
                                                         @Parameter(hidden = true) CurrentUser user) {
        accessPolicy.assertCanViewStudent(user, studentId);
        return queryService.getAllProgress(studentId);
    }

    @GetMapping("/lessons/student/{studentId}/in-progress")
    @Operation(summary = "Get the lessons currently in progress")
    public List<StudentLessonProgressDto> getInProgress(@PathVariable Long studentId,
                                                        @Parameter(hidden = true) CurrentUser user) {
        accessPolicy.assertCanViewStudent(user, studentId);
        return queryService.getInProgress(studentId);
    }

    @GetMapping("/lessons/{lessonId}/leaderboard")
    @Operation(summary = "Top 10 students by completion for a lesson")
    public List<StudentLessonProgressDto> getLessonLeaderboard(@PathVariable Long lessonId) {
        return queryService.getLessonLeaderboard(lessonId);
    }

    @GetMapping("/students/{studentId}/summary")
    @Operation(summary = "Get a student's dashboard summary")
    public StudentSummaryDto getSummary(@PathVariable Long studentId,
                                        @Parameter(hidden = true) CurrentUser user) {
        accessPolicy.assertCanViewStudent(user, studentId);
        return queryService.getSummary(studentId);
    }

    @GetMapping("/units/{unitId}/student/{studentId}/progress")
    @Operation(summary = "Get a student's progress for a unit")
    public StudentUnitProgressDto getUnitProgress(@PathVariable Long unitId,
                                                  @PathVariable Long studentId,
                                                  @Parameter(hidden = true) CurrentUser user) {
        accessPolicy.assertCanViewStudent(user, studentId);
        return queryService.getUnitProgress(studentId, unitId);
    }
}