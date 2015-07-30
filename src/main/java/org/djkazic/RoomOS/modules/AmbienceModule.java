package org.djkazic.RoomOS.modules;

import org.djkazic.RoomOS.RTCore;
import org.djkazic.RoomOS.basemodules.Module;
import org.djkazic.RoomOS.util.Settings;
import org.djkazic.RoomOS.util.Utils;

public class AmbienceModule extends Module {

	private RTCore rt;
	
	public AmbienceModule() {
		super("cmd_ambient_*");
	}
	
	public void process() {
		rt = RTCore.getInstance();
		
		if(Settings.gui) {
			RTCore.getWindow().setLoop("authenticate");
		}
		
		if(rt.ambientListening && rule.equals("cmd_ambient_deactivate")) {
			disableAmbience();
		} else if(!rt.ambientListening && rule.equals("cmd_ambient_activate")) {
			enableAmbience();
		}
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
		latch.countDown();
	}
	
	private void disableAmbience() {
		Utils uc = new Utils();
		uc.speak("Disabling ambient listening");
		rt.ambientListening = false;
	}
	
	private void enableAmbience() {
		Utils uc = new Utils();
		uc.speak("Enabling ambient listening");
		rt.ambientListening = true;
	}
}