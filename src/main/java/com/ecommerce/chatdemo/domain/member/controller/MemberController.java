package com.ecommerce.chatdemo.domain.member.controller;

import com.ecommerce.chatdemo.domain.member.dto.*;
import com.ecommerce.chatdemo.domain.member.service.MemberService;
import com.ecommerce.chatdemo.global.response.ApiResponse;
import com.ecommerce.chatdemo.global.security.annotation.LoginUser;
import com.ecommerce.chatdemo.global.security.dto.LoginUserInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(memberService.signUp(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(memberService.login(request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyInfoResponse>> getMyInfo(@LoginUser LoginUserInfo loginUserInfo) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMyInfo(loginUserInfo.id())));
    }

    /**
     * RBAC 테스트 코드 --> API 명세서 작성은 노노노
     * */
    @GetMapping("/rbac/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<String>> customerOnly() {
        return ResponseEntity.ok(ApiResponse.success("CUSTOMER 권한 확인 완료"));
    }

    @GetMapping("/rbac/super-admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> superAdminOnly() {
        return ResponseEntity.ok(ApiResponse.success("SUPER_ADMIN 권한 확인 완료"));
    }

    @GetMapping("/rbac/cs-admin")
    @PreAuthorize("hasRole('CS_ADMIN')")
    public ResponseEntity<ApiResponse<String>> csAdminOnly() {
        return ResponseEntity.ok(ApiResponse.success("CS_ADMIN 권한 확인 완료"));
    }
}