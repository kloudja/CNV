import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.amazonaws.services.ec2.model.Instance;

public class AutoScaler extends Thread{

	private InstancesInformation instancesInformation;
	private InstanceTools instanceTools;

	public AutoScaler(InstancesInformation instancesInformation, InstanceTools instanceTools) {
		this.instancesInformation = instancesInformation;
		this.instanceTools = instanceTools;
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
		LinkedHashMap<Instance, Date> tmpInstance_startTime= instancesInformation.getInstance_startTime();
		
		// Apagar instancias
		for (Entry<Instance, TimeCost> entry : tmpInstance_TimeCost.entrySet()) {
			int entryCost = entry.getValue().getCost();
			Date entryCostTime= entry.getValue().getTime();
			Date instanteStartTime = tmpInstance_startTime.get(entry.getKey());
			Date currentTime = new Date();
			long dezMinutos = 1000*60*10;
			long pertDumaHora = 1000*60*50;
			
			if(entryCost==0// Se a instancia nao tiver custo nenhum
					&& ((entryCostTime.getTime() - currentTime.getTime()) >= dezMinutos)//estiver ha 10 minutos sem fazer nada 
					&& ((instanteStartTime.getTime() - currentTime.getTime()) >= pertDumaHora)){//e tiver sido iniciada ha 50 minutos
				instanceTools.stopInstance(entry.getKey());
			}
		}
	}
}
