package org.djkazic.RoomOS.modules;

import org.djkazic.RoomOS.basemodules.Module;
import org.djkazic.RoomOS.sql.ResponseFetcher;
import org.djkazic.RoomOS.util.Utils;

public class PersonalityModule extends Module {

	public PersonalityModule() {
		super("personality_*");
	}

	public void process() {
		Utils uc = new Utils();
		ResponseFetcher rf = new ResponseFetcher();
		uc.speak(rf.queryForRule(rule));
		latch.countDown();
	}
}
