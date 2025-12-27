package common.messages;

import java.io.Serializable;

public class CityListPayload implements Serializable {

    private final int offset;
    private final int totalCities;
    private final String searchField;
    public CityListPayload(int offset, int totalCities, String searchField) {
        this.offset = offset;
        this.totalCities = totalCities;
        this.searchField = searchField;
    }

    public int getOffset() {return offset;}
    public int getTotalCities() {return totalCities;}
    public String getSearchField() {return searchField;}
}

