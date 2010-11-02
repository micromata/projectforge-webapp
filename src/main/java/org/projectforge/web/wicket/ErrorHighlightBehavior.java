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

package org.projectforge.web.wicket;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.FormComponent;

/**
 * Set class attribute of tag to error or appends " error" if class attribute does already exist.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class ErrorHighlightBehavior extends AbstractBehavior
{
  private static final long serialVersionUID = -7296498608817410487L;

  @Override
  public void onComponentTag(Component component, ComponentTag tag)
  {
    if (component instanceof FormComponent< ? > == false) {
      return;
    }
    FormComponent< ? > fc = (FormComponent< ? >) component;
    if (fc instanceof AbstractSelectPanel< ? > == true) {
      // Do ignore the AbstractSelectPanels, otherwise the icons looks not very pretty on colored background.
      return;
    }
    if (fc.isValid() == false) {
      String value = tag.getAttribute("class");
      if (StringUtils.isEmpty(value) == true) {
        tag.put("class", "error");
      } else {
        tag.put("class", value + " error");
      }
    }
  }
}
