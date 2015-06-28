package org.djkazic.RoomOS;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AlarmRunnable implements Runnable {
	
	private Date current;
	private ArrayList<String> alarms;
	
	public AlarmRunnable() {
		alarms = new ArrayList<String> ();
	}

	public void run() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm a");
		
		while(alarms.size() > 0) {
			String selected = "";
			boolean breakNow = false;
			while(!breakNow) {
				try {
					current = new Date();
					String currentStr = sdf.format(current);
					for(String str : alarms) {
						breakNow = (str.equals(currentStr));
						selected = str;
					}
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			Utils uc = new Utils();
			String[] split = selected.split(" ");
			String timeColon = split[1];
			String[] csplit = timeColon.split(":");
			String hour = csplit[0];
			String minute = csplit[1];

			uc.speak("Alarm set at " + hour + ":" + minute + " has been triggered.");
			alarms.remove(selected);
		}
	}

	public void addAlarm(String alarm) {
		alarms.add(alarm);
	}
}
