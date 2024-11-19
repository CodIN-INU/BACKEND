package inu.codin.codin.domain.info.domain.office.service;

import inu.codin.codin.domain.info.domain.office.dto.*;
import inu.codin.codin.domain.info.domain.office.entity.Office;
import inu.codin.codin.common.Department;
import inu.codin.codin.domain.info.domain.office.entity.OfficeMember;
import inu.codin.codin.domain.info.repository.InfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfficeService {

    private final InfoRepository infoRepository;
    public List<OfficeListResponseDto> getAllOffice() {
        List<Office> offices = infoRepository.findAllOffices();
        return offices.stream().map(OfficeListResponseDto::of).toList();
    }

    public OfficeDetailsResponseDto getOfficeByDepartment(Department department) {
        Office office = infoRepository.findOfficeByDepartment(department);
        return OfficeDetailsResponseDto.of(office);
    }

    public void createOfficeMember(Department department, OfficeMemberCreateUpdateRequestDto officeMemberCreateUpdateRequestDto) {
        Office office = infoRepository.findOfficeByDepartment(department);
        OfficeMember officeMember = OfficeMember.of(officeMemberCreateUpdateRequestDto);
        office.addOfficeMember(officeMember);
        infoRepository.save(office);

    }

    public void updateOffice(Department department, OfficeUpdateRequestDto officeUpdateRequestDto) {
        Office office = infoRepository.findOfficeByDepartment(department);
        office.update(officeUpdateRequestDto);
        infoRepository.save(office);
    }

    public void updateOfficeMember(Department department, int num, OfficeMemberCreateUpdateRequestDto officeMemberCreateUpdateRequestDto) {
        Office office = infoRepository.findOfficeByDepartment(department);
        OfficeMember officeMember = office.getMember().get(num);
        officeMember.update(officeMemberCreateUpdateRequestDto);
        infoRepository.save(office);
    }

    public void deleteOfficeMember(Department department, int num) {
        Office office = infoRepository.findOfficeByDepartment(department);
        office.getMember().get(num).delete();
        infoRepository.save(office);
    }
}