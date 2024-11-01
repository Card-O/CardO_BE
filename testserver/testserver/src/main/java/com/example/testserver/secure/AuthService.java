package com.example.testserver.secure;

import com.example.testserver.DB.User;
import com.example.testserver.DB.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReactiveAuthenticationManager authenticationManager;
    private final MyUserDetailsService myUserDetailsService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ReactiveAuthenticationManager authenticationManager,
                       MyUserDetailsService myUserDetailsService,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.myUserDetailsService = myUserDetailsService;
        this.jwtUtil = jwtUtil;
    }

    public Mono<AuthResponse> login(LoginRequest loginRequest) {
        // 인증을 수행하는 로직
        return Mono.defer(() -> {
                    // UsernamePasswordAuthenticationToken을 사용하여 인증 요청 생성
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

                    // 인증을 시도하고, 실패하면 예외 발생
                    return authenticationManager.authenticate(authenticationToken);
                })
                .flatMap(authentication -> {
                    // 인증 성공 시 사용자 이름을 가져옴
                    String username = authentication.getName();
                    return myUserDetailsService.findByUsername(username);
                })
                .flatMap(userDetails -> {
                    // JWT 생성
                    String jwt = jwtUtil.generateToken(userDetails.getUsername());
                    return Mono.just(new AuthResponse(jwt));
                })
                .onErrorResume(ex -> {
                    // 인증 실패 시 BadCredentialsException 발생
                    return Mono.error(new BadCredentialsException("Invalid credentials"));
                });
    }




    public void register(RegisterRequest registerRequest) {
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());

        // 사용자 엔티티 생성
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(encodedPassword); // 암호화된 비밀번호 설정

        // 사용자 저장
        userRepository.save(user).subscribe();
    }
}
