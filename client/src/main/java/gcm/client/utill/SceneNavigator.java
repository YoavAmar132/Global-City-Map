package gcm.client.utill;

import gcm.client.controllers.*;
import gcm.client.controllers.catalog.BuyMapCatalogController;
import gcm.client.controllers.catalog.ContentCatalogController;
import gcm.client.controllers.manager_util.ManageClientController;
import gcm.client.controllers.manager_util.ReportResultController;
import gcm.client.controllers.manager_util.ReportsInputController;
import gcm.client.controllers.map.*;
import gcm.client.controllers.menu.*;

import gcm.client.controllers.user_util.BuyMapScreenController;
import gcm.client.controllers.user_util.MyMapsController;
import gcm.client.controllers.user_util.SubscriptionMapsController;
import gcm.client.controllers.user_util.UserSubscriptionsController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SceneNavigator {
    private final Stage stage;
    private final Map<Class<?>, String> routes = new HashMap<>();

    public SceneNavigator(Stage stage) {
        this.stage = stage;

        // NOTE: leading "/" and matching src/main/resources structure
        routes.put(LoginController.class, "/gcm/client/Login/LoginScreen.fxml");
        routes.put(FillFormController.class, "/gcm/client/Login/FillFormScreen.fxml");
        routes.put(WelcomeController.class, "/gcm/client/Login/WelcomeScreen.fxml");
        routes.put(RegistrationController.class, "/gcm/client/Login/RegistrationScreen.fxml");
        routes.put(UserMenuController.class, "/gcm/client/menu/UserMenuScreen.fxml");
        routes.put(ContentWorkerMenuController.class, "/gcm/client/menu/ContentWorkerMenuScreen.fxml");
        routes.put(ManagerMenuController.class, "/gcm/client/menu/ManagerMenuScreen.fxml");
        routes.put(CustomerSupportMenuController.class, "/gcm/client/menu/CustomerSupportMenuScreen.fxml");
        routes.put(MapViewerController.class, "/gcm/client/map/MapViewerRoot.fxml");
        routes.put(UserMapViewerController.class, "/gcm/client/map/UserMapViewerRoot.fxml");
        routes.put(PendingMapController.class, "/gcm/client/map/PendingMapScreen.fxml");
        routes.put(ContentCatalogController.class, "/gcm/client/catalog/ContentCatalogScreen.fxml");
        routes.put(MapLoaderController.class, "/gcm/client/map/MapLoaderScreen.fxml");
        routes.put(BaseMapSelectorController.class, "/gcm/client/map/BaseMapSelectorScreen.fxml");
        routes.put(gcm.client.controllers.catalogPublic.GuestCatalogController.class,
                "/gcm/client/catalogPublic/GuestCatalogScreen.fxml");
        routes.put(gcm.client.controllers.catalogPublic.GuestCityMapsController.class,
                "/gcm/client/catalogPublic/GuestCityMapsScreen.fxml");
        routes.put(BuyMapScreenController.class, "/gcm/client/user_util/BuyMapScreen.fxml");
        routes.put(BuyMapCatalogController.class, "/gcm/client/user_util/BuyMapCatalog.fxml");
        routes.put(MyMapsController.class, "/gcm/client/user_util/MyMapsScreen.fxml");
        routes.put(EditPricesController.class, "/gcm/client/user_util/EditPricesScreen.fxml");
        routes.put(UserSubscriptionsController.class, "/gcm/client/user_util/UserSubscriptions.fxml");
        routes.put(SubscriptionMapsController.class, "/gcm/client/user_util/SubscriptionMaps.fxml");
        routes.put(ReportsInputController.class, "/gcm/client/manager_util/ReportsInputScreen.fxml");
        routes.put(ReportResultController.class, "/gcm/client/manager_util/ReportResultView.fxml");
        routes.put(MessagesController.class, "/gcm/client/menu/MessagesScreen.fxml"); //yoav
        routes.put(ManageClientController.class, "/gcm/client/manager_util/ManageClientScreen.fxml");


        routes.put(PendingRouteController.class, "/gcm/client/map/PendingRouteScreen.fxml");





        //yoav
        //adam

    }

    public <T> void show(Class<T> controllerClass) {
        String fxml = routes.get(controllerClass);
        if (fxml == null) {
            throw new IllegalArgumentException("No route for " + controllerClass.getName());
        }
        try {
            URL url = getClass().getResource(fxml);
            System.out.println("Loading FXML: " + fxml + " → " + url);

            if (url == null) {
                throw new IllegalStateException(
                        "FXML not found on classpath at: " + fxml +
                                "\nMake sure it is under src/main/resources" + fxml
                );
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/gcm/client/map/map.css").toExternalForm()
            );

            stage.setScene(scene);
            if (!stage.isShowing()) stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + fxml, e);
        }
    }


    public static class LoadedView<T> {
        public final Parent root;
        public final T controller;

        public LoadedView(Parent root, T controller) {
            this.root = root;
            this.controller = controller;
        }
    }

    public <T> LoadedView<T> get(Class<T> controllerClass) {
        String fxml = routes.get(controllerClass);
        if (fxml == null) {
            throw new IllegalArgumentException("No route for " + controllerClass.getName());
        }

        try {
            URL url = getClass().getResource(fxml);
            System.out.println("Loading FXML (no show): " + fxml + " → " + url);

            if (url == null) {
                throw new IllegalStateException("FXML not found on classpath at: " + fxml);
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            T controller = loader.getController();

            return new LoadedView<>(root, controller);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + fxml, e);
        }
    }

    public void showLoaded(Parent root) {
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                getClass().getResource("/gcm/client/map/map.css").toExternalForm()
        );

        stage.setScene(scene);
        if (!stage.isShowing()) stage.show();
    }


}
