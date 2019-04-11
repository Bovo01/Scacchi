/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi.model;

import com.scacchi.model.Pezzo.Colore;
import static com.scacchi.model.Pezzo.Colore.*;
import static com.scacchi.model.Pezzo.Simbolo.*;
import com.scacchi.model.Posizione.Colonna;
import static com.scacchi.model.Posizione.Colonna.*;
import com.scacchi.model.Posizione.Riga;
import static com.scacchi.model.Posizione.Riga.*;
import java.util.ArrayList;

/**
 *
 * @author Pietro
 */
public class Partita {

	private Colore turno;
	private ArrayList<Pezzo> bianchi;
	private ArrayList<Pezzo> neri;
	private Mossa ultimaMossa;
	private ArrayList<Mossa> mosse;
	private int regola50Mosse;
	private ArrayList<String> ripetizioneMosse;

	public Partita() {
		this.turno = BIANCO;
		this.ripetizioneMosse = new ArrayList<>();
		this.bianchi = new ArrayList<>();
		this.neri = new ArrayList<>();
		this.mosse = new ArrayList<>();
		inizioPartita();
		aggiungiRipezioneMosse();
	}

	/**
	 * Costruttore che copia i due ArrayList di pezzi
	 */
	private Partita(ArrayList<Pezzo> bianchi, ArrayList<Pezzo> neri) {
		this.bianchi = copyArray(bianchi);
		this.neri = copyArray(neri);
	}

	public Mossa getUltimaMossa() {
		return ultimaMossa;
	}

	public ArrayList<Mossa> getMosse() {
		return mosse;
	}

	public void setUltimaMossa(Mossa ultimaMossa) {
		this.ultimaMossa = ultimaMossa;
	}

	public void fine() {
		turno = null;
	}

	public Colore getTurno() {
		return turno;
	}

	public ArrayList<Pezzo> getBianchi() {
		return bianchi;
	}

	public ArrayList<Pezzo> getNeri() {
		return neri;
	}

	/**
	 * Imposta la posizione iniziale di tutti i pezzi sulla scacchiera
	 */
	private void inizioPartita() {
		//Pedoni
		for (Colonna c : Colonna.values())
		{
			bianchi.add(new Pezzo(new Posizione(R2, c), PEDONE, BIANCO));
			neri.add(new Pezzo(new Posizione(R7, c), PEDONE, NERO));
		}
		//Torri
		bianchi.add(new Pezzo(new Posizione(R1, A), TORRE, BIANCO));
		bianchi.add(new Pezzo(new Posizione(R1, H), TORRE, BIANCO));
		neri.add(new Pezzo(new Posizione(R8, A), TORRE, NERO));
		neri.add(new Pezzo(new Posizione(R8, H), TORRE, NERO));
		//Cavalli
		bianchi.add(new Pezzo(new Posizione(R1, B), CAVALLO, BIANCO));
		bianchi.add(new Pezzo(new Posizione(R1, G), CAVALLO, BIANCO));
		neri.add(new Pezzo(new Posizione(R8, B), CAVALLO, NERO));
		neri.add(new Pezzo(new Posizione(R8, G), CAVALLO, NERO));
		//Alfieri
		bianchi.add(new Pezzo(new Posizione(R1, C), ALFIERE, BIANCO));
		bianchi.add(new Pezzo(new Posizione(R1, F), ALFIERE, BIANCO));
		neri.add(new Pezzo(new Posizione(R8, C), ALFIERE, NERO));
		neri.add(new Pezzo(new Posizione(R8, F), ALFIERE, NERO));
		//Regine
		bianchi.add(new Pezzo(new Posizione(R1, D), REGINA, BIANCO));
		neri.add(new Pezzo(new Posizione(R8, D), REGINA, NERO));
		//Re
		bianchi.add(new Pezzo(new Posizione(R1, E), RE, BIANCO));
		neri.add(new Pezzo(new Posizione(R8, E), RE, NERO));
	}

	/**
	 * Data una posizione, ritorna il pezzo che si trova in quella posizione
	 *
	 * @param pos La posizione in cui cercare
	 * @return Il pezzo nella posizione indicata. Se la posizione è vuota, il
	 * ritorno è null
	 */
	public Pezzo trovaPezzo(Posizione pos) {
		for (Pezzo p : bianchi)
		{
			if (p.getPosizione().equals(pos))
				return p;
		}
		for (Pezzo p : neri)
		{
			if (p.getPosizione().equals(pos))
				return p;
		}
		return null;
	}

