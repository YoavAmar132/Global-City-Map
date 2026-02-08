package gcm.client.controllers.menu;

import common.model.Poi;
import common.model.Route;
import gcm.client.controllers.catalogPublic.GuestCatalogController;
import gcm.client.controllers.map.MapLoaderController;
import gcm.client.controllers.map.UserMapViewerController;
import javafx.application.Platform;
import javafx.event.ActionEvent;

import common.messages.*;
import common.model.City;
import common.model.MapSheet;
import gcm.client.controllers.menu.ContentWorkerMenuController;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import gcm.client.utill.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ChoiceDialog;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import java.util.List;

public class MessagesController {
    private GcmClient client;
    @FXML
    private VBox Baselist;

    private ArrayList<Message> messages; // loaded earlier

    @FXML
    private void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);


        GcmRequest request = new GcmRequest(RequestType.GET_MESSAGES, ClientApp.getCurrentUser());
        client.sendRequest(request);
    }


    private HBox createMapRow(Message message) {
        Label name = new Label(message.getTitle());
        name.setStyle("-fx-text-fill: white; -fx-font-size: 14;");

        Button open = new Button("Open");
        open.setPrefSize(90, 30);
        open.setStyle("-fx-background-color: linear-gradient(to right, #00c6ff, #0072ff); -fx-text-fill: white; -fx-font-size: 13; -fx-background-radius: 8; -fx-cursor: hand;");
        open.setOnAction(e -> openMessage(message));

        Button Dismiss = new Button("Dismiss");
        Dismiss.setPrefSize(90, 30);
        Dismiss.setStyle("-fx-background-color: rgba(255,255,255,0.20); -fx-text-fill: white; -fx-font-size: 13; -fx-background-radius: 8; -fx-cursor: hand;");
        Dismiss.setOnAction(e -> Dismiss(message));

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox row = new HBox(12, name, spacer, open, Dismiss);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.14); -fx-background-radius: 12;");
        row.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));

        return row;
    }



    public void openMessage(Message message) {
        System.out.println("opens message");
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(message.getTitle());
        alert.setContentText(message.getMessage());
        alert.showAndWait();
    }

    public void Dismiss(Message message) {
      messages.remove(message);
        Platform.runLater(() -> ClientApp.getNavigator().show(MessagesController.class));
    }


    private void handleResponse(GcmResponse response) {
        if (!response.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("loading all Messages Failed");
            alert.setContentText(response.getErrorMessage());
            alert.showAndWait();
        }else {

            Object t = response.getData();
            if(t instanceof ArrayList<?>)
            {
                ArrayList<?> list = (ArrayList<?>) t;

                if (!list.isEmpty() && list.get(0) instanceof Message) {
                    messages = (ArrayList<Message>) list;
                    Baselist.getChildren().clear();
                    String download="";
                    Message summary=new Message("City downloaded succsesfuly","");
                    for (Message message  : messages) {
                        if(message.getMessage().startsWith("Map"))
                        {
                            download=download+message.getMessage();
                            summary.setMessage(download);
                            continue;
                        }
                        Baselist.getChildren().add(createMapRow(message));
                    }
                    Baselist.getChildren().add(createMapRow(summary));
                    System.out.println("City list success");
                }


            }

        }


    }


    public void onBackClicked(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(UserMenuController.class);
    }

    public void handleClose(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(UserMenuController.class);
    }

    public void onRefreshClicked(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(MessagesController.class);
    }
}
