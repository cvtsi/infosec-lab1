package ru.ifmo.se.infosec.lab1.service.auth;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.Optional;

@Stateless
public class UserRepository {
    @PersistenceContext
    EntityManager em;

    public Optional<UserEntity> findByUsername(String username) {
        try {
            UserEntity user = em.createQuery(
                    "select u from UserEntity u where u.username = :username",
                    UserEntity.class
            ).setParameter("username", username).getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Long count() {
        return em.createQuery(
                "select count(u) from UserEntity u",
                Long.class
        ).getSingleResult();
    }

    public UserEntity create(String username, String password_digest) {
        UserEntity user = new UserEntity();

        user.setUsername(username);
        user.setPasswordDigest(password_digest);

        em.persist(user);
        return user;
    }
}