	/**
	 * Verifica se la mossa indicata per il pezzo inserito è consentita
	 *
	 * @param p Il pezzo da spostare
	 * @param posFine La posizione in cui spostare il pezzo inserito
	 * @return true se è possibile spostare il pezzo, false altrimenti
	 */
	private boolean verificaMossa(Pezzo p, Posizione posFine) {
		if (p.getPosizione().equals(posFine))//Verifica se la posizione d'inizio è uguale a quella di fine
			return false;
		if (isThereAnybodyOutThere(posFine, p.getColore()))
			return false;
		switch (p.getSimbolo())
		{
			case PEDONE:
				if (p.getColore() == NERO)
				{
					if (p.getPosizione().getColonna() != posFine.getColonna())//Verifica movimenti obliqui per mangiare
						if ((p.getPosizione().getColonna().ordinal() + 1 == posFine.getColonna().ordinal()//Se si muove correttamete in obliquo (di una sola cella)
							|| p.getPosizione().getColonna().ordinal() - 1 == posFine.getColonna().ordinal())
							&& p.getPosizione().getRiga().ordinal() + 1 == posFine.getRiga().ordinal())
							if (ultimaMossa != null && trovaPezzo(ultimaMossa.getPosFine()) != null && trovaPezzo(ultimaMossa.getPosFine()).getSimbolo() == PEDONE && trovaPezzo(ultimaMossa.getPosFine()).getColore() != p.getColore()
								&& ultimaMossa.getPosIniz().getRiga().ordinal() == ultimaMossa.getPosFine().getRiga().ordinal() + 2 && p.getPosizione().getRiga() == ultimaMossa.getPosFine().getRiga()
								&& ultimaMossa.getPosFine().getColonna() == posFine.getColonna() && posFine.getRiga().ordinal() == ultimaMossa.getPosFine().getRiga().ordinal() + 1)
								return true;//En passant
							else
								return isThereAnybodyOutThere(posFine, BIANCO);//Se c'è qualcuno sulla casella ritorna true, altrimenti false
						else
							return false;
					if (isThereAnybodyOutThere(posFine))//Verifica se nella posizione finale c'è qualcuno (siamo sicuramente nella stessa colonna)

						return false;
					if (p.getPosizione().getRiga().ordinal() + 1 == posFine.getRiga().ordinal())//Verifica se la posizione finale è nella riga successiva a quella attuale

						return true;
					if (isThereAnybodyOutThere(new Posizione(Riga.values()[p.getPosizione().getRiga().ordinal() + 1], p.getPosizione().getColonna())))//Verifica se c'è qualcuno nella traiettoria

						return false;
					if (p.getPosizione().getRiga() == R7 && posFine.getRiga() == R5)//Se è alla posizione iniziale si può muovere di due

						return true;
				}
				else
				{
					if (p.getPosizione().getColonna() != posFine.getColonna())//Verifica movimenti obliqui per mangiare
						if ((p.getPosizione().getColonna().ordinal() + 1 == posFine.getColonna().ordinal()//Se si muove correttamete in obliquo (di una sola cella)
							|| p.getPosizione().getColonna().ordinal() - 1 == posFine.getColonna().ordinal())
							&& p.getPosizione().getRiga().ordinal() - 1 == posFine.getRiga().ordinal())
							if (ultimaMossa != null && trovaPezzo(ultimaMossa.getPosFine()) != null && trovaPezzo(ultimaMossa.getPosFine()).getSimbolo() == PEDONE && trovaPezzo(ultimaMossa.getPosFine()).getColore() != p.getColore()
								&& ultimaMossa.getPosIniz().getRiga().ordinal() == ultimaMossa.getPosFine().getRiga().ordinal() - 2 && p.getPosizione().getRiga() == ultimaMossa.getPosFine().getRiga()
								&& ultimaMossa.getPosFine().getColonna() == posFine.getColonna() && posFine.getRiga().ordinal() == ultimaMossa.getPosFine().getRiga().ordinal() - 1)
								return true;//En passant
							else
								return isThereAnybodyOutThere(posFine, NERO);//Se c'è qualcuno sulla casella ritorna true, altrimenti false
						else
							return false;
					if (isThereAnybodyOutThere(posFine))//Verifica se nella posizione finale c'è qualcuno (siamo sicuramente nella stessa colonna

						return false;
					if (p.getPosizione().getRiga().ordinal() - 1 == posFine.getRiga().ordinal())//Verifica se la posizione finale è nella riga successiva a quella attuale

						return true;
					if (isThereAnybodyOutThere(new Posizione(Riga.values()[p.getPosizione().getRiga().ordinal() - 1], p.getPosizione().getColonna())))//Verifica se c'è qualcuno nella traiettoria

						return false;
					if (p.getPosizione().getRiga() == R2 && posFine.getRiga() == R4)//Se è alla posizione iniziale si può muovere di due

						return true;
				}
				break;//Movimento non contemplato = return false
			case TORRE:
				if (p.getPosizione().getRiga() != posFine.getRiga() && p.getPosizione().getColonna() != posFine.getColonna())//Verifica che riga e colonna siano orizzontali rispetto alla sua posizione

					return false;
				//Verifica traiettoria
				return controlloCroce(p, posFine);
			case CAVALLO:
				for (int i = -2; i <= 2; i += 4)//Indice di riga
				{
					for (int j = -1; j <= 1; j += 2)//Indice di colonna
					{
						if (p.getPosizione().getRiga().ordinal() + i == posFine.getRiga().ordinal()
							&& p.getPosizione().getColonna().ordinal() + j == posFine.getColonna().ordinal())//Controllo movimento a 'L'
							return true;
						if (p.getPosizione().getColonna().ordinal() + i == posFine.getColonna().ordinal()
							&& p.getPosizione().getRiga().ordinal() + j == posFine.getRiga().ordinal())//Controllo movimento a 'L'
							return true;
					}
				}
				break;//Movimento non contemplato = return false
			case ALFIERE:
				if (Math.abs(posFine.getRiga().ordinal() - p.getPosizione().getRiga().ordinal())
					!= Math.abs(posFine.getColonna().ordinal() - p.getPosizione().getColonna().ordinal()))//Esclude tutti i valori non obliqui alla posizione iniziale
					return false;
				//Verifica traiettoria
				return controlloX(p, posFine);
			case REGINA:
				if (!(p.getPosizione().getRiga() != posFine.getRiga() && p.getPosizione().getColonna() != posFine.getColonna()))
					if (controlloCroce(p, posFine))
						return true;
				return controlloX(p, posFine);
			case RE:
				for (int i = -1; i <= 1; i++)//Riga
				{
					for (int j = -1; j <= 1; j++)//Colonna
					{
						if ((p.getPosizione().getRiga().ordinal() + i >= 0 && p.getPosizione().getColonna().ordinal() + j >= 0)
							&& (p.getPosizione().getRiga().ordinal() + i < 8 && p.getPosizione().getColonna().ordinal() + j < 8))
							if (posFine.equals(new Posizione(Riga.values()[p.getPosizione().getRiga().ordinal() + i], Colonna.values()[p.getPosizione().getColonna().ordinal() + j])))
								return true;
					}
				}
				if (p.getColore() == BIANCO)//Arrocco
				{
					if (p.getMosse().isEmpty() && trovaPezzo(new Posizione(R1, H)) != null && trovaPezzo(new Posizione(R1, H)).getMosse().isEmpty() && p.getPosizione().equals(new Posizione(R1, E)) && posFine.equals(new Posizione(R1, G)) && trovaPezzo(new Posizione(R1, F)) == null
						&& trovaPezzo(new Posizione(R1, G)) == null && new Pezzo(new Posizione(R1, H), TORRE, BIANCO).equals(trovaPezzo(new Posizione(R1, H))))
						return true;
				}
				else if (p.getMosse().isEmpty() && trovaPezzo(new Posizione(R8, H)) != null && trovaPezzo(new Posizione(R8, H)).getMosse().isEmpty() && p.getPosizione().equals(new Posizione(R8, E)) && posFine.equals(new Posizione(R8, G)) && trovaPezzo(new Posizione(R8, F)) == null
					&& trovaPezzo(new Posizione(R8, G)) == null && new Pezzo(new Posizione(R8, H), TORRE, NERO).equals(trovaPezzo(new Posizione(R8, H))))
					return true;
				if (p.getColore() == BIANCO)//Arrocco lungo
				{
					if (p.getMosse().isEmpty() && trovaPezzo(new Posizione(R1, A)) != null && trovaPezzo(new Posizione(R1, A)).getMosse().isEmpty() && p.getPosizione().equals(new Posizione(R1, E)) && posFine.equals(new Posizione(R1, C)) && trovaPezzo(new Posizione(R1, D)) == null
						&& trovaPezzo(new Posizione(R1, C)) == null && trovaPezzo(new Posizione(R1, B)) == null && new Pezzo(new Posizione(R1, A), TORRE, BIANCO).equals(trovaPezzo(new Posizione(R1, A))))
						return true;
				}
				else if (p.getMosse().isEmpty() && trovaPezzo(new Posizione(R8, A)) != null && trovaPezzo(new Posizione(R8, A)).getMosse().isEmpty() && p.getPosizione().equals(new Posizione(R8, E)) && posFine.equals(new Posizione(R8, C)) && trovaPezzo(new Posizione(R8, D)) == null
					&& trovaPezzo(new Posizione(R8, C)) == null && trovaPezzo(new Posizione(R8, B)) == null && new Pezzo(new Posizione(R8, A), TORRE, NERO).equals(trovaPezzo(new Posizione(R8, A))))
					return true;
			//Movimento non contemplato = return false
		}
		return false;
	}

