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

        Circle circle = new Circle(6);
        circle.getStyleClass().add("poi-circle");

        Label label = new Label(poi.getName());
        label.getStyleClass().add("poi-label");

        setAlignment(Pos.TOP_CENTER);
        getChildren().addAll(circle, label);

        // Important for overlay behavior:
        setPickOnBounds(false);     // only actual shapes receive clicks
        setMouseTransparent(false); // this node CAN receive mouse events

        // Example click handler
        setOnMouseClicked(e -> {
            System.out.println("Clicked POI: " + poi.getName());
            e.consume(); // stop event from going down to canvas if you want
        });
    }

    // ⬅ This is what you’re missing
    public Poi getPoi() {
        return poi;
    }
}
