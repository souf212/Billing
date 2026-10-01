# Billing — Gestion de facturation

Application pédagogique de facturation multi-tenant : chaque administrateur accède uniquement à ses clients et à leurs factures.

## Technologies

- **Backend :** Java 21, Spring Boot, Spring Security JWT, Spring Data JPA, H2.
- **Frontend :** Angular, TypeScript.

## Fonctionnalités

- Connexion administrateur avec JWT.
- Liste et ajout de clients.
- Consultation et ajout de factures.
- Isolation des données entre administrateurs.

## Démarrage

Prérequis : **JDK 21**, **Node.js 24**, **npm** et **Git**. Configurer `JAVA_HOME` vers le dossier du JDK.

```bash
git clone https://github.com/souf212/Billing.git
cd Billing
```

Dans un terminal PowerShell, démarrer le backend :

```powershell
cd facture-app-backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
```

Dans un deuxième terminal, depuis la racine du projet, démarrer le frontend :

```powershell
cd facture-app-frontend
npm.cmd ci
npm.cmd start
```

Ouvrir **http://localhost:4200**. L’API fonctionne sur **http://localhost:8080**.

## Comptes de démonstration

| Email | Mot de passe |
| --- | --- |
| alice@example.com | Alice-demo-2026! |
| bob@example.com | Bob-demo-2026! |

Le profil `demo` est réservé au développement local. Les données H2 sont réinitialisées au redémarrage du backend. Un rechargement de la page nécessite une nouvelle connexion.

## Structure

```text
Billing/
├── facture-app-backend/    # API REST et sécurité
├── facture-app-frontend/   # Interface Angular
└── README.md
```

## Tests

Backend, depuis `facture-app-backend` :

```powershell
.\mvnw.cmd test
```

Frontend, depuis `facture-app-frontend` :

```powershell
npm.cmd test -- --watch=false
npm.cmd run build
```
