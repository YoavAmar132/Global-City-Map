package common.messages;

import java.io.Serializable;

public class RouteIndexPayload implements Serializable {
    private final int Index;

    public RouteIndexPayload(int index)
    {
        this.Index=index;
    }

    public int getIndex() {
        return Index;
    }

};
