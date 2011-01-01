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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
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

  protected DataObjectLPanel doPanel;

  protected MarkupContainer container;

  /**
   * Creates a FieldSetLPanel (for normal or mobile version) depending on the layout context.
   * @param id
   * @param heading
   */
  @Deprecated
  public FieldSetLPanel createFieldSetPanel(final String id, final String heading)
  {
    if (layoutContext.isMobile() == true) {
      return new FieldSetMobileLPanel(id, heading);
    } else {
      return new FieldSetLPanel(id, heading);
    }
  }

  @Deprecated
  public GroupLPanel createGroupPanel(final String id)
  {
    return createGroupPanel(id, null);
  }

  @Deprecated
  public GroupLPanel createGroupPanel(final String id, final String heading)
  {
    if (layoutContext.isMobile() == true) {
      return new GroupMobileLPanel(id, heading);
    } else {
      return new GroupLPanel(id, heading);
    }
  }

  @Deprecated
  public LabelLPanel createLabelPanel(final String id, final LayoutLength length, final String label)
  {
    return new LabelLPanel(id, length, label);
  }

  @Deprecated
  public LabelLPanel createLabelPanel(final String id, final LayoutLength length, final String label, final boolean breakBefore)
  {
    return new LabelLPanel(id, length, label, breakBefore);
  }

  @Deprecated
  public LabelLPanel createLabelPanel(final String id, final LayoutLength length, final String label, final Component labelFor,
      final boolean breakBefore)
  {
    return new LabelLPanel(id, length, label, labelFor, breakBefore);
  }

  @Deprecated
  public LabelValueTableLPanel createLabelValueTablePanel(final String id)
  {
    return new LabelValueTableLPanel(id);
  }

  @Deprecated
  public LabelLPanel createLabelValueHeadingPanel(final String id, final String label)
  {
    if (isMobile() == true) {
      return new LabelValueHeadingMobileLPanel(id, label);
    } else {
      return new LabelValueHeadingLPanel(id, label);
    }
  }

  @Deprecated
  public LabelLPanel createValuePanel(final String id, final String value)
  {
    return new ValueLPanel(id, value);
  }

  /**
   * Adds a new row to the given label-value-table only if value isn't blank. If the given value is blank nothing will be done.
   * @param labelValueTablePanel
   */
  @Deprecated
  public void addLabelValueRow(final LabelValueTableLPanel labelValueTablePanel, final String label, final String value)
  {
    if (StringUtils.isBlank(value) == true) {
      return;
    }
    labelValueTablePanel.add(label, value);
  }

  /**
   * Adds a new row to the given label-value-table.
   * @param labelValueTablePanel
   */
  @Deprecated
  public void addLabelValueRow(final LabelValueTableLPanel labelValueTablePanel, final String label, final WebMarkupContainer value)
  {
    labelValueTablePanel.add(label, value);
  }

  @Deprecated
  public DropDownChoiceLPanel createDropDownChoicePanel(final String id, final LayoutLength length, final DropDownChoice< ? > dropDownChoice)
  {
    if (isMobile() == true) {
      return new DropDownChoiceMobileLPanel(id, length, dropDownChoice);
    } else {
      return new DropDownChoiceLPanel(id, length, dropDownChoice);
    }
  }

  @Deprecated
  public CheckBoxLPanel createCheckBoxPanel(final String id, final CheckBox checkBox)
  {
    return new CheckBoxLPanel(id, checkBox);
  }

  @Deprecated
  public CheckBoxLPanel createCheckBoxPanel(final String id, final Object dataObject, final String property)
  {
    return new CheckBoxLPanel(id, dataObject, property);
  }

  @Deprecated
  public DateFieldLPanel createDateFieldPanel(final String id, final LayoutLength length, final DatePanel datePanel)
  {
    return new DateFieldLPanel(id, length, datePanel);
  }

  @Deprecated
  public ImageLPanel createImagePanel(final String id, final ImageDef imageDef, final String tooltip)
  {
    return new ImageLPanel(id, imageDef, tooltip);
  }

  @Deprecated
  public RepeaterLabelLPanel createRepeaterLabelPanel(final String id, final LayoutLength length)
  {
    return new RepeaterLabelLPanel(id, length);
  }

  @Deprecated
  public RepeaterLabelLPanel createRepeaterLabelPanel(final String id)
  {
    return new RepeaterLabelLPanel(id);
  }

  @Deprecated
  public TextFieldLPanel createTextFieldPanel(final String id, final LayoutLength length, final Object dataObject, final String property)
  {
    return new TextFieldLPanel(id, length, dataObject, property);
  }

  @Deprecated
  public TextFieldLPanel createTextFieldPanel(final String id, final LayoutLength length, final TextField< ? > textField)
  {
    return new TextFieldLPanel(id, length, textField);
  }

  public AbstractRenderer(final MarkupContainer container, final LayoutContext layoutContext)
  {
    this.layoutContext = layoutContext;
    this.doPanel = new DataObjectLPanel("fieldSetsPanel", layoutContext);
    this.container = container;
    container.add(doPanel);
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

  protected boolean isMobileReadonly()
  {
    return layoutContext.isMobileReadonly();
  }

  protected boolean isReadonly()
  {
    return layoutContext.isReadonly();
  }
}
