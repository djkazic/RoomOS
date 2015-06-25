package org.djkazic.RoomOS.modules;

import java.util.concurrent.CountDownLatch;

import org.djkazic.RoomOS.RTCore;

public abstract class Module implements Runnable {

	protected String trigger;
	protected boolean exec;
	protected String resultText;
	protected CountDownLatch latch;

	public Module(String trigger) {
		this.trigger = trigger;
		latch = new CountDownLatch(1);
		RTCore.modules.add(this);
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
		boolean match = (cmd.equals(trigger));
		exec = match;
		return match;
	}
	
	public String getTrigger() {
		return trigger;
	}
	
	public CountDownLatch getLatch() {
		return latch;
	}
}
