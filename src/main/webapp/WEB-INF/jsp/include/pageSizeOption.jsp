<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<fmt:bundle basename="${actionBean.bundleName}">
  <fmt:message key="label.pageSize" />
  <stripes:select name="pageSize">
    <stripes:options-collection collection="${actionBean.pageSizes}" label="label" value="value" />
  </stripes:select>
</fmt:bundle>
