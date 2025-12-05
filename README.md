# DIGITALISE_Projet_Gestion_Bibliotheque_Universitaire
Un projet de teste Technique pour recrutement Dev


# 📚 Système de Gestion de Bibliothèque Universitaire

Application Spring Boot pour la gestion complète d'une bibliothèque universitaire avec système de notifications en temps réel et génération de rapports.

## 📋 Table des Matières

- [Fonctionnalités](#-fonctionnalités)
- [Technologies Utilisées](#-technologies-utilisées)
- [Prérequis](#-prérequis)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Démarrage de l'Application](#-démarrage-de-lapplication)
- [Accès à l'Application](#-accès-à-lapplication)
- [Documentation API (Swagger)](#-documentation-api-swagger)
- [Base de Données](#-base-de-données)
- [Tests](#-tests)
- [Structure du Projet](#-structure-du-projet)
- [Fonctionnalités Bonus](#-fonctionnalités-bonus)
- [Dépannage](#-dépannage)

---

## 🎯 Fonctionnalités

### Fonctionnalités Principales

- ✅ **Gestion des Utilisateurs**
  - Inscription et authentification (JWT)
  - Rôles : ETUDIANT, BIBLIOTHECAIRE, ADMIN
  - Profils utilisateurs avec matricule

- ✅ **Gestion des Livres**
  - CRUD complet des livres
  - Recherche et filtrage
  - Gestion du stock (disponibles/total)

- ✅ **Gestion des Emprunts**
  - Création d'emprunts avec règles métier
  - Retour de livres avec calcul de pénalités
  - Limite d'emprunts selon le rôle
  - Durée d'emprunt variable (7j étudiants, 14j enseignants)
  - Statuts : EN_COURS, TERMINE, EN_RETARD

- ✅ **Gestion des Réservations**
  - Réservation de livres non disponibles
  - Expiration automatique après 48h
  - Notification quand le livre devient disponible

### Fonctionnalités Bonus

- 📧 **Système de Notifications Email**
  - Notification de disponibilité de réservation
  - Rappel 24h avant la date de retour
  - Alerte pour emprunts en retard
  - Email de bienvenue

- 📊 **Export de Rapports**
  - PDF : Historique des emprunts (personnel et global)
  - Excel : Statistiques complètes de la bibliothèque
  - Excel : Liste des emprunts en retard

---

## 🛠️ Technologies Utilisées

### Backend
- **Java** 17+
- **Spring Boot** 3.2.0
  - Spring Data JPA
  - Spring Security
  - Spring Mail
  - Spring Validation
- **JWT** (JSON Web Tokens) pour l'authentification
- **MySQL** pour la base de données
- **H2** pour les tests

### Génération de Rapports
- **OpenPDF** 1.3.30 (génération PDF)
- **Apache POI** 5.2.5 (génération Excel)

### Documentation
- **SpringDoc OpenAPI** 2.3.0 (Swagger UI)

### Build & Outils
- **Maven** 3.x
- **Lombok** (optionnel, pour réduire le boilerplate)

---

## 📦 Prérequis

Avant de commencer, assurez-vous d'avoir installé :

### Obligatoire

1. **JDK 17 ou supérieur** (pas seulement JRE)
   ```bash
   java -version
   # Doit afficher : java version "17" ou supérieur
   ```
   
   📥 Télécharger : [OpenJDK](https://adoptium.net/) ou [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)

2. **MySQL 8.0+**
   ```bash
   mysql --version
   ```
   
   📥 Télécharger : [MySQL Community Server](https://dev.mysql.com/downloads/mysql/)

3. **Maven 3.6+** (ou utiliser le wrapper Maven inclus)
   ```bash
   mvn -version
   ```

### Optionnel

- **Git** pour cloner le projet
- **Postman** pour tester l'API
- **IDE** : IntelliJ IDEA, Eclipse, ou VS Code

---

## 🚀 Installation

### 1. Cloner le Projet

```bash
git clone https://github.com/votre-repo/ETI_Backend.git
cd ETI_Backend
```

### 2. Créer la Base de Données

Connectez-vous à MySQL et créez la base de données :

```sql
CREATE DATABASE gestionBiliotheque CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**Note :** Le nom de la base doit être exactement `gestionBiliotheque` ou modifiez `application.properties`.

### 3. Vérifier JAVA_HOME

**Windows PowerShell :**
```powershell
# Vérifier la variable
$env:JAVA_HOME

# Si elle pointe vers JRE, la corriger
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
# ou jdk-21, jdk-23, selon votre version
```

**Linux/Mac :**
```bash
# Vérifier
echo $JAVA_HOME

# Définir (ajouter dans ~/.bashrc ou ~/.zshrc)
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH
```

---

## ⚙️ Configuration

### 1. Configuration de la Base de Données

Éditez `src/main/resources/application.properties` :

```properties
# Configuration MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/gestionBiliotheque?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=votre_mot_de_passe_mysql
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### 2. Configuration Email (Optionnel)

Pour activer les notifications email, configurez SMTP :

```properties
# Configuration SMTP (exemple avec Gmail)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=votre-email@gmail.com
spring.mail.password=votre-mot-de-passe-app
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Paramètres de notification
notification.enabled=true
notification.from.email=noreply@bibliotheque.com
notification.from.name=Bibliothèque Universitaire
```

**Pour Gmail :**
1. Activez la validation en 2 étapes
2. Générez un mot de passe d'application : https://myaccount.google.com/apppasswords
3. Utilisez ce mot de passe dans `spring.mail.password`

**Pour désactiver les emails :**
```properties
notification.enabled=false
```

### 3. Configuration JWT

Le secret JWT est déjà configuré. Pour le modifier :

```properties
jwt.secret=votre_nouveau_secret_tres_long_et_securise
jwt.expiration=86400000  # 24 heures en millisecondes
```

### 4. Configuration du Port

Par défaut, l'application démarre sur le port **5005** :

```properties
server.port=5005
```

Pour changer le port :
```properties
server.port=8080
```

---

## 🏃 Démarrage de l'Application

### Méthode 1 : Avec Maven Wrapper (Recommandé)

**Windows :**
```powershell
# Compiler le projet
.\mvnw.cmd clean install -DskipTests

# Démarrer l'application
.\mvnw.cmd spring-boot:run
```

**Linux/Mac :**
```bash
# Compiler le projet
./mvnw clean install -DskipTests

# Démarrer l'application
./mvnw spring-boot:run
```

### Méthode 2 : Avec Maven Installé

```bash
# Compiler
mvn clean install -DskipTests

# Démarrer
mvn spring-boot:run
```

### Méthode 3 : Avec un IDE

#### IntelliJ IDEA
1. Ouvrir le projet
2. Attendre l'indexation Maven
3. Clic droit sur `GestionBiliothequeApplication.java`
4. Sélectionner "Run 'GestionBiliothequeApplication'"

#### Eclipse
1. Importer comme projet Maven
2. Clic droit sur le projet → Run As → Spring Boot App

#### VS Code
1. Installer l'extension "Spring Boot Extension Pack"
2. Ouvrir le projet
3. F5 ou cliquer sur "Run" dans `GestionBiliothequeApplication.java`

### Vérifier le Démarrage

L'application démarre correctement si vous voyez :

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

...
Started GestionBiliothequeApplication in X.XXX seconds
```

---

## 🌐 Accès à l'Application

### URL de Base

```
http://localhost:5005
```

### Endpoints Principaux

| Endpoint | Description |
|----------|-------------|
| `/api/auth/register` | Inscription |
| `/api/auth/login` | Connexion |
| `/api/livres` | Gestion des livres |
| `/api/emprunts` | Gestion des emprunts |
| `/api/reservations` | Gestion des réservations |
| `/api/reports` | Génération de rapports |

---

## 📖 Documentation API (Swagger)

### Accéder à Swagger UI

Une fois l'application démarrée, accédez à :

```
http://localhost:5005/swagger-ui.html
```

ou

```
http://localhost:5005/swagger-ui/index.html
```

### Utiliser Swagger

1. **Créer un compte :**
   - Allez sur `POST /api/auth/register`
   - Cliquez sur "Try it out"
   - Remplissez les informations :
     ```json
     {
       "matricule": "ETU001",
       "nom": "Dupont",
       "prenom": "Jean",
       "email": "jean.dupont@univ.fr",
       "password": "password123",
       "role": "ETUDIANT"
     }
     ```
   - Cliquez sur "Execute"

2. **Se connecter :**
   - Allez sur `POST /api/auth/login`
   - Entrez vos identifiants :
     ```json
     {
       "email": "jean.dupont@univ.fr",
       "password": "password123"
     }
     ```
   - Copiez le `token` de la réponse

3. **S'authentifier dans Swagger :**
   - Cliquez sur le bouton **"Authorize"** en haut à droite
   - Entrez : `Bearer votre_token_ici`
   - Cliquez sur "Authorize"
   - Fermez la fenêtre

4. **Tester les endpoints protégés :**
   - Tous les endpoints sont maintenant accessibles
   - Exemple : `GET /api/livres` pour lister les livres

### Documentation OpenAPI JSON

```
http://localhost:5005/v3/api-docs
```

---

## 🗄️ Base de Données

### Schéma de la Base

L'application crée automatiquement les tables au démarrage grâce à Hibernate.

**Tables principales :**
- `utilisateur` - Utilisateurs du système
- `livre` - Catalogue de livres
- `emprunt` - Historique des emprunts
- `reservation` - Réservations en cours

### Accéder à MySQL

```bash
mysql -u root -p
use gestionBiliotheque;

# Lister les tables
SHOW TABLES;

# Voir les utilisateurs
SELECT * FROM utilisateur;

# Voir les livres
SELECT * FROM livre;
```

### Données de Test

Pour insérer des données de test :

```sql
-- Créer un admin
INSERT INTO utilisateur (matricule, nom, prenom, email, password, role) 
VALUES ('ADM001', 'Admin', 'System', 'admin@univ.fr', 
        '$2a$10$...hash_bcrypt...', 'ADMIN');

-- Ajouter des livres
INSERT INTO livre (isbn, titre, auteur, editeur, annee_publication, total, disponibles) 
VALUES 
('978-2-1234-5678-9', 'Introduction à Java', 'Martin Dupont', 'Eyrolles', 2023, 5, 5),
('978-2-9876-5432-1', 'Spring Boot en Action', 'Sophie Bernard', 'Dunod', 2024, 3, 3);
```

**Note :** Le mot de passe doit être hashé avec BCrypt. Utilisez l'endpoint `/api/auth/register` pour créer des utilisateurs.

---

## 🧪 Tests

### Exécuter Tous les Tests

```bash
# Avec Maven Wrapper
.\mvnw.cmd test

# Avec Maven
mvn test
```

### Exécuter un Test Spécifique

```bash
mvn test -Dtest=EmpruntServiceTest
```

### Configuration des Tests

Les tests utilisent une base H2 en mémoire configurée dans :
```
src/test/resources/application-test.properties
```

### Coverage

Pour générer un rapport de couverture :

```bash
mvn clean test jacoco:report
```

Le rapport sera dans : `target/site/jacoco/index.html`

---

## 📁 Structure du Projet

```
ETI_Backend/
├── src/
│   ├── main/
│   │   ├── java/com/example/gestionBiliotheque/
│   │   │   ├── auth/                    # Authentification JWT
│   │   │   ├── config/                  # Configuration Spring
│   │   │   ├── emprunt/                 # Gestion des emprunts
│   │   │   ├── exception/               # Gestion des exceptions
│   │   │   ├── livres/                  # Gestion des livres
│   │   │   ├── notification/            # 📧 Notifications email
│   │   │   ├── reports/                 # 📊 Génération de rapports
│   │   │   ├── reservations/            # Gestion des réservations
│   │   │   ├── scheduled/               # Tâches planifiées
│   │   │   ├── security/                # Sécurité JWT
│   │   │   ├── utilisateurs/            # Gestion des utilisateurs
│   │   │   └── GestionBiliothequeApplication.java
│   │   └── resources/
│   │       └── application.properties   # Configuration
│   └── test/                            # Tests unitaires
├── pom.xml                              # Dépendances Maven
├── README.md                            # Ce fichier
└── DOCUMENTATION_BONUS.md               # Doc des fonctionnalités bonus
```

### Packages Principaux

| Package | Description |
|---------|-------------|
| `auth` | Authentification et autorisation (JWT) |
| `emprunt` | Logique métier des emprunts |
| `livres` | CRUD des livres |
| `notification` | 📧 Service d'envoi d'emails |
| `reports` | 📊 Génération PDF/Excel |
| `reservations` | Gestion des réservations |
| `scheduled` | Tâches cron (rappels, nettoyage) |
| `security` | Configuration Spring Security |
| `utilisateurs` | Gestion des utilisateurs |

---

## 🎁 Fonctionnalités Bonus

### 1. Notifications Email

**Activation :**
1. Configurer SMTP dans `application.properties`
2. Définir `notification.enabled=true`

**Types de notifications :**
- ✉️ Réservation disponible (quand un livre est retourné)
- ⏰ Rappel 24h avant retour (tâche quotidienne à 9h)
- ⚠️ Alerte emprunt en retard (tâche quotidienne à minuit)
- 🎉 Email de bienvenue (à l'inscription)

### 2. Export de Rapports

**Endpoints disponibles :**

```bash
# Historique personnel (PDF)
GET /api/reports/loans/history/pdf
Authorization: Bearer {token}

# Tous les emprunts - ADMIN (PDF)
GET /api/reports/loans/all/pdf
Authorization: Bearer {token}

# Statistiques (Excel)
GET /api/reports/statistics/excel
Authorization: Bearer {token}

# Emprunts en retard (Excel)
GET /api/reports/overdue/excel
Authorization: Bearer {token}
```

**Exemple avec cURL :**
```bash
curl -X GET "http://localhost:5005/api/reports/loans/history/pdf" \
  -H "Authorization: Bearer votre_token" \
  --output mon_historique.pdf
```

📖 **Documentation complète :** Voir [DOCUMENTATION_BONUS.md](DOCUMENTATION_BONUS.md)

---

## 🔧 Dépannage

### Problème : "No compiler is provided in this environment"

**Cause :** JAVA_HOME pointe vers JRE au lieu de JDK

**Solution :**
```powershell
# Windows
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

# Linux/Mac
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
```

### Problème : "Port 5005 already in use"

**Solution 1 - Arrêter le processus :**
```powershell
# Windows
netstat -ano | findstr :5005
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :5005
kill -9 <PID>
```

**Solution 2 - Changer le port :**
```properties
# Dans application.properties
server.port=8080
```

### Problème : "Access denied for user 'root'@'localhost'"

**Solution :**
1. Vérifier le mot de passe MySQL dans `application.properties`
2. Vérifier que MySQL est démarré
3. Tester la connexion :
   ```bash
   mysql -u root -p
   ```

### Problème : "Table 'gestionBiliotheque.utilisateur' doesn't exist"

**Solution :**
1. Vérifier que `spring.jpa.hibernate.ddl-auto=update`
2. Supprimer et recréer la base :
   ```sql
   DROP DATABASE gestionBiliotheque;
   CREATE DATABASE gestionBiliotheque;
   ```
3. Redémarrer l'application

### Problème : Emails non envoyés

**Vérifications :**
1. `notification.enabled=true`
2. Identifiants SMTP corrects
3. Pour Gmail : mot de passe d'application activé
4. Vérifier les logs : `logging.level.com.example.gestionBiliotheque.notification=DEBUG`

### Problème : Swagger ne s'affiche pas

**Solutions :**
1. Vérifier l'URL : `http://localhost:5005/swagger-ui.html`
2. Vider le cache du navigateur
3. Vérifier que SpringDoc est dans les dépendances
4. Redémarrer l'application

---

## 📚 Ressources Utiles

### Documentation Officielle
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [SpringDoc OpenAPI](https://springdoc.org/)

### Tutoriels
- [JWT avec Spring Boot](https://www.bezkoder.com/spring-boot-jwt-authentication/)
- [Spring Mail](https://www.baeldung.com/spring-email)
- [OpenPDF](https://github.com/LibrePDF/OpenPDF)
- [Apache POI](https://poi.apache.org/)

---

## 👥 Contribution

Pour contribuer au projet :

1. Fork le projet
2. Créer une branche (`git checkout -b feature/nouvelle-fonctionnalite`)
3. Commit les changements (`git commit -m 'Ajout nouvelle fonctionnalité'`)
4. Push vers la branche (`git push origin feature/nouvelle-fonctionnalite`)
5. Ouvrir une Pull Request

---

## 📄 Licence

Ce projet est développé dans le cadre d'un projet universitaire.

---

## 📞 Support

Pour toute question ou problème :

1. Consulter la section [Dépannage](#-dépannage)
2. Vérifier les logs de l'application
3. Consulter la [documentation bonus](DOCUMENTATION_BONUS.md)

---

## ✨ Auteurs

- **Développement initial** : Équipe ETI
- **Fonctionnalités bonus** : Notifications email et exports de rapports

---

## 🎓 Remerciements

Merci aux technologies open-source utilisées dans ce projet :
- Spring Framework
- MySQL
- OpenPDF
- Apache POI

---

**Version :** 1.0.0  
**Dernière mise à jour :** Décembre 2025

**Bon développement ! 🚀**
