package com.example.gyeonjutravel.domain.inquiry.service;

import com.example.gyeonjutravel.domain.inquiry.dto.request.InquiryCreateRequest;
import com.example.gyeonjutravel.domain.inquiry.dto.response.InquiryCreateResponse;
import com.example.gyeonjutravel.domain.inquiry.entity.Inquiry;
import com.example.gyeonjutravel.domain.inquiry.entity.InquiryStatus;
import com.example.gyeonjutravel.domain.inquiry.repository.InquiryRepository;
import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.member.exception.MemberErrorCode;
import com.example.gyeonjutravel.domain.member.repository.MemberRepository;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {
    private final MemberRepository memberRepository;
    private final InquiryRepository inquiryRepository;

    @Transactional
    public InquiryCreateResponse create(Long memberId, InquiryCreateRequest request) {
        Inquiry inquiry = inquiryRepository.save(Inquiry.builder()
                .member(findMember(memberId))
                .title(request.title().trim())
                .content(request.content().trim())
                .status(InquiryStatus.SUBMITTED)
                .build());
        return InquiryCreateResponse.from(inquiry);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
