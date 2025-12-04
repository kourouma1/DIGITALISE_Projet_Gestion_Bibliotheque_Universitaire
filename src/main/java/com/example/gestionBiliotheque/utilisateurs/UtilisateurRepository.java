package com.example.gestionBiliotheque.utilisateurs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface UtilisateurRepository extends JpaRepository<UtilisateurModel, Long> {

    java.util.Optional<UtilisateurModel> findByEmail(String email);

    java.util.Optional<UtilisateurModel> findByMatricule(String matricule);

    boolean existsByEmail(String email);

    boolean existsByMatricule(String matricule);
}
