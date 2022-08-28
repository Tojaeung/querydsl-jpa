package com.tojaeung.querydsl.repository;

import com.tojaeung.querydsl.dto.MemberSearchCondition;
import com.tojaeung.querydsl.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);

}
