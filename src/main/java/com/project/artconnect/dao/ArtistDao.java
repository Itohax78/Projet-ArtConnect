package com.project.artconnect.dao;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Artist entity.
 *
 * <p>Définit le contrat d'accès aux données pour les artistes.
 * Deux implémentations possibles :</p>
 * <ul>
 *   <li>InMemory (données en dur pour les tests/démo)</li>
 *   <li>JDBC (persistance MySQL via {@code JdbcArtistDao})</li>
 * </ul>
 */
public interface ArtistDao {

    /** Récupère tous les artistes */
    List<Artist> findAll();

    /** Recherche un artiste par son nom exact */
    Optional<Artist> findByName(String name);

    /** Sauvegarde un nouvel artiste */
    void save(Artist artist);

    /** Met à jour un artiste existant */
    void update(Artist artist);

    /** Supprime un artiste par son nom */
    void delete(String artistName);

    /** Recherche les artistes d'une ville donnée */
    List<Artist> findByCity(String city);

    /** Récupère toutes les disciplines disponibles */
    List<Discipline> findAllDisciplines();

    /**
     * Recherche multi-critères (nom partiel, discipline, ville).
     * Les paramètres null ou vides sont ignorés (filtre non appliqué).
     */
    List<Artist> search(String query, String disciplineName, String city);
}
