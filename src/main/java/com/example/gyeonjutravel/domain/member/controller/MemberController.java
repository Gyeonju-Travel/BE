package com.example.gyeonjutravel.domain.member.controller;

import com.example.gyeonjutravel.domain.member.dto.request.MemberLoginRequest;
import com.example.gyeonjutravel.domain.member.dto.request.MemberSignUpRequest;
import com.example.gyeonjutravel.domain.member.dto.response.MemberAuthResponse;
import com.example.gyeonjutravel.domain.member.dto.response.MemberSignUpResponse;
import com.example.gyeonjutravel.domain.member.exception.MemberErrorCode;
import com.example.gyeonjutravel.domain.member.service.MemberService;
import com.example.gyeonjutravel.global.apiPayload.ApiResponse;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.example.gyeonjutravel.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "회원", description = "회원가입, 로그인, 로그아웃, 회원탈퇴 API")
public class MemberController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final MemberService memberService;

    @Operation(
            summary = "회원가입",
            description = "이메일, 비밀번호, 닉네임으로 신규 회원을 생성하고 JWT access token을 발급합니다."
    )
    @PostMapping("/signup")
    public ApiResponse<MemberSignUpResponse> signUp(@Valid @RequestBody MemberSignUpRequest request) {
        return ApiResponse.created(memberService.signUp(request));
    }

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호를 검증한 뒤 JWT access token을 발급합니다."
    )
    @PostMapping("/login")
    public ApiResponse<MemberAuthResponse> login(@Valid @RequestBody MemberLoginRequest request) {
        return ApiResponse.ok(memberService.login(request));
    }

    @Operation(
            summary = "로그아웃",
            description = "현재 access token을 블랙리스트에 등록해 이후 같은 토큰으로 API를 사용할 수 없게 합니다."
    )
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader
    ) {
        memberService.logout(extractToken(authorizationHeader));
        return ApiResponse.ok();
    }

    @Operation(
            summary = "회원탈퇴",
            description = "현재 로그인한 회원을 삭제하고 access token을 블랙리스트에 등록합니다."
    )
    @DeleteMapping("/withdraw")
    public ApiResponse<Void> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader
    ) {
        memberService.withdraw(userDetails.member(), extractToken(authorizationHeader));
        return ApiResponse.deleted();
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new GeneralException(MemberErrorCode.INVALID_AUTHORIZATION_HEADER);
        }
        return authorizationHeader.substring(BEARER_PREFIX.length());
    }
}
