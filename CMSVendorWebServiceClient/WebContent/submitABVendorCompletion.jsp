<!DOCTYPE HTML><%@page language="java"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
<title>submitABVendorCompletion</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body>


	<h2>Submit External Training Vendor COmpletion</h2>

	<form action="MLMSABVendorCompletionServlet">
		<table>
			<tr>
				<td>Please enter GUID:</td>
				<td><input type="text" name="guid" size="30px" />
				</td>
			</tr>
			<tr>
				<td>Please select Vendor Name:</td>
				<td><select name="vendorName">
						<option value="AHIP" selected>AHIP</option>
						<option value="NAHU">NAHU</option>
						<option value="LITMOS">NAHU</option>
				</select></td>
				</tr>
				<tr>
				<td>Please select Curriculum:</td>
				<td><select name="curriculumCode">
						<option value="I" selected>Individual Marketplace</option>
						<option value="J">Individual Marketplace Refresher</option>
						<option value="S">SHOP Marketplace</option>
				</select></td>
			</tr>
			<tr>
				<td>Please select Curriculum Year:</td>
				<td><select name="curriculumYear">
						<option value="2019">2019</option>
						<option value="2018" selected>2018</option>
						<option value="2017" >2017</option>
						<option value="2016">2016</option>
						
				</select></td>
			</tr>
			<tr>
				<td>Please select Curriculum Language:</td>
				<td><select name="curriculumLanguage">
						<option value="English" selected>English</option>
						
						
				</select></td>
			</tr>
		</table>



		<input type="submit" value="Submit" /> <input type="reset"
			value="Reset" />
	</form>

</body>
</html>