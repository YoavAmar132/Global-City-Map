package gcm.client.controllers.user_util;

import gcm.client.utill.ClientApp;
import gcm.client.utill.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

public class BuyMapController {

    @FXML private RadioButton oneTimePurchaseRadio;
    @FXML private RadioButton subscriptionRadio;

    @FXML
    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        oneTimePurchaseRadio.setToggleGroup(group);
        subscriptionRadio.setToggleGroup(group);
        oneTimePurchaseRadio.setSelected(true);
    }

    @FXML
    public void handleNext() {
        // 1. Calculate Price based on selection
        double price;
        boolean isSub;

        if (subscriptionRadio.isSelected()) {
            price = 150.0; // Example price
            isSub = true;
        } else {
            price = 90.0;
            isSub = false;
        }

        // 2. Update the Session (We add the price to the cart)
        PurchaseSession.getInstance().updatePrice(price, isSub);

        // 3. Navigate to Payment (Clean and simple!)
        ClientApp.getNavigator().show(PaymentController.class);
    }
}