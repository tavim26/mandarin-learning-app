package com.chineselearning.contentservice.controller;

import com.chineselearning.contentservice.domain.dto.*;
import com.chineselearning.contentservice.security.CurrentUser;
import com.chineselearning.contentservice.service.AllowedFileType;
import com.chineselearning.contentservice.service.ContentService;
import com.chineselearning.contentservice.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/content")
@Tag(name = "Content Service API")
public class ContentController
{
    private static final String FILE_CSP = "default-src 'none'; style-src 'unsafe-inline'; sandbox";

    private final ContentService contentService;
    private final StorageService storageService;

    public ContentController(ContentService contentService, StorageService storageService)
    {
        this.contentService = contentService;
        this.storageService = storageService;
    }

    // ===== Units =====

    @Operation(summary = "Get all course units, optionally filtered by HSK level")
    @GetMapping("/units")
    public List<CourseUnitDto> getAllUnits(@RequestParam(required = false) Integer hskLevel)
    {
        return contentService.getAllCourseUnits(hskLevel);
    }

    @Operation(summary = "Get a course unit")
    @GetMapping("/units/{id}")
    public CourseUnitDto getUnit(@PathVariable Long id)
    {
        return contentService.getCourseUnit(id);
    }

    @Operation(summary = "Get a course unit with its lessons")
    @GetMapping("/units/{id}/full")
    public CourseUnitFullDto getUnitFull(@PathVariable Long id)
    {
        return contentService.getCourseUnitFull(id);
    }

