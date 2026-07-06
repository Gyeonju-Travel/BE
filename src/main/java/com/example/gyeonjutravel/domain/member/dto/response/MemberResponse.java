package com.example.gyeonjutravel.domain.member.dto.response;

import com.example.gyeonjutravel.domain.member.entity.Member;

public record MemberResponse(
        Long memberId,
        String email,
        String nickname,
        String phoneNumber
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getPhoneNumber()
        );
    }
}
