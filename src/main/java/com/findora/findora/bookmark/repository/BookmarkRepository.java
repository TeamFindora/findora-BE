package com.findora.findora.bookmark.repository;

import com.findora.findora.bookmark.model.Bookmark;
import com.findora.findora.users.model.User;
import com.findora.findora.posts.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByUser(User user);
    Optional<Bookmark> findByUserAndPost(User user, Post post);
    void deleteByUserAndPost(User user, Post post);
} 