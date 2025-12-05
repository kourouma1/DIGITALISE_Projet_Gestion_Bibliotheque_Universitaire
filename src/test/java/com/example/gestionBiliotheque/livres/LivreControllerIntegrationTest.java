package com.example.gestionBiliotheque.livres;

import com.example.gestionBiliotheque.auth.dto.RegisterRequestDTO;
import com.example.gestionBiliotheque.livres.dto.CreateLivreDTO;
import com.example.gestionBiliotheque.livres.dto.UpdateLivreDTO;
import com.example.gestionBiliotheque.utilisateurs.Role;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration pour la gestion des livres
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("Tests de Gestion des Livres")
class LivreControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        livreRepository.deleteAll();
        utilisateurRepository.deleteAll();

        // Créer un utilisateur ADMIN pour les tests
        RegisterRequestDTO adminRequest = new RegisterRequestDTO();
        adminRequest.setMatricule("ADM001");
        adminRequest.setNom("Admin");
        adminRequest.setPrenom("Test");
        adminRequest.setEmail("admin@test.com");
        adminRequest.setMotDePasse("admin123");
        adminRequest.setRole(Role.ADMIN);

        ResponseEntity<JsonNode> adminResponse = restTemplate.postForEntity("/auth/register", adminRequest,
                JsonNode.class);
        assertThat(adminResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        adminToken = adminResponse.getBody().get("token").asText();

        // Créer un utilisateur USER pour les tests
        RegisterRequestDTO userRequest = new RegisterRequestDTO();
        userRequest.setMatricule("USR001");
        userRequest.setNom("User");
        userRequest.setPrenom("Test");
        userRequest.setEmail("user@test.com");
        userRequest.setMotDePasse("user123");
        userRequest.setRole(Role.ETUDIANT);

        ResponseEntity<JsonNode> userResponse = restTemplate.postForEntity("/auth/register", userRequest,
                JsonNode.class);
        assertThat(userResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        userToken = userResponse.getBody().get("token").asText();
    }

    @Test
    @DisplayName("Création de livre réussie (ADMIN)")
    void testCreateLivreAsAdmin() {
        CreateLivreDTO createLivreDTO = new CreateLivreDTO();
        createLivreDTO.setIsbn("9781234567890");
        createLivreDTO.setTitre("Clean Code");
        createLivreDTO.setAuteur("Robert C. Martin");
        createLivreDTO.setCategorie("Programming");
        createLivreDTO.setDatePublication(LocalDate.of(2008, 8, 1));
        createLivreDTO.setNombreExemplaires(5);
        createLivreDTO.setDisponibles(5);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<CreateLivreDTO> request = new HttpEntity<>(createLivreDTO, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity("/livres", request, JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().get("isbn").asText()).isEqualTo("9781234567890");
        assertThat(response.getBody().get("titre").asText()).isEqualTo("Clean Code");
        assertThat(response.getBody().get("auteur").asText()).isEqualTo("Robert C. Martin");
    }

    @Test
    @DisplayName("Création de livre échoue (USER - pas autorisé)")
    void testCreateLivreAsUserForbidden() {
        CreateLivreDTO createLivreDTO = new CreateLivreDTO();
        createLivreDTO.setIsbn("9781234567890");
        createLivreDTO.setTitre("Clean Code");
        createLivreDTO.setAuteur("Robert C. Martin");
        createLivreDTO.setCategorie("Programming");
        createLivreDTO.setDatePublication(LocalDate.of(2008, 8, 1));
        createLivreDTO.setNombreExemplaires(5);
        createLivreDTO.setDisponibles(5);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<CreateLivreDTO> request = new HttpEntity<>(createLivreDTO, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity("/livres", request, JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Récupération de tous les livres (paginé)")
    void testGetAllLivres() {
        // Créer quelques livres
        createTestLivre("9781234567890", "Clean Code", "Robert C. Martin");
        createTestLivre("9780987654321", "The Pragmatic Programmer", "Andrew Hunt");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/livres?page=0&size=10",
                HttpMethod.GET,
                request,
                JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("content").isArray()).isTrue();
        assertThat(response.getBody().get("content").size()).isEqualTo(2);
        assertThat(response.getBody().get("totalElements").asInt()).isEqualTo(2);
    }

    @Test
    @DisplayName("Récupération d'un livre par ID")
    void testGetLivreById() {
        LivreModel livre = createTestLivre("9781234567890", "Clean Code", "Robert C. Martin");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/livres/" + livre.getId(),
                HttpMethod.GET,
                request,
                JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("isbn").asText()).isEqualTo("9781234567890");
        assertThat(response.getBody().get("titre").asText()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("Récupération d'un livre inexistant retourne 404")
    void testGetLivreByIdNotFound() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/livres/999",
                HttpMethod.GET,
                request,
                JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("message").asText()).isEqualTo("Livre non trouvé(e) avec id : '999'");
    }

    @Test
    @DisplayName("Recherche de livres par mot-clé")
    void testSearchLivres() {
        createTestLivre("9781234567890", "Clean Code", "Robert C. Martin");
        createTestLivre("9780987654321", "Clean Architecture", "Robert C. Martin");
        createTestLivre("9781111111111", "The Pragmatic Programmer", "Andrew Hunt");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/livres/search?q=clean&page=0&size=10",
                HttpMethod.GET,
                request,
                JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("content").isArray()).isTrue();
        assertThat(response.getBody().get("content").size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Mise à jour d'un livre (ADMIN)")
    void testUpdateLivre() {
        LivreModel livre = createTestLivre("9781234567890", "Clean Code", "Robert C. Martin");

        UpdateLivreDTO updateDTO = new UpdateLivreDTO();
        updateDTO.setDisponibles(3);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<UpdateLivreDTO> request = new HttpEntity<>(updateDTO, headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "/livres/" + livre.getId(),
                HttpMethod.PUT,
                request,
                JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("disponibles").asInt()).isEqualTo(3);
    }

    @Test
    @DisplayName("Suppression d'un livre (ADMIN)")
    void testDeleteLivre() {
        LivreModel livre = createTestLivre("9781234567890", "Clean Code", "Robert C. Martin");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/livres/" + livre.getId(),
                HttpMethod.DELETE,
                request,
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Vérifier que le livre a été supprimé
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(userToken);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

        ResponseEntity<JsonNode> checkResponse = restTemplate.exchange(
                "/livres/" + livre.getId(),
                HttpMethod.GET,
                userRequest,
                JsonNode.class);

        assertThat(checkResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Création de livre échoue avec ISBN dupliqué")
    void testCreateLivreDuplicateIsbn() {
        createTestLivre("9781234567890", "Clean Code", "Robert C. Martin");

        CreateLivreDTO duplicateDTO = new CreateLivreDTO();
        duplicateDTO.setIsbn("9781234567890"); // ISBN dupliqué
        duplicateDTO.setTitre("Another Book");
        duplicateDTO.setAuteur("Another Author");
        duplicateDTO.setCategorie("Programming");
        duplicateDTO.setDatePublication(LocalDate.now());
        duplicateDTO.setNombreExemplaires(1);
        duplicateDTO.setDisponibles(1);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<CreateLivreDTO> request = new HttpEntity<>(duplicateDTO, headers);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity("/livres", request, JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("message").asText())
                .isEqualTo("Livre existe déjà avec ISBN : '9781234567890'");
    }

    private LivreModel createTestLivre(String isbn, String titre, String auteur) {
        LivreModel livre = new LivreModel();
        livre.setIsbn(isbn);
        livre.setTitre(titre);
        livre.setAuteur(auteur);
        livre.setCategorie("Programming");
        livre.setDatePublication(LocalDate.of(2008, 1, 1));
        livre.setNombreExemplaires(5);
        livre.setDisponibles(5);
        return livreRepository.save(livre);
    }
}
