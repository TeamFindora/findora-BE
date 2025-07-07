package com.findora.findora.categories.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryName name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public enum CategoryName {
        HOT, BEST, PI
    }

    public enum Visibility {
        STUDENT, PI, ADMIN
    }

    public void update(CategoryName name, Visibility visibility) {
        this.name = name;
        this.visibility = visibility;
    }
}
