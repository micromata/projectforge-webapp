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
import org.apache.wicket.model.Model;

/**
 * Panel containing only one check-box followed by one label.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class CheckBoxLabelPanel extends Panel
{
  private static final long serialVersionUID = 1949087680268950965L;

  final CheckBox checkBox;

  @SuppressWarnings("serial")
  public CheckBoxLabelPanel(final String id, final IModel<Boolean> model, final String i18nKey)
  {
    super(id);
    checkBox = new CheckBox("checkBox", model);
    add(checkBox.setOutputMarkupId(true));
    final Model<String> labelModel = new Model<String>() {
      public String getObject()
      {
        return getString(i18nKey);
      };
    };
    checkBox.setLabel(labelModel);
    // I18n key must be implemented as Model not as String because in constructor (before adding this component to parent) a warning will be
    // logged for using getString(String).
    add(new Label("label", labelModel).add(new SimpleAttributeModifier("for", checkBox.getMarkupId())));
    setRenderBodyOnly(true);
  }

  public CheckBoxLabelPanel setSubmitOnChange()
  {
    checkBox.add(new SimpleAttributeModifier("onchange", "javascript:submit();"));
    return this;
  }
}
