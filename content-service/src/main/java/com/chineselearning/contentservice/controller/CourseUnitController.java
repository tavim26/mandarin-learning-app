package com.chineselearning.contentservice.controller;

import com.chineselearning.contentservice.dto.CourseUnitDto;
import com.chineselearning.contentservice.dto.CourseUnitFullDto;
import com.chineselearning.contentservice.dto.CourseUnitRequest;
import com.chineselearning.contentservice.dto.UnitLessonCountDto;
import com.chineselearning.contentservice.dto.UnitXpStatsDto;
import com.chineselearning.contentservice.security.CurrentUser;
import com.chineselearning.contentservice.service.CourseUnitService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/content/units")
@Tag(name = "Course Units")
public class CourseUnitController
{
    private final CourseUnitService unitService;

    public CourseUnitController(CourseUnitService unitService)
    {
        this.unitService = unitService;
    }

    @Operation(summary = "Get all course units, optionally filtered by HSK level")
    @GetMapping
    public List<CourseUnitDto> getAll(@RequestParam(required = false) Integer hskLevel)
    {
        return unitService.findAll(hskLevel);
    }

    @Operation(summary = "Get a course unit")
    @GetMapping("/{id}")
    public CourseUnitDto get(@PathVariable Long id)
    {
        return unitService.get(id);
    }

    @Operation(summary = "Get a course unit with its lessons")
    @GetMapping("/{id}/full")
    public CourseUnitFullDto getFull(@PathVariable Long id)
    {
        return unitService.getFull(id);
    }

    @Operation(summary = "Get units created by a teacher")
    @GetMapping("/teacher/{teacherId}")
    public List<CourseUnitDto> getByTeacher(@PathVariable Long teacherId)
    {
        return unitService.findByTeacher(teacherId);
    }

    @Operation(summary = "Create a course unit (teacher or admin)")
    @PostMapping
    public ResponseEntity<CourseUnitDto> create(@Valid @RequestBody CourseUnitRequest request,
                                                @Parameter(hidden = true) CurrentUser user)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(unitService.create(request, user));
    }

    @Operation(summary = "Update a course unit (owner or admin)")
    @PutMapping("/{id}")
    public CourseUnitDto update(@PathVariable Long id, @Valid @RequestBody CourseUnitRequest request,
                                @Parameter(hidden = true) CurrentUser user)
    {
        return unitService.update(id, request, user);
    }

    @Operation(summary = "Delete a course unit, its lessons and their files (owner or admin)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @Parameter(hidden = true) CurrentUser user)
    {
        unitService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get total XP of a unit")
    @GetMapping("/{id}/stats/xp")
    public UnitXpStatsDto getXpStats(@PathVariable Long id)
    {
        return unitService.getXpStats(id);
    }

    @Operation(summary = "Get the number of lessons in a unit")
    @GetMapping("/{id}/stats/lessons")
    public UnitLessonCountDto getLessonCount(@PathVariable Long id)
    {
        return unitService.getLessonCount(id);
    }
}