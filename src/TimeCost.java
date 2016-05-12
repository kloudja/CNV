import java.math.BigInteger;
import java.util.Date;

public class TimeCost {

	private Date time; // Tempo que começou o pedido
	private int cost; // Custo
	
	public TimeCost(Date time, int cost) {
		this.time = time;
		this.cost = cost;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}
	
	
}
