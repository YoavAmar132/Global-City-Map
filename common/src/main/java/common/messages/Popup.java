package common.messages;

import java.io.Serializable;
import java.util.ArrayList;

public class Popup implements Serializable {
    private final ArrayList<Integer> ids;

    public Popup(ArrayList<Integer> ids)
    {
        this.ids=ids;
    }

    public ArrayList<Integer> getIds() {
        return ids;
    }
    public boolean isInList(int id)
    {
        return ids.contains(id);
    }
}
