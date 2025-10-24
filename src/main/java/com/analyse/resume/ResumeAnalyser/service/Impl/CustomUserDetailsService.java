package com.analyse.resume.ResumeAnalyser.service.Impl;

import com.analyse.resume.ResumeAnalyser.model.User;
import com.analyse.resume.ResumeAnalyser.repo.UserSaveRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    private final UserSaveRepo userSaveRepo;

    public CustomUserDetailsService(UserSaveRepo userSaveRepo) {
        this.userSaveRepo = userSaveRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userSaveRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword()) // encoded password from DB
                .authorities("USER")          // optional
                .build();
    }
}
