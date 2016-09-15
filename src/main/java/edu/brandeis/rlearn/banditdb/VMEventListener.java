package edu.brandeis.rlearn.banditdb;

public interface VMEventListener {
	public void queryComplete(int qID);
	public void vmReady(int vmID);
	public void vmShutdown(int vmID);
}
