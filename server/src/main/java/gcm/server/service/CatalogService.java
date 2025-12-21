package gcm.server.service;

import common.model.CityCatalogItem;
import common.model.MapCatalogItem;
import gcm.server.data.CatalogRepo;

import java.sql.SQLException;
import java.util.List;

public class CatalogService {

    private final CatalogRepo catalogRepo;

    public CatalogService(CatalogRepo catalogRepo) {
        this.catalogRepo = catalogRepo;
    }

    public List<CityCatalogItem> loadCityCatalog() throws SQLException {
        List<CityCatalogItem> result = catalogRepo.loadCityCatalog();

        System.out.println("CatalogService: cities count = " + result.size());

        return catalogRepo.loadCityCatalog();
    }

    public List<MapCatalogItem> loadMapsForCity(int cityId) throws SQLException {
        return catalogRepo.loadMapsForCity(cityId);
    }
}
