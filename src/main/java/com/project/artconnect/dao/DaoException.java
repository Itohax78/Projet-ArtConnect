package com.project.artconnect.dao;

/**
 * Exception technique levée par la couche DAO (persistence).
 *
 * <p>Wraps les {@link java.sql.SQLException} en une RuntimeException
 * spécifique pour éviter d'exposer les détails JDBC aux couches supérieures.</p>
 *
 * <p>La couche Service doit catcher cette exception et la transformer
 * en une {@link com.project.artconnect.service.ServiceException} avec
 * un message lisible pour l'utilisateur.</p>
 */
public class DaoException extends RuntimeException {

    public DaoException(String message) {
        super(message);
    }

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
