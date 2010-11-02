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

package org.projectforge.web.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.MenuBuilder;
import org.projectforge.web.MenuTreeTable;
import org.projectforge.web.MenuTreeTableNode;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.tree.TreeTableEvent;
import org.projectforge.web.wicket.AbstractSecuredBasePage;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.PresizedImage;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketAjaxUtils;
import org.projectforge.web.wicket.WicketUtils;

/**
 * Model for date and time of day components.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class MenuPanel extends Panel
{
  public static final String USER_PREF_MENU_KEY = "menuTreeTable";

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MenuPanel.class);

  private static final long serialVersionUID = -617837803627692409L;

  private static final String USER_PREF_KEY_OPENED_NODES = "menu.openedNodes";

  private MenuTreeTable menu;

  private List<MenuTreeTableNode> treeList;

  private static final String IMAGE_EMPTY = "menuTree/empty.gif";

  private static final String IMAGE_LEAF = "menuTree/join.gif";

  private static final String IMAGE_LEAF_BOTTOM = "menuTree/joinbottom.gif";

  private static final String IMAGE_CLOSED = "menuTree/plus.gif";

  private static final String IMAGE_CLOSED_BOTTOM = "menuTree/plusbottom.gif";

  private static final String IMAGE_OPENED = "menuTree/minus.gif";

  private static final String IMAGE_OPENED_BOTTOM = "menuTree/minusbottom.gif";

  private static final String IMAGE_VERTICAL_LINE = "menuTree/line.gif";

  @SpringBean(name = "menuBuilder")
  private MenuBuilder menuBuilder;

  private WebMarkupContainer rowContainer;

  private RepeatingView rowRepeater;

  public MenuPanel(String id)
  {
    super(id);
    if (log.isDebugEnabled() == true) {
      log.debug("Constructor: " + this);
    }
  }

  public void init()
  {
    add(new ContextImage("micromataIcon", WicketUtils.getImageUrl(getResponse(), WebConstants.IMAGE_MICROMATA_MENU_ICON)));
    rowContainer = new WebMarkupContainer("container");
    rowContainer.setOutputMarkupId(true);
    add(rowContainer);
    rowRepeater = new RepeatingView("repeater");
    rowRepeater.setOutputMarkupId(true);
    rowContainer.add(rowRepeater);
    getMenu();
    for (final MenuTreeTableNode node : getTreeList()) {
      final WebMarkupContainer row = createTreeRow(node);
      rowRepeater.add(row);
    }
  }

  private void openMenu(final AjaxRequestTarget target, final AbstractLink clickedLink, final MenuTreeTableNode node)
  {
    if (log.isDebugEnabled() == true) {
      log.debug("open menu="
          + this.hashCode()
          + ", entry="
          + node.getI18nKey()
          + " ("
          + node.getHashId()
          + ")"
          + ", hasTarget="
          + (target != null));
    }
    if (target == null) {
      // User has clicked an menu to open in new browser window.
      // Need to redirect for getting a new menu instance.
      setResponsePage(CalendarPage.class);
      return;
    }
    final StringBuffer prependJavascriptBuf = new StringBuffer();
    {
      // Add all childs
      final WebMarkupContainer row = getTreeRowAfter(node.getHashId());
      refresh(); // Force to rebuild tree list.
      for (final MenuTreeTableNode child : menu.getDescendants(getTreeList(), node)) {
        final WebMarkupContainer newRow = createTreeRow(child);
        rowRepeater.add(newRow);
        if (row != null) {
          prependJavascriptBuf.append(WicketAjaxUtils.insertBefore(rowContainer.getMarkupId(), row.getMarkupId(), "li", newRow
              .getMarkupId()));
        } else {
          prependJavascriptBuf.append(WicketAjaxUtils.appendChild(rowContainer.getMarkupId(), "li", newRow.getMarkupId()));
        }
        target.addComponent(newRow);
      }
    }
    {
      // Replace closed-folder-icon by opened-folder-icon.
      replaceFolderImage(target, clickedLink, node, prependJavascriptBuf);
    }
    final String javaScript = prependJavascriptBuf.toString();
    if (javaScript.length() > 0) {
      target.prependJavascript(javaScript);
    }
  }

  private void closeMenu(final AjaxRequestTarget target, final AbstractLink clickedLink, final MenuTreeTableNode node)
  {
    if (log.isDebugEnabled() == true) {
      log.debug("close menu="
          + this.hashCode()
          + ", entry="
          + node.getI18nKey()
          + " ("
          + node.getHashId()
          + ")"
          + ", hasTarget="
          + (target != null));
    }
    if (target == null) {
      // User has clicked an menu to close in new browser window.
      // Need to redirect for getting a new menu instance.
      setResponsePage(CalendarPage.class);
      return;
    }
    // Remove all childs
    final StringBuffer prependJavascriptBuf = new StringBuffer();
    @SuppressWarnings("unchecked")
    final Iterator<WebMarkupContainer> it = (Iterator<WebMarkupContainer>) rowRepeater.iterator();
    final List<WebMarkupContainer> toRemove = new ArrayList<WebMarkupContainer>();
    while (it.hasNext() == true) {
      final WebMarkupContainer row = it.next();
      final MenuTreeTableNode model = (MenuTreeTableNode) row.getDefaultModelObject();
      if (node.isParentOf(model) == true) {
        prependJavascriptBuf.append(WicketAjaxUtils.removeChild(rowContainer.getMarkupId(), row.getMarkupId()));
        toRemove.add(row);
      }
    }
    for (final WebMarkupContainer row : toRemove) {
      rowRepeater.remove(row);
    }
    {
      // Replace opened-folder-icon by closed-folder-icon.
      replaceFolderImage(target, clickedLink, node, prependJavascriptBuf);
    }
    final String javaScript = prependJavascriptBuf.toString();
    if (javaScript.length() > 0) {
      target.prependJavascript(javaScript);
    }
  }

  private void replaceFolderImage(final AjaxRequestTarget target, final AbstractLink clickedLink, final MenuTreeTableNode node,
      final StringBuffer prependJavascriptBuf)
  {
    if (target == null) {
      // If user clicked on "opened in new window".
      return;
    }
    final ContextImage oldImage = (ContextImage) clickedLink.get("treeImage");
    final ContextImage newImage = createImage(node);
    prependJavascriptBuf.append(WicketAjaxUtils.replaceChild(clickedLink.getMarkupId(), oldImage.getMarkupId(), "img", newImage
        .getMarkupId()));
    clickedLink.remove(oldImage);
    clickedLink.add(newImage);
    target.addComponent(newImage);
  }

  private WebMarkupContainer createTreeRow(final MenuTreeTableNode node)
  {
    final WebMarkupContainer row = new WebMarkupContainer(rowRepeater.newChildId(), new Model<MenuTreeTableNode>(node));
    row.setOutputMarkupId(true);
    AbstractLink link = null;
    final String labelString = HtmlHelper.escapeXml(getString(node.getI18nKey()));
    final String htmlSuffix;
    if (node.getHtmlSuffix() != null) {
      htmlSuffix = node.getHtmlSuffix().getObject();
    } else {
      htmlSuffix = "";
    }
    final Label nameLabel = new Label("name", labelString + htmlSuffix);
    nameLabel.setEscapeModelStrings(false);
    nameLabel.setRenderBodyOnly(true);
    link = createFolderLink(node);
    row.add(link);
    link.add(new PresizedImage("menuIcon", getResponse(), node.getIcon()));
    link.add(nameLabel);
    return row;
  }

  @SuppressWarnings("serial")
  private AbstractLink createFolderLink(final MenuTreeTableNode node)
  {
    AbstractLink link;
    if (node.hasChilds() == false) {
      if (node.isWicketPage() == true) {
        link = new Link<String>("link") {
          @Override
          public void onClick()
          {
            if (node.getParams() == null) {
              setResponsePage(node.getPageClass());
            } else {
              final PageParameters params = WicketUtils.getPageParameters(node.getParams());
              setResponsePage(node.getPageClass(), params);
            }
          }
        };
      } else {
        link = new ExternalLink("link", WicketUtils.getUrl(getResponse(), node.getUrl(), true));
      }
      if (node.isNewWindow() == true) {
        link.add(new SimpleAttributeModifier("target", "_blank"));
      }
    } else {
      link = new AjaxFallbackLink<MenuTreeTableNode>("link", new Model<MenuTreeTableNode>(node)) {
        public void onClick(final AjaxRequestTarget target)
        {
          if (menu == null) {
            getMenu();
          }
          if (getModelObject().isOpened() == true) {
            menu.setOpenedStatusOfNode(TreeTableEvent.CLOSE, node.getHashId());
            closeMenu(target, this, node);
          } else {
            menu.setOpenedStatusOfNode(TreeTableEvent.OPEN, node.getHashId());
            openMenu(target, this, node);
          }
          persistOpenNodes();
        };
      };
      link.setOutputMarkupId(true);
    }
    final ContextImage image = createImage(node);
    link.add(image);
    final RepeatingView imageRepeater = new RepeatingView("imageRepeater");
    imageRepeater.setRenderBodyOnly(true);
    link.add(imageRepeater);
    addImage(imageRepeater, (MenuTreeTableNode) node.getParent());
    return link;
  }

  private ContextImage createImage(final MenuTreeTableNode node)
  {
    final String treeImage;
    if (node.hasChilds() == false) {
      treeImage = menu.hasNextSibling(node) == true ? IMAGE_LEAF : IMAGE_LEAF_BOTTOM;
    } else if (node.isOpened() == true) {
      treeImage = menu.hasNextSibling(node) == true ? IMAGE_OPENED : IMAGE_OPENED_BOTTOM;
    } else {
      treeImage = menu.hasNextSibling(node) == true ? IMAGE_CLOSED : IMAGE_CLOSED_BOTTOM;
    }
    final ContextImage image = new PresizedImage("treeImage", getResponse(), treeImage);
    if (node.hasChilds() == true) {
      image.setOutputMarkupId(true);
    }
    return image;
  }

  /**
   * Return the row after the row with the given id. If the row is the last row then null is returned.
   * @param hashId
   * @return
   */
  protected WebMarkupContainer getTreeRowAfter(final Serializable hashId)
  {
    final MenuTreeTableNode node = menu.getElementAfter(getTreeList(), hashId);
    if (node == null) {
      return null;
    } else {
      return getTreeRow(node.getHashId());
    }
  }

  protected WebMarkupContainer getTreeRow(final Serializable hashId)
  {
    @SuppressWarnings("unchecked")
    final Iterator<WebMarkupContainer> it = (Iterator<WebMarkupContainer>) rowRepeater.iterator();
    while (it.hasNext() == true) {
      final WebMarkupContainer child = it.next();
      final MenuTreeTableNode node = (MenuTreeTableNode) child.getDefaultModelObject();
      if (node.getHashId().equals(hashId) == true) {
        return child;
      }
    }
    return null;
  }

  /**
   * Prepend explore images (vertical lines etc.).
   * @param imageRepeater
   * @param node
   */
  private void addImage(final RepeatingView imageRepeater, final MenuTreeTableNode node)
  {
    if (node.getParent() != null) {
      addImage(imageRepeater, (MenuTreeTableNode) node.getParent());
      final WebMarkupContainer imageItem = new WebMarkupContainer(imageRepeater.newChildId());
      imageRepeater.add(imageItem);
      if (menu.hasNextSibling(node) == true) {
        imageItem.add(new PresizedImage("treeNaviImage", getResponse(), IMAGE_VERTICAL_LINE));
      } else {
        imageItem.add(new PresizedImage("treeNaviImage", getResponse(), IMAGE_EMPTY));
      }
    }
  }

  private MenuTreeTable getMenu()
  {
    AbstractSecuredPage securedPage = null;
    if (getPage() instanceof AbstractSecuredPage) {
      securedPage = ((AbstractSecuredPage) getPage());
      menu = (MenuTreeTable) securedPage.getUserPrefEntry(USER_PREF_MENU_KEY);
      if (menu != null) {
        return menu;
      }
    }
    if (menu != null) { // After getting menu from user pref entry, because otherwise resetMenu() doesn't work if menu is stored in this
                        // panel.
      return menu;
    }
    if (log.isDebugEnabled() == true) {
      log.debug("Build new menu.");
    }
    menu = menuBuilder.build(PFUserContext.getUser());
    if (securedPage != null) {
      securedPage.putUserPrefEntry(USER_PREF_MENU_KEY, menu, false);
    }
    @SuppressWarnings("unchecked")
    final Set<Serializable> openedNodes = (Set<Serializable>) ((AbstractSecuredBasePage) getPage())
        .getUserPrefEntry(USER_PREF_KEY_OPENED_NODES);
    if (openedNodes != null) {
      final Set<Serializable> set = new HashSet<Serializable>();
      set.addAll(openedNodes);
      menu.setOpenNodes(set);
      if (log.isDebugEnabled() == true) {
        log.debug("openedNodes sucessfully get from user preferences, menu=" + this.hashCode() + ", opened=" + menu.getOpenNodes());
      }
    }
    return menu;
  }

  private void persistOpenNodes()
  {
    if (menu == null) {
      return;
    }
    // final Set<Serializable> openedNodes = menu.getOpenNodes();
    ((AbstractSecuredPage) getPage()).putUserPrefEntry(USER_PREF_KEY_OPENED_NODES, menu.getOpenNodes(), true);
    if (log.isDebugEnabled() == true) {
      log.debug("openedNodes sucessfully persisted in the user's preferences, menu=" + this.hashCode() + ", opened=" + menu.getOpenNodes());
    }
  }

  protected void refresh()
  {
    treeList = null;
  }

  /**
   * Should be used in tree table view. The current tree will be returned for tree navigation.
   * @return
   */
  private List<MenuTreeTableNode> getTreeList()
  {
    if (treeList == null) {
      treeList = getMenu().getNodeList();
    }
    return treeList;
  }
}
