package com.scacchi.model.Traduzioni;

import com.scacchi.model.TCP.Settings;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Pietro
 */
public class Lingua {

	private String lingua;
	private JSONObject jsonObj;

	public Lingua(String lingua) {
		this.lingua = lingua;
		this.jsonObj = null;
	}

	public void crea(File folder) throws FileNotFoundException, IOException, JSONException {
		if (this.jsonObj != null)
			return;
		FileInputStream fis = new FileInputStream(folder + "/" + lingua + ".json");
		InputStreamReader fisr = new InputStreamReader(fis);
		BufferedReader fbr = new BufferedReader(fisr);
		StringBuilder fileContent = new StringBuilder();
		String line;
		while ((line = fbr.readLine()) != null)
		{
			fileContent.append(line);
		}
		fbr.close();
		this.jsonObj = new JSONObject(fileContent.toString());
	}

	public String getLingua() {
		return lingua;
	}

	public JSONObject getJsonObj() {
		return jsonObj;
	}
	
	public String toString() {
		try
		{
			return (String) ((JSONObject) Settings.lingue.getLinguaCaricata().getJsonObj().get("lingue")).get(this.lingua);
		}
		catch (JSONException ex)
		{
			Logger.getLogger(Lingua.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
}
