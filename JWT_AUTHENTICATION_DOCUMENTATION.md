# JWT Authentication Documentation

## Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Files Created](#files-created)
4. [JWT Authentication Flow](#jwt-authentication-flow)
5. [Implementation Details](#implementation-details)
6. [Security Configuration](#security-configuration)
7. [Testing the API](#testing-the-api)
8. [Swagger UI Integration](#swagger-ui-integration)

---

## Overview

This project implements a stateless JWT (JSON Web Token) authentication system using Spring Boot 3.5.7, Spring Security, and Auth0's java-jwt library. The implementation provides secure user registration, login, and protected API endpoints.

### Key Features
- Stateless JWT-based authentication (no server-side sessions)
- BCrypt password hashing
- Token-based authorization
- 24-hour token expiration
- HMAC256 signature algorithm
- Swagger UI integration with JWT support
- RESTful API design with proper versioning

---

## Architecture

```
┌──────────────┐
│   Client     │
└──────┬───────┘
       │
       │ 1. POST /api/v1/auth/register or /login
       ▼
┌──────────────────────────────────┐
│      AuthController              │
│  - Validates credentials         │
│  - Creates/authenticates user    │
└──────┬───────────────────────────┘
       │
       │ 2. Generate JWT Token
       ▼
┌──────────────────────────────────┐
│        JwtUtil                   │
│  - Generates JWT with HMAC256    │
│  - Sets expiration (24h)         │
└──────┬───────────────────────────┘
       │
       │ 3. Returns token to client
       ▼
┌──────────────┐
│   Client     │ Stores token
└──────┬───────┘
       │
       │ 4. Subsequent requests with Authorization: Bearer <token>
       ▼
┌──────────────────────────────────┐
│  JwtAuthenticationFilter         │
│  - Intercepts ALL requests       │
│  - Extracts token from header    │
│  - Validates token               │
└──────┬───────────────────────────┘
       │
       │ 5. If valid, loads user details
       ▼
┌──────────────────────────────────┐
│  CustomUserDetailsService        │
│  - Loads user from database      │
└──────┬───────────────────────────┘
       │
       │ 6. Sets SecurityContext
       ▼
┌──────────────────────────────────┐
│   SecurityContextHolder          │
│  - Stores authentication         │
│  - Available to controllers      │
└──────┬───────────────────────────┘
       │
       │ 7. Access granted to protected endpoint
       ▼
┌──────────────────────────────────┐
│   BookController (Protected)     │
│  - Returns requested data        │
└──────────────────────────────────┘
```

---

## Files Created

### 1. Entity Layer

#### `User.java` - User Entity
**Location:** `src/main/java/ma/inpt/tp4_api/modal/User.java`

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;  // BCrypt hashed

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean enabled = true;
}
```

**Purpose:**
- Represents a user in the database
- Enforces unique constraints on username and email
- Stores BCrypt-hashed passwords (never plain text)
- `enabled` flag allows account activation/deactivation

---

### 2. Repository Layer

#### `UserRepository.java` - User Data Access
**Location:** `src/main/java/ma/inpt/tp4_api/repository/UserRepository.java`

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

**Purpose:**
- Provides database operations for User entity
- `findByUsername()` - Used during login and token validation
- `existsByUsername()` and `existsByEmail()` - Prevent duplicate registrations

---

### 3. DTO Layer (Data Transfer Objects)

#### `LoginRequest.java` - Login Credentials
**Location:** `src/main/java/ma/inpt/tp4_api/dto/LoginRequest.java`

```java
public class LoginRequest {
    private String username;
    private String password;
}
```

**Purpose:**
- Accepts login credentials from client
- Separates API contract from domain model

---

#### `RegisterRequest.java` - Registration Data
**Location:** `src/main/java/ma/inpt/tp4_api/dto/RegisterRequest.java`

```java
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
}
```

**Purpose:**
- Accepts registration data from client
- Could be extended with validation annotations (@Email, @NotBlank, etc.)

---

#### `AuthResponse.java` - Authentication Response
**Location:** `src/main/java/ma/inpt/tp4_api/dto/AuthResponse.java`

```java
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
}
```

**Purpose:**
- Returns JWT token and user info after successful authentication
- `type: "Bearer"` indicates the token type for Authorization header

---

### 4. Security Layer

#### `JwtUtil.java` - JWT Token Management
**Location:** `src/main/java/ma/inpt/tp4_api/util/JwtUtil.java`

```java
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // Generate JWT token
    public String generateToken(String username) {
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(expiration, ChronoUnit.MILLIS))
                .withIssuer("tp4-api")
                .sign(Algorithm.HMAC256(secret));
    }

    // Validate and decode JWT token
    public DecodedJWT validateToken(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withIssuer("tp4-api")
                .build();
        return verifier.verify(token);
    }

    // Extract username from token
    public String getUsernameFromToken(String token) {
        return validateToken(token).getSubject();
    }

    // Check if token is valid
    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }
}
```

**Purpose:**
- **Token Generation:** Creates JWT with HMAC256 signature
- **Token Validation:** Verifies signature, issuer, and expiration
- **Username Extraction:** Gets username from token's subject claim
- **Configuration:** Uses properties from application.properties

**JWT Token Structure:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VybmFtZSIsImlhdCI6MTYyMzQ1Njc4OSwiZXhwIjoxNjIzNTQzMTg5LCJpc3MiOiJ0cDQtYXBpIn0.signature
│─────────── Header ────────────│───────────────────── Payload ──────────────────────│─ Signature ─│
```

