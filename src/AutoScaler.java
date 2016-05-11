import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.amazonaws.services.ec2.model.Instance;

public class AutoScaler extends Thread{

	private InstancesInformation instancesInformation;
	private InstanceTools instanceTools;

	public AutoScaler(InstancesInformation instancesInformation) {
		this.instancesInformation = instancesInformation;
		instanceTools = new InstanceTools(this.instancesInformation);
	}

	@Override
	public void run() {
		super.run();

		while(true){
			try {
				
				checkForInstancesToTerminte();
				
				
				Thread.sleep(1000 * 60 ); // 1 minute
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
	}

	private void checkForInstancesToTerminte() throws InterruptedException {
		
		LinkedHashMap<Instance, TimeCost> tmpInstance_TimeCost = instancesInformation.getInstance_TimeCost();
		LinkedHashMap<Instance, Long> tmpInstance_startTime= instancesInformation.getInstance_startTime();
		
		// Apagar instancias
		for (Entry<Instance, TimeCost> entry : tmpInstance_TimeCost.entrySet()) {
			int entryCost = entry.getValue().getCost();
			long entryCostTime= entry.getValue().getTime();
			long instanteStartTime = tmpInstance_startTime.get(entry.getKey()); //breka-se aqui todo
			long currentTime = System.currentTimeMillis();
			long dezMinutos = 1000*60*10;
			long pertDumaHora = 1000*60*50;
			// Se a instancia nao tiver custo nenhum, estiver h� 10 minutos sem fazer nada e tiver sido iniciada h� 50 minutos
			if(entryCost==0 && ((entryCostTime - currentTime) >= dezMinutos) && ((instanteStartTime - currentTime) >= pertDumaHora)){
				instanceTools.stopInstance(entry.getKey());
			}
		}
	}
}
