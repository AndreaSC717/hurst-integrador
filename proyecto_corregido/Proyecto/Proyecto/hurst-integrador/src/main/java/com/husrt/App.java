package com.husrt;

import java.io.IOException;

import com.husrt.db.DataSourceManager;
import com.husrt.db.DemoUsersBootstrap;
import com.husrt.ui.UiStyles;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * HUSRT-Control — JavaFX entry point.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        DemoUsersBootstrap.ensure();
        Parent root = FXMLLoader.load(App.class.getResource("/com/husrt/ui/login/login.fxml"));
        Scene scene = new Scene(root, 520, 720);
        UiStyles.apply(scene);
        stage.setTitle("HUSRT-Control — Login");
        stage.setMinWidth(800);
        stage.setMinHeight(480);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        DataSourceManager.shutdown();
    }

    public static void main(String[] args) {
        launch();
    }
}
