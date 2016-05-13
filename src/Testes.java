import java.math.BigInteger;
import java.util.*;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;

public class Testes {

	public static void main(String[] args) throws InterruptedException {

		// ================== ORDENAR HASH MAP =========================================
		/*Instance a = new Instance();
		a.setInstanceId("a");
		Instance b = new Instance();
		b.setInstanceId("b");
		Instance c = new Instance();
		c.setInstanceId("c");
		Instance d = new Instance();
		d.setInstanceId("d");
		
		HashMap<Instance, Date> map = new HashMap<Instance, Date>();
		Date date = new Date();
	    map.put(d, date);
	    Thread.sleep(1000 * 30);
	    map.put(b, new Date());
	    Thread.sleep(1000 * 30);
	    map.put(c, new Date());
	    Thread.sleep(1000 * 30);
	    map.put(a, date);

	    
	    Object[] objArray = map.entrySet().toArray();
	    Arrays.sort(objArray, new Comparator() {
	        public int compare(Object o1, Object o2) {
	            return ((Map.Entry<Instance, Date>) o1).getValue().compareTo(
	                    ((Map.Entry<Instance, Date>) o2).getValue());
	        }
	    });
	    for (Object e : objArray) {
	        System.out.println(((Map.Entry<Instance, Date>) e).getKey().getInstanceId() + " : "
	                + ((Map.Entry<Instance, Date>) e).getValue());
	    }*/

		// ================== PRIMEIRO ELEMENTO DO HASH MAP ============================
		/*HashMap<String, Integer> map = new HashMap<String, Integer>();
	    map.put("a", 4);
	    map.put("c", 6);
	    map.put("b", 2);

	    Object myKey = map.keySet().toArray()[0];
	    System.out.println(myKey);*/

		// ================== INTFACTORIZATION MAIN ============================
		/*String[] s = {"50"};
		IntFactorization.main(s);*/

		// ================== ARRAYLIST PARA STRING ============================
		/*StringBuilder stringBuilder = new StringBuilder();

		ArrayList<String> al = new ArrayList<>();
		al.add("A");
		al.add("B");
		//ArrayList to String separado por ","
		for (String string : al) {
			stringBuilder.append(string + ",");
		}
		stringBuilder.deleteCharAt(stringBuilder.length()-1);
		System.out.println("Stringbuilder: " + stringBuilder);

		// Cada elemento da string
		String[] sa = stringBuilder.toString().split(",");
		for (String string : sa) {
			System.out.println("Elemento: " + string);
		}*/

		// ================== IP ADDRESS OF INSTANCES ============================
		/*try{


			System.out.println("===========================================");
			System.out.println("Welcome to the AWS Java SDK!");
			System.out.println("===========================================");

			AmazonEC2      ec2;
			AmazonCloudWatchClient cloudWatch; 

			AWSCredentials credentials = null;
			try {
				credentials = new ProfileCredentialsProvider().getCredentials();
			} catch (Exception e) {
				throw new AmazonClientException(
						"Cannot load the credentials from the credential profiles file. " +
								"Please make sure that your credentials file is at the correct " +
								"location (~/.aws/credentials), and is in valid format.",
								e);
			}
			ec2 = new AmazonEC2Client(credentials);
			cloudWatch= new AmazonCloudWatchClient(credentials);

			ec2.setEndpoint("ec2.us-west-2.amazonaws.com");
            cloudWatch.setEndpoint("monitoring.us-west-2.amazonaws.com");
            
            
			System.out.println("Starting a new instance.");
            RunInstancesRequest runInstancesRequest =
               new RunInstancesRequest();

            runInstancesRequest.withImageId("ami-44a05d24")
                               .withInstanceType("t2.micro")
                               .withMinCount(1)
                               .withMaxCount(1)
                               .withKeyName("myKeyPair")
                               .withSecurityGroups("securityGroupWebServer");
            RunInstancesResult runInstancesResult =
               ec2.runInstances(runInstancesRequest);
            String newInstanceId = runInstancesResult.getReservation().getInstances()
                                      .get(0).getInstanceId();
            //System.out.println("New instance ID: " + newInstanceId + "with IP ADDRESS : " + runInstancesResult.getReservation().getInstances().get(0).getPublicIpAddress());
            
            
			DescribeInstancesResult describeInstancesResult = ec2.describeInstances();
			List<Reservation> reservations = describeInstancesResult.getReservations();
			Set<Instance> instances = new HashSet<Instance>();


			System.out.println("total reservations = " + reservations.size());
			for (Reservation reservation : reservations) {
				instances.addAll(reservation.getInstances());
			}

			System.out.println("Total of instances: " + instances.size());
			for (Instance instance : instances) {
				System.out.println("======== Instance =======");
				System.out.println("Instance Public IP Address: " + instance.getPublicIpAddress());
				System.out.println("Instance Private IP Address: " + instance.getPrivateIpAddress());
				System.out.println("Instance Private IP Address: " + instance.getImageId());
				String name = instance.getInstanceId();
				String state = instance.getState().getName();
				System.out.println("Instance State : " + state +".");
				if (state.equals("running")) { 
					System.out.println("running instance id = " + name);
				}
				else if (state.equals("pending")) {
					System.out.println("pending instance id = " + name);
				}
			}
		} catch (AmazonServiceException ase) {
			System.out.println("Caught Exception: " + ase.getMessage());
			System.out.println("Reponse Status Code: " + ase.getStatusCode());
			System.out.println("Error Code: " + ase.getErrorCode());
			System.out.println("Request ID: " + ase.getRequestId());
		}
		*/
		/*
		System.out.println(System.currentTimeMillis());
		System.out.println(new Date().getTime());	
		*/
		
		Date a = new Date(1, 2, 3, 4, 5);
		Date c = new Date(1, 2, 3, 5, 4);
		Date b = new Date(1, 2, 3, 4, 6);
		
		System.out.println(a.compareTo(b)<0);
		System.out.println(a.compareTo(c)<0);
		System.out.println(c.compareTo(b)<0);

	}

	public ArrayList<Integer> calc(int number){

		ArrayList<Integer> arrayList = new ArrayList<>();
		BigInteger b = new BigInteger("50");
		b.toString();
		return arrayList;

	}
}
