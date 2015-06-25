package org.djkazic.RoomOS;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.speech.recognition.RuleGrammar;
import javax.speech.recognition.RuleParse;
import org.djkazic.RoomOS.basemodules.Module;
import org.djkazic.RoomOS.basemodules.PersonalizedModule;
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

	public static List<SyndEntry> alreadyRead;
	public static ArrayList<Module> modules;
	public static Connection connection;
	public static Microphone microphone;
	public static boolean playingSong;
	public static CountDownLatch speakLatch;
	public static Audio audio;

	private static BaseRecognizer jsapiRecognizer;
	private static boolean ambientListening;
	private static ConfigurationManager cm;
	public static RuleGrammar ruleGrammar;
	private static ArrayList<Profile> profiles;
	private static Profile currentProfile;
	private static Recognizer recognizer;
	private static ResponseFetcher rf;
	private static Utils uc;

	/**
	 * Starter method
	 * @param args: command line
	 */
	public static void main(String[] args) {
		(new Thread(new RTCore())).start();
	}

	/**
	 * Default constructor
	 */
	public RTCore() {
		try {
			ambientListening = true;
			modules = new ArrayList<Module> ();
			profiles = new ArrayList<Profile> ();
			alreadyRead = new ArrayList<SyndEntry> ();

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
		try {
			if(!microphone.startRecording()) {
				uc.speak("Could not initialize microphone.");
				recognizer.deallocate();
				System.exit(1);
			}
			
			//Regular control
			ArrayList<String> musicControls = new ArrayList<String> ();
			musicControls = uc.getRuleNames("cmd_music_ctrl");
			
			while (true) {
				if(speakLatch != null) {
					speakLatch.await();
					speakLatch = null;
				}
				Result result = recognizer.recognize();
				if(result != null) {
					String resultText = result.getBestFinalResultNoFiller();
					RuleParse rp = ruleGrammar.parse(resultText, null);

					if(rp != null) {
						String rule = rp.getRuleName().getRuleName();

						if(!ambientListening) {
							//Ambient listen reactivation
							if(rule.equals("cmd_ambient_activate")) {
								ambientListening = true;
								uc.speak("Ambient listening now active.");
							}
						} else {
							//TODO: this will be spoken if in SQL response
							if(rule.equals("cmd_ambient_activate")) {
								uc.speak("Ambient listening is already active.");
							}
							
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
											} else if(rule.endsWith("pause")) {
												((SCModule) m).stop();
											} else if(rule.endsWith("replay")) {
												((SCModule) m).replay();
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

									//Ambient listening plugin
									boolean isAmbientCmd = rule.equals("cmd_ambient_deactivate");
									if(isAmbientCmd) {
										ambientListening = false;
										uc.speak("Ambient listening deactivated.");
									} else {
										boolean moduleFound = false;
										for(Module m : modules) {
											if(m.filter(rule)) {
												if(m instanceof PersonalizedModule) {
													if(!((PersonalizedModule) m).getIndependentBoolean()) {
														moduleFound = true;
														continue;
													}
												}
												Thread mt = new Thread(m);
												if(m instanceof SCModule) {
													playingSong = true;
												} else {
													m.setText(resultText);
												}
												mt.start();
												m.getLatch().await();
												Thread.sleep(1000);
												moduleFound = true;
											}
										}
										
										if(!moduleFound && !isAmbientCmd) {
											String speakText = rf.queryForRule(rule);
											uc.speak(speakText); //Speak DB response
										}
									}
								}
							}
						}
					}
				} else {
					uc.speak("I couldn't process that.");
				}
				Thread.sleep(100);
				//System.out.println("> LOOP <");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static ArrayList<Profile> getProfileList() {
		return profiles;
	}
	
	public static void setCurrentProfile(String profName) {
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
	
	public static Profile getCurrentProfile() {
		return currentProfile;
	}
}