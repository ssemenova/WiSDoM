package edu.brandeis.rlearn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import com.eclipsesource.json.Json;

import edu.brandeis.wisedb.rlearn.BanditDBSimulator;
import edu.brandeis.wisedb.rlearn.BanditDBSimulatorListener;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;

public class BanditWebSocket implements WebSocketListener, BanditDBSimulatorListener {

	private BanditDBSimulator sim;
	private Session s;
	
	@Override
	public void onWebSocketClose(int arg0, String arg1) {
		sim.stop();
	}

	@Override
	public void onWebSocketConnect(Session arg0) {
		s = arg0;
	}

	@Override
	public void onWebSocketError(Throwable arg0) {
		sim.stop();
		
	}

	@Override
	public void onWebSocketBinary(byte[] arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWebSocketText(String arg0) {
		try {
			System.out.println(arg0);
			JSONObject json = new JSONObject(arg0);
			if (json.getString("type").equals("setUp")) {
				JSONArray templateIDs = json.getJSONArray("templates");
				List<Integer> templateIDList = new ArrayList<>();
				for (int i = 0; i < templateIDs.length(); i++) {
					templateIDList.add(templateIDs.getInt(i));
				}

				Map<Integer, Integer> templateToLatency = edu.brandeis.rlearn.Session.templateToLatency;
				List<Integer> templateLatencyList = new ArrayList<>();
				for (int ID : templateIDList) {
					templateLatencyList.add(templateToLatency.get(ID));
				}

				int deadline = json.getInt("deadline");

				sim = new BanditDBSimulator(200,
						templateLatencyList.stream().mapToInt(i -> i).toArray(),
						templateIDList.stream().mapToInt(i -> i).toArray(),
						2000,
						300,
						deadline);
				sim.addListener(this);
				sim.start();
			} else if (json.getString("type").equals("pause")) {
				sim.pause();
			} else if (json.getString("type").equals("play")) {
				sim.resume();
			}

		} catch (JSONException e) {
			//TODO: fuck java
			e.printStackTrace();
		}
		
	}
	
	
	@Override
	public void queryAssigned(int qID, int vmID, int queryTemplate) {
		try {
			s.getRemote().sendString(Json.object()
					.add("type", "assign")
					.add("queryID", qID)
					.add("vmID", vmID)
					.add("template", queryTemplate)
					.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void queryComplete(int qID) {
		try {
			s.getRemote().sendString(Json.object()
					.add("type", "complete")
					.add("queryID", qID)
					.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	@Override
	public void vmProvisioned(int vmID, int vmType) {
		try {
			s.getRemote().sendString(Json.object()
					.add("type", "provision")
					.add("vmID", vmID)
					.add("vmType", vmType)
					.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void vmReady(int vmID) {
		try {
			s.getRemote().sendString(Json.object()
					.add("type", "ready")
					.add("vmID", vmID)
					.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	@Override
	public void vmShutdown(int vmID) {
		try {
			s.getRemote().sendString(Json.object()
					.add("type", "shutdown")
					.add("vmID", vmID)
					.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	@Override
	public void costPerQuery(long cost, long currTick) {
		try {
			s.getRemote().sendString(Json.object()
					.add("type", "cost")
					.add("cost", cost)
					.add("tick", currTick)
					.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
