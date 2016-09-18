package edu.brandeis.rlearn;

import java.util.*;

import static spark.Spark.*;

import edu.brandeis.wisedb.aws.VMType;
import spark.template.velocity.*;
import spark.ModelAndView;

public class Server {

	private static HashMap<String, Session> sessionMap = new HashMap<>();
	private static Hashtable<Integer, String> templates = new Hashtable<>();
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
		post("doSLEARN", (req, res) -> doSLEARN(req));
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
		return renderTemplate(model);
	}

	public static String sendInitialDataS(spark.Request req) {
		HashMap model = new HashMap();
		Session session = getSessionFromMap(req);
		Hashtable<Integer, String> templatesChosen = templatesChosen(req.queryParams());

		session.setLearnType("S");
		model.put("templates", templatesChosen);
		session.addTemplates(templatesChosen);
		model.put("next-Step", "chooseSLA.vm");

		return renderTemplate(model);
	}

	public static String sendInitialDataR(spark.Request req) {
		HashMap model = new HashMap();
		Session session = getSessionFromMap(req);
		Hashtable<Integer, String> templatesChosen = templatesChosen(req.queryParams());

		session.setLearnType("R");
		model.put("templates", templatesChosen(req.queryParams()));
		session.addTemplates(templatesChosen);
		model.put("next-Step", "chooseSLA.vm");

		return renderTemplate(model);
	}

	public static String sendSLA(spark.Request req) {
		HashMap model = new HashMap();
		Session session = getSessionFromMap(req);

		session.addSLA1(req.queryParams("type"), req.queryParams("value"));

		if (session.isSLEARN()) {
			model.put("next-Step", "chooseNumQueries.vm");
			model.put("templates", session.getTemplates());
		} else {
			model.put("next-Step", "doRLEARN.vm");
		}

		return renderTemplate(model);
	}

	public static String sendNumQueries(spark.Request req) {
		HashMap model = new HashMap();
		Session session = getSessionFromMap(req);

		if (session.isSLEARN()) {
			model.put("next-step", "chooseSLA2.vm");
			model.put("SLArecs", session.recommendSLA(latencies, forMachine));
		}
		return renderTemplate(model);
	}

	public static String sendSLA2(spark.Request req) {
		HashMap model = new HashMap();
		Session session = getSessionFromMap(req);

		if (session.isSLEARN()) {
			model.put("next-step", "chooseSLA2.vm");
			model.put("SLArecs", session.recommendSLA(latencies, forMachine));
		}
		return renderTemplate(model);
	}

	public static String doSLEARN(spark.Request req) {
		return "";
	}

	/* helpers */
	private static Session getSessionFromMap(spark.Request req) {
		return sessionMap.get(req.session().id());
	}

	private static String renderTemplate(Map model) {
		return new VelocityTemplateEngine().render(new ModelAndView(model, "index.vm"));
	}

	public static Hashtable<Integer, String> templatesChosen(Set<String> params) {
		Hashtable<Integer, String> templatesChosen = new Hashtable<>();
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
