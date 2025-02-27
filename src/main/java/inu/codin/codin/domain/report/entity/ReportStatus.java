package inu.codin.codin.domain.report.entity;

import lombok.Getter;

@Getter
public enum ReportStatus {
    PENDING("대기"),
    SUSPENDED("정지 상태"),
    RESOLVED("처리 완료");

    private String description;

    ReportStatus(String description) {
        this.description = description;
    }
}
