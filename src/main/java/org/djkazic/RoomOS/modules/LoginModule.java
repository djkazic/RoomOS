package org.djkazic.RoomOS.modules;

import java.util.ArrayList;

import org.djkazic.RoomOS.RTCore;
import org.djkazic.RoomOS.Utils;
import org.djkazic.RoomOS.basemodules.Module;

public class LoginModule extends Module {
	
	private Utils uc;

	public LoginModule() {
		super("cmd_login");
		uc = new Utils();
	}

	public void process() {
		try {
			ArrayList<String> ruleNames = new ArrayList<String> ();
			ArrayList<String> authNames = new ArrayList<String> ();
			ruleNames = uc.getRuleNames("id_");
			authNames = uc.getAuthNames("id_");
			
			int index = -1;
			String[] split = resultText.split(" ");
			outerloop:
			for(int i=0; i < authNames.size(); i++) {
				for(int j=0; j < split.length; j++) {
					if(split[j].equals(authNames.get(i))) {
						index = i;
						break outerloop;
					}
				}
			}
			String load = null;
			if(index != -1) {
				load = ruleNames.get(index).substring(3);
			}
			RTCore.setCurrentProfile(load);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