- **Header:** Algorithm (HS256) and token type (JWT)
- **Payload:** Subject (username), issued at, expires at, issuer
- **Signature:** HMAC256(header + payload, secret)

---

#### `JwtAuthenticationFilter.java` - Request Interceptor
**Location:** `src/main/java/ma/inpt/tp4_api/security/JwtAuthenticationFilter.java`

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                String username = jwtUtil.getUsernameFromToken(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtUtil.isTokenValid(token)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception e) {
                logger.error("Cannot set user authentication: {}", e);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

**Purpose:**
- **Intercepts every HTTP request** before it reaches controllers
- **Extracts JWT token** from Authorization header
- **Validates token** and loads user details
- **Sets authentication** in SecurityContextHolder if valid
- **Passes request** to next filter in chain

**Execution Flow:**
1. Check for `Authorization: Bearer <token>` header
2. Extract token (remove "Bearer " prefix)
3. Validate token and extract username
4. Load user details from database
5. Create authentication object and set in SecurityContext
6. Continue filter chain (request reaches controller)

---

#### `CustomUserDetailsService.java` - User Loading
**Location:** `src/main/java/ma/inpt/tp4_api/security/CustomUserDetailsService.java`

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(new ArrayList<>())  // No roles for simplicity
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .build();
    }
}
```

**Purpose:**
- Implements Spring Security's `UserDetailsService` interface
- Loads user from database by username
- Converts domain `User` to Spring Security `UserDetails`
- Used by authentication manager and JWT filter

---

### 5. Configuration Layer

#### `SecurityConfig.java` - Spring Security Configuration
**Location:** `src/main/java/ma/inpt/tp4_api/config/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // Disable CSRF for stateless API
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()  // Public endpoints
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .anyRequest().authenticated()  // All other endpoints require auth
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

**Purpose:**
- **Disables CSRF:** Not needed for stateless JWT API
- **Stateless Sessions:** No server-side session storage
- **Public Endpoints:** `/api/v1/auth/**`, Swagger UI, H2 console
- **Protected Endpoints:** Everything else requires JWT authentication
- **JWT Filter:** Processes tokens before Spring Security's default filter
- **Password Encoder:** BCrypt for secure password hashing
- **Authentication Manager:** Used by login endpoint

---

#### `OpenApiConfig.java` - Swagger Configuration
**Location:** `src/main/java/ma/inpt/tp4_api/config/OpenApiConfig.java`

```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token authentication")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }

    @Bean
    public GroupedOpenApi v1Api() {
        return GroupedOpenApi.builder()
                .group("v1")
                .pathsToMatch("/api/v1/**")
                .build();
    }
}
```

**Purpose:**
- Adds JWT authentication support to Swagger UI
- Shows "Authorize" button in Swagger UI
- Automatically includes JWT token in API requests from Swagger

---

### 6. Controller Layer

