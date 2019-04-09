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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import javafx.application.Platform;

/**
 *
 * @author bovolenta.pietro
 *
 */
public class ThreadAccetta extends Thread implements Closeable {

	private final int port;
	private final ThreadRicevi threadRicevi;
	private final OnlineController controller;
	private boolean isFinito;

	public ThreadAccetta(int port, OnlineController controller) {
		this.port = port;
		this.controller = controller;
		threadRicevi = new ThreadRicevi();
	}

	@Override
	public void run() {
		if (Settings.player != null)
			threadRicevi.start();
		ServerSocket server;
		try
		{
			server = new ServerSocket(port);
		}
		catch (IOException ex)
		{
			Platform.runLater(() ->
			{
				FunctionsController.alertErrore("Porta già in uso da un altro processo");
				controller.sbloccaTutto();
			});
			return;
		}
		Platform.runLater(() -> Settings.schieramento = controller.scegliSchieramento());
		while (true)
		{
			try
			{
				Socket socket = server.accept();

				InputStream is = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);

				String line = br.readLine();

				OutputStream os = socket.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(os);
				BufferedWriter bw = new BufferedWriter(osw);

				if (line.equals("richiesta"))
					if (Settings.player == null)
					{
						bw.write("richiesta accettata\n");
						bw.flush();
						if (Settings.schieramento == Pezzo.Colore.BIANCO)
						{
							bw.write("nero\n");
							bw.flush();
						}
						else if (Settings.schieramento == Pezzo.Colore.NERO)
						{
							bw.write("bianco\n");
							bw.flush();
						}
						else
						{
							bw.write("richiesta colore\n");
							bw.flush();
							line = br.readLine();
							if(line.equals("bianco"))
								Settings.schieramento = Pezzo.Colore.BIANCO;
							else
								Settings.schieramento = Pezzo.Colore.NERO;
						}
						Settings.player = socket;
						Settings.playerReader = br;
						Settings.playerWriter = bw;
						threadRicevi.start();
						Platform.runLater(() -> controller.inizioPartita());
					}
					else
					{
						bw.write("richiesta rifiutata\n");
						bw.flush();
						bw.close();
						br.close();
						socket.close();
					}
				else if (line.equals("richiesta spettatore"))
				{
					bw.write("richiesta accettata\n");
					bw.flush();
					Settings.spettatori.add(socket);
					Settings.spettatoriReaders.add(br);
					Settings.spettatoriWriters.add(bw);
				}
			}
			catch (IOException ex)
			{
				System.out.println(ex.getMessage());
			}
		}
	}

	@Override
	public void close() throws IOException {//TODO chiusura thread
		if(threadRicevi != null)
			threadRicevi.close();
		if (Settings.playerReader != null)
			Settings.playerReader.close();
		if (Settings.playerWriter != null)
			Settings.playerWriter.close();
		if (Settings.player != null)
			Settings.player.close();
		if (Settings.spettatoriReaders != null)
			for (BufferedReader br : Settings.spettatoriReaders)
			{
				br.close();
			}
		if (Settings.spettatoriWriters != null)
			for (BufferedWriter bw : Settings.spettatoriWriters)
			{
				bw.close();
			}
		if (Settings.spettatori != null)
			for (Socket socket : Settings.spettatori)
			{
				socket.close();
			}
		Settings.player = null;
		Settings.playerReader = null;
		Settings.playerWriter = null;
		Settings.spettatori = null;
		Settings.spettatoriReaders = null;
		Settings.spettatoriWriters = null;
		//Blocco di thread
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	private class ThreadRicevi extends Thread implements Closeable {

		@Override
		public void run() {
			Platform.runLater(() -> controller.inizioPartita());
			while (true)
			{
				try
				{
					String line = Settings.playerReader.readLine();
					if (line.equals("resa"))
						Platform.runLater(() -> Settings.scacchieraOnlineController.finePartita("resa", Settings.schieramento));
					else if (line.substring(0, 5).equals("mossa"))
					{
						String temp = line.substring(6, line.length());//lunghezza di 5 caratteri
						Posizione pos1 = new Posizione(Posizione.Riga.values()[8 - Character.getNumericValue(temp.charAt(1))], Posizione.Colonna.getFromChar(temp.charAt(0)));
						Posizione pos2 = new Posizione(Posizione.Riga.values()[8 - Character.getNumericValue(temp.charAt(3))], Posizione.Colonna.getFromChar(temp.charAt(2)));
						Platform.runLater(() -> Settings.partita.muovi(Settings.partita.trovaPezzo(pos1), pos2));
						if (temp.charAt(4) != '0')
						{
							Platform.runLater(() -> Settings.partita.promozione(Settings.partita.trovaPezzo(pos2), Pezzo.Simbolo.values()[Integer.parseInt(Character.toString(temp.charAt(4)))]));
						}
						Platform.runLater(() -> Settings.scacchieraOnlineController.mostraScacchi());
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
					System.out.println(ex.getMessage());
				}
			}
		}

		@Override
		public void close() throws IOException {//TODO chiusura thread
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
	}
}
