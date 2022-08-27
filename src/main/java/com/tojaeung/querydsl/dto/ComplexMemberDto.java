package com.tojaeung.querydsl.dto;

import lombok.Data;

@Data
public class ComplexMemberDto {

    private String name;
    private int age;

    public ComplexMemberDto() {
    }

    public ComplexMemberDto(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
