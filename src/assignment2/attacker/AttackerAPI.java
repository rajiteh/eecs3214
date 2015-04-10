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
	Attacker attacker;
	
	Long clockOffset = (long) 0;
	Long lastSync = null;
	DescriptiveStatistics latencies = null;
	
	public AttackerAPI(Attacker a, Socket socket, Logger log) throws IOException {
		super(socket);
		this.log = log;
		this.attacker = a;
	}

	@Override
	public void createSession() throws IOException {
		while(!socket.isClosed()) {
			Exception exception = null;
			APIMessage response = null;
			APIMessage request = null;
			boolean doNotLog = false;
			
			request = APIMessage.get(socketIn);
			if (request.getMethod().equals(APIMethod.ATTACKER_QUERY_STATE) ||
					request.getMethod().equals(APIMethod.ATTACKER_START_SYNC) ||
					request.getMethod().equals(APIMethod.ATTACKER_SYNC_CYCLE) ||
					request.getMethod().equals(APIMethod.ATTACKER_SYNC_DELTA) ||
					request.getMethod().equals(APIMethod.ATTACKER_STOP_SYNC)) { doNotLog = true; }
			if (!doNotLog) log.debug("Incoming message " + request.toString());
			try {
				response = handleRequest(request);
			} catch (Exception e) {
				response = new APIMessage(e.getMessage(), APIMethod.COORDINATOR_COMMAND_FAIL);
				exception = e;
			} finally {
				if (response == null) {
					exception = new Exception("Response was not initialized. " + request.toString());
					response = new APIMessage(exception.getMessage(), APIMethod.COORDINATOR_COMMAND_FAIL);
				}
				if (!doNotLog) log.debug("Sending message " + response.toString());
				APIMessage.send(response, socketOut);
			}
			if (exception != null) {
				log.warn(exception.getMessage());
				exception.printStackTrace();
			}
			
		}
	}


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

	private APIMessage queryState(APIMessage request) {
		String target = null;
		try { 
			target = attacker.getTargetAddress().toString(); 
		} catch (Exception e) {
			target = ""; 
		}
		String data = attacker.status.toString() + "," + target;
		return okay(data);
	}
	
	
	private APIMessage setTarget(APIMessage request) throws Exception {
		attackerMustBe(AttackerStatus.IDLE);
		
		attacker.setTargetAddress(new HostPort(request.getData()));
		return okay();
	}
	
	private APIMessage startAttack(APIMessage request) throws Exception {
		attackerMustBe(AttackerStatus.IDLE);
		
		Long startTime = Long.valueOf(request.getData());
		attacker.startAttack(new Date(getLocalTime(startTime)));
		return okay();
	}
	
	private APIMessage stopAttack(APIMessage request) throws Exception {
		attackerMustBe(AttackerStatus.ATTACKING);
		
		attacker.stopAttack();
		return okay();
	}
	
	private APIMessage startSync(APIMessage request) throws Exception {
		latencies = new DescriptiveStatistics();
		return okay();
	}
	
	private APIMessage syncCycle(APIMessage request) throws Exception {
		lastSync = new Date().getTime();
		return okay();
	}
	
	private APIMessage syncDelta(APIMessage request) throws Exception {
		long latency = (new Date().getTime() - lastSync) / 2;
		latencies.addValue((double) latency);
		lastSync = new Date().getTime();
		return okay(String.valueOf(latency));
	}
	
	private APIMessage stopSync(APIMessage request) throws Exception {
		double[] values = latencies.getValues();
		double median = (new Median().evaluate(values));
		double stdDev = latencies.getStandardDeviation();
		long serverTime = Long.valueOf(request.getData());

		List<Double> validValues = new ArrayList<Double>();
		for(int i = 0; i < values.length; i++ ) {
			if (Math.abs(values[i] - median) <= stdDev) {
				validValues.add(values[i]);
			} 
		}
		double[] primitiveList = ArrayUtils.toPrimitive((validValues.toArray( new Double[validValues.size()] )));
		long latency = (long) new Mean().evaluate(primitiveList);
		setClockOffset(serverTime, latency);
		
		log.debug("Ping : MEDIAN=" + median + " STDDEV=" + stdDev + " LATENCY=" + latency);
		
		return okay();
	}
	private APIMessage okay(String message) {
		return new APIMessage(message, APIMethod.COORDINATOR_COMMAND_OK);
	}
	private APIMessage okay() {
		return okay("");
	}
	
	
	private void setClockOffset(long serverTime, long latency) {
		clockOffset = new Date().getTime() - (serverTime + latency);
	}
	
	private long getLocalTime(long serverTime) {
		log.debug("Time from server is " + new Date(serverTime) + " Offset time is " + new Date(serverTime + clockOffset));
		return serverTime + clockOffset;
	}
	
	private void attackerMustBe(AttackerStatus as) throws Exception {
		if (!attacker.getStatus().equals(as)) {
			throw new Exception("Attacker is " + attacker.getStatus().toString() + ", must be " + as.toString());
		}
	}

}
