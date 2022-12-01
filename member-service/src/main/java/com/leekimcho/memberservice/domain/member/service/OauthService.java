package com.leekimcho.memberservice.domain.member.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leekimcho.memberservice.domain.member.dto.JwtPayload;
import com.leekimcho.memberservice.domain.member.dto.LoginSuccessDto;
import com.leekimcho.memberservice.domain.member.entity.Member;
import com.leekimcho.memberservice.global.config.properties.GoogleProperties;
import com.leekimcho.memberservice.global.exception.JsonWriteException;
import com.leekimcho.memberservice.global.exception.JwtException;
import com.leekimcho.memberservice.global.utils.auth.MemberContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OauthService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final MemberService memberService;
    private final JwtService jwtService;

    private final GoogleProperties googleProperties;

    public LoginSuccessDto googleLogin(String accessToken) {
        JsonNode profile = getProfile(accessToken, googleProperties.getProfileRequestUrl());
        String email = profile.get("email").textValue();
        Optional<Member> optional = memberService.findMemberByEmail(email);
        Member member;

        if (optional.isEmpty()) { // 로그인 불가
            log.info("[GUEST 로그인] " + email);
            throw new JwtException();
        } else {                 // 로그인
            log.info("[USER 로그인] " + email);
            member = optional.get();

            String token = jwtService.createToken(new JwtPayload(member.getId(), email));
//            MemberContext.currentMember.set(member);

            return LoginSuccessDto.builder()
                    .nickname(member.getNickname())
                    .access_token(token)
                    .build();
        }
    }

    private JsonNode getProfile(String accessToken, String profileRequestUrl) {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        HttpEntity<MultiValueMap<String, String>> profileRequest = new HttpEntity<>(headers);

        ResponseEntity<String> restResponse = restTemplate.exchange(
                profileRequestUrl,
                HttpMethod.POST,
                profileRequest,
                String.class
        );
        log.info(restResponse.getBody());
        return readBody(restResponse.getBody());
    }

    private JsonNode readBody(String responseBody) {
        try {
            return objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            throw new JsonWriteException();
        }
    }
}