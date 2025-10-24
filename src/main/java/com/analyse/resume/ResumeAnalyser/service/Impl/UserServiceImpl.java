package com.analyse.resume.ResumeAnalyser.service.Impl;

import com.analyse.resume.ResumeAnalyser.config.JwtUtil;
import com.analyse.resume.ResumeAnalyser.DTO.AuthResponseDTO;
import com.analyse.resume.ResumeAnalyser.exception.UserAlreadyExistsException;
import com.analyse.resume.ResumeAnalyser.model.User;
import com.analyse.resume.ResumeAnalyser.repo.UserSaveRepo;
import com.analyse.resume.ResumeAnalyser.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserSaveRepo userSaveRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DaoAuthenticationProvider daoAuthenticationProvider;

    private final AuthenticationManager authenticationManager;

    public UserServiceImpl(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public AuthResponseDTO saveUser(User user) throws UserAlreadyExistsException {
        if (userSaveRepo.findByUsername(user.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }

        String rawPassword = user.getPassword();
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        userSaveRepo.save(newUser);

        // authenticate right after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(newUser.getEmail(), rawPassword)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtUtil.generateToken((UserDetails) authentication.getPrincipal());
        return new AuthResponseDTO(token, newUser.getEmail());
    }

    @Override
    public AuthResponseDTO loginUser(User loginRequest) {
        if (loginRequest == null) {
            throw new RuntimeException("Please enter username and password to proceed further..");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword() // raw password from Postman
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        // ✅ fetch username from DB so you can return it along with token
        User dbUser = userSaveRepo.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthResponseDTO(token, dbUser.getEmail());
    }
}
