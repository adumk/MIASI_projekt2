package com.rental.gateway.security;

import com.gateway.security.GatewayJwtValidator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class GatewayJwtValidatorTest {

    private GatewayJwtValidator jwtValidator;
    private String secret = "miasi-local-jwt-secret-key-min-32-chars-long";
    private SecretKey testKey;

    @BeforeEach
    void setUp() {
        // Ręcznie tworzymy instancję klasy testowanej, przekazując testowy klucz przez konstruktor.
        // Dzięki temu nie musimy uruchamiać całego Springa, a test wykona się w milisekundy!
        jwtValidator = new GatewayJwtValidator(secret);
        testKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void shouldAcceptValidToken() {
        // given - generujemy prawdziwy, poprawny token podpisany naszym kluczem
        String validToken = Jwts.builder()
                .subject("test-user")
                .expiration(new Date(System.currentTimeMillis() + 60_000)) // ważny przez minutę
                .signWith(testKey)
                .compact();

        // when - wywołujemy metodę walidacji
        Claims claims = jwtValidator.validate(validToken);

        // then - sprawdzamy czy pomyślnie odczytano dane z tokenu i czy podmiot się zgadza
        assertNotNull(claims, "Claims nie powinny być nullem dla poprawnego tokenu");
        assertEquals("test-user", claims.getSubject());
    }

    @Test
    void shouldThrowExpiredJwtException() {
        // given - generujemy token, który wygasł minutę temu
        String expiredToken = Jwts.builder()
                .subject("test-user")
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(testKey)
                .compact();

        // when & then - sprawdzamy, czy wywołanie metody rzuci wyjątek ExpiredJwtException
        assertThrows(ExpiredJwtException.class, () -> {
            jwtValidator.validate(expiredToken);
        }, "Wygasły token powinien rzucić ExpiredJwtException");
    }

    @Test
    void shouldThrowMalformedJwtException() {
        // given - niepoprawna struktura tekstowa (losowy ciąg znaków)
        String malformedToken = "zupelnie-losowy-tekst-ktory-nie-jest-jwt";

        // when & then
        assertThrows(MalformedJwtException.class, () -> {
            jwtValidator.validate(malformedToken);
        }, "Zły format tokenu powinien rzucić MalformedJwtException");
    }

    @Test
    void shouldThrowSignatureException() {
        // given - tworzymy token podpisany CAŁKOWICIE INNYM kluczem (symulacja sfałszowania)
        SecretKey wrongKey = Keys.hmacShaKeyFor("zly-i-podrobiony-klucz-sekretny-min-32-znaki".getBytes(StandardCharsets.UTF_8));
        String forgedToken = Jwts.builder()
                .subject("hacker")
                .signWith(wrongKey)
                .compact();

        // when & then
        assertThrows(SignatureException.class, () -> {
            jwtValidator.validate(forgedToken);
        }, "Token ze sfałszowaną sygnaturą powinien rzucić SignatureException");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForEmptyToken() {
        // given - pusty token
        String emptyToken = "";

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            jwtValidator.validate(emptyToken);
        }, "Pusty token powinien rzucić IllegalArgumentException");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForNullToken() {
        // given - token to null
        String nullToken = null;

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            jwtValidator.validate(nullToken);
        }, "Token będący nullem powinien rzucić IllegalArgumentException");
    }
}