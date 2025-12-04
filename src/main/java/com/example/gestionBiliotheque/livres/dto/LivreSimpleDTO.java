package com.example.gestionBiliotheque.livres.dto;

/**
 * DTO simplifié pour les livres
 * Utilisé dans les réponses imbriquées pour éviter les références circulaires
 */
public class LivreSimpleDTO {

    private Long id;
    private String isbn;
    private String titre;
    private String auteur;

    // Constructeurs
    public LivreSimpleDTO() {
    }

    public LivreSimpleDTO(Long id, String isbn, String titre, String auteur) {
        this.id = id;
        this.isbn = isbn;
        this.titre = titre;
        this.auteur = auteur;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getAuteur() {
        return auteur;
    }

    public void setAuteur(String auteur) {
        this.auteur = auteur;
    }
}
