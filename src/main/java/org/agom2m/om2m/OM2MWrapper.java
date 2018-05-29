package org.agom2m.om2m;

import java.io.File;

import fr.laas.om2m.client.ClientFactory;
import fr.laas.om2m.client.OM2MClient;

public class OM2MWrapper {
	private OM2MClient client;
	
	public OM2MWrapper(){
		this.client = ClientFactory.createClient(new File("default-config.json"));
	}
}
