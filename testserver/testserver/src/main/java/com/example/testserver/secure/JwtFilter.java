package com.example.testserver.secure;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JwtFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);


    private final JwtUtil jwtUtil;

    private final MyUserDetailsService userDetailsService;

    public JwtFilter(JwtUtil jwtUtil,MyUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        logger.debug("SecurityContext: {}", SecurityContextHolder.getContext());
        logger.info("JWT 토큰 검증 시작");

        // 요청 정보 로깅
        logger.info("요청 메서드: {}", exchange.getRequest().getMethod().name());
        logger.info("Authorization 헤더: {}", exchange.getRequest().getHeaders().getFirst("Authorization"));


        if ("OPTIONS".equalsIgnoreCase(exchange.getRequest().getMethod().name())) {
            logger.info("OPTIONS 요청, JWT 검증 생략");
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();
        logger.info("현재 요청 경로: {}", path);

        // /auth/login 및 /auth/register 경로에 대해 JWT 검증 건너뛰기
        if (path.equals("/auth/login") || path.equals("/auth/register")) {
            logger.info("JWT 검증 수행 안함");
            return chain.filter(exchange);
        }

        // Authorization 헤더에서 JWT 가져오기
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.error("JWT가 존재하지 않습니다.");
            return Mono.error(new RuntimeException("JWT가 존재하지 않습니다."));
        }

        // JWT가 존재하는 경우
        String jwt = authHeader.substring(7); // "Bearer " 이후의 토큰 부분
        String username = jwtUtil.extractUsername(jwt);
        logger.info("추출된 Username: {}", username);

        // JWT 유효성 검증 및 사용자 정보 가져오기
        return userDetailsService.findByUsername(username)
                .flatMap(userDetails -> {
                    // JWT 유효성 검증
                    boolean isValid = jwtUtil.validateToken(jwt, userDetails.getUsername());
                    logger.info("JWT 유효성 검증 결과: {}", isValid);

                    if (!isValid) {
                        logger.error("유효하지 않은 JWT 토큰: {}", jwt);
                        return Mono.error(new RuntimeException("유효하지 않은 JWT 토큰"));
                    }

                    logger.info("Username from DB: {}", userDetails.getUsername());

                    // 인증 토큰 설정
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    logger.info("Setting Authentication Token: {}", authToken.getName());

                    // SecurityContext 설정
                    SecurityContext context = new SecurityContextImpl(authToken);
                    System.out.println("SecurityContext: " + context);
                    // SecurityContext를 설정한 후 chain.filter 호출
                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    logger.error("사용자를 찾지 못했습니다.");
                    return Mono.error(new RuntimeException("사용자를 찾을 수 없습니다."));
                }))
                .onErrorResume(ex -> {
                    logger.error("에러 발생: {}", ex.getMessage());
                    return Mono.empty(); // 적절한 Mono를 반환합니다.
                });
    }

}
