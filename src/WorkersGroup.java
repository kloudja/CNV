import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


public class WorkersGroup implements HttpHandler {

	public static void main(String[] args) throws Exception {
		HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);
		server.createContext("/factorizacao.html", new WorkersGroup());
		server.setExecutor(null); // creates a default executor
		server.start();
	}

	public void handle(HttpExchange httpExchange) throws IOException {
		
		BigInteger numberToFactorize = getNumberFromURL(httpExchange);

		Worker worker = new Worker(numberToFactorize,httpExchange);
		worker.start();
		
	}

	private BigInteger getNumberFromURL(HttpExchange httpExchange) throws IOException {

		//http:\\localhost:8080\f.html?n=10
		Map <String,String> parms = queryToMap(httpExchange.getRequestURI().getQuery());
		BigInteger numberToFactorize = new BigInteger("0");

		//Verificar se o que foi escrito no url foi um numero
		try {

			numberToFactorize = new BigInteger(parms.get("n"));

			//Caso não seja numero, explicamos o erro
		} catch (Exception e) {

			return (BigInteger) null;

		}
		return numberToFactorize;
	}

	public static Map<String, String> queryToMap(String query){

		Map<String, String> result = new HashMap<String, String>();
		for (String param : query.split("&")) {
			String pair[] = param.split("=");
			if (pair.length>1) {
				result.put(pair[0], pair[1]);
			}else{
				result.put(pair[0], "");
			}
		}
		return result;
	}
}
