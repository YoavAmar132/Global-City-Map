package gcm.server.service;

import common.model.City;
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
    public List<City> getAllCities(int offset, int totalCities, String searchField) throws SQLException {
        return cityRepo.getAllCities(offset,totalCities,searchField);
    }


    public int getCitiesCount(String searchText) throws SQLException {
        return cityRepo.getCitiesCount(searchText);
    }

    // CREATE (admin later)
    public boolean addCity(String cityName,String imagePath) throws SQLException {

        if (cityName == null || cityName.isBlank()) {
            return false;
        }

        if (cityRepo.existsByCityName(cityName)) {
            return false;
        }

        return cityRepo.insertCity(cityName,imagePath);
    }

    // DELETE (admin later)
    public boolean removeCity(int cityId) throws SQLException {
        return cityRepo.deleteCity(cityId);
    }
}
