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
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javazoom.jl.decoder.JavaLayerException;

import org.djkazic.RoomOS.basemodules.Module;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import com.gtranslate.Audio;
import com.gtranslate.Language;

public class Utils {

	private RTCore rt;
	
	public Utils() {
		if(rt == null) {
			rt = RTCore.getInstance();
		}
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
		if(rt.audio == null) {
			getAudio();
		}
		try {
			//TODO: check for cached streams
			InputStream sound = rt.audio.getAudio(str, Language.ENGLISH);
			rt.audio.play(sound);
			if(rt.speakLatch == null) {
				rt.speakLatch = new CountDownLatch(1);
				rt.speakLatch.countDown();
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	
	@SuppressWarnings("rawtypes")
	public void findModules() {
		try {
			ClassPathScanningCandidateComponentProvider provider 
			= new ClassPathScanningCandidateComponentProvider(false);
			provider.addIncludeFilter(new AssignableTypeFilter(Module.class));
			Set<BeanDefinition> components = provider.findCandidateComponents("org/djkazic/RoomOS/modules");
			for (BeanDefinition component : components) {
			    Class cls = Class.forName(component.getBeanClassName());
			    Constructor constructor = cls.getConstructors()[0];
			    if(constructor.getParameterCount() == 0) {
			    	constructor.newInstance();
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
