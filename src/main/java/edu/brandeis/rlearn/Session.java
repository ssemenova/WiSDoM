package edu.brandeis.rlearn;

import java.util.*;
import java.util.stream.Collectors;

import edu.brandeis.wisedb.AdaptiveModelingUtils;
import edu.brandeis.wisedb.AdvisorAction;
import edu.brandeis.wisedb.CostUtils;
import edu.brandeis.wisedb.WiSeDBCachedModel;
import edu.brandeis.wisedb.WiSeDBUtils;
import edu.brandeis.wisedb.WorkloadSpecification;
import edu.brandeis.wisedb.aws.VMType;
import edu.brandeis.wisedb.cost.ModelQuery;
import edu.brandeis.wisedb.cost.sla.MaxLatencySLA;
import edu.brandeis.wisedb.cost.ModelSLA;
import edu.brandeis.wisedb.cost.sla.AverageLatencyModelSLA;
import edu.brandeis.wisedb.cost.sla.MaxLatencySLA;
import edu.brandeis.wisedb.cost.sla.PerQuerySLA;
import edu.brandeis.wisedb.cost.sla.PercentSLA;
import edu.brandeis.wisedb.scheduler.FirstFitDecreasingGraphSearch;
import edu.brandeis.wisedb.scheduler.GraphSearcher;
import edu.brandeis.wisedb.scheduler.PackNGraphSearch;
import edu.brandeis.wisedb.scheduler.training.CostModelUtil;
import edu.brandeis.wisedb.scheduler.training.ModelWorkloadGenerator;


/**
 * Created by seaurchi on 9/17/16.
 */
public class Session {
    private HashMap<Integer, String> templates;
    private Map<Integer, Integer> queryFreqs;
    private List<RecommendedSLA> recommendations;
    private RecommendedSLA originalSLA;
    private String learnType;
    private String SLAtype;
    private int slaIdx;
    WorkloadSpecification wf;
    Map<Integer, Map<VMType, Integer>> ios;
    Map<Integer, Map<VMType, Integer>> latency;


    private int startLatency;
    private final int penalty = 1; //penalty for initial SLA
    private final int numSLAToRecommend = 3; // num of SLAs to recommend
    private HashMap<RecommendedSLA, HashMap<String, Integer>> recHeuristicCost;

    private static final Map<Integer, Integer> templateToLatency = new HashMap<>();

    
    static {
    	templateToLatency.put(1, 2000);
    	templateToLatency.put(2, 3000);
    	templateToLatency.put(3, 4000);
    	templateToLatency.put(4, 5200);
    	templateToLatency.put(5, 8000);

    }

    public Session() {
        templates = new HashMap<>();
    }

    public void setTemplates(HashMap<Integer, String> templates) {
        this.templates = templates;
    }

