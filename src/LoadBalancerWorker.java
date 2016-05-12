import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.sun.net.httpserver.HttpExchange;

/**
 * Analisa os pedidos e reencaminha consoante o custo de cada um.
 * @author kloudja
 *
 */
public class LoadBalancerWorker extends Thread {

	private HttpExchange httpExchange;
	int MAX_COST = 5000;
	AwsTools awsTools;

	private SystemInformation systemInformation;

	/**
	 * 
	 * @param httpExchange
	 * @param systemInformation
	 */
	public LoadBalancerWorker(HttpExchange httpExchange, SystemInformation systemInformation) {

		this.httpExchange = httpExchange;
		this.systemInformation = systemInformation;
		awsTools = new AwsTools(systemInformation);

	}

	/**
	 * Devolve o objecto que representa o pedido HTTP do cliente.
	 * @return httpExchange
	 */
	public HttpExchange getHttpExchange() {
		return httpExchange;
	}

	/**
	 * Altera o pedido HTTP. 
	 * @param httpExchange
	 */
	public void setHttpExchange(HttpExchange httpExchange) {
		this.httpExchange = httpExchange;
	}

	/**
	 * 
	 * Através do pedido HTTP é analisado o URL "//http:\\localhost:8080\f.html?n=10" (através do método queryToMap)
	 * e extrai-se o numero a fatorizar "n=10".
	 *  
	 * @param httpExchange pedido HTTP.
	 * @param response resposta ao pedido.
	 * @return numero a fatorizar.
	 * @throws IOException
	 */
	private BigInteger getNumberFromURL(HttpExchange httpExchange, StringBuilder response) throws IOException {

		//http:\\localhost:8080\f.html?n=10
		Map <String,String> parms = queryToMap(httpExchange.getRequestURI().getQuery());
		BigInteger numberToFactorize = new BigInteger("0");

		//Verificar se o que foi escrito no url foi um numero
		try {

			numberToFactorize = new BigInteger(parms.get("n"));

			//Caso não seja numero, explicamos o erro
		} catch (Exception e) {

			response.append("<html><body>");
			response.append("<center><h1>INVALID ARGUMENT</h1></center><br>");
			response.append("<center><h2>INSERT A NUMBER</h2></center><br>");
			response.append("</body></html>");
			SystemStart.writeResponse(httpExchange, response.toString());
			return (BigInteger) null;

		}
		return numberToFactorize;
	}

	/**
	 * 
	 * Coloca os resultados num Map. Se o URL contiver "n=10", irá ser colocado <n,10> no Map.
	 * 
	 * @param query url do pedido HTTP
	 * @return Map de valores
	 */
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

	/**
	 *  Algoritmo nº1 para supor um custo para um numero caso nao exista o custo desse numero na base de dados.
	 * @param numberToFactorize numero a fatorizar
	 * @return
	 */
	private int sopaMagica(BigInteger numberToFactorize) {

		//Ir buscar valores superiores e inferiores � cache de metricas


		return 10000000;
	}

	/**
	 * Algoritmo n2.
	 * Com base no custo previamente calculado, calcula para que instancia enviar o pedido.
	 * Se necessario, cria uma instancia nova.
	 * 
	 * @param cost.
	 * @return Instance.
	 */
	private Instance calculateInstance(long cost) {

		Instance instanceToSend;

		systemInformation.sortInstancesByCost();
		LinkedHashMap<Instance, TimeCost> instanceTimeCost = systemInformation.getInstance_TimeCost();
		
		Instance firstInstanceOfHashMap = (Instance) instanceTimeCost.keySet().toArray()[0];
		int lowestCost = instanceTimeCost.get(firstInstanceOfHashMap).getCost();
		
		// Verifica se mais que uma instancia com o custo minimo
		LinkedList<Instance> instancesSameCost = new LinkedList<>();
		
		for (Entry<Instance, TimeCost> entry : instanceTimeCost.entrySet()) {
			  if (entry.getValue().getCost() == lowestCost) {
				  instancesSameCost.add(entry.getKey());
			  }
			  else
				  break;
			}
		
		//desempatar se houver mais que uma instancia pela que tem o pedido � mais tempo
		if(instancesSameCost.size()>1){
//			systemInformation.sortInstance_numberOfRequests();
//		LinkedHashMap<Instance, Integer> tmpInstance_numRqts= systemInformation.getInstance_numberOfRequests();
		
		}
		
		/* Depois de ordenado:
		 * => Se o (custo atual da instancia + novo custo) < MAX_COST escolhe-se essa instancia
		 * => Caso contrario, cria-se uma e escolhe-se essa instancia
		 */
		if((lowestCost + cost) < MAX_COST ){

			instanceToSend = firstInstanceOfHashMap;
			System.out.println("[LOAD BALANCER WORKER] Vou enviar para a instacia a correr com menos custo.");

		}
		else{

			System.out.println("[LOAD BALANCER WORKER] Vou enviar para uma nova instancia que acabei de criar.");

			return instanceToSend = awsTools.createWorkersGroupInstance();

		}

		return instanceToSend;
	}

