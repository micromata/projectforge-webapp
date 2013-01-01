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

package org.projectforge.web.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.FormComponent;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * Validation message to show as html div element.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class ValidationMsgBehavior extends Behavior
{
  private static final long serialVersionUID = 6188770593311749751L;

  /**
   * @see org.apache.wicket.behavior.Behavior#onComponentTag(org.apache.wicket.Component, org.apache.wicket.markup.ComponentTag)
   */
  @Override
  public void onComponentTag(final Component component, final ComponentTag tag)
  {
    if (component instanceof FormComponent< ? >) {
      final FormComponent< ? > fc = (FormComponent< ? >) component;
      if (fc.isValid() == false) {
        String error;
        if (fc.hasFeedbackMessage() == true) {
          error = fc.getFeedbackMessages().first().getMessage().toString();
        } else {
          error = component.getString("validation.error.generic");
        }
        // TODO: validationMsg muss raus, wenn altes Layout entfernt wurde.
        fc.getResponse().write("<div class=\"error validationMsg\">" + HtmlHelper.escapeXml(error) + "</div>");
      }
    }
    if (component.getParent() != null && component.getParent() instanceof FieldsetPanel) {
      final FieldsetPanel fsPanel = (FieldsetPanel) component.getParent();
      if (fsPanel.isValid() == false) {
        String feedbackMessage;
        if (fsPanel.hasFormChildsFeedbackMessage() == true) {
          feedbackMessage = fsPanel.getFormChildsFeedbackMessages(true);
        } else {
          feedbackMessage = component.getString("validation.error.generic");
        }
        fsPanel.setFeedbackMessage(feedbackMessage);
      }
    }
  }

  /**
   * @see org.apache.wicket.behavior.Behavior#afterRender(org.apache.wicket.Component)
   */
  @Override
  public void afterRender(final Component component)
  {
  }
}
