import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ecs.model.KeyValuePair;

public class InstancesInformation {

	private HashMap<Instance, Integer> instance_cost;

	public InstancesInformation() {
		super();
		instance_cost = new HashMap<>();
	}


	public synchronized void addInstance_cost(Instance instance, int cost){
		instance_cost.put(instance, cost);
	}


	public HashMap<Instance, Integer> getInstance_cost() {
		return instance_cost;
	}


	public synchronized void setInstance_cost(HashMap<Instance, Integer> instance_cost) {
		this.instance_cost = instance_cost;
	}

	/**
	 * Ordena as instacias por custo.
	 */
	public synchronized void sortInstancesByCost() {

		/*HashMap<String, Integer> map = new HashMap<String, Integer>();
		    map.put("a", 4);
		    map.put("c", 6);
		    map.put("b", 2);*/

		// Ordena-se
		Object[] a = instance_cost.entrySet().toArray();

		Arrays.sort(a, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Map.Entry<Instance, Integer>) o2).getValue().compareTo(
						((Map.Entry<Instance, Integer>) o1).getValue());
			}
		});

		//Limpa-se o hash map desordenado e colocamos tudo novamente, mas ordenado!
		instance_cost.clear();

		for (Object e : a) {
			instance_cost.put(((Map.Entry<Instance, Integer>) e).getKey(), ((Map.Entry<Instance, Integer>) e).getValue());
		}
	}


	public synchronized void deleteCostFromInstance(Instance instance, int cost) {
		
		int notUpdatedCost = instance_cost.get(instance);
		int actualCost = notUpdatedCost - cost;
		instance_cost.put(instance, actualCost);
		
	}
	
}
