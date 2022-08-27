package com.tojaeung.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MemberDto {

    private String username;
    private int age;

    // 기본 생성자 꼭 있어야 프로젝션 DTO 가능함
    public MemberDto() {

    }

    @QueryProjection
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
