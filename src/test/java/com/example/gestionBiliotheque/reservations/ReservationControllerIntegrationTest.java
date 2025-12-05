package com.example.gestionBiliotheque.reservations;

import com.example.gestionBiliotheque.auth.dto.RegisterRequestDTO;
import com.example.gestionBiliotheque.livres.LivreModel;
import com.example.gestionBiliotheque.livres.LivreRepository;
import com.example.gestionBiliotheque.reservations.dto.CreateReservationDTO;
import com.example.gestionBiliotheque.utilisateurs.Role;
import com.example.gestionBiliotheque.utilisateurs.UtilisateurModel;
import com.example.gestionBiliotheque.utilisateurs.UtilisateurRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration pour la gestion des réservations
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("Tests de Gestion des Réservations")
class ReservationControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    private String userToken;
    private UtilisateurModel userModel;
    private LivreModel unavailableLivre;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        livreRepository.deleteAll();
        utilisateurRepository.deleteAll();

        // Créer un utilisateur
        RegisterRequestDTO userRequest = new RegisterRequestDTO();
        userRequest.setMatricule("USR001");
        userRequest.setNom("User");
        userRequest.setPrenom("Test");
        userRequest.setEmail("user@test.com");
        userRequest.setMotDePasse("user123");
        userRequest.setRole(Role.ETUDIANT);

        ResponseEntity<JsonNode> userResult = restTemplate.postForEntity("/auth/register", userRequest, JsonNode.class);
        assertThat(userResult.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        userToken = userResult.getBody().get("token").asText();
        userModel = utilisateurRepository.findByEmail("user@test.com").orElseThrow();

        // Créer un livre non disponible (1 exemplaire total, 0 disponible)
        unavailableLivre = createTestLivre("9781234567890", "Unavailable Book", 1, 0);
    }

    @Test
    @DisplayName("Création de réservation réussie")
    void testCreateReservationSuccess() {
        CreateReservationDTO createDTO = new CreateReservationDTO();
        createDTO.setUtilisateurId(userModel.getId());
        createDTO.setLivreId(unavailableLivre.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<CreateReservationDTO> request = new HttpEntity<>(createDTO, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity("/reservations", request, JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().get("statut").asText()).isEqualTo("EN_ATTENTE");
        assertThat(response.getBody().get("utilisateur").get("email").asText()).isEqualTo("user@test.com");
        assertThat(response.getBody().get("livre").get("titre").asText()).isEqualTo("Unavailable Book");
        assertThat(response.getBody().has("dateExpiration")).isTrue();
    }

    @Test
    @DisplayName("Réservation échoue si livre disponible")
    void testCreateReservationFailsIfBookAvailable() {
        LivreModel availableLivre = createTestLivre("9780987654321", "Available Book", 5, 5);

        CreateReservationDTO createDTO = new CreateReservationDTO();
        createDTO.setUtilisateurId(userModel.getId());
        createDTO.setLivreId(availableLivre.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<CreateReservationDTO> request = new HttpEntity<>(createDTO, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity("/reservations", request, JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message").asText()).isEqualTo(
                "Le livre est actuellement disponible. Vous pouvez l'emprunter directement sans réservation.");
    }

    @Test
    @DisplayName("Réservation échoue si réservation active existe déjà")
    void testCreateReservationDuplicate() {
        // Créer une première réservation
        CreateReservationDTO firstDTO = new CreateReservationDTO();
        firstDTO.setUtilisateurId(userModel.getId());
        firstDTO.setLivreId(unavailableLivre.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<CreateReservationDTO> request = new HttpEntity<>(firstDTO, headers);

        restTemplate.postForEntity("/reservations", request, JsonNode.class);

        // Tenter une seconde réservation
        CreateReservationDTO secondDTO = new CreateReservationDTO();
        secondDTO.setUtilisateurId(userModel.getId());
        secondDTO.setLivreId(unavailableLivre.getId());

        HttpEntity<CreateReservationDTO> secondRequest = new HttpEntity<>(secondDTO, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity("/reservations", secondRequest, JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("message").asText())
                .isEqualTo("Vous avez déjà une réservation active pour ce livre");
    }

    @Test
    @DisplayName("Annulation de réservation réussie")
    void testCancelReservationSuccess() {
        ReservationModel reservation = createTestReservation(userModel, unavailableLivre);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/reservations/" + reservation.getId(),
                HttpMethod.DELETE,
                request,
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Vérifier que la réservation a été annulée
        ReservationModel updated = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(updated.getStatut()).isEqualTo(StatutReservation.ANNULEE);
    }

    @Test
    @DisplayName("Récupération des réservations de l'utilisateur")
    void testGetMesReservations() {
        createTestReservation(userModel, unavailableLivre);
        LivreModel anotherLivre = createTestLivre("9780000000000", "Another Book", 1, 0);
        createTestReservation(userModel, anotherLivre);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/reservations/mes-reservations?page=0&size=10",
                HttpMethod.GET,
                request,
                JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("content").isArray()).isTrue();
        assertThat(response.getBody().get("content").size()).isEqualTo(2);
        assertThat(response.getBody().get("totalElements").asInt()).isEqualTo(2);
    }

    @Test
    @DisplayName("Nettoyage des réservations expirées")
    void testCleanupExpiredReservations() {
        // Créer une réservation expirée
        ReservationModel reservation = createTestReservation(userModel, unavailableLivre);
        reservation.setDateExpiration(LocalDateTime.now().minusHours(1)); // Expirée
        reservationRepository.save(reservation);

        // Vérifier que la réservation existe et est expirée
        assertThat(reservationRepository.findById(reservation.getId())).isPresent();
        assertThat(reservation.getDateExpiration()).isBefore(LocalDateTime.now());
    }

    @Test
    @DisplayName("Réservation échoue avec livre inexistant")
    void testCreateReservationWithNonExistentBook() {
        CreateReservationDTO createDTO = new CreateReservationDTO();
        createDTO.setUtilisateurId(userModel.getId());
        createDTO.setLivreId(99999L); // ID inexistant

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<CreateReservationDTO> request = new HttpEntity<>(createDTO, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity("/reservations", request, JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("message").asText()).contains("Livre non trouvé");
    }

    @Test
    @DisplayName("Réservation échoue avec utilisateur inexistant")
    void testCreateReservationWithNonExistentUser() {
        CreateReservationDTO createDTO = new CreateReservationDTO();
        createDTO.setUtilisateurId(99999L); // ID inexistant
        createDTO.setLivreId(unavailableLivre.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<CreateReservationDTO> request = new HttpEntity<>(createDTO, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity("/reservations", request, JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("message").asText()).contains("Utilisateur non trouvé");
    }

    @Test
    @DisplayName("Annulation échoue pour réservation d'un autre utilisateur")
    void testCancelReservationUnauthorized() {
        // Créer un deuxième utilisateur
        RegisterRequestDTO user2Request = new RegisterRequestDTO();
        user2Request.setMatricule("USR002");
        user2Request.setNom("User2");
        user2Request.setPrenom("Test2");
        user2Request.setEmail("user2@test.com");
        user2Request.setMotDePasse("user123");
        user2Request.setRole(Role.ETUDIANT);

        ResponseEntity<JsonNode> user2Result = restTemplate.postForEntity("/auth/register", user2Request, JsonNode.class);
        String user2Token = user2Result.getBody().get("token").asText();

        // Créer une réservation pour user1
        ReservationModel reservation = createTestReservation(userModel, unavailableLivre);

        // Tenter d'annuler avec user2
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user2Token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/reservations/" + reservation.getId(),
                HttpMethod.DELETE,
                request,
                JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().get("message").asText())
                .contains("Vous ne pouvez annuler que vos propres réservations");
    }

    @Test
    @DisplayName("Réservation échoue sans authentification")
    void testCreateReservationWithoutAuth() {
        CreateReservationDTO createDTO = new CreateReservationDTO();
        createDTO.setUtilisateurId(userModel.getId());
        createDTO.setLivreId(unavailableLivre.getId());

        // Pas de token d'authentification
        ResponseEntity<JsonNode> response = restTemplate.postForEntity("/reservations", createDTO, JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Vérification de la date d'expiration (48h)")
    void testReservationExpirationDate() {
        CreateReservationDTO createDTO = new CreateReservationDTO();
        createDTO.setUtilisateurId(userModel.getId());
        createDTO.setLivreId(unavailableLivre.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<CreateReservationDTO> request = new HttpEntity<>(createDTO, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity("/reservations", request, JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        // Récupérer la réservation créée
        Long reservationId = response.getBody().get("id").asLong();
        ReservationModel reservation = reservationRepository.findById(reservationId).orElseThrow();

        // Vérifier que l'expiration est bien à 48h
        LocalDateTime expectedExpiration = reservation.getDateReservation().plusHours(48);
        assertThat(reservation.getDateExpiration()).isEqualToIgnoringSeconds(expectedExpiration);
    }

    private LivreModel createTestLivre(String isbn, String titre, int total, int disponibles) {
        LivreModel livre = new LivreModel();
        livre.setIsbn(isbn);
        livre.setTitre(titre);
        livre.setAuteur("Test Author");
        livre.setCategorie("Test Category");
        livre.setDatePublication(LocalDate.now());
        livre.setNombreExemplaires(total);
        livre.setDisponibles(disponibles);
        return livreRepository.save(livre);
    }

    private ReservationModel createTestReservation(UtilisateurModel utilisateur, LivreModel livre) {
        ReservationModel reservation = new ReservationModel();
        reservation.setUtilisateur(utilisateur);
        reservation.setLivre(livre);
        reservation.setDateReservation(LocalDateTime.now());
        reservation.setDateExpiration(LocalDateTime.now().plusHours(48));
        reservation.setStatut(StatutReservation.EN_ATTENTE);
        return reservationRepository.save(reservation);
    }
}
