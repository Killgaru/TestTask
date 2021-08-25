 <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
 <%@ page import="ServletRealization.DB_Service"%>
<!DOCTYPE html>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<link rel="stylesheet" href="css/style.css" type="text/css">
	<c:choose>
		<c:when test="${DB_Service.isTB_DEPARTMENTS(table)}">
			<c:set var="title" scope="request" value="Departments"/>
			<c:set var= "listParameters" scope= "page" value = "formaction=/TestTask/table" />
		</c:when>				
		<c:when test="${DB_Service.isTB_WORKERS(table)}">
			<c:set var="title" scope="request" value="Workers from department ${department}"/>
			<c:set var= "listParameters" scope= "page" value = "form=listForm" />
		</c:when>
	</c:choose>
	<title>${title}</title>
</head>
<body>
<h1>${title}</h1>
<form action="/TestTask/fields" method="get">
	<table>
		<tr>
			<th></th>
			<c:forEach var="columnName" items="${columnNames}">
				<th>
					${columnName}
				</th>
			</c:forEach>
		</tr>
		<c:set var="zebra" scope="page" value="lightGray"/>
		<c:set var="checkedFlag" scope="page" value="true"/>
		<c:forEach var="row" items="${tableData}">
			<c:if test="${row.getFirst() eq selected_id}">
				<c:set var="checkedFlag" scope="page" value="true"/>
			</c:if>
			<tr class="${zebra}">
				<td>
					<label for=${row.getFirst()}>
						<input type="radio" id=${row.getFirst()} name="selected_id" value=${row.getFirst()} <c:if test="${checkedFlag==true}"> checked </c:if>/>
					</label>
				</td>
				<c:forEach var="item" items="${row}">
					<td>
						<label for=${row.getFirst()}>
							${item}
						</label>
					</td>
				</c:forEach>
			</tr>
			<c:choose>
				<c:when test="${zebra == 'white'}">
					<c:set var="zebra" scope="page" value="lightGray"/>
				</c:when>				
				<c:when test="${zebra == 'lightGray'}">
					<c:set var="zebra" scope="page" value="white"/>
				</c:when>
			</c:choose>
			<c:set var="checkedFlag" scope="page" value="false"/>
		</c:forEach>
	</table>
	
	<c:if test="${empty tableData}">
		<input type="hidden" name="selected_id" value="${-1}"/>
		<c:set var="disablableControls" scope="page" value="disabled"/>
	</c:if>
	<input type="hidden" name="table" value="${table}"/>
	<c:if test="${DB_Service.isTB_WORKERS(table)}">
		<input type="hidden" name="department" value="${department}"/>
	</c:if>

	<input type="submit" name="add" value="Add" />
	<input type="submit" name="change" value="Change" ${disablableControls} />
	<input type="submit" name="delete" value="Delete" formaction="/TestTask/table" formmethod="post" ${disablableControls} />
	<input type="submit" name="list" value="List" ${listParameters} />
</form>

<c:if test="${DB_Service.isTB_WORKERS(table)}">
	<c:set var= "formParameters" scope= "page" value = "action=/TestTask/table method=get id=listForm" />
	<form action="/TestTask/table" method="get" id="listForm" >
		<input type="hidden" name="table" value="${table}">
		<input type="hidden" name="department" value="${department}">
	</form>
</c:if>

</body>
</html>