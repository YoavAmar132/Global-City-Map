package gcm.client.controllers.user_util;
import common.model.City;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import java.io.InputStream;

public class CityCardController {

    @FXML
    private Label cityNameLabel;

    @FXML
    private ImageView cityImage;

    @FXML
    private HBox root;

    private City city;

    private static final String DEFAULT_IMAGE = "imageNotFound";
    private static final String IMAGE_FOLDER_PATH = "/gcm/client/images/";
    private static final String IMAGE_FILE_FORMAT = ".png";

    @FXML
    private void initialize() {

        Rectangle clip = new Rectangle(
                cityImage.getFitWidth(),
                cityImage.getFitHeight()
        );
        clip.setArcWidth(20);
        clip.setArcHeight(20);

        cityImage.setClip(clip);

        root.setOnMouseClicked(e -> {
            System.out.println("Clicked on: " + city.getName());
        });
        root.setOnMouseEntered(e ->
                root.setStyle("-fx-background-radius: 15;-fx-padding: 10;-fx-background-color: rgba(255,255,255,0.3); -fx-border-radius: 15;"));

        root.setOnMouseExited(e ->
                root.setStyle("-fx-background-radius: 15;-fx-padding: 10;-fx-background-color: rgba(255,255,255,0.15); -fx-border-radius: 15;"));
    }

    public void setCity(City city) {
        this.city = city;
        cityNameLabel.setText(city.getName());
        String key = "";
        InputStream is;

        try{
            if (key == null) {
                is = getClass().getResourceAsStream(IMAGE_FOLDER_PATH + DEFAULT_IMAGE + IMAGE_FILE_FORMAT);

            } else {
                is = getClass().getResourceAsStream(IMAGE_FOLDER_PATH + key + IMAGE_FILE_FORMAT);
            }
            if (is == null) {
                System.err.println("Image not found: " + key);
            }
            cityImage.setImage(new Image(is));
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }
}
