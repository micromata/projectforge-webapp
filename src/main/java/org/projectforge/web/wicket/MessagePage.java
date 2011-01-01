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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

/**
 * Standard error page should be shown in production mode.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class MessagePage extends AbstractSecuredPage
{
  public static final String PARAM_MESSAGE = "msg";

  public static final String PARAM_WARNING = "warn";

  private String message;

  private boolean warning;

  public MessagePage(final String msgKey)
  {
    super(new PageParameters());
    this.message = getString(msgKey);
    init();
  }

  public MessagePage(final String msgKey, final Object... msgParams)
  {
    super(new PageParameters());
    this.message = getLocalizedMessage(msgKey, msgParams);
    init();
  }

  public MessagePage(final PageParameters params)
  {
    super(params);
    if (params.containsKey(PARAM_MESSAGE) == true) {
      message = params.getString(PARAM_MESSAGE);
    }
    if (params.containsKey(PARAM_WARNING) == true) {
      warning = true;
    }
    init();
  }

  @SuppressWarnings("serial")
  private void init()
  {
    final Label msgLabel = new Label("message", new Model<String>() {
      @Override
      public String getObject()
      {
        return message;
      }
    });
    msgLabel.add(new AttributeModifier("class", new Model<String>() {
      public String getObject()
      {
        if (warning == true) {
          return "errors";
        } else {
          return "message";
        }
      };
    }));
    body.add(msgLabel);
  }

  @Override
  protected String getTitle()
  {
    return getString("message.title");
  }

  public void setMessage(String message)
  {
    this.message = message;
  }

  public MessagePage setWarning(boolean warning)
  {
    this.warning = warning;
    return this;
  }
}
