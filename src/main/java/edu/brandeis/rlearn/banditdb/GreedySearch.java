package edu.brandeis.rlearn.banditdb;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

public class GreedySearch {
	private int[] newVMCosts;
	private double[] perfMults;
	private Random r;

	public GreedySearch(int[] newVMCosts, double[] perfMults) {
		this.newVMCosts = newVMCosts;
		this.perfMults = perfMults;
		r = new Random(42);
	}

	public VM getVMForQuery(Set<VM> current, double p, Query q) {
		PriorityQueue<VM> vms = new PriorityQueue<VM>((a, b) -> {
			return a.getCostToPlace(q) - b.getCostToPlace(q);
		});

		vms.addAll(current);

		if (vms.size() < 25) {
			for (int i = 0; i < newVMCosts.length; i++) {
				vms.add(new VM(perfMults[i], newVMCosts[i]));
			}
		}

		while (vms.size() > 1) {
			VM vm = vms.poll();
			if (r.nextDouble() < p)
				return vm;
		}

		return vms.poll();

	}
}
