package com.example.gestionBiliotheque.livres.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO de requête pour la mise à jour d'un livre
 * Tous les champs sont optionnels pour permettre les mises à jour partielles
 */
public class UpdateLivreDTO {

    @Pattern(regexp = "^\\d{10}$|^\\d{13}$", message = "L'ISBN doit contenir 10 ou 13 chiffres")
    private String isbn;

    @Size(max = 200, message = "Le titre ne peut pas dépasser 200 caractères")
    private String titre;

    @Size(max = 100, message = "L'auteur ne peut pas dépasser 100 caractères")
    private String auteur;

    @Size(max = 50, message = "La catégorie ne peut pas dépasser 50 caractères")
    private String categorie;

    private LocalDate datePublication;

    @Min(value = 1, message = "Le nombre d'exemplaires doit être au moins 1")
    private Integer nombreExemplaires;

    @Min(value = 0, message = "Le nombre de disponibles ne peut pas être négatif")
    private Integer disponibles;

    // Constructeurs
    public UpdateLivreDTO() {
    }

    // Getters et Setters
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

    public Integer getNombreExemplaires() {
        return nombreExemplaires;
    }

    public void setNombreExemplaires(Integer nombreExemplaires) {
        this.nombreExemplaires = nombreExemplaires;
    }

    public Integer getDisponibles() {
        return disponibles;
    }

    public void setDisponibles(Integer disponibles) {
        this.disponibles = disponibles;
    }
}
