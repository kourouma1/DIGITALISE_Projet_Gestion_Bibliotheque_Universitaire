package com.example.gestionBiliotheque.reports;

import com.example.gestionBiliotheque.utilisateurs.UtilisateurModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Contrôleur pour la génération de rapports
 */
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "API de génération de rapports")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * Télécharge l'historique des emprunts de l'utilisateur connecté en PDF
     */
    @GetMapping("/loans/history/pdf")
    @Operation(summary = "Télécharger l'historique des emprunts en PDF")
    public ResponseEntity<Resource> downloadLoanHistoryPdf(
            @AuthenticationPrincipal UtilisateurModel currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Resource resource = reportService.generateLoanHistoryPdf(currentUser, startDate, endDate);

        String filename = "historique_emprunts_" + currentUser.getMatricule() + "_" + 
                         LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    /**
     * Télécharge l'historique de tous les emprunts en PDF (ADMIN uniquement)
     */
    @GetMapping("/loans/all/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Télécharger tous les emprunts en PDF (ADMIN)")
    public ResponseEntity<Resource> downloadAllLoansPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Resource resource = reportService.generateAllLoansPdf(startDate, endDate);

        String filename = "rapport_emprunts_" + 
                         LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    /**
     * Télécharge les statistiques de la bibliothèque en Excel (ADMIN/BIBLIOTHECAIRE)
     */
    @GetMapping("/statistics/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTHECAIRE')")
    @Operation(summary = "Télécharger les statistiques en Excel")
    public ResponseEntity<Resource> downloadStatisticsExcel() {

        Resource resource = reportService.generateStatisticsExcel();

        String filename = "statistiques_bibliotheque_" + 
                         LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    /**
     * Télécharge les emprunts en retard en Excel (ADMIN/BIBLIOTHECAIRE)
     */
    @GetMapping("/overdue/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTHECAIRE')")
    @Operation(summary = "Télécharger les emprunts en retard en Excel")
    public ResponseEntity<Resource> downloadOverdueLoansExcel() {

        Resource resource = reportService.generateOverdueLoansExcel();

        String filename = "emprunts_retard_" + 
                         LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}
