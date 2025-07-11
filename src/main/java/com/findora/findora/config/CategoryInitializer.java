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
    public void insertInitialCategories() {
        insertIfNotExists(Category.Name.FREE, Category.Visibility.PUBLIC);
        insertIfNotExists(Category.Name.PI, Category.Visibility.PI);
        insertIfNotExists(Category.Name.BEST,Category.Visibility.PUBLIC);
        insertIfNotExists(Category.Name.ABROAD, Category.Visibility.PUBLIC);
        insertIfNotExists(Category.Name.GSPASS, Category.Visibility.PUBLIC);
        insertIfNotExists(Category.Name.INTERN, Category.Visibility.PUBLIC);
        insertIfNotExists(Category.Name.PROMOTE, Category.Visibility.PUBLIC);
        insertIfNotExists(Category.Name.WORK, Category.Visibility.PUBLIC);
    }

    private void insertIfNotExists(Category.Name name, Category.Visibility visibility) {
        categoryRepository.findByName(name)
                .orElseGet(() -> categoryRepository.save(
                        Category.builder()
                                .name(name)
                                .visibility(visibility)
                                .build()
                ));
    }
}
