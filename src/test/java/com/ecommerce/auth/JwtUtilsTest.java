package com.ecommerce.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtUtilsTest {
    
    @InjectMocks
    private JwtUtils jwtUtils;
    
    private static final String TEST_SECRET = "test-secret-key-that-is-at-least-256-bits-long-for-testing-purposes-only";
    private static final String TEST_USERNAME = "testuser";
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 86400000); // 24 hours
    }
    
    @Test
    void testGenerateJwtToken_ValidAuthentication() {
        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(TEST_USERNAME);
        
        String token = jwtUtils.generateJwtToken(authentication);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
    
    @Test
    void testGetUserNameFromJwtToken_ValidToken() {
        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(TEST_USERNAME);
        
        String token = jwtUtils.generateJwtToken(authentication);
        String username = jwtUtils.getUserNameFromJwtToken(token);
        
        assertEquals(TEST_USERNAME, username);
    }
    
    @Test
    void testValidateJwtToken_ValidToken() {
        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(TEST_USERNAME);
        
        String token = jwtUtils.generateJwtToken(authentication);
        
        assertTrue(jwtUtils.validateJwtToken(token));
    }
    
    @Test
    void testValidateJwtToken_InvalidToken() {
        String invalidToken = "invalid.token.string";
        
        assertFalse(jwtUtils.validateJwtToken(invalidToken));
    }
    
    @Test
    void testValidateJwtToken_NullToken() {
        assertFalse(jwtUtils.validateJwtToken(null));
    }
    
    @Test
    void testValidateJwtToken_EmptyToken() {
        assertFalse(jwtUtils.validateJwtToken(""));
    }
    
    @Test
    void testGetUserNameFromJwtToken_InvalidToken() {
        String invalidToken = "invalid.token.string";
        
        assertThrows(Exception.class, () -> jwtUtils.getUserNameFromJwtToken(invalidToken));
    }
}
