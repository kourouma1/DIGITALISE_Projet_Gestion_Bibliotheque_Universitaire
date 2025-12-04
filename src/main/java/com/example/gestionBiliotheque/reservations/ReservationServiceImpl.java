package com.example.gestionBiliotheque.reservations;

import com.example.gestionBiliotheque.emprunt.LoanBusinessRules;
import com.example.gestionBiliotheque.exception.ResourceAlreadyExistsException;
import com.example.gestionBiliotheque.exception.ResourceNotFoundException;
import com.example.gestionBiliotheque.livres.LivreModel;
import com.example.gestionBiliotheque.livres.LivreRepository;
import com.example.gestionBiliotheque.livres.dto.LivreSimpleDTO;
import com.example.gestionBiliotheque.reservations.dto.CreateReservationDTO;
import com.example.gestionBiliotheque.reservations.dto.ReservationDTO;
import com.example.gestionBiliotheque.utilisateurs.Role;
import com.example.gestionBiliotheque.utilisateurs.UtilisateurModel;
import com.example.gestionBiliotheque.utilisateurs.UtilisateurRepository;
import com.example.gestionBiliotheque.utilisateurs.dto.UtilisateurSimpleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implémentation du service de gestion des réservations
 */
@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationServiceImpl.class);

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private LivreRepository livreRepository;

    @Override
    public ReservationDTO createReservation(CreateReservationDTO createReservationDTO) {
        // 1. Récupérer l'utilisateur
        UtilisateurModel utilisateur = utilisateurRepository.findById(createReservationDTO.getUtilisateurId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id",
                        createReservationDTO.getUtilisateurId()));

        // 2. Récupérer le livre
        LivreModel livre = livreRepository.findById(createReservationDTO.getLivreId())
                .orElseThrow(() -> new ResourceNotFoundException("Livre", "id", createReservationDTO.getLivreId()));

        // 3. Vérifier si le livre est disponible (suggérer un emprunt direct)
        if (livre.getDisponibles() > 0) {
            throw new IllegalStateException(
                    "Le livre est actuellement disponible. Vous pouvez l'emprunter directement sans réservation.");
        }

        // 4. Vérifier si l'utilisateur a déjà une réservation active pour ce livre
        var existingReservation = reservationRepository.findActiveReservationByUtilisateurAndLivre(utilisateur, livre);
        if (existingReservation.isPresent()) {
            throw new ResourceAlreadyExistsException(
                    "Vous avez déjà une réservation active pour ce livre");
        }

        // 5. Créer la réservation
        ReservationModel reservation = new ReservationModel();
        reservation.setUtilisateur(utilisateur);
        reservation.setLivre(livre);
        reservation.setDateReservation(LocalDateTime.now());
        reservation.setDateExpiration(LocalDateTime.now().plusHours(LoanBusinessRules.RESERVATION_VALIDITY_HOURS));
        reservation.setStatut(StatutReservation.EN_ATTENTE);

        // 6. Sauvegarder
        reservation = reservationRepository.save(reservation);

        logger.info("Réservation créée: ID={}, Utilisateur={}, Livre={}",
                reservation.getId(), utilisateur.getEmail(), livre.getTitre());

        return convertToDTO(reservation);
    }

    @Override
    public void cancelReservation(Long reservationId, UtilisateurModel currentUser) {
        // 1. Récupérer la réservation
        ReservationModel reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation", "id", reservationId));

        // 2. Vérifier les permissions (propriétaire ou ADMIN)
        if (!reservation.getUtilisateur().getId().equals(currentUser.getId()) &&
                currentUser.getRole() != Role.ADMIN) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Vous ne pouvez annuler que vos propres réservations");
        }

        // 3. Annuler la réservation
        reservation.setStatut(StatutReservation.ANNULEE);
        reservationRepository.save(reservation);

        logger.info("Réservation annulée: ID={}, Utilisateur={}",
                reservationId, currentUser.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationDTO> getMesReservations(UtilisateurModel utilisateur, Pageable pageable) {
        return reservationRepository.findByUtilisateur(utilisateur, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public int cleanupExpiredReservations() {
        List<ReservationModel> expiredReservations = reservationRepository.findExpiredReservations(LocalDateTime.now());

        int count = 0;
        for (ReservationModel reservation : expiredReservations) {
            reservation.setStatut(StatutReservation.ANNULEE);
            reservationRepository.save(reservation);
            count++;
        }

        if (count > 0) {
            logger.info("Nettoyage des réservations expirées: {} réservations annulées", count);
        }

        return count;
    }

    /**
     * Convertit un ReservationModel en ReservationDTO
     */
    private ReservationDTO convertToDTO(ReservationModel reservation) {
        UtilisateurSimpleDTO utilisateurDTO = new UtilisateurSimpleDTO(
                reservation.getUtilisateur().getId(),
                reservation.getUtilisateur().getMatricule(),
                reservation.getUtilisateur().getNom(),
                reservation.getUtilisateur().getPrenom(),
                reservation.getUtilisateur().getEmail());

        LivreSimpleDTO livreDTO = new LivreSimpleDTO(
                reservation.getLivre().getId(),
                reservation.getLivre().getIsbn(),
                reservation.getLivre().getTitre(),
                reservation.getLivre().getAuteur());

        return new ReservationDTO(
                reservation.getId(),
                utilisateurDTO,
                livreDTO,
                reservation.getDateReservation(),
                reservation.getDateExpiration(),
                reservation.getStatut());
    }
}
