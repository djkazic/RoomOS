package org.djkazic.RoomOS.rest;

import java.util.logging.LogManager;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.resource.Get;
import org.restlet.routing.Router;

public class APIRouter extends Application {

	public static void init() {
		try {
			Component component = new Component();
			component.getServers().add(Protocol.HTTP, 8080);
			component.getDefaultHost().attach(new APIRouter());
			component.start();
			LogManager.getLogManager().reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Restlet landPageRoute = new Restlet() {
		@Get
		public String process() {
			return "RoomOS REST Bridge v1.0";
		}
	};
	
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attachDefault(landPageRoute);
		router.attach("/bridge", BridgeRes.class);
		return router;
	}
}