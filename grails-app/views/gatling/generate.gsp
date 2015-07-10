<%--
  Created by IntelliJ IDEA.
  User: rmorgan
  Date: 7/3/15
  Time: 3:35 PM
--%>

<%@ page import="org.springframework.http.HttpMethod" contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<html>
<head>
	<title>Generate An HTTP Scenario Check</title>
</head>

<h1>Generate An HTTP Scenario Check</h1>
<g:form action="generate" >
	<label>Request Path:</label>
	<g:select name="method" from="${HttpMethod.values()}" value="${params.method}"/>
	<br/>
	<label>Base URL:</label>
	<g:textField name="baseUrl" value="${params.baseUrl}" style="width: 50%"/>
	<br/>
	<label>Request Path:</label>
	<g:textField name="requestPath" value="${params.requestPath}" style="width: 50%"/>
	<br/>
	<label>Request Body:</label>
	<br/>
	<g:textArea name="Request Body" value="${params.requestBody}" style="width: 80%; height: 200px;"/>
	<br/>
<g:submitButton name="Generate" value="generate-gatling-request" />
</g:form>

<div style="white-space: pre-wrap;">
	Status: ${responseStatus} <br />
	${"\n" + responseBody?.toString(true)}
	<br />
	Exec Statement: <br />
	${exec}
</div>

</body>
</html>