package org.djkazic.RoomOS.modules;

import java.io.FileNotFoundException;

import org.djkazic.RoomOS.basemodules.Module;
import org.djkazic.RoomOS.util.Utils;

public class EEModule extends Module {

	public EEModule() {
		super("ee");
	}

	public void process() {
		Utils uc = new Utils();
		String asset = "";
		if(resultText.startsWith("wish me")) {
			asset = "fathersday";
		} else if(resultText.startsWith("motivate me")) {
			asset = "motivation";
		}
		if(!asset.equals("")) {
			try {
				uc.playAudio(asset);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		latch.countDown();
	}
}