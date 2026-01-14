package gcm.server.service;

import common.model.City;
import common.model.CityPricingItem;
import gcm.server.data.CityRepo;
import gcm.server.data.DbManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CityService {

    private final CityRepo cityRepo;

    public CityService(CityRepo cityRepo) {
        this.cityRepo = cityRepo;
    }


    // READ
    public List<City> getAllCities() throws SQLException {
        return cityRepo.getAllCities();
    }


    // CREATE (admin later)
    public boolean addCity(String cityName) throws SQLException {

        if (cityName == null || cityName.isBlank()) {
            return false;
        }

        if (cityRepo.existsByCityName(cityName)) {
            return false;
        }

        return cityRepo.insertCity(cityName);
    }

    public List<CityPricingItem> getAllCityPrices() throws SQLException {
        return cityRepo.getAllCityPrices();
    }

    public boolean updateCityPrice(CityPricingItem item) throws SQLException {
        return cityRepo.updateCityPrice(item);
    }



    // DELETE (admin later)
    public boolean removeCity(int cityId) throws SQLException {
        return cityRepo.deleteCity(cityId);
    }
}
