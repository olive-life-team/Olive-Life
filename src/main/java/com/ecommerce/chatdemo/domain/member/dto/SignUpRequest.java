package com.ecommerce.chatdemo.domain.member.dto;

import com.ecommerce.chatdemo.domain.member.entity.MemberRole;
import jakarta.validation.constraints.*;

public record SignUpRequest(

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,20}$",
                message = "비밀번호는 8~20자이며 영문, 숫자, 특수문자를 포함해야 합니다."
        )
        String password,

        @NotBlank(message = "이름은 필수입니다.")
        @Size(min = 2, max = 20, message = "이름은 2자 이상 20자 이하로 입력해주세요.")
        String name,

        @NotNull(message = "역할은 필수입니다.")
        MemberRole role
) {
}
