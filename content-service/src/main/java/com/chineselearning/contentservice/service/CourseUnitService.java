package com.chineselearning.contentservice.service;

import com.chineselearning.contentservice.dto.CourseUnitDto;
import com.chineselearning.contentservice.dto.CourseUnitFullDto;
import com.chineselearning.contentservice.dto.CourseUnitRequest;
import com.chineselearning.contentservice.dto.LessonDto;
import com.chineselearning.contentservice.dto.UnitLessonCountDto;
import com.chineselearning.contentservice.dto.UnitXpStatsDto;
import com.chineselearning.contentservice.entity.CourseUnitEntity;
import com.chineselearning.contentservice.entity.LessonMaterialEntity;
import com.chineselearning.contentservice.exception.ResourceNotFoundException;
import com.chineselearning.contentservice.repository.CourseUnitRepository;
import com.chineselearning.contentservice.repository.LessonMaterialRepository;
import com.chineselearning.contentservice.repository.LessonRepository;
import com.chineselearning.contentservice.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseUnitService
{
    private final CourseUnitRepository unitRepository;
    private final LessonRepository lessonRepository;
    private final LessonMaterialRepository materialRepository;
    private final ContentAccessPolicy accessPolicy;
    private final StorageService storageService;

    public CourseUnitService(CourseUnitRepository unitRepository, LessonRepository lessonRepository,
                             LessonMaterialRepository materialRepository, ContentAccessPolicy accessPolicy,
                             StorageService storageService)
    {
        this.unitRepository = unitRepository;
        this.lessonRepository = lessonRepository;
        this.materialRepository = materialRepository;
        this.accessPolicy = accessPolicy;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public List<CourseUnitDto> findAll(Integer hskLevel)
    {
        List<CourseUnitEntity> units = (hskLevel != null)
                ? unitRepository.findByHskLevelOrderByOrderIndexAsc(hskLevel)
                : unitRepository.findAllByOrderByOrderIndexAsc();

        return units.stream().map(CourseUnitDto::from).toList();
    }

    @Transactional(readOnly = true)
    public CourseUnitDto get(Long id)
    {
        return CourseUnitDto.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public CourseUnitFullDto getFull(Long id)
    {
        CourseUnitEntity unit = findOrThrow(id);
        List<LessonDto> lessons = lessonRepository.findByUnitIdOrderByOrderIndexAsc(id).stream()
                .map(LessonDto::from)
                .toList();

        return CourseUnitFullDto.from(unit, lessons);
    }

    @Transactional(readOnly = true)
    public List<CourseUnitDto> findByTeacher(Long teacherId)
    {
        return unitRepository.findByCreatedByTeacherIdOrderByOrderIndexAsc(teacherId).stream()
                .map(CourseUnitDto::from)
                .toList();
    }

    @Transactional
    public CourseUnitDto create(CourseUnitRequest request, CurrentUser user)
    {
        accessPolicy.requireEditor(user);

        CourseUnitEntity unit = new CourseUnitEntity(request.title(), request.description(),
                request.hskLevel(), request.orderIndex(), user.id());

        return CourseUnitDto.from(unitRepository.save(unit));
    }

    @Transactional
    public CourseUnitDto update(Long id, CourseUnitRequest request, CurrentUser user)
    {
        CourseUnitEntity unit = accessPolicy.requireUnitOwner(user, id);
        unit.update(request.title(), request.description(), request.hskLevel(), request.orderIndex());

        // No save(): the entity is managed, so the transaction flushes the change.
        return CourseUnitDto.from(unit);
    }

    @Transactional
    public void delete(Long id, CurrentUser user)
    {
        CourseUnitEntity unit = accessPolicy.requireUnitOwner(user, id);

        // Read the materials first: after the delete they are gone, and their files must go too.
        List<LessonMaterialEntity> materials = materialRepository.findByLesson_Unit_Id(id);

        unitRepository.delete(unit);
        storageService.deleteFilesAfterCommit(materials);
    }

    @Transactional(readOnly = true)
    public UnitXpStatsDto getXpStats(Long unitId)
    {
        requireExists(unitId);
        return new UnitXpStatsDto(unitId, Math.toIntExact(lessonRepository.sumXpRewardByUnitId(unitId)));
    }

    @Transactional(readOnly = true)
    public UnitLessonCountDto getLessonCount(Long unitId)
    {
        requireExists(unitId);
        return new UnitLessonCountDto(unitId, Math.toIntExact(lessonRepository.countByUnitId(unitId)));
    }

    private CourseUnitEntity findOrThrow(Long id)
    {
        return unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CourseUnit not found: " + id));
    }

    private void requireExists(Long unitId)
    {
        if (!unitRepository.existsById(unitId))
        {
            throw new ResourceNotFoundException("CourseUnit not found: " + unitId);
        }
    }
}