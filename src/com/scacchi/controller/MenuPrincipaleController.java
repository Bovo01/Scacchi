/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi.controller;

import com.scacchi.model.TCP.Settings;
import com.scacchi.model.Traduzioni.Lingua;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * FXML Controller class
 *
 * @author Pietro
 */
public class MenuPrincipaleController implements Initializable {

	@FXML
	private ToggleGroup gioco;
	@FXML
	private Label scacchiLabel;
	@FXML
	private RadioButton multigiocatoreOfflineLabel;
	@FXML
	private RadioButton multigiocatoreOnlineLabel;
	@FXML
	private ComboBox<Lingua> lingueCombo;
	@FXML
	private Button giocaButton;

	@FXML
	private void modalita(ActionEvent event) {
		if (gioco.getSelectedToggle() == null)
		{
			Alert alert = new Alert(Alert.AlertType.ERROR);
			try
			{
				alert.setContentText(Settings.lingue.getJSONObject("menu").getString("selezionaOpzione"));
			}
			catch (JSONException ex)
			{
				Logger.getLogger(MenuPrincipaleController.class.getName()).log(Level.SEVERE, null, ex);//Non servirà
			}
			alert.showAndWait();
			return;
		}
		if ((RadioButton) gioco.getSelectedToggle() == multigiocatoreOfflineLabel)
		{
			Node node = (Node) event.getSource();
			Stage stage = (Stage) node.getScene().getWindow();
			Scene scene = stage.getScene();
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/scacchi/view/Scacchiera.fxml"));
			try
			{
				Parent root = (Parent) fxmlLoader.load();
				node.getScene().getWindow().setHeight(598 + 39);//height + 39
				node.getScene().getWindow().setWidth(748 + 16);//width + 16
				scene.setRoot(root);
			}
			catch (IOException ex)
			{
			}
		}
		else
		{
			Node node = (Node) event.getSource();
			Stage stage = (Stage) node.getScene().getWindow();
			Scene scene = stage.getScene();
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/scacchi/view/Online.fxml"));
			try
			{
				Parent root = (Parent) fxmlLoader.load();
				scene.setRoot(root);
			}
			catch (IOException ex)
			{
			}
		}
	}

	@FXML
	private void cambiaLingua(ActionEvent event) {
		if (!Settings.lingue.setLinguaCaricata(lingueCombo.getSelectionModel().getSelectedItem()))
			return;
		try
		{
			traduciTutto();
		}
		catch (JSONException ex)
		{
			Logger.getLogger(MenuPrincipaleController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void traduciTutto() throws JSONException {
		JSONObject jsonObj = Settings.lingue.getJSONObject("menu");
		scacchiLabel.setText(jsonObj.getString("scacchi"));
		multigiocatoreOfflineLabel.setText(jsonObj.getString("multigiocatoreOffline"));
		multigiocatoreOnlineLabel.setText(jsonObj.getString("multigiocatoreOnline"));
		giocaButton.setText(jsonObj.getString("gioca"));
	}

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		lingueCombo.getItems().addAll(Settings.lingue.getLingue());
		lingueCombo.getSelectionModel().select(Settings.lingue.getLinguaCaricata());
		try
		{
			traduciTutto();
		}
		catch (JSONException ex)
		{
			Logger.getLogger(MenuPrincipaleController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
