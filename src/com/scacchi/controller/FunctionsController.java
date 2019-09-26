/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi.controller;

import com.scacchi.model.TCP.Settings;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Pietro
 */
public class FunctionsController {

	/**
	 * Fa comparire un alert con il titolo e il messaggio inseriti, entrambe
	 * parole chiave per una traduzione
	 */
	public static void alertInfo(String title, String message) {
		try
		{
			JSONObject messaggi = Settings.lingue.getJSONObject("messaggi");
			JSONObject titolo = messaggi.getJSONObject("titolo");
			JSONObject contenuto = messaggi.getJSONObject("contenuto");
			Alert alert = new Alert(Alert.AlertType.NONE);
			alert.getButtonTypes().add(new ButtonType(Settings.lingue.getString("ok")));
			alert.setTitle(titolo.getString(title));
			alert.setContentText(contenuto.getString(message));
			alert.show();
		}
		catch (JSONException ex)
		{
		}
	}

	/**
	 * Fa comparire un alert con il titolo e il messaggio inseriti, quest'ultimo
	 * diviso in 3 parole chiave
	 */
	public static void alertInfo(String title, String message, String coseDaAggiungere, String messageDaAggiungere) {
		try
		{
			JSONObject messaggi = Settings.lingue.getJSONObject("messaggi");
			JSONObject titolo = messaggi.getJSONObject("titolo");
			JSONObject contenuto = messaggi.getJSONObject("contenuto");
			Alert alert = new Alert(Alert.AlertType.NONE);
			alert.getButtonTypes().add(new ButtonType(Settings.lingue.getString("ok")));
			alert.setTitle(titolo.getString(title));
			alert.setContentText(contenuto.getString(message) + contenuto.getString(coseDaAggiungere) + contenuto.getString(messageDaAggiungere));
			alert.show();
		}
		catch (JSONException ex)
		{
		}
	}

	/**
	 * Fa comparire un alert d'errore con il messaggio che identifica una parola
	 * chiave delle traduzioni
	 */
	public static void alertErrore(String message) {
		try
		{
			Alert alert = new Alert(Alert.AlertType.NONE);
			alert.getButtonTypes().add(new ButtonType(Settings.lingue.getString("ok")));
			alert.setTitle(Settings.lingue.getString("errore"));
			alert.setContentText(Settings.lingue.getJSONObject("messaggi").getJSONObject("contenuto").getString(message));
			alert.show();
		}
		catch (JSONException ex)
		{
		}
	}
}
