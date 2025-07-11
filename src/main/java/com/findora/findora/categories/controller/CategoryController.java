package com.findora.findora.categories.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.findora.findora.categories.dto.CategoryResponseDto;
import com.findora.findora.categories.model.Category;
import com.findora.findora.categories.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 카테고리 API 컨트롤러
 * 
 * 게시판 카테고리 관리를 위한 API를 제공합니다.
 * 카테고리별로 다른 공개 범위(PUBLIC, STUDENT_ONLY, PROFESSOR_ONLY)를 가집니다.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "📁 카테고리 API", description = "게시판 카테고리 조회 및 관리 API")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(
        summary = "전체 카테고리 조회", 
        description = "시스템에서 사용 가능한 모든 카테고리 목록을 조회합니다. 사용자 권한에 따라 접근 가능한 카테고리만 표시됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "카테고리 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoryResponseDto.class),
                examples = @ExampleObject(
                    name = "카테고리 목록 응답",
                    value = """
                    [
                      {
                        "id": 1,
                        "name": "GENERAL",
                        "visibility": "PUBLIC",
                        "createdAt": "2025-07-07T10:00:00"
                      },
                      {
                        "id": 2,
                        "name": "NOTICE",
                        "visibility": "PUBLIC",
                        "createdAt": "2025-07-07T10:00:00"
                      },
                      {
                        "id": 3,
                        "name": "QNA",
                        "visibility": "STUDENT_ONLY",
                        "createdAt": "2025-07-07T10:00:00"
                      },
                      {
                        "id": 4,
                        "name": "RESEARCH",
                        "visibility": "PROFESSOR_ONLY",
                        "createdAt": "2025-07-07T10:00:00"
                      }
                    ]
                    """
                )
            )
        )
    })
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{name}")
    @Operation(
        summary = "이름으로 카테고리 조회", 
        description = "카테고리 이름으로 특정 카테고리의 상세 정보를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "카테고리 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoryResponseDto.class),
                examples = @ExampleObject(
                    name = "카테고리 상세 응답",
                    value = """
                    {
                      "id": 1,
                      "name": "GENERAL",
                      "visibility": "PUBLIC",
                      "createdAt": "2025-07-07T10:00:00"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "카테고리를 찾을 수 없음",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                    {
                      "error": "카테고리를 찾을 수 없습니다.",
                      "timestamp": "2025-07-07T14:30:00"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "해당 카테고리에 접근 권한 없음"
        )
    })
    public ResponseEntity<CategoryResponseDto> getCategoryByName(
        @Parameter(
            description = "조회할 카테고리 이름", 
            example = "GENERAL",
            required = true,
            schema = @Schema(
                type = "string",
                allowableValues = {"GENERAL", "NOTICE", "QNA", "RESEARCH", "FREE_BOARD"}
            )
        )
        @PathVariable Category.Name name
    ) {
        return ResponseEntity.ok(categoryService.getCategoryByName(name));
    }
}
