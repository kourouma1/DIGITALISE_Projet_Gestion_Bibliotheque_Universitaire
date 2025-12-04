package com.example.gestionBiliotheque.livres;


import com.example.gestionBiliotheque.emprunt.EmpruntModel;
import com.example.gestionBiliotheque.reservations.ReservationModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "livres", uniqueConstraints = @UniqueConstraint(columnNames = "isbn"))
public class LivreModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 17)
    @NotBlank
    @Pattern(regexp = "^\\d{10}$|^\\d{13}$")
    private String isbn;

    @Column(nullable = false, length = 200)
    @NotBlank
    @Size(max = 200)
    private String titre;

    @Column(nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String auteur;

    @Column(length = 50)
    private String categorie;

    private LocalDate datePublication;

    @Min(1)
    private int nombreExemplaires = 1;

    @Min(0)
    private int disponibles = 1;

    @OneToMany(mappedBy = "livre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmpruntModel> emprunts = new ArrayList<>();

    @OneToMany(mappedBy = "livre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationModel> reservations = new ArrayList<>();

    // Constructeurs
    public LivreModel() {}

    public LivreModel(String isbn, String titre, String auteur) {
        this.isbn = isbn;
        this.titre = titre;
        this.auteur = auteur;
    }

    // Getters & Setters (tous générés)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public LocalDate getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDate datePublication) { this.datePublication = datePublication; }

    public int getNombreExemplaires() { return nombreExemplaires; }
    public void setNombreExemplaires(int nombreExemplaires) { this.nombreExemplaires = nombreExemplaires; }

    public int getDisponibles() { return disponibles; }
    public void setDisponibles(int disponibles) { this.disponibles = disponibles; }

    public List<EmpruntModel> getEmprunts() { return emprunts; }
    public void setEmprunts(List<EmpruntModel> emprunts) { this.emprunts = emprunts; }

    public List<ReservationModel> getReservations() { return reservations; }
    public void setReservations(List<ReservationModel> reservations) { this.reservations = reservations; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LivreModel livre = (LivreModel) o;
        return id != null && id.equals(livre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
