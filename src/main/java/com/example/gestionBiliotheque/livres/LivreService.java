package com.example.gestionBiliotheque.livres;

import com.example.gestionBiliotheque.livres.dto.CreateLivreDTO;
import com.example.gestionBiliotheque.livres.dto.LivreDTO;
import com.example.gestionBiliotheque.livres.dto.UpdateLivreDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface du service de gestion des livres
 */
public interface LivreService {

    /**
     * Récupère tous les livres avec pagination
     */
    Page<LivreDTO> getAllLivres(Pageable pageable);

    /**
     * Récupère un livre par son ID
     */
    LivreDTO getLivreById(Long id);

    /**
     * Recherche des livres par mot-clé avec pagination
     */
    Page<LivreDTO> searchLivres(String query, Pageable pageable);

    /**
     * Crée un nouveau livre
     */
    LivreDTO createLivre(CreateLivreDTO createLivreDTO);

    /**
     * Met à jour un livre existant
     */
    LivreDTO updateLivre(Long id, UpdateLivreDTO updateLivreDTO);

    /**
     * Supprime un livre
     */
    void deleteLivre(Long id);
}
