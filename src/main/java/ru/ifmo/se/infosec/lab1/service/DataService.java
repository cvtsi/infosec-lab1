package ru.ifmo.se.infosec.lab1.service;

import jakarta.ejb.Stateless;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import ru.ifmo.se.infosec.lab1.service.auth.UserDataEntity;
import ru.ifmo.se.infosec.lab1.service.auth.UserEntity;

import java.io.StringReader;
import java.time.Instant;
import java.util.List;

@Stateless
public class DataService {
    @PersistenceContext
    EntityManager entityManager;

    public JsonObject create(String username, JsonValue data) {
        UserEntity user = requireUser(username);

        UserDataEntity entity = new UserDataEntity();
        entity.setUser(user);
        entity.setContent(data == null ? JsonValue.NULL.toString() : data.toString());

        entityManager.persist(entity);
        entityManager.flush();

        return toJson(entity);
    }

    public JsonArray findByUsername(String username) {
        requireUser(username);

        List<UserDataEntity> entities = entityManager.createQuery(
                        """
                        select d
                        from UserDataEntity d
                        join d.user u
                        where u.username = :username
                        order by d.creationDate desc, d.id desc
                        """,
                        UserDataEntity.class
                )
                .setParameter("username", username)
                .getResultList();

        JsonArrayBuilder builder = Json.createArrayBuilder();

        for (UserDataEntity entity : entities) {
            builder.add(toJson(entity));
        }

        return builder.build();
    }

    private UserEntity requireUser(String username) {
        try {
            return entityManager.createQuery(
                            "select u from UserEntity u where u.username = :username",
                            UserEntity.class
                    )
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new WebApplicationException(
                    Response.status(Response.Status.UNAUTHORIZED).build()
            );
        }
    }

    private JsonObject toJson(UserDataEntity entity) {
        JsonValue data;

        try (JsonReader reader = Json.createReader(new StringReader(entity.getContent()))) {
            data = reader.readValue();
        } catch (Exception e) {
            data = Json.createValue(entity.getContent());
        }

        return Json.createObjectBuilder()
                .add("id", entity.getId())
                .add("createdAt", entity.getCreationDate() == null
                        ? Instant.now().toString()
                        : entity.getCreationDate().toString())
                .add("data", data)
                .build();
    }
}
