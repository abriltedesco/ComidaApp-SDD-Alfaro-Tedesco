package com.comidapp.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.comidapp.domain.enums.RolUsuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Servicio JWT — migrado de TP3 com.example.demo.service.JwtService.
 * Genera y valida tokens con claims: mail (subject), tipo, dni.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generarToken(String mail, RolUsuario tipo, int dni) {
        return Jwts.builder()
                .subject(mail)
                .claim("tipo", tipo.name())
                .claim("dni", dni)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    public String extraerMail(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public String extraerTipo(String token) {
        return extraerClaim(token, c -> c.get("tipo", String.class));
    }

    public int extraerDni(String token) {
        return extraerClaim(token, c -> c.get("dni", Integer.class));
    }

    public boolean validarToken(String token, String mail) {
        return extraerMail(token).equals(mail) && !estaExpirado(token);
    }

    private boolean estaExpirado(String token) {
        return extraerClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extraerClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }

    private SecretKey getKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
