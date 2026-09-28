package ru.ifmo.se.infosec.lab1.service.jwt;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class JwtService {
    private static final String   HMAC_SHA256 = "HmacSHA256";
    private static final Duration TTL         = Duration.ofHours(1);
    private static final Duration CLOCK_SKEW  = Duration.ofSeconds(30);

    private final SecretKeySpec key;

    public JwtService() {
        try {
            String secret = System.getenv("JWT_SECRET");

            if (secret == null || secret.isBlank()) {
                secret = System.getProperty("jwt.secret");
            }

            if (secret != null) {
                secret = secret.trim();
            }

            if (secret == null || secret.isBlank()) {
                secret = "CHANGEITxDEADBEEF";
            }

            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

            if (keyBytes.length < 32) {
                keyBytes = MessageDigest.getInstance("SHA-256").digest(keyBytes);
            }

            this.key = new SecretKeySpec(keyBytes, HMAC_SHA256);
        } catch (Exception e) {
            throw new IllegalStateException("unable to initialize JWT signing key", e);
        }
    }

    public long getTokenTtlSeconds() {
        return TTL.toSeconds();
    }

    public String issueToken(String subject) {
        Instant now = Instant.now();
        long iat = now.getEpochSecond();
        long exp = now.plus(TTL).getEpochSecond();

        JsonObject header = Json.createObjectBuilder()
                .add("alg", "HS256")
                .add("typ", "JWT")
                .build();

        JsonObject payload = Json.createObjectBuilder()
                .add("sub", subject)
                .add("upn", subject)
                .add("groups", Json.createArrayBuilder().add("USER"))
                .add("iat", iat)
                .add("exp", exp)
                .add("jti", UUID.randomUUID().toString())
                .build();

        String encodedHeader  = base64Url(toJson(header).getBytes(StandardCharsets.UTF_8));
        String encodedPayload = base64Url(toJson(payload).getBytes(StandardCharsets.UTF_8));
        String signature      = base64Url(sign(encodedHeader + "." + encodedPayload));

        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    public Optional<JwtClaims> validateToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return Optional.empty();
        }

        try {
            byte[] expectedSignature = Base64.getUrlDecoder().decode(parts[2]);
            byte[] actualSignature = sign(parts[0] + "." + parts[1]);

            if (!MessageDigest.isEqual(expectedSignature, actualSignature)) {
                return Optional.empty();
            }

            JsonObject header = readJsonObject(base64UrlDecodeToString(parts[0]));
            if (!"HS256".equals(header.getString("alg", ""))) {
                return Optional.empty();
            }

            JsonObject payload = readJsonObject(base64UrlDecodeToString(parts[1]));

            String subject = payload.getString("sub", payload.getString("upn", null));
            if (subject == null || subject.isBlank()) {
                return Optional.empty();
            }

            long exp = payload.getInt("exp", 0);
            Instant expiresAt = Instant.ofEpochSecond(exp);

            if (Instant.now().isAfter(expiresAt.plus(CLOCK_SKEW))) {
                return Optional.empty();
            }

            Set<String> roles = new HashSet<>();
            JsonValue groups = payload.get("groups");

            if (groups instanceof JsonArray jsonArray) {
                for (JsonValue value : jsonArray) {
                    if (value instanceof JsonString jsonString) {
                        roles.add(jsonString.getString());
                    }
                }
            }

            if (roles.isEmpty()) {
                roles.add("USER");
            }

            return Optional.of(new JwtClaims(subject, Set.copyOf(roles), expiresAt));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private byte[] sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(key);
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to sign JWT", e);
        }
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String base64UrlDecodeToString(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private JsonObject readJsonObject(String json) {
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            return reader.readObject();
        }
    }

    private String toJson(JsonObject jsonObject) {
        StringWriter writer = new StringWriter();

        try (jakarta.json.JsonWriter jsonWriter = Json.createWriter(writer)) {
            jsonWriter.writeObject(jsonObject);
        }

        return writer.toString();
    }
}