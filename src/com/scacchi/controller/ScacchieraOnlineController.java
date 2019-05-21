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
import com.scacchi.model.TCP.ThreadRicevi;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
	public Button restart;
	@FXML
	public Button salvaCaricaElimina;

	public void disattivaBottoni() {
		resa.setDisable(true);
		patta.setDisable(true);
		restart.setDisable(true);
		salvaCaricaElimina.setDisable(true);
	}

	public void attivaBottoni() {
		resa.setDisable(false);
		patta.setDisable(false);
		restart.setDisable(false);
		salvaCaricaElimina.setDisable(false);
	}

	@FXML
	@Override
	protected void menu(ActionEvent event) {
		if (partita.getTurno() != null)
		{
			Alert alert = new Alert(Alert.AlertType.NONE);
			alert.setTitle("Sicuro?");
			alert.setContentText("Sei sicuro di voler uscire? Facendolo abbandonerai la partita.");
			ButtonType SI = new ButtonType("Si");
			ButtonType NO = new ButtonType("No");
			alert.getButtonTypes().addAll(SI, NO);
			Optional<ButtonType> scelta = alert.showAndWait();
			if (scelta.get() == NO)
				return;
		}
		try
		{
			if (Settings.threadAccetta != null && Settings.threadAccetta.isAlive())
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
		Settings.scacchieraOnlineController = null;
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
	private void salvaCaricaElimina(ActionEvent event) {
		String userName = System.getProperty("user.name");
		File folder = new File("C:" + File.separator + "Users" + File.separator + userName + File.separator + "Documents" + File.separator + "My Games");
		if (!folder.exists())
			folder.mkdirs();
		folder = new File(folder, "Scacchi");
		if (!folder.exists())
			folder.mkdirs();
		folder = new File(folder, "Multiplayer");
		Alert alert = new Alert(Alert.AlertType.NONE);
		alert.setContentText("Vuoi salvare o caricare una partita?");
		ButtonType SALVA = new ButtonType("Salva");
		ButtonType CARICA = new ButtonType("Carica");
		ButtonType ELIMINA = new ButtonType("Elimina");
		ButtonType OK = new ButtonType("Ok");
		ButtonType ANNULLA = new ButtonType("Annulla");
		alert.getButtonTypes().addAll(SALVA, CARICA, ELIMINA, ANNULLA);
		Optional<ButtonType> scelta = alert.showAndWait();
		if (scelta.get() == ANNULLA)
		{
			FunctionsController.alertInfo("Annullato", "Hai annullato la procedura");
			return;
		}
		if (scelta.get() == SALVA)
		{
			boolean ripeti;
			File file = null;
			if (!folder.exists())
				folder.mkdirs();
			do
			{
				ripeti = false;
				Node node = new TextField();
				node.setId("nomeFile");
				((TextField) node).setPromptText("Inserisci il nome della partita");
				Label label = new Label("Inserisci il nome della partita");
				VBox vbox = new VBox(label, node);
				do
				{
					alert = new Alert(Alert.AlertType.NONE);
					alert.setTitle("Nome partita");
					alert.getDialogPane().setContent(vbox);
					alert.getButtonTypes().addAll(OK, ANNULLA);
					scelta = alert.showAndWait();
					if (scelta.get() == ANNULLA)
					{
						FunctionsController.alertInfo("Salvataggio annullato", "Hai annullato il salvataggio della partita");
						return;
					}
				}
				while (((TextField) node).getText().equals(""));

				String nomeFile = ((TextField) node).getText();
				file = new File(folder, nomeFile + ".dat");

				if (file.exists())
				{
					alert = new Alert(Alert.AlertType.NONE);
					alert.setTitle("Partita presente");
					alert.setContentText("Vuoi sovrascrivere?");
					alert.getButtonTypes().addAll(OK, ANNULLA);
					scelta = alert.showAndWait();
					if (scelta.get() != OK)
						ripeti = true;
				}
				if (!ripeti)
					try
					{
						file.createNewFile();
					}
					catch (IOException ex)
					{
					}
			}
			while (ripeti);

			try
			{
				FileOutputStream fos = new FileOutputStream(file);
				ObjectOutputStream foos = new ObjectOutputStream(fos);
				foos.writeObject(partita);
				foos.close();
			}
			catch (IOException ex)
			{
			}

			FunctionsController.alertInfo("Salvato", "Partita salvata!");
		}
		else if (scelta.get() == CARICA)
		{
			ListView<String> listaFileDaCaricare = new ListView<>();
			File[] files = folder.listFiles();
			if (files == null || files.length == 0)
			{
				FunctionsController.alertErrore("Non sono presenti salvataggi");
				return;
			}
			ArrayList<String> nomiFilesDaCaricare = new ArrayList<>();
			for (File file : files)
			{
				nomiFilesDaCaricare.add(file.getName().substring(0, file.getName().length() - 4));
			}
			listaFileDaCaricare.getItems().addAll(nomiFilesDaCaricare);

			int selectedIndex = -1;

			do
			{
				alert = new Alert(Alert.AlertType.NONE);
				alert.getButtonTypes().addAll(OK, ANNULLA);
				alert.getDialogPane().setContent(listaFileDaCaricare);
				scelta = alert.showAndWait();
				selectedIndex = listaFileDaCaricare.getSelectionModel().getSelectedIndex();
				if (scelta.get() == ANNULLA)
				{
					FunctionsController.alertInfo("Caricamento annullato", "Hai annullato il caricamento della partita");
					return;
				}
			}
			while (selectedIndex == -1);

			try
			{
				FileInputStream fis = new FileInputStream(files[selectedIndex]);
				ObjectInputStream ois = new ObjectInputStream(fis);
				Settings.partitaDaCaricare = (Partita) ois.readObject();
				ois.close();
				mostraScacchi();
				FunctionsController.alertInfo("Richiesta inviata", "Inviata la richiesta di caricamento della partita all'avversario");
				sendMessage("richiesta caricamento");
				disattivaBottoni();
				if(Settings.playerOOS == null)
					Settings.playerOOS = new ObjectOutputStream(Settings.player.getOutputStream());
				Settings.playerOOS.writeObject(Settings.partitaDaCaricare);
			}
			catch (FileNotFoundException ex)
			{//Il file ci sarà sempre
			}
			catch (IOException ex)
			{
			}
			catch (ClassNotFoundException ex)
			{
				FunctionsController.alertErrore("Il file selezionato non è nel formato corretto");
			}
		}
		else
		{
			ListView<String> listaFileDaCancellare = new ListView<>();
			File[] files = folder.listFiles();
			if (files == null || files.length == 0)
			{
				FunctionsController.alertErrore("Non sono presenti salvataggi");
				return;
			}
			ArrayList<String> nomiFilesDaCaricare = new ArrayList<>();
			for (File file : files)
			{
				nomiFilesDaCaricare.add(file.getName().substring(0, file.getName().length() - 4));
			}
			listaFileDaCancellare.getItems().addAll(nomiFilesDaCaricare);

			int selectedIndex = -1;

			do
			{
				alert = new Alert(Alert.AlertType.NONE);
				alert.getButtonTypes().addAll(OK, ANNULLA);
				alert.getDialogPane().setContent(listaFileDaCancellare);
				scelta = alert.showAndWait();
				selectedIndex = listaFileDaCancellare.getSelectionModel().getSelectedIndex();
				if (scelta.get() == ANNULLA)
				{
					FunctionsController.alertInfo("Eliminazione annullato", "Hai annullato l'eliminazione della partita");
					return;
				}
			}
			while (selectedIndex == -1);

			files[selectedIndex].delete();
			FunctionsController.alertInfo("Successo", "Partita eliminata con successo");
		}
	}

	@FXML
	@Override
	protected void restart(ActionEvent event) {
		disattivaBottoni();
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
			attivaBottoni();
			ricomincia();
			ThreadRicevi.sendToSpettatori("conferma restart");
		}
		else
		{
			alert.setContentText("Restart rifiutato");
			sendMessage("rifiuto restart");
		}
		alert.show();
	}

	public void confermaCaricamento(boolean isCaricamento) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Caricamento");
		if (isCaricamento)
		{
			alert.setContentText("Caricamento accettato");
			sendMessage("conferma caricamento");
			attivaBottoni();
			partita = Settings.partitaDaCaricare;
			Settings.partita = partita;
			Settings.partitaDaCaricare = null;
			mostraScacchi();
			ThreadRicevi.sendToSpettatori("conferma caricamento");
		}
		else
		{
			alert.setContentText("Caricamento rifiutato");
			sendMessage("rifiuto caricamento");
			Settings.partitaDaCaricare = null;
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

	public boolean richiestaCaricamento() {
		Alert alert = new Alert(Alert.AlertType.NONE);
		alert.setTitle("Richiesta caricamento");
		alert.setContentText("L'avversario ha richiesto di caricare una partita");
		ButtonType ACCETTA = new ButtonType("Accetta");
		ButtonType RIFIUTA = new ButtonType("Rifiuta");
		alert.getButtonTypes().addAll(ACCETTA, RIFIUTA);
		Canvas c = new Canvas(300, 300);
		ScacchieraController.mostraScacchi(c, Settings.partitaDaCaricare);
		Pane pane = new Pane(c);
		alert.getDialogPane().setContent(c);
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
		ThreadRicevi.sendToSpettatori("resa");
		ThreadRicevi.sendToSpettatori(Settings.schieramento.notThis().toString().toLowerCase());
	}

	@FXML
	private void patta(ActionEvent event) {
		disattivaBottoni();
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
			ThreadRicevi.sendToSpettatori("conferma patta");
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
			if (Settings.spettatori != null && Settings.partita.isFinita())
				for (BufferedWriter bw : Settings.spettatoriWriters)
				{
					bw.write("fine\n");
					bw.flush();
				}
		}
		catch (IOException ex)
		{
			System.out.println(ex.getMessage());
		}
		if ((message.length() > 5 && message.substring(0, 5).equals("mossa")) && Settings.spettatoriWriters != null)
		{
			ArrayList<Integer> indexesToRemove = new ArrayList<>();
			Iterator<BufferedWriter> it = Settings.spettatoriWriters.iterator();
			while (it.hasNext())
			{
				BufferedWriter bw = it.next();
				try
				{
					bw.write(message);
					bw.newLine();
					bw.flush();
				}
				catch (IOException ex)
				{
					indexesToRemove.add(Settings.spettatoriWriters.indexOf(bw));
				}
			}
			for (Integer index : indexesToRemove)
			{
				try
				{
					Settings.spettatori.get(index).close();
				}
				catch (IOException ex1)
				{
				}
				Settings.spettatoriWriters.remove(index.intValue());
				Settings.spettatori.remove(index.intValue());
				Settings.spettatoriReaders.remove(index.intValue());
			}
		}
	}
}
