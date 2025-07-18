package mediaDatabase;/**
 * Author: Jacqueline Kurniawan
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MediaDatabaseApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MediaDatabaseApplication.class.getResource("mainPage-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1085, 698);
        stage.setTitle("Main Page");
        stage.setScene(scene);
        scene.getRoot().setStyle("-fx-font-family: SansSerif");
        stage.show();
    }
}