	/**
	 * Metodo che controlla se in una croce attorno ad un punto è possibile
	 * muoversi nella posizione finale Serve per la Torre e per la Regina
	 *
	 * @param p Pezzo che si muove
	 * @param posFine Posizione finale in cui si deve muovere
	 * @return true se il movimento è possibile, false altrimenti
	 */
	private boolean controlloCroce(Pezzo p, Posizione posFine) {
		if (posFine.getRiga() == p.getPosizione().getRiga())//Stessa riga
			if (posFine.getColonna().ordinal() > p.getPosizione().getColonna().ordinal())//Stessa riga verso destra
				for (int i = p.getPosizione().getColonna().ordinal() + 1; i < 8; i++)
				{
					if (posFine.getColonna().ordinal() == i)
						return true;
					if (isThereAnybodyOutThere(new Posizione(p.getPosizione().getRiga(), Colonna.values()[i])))
						return false;
				}
			else//Stessa riga verso sinistra
				for (int i = p.getPosizione().getColonna().ordinal() - 1; i >= 0; i--)
				{
					if (posFine.getColonna().ordinal() == i)
						return true;
					if (isThereAnybodyOutThere(new Posizione(p.getPosizione().getRiga(), Colonna.values()[i])))
						return false;
				}
		else//Stessa colonna
		if (posFine.getRiga().ordinal() > p.getPosizione().getRiga().ordinal())//Stessa colonna verso l'alto
			for (int i = p.getPosizione().getRiga().ordinal() + 1; i < 8; i++)
			{
				if (posFine.getRiga().ordinal() == i)
					return true;
				if (isThereAnybodyOutThere(new Posizione(Riga.values()[i], p.getPosizione().getColonna())))
					return false;
			}
		else//Stessa colonna verso il basso
			for (int i = p.getPosizione().getRiga().ordinal() - 1; i >= 0; i--)
			{
				if (posFine.getRiga().ordinal() == i)
					return true;
				if (isThereAnybodyOutThere(new Posizione(Riga.values()[i], p.getPosizione().getColonna())))
					return false;
			}
		return false;//Non si arriverà mai qua
	}

