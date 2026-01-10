package gcm.client.controllers;

import common.messages.RegisterPayload;
import gcm.client.controllers.map.MapViewerController;
import gcm.client.utill.ClientApp;
import gcm.client.utill.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class FillFormController {

    private String username;
    private String password;
    @FXML
    private TextField FirstName;
    @FXML private TextField LastName;
    @FXML private TextField PhoneNumber;
    @FXML private PasswordField CreditNumber1;
    @FXML private PasswordField CreditNumber2;
    @FXML private PasswordField CreditNumber3;
    @FXML private PasswordField CreditNumber4;
    @FXML private PasswordField PinNumber;
    public void handleClose(ActionEvent actionEvent) {
    }
    public void setUsername(String username)
    {
        this.username=username;
    }
    public void setPassword(String password)
    {
        this.password=password;
    }
    public Boolean ValidateCard(String card,String pin,String number)
    {
        boolean v1 = card.matches("[0-9 -]*");

        boolean v2=card.length()==19;
        boolean v3 = pin.matches("[0-9]*");
        boolean v4=pin.length()==3;
        boolean v5 = number.matches("[0-9]*");
        boolean v6=number.length()==10;
        System.out.println(v1+""+v2+""+v3+""+v4+""+v5+""+v6);
        return (v1&&v2&&v3&&v4&&v5&&v6);


    }

    public void onSendClicked(ActionEvent actionEvent) {
        String firstname=FirstName.getText();
        System.out.printf(firstname);
        String lastname=LastName.getText();

        String phone=PhoneNumber.getText();
        System.out.printf(phone);
        String creditcard=CreditNumber1.getText()+"-"+CreditNumber2.getText()+"-"+CreditNumber3.getText()+"-"+CreditNumber4.getText();
        System.out.printf(creditcard);
        String pin=PinNumber.getText();
        System.out.printf(pin);
        if (ValidateCard(creditcard,pin,phone))
        {
            RegisterPayload payload=new RegisterPayload(username,password,firstname,lastname,phone,creditcard,pin);
            SceneNavigator.LoadedView<RegistrationController> view =
                    ClientApp.getNavigator().get(RegistrationController.class);
            // set values BEFORE showing
            view.controller.initialize(payload);
            ClientApp.getNavigator().showLoaded(view.root);
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Form Failed");
            alert.setContentText("wrong credit card or phone number");
            alert.showAndWait();
            return;
        }

    }
}