    @Operation(summary = "Create a course unit")
    @PostMapping("/units")
    public ResponseEntity<CourseUnitDto> createUnit(@Valid @RequestBody CourseUnitDto dto,
                                                    @Parameter(hidden = true) CurrentUser user)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(contentService.createCourseUnit(dto, user));
    }

    @Operation(summary = "Update a course unit (owner or admin)")
    @PutMapping("/units/{id}")
    public CourseUnitDto updateUnit(@PathVariable Long id, @Valid @RequestBody CourseUnitDto dto,
                                    @Parameter(hidden = true) CurrentUser user)
    {
        return contentService.updateCourseUnit(id, dto, user);
    }

    @Operation(summary = "Delete a course unit and its lessons (owner or admin)")
    @DeleteMapping("/units/{id}")
    public ResponseEntity<Void> deleteUnit(@PathVariable Long id, @Parameter(hidden = true) CurrentUser user)
    {
        contentService.deleteCourseUnit(id, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get units created by a teacher")
    @GetMapping("/units/teacher/{teacherId}")
    public List<CourseUnitDto> getUnitsByTeacher(@PathVariable Long teacherId)
    {
        return contentService.getCourseUnitsByTeacher(teacherId);
    }

    // ===== Lessons =====

    @Operation(summary = "Get lessons of a unit")
    @GetMapping("/units/{unitId}/lessons")
    public List<LessonDto> getLessonsByUnit(@PathVariable Long unitId)
    {
        return contentService.getLessonsByUnitId(unitId);
    }

    @Operation(summary = "Get a lesson with its exercises")
    @GetMapping("/lessons/{id}")
    public LessonDto getLesson(@PathVariable Long id)
    {
        return contentService.getLesson(id);
    }

    @Operation(summary = "Create a lesson in an owned unit")
    @PostMapping("/lessons")
    public ResponseEntity<LessonDto> createLesson(@Valid @RequestBody LessonDto dto,
                                                  @Parameter(hidden = true) CurrentUser user)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(contentService.createLesson(dto, user));
    }

    @Operation(summary = "Update a lesson (owner or admin)")
    @PutMapping("/lessons/{id}")
    public LessonDto updateLesson(@PathVariable Long id, @Valid @RequestBody LessonDto dto,
                                  @Parameter(hidden = true) CurrentUser user)
    {
        return contentService.updateLesson(id, dto, user);
    }

    @Operation(summary = "Delete a lesson (owner or admin)")
    @DeleteMapping("/lessons/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id, @Parameter(hidden = true) CurrentUser user)
    {
        contentService.deleteLesson(id, user);
        return ResponseEntity.noContent().build();
    }

    // ===== Materials and files =====

    @Operation(summary = "Get materials of a lesson")
    @GetMapping("/lessons/{lessonId}/materials")
    public List<LessonMaterialDto> getMaterials(@PathVariable Long lessonId)
    {
        return contentService.getMaterialsForLesson(lessonId);
    }

    @Operation(summary = "Attach a material to an owned lesson")
    @PostMapping("/materials")
    public ResponseEntity<LessonMaterialDto> addMaterial(@Valid @RequestBody LessonMaterialDto dto,
                                                         @Parameter(hidden = true) CurrentUser user)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(contentService.addLessonMaterial(dto, user));
    }

    @Operation(summary = "Delete a material and its file (owner or admin)")
    @DeleteMapping("/materials/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id, @Parameter(hidden = true) CurrentUser user)
    {
        contentService.deleteLessonMaterial(id, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Upload a file. Allowed: JPG, PNG, GIF, PDF, DOC, DOCX, MP3, WAV, MP4 (max 50MB)")
    @PostMapping("/materials/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                                             @Parameter(hidden = true) CurrentUser user) throws IOException
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(contentService.uploadMaterialFile(file, user));
    }

    @Operation(summary = "Serve an uploaded file")
    @GetMapping("/files/{fileName}")
    public ResponseEntity<Resource> serveFile(@PathVariable String fileName)
    {
        StorageService.StoredFile file = storageService.load(fileName);
        AllowedFileType type = file.type();

        ContentDisposition disposition = (type.isInline() ? ContentDisposition.inline() : ContentDisposition.attachment())
                .filename(fileName)
                .build();

        ResponseEntity.BodyBuilder response = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(type.mediaType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.maxAge(Duration.ofDays(7)));

        // Browsers refuse to render PDFs inside a sandboxed document.
        if (type != AllowedFileType.PDF)
        {
            response.header("Content-Security-Policy", FILE_CSP);
        }

        return response.body(file.resource());
    }

    // ===== Exercises =====

    @Operation(summary = "Get an exercise")
    @GetMapping("/exercises/{id}")
    public ExerciseDto getExercise(@PathVariable Long id)
    {
        return contentService.getExercise(id);
    }

    @Operation(summary = "Get exercises of a lesson")
    @GetMapping("/lessons/{lessonId}/exercises")
    public List<ExerciseDto> getExercises(@PathVariable Long lessonId)
    {
        return contentService.getExercisesForLesson(lessonId);
    }

    @Operation(summary = "Add an exercise to an owned lesson")
    @PostMapping("/exercises")
    public ResponseEntity<ExerciseDto> addExercise(@Valid @RequestBody ExerciseDto dto,
                                                   @Parameter(hidden = true) CurrentUser user)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(contentService.addExercise(dto, user));
    }

    @Operation(summary = "Update an exercise (owner or admin)")
    @PutMapping("/exercises/{id}")
    public ExerciseDto updateExercise(@PathVariable Long id, @Valid @RequestBody ExerciseDto dto,
                                      @Parameter(hidden = true) CurrentUser user)
    {
        return contentService.updateExercise(id, dto, user);
    }

    @Operation(summary = "Delete an exercise (owner or admin)")
    @DeleteMapping("/exercises/{id}")
    public ResponseEntity<Void> deleteExercise(@PathVariable Long id, @Parameter(hidden = true) CurrentUser user)
    {
        contentService.deleteExercise(id, user);
        return ResponseEntity.noContent().build();
    }

    // ===== Statistics =====

    @Operation(summary = "Get total XP of a unit")
    @GetMapping("/units/{id}/stats/xp")
    public UnitXpStatsDto getUnitXpStats(@PathVariable Long id)
    {
        return contentService.getUnitXpStats(id);
    }

    @Operation(summary = "Get the number of lessons in a unit")
    @GetMapping("/units/{id}/stats/lessons")
    public UnitLessonCountDto getUnitLessonCount(@PathVariable Long id)
    {
        return contentService.getUnitLessonCount(id);
    }

    @Operation(summary = "Get exercise type distribution of a lesson")
    @GetMapping("/lessons/{id}/stats/exercise-types")
    public LessonExerciseTypesDto getLessonExerciseTypes(@PathVariable Long id)
    {
        return contentService.getLessonExerciseTypes(id);
    }
}