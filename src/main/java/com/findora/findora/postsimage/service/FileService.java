package com.findora.findora.postsimage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    @Value("${file.upload-dir}")
    private String uploadDir;

    //uploads/posts/{postid}/uuid_filename.jpg 형식으로 파일 저장
    public String saveFile(MultipartFile file, Long postId) {
        String originalFilename = file.getOriginalFilename();

        // 게시글 ID를 이용하여 하위 폴더 경로 생성 + uuid로 파일 이름 생성(중복방지)
        String postFolder = String.format("posts/%d", postId);
        String filename = UUID.randomUUID() + "_" + originalFilename;

        //파일 경로 생성
        Path uploadPath = Paths.get(uploadDir)
                .resolve(postFolder)
                .toAbsolutePath();
        Path filepath = uploadPath.resolve(filename);

        try {
            Files.createDirectories(filepath.getParent());
            file.transferTo(filepath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }

        // URL 경로 반환 (/uploads/posts/1/uuid_filename.jpg 형식)
        return String.format("/uploads/%s/%s", postFolder, filename);
    }

    // 파일 삭제 메서드
    public void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            Files.deleteIfExists(filePath);
            log.info("파일 삭제 완료: {}", fileName);
        } catch (IOException e) {
            log.error("파일 삭제 중 오류 발생: {}", fileName, e);
            throw new RuntimeException("파일을 삭제할 수 없습니다: " + fileName, e);
        }
    }

}
