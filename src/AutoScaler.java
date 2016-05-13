import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.amazonaws.services.ec2.model.Instance;

public class AutoScaler extends Thread{

	private SystemInformation systemInformation;
	private AwsTools awsTools;

	public AutoScaler(SystemInformation systemInformation, AwsTools awsTools) {
		this.systemInformation = systemInformation;
		this.awsTools = awsTools;
	}

	@Override
	public void run() {
		super.run();

		while(true){
			try {
				Thread.sleep(1000 * 10 * 3); // 3 minutos

				awsTools.cacheMetrics(); // Faz cache das metricas
				checkForInstancesToTerminte(); //Verifica se ha instancias para serem terminadas
				//TODO  Verificar se ha instancias para serem iniciadas


			} catch (InterruptedException e) {
				System.out.println("[AUTO SCALER] Fui acordado!");
			} 
		}
	}

	private void checkForInstancesToTerminte() throws InterruptedException {

		LinkedHashMap<Instance, TimeCost> tmpInstance_TimeCost = systemInformation.getInstance_TimeCost();
		LinkedHashMap<Instance, Date> tmpInstance_startTime= systemInformation.getInstance_startTime();

		// Apagar instancias
		for (Entry<Instance, TimeCost> entry : tmpInstance_TimeCost.entrySet()) {
			long entryCost = entry.getValue().getCost();
			Date entryCostTime= entry.getValue().getTime();
			Date instanteStartTime = tmpInstance_startTime.get(entry.getKey());
			Date currentTime = new Date();
			long dezMinutos = 1000*60*10;
			long pertDumaHora = 1000*60*50; //cinquenta minutos

			if(entryCost==0// Se a instancia nao tiver custo nenhum
					&& (( currentTime.getTime() - entryCostTime.getTime() ) >= dezMinutos))//estiver ha 10 minutos sem fazer nada 			
			{

				long i = currentTime.getTime() - instanteStartTime.getTime();
				if(instanteStartTime.getMinutes()>=10){
					if(i>=-10 && i<=-1){
						System.out.println("[AUTO SCALER] Vou apagar a instancia com ID : [" + entry.getKey().getInstanceId() + "]"); 
						awsTools.terminateInstance(entry.getKey());
					}
				} else{
					if(i<=59 && i>=50){
						System.out.println("[AUTO SCALER] Vou apagar a instancia com ID : [" + entry.getKey().getInstanceId() + "]"); 
						awsTools.terminateInstance(entry.getKey());
					}
				}

			}
		}
	}
}
