package edu.brandeis.rlearn;

import java.util.Map;

import edu.brandeis.wisedb.CostUtils;
import edu.brandeis.wisedb.WiSeDBCachedModel;
import edu.brandeis.wisedb.WiSeDBUtils;

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
	
	public int getDeadlineSeconds() {
		return getDeadline() / 1000;
	}
	
	public int getCostCents() {
		return getCost() / 10;
	}
	
	public void recost(Map<Integer, Integer> queryFreqs) {
		// TODO this is a race
		WiSeDBUtils.GLPSOL_ENABLED = true;
		cost = CostUtils.getCostForPlan(model.getWorkloadSpecification(),WiSeDBUtils.doPlacement(model, queryFreqs));
		WiSeDBUtils.GLPSOL_ENABLED = false;
	}
	
	
}
