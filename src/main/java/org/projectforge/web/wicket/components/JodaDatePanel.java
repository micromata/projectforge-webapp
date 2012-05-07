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

package org.projectforge.web.wicket.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.joda.time.DateMidnight;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.flowlayout.ComponentWrapperPanel;

/**
 * Panel for date selection. Works for java.util.Date and java.sql.Date. For java.sql.Date don't forget to call the constructor with
 * targetType java.sql.Date.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class JodaDatePanel extends FormComponentPanel<DateMidnight> implements ComponentWrapperPanel
{
  private static final long serialVersionUID = 3785639935585959803L;

  protected JodaDateField dateField;

  /**
   * @param id
   * @param label Only for displaying the field's name on validation messages.
   * @param model
   */
  public JodaDatePanel(final String id, final IModel<DateMidnight> model)
  {
    super(id, model);
    dateField = new JodaDateField("dateField", model);
    dateField.add(AttributeModifier.replace("size", "10"));
    dateField.setOutputMarkupId(true);
    add(dateField);
  }

  /**
   * @see org.apache.wicket.markup.html.form.FormComponent#setLabel(org.apache.wicket.model.IModel)
   */
  @Override
  public JodaDatePanel setLabel(final IModel<String> labelModel)
  {
    dateField.setLabel(labelModel);
    super.setLabel(labelModel);
    return this;
  }

  public JodaDatePanel setFocus()
  {
    dateField.add(WicketUtils.setFocus());
    return this;
  }


  public JodaDateField getDateField()
  {
    return dateField;
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.ComponentWrapperPanel#getComponentOutputId()
   */
  @Override
  public String getComponentOutputId()
  {
    return dateField.getMarkupId();
  }
}
