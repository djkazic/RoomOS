package org.djkazic.RoomOS.extrun;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.djkazic.RoomOS.util.Utils;

public class AlarmRunnable implements Runnable {
	
	private Date current;
	private ArrayList<String> alarms;
	
	public AlarmRunnable() {
		alarms = new ArrayList<String> ();
	}

	public void run() {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm a");
			
			while(alarms.size() > 0) {
				System.out.println("outer loop hit");
				
				String selected = "";
				boolean breakNow = false;
				String currentStr = "";
				
				while(!breakNow) {
					try {
						current = new Date();
						currentStr = sdf.format(current);
						breakNow = alarms.contains(currentStr);
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				selected = currentStr;
				Utils uc = new Utils();
				String[] split = selected.split(" ");
				String timeColon = split[1];
				String[] csplit = timeColon.split(":");
				String hour = csplit[0];
				String minute = csplit[1];

				uc.speak("Alarm set at " + hour + ":" + minute + " has been triggered.");
				alarms.remove(selected);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addAlarm(String alarm) {
		alarms.add(alarm);
	}
}
