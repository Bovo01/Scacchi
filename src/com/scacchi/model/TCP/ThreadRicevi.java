/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi.model.TCP;

import com.scacchi.controller.FunctionsController;
import com.scacchi.controller.OnlineController;
import com.scacchi.model.Partita;
import com.scacchi.model.Pezzo;
import static com.scacchi.model.Pezzo.Colore.BIANCO;
import static com.scacchi.model.Pezzo.Colore.NERO;
import com.scacchi.model.Posizione;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 *
 * @author Pietro
 */
public class ThreadRicevi extends Thread implements Closeable {

	private OnlineController controller;
	private boolean isFinito;
	private final boolean isSpettatore;

	public ThreadRicevi(OnlineController controller) {
		this.controller = controller;
		this.isSpettatore = false;
	}

	public ThreadRicevi(OnlineController controller, boolean isSpettatore) {
		this.controller = controller;
		this.isSpettatore = isSpettatore;
	}

	public ThreadRicevi() {
		this.isSpettatore = false;
	}

	@Override
	public void run() {
		if (controller != null && !isSpettatore)
			Platform.runLater(() -> controller.inizioPartita());
		while (true)
		{
			try
			{
				String line = Settings.playerReader.readLine();
				if (line == null)
				{
					if (Settings.scacchieraOnlineController != null)
						Platform.runLater(() ->
						{
							Settings.scacchieraOnlineController.disattivaBottoni();
							Alert alert = new Alert(Alert.AlertType.INFORMATION);
							if (Settings.partita.getTurno() == null || Settings.partita.getMosse().isEmpty())
								alert.setTitle("Info");
							else
								alert.setTitle("Hai vinto!");
							alert.setContentText("L'avversario ha abbandonato la partita");
							alert.show();
							Settings.partita.fine();
						});
					if (Settings.threadAccetta != null)
						Settings.threadAccetta.close();
					else
						Platform.runLater(() ->
						{
							FunctionsController.alertInfo("Fine partita!", "Il " + Settings.schieramento.notThis().toString().toLowerCase() + " ha vinto per resa");
							Settings.partita.fine();
							Settings.scacchieraOnlineSpettatoriController.mostraScacchi();
						});
					return;
				}
				if (line.equals("resa"))
				{
					String line2 = Settings.scacchieraOnlineSpettatoriController != null ? Settings.playerReader.readLine() : null;
					Platform.runLater(() ->
					{
						if (Settings.scacchieraOnlineController != null)
						{
							sendToSpettatori("resa");
							sendToSpettatori(Settings.schieramento.toString().toLowerCase());
							Settings.scacchieraOnlineController.finePartita("resa", Settings.schieramento);
							Settings.scacchieraOnlineController.resa.setDisable(true);
							Settings.scacchieraOnlineController.patta.setDisable(true);
						}
						else if (Settings.scacchieraOnlineSpettatoriController != null)
							Settings.scacchieraOnlineSpettatoriController.finePartita("resa", line2.equals("bianco") ? Pezzo.Colore.BIANCO : Pezzo.Colore.NERO);
					});
				}
				else if (line.equals("fine"))//Messaggio solo per gli spettatori
				{
					String line2 = Settings.playerReader.readLine();
					Platform.runLater(() ->
					{
						Alert alert = new Alert(Alert.AlertType.INFORMATION);
						alert.setTitle("Fine partita!");
						if (line2 == null || line2.equals("null"))
							alert.setContentText("Partita finita per patta");
						else
							alert.setContentText("Vincitore: " + line2.toLowerCase());
						alert.show();
						Settings.partita.fine();
						Settings.scacchieraOnlineSpettatoriController.mostraScacchi();
					});
				}
				else if (line.substring(0, 5).equals("mossa"))
				{
					String temp = line.substring(6, line.length());//lunghezza di 5 caratteri
					Posizione pos1 = new Posizione(Posizione.Riga.values()[8 - Character.getNumericValue(temp.charAt(1))], Posizione.Colonna.getFromChar(temp.charAt(0)));
					Posizione pos2 = new Posizione(Posizione.Riga.values()[8 - Character.getNumericValue(temp.charAt(3))], Posizione.Colonna.getFromChar(temp.charAt(2)));
					Platform.runLater(() -> Settings.partita.muovi(Settings.partita.trovaPezzo(pos1), pos2));
					if (temp.charAt(4) != '0')
						Platform.runLater(() -> Settings.partita.promozione(Settings.partita.trovaPezzo(pos2), Pezzo.Simbolo.values()[Integer.parseInt(Character.toString(temp.charAt(4)))]));
					if (Settings.scacchieraOnlineController != null)
						Platform.runLater(() ->
						{
							Settings.scacchieraOnlineController.mostraScacchi();
							if (Settings.partita.isFinita())
							{
								Settings.scacchieraOnlineController.resa.setDisable(true);
								Settings.scacchieraOnlineController.patta.setDisable(true);
								Alert alert = new Alert(Alert.AlertType.INFORMATION);
								if (Settings.partita.vincitore() == null)
								{
									alert.setTitle("Patta!");
									alert.setContentText("Partita finita per " + Settings.partita.comeEFinita());
								}
								else
								{
									alert.setTitle("Fine partita!");
									alert.setContentText(Settings.partita.comeEFinita() + "\nVincitore: " + Settings.partita.vincitore().toString().toLowerCase());
								}
								alert.show();
								sendToSpettatori("fine");
								sendToSpettatori(Settings.partita.vincitore() == null ? "null" : Settings.partita.vincitore().toString().toLowerCase());
							}
						});
					else if (Settings.scacchieraOnlineSpettatoriController != null)
						Platform.runLater(() -> Settings.scacchieraOnlineSpettatoriController.mostraScacchi());
					if (Settings.spettatoriWriters != null)
					{
						ArrayList<Integer> indexesToRemove = new ArrayList<>();
						Iterator<BufferedWriter> it = Settings.spettatoriWriters.iterator();
						while (it.hasNext())
						{
							BufferedWriter bw = it.next();
							try
							{
								bw.write(line);
								bw.newLine();
								bw.flush();
								if (Settings.partita.isFinita())
								{
									bw.write("fine\n");
									bw.flush();
									bw.write(Settings.partita.vincitore() == null ? "null" : Settings.partita.vincitore().toString().toLowerCase());
									bw.newLine();
									bw.flush();
								}
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
								Settings.spettatoriWriters.remove(index.intValue());
								Settings.spettatori.remove(index.intValue());
								Settings.spettatoriReaders.remove(index.intValue());
								Settings.spettatoriOOS.remove(index.intValue());
							}
							catch (IOException ex1)
							{
							}
						}
					}
				}
				else if (line.equals("richiesta patta"))
					Platform.runLater(() ->
					{
						boolean isPatta = Settings.scacchieraOnlineController.richiestaPatta();
						Settings.scacchieraOnlineController.confermaPatta(isPatta);
					});
				else if (line.equals("conferma patta"))
					Platform.runLater(() ->
					{
						if (Settings.scacchieraOnlineController != null)
						{
							sendToSpettatori(line);
							Settings.scacchieraOnlineController.finePartita("patta", null);
							Settings.scacchieraOnlineController.restart.setDisable(false);
							Settings.scacchieraOnlineController.salvaCaricaElimina.setDisable(false);
						}
						else if (Settings.scacchieraOnlineSpettatoriController != null)
							Settings.scacchieraOnlineSpettatoriController.finePartita("patta", null);
					});
				else if (line.equals("rifiuto patta"))
					Platform.runLater(() ->
					{
						Settings.scacchieraOnlineController.attivaBottoni();
						FunctionsController.alertInfo("Patta rifiutata", "La tua richiesta di patta è stata rifiutata");
					});
				else if (line.equals("richiesta restart"))
					Platform.runLater(() ->
					{
						boolean isRestart = Settings.scacchieraOnlineController.richiestaRestart();
						Settings.scacchieraOnlineController.confermaRestart(isRestart);
					});
				else if (line.equals("conferma restart"))
					Platform.runLater(() ->
					{
						if (Settings.scacchieraOnlineController != null)
						{
							sendToSpettatori(line);
							Settings.scacchieraOnlineController.ricomincia();
							Settings.scacchieraOnlineController.attivaBottoni();
						}
						else if (Settings.scacchieraOnlineSpettatoriController != null)
						{
							Settings.scacchieraOnlineSpettatoriController.ricomincia();
							Platform.runLater(() -> FunctionsController.alertInfo("Restart", "La partita è ricominciata"));
						}
					});
				else if (line.equals("rifiuto restart"))
					Platform.runLater(() ->
					{
						if (Settings.partita.getTurno() != null)
							Settings.scacchieraOnlineController.attivaBottoni();
						else
						{
							Settings.scacchieraOnlineController.restart.setDisable(false);
							Settings.scacchieraOnlineController.salvaCaricaElimina.setDisable(false);
						}
						FunctionsController.alertInfo("Restart rifiutato", "La tua richiesta di ricominciare è stata rifiutata");
					});
				else if (line.equals("richiesta caricamento"))
				{
					try
					{
						if (Settings.playerOIS == null)
							Settings.playerOIS = new ObjectInputStream(Settings.player.getInputStream());
						Settings.partitaDaCaricare = (Partita) Settings.playerOIS.readObject();
					}
					catch (ClassNotFoundException ex)
					{//La classe sarà sempre corretta
					}
					catch (IOException ex)
					{
						String temp;
						while ((temp = Settings.playerReader.readLine()) != null)
						{//Svuoto il buffer buggato dell'oggetto partita in stringhe
						}
						Settings.playerWriter.write("rifiuta caricamento\n");
						Settings.playerWriter.flush();
					}
					if (Settings.partitaDaCaricare != null)
						Platform.runLater(() ->
						{
							boolean isCaricato = Settings.scacchieraOnlineController.richiestaCaricamento();
							Settings.scacchieraOnlineController.confermaCaricamento(isCaricato);
							if (isCaricato)
								sendObjectToSpettatori(Settings.partitaDaCaricare);
						});
					else
						Platform.runLater(() -> FunctionsController.alertErrore("C'è stato un problema nel caricamento"));
				}
				else if (line.equals("conferma caricamento"))
					Platform.runLater(() ->
					{
						if (Settings.scacchieraOnlineController != null)
						{
							sendToSpettatori(line);
							Settings.partita = Settings.partitaDaCaricare;
							Settings.partitaDaCaricare = null;
							Settings.scacchieraOnlineController.partita = Settings.partita;
							Settings.scacchieraOnlineController.mostraScacchi();
							Settings.scacchieraOnlineController.attivaBottoni();
						}
						else if (Settings.scacchieraOnlineSpettatoriController != null)
						{
							Settings.scacchieraOnlineSpettatoriController.ricomincia();
							FunctionsController.alertInfo("Caricamento", "La partita è stata caricata");
						}
					});
				else if (line.equals("rifiuto caricamento"))
				{
					Settings.partitaDaCaricare = null;
					Settings.scacchieraOnlineController.attivaBottoni();
				}
				else if (line.equals("iniziata")) //Messaggio per spettatori
				{
					Settings.partita = new Partita();
					Settings.schieramento = Settings.playerReader.readLine().equals("bianco") ? BIANCO : NERO;
					Platform.runLater(() -> controller.inizioSpettatore());
				}
			}
			catch (IOException ex)
			{
				if (ex.getMessage().equals("Socket closed") || isFinito)
					return;
				Logger logger = Logger.getAnonymousLogger();
				logger.log(Level.SEVERE, "an exception was thrown", ex);
			}
		}
	}

