/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi.controller;

import com.scacchi.model.Pezzo.Colore;
import com.scacchi.model.TCP.Settings;
import com.scacchi.model.TCP.ThreadAccetta;
import com.scacchi.model.TCP.ThreadSend;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Pietro
 */
public class OnlineController implements Initializable {

	@FXML
	private AnchorPane anchorPane;
	//Ip e porte
	@FXML
	private TextField ipv4_1;
	@FXML
	private TextField ipv4_2;
	@FXML
	private TextField ipv4_2_port;
	@FXML
	private TextField host_port;
	@FXML
	private TextField ipv4_1_spect;
	@FXML
	private TextField ipv4_2_spect;
	@FXML
	private TextField ipv4_2_port_spect;
	//Tutti gli elementi che andranno disattivati
	//Tab
	@FXML
	private Tab tabEsci;
	//Bottoni
	@FXML
	private Button btnCerca1;
	@FXML
	private Button btnCerca2;
	@FXML
	private Button btnCerca3;
	@FXML
	private Button btnHost1;
	@FXML
	private Button btnHost2;
	@FXML
	private Button btnSpect1;
	@FXML
	private Button btnSpect2;

	private void disattivaTutto() {
		btnCerca1.setDisable(true);
		btnCerca2.setDisable(true);
		btnCerca3.setDisable(true);
		btnHost1.setDisable(true);
		btnHost2.setDisable(true);
		btnSpect1.setDisable(true);
		btnSpect2.setDisable(true);
	}

	@FXML
	private void cercaConIp(ActionEvent event) {
		if (ipv4_1.getText().equals(""))
		{
			FunctionsController.alertErrore("Riempi tutti i campi!");
			return;
		}
		disattivaTutto();//TODO togli il commento
//		try
//		{
//			ServerSocket server = new ServerSocket(Settings.DEFAULTPORT);
//			server.close();
			new ThreadSend(ipv4_1.getText(), Settings.DEFAULTPORT, this, "richiesta").start();
//		}
//		catch (IOException ex)
//		{
//			sbloccaTutto();
//			FunctionsController.alertErrore("Non puoi connetterti a questa porta");
//		}
	}

	@FXML
	private void cercaConPorta(ActionEvent event) throws IOException {
		if (ipv4_2.getText().equals("") || ipv4_2_port.getText().equals(""))
		{
			FunctionsController.alertErrore("Riempi tutti i campi!");
			return;
		}
		disattivaTutto();
		try
		{
			ServerSocket server = new ServerSocket(Integer.parseInt(ipv4_2_port.getText()));
			server.close();
			new ThreadSend(ipv4_2.getText(), Integer.parseInt(ipv4_2_port.getText()), this, "richiesta").start();
		}
		catch (IOException ex)
		{
			sbloccaTutto();
			FunctionsController.alertErrore("Non puoi connetterti a questa porta");
		}
	}

	@FXML
	private void esci(Event event) {
		Stage stage = (Stage) anchorPane.getScene().getWindow();
		Scene scene = stage.getScene();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/scacchi/view/MenuPrincipale.fxml"));
		try
		{
			Parent root = (Parent) fxmlLoader.load();
			if(Settings.threadAccetta != null)
			{
				Settings.threadAccetta.close();
				Settings.threadAccetta = null;
			}
			scene.setRoot(root);
		}
		catch (IOException ex)
		{
			FunctionsController.alertErrore(ex.getMessage());
		}
	}

	public Colore scegliSchieramento(String line) {
		Alert alert = new Alert(Alert.AlertType.NONE);
		alert.setTitle("Scegli");
		alert.setContentText("Quale schieramento preferisci?");
		ButtonType BIANCHI = new ButtonType("Bianchi");
		ButtonType NERI = new ButtonType("Neri");
		ButtonType NIENTE = new ButtonType(line);
		alert.getButtonTypes().clear();
		alert.getButtonTypes().addAll(BIANCHI, NERI, NIENTE);
		Optional<ButtonType> option = alert.showAndWait();
		if (option.get() == BIANCHI)
			return Colore.BIANCO;
		if (option.get() == NERI)
			return Colore.NERO;
		return null;
	}

