package com.chineselearning.progressservice.controller;

import com.chineselearning.progressservice.dto.StudentReplicaDto;
import com.chineselearning.progressservice.security.CurrentUser;
import com.chineselearning.progressservice.security.ProgressAccessPolicy;
import com.chineselearning.progressservice.service.StudentReplicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/progress/students")
@Tag(name = "Student Progress Summary", description = "Student XP, level and leaderboard")
public class StudentReplicaController {

    private final StudentReplicaService replicaService;
    private final ProgressAccessPolicy accessPolicy;

    public StudentReplicaController(StudentReplicaService replicaService, ProgressAccessPolicy accessPolicy) {
        this.replicaService = replicaService;
        this.accessPolicy = accessPolicy;
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Global top 10 by XP")
    public List<StudentReplicaDto> getLeaderboard() {
        return replicaService.getLeaderboard();
    }

    @GetMapping("/admin/all")
    @Operation(summary = "All student replicas (admin only)")
    public List<StudentReplicaDto> getAllStudents(@Parameter(hidden = true) CurrentUser user) {
        accessPolicy.assertAdmin(user);
        return replicaService.getAllStudents();
    }

    @GetMapping("/{studentId}")
    @Operation(summary = "Get a student's XP and level")
    public StudentReplicaDto getStudent(@PathVariable Long studentId,
                                        @Parameter(hidden = true) CurrentUser user) {
        accessPolicy.assertCanViewStudent(user, studentId);
        return replicaService.getStudent(studentId);
    }

    @GetMapping("/{studentId}/exists")
    @Operation(summary = "Whether the student has a replica (at least one attempt)")
    public boolean studentExists(@PathVariable Long studentId,
                                 @Parameter(hidden = true) CurrentUser user) {
        accessPolicy.assertCanViewStudent(user, studentId);
        return replicaService.studentExists(studentId);
    }
}