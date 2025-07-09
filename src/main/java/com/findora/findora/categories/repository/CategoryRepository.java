package com.findora.findora.categories.repository;

import com.findora.findora.categories.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long>{
    Optional<Category> findByName(Category.Name name);
}
