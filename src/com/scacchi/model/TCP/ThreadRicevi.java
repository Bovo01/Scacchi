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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

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
		boolean isFinitaPerSpettatore = false;
		while (true)
		{
			try
			{
				String line = Settings.playerReader.readLine();
				if (line == null)
				{
					if (Settings.scacchieraOnlineController != null)
					{
						ThreadRicevi.sendToSpettatori("fine");
						ThreadRicevi.sendToSpettatori(Settings.schieramento.toString());
						ThreadRicevi.sendToSpettatori("resa" + Settings.schieramento.notThis().toString());
						Platform.runLater(() ->
						{
							Settings.scacchieraOnlineController.disattivaBottoni();
							if (Settings.partita.getTurno() == null || Settings.partita.getMosse().isEmpty())
								FunctionsController.alertInfo("info", "abbandonato");
							else
								FunctionsController.alertInfo("vinto", "abbandonato");
							Settings.partita.fine();
							if (Settings.threadAccetta != null)
								try
								{
									Settings.threadAccetta.close();
								}
								catch (IOException ex)
								{
									Logger.getLogger(ThreadRicevi.class.getName()).log(Level.SEVERE, null, ex);
								}
						});
					}
					else if (Settings.threadAccetta == null && !isFinitaPerSpettatore)
						Platform.runLater(() ->
						{
							FunctionsController.alertInfo("finePartita", "resa" + Settings.schieramento.toString());
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
					String vincitore = Settings.playerReader.readLine();
					String comeEFinita = Settings.playerReader.readLine();
					if (!Settings.partita.isFinita())
					{
						isFinitaPerSpettatore = true;
						Platform.runLater(() ->
						{
							if (vincitore == null || vincitore.equals("null"))
								FunctionsController.alertInfo("patta", comeEFinita);
							else
								FunctionsController.alertInfo("finePartita", comeEFinita, "vincitore", vincitore.toLowerCase());
							Settings.partita.fine();
							Settings.scacchieraOnlineSpettatoriController.mostraScacchi();
						});
					}
				}
				else if (line.substring(0, 5).equals("mossa"))
				{
					String temp = line.substring(6, line.length());//lunghezza di 5 caratteri
					Posizione pos1 = new Posizione(Posizione.Riga.values()[8 - Character.getNumericValue(temp.charAt(1))], Posizione.Colonna.getFromChar(temp.charAt(0)));
					Posizione pos2 = new Posizione(Posizione.Riga.values()[8 - Character.getNumericValue(temp.charAt(3))], Posizione.Colonna.getFromChar(temp.charAt(2)));
					Settings.partita.muovi(Settings.partita.trovaPezzo(pos1), pos2);
					if (temp.charAt(4) != '0')
						Settings.partita.promozione(Settings.partita.trovaPezzo(pos2), Pezzo.Simbolo.values()[Integer.parseInt(Character.toString(temp.charAt(4)))]);
					if (Settings.scacchieraOnlineController != null)
					{
						sendToSpettatori(line);
						Platform.runLater(() ->
						{
							Settings.scacchieraOnlineController.mostraScacchi();
							if (Settings.partita.isFinita())
							{
								Settings.scacchieraOnlineController.resa.setDisable(true);
								Settings.scacchieraOnlineController.patta.setDisable(true);
								if (Settings.partita.vincitore() == null)
									FunctionsController.alertInfo("patta", Settings.partita.comeEFinita());
								else
									FunctionsController.alertInfo("finePartita", Settings.partita.comeEFinita(), "vincitore", Settings.partita.vincitore().toString().toLowerCase());
								sendToSpettatori("fine");
								sendToSpettatori(Settings.partita.vincitore() == null ? "null" : Settings.partita.vincitore().toString());
								sendToSpettatori(Settings.partita.comeEFinita());
							}
						});
					}
					else if (Settings.scacchieraOnlineSpettatoriController != null)
					{
						if (Settings.partita.isFinita())
							if (Settings.partita.vincitore() == null)
								Platform.runLater(() -> FunctionsController.alertInfo("patta", Settings.partita.comeEFinita()));
							else
								Platform.runLater(() -> FunctionsController.alertInfo("finePartita", Settings.partita.comeEFinita(), "vincitore", Settings.partita.vincitore().toString().toLowerCase()));
						Platform.runLater(() -> Settings.scacchieraOnlineSpettatoriController.mostraScacchi());
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
						FunctionsController.alertInfo("pattaRifiutata", "pattaRifiutata");
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
						FunctionsController.alertInfo("restart", "restartAccettato");
						if (Settings.scacchieraOnlineController != null)
						{
							sendToSpettatori(line);
							Settings.scacchieraOnlineController.ricomincia();
							Settings.scacchieraOnlineController.attivaBottoni();
						}
						else if (Settings.scacchieraOnlineSpettatoriController != null)
							Settings.scacchieraOnlineSpettatoriController.ricomincia();
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
						FunctionsController.alertInfo("restartRifiutato", "restartRifiutato");
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
					{//Dovrei aver risolto questo problema ma non si sa mai
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
						});
					else
						Platform.runLater(() -> FunctionsController.alertErrore("problemaCaricamento"));
				}
				else if (line.equals("conferma caricamento"))
					Platform.runLater(() ->
					{
						FunctionsController.alertInfo("caricato", "partitaCaricata");
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
							Settings.scacchieraOnlineSpettatoriController.ricomincia();
					});
				else if (line.equals("rifiuto caricamento"))
				{
					Settings.partitaDaCaricare = null;
					Settings.scacchieraOnlineController.attivaBottoni();
					Platform.runLater(() -> FunctionsController.alertInfo("caricamentoRifiutato", "caricamentoRifiutato"));
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
				if (Settings.scacchieraOnlineController != null)
				{
					sendToSpettatori("fine");
					sendToSpettatori(Settings.schieramento.notThis().toString());
					sendToSpettatori("resa " + Settings.schieramento.toString());
				}
				return;
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
