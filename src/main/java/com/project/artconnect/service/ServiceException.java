package com.project.artconnect.service;

/**
 * Exception métier levée par la couche Service.
 *
 * <p>Utilisée pour deux cas :</p>
 * <ul>
 *   <li><b>Erreur métier</b> — validation échouée (prix négatif, nom manquant, etc.)</li>
 *   <li><b>Erreur technique wrappée</b> — une {@link com.project.artconnect.dao.DaoException}
 *       attrapée par le service et transformée en message lisible pour l'UI</li>
 * </ul>
 *
 * <p>Le Controller UI doit catcher cette exception et afficher
 * {@code getMessage()} à l'utilisateur.</p>
 */
public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
