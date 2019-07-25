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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * FXML Controller class
 *
 * @author Pietro
 */
public class OnlineController implements Initializable {

	@FXML
	private Tab tabCerca;
	@FXML
	private Tab tabHost;
	@FXML
	private Tab tabSpect;
	@FXML
	private Label cercaIntelligente;
	@FXML
	private Label cercaConIndirizzo;
	@FXML
	private Label cercaConPort;
	@FXML
	private Label hostIntelligente;
	@FXML
	private Label hostConPort;
	@FXML
	private Label guardaIntelligente;
	@FXML
	private Label guardaConIndirizzo;
	@FXML
	private Label guardaConPort;

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
	public Button btnSpect;
	@FXML
	public Button btnSpect1;
	@FXML
	public Button btnSpect2;

	public void disattivaTutto() {
		btnCerca1.setDisable(true);
		btnCerca2.setDisable(true);
		btnCerca3.setDisable(true);
		btnHost1.setDisable(true);
		btnHost2.setDisable(true);
		btnSpect.setDisable(true);
		btnSpect1.setDisable(true);
		btnSpect2.setDisable(true);
	}

	@FXML
	private void cercaConIp(ActionEvent event) {
		JSONObject translator = Settings.lingue.getJSONObject("onlineMenu");
		if (ipv4_1.getText().equals(""))
		{
			FunctionsController.alertErrore("riempiCampi");
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
//			FunctionsController.alertErrore("nonPuoiConnetterti");
//		}
	}

	@FXML
	private void cercaConPorta(ActionEvent event) throws IOException {
		if (ipv4_2.getText().equals("") || ipv4_2_port.getText().equals(""))
		{
			FunctionsController.alertErrore("riempiCampi");
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
			FunctionsController.alertErrore("nonPuoiConnetterti");
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
			if (Settings.threadAccetta != null)
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

	public Colore scegliSchieramento(String message) {
		try
		{
			JSONObject jsonObj = Settings.lingue.getJSONObject("messaggi");
			JSONObject titolo = jsonObj.getJSONObject("titolo");
			JSONObject contenuto = jsonObj.getJSONObject("contenuto");
			JSONObject colori = Settings.lingue.getJSONObject("partita").getJSONObject("colore");

			Alert alert = new Alert(Alert.AlertType.NONE);
			alert.setTitle(titolo.getString("scegli"));
			alert.setContentText(contenuto.getString("schieramentoPreferito"));
			ButtonType BIANCHI = new ButtonType(colori.getString("bianco"));
			ButtonType NERI = new ButtonType(colori.getString("nero"));
			ButtonType NIENTE = new ButtonType(contenuto.getString(message));
			alert.getButtonTypes().clear();
			alert.getButtonTypes().addAll(BIANCHI, NERI, NIENTE);
			Optional<ButtonType> option = alert.showAndWait();
			if (option.get() == BIANCHI)
				return Colore.BIANCO;
			if (option.get() == NERI)
				return Colore.NERO;
		}
		catch (JSONException ex)
		{
			Logger.getLogger(OnlineController.class.getName()).log(Level.SEVERE, null, ex); //Tanto è inutile
		}
		return null;
	}

	@FXML
	private void host(ActionEvent event) {
		JSONObject translator = Settings.lingue.getJSONObject("onlineMenu");
		if (Settings.threadAccetta != null)
			try
			{
				Settings.threadAccetta.close();
				btnHost1.setText(translator.getString("host"));
				Settings.threadAccetta = null;
				sbloccaTutto();
				FunctionsController.alertInfo("ricercaAnnullata", "haiAnnullatoRicerca");
				return;
			}
			catch (IOException | JSONException ex)
			{
				FunctionsController.alertErrore("errore");
				return;
			}
		disattivaTutto();
		btnHost1.setDisable(false);
		try
		{
			btnHost1.setText(translator.getString("annullaRicerca"));
		}
		catch (JSONException ex)
		{
			Logger.getLogger(OnlineController.class.getName()).log(Level.SEVERE, null, ex);//Non darà problemi
		}
		Settings.threadAccetta = new ThreadAccetta(Settings.DEFAULTPORT, this);
		Settings.threadAccetta.start();
	}

	@FXML
	private void cerca(ActionEvent event) {
		try
		{
			JSONObject translator = Settings.lingue.getJSONObject("onlineMenu");
			disattivaTutto();
			try
			{
				ServerSocket server = new ServerSocket(Settings.DEFAULTPORT);
				server.close();
			}
			catch (IOException ex)
			{
				sbloccaTutto();
				FunctionsController.alertErrore("nonPuoiConnetterti");
				return;
			}
			String net = getReteOfMe();
			ListView<String> listView = new ListView<>();
			listView.setPrefHeight(300);
			for (int i = 1; i <= 254; i++)
			{
				new ThreadSend(net + i, Settings.DEFAULTPORT, this, "niente", listView).start();
			}
			Alert alert = new Alert(Alert.AlertType.NONE);
			ButtonType OK = new ButtonType(Settings.lingue.getString("ok"));
			ButtonType ANNULLA = new ButtonType(Settings.lingue.getString("annulla"));
			alert.getButtonTypes().addAll(OK, ANNULLA);
			alert.setTitle(translator.getString("connettiti"));
			alert.setContentText(translator.getString("selezionaGiocatore"));
			alert.getDialogPane().setContent(listView);
			Optional<ButtonType> scelta = alert.showAndWait();
			if (scelta.get() == ANNULLA)
			{
				FunctionsController.alertInfo("operazioneAnnullata", "operazioneAnnullata");
				return;
			}
			if (listView.getSelectionModel().getSelectedIndex() == -1)
			{
				FunctionsController.alertErrore("noAvversarioSelezionato");
				return;
			}
			new ThreadSend(listView.getSelectionModel().getSelectedItem(), Settings.DEFAULTPORT, this, "richiesta").start();
		}
		catch (JSONException ex)
		{
			//Non passerà mai
		}
	}

	private static String getReteOfMe() {
		String line;
		try
		{
			line = InetAddress.getLocalHost().toString();
		}
		catch (UnknownHostException ex)
		{
			return "";
		}
		line = line.substring(line.indexOf('/') + 1);
		int punti = 0, counter = 0;
		for (char c : line.toCharArray())
		{
			if (punti != 3)
				counter++;
			if (c == '.')
				punti++;
		}
		return line.substring(0, counter);
	}

	@FXML
	public void guardaConIp(ActionEvent event) {
		if (ipv4_1_spect.getText().equals(""))
		{
			FunctionsController.alertErrore("inserisciIndirizzo");
			return;
		}
		disattivaTutto();
		try
		{
			new ThreadSend(ipv4_1_spect.getText(), Settings.DEFAULTPORT, this, "richiesta spettatore", btnSpect1).start();
		}
		catch (NumberFormatException ex)
		{
			FunctionsController.alertErrore("portaNumero");
			sbloccaTutto();
		}
	}

	@FXML
	public void guarda(ActionEvent event) {
		try
		{
			disattivaTutto();
			btnSpect.setDisable(false);//TODO togli il commento
//		try
//		{
//			ServerSocket server = new ServerSocket(Settings.DEFAULTPORT);
//			server.close();
//		}
//		catch (IOException ex)
//		{
//			sbloccaTutto();
//			FunctionsController.alertErrore("nonPuoiConneterti");
//			return;
//		}
			String net = getReteOfMe();
			if (net.equals("127.0.0."))
			{
				FunctionsController.alertErrore("nonSeiConnesso");
				return;
			}
			ListView<String> listView = new ListView<>();
			listView.setPrefHeight(300);
			for (int i = 1; i <= 254; i++)
			{
				new ThreadSend(net + i, Settings.DEFAULTPORT, this, "niente", listView).start();
			}
			JSONObject onlineMenu = Settings.lingue.getJSONObject("onlineMenu");
			Alert alert = new Alert(Alert.AlertType.NONE);
			ButtonType OK = new ButtonType(Settings.lingue.getString("ok"));
			ButtonType ANNULLA = new ButtonType(Settings.lingue.getString("annulla"));
			alert.getButtonTypes().addAll(OK, ANNULLA);
			alert.setTitle(onlineMenu.getString("guarda"));
			alert.setContentText(onlineMenu.getString("selezionaGiocatore"));
			alert.getDialogPane().setContent(listView);
			Optional<ButtonType> scelta = alert.showAndWait();
			if (scelta.get() == ANNULLA)
			{
				FunctionsController.alertInfo("operazioneAnnullata", "operazioneAnnullata");
				return;
			}
			if (listView.getSelectionModel().getSelectedIndex() == -1)
			{
				FunctionsController.alertErrore("noGiocatoreSelezionato");
				return;
			}
			new ThreadSend(listView.getSelectionModel().getSelectedItem(), Settings.DEFAULTPORT, this, "richiesta spettatore", btnSpect).start();
		}
		catch (JSONException ex)
		{
			Logger.getLogger(OnlineController.class.getName()).log(Level.SEVERE, null, ex);//Non servirà
		}
	}

	@FXML
	public void guardaConPorta(ActionEvent event) {
		if (ipv4_2_spect.getText().equals("") || ipv4_2_port_spect.getText().equals(""))
		{
			FunctionsController.alertErrore("riempiCampi");
			return;
		}
		disattivaTutto();
		try
		{
			new ThreadSend(ipv4_1_spect.getText(), Integer.parseInt(ipv4_2_port_spect.getText()), this, "richiesta spettatore", btnSpect2).start();
		}
		catch (NumberFormatException ex)
		{
			FunctionsController.alertErrore("portaNumero");
			sbloccaTutto();
		}
	}

	@FXML
	private void hostConPorta(ActionEvent event) {
		if (host_port.getText().equals(""))
		{
			FunctionsController.alertErrore("portaDaAprire");
			return;
		}
		JSONObject onlineMenu = Settings.lingue.getJSONObject("onlineMenu");
		if (Settings.threadAccetta != null)
			try
			{
				Settings.threadAccetta.close();
				btnHost2.setText(onlineMenu.getString("host"));
				Settings.threadAccetta = null;
				sbloccaTutto();
				FunctionsController.alertInfo("ricercaAnnullata", "haiAnnullatoRicerca");
				anchorPane.getScene().getWindow().setOnCloseRequest((event2) ->
				{
				});
				return;
			}
			catch (IOException|JSONException ex)
			{
				FunctionsController.alertErrore("Errore");
				return;
			}
		try
		{
			Settings.threadAccetta = new ThreadAccetta(Integer.parseInt(host_port.getText()), this);
		}
		catch (NumberFormatException ex)
		{
			FunctionsController.alertErrore("portaNumero");
			return;
		}
		disattivaTutto();
		btnHost2.setDisable(false);
		try
		{
			btnHost2.setText(onlineMenu.getString("annullaRicerca"));
		}
		catch (JSONException ex)
		{
			Logger.getLogger(OnlineController.class.getName()).log(Level.SEVERE, null, ex);//Non servirà
		}
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
		btnSpect.setDisable(false);
		btnSpect1.setDisable(false);
		btnSpect2.setDisable(false);
	}

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		try
		{
			traduciTutto(); //Non avrà mai problemi
		}
		catch (JSONException ex)
		{
			Logger.getLogger(OnlineController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void traduciTutto() throws JSONException {
		JSONObject jsonObj = Settings.lingue.getJSONObject("onlineMenu");
		//Tab "Cerca partita"
		tabCerca.setText(jsonObj.getString("cerca"));
		btnCerca1.setText(jsonObj.getString("cerca"));
		btnCerca2.setText(jsonObj.getString("cerca"));
		btnCerca3.setText(jsonObj.getString("cerca"));
		cercaIntelligente.setText(jsonObj.getString("cercaIntelligente"));
		cercaConIndirizzo.setText(jsonObj.getString("cercaConIndirizzo"));
		cercaConPort.setText(jsonObj.getString("cercaConPort"));
		ipv4_1.setPromptText(jsonObj.getString("cercaConIndirizzoPlaceholder"));
		ipv4_2.setPromptText(jsonObj.getString("cercaConIndirizzoPlaceholder"));
		ipv4_2_port.setPromptText(jsonObj.getString("porta"));
		//Tab "Host partita"
		tabHost.setText(jsonObj.getString("host"));
		btnHost1.setText(jsonObj.getString("host"));
		btnHost2.setText(jsonObj.getString("host"));
		hostIntelligente.setText(jsonObj.getString("hostIntelligente"));
		hostConPort.setText(jsonObj.getString("hostConPorta"));
		host_port.setPromptText(jsonObj.getString("porta"));
		//Tab "Spettatore"
		tabSpect.setText(jsonObj.getString("spect"));
		btnSpect.setText(jsonObj.getString("guarda"));
		btnSpect1.setText(jsonObj.getString("guarda"));
		btnSpect2.setText(jsonObj.getString("guarda"));
		guardaIntelligente.setText(jsonObj.getString("guardaIntelligente"));
		guardaConIndirizzo.setText(jsonObj.getString("guardaConIndirizzo"));
		guardaConPort.setText(jsonObj.getString("guardaConPorta"));
		ipv4_1_spect.setPromptText(jsonObj.getString("guardaPlaceholder"));
		ipv4_2_spect.setPromptText(jsonObj.getString("guardaPlaceholder"));
		ipv4_2_port_spect.setPromptText(jsonObj.getString("porta"));
		//Tab "Torna al menu"
		tabEsci.setText(jsonObj.getString("esci"));
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
