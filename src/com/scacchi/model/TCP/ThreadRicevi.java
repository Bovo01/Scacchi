/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi.model.TCP;

import com.scacchi.controller.FunctionsController;
import com.scacchi.controller.OnlineController;
import com.scacchi.model.Pezzo;
import com.scacchi.model.Posizione;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 *
 * @author Pietro
 */
public class ThreadRicevi extends Thread implements Closeable {

	private OnlineController controller;
	private boolean isFinito;

	public ThreadRicevi(OnlineController controller) {
		this.controller = controller;
	}

	public ThreadRicevi() {

	}

	@Override
	public void run() {
		if (controller != null)
			Platform.runLater(() -> controller.inizioPartita());
		while (true)
		{
			try
			{
				String line = Settings.playerReader.readLine();
				if (line == null)
				{
					Platform.runLater(() ->
					{
						Settings.scacchieraOnlineController.disattivaBottoni();
						Alert alert = new Alert(Alert.AlertType.INFORMATION);
						if (Settings.partita.getTurno() == null || Settings.partita.getMosse().isEmpty())
							alert.setTitle("Info");
						else
							alert.setContentText("Hai vinto!");
						alert.setContentText("L'avversario ha abbandonato la partita");
						alert.show();
						Settings.partita.fine();
					});
					Settings.threadAccetta.close();
					return;
				}
				if (line.equals("resa"))
					Platform.runLater(() ->
					{
						Settings.scacchieraOnlineController.finePartita("resa", Settings.schieramento);
						Settings.scacchieraOnlineController.patta.setDisable(true);
						Settings.scacchieraOnlineController.resa.setDisable(true);
					});
				else if(line.equals("fine"))//Messaggio solo per gli spettatori
				{
					Platform.runLater(() -> {
						Alert alert = new Alert(Alert.AlertType.INFORMATION);
						alert.setTitle("Fine partita!");
						alert.setContentText("Partita finita per patta");
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
						Platform.runLater(() -> Settings.scacchieraOnlineController.mostraScacchi());
					else if (Settings.scacchieraOnlineSpettatoriController != null)
					{
//						if(Settings.playerReader.readLine().equals("fine"))
//							Settings.partita.fine();
						Platform.runLater(() -> Settings.scacchieraOnlineSpettatoriController.mostraScacchi());
					}
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
//								if(Settings.partita.isFinita())
//									bw.write("fine\n");
//								else
//									bw.write("niente\n");
//								bw.flush();
							}
							catch (IOException ex)
							{
								indexesToRemove.add(Settings.spettatoriWriters.indexOf(bw));
							}
						}
						for(Integer index : indexesToRemove)
						{
							try
							{
								Settings.spettatori.get(index).close();
							}
							catch (IOException ex1)
							{
							}
							Settings.spettatoriWriters.remove(index);
							Settings.spettatori.remove(index);
							Settings.spettatoriReaders.remove(index);
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
						Settings.scacchieraOnlineController.finePartita("patta", null);
						Settings.scacchieraOnlineController.patta.setDisable(true);
						Settings.scacchieraOnlineController.resa.setDisable(true);
					});
				else if (line.equals("rifiuto patta"))
					Platform.runLater(() -> FunctionsController.alertInfo("Patta rifiutata", "La tua richiesta di patta è stata rifiutata"));
				else if (line.equals("richiesta restart"))
					Platform.runLater(() ->
					{
						boolean isRestart = Settings.scacchieraOnlineController.richiestaRestart();
						Settings.scacchieraOnlineController.confermaRestart(isRestart);
					});
				else if (line.equals("conferma restart"))
					Platform.runLater(() ->
					{
						Settings.scacchieraOnlineController.ricomincia();
						Settings.scacchieraOnlineController.patta.setDisable(false);
						Settings.scacchieraOnlineController.resa.setDisable(false);
					});
				else if (line.equals("rifiuto restart"))
					Platform.runLater(() -> FunctionsController.alertInfo("Restart rifiutato", "La tua richiesta di ricominciare è stata rifiutata"));
			}
			catch (IOException ex)
			{
				if (ex.getMessage().equals("Socket closed") || isFinito)
					return;
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
	}
}
