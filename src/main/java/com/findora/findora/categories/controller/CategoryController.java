package com.findora.findora.categories.controller;

import com.findora.findora.categories.dto.CategoryResponseDto;
import com.findora.findora.categories.model.Category;
import com.findora.findora.categories.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "카테고리 API")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "전체 카테고리 조회")
    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "이름으로 카테고리 조회")
    @GetMapping("/{name}")
    public ResponseEntity<CategoryResponseDto> getCategoryByName(@PathVariable Category.Name name) {
        return ResponseEntity.ok(categoryService.getCategoryByName(name));
    }
}
