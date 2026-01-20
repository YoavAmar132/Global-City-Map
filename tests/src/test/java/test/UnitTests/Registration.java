package test.UnitTests;

import common.messages.RegisterPayload;
import common.model.User;
import gcm.server.data.DbManager;
import gcm.server.data.UserRepo;
import gcm.server.service.AuthService;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

 class Registration {
     private UserRepo userRepo=new UserRepo();
     private AuthService authService=new AuthService(userRepo);
     @Test
     void registrationTest()
     {
         String test="test";

         RegisterPayload tester=new RegisterPayload("Adam123","Adam199*",test,test,"0",test,test);
         tester.setRole("Customer");
         tester.setEmail(test);
         User result = null;
         try {
             DbManager.init("jdbc:mysql://localhost:3306/GCM_DB","root","Yoavamar132!");
             result = authService.register(tester);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }

         assertNotNull(result);
     }

}
