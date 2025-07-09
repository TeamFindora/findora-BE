package com.findora.findora.categories.service;

import com.findora.findora.categories.dto.CategoryResponseDto;
import com.findora.findora.categories.model.Category;
import com.findora.findora.categories.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public CategoryResponseDto getCategoryByName(Category.Name name) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다: " + name));
        return CategoryResponseDto.fromEntity(category);
    }
}
