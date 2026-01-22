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
    @FXML private TextField email;
    @FXML private PasswordField CreditNumber1;
    @FXML private PasswordField CreditNumber2;
    @FXML private PasswordField CreditNumber3;
    @FXML private PasswordField CreditNumber4;
    @FXML private PasswordField PinNumber;
    private String Errormsg;
    public void handleClose(ActionEvent actionEvent) {
        ClientApp.getNavigator().show(RegistrationController.class);
    }
    public void setUsername(String username)
    {
        this.username=username;
    }
    public void setPassword(String password)
    {
        this.password=password;
    }
    public boolean validEmail(String email) {

        int totalLen = email.length() ,len1,len3,NumOfParts,atIndex,lastDot; //variables to split tokens

        //default checks:
        atIndex = email.indexOf('@');
        lastDot = email.lastIndexOf('.');

        if (totalLen < 2 || totalLen > 50) {
            Errormsg ="Username is too long, try something shorter ";
            return false;
        }
        if (atIndex <= 0  || lastDot == -1 || lastDot < atIndex) {
            Errormsg = "Please enter a valid Email as username ";
            return false;
        }

        // dividing to tokens
        String part1 = email.substring(0, atIndex);
        String part2 = email.substring(atIndex + 1, lastDot);
        String part3 = email.substring(lastDot + 1);
        //first part check
        len1 = part1.length();
        if (len1 < 1) {
            Errormsg ="Please enter a valid Email as username ";
            return false;
        }
        //second part
        boolean valid = part2.matches("[A-Za-z0-9.-]+");
        if (!valid) {
            Errormsg = "Please enter a valid Email as username ";
            return false;
        }
        //third part check
        len3 = part3.length();
        if (len3 < 2) {
            Errormsg = "Please enter a valid Email as username ";
            return false;
        }

        return true;
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
        String mail=email.getText();

        String phone=PhoneNumber.getText();
        System.out.printf(phone);
        String creditcard=CreditNumber1.getText()+"-"+CreditNumber2.getText()+"-"+CreditNumber3.getText()+"-"+CreditNumber4.getText();
        System.out.printf(creditcard);
        String pin=PinNumber.getText();
        System.out.printf(pin);
        if (ValidateCard(creditcard,pin,phone))
        {
            if(validEmail(mail)) {
                RegisterPayload payload = new RegisterPayload(username, password, firstname, lastname, phone, creditcard, pin);
                payload.setEmail(mail);
                SceneNavigator.LoadedView<RegistrationController> view =
                        ClientApp.getNavigator().get(RegistrationController.class);
                // set values BEFORE showing
                view.controller.initialize(payload);
                ClientApp.getNavigator().showLoaded(view.root);
            }else{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Form Failed");
                alert.setContentText(Errormsg);
                alert.showAndWait();
                return;

            }
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
