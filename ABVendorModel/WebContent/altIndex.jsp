<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>MLMS Team Side Door</title>
<style>
 .notes {
 	text-align:left;
 	color:blue;
 	font-size: 75%;
 }
 .notes-head{
 	text-align:left;
 	color:blue;
 	font-size: 85%;
 	font-style: bold;
 }
</style>
</head>
<body>

<form action="/Saba/index.jsp" method="post">
<table width="50%">
<tr><th>User Data</th>

</tr>

<tr>
	<td>	User Name</td>
	<td><input type="text" name="username" size="20" value="sjmeyer"></td>
</tr>
<tr>
	<td>Password</td>
	<td><input type="text" name="password" size="20" value="welcome"></td>
</tr>
<tr>
  <td>	First Name</td>
  <td><input type="text" name="fname" size="20" value="fname"></td>
 </tr>
 <tr>
   <td> Last Name </td>
   <td> <input type="text" name="lname" size="20" value="lname"></td>
 </tr>
 <tr>
 <td><p class="notes-head">Notes:</p></td><td><p class="notes"> A learner can have either Assister role or Training Access (base access for Agent Broker)</p></td>
 </tr >
 <tr><td></td><td><p class="notes">Assigning the agent broker role indicates FFM access has been granted<p class="notes"></td></tr>
 <tr>
 <tr>
 	<th>CMS Roles</th>
 </tr>
 
 	<td>Agent/Broker </td>
 	<td> <input type="checkbox" name="ffmTraining" value="ffm_agent_broker" size="20"></td>
 </tr>
 <tr>
 	<td>Assister   </td>
 	<td>   <input type="radio" name="requiredRole" value="ffm_assister" size="20"></td>
</tr>
 <tr>
 	<td>Training Access </td>
 	<td>   <input type="radio" name="requiredRole" value="ffm_training_access" size="20"></td>
</tr>
<tr>
 	<th>Admin Roles</th>
 </tr>
<tr>
 	<td>MLMS Business Owner </td>
 	<td>   <input type="radio" name="requiredRole" value="mlms_business_owner" size="20"></td>
</tr>
<tr>
 	<td>MLMS Admin </td>
 	<td>   <input type="radio" name="requiredRole" value="mlms_admin" size="20"></td>
</tr>
<tr>
<td>Set Certificate Deep Link </td><td><input type="checkbox" name="MLMSDeepLink" value="Certificate" size="20"></td>
</tr>
<tr>
<td>Set Agent Training complete </td><td><input type="checkbox" name="FFMTraining" value="FFMTraining" size="20"></td>
</tr>
</table>	       
<table>	 
<tr>
   <th> is dev </th><td> <input type="checkbox" name="isDev" value="true" size="20" checked></td>


  <th> Submit Request </th><td><input type="submit" value="submit"/></td>
</tr>
	
</table>

</form>

</body>
</html>