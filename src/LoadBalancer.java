import java.io.IOException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 
 * Classe que recebe os pedidos e cria "Load Balancer Workers" para lidar com cada pedido.
 * 
 * @author kloudja
 *
 */
public class LoadBalancer implements HttpHandler {

	InstancesInformation instancesInformation;
	InstanceTools instanceTools;
	
	/**
	 * Prepara o sistema com todas as instancias necessarias para poder trabalhar normalmente. 
	 */
	public LoadBalancer() {
		instancesInformation = new InstancesInformation();
		
		instanceTools = new InstanceTools(instancesInformation);
		instanceTools.prepareSystem();
		
		//TODO Criar Auto-Scaler
		new AutoScaler(instancesInformation, instanceTools).start();
		
	}

	public void handle(HttpExchange httpExchange) throws IOException {

		//Todos os workers mexem na mesma classe que possui informação sobre as instancias do sistema
		LoadBalancerWorker loadBalancerWorker = new LoadBalancerWorker(httpExchange, instancesInformation); 
		loadBalancerWorker.start();
	}
}