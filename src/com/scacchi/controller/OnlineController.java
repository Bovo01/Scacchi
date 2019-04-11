/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi.controller;

import com.scacchi.model.Pezzo.Colore;
import com.scacchi.model.TCP.Settings;
import com.scacchi.model.TCP.ThreadAccetta;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.application.Platform;
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

	private ThreadAccetta threadAccetta;

	private void disattivaTutto() {
		tabEsci.setOnSelectionChanged((event) -> event.consume());
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
		disattivaTutto();
		new ThreadSend(ipv4_1.getText(), Settings.DEFAULTPORT, this, "richiesta").start();
	}

	@FXML
	private void cercaConPorta(ActionEvent event) throws IOException {
		if (ipv4_2.getText().equals("") || ipv4_2_port.getText().equals(""))
		{
			FunctionsController.alertErrore("Riempi tutti i campi!");
			return;
		}
		disattivaTutto();
		new ThreadSend(ipv4_2.getText(), Integer.parseInt(ipv4_2_port.getText()), this, "richiesta").start();
	}

	@FXML
	private void esci(Event event) {
		Stage stage = (Stage) anchorPane.getScene().getWindow();
		Scene scene = stage.getScene();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/scacchi/view/MenuPrincipale.fxml"));
		try
		{
			Parent root = (Parent) fxmlLoader.load();
			scene.setRoot(root);
		}
		catch (IOException ex)
		{
			FunctionsController.alertErrore(ex.getMessage());
		}
	}

	public Colore scegliSchieramento() {
		Alert alert = new Alert(Alert.AlertType.NONE);
		alert.setTitle("Scegli");
		alert.setContentText("Quale schieramento preferisci?");
		ButtonType BIANCHI = new ButtonType("Bianchi");
		ButtonType NERI = new ButtonType("Neri");
		ButtonType NIENTE = new ButtonType("Non cambia nulla");
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
		if (threadAccetta != null)
			try
			{
				threadAccetta.close();
				threadAccetta = null;
				Settings.thread = null;
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
		threadAccetta = new ThreadAccetta(Settings.DEFAULTPORT, this);
		Settings.thread = threadAccetta;
		threadAccetta.start();
	}
	
	@FXML
	private void guardaConIp(ActionEvent event) {
		if(ipv4_1_spect.getText().equals(""))
		{
			FunctionsController.alertErrore("Inserisci l'indirizzo del giocatore");
			return;
		}
		disattivaTutto();
		new ThreadSend(ipv4_1_spect.getText(), Settings.DEFAULTPORT, this, "richiesta spettatore").start();
	}
	
	@FXML
	private void guardaConPorta(ActionEvent event) {
		if(ipv4_2_spect.getText().equals("") || ipv4_2_port_spect.getText().equals(""))
		{
			FunctionsController.alertErrore("Riempi tutti i campi");
			return;
		}
		disattivaTutto();
		new ThreadSend(ipv4_1_spect.getText(), Integer.parseInt(ipv4_2_port_spect.getText()), this, "richiesta spettatore").start();
	}
	
	@FXML
	private void hostConPorta(ActionEvent event) {
		if(host_port.getText().equals(""))
		{
			FunctionsController.alertErrore("Inserisci la porta su cui aprire la partita");
			return;
		}
		if (threadAccetta != null)
			try
			{
				sbloccaTutto();
				threadAccetta.close();
				threadAccetta = null;
				Settings.thread = null;
				FunctionsController.alertInfo("Ricerca annullata", "Hai annullato la ricerca.\nOra nessuno può più connettersi a te");
				return;
			}
			catch (IOException ex)
			{
				FunctionsController.alertErrore("Errore");
				return;
			}
		disattivaTutto();
		btnHost2.setDisable(false);
		threadAccetta = new ThreadAccetta(Integer.parseInt(host_port.getText()), this);
		Settings.thread = threadAccetta;
		threadAccetta.start();
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
		tabEsci.setOnSelectionChanged((event) -> esci(event));
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

	private class ThreadSend extends Thread {

		private final String address;
		private final int port;
		private final OnlineController controller;
		private final String message;

		public ThreadSend(String address, int port, OnlineController controller, String message) {
			this.address = address;
			this.port = port;
			this.controller = controller;
			this.message = message;
		}

		@Override
		public void run() {
			try
			{
				Socket socket = new Socket();
				socket.connect(new InetSocketAddress(address, port), 500);

				InputStream is = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);

				OutputStream os = socket.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(os);
				BufferedWriter bw = new BufferedWriter(osw);

				bw.write(message);
				bw.newLine();
				bw.flush();

				String line = br.readLine();
				if(line.equals("richiesta accettata"))
				{
					line = br.readLine();
					if (line.equals("bianco") || line.equals("nero"))
					{
						Settings.player = socket;
						Settings.playerReader = br;
						Settings.playerWriter = bw;
						Settings.thread = new ThreadAccetta(port, controller);
						Settings.thread.start();
						if (line.equals("bianco"))
							Settings.schieramento = Colore.BIANCO;
						else if (line.equals("nero"))
							Settings.schieramento = Colore.NERO;
					}
					else if(line.equals("richiesta colore"))
					{
						Platform.runLater(() -> {
							try
							{
								Settings.schieramento = scegliSchieramento();
								if(Settings.schieramento == null)
									Settings.schieramento = Colore.values()[Math.abs(new Random().nextInt()) % 2];
								if(Settings.schieramento == Colore.BIANCO)
									bw.write("nero\n");
								else if(Settings.schieramento == Colore.NERO)
									bw.write("bianco\n");
								bw.flush();
							}
							catch (IOException ex)
							{
								FunctionsController.alertErrore("È avvenuto un problema nella connessione");
							}
						});
					}
				}
			}
			catch (IOException ex)
			{
				Platform.runLater(() -> FunctionsController.alertErrore("È avvenuto un problema nella connessione"));
			}
			sbloccaTutto();
		}
	}
}
