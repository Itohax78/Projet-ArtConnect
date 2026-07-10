# ArtConnect Pro - Local Art Community Platform

## Overview
ArtConnect Pro is a JavaFX-based management system for local art communities. It allows managing artists, artworks, exhibitions, galleries, workshops, and community members.

Starting from a provided UI and OOP skeleton, the objective of this project was to implement the entire persistence layer and database architecture. It focuses on:
1. **Layered Architecture**: Presentation, Service, DAO, and Model layers.
2. **Database Persistence**: Implementing JDBC DAOs to connect to a MySQL database.
3. **JavaFX UI**: Working with FXML, TableViews, and Controllers.


**My specific contributions:**
* Designed and created the MySQL database schema.
* Implemented the JDBC Data Access Objects (DAOs) to bridge the database with the Java OOP models.
* [Ajoute d'autres tâches si besoin, ex: écriture des requêtes SQL complexes, tests, etc.]

## Project Structure
- `com.project.artconnect.MainApp`: Entry point.
- `com.project.artconnect.model`: Domain entities (POJOs/Stubs).
- `com.project.artconnect.dao`: Data Access Object interfaces.
- `com.project.artconnect.persistence`: JDBC implementations.
- `com.project.artconnect.service`: Business logic layer.
- `com.project.artconnect.ui`: JavaFX Controllers and FXML views.
- `com.project.artconnect.util`: Utility classes like `ConnectionManager` and `ServiceProvider`.

## How to Run
Requirement: Java 17+ and Maven installed.

```bash
mvn clean javafx:run
