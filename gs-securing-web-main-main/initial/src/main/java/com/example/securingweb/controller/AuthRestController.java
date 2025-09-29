package com.example.securingweb.controller;

import com.example.securingweb.dto.AuthRequest;
import com.example.securingweb.dto.AuthResponse;
import com.example.securingweb.model.User;
import com.example.securingweb.security.JwtTokenUtil;
import com.example.securingweb.service.UserRegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRegistrationService registrationService;

    public AuthRestController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, UserRegistrationService registrationService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.registrationService = registrationService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            String token = jwtTokenUtil.generateToken(auth);
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        User user = registrationService.registerNewUser(request.getUsername(), request.getPassword());
        return ResponseEntity.ok().body("User registered with id=" + user.getId());
    }
}

