/**
 * 
 */
package gov.cms.hios.service;

import gov.cms.hios.AssisterRequest;
import gov.cms.hios.AssisterResponse;
import gov.cms.hios.service.exception.RetrieveAssisterException;
import gov.cms.hios.soa.assister.RetrieveAssisterPortType;
import gov.cms.hios_api_types.Assister;
import gov.cms.hios_api_types.AssisterCertificationStatusType;
import gov.cms.hios_api_types.ContactType;
import gov.cms.hios_api_types.GranteeOrganization;
import gov.cms.hios_api_types.OrganizationAddress;
import gov.cms.hios_api_types.ResponseHeaderType;
import gov.cms.hios_api_types.ReturnCodeType;
import gov.cms.hios_api_types.ServiceError;

import java.util.List;

import javax.xml.ws.Service;

public class HiosAssisterService {
	
	public AssisterResponse retrieveAssister(List<String> assisterIds, String customer){
		String methodName = "retrieveAssister";
		StringBuilder sb = new StringBuilder(HiosAssisterService.class.getName());
		
		sb.append(" ");
		sb.append(methodName);
		sb.append("\nCustomer: '");
		sb.append((customer == null)?"<null>":customer);
		sb.append("'\nAssister IDs: ");
		if (assisterIds == null) {
			sb.append("'<null>'");
		} else {
			sb.append(assisterIds.size());
			for (String tempStr:assisterIds) {
				sb.append("\n");
				sb.append(tempStr);
			}
		}
		// TODO: Convert to logger later
		System.out.println(sb.toString());
		
		AssisterRequest request = new AssisterRequest();
		request.setConsumer(customer);
		request.getAssisterID().addAll(assisterIds);
		AssisterResponse response = null;
		
		try {
			Service retrieveAssisterService = ServiceFactory.getAssisterService();
			RetrieveAssisterPortType proxy = retrieveAssisterService.getPort(RetrieveAssisterPortType.class);
			response = proxy.retrieveAssisterOp(request);
			// TODO: Convert to logger later
			System.out.println("AssisterResponse: " + convertAssisterResponseToString(response));
		} catch (RetrieveAssisterException e) {
			System.err.println(HiosAssisterService.class.getName() + ": Error invoking Hios Retrieve Assister WS.");
			e.printStackTrace();
			// Construct a response object to display on UI
			response = new AssisterResponse();
			ResponseHeaderType header = new ResponseHeaderType();
			header.setReturnCode(ReturnCodeType.ERROR);
			ServiceError error = new ServiceError();
			error.setErrorCode("-1");
			error.setErrorMessage(e.toString());
			header.setServiceError(error);
			response.setResponseHeader(header);
			response.setAssister(null);
		}
		
		return response;
	}

	public static String convertAssisterResponseToString(AssisterResponse response) {
		ResponseHeaderType header = null;
		Assister assister = null;
		StringBuilder sb = new StringBuilder();
		
		if (response == null) {
			sb.append("'<null>'");
			return sb.toString();
		}
		if ((header = response.getResponseHeader()) == null) {
			sb.append("Header: <null>");
		} else {
			sb.append("Header: \n\tReturn Code: ");
			sb.append(header.getReturnCode());
			sb.append("\n\tError: ");
			ServiceError error = header.getServiceError();
			if (error == null) {
				sb.append("<null>");
			} else {
				sb.append("\n\t\tError Code: ");
				sb.append(error.getErrorCode());
				sb.append("\n\t\tError Msg.: ");
				sb.append(error.getErrorMessage());
			}
		}
		if ((assister = response.getAssister()) == null) {
			sb.append("\nAssister: <null>");
		} else {
			sb.append("\nAssister: \n\tID: ");
			sb.append(assister.getAssisterID());
			sb.append("\n\tAssister Contact: ");
			ContactType contact = assister.getAssisterContact();
			if (contact == null) {
				sb.append("<null>");
			} else {
				sb.append("\n\t\tFirst Name: ");
				sb.append(contact.getFirstName());
				sb.append("\n\t\tLast Name: ");
				sb.append(contact.getLastName());
				sb.append("\n\t\tEmail: ");
				sb.append(contact.getContactEmail());
				sb.append("\n\t\tPhone: ");
				sb.append(contact.getContactPhoneNumber());
				sb.append("\n\t\tExtension: ");
				sb.append(contact.getContactExtension());
			}
			sb.append("\n\tStatus: ");
			AssisterCertificationStatusType status = assister.getCertificationStatus();
			sb.append((status==null)?"<null>":status.value());
			sb.append("\n\tCert. Date: ");
			sb.append((assister.getCertificationDate()==null)?"<null>":assister.getCertificationDate().toString());
			sb.append("\n\tGrantee Org.: ");
			GranteeOrganization org = assister.getGrantee().getGranteeOrganization();
			if (org == null) {
				sb.append("<null>");
			} else {
				sb.append(org.getName());
				sb.append("\n\tGrantee Add.: ");
				OrganizationAddress address = org.getAddress();
				if (address == null) {
					sb.append("<null>");
				} else {
					sb.append("\n\t\tAddr 1: ");
					sb.append(address.getAddressLine1());
					sb.append("\n\t\tAddr 2: ");
					sb.append(address.getAddressLine2());
					sb.append("\n\t\tCity: ");
					sb.append(address.getCity());
					sb.append("\n\t\tState: ");
					sb.append(address.getState());
					sb.append("\n\t\tZip: ");
					sb.append(address.getZip());
					sb.append("\n\t\tZip + 4: ");
					sb.append(address.getZipPlus4());
				}
			}
			
		}
		
		return sb.toString();
	}

