package assignment2.attacker;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.logging.log4j.Logger; 

import assignment2.util.API;
import assignment2.util.APIMessage;
import assignment2.util.APIMethod;
import assignment2.util.AttackerStatus;
import assignment2.util.HostPort;

public class AttackerAPI extends API {

	Logger log;
	
	
	/**
	 * Attacker instance for the API 
	 */
	Attacker attacker;
	
	/**
	 * Clock offset between the attacker and the coordnator
	 */
	Long clockOffset = (long) 0;
	
	/**
	 * Time last clock synchronization 
	 */
	Long lastSync = null;
	
	/**
	 * Helper for calculating standard deviation for latencies. 
	 */
	DescriptiveStatistics latencies = null;
	
	public AttackerAPI(Attacker a, Socket socket, Logger log) throws IOException {
		super(socket);
		this.log = log;
		this.attacker = a;
	}

	/* (non-Javadoc)
	 * @see assignment2.util.API#createSession()
	 * 
	 * Creates an API session by listerning for incoming commands from a coordnator and responding
	 * with an APIMessage object. Attacker will always wait for coordinator to poll.
	 * 
	 */
	@Override
	public void createSession() throws IOException {
		Exception exception = null;
		APIMessage response = null;
		APIMessage request = null;
		boolean doNotLog = false;
		while(!socket.isClosed()) {
			//Get the object from remote.
			request = APIMessage.get(socketIn);
			//Ignore logging for noisy commands.
			if (request.getMethod().equals(APIMethod.ATTACKER_QUERY_STATE) ||
					request.getMethod().equals(APIMethod.ATTACKER_START_SYNC) ||
					request.getMethod().equals(APIMethod.ATTACKER_SYNC_CYCLE) ||
					request.getMethod().equals(APIMethod.ATTACKER_SYNC_DELTA) ||
					request.getMethod().equals(APIMethod.ATTACKER_STOP_SYNC)) { doNotLog = true; }
			if (!doNotLog) log.debug("Incoming message " + request.toString());
			try {
				//Obtain a response for the request.
				response = handleRequest(request);
			} catch (Exception e) {
				//If an error is thrown from downstream request handlers.
				response = new APIMessage(e.getMessage(), APIMethod.COORDINATOR_COMMAND_FAIL);
				exception = e;
			} finally {
				//Sanity check: Ensure a response is created.
				if (response == null) {
					exception = new Exception("Response was not initialized. " + request.toString());
					response = new APIMessage(exception.getMessage(), APIMethod.COORDINATOR_COMMAND_FAIL);
				}
				if (!doNotLog) log.debug("Sending message " + response.toString());
				//Send the message back to coordinator.
				APIMessage.send(response, socketOut);
			}
			//Print message if an exception occurred.
			if (exception != null) {
				log.warn(exception.getMessage());
				exception.printStackTrace();
			}
			
		}
	}


	/**
	 * @param request
	 * @return
	 * @throws Exception
	 * 
	 * Handles a request by checking it's APIMethod and calls the corresponding 
	 * function to retrieve a response APIMessage.
	 */
	private APIMessage handleRequest(APIMessage request) throws Exception {
		switch (request.getMethod()) {
		case ATTACKER_QUERY_STATE:
			return queryState(request);
		case ATTACKER_SET_TARGET:
			return setTarget(request);
		case ATTACKER_START_ATTACK:
			return startAttack(request);
		case ATTACKER_STOP_ATTACK:
			return stopAttack(request);
		case ATTACKER_START_SYNC:
			return startSync(request);
		case ATTACKER_SYNC_DELTA:
			return syncDelta(request);
		case ATTACKER_SYNC_CYCLE:
			return syncCycle(request);
		case ATTACKER_STOP_SYNC:
			return stopSync(request);
		default:
			throw new Exception("Unknown request. " + request.toString());
		}
	}

	/**
	 * @param request
	 * @return APIMessage containing the current status and remote information.
	 * 
	 * Generate a state query response.
	 */
	private APIMessage queryState(APIMessage request) {
		String target = null;
		try { 
			target = attacker.getTargetAddress().toString(); 
		} catch (Exception e) {
			target = ""; 
		}
		String data = attacker.getStatus().getStatus() + "," + attacker.getStatus().getMessage() + "," + target;
		return okay(data);
	}
	
	
	/**
	 * @param request
	 * @return APIMessage indicating the success of setting the target.
	 * @throws Exception
	 * 
	 * Sets the current attackers target from the value of the request.
	 */
	private APIMessage setTarget(APIMessage request) throws Exception {
		attackerMustBe(AttackerStatus.IDLE);
		String requestData = request.getData();
		HostPort hostPort  = null;
		if (requestData.length() != 0) {
			hostPort = new HostPort(requestData);
			log.info("Target set to " + hostPort.toString());
		} else {
			log.info("Target cleared.");
		}
		
		attacker.setTargetAddress(hostPort);
		return okay();
	}
	
