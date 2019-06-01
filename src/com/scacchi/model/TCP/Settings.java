/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi.model.TCP;

import com.scacchi.controller.ScacchieraOnlineController;
import com.scacchi.controller.ScacchieraOnlineSpettatoriController;
import com.scacchi.model.Partita;
import com.scacchi.model.Pezzo;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author bovolenta.pietro
 */
public class Settings {//SPORCO
	//Costanti
	public static final int DEFAULTPORT = 25560;
	//Connessione
	public static Socket player;
	public static BufferedReader playerReader;
	public static BufferedWriter playerWriter;
	public static ObjectOutputStream playerOOS;
	public static ObjectInputStream playerOIS;
	public static ArrayList<Socket> spettatori;
	public static ArrayList<BufferedReader> spettatoriReaders;
	public static ArrayList<BufferedWriter> spettatoriWriters;
	public static ArrayList<ObjectOutputStream> spettatoriOOS;
	//Interazione da thread a controller
	public static Partita partita;
	public static Pezzo.Colore schieramento;
	public static ScacchieraOnlineController scacchieraOnlineController;
	public static ScacchieraOnlineSpettatoriController scacchieraOnlineSpettatoriController;
	//Thread
	public static ThreadAccetta threadAccetta;
	public static ThreadRicevi threadRicevi;//Serve solo agli spettatori
	//Caricamento partita
	public static Partita partitaDaCaricare;
}
