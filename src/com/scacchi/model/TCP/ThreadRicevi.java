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
		if(controller != null)
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
					Platform.runLater(() -> Settings.scacchieraOnlineController.finePartita("resa", Settings.schieramento));
				else if (line.substring(0, 5).equals("mossa"))
				{
					String temp = line.substring(6, line.length());//lunghezza di 5 caratteri
					Posizione pos1 = new Posizione(Posizione.Riga.values()[8 - Character.getNumericValue(temp.charAt(1))], Posizione.Colonna.getFromChar(temp.charAt(0)));
					Posizione pos2 = new Posizione(Posizione.Riga.values()[8 - Character.getNumericValue(temp.charAt(3))], Posizione.Colonna.getFromChar(temp.charAt(2)));
					Platform.runLater(() -> Settings.partita.muovi(Settings.partita.trovaPezzo(pos1), pos2));
					if (temp.charAt(4) != '0')
						Platform.runLater(() -> Settings.partita.promozione(Settings.partita.trovaPezzo(pos2), Pezzo.Simbolo.values()[Integer.parseInt(Character.toString(temp.charAt(4)))]));
					if(Settings.scacchieraOnlineController != null)
						Platform.runLater(() -> Settings.scacchieraOnlineController.mostraScacchi());
					else if(Settings.scacchieraOnlineSpettatoriController != null)
						Platform.runLater(() -> Settings.scacchieraOnlineSpettatoriController.mostraScacchi());
					if (Settings.spettatoriWriters != null)
						for (BufferedWriter bw : Settings.spettatoriWriters)
						{
							bw.write(line);
							bw.newLine();
							bw.flush();
						}
				}
				else if (line.equals("richiesta patta"))
					Platform.runLater(() ->
					{
						boolean isPatta = Settings.scacchieraOnlineController.richiestaPatta();
						Settings.scacchieraOnlineController.confermaPatta(isPatta);
					});
				else if (line.equals("conferma patta"))
					Platform.runLater(() -> Settings.scacchieraOnlineController.finePartita("patta", null));
				else if (line.equals("rifiuto patta"))
					Platform.runLater(() -> FunctionsController.alertInfo("Patta rifiutata", "La tua richiesta di patta è stata rifiutata"));
				else if (line.equals("richiesta restart"))
					Platform.runLater(() ->
					{
						boolean isRestart = Settings.scacchieraOnlineController.richiestaRestart();
						Settings.scacchieraOnlineController.confermaRestart(isRestart);
					});
				else if (line.equals("conferma restart"))
					Platform.runLater(() -> Settings.scacchieraOnlineController.ricomincia());
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
		if(Settings.player != null && !Settings.player.isClosed())
			Settings.player.close();
	}
}
