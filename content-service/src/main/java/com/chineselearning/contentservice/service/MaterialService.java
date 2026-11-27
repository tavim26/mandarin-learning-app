package com.chineselearning.contentservice.service;

import com.chineselearning.contentservice.dto.LessonMaterialDto;
import com.chineselearning.contentservice.dto.MaterialRequest;
import com.chineselearning.contentservice.entity.LessonEntity;
import com.chineselearning.contentservice.entity.LessonMaterialEntity;
import com.chineselearning.contentservice.exception.InvalidRequestException;
import com.chineselearning.contentservice.repository.LessonMaterialRepository;
import com.chineselearning.contentservice.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class MaterialService
{
    private static final Set<String> MATERIAL_TYPES = Set.of("pdf", "word", "image", "audio", "video", "link");

    private final LessonMaterialRepository materialRepository;
    private final ContentAccessPolicy accessPolicy;
    private final StorageService storageService;

    public MaterialService(LessonMaterialRepository materialRepository, ContentAccessPolicy accessPolicy,
                           StorageService storageService)
    {
        this.materialRepository = materialRepository;
        this.accessPolicy = accessPolicy;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public List<LessonMaterialDto> findByLesson(Long lessonId)
    {
        return materialRepository.findByLessonIdOrderByIdAsc(lessonId).stream()
                .map(LessonMaterialDto::from)
                .toList();
    }

    public String upload(MultipartFile file, CurrentUser user) throws IOException
    {
        accessPolicy.requireEditor(user);
        return storageService.upload(file);
    }

    @Transactional
    public LessonMaterialDto add(MaterialRequest request, CurrentUser user)
    {
        LessonEntity lesson = accessPolicy.requireLessonOwner(user, request.lessonId());
        String type = normalizeType(request.type());
        String url = validateUrl(type, request.url());

        LessonMaterialEntity material = new LessonMaterialEntity(lesson, request.title(), type, url);
        return LessonMaterialDto.from(materialRepository.save(material));
    }

    @Transactional
    public void delete(Long id, CurrentUser user)
    {
        LessonMaterialEntity material = accessPolicy.requireMaterialOwner(user, id);

        materialRepository.delete(material);
        storageService.deleteFilesAfterCommit(List.of(material));
    }

    private static String normalizeType(String type)
    {
        String normalized = type == null ? "" : type.toLowerCase(Locale.ROOT);
        if (!MATERIAL_TYPES.contains(normalized))
        {
            throw new InvalidRequestException("Unknown material type: " + type);
        }
        return normalized;
    }

    private String validateUrl(String materialType, String url)
    {
        if (url == null || url.isBlank())
        {
            throw new InvalidRequestException("Material URL is mandatory");
        }
        String trimmed = url.trim();

        if (LessonMaterialEntity.TYPE_LINK.equals(materialType))
        {
            return validateLink(trimmed);
        }

        String fileName = storageService.fileNameFromUrl(trimmed)
                .orElseThrow(() -> new InvalidRequestException("File materials must reference a file uploaded to this platform"));

        AllowedFileType fileType = storageService.storedFileType(fileName)
                .orElseThrow(() -> new InvalidRequestException("Uploaded file not found"));

        if (!fileType.materialType().equals(materialType))
        {
            throw new InvalidRequestException("File type does not match material type '" + materialType + "'");
        }
        if (materialRepository.existsByUrl(trimmed))
        {
            throw new InvalidRequestException("This file is already attached to a material");
        }
        return trimmed;
    }

    private static String validateLink(String url)
    {
        URI uri;
        try
        {
            uri = new URI(url);
        }
        catch (URISyntaxException e)
        {
            throw new InvalidRequestException("Invalid link URL");
        }

        boolean isHttp = "http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme());
        if (!isHttp || uri.getHost() == null)
        {
            throw new InvalidRequestException("Links must be absolute http or https URLs");
        }
        return url;
    }
}