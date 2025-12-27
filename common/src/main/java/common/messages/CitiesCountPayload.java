package common.messages;

import java.io.Serializable;


public class CitiesCountPayload implements Serializable {

    private final String searchField;

    public CitiesCountPayload(String searchField) {
        this.searchField = searchField;
    }

    public String getSearchField() {return searchField;}
}
