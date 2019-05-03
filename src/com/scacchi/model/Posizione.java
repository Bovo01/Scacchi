/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi.model;

import java.io.Serializable;

/**
 *
 * @author Pietro
 */
public class Posizione implements Cloneable, Serializable {

	private final Riga riga;
	private final Colonna colonna;

	public Posizione(Riga riga, Colonna colonna) {
		this.riga = riga;
		this.colonna = colonna;
	}

	public Posizione(Posizione pos) {
		this.riga = pos.riga;
		this.colonna = pos.colonna;
	}

	public enum Riga {
		R1, R2, R3, R4, R5, R6, R7, R8;
		
		public String toString() {
			return Integer.toString(ordinal()+1);
		}
	}

	public enum Colonna {
		A('a'), B('b'), C('c'), D('d'), E('e'), F('f'), G('g'), H('h');
		private final char simbolo;
		
		private Colonna(char simbolo)
		{
			this.simbolo = simbolo;
		}
		
		public char getSimbolo() {
			return simbolo;
		}
		
		public static Colonna getFromChar(char simbolo) {
			for(Colonna c : Colonna.values())
			{
				if(simbolo == c.getSimbolo())
					return c;
			}
			return null;
		}
		
		public String toString()
		{
			return Character.toString(simbolo);
		}
	}

	public Riga getRiga() {
		return riga;
	}

	public Colonna getColonna() {
		return colonna;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Posizione other = (Posizione) obj;
		return this.riga == other.riga && this.colonna == other.colonna;
	}

	@Override
	public String toString() {
		return colonna.toString() + Integer.toString(8 - riga.ordinal());
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
