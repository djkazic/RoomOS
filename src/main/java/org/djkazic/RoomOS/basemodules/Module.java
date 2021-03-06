package org.djkazic.RoomOS.basemodules;

import java.util.concurrent.CountDownLatch;

import org.djkazic.RoomOS.RTCore;

public abstract class Module implements Runnable {

	protected String trigger;
	protected String rule;
	protected boolean exec;
	protected String resultText;
	protected CountDownLatch latch;

	public Module(String trigger) {
		this.trigger = trigger;
		latch = new CountDownLatch(1);
		RTCore.getInstance().modules.add(this);
	}

	public abstract void process();
	
	public void setText(String text) {
		resultText = text;
	}
	
	public void run() {
		if(exec) {
			process();
		}
	}
	
	public boolean filter(String cmd) {
		boolean match = false;
		if(trigger.contains("*")) {
			match = cmd.startsWith(trigger.substring(0, trigger.length() - 2));
		} else {
			match = (cmd.equals(trigger));
		}
		exec = match;
		return match;
	}
	
	public void regenLatch() {
		latch = new CountDownLatch(1);
	}
	
	public String getTrigger() {
		return trigger;
	}
	
	public CountDownLatch getLatch() {
		return latch;
	}
	
	public void setRule(String str) {
		rule = str;
	}
}
