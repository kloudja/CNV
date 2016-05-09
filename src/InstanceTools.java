import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.amazonaws.AmazonClientException;
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

public class InstanceTools {
	AmazonEC2 ec2;
	AmazonCloudWatchClient cloudWatch;
	private InstancesInformation instancesInformation;



	public InstanceTools(InstancesInformation instancesInformation) {
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


	public Instance createWorkersGroupInstance(){		

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

		Instance instance = runInstancesResult.getReservation().getInstances().get(0);

		instancesInformation.addInstance_cost(instance, 0); //Adiciona instancia à InstanceInformation!!

		return instance;
	}

	public void prepareSystem() {

		initializeAWSConnection();
		/*
		 * Verifica se existe alguma instancia que possa receber os pedidos para fatorizar.
		 * Caso não exista inicia uma instancia.
		 */
		Set<Instance> workersGroupInstances = getAllWorkersGroupInstances();
		//		System.out.println("Há ["+workerGroupInstances.size()+"] instancias WorkerGroup a correr");
		if(workersGroupInstances.size()==0){
			createWorkersGroupInstance();
		}
		else{
			for (Instance instance : workersGroupInstances) {
				instancesInformation.addInstance_cost(instance, 0);
			}
		}
		System.out.println("De momento há [" + workersGroupInstances.size() + "] WorkersGroupInstances a correr");
	}

}
