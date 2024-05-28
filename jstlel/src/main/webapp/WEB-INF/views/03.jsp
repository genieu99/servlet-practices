<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%> 
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<h1>JSTL Test: forEach</h1>
	
	<c:set var="count" value="${fn:length(list) }" />
	<c:forEach items='${list }' var='vo' varStatus='status'>
		<h4>[${count-status.index}](${status.index}:${status.count}) ${vo.no } : ${vo.name }</h4>
		
	</c:forEach>
</body>
</html>