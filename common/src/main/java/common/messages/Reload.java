package common.messages;

import java.io.Serializable;

public class Reload implements Serializable {
    private String order;

    public void setOrder(String order) {
        this.order = order;
    }

    public String getOrder() {
        return order;
    }
}
