package ru.ifmo.se.infosec.lab1.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import ru.ifmo.se.infosec.lab1.service.DataService;

import java.security.Principal;

@Path("/api/data")
@RequestScoped
public class DataResource {

    @Inject
    DataService dataService;

    @Context
    SecurityContext securityContext;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("USER")
    public Response getData() {
        return Response.ok(dataService.findByUsername(currentUsername())).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("USER")
    public Response postData(JsonValue body) {
        if (body == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(jakarta.json.Json.createObjectBuilder()
                            .add("error", "request body is required")
                            .build())
                    .build();
        }

        JsonObject saved = dataService.create(currentUsername(), body);

        return Response.status(Response.Status.CREATED).entity(saved).build();
    }

    private String currentUsername() {
        Principal principal = securityContext.getUserPrincipal();

        if (principal == null || principal.getName() == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.UNAUTHORIZED).build()
            );
        }

        return principal.getName();
    }
}