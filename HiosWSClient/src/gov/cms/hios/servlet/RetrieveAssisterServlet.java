package gov.cms.hios.servlet;

import gov.cms.hios.AssisterResponse;
import gov.cms.hios.service.HiosAssisterService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class RetrieveAssisterServlet
 */
@WebServlet("/retrieveAssisterServlet")
public class RetrieveAssisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RetrieveAssisterServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String customer = request.getParameter("cutomer");
		String assisterIdParam = request.getParameter("assisterId");
		String[] assisterIdArray = assisterIdParam.split(",");
		List<String> assisterIds = new ArrayList<String>();
		for(String assisterId : assisterIdArray){
			assisterIds.add(assisterId.trim());
		}
		
		HiosAssisterService hiosAssisterService = new HiosAssisterService();
		AssisterResponse assisterResponse = hiosAssisterService.retrieveAssister(assisterIds, customer);
		
		request.setAttribute("assisterResponse", assisterResponse);
		
		getServletContext().getRequestDispatcher("retrieveAssisterResult.jsp").forward(request, response);
	}

}
