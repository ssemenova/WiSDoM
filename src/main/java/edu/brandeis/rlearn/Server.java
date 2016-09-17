package edu.brandeis.rlearn;

import java.util.*;

import static spark.Spark.*;

import edu.brandeis.wisedb.*;
import edu.brandeis.wisedb.aws.VMType;
import edu.brandeis.wisedb.cost.sla.MaxLatencySLA;
import spark.template.velocity.*;
import spark.ModelAndView;

public class Server {
	public static void main(String[] args) {
//		Session session = new Session();
		webSocket("/bandit", BanditWebSocket.class);
		staticFiles.location("/assets");

		get("/", (req, res) -> renderFirstPage(req));

		post("/sendInitialDataS", (req, res) -> sendInitialDataS(req));
		post("/sendInitialDataR", (req, res) -> sendInitialDataR(req));
		post("/sendNumQueries", (req, res) -> sendNumQueries(req)); //for slearn
		init();
	}

	private static String renderTemplate(Map model) {
		return new VelocityTemplateEngine().render(new ModelAndView(model, "index.vm"));
	}

	public static String renderFirstPage(spark.Request req) {
		HashMap model = new HashMap();
		model.put("initial-Data-Form", "initialForm.vm");

		Hashtable<Integer, String> templates = defineTemplates();
		Map<Integer, Map<VMType, Integer>> latency = defineLatencies();

		model.put("templates", templates);
		model.put("latency", latency);
		return renderTemplate(model);
	}

	public static Hashtable<Integer, String> defineTemplates() {
		Hashtable<Integer, String> templates = new Hashtable<>();
		templates.put(1, "SQL QUERY 1");
		templates.put(2, "SQL QUERY 2");
		templates.put(3, "SQL QUERY 3");

		return templates;
	}

	public static Map<Integer, Map<VMType, Integer>> defineLatencies() {
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

		return latency;
	}

	public static String sendInitialDataS(spark.Request req) {
		HashMap model = new HashMap();

		model.put("templates", templatesChosen(req.queryParams()));
		model.put("initial-Data-Form", "initialFormFilled.vm");
		model.put("step-Two", "data-s-display.vm");

		return renderTemplate(model);
	}

	public static List<String> templatesChosen(Set<String> params) {
		List<String> templatesChosen = new LinkedList<>();
		for (String param : params) {
			if (param.contains("template")) {
				templatesChosen.add(param.substring(8));
			}
		}

		return templatesChosen;
	}

	public static String sendInitialDataR(spark.Request req) {
		HashMap model = new HashMap();

		model.put("templates", templatesChosen(req.queryParams()));
		model.put("initial-Data-Form", "initialFormFilled.vm");
		model.put("step-Two", "data-r-display.vm");

		return renderTemplate(model);
	}

	public static ModelAndView sendNumQueries(spark.Request req) {
		HashMap model = new HashMap();
		return new ModelAndView(model, "index.vm");
	}

//	public static void recommendSLA(Map<VMType, Integer> forMachine) {
//		Map<Integer, Map<VMType, Integer>> ios = new HashMap<>();
//		forMachine = new HashMap<>();
//		forMachine.put(VMType.T2_SMALL, 10);
//		ios.put(1, forMachine);
//
//		forMachine = new HashMap<>();
//		forMachine.put(VMType.T2_SMALL, 10);
//		ios.put(2, forMachine);
//
//		forMachine = new HashMap<>();
//		forMachine.put(VMType.T2_SMALL, 10);
//		ios.put(3, forMachine);
//
//		int loosestLatency = startLatency + ((Rincrement * Rn)/2);
//
//		WorkloadSpecification wf = new WorkloadSpecification(
//				latency,
//				ios,
//				new VMType[] { VMType.T2_SMALL },
//				new MaxLatencySLA(loosestLatency, penalty));
//
//		List<WiSeDBCachedModel> models = AdaptiveModelingUtils.tightenAndRetrain(wf, Rincrement, Rn, numQueries, numWorkloads);
//
//		Map<Integer, Integer> freqs = new HashMap<>();
//		freqs.put(1, 100);
//		freqs.put(2, 100);
//		freqs.put(3, 100);
//
//		List<Integer> cost = new LinkedList<Integer>();
//		for (WiSeDBCachedModel model : models) {
//			cost.add(CostUtils.getCostForPlan(model.getWorkloadSpecification(),
//					WiSeDBUtils.doPlacement(model, freqs)));
//		}
//
//		List<Integer> minimizedList = minimizeList(cost, numSLAToRecommend);
//
//		Map<String, Object> model = new HashMap<>();
//		model.put("SLARecs", minimizedList);
//	}
}
