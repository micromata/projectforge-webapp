/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.scripting;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.projectforge.core.DefaultBaseDO;


/**
 * Scripts can be stored and executed by authorized users.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Entity
@Indexed
@Table(name = "T_SCRIPT")
public class ScriptDO extends DefaultBaseDO
{
  private static final long serialVersionUID = 7069806875752038860L;

  static {
    invalidHistorizableProperties.add("script");
    invalidHistorizableProperties.add("scriptBackup");
  }

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String name; // 255 not null

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String description; // 4000;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String script; // 100000;

  private String scriptBackup; // 100000;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String parameter1Name;

  private ScriptParameterType parameter1Type;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String parameter2Name;

  private ScriptParameterType parameter2Type;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String parameter3Name;

  private ScriptParameterType parameter3Type;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String parameter4Name;

  private ScriptParameterType parameter4Type;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String parameter5Name;

  private ScriptParameterType parameter5Type;

  @Column(length = 255, nullable = false)
  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Please note: script is not historizable. Therefore there is now history of scripts.
   * @return
   */
  @Column(length = 100000)
  public String getScript()
  {
    return script;
  }

  public void setScript(String script)
  {
    this.script = script;
  }

  /**
   * Instead of historizing the script the last version of the script after changing it will stored in this field.
   * @return
   */
  @Column(length = 100000)
  public String getScriptBackup()
  {
    return scriptBackup;
  }

  public void setScriptBackup(String scriptBackup)
  {
    this.scriptBackup = scriptBackup;
  }

  @Column(length = 4000)
  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  @Column(length = 100)
  public String getParameter1Name()
  {
    return parameter1Name;
  }

  public void setParameter1Name(String parameter1Name)
  {
    this.parameter1Name = parameter1Name;
  }

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  public ScriptParameterType getParameter1Type()
  {
    return parameter1Type;
  }

  public void setParameter1Type(ScriptParameterType parameter1Type)
  {
    this.parameter1Type = parameter1Type;
  }

  @Column(length = 100)
  public String getParameter2Name()
  {
    return parameter2Name;
  }

  public void setParameter2Name(String parameter2Name)
  {
    this.parameter2Name = parameter2Name;
  }

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  public ScriptParameterType getParameter2Type()
  {
    return parameter2Type;
  }

  public void setParameter2Type(ScriptParameterType parameter2Type)
  {
    this.parameter2Type = parameter2Type;
  }

  @Column(length = 100)
  public String getParameter3Name()
  {
    return parameter3Name;
  }

  public void setParameter3Name(String parameter3Name)
  {
    this.parameter3Name = parameter3Name;
  }

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  public ScriptParameterType getParameter3Type()
  {
    return parameter3Type;
  }

  public void setParameter3Type(ScriptParameterType parameter3Type)
  {
    this.parameter3Type = parameter3Type;
  }

  @Column(length = 100)
  public String getParameter4Name()
  {
    return parameter4Name;
  }

  public void setParameter4Name(String parameter4Name)
  {
    this.parameter4Name = parameter4Name;
  }

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  public ScriptParameterType getParameter4Type()
  {
    return parameter4Type;
  }

  public void setParameter4Type(ScriptParameterType parameter4Type)
  {
    this.parameter4Type = parameter4Type;
  }

  @Column(length = 100)
  public String getParameter5Name()
  {
    return parameter5Name;
  }

  public void setParameter5Name(String parameter5Name)
  {
    this.parameter5Name = parameter5Name;
  }

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  public ScriptParameterType getParameter5Type()
  {
    return parameter5Type;
  }

  public void setParameter5Type(ScriptParameterType parameter5Type)
  {
    this.parameter5Type = parameter5Type;
  }

  @Transient
  public String getParameterNames(boolean capitalize)
  {
    final StringBuffer buf = new StringBuffer();
    boolean first = appendParameterName(buf, parameter1Name, capitalize, true);
    first = appendParameterName(buf, parameter2Name, capitalize, first);
    first = appendParameterName(buf, parameter3Name, capitalize, first);
    first = appendParameterName(buf, parameter4Name, capitalize, first);
    first = appendParameterName(buf, parameter5Name, capitalize, first);
    return buf.toString();
  }

  private boolean appendParameterName(final StringBuffer buf, final String parameterName, final boolean capitalize, boolean first)
  {
    if (StringUtils.isNotBlank(parameterName) == true) {
      if (first == true) {
        first = false;
      } else {
        buf.append(", ");
      }
      if (capitalize == true) {
        buf.append(StringUtils.capitalize(parameterName));
      } else {
        buf.append(parameterName);
      }
    }
    return first;
  }
}
