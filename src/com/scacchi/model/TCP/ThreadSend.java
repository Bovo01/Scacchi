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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;
import javafx.application.Platform;
import javafx.scene.control.ListView;

/**
 *
 * @author Pietro
 */
public class ThreadSend extends Thread {

		private final String address;
		private final int port;
		private final OnlineController controller;
		private final String message;
		private ListView<String> listView;

		public ThreadSend(String address, int port, OnlineController controller, String message) {
			this.address = address;
			this.port = port;
			this.controller = controller;
			this.message = message;
		}
		
		public ThreadSend(String address, int port, OnlineController controller, String message, ListView<String> listView) {
			this.address = address;
			this.port = port;
			this.controller = controller;
			this.message = message;
			this.listView = listView;
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
						if (line.equals("bianco"))
							Settings.schieramento = Pezzo.Colore.BIANCO;
						else
							Settings.schieramento = Pezzo.Colore.NERO;
						Settings.threadAccetta = new ThreadAccetta(50000, controller);//TODO parametrizzare la porta con la variabile istanza port
						Settings.threadAccetta.start();
					}
					else if(line.equals("richiesta colore"))
					{
						Settings.player = socket;
						Settings.playerReader = br;
						Settings.playerWriter = bw;
						Platform.runLater(() -> {
							try
							{
								Settings.schieramento = controller.scegliSchieramento("Casuale");
								if(Settings.schieramento == null)
									Settings.schieramento = Pezzo.Colore.values()[Math.abs(new Random().nextInt()) % 2];
								bw.write(Settings.schieramento.notThis().toString().toLowerCase());
								bw.newLine();
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
				}
				else if(line.equals("richiesta accettata spettatore"))
				{
					Settings.playerOIS = new ObjectInputStream(socket.getInputStream());
					Settings.player = socket;
					Settings.playerReader = br;
					Settings.playerWriter = bw;
					Settings.threadRicevi = new ThreadRicevi(controller, true);
					if(br.readLine().equals("aspetta"))
					{
						Settings.threadRicevi.start();
						Platform.runLater(() -> FunctionsController.alertInfo("Partita non iniziata", "La partita non è ancora iniziata, attendi"));
					}
					else
					{
						Settings.partita = (Partita) Settings.playerOIS.readObject();
						Settings.schieramento = br.readLine().equals("bianco") ? BIANCO : NERO;
						Platform.runLater(() -> controller.inizioSpettatore());
					}
				}
				else if(line.equals("niente"))
				{
					listView.getItems().add(socket.getInetAddress().getHostAddress());
				}
				else
				{
					Platform.runLater(() -> FunctionsController.alertErrore("Non è stato possibile connettersi"));
					controller.sbloccaTutto();
					return;
				}
			}
			catch (IOException ex)
			{
				if(!message.equals("niente"))
					Platform.runLater(() -> FunctionsController.alertErrore("È avvenuto un problema nella connessione"));
			}
			catch (ClassNotFoundException ex)
			{
				//Catch inutile, l'oggetto ricevuto è sempre una partita
			}
			controller.sbloccaTutto();
		}
	}