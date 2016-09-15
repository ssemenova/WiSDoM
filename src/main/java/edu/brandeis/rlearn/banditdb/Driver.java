package edu.brandeis.rlearn.banditdb;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Driver {

	public static void main(String[] args) {
		Set<VM> vms = new HashSet<>();

		GreedySearch gs = new GreedySearch(
				new int[] {3, 5},
				new double[] {1.0, 1.3}
				);

		Random r = new Random();
		int numQueries = 100;

		long totalCost = 0;
		int arrivalRate = 400;
		double p = 0.01;
		for (int i = 0; i < numQueries * arrivalRate; i++) {
			p = p * 1.00299;
			p = Math.min(0.98, p);
			for (VM vm : vms) {
				totalCost += vm.tick(i);
			}
			
			if (i % arrivalRate == 0) {
				Query q = new Query(r.nextInt(700) + 300, 1000);
				VM v = gs.getVMForQuery(vms, p, q);
				if (!vms.contains(v)) {
					vms.add(v);
				}
				v.processQuery(q);
			}
			
			if (i % arrivalRate == 0) {
				if (i == 0) continue;
				System.out.println((i / 100) + ", " + totalCost / (i / arrivalRate) + ", " + p);
			}
		}

		System.out.println("Total cost: " + totalCost);

	}

}
