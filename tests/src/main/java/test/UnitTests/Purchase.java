package test.UnitTests;

import common.messages.*;
import common.model.User;
import gcm.client.controllers.manager_util.ClientCardController;
import gcm.client.controllers.user_util.BuyMapScreenController;
import gcm.client.utill.SceneNavigator;
import gcm.server.controllers.RequestHandler;
import gcm.server.data.*;
import gcm.server.service.AuthService;
import gcm.server.service.MapService;
import gcm.server.service.StatsService;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;

import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import test.TestRunnerMain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // keep same instance so fields persist if you want

public class Purchase {
    private UserRepo userRepo;
    private MapRepo mapRepo;
    private RouteRepo routeRepo;
    private StatsRepo statsRepo;
    private StatsService statsService;
    private AuthService authService;
    private MapService mapService;
    private RequestHandler requestHandler;
    private RegisterPayload p;

    public static final String DB_URL = Registration.DB_URL;
    public static final String DB_USER = Registration.DB_USER;
    public static final String DB_PASS = Registration.DB_PASS;
    private static final String USERNAME = "Tester123";
    private static final String PASSWORD = "Tester123*";
    private User testUser;

    @BeforeAll
    void setup() throws SQLException {
        // init DB once
        DbManager.init(DB_URL, DB_USER, DB_PASS);

        // init services once
        userRepo=new UserRepo();
        mapRepo=new MapRepo();
        routeRepo=new RouteRepo();
        statsRepo=new StatsRepo();
        statsService=new StatsService(statsRepo);
        authService = new AuthService(userRepo);
        mapService=new MapService(mapRepo,userRepo,routeRepo);
        requestHandler = new RequestHandler(authService, mapService, null, null, statsService);

        // make sure starting state is clean so @Order(1) is deterministic
        try {
            cleanUp();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    void teardown() throws SQLException {
        // clean up so repeated runs won't break
        deleteUserByUsername(USERNAME);
    }
    //-----------test 1---------------------------------------------------------------------------
    @Test
    @Order(7)
    void PurchaseTest1() {
        RegisterPayload tester = buildRegPayload(USERNAME,PASSWORD);
         this.p=tester;
        GcmResponse result = handleRegister(tester);

        assertNotNull(
                result.getData(),
                () -> "Expected registration success, but failed: " + result.getErrorMessage()
        );
        if(result.getData() instanceof User) {
            this.testUser = ((User) result.getData());
            BuyMapPayload payload = buildBuyPayload(testUser.getId(),false);
            GcmResponse result2 = handlePurchase(payload);
            assertTrue(mapService.isCityPurchased(testUser.getId(), "Akko"),
                    () -> "Expected Purchase to be success, but failed: ");


            // store message for your custom runner (only if you really want it)
            TestRunnerMain.setSuccessMsg(
                    "PurchaseTest1()",
                    "Successfuly Purchased Akko ");

        }

    }
//----------test2------------------------------------


    @Test
    @Order(8)
    void PurchaseTest2() throws SQLException {

            BuyMapPayload payload = buildBuyPayload(testUser.getId(), true);
            GcmResponse result = handlePurchase(payload);
            assertTrue(mapService.isUserSubscribed(testUser.getId(), "Akko"),
                    () -> "Expected Subscription to be success, but failed: ");


            // store message for your custom runner (only if you really want it)
            TestRunnerMain.setSuccessMsg(
                    "PurchaseTest2()",
                    "Successfuly Subscribed to Akko for 1 month ");

        }

        //---------------------------------------------------------------------------
        @Nested
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        public class BuyMapPopupTest extends ApplicationTest {

            private SceneNavigator.LoadedView<BuyMapScreenController> view;
            private SceneNavigator.LoadedView<ClientCardController> view2;
             private SceneNavigator nav;
            @Override
            public void start(Stage stage) {
                 nav = new SceneNavigator(stage);

                // Load the screen AND keep controller reference
                this.view = nav.get(BuyMapScreenController.class);
                nav.showLoaded(view.root);
                this.view2=nav.get(ClientCardController.class);

            }

            @Test
            @Order(8)
            public void PurchaseTest3() {
                // 1) open the popup from the controller (no need to click screen buttons)
                BuyMapPayload payload = new BuyMapPayload(
                        1,      // userId (doesn't matter for validation)
                        "Akko",  // city
                        0.0,     // price
                        false,   // isSubscription
                        1        // version
                );

                Platform.runLater(() -> view.controller.showTransactionPopup(payload));
                WaitForAsyncUtils.waitForFxEvents();

                // 2) fill fields by PROMPT TEXT (because you didn't set fx:id)
                TextField cardField = lookup(".text-field")
                        .match(n -> n instanceof TextField tf && "Credit Card Number".equals(tf.getPromptText()))
                        .queryAs(TextField.class);

                TextField expiryField = lookup(".text-field")
                        .match(n -> n instanceof TextField tf && "MM/YY".equals(tf.getPromptText()))
                        .queryAs(TextField.class);

                PasswordField pinField = lookup(".password-field")
                        .match(n -> n instanceof PasswordField pf && "PIN".equals(pf.getPromptText()))
                        .queryAs(PasswordField.class);

                clickOn(cardField).write("123");      // WRONG (not 16 digits)
                clickOn(expiryField).write("12/99");  // valid future
                clickOn(pinField).write("123");       // valid 3 digits

                // 3) click confirm
                clickOn("Confirm Payment");
                WaitForAsyncUtils.waitForFxEvents();

                // 4) success condition: ERROR alert appears
                DialogPane dialog = lookup(".dialog-pane").queryAs(DialogPane.class);
                assertNotNull(dialog);

                assertTrue(dialog.getStyleClass().contains("alert"));
                assertTrue(dialog.getStyleClass().contains("error"));

                // Optional: check message text
                String msg = dialog.getContentText().toLowerCase();
                assertTrue(msg.contains("not complete") || msg.contains("payment"));

                // close the alert so test doesn't hang
                // wait 2 seconds
                WaitForAsyncUtils.sleep(2, TimeUnit.SECONDS);
                clickOn("OK");
                WaitForAsyncUtils.waitForFxEvents();

                // close popup (still open after alert)

                clickOn("Cancel");
                WaitForAsyncUtils.waitForFxEvents();
                TestRunnerMain.setSuccessMsg(
                        "PurchaseTest3()",
                        "Expected error your transaction is  not complete... ");


            }

            @Test
            @Order(9)
            public void PurchaseTest4() {
                int id;
                try {
                   id =getUserIdByUsername();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
             p.setUserid(id);
                GcmResponse response=handleHistory(p);
                if (response.isSuccess())
                {
                    view2.controller.setPurchaseHistory((ArrayList<String>) response.getData());
                    view2.controller.setType((RequestType.GET_USER_BY_ID));
                    view2.controller.setClientInfo(p);
                  Platform.runLater(() -> nav.showLoaded(view2.root));
                    WaitForAsyncUtils.waitForFxEvents();
                    WaitForAsyncUtils.sleep(5, TimeUnit.SECONDS);
                    // lookup the table by fx:id
                    TableView<?> table = lookup("#tblPurchases").queryAs(TableView.class);

// table exists
                    assertNotNull(table, "tblPurchases TableView was not found");

// table has data
                    assertFalse(
                            table.getItems().isEmpty(),
                            "Expected tblPurchases to contain rows, but it was empty"
                    );
                    TestRunnerMain.setSuccessMsg(
                            "PurchaseTest4()",
                            " Transaction compleated Purchase history fully updated ");

                }

            }
        }








    /* ================= helpers ================= */

    private RegisterPayload buildRegPayload(String username,String password) {
        String test = "test";
        RegisterPayload p = new RegisterPayload(username, password, test, test, "0", test, test);
        p.setRole("Customer");
        p.setEmail(test);
        return p;
    }
    private BuyMapPayload buildBuyPayload(int userId,boolean sub) {
        BuyMapPayload payload= new BuyMapPayload(
                userId,     // userId
                "Akko",     // cityName
                0.0,        // price (set real price later if needed)
                sub,      // isSubscription
                1           // version
        );
        payload.setCredit("1234");
        return payload;
    }


    private GcmResponse handleRegister(RegisterPayload payload) {
        GcmRequest request = new GcmRequest(RequestType.REGISTER, payload);
        try {
            return requestHandler.handle(request);
        } catch (SQLException e) {
            fail("SQLException during requestHandler.handle(): " + e.getMessage());
            return null; // unreachable, but required by compiler
        }
    }
    private GcmResponse handlePurchase(BuyMapPayload payload) {
        GcmRequest request = new GcmRequest(RequestType.BUY_MAP, payload);
        try {
            return requestHandler.handle(request);
        } catch (SQLException e) {
            fail("SQLException during requestHandler.handle(): " + e.getMessage());
            return null; // unreachable, but required by compiler
        }
    }
    private GcmResponse handleHistory(RegisterPayload payload) {
        GcmRequest request = new GcmRequest(RequestType.LIST_USER_PURCHASES_HISTORY, payload);
        try {
            return requestHandler.handle(request);
        } catch (SQLException e) {
            fail("SQLException during requestHandler.handle(): " + e.getMessage());
            return null; // unreachable, but required by compiler
        }
    }

    private void deleteUserByUsername(String username) throws SQLException {
        // Use DbManager.getConnection() if you have it; otherwise adapt to your DbManager API.
        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM Users WHERE UserName = ?")) {
            ps.setString(1, username);
            ps.executeUpdate();
        }
    }
    private int getUserIdByUsername() throws SQLException {
        String sql = "SELECT UserID FROM Users WHERE UserName = ?";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, USERNAME);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("UserID");
                }
            }
        }

        // user not found
        return -1;
    }

    private void cleanUp() throws SQLException {
        // Use DbManager.getConnection() if you have it; otherwise adapt to your DbManager API.
        int userid;
        try {
             userid=getUserIdByUsername();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if(userid==-1){return;}
        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM MESSAGES WHERE UserID = ?")) {
            ps.setInt(1, userid);
            ps.executeUpdate();
        }
        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM PURCHASES WHERE UserID = ?")) {
            ps.setInt(1, userid);
            ps.executeUpdate();
        }
        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM SUBSCRIPTIONS WHERE UserID = ?")) {
            ps.setInt(1, userid);
            ps.executeUpdate();
        }
        try {
            deleteUserByUsername(USERNAME);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
