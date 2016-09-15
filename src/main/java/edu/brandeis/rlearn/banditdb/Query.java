package edu.brandeis.rlearn.banditdb;
public class Query {	
	private long startTick = -1;
	private long stopTick = -1;

	private double duration;
	private int cost;
	private int deadline;

	public Query(double duration, int deadline) {
		this.duration = duration;
		this.deadline = deadline;
	}

	public double getWeight() {
		return duration;
	}

	public void start(int tick) {
		startTick = tick;
	}

	public void processFor(long currTick, double progress) {
		duration -= progress;

		if (isFinished()) {
			stopTick = currTick;
			addToCost(getPenalty(startTick, stopTick));
		}
	}

	public int getPenalty(long start, long stop) {
		// calculate the penalty
		int penalty = (int)((start - stop) - (long)deadline);
		if (penalty > 0) {
			return penalty;
		}
		return 0;
	}

	public boolean isFinished() {
		return duration <= 0.0;
	}

	public void addToCost(int amount) {
		cost += amount;
	}

	public int getCost() {
		return cost;
	}

}
