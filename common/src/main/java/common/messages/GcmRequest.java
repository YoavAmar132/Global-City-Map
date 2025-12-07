package common.messages;
import java.io.Serializable;

public class GcmRequest implements Serializable {


    private final RequestType type;
    private final Object payload;  // must also be Serializable at runtime

    public GcmRequest(RequestType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public RequestType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }
}