	/**
	 * Metodo che controlla se in una X attorno ad un punto è possibile muoversi
	 * nella posizione finale Serve per l'Alfiere e per la Regina
	 *
	 * @param p Pezzo che si muove
	 * @param posFine Posizione finale in cui si deve muovere
	 * @return true se il movimento è possibile, false altrimenti
	 */
	private boolean controlloX(Pezzo p, Posizione posFine) {
		if (posFine.getColonna().ordinal() > p.getPosizione().getColonna().ordinal())//Zona a destra del punto iniziale

			if (posFine.getRiga().ordinal() > p.getPosizione().getRiga().ordinal())//Zona in basso a destra del punto iniziale

				for (int i = 1; i <= Math.min(7 - p.getPosizione().getRiga().ordinal(), 7 - p.getPosizione().getColonna().ordinal()); i++)
				{
					if (posFine.equals(new Posizione(Riga.values()[p.getPosizione().getRiga().ordinal() + i], Colonna.values()[p.getPosizione().getColonna().ordinal() + i])))
						return true;
					if (isThereAnybodyOutThere(new Posizione(Riga.values()[p.getPosizione().getRiga().ordinal() + i], Colonna.values()[p.getPosizione().getColonna().ordinal() + i])))
						return false;
				}
			else//Zona in alto a destra del punto iniziale

				for (int i = 1; i <= Math.min(p.getPosizione().getRiga().ordinal(), 7 - p.getPosizione().getColonna().ordinal()); i++)
				{
					if (posFine.equals(new Posizione(Riga.values()[p.getPosizione().getRiga().ordinal() - i], Colonna.values()[p.getPosizione().getColonna().ordinal() + i])))
						return true;
					if (isThereAnybodyOutThere(new Posizione(Riga.values()[p.getPosizione().getRiga().ordinal() - i], Colonna.values()[p.getPosizione().getColonna().ordinal() + i])))
						return false;
				}
		else//Zona a sinistra del punto iniziale
		if (posFine.getRiga().ordinal() > p.getPosizione().getRiga().ordinal())//Zona in basso a sinistra del punto iniziale

			for (int i = 1; i <= Math.min(7 - p.getPosizione().getRiga().ordinal(), p.getPosizione().getColonna().ordinal()); i++)
			{
				if (posFine.equals(new Posizione(Riga.values()[p.getPosizione().getRiga().ordinal() + i], Colonna.values()[p.getPosizione().getColonna().ordinal() - i])))
					return true;
				if (isThereAnybodyOutThere(new Posizione(Riga.values()[p.getPosizione().getRiga().ordinal() + i], Colonna.values()[p.getPosizione().getColonna().ordinal() - i])))
					return false;
			}
		else//Zona in basso a destra del punto iniziale

			for (int i = 1; i <= Math.min(p.getPosizione().getRiga().ordinal(), p.getPosizione().getColonna().ordinal()); i++)
			{
				if (posFine.equals(new Posizione(Riga.values()[p.getPosizione().getRiga().ordinal() - i], Colonna.values()[p.getPosizione().getColonna().ordinal() - i])))
					return true;
				if (isThereAnybodyOutThere(new Posizione(Riga.values()[p.getPosizione().getRiga().ordinal() - i], Colonna.values()[p.getPosizione().getColonna().ordinal() - i])))
					return false;
			}
		return false;
	}

