package br.com.canais.keycloak.userstorage;

public record UserEntity (
    String externalId,
    String email,
    String password
) {}
