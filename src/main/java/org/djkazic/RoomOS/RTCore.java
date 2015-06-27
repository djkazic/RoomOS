package org.djkazic.RoomOS;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.speech.recognition.RuleGrammar;
import javax.speech.recognition.RuleParse;
import org.djkazic.RoomOS.basemodules.Module;
import org.djkazic.RoomOS.basemodules.PersonalizedModule;
import org.djkazic.RoomOS.modules.AmbienceModule;
import org.djkazic.RoomOS.modules.SCModule;
import org.djkazic.RoomOS.sql.ResponseFetcher;
import com.gtranslate.Audio;
import com.sun.speech.engine.recognition.BaseRecognizer;
import com.sun.speech.engine.recognition.BaseRuleGrammar;
import com.sun.syndication.feed.synd.SyndEntry;
import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.jsgf.JSGFGrammar;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;

public class RTCore implements Runnable {

	public Audio audio;
	public Connection connection;
	public Microphone microphone;
	public RuleGrammar ruleGrammar;
	public boolean ambientListening;
	public ArrayList<Module> modules;
	public CountDownLatch speakLatch;
	public List<SyndEntry> alreadyRead;

	private Utils uc;
	private ResponseFetcher rf;
	private boolean playingSong;
	private Recognizer recognizer;
	private Profile currentProfile;
	private ConfigurationManager cm;
	private ArrayList<Profile> profiles;
	private BaseRecognizer jsapiRecognizer;
	
	private static RTCore rtcore;
	
	/**
	 * Starter method
	 * @param args: command line
	 */
	public static void main(String[] args) {
		rtcore = new RTCore();
		(new Thread(rtcore)).start();
	}

	public void init() {
		try {
			//Set logger
			System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "ERROR");
			
			modules = new ArrayList<Module> ();
			profiles = new ArrayList<Profile> ();
			alreadyRead = new ArrayList<SyndEntry> ();
			ambientListening = true;

			rf = new ResponseFetcher();
			Profile.loadProfiles();
			currentProfile = null;

			cm = new ConfigurationManager(RTCore.class.getResource("core.xml"));
			uc = new Utils();

			uc.speak("Recognition subsystem online.");
			uc.speak("Loading process modules.");
			uc.findModules();

			recognizer = (Recognizer) cm.lookup("recognizer");
			recognizer.allocate();

			microphone = (Microphone) cm.lookup("microphone");

			JSGFGrammar jsgf = (JSGFGrammar) cm.lookup("jsgfGrammar");

			jsapiRecognizer = new BaseRecognizer(jsgf.getGrammarManager());
			jsapiRecognizer.allocate();

			ruleGrammar = new BaseRuleGrammar(jsapiRecognizer, jsgf.getRuleGrammar());
			uc.speak("Standing by.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Main threaded method - processes recognition
	 */
	public void run() {
		init();
		try {
			//Regular control
			ArrayList<String> musicControls = new ArrayList<String> ();
			musicControls = uc.getRuleNames("cmd_music_ctrl");

			while (true) {

				Result result = recognizer.recognize();
				if(result != null) {
					String resultText = result.getBestFinalResultNoFiller();
					RuleParse rp = ruleGrammar.parse(resultText, null);

					if(rp != null) {
						String rule = rp.getRuleName().getRuleName();

						if(ambientListening) {
							if(playingSong) {
								if(musicControls.contains(rule)) {
									if(!resultText.equals("")) {
										System.out.println();
										System.out.println("[M] User said: " + resultText);
									}
									//Do pausing or stopping
									for(Module m : modules) {
										if(m instanceof SCModule) {
											if(rule.endsWith("quit")) {
												((SCModule) m).stop();
												playingSong = false;
												uc.speak("Music controls disabled.");
												microphone.clear();
											} else if(rule.endsWith("pause")) {
												((SCModule) m).stop();
											} else if(rule.endsWith("resume")) {
												((SCModule) m).resume();
												(new Thread(m)).start();
											}
										}
									}
								}
							} else {
								if(!musicControls.contains(rule)) {							
									if(!resultText.equals("")) {
										System.out.println();
										System.out.println("User said: " + resultText);
									}
									boolean moduleFound = false;
									for(Module m : modules) {
										if(m.filter(rule)) {
											if(m instanceof PersonalizedModule) {
												if(!((PersonalizedModule) m).getIndependentBoolean()) {
													moduleFound = true;
													continue;
												}
											}
											if(m instanceof SCModule) {
												playingSong = true;
											} 
											m.setText(resultText);
											m.setRule(rule);		
											microphone.stopRecording();
											(new Thread(m)).start();
											m.getLatch().await();
											Thread.sleep(1000);
											//TODO: may have to set result to null
											moduleFound = true;
										}
									}
									if(!moduleFound) {
										String speakText = rf.queryForRule(rule);
										uc.speak(speakText); //Speak DB response
									}
								}
							}
						} else {
							for(Module m : modules) {
								if(m instanceof AmbienceModule) {
									m.setRule(rule);
									(new Thread(m)).start();
									m.getLatch().await();
								}
							}
						}
					}
				} else {
					//System.out.println("Discarded loop");
					//uc.speak("I couldn't process that.");
				}
				if(playingSong) {
					Thread.sleep(200);
				} else {
					Thread.sleep(100);
				}
				//System.out.println("> LOOP <");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static RTCore getInstance() {
		return rtcore;
	}

	public ArrayList<Profile> getProfileList() {
		return profiles;
	}

	public void setCurrentProfile(String profName) {
		boolean set = false;
		for(Profile prof : profiles) {
			if(prof.getName().equalsIgnoreCase(profName)) {
				currentProfile = prof;
				set = true;
			}
		}
		if(!set) {
			uc.speak("Profile " + profName + " could not be loaded.");
		} else {
			//TODO: prompt for PIN
			uc.speak("Profile logged in successfully. Welcome, " + currentProfile.getName() + ".");
		}
	}

	public Profile getCurrentProfile() {
		return currentProfile;
	}
}