package edu.brandeis.rlearn;

import static spark.Spark.*;

public class Server {
	public static void main(String[] args) {
		webSocket("/bandit", BanditWebSocket.class);
		staticFiles.location("/assets");
		init();
	}
}
