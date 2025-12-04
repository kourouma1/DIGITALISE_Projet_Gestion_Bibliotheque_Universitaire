package com.example.gestionBiliotheque.auth;

import com.example.gestionBiliotheque.auth.dto.AuthResponseDTO;
import com.example.gestionBiliotheque.auth.dto.LoginRequestDTO;
import com.example.gestionBiliotheque.auth.dto.RegisterRequestDTO;
import com.example.gestionBiliotheque.utilisateurs.Role;
import com.example.gestionBiliotheque.utilisateurs.UtilisateurRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration pour l'authentification
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Tests d'Authentification")
class AuthControllerIntegrationTest {

        @Autowired
        private TestRestTemplate restTemplate;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UtilisateurRepository utilisateurRepository;

        @BeforeEach
        void setUp() {
                utilisateurRepository.deleteAll();
        }

        @Test
        @DisplayName("Inscription réussie d'un nouvel utilisateur")
        void testRegisterSuccess() {
                RegisterRequestDTO registerRequest = new RegisterRequestDTO();
                registerRequest.setMatricule("ETU001");
                registerRequest.setNom("Dupont");
                registerRequest.setPrenom("Jean");
                registerRequest.setEmail("jean.dupont@test.com");
                registerRequest.setMotDePasse("password123");
                registerRequest.setRole(Role.ETUDIANT);

                ResponseEntity<JsonNode> response = restTemplate.postForEntity("/auth/register", registerRequest,
                                JsonNode.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                assertThat(response.getBody().has("token")).isTrue();
                assertThat(response.getBody().get("type").asText()).isEqualTo("Bearer");
                assertThat(response.getBody().get("utilisateur").get("email").asText())
                                .isEqualTo("jean.dupont@test.com");
                assertThat(response.getBody().get("utilisateur").get("nom").asText()).isEqualTo("Dupont");

                assertThat(response.getBody().get("token").asText()).isNotEmpty();
                assertThat(response.getBody().get("utilisateur").get("role").asText()).isEqualTo("ETUDIANT");
        }

        @Test
        @DisplayName("Inscription échoue avec email déjà utilisé")
        void testRegisterDuplicateEmail() {
                // Créer un premier utilisateur
                RegisterRequestDTO firstUser = new RegisterRequestDTO();
                firstUser.setMatricule("ETU001");
                firstUser.setNom("Dupont");
                firstUser.setPrenom("Jean");
                firstUser.setEmail("jean.dupont@test.com");
                firstUser.setMotDePasse("password123");
                firstUser.setRole(Role.ETUDIANT);

                restTemplate.postForEntity("/auth/register", firstUser, JsonNode.class);

                // Tenter de créer un second utilisateur avec le même email
                RegisterRequestDTO secondUser = new RegisterRequestDTO();
                secondUser.setMatricule("ETU002");
                secondUser.setNom("Martin");
                secondUser.setPrenom("Pierre");
                secondUser.setEmail("jean.dupont@test.com"); // Email dupliqué
                secondUser.setMotDePasse("password456");
                secondUser.setRole(Role.ETUDIANT);

                ResponseEntity<JsonNode> response = restTemplate.postForEntity("/auth/register", secondUser,
                                JsonNode.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(response.getBody().get("message").asText()).isEqualTo("L'email est déjà utilisé");
        }

        @Test
        @DisplayName("Inscription échoue avec matricule déjà utilisé")
        void testRegisterDuplicateMatricule() {
                RegisterRequestDTO firstUser = new RegisterRequestDTO();
                firstUser.setMatricule("ETU001");
                firstUser.setNom("Dupont");
                firstUser.setPrenom("Jean");
                firstUser.setEmail("jean.dupont@test.com");
                firstUser.setMotDePasse("password123");
                firstUser.setRole(Role.ETUDIANT);

                restTemplate.postForEntity("/auth/register", firstUser, JsonNode.class);

                RegisterRequestDTO secondUser = new RegisterRequestDTO();
                secondUser.setMatricule("ETU001"); // Matricule dupliqué
                secondUser.setNom("Martin");
                secondUser.setPrenom("Pierre");
                secondUser.setEmail("pierre.martin@test.com");
                secondUser.setMotDePasse("password456");
                secondUser.setRole(Role.ETUDIANT);

                ResponseEntity<JsonNode> response = restTemplate.postForEntity("/auth/register", secondUser,
                                JsonNode.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(response.getBody().get("message").asText()).isEqualTo("Le matricule est déjà utilisé");
        }

        @Test
        @DisplayName("Connexion réussie avec credentials valides")
        void testLoginSuccess() {
                // D'abord, créer un utilisateur
                RegisterRequestDTO registerRequest = new RegisterRequestDTO();
                registerRequest.setMatricule("ETU001");
                registerRequest.setNom("Dupont");
                registerRequest.setPrenom("Jean");
                registerRequest.setEmail("jean.dupont@test.com");
                registerRequest.setMotDePasse("password123");
                registerRequest.setRole(Role.ETUDIANT);

                restTemplate.postForEntity("/auth/register", registerRequest, JsonNode.class);

                // Ensuite, se connecter
                LoginRequestDTO loginRequest = new LoginRequestDTO();
                loginRequest.setEmail("jean.dupont@test.com");
                loginRequest.setMotDePasse("password123");

                ResponseEntity<JsonNode> response = restTemplate.postForEntity("/auth/login", loginRequest,
                                JsonNode.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().has("token")).isTrue();
                assertThat(response.getBody().get("type").asText()).isEqualTo("Bearer");
                assertThat(response.getBody().get("utilisateur").get("email").asText())
                                .isEqualTo("jean.dupont@test.com");
        }

        @Test
        @DisplayName("Connexion échoue avec mot de passe incorrect")
        void testLoginWrongPassword() {
                // Créer un utilisateur
                RegisterRequestDTO registerRequest = new RegisterRequestDTO();
                registerRequest.setMatricule("ETU001");
                registerRequest.setNom("Dupont");
                registerRequest.setPrenom("Jean");
                registerRequest.setEmail("jean.dupont@test.com");
                registerRequest.setMotDePasse("password123");
                registerRequest.setRole(Role.ETUDIANT);

                restTemplate.postForEntity("/auth/register", registerRequest, JsonNode.class);

                // Tenter de se connecter avec un mauvais mot de passe
                LoginRequestDTO loginRequest = new LoginRequestDTO();
                loginRequest.setEmail("jean.dupont@test.com");
                loginRequest.setMotDePasse("wrongpassword");

                ResponseEntity<JsonNode> response = restTemplate.postForEntity("/auth/login", loginRequest,
                                JsonNode.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                assertThat(response.getBody().get("message").asText()).isEqualTo("Email ou mot de passe incorrect");
        }

        @Test
        @DisplayName("Inscription échoue avec données invalides")
        void testRegisterInvalidData() {
                RegisterRequestDTO invalidRequest = new RegisterRequestDTO();
                invalidRequest.setMatricule(""); // Matricule vide
                invalidRequest.setNom("");
                invalidRequest.setPrenom("");
                invalidRequest.setEmail("invalid-email"); // Email invalide
                invalidRequest.setMotDePasse("123"); // Mot de passe trop court

                ResponseEntity<JsonNode> response = restTemplate.postForEntity("/auth/register", invalidRequest,
                                JsonNode.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(response.getBody().has("errors")).isTrue();
        }
}
