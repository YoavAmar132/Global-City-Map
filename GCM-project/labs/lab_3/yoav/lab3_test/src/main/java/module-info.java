module org.example.lab3_test {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.lab3_test to javafx.fxml;
    exports org.example.lab3_test;
}