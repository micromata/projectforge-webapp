/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.contact;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.projectforge.xml.stream.AliasMap;
import org.projectforge.xml.stream.XmlObjectReader;
import org.projectforge.xml.stream.XmlObjectWriter;

/**
 * @author Werner Feder (werner.feder@t-online.de)
 *
 */
public class XmlConverter<T>
{
  private static final String ENCLOSING_ENTITY ="values";

  private final T value;
  public XmlConverter(final T value) { this.value = value; }

  public List<T> readValues(final String valuesAsXml)
  {
    if (StringUtils.isBlank(valuesAsXml) == true) {
      return null;
    }
    final XmlObjectReader reader = new XmlObjectReader();
    final AliasMap aliasMap = new AliasMap();
    aliasMap.put(List.class, ENCLOSING_ENTITY);
    reader.setAliasMap(aliasMap).initialize(this.value.getClass());;
    @SuppressWarnings("unchecked")
    final List<T> list = (List<T>) reader.read(valuesAsXml);
    return list;
  }

  public String getValuesAsXml(final T... values)
  {
    if (values == null)
      return "";
    String xml =  "<" + ENCLOSING_ENTITY + ">";
    for (final T value : values) {
      xml += XmlObjectWriter.writeAsXml(value);
    }
    xml += "</" + ENCLOSING_ENTITY + ">";
    return xml;
  }
}
