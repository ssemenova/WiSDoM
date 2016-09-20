package edu.brandeis.rlearn;

import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.init;
import static spark.Spark.post;
import static spark.Spark.staticFiles;
import static spark.Spark.webSocket;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.brandeis.wisedb.aws.VMType;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

public class Server {

	private static HashMap<String, Session> sessionMap = new HashMap<>();
	private static HashMap<Integer, String> templates = new HashMap<>();
	private static Map<Integer, Map<VMType, Integer>> latencies = new HashMap<>();
	private static Map<VMType, Integer> forMachine = new HashMap<>();

	public static void main(String[] args) {
		webSocket("/bandit", BanditWebSocket.class);
		staticFiles.location("/assets");

		defineDefaults();

		get("/", (req, res) -> renderFirstPage(req));

		//data input workflow:
		post("/sendInitialDataS", (req, res) -> sendInitialDataS(req));
		post("/sendInitialDataR", (req, res) -> sendInitialDataR(req));
		post("/sendSLA", (req, res) -> sendSLA(req));
		post("/sendNumQueries", (req, res) -> sendNumQueries(req)); //for slearn
		post("/sendSLA2", (req, res) -> sendSLA2(req));
		
		exception(Exception.class, (e, req, res) -> {
			e.printStackTrace();
		});
		
		init();
	}

	/* urls */
	public static String renderFirstPage(spark.Request req) {
		Session session = new Session();
		sessionMap.put(req.session().id(), session);

		HashMap model = new HashMap();
		model.put("next-Step", "initialForm.vm");

		model.put("templates", templates);
		model.put("latencies", latencies);
		return renderTemplate(model, "index.vm");
	}

	public static String sendInitialDataS(spark.Request req) {
		HashMap model = new HashMap();
		Session session = getSessionFromMap(req);
		HashMap<Integer, String> templatesChosen = templatesChosen(req.queryParams());

		session.setLearnType("S");
		model.put("templates", templatesChosen);
		session.setTemplates(templatesChosen);
		model.put("next-Step", "chooseSLA.vm");

		return renderTemplate(model, "chooseSLA.vm");
	}

	public static String sendInitialDataR(spark.Request req) {
		HashMap model = new HashMap();
		Session session = getSessionFromMap(req);
		HashMap<Integer, String> templatesChosen = templatesChosen(req.queryParams());

		session.setLearnType("R");
		model.put("templates", templatesChosen(req.queryParams()));
		session.setTemplates(templatesChosen);
		model.put("next-Step", "chooseSLA.vm");

		return renderTemplate(model, "chooseSLA.vm");
	}

	public static String sendSLA(spark.Request req) {
		HashMap model = new HashMap();
		Session session = getSessionFromMap(req);

		session.addSLA1(req.queryParams("type"), req.queryParams("value"));

		if (session.isSLEARN()) {
			model.put("next-Step", "chooseNumQueries.vm");
			model.put("templates", session.getTemplates());
			return renderTemplate(model, "chooseNumQueries.vm");
		} else {
			model.put("next-Step", "doRLEARN.vm");
			return renderTemplate(model, "doRLEARN.vm");
		}
	}

	public static String sendNumQueries(spark.Request req) {
		Map<String, Object> model = new HashMap<>();
		Session session = getSessionFromMap(req);
		Map<Integer, Integer> queryFreqs = new HashMap<>();
		
		
		for (String s : req.queryParams()) {
			if (s.startsWith("templatecount-")) {
				int tID  = Integer.valueOf(s.substring("templatecount-".length()));
				int freq = Integer.valueOf(req.queryParams(s));
				queryFreqs.put(tID, freq);
			}
		}
		
		// TODO: make sure they entered at least some queries of each template?
		session.setQueryFreqs(queryFreqs);
		session.recommendSLA();
		if (session.isSLEARN()) {
			model.put("next-step", "chooseSLA2.vm");
			model.put("SLARecs", session.getRecommendations());
		}
		
		return renderTemplate(model, "chooseSLA2.vm");
	}

	public static String sendSLA2(spark.Request req) {
		Map<String, Object> model = new HashMap<String, Object>();
		Session session = getSessionFromMap(req);
		
		if (session.isSLEARN()) {
			model.put("next-step", "doSLEARN.vm");
			session.setSLAIndex(Integer.valueOf(req.queryParams("slaIdx")));
		}
		model.put("actions", session.doPlacementWithSelected()
				.stream()
				.map(a -> a.toString())
				.collect(Collectors.toList()));
		return renderTemplate(model, "doSLEARN.vm");
	}

	/* helpers */
	private static Session getSessionFromMap(spark.Request req) {
		return sessionMap.get(req.session().id());
	}

	private static String renderTemplate(Object model, String template) {
		return new VelocityTemplateEngine().render(new ModelAndView(model, template));
	}

	public static HashMap<Integer, String> templatesChosen(Set<String> params) {
		HashMap<Integer, String> templatesChosen = new HashMap<>();
		for (String param : params) {
			if (param.contains("template")) {
				templatesChosen.put(Integer.valueOf(param.substring(8)), templates.get(Integer.valueOf(param.substring(8))));
			}
		}

		return templatesChosen;
	}

	/* wise-specific stuff */
	public static void defineDefaults() {
		forMachine.put(VMType.T2_SMALL, 20000);
		latencies.put(1, forMachine);

		forMachine = new HashMap<>();
		forMachine.put(VMType.T2_SMALL, 30000);
		latencies.put(2, forMachine);

		forMachine = new HashMap<>();
		forMachine.put(VMType.T2_SMALL, 40000);
		latencies.put(3, forMachine);

		templates.put(1, "SQL QUERY 1");
		templates.put(2, "SQL QUERY 2");
		templates.put(3, "SQL QUERY 3");

	}

}
