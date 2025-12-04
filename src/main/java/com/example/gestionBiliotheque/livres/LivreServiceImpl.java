package com.example.gestionBiliotheque.livres;

import com.example.gestionBiliotheque.exception.ResourceAlreadyExistsException;
import com.example.gestionBiliotheque.exception.ResourceNotFoundException;
import com.example.gestionBiliotheque.livres.dto.CreateLivreDTO;
import com.example.gestionBiliotheque.livres.dto.LivreDTO;
import com.example.gestionBiliotheque.livres.dto.UpdateLivreDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation du service de gestion des livres
 */
@Service
@Transactional
public class LivreServiceImpl implements LivreService {

    @Autowired
    private LivreRepository livreRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<LivreDTO> getAllLivres(Pageable pageable) {
        return livreRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public LivreDTO getLivreById(Long id) {
        LivreModel livre = livreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livre", "id", id));
        return convertToDTO(livre);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LivreDTO> searchLivres(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return getAllLivres(pageable);
        }
        return livreRepository.searchLivres(query.trim(), pageable)
                .map(this::convertToDTO);
    }

    @Override
    public LivreDTO createLivre(CreateLivreDTO createLivreDTO) {
        // Vérifier si un livre avec cet ISBN existe déjà
        if (livreRepository.existsByIsbn(createLivreDTO.getIsbn())) {
            throw new ResourceAlreadyExistsException("Livre", "ISBN", createLivreDTO.getIsbn());
        }

        // Créer le nouveau livre
        LivreModel livre = new LivreModel();
        livre.setIsbn(createLivreDTO.getIsbn());
        livre.setTitre(createLivreDTO.getTitre());
        livre.setAuteur(createLivreDTO.getAuteur());
        livre.setCategorie(createLivreDTO.getCategorie());
        livre.setDatePublication(createLivreDTO.getDatePublication());
        livre.setNombreExemplaires(
                createLivreDTO.getNombreExemplaires() != null ? createLivreDTO.getNombreExemplaires() : 1);
        livre.setDisponibles(createLivreDTO.getDisponibles() != null ? createLivreDTO.getDisponibles()
                : livre.getNombreExemplaires());

        // Sauvegarder
        livre = livreRepository.save(livre);
        return convertToDTO(livre);
    }

    @Override
    public LivreDTO updateLivre(Long id, UpdateLivreDTO updateLivreDTO) {
        // Récupérer le livre existant
        LivreModel livre = livreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livre", "id", id));

        // Vérifier si l'ISBN est modifié et s'il existe déjà
        if (updateLivreDTO.getIsbn() != null &&
                !updateLivreDTO.getIsbn().equals(livre.getIsbn()) &&
                livreRepository.existsByIsbn(updateLivreDTO.getIsbn())) {
            throw new ResourceAlreadyExistsException("Livre", "ISBN", updateLivreDTO.getIsbn());
        }

        // Mettre à jour les champs non nuls
        if (updateLivreDTO.getIsbn() != null) {
            livre.setIsbn(updateLivreDTO.getIsbn());
        }
        if (updateLivreDTO.getTitre() != null) {
            livre.setTitre(updateLivreDTO.getTitre());
        }
        if (updateLivreDTO.getAuteur() != null) {
            livre.setAuteur(updateLivreDTO.getAuteur());
        }
        if (updateLivreDTO.getCategorie() != null) {
            livre.setCategorie(updateLivreDTO.getCategorie());
        }
        if (updateLivreDTO.getDatePublication() != null) {
            livre.setDatePublication(updateLivreDTO.getDatePublication());
        }
        if (updateLivreDTO.getNombreExemplaires() != null) {
            livre.setNombreExemplaires(updateLivreDTO.getNombreExemplaires());
        }
        if (updateLivreDTO.getDisponibles() != null) {
            livre.setDisponibles(updateLivreDTO.getDisponibles());
        }

        // Sauvegarder
        livre = livreRepository.save(livre);
        return convertToDTO(livre);
    }

    @Override
    public void deleteLivre(Long id) {
        // Vérifier que le livre existe
        if (!livreRepository.existsById(id)) {
            throw new ResourceNotFoundException("Livre", "id", id);
        }
        livreRepository.deleteById(id);
    }

    /**
     * Convertit un LivreModel en LivreDTO
     */
    private LivreDTO convertToDTO(LivreModel livre) {
        return new LivreDTO(
                livre.getId(),
                livre.getIsbn(),
                livre.getTitre(),
                livre.getAuteur(),
                livre.getCategorie(),
                livre.getDatePublication(),
                livre.getNombreExemplaires(),
                livre.getDisponibles());
    }
}
