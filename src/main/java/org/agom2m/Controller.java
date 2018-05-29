package org.agom2m;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agom2m.http.HTTPClient;
import org.agom2m.om2m.OM2MWrapper;
import org.apache.http.client.ClientProtocolException;

import client.VicinityClient;
import client.model.Triple;
import fr.irit.melodi.sparql.exceptions.NotAFolderException;
import fr.irit.melodi.sparql.files.FolderManager;

public class Controller {

	private FolderManager queries;
	private HTTPClient httpClient;
	private OM2MWrapper om2m;

	public Controller() {
		try {
			this.queries = new FolderManager("queries");
			this.queries.loadQueries();
			this.om2m = new OM2MWrapper();
			this.httpClient = new HTTPClient();
		} catch (NotAFolderException e) {
			e.printStackTrace();
		}
	}

	public String getQuery(String queryKey) {
		return this.queries.getQueries().get(queryKey);
	}

	public HTTPClient getHttpClient() {
		return this.httpClient;
	}
	
	public OM2MWrapper getOM2MWrapper(){
		return this.om2m;
	}
	
	public void performTestDiscovery(){
		/*
		 *  Data required for discovery relevant data and solve a SPARQL query
		 */
		String query = this.getQuery("default-discovery");

		Set<String> neighbours = new HashSet<String>();
		neighbours.add("http://api.stars4all.eu/photometers/stars91/observations/59035368cc15520001bbd12e");
		// An empty in-memory log
		StringBuilder log = new StringBuilder();

		// Retrieve from the Gateway API Services using a secured channel
		// A JSON-LD with a relevant TED for the query
		String jsonTED = this.getHttpClient().executeDiscoveryQuery(query);
		// A JSON document containing VICINITY ontology prefixes
		String jsonPrefixes = this.getHttpClient().retrievePrefixes();

		/*
		 *  Init the client
		 */
		VicinityClient client = new VicinityClient(jsonTED, jsonPrefixes);

		/*
		 *  Discovery
		 */
		while (client.existIterativelyDiscoverableThings()) {
			// Discover relevant resources in the TED
			List<String> neighboursThingsIRIs = client.discoverRelevantThingIRI();
			// Retrieve remote JSON data for each Thing IRI
			for (String neighboursThingIRI : neighboursThingsIRIs) {
				// retrieve the RDF located at the provided IRI
				/* Retrieve the JSON-LD exposed by the GATEWAY API SERVICES
				   for this IRI Thing */
				String thingsJsonRDF;
				try {
					thingsJsonRDF = this.httpClient.get(neighboursThingIRI);
					client.updateDiscovery(thingsJsonRDF);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		List<Triple<String, String, String>> relevantGatewayAPIAddresses = client.getRelevantGatewayAPIAddresses();
		/*
		 *  Distributed access thorugh secured channel
		 */
		for (Triple<String, String, String> neighbourGatewayAPIAddress : relevantGatewayAPIAddresses) {
			String gatewayApiAddress = neighbourGatewayAPIAddress.getThirdElement();
			// Retrieve the JSON document exposed by URL in gatewayApiAddress
			String jsonData;
			try {
				if(gatewayApiAddress.contains("belge.laas.fr")){
					String rid = gatewayApiAddress.split("~")[1];
					jsonData = this.om2m.retrieveSensorObservation(rid);
				} else {
					jsonData = this.httpClient.get(gatewayApiAddress);
				}
				neighbourGatewayAPIAddress.setThirdElement(jsonData);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/*
		 *  Solve query
		 */
		List<Map<String, String>> queryResults = client.solveQuery(query, relevantGatewayAPIAddresses);
		System.out.println(queryResults);
		client.close();
	}
	
	static {
//		System.setProperty("log4j.configurationFile", "log4j2.xml");
//		System.setProperty("log4j.configuration", "log4j.properties");
    }

	public static void main(String[] args) {
		Controller controller = new Controller();
		controller.performTestDiscovery();
//		System.out.println(controller.om2m.testRetrieveSensorObservation());
	}
}
