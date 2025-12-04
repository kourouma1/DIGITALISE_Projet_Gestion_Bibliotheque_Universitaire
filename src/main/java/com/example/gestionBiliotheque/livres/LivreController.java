package com.example.gestionBiliotheque.livres;

import com.example.gestionBiliotheque.livres.dto.CreateLivreDTO;
import com.example.gestionBiliotheque.livres.dto.LivreDTO;
import com.example.gestionBiliotheque.livres.dto.UpdateLivreDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion des livres
 */
@RestController
@RequestMapping("/livres")
public class LivreController {

    @Autowired
    private LivreService livreService;

    /**
     * GET /api/livres
     * Récupère la liste paginée de tous les livres
     * Accessible à tous les utilisateurs authentifiés
     */
    @GetMapping
    public ResponseEntity<Page<LivreDTO>> getAllLivres(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "titre") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<LivreDTO> livres = livreService.getAllLivres(pageable);
        return ResponseEntity.ok(livres);
    }

    /**
     * GET /api/livres/{id}
     * Récupère un livre par son ID
     * Accessible à tous les utilisateurs authentifiés
     */
    @GetMapping("/{id}")
    public ResponseEntity<LivreDTO> getLivreById(@PathVariable Long id) {
        LivreDTO livre = livreService.getLivreById(id);
        return ResponseEntity.ok(livre);
    }

    /**
     * GET /api/livres/search?q={query}
     * Recherche des livres par mot-clé (titre, auteur, ISBN, catégorie)
     * Accessible à tous les utilisateurs authentifiés
     */
    @GetMapping("/search")
    public ResponseEntity<Page<LivreDTO>> searchLivres(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "titre") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<LivreDTO> livres = livreService.searchLivres(q, pageable);
        return ResponseEntity.ok(livres);
    }

    /**
     * POST /api/livres
     * Crée un nouveau livre
     * Accessible uniquement aux ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LivreDTO> createLivre(@Valid @RequestBody CreateLivreDTO createLivreDTO) {
        LivreDTO livre = livreService.createLivre(createLivreDTO);
        return new ResponseEntity<>(livre, HttpStatus.CREATED);
    }

    /**
     * PUT /api/livres/{id}
     * Met à jour un livre existant
     * Accessible uniquement aux ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LivreDTO> updateLivre(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLivreDTO updateLivreDTO) {
        LivreDTO livre = livreService.updateLivre(id, updateLivreDTO);
        return ResponseEntity.ok(livre);
    }

    /**
     * DELETE /api/livres/{id}
     * Supprime un livre
     * Accessible uniquement aux ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLivre(@PathVariable Long id) {
        livreService.deleteLivre(id);
        return ResponseEntity.noContent().build();
    }
}
