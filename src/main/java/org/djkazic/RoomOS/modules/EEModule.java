package org.djkazic.RoomOS.modules;

import java.io.FileNotFoundException;

import org.djkazic.RoomOS.Utils;
import org.djkazic.RoomOS.basemodules.Module;

public class EEModule extends Module {

	public EEModule() {
		super("ee");
	}

	public void process() {
		Utils uc = new Utils();
		String asset = "";
		if(resultText.contains("wish me")) {
			asset = "fathersday";
		}
		try {
			uc.playAudio(asset);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		latch.countDown();
	}
}