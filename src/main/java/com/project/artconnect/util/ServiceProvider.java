package com.project.artconnect.util;

import com.project.artconnect.dao.*;
import com.project.artconnect.persistence.*;
import com.project.artconnect.service.*;
import com.project.artconnect.service.impl.*;

/**
 * Service Provider — gère les instances singleton des services
 * et leur initialisation.
 *
 * <p>C'est ici que l'on choisit quelle implémentation utiliser :</p>
 * <ul>
 *   <li><b>InMemory</b> — données fictives en mémoire (démo/tests)</li>
 *   <li><b>JDBC</b>     — persistance MySQL via les JdbcDao</li>
 * </ul>
 *
 * <p>Pour basculer entre les deux modes, commentez/décommentez les blocs
 * correspondants ci-dessous.</p>
 */
public class ServiceProvider {

    // =========================================================================
    // Mode JDBC — persistance MySQL (ACTIF)
    // =========================================================================

    // DAOs
    private static final ArtistDao artistDao = new JdbcArtistDao();
    private static final ArtworkDao artworkDao = new JdbcArtworkDao();
    private static final GalleryDao galleryDao = new JdbcGalleryDao();
    private static final ExhibitionDao exhibitionDao = new JdbcExhibitionDao();
    private static final WorkshopDao workshopDao = new JdbcWorkshopDao();
    private static final CommunityMemberDao communityMemberDao = new JdbcCommunityMemberDao();

    // Services — les DAOs sont injectés par constructeur
    private static final ArtistService artistService = new JdbcArtistService(artistDao);
    private static final ArtworkService artworkService = new JdbcArtworkService(artworkDao);
    private static final GalleryService galleryService = new JdbcGalleryService(galleryDao, exhibitionDao);
    private static final ExhibitionService exhibitionService = new JdbcExhibitionService(exhibitionDao);
    private static final WorkshopService workshopService = new JdbcWorkshopService(workshopDao);
    private static final CommunityService communityService = new JdbcCommunityService(communityMemberDao);

    // =========================================================================
    // Accesseurs
    // =========================================================================

    public static ArtistService getArtistService() {
        return artistService;
    }

    public static ArtworkService getArtworkService() {
        return artworkService;
    }

    public static GalleryService getGalleryService() {
        return galleryService;
    }

    public static ExhibitionService getExhibitionService() {
        return exhibitionService;
    }

    public static WorkshopService getWorkshopService() {
        return workshopService;
    }

    public static CommunityService getCommunityService() {
        return communityService;
    }
}
