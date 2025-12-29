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
        switch (poi.getCategory()){
            case RESTAURANT : getStyleClass().addAll("poi", "poi-restaurant");
            break;
            case BAR: getStyleClass().addAll("poi", "poi-bar");
            break;
            case HOTEL: getStyleClass().addAll("poi", "poi-hotel");
            break;
            case PARK:getStyleClass().addAll("poi", "poi-park");
            break;
            case SHOP:getStyleClass().addAll("poi", "poi-shop");
            break;
            case CINEMA: SHOP:getStyleClass().addAll("poi", "poi-cinema");
            break;
            case MUSEUM:getStyleClass().addAll("poi", "poi-museum");
            break;
            case MONUMENT:getStyleClass().addAll("poi", "poi-monument");
            break;
            case OTHER:getStyleClass().addAll("poi", "poi-other");
            break;
            case null, default:getStyleClass().addAll("poi", "poi-other");
            break;
        }


        setMinWidth(30);
        setMinHeight(30);
        setPrefSize(30, 30);
        // Optional: Add a temporary border to ensure you see it even without images
        setStyle("-fx-border-color: red; -fx-border-width: 2px;");


        setFocusTraversable(false);
        setPickOnBounds(true);

        // important: prevent map pan
        setOnMouseClicked(e -> e.consume());
    }

    public Poi getPoi() {
        return poi;
    }
}