#### `AuthController.java` - Authentication Endpoints
**Location:** `src/main/java/ma/inpt/tp4_api/controller/AuthController.java`

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Username is already taken!");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Email is already in use!");
        }

        // Create new user with BCrypt hashed password
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEnabled(true);

        userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new AuthResponse(token, user.getUsername(), user.getEmail())
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate user with Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Get user details
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getUsername());

            return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getEmail()));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }
}
```

**Purpose:**
- **Registration:** Creates new user account, returns JWT token
- **Login:** Validates credentials, returns JWT token
- **Security:** Uses BCrypt for password hashing
- **Validation:** Checks for duplicate usernames and emails

---

### 7. Configuration Files

#### `application.properties` - JWT Configuration
**Location:** `src/main/resources/application.properties`

```properties
# JWT Configuration
jwt.secret=mySecretKeyForJWTTokenGenerationAndValidation123456789
jwt.expiration=86400000
```

**Purpose:**
- `jwt.secret` - Secret key for HMAC256 signature (should be strong and stored securely in production)
- `jwt.expiration` - Token expiration in milliseconds (86400000 = 24 hours)

**Production Note:** In production, use environment variables or a secrets manager for the JWT secret, not application.properties.

---

#### `pom.xml` - Dependencies Added
**Location:** `pom.xml`

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>com.auth0</groupId>
    <artifactId>java-jwt</artifactId>
    <version>4.4.0</version>
</dependency>
```

---

## JWT Authentication Flow

### Registration Flow

```
Client                    AuthController              UserRepository          JwtUtil
  │                            │                          │                      │
  │──1. POST /register─────────>│                          │                      │
  │   {username, email, pwd}    │                          │                      │
  │                             │──2. Check username───────>│                      │
  │                             │<──3. Not exists──────────│                      │
  │                             │──4. Check email──────────>│                      │
  │                             │<──5. Not exists──────────│                      │
  │                             │──6. Hash password────────│                      │
  │                             │   (BCrypt)               │                      │
  │                             │──7. Save user────────────>│                      │
  │                             │<──8. User saved──────────│                      │
  │                             │──9. Generate token───────────────────────────────>│
  │                             │<─10. JWT token───────────────────────────────────│
  │<─11. Response───────────────│                          │                      │
  │   {token, username, email}  │                          │                      │
```

### Login Flow

```
Client                    AuthController      AuthenticationManager    UserRepository    JwtUtil
  │                            │                       │                     │             │
  │──1. POST /login────────────>│                       │                     │             │
  │   {username, password}      │                       │                     │             │
  │                             │──2. Authenticate──────>│                     │             │
  │                             │                       │──3. Load user───────>│             │
  │                             │                       │<──4. User details───│             │
  │                             │                       │──5. Verify password─│             │
  │                             │<──6. Auth success─────│   (BCrypt compare)  │             │
  │                             │──7. Get user──────────────────────────────>│             │
  │                             │<──8. User─────────────────────────────────│             │
  │                             │──9. Generate token──────────────────────────────────────>│
  │                             │<─10. JWT token──────────────────────────────────────────│
  │<─11. Response───────────────│                       │                     │             │
  │   {token, username, email}  │                       │                     │             │
```

### Protected Endpoint Access Flow

```
Client          JwtAuthFilter       JwtUtil    UserDetailsService    SecurityContext    Controller
  │                   │                 │              │                    │               │
  │──1. GET /books────>│                 │              │                    │               │
  │   Header:          │                 │              │                    │               │
  │   Authorization:   │                 │              │                    │               │
  │   Bearer <token>   │                 │              │                    │               │
  │                    │──2. Extract─────│              │                    │               │
  │                    │    token        │              │                    │               │
  │                    │──3. Validate────>│              │                    │               │
  │                    │    token        │              │                    │               │
  │                    │<──4. Username───│              │                    │               │
  │                    │──5. Load user───────────────────>│                    │               │
  │                    │<──6. UserDetails─────────────────│                    │               │
  │                    │──7. Set auth─────────────────────────────────────────>│               │
  │                    │──8. Continue────────────────────────────────────────────────────────>│
  │                    │    filter chain                  │                    │               │
  │                    │                 │              │                    │               │
  │<──9. Response──────────────────────────────────────────────────────────────────────────────│
  │   {books data}     │                 │              │                    │               │
```

### Invalid/Missing Token Flow

```
Client          JwtAuthFilter       SecurityConfig      Response
  │                   │                     │               │
  │──1. GET /books────>│                     │               │
  │   (No Auth header) │                     │               │
  │                    │──2. No token────────│               │
  │                    │    found            │               │
  │                    │──3. Continue────────>│               │
  │                    │    filter           │               │
  │                    │                     │──4. Deny──────>│
  │                    │                     │   (No auth)   │
  │<──5. HTTP 403──────────────────────────────────────────────│
  │   Forbidden        │                     │               │
```

---

## Implementation Details

### Password Security

**BCrypt Hashing:**
- Passwords are NEVER stored in plain text
- BCrypt automatically handles salting
- Each password hash is unique even if passwords are identical
- Computational cost factor prevents brute-force attacks

