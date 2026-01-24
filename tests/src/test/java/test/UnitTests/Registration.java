package test.UnitTests;

import common.messages.GcmRequest;
import common.messages.GcmResponse;
import common.messages.RegisterPayload;
import common.messages.RequestType;
import common.model.User;
import gcm.server.controllers.RequestHandler;
import gcm.server.data.DbManager;
import gcm.server.data.UserRepo;
import gcm.server.service.AuthService;
import org.junit.jupiter.api.*;

import test.TestRunnerMain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // keep same instance so fields persist if you want
class Registration {

    private UserRepo userRepo;
    private AuthService authService;
    private RequestHandler requestHandler;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/GCM_DB";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Yoavamar132!";

    private static final String USERNAME = "Adam123";
    private static final String PASSWORD = "Adam199*";

    @BeforeAll
    void setup() throws SQLException {
        // init DB once
        DbManager.init(DB_URL, DB_USER, DB_PASS);

        // init services once
        userRepo = new UserRepo();
        authService = new AuthService(userRepo);
        requestHandler = new RequestHandler(authService, null, null, null, null);

        // make sure starting state is clean so @Order(1) is deterministic
        deleteUserByUsername(USERNAME);
    }

    @AfterAll
    void teardown() throws SQLException {
        // clean up so repeated runs won't break
        deleteUserByUsername(USERNAME);
    }

    @Test
    @Order(1)
    void RegisterTest1() {
        RegisterPayload tester = buildPayload(USERNAME,PASSWORD);

        GcmResponse result = handleRegister(tester);

        assertNotNull(
                result.getData(),
                () -> "Expected registration success, but failed: " + result.getErrorMessage()
        );
        // store message for your custom runner (only if you really want it)
        TestRunnerMain.setSuccessMsg(
                "RegisterTest1()",
                "Successfuly added " + ((User) result.getData()).getUsername()
        );
    }

    @Test
    @Order(2)
    void RegisterTest2() {
        RegisterPayload tester = buildPayload(USERNAME,PASSWORD);

        GcmResponse result = handleRegister(tester);

        // Expect failure: data should be null (duplicate username)
        assertNull(
                result.getData(),
                () -> "Expected duplicate registration to fail, but it succeeded."
        );

        // store message for your custom runner (only if you really want it)
       TestRunnerMain.setSuccessMsg(
                "RegisterTest2()",
                "Expected failure: " + result.getErrorMessage()
        );
    }
    @Test
    @Order(3)
    void RegisterTest3() {
        RegisterPayload tester = buildPayload("123",PASSWORD);

        GcmResponse result = handleRegister(tester);

        // Expect failure: data should be null (duplicate username)
        assertNull(
                result.getData(),
                () -> "Expected duplicate registration to fail, but it succeeded."
        );

       if(result.getData()==null) {
           TestRunnerMain.setSuccessMsg(
                   "RegisterTest3()",
                   "Expected failure: " + result.getErrorMessage()
           );
           try {
               deleteUserByUsername(USERNAME);
           } catch (SQLException e) {
               throw new RuntimeException(e);
           }
       }
    }
    @Test
    @Order(4)
    void RegisterTest4() {
        RegisterPayload tester = buildPayload(USERNAME,"123");

        GcmResponse result = handleRegister(tester);

        // Expect failure: data should be null (duplicate username)
        assertNull(
                result.getData(),
                () -> "Expected duplicate registration to fail, but it succeeded."
        );

        // store message for your custom runner (only if you really want it)
        TestRunnerMain.setSuccessMsg(
                "RegisterTest4()",
                "Expected failure: " + result.getErrorMessage()
        );
    }

    /* ================= helpers ================= */

    private RegisterPayload buildPayload(String username,String password) {
        String test = "test";
        RegisterPayload p = new RegisterPayload(username, password, test, test, "0", test, test);
        p.setRole("Customer");
        p.setEmail(test);
        return p;
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

    private void deleteUserByUsername(String username) throws SQLException {
        // Use DbManager.getConnection() if you have it; otherwise adapt to your DbManager API.
        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM Users WHERE UserName = ?")) {
            ps.setString(1, username);
            ps.executeUpdate();
        }
    }
}
