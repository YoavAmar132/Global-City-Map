package gcm.server.controllers;

import common.messages.GcmRequest;
import common.messages.GcmResponse;
import common.messages.RequestType;
import common.messages.LoginPayload;
import common.model.User;
import gcm.server.service.AuthService;

import java.sql.SQLException;

public class RequestHandler {

    private final AuthService authService;


    public RequestHandler(AuthService authService) {
        this.authService = authService;

    }

    /**
     * Main entry point for handling a request from a client.
     * For now, we only support LOGIN.
     */
    public GcmResponse handle(GcmRequest request) {

        RequestType type = request.getType();

        if (type == RequestType.LOGIN) {
            try {
                return handleLogin(request);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (type == RequestType.REGISTER) {
            try {
                return handleRegistration(request);
            } catch (SQLException e) {
                throw new RuntimeException("failed to register user", e);
            }
        }

        // later you'll add more cases for other RequestTypes
        return GcmResponse.error("Unsupported request type: " + type);
    }
    /**
     * Handles LOGIN requests.
     * Expects payload = LoginPayload
     * Returns: GcmResponse.ok(User) on success, or GcmResponse.error(...) on failure.
     */
    private GcmResponse handleLogin(GcmRequest request) throws SQLException {
        System.out.println("login request received");
        // 1. Validate and cast payload
        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof LoginPayload payload)) {
            return GcmResponse.error("Invalid payload for LOGIN request");
        }

        String username = payload.getUsername();
        String password = payload.getPassword();

        // 2. Delegate to AuthService to check DB / users list
        User user = authService.login(username, password);

        // 3. Handle failure
        if (user == null) {
            return GcmResponse.error("Invalid username or password");
        }

        // 4. Success → return the User directly
        return GcmResponse.ok(user);
    }

    //registration handler
    private GcmResponse handleRegistration(GcmRequest request) throws SQLException {
        System.out.println("registration request received");
        // 1. Validate and cast payload
        Object rawPayload = request.getPayload();
        if (!(rawPayload instanceof LoginPayload payload)) {
            return GcmResponse.error("Invalid payload for register request");
        }

        String username = payload.getUsername();
        String password = payload.getPassword();

        // 2. Delegate to AuthService to check DB / users list
        User user = authService.register(username,password);

        // 3. Handle failure
        if (user == null) {
            return GcmResponse.error(authService.getErrormsg());
        }

        // 4. Success → return the User directly
        return GcmResponse.ok(user);
    }
}