package edu.brandeis.rlearn.banditdb;
public interface BanditDBSimulatorListener {
	public void queryAssigned(int qID, int vmID);
	public void queryComplete(int qID);
	public void vmProvisioned(int vmID, int vmType);
	public void vmReady(int vmID);
	public void vmShutdown(int vmID);
	public void costPerQuery(long cost, long currTick);
}
