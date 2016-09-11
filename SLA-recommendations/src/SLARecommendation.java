import edu.brandeis.wisedb.AdaptiveModelingUtils;
import edu.brandeis.wisedb.AdvisorAction;
import edu.brandeis.wisedb.WiSeDBUtils;
import edu.brandeis.wisedb.WorkloadSpecification;
import edu.brandeis.wisedb.aws.VMType;
import edu.brandeis.wisedb.cost.sla.MaxLatencySLA;
import edu.brandeis.wisedb.scheduler.Action;
import org.apache.commons.math3.analysis.function.Max;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class SLARecommendation {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // some constants
        int Rn = 5; // amount of SLAs to recommend
        int Rincrement = 5000;
        int workloadSize = 10;
        int numWorkloads = 50;
        int startLatency = 60000 + 91000; //latency for initial SLA
        int startPenalty = 1; //penalty for initial SLA

        //*INITIAL SETUP OF VARIABLES/ENVIRONMENT*

        // first, we build a map that tells us how long each of our
        // query types takes to process on different VMs.
        // here, we create type 1 which takes 20 seconds, type 2
        // which takes 30 seconds, and type 3 that takes 40 seconds on a
        // t2.small machine.
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

        // here, we specify the IOs used by each task
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

//        WorkloadSpecification wf = new WorkloadSpecification(
//                latency,
//                ios,
//                new VMType[] { VMType.T2_SMALL },
//                new MaxLatencySLA(startLatency, startPenalty));
//
//        String training = WiSeDBUtils.constructTrainingData(wf, workloadSize, numWorkloads).get();
//
//        Map<Integer, Integer> queryFreqs = new HashMap<>();
//        queryFreqs.put(1, 2);
//        queryFreqs.put(2, 2);
//        queryFreqs.put(3, 2);
//
//        ByteArrayInputStream bis = new ByteArrayInputStream(training.getBytes());
//        List<AdvisorAction> a = WiSeDBUtils.doPlacement(bis, wf, queryFreqs);


        // *STRATEGY RECOMMENDATION*
        //create sequence of performance goals centered around the initial performance goal
        int loosestLatency = startLatency - (Rincrement * Rn);

        WorkloadSpecification loosest = new WorkloadSpecification(
                latency,
                ios,
                new VMType[] { VMType.T2_SMALL },
                new MaxLatencySLA(loosestLatency, startPenalty));

        AdaptiveModelingUtils adaptive = new AdaptiveModelingUtils();
        List<String> models = adaptive.tightenAndRetrain(loosest, Rincrement, Rn, workloadSize, numWorkloads);

        List<Integer> cost = new LinkedList<Integer>();
        for (String trainingData : models) {
            cost.add(calculateCost());
        }

        System.out.println(models);

    }

    public static int calculateCost() {
        Map<Integer, Integer> queryFreqs = new HashMap<>();
        queryFreqs.put(1, 2);
        queryFreqs.put(2, 2);
        queryFreqs.put(3, 2);

        ByteArrayInputStream bis = new ByteArrayInputStream(training.getBytes());
        List<AdvisorAction> a = WiSeDBUtils.doPlacement(bis, wf, queryFreqs);

        ByteArrayInputStream bis = new ByteArrayInputStream(training.getBytes());
        List<AdvisorAction> a = WiSeDBUtils.doPlacement(bis, wf, queryFreqs);


        return cost;
    }

    public static void smallestDistance() {
        return;
    }

}
