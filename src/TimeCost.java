import java.math.BigInteger;
import java.util.Date;

public class TimeCost {

	private Date time; // Tempo que começou o pedido
	private long cost; // Custo
	
	public TimeCost(Date time, long cost) {
		this.time = time;
		this.cost = cost;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public long getCost() {
		return cost;
	}

	public void setCost(long cost) {
		this.cost = cost;
	}
	
	
}
