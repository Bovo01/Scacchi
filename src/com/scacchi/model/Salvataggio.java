/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi.model;

import static com.scacchi.model.Pezzo.Colore.*;
import static com.scacchi.model.Pezzo.Simbolo.*;
import com.scacchi.model.Posizione.Colonna;
import com.scacchi.model.Posizione.Riga;
import java.util.ArrayList;

/**
 *
 * @author bovolenta.pietro
 */
public class Salvataggio {

	public static String encoding(ArrayList<Mossa> mosse) {
		StringBuilder code = new StringBuilder();
		Partita partita = new Partita();
		for (int i = 0; i < (mosse.size() + 1) / 2; i++)
		{
			code.append((i + 1));
			code.append(". ");
			//Bianco
			Mossa mossa = mosse.get(i * 2);
			Pezzo p = partita.trovaPezzo(mossa.getPosIniz());
			if (p.getSimbolo() == RE//Arrocco
					&& (mossa.getPosIniz().getColonna().ordinal() == mossa.getPosFine().getColonna().ordinal() + 2
					|| mossa.getPosIniz().getColonna().ordinal() == mossa.getPosFine().getColonna().ordinal() - 2))
			{
				code.append("0-0");
				if (mossa.getPosIniz().getColonna().ordinal() == mossa.getPosFine().getColonna().ordinal() + 2)
					code.append("-0");//Arrocco lungo
			}
			else
			{
				if (p.getSimbolo() != PEDONE)
					code.append(p.getSimbolo().getSimbolo());
				if (isAmbiguo(partita, p, mossa.getPosFine()))
					code.append(ambiguo(partita, mossa));
				if (partita.trovaPezzo(mossa.getPosFine()) != null)
					code.append("x");
				code.append(mossa.getPosFine().toString());
				if (mossa.getSimbolo() != null)//Promozione
				{
					code.append("=");
					code.append(mossa.getSimbolo().getSimbolo());
					p.setSimbolo(mossa.getSimbolo());
				}
			}
			partita.muoviSenzaControlli(p, mossa.getPosFine());
			if (partita.isScaccoMatto(NERO))
				code.append("#");
			else if (partita.isScacco(NERO))
				code.append("+");
			code.append(" ");
			//Nero
			if (mosse.size() == i * 2 + 1)
				break;
			mossa = mosse.get(i * 2 + 1);
			p = partita.trovaPezzo(mossa.getPosIniz());
			if (partita.trovaPezzo(mossa.getPosIniz()).getSimbolo() == RE//Arrocco
					&& (mossa.getPosIniz().getColonna().ordinal() == mossa.getPosFine().getColonna().ordinal() + 2
					|| mossa.getPosIniz().getColonna().ordinal() == mossa.getPosFine().getColonna().ordinal() - 2))
			{
				code.append("0-0");
				if (mossa.getPosIniz().getColonna().ordinal() == mossa.getPosFine().getColonna().ordinal() + 2)
					code.append("-0");//Arrocco lungo
			}
			else
			{
				if (p.getSimbolo() != PEDONE)
					code.append(p.getSimbolo().getSimbolo());
				if (isAmbiguo(partita, p, mossa.getPosFine()))
					code.append(ambiguo(partita, mossa));
				if (partita.trovaPezzo(mossa.getPosFine()) != null)
					code.append("x");
				code.append(mossa.getPosFine().toString());
				if (mossa.getSimbolo() != null)//Promozione
				{
					code.append("=");
					code.append(mossa.getSimbolo().getSimbolo());
					p.setSimbolo(mossa.getSimbolo());
				}
			}
			partita.muoviSenzaControlli(p, mossa.getPosFine());
			if (partita.isScaccoMatto(BIANCO))
				code.append("#");
			else if (partita.isScacco(BIANCO))
				code.append("+");
			code.append(" ");
		}
		if (partita.isScaccoMatto(NERO))
			code.append("1-0");
		else if (partita.isScaccoMatto(BIANCO))
			code.append("0-1");
		else
			code.append("1/2-1/2");
		return code.toString();
	}

	public static ArrayList<Mossa> decoding(String code) {
		ArrayList<Mossa> mosse = new ArrayList<>();
		String[] array = code.split(" ");
		int i = 0;
		for (String s : array)
		{
			if (!s.contains("."))
			{
				
			}
			i++;
		}

		return mosse;
	}

