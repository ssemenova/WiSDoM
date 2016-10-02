package edu.brandeis.rlearn;

import java.util.LinkedList;
import java.util.List;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

public class VMModel {
	private List<Integer> queries;
	
	public VMModel() {
		queries = new LinkedList<Integer>();
	}
	
	public void addQuery(Integer i) {
		queries.add(i);
	}
	
	public List<Integer> getQueries() {
		return queries;
	}
	
	public JsonObject toJSON() {
		JsonObject toR = Json.object();
		
		JsonArray q = Json.array().asArray();
		queries.forEach(q::add);
		
		toR.add("queries", q);
		return toR;
	}
}
