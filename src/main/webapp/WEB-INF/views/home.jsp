<%@ page import="com.pr.server.*"%>
<%@ page import="com.pr.server.services.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>

<html>
<head>
<title>Home</title>
</head>
<body>
	<%
		ServiceThread server = new ServiceThread();
		server.run();
	%>
	<h1>Hello server</h1>
	<P>The time on the server is ${serverTime}.</P>

</body>
</html>
