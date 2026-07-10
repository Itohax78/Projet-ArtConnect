package com.project.artconnect.service.impl;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.persistence.JdbcGalleryDao;
import com.project.artconnect.service.GalleryService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class JdbcGalleryService implements GalleryService {

    private final GalleryDao galleryDao;
    private final ExhibitionDao exhibitionDao;

    public JdbcGalleryService(GalleryDao galleryDao, ExhibitionDao exhibitionDao) {
        this.galleryDao = galleryDao;
        this.exhibitionDao = exhibitionDao;
    }

    @Override
    public List<Gallery> getAllGalleries() {
        return galleryDao.findAll();
    }

    @Override
    public Optional<Gallery> getGalleryByName(String name) {
        return galleryDao.findByName(name);
    }

    @Override
    public List<Exhibition> getExhibitionsByGallery(Gallery gallery) {
        if (gallery == null) return Collections.emptyList();
        if (galleryDao instanceof JdbcGalleryDao) {
            int galleryId = ((JdbcGalleryDao) galleryDao).findGalleryIdByName(gallery.getName());
            if (galleryId > 0) {
                return exhibitionDao.findByGalleryId(galleryId);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void createGallery(Gallery gallery) {
        galleryDao.save(gallery);
    }

    @Override
    public void updateGallery(Gallery gallery) {
        galleryDao.update(gallery);
    }

    @Override
    public void deleteGallery(String name) {
        galleryDao.delete(name);
    }
}
