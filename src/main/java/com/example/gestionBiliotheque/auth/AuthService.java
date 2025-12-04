package com.example.gestionBiliotheque.auth;

import com.example.gestionBiliotheque.auth.dto.AuthResponseDTO;
import com.example.gestionBiliotheque.auth.dto.LoginRequestDTO;
import com.example.gestionBiliotheque.auth.dto.RegisterRequestDTO;
import com.example.gestionBiliotheque.auth.exception.AuthException;
import com.example.gestionBiliotheque.security.JwtTokenProvider;
import com.example.gestionBiliotheque.utilisateurs.Role;
import com.example.gestionBiliotheque.utilisateurs.UtilisateurModel;
import com.example.gestionBiliotheque.utilisateurs.UtilisateurRepository;
import com.example.gestionBiliotheque.utilisateurs.dto.UtilisateurDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service pour gérer l'authentification et l'inscription
 */
@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Inscription d'un nouvel utilisateur
     */
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO registerRequest) {
        // Vérifier si l'email existe déjà
        if (utilisateurRepository.existsByEmail(registerRequest.getEmail())) {
            throw new AuthException("L'email est déjà utilisé");
        }

        // Vérifier si le matricule existe déjà
        if (utilisateurRepository.existsByMatricule(registerRequest.getMatricule())) {
            throw new AuthException("Le matricule est déjà utilisé");
        }

        // Créer le nouvel utilisateur
        UtilisateurModel utilisateur = new UtilisateurModel();
        utilisateur.setMatricule(registerRequest.getMatricule());
        utilisateur.setNom(registerRequest.getNom());
        utilisateur.setPrenom(registerRequest.getPrenom());
        utilisateur.setEmail(registerRequest.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(registerRequest.getMotDePasse()));
        utilisateur.setRole(registerRequest.getRole() != null ? registerRequest.getRole() : Role.ETUDIANT);
        utilisateur.setActif(true);

        // Sauvegarder l'utilisateur
        utilisateur = utilisateurRepository.save(utilisateur);

        // Générer le token JWT
        String token = tokenProvider.generateTokenFromUsername(utilisateur.getEmail());

        // Créer le DTO de réponse
        UtilisateurDTO utilisateurDTO = convertToDTO(utilisateur);
        return new AuthResponseDTO(token, utilisateurDTO);
    }

    /**
     * Connexion d'un utilisateur
     */
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        // Authentifier l'utilisateur
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getMotDePasse()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Générer le token JWT
        String token = tokenProvider.generateToken(authentication);

        // Récupérer l'utilisateur
        UtilisateurModel utilisateur = utilisateurRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new AuthException("Utilisateur non trouvé"));

        // Créer le DTO de réponse
        UtilisateurDTO utilisateurDTO = convertToDTO(utilisateur);
        return new AuthResponseDTO(token, utilisateurDTO);
    }

    /**
     * Convertit un UtilisateurModel en UtilisateurDTO
     */
    private UtilisateurDTO convertToDTO(UtilisateurModel utilisateur) {
        return new UtilisateurDTO(
                utilisateur.getId(),
                utilisateur.getMatricule(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getEmail(),
                utilisateur.getRole(),
                utilisateur.getDateInscription(),
                utilisateur.isActif());
    }
}
