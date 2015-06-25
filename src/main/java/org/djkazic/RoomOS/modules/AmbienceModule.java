package org.djkazic.RoomOS.modules;

import org.djkazic.RoomOS.RTCore;
import org.djkazic.RoomOS.Utils;
import org.djkazic.RoomOS.basemodules.Module;

public class AmbienceModule extends Module {

	public AmbienceModule() {
		super("cmd_ambient_activate cmd_ambient_deactivate");
	}
	
	public void process() {
		Utils uc = new Utils();
		if(RTCore.ambientListening && rule.equals("cmd_ambient_deactivate")) {
			uc.speak("Disabling ambient listening");
			RTCore.ambientListening = false;
			latch.countDown(); //Go back to the main thread, where this will be instantiated again
			return;
		}
		
		if(!RTCore.ambientListening && rule.equals("cmd_ambient_activate")) {
			uc.speak("Enabling ambient listening");
			RTCore.ambientListening = true;
			latch.countDown(); //Go back to the main thread, where normal behavior resumes
			return;
		}
		
		//Did not meet either condition
		latch.countDown();
	}
}