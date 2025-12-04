package com.example.gestionBiliotheque.livres.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO de réponse pour les données de livre
 */
public class LivreDTO {

    private Long id;

    @NotBlank
    @Pattern(regexp = "^\\d{10}$|^\\d{13}$", message = "L'ISBN doit contenir 10 ou 13 chiffres")
    private String isbn;

    @NotBlank
    @Size(max = 200)
    private String titre;

    @NotBlank
    @Size(max = 100)
    private String auteur;

    @Size(max = 50)
    private String categorie;

    private LocalDate datePublication;

    @Min(1)
    private int nombreExemplaires;

    @Min(0)
    private int disponibles;

    // Constructeurs
    public LivreDTO() {
    }

    public LivreDTO(Long id, String isbn, String titre, String auteur, String categorie,
            LocalDate datePublication, int nombreExemplaires, int disponibles) {
        this.id = id;
        this.isbn = isbn;
        this.titre = titre;
        this.auteur = auteur;
        this.categorie = categorie;
        this.datePublication = datePublication;
        this.nombreExemplaires = nombreExemplaires;
        this.disponibles = disponibles;
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

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public LocalDate getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(LocalDate datePublication) {
        this.datePublication = datePublication;
    }

    public int getNombreExemplaires() {
        return nombreExemplaires;
    }

    public void setNombreExemplaires(int nombreExemplaires) {
        this.nombreExemplaires = nombreExemplaires;
    }

    public int getDisponibles() {
        return disponibles;
    }

    public void setDisponibles(int disponibles) {
        this.disponibles = disponibles;
    }
}