	public static void sendToSpettatori(String message) {
		if (Settings.spettatoriWriters != null)
		{
			ArrayList<Integer> indexesToRemove = new ArrayList<>();
			for (BufferedWriter bw : Settings.spettatoriWriters)
			{
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
					Settings.spettatoriWriters.remove(index.intValue());
					Settings.spettatori.remove(index.intValue());
					Settings.spettatoriReaders.remove(index.intValue());
					Settings.spettatoriOOS.remove(index.intValue());
				}
				catch (IOException ex)
				{
				}
			}
		}
	}

	@Override
	public void close() throws IOException {
		isFinito = true;
		if (Settings.player != null && !Settings.player.isClosed())
			Settings.player.close();
		Settings.player = null;
		Settings.playerReader = null;
		Settings.playerWriter = null;
		Settings.playerOOS = null;
		Settings.playerOIS = null;
	}

	public static void sendObjectToSpettatori(Partita partita) {
		if (Settings.spettatoriWriters != null)
		{
			ArrayList<Integer> indexesToRemove = new ArrayList<>();
			for (ObjectOutputStream oos : Settings.spettatoriOOS)
			{
				try
				{
					oos.writeObject(partita);
					oos.flush();
				}
				catch (IOException ex)
				{
					indexesToRemove.add(Settings.spettatoriWriters.indexOf(oos));
				}
			}
			for (Integer index : indexesToRemove)
			{
				try
				{
					Settings.spettatori.get(index).close();
					Settings.spettatoriWriters.remove(index.intValue());
					Settings.spettatori.remove(index.intValue());
					Settings.spettatoriReaders.remove(index.intValue());
					Settings.spettatoriOOS.remove(index.intValue());
				}
				catch (IOException ex)
				{
				}
			}
		}
	}
}
