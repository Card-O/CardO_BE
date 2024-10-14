package com.example.testserver.secure;

import com.example.testserver.DB.User;
import com.example.testserver.DB.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService myUserDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        UserDetails userDetails = myUserDetailsService.loadUserByUsername(loginRequest.getUsername());
        String jwt = jwtUtil.generateToken(userDetails.getUsername());

        return new AuthResponse(jwt);
    }

    public void register(RegisterRequest registerRequest) {
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());

        // 사용자 엔티티 생성
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(encodedPassword); // 암호화된 비밀번호 설정

        // 사용자 저장
        userRepository.save(user);
    }
}
