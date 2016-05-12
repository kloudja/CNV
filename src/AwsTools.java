import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.Tables;
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

public class AwsTools {
	private AmazonEC2 ec2;
	private AmazonCloudWatchClient cloudWatch;
	private SystemInformation systemInformation;
	private AmazonDynamoDBClient dynamoDB;

	public AwsTools(SystemInformation systemInformation) {

		try {

			initializeAWSConnection();
			initDbConnection();
			this.systemInformation = systemInformation;

		} catch (Exception e) {
			e.printStackTrace();
		}
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

	/**
	 * Inicia a ligação à base de dados com base no ficheiro que contem as credenciais.
	 * 
	 * @throws Exception
	 */
	private void initDbConnection() throws Exception {
		/*
		 * The ProfileCredentialsProvider will return your [default]
		 * credential profile by reading from the credentials file located at
		 * (~/.aws/credentials).
		 */
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
		dynamoDB = new AmazonDynamoDBClient(credentials);
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		dynamoDB.setRegion(usWest2);
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
					newInstance = instance;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


		System.out.println("[INSTANCE TOOLS] Criei nova instancia com id [" + newInstance.getInstanceId() + "] e IP ["+newInstance.getPublicIpAddress()+"]");

		//Adiciona instancia a InstanceInformation!!
		systemInformation.addInstance_cost(newInstance, 0); 
		systemInformation.addInstance_startTime(newInstance, newInstance.getLaunchTime());

		// Sleep de 30 segundos para deixar a instancia abrir as sockets
		try {
			Thread.sleep(1000*30);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

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
				systemInformation.addInstance_cost(instance, 0);
				systemInformation.addInstance_startTime(instance, instance.getLaunchTime());
			}
		}
		System.out.println("De momento ha [" + workersGroupInstances.size() + "] WorkersGroupInstances a correr");
		
		cacheMetrics();
	}


	public void terminateInstance(Instance instance) throws AmazonServiceException, AmazonClientException, InterruptedException
	{
		final String instanceId = instance.getInstanceId();
		final Boolean forceStop = true;
		ArrayList<String> instanceIds = new ArrayList<>();
		instanceIds.add(instance.getInstanceId());
		// Terminate the instance
		TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest(instanceIds);
		ec2.terminateInstances(terminateRequest);
	}

	/**
	 * 
	 * Apos iniciada a ligacao a� base de dados, verifica se ja existe um custo associado ao numero pedido.
	 * 
	 * @param numberToFactorize numero a fatorizar
	 * @return custo do pedido
	 * @throws Exception
	 */
	public int checkMetricInDB(BigInteger numberToFactorize) throws Exception {

		int cost = 0;

		try {
			String tableName = "Costs";

			// Create table if it does not exist yet
			if (Tables.doesTableExist(dynamoDB, tableName)) {
				//System.out.println("Table " + tableName + " is already ACTIVE");
			} else {
				// Create a table with a primary hash key named 'name', which holds a string
				CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
						.withKeySchema(new KeySchemaElement().withAttributeName("number").withKeyType(KeyType.HASH))
						.withAttributeDefinitions(new AttributeDefinition().withAttributeName("number").withAttributeType(ScalarAttributeType.S))
						.withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));
				TableDescription createdTableDescription = dynamoDB.createTable(createTableRequest).getTableDescription();
				System.out.println("Created Table: " + createdTableDescription);

				// Wait for it to become active
				//System.out.println("Waiting for " + tableName + " to become ACTIVE...");
				Tables.awaitTableToBecomeActive(dynamoDB, tableName);
			}

			// Describe our new table
			//System.out.println("==================================================================================================");
			DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
			TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
			//System.out.println("Table Description: " + tableDescription);

			// Scan items 
			HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
			Condition condition = new Condition()
					.withComparisonOperator(ComparisonOperator.EQ.toString())
					.withAttributeValueList(new AttributeValue().withS(numberToFactorize.toString()));
			scanFilter.put("number", condition);
			ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
			ScanResult scanResult = dynamoDB.scan(scanRequest);

			cost = Integer.parseInt(scanResult.getItems().get(0).get("cost").getN());

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with AWS, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}

		return cost;
	}

	public void cacheMetrics(){

		ScanRequest scanRequest = new ScanRequest()
				.withTableName("Costs");

		ScanResult result = dynamoDB.scan(scanRequest);
		for (Map<String, AttributeValue> item : result.getItems()){
			long number = 0;
			long cost = 0;
			for (Entry<String, AttributeValue> iterable_element : item.entrySet()) {

				if (iterable_element.getValue().getS() != null)
					number = Long.parseLong(iterable_element.getValue().getS());
				else
					cost = Long.parseLong(iterable_element.getValue().getN());

			}
			systemInformation.addMemcache(number, cost);
		}
		

	}
}
