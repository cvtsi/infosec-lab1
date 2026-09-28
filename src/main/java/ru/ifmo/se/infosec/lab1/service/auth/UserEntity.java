package ru.ifmo.se.infosec.lab1.service.auth;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 24)
    private String username;

    @Column(nullable = false, length = 1024)
    private String password_digest;

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordDigest() {
        return password_digest;
    }

    public void setPasswordDigest(String password_digest) {
        this.password_digest = password_digest;
    }
}
