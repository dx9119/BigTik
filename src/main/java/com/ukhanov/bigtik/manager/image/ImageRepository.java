package com.ukhanov.bigtik.manager.image;

import com.ukhanov.bigtik.core.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    // Проверка существования изображения по хешу
    boolean existsByHash(String hash);

    // Получить изображение по хешу
    Optional<Image> findByHash(String hash);

    // Получить все изображения (пагинация)
    Page<Image> findAll(Pageable pageable);

    // Получить изображения конкретного пользователя (пагинация)
    Page<Image> findByUploader(User uploader, Pageable pageable);

    // Получить изображения, содержащие хотя бы один из указанных тегов (пагинация)
    Page<Image> findByTagsIn(Set<String> tags, Pageable pageable);
}
