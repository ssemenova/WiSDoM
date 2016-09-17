package edu.brandeis.rlearn;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import com.eclipsesource.json.Json;

import edu.brandeis.wisedb.rlearn.BanditDBSimulator;
import edu.brandeis.wisedb.rlearn.BanditDBSimulatorListener;

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
		sim = new BanditDBSimulator(200, 700, 300);
		sim.addListener(this);
		sim.start();
		
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
		// TODO Auto-generated method stub
		
	}

	
	
	@Override
	public void queryAssigned(int qID, int vmID) {
		try {
			s.getRemote().sendString(Json.object()
					.add("type", "assign")
					.add("queryID", qID)
					.add("vmID", vmID)
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
