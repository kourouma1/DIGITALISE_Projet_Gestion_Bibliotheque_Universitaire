package com.example.gestionBiliotheque.livres;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour la gestion des livres
 */
@Repository
public interface LivreRepository extends JpaRepository<LivreModel, Long> {

    /**
     * Trouve un livre par son ISBN
     */
    Optional<LivreModel> findByIsbn(String isbn);

    /**
     * Vérifie si un livre existe avec cet ISBN
     */
    boolean existsByIsbn(String isbn);

    /**
     * Recherche des livres par titre, auteur, ISBN ou catégorie
     * Recherche insensible à la casse
     */
    @Query("SELECT l FROM LivreModel l WHERE " +
            "LOWER(l.titre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(l.auteur) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(l.isbn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(l.categorie) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<LivreModel> searchLivres(@Param("query") String query, Pageable pageable);

    /**
     * Trouve les livres par catégorie avec pagination
     */
    Page<LivreModel> findByCategorie(String categorie, Pageable pageable);

    /**
     * Trouve les livres disponibles (disponibles > 0)
     */
    @Query("SELECT l FROM LivreModel l WHERE l.disponibles > 0")
    Page<LivreModel> findAvailableLivres(Pageable pageable);
}
