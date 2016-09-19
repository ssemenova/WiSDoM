package edu.brandeis.rlearn;

import edu.brandeis.wisedb.WiSeDBCachedModel;

public class RecommendedSLA {
	private int deadline;
	private WiSeDBCachedModel model;
	private int cost;
	
	public RecommendedSLA(int deadline, WiSeDBCachedModel model, int cost) {
		this.deadline = deadline;
		this.model = model;
		this.cost = cost;
	}

	public int getDeadline() {
		return deadline;
	}

	public WiSeDBCachedModel getModel() {
		return model;
	}

	public int getCost() {
		return cost;
	}
	
	
}
