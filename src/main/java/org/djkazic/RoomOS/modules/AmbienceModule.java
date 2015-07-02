package org.djkazic.RoomOS.modules;

import org.djkazic.RoomOS.RTCore;
import org.djkazic.RoomOS.basemodules.Module;
import org.djkazic.RoomOS.util.Settings;
import org.djkazic.RoomOS.util.Utils;

public class AmbienceModule extends Module {

	private RTCore rt;
	
	public AmbienceModule() {
		super("cmd_ambient_*");
		rt = RTCore.getInstance();
	}
	
	public void process() {
		if(Settings.gui) {
			RTCore.getWindow().setLoop("authenticate");
		}
		
		Utils uc = new Utils();
		if(rt.ambientListening && rule.equals("cmd_ambient_deactivate")) {
			uc.speak("Disabling ambient listening");
			rt.ambientListening = false;
			latch.countDown();
			return;
		}
		
		if(!rt.ambientListening && rule.equals("cmd_ambient_activate")) {
			uc.speak("Enabling ambient listening");
			rt.ambientListening = true;
			latch.countDown();
			return;
		}
		latch.countDown();
	}
}