package edu.brandeis.rlearn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import edu.brandeis.wisedb.scheduler.FirstFitDecreasingGraphSearch;
import edu.brandeis.wisedb.scheduler.GraphSearcher;
import edu.brandeis.wisedb.scheduler.PackNGraphSearch;
import edu.brandeis.wisedb.scheduler.training.CostModelUtil;


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
    private Map<RecommendedSLA, Map<String, Integer>> recHeuristicCost;

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

	public Map<String, Integer> generateHeuristicCharts(RecommendedSLA SLA) {
	    if (recHeuristicCost.containsKey(SLA)) {
	        return recHeuristicCost.get(SLA);
        } else {
            HashMap<String, Integer> costs = new HashMap<>();

            Set<ModelQuery> workload = new HashSet<>();
            // difference from paper: use 2000 instead of 5000 queries for faster schedModelQuery        
            for (Entry<Integer, Integer> e : queryFreqs.entrySet()) {
            	for (int i = 0; i < e.getValue(); i++)
            		workload.add(new ModelQuery(e.getKey()));
            }
            

            System.out.println("Created sample workload");

            int ffd, ffi, pack9;

            GraphSearcher ffdSearch = new FirstFitDecreasingGraphSearch(wf.getSLA(), wf.getQueryTimePredictor());
            GraphSearcher ffiSearch = new FirstFitDecreasingGraphSearch(wf.getSLA(), wf.getQueryTimePredictor(), true);
            GraphSearcher pack9search = new PackNGraphSearch(9, wf.getQueryTimePredictor(), wf.getSLA());

            ffd = CostModelUtil.getCostForPlan(ffdSearch.schedule(workload), wf.getSLA());
            ffi = CostModelUtil.getCostForPlan(ffiSearch.schedule(workload), wf.getSLA());
            pack9 = CostModelUtil.getCostForPlan(pack9search.schedule(workload), wf.getSLA());

            Map<String, Integer> hCosts = new HashMap<>();
            hCosts.put("ffd", ffd);
            hCosts.put("ffi", ffi);
            hCosts.put("pack9", pack9);
            hCosts.put("wisedb", (int)(CostUtils.getCostForPlan(getSelectedSLA().getModel().getWorkloadSpecification(), doPlacementWithSelected())/10.0));
            recHeuristicCost.put(SLA, hCosts);
            
            return costs;
        }

    }


}
