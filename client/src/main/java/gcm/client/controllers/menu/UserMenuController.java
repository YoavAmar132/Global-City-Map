package gcm.client.controllers.menu;

import gcm.client.controllers.WelcomeController;
import gcm.client.controllers.catalog.BuyMapCatalogController;
import gcm.client.controllers.catalogPublic.GuestCatalogController;
import gcm.client.controllers.user_util.ComplaintHistoryController;
import gcm.client.controllers.user_util.CreateComplaintController;
import gcm.client.controllers.manager_util.ClientCardController;
import gcm.client.controllers.user_util.BuyMapScreenController;
import gcm.client.controllers.user_util.MyMapsController;
import gcm.client.network.GcmClient ;
import gcm.client.utill.ClientApp;
import common.messages.*;
import gcm.client.utill.SceneNavigator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert;
import java.util.ArrayList;


public class UserMenuController {
    private GcmClient client;
    private RegisterPayload user;

    public void initialize() {
        client = ClientApp.getClient();
        client.setResponseHandler(this::handleResponse);
    }

    private void handleResponse(GcmResponse response) {
        if(!response.isSuccess())
        {
            String Error=response.getErrorMessage();
            if(Error.equals("faild to get history from db"))
            {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("");
                alert.setContentText("There is no purchase history for your account");
                alert.showAndWait();
                return;
            }
            System.out.println(response.getErrorMessage());
            return;
        }
        System.out.println("called");
        Object t = response.getData();
        if (t instanceof Popup) {
            System.out.println("is popup");
            if((((Popup) t).isInList(ClientApp.getCurrentUser().getId())))
            {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("");
                alert.setContentText("you have a new message");
                alert.showAndWait();
            }
        }
        if (t instanceof RegisterPayload user) {
            System.out.println("got user payload");
            this.user=user;
            System.out.println(user.getUsername());
           GcmRequest request= new GcmRequest(RequestType.LIST_USER_PURCHASES_HISTORY,user);
            client.sendRequest(request);

        }
        if(t instanceof ArrayList<?>)
        {
            ArrayList<?> list = (ArrayList<?>) t;
            if (!list.isEmpty() && list.get(0) instanceof String) {
                SceneNavigator.LoadedView<ClientCardController> view =
                        ClientApp.getNavigator().get(ClientCardController.class);
                view.controller.setType(RequestType.GET_USER_BY_ID);
                view.controller.setClientInfo(user);
                view.controller.setPurchaseHistory((ArrayList<String>) list);
                ClientApp.getNavigator().showLoaded(view.root);
            }
        }else{
            System.out.println("faild to load history");
        }


    }


    @FXML
    private void handleClose(ActionEvent event) {
        ClientApp.logout();
    }


    @FXML
    private void onViewCatalogClicked(ActionEvent event) {
        ClientApp.getNavigator().show(GuestCatalogController.class);

    }

    @FXML
    private void onBuyMapClicked(ActionEvent event) {
        System.out.println("Buy a Map clicked");
        ClientApp.getNavigator().show(BuyMapCatalogController.class);
        // TODO: open purchase flow
    }

    @FXML
    private void onMyMapsClicked(ActionEvent event) {
        System.out.println("My Maps clicked");
        ClientApp.getNavigator().show(MyMapsController.class);
    }

    public void onMyMessagesClicked(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(MessagesController.class);
    }

    public void onPurchaseHistoryClicked(ActionEvent actionEvent) {
        int id=ClientApp.getCurrentUser().getId();
        GcmRequest request=new GcmRequest(RequestType.GET_USER_BY_ID,id);
        client.sendRequest(request);
    }

    @FXML
    private void onViewComplaintHistoryClicked(ActionEvent event) {
        System.out.println("View previous complaints");
        ClientApp.getNavigator().show(ComplaintHistoryController.class);
    }
}