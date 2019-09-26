/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi.controller;

import com.scacchi.model.Partita;
import com.scacchi.model.Pezzo;
import com.scacchi.model.Pezzo.Colore;
import static com.scacchi.model.Pezzo.Colore.*;
import com.scacchi.model.Posizione;
import com.scacchi.model.Posizione.Colonna;
import com.scacchi.model.Posizione.Riga;
import com.scacchi.model.TCP.Settings;
import com.scacchi.model.TCP.ThreadRicevi;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.json.JSONException;
import org.json.JSONObject;

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
	@FXML
	private Button tornaAlMenu;

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
		try
		{
			JSONObject scacchieraOnline = Settings.lingue.getJSONObject("scacchieraOnline");
			if (partita.getTurno() != null)
			{
				Alert alert = new Alert(Alert.AlertType.NONE);
				alert.setTitle(scacchieraOnline.getString("sicuro"));
				alert.setContentText(scacchieraOnline.getString("sicuroUscire"));
				ButtonType SI = new ButtonType(Settings.lingue.getString("si"));
				ButtonType NO = new ButtonType(Settings.lingue.getString("no"));
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
				FunctionsController.alertErrore("erroreTornoMenu");
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
		catch (JSONException ex)
		{
			//Non succederà mai
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
					try
					{
						JSONObject translator = Settings.lingue.getJSONObject("scacchiera");
						Alert alert = new Alert(Alert.AlertType.NONE);
						alert.setContentText(translator.getString("trasformare"));
						alert.setTitle(translator.getString("cambioPezzo"));
						ButtonType REGINA = new ButtonType(translator.getString("regina"));
						ButtonType ALFIERE = new ButtonType(translator.getString("alfiere"));
						ButtonType CAVALLO = new ButtonType(translator.getString("cavallo"));
						ButtonType TORRE = new ButtonType(translator.getString("torre"));
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
					catch (JSONException ex)
					{
						Logger.getLogger(ScacchieraOnlineController.class.getName()).log(Level.SEVERE, null, ex);//Non servirà
					}
				sendMessage("mossa " + Settings.partita.getMosse().get(Settings.partita.getMosse().size() - 1).toString());
				if (Settings.partita.getTurno() == null)//Se c'è un vincitore il metodo "muovi" imposta il turno a null
				{
					String line = partita.comeEFinita();
					if (partita.vincitore() == null)
						FunctionsController.alertInfo("patta", line);
					else
						FunctionsController.alertInfo("finePartita", line, "vincitore", partita.vincitore().toString().toLowerCase());
				}
				mostraScacchi();
			}
			if (partita.isFinita())
			{
				resa.setDisable(true);
				patta.setDisable(true);
			}
			pos1 = null;
			pos2 = null;
		}
	}

	@FXML
	private void salvaCaricaElimina(ActionEvent event) {
		try
		{
			JSONObject translator = Settings.lingue.getJSONObject("messaggi").getJSONObject("salvaCaricaElimina");

			String userName = System.getProperty("user.name");
			File folder = new File("C:" + File.separator + "Users" + File.separator + userName + File.separator + "Documents" + File.separator + "My Games");
			if (!folder.exists())
				folder.mkdirs();
			folder = new File(folder, "Scacchi");
			if (!folder.exists())
				folder.mkdirs();
			folder = new File(folder, "Multiplayer");
			Alert alert = new Alert(Alert.AlertType.NONE);
			alert.setContentText(translator.getString("salvaCaricaElimina"));
			ButtonType SALVA = new ButtonType(translator.getString("salva"));
			ButtonType CARICA = new ButtonType(translator.getString("carica"));
			ButtonType ELIMINA = new ButtonType(translator.getString("elimina"));
			ButtonType OK = new ButtonType(Settings.lingue.getString("ok"));
			ButtonType ANNULLA = new ButtonType(Settings.lingue.getString("annulla"));
			alert.getButtonTypes().addAll(SALVA, CARICA, ELIMINA, ANNULLA);
			Optional<ButtonType> scelta = alert.showAndWait();
			if (scelta.get() == ANNULLA)
			{
				FunctionsController.alertInfo("annullato", "annullato");
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
					TextField textfield = new TextField();
					textfield.setId("nomeFile");
					textfield.setPromptText(translator.getString("inserisciNomePartita"));
					Label label = new Label(translator.getString("inserisciNomePartita"));
					VBox vbox = new VBox(label, textfield);
					do
					{
						alert = new Alert(Alert.AlertType.NONE);
						alert.setTitle(translator.getString("nomePartita"));
						alert.getDialogPane().setContent(vbox);
						alert.getButtonTypes().addAll(OK, ANNULLA);
						scelta = alert.showAndWait();
						if (scelta.get() == ANNULLA)
						{
							FunctionsController.alertInfo("annullatoSalvataggio", "annullatoSalvataggio");
							return;
						}
					}
					while (textfield.getText().equals(""));

					String nomeFile = textfield.getText();
					file = new File(folder, nomeFile + ".sca");

					if (file.exists())
					{
						alert = new Alert(Alert.AlertType.NONE);
						alert.setTitle(translator.getString("partitaEsistente"));
						alert.setContentText(translator.getString("sovrascrivere"));
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

				FunctionsController.alertInfo("salvato", "partitaSalvata");
			}
			else if (scelta.get() == CARICA)
			{
				ListView<String> listaFileDaCaricare = new ListView<>();
				File[] files = folder.listFiles();
				if (files == null || files.length == 0)
				{
					FunctionsController.alertErrore("noSalvataggi");
					return;
				}
				ArrayList<String> nomiFilesDaCaricare = new ArrayList<>();
				for (File file : files)
				{
					nomiFilesDaCaricare.add(file.getName().substring(0, file.getName().length() - 4));
				}
				listaFileDaCaricare.getItems().addAll(nomiFilesDaCaricare);
				listaFileDaCaricare.setPrefHeight(300);
				Canvas c = new Canvas(300, 300);
				GraphicsContext context = c.getGraphicsContext2D();
				context.setTextAlign(TextAlignment.CENTER);
				context.setTextBaseline(VPos.CENTER);
				context.fillText(translator.getString("canvasAnteprimaAlt"), c.getWidth() / 2, c.getHeight() / 2);
				listaFileDaCaricare.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->
				{
					try (FileInputStream fis = new FileInputStream(files[listaFileDaCaricare.getSelectionModel().getSelectedIndex()]))
					{
						ObjectInputStream fois = new ObjectInputStream(fis);
						Partita p = (Partita) fois.readObject();
						ScacchieraController.mostraScacchi(c, p);
					}
					catch (ClassNotFoundException | IOException ex)
					{
						try
						{
							context.clearRect(0, 0, c.getWidth(), c.getHeight());
							context.setFill(Color.BLACK);
							context.fillText(translator.getString("canvasAnteprimaCorrotta"), c.getWidth() / 2, c.getHeight() / 2);
						}
						catch (JSONException ex1)
						{
							Logger.getLogger(ScacchieraOnlineController.class.getName()).log(Level.SEVERE, null, ex1);//Non servirà mai
						}
					}
				});

				int selectedIndex = -1;

				do
				{
					alert = new Alert(Alert.AlertType.NONE);
					alert.getButtonTypes().addAll(OK, ANNULLA);
					alert.getDialogPane().setContent(new HBox(c, listaFileDaCaricare));
					scelta = alert.showAndWait();
					if (scelta.get() == ANNULLA)
					{
						FunctionsController.alertInfo("annullatoCaricamento", "annullatoCaricamento");
						return;
					}
					selectedIndex = listaFileDaCaricare.getSelectionModel().getSelectedIndex();
				}
				while (selectedIndex == -1);

				try
				{
					FileInputStream fis = new FileInputStream(files[selectedIndex]);
					ObjectInputStream ois = new ObjectInputStream(fis);
					Settings.partitaDaCaricare = (Partita) ois.readObject();
					ois.close();
					FunctionsController.alertInfo("richiestaInviata", "richiestaInviata");
					sendMessage("richiesta caricamento");
					disattivaBottoni();
					if (Settings.playerOOS == null)
						Settings.playerOOS = new ObjectOutputStream(Settings.player.getOutputStream());
					Settings.playerOOS.writeObject(Settings.partitaDaCaricare);
				}
				catch (IOException | ClassNotFoundException ex)
				{
					FunctionsController.alertErrore("fileCorrotto");
				}
			}
			else
			{
				ListView<String> listaFileDaCancellare = new ListView<>();
				File[] files = folder.listFiles();
				if (files == null || files.length == 0)
				{
					FunctionsController.alertErrore("noSalvataggi");
					return;
				}
				ArrayList<String> nomiFilesDaCaricare = new ArrayList<>();
				for (File file : files)
				{
					nomiFilesDaCaricare.add(file.getName().substring(0, file.getName().length() - 4));
				}
				listaFileDaCancellare.getItems().addAll(nomiFilesDaCaricare);
				listaFileDaCancellare.setPrefHeight(300);
				Canvas c = new Canvas(300, 300);
				GraphicsContext context = c.getGraphicsContext2D();
				context.setTextAlign(TextAlignment.CENTER);
				context.setTextBaseline(VPos.CENTER);
				context.fillText(translator.getString("canvasAnteprimaAlt"), c.getWidth() / 2, c.getHeight() / 2);
				listaFileDaCancellare.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->
				{
					try (FileInputStream fis = new FileInputStream(files[listaFileDaCancellare.getSelectionModel().getSelectedIndex()]))
					{
						ObjectInputStream fois = new ObjectInputStream(fis);
						Partita p = (Partita) fois.readObject();
						ScacchieraController.mostraScacchi(c, p);
					}
					catch (ClassNotFoundException | IOException ex)
					{
						try
						{
							context.clearRect(0, 0, c.getWidth(), c.getHeight());
							context.setFill(Color.BLACK);
							context.fillText(translator.getString("canvasAnteprimaCorrotta"), c.getWidth() / 2, c.getHeight() / 2);
						}
						catch (JSONException ex1)
						{
							Logger.getLogger(ScacchieraOnlineController.class.getName()).log(Level.SEVERE, null, ex1);//Non succederà mai
						}
					}
				});

				int selectedIndex = -1;

				do
				{
					alert = new Alert(Alert.AlertType.NONE);
					alert.getButtonTypes().addAll(OK, ANNULLA);
					alert.getDialogPane().setContent(new HBox(c, listaFileDaCancellare));
					scelta = alert.showAndWait();
					selectedIndex = listaFileDaCancellare.getSelectionModel().getSelectedIndex();
					if (scelta.get() == ANNULLA)
					{
						FunctionsController.alertInfo("annullatoEliminazione", "annullatoEliminazione");
						return;
					}
				}
				while (selectedIndex == -1);

				if (files[selectedIndex].delete())
					FunctionsController.alertInfo("eliminato", "partitaEliminata");
				else
					FunctionsController.alertErrore("erroreEliminazione");//Non dovrebbe mai succedere
			}
		}
		catch (JSONException ex)
		{
			//Non è necessario
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
		if (isRestart)
		{
			FunctionsController.alertInfo("restart", "restartAccettato");
			sendMessage("conferma restart");
			attivaBottoni();
			ricomincia();
			ThreadRicevi.sendToSpettatori("conferma restart");
		}
		else
		{
			FunctionsController.alertInfo("restart", "haiRifiutatoRestart");
			sendMessage("rifiuto restart");
		}
	}

	public void confermaCaricamento(boolean isCaricamento) {
		if (isCaricamento)
		{
			FunctionsController.alertInfo("caricamentoPartita", "partitaCaricata");
			sendMessage("conferma caricamento");
			attivaBottoni();
			partita = Settings.partitaDaCaricare;
			Settings.partita = partita;
			Settings.partitaDaCaricare = null;
			mostraScacchi();
			ThreadRicevi.sendToSpettatori("conferma caricamento");
			ThreadRicevi.sendObjectToSpettatori(Settings.partitaDaCaricare);
		}
		else
		{
			FunctionsController.alertInfo("caricamentoPartita", "haiRifiutatoCaricamento");
			sendMessage("rifiuto caricamento");
			Settings.partitaDaCaricare = null;
		}
	}

	public boolean richiestaRestart() {
		try
		{
			JSONObject scacchieraOnline = Settings.lingue.getJSONObject("scacchieraOnline");

			Alert alert = new Alert(Alert.AlertType.NONE);
			alert.setTitle(scacchieraOnline.getString("richiestaRestartTitle"));
			alert.setContentText(scacchieraOnline.getString("richiestaRestartBody"));
			ButtonType ACCETTA = new ButtonType(Settings.lingue.getString("accetta"));
			ButtonType RIFIUTA = new ButtonType(Settings.lingue.getString("rifiuta"));
			alert.getButtonTypes().addAll(ACCETTA, RIFIUTA);
			Optional<ButtonType> scelta = alert.showAndWait();
			return scelta.get() == ACCETTA;
		}
		catch (JSONException ex)
		{
			//Non succederà mai
		}
		return false;
	}

	public boolean richiestaCaricamento() {
		try
		{
			JSONObject scacchieraOnline = Settings.lingue.getJSONObject("scacchieraOnline");

			Alert alert = new Alert(Alert.AlertType.NONE);
			alert.setTitle(scacchieraOnline.getString("richiestaCaricamentoTitle"));
			alert.setContentText(scacchieraOnline.getString("richiestaCaricamentoBody"));
			ButtonType ACCETTA = new ButtonType(Settings.lingue.getString("accetta"));
			ButtonType RIFIUTA = new ButtonType(Settings.lingue.getString("rifiuta"));
			alert.getButtonTypes().addAll(ACCETTA, RIFIUTA);
			Canvas c = new Canvas(300, 300);
			ScacchieraController.mostraScacchi(c, Settings.partitaDaCaricare);
			alert.getDialogPane().setContent(c);
			Optional<ButtonType> scelta = alert.showAndWait();
			return scelta.get() == ACCETTA;
		}
		catch (JSONException ex)
		{
			//Non succederà mai
		}
		return false;
	}

	@FXML
	private void resa(ActionEvent event) {
		try
		{
			JSONObject scacchieraOnline = Settings.lingue.getJSONObject("scacchieraOnline");
			Alert scelta = new Alert(Alert.AlertType.NONE);
			scelta.setTitle(scacchieraOnline.getString("sicuro"));
			scelta.setContentText(scacchieraOnline.getString("sicuroResa"));
			ButtonType SI = new ButtonType(Settings.lingue.getString("si"));
			ButtonType NO = new ButtonType(Settings.lingue.getString("no"));
			scelta.getButtonTypes().addAll(SI, NO);
			Optional<ButtonType> option = scelta.showAndWait();
			if (option.get() == NO)
				return;
			sendMessage("resa");
			Settings.partita.fine();
			FunctionsController.alertInfo("finePartita", "resa" + Settings.schieramento.toString());
			resa.setDisable(true);
			patta.setDisable(true);
			mostraScacchi();
			ThreadRicevi.sendToSpettatori("resa");
			ThreadRicevi.sendToSpettatori(Settings.schieramento.notThis().toString().toLowerCase());
		}
		catch (JSONException ex)
		{
			//Non succederà mai
		}
	}

	@FXML
	private void patta(ActionEvent event) {
		disattivaBottoni();
		sendMessage("richiesta patta");
	}

	public boolean richiestaPatta() {
		try
		{
			JSONObject scacchieraOnline = Settings.lingue.getJSONObject("scacchieraOnline");

			Alert alert = new Alert(Alert.AlertType.NONE);
			alert.setTitle(scacchieraOnline.getString("richiestaPattaTitle"));
			alert.setContentText(scacchieraOnline.getString("richiestaPattaBody"));
			ButtonType ACCETTA = new ButtonType(Settings.lingue.getString("accetta"));
			ButtonType RIFIUTA = new ButtonType(Settings.lingue.getString("rifiuta"));
			alert.getButtonTypes().addAll(ACCETTA, RIFIUTA);
			Optional<ButtonType> scelta = alert.showAndWait();
			return scelta.get() == ACCETTA;
		}
		catch (JSONException ex)
		{
			//Non succederà mai
		}
		return false;
	}

	public void finePartita(String come, Colore vincitore) {
		if (vincitore == null)
		{
			if (come.equals("patta"))
				FunctionsController.alertInfo("patta", "accordo");
		}
		else if (come.equals("resa"))
			FunctionsController.alertInfo("finePartita", "resa" + vincitore.notThis().toString());
		Settings.partita.fine();
		mostraScacchi();
	}

	public void pattaRifiutata() {
		FunctionsController.alertInfo("pattaRifiutata", "pattaRifiutata");
	}

	public void confermaPatta(boolean isPatta) {
		if (isPatta)
		{
			FunctionsController.alertInfo("patta", "accordo");
			sendMessage("conferma patta");
			Settings.partita.fine();
			resa.setDisable(true);
			patta.setDisable(true);
			mostraScacchi();
			ThreadRicevi.sendToSpettatori("conferma patta");
		}
		else
		{
			FunctionsController.alertInfo("pattaRifiutata", "haiRifiutatoPatta");
			sendMessage("rifiuto patta");
		}
	}

	private void traduciTutto() throws JSONException {
		JSONObject scacchieraOnline = Settings.lingue.getJSONObject("scacchieraOnline");
		tornaAlMenu.setText(scacchieraOnline.getString("esci"));
		resa.setText(scacchieraOnline.getString("resa"));
		patta.setText(scacchieraOnline.getString("patta"));
		restart.setText(scacchieraOnline.getString("restart"));
		salvaCaricaElimina.setText(scacchieraOnline.getString("salvaCaricaElimina"));
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		try
		{
			traduciTutto();
		}
		catch (JSONException ex)
		{
			Logger.getLogger(ScacchieraOnlineController.class.getName()).log(Level.SEVERE, null, ex);
		}
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

	private static void sendMessage(String message) {
		try
		{
			Settings.playerWriter.write(message);
			Settings.playerWriter.newLine();
			Settings.playerWriter.flush();
			if (message.length() > 5 && message.substring(0, 5).equals("mossa"))
				ThreadRicevi.sendToSpettatori(message);
			if (Settings.partita.isFinita() && !message.equals("richiesta restart") && !message.equals("conferma restart"))
			{
				ThreadRicevi.sendToSpettatori("fine");
				ThreadRicevi.sendToSpettatori(Settings.partita.vincitore() == null ? "null" : Settings.partita.vincitore().toString().toLowerCase());
				ThreadRicevi.sendToSpettatori(Settings.partita.comeEFinita());
			}
		}
		catch (IOException ex)
		{
			System.out.println(ex.getMessage());
		}
	}
}
