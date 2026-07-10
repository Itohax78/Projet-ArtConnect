package com.project.artconnect.service.impl;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.ArtistService;

import java.util.List;
import java.util.Optional;

/**
 * Implémentation du service Artist basée sur JDBC.
 *
 * <p>Cette classe délègue <b>toute</b> l'accès aux données au {@link ArtistDao}
 * injecté via le constructeur. Elle ne contient aucune logique SQL ni aucune
 * référence directe à JDBC — la séparation des responsabilités est stricte :</p>
 *
 * <ul>
 *   <li><b>Service</b> → logique métier, orchestration, validation</li>
 *   <li><b>DAO</b>     → accès aux données (SQL, JDBC, mapping ResultSet)</li>
 * </ul>
 *
 * <h3>Injection de dépendance :</h3>
 * <p>Le DAO est injecté par constructeur, ce qui permet de :</p>
 * <ul>
 *   <li>Tester le service avec un mock DAO</li>
 *   <li>Changer d'implémentation (InMemory, JDBC, JPA…) sans modifier le service</li>
 * </ul>
 *
 * <h3>Architecture :</h3>
 * <pre>
 *   UI Controller
 *        ↓
 *   ArtistService (interface)
 *        ↓
 *   JdbcArtistService (cette classe — logique métier uniquement)
 *        ↓
 *   ArtistDao (interface)
 *        ↓
 *   JdbcArtistDao (SQL + JDBC)
 * </pre>
 *
 * @see ArtistDao
 * @see com.project.artconnect.persistence.JdbcArtistDao
 */
public class JdbcArtistService implements ArtistService {

    // =========================================================================
    // Dépendance — DAO injecté par constructeur
    // =========================================================================

    private final ArtistDao artistDao;

    /**
     * Construit le service avec le DAO spécifié.
     *
     * @param artistDao l'implémentation du DAO à utiliser (ex : JdbcArtistDao)
     */
    public JdbcArtistService(ArtistDao artistDao) {
        this.artistDao = artistDao;
    }

    // =========================================================================
    // Implémentation de ArtistService — délégation au DAO
    // =========================================================================

    /**
     * Récupère tous les artistes.
     *
     * @return la liste complète des artistes avec leurs disciplines
     */
    @Override
    public List<Artist> getAllArtists() {
        return artistDao.findAll();
    }

    /**
     * Recherche un artiste par son nom exact.
     *
     * @param name le nom de l'artiste
     * @return un Optional contenant l'artiste, ou vide si non trouvé
     */
    @Override
    public Optional<Artist> getArtistByName(String name) {
        return artistDao.findByName(name);
    }

    /**
     * Crée un nouvel artiste.
     * <p>L'artiste est automatiquement marqué comme actif si ce n'est pas déjà le cas.</p>
     *
     * @param artist l'artiste à créer
     */
    @Override
    public void createArtist(Artist artist) {
        artistDao.save(artist);
    }

    /**
     * Met à jour un artiste existant.
     *
     * @param artist l'artiste avec les nouvelles valeurs
     */
    @Override
    public void updateArtist(Artist artist) {
        artistDao.update(artist);
    }

    /**
     * Supprime un artiste par son nom.
     *
     * @param name le nom de l'artiste à supprimer
     */
    @Override
    public void deleteArtist(String name) {
        artistDao.delete(name);
    }

    /**
     * Récupère toutes les disciplines disponibles.
     *
     * @return la liste de toutes les disciplines
     */
    @Override
    public List<Discipline> getAllDisciplines() {
        return artistDao.findAllDisciplines();
    }

    /**
     * Recherche multi-critères parmi les artistes.
     * <p>Les paramètres null ou vides sont ignorés (le filtre correspondant
     * n'est pas appliqué).</p>
     *
     * @param query          recherche partielle sur le nom
     * @param disciplineName filtre par discipline
     * @param city           filtre par ville
     * @return la liste des artistes correspondant aux critères
     */
    @Override
    public List<Artist> searchArtists(String query, String disciplineName, String city) {
        return artistDao.search(query, disciplineName, city);
    }
}
