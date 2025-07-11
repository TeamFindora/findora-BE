package com.findora.findora.posts.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.findora.findora.categories.model.Category;
import com.findora.findora.categories.repository.CategoryRepository;
import com.findora.findora.posts.dto.PostRequestDto;
import com.findora.findora.posts.dto.PostResponseDto;
import com.findora.findora.posts.model.Post;
import com.findora.findora.posts.repository.PostRepository;
import com.findora.findora.users.model.User;
import com.findora.findora.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createPost(PostRequestDto requestDto, Long userId) {
        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리가 없습니다."));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다."));
        
        Post post = requestDto.toEntity(category, user);
        return postRepository.save(post).getId();
    }

    public List<PostResponseDto> getAllPosts() {
        return postRepository.findAll().stream()
                .map(PostResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public PostResponseDto getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다: " + id));
        return PostResponseDto.fromEntity(post);
    }

    public List<PostResponseDto> getPostsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리에 게시글이 존재하지 않습니다."));

        return postRepository.findByCategory(category).stream()
                .map(PostResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updatePost(Long id, PostRequestDto requestDto, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다: " + id));
        if (!post.getUser().getId().equals(userId)) {
            throw new SecurityException("작성자만 수정할 수 있습니다.");
        }
        post.update(requestDto.getTitle(), requestDto.getContent(), post.getCategory());
    }

    @Transactional
    public void deletePost(Long id, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다: " + id));
        if (!post.getUser().getId().equals(userId)) {
            throw new SecurityException("작성자만 삭제할 수 있습니다.");
        }
        postRepository.deleteById(id);
    }
}
