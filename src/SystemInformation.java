import java.math.BigInteger;
import java.util.ArrayList;
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

public class SystemInformation {

	private LinkedHashMap<Instance, TimeCost> instance_TimeCost; // A instancia X tem o custo Y desde o instante Z
	private LinkedHashMap<Instance, Integer> instance_numberOfRequests; // Numero de pedidos que cada instancia esta a processar
	private LinkedHashMap<Instance, Date> instance_startTime; // Tempo que cada instancia foi lancada
	private LinkedHashMap<BigInteger, Long> memcache; // Cache [numero->custo]
	private LinkedHashMap<BigInteger, ArrayList<Request>> requestMemory; // Info [numero->pedido]

	public SystemInformation() {
		super();
		instance_TimeCost = new LinkedHashMap<>();
		instance_startTime = new LinkedHashMap<>();
		memcache = new LinkedHashMap<>();
		requestMemory = new LinkedHashMap<>();
	}

	public void addInstance_cost(Instance instance, int cost){

		synchronized(instance_TimeCost){
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
	}

	public void addInstance_startTime(Instance instance, Date date){

		synchronized(instance_startTime){
			instance_startTime.put(instance, date);
		}
	}

	public void addMemcache(BigInteger number, Long cost){
		synchronized(memcache){
			memcache.put(number, cost);
		}
	}

	public void addHistoryRequest(BigInteger bigInteger, Request request){
		synchronized(requestMemory){
			ArrayList<Request> tmp = requestMemory.get(bigInteger);
			tmp.add(request);
			requestMemory.put(bigInteger, tmp);
		}
	}

	public void addRequestToInstance(Instance instance){
		synchronized(instance_numberOfRequests){
			int oldNumberOfRequests = instance_numberOfRequests.get(instance);
			instance_numberOfRequests.put(instance, oldNumberOfRequests++);
		}
	}

	public synchronized void deleteRequestToInstance(Instance instance){
		synchronized(instance_numberOfRequests){
			int oldNumberOfRequests = instance_numberOfRequests.get(instance);
			instance_numberOfRequests.put(instance, oldNumberOfRequests--);
		}
	}

	public LinkedHashMap<Instance, Integer> getInstance_numberOfRequests() {
		return instance_numberOfRequests;
	}

	public synchronized void setInstance_numberOfRequests(LinkedHashMap<Instance, Integer> instance_numberOfRequests) {
		this.instance_numberOfRequests = instance_numberOfRequests;
	}

	public LinkedHashMap<Instance, TimeCost> getInstance_TimeCost() {
		return instance_TimeCost;
	}

	public synchronized void setInstance_TimeCost(LinkedHashMap<Instance, TimeCost> instance_TimeCost) {
		this.instance_TimeCost = instance_TimeCost;
	}

	public LinkedHashMap<Instance, Date> getInstance_startTime() {
		return instance_startTime;
	}

	public synchronized void setInstance_startTime(LinkedHashMap<Instance, Date> instance_startTime) {
		this.instance_startTime = instance_startTime;
	}

	public LinkedHashMap<BigInteger, Long> getMemcache() {
		return memcache;
	}

	public synchronized void setMemcache(LinkedHashMap<BigInteger, Long> memcache) {
		this.memcache = memcache;
	}

	public LinkedHashMap<BigInteger, ArrayList<Request>> getRequestMemory() {
		return requestMemory;
	}

	public void setRequestMemory(LinkedHashMap<BigInteger, ArrayList<Request>> requestMemory) {
		this.requestMemory = requestMemory;
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

		synchronized(instance_TimeCost){

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
	}


	public void deleteCostFromInstance(Instance instance, int cost) {

		synchronized(instance_TimeCost){
			if(instance_TimeCost.containsKey(instance)){
				TimeCost timeCost = instance_TimeCost.get(instance);
				int notUpdatedCost = timeCost.getCost();

				int actualCost = notUpdatedCost - cost;
				instance_TimeCost.put(instance, new TimeCost(new Date(), actualCost));
			}
		}
	}

	public void sortInstance_numberOfRequests(){

		synchronized(instance_numberOfRequests){
			Object[] objArray = instance_numberOfRequests.entrySet().toArray();
			Arrays.sort(objArray, new Comparator() {
				public int compare(Object o1, Object o2) {
					return ((Map.Entry<Instance, Integer>) o1).getValue().compareTo(
							((Map.Entry<Instance, Integer>) o2).getValue());
				}
			});


			//Limpa-se o hash map desordenado e colocamos tudo novamente, mas ordenado!
			instance_numberOfRequests.clear();

			for (Object e : objArray) {
				instance_numberOfRequests.put(((Map.Entry<Instance, Integer>) e).getKey(), ((Map.Entry<Instance, Integer>) e).getValue());
			}
		}
	}
}