	/**
	 * Rimuove il pezzo inserito dall'ArrayList corretto
	 *
	 * @param p Il pezzo da rimuovere
	 * @return false se non è stato possibile rimuovere il pezzo perché
	 * inesistente, true altrimenti
	 */
	private boolean rimuoviPezzo(Pezzo p) {
		if (p == null)
			return false;
		if (p.getColore() == BIANCO)
			return bianchi.remove(p);
		else
			return neri.remove(p);
	}

	/**
	 * Dato un pezzo, ritorna l'elenco delle posizioni in cui quel determinato
	 * pezzo si può muovere
	 *
	 * @param p Il pezzo da controllare
	 * @return L'ArrayList delle mosse possibili per il pezzo inserito
	 */
	private ArrayList<Posizione> elencoMosse(Pezzo p) {
		ArrayList<Posizione> elenco = new ArrayList<>();
		for (Riga r : Riga.values())
		{
			for (Colonna c : Colonna.values())
			{
				if (verificaMossa(p, new Posizione(r, c)))
					elenco.add(new Posizione(r, c));
			}
		}
		return elenco;
	}

	/**
	 * Metodo che permette di muovere un pezzo nella posizione indicata, solo se
	 * il movimento è possibile
	 *
	 * @param p Il pezzo da muovere
	 * @param pos La posizione in cui muovere il pezzo
	 * @return true se il pezzo è stato spostato a buon fine, false altrimenti
	 */
	public boolean muovi(Pezzo p, Posizione pos) {
		if (turno != p.getColore())//Non è il suo turno
			return false;
		boolean scacco = false;
		for (Posizione posizione : elencoMosseScacco(p))
		{
			if (pos.equals(posizione))
			{
				scacco = true;
				break;
			}
		}
		if (!scacco)
			return false;
		p.getMosse().add(new Mossa(p.getPosizione(), pos));
		mosse.add(new Mossa(p.getPosizione(), pos));
		if (p.getSimbolo() == PEDONE || trovaPezzo(pos) != null)
			regola50Mosse = 0;
		else
			regola50Mosse++;
		muoviSenzaControlli(p, pos);
		aggiungiRipezioneMosse();
		if (isFinita())
		{
			turno = null;
			return true;
		}
		if (turno == BIANCO)
			turno = NERO;
		else
			turno = BIANCO;
		return true;
	}

