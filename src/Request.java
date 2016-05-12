import java.math.BigInteger;
import java.util.Date;

import com.amazonaws.services.ec2.model.Instance;

public class Request {

	private BigInteger number;
	private Instance instance;
	private Date start;
	private Date end;
	private long duration;
	
	public BigInteger getNumber() {
		return number;
	}
	public void setNumber(BigInteger number) {
		this.number = number;
	}
	public Instance getInstance() {
		return instance;
	}
	public void setInstance(Instance instance) {
		this.instance = instance;
	}
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public Date getEnd() {
		return end;
	}
	public void setEnd(Date end) {
		this.end = end;
		this.duration = (end.getTime() - start.getTime());
	}
	
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	
	
	
	

	
}
