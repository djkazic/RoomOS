package org.djkazic.RoomOS.rest;

import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class BridgeRes extends ServerResource {

	@Post("application/json")
	public String process(JsonRepresentation entity) {
		JSONObject json = null;			
		try {
			json = entity.getJsonObject();
			if(json.has("client") && json.has("secret")) {
				return "CL: " + json.getString("client") + " | SC: " + json.getString("secret") + " SUCCESS";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
