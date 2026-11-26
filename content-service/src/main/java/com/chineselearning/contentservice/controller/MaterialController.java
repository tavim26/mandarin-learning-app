package com.chineselearning.contentservice.controller;

import com.chineselearning.contentservice.dto.LessonMaterialDto;
import com.chineselearning.contentservice.dto.MaterialRequest;
import com.chineselearning.contentservice.security.CurrentUser;
import com.chineselearning.contentservice.service.MaterialService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/content")
@Tag(name = "Lesson Materials")
public class MaterialController
{
    private final MaterialService materialService;

    public MaterialController(MaterialService materialService)
    {
        this.materialService = materialService;
    }

    @Operation(summary = "Get materials of a lesson")
    @GetMapping("/lessons/{lessonId}/materials")
    public List<LessonMaterialDto> getByLesson(@PathVariable Long lessonId)
    {
        return materialService.findByLesson(lessonId);
    }

    @Operation(summary = "Attach a material to an owned lesson")
    @PostMapping("/materials")
    public ResponseEntity<LessonMaterialDto> add(@Valid @RequestBody MaterialRequest request,
                                                 @Parameter(hidden = true) CurrentUser user)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(materialService.add(request, user));
    }

    @Operation(summary = "Delete a material and its file (owner or admin)")
    @DeleteMapping("/materials/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @Parameter(hidden = true) CurrentUser user)
    {
        materialService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Upload a file. Allowed: JPG, PNG, GIF, PDF, DOC, DOCX, MP3, WAV, MP4 (max 50MB)")
    @PostMapping("/materials/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file,
                                         @Parameter(hidden = true) CurrentUser user) throws IOException
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(materialService.upload(file, user));
    }
}