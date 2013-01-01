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

package org.projectforge.web.wicket.flowlayout;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

/**
 * Represents a entry of a group panel. This can be a label, text field or other form components.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class DialogPanel extends Panel
{
  private static final long serialVersionUID = 6130552547273354134L;

  private final Form<Void> ajaxForm;

  private final RepeatingView content;

  private final MyComponentsRepeater<Component> buttons;

  /**
   * @param id
   */
  @SuppressWarnings("serial")
  public DialogPanel(final ModalWindow modalWindow, final String title)
  {
    super(modalWindow.getContentId());
    modalWindow.setTitle(title);
    super.add(ajaxForm = new Form<Void>("form"));
    ajaxForm.add(new FeedbackPanel("feedback") {
      @Override
      public boolean isVisible()
      {
        return hasErrorMessage();

      }
    }.setOutputMarkupId(true));
    content = new RepeatingView("content");
    ajaxForm.add(content);
    buttons = new MyComponentsRepeater<Component>("buttons");
    ajaxForm.add(buttons.getRepeatingView());
  }

  /**
   * @see org.apache.wicket.Component#onBeforeRender()
   */
  @Override
  protected void onBeforeRender()
  {
    super.onBeforeRender();
    buttons.render();
  }

  public DialogPanel addButton(final Component component)
  {
    buttons.add(component);
    return this;
  }

  public DialogPanel addButton(final int pos, final Component component)
  {
    buttons.add(pos, component);
    return this;
  }

  public String newButtonChildId()
  {
    return buttons.newChildId();
  }

  /**
   * Adds components to the content's RepeatingView.
   * @see org.apache.wicket.MarkupContainer#add(org.apache.wicket.Component[])
   */
  @Override
  public DialogPanel add(final Component... components)
  {
    content.add(components);
    return this;
  }

  /**
   * @return New child id of the content's RepeatingView.
   * @see RepeatingView#newChildId()
   */
  public String newChildId()
  {
    return content.newChildId();
  }
}
