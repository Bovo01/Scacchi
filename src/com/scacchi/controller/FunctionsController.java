/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi.controller;

import com.scacchi.model.TCP.Settings;
import javafx.scene.control.Alert;
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
			JSONObject jsonObj = (JSONObject) Settings.lingue.getKey("messaggi");
			JSONObject titolo = (JSONObject) jsonObj.get("titolo");
			JSONObject contenuto = (JSONObject) jsonObj.get("contenuto");
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle((String) titolo.get(title));
			alert.setContentText((String) contenuto.get(message));
			alert.show();
		}
		catch (JSONException ex)
		{
			alertErrore("Titolo: " + title + "\nMessaggio: " + message, false);
		}
	}

	/**
	 * Fa comparire un alert con il titolo e il messaggio inseriti, entrambe
	 * parole chiave per una traduzione
	 */
	public static void alertInfo(String title, String message, String coseDaAggiungere, String messageDaAggiungere) {
		try
		{
			JSONObject titolo = (JSONObject) Settings.lingue.getKey("titolo");
			JSONObject contenuto = (JSONObject) Settings.lingue.getKey("contenuto");
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle((String) titolo.get(title));
			alert.setContentText((String) contenuto.get(message) + coseDaAggiungere + (String) contenuto.get(messageDaAggiungere));
			alert.show();
		}
		catch (JSONException ex)
		{
			alertErrore("Titolo: " + title + "\nMessaggio: " + message, false);
		}
	}

	/**
	 * Fa comparire un alert d'errore con il messaggio che identifica una parola
	 * chiave delle traduzioni
	 */
	public static void alertErrore(String message, boolean isErrore) {//TODO Togli funzione test
		Alert alert = new Alert(Alert.AlertType.ERROR);
		if(isErrore)
			alert.setTitle("Errore");
		else
			alert.setTitle("Info");
		alert.setContentText(message);
		alert.show();
	}

	/**
	 * Fa comparire un alert d'errore con il messaggio che identifica una parola
	 * chiave delle traduzioni
	 */
	public static void alertErrore(String message) {
		try
		{
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle((String) Settings.lingue.getKey("errore"));
			alert.setContentText((String) ((JSONObject) ((JSONObject) Settings.lingue.getKey("messaggi")).get("contenuto")).get(message));
			alert.show();
		}
		catch (JSONException ex)
		{
			alertErrore("Message: " + message, true);
		}
	}
}
