package org.djkazic.RoomOS.basemodules;

import org.djkazic.RoomOS.RTCore;
import org.djkazic.RoomOS.Utils;

public abstract class PersonalizedModule extends Module {
	
	public PersonalizedModule(String trigger) {
		super(trigger);
	}
	
	public boolean getIndependentBoolean() {
		Utils uc = new Utils();
		boolean indep = RTCore.getInstance().getCurrentProfile() != null;
		if(!indep) {
			uc.speak("I require profile data for this action, please log in.");
		}
		return indep;
	}
}
