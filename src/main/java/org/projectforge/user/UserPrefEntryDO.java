/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.user;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.AbstractBaseDO;
import org.projectforge.core.BaseDO;
import org.projectforge.core.ModificationStatus;
import org.projectforge.core.UserPrefParameter;

/**
 * Represents a single generic user preference entry.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@Entity
@Table(name = "T_USER_PREF_ENTRY", uniqueConstraints = { @UniqueConstraint(columnNames = { "user_pref_fk", "parameter"})})
public class UserPrefEntryDO implements BaseDO<Integer>, Serializable
{
  private static final long serialVersionUID = 7163902159871289059L;

  public static final int MAX_STRING_VALUE_LENGTH = 10000;

  private String parameter; // 255 not null

  private String value; // MAX_STRING_VALUE_LENGTH

  protected String orderString;

  protected String i18nKey;

  protected String tooltipI18nKey;

  protected String dependsOn;

  protected Class< ? > type;

  protected Object valueAsObject;

  protected boolean required;

  protected Integer maxLength;

  protected boolean multiline;

  private Integer id;

  @Id
  @GeneratedValue
  @Column(name = "pk")
  public Integer getId()
  {
    return id;
  }

  public void setId(final Integer id)
  {
    this.id = id;
  }

  @Column(length = 255)
  public String getParameter()
  {
    return parameter;
  }

  public void setParameter(final String parameter)
  {
    this.parameter = parameter;
  }

  @Column(name = "s_value", length = MAX_STRING_VALUE_LENGTH)
  public String getValue()
  {
    return value;
  }

  public void setValue(final String value)
  {
    this.value = value;
  }

  /**
   * The entries will be ordered by this property. This field is not persisted.
   */
  @Transient
  public String getOrderString()
  {
    return orderString;
  }

  /**
   * Value as object, if given. This field is not persisted.
   */
  @Transient
  public Object getValueAsObject()
  {
    return valueAsObject;
  }

  @Transient
  public Integer getValueAsInteger()
  {
    return NumberHelper.parseInteger(value);
  }

  /**
   * For displaying paramter's localized label (if given). This field is not persisted.
   * @see UserPrefParameter#i18nKey()
   */
  @Transient
  public String getI18nKey()
  {
    return i18nKey;
  }

  @Transient
  public String getTooltipI18nKey()
  {
    return tooltipI18nKey;
  }

  @Transient
  public String getDependsOn()
  {
    return dependsOn;
  }

  /**
   * This field is not persisted.
   * @see UserPrefParameter#required()
   */
  @Transient
  public boolean isRequired()
  {
    return required;
  }

  /**
   * This field is not persisted.
   * @see UserPrefParameter#multiline()
   */
  @Transient
  public boolean isMultiline()
  {
    return multiline;
  }

  /**
   * This field is not persisted.
   */
  @Transient
  public Integer getMaxLength()
  {
    return maxLength;
  }

  /**
   * Type of parameter value (if given). This field is not persisted.
   */
  @Transient
  public Class< ? > getType()
  {
    return type;
  }

  /**
   * @return Always true.
   * @see org.projectforge.core.BaseDO#isMinorChange()
   */
  @Transient
  public boolean isMinorChange()
  {
    return false;
  }

  /**
   * Throws UnsupportedOperationException.
   * @see org.projectforge.core.BaseDO#setMinorChange(boolean)
   */
  public void setMinorChange(final boolean value)
  {
    throw new UnsupportedOperationException();
  }

  public UserPrefEntryDO()
  {
  }

  @Override
  public boolean equals(final Object o)
  {
    if (o instanceof UserPrefEntryDO) {
      final UserPrefEntryDO other = (UserPrefEntryDO) o;
      if (ObjectUtils.equals(this.parameter, other.parameter) == false)
        return false;
      if (ObjectUtils.equals(this.getId(), other.getId()) == false)
        return false;
      return true;
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    final HashCodeBuilder hcb = new HashCodeBuilder();
    hcb.append(parameter);
    hcb.append(getId());
    return hcb.toHashCode();
  }

  @Override
  public String toString()
  {
    final ToStringBuilder sb = new ToStringBuilder(this);
    sb.append("id", getId());
    sb.append("parameter", this.parameter);
    sb.append("value", this.value);
    return sb.toString();
  }

  /**
   * @param src
   * @see AbstractBaseDO#copyValues(BaseDO, BaseDO, String...)
   */
  public ModificationStatus copyValuesFrom(final BaseDO< ? extends Serializable> src, final String... ignoreFields)
  {
    return AbstractBaseDO.copyValues(src, this, ignoreFields);
  }

  public Object getAttribute(final String key)
  {
    throw new UnsupportedOperationException();
  }

  public void setAttribute(final String key, final Object value)
  {
    throw new UnsupportedOperationException();
  }
}
