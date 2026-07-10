package com.project.artconnect.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration centralisée pour la connexion à la base de données MySQL.
 *
 * <p>Les identifiants sont lus depuis le fichier {@code db.properties} situé
 * dans {@code src/main/resources/}. Ce fichier est <b>gitignored</b> pour ne
 * jamais exposer de mots de passe sur GitHub.</p>
 *
 * <h3>Mise en place pour un nouveau développeur :</h3>
 * <ol>
 *   <li>Copier {@code db.properties.example} → {@code db.properties}</li>
 *   <li>Remplir avec ses propres identifiants MySQL</li>
 *   <li>Ne jamais commiter {@code db.properties}</li>
 * </ol>
 *
 * @see com.project.artconnect.util.ConnectionManager
 */
public final class DatabaseConfig {

    // =========================================================================
    // Propriétés chargées depuis db.properties
    // =========================================================================

    /** Classe du driver JDBC MySQL */
    public static final String DRIVER;

    /** URL de connexion JDBC complète */
    public static final String URL;

    /** Nom d'utilisateur MySQL */
    public static final String USER;

    /** Mot de passe MySQL */
    public static final String PASSWORD;

    // =========================================================================
    // Bloc statique — chargement du fichier db.properties
    // =========================================================================

    static {
        Properties props = new Properties();

        // Charger db.properties depuis le classpath (src/main/resources/)
        try (InputStream input = DatabaseConfig.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new RuntimeException(
                    "Fichier 'db.properties' introuvable dans src/main/resources/.\n"
                    + "-> Copiez db.properties.example en db.properties et renseignez vos identifiants."
                );
            }

            props.load(input);

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la lecture de db.properties.", e);
        }

        // Affecter les constantes depuis les propriétés chargées
        DRIVER   = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        URL      = props.getProperty("db.url");
        USER     = props.getProperty("db.user");
        PASSWORD = props.getProperty("db.password");

        // Vérifications — échouer vite si la config est incomplète
        if (URL == null || URL.isBlank()) {
            throw new RuntimeException("Propriété 'db.url' manquante dans db.properties.");
        }
        if (USER == null || USER.isBlank()) {
            throw new RuntimeException("Propriété 'db.user' manquante dans db.properties.");
        }
        if (PASSWORD == null) {
            throw new RuntimeException("Propriété 'db.password' manquante dans db.properties.");
        }
    }

    // =========================================================================
    // Constructeur privé — empêche l'instanciation
    // =========================================================================

    private DatabaseConfig() {
        throw new AssertionError("Classe utilitaire — ne pas instancier.");
    }
}
