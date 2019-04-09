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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 *
 * @author Pietro
 */
public class ScacchieraController implements Initializable {

	@FXML
	private Label turno;
	@FXML
	private Canvas canvas;
	@FXML
	private ImageView scacchiera;

	private Partita partita;
	private GraphicsContext graphics;
	private Posizione pos1, pos2;
	private Colore versoScacchiera;
	private static final double SCACCHIERA_DIM = 1080;
	private double scala;//Scala invertita
	private static final Image SCACCHIERA = new Image("com/scacchi/view/img/scacchiera.jpg");
	private static final Image SCACCHIERA_INV = new Image("com/scacchi/view/img/scacchiera_inv.jpg");
	private static final Image SCACCHI = new Image("com/scacchi/view/img/scacchi.png");

	@FXML
	private void click(MouseEvent event) {
		double x = event.getSceneX() * scala;
		double y = event.getSceneY() * scala;
		if (x < 48 || x > 1031 || y < 48 || y > 1031)
			return;
		if (partita.getTurno() == null)
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
					partita.getMosse().get(partita.getMosse().size() - 1).setSimbolo(pezzo.getSimbolo());
				}
				if (partita.getTurno() == null)//Se c'è un vincitore il metodo "muovi" imposta il turno a null
				{
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					String line =  partita.comeEFinita();
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
	private void inverti(ActionEvent event) {
		if (versoScacchiera == BIANCO)
		{
			versoScacchiera = NERO;
			scacchiera.setImage(SCACCHIERA_INV);
		}
		else
		{
			versoScacchiera = BIANCO;
			scacchiera.setImage(SCACCHIERA);
		}
		mostraScacchi();
		if (pos1 != null)
			mostraMosse(partita.trovaPezzo(pos1));
	}

	@FXML
	private void restart(ActionEvent event) {
		pos1 = null;
		pos2 = null;
		partita = new Partita();
		versoScacchiera = BIANCO;
		mostraScacchi();
	}

	@FXML
	private void menu(ActionEvent event) throws IOException {
		Node node = (Node) event.getSource();
		Stage stage = (Stage) node.getScene().getWindow();
		Scene scene = stage.getScene();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/scacchi/view/MenuPrincipale.fxml"));
		try
		{
			Parent root = (Parent) fxmlLoader.load();
			node.getScene().getWindow().setHeight(400 + 39);
			node.getScene().getWindow().setWidth(600 + 16);
			scene.setRoot(root);
		}
		catch (IOException ex)
		{
		}
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		graphics = canvas.getGraphicsContext2D();
		partita = new Partita();
		scala = SCACCHIERA_DIM / canvas.getWidth();
		versoScacchiera = BIANCO;
		mostraScacchi();
	}

	private void mostraScacchi() {
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
		if (partita.isScacco(BIANCO))//Verde a quelli che mettono in scacco
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
					dx = ((7-pezzo.getPosizione().getColonna().ordinal()) * 123 + 48) / scala;
					dy = ((7-pezzo.getPosizione().getRiga().ordinal()) * 123 + 48) / scala;
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
					dx = ((7-pezzo.getPosizione().getColonna().ordinal()) * 123 + 48) / scala;
					dy = ((7-pezzo.getPosizione().getRiga().ordinal()) * 123 + 48) / scala;
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
}
