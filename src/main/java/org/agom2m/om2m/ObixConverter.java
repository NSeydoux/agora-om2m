package org.agom2m.om2m;

import org.eclipse.om2m.commons.obix.Bool;
import org.eclipse.om2m.commons.obix.Int;
import org.eclipse.om2m.commons.obix.Obj;
import org.eclipse.om2m.commons.obix.Real;
import org.eclipse.om2m.commons.obix.Str;
import org.eclipse.om2m.commons.obix.io.ObixDecoder;

public class ObixConverter {
	private static String buildJsonEntry(String key, String value){
		StringBuilder result = new StringBuilder();
		result.append("\"");
		result.append(key);
		result.append("\":");
		result.append("\"");
		result.append(value);
		result.append("\",\n");
		return result.toString();
	}
	
	private static String buildJsonEntry(String key, Integer value){
		StringBuilder result = new StringBuilder();
		result.append("\"");
		result.append(key);
		result.append("\":");
		result.append(value);
		result.append(",\n");
		return result.toString();
	}
	
	private static String buildJsonEntry(String key, Float value){
		StringBuilder result = new StringBuilder();
		result.append("\"");
		result.append(key);
		result.append("\":");
		result.append(value);
		result.append(",\n");
		return result.toString();
	}
	
	private static String buildJsonEntry(String key, Boolean value){
		StringBuilder result = new StringBuilder();
		result.append("\"");
		result.append(key);
		result.append("\":");
		result.append(value?"true":"false");
		result.append(",\n");
		return result.toString();
	}
	
	private static String simpleElementConversion(Object o){
		StringBuilder result = new StringBuilder();
		if(o instanceof Str){
			result.append(buildJsonEntry(((Str) o).getName(), ((Str) o).getVal()));
		} else if(o instanceof Int){
			result.append(buildJsonEntry(((Int) o).getName(), ((Int) o).getVal().intValue()));
		} else if(o instanceof Real){
			result.append(buildJsonEntry(((Real) o).getName(), ((Real) o).getVal().floatValue()));
		} else if(o instanceof Bool){
			result.append(buildJsonEntry(((Bool) o).getName(), ((Bool) o).getVal()));
		} else {
			result.append(buildJsonEntry(((Obj) o).getName(), ""));
		}
		return result.toString();
	}
	
	public static String obix2json(String obixStr){
		StringBuilder result = new StringBuilder();
		Obj obix = ObixDecoder.fromString(obixStr);
		
		result.append("{");
		for(Object o : obix.getObjGroup()){
			result.append(simpleElementConversion(o));
		}
		// Remove trailing comma
		result.deleteCharAt(result.length()-2);
		result.append("}");
		return result.toString();
	}
}
