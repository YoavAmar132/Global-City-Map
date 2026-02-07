package common.messages;

import java.io.Serializable;

public class SearchPayload implements Serializable {

    private String query;

    // Default constructor for serialization if needed
    public SearchPayload() {}

    public SearchPayload(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}