package com.findora.findora.posts.service;

import com.findora.findora.posts.dto.PostRequestDto;
import com.findora.findora.posts.dto.PostResponseDto;
import com.findora.findora.posts.model.Post;
import com.findora.findora.posts.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    @Transactional
    public Long createPost(PostRequestDto requestDto) {
        Post post = requestDto.toEntity();
        return postRepository.save(post).getId();
    }

    @Transactional(readOnly = true)
    public List<PostResponseDto> getAllPosts() {
        return postRepository.findAll().stream()
                .map(PostResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PostResponseDto getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다: " + id));
        return PostResponseDto.fromEntity(post);
    }

    @Transactional
    public void updatePost(Long id, PostRequestDto requestDto) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다: " + id));
        post.update(requestDto.getTitle(), requestDto.getContent());
    }

    @Transactional
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }
}
