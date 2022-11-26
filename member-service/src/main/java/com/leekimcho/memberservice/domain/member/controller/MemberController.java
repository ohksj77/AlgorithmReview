package com.leekimcho.memberservice.domain.member.controller;

import com.leekimcho.memberservice.domain.member.dto.MemberDto;
import com.leekimcho.memberservice.domain.member.entity.Member;
import com.leekimcho.memberservice.domain.member.service.MemberService;
import com.leekimcho.memberservice.global.dto.Result;
import com.leekimcho.memberservice.global.utils.auth.MemberContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/member-service/api")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("member")
    public ResponseEntity<Result<GetMemberResponse>> join(MemberDto member) {
        MemberDto register = memberService.register(member);
        return ResponseEntity.ok(Result.createSuccessResult(register));
    }

    @GetMapping("/member")
    public ResponseEntity<Result<GetMemberByTokenResponse>> getMemberByToken(@Valid @RequestHeader(value="Member-id") String MemberId) {

        MemberDto memberDto = memberService.findMemberByMemberId(Long.parseLong(MemberId));

        GetMemberByTokenResponse getMemberByTokenResponse = new GetMemberByTokenResponse(memberDto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Result.createSuccessResult(getMemberByTokenResponse));
    }

    @GetMapping("/member-context")
    public ResponseEntity<Result<GetMemberResponse>> getMemberContext() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(Result.createSuccessResult(new GetMemberResponse(new MemberDto(MemberContext.currentMember.get()))));
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    static class GetMemberByTokenResponse {
        private Long MemberId;
        private String email;
        private String MemberName;
        private String phoneNumber;

        public GetMemberByTokenResponse(MemberDto memberDto) {
            this.MemberId = memberDto.getId();
            this.email = memberDto.getEmail();
            this.MemberName = memberDto.getName();
        }
    }


    @Data @NoArgsConstructor @AllArgsConstructor
    static class GetMemberResponse {
        private Long MemberId;
        private String MemberName;
        private String phoneNumber;

        public GetMemberResponse(MemberDto memberDto) {
            this.MemberId = memberDto.getId();
            this.MemberName = memberDto.getName();
        }
    }

}