package org.djkazic.RoomOS;

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

import org.djkazic.RoomOS.basemodules.Module;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import com.gtranslate.Audio;
import com.gtranslate.Language;

public class Utils {
	
	public Connection getConnection() {
		try {
			if(RTCore.connection == null) {
				Class.forName("org.sqlite.JDBC");
				Properties prop = new Properties();
				prop.setProperty("shared_cache", "true");
				RTCore.connection = DriverManager.getConnection("jdbc:sqlite:room_os.db", prop);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return RTCore.connection;
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
		RTCore.audio = Audio.getInstance();
	}
	
	public void speak(String str) {
		System.out.println("> " + str);
		if(RTCore.audio == null) {
			getAudio();
		}
		try {
			//TODO: check for cached streams
			InputStream sound = RTCore.audio.getAudio(str, Language.ENGLISH);
			RTCore.audio.play(sound);
			if(RTCore.speakLatch == null) {
				RTCore.speakLatch = new CountDownLatch(1);
				RTCore.speakLatch.countDown();
			}
		} catch (Exception e) {
			e.printStackTrace();
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
		int upperChar = 0;
	    String[] words = str.split(" ");
	    StringBuilder ret = new StringBuilder();
	    for(int i = 0; i < words.length; i++) {
	    	char firstChar = words[i].charAt(0);
	    	if(firstChar == '(' || firstChar == '[') {
	    		upperChar = 1;
	    	}
	    	ret.append(Character.toUpperCase(words[i].charAt(upperChar)));
	        ret.append(words[i].substring(1));
	        if(i < words.length - 1) {
	            ret.append(' ');
	        }
	    }
	    return ret.toString();
	}
	
	public ArrayList<String> getRuleNames(String prefix) {
		String[] allRules = RTCore.ruleGrammar.listRuleNames();
		ArrayList<String> ruleNames = new ArrayList<String> ();
		for(String str : allRules) {
			if(str.startsWith(prefix)) {
				ruleNames.add(str);
			}
		}
		return ruleNames;
	}
	
	public ArrayList<String> getAuthNames(String prefix) {
		String[] allRules = RTCore.ruleGrammar.listRuleNames();
		ArrayList<String> authNames = new ArrayList<String> ();
		for(String str : allRules) {
			if(str.startsWith(prefix)) {
				authNames.add(RTCore.ruleGrammar.getRule(str).toString());
			}
		}
		return authNames;
	}
}
