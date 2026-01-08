package gcm.client.controllers.map;

import common.model.Poi;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;

public class PoiView extends Button {

    private final Poi poi;
    private boolean selected = false;

    private final Label orderLabel = new Label();

    public PoiView(Poi poi) {
        this.poi = poi;

        setText("");

        switch (poi.getCategory()) {
            case RESTAURANT -> getStyleClass().addAll("poi", "poi-restaurant");
            case BAR        -> getStyleClass().addAll("poi", "poi-bar");
            case HOTEL      -> getStyleClass().addAll("poi", "poi-hotel");
            case PARK       -> getStyleClass().addAll("poi", "poi-park");
            case SHOP       -> getStyleClass().addAll("poi", "poi-shop");
            case CINEMA     -> getStyleClass().addAll("poi", "poi-cinema");
            case MUSEUM     -> getStyleClass().addAll("poi", "poi-museum");
            case MONUMENT   -> getStyleClass().addAll("poi", "poi-monument");
            default         -> getStyleClass().addAll("poi", "poi-other");
        }

        setMinSize(30, 30);
        setPrefSize(30, 30);
        setMaxSize(30, 30);

        // 🔢 Order label (route mode only)
        orderLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-color: rgba(0,0,0,0.7);" +
                        "-fx-padding: 2 6;" +
                        "-fx-background-radius: 10;"
        );
        orderLabel.setVisible(false);

        setGraphic(orderLabel);
        setContentDisplay(ContentDisplay.CENTER);

        setSelected(false);
        setFocusTraversable(false);
        setPickOnBounds(true);

        // do not let the button steal map clicks
        setOnMouseClicked(e -> e.consume());
    }

    public Poi getPoi() {
        return poi;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;

        if (selected) {
            setStyle("-fx-border-color: limegreen; -fx-border-width: 3px;");
        } else {
            setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        }
    }

    public void setOrderIndex(int index) {
        if (index < 0) {
            clearOrderIndex();
        } else {
            orderLabel.setText(String.valueOf(index + 1)); // 1-based
            orderLabel.setVisible(true);
        }
    }

    public void clearOrderIndex() {
        orderLabel.setText("");
        orderLabel.setVisible(false);
    }
}
