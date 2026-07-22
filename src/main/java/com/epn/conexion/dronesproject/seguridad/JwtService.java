package com.epn.conexion.dronesproject.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;


@Service
public class JwtService {

    public static final String CLAIM_ROL = "rol";

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${jwt.secret}") String secretoBase64,
                      @Value("${jwt.expiration-ms}") long expiracionMs) {
        byte[] bytes = Base64.getDecoder().decode(secretoBase64);
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(String username, String rol) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_ROL, rol)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }

    public boolean esValido(String token) {
        try {
            leerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Firma invalida, token expirado, formato corrupto o cadena vacia.
            return false;
        }
    }

    public String extraerUsername(String token) {
        return leerClaims(token).getSubject();
    }

    public String extraerRol(String token) {
        return leerClaims(token).get(CLAIM_ROL, String.class);
    }

    private Claims leerClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
