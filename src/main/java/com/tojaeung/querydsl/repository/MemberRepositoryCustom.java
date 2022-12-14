package com.tojaeung.querydsl.repository;

import com.tojaeung.querydsl.dto.MemberSearchCondition;
import com.tojaeung.querydsl.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);

    Page<MemberTeamDto> searchPage(MemberSearchCondition condition, Pageable pageable);

}
