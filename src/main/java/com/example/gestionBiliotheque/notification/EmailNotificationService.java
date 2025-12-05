package com.example.gestionBiliotheque.notification;

import com.example.gestionBiliotheque.emprunt.EmpruntModel;
import com.example.gestionBiliotheque.reservations.ReservationModel;
import com.example.gestionBiliotheque.utilisateurs.UtilisateurModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

/**
 * Implémentation du service de notification par email
 */
@Service
public class EmailNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    @Autowired
    private JavaMailSender mailSender;

    @Value("${notification.enabled:true}")
    private boolean notificationEnabled;

    @Value("${notification.from.email:noreply@bibliotheque.com}")
    private String fromEmail;

    @Value("${notification.from.name:Bibliothèque Universitaire}")
    private String fromName;

    @Override
    public void sendReservationAvailableNotification(ReservationModel reservation) {
        if (!notificationEnabled) {
            logger.debug("Notifications désactivées - Email non envoyé");
            return;
        }

        try {
            String to = reservation.getUtilisateur().getEmail();
            String subject = "📚 Votre livre réservé est maintenant disponible !";
            String content = buildReservationAvailableEmail(reservation);

            sendHtmlEmail(to, subject, content);
            logger.info("Notification de disponibilité envoyée à: {}", to);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification de disponibilité", e);
        }
    }

    @Override
    public void sendReturnReminderNotification(EmpruntModel emprunt) {
        if (!notificationEnabled) {
            logger.debug("Notifications désactivées - Email non envoyé");
            return;
        }

        try {
            String to = emprunt.getUtilisateur().getEmail();
            String subject = "⏰ Rappel : Retour de livre dans 24 heures";
            String content = buildReturnReminderEmail(emprunt);

            sendHtmlEmail(to, subject, content);
            logger.info("Rappel de retour envoyé à: {}", to);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi du rappel de retour", e);
        }
    }

    @Override
    public void sendWelcomeNotification(UtilisateurModel utilisateur) {
        if (!notificationEnabled) {
            logger.debug("Notifications désactivées - Email non envoyé");
            return;
        }

        try {
            String to = utilisateur.getEmail();
            String subject = "🎉 Bienvenue à la Bibliothèque Universitaire";
            String content = buildWelcomeEmail(utilisateur);

            sendHtmlEmail(to, subject, content);
            logger.info("Email de bienvenue envoyé à: {}", to);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email de bienvenue", e);
        }
    }

    @Override
    public void sendOverdueNotification(EmpruntModel emprunt) {
        if (!notificationEnabled) {
            logger.debug("Notifications désactivées - Email non envoyé");
            return;
        }

        try {
            String to = emprunt.getUtilisateur().getEmail();
            String subject = "⚠️ Livre en retard - Action requise";
            String content = buildOverdueEmail(emprunt);

            sendHtmlEmail(to, subject, content);
            logger.info("Notification de retard envoyée à: {}", to);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification de retard", e);
        }
    }

    /**
     * Envoie un email HTML
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException, MailException, java.io.UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail, fromName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    /**
     * Construit l'email de disponibilité de réservation
     */
    private String buildReservationAvailableEmail(ReservationModel reservation) {
        String userName = reservation.getUtilisateur().getPrenom() + " " + reservation.getUtilisateur().getNom();
        String bookTitle = reservation.getLivre().getTitre();
        String bookAuthor = reservation.getLivre().getAuteur();

        return buildEmailTemplate(
                "Livre Disponible",
                "Bonjour " + userName + ",",
                "Bonne nouvelle ! Le livre que vous avez réservé est maintenant disponible :",
                "<div style='background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
                        "<strong style='color: #2c3e50; font-size: 16px;'>" + bookTitle + "</strong><br>" +
                        "<span style='color: #7f8c8d;'>par " + bookAuthor + "</span>" +
                        "</div>",
                "Veuillez venir le récupérer dans les 48 heures, sinon votre réservation sera annulée.",
                "#28a745"
        );
    }

    /**
     * Construit l'email de rappel de retour
     */
    private String buildReturnReminderEmail(EmpruntModel emprunt) {
        String userName = emprunt.getUtilisateur().getPrenom() + " " + emprunt.getUtilisateur().getNom();
        String bookTitle = emprunt.getLivre().getTitre();
        String returnDate = emprunt.getDateRetourPrevue().format(DATE_FORMATTER);

        return buildEmailTemplate(
                "Rappel de Retour",
                "Bonjour " + userName + ",",
                "Ceci est un rappel amical concernant le retour de votre livre :",
                "<div style='background-color: #fff3cd; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
                        "<strong style='color: #856404; font-size: 16px;'>" + bookTitle + "</strong><br>" +
                        "<span style='color: #856404;'>Date de retour prévue : " + returnDate + "</span>" +
                        "</div>",
                "Merci de retourner ce livre avant la date limite pour éviter des pénalités.",
                "#ffc107"
        );
    }

    /**
     * Construit l'email de bienvenue
     */
    private String buildWelcomeEmail(UtilisateurModel utilisateur) {
        String userName = utilisateur.getPrenom() + " " + utilisateur.getNom();

        return buildEmailTemplate(
                "Bienvenue",
                "Bonjour " + userName + ",",
                "Bienvenue à la Bibliothèque Universitaire !",
                "<div style='background-color: #e3f2fd; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
                        "<p style='color: #1976d2; margin: 5px 0;'>✓ Votre compte a été créé avec succès</p>" +
                        "<p style='color: #1976d2; margin: 5px 0;'>✓ Vous pouvez maintenant emprunter des livres</p>" +
                        "<p style='color: #1976d2; margin: 5px 0;'>✓ Matricule : " + utilisateur.getMatricule() + "</p>" +
                        "</div>",
                "Nous vous souhaitons une excellente expérience de lecture !",
                "#2196f3"
        );
    }

    /**
     * Construit l'email de retard
     */
    private String buildOverdueEmail(EmpruntModel emprunt) {
        String userName = emprunt.getUtilisateur().getPrenom() + " " + emprunt.getUtilisateur().getNom();
        String bookTitle = emprunt.getLivre().getTitre();
        String returnDate = emprunt.getDateRetourPrevue().format(DATE_FORMATTER);

        return buildEmailTemplate(
                "Livre en Retard",
                "Bonjour " + userName + ",",
                "Votre emprunt est maintenant en retard :",
                "<div style='background-color: #f8d7da; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
                        "<strong style='color: #721c24; font-size: 16px;'>" + bookTitle + "</strong><br>" +
                        "<span style='color: #721c24;'>Date de retour prévue : " + returnDate + "</span>" +
                        "</div>",
                "Veuillez retourner ce livre dès que possible. Des pénalités peuvent s'appliquer.",
                "#dc3545"
        );
    }

    /**
     * Template HTML générique pour les emails
     */
    private String buildEmailTemplate(String title, String greeting, String message, String content, String footer, String accentColor) {
        return "<!DOCTYPE html>" +
                "<html lang='fr'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "</head>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<div style='background: linear-gradient(135deg, " + accentColor + " 0%, " + adjustColor(accentColor) + " 100%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;'>" +
                "<h1 style='color: white; margin: 0; font-size: 24px;'>" + title + "</h1>" +
                "</div>" +
                "<div style='background-color: white; padding: 30px; border: 1px solid #e0e0e0; border-top: none; border-radius: 0 0 10px 10px;'>" +
                "<p style='font-size: 16px; margin-bottom: 10px;'>" + greeting + "</p>" +
                "<p style='font-size: 14px; color: #555;'>" + message + "</p>" +
                content +
                "<p style='font-size: 14px; color: #555; margin-top: 20px;'>" + footer + "</p>" +
                "<hr style='border: none; border-top: 1px solid #e0e0e0; margin: 30px 0;'>" +
                "<p style='font-size: 12px; color: #999; text-align: center;'>" +
                "Ceci est un email automatique, merci de ne pas y répondre.<br>" +
                "© " + java.time.Year.now().getValue() + " Bibliothèque Universitaire" +
                "</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Ajuste la couleur pour le dégradé
     */
    private String adjustColor(String color) {
        // Simple darkening effect for gradient
        return color.replace("a745", "8c3a")
                .replace("c107", "a106")
                .replace("96f3", "64b5")
                .replace("3545", "1f2d");
    }
}
