package com.example.securingweb.repository;

import com.example.securingweb.model.Blog;
import com.example.securingweb.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    List<Blog> findByOwner(User owner);
}

