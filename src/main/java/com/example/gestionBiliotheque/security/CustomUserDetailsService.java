package com.example.gestionBiliotheque.security;

import com.example.gestionBiliotheque.utilisateurs.UtilisateurModel;
import com.example.gestionBiliotheque.utilisateurs.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * Service pour charger les détails de l'utilisateur depuis la base de données
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UtilisateurModel utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + email));

        if (!utilisateur.isActif()) {
            throw new UsernameNotFoundException("Le compte utilisateur est désactivé");
        }

        return new User(
                utilisateur.getEmail(),
                utilisateur.getMotDePasse(),
                getAuthorities(utilisateur));
    }

    /**
     * Convertit le rôle de l'utilisateur en autorités Spring Security
     */
    private Collection<? extends GrantedAuthority> getAuthorities(UtilisateurModel utilisateur) {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name()));
    }
}
