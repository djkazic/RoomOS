package org.djkazic.RoomOS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

import javazoom.jl.decoder.JavaLayerException;

import org.djkazic.RoomOS.basemodules.Module;
import org.djkazic.RoomOS.modules.AmbienceModule;
import org.djkazic.RoomOS.modules.EEModule;
import org.djkazic.RoomOS.modules.LoginModule;
import org.djkazic.RoomOS.modules.NewsModule;
import org.djkazic.RoomOS.modules.SCModule;

import com.gtranslate.Audio;
import com.gtranslate.Language;
import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;

public class Utils {

	private RTCore rt;
	
	public Utils() {
		rt = RTCore.getInstance();
	}

	public Connection getConnection() {
		try {
			if(rt.connection == null) {
				Class.forName("org.sqlite.JDBC");
				Properties prop = new Properties();
				prop.setProperty("shared_cache", "true");
				rt.connection = DriverManager.getConnection("jdbc:sqlite:room_os.db", prop);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rt.connection;
	}
	
	public ResultSet doQuery(Connection connection, String query) {
		try {
			Statement stmt = connection.createStatement();
			stmt.setQueryTimeout(40);
			ResultSet rs = stmt.executeQuery(query);
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void getAudio() {
		rt.audio = Audio.getInstance();
	}
	
	public void speak(String str) {
		System.out.println("> " + str);
		if(Settings.vocal) {
			if(rt.audio == null) {
				getAudio();
			}
			try {
				//TODO: check for cached streams
				InputStream sound = rt.audio.getAudio(str, Language.ENGLISH);
				rt.audio.play(sound);
				if(rt.microphone != null && rt.microphone.isRecording()) {
					rt.microphone.stopRecording();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void playAudio(String assetName) throws FileNotFoundException {
		File file = new File("assets/" + assetName);
		if(!file.exists()) {
			speak("Attempt to play " + assetName + " failed.");
		} else {
			FileInputStream fis = new FileInputStream(file);
			try {
				rt.audio.play(fis);
			} catch (JavaLayerException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void findModules() {
		try {
			for(PojoClass pojoClass : PojoClassFactory
									  .enumerateClassesByExtendingType("org.djkazic.RoomOS.modules", Module.class, null)) {
				Constructor<?> con = pojoClass.getClazz().getConstructors()[0];
				if(con.getParameterCount() == 0) {
					con.newInstance();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String firstCaps(String str) {
		String output = "";
		String[] words = str.split(" ");
		for(int i=0; i < words.length; i++) {
			if(words[i].startsWith("(") || words[i].startsWith("[") || words[i].startsWith("\"")) {
				words[i] = words[i].substring(0, 1) + words[i].substring(1, 2).toUpperCase() + words[i].substring(2);
			} else {
				words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
			}
		}
		for(String word : words) {
			output += word + " ";
		}
		return output.substring(0, output.length() - 1);
	}
	
	public ArrayList<String> getRuleNames(String prefix) {
		String[] allRules = rt.ruleGrammar.listRuleNames();
		ArrayList<String> ruleNames = new ArrayList<String> ();
		for(String str : allRules) {
			if(str.startsWith(prefix)) {
				ruleNames.add(str);
			}
		}
		return ruleNames;
	}
	
	public ArrayList<String> getAuthNames(String prefix) {
		String[] allRules = rt.ruleGrammar.listRuleNames();
		ArrayList<String> authNames = new ArrayList<String> ();
		for(String str : allRules) {
			if(str.startsWith(prefix)) {
				authNames.add(rt.ruleGrammar.getRule(str).toString());
			}
		}
		return authNames;
	}
}
