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

package org.projectforge.web.wicket.components;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.projectforge.web.wicket.WicketUtils;

/**
 * A stylish checkbox looking like a button.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@SuppressWarnings("serial")
public class CoolCheckBoxPanel extends Panel
{
  private CheckBox checkBox;

  private Label label;

  public CoolCheckBoxPanel(final String id, final IModel<Boolean> model, final String label)
  {
    this(id, model, label, false);
  }

  /**
   * @param id
   * @param model
   * @param labelString
   * @param submitOnChange if true then onchange="javascript:submit()" is added.
   */
  public CoolCheckBoxPanel(final String id, final IModel<Boolean> model, final String labelString, boolean submitOnChange)
  {
    super(id);
    checkBox = new CheckBox("checkBox", model) {
      @Override
      public void onSelectionChanged()
      {
        CoolCheckBoxPanel.this.onSelectionChanged();
      }

      @Override
      protected boolean wantOnSelectionChangedNotifications()
      {
        return CoolCheckBoxPanel.this.wantOnSelectionChangedNotifications();
      }
    };
    checkBox.setOutputMarkupId(true);
    if (submitOnChange == true) {
      checkBox.add(new SimpleAttributeModifier("onchange", "javascript:submit();"));
    }
    add(checkBox);
    label = new Label("label", labelString);
    label.add(new SimpleAttributeModifier("for", checkBox.getMarkupId()));
    add(label);
    setRenderBodyOnly(true);
  }

  /**
   * Sets tool-tip for the label.
   * @param tooltip
   * @return this for chaining.
   */
  public CoolCheckBoxPanel setTooltip(final String tooltip)
  {
    WicketUtils.addTooltip(label, tooltip);
    return this;
  }

  protected void onSelectionChanged()
  {
  }

  /**
   * Doesn't work, isn't it?
   * @return
   */
  protected boolean wantOnSelectionChangedNotifications()
  {
    return false;
  }

  public CheckBox getCheckBox()
  {
    return checkBox;
  }

}
