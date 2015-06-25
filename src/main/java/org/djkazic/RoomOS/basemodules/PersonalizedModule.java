package org.djkazic.RoomOS.basemodules;

import org.djkazic.RoomOS.RTCore;
import org.djkazic.RoomOS.Utils;

public abstract class PersonalizedModule extends Module {
	
	private Utils uc;
	private boolean loggedIn;
	
	public PersonalizedModule(String trigger) {
		super(trigger);
		uc = new Utils();
	}

	public boolean filter(String cmd) {
		boolean basicMatch = super.filter(cmd);
		if(loggedIn = RTCore.getCurrentProfile() == null) {
			uc.speak("No current profile is logged in.");
		}
		return (basicMatch && !loggedIn);
	}
	
	public boolean getIndependentBoolean() {
		return loggedIn;
	}
}
