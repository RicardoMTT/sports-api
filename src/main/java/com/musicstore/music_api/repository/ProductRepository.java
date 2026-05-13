package com.musicstore.music_api.repository;

import com.musicstore.music_api.domain.entities.Product;
import com.musicstore.music_api.domain.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Búsqueda por nombre usando JPQL y la cláusula LIKE explícita

    // Obtenemos el Page de Product , usamos like para buscar parciales , osea
    // que el nombre contenga el string que se le pase
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Product> searchProductsWithLike(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Spring boot leera el parametro pageable e inyectara el LIMIT Y OFFSET en el SQL
    Page<Product> findByCategory(Category category, Pageable pageable);
}

