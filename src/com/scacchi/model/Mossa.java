/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi.model;

import com.scacchi.model.Pezzo.Simbolo;

/**
 *
 * @author Pietro
 */
public class Mossa implements Cloneable {
	
	private final Posizione posIniz;
	private final Posizione posFine;
	private Simbolo simbolo;

	public Mossa(Posizione posIniz, Posizione posFine) {
		this.posIniz = posIniz;
		this.posFine = posFine;
	}

	public Mossa(Mossa m) {
		posIniz = m.posIniz;
		posFine = m.posFine;
		simbolo = m.simbolo;
	}

	public void setSimbolo(Simbolo simbolo) {
		this.simbolo = simbolo;
	}

	public Simbolo getSimbolo() {
		return simbolo;
	}

	public Posizione getPosIniz() {
		return posIniz;
	}

	public Posizione getPosFine() {
		return posFine;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public String toString() {
		return posIniz.toString() + posFine.toString() + (simbolo == null ? 0 : simbolo.ordinal());
	}
}
