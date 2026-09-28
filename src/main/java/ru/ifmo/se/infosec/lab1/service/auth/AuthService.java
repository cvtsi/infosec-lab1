package ru.ifmo.se.infosec.lab1.service.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.ifmo.se.infosec.lab1.service.jwt.JwtService;

import java.util.Optional;

@ApplicationScoped
public class AuthService {

    @Inject
    UserRepository users;

    @Inject
    ScryptPasswordHasher passwordHasher;

    @Inject
    JwtService jwtService;

    public String authenticate(String login, String password) {
        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            return null;
        }

        Optional<UserEntity> user = users.findByUsername(login.trim());
        Optional<String>     hash = user.map(UserEntity::getPasswordDigest);

        if (hash.isEmpty()) {
            return null;
        }

        String hashToCheck = hash.get();

        boolean valid = passwordHasher.verify(password, hashToCheck);

        if (!valid) {
            return null;
        }

        return jwtService.issueToken(user.get().getUsername());
    }
}