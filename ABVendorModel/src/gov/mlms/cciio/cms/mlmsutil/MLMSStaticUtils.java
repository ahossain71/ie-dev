package gov.mlms.cciio.cms.mlmsutil;

import java.util.Random;

public class MLMSStaticUtils {
	static final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	/**
	 * 
	 * @return
	 */
	static public String getRandomString(){
		Random r = new Random();
		 
		return String.valueOf(alphabet.charAt(r.nextInt(alphabet.length())));
	}
	static public int getRandomNumber(int max){
		Random r = new Random();
		return r.nextInt(max);
	}
}
