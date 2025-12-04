package com.example.gestionBiliotheque.emprunt;

import com.example.gestionBiliotheque.emprunt.dto.CreateEmpruntDTO;
import com.example.gestionBiliotheque.emprunt.dto.EmpruntDTO;
import com.example.gestionBiliotheque.utilisateurs.UtilisateurModel;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.gestionBiliotheque.utilisateurs.UtilisateurRepository;

/**
 * Contrôleur REST pour la gestion des emprunts
 */
@RestController
@RequestMapping("/emprunts")
public class EmpruntController {

    @Autowired
    private EmpruntService empruntService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    /**
     * POST /api/emprunts
     * Crée un nouvel emprunt
     */
    @PostMapping
    public ResponseEntity<EmpruntDTO> createEmprunt(@Valid @RequestBody CreateEmpruntDTO createEmpruntDTO) {
        EmpruntDTO emprunt = empruntService.createEmprunt(createEmpruntDTO);
        return new ResponseEntity<>(emprunt, HttpStatus.CREATED);
    }

    /**
     * PUT /api/emprunts/{id}/retour
     * Retourne un livre
     */
    @PutMapping("/{id}/retour")
    public ResponseEntity<EmpruntDTO> returnLivre(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        UtilisateurModel currentUser = utilisateurRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        EmpruntDTO emprunt = empruntService.returnLivre(id, currentUser);
        return ResponseEntity.ok(emprunt);
    }

    /**
     * GET /api/emprunts/mes-emprunts
     * Récupère les emprunts de l'utilisateur connecté
     */
    @GetMapping("/mes-emprunts")
    public ResponseEntity<Page<EmpruntDTO>> getMesEmprunts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateEmprunt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        UtilisateurModel currentUser = utilisateurRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<EmpruntDTO> emprunts = empruntService.getMesEmprunts(currentUser, pageable);
        return ResponseEntity.ok(emprunts);
    }

    /**
     * GET /api/emprunts/en-retard
     * Récupère tous les emprunts en retard (ADMIN/MANAGER only)
     */
    @GetMapping("/en-retard")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<EmpruntDTO>> getEmpruntsEnRetard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateRetourPrevue") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<EmpruntDTO> emprunts = empruntService.getEmpruntsEnRetard(pageable);
        return ResponseEntity.ok(emprunts);
    }
}
