package org.mlms.cciio.ibm.cms.training.dao;

import java.util.List;

import org.mlms.cciio.ibm.cms.retrievetrainingcompletionstatustype.CertificationType;

public class DaoTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TrainingCertificationDao dao = new TrainingCertificationDao();
		try{
			List<CertificationType> certs = dao.getCertificationInfo("emplo000000000001121");
			for(CertificationType cert : certs){
				System.out.println("Cert Id: " + cert.getCertificationId());
				System.out.println("Cert Name: " + cert.getCertificationName());
				System.out.println("Cert Status " + cert.getCertificationStatus());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
