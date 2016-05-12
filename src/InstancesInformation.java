import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.rowset.spi.SyncResolver;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ecs.model.KeyValuePair;

public class InstancesInformation {

	private LinkedHashMap<Instance, TimeCost> instance_TimeCost;
	private LinkedHashMap<Instance, Date> instance_startTime;
	private LinkedHashMap<Integer, Integer> memcache;
	
	
	public InstancesInformation() {
		super();
		instance_TimeCost = new LinkedHashMap<>();
		instance_startTime = new LinkedHashMap<>();
		memcache = new LinkedHashMap<>();
	}

	public synchronized void addInstance_cost(Instance instance, int cost){

		if(instance_TimeCost.containsKey(instance)){
			TimeCost timeCost = instance_TimeCost.get(instance);
			int notUpdatedCost = timeCost.getCost();

			int actualCost = notUpdatedCost + cost;
			instance_TimeCost.put(instance, new TimeCost(new Date(), actualCost));
		}
		else {
			instance_TimeCost.put(instance, new TimeCost(new Date(), cost));
		}
	}
	
	public synchronized void addInstance_startTime(Instance instance, Date date){
		
			instance_startTime.put(instance, date);
		

	}
	
	public LinkedHashMap<Instance, TimeCost> getInstance_TimeCost() {
		return instance_TimeCost;
	}

	public void setInstance_TimeCost(LinkedHashMap<Instance, TimeCost> instance_TimeCost) {
		this.instance_TimeCost = instance_TimeCost;
	}

	public LinkedHashMap<Instance, Date> getInstance_startTime() {
		return instance_startTime;
	}

	public void setInstance_startTime(LinkedHashMap<Instance, Date> instance_startTime) {
		this.instance_startTime = instance_startTime;
	}

	/**
	 * Ordena as instacias por custo.
	 */
	public synchronized void sortInstancesByCost() {

//		Instance a = new Instance();
//		a.setInstanceId("A");
//		Instance b = new Instance();
//		b.setInstanceId("B");
//		Instance c = new Instance();
//		c.setInstanceId("C");
//		
//		HashMap<Instance, TimeCost> map = new LinkedHashMap<Instance, TimeCost>();
//		map.put(a, new TimeCost(10, 5));	
//		map.put(c, new TimeCost(10, 6));
//		map.put(b, new TimeCost(10, 2));


		// Ordena-se
		Object[] objectArray = instance_TimeCost.entrySet().toArray();

		Arrays.sort(objectArray, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Integer)((Map.Entry<Instance, TimeCost>) o1).getValue().getCost()).compareTo(
						(Integer)((Map.Entry<Instance, TimeCost>) o2).getValue().getCost());
			}
		});

		//Limpa-se o hash map desordenado e colocamos tudo novamente, mas ordenado!
		instance_TimeCost.clear();

		for (Object e : objectArray) {
			instance_TimeCost.put(((Map.Entry<Instance, TimeCost>) e).getKey(), ((Map.Entry<Instance, TimeCost>) e).getValue());
		}
	}


	public synchronized void deleteCostFromInstance(Instance instance, int cost) {

		if(instance_TimeCost.containsKey(instance)){
			TimeCost timeCost = instance_TimeCost.get(instance);
			int notUpdatedCost = timeCost.getCost();

			int actualCost = notUpdatedCost - cost;
			instance_TimeCost.put(instance, new TimeCost(new Date(), actualCost));
		}

	}
}
