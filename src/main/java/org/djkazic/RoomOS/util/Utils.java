package org.djkazic.RoomOS.util;

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

import org.djkazic.RoomOS.RTCore;
import org.djkazic.RoomOS.basemodules.Module;

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
				if(rt.microphone != null && rt.microphone.isRecording()) {
					rt.microphone.stopRecording();
				}
				rt.audio.play(sound);
				if(rt.microphone != null) {
					rt.microphone.clear();
					rt.microphone.startRecording();
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
	
	public int wordToInt(String word) {
		int num = 0;
		switch(word) {
			case "one":  num = 1;
			break;
			case "two":  num = 2;
			break;
			case "three":  num = 3;
			break;
			case "four":  num = 4;
			break;
			case "five":  num = 5;
			break;
			case "six":  num = 6;
			break;
			case "seven":  num = 7;
			break;
			case "eight":  num = 8;
			break;
			case "nine":  num = 9;
			break;
			case "ten": num = 10;
			break;
			case "eleven": num = 11;
			break;
			case "twelve": num = 12;
			break;
			case "thirteen": num = 13;
			break;
			case "fourteen": num = 14;
			break;             
			case "fifteen": num = 15;
			break;
			case "sixteen": num = 16;
			break;
			case "seventeen": num = 17;
			break;
			case "eighteen": num = 18;
			break;
			case "nineteen": num = 19;
			break;
			case "twenty":  num = 20;
			break;
			case "thirty":  num = 30;
			break;
			case "forty":  num = 40;
			break;
			case "fifty":  num = 50;
			break;
			case "sixty":  num = 60;
			break;
			case "seventy":  num = 70;
			break;
			case"eighty":  num = 80;
			break;
			case "ninety":  num = 90;
			break; 
			case "hundred": num = 100;
			break;
			case "thousand": num = 1000;
			break;
		}
		return num;
	}
}
