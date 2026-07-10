package com.project.artconnect.util;

import com.project.artconnect.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestionnaire de connexions JDBC — Pattern Singleton thread-safe.
 *
 * <p>Cette classe fournit un point d'accès unique et centralisé pour obtenir
 * une connexion à la base de données MySQL. Elle implémente le pattern
 * <b>Singleton</b> via le <i>Bill Pugh Holder Idiom</i> (classe interne statique),
 * qui garantit :</p>
 * <ul>
 *   <li><b>Lazy initialization</b> — l'instance n'est créée qu'au premier appel</li>
 *   <li><b>Thread-safety</b> — garanti par le mécanisme de chargement de classes de la JVM</li>
 *   <li><b>Performance</b> — aucun coût de synchronisation après l'initialisation</li>
 * </ul>
 *
 * <h3>Utilisation dans un DAO :</h3>
 * <pre>{@code
 * try (Connection conn = ConnectionManager.getConnection()) {
 *     // ... utilisation de la connexion
 * } // La connexion est automatiquement fermée ici
 * }</pre>
 *
 * <h3>Architecture :</h3>
 * <pre>
 *   DatabaseConfig (constantes)
 *        ↓
 *   ConnectionManager (singleton, fournit les connexions)
 *        ↓
 *   JdbcXxxDao (consomme les connexions via try-with-resources)
 * </pre>
 *
 * @see DatabaseConfig
 */
public class ConnectionManager {

    // =========================================================================
    // Singleton — Bill Pugh Holder Idiom
    // =========================================================================

    /**
     * Classe interne statique qui détient l'instance unique.
     * <p>La JVM ne charge cette classe que lorsqu'elle est référencée
     * pour la première fois (appel à {@code getInstance()}), ce qui
     * garantit une initialisation paresseuse et thread-safe sans
     * synchronisation explicite.</p>
     */
    private static class Holder {
        private static final ConnectionManager INSTANCE = new ConnectionManager();
    }

    /**
     * Retourne l'instance unique du ConnectionManager.
     *
     * @return l'instance singleton
     */
    public static ConnectionManager getInstance() {
        return Holder.INSTANCE;
    }

    // =========================================================================
    // Constructeur privé — chargement du driver
    // =========================================================================

    /**
     * Constructeur privé : charge le driver JDBC MySQL une seule fois
     * lors de la création de l'instance singleton.
     */
    private ConnectionManager() {
        try {
            Class.forName(DatabaseConfig.DRIVER);
            System.out.println("[ConnectionManager] Driver MySQL chargé avec succès.");
        } catch (ClassNotFoundException e) {
            System.err.println("[ConnectionManager] ERREUR — Driver MySQL introuvable : " + e.getMessage());
            System.err.println("[ConnectionManager] Vérifiez que mysql-connector-j est dans le classpath (pom.xml).");
            throw new RuntimeException("Impossible de charger le driver JDBC MySQL.", e);
        }
    }

    // =========================================================================
    // Méthode publique — obtention d'une connexion
    // =========================================================================

    /**
     * Crée et retourne une nouvelle connexion à la base de données MySQL.
     *
     * <p><b>Important :</b> Chaque appel crée une nouvelle connexion.
     * L'appelant est responsable de la fermer, idéalement via
     * {@code try-with-resources} :</p>
     *
     * <pre>{@code
     * try (Connection conn = ConnectionManager.getConnection()) {
     *     PreparedStatement ps = conn.prepareStatement("SELECT ...");
     *     // ...
     * }
     * }</pre>
     *
     * @return une nouvelle connexion JDBC active
     * @throws SQLException si la connexion échoue (serveur inaccessible,
     *                      identifiants incorrects, base inexistante, etc.)
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.URL,
                DatabaseConfig.USER,
                DatabaseConfig.PASSWORD
        );
    }

    // =========================================================================
    // Méthode utilitaire — test de connexion
    // =========================================================================

    /**
     * Teste si la connexion à la base de données est fonctionnelle.
     * <p>Utile pour vérifier la configuration au démarrage de l'application
     * ou dans un health-check.</p>
     *
     * @return {@code true} si la connexion a pu être établie et est valide
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            boolean valid = conn != null && conn.isValid(5); // timeout 5 secondes
            if (valid) {
                System.out.println("[ConnectionManager] ✓ Connexion à la base de données réussie.");
                System.out.println("[ConnectionManager]   URL  : " + DatabaseConfig.URL);
                System.out.println("[ConnectionManager]   User : " + DatabaseConfig.USER);
            }
            return valid;
        } catch (SQLException e) {
            System.err.println("[ConnectionManager] ✗ Échec de connexion à la base de données.");
            System.err.println("[ConnectionManager]   URL      : " + DatabaseConfig.URL);
            System.err.println("[ConnectionManager]   User     : " + DatabaseConfig.USER);
            System.err.println("[ConnectionManager]   Erreur   : " + e.getMessage());
            System.err.println("[ConnectionManager]   SQLState : " + e.getSQLState());
            return false;
        }
    }
}
