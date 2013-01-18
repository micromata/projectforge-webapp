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

package org.projectforge.web.wicket.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.projectforge.common.StringHelper;

/**
 * Panel for using as single button (needed for css decoration).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class SingleButtonPanel extends Panel
{
  private static final long serialVersionUID = -3241045253014479836L;

  private final Button button;

  /**
   * The component id of the enclosed button.
   */
  public static final String WICKET_ID = "button";

  public static final String CANCEL = "btn-danger";

  public static final String DEFAULT_SUBMIT = "btn-success";

  public static final String DELETE = "btn-warning";

  public static final String GREY = "btn-info";

  public static final String RED = "btn-danger";

  public static final String RESET = "btn-danger";

  /**
   * 
   * @param id
   * @param button
   * @param label
   * @param classnames css class names
   */
  public SingleButtonPanel(final String id, final Button button, final String label, final String... classnames)
  {
    this(id, button, new Model<String>(label), classnames);
  }

  /**
   * 
   * @param id
   * @param button
   * @param label
   * @param classnames css class names
   */
  public SingleButtonPanel(final String id, final Button button, final IModel<String> label, final String... classnames)
  {
    super(id);
    this.button = button;
    add(button);
    button.add(new Label("label", label).setRenderBodyOnly(true));
    if (classnames != null) {
      button.add(AttributeModifier.append("class", StringHelper.listToString(" ", classnames)));
    }
  }

  /**
   * @param classnames css class names
   * @return this for chaining.
   */
  public SingleButtonPanel setClassnames(final String... classnames)
  {
    if (classnames != null) {
      button.add(AttributeModifier.append("class", StringHelper.listToString(" ", classnames)));
    }
    return this;
  }

  public SingleButtonPanel setTooltip(final String tooltip)
  {
    if (tooltip != null) {
      button.add(AttributeModifier.replace("title", tooltip));
    }
    return this;
  }

  public Button getButton()
  {
    return button;
  }
}
