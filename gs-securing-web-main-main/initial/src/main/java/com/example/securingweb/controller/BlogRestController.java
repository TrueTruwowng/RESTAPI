package com.example.securingweb.controller;

import com.example.securingweb.dto.BlogRequest;
import com.example.securingweb.dto.BlogResponse;
import com.example.securingweb.model.Blog;
import com.example.securingweb.model.User;
import com.example.securingweb.repository.BlogRepository;
import com.example.securingweb.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/blogs")
public class BlogRestController {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;

    public BlogRestController(BlogRepository blogRepository, UserRepository userRepository) {
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private BlogResponse toResponse(Blog b) {
        return new BlogResponse(b.getId(), b.getTitle(), b.getContent(), b.getOwner().getUsername(), b.getCreatedAt(), b.getUpdatedAt());
    }

    @GetMapping
    public List<BlogResponse> list(Authentication auth) {
        User current = userRepository.findByUsername(auth.getName());
        if (isAdmin(auth)) {
            return blogRepository.findAll().stream().map(this::toResponse).toList();
        }
        return blogRepository.findByOwner(current).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogResponse> get(@PathVariable Long id, Authentication auth) {
        Optional<Blog> opt = blogRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Blog b = opt.get();
        if (!isAdmin(auth) && !b.getOwner().getUsername().equals(auth.getName())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(toResponse(b));
    }

    @PostMapping
    public ResponseEntity<BlogResponse> create(@RequestBody BlogRequest req, Authentication auth) {
        if (req.getTitle() == null || req.getTitle().isBlank() || req.getContent() == null || req.getContent().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        User current = userRepository.findByUsername(auth.getName());
        Blog blog = new Blog();
        blog.setTitle(req.getTitle().trim());
        blog.setContent(req.getContent());
        blog.setOwner(current);
        blog = blogRepository.save(blog);
        return ResponseEntity.ok(toResponse(blog));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BlogResponse> update(@PathVariable Long id, @RequestBody BlogRequest req, Authentication auth) {
        Optional<Blog> opt = blogRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Blog b = opt.get();
        if (!isAdmin(auth) && !b.getOwner().getUsername().equals(auth.getName())) {
            return ResponseEntity.status(403).build();
        }
        if (req.getTitle() != null && !req.getTitle().isBlank()) b.setTitle(req.getTitle().trim());
        if (req.getContent() != null && !req.getContent().isBlank()) b.setContent(req.getContent());
        Blog saved = blogRepository.save(b);
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        Optional<Blog> opt = blogRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Blog b = opt.get();
        if (!isAdmin(auth) && !b.getOwner().getUsername().equals(auth.getName())) {
            return ResponseEntity.status(403).build();
        }
        blogRepository.delete(b);
        return ResponseEntity.noContent().build();
    }
}
