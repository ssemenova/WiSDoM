package edu.brandeis.rlearn;

import java.util.LinkedList;
import java.util.List;

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
}
