package inu.codin.common.dto;

public enum Department {
    CSE("컴퓨터공학과"),
    IIE("정보통신공학과"),
    ECE("전자공학과"),
    ME("기계공학과"),
    CE("화학공학과"),
    IE("산업경영공학과"),
    EE("전기공학과"),
    OTHERS("기타");

    private final String description;

    Department(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
