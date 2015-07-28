package org.djkazic.RoomOS.modules;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.djkazic.RoomOS.RTCore;
import org.djkazic.RoomOS.basemodules.Module;
import org.djkazic.RoomOS.modext.AlarmRunnable;
import org.djkazic.RoomOS.util.Settings;
import org.djkazic.RoomOS.util.Utils;

public class AlarmModule extends Module {

	private AlarmRunnable ar;

	public AlarmModule() {
		super("cmd_alarm_*");
	}

	public void process() {
		if(Settings.gui) {
			RTCore.getWindow().setLoop("alarm");
		}

		Utils uc = new Utils();

		if(rule.equals("cmd_alarm_laundry")) {
			Date currently = new Date();
			long curMs = currently.getTime();
			Date laundry = new Date(curMs + (30 * 60000));
			Date dryer = new Date(curMs + ((30 + 65) * 60000));
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm a");
			
			ar = new AlarmRunnable();
			Thread alarmThread = new Thread(ar);
			alarmThread.start();
			
			ar.addAlarm(sdf.format(laundry));
			ar.addAlarm(sdf.format(dryer));
			
			uc.speak("Laundry alarm has been set.");
		} else if(rule.equals("cmd_alarm_gen")) {
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

			if(alarmTime.before(currently)) {
				uc.speak("Cannot set alarm in past.");
			} else {
				if(ar == null) {
					ar = new AlarmRunnable();
					Thread alarmThread = new Thread(ar);
					alarmThread.start();
				}
				ar.addAlarm(parsedStr);
				uc.speak("Alarm has been set.");
			}
		}

		latch.countDown();
	}
}
