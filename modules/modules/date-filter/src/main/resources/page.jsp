<%-- Archivo: page.jsp --%>

<%@ include file="/init.jsp" %>
<%
FragmentRendererContext fragmentRendererContext = (FragmentRendererContext)request.getAttribute(FragmentRendererContext.class.getName());

FragmentEntryLink fragmentEntryLink = fragmentRendererContext.getFragmentEntryLink();

String fragmentEntryLinkNamespace = fragmentEntryLink.getNamespace() + fragmentEntryLink.getFragmentEntryLinkId();
%>

<form id="<%= fragmentEntryLinkNamespace %>form">
	<label>
		<span><%= LanguageUtil.get(request, "date-period") %></span>
		
		<%-- Nuevo Combobox --%>
		<select name="datePeriod">
			<option value=""><%= LanguageUtil.get(request, "all-dates") %></option>
			<option value="lastMonth"><%= LanguageUtil.get(request, "last-month") %></option>
			<option value="last3Months"><%= LanguageUtil.get(request, "last-3-months") %></option>
			<option value="lastYear"><%= LanguageUtil.get(request, "last-year") %></option>
		</select>
	</label>
</form>

<liferay-frontend:component
	context='<%=
		HashMapBuilder.<String, Object>put(
			"fragmentEntryLinkId", fragmentEntryLink.getFragmentEntryLinkId()
		).put(
			"fragmentEntryLinkNamespace", fragmentEntryLinkNamespace
		).build()
	%>'
	module="DateFilter"
/>