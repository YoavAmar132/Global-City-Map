package gcm.server.service;

import common.model.City;
import gcm.server.data.CityRepo;

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

    // DELETE (admin later)
    public boolean removeCity(int cityId) throws SQLException {
        return cityRepo.deleteCity(cityId);
    }
}
