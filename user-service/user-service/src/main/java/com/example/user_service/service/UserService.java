package com.example.user_service.service;

import com.example.user_service.dto.LoginRequest;
import com.example.user_service.dto.LoginResponse;
import com.example.user_service.dto.RegisterRequest;
import com.example.user_service.dto.UserResponse;
import com.example.user_service.entity.User;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;


    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;

        this.passwordEncoder = passwordEncoder;

        this.jwtService = jwtService;
    }


    // =====================================================
    // REGISTER
    // =====================================================

    public UserResponse register(
            RegisterRequest request) {


        // 1. Get role

        String role = request.getRole();


        // 2. If role is empty,
        //    default USER

        if (role == null ||
                role.isBlank()) {

            role = "USER";
        }


        // 3. Remove spaces and
        //    convert to uppercase

        role = role
                .trim()
                .toUpperCase();


        // 4. Allow only USER or RETAILER

        if (!role.equals("USER") &&
                !role.equals("RETAILER")) {

            throw new RuntimeException(
                    "Invalid role. Use USER or RETAILER"
            );
        }


        // 5. Check username + role

        boolean exists =
                userRepository
                        .existsByUsernameAndRole(
                                request.getUsername(),
                                role
                        );


        // 6. Duplicate check

        if (exists) {

            throw new RuntimeException(
                    "Username already exists for this role"
            );
        }


        // 7. Create User

        User user = new User();


        user.setUsername(
                request.getUsername()
        );


        // 8. Encrypt password

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );


        // 9. Set role

        user.setRole(role);


        // 10. Save

        User savedUser =
                userRepository.save(user);


        // 11. Return response

        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getRole()
        );
    }


    // =====================================================
    // LOGIN
    // =====================================================
public LoginResponse login(
        LoginRequest request) {

    // 1. Username ద్వారా users తీసుకోవడం

    List<User> users =
            userRepository.findByUsername(
                    request.getUsername()
            );

    // 2. Username లేకపోతే

    if (users.isEmpty()) {

        throw new RuntimeException(
                "Invalid username or password"
        );
    }

    // 3. Password match అయిన user వెతకడం

    User matchedUser = null;

    for (User user : users) {

        if (passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            if (matchedUser != null) {

                throw new RuntimeException(
                        "Multiple accounts found. " +
                        "Use different passwords for different roles."
                );
            }

            matchedUser = user;
        }
    }

    // 4. Password wrong

    if (matchedUser == null) {

        throw new RuntimeException(
                "Invalid username or password"
        );
    }

    // 5. Role database నుంచి automatically తీసుకుంటాం

    String role =
            matchedUser.getRole();

    // 6. JWT generate

    String token =
            jwtService.generateToken(
                    matchedUser.getUsername(),
                    role
            );

    // 7. Response

    return new LoginResponse(
            token,
            matchedUser.getUsername(),
            role
    );
}
   

    // =====================================================
    // GET CURRENT USER USING JWT
    // =====================================================

    public UserResponse getCurrentUser(
            String username,
            String role) {


        User user =
                userRepository
                        .findByUsernameAndRole(
                                username,
                                role
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found"
                                )
                        );


        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
    }
}