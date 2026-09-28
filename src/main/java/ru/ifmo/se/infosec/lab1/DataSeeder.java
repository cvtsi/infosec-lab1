package ru.ifmo.se.infosec.lab1;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import ru.ifmo.se.infosec.lab1.service.auth.ScryptPasswordHasher;
import ru.ifmo.se.infosec.lab1.service.auth.UserRepository;

import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@Startup
public class DataSeeder {
    private static final Logger LOG = Logger.getLogger(DataSeeder.class.getName());

    @Inject
    UserRepository users;

    @Inject
    ScryptPasswordHasher passwordHasher;

    @PostConstruct
    public void createDemoUser() {
        try {
            if (users.count() == 0) {
                String username = env("DEMO_USER", "demo");
                String password = env("DEMO_PASSWORD", "demo123");

                users.create(username, passwordHasher.hash(password));
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "demo user seeding failed", e);
        }
    }

    private String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}