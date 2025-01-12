package inu.codin.codin.domain.info.domain.lab.service;

import inu.codin.codin.domain.info.domain.lab.dto.request.LabCreateUpdateRequestDto;
import inu.codin.codin.domain.info.domain.lab.dto.response.LabListResponseDto;
import inu.codin.codin.domain.info.domain.lab.dto.response.LabThumbnailResponseDto;
import inu.codin.codin.domain.info.domain.lab.entity.Lab;
import inu.codin.codin.domain.info.domain.lab.exception.LabNotFoundException;
import inu.codin.codin.domain.info.repository.InfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabService {

    private final InfoRepository infoRepository;

    public LabThumbnailResponseDto getLabThumbnail(String id) {
        Lab lab = infoRepository.findLabById(new ObjectId(id))
                .orElseThrow(() -> new LabNotFoundException("연구실 정보를 찾을 수 없습니다."));
        log.info("[getLabThumbnail] {}의 연구실 정보 열람", id);
        return LabThumbnailResponseDto.of(lab);
    }

    public List<LabListResponseDto> getAllLab() {
        List<Lab> labs = infoRepository.findAllLabs();
        List<LabListResponseDto> list = labs.stream().map(LabListResponseDto::of).toList();
        log.info("[getAllLab] 모든 연구실 정보 반환");
        return list;
    }

    public void createLab(LabCreateUpdateRequestDto labCreateUpdateRequestDto) {
        Lab lab = Lab.of(labCreateUpdateRequestDto);
        infoRepository.save(lab);
        log.info("[createLab] '{}'의 연구실 정보 생성", lab.getTitle());
    }

    public void updateLab(LabCreateUpdateRequestDto labCreateUpdateRequestDto, String id) {
        Lab lab = infoRepository.findLabById(new ObjectId(id))
                .orElseThrow(() -> new LabNotFoundException("연구실 정보를 찾을 수 없습니다."));
        lab.update(labCreateUpdateRequestDto);
        infoRepository.save(lab);
        log.info("[updateLab] {}의 연구실 정보 업데이트", lab.get_id().toString());
    }

    public void deleteLab(String id) {
        Lab lab = infoRepository.findLabById(new ObjectId(id))
                .orElseThrow(() -> new LabNotFoundException("연구실 정보를 찾을 수 없습니다."));
        lab.delete();
        infoRepository.save(lab);
        log.info("[deleteLab] {}의 연구실 정보 삭제", lab.get_id().toString());
    }
}
