package com.tojaeung.querydsl.controller;

import com.tojaeung.querydsl.domain.Member;
import com.tojaeung.querydsl.dto.MemberSearchCondition;
import com.tojaeung.querydsl.dto.MemberTeamDto;
import com.tojaeung.querydsl.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
        return memberRepository.search(condition);
    }

    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchMemberV2(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPage(condition, pageable);
    }

    @GetMapping("/v3/members")
    public List<Member> searchMemberV3() {
        return memberRepository.findAll();
    }

}
