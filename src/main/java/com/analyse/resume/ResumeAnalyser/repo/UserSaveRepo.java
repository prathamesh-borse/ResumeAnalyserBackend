package com.analyse.resume.ResumeAnalyser.repo;

import com.analyse.resume.ResumeAnalyser.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserSaveRepo extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
