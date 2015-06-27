package org.djkazic.RoomOS.modules;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.djkazic.RoomOS.Utils;
import org.djkazic.RoomOS.basemodules.Module;

public class AlarmModule extends Module {
	
	public AlarmModule() {
		super("cmd_alarm");
	}

	public void process() {
		Utils uc = new Utils();
		
		resultText.replace("oh", "");
		String[] rsplit = resultText.split("at");
		String timeStr = rsplit[1];
		String[] hourMinSplit = timeStr.split(" ");
		
		int hour = uc.wordToInt(hourMinSplit[1]);
		String minute = "00";
		
		String minuteAmPmBlock = "";
		for(int i=2; i < hourMinSplit.length; i++) {
			minuteAmPmBlock += hourMinSplit[i] + " ";
		}
		
		String[] minAmPmSplit = minuteAmPmBlock.split(" ");
		String ampm = null;
		
		if(minuteAmPmBlock.contains("clock")) {
			ampm = minAmPmSplit[1].replace("clock", "") + minAmPmSplit[2] + minAmPmSplit[3];
		} else {
			if(minAmPmSplit.length > 3) {
				//Compound minute
				minute = (uc.wordToInt(minAmPmSplit[0]) + uc.wordToInt(minAmPmSplit[1])) + "";
				ampm = minAmPmSplit[2] + minAmPmSplit[3];
			} else if(minAmPmSplit.length == 3) {
				//Regular minute
				minute = uc.wordToInt(minAmPmSplit[0]) + "";
				ampm = minAmPmSplit[1] + minAmPmSplit[2];
			}
		}
		
		Date currently = new Date();
		SimpleDateFormat justDay = new SimpleDateFormat("MM-dd-yyyy");
		String preParse = justDay.format(currently) + " " + hour + ":" + minute + " " + ampm;
		
		final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm a");

		Date alarmTime = new Date();
		String parsedStr = "";
		try {
			alarmTime = sdf.parse(preParse);
			parsedStr = sdf.format(alarmTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		final String parsedStrPass = parsedStr;
		
		if(alarmTime.before(currently)) {
			uc.speak("Cannot set alarm in past.");
		} else {
			(new Thread(new Runnable() {
				private Date current;
				
				public void run() {
					boolean breakNow = false;
					while(!breakNow) {
						try {
							current = new Date();
							String currentStr = sdf.format(current);
							breakNow = (parsedStrPass.equals(currentStr));
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
					Utils uc = new Utils();
					String[] split = parsedStrPass.split(" ");
					String timeColon = split[1];
					String[] csplit = timeColon.split(":");
					String hour = csplit[0];
					String minute = csplit[1];
					
					uc.speak("Alarm set at " + hour + ":" + minute + " has been triggered.");
				}
			})).start();
			uc.speak("Alarm has been set.");
		}
		
		latch.countDown();
	}
}
