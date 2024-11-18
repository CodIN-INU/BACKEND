package inu.codin.codin.domain.info.domain.professor.controller;

import inu.codin.codin.common.ResponseUtils;
import inu.codin.codin.domain.info.domain.professor.dto.ProfessorListResponseDto;
import inu.codin.codin.domain.info.domain.professor.dto.ProfessorThumbnailResponseDto;
import inu.codin.codin.domain.info.domain.professor.service.ProfessorService;
import inu.codin.codin.common.Department;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/info/professor")
@Tag(name = "Info API")
public class ProfessorController {

    private final ProfessorService professorService;

    @Operation(summary = "교수 리스트 반환")
    @GetMapping("/{department}")
    public ResponseEntity<List<ProfessorListResponseDto>> getProfessorList(@PathVariable("department") Department department){
        return ResponseUtils.success(professorService.getProfessorByDepartment(department));
    }

    @Operation(summary = "id값에 따른 교수 썸네일 반환")
    @GetMapping("/detail/{id}")
    public ResponseEntity<ProfessorThumbnailResponseDto> getProfessorThumbnail(@PathVariable("id") String id){
        return ResponseUtils.success(professorService.getProfessorThumbnail(id));
    }
}
