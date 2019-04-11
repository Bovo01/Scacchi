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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;
import javafx.application.Platform;

/**
 *
 * @author Pietro
 */
public class ThreadSend extends Thread {

		private final String address;
		private final int port;
		private final OnlineController controller;
		private final String message;

		public ThreadSend(String address, int port, OnlineController controller, String message) {
			this.address = address;
			this.port = port;
			this.controller = controller;
			this.message = message;
		}

		@Override
		public void run() {
			try
			{
				Socket socket = new Socket();
				socket.connect(new InetSocketAddress(address, port), 500);

				InputStream is = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);

				OutputStream os = socket.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(os);
				BufferedWriter bw = new BufferedWriter(osw);

				bw.write(message);
				bw.newLine();
				bw.flush();

				String line = br.readLine();
				if(line.equals("richiesta accettata"))
				{
					line = br.readLine();
					if (line.equals("bianco") || line.equals("nero"))
					{
						Settings.player = socket;
						Settings.playerReader = br;
						Settings.playerWriter = bw;
						Settings.threadAccetta = new ThreadAccetta(50000, controller);//TODO parametrizzare la porta con la variabile istanza port
						Settings.threadAccetta.start();
						if (line.equals("bianco"))
							Settings.schieramento = Pezzo.Colore.BIANCO;
						else if (line.equals("nero"))
							Settings.schieramento = Pezzo.Colore.NERO;
					}
					else if(line.equals("richiesta colore"))
					{
						Settings.player = socket;
						Settings.playerReader = br;
						Settings.playerWriter = bw;
						Platform.runLater(() -> {
							try
							{
								Settings.schieramento = controller.scegliSchieramento();
								if(Settings.schieramento == null)
									Settings.schieramento = Pezzo.Colore.values()[Math.abs(new Random().nextInt()) % 2];
								if(Settings.schieramento == Pezzo.Colore.BIANCO)
									bw.write("nero\n");
								else if(Settings.schieramento == Pezzo.Colore.NERO)
									bw.write("bianco\n");
								bw.flush();
								Settings.threadAccetta = new ThreadAccetta(50000, controller);//TODO parametrizzare la porta con la variabile istanza port
								Settings.threadAccetta.start();
							}
							catch (IOException ex)
							{
								FunctionsController.alertErrore("È avvenuto un problema nella connessione");
							}
						});
					}
					else//Spettatore (inviata la partita)
					{
						Settings.partita = new Partita(line);
						Settings.player = socket;
						Settings.playerReader = br;
						Settings.playerWriter = bw;
						Settings.threadRicevi = new ThreadRicevi(controller);
						Platform.runLater(() -> controller.inizioSpettatore());
					}
				}
			}
			catch (IOException ex)
			{
				Platform.runLater(() -> FunctionsController.alertErrore("È avvenuto un problema nella connessione"));
			}
			controller.sbloccaTutto();
		}
	}