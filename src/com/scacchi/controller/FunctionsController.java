/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi.controller;

import javafx.scene.control.Alert;

/**
 *
 * @author Pietro
 */
public class FunctionsController {
	public static void alertInfo(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setContentText(message);
		alert.show();
	}

	public static void alertErrore(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Errore");
		alert.setContentText(message);
		alert.show();
	}
}
