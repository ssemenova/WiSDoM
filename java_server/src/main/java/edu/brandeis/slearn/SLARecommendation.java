package edu.brandeis.slearn;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import edu.brandeis.wisedb.AdaptiveModelingUtils;
import edu.brandeis.wisedb.CostUtils;
import edu.brandeis.wisedb.WiSeDBCachedModel;
import edu.brandeis.wisedb.WiSeDBUtils;
import edu.brandeis.wisedb.WorkloadSpecification;
import edu.brandeis.wisedb.aws.VMType;
import edu.brandeis.wisedb.cost.sla.MaxLatencySLA;

public class SLARecommendation {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
    	long now = System.currentTimeMillis();
		// some constants
        int Rn = 10; // num tightens
        int Rincrement = 5000;
        int numQueries = 9;
        int numWorkloads = 250;
        int startLatency = 60000 + 100000; //latency for initial SLA
        int penalty = 1; //penalty for initial SLA
		int numSLAToRecommend = 3; // num of SLAs to recommend

		WiSeDBUtils.setThreadCountForTraining(4);

		Map<Integer, Map<VMType, Integer>> latency = new HashMap<>();
		Map<VMType, Integer> forMachine = new HashMap<>();
		forMachine.put(VMType.T2_SMALL, 20000);
		latency.put(1, forMachine);

		forMachine = new HashMap<>();
		forMachine.put(VMType.T2_SMALL, 30000);
		latency.put(2, forMachine);

		forMachine = new HashMap<>();
		forMachine.put(VMType.T2_SMALL, 40000);
		latency.put(3, forMachine);


		Map<Integer, Map<VMType, Integer>> ios = new HashMap<>();
		forMachine = new HashMap<>();
		forMachine.put(VMType.T2_SMALL, 10);
		ios.put(1, forMachine);

		forMachine = new HashMap<>();
		forMachine.put(VMType.T2_SMALL, 10);
		ios.put(2, forMachine);

		forMachine = new HashMap<>();
		forMachine.put(VMType.T2_SMALL, 10);
		ios.put(3, forMachine);

		int loosestLatency = startLatency + ((Rincrement * Rn)/2);

		WorkloadSpecification wf = new WorkloadSpecification(
				latency,
				ios,
				new VMType[] { VMType.T2_SMALL },
				new MaxLatencySLA(loosestLatency, penalty));

		List<WiSeDBCachedModel> models = AdaptiveModelingUtils.tightenAndRetrain(wf, Rincrement, Rn, numQueries, numWorkloads);

		Map<Integer, Integer> freqs = new HashMap<>();
		freqs.put(1, 100);
		freqs.put(2, 100);
		freqs.put(3, 100);

		List<Integer> cost = new LinkedList<Integer>();
		for (WiSeDBCachedModel model : models) {
			cost.add(CostUtils.getCostForPlan(model.getWorkloadSpecification(),
					WiSeDBUtils.doPlacement(model, freqs)));
		}

		minimizeList(cost, numSLAToRecommend);

		System.out.println(System.currentTimeMillis() - now);
    }

    public static void minimizeList(List<Integer> cost, int numToRec) {
    	int[] minPairIndex = new int[2];
		while (cost.size() > 3) {
			minPairIndex = findMin(cost);
			cost.remove(minPairIndex[1]); //remove the first one
		}
		System.out.println(cost);
	}

	public static int[] findMin(List<Integer> cost) {
		int minDiff = Math.abs(cost.get(0) - cost.get(1));
		int index1 = 0;
		int index2 = 1;
		int curr = 0;

		for (int i = 1; i < cost.size()-1; i++) {
			curr = Math.abs(cost.get(i) - cost.get(i+1));
			if (curr < minDiff) {
				minDiff = curr;
				index1 = i;
				index2 = i + 1;
			}
		}
		return new int[]{index1, index2};
	}
}
