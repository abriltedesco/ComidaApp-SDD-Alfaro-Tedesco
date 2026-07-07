package com.comidapp.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de login — migrado de TP3 LogInDTO.
 */
public class LoginDTO {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @JsonAlias("mail")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasenia;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getContrasenia() { return contrasenia; }
    public void setContrasenia(String contrasenia) { this.contrasenia = contrasenia; }
}
