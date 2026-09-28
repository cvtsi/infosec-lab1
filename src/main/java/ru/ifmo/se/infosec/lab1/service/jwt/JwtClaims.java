package ru.ifmo.se.infosec.lab1.service.jwt;

import java.time.Instant;
import java.util.Set;

public record JwtClaims(String subject, Set<String> roles, Instant expiresAt) {
}