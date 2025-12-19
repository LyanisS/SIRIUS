CREATE DATABASE pcc;

-- drop all tables
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

-- give access to postgres
GRANT ALL ON SCHEMA public TO test;
GRANT ALL ON SCHEMA public TO public;

-- create tables
-- Table Jour
CREATE TABLE IF NOT EXISTS Jour (
    ID SERIAL PRIMARY KEY,
    nom VARCHAR(20) NOT NULL UNIQUE
);

-- Table Ligne
CREATE TABLE IF NOT EXISTS Ligne (
    ID SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL
);

-- Table Frequence
CREATE TABLE IF NOT EXISTS Frequence (
    ID SERIAL PRIMARY KEY,
    recurrence INT NOT NULL,
    dateDebut DATE NOT NULL,
    dateFin DATE NOT NULL,
    heureDebut TIME NOT NULL,
    heureFin TIME NOT NULL,
    sens BOOLEAN NOT NULL,
    ligneID INT NOT NULL,
    FOREIGN KEY (ligneID) REFERENCES Ligne(ID) ON DELETE CASCADE
);

-- Table association Frequence - Jour (s'applique)
CREATE TABLE IF NOT EXISTS Frequence_Jour (
    frequenceID INT NOT NULL,
    jourID INT NOT NULL,
    PRIMARY KEY (frequenceID, jourID),
    FOREIGN KEY (frequenceID) REFERENCES Frequence(ID) ON DELETE CASCADE,
    FOREIGN KEY (jourID) REFERENCES Jour(ID) ON DELETE CASCADE
);

-- Table Station
CREATE TABLE IF NOT EXISTS Station (
    ID SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL
);

-- Table LigneStation
CREATE TABLE IF NOT EXISTS LigneStation (
    ID SERIAL PRIMARY KEY,
    ordre INT NOT NULL,
    ligneID INT NOT NULL,
    stationID INT NOT NULL,
    FOREIGN KEY (ligneID) REFERENCES Ligne(ID) ON DELETE CASCADE,
    FOREIGN KEY (stationID) REFERENCES Station(ID) ON DELETE CASCADE,
     UNIQUE (ligneID, ordre),
     UNIQUE (ligneID, stationID)
);

-- Table Trajet
CREATE TABLE IF NOT EXISTS Trajet (
    ID SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    ligneID INT NOT NULL,
    FOREIGN KEY (ligneID) REFERENCES Ligne(ID) ON DELETE CASCADE
);

-- Table Horaire
CREATE TABLE IF NOT EXISTS Horaire (
    ID SERIAL PRIMARY KEY,
    dateArriveeTheorique TIMESTAMP NOT NULL,
    dateArriveeReelle TIMESTAMP,
    dateDepartTheorique TIMESTAMP NOT NULL,
    dateDepartReelle TIMESTAMP,
    trajetID INT NOT NULL,
    ligneStationID INT NOT NULL,
    FOREIGN KEY (trajetID) REFERENCES Trajet(ID) ON DELETE CASCADE,
    FOREIGN KEY (ligneStationID) REFERENCES LigneStation(ID) ON DELETE CASCADE
);

-- Table Train
CREATE TABLE IF NOT EXISTS Train (
    ID SERIAL PRIMARY KEY,
    vitesse FLOAT NOT NULL,
    dateArriveePosition TIMESTAMP NOT NULL
);

-- Table ElementVoie
CREATE TABLE IF NOT EXISTS ElementVoie (
    ID SERIAL PRIMARY KEY,
    longueur INT NOT NULL,
    elementSuivantID INT,
    FOREIGN KEY (elementSuivantID) REFERENCES ElementVoie(ID) ON DELETE SET NULL
);

-- Table association Train - ElementVoie (est positionné sur)
CREATE TABLE IF NOT EXISTS Train_ElementVoie (
    trainID INT NOT NULL,
    elementVoieID INT NOT NULL,
    PRIMARY KEY (trainID, elementVoieID),
    FOREIGN KEY (trainID) REFERENCES Train(ID) ON DELETE CASCADE,
     FOREIGN KEY (elementVoieID) REFERENCES ElementVoie(ID) ON DELETE CASCADE
);

-- Table Incident
CREATE TABLE IF NOT EXISTS Incident (
    ID SERIAL PRIMARY KEY,
    message TEXT NOT NULL,
    dateDebut TIMESTAMP NOT NULL,
    dateFin TIMESTAMP,
    trainID INT NOT NULL,
    FOREIGN KEY (trainID) REFERENCES Train(ID) ON DELETE CASCADE
);

-- Table association Trajet - Train (fait partie d'un)
CREATE TABLE IF NOT EXISTS Trajet_Train (
    trajetID INT NOT NULL,
    trainID INT NOT NULL,
    PRIMARY KEY (trajetID, trainID),
    FOREIGN KEY (trajetID) REFERENCES Trajet(ID) ON DELETE CASCADE,
     FOREIGN KEY (trainID) REFERENCES Train(ID) ON DELETE CASCADE
);

-- Table Utilisateur
CREATE TABLE IF NOT EXISTS Utilisateur (
    ID SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    motDePasse VARCHAR(255) NOT NULL
);

-- Table Itineraire
CREATE TABLE IF NOT EXISTS Itineraire (
    ID SERIAL PRIMARY KEY,
    date TIMESTAMP NOT NULL,
    depart BOOLEAN NOT NULL
);

-- Table Voyageur (hérite d'Utilisateur)
CREATE TABLE IF NOT EXISTS Voyageur (
    utilisateurID INT PRIMARY KEY,
    itineraireID INT,
    FOREIGN KEY (utilisateurID) REFERENCES Utilisateur(ID) ON DELETE CASCADE,
    FOREIGN KEY (itineraireID) REFERENCES Itineraire(ID) ON DELETE SET NULL
);

-- Table association Itineraire - Station (arrivée)
CREATE TABLE IF NOT EXISTS Itineraire_Station_Arrivee (
    itineraireID INT NOT NULL,
    stationID INT NOT NULL,
    PRIMARY KEY (itineraireID, stationID),
    FOREIGN KEY (itineraireID) REFERENCES Itineraire(ID) ON DELETE CASCADE,
    FOREIGN KEY (stationID) REFERENCES Station(ID) ON DELETE CASCADE
);

-- Table association Itineraire - Station (départ)
CREATE TABLE IF NOT EXISTS Itineraire_Station_Depart (
    itineraireID INT NOT NULL,
    stationID INT NOT NULL,
    PRIMARY KEY (itineraireID, stationID),
    FOREIGN KEY (itineraireID) REFERENCES Itineraire(ID) ON DELETE CASCADE,
    FOREIGN KEY (stationID) REFERENCES Station(ID) ON DELETE CASCADE
);
