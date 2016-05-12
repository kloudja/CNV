import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.amazonaws.services.opsworks.model.StopInstanceRequest;

public class InstanceTools {
	AmazonEC2 ec2;
	AmazonCloudWatchClient cloudWatch;
	private InstancesInformation instancesInformation;

	public InstanceTools(InstancesInformation instancesInformation) {
		initializeAWSConnection();
		this.instancesInformation = instancesInformation;
	}

	private void initializeAWSConnection() {
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
	}

	public Set<Instance> getAllInstances(){		

		DescribeInstancesResult describeInstancesResult = ec2.describeInstances();
		List<Reservation> reservations = describeInstancesResult.getReservations();
		Set<Instance> instances = new HashSet<Instance>();

		//		System.out.println("total reservations = " + reservations.size());
		for (Reservation reservation : reservations) {
			instances.addAll(reservation.getInstances());
		}

		return instances;
	}

	public int numberOfInstancesRunning(){

		return getAllInstances().size();
	}



	public Set<Instance> getAllWorkersGroupInstances(){
		Set<Instance> instances = getAllInstances();

		Set<Instance> workersGroupInstances = new HashSet<Instance>();
		for(Instance instance: instances){
			if(instance.getImageId().equals("ami-44a05d24")){
				workersGroupInstances.add(instance);
			}
		}
		return workersGroupInstances;
	}


	public synchronized Instance createWorkersGroupInstance(){		
		
		RunInstancesRequest runInstancesRequest =
				new RunInstancesRequest();

		runInstancesRequest.withImageId("ami-44a05d24") 
		.withInstanceType("t2.micro")
		.withMinCount(1)
		.withMaxCount(1)
		.withKeyName("myKeyPair")
		.withSecurityGroups("securityGroupWebServer");
		RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
		Instance newInstance = runInstancesResult.getReservation().getInstances().get(0);

		//Fazer sleep de 2 segundos para depois ir buscar com IP
		try {
			Thread.sleep(1000 * 2);
			Set<Instance> a = getAllInstances();
			for (Instance instance : a) {
				if(instance.getLaunchTime().compareTo(newInstance.getLaunchTime())==0){
					System.out.println("Ip passado 2 segundos [" + instance.getPublicIpAddress() + "]");
					newInstance = instance;
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println("[INSTANCE TOOLS] Criei nova instancia com id [" + newInstance.getInstanceId() + "] e IP ["+newInstance.getPublicIpAddress()+"]");

		instancesInformation.addInstance_cost(newInstance, 0); //Adiciona instancia a� InstanceInformation!!
		instancesInformation.addInstance_startTime(newInstance, System.currentTimeMillis());

		return newInstance;
	}

	public void prepareSystem() {
		/*
		 * Verifica se existe alguma instancia que possa receber os pedidos para fatorizar.
		 * Caso nao exista inicia uma instancia.
		 */
		Set<Instance> workersGroupInstances = getAllWorkersGroupInstances();
		//		System.out.println("Ha ["+workerGroupInstances.size()+"] instancias WorkerGroup a correr");
		if(workersGroupInstances.size()==0){
			createWorkersGroupInstance();
		}
		else{
			for (Instance instance : workersGroupInstances) {
				instancesInformation.addInstance_cost(instance, 0);
				instancesInformation.addInstance_startTime(instance, 0);
			}
		}
		System.out.println("De momento ha [" + workersGroupInstances.size() + "] WorkersGroupInstances a correr");
	}


	public void stopInstance(Instance instance) throws AmazonServiceException, AmazonClientException, InterruptedException
	{
		final String instanceId = instance.getInstanceId();
		final Boolean forceStop = true;
		ArrayList<String> instanceIds = new ArrayList<>();
		instanceIds.add(instance.getInstanceId());
		// Terminate the instance
		TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest(instanceIds);
		ec2.terminateInstances(terminateRequest);


	}

}