	/**
	 * Controlla se la partita è finita
	 *
	 * @return true se la partita è conclusa, false altrimenti
	 */
	private boolean isFinita() {
		if (isStallo(NERO) || isStallo(BIANCO))
			return true;
		if (isScaccoMatto(NERO) || isScaccoMatto(BIANCO))
			return true;
		if (regola50Mosse == 100)
			return true;
		return isRipezioneMosse();
	}

	/**
	 * Dice il modo in cui è finita la partita
	 *
	 * @return null se non è ancora finita, altrimenti il modo in cui è finita
	 */
	public String comeEFinita() {
		if (isStallo(NERO) || isStallo(BIANCO))
			return "Stallo";
		if (isScaccoMatto(NERO) || isScaccoMatto(BIANCO))
			return "Scacco matto";
		if (regola50Mosse == 100)
			return "Regola delle 50 mosse";
		if (isRipezioneMosse())
			return "Ripetizione di mosse";
		return null;
	}

	/**
	 * Dice chi ha vinto la partita
	 *
	 * @return Il colore del vincitore. In caso di patta il return è null
	 */
	public Colore vincitore() {
		if (isStallo(BIANCO) || isStallo(NERO))
			return null;
		if (isScaccoMatto(BIANCO))
			return NERO;
		if (isScaccoMatto(NERO))
			return BIANCO;
		return null;
	}

	/**
	 * Verifica se c'è un pezzo nella posizione indicata
	 *
	 * @param pos La posizione da controllare
	 * @return true se c'è qualcosa, false altrimenti
	 */
	private boolean isThereAnybodyOutThere(Posizione pos) {
		if (isThereAnybodyOutThere(pos, BIANCO))
			return true;
		return isThereAnybodyOutThere(pos, NERO);
	}

	/**
	 * Verifica se c'è un pezzo nella posizione indicata dell'ArrayList del
	 * colore indicato
	 *
	 * @param pos La posizione da controllare
	 * @param c Il colore dell'ArrayList da controllare
	 * @return true se c'è qualcosa, false altrimenti
	 */
	private boolean isThereAnybodyOutThere(Posizione pos, Colore c) {
		if (c == BIANCO)
		{
			for (Pezzo p : bianchi)
			{
				if (p.getPosizione().equals(pos))
					return true;
			}
		}
		else
			for (Pezzo p : neri)
			{
				if (p.getPosizione().equals(pos))
					return true;
			}
		return false;
	}

	/**
	 * Permette di muovere un pezzo eludendo tutti i controlli
	 *
	 * @param p
	 * @param pos
	 */
	protected void muoviSenzaControlli(Pezzo p, Posizione pos) {
		if (p.getSimbolo() == PEDONE && p.getPosizione().getColonna() != pos.getColonna() && trovaPezzo(pos) == null)//En passant
			rimuoviPezzo(trovaPezzo(new Posizione(p.getPosizione().getRiga(), pos.getColonna())));
		else if (p.getColore() == BIANCO && p.equals(new Pezzo(new Posizione(R1, E), RE, BIANCO)) && pos.equals(new Posizione(R1, G)))//Arrocco Bianco
			trovaPezzo(new Posizione(R1, H)).setPosizione(new Posizione(R1, F));
		else if (p.getColore() == NERO && p.equals(new Pezzo(new Posizione(R8, E), RE, NERO)) && pos.equals(new Posizione(R8, G)))//Arrocco Nero
			trovaPezzo(new Posizione(R8, H)).setPosizione(new Posizione(R8, F));
		else if (p.getColore() == BIANCO && p.equals(new Pezzo(new Posizione(R1, E), RE, BIANCO)) && pos.equals(new Posizione(R1, C)))//Arrocco lungo Bianco
			trovaPezzo(new Posizione(R1, A)).setPosizione(new Posizione(R1, D));
		else if (p.getColore() == NERO && p.equals(new Pezzo(new Posizione(R8, E), RE, NERO)) && pos.equals(new Posizione(R8, C)))//Arrocco lungo Nero
			trovaPezzo(new Posizione(R8, A)).setPosizione(new Posizione(R8, D));
		ultimaMossa = new Mossa(p.getPosizione(), pos);
		rimuoviPezzo(p);
		if (trovaPezzo(pos) != null)
			rimuoviPezzo(trovaPezzo(pos));
		p.setPosizione(pos);
		if (p.getColore() == BIANCO)
			bianchi.add(p);
		else
			neri.add(p);
	}