**Example:**
```
Plain password: "password123"
BCrypt hash:    "$2a$10$N9qo8uLOickgx2ZMRZoMy.eX5qWczKfCU1WOPfvxKWQgKEfWz/kWG"
                  │   │  └─────────────────────────────────────────────────┘
                  │   │              Actual hash + salt
                  │   └── Cost factor (2^10 = 1024 rounds)
                  └────── BCrypt version
```

### Token Validation

**Validation Steps:**
1. **Signature Verification:** Ensures token wasn't tampered with
2. **Issuer Check:** Verifies token was issued by our application ("tp4-api")
3. **Expiration Check:** Ensures token hasn't expired
4. **Structure Validation:** Confirms proper JWT format

**Security Considerations:**
- Token signature prevents tampering
- Short expiration limits damage if token is stolen
- No sensitive data stored in token (only username)
- Token can't be revoked (limitation of stateless JWT)

### Stateless Authentication

**Benefits:**
- **Scalability:** No server-side session storage required
- **Distributed Systems:** Works across multiple servers
- **Performance:** No database lookups to validate sessions
- **Simplicity:** No session management complexity

**Trade-offs:**
- **Token Revocation:** Can't invalidate tokens before expiration
- **Token Size:** JWT is larger than session ID
- **Secret Management:** Secret key must be protected

---

## Security Configuration

### Endpoint Security Matrix

| Endpoint | Authentication Required | Description |
|----------|------------------------|-------------|
| `POST /api/v1/auth/register` | ❌ No | Public - User registration |
| `POST /api/v1/auth/login` | ❌ No | Public - User login |
| `GET /api/v1/books` | ✅ Yes | Protected - Requires JWT |
| `POST /api/v1/books` | ✅ Yes | Protected - Requires JWT |
| `PUT /api/v1/books/{id}` | ✅ Yes | Protected - Requires JWT |
| `DELETE /api/v1/books/{id}` | ✅ Yes | Protected - Requires JWT |
| `/h2-console/**` | ❌ No | Public - Development only |
| `/swagger-ui/**` | ❌ No | Public - API documentation |

### CORS Configuration

CORS is enabled for development (all origins allowed). In production, restrict to specific domains:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("https://yourdomain.com"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    // ...
}
```

---

## Testing the API

### 1. Register a New User

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "email": "john@example.com",
    "password": "secret123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "john",
  "email": "john@example.com"
}
```

### 2. Login

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "secret123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "john",
  "email": "john@example.com"
}
```

### 3. Access Protected Endpoint (Without Token)

**Request:**
```bash
curl -X GET http://localhost:8080/api/v1/books
```

**Response:**
```
HTTP 403 Forbidden
```

### 4. Access Protected Endpoint (With Token)

**Request:**
```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET http://localhost:8080/api/v1/books \
  -H "Authorization: Bearer $TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "Books retrieved successfully",
  "data": [...]
}
```

### 5. Create Resource (With Authentication)

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/books \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Clean Code",
    "author": "Robert Martin",
    "category": "Programming",
    "year": 2008,
    "price": 45.99
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Book created successfully",
  "data": {
    "id": 1,
    "title": "Clean Code",
    "author": "Robert Martin",
    "category": "Programming",
    "year": 2008,
    "price": 45.99
  }
}
```

---

## Swagger UI Integration

### Accessing Swagger UI

Navigate to: **http://localhost:8080/swagger-ui.html**

### Using JWT Authentication in Swagger

1. **Register or Login:**
   - Expand `/api/v1/auth/register` or `/api/v1/auth/login`
   - Click "Try it out"
   - Enter credentials
   - Click "Execute"
   - Copy the `token` value from response

2. **Authorize:**
   - Click the **"Authorize"** button (🔓 lock icon) at the top right
   - In the dialog, paste your token (WITHOUT the "Bearer " prefix)
   - Click **"Authorize"**
   - Click **"Close"**

3. **Make Authenticated Requests:**
   - All subsequent API calls will automatically include your JWT token
   - Lock icons next to endpoints indicate authentication status:
     - 🔒 = Protected (requires authentication)
     - 🔓 = Public (no authentication required)

4. **Test Protected Endpoints:**
   - Try `/api/v1/books` GET, POST, PUT, DELETE
   - All should work with valid token
   - Invalid/missing token returns 403 Forbidden

---

## Production Considerations

### 1. Secret Key Management

