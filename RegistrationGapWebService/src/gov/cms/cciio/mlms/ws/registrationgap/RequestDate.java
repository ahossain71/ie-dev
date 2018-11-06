package gov.cms.cciio.mlms.ws.registrationgap;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RequestDate {
   private String requestDate;

public String getRequestDate() {
	return requestDate;
}

public void setRequestDate(String requestDate) {
	this.requestDate = requestDate;
}
}
