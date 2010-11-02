/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.wicket;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.wicket.Component;
import org.apache.wicket.IClusterable;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.model.IComponentAssignedModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.value.IValueMap;

/**
 * Modified version of AttributeModifier for implementing e. g. css style attribute with support of appending attribute values such as
 * style="text-align: right; font-weight:bold; color: red;"
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class AttributeAppendModifier extends AbstractBehavior implements IClusterable
{
  private static final long serialVersionUID = 3132727102170379490L;

  /** Attribute specification. */
  private final String attribute;

  /** Modification information. */
  private boolean enabled;

  private boolean prepend;

  /** The model that is to be used for the replacement. */
  private final IModel< ? > replaceModel;

  /**
   * Create a new attribute modifier with the given attribute name and value to append. If the attribute does already exist then the
   * value will be appended.
   * 
   * @param attribute The attribute name to replace the value for
   * @param value The value to append.
   */
  public AttributeAppendModifier(final String attribute, final String value)
  {
    this(attribute, new Model<String>(value));
  }

  /**
   * Create a new attribute modifier with the given attribute name and model to replace with. If the attribute does already exist then the
   * replaceModel will be appended.
   * 
   * @param attribute The attribute name to replace the value for
   * @param replaceModel The model to replace the value with
   */
  public AttributeAppendModifier(final String attribute, final IModel< ? > replaceModel)
  {
    Validate.notNull(attribute);
    this.attribute = attribute;
    enabled = true;
    this.replaceModel = replaceModel;
  }

  /**
   * Detach the model if it was a IDetachableModel Internal method. shouldn't be called from the outside. If the attribute modifier is
   * shared, the detach method will be called multiple times.
   * 
   * @param component the model that initiates the detachment
   */
  @Override
  public final void detach(Component component)
  {
    if (replaceModel != null) {
      replaceModel.detach();
    }
  }

  /**
   * @return the attribute name to replace the value for
   */
  public final String getAttribute()
  {
    return attribute;
  }

  /**
   * Made final to support the parameterless variant.
   * 
   * @see org.apache.wicket.behavior.AbstractBehavior#isEnabled(org.apache.wicket.Component)
   */
  @Override
  public boolean isEnabled(Component component)
  {
    return enabled;
  }

  /**
   * Prepend attribute values instead of append.
   * @return this (fluent).
   */
  public AttributeAppendModifier setPrepend()
  {
    this.prepend = true;
    return this;
  }

  /**
   * @see org.apache.wicket.behavior.IBehavior#onComponentTag(org.apache.wicket.Component, org.apache.wicket.markup.ComponentTag)
   */
  @Override
  public final void onComponentTag(Component component, ComponentTag tag)
  {
    if (tag.getType() != XmlTag.CLOSE) {
      replaceAttributeValue(component, tag);
    }
  }

  /**
   * Checks the given component tag for an instance of the attribute to modify and if all criteria are met then replace the value of this
   * attribute with the value of the contained model object.
   * 
   * @param component The component
   * @param tag The tag to replace the attribute value for
   */
  public final void replaceAttributeValue(final Component component, final ComponentTag tag)
  {
    if (isEnabled(component)) {
      final IValueMap attributes = tag.getAttributes();
      final Object replacementValue = getReplacementOrNull(component);

      if (attributes.containsKey(attribute)) {
        final String currentValue = StringUtils.defaultString(toStringOrNull(attributes.get(attribute)));
        final String newValue = toStringOrNull(replacementValue);
        if (newValue != null) {
          if (prepend == true) {
            attributes.put(attribute, newValue + currentValue);
          } else {
            attributes.put(attribute, currentValue + newValue);
          }
        }
      } else {
        final String newValue = toStringOrNull(replacementValue);
        if (newValue != null) {
          attributes.put(attribute, newValue);
        }
      }
    }
  }

  /**
   * Sets whether this attribute modifier is enabled or not.
   * 
   * @param enabled Whether enabled or not
   */
  public final void setEnabled(final boolean enabled)
  {
    this.enabled = enabled;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return "[AttributeModifier attribute=" + attribute + ", enabled=" + enabled + ", replacementModel=" + replaceModel + "]";
  }

  /**
   * gets replacement with null check.
   * 
   * @param component
   * @return replacement value
   */
  private Object getReplacementOrNull(final Component component)
  {
    IModel< ? > model = replaceModel;
    if (model instanceof IComponentAssignedModel< ? >) {
      model = ((IComponentAssignedModel< ? >) model).wrapOnAssignment(component);
    }
    return (model != null) ? model.getObject() : null;
  }

  /**
   * gets replacement as a string with null check.
   * 
   * @param replacementValue
   * @return replacement value as a string
   */
  private String toStringOrNull(final Object replacementValue)
  {
    return (replacementValue != null) ? replacementValue.toString() : null;
  }

  /**
   * Gets the replacement model. Allows subclasses access to replace model.
   * 
   * @return the replace model of this attribute modifier
   */
  protected final IModel< ? > getReplaceModel()
  {
    return replaceModel;
  }
}
