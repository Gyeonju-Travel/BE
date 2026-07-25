package com.example.gyeonjutravel.domain.member.dto.response;

import com.example.gyeonjutravel.domain.member.entity.Gender;
import com.example.gyeonjutravel.domain.member.entity.Member;

import java.time.LocalDate;

public record MemberResponse(
        Long memberId,
        String email,
        String name,
        LocalDate birthDate,
        Gender gender,
        String phoneNumber
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getBirthDate(),
                member.getGender(),
                member.getPhoneNumber()
        );
    }
}
