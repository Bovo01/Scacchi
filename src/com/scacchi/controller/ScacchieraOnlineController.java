/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi.controller;

import com.scacchi.model.*;
import com.scacchi.model.Pezzo.Colore;
import static com.scacchi.model.Pezzo.Colore.BIANCO;
import static com.scacchi.model.Pezzo.Colore.NERO;
import com.scacchi.model.Posizione.Colonna;
import com.scacchi.model.Posizione.Riga;
import com.scacchi.model.TCP.Settings;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 *
 * @author Pietro
 */
public class ScacchieraOnlineController extends ScacchieraController implements Initializable {

	@FXML
	private AnchorPane anchorPane;
	@FXML
	public Button resa;
	@FXML
	public Button patta;
	@FXML
	private Button restart;

	public void disattivaBottoni() {
		resa.setDisable(true);
		patta.setDisable(true);
		restart.setDisable(true);
	}

	@FXML
	@Override
	protected void menu(ActionEvent event) {
		try
		{
			if(Settings.threadAccetta != null && Settings.threadAccetta.isAlive())
				Settings.threadAccetta.close();
		}
		catch (IOException ex)
		{
			FunctionsController.alertErrore("Non è stato possibile tornare al menu. Per favore riprova");
			return;
		}
		Stage stage = (Stage) anchorPane.getScene().getWindow();
		Scene scene = stage.getScene();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/scacchi/view/MenuPrincipale.fxml"));
		try
		{
			Parent root = (Parent) fxmlLoader.load();
			scene.getWindow().setHeight(400 + 39);
			scene.getWindow().setWidth(600 + 16);
			scene.setRoot(root);
		}
		catch (IOException ex)
		{
			FunctionsController.alertErrore(ex.getMessage());
		}
	}

	@FXML
	@Override
	protected void click(MouseEvent event) {
		double x = event.getSceneX() * scala;
		double y = event.getSceneY() * scala;
		if (x < 48 || x > 1031 || y < 48 || y > 1031)
			return;
		if (Settings.partita.getTurno() != Settings.schieramento)
			return;
		if (pos1 == null)
		{
			if (Settings.schieramento == BIANCO)
				pos1 = new Posizione(Riga.values()[(int) ((y - 48) / 123)], Colonna.values()[(int) ((x - 48) / 123)]);
			else
				pos1 = new Posizione(Riga.values()[7 - (int) ((y - 48) / 123)], Colonna.values()[7 - (int) ((x - 48) / 123)]);
			if (Settings.partita.trovaPezzo(pos1) == null || Settings.partita.trovaPezzo(pos1).getColore() != Settings.partita.getTurno())
			{
				pos1 = null;
				return;
			}
			mostraMosse(Settings.partita.trovaPezzo(pos1));
		}
		else
		{
			Pezzo pezzo = Settings.partita.trovaPezzo(pos1);
			if (Settings.schieramento == BIANCO)
				pos2 = new Posizione(Riga.values()[(int) ((y - 48) / 123)], Colonna.values()[(int) ((x - 48) / 123)]);
			else
				pos2 = new Posizione(Riga.values()[7 - (int) ((y - 48) / 123)], Colonna.values()[7 - (int) ((x - 48) / 123)]);
			if (pos1.equals(pos2))
			{
				pos1 = null;
				pos2 = null;
				mostraScacchi();
				return;
			}
			if (pezzo != null)
			{
				if (Settings.partita.trovaPezzo(pos2) != null && pezzo.getColore() == Settings.partita.trovaPezzo(pos2).getColore())
				{
					pos1 = pos2;
					pos2 = null;
					mostraScacchi();
					mostraMosse(Settings.partita.trovaPezzo(pos1));
					return;
				}
				if (!Settings.partita.muovi(pezzo, pos2))
					return;
				if (pezzo.getSimbolo() == Pezzo.Simbolo.PEDONE && (pezzo.getPosizione().getRiga() == Posizione.Riga.R1 || pezzo.getPosizione().getRiga() == Posizione.Riga.R8))
				{
					Alert alert = new Alert(Alert.AlertType.NONE);
					alert.setContentText("Scegli in cosa vuoi trasformare il tuo pedone");
					alert.setTitle("Cambio pezzo");
					ButtonType REGINA = new ButtonType("Regina");
					ButtonType ALFIERE = new ButtonType("Alfiere");
					ButtonType CAVALLO = new ButtonType("Cavallo");
					ButtonType TORRE = new ButtonType("Torre");
					alert.getButtonTypes().addAll(REGINA, ALFIERE, CAVALLO, TORRE);
					Optional<ButtonType> option = alert.showAndWait();
					if (option.get() == REGINA)
						Settings.partita.promozione(pezzo, Pezzo.Simbolo.REGINA);
					else if (option.get() == ALFIERE)
						Settings.partita.promozione(pezzo, Pezzo.Simbolo.ALFIERE);
					else if (option.get() == TORRE)
						Settings.partita.promozione(pezzo, Pezzo.Simbolo.TORRE);
					else if (option.get() == CAVALLO)
						Settings.partita.promozione(pezzo, Pezzo.Simbolo.CAVALLO);
				}
				sendMessage("mossa " + Settings.partita.getMosse().get(Settings.partita.getMosse().size() - 1).toString());
				if (Settings.partita.getTurno() == null)//Se c'è un vincitore il metodo "muovi" imposta il turno a null
				{
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					String line = Settings.partita.comeEFinita();
					if (Settings.partita.vincitore() == null)
						alert.setTitle("Patta!");
					else
					{
						alert.setTitle("Fine partita!");
						line += "\nVincitore: " + Settings.partita.vincitore();
					}
					alert.setContentText(line);
					alert.show();
				}
				mostraScacchi();
			}
			pos1 = null;
			pos2 = null;
		}
	}

