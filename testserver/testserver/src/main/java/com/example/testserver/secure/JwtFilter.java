package com.example.testserver.secure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        String requestURI = request.getRequestURI();
        String username = null;
        String jwt = null;

        // login or register 엔드포인트인 경우 JWT 검사 건너뛰기
        if (requestURI.equals("/auth/login") || requestURI.equals("/auth/register")) {
            filterChain.doFilter(request, response); // 다음 필터로 이동
            System.out.println("JWT 검사 제외 대상");
            return; // 더 이상 JWT 검사 로직을 실행하지 않음
        }

        // JWT가 존재하는지 확인
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT가 존재하지 않습니다."); // 인증 에러
            return; // 필터 체인 종료
        }

        // JWT 추출
        jwt = authorizationHeader.substring(7);
        username = jwtUtil.extractUsername(jwt);

        // JWT가 있는 경우 해당 JWT가 유효한지 검사
        if (username != null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // JWT 유효성 검증
            if (!jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 JWT입니다."); // 유효하지 않은 경우 에러
                return; // 필터 체인 종료
            }

            // JWT가 유효한 경우에만 인증 성공
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        // 다음 필터 체인으로 진행
        filterChain.doFilter(request, response);
    }

}

