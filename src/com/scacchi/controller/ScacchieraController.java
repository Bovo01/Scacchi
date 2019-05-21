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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 *
 * @author Pietro
 */
public class ScacchieraController implements Initializable {

	@FXML
	protected Label turno;
	@FXML
	protected Canvas canvas;
	@FXML
	protected ImageView scacchiera;

	public Partita partita;
	protected GraphicsContext graphics;
	protected Posizione pos1, pos2;
	protected Colore versoScacchiera;
	protected static final double SCACCHIERA_DIM = 1080;
	protected double scala;//Scala invertita
	protected static final Image SCACCHIERA = new Image("com/scacchi/view/img/scacchiera.jpg");
	protected static final Image SCACCHIERA_INV = new Image("com/scacchi/view/img/scacchiera_inv.jpg");
	protected static final Image SCACCHI = new Image("com/scacchi/view/img/scacchi.png");

	@FXML
	protected void click(MouseEvent event) {
		double x = event.getSceneX() * scala;
		double y = event.getSceneY() * scala;
		if (x < 48 || x > 1031 || y < 48 || y > 1031)
			return;
		if (partita.getTurno() == null)
			return;
		int y1 = (int) ((y - 48) / 123);
		int x1 = (int) ((x - 48) / 123);
		muovi(x1, y1);
	}

	protected void muovi(int x, int y) {
		if (pos1 == null)
		{
			if (versoScacchiera == BIANCO)
				pos1 = new Posizione(Riga.values()[y], Colonna.values()[x]);
			else
				pos1 = new Posizione(Riga.values()[7 - y], Colonna.values()[7 - x]);
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
				pos2 = new Posizione(Riga.values()[y], Colonna.values()[x]);
			else
				pos2 = new Posizione(Riga.values()[7 - y], Colonna.values()[7 - x]);
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
	protected void inverti(ActionEvent event) {
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
	protected void restart(ActionEvent event) {
		pos1 = null;
		pos2 = null;
		partita = new Partita();
		versoScacchiera = BIANCO;
		mostraScacchi();
	}

	@FXML
	private void salvaCaricaElimina(ActionEvent event) {
		String userName = System.getProperty("user.name");
		File folder = new File("C:" + File.separator + "Users" + File.separator + userName + File.separator + "Documents" + File.separator + "My Games");
		if(!folder.exists())
			folder.mkdirs();
		folder = new File(folder, "Scacchi");
		if(!folder.exists())
			folder.mkdirs();
		folder = new File(folder, "Singleplayer");
		Alert alert = new Alert(Alert.AlertType.NONE);
		alert.setContentText("Vuoi salvare o caricare una partita?");
		ButtonType SALVA = new ButtonType("Salva");
		ButtonType CARICA = new ButtonType("Carica");
		ButtonType ELIMINA = new ButtonType("Elimina");
		ButtonType OK = new ButtonType("Ok");
		ButtonType ANNULLA = new ButtonType("Annulla");
		alert.getButtonTypes().addAll(SALVA, CARICA, ELIMINA, ANNULLA);
		Optional<ButtonType> scelta = alert.showAndWait();
		if(scelta.get() == ANNULLA)
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
		else if(scelta.get() == CARICA)
		{
			ListView<String> listaFileDaCaricare = new ListView<>();
			File[] files = folder.listFiles();
			if(files == null || files.length == 0)
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
				this.partita = (Partita) ois.readObject();
				ois.close();
				mostraScacchi();
				FunctionsController.alertInfo("Successo", "Partita caricata con successo");
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
			if(files == null || files.length == 0)
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
	protected void menu(ActionEvent event) throws IOException {
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
	
	public void mostraScacchi() {
		graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		mostraScacchi(this.graphics, this.canvas, this.partita, this.turno, this.versoScacchiera, this.scala, partita.getUltimaMossa());
	}
	
	public static void mostraScacchi(Canvas canvas, Partita partita) {
		GraphicsContext context = canvas.getGraphicsContext2D();
		context.drawImage(SCACCHIERA, 0, 0, canvas.getWidth(), canvas.getHeight());
		mostraScacchi(context, canvas, partita, null, BIANCO, SCACCHIERA_DIM / canvas.getWidth(), null);
	}

	private static void mostraScacchi(GraphicsContext graphics, Canvas canvas, Partita partita, Label turno, Colore versoScacchiera, double scala, Mossa ultimaMossa) {
		if(turno != null)
			if (partita.getTurno() == null)
				turno.setText("PARTITA CONCLUSA!");
			else
				turno.setText(partita.getTurno().toString());
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
					dx = ((7 - pezzo.getPosizione().getColonna().ordinal()) * 123 + 48) / scala;
					dy = ((7 - pezzo.getPosizione().getRiga().ordinal()) * 123 + 48) / scala;
				}
				graphics.fillRect(dx, dy, dw, dh);
				disegnaPezzo(partita.trovaPezzo(pezzo.getPosizione()), scala, graphics, versoScacchiera);
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
				disegnaPezzo(partita.trovaPezzo(pezzo.getPosizione()), scala, graphics, versoScacchiera);
			}
		}
		for (Pezzo p : unione)
		{
			disegnaPezzo(p, scala, graphics, versoScacchiera);
		}
	}

	protected static void disegnaPezzo(Pezzo p, double scala, GraphicsContext graphics, Colore versoScacchiera) {
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

	protected void mostraMosse(Pezzo p) {
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

	protected void disegnaPezzo(Pezzo pezzo) {
		disegnaPezzo(pezzo, this.scala, this.graphics, this.versoScacchiera);
	}
}
