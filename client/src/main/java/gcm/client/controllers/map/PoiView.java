package gcm.client.controllers.map;

import common.model.Poi;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
public class PoiView extends StackPane {
    private final Poi poi;

    public PoiView(Poi poi) {
        this.poi = poi;

        Circle circle = new Circle(5);
        circle.getStyleClass().add("poi-circle");

        Label label = new Label(poi.getName());
        label.getStyleClass().add("poi-label");

        setAlignment(Pos.TOP_CENTER);
        getChildren().addAll(circle, label);

        setPickOnBounds(false);
        setMouseTransparent(false);
    }

    public Poi getPoi() {
        return poi;
    }
}