	/**
	 * Metodo che controlla se la situazione iniziale di uno schieramento è in
	 * scacco matto o meno
	 *
	 * @param colore il colore dello schieramento da controllare
	 * @return true se lo schieramento inserito	è in scacco matto, altrimenti
	 * false
	 */
	public boolean isScaccoMatto(Colore colore) {
		Partita partita = new Partita(bianchi, neri);
		if (!partita.isScacco(colore))
			return false;
		if (colore == BIANCO)
			for (int i = 0; i < partita.bianchi.size(); i++)
			{
				Pezzo p = partita.bianchi.get(i);
				ArrayList<Posizione> mosse = partita.elencoMosseScacco(p);
				for (Posizione pos : mosse)
				{
					partita.muoviSenzaControlli(p, pos);
					if (!partita.isScacco(colore))
						return false;
					partita = new Partita(bianchi, neri);
				}
				partita = new Partita(bianchi, neri);
			}
		else
			for (int i = 0; i < partita.neri.size(); i++)
			{
				Pezzo p = partita.neri.get(i);
				ArrayList<Posizione> mosse = partita.elencoMosseScacco(p);
				Posizione posizioneIniziale = p.getPosizione();
				for (Posizione pos : mosse)
				{
					partita.muoviSenzaControlli(p, pos);
					if (!partita.isScacco(colore))
						return false;
					partita = new Partita(bianchi, neri);
				}
				partita = new Partita(bianchi, neri);
			}
		return true;
	}

	/**
	 * Metodo che controlla se lo schieramento inserito è in scacco, ossia se il
	 * re può essere mangiato ma può ancora essere salvato
	 *
	 * @param colore il colore dello schieramento da controllare
	 * @return true se il re è in scacco, altrimenti false
	 */
	public boolean isScacco(Colore colore) {
		return !pedineScacco(colore).isEmpty();
	}

	/**
	 * Metodo che controlla se l'arrocco del tipo inserito è o meno valido
	 *
	 * @param re il re di cui verificare l'arrocco
	 * @param isCorto true se si verifica l'arrocco corto, false se si controlla
	 * l'arrocco lungo
	 * @return true se l'arrocco si può fare, false altrimenti
	 */
	private boolean isArroccoCorretto(Pezzo re, boolean isCorto) {
		if (isScacco(re.getColore()))
			return false;
		Partita partita = new Partita(bianchi, neri);
		if (isCorto)
			partita.muoviSenzaControlli(partita.trovaPezzo(re.getPosizione()), new Posizione(re.getPosizione().getRiga(), Colonna.values()[re.getPosizione().getColonna().ordinal() + 1]));
		else
			partita.muoviSenzaControlli(partita.trovaPezzo(re.getPosizione()), new Posizione(re.getPosizione().getRiga(), Colonna.values()[re.getPosizione().getColonna().ordinal() - 1]));
		return !partita.isScacco(re.getColore());
	}

	/**
	 * Metodo che, data una partita e uno schieramento, elenca tutte le pedine
	 * dello schieramento nemico che mettono in scacco il re
	 *
	 * @param colore il colore dello schieramento da controllare
	 * @return Un ArrayList contenente tutti i pezzi che mettono in scacco il re
	 */
	public ArrayList<Pezzo> pedineScacco(Colore colore) {
		ArrayList<Pezzo> enemies;
		if (colore == BIANCO)
			enemies = neri;
		else
			enemies = bianchi;
		ArrayList<Pezzo> fine = new ArrayList<>();
		Posizione re = trovaRe(colore);
		for (Pezzo p : enemies)
		{
			if (verificaMossa(p, re))
				fine.add(p);
		}
		return fine;
	}

	/**
	 * Trova il re di un dato schieramento
	 *
	 * @param colore il colore dello schieramento
	 * @return Il re dello schieramento inserito
	 */
	private Posizione trovaRe(Colore colore) {
		if (colore == BIANCO)
		{
			for (int i = 0; i < bianchi.size(); i++)
			{
				if (bianchi.get(i).getSimbolo() == RE)
					return bianchi.get(i).getPosizione();
			}
		}
		else
			for (int i = 0; i < neri.size(); i++)
			{
				if (neri.get(i).getSimbolo() == RE)
					return neri.get(i).getPosizione();
			}
		return null;
	}

