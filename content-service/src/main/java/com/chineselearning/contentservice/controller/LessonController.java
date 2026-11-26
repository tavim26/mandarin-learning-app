package com.chineselearning.contentservice.controller;

import com.chineselearning.contentservice.dto.LessonDto;
import com.chineselearning.contentservice.dto.LessonRequest;
import com.chineselearning.contentservice.security.CurrentUser;
import com.chineselearning.contentservice.service.LessonService;
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
@Tag(name = "Lessons")
public class LessonController
{
    private final LessonService lessonService;

    public LessonController(LessonService lessonService)
    {
        this.lessonService = lessonService;
    }

    @Operation(summary = "Get lessons of a unit")
    @GetMapping("/units/{unitId}/lessons")
    public List<LessonDto> getByUnit(@PathVariable Long unitId)
    {
        return lessonService.findByUnit(unitId);
    }

    @Operation(summary = "Get a lesson with its exercises")
    @GetMapping("/lessons/{id}")
    public LessonDto get(@PathVariable Long id)
    {
        return lessonService.get(id);
    }

    @Operation(summary = "Create a lesson in an owned unit")
    @PostMapping("/lessons")
    public ResponseEntity<LessonDto> create(@Valid @RequestBody LessonRequest request,
                                            @Parameter(hidden = true) CurrentUser user)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.create(request, user));
    }

    @Operation(summary = "Update a lesson (owner or admin)")
    @PutMapping("/lessons/{id}")
    public LessonDto update(@PathVariable Long id, @Valid @RequestBody LessonRequest request,
                            @Parameter(hidden = true) CurrentUser user)
    {
        return lessonService.update(id, request, user);
    }

    @Operation(summary = "Delete a lesson and its files (owner or admin)")
    @DeleteMapping("/lessons/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @Parameter(hidden = true) CurrentUser user)
    {
        lessonService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}