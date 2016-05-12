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

	SystemInformation systemInformation;
	AwsTools awsTools;
	
	/**
	 * Prepara o sistema com todas as instancias necessarias para poder trabalhar normalmente. 
	 */
	public LoadBalancer() {
		systemInformation = new SystemInformation();
		
		awsTools = new AwsTools(systemInformation);
		awsTools.prepareSystem();
		
		//TODO Criar Auto-Scaler
		new AutoScaler(systemInformation, awsTools).start();
		
	}

	public void handle(HttpExchange httpExchange) throws IOException {

		//Todos os workers mexem na mesma classe que possui informacao sobre as instancias do sistema
		LoadBalancerWorker loadBalancerWorker = new LoadBalancerWorker(httpExchange, systemInformation); 
		loadBalancerWorker.start();
	}
}