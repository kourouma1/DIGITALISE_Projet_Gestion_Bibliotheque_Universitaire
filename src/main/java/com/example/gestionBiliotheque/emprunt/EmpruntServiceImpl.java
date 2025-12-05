package com.example.gestionBiliotheque.emprunt;

import com.example.gestionBiliotheque.emprunt.dto.CreateEmpruntDTO;
import com.example.gestionBiliotheque.emprunt.dto.EmpruntDTO;
import com.example.gestionBiliotheque.exception.*;
import com.example.gestionBiliotheque.livres.LivreModel;
import com.example.gestionBiliotheque.livres.LivreRepository;
import com.example.gestionBiliotheque.livres.dto.LivreSimpleDTO;
import com.example.gestionBiliotheque.notification.NotificationService;
import com.example.gestionBiliotheque.reservations.ReservationModel;
import com.example.gestionBiliotheque.reservations.ReservationRepository;
import com.example.gestionBiliotheque.reservations.StatutReservation;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Implémentation du service de gestion des emprunts
 */
@Service
@Transactional
public class EmpruntServiceImpl implements EmpruntService {

    private static final Logger logger = LoggerFactory.getLogger(EmpruntServiceImpl.class);

    @Autowired
    private EmpruntRepository empruntRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public EmpruntDTO createEmprunt(CreateEmpruntDTO createEmpruntDTO) {
        // 1. Récupérer l'utilisateur
        UtilisateurModel utilisateur = utilisateurRepository.findById(createEmpruntDTO.getUtilisateurId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Utilisateur", "id", createEmpruntDTO.getUtilisateurId()));

        // 2. Récupérer le livre
        LivreModel livre = livreRepository.findById(createEmpruntDTO.getLivreId())
                .orElseThrow(() -> new ResourceNotFoundException("Livre", "id", createEmpruntDTO.getLivreId()));

        // 3. Vérifier les pénalités de l'utilisateur
        BigDecimal totalPenalties = empruntRepository.sumPenalitesByUtilisateur(utilisateur);
        if (totalPenalties.compareTo(LoanBusinessRules.MAX_PENALTY_ALLOWED) > 0) {
            throw new PenaltyBlockException(totalPenalties, LoanBusinessRules.MAX_PENALTY_ALLOWED);
        }

        // 4. Vérifier la limite d'emprunts
        long activeLoans = empruntRepository.countByUtilisateurAndStatut(utilisateur, StatutEmprunt.EN_COURS);
        int maxLoans = LoanBusinessRules.getMaxLoansForRole(utilisateur.getRole());
        if (activeLoans >= maxLoans) {
            throw new LoanLimitExceededException((int) activeLoans, maxLoans);
        }

        // 5. Vérifier la disponibilité du livre
        if (livre.getDisponibles() <= 0) {
            // Créer une réservation automatiquement
            ReservationModel reservation = createAutoReservation(utilisateur, livre);
            throw new BookNotAvailableException(
                    "Livre non disponible. Une réservation a été créée pour vous (valable 48h).",
                    reservation.getId());
        }

        // 6. Créer l'emprunt
        EmpruntModel emprunt = new EmpruntModel();
        emprunt.setUtilisateur(utilisateur);
        emprunt.setLivre(livre);
        emprunt.setDateEmprunt(LocalDateTime.now());

        // Calculer la date de retour prévue selon le rôle
        int loanDuration = LoanBusinessRules.getLoanDurationForRole(utilisateur.getRole());
        emprunt.setDateRetourPrevue(LocalDateTime.now().plusDays(loanDuration));

        emprunt.setStatut(StatutEmprunt.EN_COURS);
        emprunt.setPenalite(BigDecimal.ZERO);

        // 7. Décrémenter le nombre de livres disponibles
        livre.setDisponibles(livre.getDisponibles() - 1);
        livreRepository.save(livre);

        // 8. Sauvegarder l'emprunt
        emprunt = empruntRepository.save(emprunt);

        return convertToDTO(emprunt);
    }

    @Override
    public EmpruntDTO returnLivre(Long empruntId, UtilisateurModel currentUser) {
        // 1. Récupérer l'emprunt
        EmpruntModel emprunt = empruntRepository.findById(empruntId)
                .orElseThrow(() -> new ResourceNotFoundException("Emprunt", "id", empruntId));

        // 2. Vérifier que l'emprunt est EN_COURS
        if (emprunt.getStatut() != StatutEmprunt.EN_COURS) {
            throw new IllegalStateException("Cet emprunt a déjà été retourné");
        }

        // 3. Vérifier les permissions (propriétaire ou ADMIN/MANAGER)
        if (!emprunt.getUtilisateur().getId().equals(currentUser.getId()) &&
                currentUser.getRole() != com.example.gestionBiliotheque.utilisateurs.Role.ADMIN &&
                currentUser.getRole() != Role.BIBLIOTHECAIRE) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Vous ne pouvez retourner que vos propres emprunts");
        }

        // 4. Définir la date de retour effective
        LocalDateTime now = LocalDateTime.now();
        emprunt.setDateRetourEffective(now);

        // 5. Calculer les pénalités si retard
        long daysLate = ChronoUnit.DAYS.between(emprunt.getDateRetourPrevue(), now);
        if (daysLate > 0) {
            BigDecimal penalty = LoanBusinessRules.calculatePenalty(daysLate);
            emprunt.setPenalite(penalty);
        }

        // 6. Mettre à jour le statut
        emprunt.setStatut(StatutEmprunt.TERMINE);

        // 7. Incrémenter le nombre de livres disponibles
        LivreModel livre = emprunt.getLivre();
        livre.setDisponibles(livre.getDisponibles() + 1);
        livreRepository.save(livre);

        // 8. Vérifier s'il y a des réservations en attente pour ce livre
        List<ReservationModel> pendingReservations = reservationRepository.findActiveReservationsForLivre(livre);
        if (!pendingReservations.isEmpty()) {
            // Valider la première réservation
            ReservationModel firstReservation = pendingReservations.get(0);
            firstReservation.setStatut(StatutReservation.VALIDEE);
            reservationRepository.save(firstReservation);
            
            // Envoyer une notification à l'utilisateur
            try {
                notificationService.sendReservationAvailableNotification(firstReservation);
                logger.info("Notification de disponibilité envoyée pour la réservation ID: {}", firstReservation.getId());
            } catch (Exception e) {
                logger.error("Erreur lors de l'envoi de la notification de disponibilité", e);
                // Ne pas bloquer le processus de retour si la notification échoue
            }
        }

        // 9. Sauvegarder l'emprunt
        emprunt = empruntRepository.save(emprunt);

        return convertToDTO(emprunt);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmpruntDTO> getMesEmprunts(UtilisateurModel utilisateur, Pageable pageable) {
        return empruntRepository.findByUtilisateur(utilisateur, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmpruntDTO> getEmpruntsEnRetard(Pageable pageable) {
        return empruntRepository.findOverdueEmprunts(LocalDateTime.now(), pageable)
                .map(this::convertToDTO);
    }

    /**
     * Crée une réservation automatique lorsqu'un livre n'est pas disponible
     */
    private ReservationModel createAutoReservation(UtilisateurModel utilisateur, LivreModel livre) {
        // Vérifier si l'utilisateur a déjà une réservation active pour ce livre
        var existingReservation = reservationRepository.findActiveReservationByUtilisateurAndLivre(utilisateur, livre);
        if (existingReservation.isPresent()) {
            return existingReservation.get();
        }

        ReservationModel reservation = new ReservationModel();
        reservation.setUtilisateur(utilisateur);
        reservation.setLivre(livre);
        reservation.setDateReservation(LocalDateTime.now());
        reservation.setDateExpiration(LocalDateTime.now().plusHours(LoanBusinessRules.RESERVATION_VALIDITY_HOURS));
        reservation.setStatut(StatutReservation.EN_ATTENTE);

        return reservationRepository.save(reservation);
    }

    /**
     * Convertit un EmpruntModel en EmpruntDTO
     */
    private EmpruntDTO convertToDTO(EmpruntModel emprunt) {
        UtilisateurSimpleDTO utilisateurDTO = new UtilisateurSimpleDTO(
                emprunt.getUtilisateur().getId(),
                emprunt.getUtilisateur().getMatricule(),
                emprunt.getUtilisateur().getNom(),
                emprunt.getUtilisateur().getPrenom(),
                emprunt.getUtilisateur().getEmail());

        LivreSimpleDTO livreDTO = new LivreSimpleDTO(
                emprunt.getLivre().getId(),
                emprunt.getLivre().getIsbn(),
                emprunt.getLivre().getTitre(),
                emprunt.getLivre().getAuteur());

        return new EmpruntDTO(
                emprunt.getId(),
                utilisateurDTO,
                livreDTO,
                emprunt.getDateEmprunt(),
                emprunt.getDateRetourPrevue(),
                emprunt.getDateRetourEffective(),
                emprunt.getStatut(),
                emprunt.getPenalite());
    }
}
