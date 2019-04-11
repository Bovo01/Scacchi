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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Pietro
 */
public class ScacchieraOnlineController implements Initializable {

	@FXML
	private Label turno;
	@FXML
	private Canvas canvas;
	@FXML
	private ImageView scacchiera;
	@FXML
	private AnchorPane anchorPane;
	@FXML
	private Button resa;
	@FXML
	private Button patta;
	@FXML
	private Button restart;

	private Partita partita;
	private GraphicsContext graphics;
	private Posizione pos1, pos2;
	private Colore versoScacchiera;
	private static final double SCACCHIERA_DIM = 1080;
	private double scala;//Scala invertita
	private static final Image SCACCHIERA = new Image("com/scacchi/view/img/scacchiera.jpg");
	private static final Image SCACCHIERA_INV = new Image("com/scacchi/view/img/scacchiera_inv.jpg");
	private static final Image SCACCHI = new Image("com/scacchi/view/img/scacchi.png");

	public void disattivaBottoni() {
		resa.setDisable(true);
		patta.setDisable(true);
		restart.setDisable(true);
	}

	@FXML
	private void menu(ActionEvent event) {
		try
		{
			if(Settings.thread != null && Settings.thread.isAlive())
				Settings.thread.close();
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
			scene.getWindow().setOnCloseRequest(((event2) -> {}));
		}
		catch (IOException ex)
		{
			FunctionsController.alertErrore(ex.getMessage());
		}
	}

