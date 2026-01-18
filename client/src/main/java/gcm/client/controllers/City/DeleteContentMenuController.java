package gcm.client.controllers.City;

import gcm.client.controllers.menu.ManagerMenuController;
import gcm.client.utill.ClientApp;
import gcm.client.utill.SceneNavigator;
import javafx.fxml.FXML;

public class DeleteContentMenuController {

    public enum DeleteTarget {
        POI, MAP, ROUTE
    }

    @FXML
    private void onDeletePoi() {
        openCitySelector(DeleteTarget.POI);
    }

    @FXML
    private void onDeleteMap() {
        openCitySelector(DeleteTarget.MAP);
    }

    @FXML
    private void onDeleteRoute() {
        openCitySelector(DeleteTarget.ROUTE);
    }

    private void openCitySelector(DeleteTarget target) {
        SceneNavigator.LoadedView<CitySelectionController> view =
                ClientApp.getNavigator().get(CitySelectionController.class);

        view.controller.setDeleteTarget(target);

        ClientApp.getNavigator().showLoaded(view.root);
    }


    @FXML
    private void handleClose() {
        ClientApp.getNavigator().show(ManagerMenuController.class);
    }
}

