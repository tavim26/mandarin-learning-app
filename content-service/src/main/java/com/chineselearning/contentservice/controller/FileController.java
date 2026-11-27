package com.chineselearning.contentservice.controller;

import com.chineselearning.contentservice.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping(StorageService.FILES_PATH)
@Tag(name = "Files")
public class FileController
{
    private final StorageService storageService;

    public FileController(StorageService storageService)
    {
        this.storageService = storageService;
    }

    @Operation(summary = "Download an uploaded lesson file")
    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> download(@PathVariable String fileName)
    {
        StorageService.StoredFile file = storageService.load(fileName);

        ContentDisposition disposition = (file.type().isInline()
                ? ContentDisposition.inline()
                : ContentDisposition.attachment())
                .filename(fileName)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.type().mediaType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .body(file.resource());
    }
}