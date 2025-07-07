package com.findora.findora.posts.repository;

import com.findora.findora.posts.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PostRepository extends JpaRepository<Post, Long>{
}
