package com.example.securingweb.controller;

import com.example.securingweb.dto.UserDto;
import com.example.securingweb.model.Role;
import com.example.securingweb.model.User;
import com.example.securingweb.repository.RoleRepository;
import com.example.securingweb.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserRestController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRestController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getUsername(), u.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> allUsers() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id, Authentication auth) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        User u = opt.get();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !u.getUsername().equals(auth.getName())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(toDto(u));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto dto) {
        if (dto.getUsername() == null || dto.getUsername().isBlank()) return ResponseEntity.badRequest().build();
        if (userRepository.findByUsername(dto.getUsername()) != null) return ResponseEntity.status(409).build();
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getUsername()));
        Set<Role> roles = (dto.getRoles() == null || dto.getRoles().isEmpty()) ? null : dto.getRoles().stream()
                .map(roleRepository::findByName)
                .filter(r -> r != null)
                .collect(Collectors.toSet());
        if (roles == null || roles.isEmpty()) {
            Role userRole = roleRepository.findByName("USER");
            if (userRole == null) { userRole = new Role(); userRole.setName("USER"); userRole = roleRepository.save(userRole); }
            roles = Set.of(userRole);
        }
        user.setRoles(roles);
        user = userRepository.save(user);
        return ResponseEntity.ok(toDto(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto dto, Authentication auth) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        User u = opt.get();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !u.getUsername().equals(auth.getName())) {
            return ResponseEntity.status(403).build();
        }
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            u.setUsername(dto.getUsername());
        }
        if (dto.getRoles() != null && isAdmin) {
            Set<Role> roles = dto.getRoles().stream().map(roleRepository::findByName).filter(r -> r != null).collect(Collectors.toSet());
            if (!roles.isEmpty()) u.setRoles(roles);
        }
        User saved = userRepository.save(u);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return ResponseEntity.ok(
            java.util.Map.of(
                "username", authentication.getName(),
                "roles", authentication.getAuthorities().stream().map(a -> a.getAuthority()).collect(java.util.stream.Collectors.toList())
            )
        );
    }
}
