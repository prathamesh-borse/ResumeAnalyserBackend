package com.analyse.resume.ResumeAnalyser.service;

import com.analyse.resume.ResumeAnalyser.DTO.AuthResponseDTO;
import com.analyse.resume.ResumeAnalyser.model.User;

public interface UserService {

    AuthResponseDTO saveUser(User user);

    AuthResponseDTO loginUser(User loginRequest);
}
