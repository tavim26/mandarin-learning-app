package com.chineselearning.contentservice.service;

import com.chineselearning.contentservice.domain.CourseUnit;
import com.chineselearning.contentservice.domain.Exercise;
import com.chineselearning.contentservice.domain.Lesson;
import com.chineselearning.contentservice.domain.LessonMaterial;
import com.chineselearning.contentservice.domain.dao.ICourseUnitDao;
import com.chineselearning.contentservice.domain.dao.IExerciseDao;
import com.chineselearning.contentservice.domain.dao.ILessonDao;
import com.chineselearning.contentservice.domain.dao.ILessonMaterialDao;
import com.chineselearning.contentservice.domain.dto.*;
import com.chineselearning.contentservice.exception.ResourceNotFoundException;
import com.chineselearning.contentservice.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ContentService
{
    private static final Set<String> MATERIAL_TYPES = Set.of("pdf", "word", "image", "audio", "video", "link");

    private final ICourseUnitDao courseUnitDao;
    private final ILessonDao lessonDao;
    private final ILessonMaterialDao lessonMaterialDao;
    private final IExerciseDao exerciseDao;
    private final StorageService storageService;
    private final ContentAccessPolicy accessPolicy;

    public ContentService(ICourseUnitDao courseUnitDao, ILessonDao lessonDao, ILessonMaterialDao lessonMaterialDao,
                          IExerciseDao exerciseDao, StorageService storageService, ContentAccessPolicy accessPolicy)
    {
        this.courseUnitDao = courseUnitDao;
        this.lessonDao = lessonDao;
        this.lessonMaterialDao = lessonMaterialDao;
        this.exerciseDao = exerciseDao;
        this.storageService = storageService;
        this.accessPolicy = accessPolicy;
    }

    // ===== Course units =====

    @Transactional(readOnly = true)
    public List<CourseUnitDto> getAllCourseUnits(Integer hskLevel)
    {
        List<CourseUnit> units = (hskLevel != null)
                ? courseUnitDao.findByHskLevel(hskLevel)
                : courseUnitDao.findAllByOrderByOrderIndexAsc();

        return units.stream()
                .map(this::mapUnitToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseUnitDto getCourseUnit(Long id)
    {
        return mapUnitToDto(findUnitOrThrow(id));
    }

    @Transactional(readOnly = true)
    public CourseUnitFullDto getCourseUnitFull(Long id)
    {
        CourseUnit unit = findUnitOrThrow(id);

        List<LessonDto> lessons = lessonDao.findByUnitIdOrderByOrderIndexAsc(id).stream()
                .map(this::mapLessonToDto)
                .collect(Collectors.toList());

        return new CourseUnitFullDto(
                unit.getId(),
                unit.getTitle(),
                unit.getDescription(),
                unit.getHskLevel(),
                unit.getOrderIndex(),
                lessons
        );
    }

    @Transactional
    public CourseUnitDto createCourseUnit(CourseUnitDto dto, CurrentUser user)
    {
        accessPolicy.requireEditor(user);

        CourseUnit unit = new CourseUnit();
        unit.setTitle(dto.getTitle());
        unit.setDescription(dto.getDescription());
        unit.setHskLevel(dto.getHskLevel());
        unit.setOrderIndex(dto.getOrderIndex());
        unit.setCreatedByTeacherId(user.id());

        return mapUnitToDto(courseUnitDao.save(unit));
    }

    @Transactional
    public CourseUnitDto updateCourseUnit(Long id, CourseUnitDto dto, CurrentUser user)
    {
        CourseUnit unit = accessPolicy.requireUnitOwner(user, id);

        unit.setTitle(dto.getTitle());
        unit.setDescription(dto.getDescription());
        unit.setHskLevel(dto.getHskLevel());
        unit.setOrderIndex(dto.getOrderIndex());

        return mapUnitToDto(courseUnitDao.save(unit));
    }

    @Transactional
    public void deleteCourseUnit(Long id, CurrentUser user)
    {
        accessPolicy.requireUnitOwner(user, id);
        courseUnitDao.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<CourseUnitDto> getCourseUnitsByTeacher(Long teacherId)
    {
        return courseUnitDao.findByCreatedByTeacherId(teacherId).stream()
                .map(this::mapUnitToDto)
                .collect(Collectors.toList());
    }

    // ===== Lessons =====

    @Transactional(readOnly = true)
    public List<LessonDto> getLessonsByUnitId(Long unitId)
    {
        return lessonDao.findByUnitIdOrderByOrderIndexAsc(unitId).stream()
                .map(this::mapLessonToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LessonDto getLesson(Long id)
    {
        LessonDto dto = mapLessonToDto(findLessonOrThrow(id));

        List<ExerciseDto> exercises = exerciseDao.findByLessonId(id).stream()
                .map(this::mapExerciseToDto)
                .collect(Collectors.toList());
        dto.setExercises(exercises);

        return dto;
    }

    @Transactional
    public LessonDto createLesson(LessonDto dto, CurrentUser user)
    {
        CourseUnit unit = accessPolicy.requireUnitOwner(user, dto.getUnitId());

        Lesson lesson = new Lesson();
        lesson.setUnit(unit);
        lesson.setTitle(dto.getTitle());
        lesson.setDescription(dto.getDescription());
        lesson.setXpReward(dto.getXpReward());
        lesson.setOrderIndex(dto.getOrderIndex());

        return mapLessonToDto(lessonDao.save(lesson));
    }

    @Transactional
    public LessonDto updateLesson(Long id, LessonDto dto, CurrentUser user)
    {
        Lesson lesson = accessPolicy.requireLessonOwner(user, id);

        // Moving a lesson requires ownership of the target unit as well.
        if (dto.getUnitId() != null && !dto.getUnitId().equals(lesson.getUnit().getId()))
        {
            lesson.setUnit(accessPolicy.requireUnitOwner(user, dto.getUnitId()));
        }

        lesson.setTitle(dto.getTitle());
        lesson.setDescription(dto.getDescription());
        lesson.setXpReward(dto.getXpReward());
        lesson.setOrderIndex(dto.getOrderIndex());

        return mapLessonToDto(lessonDao.save(lesson));
    }

    @Transactional
    public void deleteLesson(Long id, CurrentUser user)
    {
        accessPolicy.requireLessonOwner(user, id);
        lessonDao.deleteById(id);
    }

    // ===== Materials =====

    @Transactional(readOnly = true)
    public List<LessonMaterialDto> getMaterialsForLesson(Long lessonId)
    {
        return lessonMaterialDao.findByLessonId(lessonId).stream()
                .map(this::mapMaterialToDto)
                .collect(Collectors.toList());
    }

    public String uploadMaterialFile(MultipartFile file, CurrentUser user) throws IOException
    {
        accessPolicy.requireEditor(user);
        return storageService.upload(file);
    }

    @Transactional
    public LessonMaterialDto addLessonMaterial(LessonMaterialDto dto, CurrentUser user)
    {
        Lesson lesson = accessPolicy.requireLessonOwner(user, dto.getLessonId());
        String materialType = normalizeMaterialType(dto.getType());

        LessonMaterial material = new LessonMaterial();
        material.setLesson(lesson);
        material.setTitle(dto.getTitle());
        material.setType(materialType);
        material.setUrl(validateMaterialUrl(materialType, dto.getUrl()));

        return mapMaterialToDto(lessonMaterialDao.save(material));
    }

    @Transactional
    public void deleteLessonMaterial(Long id, CurrentUser user)
    {
        LessonMaterial material = accessPolicy.requireMaterialOwner(user, id);
        lessonMaterialDao.deleteById(id);

        // A link may point to another teacher's file, so only file materials delete from disk.
        if (!"link".equalsIgnoreCase(material.getType()))
        {
            storageService.fileNameFromUrl(material.getUrl()).ifPresent(storageService::delete);
        }
    }

    // ===== Exercises =====

    @Transactional(readOnly = true)
    public ExerciseDto getExercise(Long id)
    {
        return mapExerciseToDto(findExerciseOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ExerciseDto> getExercisesForLesson(Long lessonId)
    {
        return exerciseDao.findByLessonId(lessonId).stream()
                .map(this::mapExerciseToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExerciseDto addExercise(ExerciseDto dto, CurrentUser user)
    {
        // lessonId is not annotated on the DTO because the update request does not send it.
        if (dto.getLessonId() == null)
        {
            throw new IllegalArgumentException("Lesson id is mandatory");
        }
        Lesson lesson = accessPolicy.requireLessonOwner(user, dto.getLessonId());

        Exercise exercise = new Exercise();
        exercise.setLesson(lesson);
        exercise.setType(dto.getType());
        exercise.setPrompt(dto.getPrompt());
        exercise.setDifficulty(dto.getDifficulty());
        exercise.setContentData(dto.getContentData());

        return mapExerciseToDto(exerciseDao.save(exercise));
    }

    @Transactional
    public ExerciseDto updateExercise(Long id, ExerciseDto dto, CurrentUser user)
    {
        Exercise exercise = accessPolicy.requireExerciseOwner(user, id);

        exercise.setType(dto.getType());
        exercise.setPrompt(dto.getPrompt());
        exercise.setDifficulty(dto.getDifficulty());
        exercise.setContentData(dto.getContentData());

        return mapExerciseToDto(exerciseDao.save(exercise));
    }

    @Transactional
    public void deleteExercise(Long id, CurrentUser user)
    {
        accessPolicy.requireExerciseOwner(user, id);
        exerciseDao.deleteById(id);
    }

    // ===== Statistics =====

    @Transactional(readOnly = true)
    public UnitXpStatsDto getUnitXpStats(Long unitId)
    {
        requireUnitExists(unitId);

        Integer totalXp = lessonDao.findByUnitIdOrderByOrderIndexAsc(unitId).stream()
                .filter(lesson -> lesson.getXpReward() != null)
                .mapToInt(Lesson::getXpReward)
                .sum();

        return new UnitXpStatsDto(unitId, totalXp);
    }

    @Transactional(readOnly = true)
    public UnitLessonCountDto getUnitLessonCount(Long unitId)
    {
        requireUnitExists(unitId);
        int totalLessons = (int) lessonDao.countByUnitId(unitId);
        return new UnitLessonCountDto(unitId, totalLessons);
    }

    @Transactional(readOnly = true)
    public LessonExerciseTypesDto getLessonExerciseTypes(Long lessonId)
    {
        if (!lessonDao.existsById(lessonId))
        {
            throw new ResourceNotFoundException("Lesson not found: " + lessonId);
        }

        Map<String, Long> exerciseTypes = exerciseDao.findByLessonId(lessonId).stream()
                .collect(Collectors.groupingBy(Exercise::getType, Collectors.counting()));

        return new LessonExerciseTypesDto(lessonId, exerciseTypes);
    }

    // ===== Helpers =====

    private CourseUnit findUnitOrThrow(Long id)
    {
        return courseUnitDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CourseUnit not found: " + id));
    }

    private Lesson findLessonOrThrow(Long id)
    {
        return lessonDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + id));
    }

    private Exercise findExerciseOrThrow(Long id)
    {
        return exerciseDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found: " + id));
    }

    private void requireUnitExists(Long unitId)
    {
        if (!courseUnitDao.existsById(unitId))
        {
            throw new ResourceNotFoundException("CourseUnit not found: " + unitId);
        }
    }

    private String normalizeMaterialType(String type)
    {
        String normalized = type == null ? "" : type.toLowerCase(Locale.ROOT);
        if (!MATERIAL_TYPES.contains(normalized))
        {
            throw new IllegalArgumentException("Unknown material type: " + type);
        }
        return normalized;
    }

    private String validateMaterialUrl(String materialType, String url)
    {
        if (url == null || url.isBlank())
        {
            throw new IllegalArgumentException("Material URL is mandatory");
        }
        String trimmed = url.trim();

        if ("link".equals(materialType))
        {
            URI uri;
            try {
                uri = new URI(trimmed);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid link URL");
            }
            boolean isHttp = "http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme());
            if (!isHttp || uri.getHost() == null)
            {
                throw new IllegalArgumentException("Links must be absolute http or https URLs");
            }
            return trimmed;
        }

        String fileName = storageService.fileNameFromUrl(trimmed)
                .orElseThrow(() -> new IllegalArgumentException("File materials must reference a file uploaded to this platform"));

        AllowedFileType fileType = storageService.storedFileType(fileName)
                .orElseThrow(() -> new IllegalArgumentException("Uploaded file not found"));

        if (!fileType.materialType().equals(materialType))
        {
            throw new IllegalArgumentException("File type does not match material type '" + materialType + "'");
        }
        if (lessonMaterialDao.existsByUrl(trimmed))
        {
            throw new IllegalArgumentException("This file is already attached to a material");
        }
        return trimmed;
    }

    // ===== Mappers =====

    private CourseUnitDto mapUnitToDto(CourseUnit unit)
    {
        CourseUnitDto dto = new CourseUnitDto(
                unit.getId(),
                unit.getTitle(),
                unit.getDescription(),
                unit.getHskLevel(),
                unit.getOrderIndex()
        );
        dto.setCreatedByTeacherId(unit.getCreatedByTeacherId());
        return dto;
    }

    private LessonDto mapLessonToDto(Lesson lesson)
    {
        return new LessonDto(
                lesson.getId(),
                lesson.getUnit().getId(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getXpReward(),
                lesson.getOrderIndex()
        );
    }

    private LessonMaterialDto mapMaterialToDto(LessonMaterial material)
    {
        return new LessonMaterialDto(
                material.getId(),
                material.getLesson().getId(),
                material.getTitle(),
                material.getType(),
                material.getUrl()
        );
    }

    private ExerciseDto mapExerciseToDto(Exercise exercise)
    {
        return new ExerciseDto(
                exercise.getId(),
                exercise.getLesson().getId(),
                exercise.getType(),
                exercise.getPrompt(),
                exercise.getDifficulty(),
                exercise.getContentData()
        );
    }
}