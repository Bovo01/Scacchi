/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi.model.Traduzioni;

import com.scacchi.model.TCP.Settings;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;

/**
 *
 * @author Pietro
 */
public class ListaLingue {

	private ArrayList<Lingua> lingue;
	private Lingua linguaCaricata;
	private final File translationsFolder;

	public ListaLingue() {
		this.lingue = new ArrayList<>();
		ClassLoader cl = getClass().getClassLoader();
		this.translationsFolder = new File(cl.getResource("./com/scacchi/model/Traduzioni/Translations").getFile());
		for (File file : this.translationsFolder.listFiles())
		{
			this.lingue.add(new Lingua(file.getName().substring(0, file.getName().length() - 5)));
		}
		this.linguaCaricata = getLinguaByName(Settings.DEFAULT_LANGUAGE);
		try
		{
			this.linguaCaricata.crea(this.translationsFolder); //Non dovrebbe dare problemi ma non si sa mai
		}
		catch (IOException | JSONException ex)
		{
			Logger.getLogger(ListaLingue.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public Lingua getLinguaByName(String name) {
		for (Lingua lingua : this.lingue)
		{
			if (lingua.getLingua().equals(name))
				return lingua;
		}
		return null;
	}

	public boolean setLinguaCaricata(String name) {
		for (Lingua lingua : this.lingue)
		{
			if (lingua.getLingua().equals(name))
			{
				this.linguaCaricata = lingua;
				try
				{
					this.linguaCaricata.crea(this.translationsFolder); //Non dovrebbe dare problemi ma non si sa mai
				}
				catch (IOException | JSONException ex)
				{
					Logger.getLogger(ListaLingue.class.getName()).log(Level.SEVERE, null, ex);
				}
				return true;
			}
		}
		return false;
	}

	public boolean setLinguaCaricata(Lingua lingua) {
		if(!this.lingue.contains(lingua))
			return false;
		this.linguaCaricata = lingua;
		try
		{
			this.linguaCaricata.crea(this.translationsFolder);
		}
		catch (IOException | JSONException ex)
		{
			Logger.getLogger(ListaLingue.class.getName()).log(Level.SEVERE, null, ex);
		}
		return true;
	}

	public Lingua getLinguaCaricata() {
		return linguaCaricata;
	}

	public ArrayList<Lingua> getLingue() {
		return lingue;
	}
}
