/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scacchi.model.Traduzioni;

import com.scacchi.Scacchi;
import com.scacchi.model.TCP.Settings;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Pietro
 */
public class ListaLingue {

    private ArrayList<Lingua> lingue;
    private Lingua linguaCaricata;
    private static final String TRANSLATIONS_DIR = "com/scacchi/model/Traduzioni/Translations/";

    public ListaLingue() throws IOException {
        this.lingue = new ArrayList<>();
        CodeSource src = Scacchi.class.getProtectionDomain().getCodeSource();
        if (src != null)
        {
            URL jar = src.getLocation();
            ZipInputStream zip = new ZipInputStream(jar.openStream());
            while (true)
            {
                ZipEntry e = zip.getNextEntry();
                if (e == null)
                    break;
                String name = e.getName();
                if (name.startsWith(TRANSLATIONS_DIR) && !name.equals(TRANSLATIONS_DIR))
                {
                    this.lingue.add(new Lingua(name.substring(42, name.length() - 5)));
                    try
                    {
                        this.lingue.get(this.lingue.size() - 1).crea(zip);
                    }
                    catch (FileNotFoundException | JSONException ex)
                    {
                        Logger.getLogger(ListaLingue.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            zip.close();
            this.linguaCaricata = getLinguaByName(Settings.DEFAULT_LANGUAGE);
        }
        else
        {
            /* Fail... */
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
                return true;
            }
        }
        return false;
    }

    public boolean setLinguaCaricata(Lingua lingua) {
        if (!this.lingue.contains(lingua))
            return false;
        this.linguaCaricata = lingua;
        return true;
    }

    public Lingua getLinguaCaricata() {
        return linguaCaricata;
    }

    public ArrayList<Lingua> getLingue() {
        return lingue;
    }

    public Object get(String key) {
        try
        {
            return this.linguaCaricata.getJsonObj().get(key);
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    public String getString(String key) {
        try
        {
            return this.linguaCaricata.getJsonObj().getString(key);
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    public JSONObject getJSONObject(String key) {
        try
        {
            return this.linguaCaricata.getJsonObj().getJSONObject(key);
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
}
