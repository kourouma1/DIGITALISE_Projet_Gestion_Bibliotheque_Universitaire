package com.example.gestionBiliotheque.emprunt;

import com.example.gestionBiliotheque.emprunt.dto.CreateEmpruntDTO;
import com.example.gestionBiliotheque.emprunt.dto.EmpruntDTO;
import com.example.gestionBiliotheque.utilisateurs.UtilisateurModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface du service de gestion des emprunts
 */
public interface EmpruntService {

    /**
     * Crée un nouvel emprunt avec validation des règles métier
     */
    EmpruntDTO createEmprunt(CreateEmpruntDTO createEmpruntDTO);

    /**
     * Retourne un livre et calcule les pénalités
     */
    EmpruntDTO returnLivre(Long empruntId, UtilisateurModel currentUser);

    /**
     * Récupère les emprunts de l'utilisateur connecté
     */
    Page<EmpruntDTO> getMesEmprunts(UtilisateurModel utilisateur, Pageable pageable);

    /**
     * Récupère tous les emprunts en retard (ADMIN/MANAGER only)
     */
    Page<EmpruntDTO> getEmpruntsEnRetard(Pageable pageable);
}
