/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi.model.TCP;

import com.scacchi.controller.FunctionsController;
import com.scacchi.controller.OnlineController;
import com.scacchi.model.Pezzo;
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
import java.util.ArrayList;
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
	private ServerSocket server;

	public ThreadAccetta(int port, OnlineController controller) {
		this.port = port;
		this.controller = controller;
		threadRicevi = new ThreadRicevi(controller);
	}

	@Override
	public void run() {
		if (Settings.player != null)
			threadRicevi.start();
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
		if (Settings.schieramento == null)
			Platform.runLater(() -> Settings.schieramento = controller.scegliSchieramento("Non cambia nulla"));
		while (true)
		{
			try
			{
				Socket socket = server.accept();
				if (isFinito)
				{
					socket.close();
					server.close();
					Settings.schieramento = null;
					return;
				}

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
						if (Settings.schieramento == Pezzo.Colore.BIANCO)//TODO fai meglio
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
							if (line.equals("bianco"))
								Settings.schieramento = Pezzo.Colore.BIANCO;
							else
								Settings.schieramento = Pezzo.Colore.NERO;
						}
						Settings.player = socket;
						Settings.playerReader = br;
						Settings.playerWriter = bw;
						threadRicevi.start();
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
					bw.write(Settings.partita.toString());//Invio partita
					bw.newLine();
					bw.flush();
					bw.write(Settings.partita.getTurno().toString().toLowerCase());//Invio turno attuale
					bw.newLine();
					bw.flush();
					bw.write(Settings.partita.getUltimaMossa().toString());//Invio l'ultima mossa
					bw.newLine();
					bw.flush();
					bw.write(Settings.schieramento.toString().toLowerCase());//Invio schieramento
					bw.newLine();
					bw.flush();
					if(Settings.spettatori == null)
						Settings.spettatori = new ArrayList<>();
					if(Settings.spettatoriReaders == null)
						Settings.spettatoriReaders = new ArrayList<>();
					if(Settings.spettatoriWriters == null)
						Settings.spettatoriWriters = new ArrayList<>();
					Settings.spettatori.add(socket);
					Settings.spettatoriReaders.add(br);
					Settings.spettatoriWriters.add(bw);
				}
			}
			catch (IOException ex)
			{
				if(isFinito)
					return;
				Platform.runLater(() -> FunctionsController.alertErrore(ex.getMessage()));
			}
		}
	}

	@Override
	public void close() throws IOException {
		isFinito = true;
		if (threadRicevi != null && threadRicevi.isAlive())
			threadRicevi.close();
		server.close();
		if (Settings.player != null && !Settings.player.isClosed())
			Settings.player.close();
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
		Settings.schieramento = null;
		Settings.threadAccetta = null;
		Platform.runLater(() -> {
			if(Settings.scacchieraOnlineController == null)
				return;
			Settings.partita.fine();
			Settings.scacchieraOnlineController.mostraScacchi();
		});
	}
}
