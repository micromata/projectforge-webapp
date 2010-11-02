<%@ page pageEncoding="utf-8" session="true" isELIgnored="false"%>
<%@ include file="../base-include.jsp"%>
<fmt:bundle basename="${actionBean.bundleName}">
  <fmt:message key="label.exportFormat" />
  <stripes:select name="exportFormat">
    <stripes:options-collection collection="${actionBean.exportFormats}" label="label" value="value" />
  </stripes:select>
</fmt:bundle>
