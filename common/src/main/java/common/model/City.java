package common.model;

import java.io.Serializable;

public class City implements Serializable {

    private int id;
    private String name;
    private String basemap;
    private double price;      // תואם ל-CityPrice
    private double subPrice;
    private String description;// **חדש: תואם ל-SubPrice**

    // עדכון הבנאי (Constructor) לקבלת המחיר החדש
    public City(int id, String name, String baseMap, double price, double subPrice) {
        this.id = id;
        this.name = name;
        this.basemap = baseMap;
        this.price = price;
        this.subPrice = subPrice;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getBasemap() { return basemap; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    // **חדש**
    public double getSubPrice() { return subPrice; }
    public void setSubPrice(double subPrice) { this.subPrice = subPrice; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}