package com.example.testserver.secure;

import io.jsonwebtoken.Jwt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final MyUserDetailsService myUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(MyUserDetailsService myUserDetailsService,PasswordEncoder passwordEncoder) {
        this.myUserDetailsService = myUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, JwtUtil jwtUtil) {
        JwtFilter jwtFilter = new JwtFilter(jwtUtil,myUserDetailsService);
        return http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .authorizeExchange(authorize -> authorize
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll() // OPTIONS 요청 허용
                        .pathMatchers("/auth/**").permitAll() // 인증 관련 경로 허용
                        .anyExchange().authenticated() // 나머지 경로는 인증 필요
                )
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance()) // STATELESS
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, ex) -> {
                            return Mono.fromRunnable(() -> {
                                exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                            });
                        })
                )
                .addFilterBefore(jwtFilter, SecurityWebFiltersOrder.AUTHORIZATION) // JWT 필터 추가
                .build();
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager() {
        return authentication -> {
            String username = authentication.getName();
            String password = (String) authentication.getCredentials();
            System.out.println("username:"+username);
            System.out.println("password:"+password);
            return myUserDetailsService.findByUsername(username)
                    .doOnNext(userDetails -> {
                        System.out.println("User details found: " + userDetails);
                    })
                    .doOnError(e -> {
                        System.err.println("Error retrieving user: " + e.getMessage());
                    })
                    .flatMap(userDetails -> {
                        System.out.println(userDetails.getPassword());
                        if (passwordEncoder.matches(password, userDetails.getPassword())) {
                            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
                            return Mono.just(auth);
                        } else {
                            return Mono.error(new BadCredentialsException("Invalid credentials")); // 여기서 예외 발생
                        }
                    })
                    .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid credentials")));

        };
                // 실제 인증 로직 구현



    }
}
