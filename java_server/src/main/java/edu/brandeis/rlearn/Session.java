package edu.brandeis.rlearn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import edu.brandeis.wisedb.AdaptiveModelingUtils;
import edu.brandeis.wisedb.AdvisorAction;
import edu.brandeis.wisedb.CostUtils;
import edu.brandeis.wisedb.WiSeDBCachedModel;
import edu.brandeis.wisedb.WiSeDBUtils;
import edu.brandeis.wisedb.WorkloadSpecification;
import edu.brandeis.wisedb.aws.VMType;
import edu.brandeis.wisedb.cost.sla.MaxLatencySLA;
import edu.brandeis.wisedb.scheduler.FirstFitDecreasingGraphSearch;
import edu.brandeis.wisedb.scheduler.GraphSearcher;
import edu.brandeis.wisedb.scheduler.PackNGraphSearch;


/**
 * Created by seaurchi on 9/17/16.
 */
public class Session {
    private Set<Integer> templates;
    private Map<Integer, Integer> queryFreqs;
    private List<RecommendedSLA> recommendations;
    private RecommendedSLA originalSLA;
    private String learnType;
    private String SLAtype;
    private int slaIdx;
    Map<Integer, Map<VMType, Integer>> ios;
    Map<Integer, Map<VMType, Integer>> latency;


    private int startLatency;
    private final int penalty = 1; //penalty for initial SLA
    private final int numSLAToRecommend = 3; // num of SLAs to recommend
    private Map<RecommendedSLA, JsonObject> recHeuristicCost = new HashMap<>();

    public static final Map<Integer, Integer> templateToLatency = new HashMap<>();

    
    static {
    	templateToLatency.put(1, 2000);
    	templateToLatency.put(2, 3000);
    	templateToLatency.put(3, 4000);
    	templateToLatency.put(4, 5200);
    	templateToLatency.put(5, 8000);
    	WiSeDBUtils.setThreadCountForTraining(8);

    }

    public Session() {
        templates = new HashSet<>();
    }

    public void setTemplates(Set<Integer> templates) {
        this.templates = templates;
    }

    public Set<Integer> getTemplates() {
        return templates;
    }

    public void setLearnType(String learnType) {
        this.learnType = learnType;
    }

    public Boolean isSLEARN() {
        return learnType.equals("S");
    }

    public void addSLA1(String type, int value) {
        SLAtype = type;
        startLatency = value;
    }

    public void recommendSLA() {
        ios = new HashMap<>();
        latency = new HashMap<>();
        
        for (Integer selectedTemplate : templates) {
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

       WorkloadSpecification wf = new WorkloadSpecification(
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

        
        // recost the ones we actually picked with GPLSOL
        recommendations.forEach(r -> r.recost(queryFreqs));
        getOriginalSLA().recost(queryFreqs);
        
//        for (RecommendedSLA recommendation : recommendations) {
//            recHeuristicCost.put(recommendation, generateHeuristicCharts(recommendation));
//        }
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
    	// TODO this is a race
    	WiSeDBUtils.GLPSOL_ENABLED = true;
    	List<AdvisorAction> toR = WiSeDBUtils.doPlacement(getSelectedSLA().getModel(), queryFreqs);
    	WiSeDBUtils.GLPSOL_ENABLED = false;
    	return toR;
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

	public JsonObject generateHeuristicCharts() {
		RecommendedSLA SLA = this.getSelectedSLA();
	    if (recHeuristicCost.containsKey(SLA)) {
	        return recHeuristicCost.get(SLA);
        } else {  
            System.out.println("Created sample workload");

            int ffd, ffi, pack9;
            
            WorkloadSpecification wf = this.getSelectedSLA().getModel().getWorkloadSpecification();
            
            GraphSearcher ffdSearch = new FirstFitDecreasingGraphSearch(wf.getSLA(), wf.getQueryTimePredictor(), false);
            GraphSearcher ffiSearch = new FirstFitDecreasingGraphSearch(wf.getSLA(), wf.getQueryTimePredictor(), true);
            GraphSearcher pack9Search = new PackNGraphSearch(9, wf.getQueryTimePredictor(), wf.getSLA());

            ffd = CostUtils.getCostForSearcher(ffdSearch, wf, queryFreqs);
            ffi = CostUtils.getCostForSearcher(ffiSearch, wf, queryFreqs);
            pack9 = CostUtils.getCostForSearcher(pack9Search, wf, queryFreqs);

            JsonObject toR = Json.object();
            toR.add("ffd", ffd/10.0);
            toR.add("ffi", ffi/10.0);
            toR.add("pack9", pack9/10.0);
            toR.add("wisedb", (int)(CostUtils.getCostForPlan(getSelectedSLA().getModel().getWorkloadSpecification(), doPlacementWithSelected())/10.0));
            recHeuristicCost.put(SLA, toR);
            
            return toR;
        }

    }

	public JsonObject getCloudCost() {
		JsonObject toR = Json.object();
		int wiseCost = (int)(CostUtils.getCostForPlan(getSelectedSLA().getModel().getWorkloadSpecification(), doPlacementWithSelected())/10.0);
		double cloudCost = ((new Random()).nextDouble() - 0.5) + wiseCost;
		toR.add("actualCost", cloudCost);
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return toR;
	}


}
