package ru.ifmo.se.infosec.lab1.resource;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.ifmo.se.infosec.lab1.service.auth.AuthService;
import ru.ifmo.se.infosec.lab1.service.jwt.JwtService;
import ru.ifmo.se.infosec.lab1.service.auth.LoginRequest;
import ru.ifmo.se.infosec.lab1.service.auth.LoginResponse;

@Path("/auth")
@RequestScoped
public class AuthResource {

    @Inject
    AuthService authService;

    @Inject
    JwtService jwtService;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response login(LoginRequest request) {
        if (request == null || request.getLogin() == null || request.getPassword() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Json.createObjectBuilder()
                            .add("error", "login and password are required")
                            .build())
                    .build();
        }

        String token = authService.authenticate(request.getLogin(), request.getPassword());

        if (token == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Json.createObjectBuilder()
                            .add("error", "invalid credentials")
                            .build())
                    .build();
        }

        return Response.ok(new LoginResponse(token, jwtService.getTokenTtlSeconds())).build();
    }
}