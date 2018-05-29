package org.agom2m.om2m;

import java.io.File;
import fr.laas.om2m.client.ClientFactory;
import fr.laas.om2m.client.OM2MClient;

import org.eclipse.om2m.commons.constants.ResourceType;
import org.eclipse.om2m.commons.resource.ContentInstance;

public class OM2MWrapper {
	private OM2MClient client;
	
	public OM2MWrapper(){
		this.client = ClientFactory.createClient(new File("default-config.json"));
	}
	
	private static final String temperatureSensorId = "/BBB_ADREAM_1/ETH_GW/PHG_TMP_05/DATA/la";
	
	public String testRetrieveSensorObservation(){
		return retrieveSensorObservation(temperatureSensorId);
	}
	
	public String retrieveSensorObservation(String id){
		ContentInstance cin = (ContentInstance)this.client.retrieveResourceFromId("/BBB_ADREAM_1/ETH_GW/PHG_TMP_05/DATA/la", ResourceType.CONTENT_INSTANCE);
		String content = cin.getContent();
		return ObixConverter.obix2json(content);
	}
	
	
}
