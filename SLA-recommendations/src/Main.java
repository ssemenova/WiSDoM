import edu.brandeis.wisedb.WiSeDBUtils;
import edu.brandeis.wisedb.WorkloadSpecification;
import edu.brandeis.wisedb.aws.VMType;
import edu.brandeis.wisedb.cost.sla.MaxLatencySLA;
import edu.brandeis.wisedb.scheduler.Action;

import java.io.ByteArrayInputStream;
import java.util.*;

public class Main {

    public static void main(String[] args) {
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

        // here, we create the workload specification,
        // which gives the latency data, the IO data,
        // the machine types we want to make available,
        // and the SLA.
        WorkloadSpecification wf = new WorkloadSpecification(
                latency,
                ios,
                new VMType[] { VMType.T2_SMALL },
                new MaxLatencySLA(60000 + 91000, 1));

        // here, we construct a training set of 5000 workloads of size
        // 10 in order to train our model.
        String training = WiSeDBUtils.constructTrainingData(wf, 5000, 10);

        // here we build our workload (two instances of each query type)
        Map<Integer, Integer> queryFreqs = new HashMap<>();
        queryFreqs.put(1, 2);
        queryFreqs.put(2, 2);
        queryFreqs.put(3, 2);

        // here we use WiSeDB to get our workload management strategy
        ByteArrayInputStream bis = new ByteArrayInputStream(training.getBytes());
        List<Action> a = WiSeDBUtils.doPlacement(bis, wf, queryFreqs);

        System.out.println(a);

    }
}
