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

package org.projectforge.web.mobile;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.projectforge.web.wicket.ImageDef;
import org.projectforge.web.wicket.PresizedImage;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.layout.IField;
import org.projectforge.web.wicket.layout.LayoutAlignment;

/**
 * A link panel which instantiates a phone call on an iPhone as well as an sms or an e-mail.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class ActionLinkPanel extends Panel implements IField
{
  private static final long serialVersionUID = -5497704312133705066L;

  private AbstractLink link1, link2;

  public ActionLinkPanel(final String id, final ActionLinkType actionLinkType, final String value)
  {
    super(id);
    if (actionLinkType == ActionLinkType.CALL) {
      add(link1 = getCallLink(value));
      add(getInvisibleSmsLink());
    } else if (actionLinkType == ActionLinkType.SMS) {
      add(new Label("link", "[invisible]").setVisible(false));
      add(link2 = getSmsLink(value));
    } else if (actionLinkType == ActionLinkType.CALL_AND_SMS) {
      add(link1 = getCallLink(value));
      add(link2 = getSmsLink(value));
    } else if (actionLinkType == ActionLinkType.MAIL) {
      add(link1 = new ExternalLink("link", "mailto:" + value, value));
      add(getInvisibleSmsLink());
    } else {
      final String url;
      if (value != null && value.contains("://") == true) {
        url = value;
      } else {
        url = "http://" + value;
      }
      add(link1 = new ExternalLink("link", url, value));
      add(getInvisibleSmsLink());
    }
  }
  
  /**
   * Does nothing (not yet implemented).
   * @see org.projectforge.web.wicket.layout.IField#setAlignment(org.projectforge.web.wicket.layout.LayoutAlignment)
   */
  @Override
  public IField setAlignment(LayoutAlignment aligment)
  {
    return this;
  }

  @Override
  public ActionLinkPanel setStrong()
  {
    add(new SimpleAttributeModifier("class", "strong"));
    return this;
  }
  
  @Override
  public IField setCssStyle(final String cssStyle)
  {
    add(new SimpleAttributeModifier("style", cssStyle));
    return this;
  }

  @Override
  public ActionLinkPanel setFocus()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public ActionLinkPanel setRequired()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public ActionLinkPanel setTooltip(String text)
  {
    if (link1 != null) {
      WicketUtils.addTooltip(link1, text);
    }
    if (link2 != null) {
      WicketUtils.addTooltip(link2, text);
    }
    return this;
  }

  private ExternalLink getCallLink(final String number)
  {
    return new ExternalLink("link", "tel:" + number, number);
  }

  private ExternalLink getSmsLink(final String number)
  {
    final ExternalLink smsLink = new ExternalLink("sms", "sms:" + number);
    smsLink.add(new PresizedImage("smsImage", getResponse(), ImageDef.SMS));
    return smsLink;
  }

  private Component getInvisibleSmsLink()
  {
    return new Label("sms", "[invisible]").setVisible(false);
  }
}
