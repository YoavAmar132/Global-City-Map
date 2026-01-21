package gcm.server.service;

import common.messages.CreateCityPayload;
import common.messages.PendingCityPricePayload;
import common.model.City;
import common.model.CityPricingItem;
import common.model.PendingCityPriceItem;
import gcm.server.data.CityRepo;

import java.sql.SQLException;
import java.util.List;

public class CityService {

    private final CityRepo cityRepo;

    public boolean createCity(CreateCityPayload payload) throws SQLException {
        return cityRepo.createCity(
                payload.getCityName(),
                payload.getBaseMapPath(),
                payload.getDescription()
        );
    }


    public CityService(CityRepo cityRepo) {
        this.cityRepo = cityRepo;
    }

    public boolean requestCityPriceChange(CityPricingItem item) {
        return cityRepo.insertPendingPriceChange(item);
    }


    // READ
    public List<City> getAllCities() throws SQLException {
        return cityRepo.getAllCities();
    }

    public List<City> getAllCitiesWithMaps() throws SQLException {
        return cityRepo.getAllCitiesWithMaps();
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

    public boolean requestCityPriceChange(PendingCityPricePayload payload)
            throws SQLException {

        return cityRepo.upsertPendingCityPrice(
                payload.getCityId(),
                payload.getCityPrice(),
                payload.getSubPrice()
        );
    }

    public List<PendingCityPriceItem> getPendingCityPrices()
            throws SQLException {
        return cityRepo.getPendingCityPrices();
    }

    public boolean approveCityPrice(int cityId) throws SQLException {

        PendingCityPriceItem item =
                cityRepo.getPendingCityPrice(cityId);

        if (item == null) return false;

        cityRepo.updateCityPrice(
                cityId,
                item.getCityPrice(),
                item.getSubPrice()
        );

        cityRepo.deletePendingCityPrice(cityId);
        return true;
    }

    public boolean rejectCityPrice(int cityId) throws SQLException {
        return cityRepo.deletePendingCityPrice(cityId);
    }

}
