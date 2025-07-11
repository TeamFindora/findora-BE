package com.findora.findora.bookmark.service;

import com.findora.findora.bookmark.dto.BookmarkRequestDto;
import com.findora.findora.bookmark.dto.BookmarkResponseDto;
import com.findora.findora.bookmark.model.Bookmark;
import com.findora.findora.bookmark.repository.BookmarkRepository;
import com.findora.findora.posts.model.Post;
import com.findora.findora.posts.repository.PostRepository;
import com.findora.findora.users.model.User;
import com.findora.findora.users.repository.UserRepository;
import com.findora.findora.common.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    public SuccessResponse addBookmark(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        // 중복 방지
        bookmarkRepository.findByUserAndPost(user, post)
                .ifPresent(b -> { throw new IllegalArgumentException("이미 즐겨찾기되어있는 게시글입니다."); });
        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .post(post)
                .build();
        bookmarkRepository.save(bookmark);
        return SuccessResponse.of("즐겨찾기 추가 성공하였습니다.");
    }

    @Transactional
    public SuccessResponse removeBookmark(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        // 즐겨찾기가 존재하는지 먼저 확인
        Bookmark bookmark = bookmarkRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new IllegalArgumentException("즐겨찾기되어있지 않은 게시글입니다."));
        bookmarkRepository.delete(bookmark);
        return SuccessResponse.of("즐겨찾기 삭제 성공하였습니다.");
    }

    @Transactional(readOnly = true)
    public List<BookmarkResponseDto> getBookmarksByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return bookmarkRepository.findByUser(user).stream()
                .map(BookmarkService::toResponseDto)
                .collect(Collectors.toList());
    }

    public static BookmarkResponseDto toResponseDto(Bookmark bookmark) {
        return BookmarkResponseDto.builder()
                .id(bookmark.getId())
                .postId(bookmark.getPost().getId())
                .postTitle(bookmark.getPost().getTitle())
                .userId(bookmark.getUser().getId())
                .createdAt(bookmark.getCreatedAt())
                .build();
    }
} 