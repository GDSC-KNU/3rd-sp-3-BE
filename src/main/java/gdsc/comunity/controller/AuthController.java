package gdsc.comunity.controller;

import gdsc.comunity.dto.JwtTokensDto;
import gdsc.comunity.dto.RefreshTokenDto;
import gdsc.comunity.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<JwtTokensDto> refreshNewTokens(
            @RequestBody RefreshTokenDto refreshTokenDto
    ) {
        //1. refresh token 유효성 검증
        authService.validateRefreshToken(refreshTokenDto.refreshToken());

        JwtTokensDto newTokens = authService.refreshNewTokens(refreshTokenDto.refreshToken());
        return ResponseEntity.ok(newTokens);
    }
}
