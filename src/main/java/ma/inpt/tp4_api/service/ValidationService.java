package ma.inpt.tp4_api.service;

import ma.inpt.tp4_api.exception.EmailValidationException;
import ma.inpt.tp4_api.exception.PasswordValidationException;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class ValidationService {

    // Email validation regex pattern (RFC 5322 simplified)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Password requirements
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 50;

    /**
     * Validates email format
     * @param email Email to validate
     * @throws EmailValidationException if email is invalid
     */
    public void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new EmailValidationException("Email cannot be empty");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new EmailValidationException("Invalid email format");
        }

        if (email.length() > 255) {
            throw new EmailValidationException("Email is too long (maximum 255 characters)");
        }
    }

    /**
     * Validates password strength
     * @param password Password to validate
     * @throws PasswordValidationException if password is invalid
     */
    public void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new PasswordValidationException("Password cannot be empty");
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new PasswordValidationException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long"
            );
        }

        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw new PasswordValidationException(
                    "Password is too long (maximum " + MAX_PASSWORD_LENGTH + " characters)"
            );
        }

        // Check for at least one letter
        if (!password.matches(".*[a-zA-Z].*")) {
            throw new PasswordValidationException("Password must contain at least one letter");
        }

        // Check for at least one digit
        if (!password.matches(".*\\d.*")) {
            throw new PasswordValidationException("Password must contain at least one digit");
        }

        // Check for common weak passwords
        String lowerPassword = password.toLowerCase();
        if (lowerPassword.equals("password") ||
            lowerPassword.equals("12345678") ||
            lowerPassword.equals("qwerty123") ||
            lowerPassword.equals("admin123")) {
            throw new PasswordValidationException("Password is too common, please choose a stronger password");
        }
    }

    /**
     * Validates username
     * @param username Username to validate
     * @throws IllegalArgumentException if username is invalid
     */
    public void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        if (username.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long");
        }

        if (username.length() > 50) {
            throw new IllegalArgumentException("Username is too long (maximum 50 characters)");
        }

        if (!username.matches("^[a-zA-Z0-9_.-]+$")) {
            throw new IllegalArgumentException(
                    "Username can only contain letters, numbers, dots, hyphens, and underscores"
            );
        }
    }
}