	public static String convertAssisterResponseToHTML(AssisterResponse response) {
		ResponseHeaderType header = null;
		Assister assister = null;
		StringBuilder sb = new StringBuilder("<table class=\"AssisterResponse\">");
		
		if (response == null) {
			sb.append("\n<tr><td>null</td></tr>\n</table>");
			return sb.toString();
		}
		sb.append("\n<tr><td colspan=\"3\">Header:</td></tr>");
		if ((header = response.getResponseHeader()) != null) {
			sb.append("\n<tr><td></td><td>Return Code:</td><td>");
			sb.append(header.getReturnCode());
			sb.append("</td></tr>");
			sb.append("\n<tr><td></td><td>Error:</td><td>");
			ServiceError error = header.getServiceError();
			sb.append("</td></tr>");
			if (error != null) {
				sb.append("\n<tr><td colspan=\"2\"></td><td>Error Code:</td><td>");
				sb.append(error.getErrorCode());
				sb.append("</td></tr>");
				sb.append("\n<tr><td colspan=\"2\"></td><td>Error Msg.:</td><td>");
				sb.append(error.getErrorMessage());
				sb.append("</td></tr>");
			}
		}

		sb.append("\n<tr><td colspan=\"3\">Assister:</td></tr>");
		if ((assister = response.getAssister()) != null) {
			sb.append("\n<tr><td></td><td>ID:</td><td>");
			sb.append(assister.getAssisterID());
			sb.append("</td></tr>");
			sb.append("\n<tr><td></td><td>Contact:</td></tr>");
			ContactType contact = assister.getAssisterContact();
			if (contact != null) {
				sb.append("\n<tr><td colspan=\"2\"></td><td>First Name:</td><td>");
				sb.append(contact.getFirstName());
				sb.append("</td></tr>");
				sb.append("\n<tr><td colspan=\"2\"></td><td>Last Name: ");
				sb.append(contact.getLastName());
				sb.append("</td></tr>");
				sb.append("\n<tr><td colspan=\"2\"></td><td>Email: ");
				sb.append(contact.getContactEmail());
				sb.append("</td></tr>");
				sb.append("\n<tr><td colspan=\"2\"></td><td>Phone: ");
				sb.append(contact.getContactPhoneNumber());
				sb.append("</td></tr>");
				sb.append("\n<tr><td colspan=\"2\"></td><td>Extension: ");
				sb.append(contact.getContactExtension());
				sb.append("</td></tr>");
			}
			sb.append("\n<tr><td></td><td>Status:</td><td>");
			AssisterCertificationStatusType status = assister.getCertificationStatus();
			sb.append((status==null)?"":status.value());
			sb.append("</td></tr>");
			sb.append("\n<tr><td></td><td>Cert. Date:</td><td>");
			sb.append((assister.getCertificationDate()==null)?"":assister.getCertificationDate().toString());
			sb.append("</td></tr>");
			sb.append("\n<tr><td></td><td>Grantee Org.:</td></tr>");
			GranteeOrganization org = assister.getGrantee().getGranteeOrganization();
			if (org != null) {
				sb.append("\n<tr><td></td><td>Name:</td><td>");
				sb.append(org.getName());
				sb.append("</td></tr>");
				sb.append("\n<tr><td></td><td>Address:</td></tr>");
				OrganizationAddress address = org.getAddress();
				if (address != null) {
					sb.append("\n<tr><td colspan=\"2\"></td><td>Addr 1:</td><td>");
					sb.append(address.getAddressLine1());
					sb.append("</td></tr>");
					sb.append("\n<tr><td colspan=\"2\"></td><td>Addr 2:</td><td>");
					sb.append(address.getAddressLine2());
					sb.append("</td></tr>");
					sb.append("\n<tr><td colspan=\"2\"></td><td>City:</td><td>");
					sb.append(address.getCity());
					sb.append("</td></tr>");
					sb.append("\n<tr><td colspan=\"2\"></td><td>State:</td><td>");
					sb.append(address.getState());
					sb.append("</td></tr>");
					sb.append("\n<tr><td colspan=\"2\"></td><td>Zip:</td><td>");
					sb.append(address.getZip());
					sb.append("</td></tr>");
					sb.append("\n<tr><td colspan=\"2\"></td><td>Zip + 4:</td><td>");
					sb.append(address.getZipPlus4());
					sb.append("</td></tr>");
				}
			}
			
		}
		sb.append("</table>");
		
		return sb.toString();
	}
}
