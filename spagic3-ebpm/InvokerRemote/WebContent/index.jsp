<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Spagic Embedded Tester</title>
<link rel='stylesheet' type='text/css' href='css/content.css'/>
</head>
<script type="text/javascript" src="js/prototype.js">
</script>
<script type="text/javascript">

	function sendRequest(){
		new Ajax.Request('proxy',
				  {
				    method:'post',
				    parameters: $('myForm').serialize(true),
				    onSuccess: function(transport){
				      var response = transport.responseText || "no response text";
				      $('response').value=response;
				    },
				    onFailure: function(){ alert('Something went wrong...') }
				  });

	}
</script>
<body>
<form id="myForm">

<table>
  <thead>
  </thead>
  <tbody>
   <tr>
   		<td>
   			Id Servizio Spagic
   		</td>	
   		<td>
   			<input type="text" id="serviceId" name="serviceId"/>
   		</td>
   </tr>
   <tr>
  		<td>
  			Request
  		</td>
  		<td>
  			Response
  		</td>
  	</tr>
  	<tr>
    	<td>
  			<textarea id="request" name="request" style="width:400px;height:400px">
  			</textarea>
    	</td>
    	<td>
  			<textarea id="response" name="response" style="width:400px;height:400px">
  			</textarea>
    	</td>
  	</tr>
  	<tr>
    	<td colspan=2>
  			<input type="button" value="Test Service" onClick="sendRequest();"/>
    	</td>
  	</tr>
  </tbody>
</table>
</form>
</body>
</html>