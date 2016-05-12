import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * 
 * Classe que recebe os pedidos e encaminha para o LoadBalancer.
 * 
 * @author kloudja
 *
 */
public class SystemStart {
	
	/**
	 * 
	 * Cria um servidor HTTP no endereço "localhost" e no porto "8000".
	 * Para cada pedido recebido para a página "http://localhost:8000/f.html", reemcaminha esse pedido para o Load Balancer.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("====== PREPARING THE SYSTEM ======");
		LoadBalancer loadBalancer = new LoadBalancer();
		InetSocketAddress inetSocketAddress = new InetSocketAddress(8000);
		HttpServer server = HttpServer.create(inetSocketAddress, 0);
		System.out.println("System IP Adress: [" + inetSocketAddress.getAddress().toString()+"]");
		server.createContext("/f.html", loadBalancer);
		server.setExecutor(null); 
		server.start();
		System.out.println("====== SYSTEM READY ======");
	}

	/**
	 * 
	 * Envia a resposta ao pedido HTTP.
	 * 
	 * @param httpExchange pedido HTTP.
	 * @param response resposta a ser enviada.
	 * @throws IOException
	 */
	public static void writeResponse(HttpExchange httpExchange, String response) throws IOException {
		httpExchange.sendResponseHeaders(200, response.length());
		OutputStream os = httpExchange.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}
}