package gov.cms.cciio.common.auth;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  Custom class created by ****arosenthal*** to do encryption for submission to the custom loginModule.
 *  in the custom loginModule, this class is used to do the decryption to match the password with the username.
 *
 * 	uses a Gregorian Calendar, date xxxxxxxx as mangler
 * @author - arosenthal
 */
/*
 * 2010-07-21   Feilung Wong    Copied from 5.3 code: mil.army.dls.auth.Encrypter.java
 *                              the LMS and interface should point to this single class instead of keeping two copies separately
 */
public class Encrypter
{

    /** Creating className instance */
    private static final String className = Encrypter.class.getName();
    /** Creating logger */
    private static final Logger logger = Logger.getLogger(className);  
	/**
	 * mangleChar (char, key)
	 *
	 * @param src	source character
	 * @param Key	character to use to shift this
	 * @return	new char
	 */
	private static String mangleChar(char src, char Key)
	{
		String hex_num = "";

		// Calculates ASCII values
		int code = (int) src;
		int key_num = (int) Key;

		int mangled = code + key_num;
		if (mangled > 127)
			mangled = mangled - 127;

		// Calculates Hex from decimal
		hex_num = Integer.toHexString(mangled);

		if (hex_num.length() == 1)
			hex_num = "0" + hex_num;
		return hex_num.toUpperCase();
	}

	/**
	 * mangleString (string, string key)
	 *
	 * @param strDat	string to mangle
	 * @param key	key to use for shifting
	 * @return	mangled string
	 */
	private static String mangleString(String strDat, String key)
	{
		String temp = "";
		int j = 0;
		for (int i = 0; i < strDat.length(); i++)
		{
			temp = temp + mangleChar(strDat.charAt(i), key.charAt(j));
			j++;
			if (j >= key.length())
				j = 0;
		}
		return temp;
	}

	/**
	 * unMangleChar (char, key)
 	 *
 	 * @param src	source character
	 * @param Key	character to use to shift this
	 * @return	new char
	 *
	*/
	private static char unMangleChar(String hex_num, char Key)
	{
		int mangled = Integer.parseInt(hex_num,16);
		int key_num = (int) Key;
		int code = mangled - key_num;
		if (code < 33)
			code = code + 127;

		char letter = (char) code;

		return letter;
	}

	/**
	 * unMangleString (string, string key)
	 *
	 * @param strDat string to unmangle
	 * @param key	key to use for shifting
	 * @return	unmangled string
	 */
	private static String unMangleString(String strDat, String key)
	{
		StringBuilder temp = new StringBuilder();
		int j = 0;
		for (int i = 0; i < strDat.length()-1;)
		{
			String hex_num = strDat.substring(i,i+2);
			temp = temp.append(unMangleChar(hex_num, key.charAt(j)));
			j++;
			if (j >= key.length())
				j = 0;
			i = i + 2;
		}
		return temp.toString();
	}

	/**
	 * encrypt (string)
	 *
	 * @param data	String to encrypt
	 * @return	encrypted String, using default key
	 */
	public static String encrypt(String data)
	{
		return encrypt(data,getMangler());
	}

	/**
	 * encrypt (string, key)
	 * @param data	String to encrypt
	 * @param key	String to use for encryption
	 * @return	encrypted string
	 */
	public static String encrypt(String data, String key)
	{
		return mangleString(data,key);
	}

	/**
	 * decrypt (string)
	 * @param data	String to decrypt
	 * @return	decrypted string, using default key
	 */
	public static String decrypt(String data)
	{
		return unMangleString(data,getMangler());
	}


	/**
	 * decrypt (string,int)
	 * @param data	String to decrypt,  Integer to offset the date by.
	 * @return	decrypted string, using default key
	 */
	public static String decrypt(String data, int i)
	{
		return unMangleString(data,getMangler(i));
	}

	/**
	 * decrypt (string, key)
	 *
	 * @param data	String to decrypt
	 * @param key	Key to use for decrypting the string
	 * @return	decrypted String
	 */
	public static String decrypt(String data, String key)
	{
		return unMangleString(data,key);
	}

	/**
	 * getMangler
	  *
	  * 	creates a new GregorianCalendar based on this computer's time, offset to
	  *		the America/Los_Angeles locale
	  *
	  *	@return String key to use to mangle the string
	 */
	public static String getMangler(int offset)
	{
		//create the LA timezone, for common ground (PST adjusted for daylight savings)
		TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");
		GregorianCalendar calendar = new GregorianCalendar(timeZone, Locale.US);

		// offset is used during decryption when we mangle down to the minute.
		//  offset will allow the logon module to decrypt using previous minute.
		//  this prevents some delay causing the clock to rollover to the next minute from causing a login failure.
		calendar.add(Calendar.MINUTE,offset);

		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		//int hour = calendar.get(Calendar.HOUR_OF_DAY);
		//int min = (calendar.get(Calendar.MINUTE));
		//String mangler=""+day+hour+min;
		String mangler = ""+year+month+day;  //  login string will be good for this day only
		return mangler;
	}


	public static String getMangler()
	{
		return getMangler(0);
	}

	/** for testing only */
	public static void main(String[] args)
	{
		//print string to be encrypted
		logger.log(Level.FINER, "String to be encrypted: " + args[0]);

		//do encryption and decryption
		String temp =  encrypt(args[0]);
		logger.log(Level.FINER, "Encrypted: " + temp);
		if (args[1].equals("s"))
			try {
				Thread.sleep(45000);
			} catch (InterruptedException e) {
				// do nothing
			}
		String temp2 = decrypt(temp);
		logger.log(Level.FINER, "Decrypted: " + temp2);
		if (!args[0].equals(temp2))
		{
			temp2 = decrypt(temp,-1);
			logger.log(Level.FINER, "Offset Decrypted: " + temp2);
		}
	}
}
