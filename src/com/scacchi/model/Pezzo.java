/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi.model;

import com.scacchi.model.TCP.Settings;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Pietro
 */
public class Pezzo implements Cloneable, Serializable {

	private Posizione posizione;
	private Simbolo simbolo;
	private final Colore colore;
	private ArrayList<Mossa> mosse;

	public Pezzo(Posizione posizione, Simbolo simbolo, Colore colore) {
		this.posizione = posizione;
		this.simbolo = simbolo;
		this.colore = colore;
		mosse = new ArrayList<>();
	}

	public Pezzo(Pezzo p) {
		this.posizione = new Posizione(p.posizione);
		this.simbolo = p.simbolo;
		this.colore = p.colore;
		mosse = new ArrayList<>();
	}

	protected Pezzo(Simbolo simbolo, Colore colore) {
		this.simbolo = simbolo;
		this.colore = colore;
	}

	protected void setPosizione(Posizione posizione) {
		this.posizione = posizione;
	}

	protected void setSimbolo(Simbolo simbolo) {
		this.simbolo = simbolo;
	}

	public Posizione getPosizione() {
		return posizione;
	}

	public Simbolo getSimbolo() {
		return simbolo;
	}

	public Colore getColore() {
		return colore;
	}

	public ArrayList<Mossa> getMosse() {
		return mosse;
	}

	public enum Simbolo {
		RE('K'), REGINA('Q'), ALFIERE('B'), CAVALLO('N'), TORRE('R'), PEDONE(' ');
		private final char simbolo;

		private Simbolo(char simbolo) {
			this.simbolo = simbolo;
		}

		public char getSimbolo() {
			return simbolo;
		}

		public static Simbolo getSimboloFromChar(char c) {
			switch (c)
			{
				case 'K':
					return RE;
				case 'Q':
					return REGINA;
				case 'B':
					return ALFIERE;
				case 'N':
					return CAVALLO;
				case 'R':
					return TORRE;
				case ' ':
					return PEDONE;
				default:
					return null;
			}
		}
	}

	public enum Colore {
		BIANCO, NERO;

		public Colore notThis() {
			if (this == BIANCO)
				return NERO;
			return BIANCO;
		}
	}

	@Override
	public String toString() {
		return posizione.toString() + ", " + simbolo + ", " + colore;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Pezzo other = (Pezzo) obj;
		return this.posizione.equals(other.posizione) && this.simbolo == other.simbolo && this.colore == other.colore;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Pezzo p = new Pezzo((Posizione) posizione.clone(), simbolo, colore);
		p.mosse = Partita.copyArray(mosse);
		return p;
	}
}
