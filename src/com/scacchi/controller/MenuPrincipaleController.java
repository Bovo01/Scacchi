/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Pietro
 */
public class MenuPrincipaleController implements Initializable {

	@FXML
	private ToggleGroup gioco;

	@FXML
	private void modalita(ActionEvent event) {
		if (gioco.getSelectedToggle() == null)
		{
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setContentText("Seleziona un'opzione");
			alert.showAndWait();
			return;
		}
		if (((RadioButton) gioco.getSelectedToggle()).getText().equals("Multigiocatore locale"))
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

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		
	}

}