	@FXML
	private void click(MouseEvent event) {
		double x = event.getSceneX() * scala;
		double y = event.getSceneY() * scala;
		if (x < 48 || x > 1031 || y < 48 || y > 1031)
			return;
		if (partita.getTurno() != Settings.schieramento)
			return;
		if (pos1 == null)
		{
			if (versoScacchiera == BIANCO)
				pos1 = new Posizione(Riga.values()[(int) ((y - 48) / 123)], Colonna.values()[(int) ((x - 48) / 123)]);
			else
				pos1 = new Posizione(Riga.values()[7 - (int) ((y - 48) / 123)], Colonna.values()[7 - (int) ((x - 48) / 123)]);
			if (partita.trovaPezzo(pos1) == null || partita.trovaPezzo(pos1).getColore() != partita.getTurno())
			{
				pos1 = null;
				return;
			}
			mostraMosse(partita.trovaPezzo(pos1));
		}
		else
		{
			Pezzo pezzo = partita.trovaPezzo(pos1);
			if (versoScacchiera == BIANCO)
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
				if (partita.trovaPezzo(pos2) != null && pezzo.getColore() == partita.trovaPezzo(pos2).getColore())
				{
					pos1 = pos2;
					pos2 = null;
					mostraScacchi();
					mostraMosse(partita.trovaPezzo(pos1));
					return;
				}
				if (!partita.muovi(pezzo, pos2))
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
						partita.promozione(pezzo, Pezzo.Simbolo.REGINA);
					else if (option.get() == ALFIERE)
						partita.promozione(pezzo, Pezzo.Simbolo.ALFIERE);
					else if (option.get() == TORRE)
						partita.promozione(pezzo, Pezzo.Simbolo.TORRE);
					else if (option.get() == CAVALLO)
						partita.promozione(pezzo, Pezzo.Simbolo.CAVALLO);
				}
				sendMessage("mossa " + partita.getMosse().get(partita.getMosse().size() - 1).toString());
				if (partita.getTurno() == null)//Se c'è un vincitore il metodo "muovi" imposta il turno a null
				{
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					String line = partita.comeEFinita();
					if (partita.vincitore() == null)
						alert.setTitle("Patta!");
					else
					{
						alert.setTitle("Fine partita!");
						line += "\nVincitore: " + partita.vincitore();
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
	private void restart(ActionEvent event) {
		sendMessage("richiesta restart");
	}

	public void ricomincia() {
		partita = new Partita();
		Settings.partita = partita;
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
		partita.fine();
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Fine partita!");
		alert.setContentText("Vicitore: " + Settings.schieramento.notThis() + " per resa");
		alert.show();
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
		partita.fine();
		mostraScacchi();
	}

	public void pattaRifiutata() {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Patta rifiutat");
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
			partita.fine();
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
		partita = new Partita();
		Settings.partita = partita;
		scala = SCACCHIERA_DIM / canvas.getWidth();
		versoScacchiera = Settings.schieramento;
		if (versoScacchiera == NERO)
			scacchiera.setImage(SCACCHIERA_INV);
		Settings.scacchieraOnlineController = this;
		mostraScacchi();
		Platform.runLater(() -> anchorPane.getScene().getWindow().setOnCloseRequest((WindowEvent event) ->
		{
			try
			{
				Settings.thread.close();
			}
			catch (IOException ex)
			{
				event.consume();
			}
		}));
	}

	public void mostraScacchi() {
		Mossa ultimaMossa = partita.getUltimaMossa();
		if (partita.getTurno() == null)
			turno.setText("PARTITA CONCLUSA!");
		else
			turno.setText(partita.getTurno().toString());
		graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		ArrayList<Pezzo> unione = new ArrayList<>();
		unione.addAll(partita.getBianchi());
		unione.addAll(partita.getNeri());
		graphics.setFill(Color.POWDERBLUE);
		if (ultimaMossa != null)
		{
			double riempiX1, riempiY1, riempiX2, riempiY2;
			if (versoScacchiera == BIANCO)
			{
				riempiX1 = ultimaMossa.getPosIniz().getColonna().ordinal() * 123;
				riempiY1 = ultimaMossa.getPosIniz().getRiga().ordinal() * 123;
				riempiX2 = ultimaMossa.getPosFine().getColonna().ordinal() * 123;
				riempiY2 = ultimaMossa.getPosFine().getRiga().ordinal() * 123;
			}
			else
			{
				riempiX1 = (7 - ultimaMossa.getPosIniz().getColonna().ordinal()) * 123;
				riempiY1 = (7 - ultimaMossa.getPosIniz().getRiga().ordinal()) * 123;
				riempiX2 = (7 - ultimaMossa.getPosFine().getColonna().ordinal()) * 123;
				riempiY2 = (7 - ultimaMossa.getPosFine().getRiga().ordinal()) * 123;
			}
			graphics.fillRect((48 + riempiX1) / scala, (48 + riempiY1) / scala, 123 / scala, 123 / scala);
			graphics.clearRect((57 + riempiX1) / scala, (57 + riempiY1) / scala, 105 / scala, 105 / scala);
			graphics.fillRect((48 + riempiX2) / scala, (48 + riempiY2) / scala, 123 / scala, 123 / scala);
			graphics.clearRect((57 + riempiX2) / scala, (57 + riempiY2) / scala, 105 / scala, 105 / scala);
		}
		if (partita.isScacco(BIANCO))//Rosso (scuro) a quelli che mettono in scacco
		{
			graphics.setFill(Color.rgb(0, 150, 0));
			for (Pezzo pezzo : partita.pedineScacco(BIANCO))
			{
				double dx, dy, dw = 123 / scala, dh = 123 / scala;
				if (versoScacchiera == BIANCO)
				{
					dx = (pezzo.getPosizione().getColonna().ordinal() * 123 + 48) / scala;
					dy = (pezzo.getPosizione().getRiga().ordinal() * 123 + 48) / scala;
				}
				else
				{
					dx = ((7 - pezzo.getPosizione().getColonna().ordinal()) * 123 + 48) / scala;
					dy = ((7 - pezzo.getPosizione().getRiga().ordinal()) * 123 + 48) / scala;
				}
				graphics.fillRect(dx, dy, dw, dh);
				disegnaPezzo(partita.trovaPezzo(pezzo.getPosizione()));
			}
		}
		else if (partita.isScacco(NERO))
		{
			graphics.setFill(Color.rgb(0, 150, 0));
			for (Pezzo pezzo : partita.pedineScacco(NERO))
			{
				double dx, dy, dw = 123 / scala, dh = 123 / scala;
				if (versoScacchiera == BIANCO)
				{
					dx = (pezzo.getPosizione().getColonna().ordinal() * 123 + 48) / scala;
					dy = (pezzo.getPosizione().getRiga().ordinal() * 123 + 48) / scala;
				}
				else
				{
					dx = ((7 - pezzo.getPosizione().getColonna().ordinal()) * 123 + 48) / scala;
					dy = ((7 - pezzo.getPosizione().getRiga().ordinal()) * 123 + 48) / scala;
				}
				graphics.fillRect(dx, dy, dw, dh);
				disegnaPezzo(partita.trovaPezzo(pezzo.getPosizione()));
			}
		}
		for (Pezzo p : unione)
		{
			disegnaPezzo(p);
		}
	}

	private void disegnaPezzo(Pezzo p) {
		double sx, sy, sw = SCACCHI.getWidth() / 6, sh = SCACCHI.getHeight() / 2;//Coordinate da cui ritagliare l'immagine
		double dx, dy, dw = 123 / scala, dh = 123 / scala;//Coordinate dove inserire l'immagine. Le dimensioni sono fisse (rappresentano la dimensione di un quadrato della scacchiera)
		sx = sw * p.getSimbolo().ordinal();
		sy = sh * p.getColore().ordinal();
		if (versoScacchiera == BIANCO)
		{
			dx = (p.getPosizione().getColonna().ordinal() * 123 + 48) / scala;
			dy = (p.getPosizione().getRiga().ordinal() * 123 + 48) / scala;
		}
		else
		{
			dx = ((7 - p.getPosizione().getColonna().ordinal()) * 123 + 48) / scala;
			dy = ((7 - p.getPosizione().getRiga().ordinal()) * 123 + 48) / scala;
		}
		graphics.drawImage(SCACCHI, sx, sy, sw, sh, dx, dy, dw, dh);
	}

	private void mostraMosse(Pezzo p) {
		ArrayList<Posizione> elenco = partita.elencoMosseScacco(p);
		//En passant
		if (partita.getUltimaMossa() != null && (partita.getUltimaMossa().getPosFine().getRiga().ordinal() == partita.getUltimaMossa().getPosIniz().getRiga().ordinal() + 2
			|| partita.getUltimaMossa().getPosFine().getRiga().ordinal() == partita.getUltimaMossa().getPosIniz().getRiga().ordinal() - 2)
			&& (p.getPosizione().getRiga().ordinal() + 1 < 8 && p.getPosizione().getRiga().ordinal() - 1 >= 0)//Questa riga evita IndexOutOfBoundException nell'enum riga
			&& (elenco.contains(new Posizione(Riga.values()[p.getPosizione().getRiga().ordinal() + 1], partita.getUltimaMossa().getPosIniz().getColonna()))
			|| elenco.contains(new Posizione(Riga.values()[p.getPosizione().getRiga().ordinal() - 1], partita.getUltimaMossa().getPosIniz().getColonna()))))
		{
			Pezzo p2 = partita.trovaPezzo(partita.getUltimaMossa().getPosFine());
			if (p.getColore() != p2.getColore() && p.getSimbolo() == Pezzo.Simbolo.PEDONE && p2.getSimbolo() == Pezzo.Simbolo.PEDONE
				&& (p2.getPosizione().getColonna().ordinal() == p.getPosizione().getColonna().ordinal() + 1 || p2.getPosizione().getColonna().ordinal() == p.getPosizione().getColonna().ordinal() - 1)
				&& p2.getPosizione().getRiga() == p.getPosizione().getRiga())
				if (versoScacchiera == BIANCO)
				{
					graphics.setFill(Color.rgb(255, 30, 30));
					double dx = (p2.getPosizione().getColonna().ordinal() * 123 + 48) / scala, dy = (p2.getPosizione().getRiga().ordinal() * 123 + 48) / scala, dw = 123 / scala, dh = 123 / scala;
					graphics.fillRect(dx, dy, dw, dh);
					disegnaPezzo(partita.trovaPezzo(p2.getPosizione()));
					graphics.setFill(Color.GRAY);
				}
				else
				{
					graphics.setFill(Color.rgb(255, 30, 30));
					double dx = ((7 - p2.getPosizione().getColonna().ordinal()) * 123 + 48) / scala, dy = ((7 - p2.getPosizione().getRiga().ordinal()) * 123 + 48) / scala, dw = 123 / scala, dh = 123 / scala;
					graphics.fillRect(dx, dy, dw, dh);
					disegnaPezzo(partita.trovaPezzo(p2.getPosizione()));
					graphics.setFill(Color.GRAY);
				}
		}
		graphics.setFill(Color.GRAY);
		for (Posizione pos : elenco)
		{
			if (versoScacchiera == BIANCO)
			{
				if (partita.trovaPezzo(pos) != null)
				{
					graphics.setFill(Color.rgb(255, 30, 30));
					double dx = (pos.getColonna().ordinal() * 123 + 48) / scala, dy = (pos.getRiga().ordinal() * 123 + 48) / scala, dw = 123 / scala, dh = 123 / scala;
					graphics.fillRect(dx, dy, dw, dh);
					disegnaPezzo(partita.trovaPezzo(pos));
					graphics.setFill(Color.GRAY);
				}
				graphics.fillOval((48 + 36.5 + 123 * pos.getColonna().ordinal()) / scala, (48 + 36.5 + 123 * pos.getRiga().ordinal()) / scala, 50 / scala, 50 / scala);
			}
			else
			{
				if (partita.trovaPezzo(pos) != null)
				{
					graphics.setFill(Color.rgb(255, 30, 30));
					double dx = ((7 - pos.getColonna().ordinal()) * 123 + 48) / scala, dy = ((7 - pos.getRiga().ordinal()) * 123 + 48) / scala, dw = 123 / scala, dh = 123 / scala;
					graphics.fillRect(dx, dy, dw, dh);
					disegnaPezzo(partita.trovaPezzo(pos));
					graphics.setFill(Color.GRAY);
				}
				graphics.fillOval((48 + 36.5 + 123 * (7 - pos.getColonna().ordinal())) / scala, (48 + 36.5 + 123 * (7 - pos.getRiga().ordinal())) / scala, 50 / scala, 50 / scala);
			}
		}
		if (versoScacchiera == BIANCO)
			graphics.drawImage(new Image("com/scacchi/view/img/selected.png"), (48 + 123 * p.getPosizione().getColonna().ordinal()) / scala, (48 + 123 * p.getPosizione().getRiga().ordinal()) / scala, 123 / scala, 123 / scala);
		else
			graphics.drawImage(new Image("com/scacchi/view/img/selected.png"), (48 + 123 * (7 - p.getPosizione().getColonna().ordinal())) / scala, (48 + 123 * (7 - p.getPosizione().getRiga().ordinal())) / scala, 123 / scala, 123 / scala);
	}

	private void sendMessage(String message) {
		try
		{
			Settings.playerWriter.write(message);
			Settings.playerWriter.newLine();
			Settings.playerWriter.flush();
		}
		catch (IOException ex)
		{
			System.out.println(ex.getMessage());
		}
	}
}
