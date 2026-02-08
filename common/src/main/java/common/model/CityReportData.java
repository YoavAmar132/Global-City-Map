package common.model;

import java.io.Serializable;

public class CityReportData implements Serializable {
    private String cityName;
    private int numMaps;
    private int numPurchases;
    private int numSubscriptions;
    private int numViews;
    private int numDownloads;

    public CityReportData(String cityName, int numMaps, int numPurchases, int numSubscriptions, int numViews,int numdownloads) {
        this.cityName = cityName;
        this.numMaps = numMaps;
        this.numPurchases = numPurchases;
        this.numSubscriptions = numSubscriptions;
        this.numViews = numViews;
        this.numDownloads=numdownloads;
    }

    // Getters
    public String getCityName() { return cityName; }
    public int getNumMaps() { return numMaps; }
    public int getNumPurchases() { return numPurchases; }
    public int getNumSubscriptions() { return numSubscriptions; }
    public int getNumViews() { return numViews; }

    public int getNumDownloads() {
        return numDownloads;
    }
}