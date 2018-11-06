package gov.mlms.cciio.cms.fileProcess.ejb.schedule;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ShopXMLTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		StringBuilder fileName = new StringBuilder("ABINF1.MLM.D")
		.append(new SimpleDateFormat("ddMMyy").format(new Date()))
		.append(".T")
		.append(new SimpleDateFormat("HHmmssSSS").format(new Timestamp(System.currentTimeMillis())))
		.append(".T");
		System.out.println(fileName);
	}

}
