package gcm.client.controllers.map;

import common.model.Poi;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
public class PoiView extends Button {

    private final Poi poi;

    public PoiView(Poi poi) {
        this.poi = poi;

        setText("");
        getStyleClass().add("poi-marker");
        setFocusTraversable(false);
        setPickOnBounds(true);

        // important: prevent map pan
        setOnMouseClicked(e -> e.consume());
    }

    public Poi getPoi() {
        return poi;
    }
}

