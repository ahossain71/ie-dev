package gov.cms.cciio.mlms.vendor.ab.servlet;

import gov.cms.cciio.mlms.vendor.ab.file.VendorFileTransferService;

import java.io.IOException;
import java.io.PrintWriter;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.logging.*;


/**
 * Servlet implementation class ABVendorFileServiceConfServlet
 */
@WebServlet("/ABVendorFileServiceConfServlet")
public class ABVendorFileServiceConfServlet extends HttpServlet {
	private static Logger logger = Logger.getLogger("ABVendorFileServiceConfServlet.class");
	private static final long serialVersionUID = 1L;
	
	public final static String SCHEDULED_TIME = "FileTransferTime_weekday:hour:minute:second";
	
	@EJB
	private VendorFileTransferService vendorFileTransferBean; 
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ABVendorFileServiceConfServlet() {
        super();
        logger.info("ABVendorFileServiceServlet Init");
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String weekDay = "*";
		String hour = "0";
		String minute = "0";
		String second = "2";
		
		String scheduleTime = System.getProperty(SCHEDULED_TIME);
		if(scheduleTime != null && scheduleTime.length() > 0){
			String [] times = scheduleTime.split(":");
			weekDay = times[0];
			hour = times[1];
			minute = times[2];
			second = times [3];
		}
		
		vendorFileTransferBean.configureSchedule(second, minute, hour, weekDay);
	
	      response.setContentType("text/html;charset=UTF-8");
	      // Allocate a output writer to write the response message into the network socket
	      PrintWriter out = response.getWriter();
	 
	      // Write the response message, in an HTML page
	      try {
	         out.println("<!DOCTYPE html>");
	         out.println("<html><head>");
	         out.println("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
	         out.println("<title>Data Delivery Email Configuration Result</title></head>");
	         out.println("<body>");
	         out.println("<h3>The Configuration for file processing scheduling is completed successfully!</h3>");  
	         out.println("</body>");
	         out.println("</html>");
	      } finally {
	         out.close(); 
	      }
	}

}