**Current (Development):**
```properties
jwt.secret=mySecretKeyForJWTTokenGenerationAndValidation123456789
```

**Production:**
- Use environment variables:
  ```properties
  jwt.secret=${JWT_SECRET}
  ```
- Or use AWS Secrets Manager, Azure Key Vault, HashiCorp Vault
- Generate strong secret (256+ bits): `openssl rand -base64 64`

### 2. Token Expiration Strategy

**Current:** 24 hours

**Recommended for Production:**
- **Access Token:** Short-lived (15 minutes)
- **Refresh Token:** Long-lived (7 days)
- Implement refresh token rotation

### 3. HTTPS Only

In production, enforce HTTPS:
```properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_PASSWORD}
server.ssl.key-store-type=PKCS12
```

### 4. Rate Limiting

Implement rate limiting on auth endpoints to prevent brute force:
```java
// Using Bucket4j or similar
@RateLimiter(name = "authLimiter")
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    // ...
}
```

### 5. Token Blacklisting

For logout or token revocation, maintain a token blacklist in Redis:
```java
@Service
public class TokenBlacklistService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void blacklistToken(String token, long expirationTime) {
        redisTemplate.opsForValue().set(token, "blacklisted", expirationTime, TimeUnit.MILLISECONDS);
    }

    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }
}
```

### 6. Security Headers

Add security headers:
```java
http.headers(headers -> headers
    .contentSecurityPolicy("default-src 'self'")
    .xssProtection()
    .frameOptions().deny()
);
```

### 7. Database Security

- Use PostgreSQL or MySQL (not H2) in production
- Encrypt sensitive data at rest
- Use database connection pooling
- Implement proper backup strategy

### 8. Logging and Monitoring

Add security event logging:
```java
@Component
public class SecurityEventLogger {
    private static final Logger log = LoggerFactory.getLogger(SecurityEventLogger.class);

    public void logLoginSuccess(String username) {
        log.info("Successful login for user: {}", username);
    }

    public void logLoginFailure(String username) {
        log.warn("Failed login attempt for user: {}", username);
    }

    public void logInvalidToken(String token) {
        log.warn("Invalid token detected: {}", token.substring(0, 10) + "...");
    }
}
```

---

## Troubleshooting

### Issue: 403 Forbidden on Protected Endpoints

**Cause:** Missing or invalid JWT token

**Solution:**
1. Ensure you're logged in and have a valid token
2. Check Authorization header format: `Authorization: Bearer <token>`
3. Verify token hasn't expired (24 hours)
4. Check for typos in token

### Issue: "User not found" on Login

**Cause:** User doesn't exist or database is empty (H2 in-memory)

**Solution:**
1. Register a new user first
2. If using H2, remember data is lost on restart
3. Check username spelling (case-sensitive)

### Issue: Swagger UI not showing Authorize button

**Cause:** OpenAPI security configuration not loaded

**Solution:**
1. Restart application
2. Clear browser cache
3. Verify OpenApiConfig.java is in config package

### Issue: Password doesn't work after registration

**Cause:** Password length/complexity issue or BCrypt misconfiguration

**Solution:**
1. Ensure password is at least 8 characters
2. Verify BCryptPasswordEncoder bean is configured
3. Check if password is being encoded before save

---

## Summary

This JWT authentication implementation provides:

✅ **Secure user registration and login**
- BCrypt password hashing
- Duplicate username/email validation

✅ **Stateless JWT-based authentication**
- HMAC256 signature
- 24-hour token expiration
- Issuer validation

✅ **Protected API endpoints**
- Automatic token validation
- SecurityContext integration
- 403 Forbidden on invalid/missing tokens

✅ **Swagger UI integration**
- Authorization button
- Token storage
- Automatic inclusion in requests

✅ **Production-ready architecture**
- Separation of concerns
- Configurable via properties
- Extensible design

---

## Next Steps

**Enhancements:**
1. Implement refresh tokens
2. Add role-based access control (RBAC)
3. Email verification for registration
4. Password reset functionality
5. Two-factor authentication (2FA)
6. Account lockout after failed login attempts
7. Password strength validation
8. Audit logging for security events

**Testing:**
1. Unit tests for JwtUtil
2. Integration tests for AuthController
3. Security tests for protected endpoints
4. Load testing for authentication endpoints

---

**Created by:** Claude Code
**Date:** 2025-11-04
**Spring Boot Version:** 3.5.7
**JWT Library:** Auth0 java-jwt 4.4.0
