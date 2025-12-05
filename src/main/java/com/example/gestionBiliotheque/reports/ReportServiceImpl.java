package com.example.gestionBiliotheque.reports;

import com.example.gestionBiliotheque.emprunt.EmpruntModel;
import com.example.gestionBiliotheque.emprunt.EmpruntRepository;
import com.example.gestionBiliotheque.livres.LivreModel;
import com.example.gestionBiliotheque.livres.LivreRepository;
import com.example.gestionBiliotheque.utilisateurs.UtilisateurModel;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implémentation du service de génération de rapports
 */
@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    private EmpruntRepository empruntRepository;

    @Autowired
    private LivreRepository livreRepository;

    @Override
    public Resource generateLoanHistoryPdf(UtilisateurModel utilisateur, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            logger.info("Génération du rapport PDF pour l'utilisateur: {}", utilisateur.getEmail());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);

            document.open();

            // En-tête
            addPdfHeader(document, "Historique des Emprunts");
            
            // Informations utilisateur
            addUserInfo(document, utilisateur);
            
            // Période
            if (startDate != null || endDate != null) {
                Paragraph period = new Paragraph("Période: " + 
                    (startDate != null ? startDate.format(DATE_FORMATTER) : "Début") + 
                    " - " + 
                    (endDate != null ? endDate.format(DATE_FORMATTER) : "Aujourd'hui"),
                    FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC));
                period.setSpacingAfter(10);
                document.add(period);
            }

            // Récupérer les emprunts
            List<EmpruntModel> emprunts = empruntRepository.findByUtilisateur(utilisateur, org.springframework.data.domain.Pageable.unpaged()).getContent();
            
            // Filtrer par date si nécessaire
            if (startDate != null || endDate != null) {
                emprunts = emprunts.stream()
                    .filter(e -> (startDate == null || e.getDateEmprunt().isAfter(startDate)) &&
                                 (endDate == null || e.getDateEmprunt().isBefore(endDate)))
                    .toList();
            }

            // Tableau des emprunts
            addLoansTable(document, emprunts);

            // Statistiques
            addLoanStatistics(document, emprunts);

            document.close();

            logger.info("Rapport PDF généré avec succès: {} emprunts", emprunts.size());
            return new ByteArrayResource(baos.toByteArray());

        } catch (Exception e) {
            logger.error("Erreur lors de la génération du rapport PDF", e);
            throw new RuntimeException("Erreur lors de la génération du rapport PDF", e);
        }
    }

    @Override
    public Resource generateAllLoansPdf(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            logger.info("Génération du rapport PDF de tous les emprunts");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate()); // Paysage pour plus de colonnes
            PdfWriter.getInstance(document, baos);

            document.open();

            // En-tête
            addPdfHeader(document, "Rapport Global des Emprunts");
            
            // Période
            if (startDate != null || endDate != null) {
                Paragraph period = new Paragraph("Période: " + 
                    (startDate != null ? startDate.format(DATE_FORMATTER) : "Début") + 
                    " - " + 
                    (endDate != null ? endDate.format(DATE_FORMATTER) : "Aujourd'hui"),
                    FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC));
                period.setSpacingAfter(10);
                document.add(period);
            }

            // Récupérer tous les emprunts
            List<EmpruntModel> emprunts = empruntRepository.findAll();
            
            // Filtrer par date si nécessaire
            if (startDate != null || endDate != null) {
                emprunts = emprunts.stream()
                    .filter(e -> (startDate == null || e.getDateEmprunt().isAfter(startDate)) &&
                                 (endDate == null || e.getDateEmprunt().isBefore(endDate)))
                    .toList();
            }

            // Tableau des emprunts avec utilisateur
            addAllLoansTable(document, emprunts);

            document.close();

            logger.info("Rapport PDF global généré avec succès: {} emprunts", emprunts.size());
            return new ByteArrayResource(baos.toByteArray());

        } catch (Exception e) {
            logger.error("Erreur lors de la génération du rapport PDF global", e);
            throw new RuntimeException("Erreur lors de la génération du rapport PDF global", e);
        }
    }

    @Override
    public Resource generateStatisticsExcel() {
        try {
            logger.info("Génération du rapport Excel des statistiques");

            Workbook workbook = new XSSFWorkbook();

            // Feuille 1: Statistiques générales
            createGeneralStatisticsSheet(workbook);

            // Feuille 2: Top livres empruntés
            createTopBooksSheet(workbook);

            // Feuille 3: Statistiques par statut
            createStatusStatisticsSheet(workbook);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            workbook.close();

            logger.info("Rapport Excel des statistiques généré avec succès");
            return new ByteArrayResource(baos.toByteArray());

        } catch (Exception e) {
            logger.error("Erreur lors de la génération du rapport Excel", e);
            throw new RuntimeException("Erreur lors de la génération du rapport Excel", e);
        }
    }

    @Override
    public Resource generateOverdueLoansExcel() {
        try {
            logger.info("Génération du rapport Excel des emprunts en retard");

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Emprunts en Retard");

            // Styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            // En-tête
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Utilisateur", "Matricule", "Livre", "ISBN", "Date Emprunt", "Date Retour Prévue", "Jours de Retard", "Pénalité"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Récupérer les emprunts en retard
            List<EmpruntModel> overdueLoans = empruntRepository.findOverdueEmprunts(
                LocalDateTime.now(), 
                org.springframework.data.domain.Pageable.unpaged()
            ).getContent();

            // Données
            int rowNum = 1;
            for (EmpruntModel emprunt : overdueLoans) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(emprunt.getUtilisateur().getNom() + " " + emprunt.getUtilisateur().getPrenom());
                row.createCell(1).setCellValue(emprunt.getUtilisateur().getMatricule());
                row.createCell(2).setCellValue(emprunt.getLivre().getTitre());
                row.createCell(3).setCellValue(emprunt.getLivre().getIsbn());
                
                Cell dateEmpruntCell = row.createCell(4);
                dateEmpruntCell.setCellValue(emprunt.getDateEmprunt().format(DATE_FORMATTER));
                dateEmpruntCell.setCellStyle(dateStyle);
                
                Cell dateRetourCell = row.createCell(5);
                dateRetourCell.setCellValue(emprunt.getDateRetourPrevue().format(DATE_FORMATTER));
                dateRetourCell.setCellStyle(dateStyle);
                
                long daysLate = java.time.temporal.ChronoUnit.DAYS.between(emprunt.getDateRetourPrevue(), LocalDateTime.now());
                row.createCell(6).setCellValue(daysLate);
                row.createCell(7).setCellValue(emprunt.getPenalite().doubleValue());
            }

            // Auto-size colonnes
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            workbook.close();

            logger.info("Rapport Excel des emprunts en retard généré: {} emprunts", overdueLoans.size());
            return new ByteArrayResource(baos.toByteArray());

        } catch (Exception e) {
            logger.error("Erreur lors de la génération du rapport Excel des retards", e);
            throw new RuntimeException("Erreur lors de la génération du rapport Excel des retards", e);
        }
    }

    // Méthodes utilitaires pour PDF

    private void addPdfHeader(Document document, String title) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(41, 128, 185));
        Paragraph titlePara = new Paragraph(title, titleFont);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        titlePara.setSpacingAfter(20);
        document.add(titlePara);

        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, Color.GRAY);
        Paragraph subtitle = new Paragraph("Généré le " + LocalDateTime.now().format(DATE_FORMATTER), subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);
    }

    private void addUserInfo(Document document, UtilisateurModel utilisateur) throws DocumentException {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        Paragraph userInfo = new Paragraph();
        userInfo.add(new Chunk("Utilisateur: ", labelFont));
        userInfo.add(new Chunk(utilisateur.getPrenom() + " " + utilisateur.getNom() + "\n", valueFont));
        userInfo.add(new Chunk("Matricule: ", labelFont));
        userInfo.add(new Chunk(utilisateur.getMatricule() + "\n", valueFont));
        userInfo.add(new Chunk("Email: ", labelFont));
        userInfo.add(new Chunk(utilisateur.getEmail(), valueFont));
        userInfo.setSpacingAfter(15);
        document.add(userInfo);
    }

    private void addLoansTable(Document document, List<EmpruntModel> emprunts) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        // En-tête
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        Color headerColor = new Color(52, 73, 94);
        
        addTableHeader(table, "Livre", headerFont, headerColor);
        addTableHeader(table, "Date Emprunt", headerFont, headerColor);
        addTableHeader(table, "Date Retour Prévue", headerFont, headerColor);
        addTableHeader(table, "Statut", headerFont, headerColor);
        addTableHeader(table, "Pénalité", headerFont, headerColor);

        // Données
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        for (EmpruntModel emprunt : emprunts) {
            table.addCell(new PdfPCell(new Phrase(emprunt.getLivre().getTitre(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(emprunt.getDateEmprunt().format(DATE_FORMATTER), cellFont)));
            table.addCell(new PdfPCell(new Phrase(emprunt.getDateRetourPrevue().format(DATE_FORMATTER), cellFont)));
            table.addCell(new PdfPCell(new Phrase(emprunt.getStatut().toString(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(emprunt.getPenalite().toString() + " €", cellFont)));
        }

        document.add(table);
    }

    private void addAllLoansTable(Document document, List<EmpruntModel> emprunts) throws DocumentException {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        // En-tête
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Color headerColor = new Color(52, 73, 94);
        
        addTableHeader(table, "Utilisateur", headerFont, headerColor);
        addTableHeader(table, "Livre", headerFont, headerColor);
        addTableHeader(table, "Date Emprunt", headerFont, headerColor);
        addTableHeader(table, "Date Retour", headerFont, headerColor);
        addTableHeader(table, "Statut", headerFont, headerColor);
        addTableHeader(table, "Pénalité", headerFont, headerColor);

        // Données
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        for (EmpruntModel emprunt : emprunts) {
            table.addCell(new PdfPCell(new Phrase(emprunt.getUtilisateur().getNom() + " " + emprunt.getUtilisateur().getPrenom(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(emprunt.getLivre().getTitre(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(emprunt.getDateEmprunt().format(DATE_FORMATTER), cellFont)));
            table.addCell(new PdfPCell(new Phrase(emprunt.getDateRetourPrevue().format(DATE_FORMATTER), cellFont)));
            table.addCell(new PdfPCell(new Phrase(emprunt.getStatut().toString(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(emprunt.getPenalite().toString() + " €", cellFont)));
        }

        document.add(table);
    }

    private void addTableHeader(PdfPTable table, String text, Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addLoanStatistics(Document document, List<EmpruntModel> emprunts) throws DocumentException {
        Paragraph statsTitle = new Paragraph("Statistiques", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
        statsTitle.setSpacingBefore(15);
        statsTitle.setSpacingAfter(10);
        document.add(statsTitle);

        long totalLoans = emprunts.size();
        long activeLoans = emprunts.stream().filter(e -> e.getStatut() == com.example.gestionBiliotheque.emprunt.StatutEmprunt.EN_COURS).count();
        long overdueLoans = emprunts.stream().filter(e -> e.getStatut() == com.example.gestionBiliotheque.emprunt.StatutEmprunt.EN_RETARD).count();
        double totalPenalties = emprunts.stream().mapToDouble(e -> e.getPenalite().doubleValue()).sum();

        Font statFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Paragraph stats = new Paragraph();
        stats.add(new Chunk("Total des emprunts: " + totalLoans + "\n", statFont));
        stats.add(new Chunk("Emprunts en cours: " + activeLoans + "\n", statFont));
        stats.add(new Chunk("Emprunts en retard: " + overdueLoans + "\n", statFont));
        stats.add(new Chunk("Total des pénalités: " + String.format("%.2f", totalPenalties) + " €", statFont));
        document.add(stats);
    }

    // Méthodes utilitaires pour Excel

    private void createGeneralStatisticsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Statistiques Générales");
        CellStyle headerStyle = createHeaderStyle(workbook);

        int rowNum = 0;
        
        // Titre
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Statistiques de la Bibliothèque");
        titleCell.setCellStyle(createTitleStyle(workbook));
        rowNum++; // Ligne vide

        // Statistiques des emprunts
        List<EmpruntModel> allLoans = empruntRepository.findAll();
        long totalLoans = allLoans.size();
        long activeLoans = allLoans.stream().filter(e -> e.getStatut() == com.example.gestionBiliotheque.emprunt.StatutEmprunt.EN_COURS).count();
        long completedLoans = allLoans.stream().filter(e -> e.getStatut() == com.example.gestionBiliotheque.emprunt.StatutEmprunt.TERMINE).count();
        long overdueLoans = allLoans.stream().filter(e -> e.getStatut() == com.example.gestionBiliotheque.emprunt.StatutEmprunt.EN_RETARD).count();
        double totalPenalties = allLoans.stream().mapToDouble(e -> e.getPenalite().doubleValue()).sum();

        addStatRow(sheet, rowNum++, "Total des emprunts", totalLoans, headerStyle);
        addStatRow(sheet, rowNum++, "Emprunts en cours", activeLoans, headerStyle);
        addStatRow(sheet, rowNum++, "Emprunts terminés", completedLoans, headerStyle);
        addStatRow(sheet, rowNum++, "Emprunts en retard", overdueLoans, headerStyle);
        addStatRow(sheet, rowNum++, "Total des pénalités (€)", totalPenalties, headerStyle);
        rowNum++; // Ligne vide

        // Statistiques des livres
        List<LivreModel> allBooks = livreRepository.findAll();
        long totalBooks = allBooks.size();
        long availableBooks = allBooks.stream().filter(l -> l.getDisponibles() > 0).count();
        long unavailableBooks = totalBooks - availableBooks;

        addStatRow(sheet, rowNum++, "Total de livres", totalBooks, headerStyle);
        addStatRow(sheet, rowNum++, "Livres disponibles", availableBooks, headerStyle);
        addStatRow(sheet, rowNum++, "Livres non disponibles", unavailableBooks, headerStyle);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createTopBooksSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Top Livres");
        CellStyle headerStyle = createHeaderStyle(workbook);

        // En-tête
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Titre", "Auteur", "ISBN", "Nombre d'emprunts"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Compter les emprunts par livre
        List<EmpruntModel> allLoans = empruntRepository.findAll();
        java.util.Map<LivreModel, Long> bookCounts = allLoans.stream()
            .collect(java.util.stream.Collectors.groupingBy(EmpruntModel::getLivre, java.util.stream.Collectors.counting()));

        // Trier par nombre d'emprunts
        List<java.util.Map.Entry<LivreModel, Long>> sortedBooks = bookCounts.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(20) // Top 20
            .toList();

        // Données
        int rowNum = 1;
        for (java.util.Map.Entry<LivreModel, Long> entry : sortedBooks) {
            Row row = sheet.createRow(rowNum++);
            LivreModel livre = entry.getKey();
            row.createCell(0).setCellValue(livre.getTitre());
            row.createCell(1).setCellValue(livre.getAuteur());
            row.createCell(2).setCellValue(livre.getIsbn());
            row.createCell(3).setCellValue(entry.getValue());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createStatusStatisticsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Statistiques par Statut");
        CellStyle headerStyle = createHeaderStyle(workbook);

        // En-tête
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Statut", "Nombre", "Pourcentage"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Compter par statut
        List<EmpruntModel> allLoans = empruntRepository.findAll();
        long total = allLoans.size();
        
        java.util.Map<com.example.gestionBiliotheque.emprunt.StatutEmprunt, Long> statusCounts = allLoans.stream()
            .collect(java.util.stream.Collectors.groupingBy(EmpruntModel::getStatut, java.util.stream.Collectors.counting()));

        // Données
        int rowNum = 1;
        for (java.util.Map.Entry<com.example.gestionBiliotheque.emprunt.StatutEmprunt, Long> entry : statusCounts.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey().toString());
            row.createCell(1).setCellValue(entry.getValue());
            row.createCell(2).setCellValue(String.format("%.2f%%", (entry.getValue() * 100.0 / total)));
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void addStatRow(Sheet sheet, int rowNum, String label, Number value, CellStyle headerStyle) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(headerStyle);
        
        Cell valueCell = row.createCell(1);
        if (value instanceof Double || value instanceof Float) {
            valueCell.setCellValue(String.format("%.2f", value.doubleValue()));
        } else {
            valueCell.setCellValue(value.longValue());
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
        return style;
    }
}