	@FXML
	private void host(ActionEvent event) {
		if (Settings.threadAccetta != null)
			try
			{
				Settings.threadAccetta.close();
				btnHost1.setText("Host partita");
				Settings.threadAccetta = null;
				sbloccaTutto();
				FunctionsController.alertInfo("Ricerca annullata", "Hai annullato la ricerca.\nOra nessuno può più connettersi a te");
				return;
			}
			catch (IOException ex)
			{
				FunctionsController.alertErrore("Errore");
				return;
			}
		disattivaTutto();
		btnHost1.setDisable(false);
		btnHost1.setText("Annulla ricerca");
		Settings.threadAccetta = new ThreadAccetta(Settings.DEFAULTPORT, this);
		Settings.threadAccetta.start();
	}
	
	@FXML
	private void cerca(ActionEvent event) {//TODO cerca in tutta la rete
		FunctionsController.alertInfo("TODO", "TODO");
	}

	@FXML
	private void guardaConIp(ActionEvent event) {
		if (ipv4_1_spect.getText().equals(""))
		{
			FunctionsController.alertErrore("Inserisci l'indirizzo del giocatore");
			return;
		}
		disattivaTutto();
		try
		{
			new ThreadSend(ipv4_1_spect.getText(), Settings.DEFAULTPORT, this, "richiesta spettatore").start();
		}
		catch(NumberFormatException ex)
		{
			FunctionsController.alertErrore("La porta deve essere un numero");
		}
	}

	@FXML
	private void guardaConPorta(ActionEvent event) {
		if (ipv4_2_spect.getText().equals("") || ipv4_2_port_spect.getText().equals(""))
		{
			FunctionsController.alertErrore("Riempi tutti i campi");
			return;
		}
		disattivaTutto();
		try
		{
			new ThreadSend(ipv4_1_spect.getText(), Integer.parseInt(ipv4_2_port_spect.getText()), this, "richiesta spettatore").start();
		}
		catch(NumberFormatException ex)
		{
			FunctionsController.alertErrore("La porta deve essere un numero");
		}
	}

	@FXML
	private void hostConPorta(ActionEvent event) {
		if (host_port.getText().equals(""))
		{
			FunctionsController.alertErrore("Inserisci la porta su cui aprire la partita");
			return;
		}
		if (Settings.threadAccetta != null)
			try
			{
				Settings.threadAccetta.close();
				btnHost2.setText("Host partita");
				Settings.threadAccetta = null;
				sbloccaTutto();
				FunctionsController.alertInfo("Ricerca annullata", "Hai annullato la ricerca.\nOra nessuno può più connettersi a te");
				anchorPane.getScene().getWindow().setOnCloseRequest((event2) ->
				{
				});
				return;
			}
			catch (IOException ex)
			{
				FunctionsController.alertErrore("Errore");
				return;
			}
		try
		{
			Settings.threadAccetta = new ThreadAccetta(Integer.parseInt(host_port.getText()), this);
		}
		catch(NumberFormatException ex)
		{
			FunctionsController.alertErrore("La porta deve essere un numero");
			return;
		}
		disattivaTutto();
		btnHost2.setDisable(false);
		btnHost2.setText("Annulla ricerca");
		Settings.threadAccetta.start();
	}

	public void inizioPartita() {
		Stage stage = (Stage) anchorPane.getScene().getWindow();
		Scene scene = stage.getScene();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/scacchi/view/ScacchieraOnline.fxml"));
		try
		{
			Parent root = (Parent) fxmlLoader.load();
			anchorPane.getScene().getWindow().setHeight(598 + 39);//height + 39
			anchorPane.getScene().getWindow().setWidth(748 + 16);//width + 16
			scene.setRoot(root);
		}
		catch (IOException ex)
		{
		}
	}

	public void sbloccaTutto() {
		btnCerca1.setDisable(false);
		btnCerca2.setDisable(false);
		btnCerca3.setDisable(false);
		btnHost1.setDisable(false);
		btnHost2.setDisable(false);
		btnSpect1.setDisable(false);
		btnSpect2.setDisable(false);
	}

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {

	}

	public void inizioSpettatore() {
		Stage stage = (Stage) anchorPane.getScene().getWindow();
		Scene scene = stage.getScene();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/scacchi/view/ScacchieraOnlineSpettatori.fxml"));
		try
		{
			Parent root = (Parent) fxmlLoader.load();
			scene.getWindow().setHeight(598 + 39);//height + 39
			scene.getWindow().setWidth(748 + 16);//width + 16
			scene.setRoot(root);
		}
		catch (IOException ex)
		{
		}
	}
}
