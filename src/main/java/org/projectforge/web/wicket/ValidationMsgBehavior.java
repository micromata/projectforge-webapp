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

package org.projectforge.web.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.form.FormComponent;
import org.projectforge.web.HtmlHelper;

/**
 * Validation message to show as html div element.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class ValidationMsgBehavior extends AbstractBehavior
{
  private static final long serialVersionUID = 6188770593311749751L;

  public void onRendered(Component component)
  {
    if (component instanceof FormComponent<?> == false) {
      return;
    }
    FormComponent< ? > fc = (FormComponent< ? >) component;
    if (fc.isValid() == false) {
      String error;
      if (fc.hasFeedbackMessage() == true) {
        error = fc.getFeedbackMessage().getMessage().toString();
      } else {
        error = "Your input is invalid.";
      }
      fc.getResponse().write("<div class=\"validationMsg\">" + HtmlHelper.escapeXml(error) + "</div>");
    }
  }
}
