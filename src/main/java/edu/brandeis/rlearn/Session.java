package edu.brandeis.rlearn;

import edu.brandeis.wisedb.*;
import edu.brandeis.wisedb.aws.VMType;
import edu.brandeis.wisedb.cost.sla.MaxLatencySLA;

import java.util.*;

/**
 * Created by seaurchi on 9/17/16.
 */
public class Session {
    private Hashtable<Integer, String> templates;
    private String learnType;
    private String SLAtype;
    private final int Rn = 10; // num tightens
    private final int Rincrement = 5000;
    private final int numQueries = 9;
    private final int numWorkloads = 250;
    private int startLatency;
    private final int penalty = 1; //penalty for initial SLA
    private final int numSLAToRecommend = 3; // num of SLAs to recommend


    public Session() {
        templates = new Hashtable<>();
    }

    public void addTemplates(Hashtable<Integer, String> templates) {
        this.templates = templates;
    }

    public Hashtable<Integer, String> getTemplates() {
        return templates;
    }

    public void setLearnType(String learnType) {
        this.learnType = learnType;
    }

    public Boolean isSLEARN() {
        return learnType.equals("S");
    }

    public void addSLA1(String type, String value) {
        SLAtype = type;
        startLatency = Integer.parseInt(value);
    }

    public List<Integer> recommendSLA(Map<Integer, Map<VMType, Integer>> latency, Map<VMType, Integer> forMachine) {
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

        return minimizeList(cost, numSLAToRecommend);
    }

    private List<Integer> minimizeList(List<Integer> cost, int numToRec) {
        int[] minPairIndex = new int[2];
        while (cost.size() > 3) {
            minPairIndex = findMin(cost);
            cost.remove(minPairIndex[1]); //remove the first one
        }
        return cost;
    }

    private int[] findMin(List<Integer> cost) {
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
