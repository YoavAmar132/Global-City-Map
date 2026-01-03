package common.messages;

import java.io.Serializable;

public class GcmResponse implements Serializable {

    private final boolean success;
    private final Object data;
    private final String errorMessage;
    private static int refresh=0;

    // PRIVATE CONSTRUCTOR → forces use of static methods
    private GcmResponse(boolean success, Object data, String errorMessage) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    public void setRefresh(int i)
    {
        this.refresh=i;
    }

    public static int getRefresh() {
        return refresh;
    }

    // FACTORY METHOD → correct for success case
    public static GcmResponse ok(Object data) {
        return new GcmResponse(true, data, null);
    }

    // FACTORY METHOD → correct for error case
    public static GcmResponse error(String errorMessage) {
        return new GcmResponse(false, null, errorMessage);
    }

    // GETTERS
    public boolean isSuccess() {
        return success;
    }

    public Object getData() {
        return data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
