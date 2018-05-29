package org.agom2m;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agom2m.http.HTTPClient;

import client.VicinityClient;
import client.model.Triple;
import fr.irit.melodi.sparql.exceptions.NotAFolderException;
import fr.irit.melodi.sparql.files.FolderManager;

public class Controller {
	
	
	
	private FolderManager queries;
	private HTTPClient httpClient;
	
	public Controller(){
		try {
			this.queries = new FolderManager("queries");
			this.queries.loadQueries();
			this.httpClient = new HTTPClient();
		} catch (NotAFolderException e) {
			e.printStackTrace();
		}
	}
	
	public String getQuery(String queryKey){
		return this.queries.getQueries().get(queryKey);
	}
	
	public HTTPClient getHttpClient(){
		return this.httpClient;
	}
	
	public static void main(String[] args) {
		Controller controller = new Controller();
		// -- Data required for discovery relevant data and solve a SPARQL query
		String query  = controller.getQuery("default-discovery"); // A SPARQL query
		Set<String> neighbours = new HashSet<String>(); // A set of neighbour oids
		neighbours.add("http://api.stars4all.eu/photometers/stars91/observations/59035368cc15520001bbd12e");
		StringBuilder log = new StringBuilder(); // An empty in-memory log
		    
		// Retrieve from the Gateway API Services using a secured channel (datails)
		String jsonTED = controller.getHttpClient().executeDiscoveryQuery(query);  	  // A JSON-LD with a relevant TED for the query
		String jsonPrefixes = controller.getHttpClient().retrievePrefixes(); // A JSON document containing VICINITY ontology prefixes

		// -- Init the client
		VicinityClient client = new VicinityClient(jsonTED, neighbours, jsonPrefixes);

		// -- Discovery
		while(client.existIterativelyDiscoverableThings()){
		  // Discover relevant resources in the TED
		  List<String> neighboursThingsIRIs = client.discoverRelevantThingIRI();
		  // Retrieve remote JSON data for each Thing IRI
		  for(String neighboursThingIRI:neighboursThingsIRIs){
			  System.out.println(neighboursThingIRI);
			// retrieve the RDF located at the provided IRI
		    String thingsJsonRDF = ""; // Retrieve the JSON-LD exposed by the GATEWAY API SERVICES for this IRI Thing 
		    client.updateDiscovery(thingsJsonRDF);
		  }
		}
		List<Triple<String,String,String>> relevantGatewayAPIAddresses = client.getRelevantGatewayAPIAddresses();

		// -- Distributed access thorugh secured channel
		for(Triple<String,String,String> neighbourGatewayAPIAddress:relevantGatewayAPIAddresses){ 
		   String gatewayApiAddress =  neighbourGatewayAPIAddress.getThirdElement();
		   
		   String jsonData = ""; // Retrieve the JSON document exposed by URL in gatewayApiAddress
		   neighbourGatewayAPIAddress.setThirdElement(jsonData);
		}

		// -- Solve query
		List<Map<String,String>> queryResults = client.solveQuery(query, relevantGatewayAPIAddresses);
		client.close();
	}
}
