package com.findora.findora.posts.repository;

import com.findora.findora.categories.model.Category;
import com.findora.findora.posts.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>{
    List<Post> findByCategory(Category category);
}
