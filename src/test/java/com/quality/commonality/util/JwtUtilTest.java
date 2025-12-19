package com.quality.commonality.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String secret = "mySecretKeyForTestingMySecretKeyForTestingMySecretKeyForTesting"; // Needs to be long enough for HS512
    private long expiration = 3600000;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);
    }

    @Test
    void generateToken_shouldGenerateValidToken() {
        Long userId = 1L;
        String username = "testuser";
        String role = "USER";

        String token = jwtUtil.generateToken(userId, username, role);

        assertNotNull(token);
        assertTrue(token.length() > 0);

        // Verify content
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();

        assertEquals(username, claims.getSubject());
        assertEquals(String.valueOf(userId), claims.getId());
        assertEquals(role, claims.get("role"));
    }
}

