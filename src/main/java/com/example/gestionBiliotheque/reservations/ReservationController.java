package com.example.gestionBiliotheque.reservations;

import com.example.gestionBiliotheque.reservations.dto.CreateReservationDTO;
import com.example.gestionBiliotheque.reservations.dto.ReservationDTO;
import com.example.gestionBiliotheque.utilisateurs.UtilisateurModel;
import com.example.gestionBiliotheque.utilisateurs.UtilisateurRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion des réservations
 */
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    /**
     * POST /api/reservations
     * Crée une nouvelle réservation
     */
    @PostMapping
    public ResponseEntity<ReservationDTO> createReservation(
            @Valid @RequestBody CreateReservationDTO createReservationDTO) {
        ReservationDTO reservation = reservationService.createReservation(createReservationDTO);
        return new ResponseEntity<>(reservation, HttpStatus.CREATED);
    }

    /**
     * DELETE /api/reservations/{id}
     * Annule une réservation
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        UtilisateurModel currentUser = utilisateurRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        reservationService.cancelReservation(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/reservations/mes-reservations
     * Récupère les réservations de l'utilisateur connecté
     */
    @GetMapping("/mes-reservations")
    public ResponseEntity<Page<ReservationDTO>> getMesReservations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UtilisateurModel currentUser = utilisateurRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateReservation"));
        Page<ReservationDTO> reservations = reservationService.getMesReservations(currentUser, pageable);
        return ResponseEntity.ok(reservations);
    }
}
