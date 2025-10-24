package com.analyse.resume.ResumeAnalyser.controller;

import com.analyse.resume.ResumeAnalyser.DTO.AuthResponseDTO;
import com.analyse.resume.ResumeAnalyser.model.User;
import com.analyse.resume.ResumeAnalyser.service.Impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserSaveController {

    private final UserServiceImpl userService;

    @PostMapping("/auth/register")
    public ResponseEntity<AuthResponseDTO> registerUser(@RequestBody User user) {
        return ResponseEntity.status(200).body(userService.saveUser(user));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponseDTO> loginUser(@RequestBody User loginRequest) {
        return ResponseEntity.status(200).body(userService.loginUser(loginRequest));
    }


}
