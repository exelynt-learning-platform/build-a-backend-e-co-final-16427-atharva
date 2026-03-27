package com.ecommerce.controller;

import java.security.Principal;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base controller with common functionality for all controllers.
 * Extracts common user retrieval logic to eliminate duplication.
 */
public abstract class BaseController {
    
    @Autowired
    protected UserRepository userRepository;
    
    /**
     * Gets the current authenticated user from the principal.
     * @param principal the security principal
     * @return the authenticated user
     * @throws ResourceNotFoundException if user not found
     */
    protected User getCurrentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
