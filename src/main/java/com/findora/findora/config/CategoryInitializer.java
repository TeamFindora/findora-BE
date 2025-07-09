package com.findora.findora.config;

import com.findora.findora.categories.model.Category;
import com.findora.findora.categories.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryInitializer {

    private final CategoryRepository categoryRepository;

    @PostConstruct
    public void initCategories() {
        // 이미 등록된 카테고리가 있으면 초기화 안 함
        if (categoryRepository.count() > 0) return;

        //자유게시판
        categoryRepository.save(Category.builder()
                .name(Category.Name.FREE)
                .visibility(Category.Visibility.PUBLIC)
                .build());
        //교수전용게시판
        categoryRepository.save(Category.builder()
                .name(Category.Name.PI)
                .visibility(Category.Visibility.PI) // PI는 PI 권한만 접근 가능
                .build());
        //베스트게시판
        categoryRepository.save(Category.builder()
                .name(Category.Name.BEST)
                .visibility(Category.Visibility.PUBLIC)
                .build());
        //유학게시판
        categoryRepository.save(Category.builder()
                .name(Category.Name.ABROAD)
                .visibility(Category.Visibility.PUBLIC)
                .build());
        //대학원합격후기게시판
        categoryRepository.save(Category.builder()
                .name(Category.Name.GSPASS)
                .visibility(Category.Visibility.PUBLIC)
                .build());
        //인턴게시판
        categoryRepository.save(Category.builder()
                .name(Category.Name.INTERN)
                .visibility(Category.Visibility.PUBLIC)
                .build());
        //홍보게시판
        categoryRepository.save(Category.builder()
                .name(Category.Name.PROMOTE)
                .visibility(Category.Visibility.PUBLIC)
                .build());
        //취업게시판
        categoryRepository.save(Category.builder()
                .name(Category.Name.WORK)
                .visibility(Category.Visibility.PUBLIC)
                .build());
    }
}
