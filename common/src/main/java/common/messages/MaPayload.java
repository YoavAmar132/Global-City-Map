package common.messages;

import java.io.Serializable;

public class MaPayload  implements Serializable {
    private final int version;
    private final String name;
    public MaPayload(int version,String name)
    {
        this.version=version;
        this.name=name;
    }

    public int getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }
}
