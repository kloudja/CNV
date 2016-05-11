import java.util.ArrayList;
import java.util.HashMap;
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

//				HashMap<Instance, Integer> instance_cost = instancesInformation.getInstance_cost();
//				ArrayList<Instance> uselessInstances = new ArrayList<>();
//				
//				for (Entry<Instance, Integer> inst_cost : instance_cost.entrySet()) {
//					if(inst_cost.getValue() == 0){
//						uselessInstances.add(inst_cost.getKey());
//					}
//				}
//
//				for (Instance instance : uselessInstances) {
//					instanceTools.stopInstance(instance);
//				}
				
				
				Thread.sleep(300000); // 5 minutes
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
	}
}