	private static String ambiguo(Partita partita, Mossa mossa) {
		ArrayList<Pezzo> sameSimbolo = sameSimbolo(partita, partita.trovaPezzo(mossa.getPosIniz()), mossa.getPosFine());
		Pezzo p = partita.trovaPezzo(mossa.getPosIniz());
		boolean riga = false, colonna = false;//true se c'è ambiguità
		for (Pezzo pezzo : sameSimbolo)
		{
			if (p.equals(pezzo))
				continue;
			if (pezzo.getPosizione().getRiga() == p.getPosizione().getRiga())
				riga = true;
			if (pezzo.getPosizione().getColonna() == p.getPosizione().getColonna())
				colonna = true;
		}
		return (riga ? p.getPosizione().getColonna().toString() + (colonna ? p.getPosizione().getRiga().toString() : "") : (colonna ? p.getPosizione().getRiga().toString() : ""));
	}

	/**
	 * Ritorna l'ArrayList dei pezzi abigui
	 *
	 * @param partita
	 * @param p
	 * @return
	 */
	private static ArrayList<Pezzo> sameSimbolo(Partita partita, Pezzo p, Posizione pos) {
		ArrayList<Pezzo> array = new ArrayList<>();
		if (p.getColore() == BIANCO)
			for (Pezzo pezzo : partita.getBianchi())
			{
				if (p.getSimbolo() == pezzo.getSimbolo() && partita.elencoMosseScacco(pezzo).contains(pos))
					array.add(pezzo);
			}
		else
			for (Pezzo pezzo : partita.getNeri())
			{
				if (p.getSimbolo() == pezzo.getSimbolo() && partita.elencoMosseScacco(pezzo).contains(pos))
					array.add(pezzo);
			}
		return array;
	}

	private static boolean isAmbiguo(Partita partita, Pezzo p, Posizione pos) {
		return sameSimbolo(partita, p, pos).size() != 1;
	}

	private static Mossa stringToMossa(String s, int i) {
		Pezzo p;
		if (i % 3 == 1)
			p = new Pezzo(PEDONE, BIANCO);
		else
			p = new Pezzo(PEDONE, NERO);
		int stato = 0;
		Riga row = null;
		Colonna col = null;
		for (int c = 0; c < s.length(); c++)
		{
			switch (stato)
			{
				case 0:
					switch (s.charAt(c)) {
						case 'N':
							p.setSimbolo(CAVALLO);
							break;
						case 'K':
							p.setSimbolo(RE);
							break;
						case 'Q':
							p.setSimbolo(REGINA);
							break;
						case 'B':
							p.setSimbolo(ALFIERE);
							break;
						case 'T':
							p.setSimbolo(TORRE);
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
						case 'g':
						case 'h':
							stato = 1;
							break;
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
							stato = 2;
							break;
						case '0':
							if(s.length() == 3)//Arrocco corto (0-0)
							{
								if(p.getColore() == BIANCO)
								{
									p.setPosizione(new Posizione(Riga.R1, Colonna.G));
								}
								else
								{
									p.setPosizione(new Posizione(Riga.R8, Colonna.G));
								}
							}
							else//Arrocco lungo (0-0-0)
							{
								if(p.getColore() == BIANCO)
								{
									p.setPosizione(new Posizione(Riga.R1, Colonna.C));
								}
								else
								{
									p.setPosizione(new Posizione(Riga.R8, Colonna.C));
								}
							}
					}
					break;
				case 1://Gestisce colonna
					switch(s.charAt(c)) {
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
						case 'g':
						case 'h':
						case 'x':
							col = Colonna.getFromChar(s.charAt(c-1));
							break;
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
							if(c < s.length() - 2)
							{
								col = Colonna.getFromChar(s.charAt(c-1));
								row = Riga.values()[Integer.parseInt(s, c)];
							}
							else
								p.setPosizione(new Posizione(Riga.values()[Integer.parseInt(s, c)], Colonna.getFromChar(s.charAt(c-1))));
							stato = 2;
					}
					break;
				case 2://Gestione riga
					switch(s.charAt(c)) {
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
						case 'g':
						case 'h':
						case 'x':
							row = Riga.values()[Integer.parseInt(s, c-1)];
					}
			}
		}
		
		
		return null;
	}
}
