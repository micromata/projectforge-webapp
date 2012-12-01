/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.flowlayout.GridBuilder;
import org.projectforge.web.wicket.flowlayout.MyComponentsRepeater;

public class AbstractStandardForm<F, P extends AbstractStandardFormPage> extends AbstractForm<F, P>
{
  private static final long serialVersionUID = -2450673501083584299L;

  protected GridBuilder gridBuilder;

  protected MyComponentsRepeater<SingleButtonPanel> actionButtons;

  public AbstractStandardForm(final P parentPage)
  {
    super(parentPage);
  }

  @Override
  protected void init()
  {
    super.init();
    addFeedbackPanel();
    addMessageField();
    final RepeatingView repeater = new RepeatingView("flowform");
    add(repeater);
    gridBuilder = newGridBuilder(repeater);
    actionButtons = new MyComponentsRepeater<SingleButtonPanel>("buttons");
    add(actionButtons.getRepeatingView());
  }

  protected SingleButtonPanel addCancelButton(final Button cancelButton)
  {
    cancelButton.setDefaultFormProcessing(false); // No validation of the form.
    final SingleButtonPanel cancelButtonPanel = new SingleButtonPanel(actionButtons.newChildId(), cancelButton, getString("cancel"),
        SingleButtonPanel.CANCEL);
    actionButtons.add(cancelButtonPanel);
    return cancelButtonPanel;
  }

  /**
   * Adds invisible field as default.
   */
  protected void addMessageField()
  {
    add(new WebMarkupContainer("message").setVisible(false));
  }

  /**
   * @see org.projectforge.web.wicket.AbstractForm#onBeforeRender()
   */
  @Override
  public void onBeforeRender()
  {
    super.onBeforeRender();
    actionButtons.render();
  }
}