	@FXML
	@Override
	protected void restart(ActionEvent event) {
		sendMessage("richiesta restart");
	}

	public void ricomincia() {
		Settings.partita = new Partita();
		partita = Settings.partita;
		pos1 = null;
		pos2 = null;
		mostraScacchi();
	}

	public void confermaRestart(boolean isRestart) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Restart");
		if (isRestart)
		{
			alert.setContentText("Restart accettato");
			sendMessage("conferma restart");
			resa.setDisable(false);
			patta.setDisable(false);
			ricomincia();
		}
		else
		{
			alert.setContentText("Restart rifiutato");
			sendMessage("rifiuto restart");
		}
		alert.show();
	}

	public boolean richiestaRestart() {
		Alert alert = new Alert(Alert.AlertType.NONE);
		alert.setTitle("Richiesta restart");
		alert.setContentText("L'avversario ha richiesto di riavviare la partita");
		ButtonType ACCETTA = new ButtonType("Accetta");
		ButtonType RIFIUTA = new ButtonType("Rifiuta");
		alert.getButtonTypes().addAll(ACCETTA, RIFIUTA);
		Optional<ButtonType> scelta = alert.showAndWait();
		return scelta.get() == ACCETTA;
	}

	@FXML
	private void resa(ActionEvent event) {
		Alert scelta = new Alert(Alert.AlertType.NONE);
		scelta.setTitle("Sicuro?");
		scelta.setContentText("Sei sicuro di volerti arrendere?");
		ButtonType SI = new ButtonType("Si");
		ButtonType NO = new ButtonType("No");
		scelta.getButtonTypes().addAll(SI, NO);
		Optional<ButtonType> option = scelta.showAndWait();
		if (option.get() == NO)
			return;
		sendMessage("resa");
		Settings.partita.fine();
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Fine partita!");
		alert.setContentText("Vicitore: " + Settings.schieramento.notThis() + " per resa");
		alert.show();
		resa.setDisable(true);
		patta.setDisable(true);
		mostraScacchi();
	}

	@FXML
	private void patta(ActionEvent event) {
		sendMessage("richiesta patta");
	}

	public boolean richiestaPatta() {
		Alert alert = new Alert(Alert.AlertType.NONE);
		alert.setTitle("Richiesta patta");
		alert.setContentText("L'avversario ha richiesto la patta");
		ButtonType ACCETTA = new ButtonType("Accetta");
		ButtonType RIFIUTA = new ButtonType("Rifiuta");
		alert.getButtonTypes().addAll(ACCETTA, RIFIUTA);
		Optional<ButtonType> scelta = alert.showAndWait();
		return scelta.get() == ACCETTA;
	}

	public void finePartita(String come, Colore vincitore) {
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

	public void pattaRifiutata() {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Patta rifiutata");
		alert.setContentText("La tua richiesta di patta è stata rifiutata");
		alert.show();
	}

	public void confermaPatta(boolean isPatta) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		if (isPatta)
		{
			alert.setTitle("Patta!");
			alert.setContentText("Patta accettata");
			sendMessage("conferma patta");
			Settings.partita.fine();
			resa.setDisable(true);
			patta.setDisable(true);
			mostraScacchi();
		}
		else
		{
			alert.setTitle("Risultato patta");
			alert.setContentText("Patta rifiutata");
			sendMessage("rifiuto patta");
		}
		alert.show();
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		graphics = canvas.getGraphicsContext2D();
		Settings.partita = new Partita();
		partita = Settings.partita;
		scala = SCACCHIERA_DIM / canvas.getWidth();
		versoScacchiera = Settings.schieramento;
		if (Settings.schieramento == NERO)
			scacchiera.setImage(SCACCHIERA_INV);
		Settings.scacchieraOnlineController = this;
		mostraScacchi();
	}

	private void sendMessage(String message) {
		try
		{
			Settings.playerWriter.write(message);
			Settings.playerWriter.newLine();
			Settings.playerWriter.flush();
			if(message.length() > 5 && message.substring(0, 5).equals("mossa") && Settings.spettatoriWriters != null)
				for(BufferedWriter bw : Settings.spettatoriWriters)
				{
					bw.write(message);
					bw.newLine();
					bw.flush();
				}
		}
		catch (IOException ex)
		{
			System.out.println(ex.getMessage());
		}
	}
}
