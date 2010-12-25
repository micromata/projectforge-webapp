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

package org.projectforge.web.wicket.layout;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.projectforge.web.wicket.ImageDef;
import org.projectforge.web.wicket.components.DatePanel;

/**
 * Base class for renderers of data objects. This renderer can be re-used by different pages (mobile pages as well as read-only or edit form
 * pages).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public abstract class AbstractRenderer implements Serializable
{
  private static final long serialVersionUID = -5202334758019046183L;

  protected LayoutContext layoutContext;

  protected MarkupContainer container;

  /**
   * Creates a FieldSetLPanel (for normal or mobile version) depending on the layout context.
   * @param id
   * @param heading
   */
  public FieldSetLPanel createFieldSetLPanel(final String id, final String heading)
  {
    if (layoutContext.isMobile() == true) {
      return new FieldSetMobileLPanel(id, heading);
    } else {
      return new FieldSetLPanel(id, heading);
    }
  }

  public GroupLPanel createGroupLPanel(final String id)
  {
    return createGroupLPanel(id, null);
  }

  public GroupLPanel createGroupLPanel(final String id, final String heading)
  {
    if (layoutContext.isMobile() == true) {
      return new GroupMobileLPanel(id, heading);
    } else {
      return new GroupLPanel(id, heading);
    }
  }

  public LabelLPanel createLabelLPanel(final String id, final LayoutLength length, final String label)
  {
    return new LabelLPanel(id, length, label);
  }

  public LabelLPanel createLabelLPanel(final String id, final LayoutLength length, final String label, final boolean breakBefore)
  {
    return new LabelLPanel(id, length, label, breakBefore);
  }

  public LabelLPanel createLabelLPanel(final String id, final LayoutLength length, final String label, final Component labelFor,
      final boolean breakBefore)
  {
    return new LabelLPanel(id, length, label, labelFor, breakBefore);
  }

  public DropDownChoiceLPanel createDropDownChoiceLPanel(final String id, final LayoutLength length,
      final DropDownChoice< ? > dropDownChoice)
  {
    return new DropDownChoiceLPanel(id, length, dropDownChoice);
  }

  public CheckBoxLPanel createCheckBoxLPanel(final String id, final CheckBox checkBox)
  {
    return new CheckBoxLPanel(id, checkBox);
  }

  public CheckBoxLPanel createCheckBoxLPanel(final String id, final Object dataObject, final String property)
  {
    return new CheckBoxLPanel(id, dataObject, property);
  }

  public DateFieldLPanel createDateFieldLPanel(final String id, final LayoutLength length, final DatePanel datePanel)
  {
    return new DateFieldLPanel(id, length, datePanel);
  }

  public ImageLPanel createImageLPanel(final String id, final ImageDef imageDef, final String tooltip)
  {
    return new ImageLPanel(id, imageDef, tooltip);
  }

  public RepeaterLabelLPanel createRepeaterLabelLPanel(final String id, final LayoutLength length)
  {
    return new RepeaterLabelLPanel(id, length);
  }

  public RepeaterLabelLPanel createRepeaterLabelLPanel(final String id)
  {
    return new RepeaterLabelLPanel(id);
  }

  public TextFieldLPanel createTextFieldLPanel(final String id, final LayoutLength length, final Object dataObject, final String property)
  {
    return new TextFieldLPanel(id, length, dataObject, property);
  }

  public TextFieldLPanel createTextFieldLPanel(final String id, final LayoutLength length, final TextField<?> textField)
  {
    return new TextFieldLPanel(id, length, textField);
  }

  public AbstractRenderer(final MarkupContainer container, final LayoutContext layoutContext)
  {
    this.layoutContext = layoutContext;
    this.container = container;
  }

  public abstract void add();

  protected AbstractRenderer add(final Component component)
  {
    container.add(component);
    return this;
  }

  protected String getString(final String i18nKey)
  {
    return container.getString(i18nKey);
  }

  protected boolean isNew()
  {
    return layoutContext.isNew();
  }

  protected boolean isMobile()
  {
    return layoutContext.isMobile();
  }

  protected boolean isReadonly()
  {
    return layoutContext.isReadonly();
  }
}
