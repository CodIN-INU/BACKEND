package inu.codin.codin.domain.info.domain.lab.service;

import inu.codin.codin.domain.info.domain.lab.dto.LabListResDTO;
import inu.codin.codin.domain.info.domain.lab.dto.LabThumbnailResDTO;
import inu.codin.codin.domain.info.domain.lab.entity.Lab;
import inu.codin.codin.domain.info.domain.lab.exception.LabNotFoundException;
import inu.codin.codin.domain.info.domain.professor.entity.Professor;
import inu.codin.codin.domain.info.entity.Info;
import inu.codin.codin.domain.info.repository.InfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabService {

    private final InfoRepository infoRepository;

    public LabThumbnailResDTO getLabThumbnail(String id) {
        Lab lab = infoRepository.findLabById(id)
                .orElseThrow(() -> new LabNotFoundException("연구실 정보를 찾을 수 없습니다."));
        return LabThumbnailResDTO.of(lab);
    }

    public List<LabListResDTO> getAllLab() {
        List<Lab> labs = infoRepository.findAllLabs();
        return labs.stream().map(LabListResDTO::of).toList();
    }
}
