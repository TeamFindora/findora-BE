package com.findora.findora.postsimage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "게시글 이미지 업로드 요청 DTO")
public class PostImageRequestDto {
    @Schema(
        description = "이미지 파일들 (최대 10개)", 
        type = "array", 
        format = "binary",
        example = "이미지 파일들",
        required = false
    )
    private List<MultipartFile> images;
}
