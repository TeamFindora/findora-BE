package com.findora.findora.categories.dto;

import com.findora.findora.categories.model.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CategoryResponseDto {
    @Schema(description = "카테고리 ID", example = "1")
    private Long id;

    @Schema(description = "카테고리 이름")
    private Category.Name name;

    @Schema(description = "공개 범위")
    private Category.Visibility visibility;

    @Schema(description = "생성일자")
    private LocalDateTime createdAt;

    public static CategoryResponseDto fromEntity(Category category) {
        return CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .visibility(category.getVisibility())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
