import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;

import com.sun.net.httpserver.HttpExchange;

public class Worker extends Thread{

	private BigInteger number;
	private HttpExchange httpExchange;

	public Worker(BigInteger number,HttpExchange httpExchange) {
		this.number = number;
		this.httpExchange = httpExchange;
	}

	public BigInteger getNumber() {
		return number;
	}

	@Override
	public void run() {
		
		super.run();
		IntFactorization intFactorization = new IntFactorization();

		System.out.println("[WORKER] Recebi o seguinte numero para fatorizar: " + number.toString());

		String[] args = {number.toString()};
		intFactorization.main(args); //TODO Cria overhead por causa da base de dados!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		ArrayList<BigInteger> result = intFactorization.getFactors();


		StringBuilder stringBuilder = new StringBuilder();

		// Conversão do ArrayList para string
		for (BigInteger bigInteger : result) {
			String number = bigInteger.toString();
			stringBuilder.append(number + ",");
		}
		try{
			stringBuilder.deleteCharAt(stringBuilder.length()-1);
		} catch (Exception e){

		}


		String response = stringBuilder.toString();

		System.out.println("[WORKER] RESULTADO DA FATORIZAÇÃO: " + response);

		try {
			httpExchange.sendResponseHeaders(200, response.length());

			OutputStream os = httpExchange.getResponseBody();

			os.write(response.getBytes());
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
