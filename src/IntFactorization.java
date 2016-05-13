import java.math.BigInteger;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;



public class IntFactorization {



	private BigInteger zero = new BigInteger("0");

	private BigInteger one = new BigInteger("1");

	private BigInteger divisor = new BigInteger("2");

	private ArrayList<BigInteger> factors = new ArrayList<BigInteger>();

	public ArrayList<BigInteger> calcPrimeFactors(BigInteger num) {



		if (num.compareTo(one)==0) {

			return factors;

		}



		while(num.remainder(divisor).compareTo(zero)!=0) {

			divisor = divisor.add(one);

		}



		factors.add(divisor);

		return calcPrimeFactors(num.divide(divisor));

	}



	public static void main(String[] args) {


		for(int j = 1 ; j < 1000; j++){
			if(isSemi(BigInteger.valueOf(j))){
				System.out.println("Factoring " + j + "...");
				new IntFactorization().calcPrimeFactors(BigInteger.valueOf(j));
			}
		}
	}



	public ArrayList<BigInteger> getFactors() {

		return factors;

	}

	public static List<BigInteger> primeDecomp(BigInteger a){
		// impossible for values lower than 2
		if(a.compareTo(new BigInteger("2")) < 0){
			return null; 
		}

		//quickly handle even values
		List<BigInteger> result = new ArrayList<BigInteger>();
		while(a.and(BigInteger.ONE).equals(BigInteger.ZERO)){
			a = a.shiftRight(1);
			result.add(new BigInteger("2"));
		}

		//left with odd values
		if(!a.equals(BigInteger.ONE)){
			BigInteger b = BigInteger.valueOf(3);
			while(b.compareTo(a) < 0){
				if(b.isProbablePrime(10)){
					BigInteger[] dr = a.divideAndRemainder(b);
					if(dr[1].equals(BigInteger.ZERO)){
						result.add(b);
						a = dr[0];
					}
				}
				b = b.add(new BigInteger("2"));
			}
			result.add(b); //b will always be prime here...
		}
		return result;
	}

	public static boolean isSemi(BigInteger x){
		List<BigInteger> decomp = primeDecomp(x);
		return decomp != null && decomp.size() == 2;
	}

}