package ru.ifmo.se.infosec.lab1.service.jwt;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.CallerPrincipal;
import jakarta.security.enterprise.authentication.mechanism.http.AutoApplySession;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashSet;
import java.util.Optional;

@ApplicationScoped
@AutoApplySession
public class JwtAuth implements HttpAuthenticationMechanism {

    @Inject
    JwtService jwtService;

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request,
                                                HttpServletResponse response,
                                                HttpMessageContext context) {

        String path = request.getRequestURI().substring(request.getContextPath().length());
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            Optional<JwtClaims> claims =
                    jwtService.validateToken(authorization.substring(7).trim());

            if (claims.isPresent()) {
                JwtClaims jwtClaims = claims.get();

                return context.notifyContainerAboutLogin(
                        new CallerPrincipal(jwtClaims.subject()),
                        new HashSet<>(jwtClaims.roles())
                );
            }
        }

        if (path.startsWith("/api/")) {
            response.setHeader("WWW-Authenticate", "Bearer");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return AuthenticationStatus.SEND_FAILURE;
        }

        return context.doNothing();
    }
}