package gcm.client.controllers.map;

import common.messages.*;
import common.model.PendingRoute;
import gcm.client.controllers.menu.ContentWorkerMenuController;
import gcm.client.network.GcmClient;
import gcm.client.utill.ClientApp;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class PendingRouteController {

    @FXML
    private VBox pendingList;

    private GcmClient client;

    @FXML
    private void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);

        client.sendRequest(
                new GcmRequest(RequestType.GET_PENDING_ROUTES, new EmptyPayload())
        );
    }

    private HBox createRouteRow(PendingRoute route) {

        Label name = new Label(route.getName());
        name.setStyle("-fx-text-fill: white; -fx-font-size: 14;");

        Button open = new Button("Open");
        open.setPrefSize(90, 30);
        open.setStyle("""
            -fx-background-color: linear-gradient(to right, #00c6ff, #0072ff);
            -fx-text-fill: white;
            -fx-font-size: 13;
            -fx-background-radius: 8;
            -fx-cursor: hand;
        """);
        open.setOnAction(e ->
                System.out.println("TODO: open route " + route.getRouteId())
        );

        Button approve = new Button("Approve");
        approve.setPrefSize(90, 30);
        approve.setStyle("""
            -fx-background-color: rgba(255,255,255,0.20);
            -fx-text-fill: white;
            -fx-font-size: 13;
            -fx-background-radius: 8;
            -fx-cursor: hand;
        """);
        approve.setOnAction(e -> approveRoute(route));

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox row = new HBox(12, name, spacer, open, approve);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.14); -fx-background-radius: 12;");
        row.setPadding(new javafx.geometry.Insets(10, 12, 10, 12));

        return row;
    }

    private void approveRoute(PendingRoute route) {
        client.sendRequest(
                new GcmRequest(
                        RequestType.APPROVE_ROUTE,
                        new ApproveRoutePayload(route.getRouteId())
                )
        );
    }

    private void handleResponse(GcmResponse response) {
        Platform.runLater(() -> {

            if (!response.isSuccess()) {
                System.out.println(response.getErrorMessage());
                return;
            }

            if (response.getData() == null) {
                // approval success → refresh
                onRefreshClicked(null);
                return;
            }

            @SuppressWarnings("unchecked")
            List<PendingRoute> routes = (List<PendingRoute>) response.getData();

            pendingList.getChildren().clear();
            for (PendingRoute r : routes) {
                pendingList.getChildren().add(createRouteRow(r));
            }
        });
    }

    @FXML
    public void handleClose(ActionEvent event) {
        ClientApp.getNavigator().show(ContentWorkerMenuController.class);
    }

    @FXML
    public void onRefreshClicked(ActionEvent event) {
        ClientApp.getNavigator().show(PendingRouteController.class);
    }
}
