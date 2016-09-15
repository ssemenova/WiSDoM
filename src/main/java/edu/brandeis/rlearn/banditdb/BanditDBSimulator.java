package edu.brandeis.rlearn.banditdb;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BanditDBSimulator implements VMEventListener {
	private static final int ARRIVAL_RATE = 300;
	private static final double[] VM_PERF_MULTS = {1.0, 1.3};
	private static final int[] VM_COSTS = {3, 5};
	private static final int LATENCY_RAND = 700;
	private static final int LATENCY_SHIFT = 300;
	
	private GreedySearch gs;
	private Set<VM> vms;
	private double p = 0.1;
	private Random r = new Random();
	private long currTick = 0;
	private long cost = 0;
	private long queriesComplete = 0;
	private boolean alive = false;
	
	private Set<BanditDBSimulatorListener> listeners;
	
	public BanditDBSimulator() {
		gs = new GreedySearch(VM_COSTS, VM_PERF_MULTS);
		vms = new HashSet<VM>();
		listeners = new HashSet<>();
	}
	
	public void start() {
		alive = true;
		Runnable r = new Runnable() {

			@Override
			public void run() {
				while (alive) {
					tick();
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {}
				}
			}
		};
		
		Thread t = new Thread(r);
		t.start();
	}
	
	public void stop() {
		alive = false;
	}
	
	public void addQuery() {
		final Query q = new Query(r.nextInt(LATENCY_RAND) + LATENCY_SHIFT, 1000);
		final VM v = gs.getVMForQuery(vms, p, q);
		if (!vms.contains(v)) {
			vms.add(v);
			v.setListener(this);
			listeners.forEach(l -> l.vmProvisioned(v.hashCode(), v.getCostPerTick()));
		}
		v.processQuery(q);
		
		listeners.forEach(l -> l.queryAssigned(q.hashCode(), v.hashCode()));
	}
	
	public void tick() {
		currTick++;
		if (currTick % ARRIVAL_RATE == 0) {
			addQuery();
		}
		
		for (VM vm : vms) {
			cost += vm.tick(currTick);
		}
		
		if (currTick % 500 == 0 && queriesComplete != 0)
			listeners.forEach(l -> l.costPerQuery(cost / queriesComplete, currTick));
	}
	
	public void addListener(BanditDBSimulatorListener bdsl) {
		listeners.add(bdsl);
	}
	
	public void removeListener(BanditDBSimulator toRemove) {
		listeners.remove(toRemove);
	}

	@Override
	public void queryComplete(int qID) {
		listeners.forEach(l -> l.queryComplete(qID));
		queriesComplete++;
//		if (queriesComplete >= 20) {
//			cost = 0;
//			queriesComplete = 0;
//		}
		
		p = p * 1.07905;
		p = Math.min(p,  0.98);
	}
	
	@Override
	public void vmReady(int vmID) {
		listeners.forEach(l -> l.vmReady(vmID));
	}

	@Override
	public void vmShutdown(int vmID) {
		listeners.forEach(l -> l.vmShutdown(vmID));
	}
	
	
	public static void main(String[] args) {
		BanditDBSimulator sim = new BanditDBSimulator();
		sim.addListener(new BanditDBSimulatorListener() {

			@Override
			public void queryAssigned(int qID, int vmID) {
				System.out.println("Query " + qID + " assigned to " + vmID);
				
			}

			@Override
			public void queryComplete(int qID) {
				System.out.println("Query " + qID + " complete");
			}

			@Override
			public void vmProvisioned(int vmID, int vmType) {
				System.out.println("VM " + vmID + " of type " + vmType + " provisioned");
			}

			@Override
			public void vmReady(int vmID) {
				System.out.println("VM " + vmID + " ready");
			}

			@Override
			public void costPerQuery(long cost, long currTick) {
				System.out.println("Current cost per query: " + cost);
				
			}

			@Override
			public void vmShutdown(int vmID) {
				System.out.println("VM " + vmID + " shutdown");
			}
		
		});
		
		sim.start();
	}


}