	/**
	 * 
	 * Envia o pedido de fatorização para o endereço espeicficado na String "url".
	 * Espera a resposta ao pedido.
	 * 
	 * @param numberToFactorize numero a fatorizar
	 * @param instance 
	 * @return resultado da fatorização
	 */
	private String sendRequest(BigInteger numberToFactorize, Instance instance) {

		String stringArray = null;

		try{

			String url = "http://"+instance.getPublicIpAddress()+":8000/factorizacao.html?n="+numberToFactorize.toString();
			//			String url = "http://54.200.250.35:8000/factorizacao.html?n="+numberToFactorize.toString();

			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();// Envia o pedido

			System.out.println("[LOAD BALANCER WORKER] ENVIEI O PEDIDO PARA FATORIZAR PARA O URL: [" + url + "]");
			// optional default is GET
			con.setRequestMethod("GET");

			//add request header
			con.setRequestProperty("User-Agent", "Mozilla/5.0");

			int responseCode = con.getResponseCode();


			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));

			String inputLine;
			StringBuffer response = new StringBuffer();

			// Recebe a resposta oa pedido
			while ((inputLine = in.readLine()) != null) {
				System.out.println("[LOAD BALANCER WORKER] Recebi este resultado: " + inputLine);
				response.append(inputLine);
			}
			in.close();

			// Atualizar o custo atual na instancia
			int cost = awsTools.checkMetricInDB(numberToFactorize);
			systemInformation.deleteCostFromInstance(instance,cost);

			stringArray = response.toString();


		} catch(Exception e){
			System.out.println(e);
		}

		return stringArray;

	}

	@Override
	public void run() {
		super.run();
		try {
			System.out.println("=========================================================");

			StringBuilder response = new StringBuilder();
			Request request = new Request(); //Inicia um novo objeto do tipo pedido
			request.setStart(new Date());

			//Extrai o numero do URL
			BigInteger numberToFactorize = getNumberFromURL(httpExchange, response);
			request.setNumber(numberToFactorize);


			long cost = 0;

			try {

				cost = systemInformation.getMemcache().get(numberToFactorize.longValue()); // Ve se ha metrica na cache
				System.out.println("[LOAD BALANCER WORKER] Encontrei metrica na cache!");
			} catch (Exception e) {
				try {
					// Se nao houver metrica em cache verifica se existe metrica na base dados
					cost = awsTools.checkMetricInDB(numberToFactorize);
				} catch (Exception e1) {

				}
			}
			
			// Se n�o houver informa��o sobre o numero vai fazer estimativa
			if(cost==0){
				// Faz a sopa magica
				cost = sopaMagica(numberToFactorize); //Algoritmo 1
			}

			// Saber para que instancia mandar o numero com base no custo previamente calculado
			Instance instance = calculateInstance(cost);//Algoritmo 2
			request.setInstance(instance);
			//TODO Adicionar informacao ao array instance_TimeCost
			
			// Depois de se saber para que instancia mandar, ordena-se que ela calcule e devolva o numero fatorizado.
			String result = sendRequest(numberToFactorize, instance);
			request.setEnd(new Date());
			systemInformation.addHistoryRequest(numberToFactorize, request);
			
			response.append("<html><body>");
			response.append("The prime numbers are : " + result + "<br/>");
			response.append("</body></html>");

			SystemStart.writeResponse(httpExchange, response.toString());

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
