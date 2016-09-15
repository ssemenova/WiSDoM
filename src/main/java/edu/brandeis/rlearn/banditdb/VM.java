package edu.brandeis.rlearn.banditdb;
import java.util.LinkedList;
import java.util.List;

public class VM {
	private static final int IDLE_TIME_BEFORE_SHUTDOWN = 1000;
	private static final int PROVISION_TIME = 1000;
	
	private double performanceMult;
	private List<Query> queue;
	
	private int provisionTimeLeft = PROVISION_TIME;
	private int idleTime = 0;
	private int costPerTick;
	private VMEventListener l;

	public VM(double perf, int costPerTick) {
		this.performanceMult = perf;
		queue = new LinkedList<Query>();
		this.costPerTick = costPerTick;
	}
	
	public void setListener(VMEventListener l) {
		this.l = l;
	}

	public int getCostPerTick() {
		return costPerTick;
	}
	
	public int tick(long currTick) {
		if (provisionTimeLeft > 0 && queue.size() == 0)
			// we are off.
			return 0;
		
		// progress the VM by 1/100th of a second
		if (provisionTimeLeft > 0) {
			// we still need to spend time provisioning the VM
			Query current = queue.get(0);
			current.addToCost(costPerTick);
			provisionTimeLeft--;
			
			if (provisionTimeLeft == 0)
				l.vmReady(this.hashCode());
			
			return costPerTick;
		}

		// we are provisioned. process any query...
		if (queue.size() > 0) {
			Query current = queue.get(0);
			current.processFor(currTick, performanceMult);
			current.addToCost(costPerTick);
			if (current.isFinished()) {
				l.queryComplete(current.hashCode());
				queue.remove(0);
			}
			return costPerTick;
		}
		
		// no queries to process.
		idleTime++;
		if (idleTime >= IDLE_TIME_BEFORE_SHUTDOWN) {
			// shutdown this VM
			l.vmShutdown(this.hashCode());
			idleTime = 0;
			provisionTimeLeft = PROVISION_TIME;
			return costPerTick;
		}
		
		return costPerTick;
	}
	

	public void processQuery(Query q) {
		queue.add(q);
	}
	
	public int getCostToPlace(Query toTest) {
		double ticksBeforeDone = queue.stream().mapToDouble(q -> q.getWeight() / performanceMult).sum();
		ticksBeforeDone += toTest.getWeight() / performanceMult;
		ticksBeforeDone += provisionTimeLeft;
		int cost = (int)(ticksBeforeDone * costPerTick);
		cost += toTest.getPenalty(0, (int)ticksBeforeDone);
		return cost;
	}
	
	
	
	@Override
	public String toString() {
		return "[VM (" + queue.size() + ")" + "ptl: " + provisionTimeLeft + "]";
	}
	
	
}