	/**
	 * @param request
	 * @return APIMessage indicating the success of starting the attack.
	 * @throws Exception
	 * 
	 * Starts an attack against the current target at the time specified.
	 */
	private APIMessage startAttack(APIMessage request) throws Exception {
		attackerMustBe(AttackerStatus.IDLE);
		
		Long startTime = Long.valueOf(request.getData());
		attacker.startAttack(new Date(getLocalTime(startTime)));
		return okay();
	}
	
	/**
	 * @param request
	 * @return APIMessage indicating the success of stopping the attack.
	 * @throws Exception
	 * 
	 * Stops an ongoing attack immediately.
	 */
	private APIMessage stopAttack(APIMessage request) throws Exception {
		attackerMustBe(AttackerStatus.ATTACKING);
		
		attacker.stopAttack();
		return okay();
	}
	
	/**
	 * @param request
	 * @return APIMessage indicating the success of initialization of synchronization.
	 * @throws Exception
	 * 
	 * Initializes the variables for a new time synchroniztion run.
	 */
	private APIMessage startSync(APIMessage request) throws Exception {
		log.debug("SYNC : Start.");
		latencies = new DescriptiveStatistics();
		return okay();
	}
	
	/**
	 * @param request
	 * @return APIMessage indicating the success of updating the last sync time.
	 * @throws Exception
	 * 
	 * Protocol helper to update the lastSync time before a burst.
	 */
	private APIMessage syncCycle(APIMessage request) throws Exception {
		log.debug("SYNC : Cycle "+ request.getData());
		lastSync = request.getTimeAtRetrieve();
		return okay();
	}
	
	/**
	 * @param request
	 * @return APIMessage indicating the latency of the retrieved message.
	 * @throws Exception
	 * 
	 * Calculates the time between current request and previous request 
	 * observed via the lastSync variable. Calculates the RTT/2 and stores
	 * it for further calculation.
	 */
	private APIMessage syncDelta(APIMessage request) throws Exception {
		long clientTime = request.getTimeAtRetrieve();
		long latency = (clientTime - lastSync) / 2;
		latencies.addValue((double) latency);
		lastSync = clientTime;
		return okay(String.valueOf(latency));
	}
	
	/**
	 * @param request
	 * @return APIMessage indicating the calculated latency and time offset.
	 * @throws Exception
	 * 
	 * Calculates the latency between the attacker and coordinator by calculating 
	 * the arithmetic mean of the values within one standard deviation of the 
	 * median of the latencies recorded by the syncDelta's.
	 */
	private APIMessage stopSync(APIMessage request) throws Exception {
		long clientTime = request.getTimeAtRetrieve();
		long serverTime = request.getTimeAtSend();
		double[] latencyArray = latencies.getValues();
		double median = (new Median().evaluate(latencyArray));
		double stdDev = latencies.getStandardDeviation();
		List<Double> validLatencies = new ArrayList<Double>();
		
		//Filter out values that are outside one standard deviation from the
		//median latency
		for(int i = 0; i < latencyArray.length; i++ ) {
			if (Math.abs(latencyArray[i] - median) <= stdDev) {
				validLatencies.add(latencyArray[i]);
			} 
		}
		
		//Calculate the mean value of the filtered latencies.
		long latency = (long) new Mean().evaluate(
				ArrayUtils.toPrimitive(validLatencies.toArray( new Double[validLatencies.size()] ))
				);
		
		setClockOffset(clientTime, serverTime, latency);
		
		log.info("SYNC : MEDIAN=" + median + "ms STDDEV=" + stdDev + "ms LATENCY=" +
				latency + "ms OFFSET=" + clockOffset + "ms");
		
		return okay(latency + "," + clockOffset);
	}
	private APIMessage okay(String message) {
		return new APIMessage(message, APIMethod.COORDINATOR_COMMAND_OK);
	}
	private APIMessage okay() {
		return okay("");
	}
	
	/**
	 * @param referenceTime time to calculate the offset against.
	 * @param serverTime time retrieved from the remote
	 * @param latency latency between current time and remote time
	 */
	private void setClockOffset(long referenceTime, long serverTime, long latency) {
		clockOffset = referenceTime - (serverTime + latency);
	}
	
	
	/**
	 * @param serverTime
	 * @return serverTime adjusted to local time via the calculated offset.
	 * 
	 */
	private long getLocalTime(long serverTime) {
		log.debug("Time from server is " + new Date(serverTime) + " Offset time is " + new Date(serverTime + clockOffset));
		return clockOffset + serverTime;
	}
	
	private void attackerMustBe(AttackerStatus as) throws Exception {
		if (!attacker.getStatus().equals(as)) {
			throw new Exception("Attacker is " + attacker.getStatus().toString() + ", must be " + as.toString());
		}
	}

}
