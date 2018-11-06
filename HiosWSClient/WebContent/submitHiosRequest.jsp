<!DOCTYPE HTML>
<%@page language="java"	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
	<head>
		<title>Submit Hios Request</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	</head>
	<body>
		<h2>Submit Hios Request</h2>

		<form action="retrieveAssisterServlet">
			<table>
				<tr>
					<td>Please enter Customer:</td>
					<td><input type="text" name="customer" size="30px" /></td>
				</tr>
				<tr>
					<td>Please enter Assister IDs, separated by comma:</td>
					<td><input type="text" name="assisterId" size="300px" /></td>
				</tr>
			</table>

			<input type="submit" value="Submit" /> <input type="reset" value="Reset" />
		</form>
	</body>
</html>
