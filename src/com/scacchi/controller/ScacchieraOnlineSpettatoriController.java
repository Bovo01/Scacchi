/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi.controller;

import com.scacchi.model.Partita;
import com.scacchi.model.Pezzo;
import static com.scacchi.model.Pezzo.Colore.*;
import com.scacchi.model.TCP.Settings;
import com.scacchi.model.TCP.ThreadRicevi;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 *
 * @author Pietro
 */
public class ScacchieraOnlineSpettatoriController extends ScacchieraController implements Initializable {
	
	@FXML
	private AnchorPane anchorPane;
	
	private ThreadRicevi thread;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		graphics = canvas.getGraphicsContext2D();
		partita = Settings.partita;
		scala = SCACCHIERA_DIM / canvas.getWidth();
		versoScacchiera = Settings.schieramento;
		if(versoScacchiera == NERO)
			scacchiera.setImage(SCACCHIERA_INV);
		if(!Settings.threadRicevi.isAlive())
			Settings.threadRicevi.start();
		Settings.scacchieraOnlineSpettatoriController = this;
		mostraScacchi();
	}
	
	@FXML
	@Override
	protected void menu(ActionEvent event) {
		try
		{
			Settings.player.close();
			Settings.threadRicevi.close();
		}
		catch (IOException ex)
		{
			FunctionsController.alertErrore(ex.getMessage());
			return;
		}
		Settings.player = null;
		Settings.playerReader = null;
		Settings.playerWriter = null;
		
		Stage stage = (Stage) anchorPane.getScene().getWindow();
		Scene scene = stage.getScene();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/scacchi/view/MenuPrincipale.fxml"));
		try
		{
			Parent root = (Parent) fxmlLoader.load();
			stage.setHeight(400 + 39);//height + 39
			stage.setWidth(600 + 16);//width + 16
			scene.setRoot(root);
		}
		catch (IOException ex)
		{
		}
		Settings.scacchieraOnlineSpettatoriController = null;
	}

	public void ricomincia() {
		Settings.partita = new Partita();
		partita = Settings.partita;
		mostraScacchi();
	}

	public void finePartita(String come, Pezzo.Colore vincitore) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Fine partita!");
		String line = "Partita finita per " + come;
		if (vincitore != null)
			line += "\nVincitore: " + vincitore.toString();
		alert.setContentText(line);
		alert.show();
		Settings.partita.fine();
		mostraScacchi();
	}
}