    public HashMap<Integer, String> getTemplates() {
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

    public void recommendSLA() {
        ios = new HashMap<>();
        latency = new HashMap<>();
        
        for (Integer selectedTemplate : templates.keySet()) {
        	// say that every query takes 1 IO
        	Map<VMType, Integer> iosForThisQuery = new HashMap<>();
        	iosForThisQuery.put(VMType.T2_SMALL, 1);
        	ios.put(selectedTemplate, iosForThisQuery);
        	
        	Map<VMType, Integer> latencyForThisQuery = new HashMap<>();
        	latencyForThisQuery.put(VMType.T2_SMALL, templateToLatency.get(selectedTemplate));
        	latency.put(selectedTemplate, latencyForThisQuery);
        }
        
        // TODO: select these variables in a smarter way
        int loosestLatency = startLatency + 10000;
        int increment = 1000;
        int numSteps = 20;
        
        // TODO obvi do this with math instead of a loop
        while (loosestLatency - (increment*numSteps) < 60000 + 8000) {
        	numSteps--;
        }

        wf = new WorkloadSpecification(
                latency,
                ios,
                new VMType[] { VMType.T2_SMALL },
                new MaxLatencySLA(loosestLatency, penalty));

        List<WiSeDBCachedModel> models = AdaptiveModelingUtils.tightenAndRetrain(wf, increment, numSteps, 9, 200);
        List<Integer> cost = new LinkedList<Integer>();
        
        for (WiSeDBCachedModel model : models) {
            cost.add(CostUtils.getCostForPlan(model.getWorkloadSpecification(),
                    WiSeDBUtils.doPlacement(model, queryFreqs)));
        }
        
        recommendations = new LinkedList<>();
        for (int i = 0; i < cost.size(); i++) {
        	if (loosestLatency - (increment * i) == startLatency) {
        		originalSLA = new RecommendedSLA(startLatency, models.get(i), cost.get(i));
        	}
        	recommendations.add(new RecommendedSLA(loosestLatency - (increment * i), models.get(i), cost.get(i)));
        }
        recommendations = minimizeList(recommendations, numSLAToRecommend);
        for (RecommendedSLA recommendation : recommendations) {
            recHeuristicCost.put(recommendation, generateHeuristicCharts(recommendation));
        }
    }
    
    public RecommendedSLA getOriginalSLA() {
    	return originalSLA;
    }
    
    public List<RecommendedSLA> getRecommendations() {
    	return recommendations;
    }
    
    public void setSLAIndex(int idx) {
    	this.slaIdx = idx;
    }
    
    public List<AdvisorAction> doPlacementWithSelected() {
    	return WiSeDBUtils.doPlacement(getSelectedSLA().getModel(), queryFreqs);
    }
    
    public RecommendedSLA getSelectedSLA() {
    	if (this.slaIdx == -1)
    		return originalSLA;
    	return recommendations.get(slaIdx);
    }

    private List<RecommendedSLA> minimizeList(List<RecommendedSLA> cost, int numToRec) {
        int[] minPairIndex = new int[2];
        while (cost.size() > numToRec) {
            minPairIndex = findMin(cost.stream().map(r -> r.getCost()).collect(Collectors.toList()));
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

	public void setQueryFreqs(Map<Integer, Integer> queryFreqs) {
		this.queryFreqs = queryFreqs;
	}

	public HashMap<String, Integer> generateHeuristicCharts(RecommendedSLA SLA) {
	    if (recHeuristicCost.containsKey(SLA)) {
	        return recHeuristicCost.get(SLA);
        } else {
            HashMap<String, Integer> costs = new HashMap<>();

            VMType[] types = new VMType[]{VMType.T2_SMALL};

            if (SLAtype.equals("average")) {
                ModelSLA slas = new ModelSLA() {
                    new AverageLatencyModelSLA(startLatency, penalty)
                };
            } else if (SLAtype.equals("percentile")) {

            } else if (SLAtype.equals("max")) {

            }

            ModelSLA[] slas = new ModelSLA[]{
//                    new PerQuerySLA(latency, penalty),
                    new AverageLatencyModelSLA(startLatency, penalty),
                    new MaxLatencySLA(startLatency, penalty),
//                    PercentSLA.nintyTenSLA()
            };

            WorkloadSpecification[] ws = Arrays.stream(slas)
                    .map(sla -> new WorkloadSpecification(types, sla))
                    .toArray(i -> new WorkloadSpecification[i]);


            // difference from paper: use 2000 instead of 5000 queries for faster scheduling
            Set<ModelQuery> workload = ModelWorkloadGenerator.randomQueries(2000, 42, ws[0].getQueryTimePredictor().QUERY_TYPES);

            System.out.println("Created sample workload");

            // get all costs
            int[] ffd = new int[slas.length];
            int[] ffi = new int[slas.length];
            int[] pack9 = new int[slas.length];
            int[] dt = new int[slas.length];

            for (int i = 0; i < slas.length; i++) {
                GraphSearcher ffdSearch = new FirstFitDecreasingGraphSearch(ws[i].getSLA(), ws[i].getQueryTimePredictor());
                GraphSearcher ffiSearch = new FirstFitDecreasingGraphSearch(ws[i].getSLA(), ws[i].getQueryTimePredictor(), true);
                GraphSearcher pack9search = new PackNGraphSearch(9, ws[i].getQueryTimePredictor(), ws[i].getSLA());

                ffd[i] = CostModelUtil.getCostForPlan(ffdSearch.schedule(workload), ws[i].getSLA());
                ffi[i] = CostModelUtil.getCostForPlan(ffiSearch.schedule(workload), ws[i].getSLA());
                pack9[i] = CostModelUtil.getCostForPlan(pack9search.schedule(workload), ws[i].getSLA());
            }
            recHeuristicCost.put(SLA, costs);
            return costs;
        }

    }


}
