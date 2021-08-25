 <%@ taglib prefix = "c" uri="http://java.sun.com/jsp/jstl/core" %>
 <%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
 <%@ page import="java.sql.Types"%>
 <%@ page import="ServletRealization.DB_Service"%>
<!DOCTYPE html>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<link rel="stylesheet" href="css/style.css" type="text/css">
	<c:choose>
		<c:when test="${DB_Service.isTB_DEPARTMENTS(table)}">
			<c:if test="${not empty add}">
				<c:set var="title" scope="request" value="Add new department"/>
			</c:if>
			<c:if test="${not empty change}">
				<c:set var="title" scope="request" value="Cahge department"/>
			</c:if>
		</c:when>				
		<c:when test="${DB_Service.isTB_WORKERS(table)}">
			<c:if test="${not empty add}">
				<c:set var="title" scope="request" value="Add new worker"/>
			</c:if>
			<c:if test="${not empty change}">
				<c:set var="title" scope="request" value="Change worker"/>
			</c:if>
		</c:when>
	</c:choose>
	<c:if test="${not empty add}">
		<c:set var="submitName" scope="page" value="add"/>
	</c:if>
	<c:if test="${not empty change}">
		<c:set var="submitName" scope="page" value="change"/>
	</c:if>
	<title>${title}</title>
</head>
<body>
<h1>${title}</h1>
<form method="post" action="/TestTask/fields">
	<table class="invisible">
		<c:forEach var="field" items="${fields}">
			<c:set var="parameters" value=""/>
			<c:set var="parametersPattern" value=""/>
			<c:set var="parametersValue" value=""/>
			
			<c:if test="${field.DISABLED}">
				<c:set var="parameters" scope="page" value="${parameters} disabled "/>
			</c:if>
			<c:if test="${not field.NULLABLE and not field.DISABLED}">
				<c:set var="parameters" scope="page" value="${parameters} required "/>
			</c:if>
			<c:choose>
				<c:when test="${field.isINTEGER()}">
					<c:set var="parameters" scope="page" value="${parameters} type=number min=0 step=1 "/>
					<c:if test="${empty field.value and field.COLUMN ne 'id'}">
						<c:set var="parametersValue" value="0"/>
					</c:if>
				</c:when>				
				<c:when test="${field.isVARCHAR()}">
					<c:choose>
						<c:when test="${field.COLUMN eq 'mail'}">
							<c:set var="parameters" value="${parameters} type=email "/>
							<c:set var="parametersPattern" value="(\w+@\w+\.\w+)"/>
						</c:when>
						<c:when test="${field.COLUMN eq 'department'}">
							<c:set var="parameters" value="${parameters} type=text list=departmentsList "/>
							<c:if test="${empty field.value}">
								<c:set var="parametersValue" value="${department}" />
							</c:if>
						</c:when>
						<c:otherwise>
							<c:set var="parameters" value="${parameters} type=text "/>
						</c:otherwise>
					</c:choose>
					<c:set var="parameters" value="${parameters} maxlength=${field.COLUMN_SIZE} "/>
				</c:when>
				<c:when test="${field.isDATE()}">
					<c:set var="parameters" value="${parameters} type=date " />
					<c:if test="${empty field.value}">
						<jsp:useBean id="now" class="java.util.Date" />
						<fmt:formatDate value="${now}" pattern="yyyy-MM-dd" var="nowFormated" />
						<c:set var="parametersValue" value="${nowFormated}" />
					</c:if>
				</c:when>
			</c:choose>
			<c:set var="parameters" value="${parameters} name=${field.COLUMN} id=${field.COLUMN} "/>
			<c:if test="${not empty field.value}">
				<c:set var="parametersValue" value="${field.value}" />
			</c:if>
			<c:if test = "${not field.isValid()}" >
				<c:set var="parametersValue" value="${field.invalidValue}" />
				<c:if test = "${empty parametersPattern}" >
					<c:set var="parametersPattern" value="(.*)" />
				</c:if>
				<c:set var="parametersPattern" value="^(?!${field.invalidValue}$)${parametersPattern}" />
			</c:if>
			<c:if test = "${not empty parametersPattern}" >
				<c:set var="parametersPattern" value="pattern=&quot;${parametersPattern}&quot; " />
			</c:if>
			<c:if test = "${not empty parametersValue}" >
				<c:set var="parametersValue" value="value=&quot;${parametersValue}&quot; " />
			</c:if>
			<tr class="invisible">
				<td class="invisible"><label for=${field.COLUMN}>${field.COLUMN}</label></td>
				<td class="invisible">
					<input ${parameters} ${parametersPattern} ${parametersValue}/>
					<c:if test = "${not field.isValid()}" >
						</br>
						<p class="error" >${field.invalidMessage}</p>
					</c:if>
				</td>
			</tr>
			<c:if test="${field.COLUMN eq 'department'}">
				<datalist id="departmentsList">
					<c:forEach var="departmentName" items="${departmentsNames}">
						<option value="${departmentName}" >${departmentName}</option>
					</c:forEach>
				</datalist>
			</c:if>
		</c:forEach>
	</table>

	<input type="hidden" name="table" value="${table}"/>
	<input type="hidden" name="selected_id" value="${selected_id}"/>
	<c:if test="${not empty department}">
		<input type="hidden" name="department" value="${department}" />
	</c:if>

	<input type="submit" name="${submitName}" value="Submit"/>
	<input type="submit" name="cancel" value="Cancel" form="tables"/>
</form>
<form id="tables" method="get" action="/TestTask/table">
	<input type="hidden" name="selected_id" value="${selected_id}"/>
	<input type="hidden" name="table" value="${table}"/>
	<c:if test="${not empty department}">
		<input type="hidden" name="department" value="${department}" />
	</c:if>
</form>
</body>
</html>