	/**
	 * Funzione con generico che permette di copiare un qualsiasi ArrayList,
	 * usando il metodo "clone"
	 *
	 * @param <E> la classe degli oggetti appartenenti all'ArrayList di partenza
	 * @param array l'ArrayList finale che coincide con quello iniziale (ma con
	 * indirizzo diverso)
	 * @return la copia dell'ArrayList passato come argomento
	 */
	static <E> ArrayList<E> copyArray(ArrayList<E> array) {
		ArrayList<E> finale = new ArrayList<>();
		for (E obj : array)
		{
			try
			{
				finale.add((E) obj.getClass().getMethod("clone").invoke(obj));
			}
			catch (Exception ex)
			{
			}
		}
		return finale;
	}

	/**
	 * Trova l'elenco delle posizioni in cui un pezzo si può muovere
	 *
	 * @param p il pezzo da controllare
	 * @return L'ArrayList delle mosse che il dato pezzo può fare
	 */
	public ArrayList<Posizione> elencoMosseScacco(Pezzo p) {
		ArrayList<Posizione> elenco = elencoMosse(p);
		ArrayList<Posizione> fine = new ArrayList<>();
		for (Posizione pos : elenco)
		{
			Partita partita = new Partita(bianchi, neri);
			Pezzo pezzo = partita.trovaPezzo(p.getPosizione());
			if (p.getSimbolo() == RE && (p.getPosizione().getColonna().ordinal() + 2 == pos.getColonna().ordinal() || p.getPosizione().getColonna().ordinal() - 2 == pos.getColonna().ordinal()))
				if (p.getPosizione().getColonna().ordinal() + 2 == pos.getColonna().ordinal() && !isArroccoCorretto(p, true))
					continue;
				else if (!isArroccoCorretto(p, false))
					continue;
			partita.muoviSenzaControlli(pezzo, pos);
			if (partita.pedineScacco(pezzo.getColore()).isEmpty())
				fine.add(pos);
		}
		return fine;
	}

	/**
	 * Controlla se la situazione dello schieramento inserito è di stallo
	 *
	 * @param colore lo schieramento da controllare
	 * @return true se si è in una situazione di stallo, altrimenti false
	 */
	public boolean isStallo(Colore colore) {
		ArrayList<Pezzo> array;
		if (isScacco(colore))
			return false;
		if (colore == BIANCO)
			array = bianchi;
		else
			array = neri;
		for (Pezzo p : array)
		{
			if (!elencoMosseScacco(p).isEmpty())
				return false;
		}
		return true;
	}

	/**
	 * Effettua il coding delle posizioni di tutti i pezzi della scacchiera.
	 * Questo serve per la "ripetizione di mossse"
	 */
	private void aggiungiRipezioneMosse() {
		StringBuilder sb = new StringBuilder();
		for (Riga r : Riga.values())
		{
			for (Colonna c : Colonna.values())
			{
				Pezzo p = trovaPezzo(new Posizione(r, c));
				if (p == null)
					sb.append('x');
				else
					sb.append(p.getSimbolo().getSimbolo());
			}
		}
		ripetizioneMosse.add(sb.toString());
	}

	/**
	 * Metodo che verifica se l'ultima mossa eseguita è stata già fatta altre
	 * due volte
	 *
	 * @return true se l'ultima mossa è stata fatta tre volte, false altrimenti
	 */
	private boolean isRipezioneMosse() {
		String last = ripetizioneMosse.get(ripetizioneMosse.size() - 1);
		int count = 0;
		for (String s : ripetizioneMosse)
		{
			if (s.equals(last))
				count++;
		}
		return count == 3;
	}

	/**
	 * Metodo che permette la promozione di un pezzo, se possibile
	 *
	 * @param p il pedone da promuovere
	 * @param simbolo il simbolo che il pedone dovrà assumere
	 * @return true se il pezzo può essere promosso, false altrimenti
	 */
	public boolean promozione(Pezzo p, Pezzo.Simbolo simbolo) {
		if (p.getSimbolo() == PEDONE && (p.getPosizione().getRiga() == R1 || p.getPosizione().getRiga() == R8))
		{
			p.setSimbolo(simbolo);
			mosse.get(mosse.size() - 1).setSimbolo(simbolo);
			return true;
		}
		return false;
	}
	
	/**
	 * To string che ritorna la visualizzazione della partita con uno standard che viene utilizzato per controllare la ripetizione di mosse
	 * 
	 * @return la codifica per l'ultima posizione della scacchiera
	 */
	@Override
	public String toString() {
		return ripetizioneMosse.get(ripetizioneMosse.size()-1);
	}
}
