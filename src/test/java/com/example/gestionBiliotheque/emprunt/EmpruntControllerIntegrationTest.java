package com.example.gestionBiliotheque.emprunt;

import com.example.gestionBiliotheque.auth.dto.RegisterRequestDTO;
import com.example.gestionBiliotheque.emprunt.dto.CreateEmpruntDTO;
import com.example.gestionBiliotheque.livres.LivreModel;
import com.example.gestionBiliotheque.livres.LivreRepository;
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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration pour la gestion des emprunts
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Tests de Gestion des Emprunts")
class EmpruntControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmpruntRepository empruntRepository;

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    private String userToken;
    private String managerToken;
    private UtilisateurModel userModel;
    private UtilisateurModel managerModel;
    private LivreModel livreModel;

    @BeforeEach
    void setUp() {
        empruntRepository.deleteAll();
        livreRepository.deleteAll();
        utilisateurRepository.deleteAll();

        // Créer un utilisateur USER
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

        // Créer un utilisateur MANAGER
        RegisterRequestDTO managerRequest = new RegisterRequestDTO();
        managerRequest.setMatricule("MGR001");
        managerRequest.setNom("Manager");
        managerRequest.setPrenom("Test");
        managerRequest.setEmail("manager@test.com");
        managerRequest.setMotDePasse("manager123");
        managerRequest.setRole(Role.BIBLIOTHECAIRE);

        ResponseEntity<JsonNode> managerResult = restTemplate.postForEntity("/auth/register", managerRequest,
                JsonNode.class);
        assertThat(managerResult.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        managerToken = managerResult.getBody().get("token").asText();
        managerModel = utilisateurRepository.findByEmail("manager@test.com").orElseThrow();

        // Créer un livre de test
        livreModel = createTestLivre("9781234567890", "Clean Code", 5);
    }

    @Test
    @DisplayName("Création d'emprunt réussie")
    void testCreateEmpruntSuccess() {
        CreateEmpruntDTO createDTO = new CreateEmpruntDTO();
        createDTO.setUtilisateurId(userModel.getId());
        createDTO.setLivreId(livreModel.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<CreateEmpruntDTO> request = new HttpEntity<>(createDTO, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity("/emprunts", request, JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().get("statut").asText()).isEqualTo("EN_COURS");
        assertThat(response.getBody().get("penalite").asInt()).isEqualTo(0);
        assertThat(response.getBody().get("utilisateur").get("email").asText()).isEqualTo("user@test.com");
        assertThat(response.getBody().get("livre").get("titre").asText()).isEqualTo("Clean Code");

        // Vérifier que le livre disponible a été décrémenté
        LivreModel updatedLivre = livreRepository.findById(livreModel.getId()).orElseThrow();
        assertThat(updatedLivre.getDisponibles()).isEqualTo(4);
    }

    @Test
    @DisplayName("Emprunt échoue si limite atteinte (USER: 3 livres)")
    void testCreateEmpruntLimitExceeded() {
        // Créer 3 emprunts pour l'utilisateur
        for (int i = 0; i < 3; i++) {
            LivreModel livre = createTestLivre("978123456789" + i, "Book " + i, 5);
            createTestEmprunt(userModel, livre);
        }

        // Tenter un 4ème emprunt
        LivreModel newLivre = createTestLivre("9789999999999", "Fourth Book", 5);
        CreateEmpruntDTO createDTO = new CreateEmpruntDTO();
        createDTO.setUtilisateurId(userModel.getId());
        createDTO.setLivreId(newLivre.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<CreateEmpruntDTO> request = new HttpEntity<>(createDTO, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity("/emprunts", request, JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message").asText())
                .isEqualTo("Limite d'emprunts atteinte (3/3). Veuillez retourner un livre avant d'emprunter.");
    }

    @Test
    @DisplayName("MANAGER peut emprunter 5 livres")
    void testManagerCanBorrow5Books() {
        // Créer 5 emprunts pour le manager
        for (int i = 0; i < 5; i++) {
            LivreModel livre = createTestLivre("978123456789" + i, "Book " + i, 5);
            CreateEmpruntDTO createDTO = new CreateEmpruntDTO();
            createDTO.setUtilisateurId(managerModel.getId());
            createDTO.setLivreId(livre.getId());

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(managerToken);
            HttpEntity<CreateEmpruntDTO> request = new HttpEntity<>(createDTO, headers);

            ResponseEntity<JsonNode> response = restTemplate.postForEntity("/emprunts", request, JsonNode.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        // Vérifier qu'il a bien 5 emprunts
        long count = empruntRepository.countByUtilisateurAndStatut(managerModel, StatutEmprunt.EN_COURS);
        assertThat(count).isEqualTo(5);
    }

    @Test
    @DisplayName("Emprunt échoue si livre non disponible - crée réservation")
    void testCreateEmpruntBookNotAvailable() {
        LivreModel unavailableLivre = createTestLivre("9780000000000", "Unavailable Book", 0);

        CreateEmpruntDTO createDTO = new CreateEmpruntDTO();
        createDTO.setUtilisateurId(userModel.getId());
        createDTO.setLivreId(unavailableLivre.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<CreateEmpruntDTO> request = new HttpEntity<>(createDTO, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity("/emprunts", request, JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message").asText())
                .isEqualTo("Livre non disponible. Une réservation a été créée pour vous (valable 48h).");
        assertThat(response.getBody().has("reservationId")).isTrue();
    }

    @Test
    @DisplayName("Emprunt échoue si pénalités > 10000 GNF")
    void testCreateEmpruntBlockedByPenalties() {
        // Créer un emprunt avec pénalité élevée
        EmpruntModel emprunt = createTestEmprunt(userModel, livreModel);
        emprunt.setPenalite(new BigDecimal("15000"));
        empruntRepository.save(emprunt);

        LivreModel newLivre = createTestLivre("9789999999999", "New Book", 5);
        CreateEmpruntDTO createDTO = new CreateEmpruntDTO();
        createDTO.setUtilisateurId(userModel.getId());
        createDTO.setLivreId(newLivre.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<CreateEmpruntDTO> request = new HttpEntity<>(createDTO, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity("/emprunts", request, JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().get("message").asText()).isEqualTo(
                "Vous avez 15000 GNF de pénalités impayées. Veuillez régulariser avant d'emprunter (maximum autorisé: 10000 GNF).");
    }

    @Test
    @DisplayName("Retour de livre réussi sans retard")
    void testReturnLivreOnTime() {
        EmpruntModel emprunt = createTestEmprunt(userModel, livreModel);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/emprunts/" + emprunt.getId() + "/retour",
                HttpMethod.PUT,
                request,
                JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("statut").asText()).isEqualTo("TERMINE");
        assertThat(response.getBody().get("penalite").asInt()).isEqualTo(0);
        assertThat(response.getBody().has("dateRetourEffective")).isTrue();

        // Vérifier que le livre disponible a été incrémenté
        LivreModel updatedLivre = livreRepository.findById(livreModel.getId()).orElseThrow();
        assertThat(updatedLivre.getDisponibles()).isEqualTo(5);
    }

    @Test
    @DisplayName("Retour de livre en retard calcule pénalité")
    void testReturnLivreLateWithPenalty() {
        EmpruntModel emprunt = createTestEmprunt(userModel, livreModel);
        // Simuler un retard de 5 jours
        emprunt.setDateRetourPrevue(LocalDateTime.now().minusDays(5));
        empruntRepository.save(emprunt);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/emprunts/" + emprunt.getId() + "/retour",
                HttpMethod.PUT,
                request,
                JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("statut").asText()).isEqualTo("TERMINE");
        assertThat(response.getBody().get("penalite").asInt()).isEqualTo(5000); // 5 jours * 1000 GNF
    }

    @Test
    @DisplayName("Récupération des emprunts de l'utilisateur")
    void testGetMesEmprunts() {
        createTestEmprunt(userModel, livreModel);
        LivreModel livre2 = createTestLivre("9780987654321", "Another Book", 5);
        createTestEmprunt(userModel, livre2);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/emprunts/mes-emprunts?page=0&size=10",
                HttpMethod.GET,
                request,
                JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("content").isArray()).isTrue();
        assertThat(response.getBody().get("content").size()).isEqualTo(2);
        assertThat(response.getBody().get("totalElements").asInt()).isEqualTo(2);
    }

    @Test
    @DisplayName("Récupération des emprunts en retard (MANAGER)")
    void testGetEmpruntsEnRetard() {
        // Créer un emprunt en retard
        EmpruntModel emprunt = createTestEmprunt(userModel, livreModel);
        emprunt.setDateRetourPrevue(LocalDateTime.now().minusDays(5));
        empruntRepository.save(emprunt);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(managerToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/emprunts/en-retard?page=0&size=10",
                HttpMethod.GET,
                request,
                JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("content").isArray()).isTrue();
        assertThat(response.getBody().get("content").size()).isEqualTo(1);
    }

    @Test
    @DisplayName("USER ne peut pas accéder aux emprunts en retard")
    void testGetEmpruntsEnRetardForbiddenForUser() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/emprunts/en-retard",
                HttpMethod.GET,
                request,
                JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private LivreModel createTestLivre(String isbn, String titre, int disponibles) {
        LivreModel livre = new LivreModel();
        livre.setIsbn(isbn);
        livre.setTitre(titre);
        livre.setAuteur("Test Author");
        livre.setCategorie("Test Category");
        livre.setDatePublication(LocalDate.now());
        livre.setNombreExemplaires(disponibles);
        livre.setDisponibles(disponibles);
        return livreRepository.save(livre);
    }

    private EmpruntModel createTestEmprunt(UtilisateurModel utilisateur, LivreModel livre) {
        EmpruntModel emprunt = new EmpruntModel();
        emprunt.setUtilisateur(utilisateur);
        emprunt.setLivre(livre);
        emprunt.setDateEmprunt(LocalDateTime.now());
        emprunt.setDateRetourPrevue(LocalDateTime.now().plusDays(14));
        emprunt.setStatut(StatutEmprunt.EN_COURS);
        emprunt.setPenalite(BigDecimal.ZERO);

        // Décrémenter disponibles
        livre.setDisponibles(livre.getDisponibles() - 1);
        livreRepository.save(livre);

        return empruntRepository.save(emprunt);
    }
}
