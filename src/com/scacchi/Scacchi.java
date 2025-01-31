/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi;

import com.scacchi.controller.FunctionsController;
import com.scacchi.model.TCP.Settings;
import com.scacchi.model.Traduzioni.ListaLingue;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author Pietro
 */
public class Scacchi extends Application {

    public static final int x = 6;

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("view/MenuPrincipale.fxml"));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.getIcons().add(new Image("com/scacchi/view/img/icon.png"));
        stage.setOnCloseRequest((event) ->
        {
            try
            {
                if (Settings.threadAccetta != null && Settings.threadAccetta.isAlive())
                    Settings.threadAccetta.close();
                if (Settings.threadRicevi != null && Settings.threadRicevi.isAlive())
                    Settings.threadRicevi.close();
            }
            catch (IOException ex)
            {
                Platform.runLater(() -> FunctionsController.alertErrore("Errore nella chiusura della finestra"));
                event.consume();
            }
        });
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        Settings.lingue = new ListaLingue();
        launch(args);
    }
}
