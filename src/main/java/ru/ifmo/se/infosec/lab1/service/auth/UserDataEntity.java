package ru.ifmo.se.infosec.lab1.service.auth;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "user_data")
public class UserDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Instant creationDate;

    @PrePersist
    void prePersist() {
        if (creationDate == null) {
            creationDate = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreationDate() {
        return creationDate;
    }
}
