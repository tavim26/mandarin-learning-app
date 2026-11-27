package com.chineselearning.progressservice.service;

import com.chineselearning.progressservice.dto.StudentReplicaDto;
import com.chineselearning.progressservice.exception.ResourceNotFoundException;
import com.chineselearning.progressservice.repository.StudentReplicaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudentReplicaService
{

    private final StudentReplicaRepository replicaRepository;

    public StudentReplicaService(StudentReplicaRepository replicaRepository) {
        this.replicaRepository = replicaRepository;
    }

    public List<StudentReplicaDto> getAllStudents() {
        return replicaRepository.findAll(Sort.by(Sort.Direction.DESC, "xpTotal").and(Sort.by("studentId")))
                .stream().map(StudentReplicaDto::from).toList();
    }

    public StudentReplicaDto getStudent(Long studentId) {
        return replicaRepository.findById(studentId)
                .map(StudentReplicaDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("Student " + studentId + " has no progress yet"));
    }

    public boolean studentExists(Long studentId) {
        return replicaRepository.existsById(studentId);
    }

    public List<StudentReplicaDto> getLeaderboard() {
        return replicaRepository.findTop10ByOrderByXpTotalDescStudentIdAsc().stream()
                .map(StudentReplicaDto::from).toList();
    }
}