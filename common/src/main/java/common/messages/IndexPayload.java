package common.messages;

import java.io.Serializable;

public class IndexPayload implements Serializable {
    private final int Index;
    public IndexPayload(int index)
    {
        this.Index=index;
    }

    public int getIndex() {
        return Index;
    }
}
