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

package org.projectforge.web.wicket.embats;

import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.projectforge.web.wicket.AttributeAppendModifier;
import org.projectforge.web.wicket.MySession;
import org.projectforge.web.wicket.PresizedImage;
import org.projectforge.web.wicket.WicketUtils;

/**
 * An image or embats icon as link with an href and with a tooltip.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class IconLinkPanel extends Panel
{
  private static final long serialVersionUID = -8093620122285662705L;

  public static final String LINK_WICKET_ID = "link";

  public static final String IMAGE_WICKET_ID = "image";

  private boolean embatsSupported;

  private AbstractLink link;

  private IconLinkPanel(final String id)
  {
    super(id);
    final MySession session = (MySession) getSession();
    embatsSupported = session.isEmbatsSupported();
  }

  /**
   * @param embatsChar
   * @return true if icon was added, false if an image icon is left to add.
   */
  private boolean addIcon(final EmbatsChar embatsChar)
  {
    if (embatsSupported == true) {
      link.add(new Label("embatschar", embatsChar.getFontString()).setRenderBodyOnly(true));
      link.add(new AttributeAppendModifier("class", embatsChar.getCssClass()));
      link.add(new Label(IMAGE_WICKET_ID, "invisible").setVisible(false));
      return true;
    } else {
      link.add(new Label("embatschar", "invisible").setVisible(false));
      return false;
    }
  }

  private void addImage(final EmbatsChar embatsChar)
  {
    link.add(new PresizedImage(IMAGE_WICKET_ID, getResponse(), embatsChar.getFallbackImage()));
  }

  public IconLinkPanel(final String id, final EmbatsChar embatsChar, final Class< ? > pageClass)
  {
    this(id, embatsChar, pageClass, null, (IModel<String>) null);
  }

  public IconLinkPanel(final String id, final EmbatsChar embatsChar, final Class< ? > pageClass, final PageParameters params)
  {
    this(id, embatsChar, pageClass, null, (IModel<String>) null);
  }

  public IconLinkPanel(final String id, final EmbatsChar embatsChar, final Class< ? > pageClass, final String tooltip)
  {
    this(id, embatsChar, pageClass, null, tooltip);
  }

  @SuppressWarnings("unchecked")
  public IconLinkPanel(final String id, final EmbatsChar embatsChar, final Class< ? > pageClass, final PageParameters params,
      final String tooltip)
  {
    this(id);
    if (params != null) {
      link = new BookmarkablePageLink(LINK_WICKET_ID, pageClass, params);
    } else {
      link = new BookmarkablePageLink(LINK_WICKET_ID, pageClass);
    }
    add(link);
    if (addIcon(embatsChar) == false) {
      addImage(embatsChar);
    }
    if (tooltip != null) {
      WicketUtils.addTooltip(link, tooltip, true);
    }
  }

  public IconLinkPanel(final String id, final EmbatsChar embatsChar, final Class< ? > pageClass, final IModel<String> tooltip)
  {
    this(id, embatsChar, pageClass, null, tooltip);
  }

  @SuppressWarnings("unchecked")
  public IconLinkPanel(final String id, final EmbatsChar embatsChar, final Class< ? > pageClass, final PageParameters params,
      final IModel<String> tooltip)
  {
    this(id);
    if (params != null) {
      link = new BookmarkablePageLink(LINK_WICKET_ID, pageClass, params);
    } else {
      link = new BookmarkablePageLink(LINK_WICKET_ID, pageClass);
    }
    add(link);
    if (addIcon(embatsChar) == false) {
      addImage(embatsChar);
    }
    if (tooltip != null) {
      WicketUtils.addTooltip(link, tooltip, true);
    }
  }

  public IconLinkPanel(final String id, final EmbatsChar embatsChar, final String href)
  {
    this(id, embatsChar, href, (IModel<String>) null);
  }

  public IconLinkPanel(final String id, final EmbatsChar embatsChar, final String href, final String tooltip)
  {
    this(id);
    link = new ExternalLink(LINK_WICKET_ID, href);
    add(link);
    if (addIcon(embatsChar) == false) {
      addImage(embatsChar);
    }
    if (tooltip != null) {
      WicketUtils.addTooltip(link, tooltip, true);
    }
  }

  public IconLinkPanel(final String id, final EmbatsChar embatsChar, final String href, final IModel<String> tooltip)
  {
    this(id);
    link = new ExternalLink(LINK_WICKET_ID, href);
    add(link);
    if (addIcon(embatsChar) == false) {
      addImage(embatsChar);
    }
    if (tooltip != null) {
      WicketUtils.addTooltip(link, tooltip, true);
    }
  }

  public IconLinkPanel(final String id, final AbstractLink link, final EmbatsChar embatsChar, final String tooltip)
  {
    this(id);
    this.link = link;
    add(link);
    if (addIcon(embatsChar) == false) {
      addImage(embatsChar);
    }
    if (tooltip != null) {
      WicketUtils.addTooltip(link, tooltip, true);
    }
  }

  public IconLinkPanel(final String id, final AbstractLink link, final EmbatsChar embatsChar, final IModel<String> tooltip)
  {
    this(id);
    this.link = link;
    add(link);
    if (addIcon(embatsChar) == false) {
      addImage(embatsChar);
    }
    if (tooltip != null) {
      WicketUtils.addTooltip(link, tooltip, true);
    }
  }

  /**
   * @param cssClass
   * @return this for chaining.
   */
  public IconLinkPanel appendCssClass(final String cssClass) {
    link.add(new AttributeAppendModifier("class", cssClass));
    return this;
  }
  
  /**
   * @param cssClass
   * @return this for chaining.
   */
  public IconLinkPanel setCssStyle(final String cssStyle) {
    link.add(new SimpleAttributeModifier("style", cssStyle));
    return this;
  }
}
