/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

import org.apache.wicket.Component;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;

/**
 * Represents a two-column table with label values. This component is used for mobile devices.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LabelValueTableLPanel extends AbstractLPanel
{
  public static final String WICKET_ID_VALUE = "value";

  public static final String WICKET_ID_LABEL = "label";

  private static final long serialVersionUID = 1232385200034295638L;

  private RepeatingView rowRepeater;

  private boolean hasChildren;

  /**
   * @see AbstractFormRenderer#createRepeaterLabelPanel(String)
   */
  LabelValueTableLPanel(final String id)
  {
    super(id, null);
    rowRepeater = new RepeatingView("rowRepeater");
    super.add(rowRepeater);
  }

  public boolean hasChildren()
  {
    return hasChildren;
  }

  public LabelValueTableLPanel add(final String label, final String value)
  {
    hasChildren = true;
    final WebMarkupContainer row = new WebMarkupContainer(rowRepeater.newChildId());
    rowRepeater.add(row);
    row.add(new Label("label", label));
    row.add(new Label(WICKET_ID_VALUE, value));
    return this;
  }

  public LabelValueTableLPanel add(final String label, final WebMarkupContainer value)
  {
    return add(label, value, false);
  }

  public LabelValueTableLPanel add(final String label, final Component value, final boolean newLineBetweenLabelAndValue)
  {
    hasChildren = true;
    WebMarkupContainer row = new WebMarkupContainer(rowRepeater.newChildId());
    rowRepeater.add(row);
    if (newLineBetweenLabelAndValue == true) {
      row.add(new Label("label", label).add(new SimpleAttributeModifier("class", "label-value-single-col")));
      row.add(new Label(WICKET_ID_VALUE, ""));
      row = new WebMarkupContainer(rowRepeater.newChildId());
      rowRepeater.add(row);
      row.add(value.add(new SimpleAttributeModifier("colspan", "2")).add(new SimpleAttributeModifier("class", "label-value-single-col strong")));
      row.add(new Label(WICKET_ID_VALUE, "[invisible]").setVisible(false));
    } else {
      row.add(new Label("label", label));
      row.add(value);
    }
    return this;
  }

  @Override
  protected Component getClassModifierComponent()
  {
    return this;
  }
}
