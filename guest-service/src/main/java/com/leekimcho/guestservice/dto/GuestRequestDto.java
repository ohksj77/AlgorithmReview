package com.leekimcho.guestservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter @Builder @Setter
public class GuestRequestDto {

    @NotBlank
    private String nickname;

    @NotBlank
    private String content;

    private Long memberId;

}
