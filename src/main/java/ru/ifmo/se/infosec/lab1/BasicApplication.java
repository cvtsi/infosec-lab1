package ru.ifmo.se.infosec.lab1;

import jakarta.annotation.security.DeclareRoles;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/")
@DeclareRoles("USER")
public class BasicApplication extends Application {

}