package com.findora.findora.categories.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    public enum Name {
        FREE, PI, BEST, ABROAD, GSPASS, INTERN, PROMOTE, WORK
    }

    public enum Visibility {
        PUBLIC, PI, STUDENT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private Name name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility;

    @Builder
    public Category(Name name, Visibility visibility) {
        this.name = name;
        this.visibility = visibility;
        this.createdAt = LocalDateTime.now();
    }

}
