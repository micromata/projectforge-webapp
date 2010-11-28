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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.projectforge.web.wicket.components.ContentMenuEntryPanel;

/** All pages with required login should be derived from this page. */
public abstract class AbstractSecuredPage extends AbstractSecuredBasePage
{
  protected WebMarkupContainer contentMenuArea;

  /**
   * List to create content menu in the desired order before creating the RepeatingView.
   */
  protected List<ContentMenuEntryPanel> contentMenuEntries = new ArrayList<ContentMenuEntryPanel>();

  // Needed for generating RepeatingView in onBeforeRender() if not already generated.
  private boolean contentMenuRendered = false;

  private MyRepeatingView contentMenu;

  protected WebMarkupContainer dropDownMenu;

  /**
   * If set then return after save, update or cancel to this page. If not given then return to given list page.
   */
  protected AbstractSecuredPage returnToPage;

  /**
   * List to create drop down menu in the desired order before creating the RepeatingView.
   */
  protected List<WebMarkupContainer> dropDownMenuEntries = new ArrayList<WebMarkupContainer>();

  // Needed for generating RepeatingView in onBeforeRender() if not already generated.
  private boolean dropDownMenuRendered = false;

  private RepeatingView dropDownMenuRepeater;

  @SuppressWarnings("serial")
  public AbstractSecuredPage(final PageParameters parameters)
  {
    super(parameters);
    final Model<String> alertMessageModel = new Model<String>() {
      public String getObject()
      {
        if (WicketApplication.getAlertMessage() == null) {
          return "neverDisplayed";
        }
        return WicketApplication.getAlertMessage();
      }
    };
    final Label alertMessageLabel = new Label("alertMessage", alertMessageModel) {
      @Override
      public boolean isVisible()
      {
        return (WicketApplication.getAlertMessage() != null);
      }
    };
    body.add(alertMessageLabel);
    contentMenuArea = new WebMarkupContainer("contentMenuArea") {
      @Override
      public boolean isVisible()
      {
        return contentMenu.isVisible() || dropDownMenu.isVisible();
      }
    };
    body.add(contentMenuArea);
    contentMenuArea.setVisible(false);
    contentMenu = new MyRepeatingView("menu");
    contentMenu.setRenderBodyOnly(true);
    contentMenuArea.add(contentMenu);
    dropDownMenu = new WebMarkupContainer("dropDownMenu");
    dropDownMenu.add(new PresizedImage("cogImage", getResponse(), WebConstants.IMAGE_COG));
    dropDownMenu.add(new PresizedImage("arrowDownImage", getResponse(), WebConstants.IMAGE_ARROW_DOWN));
    dropDownMenuRepeater = new MyRepeatingView("menu");
    dropDownMenu.add(dropDownMenuRepeater);
    dropDownMenu.setVisible(false);
    contentMenuArea.add(dropDownMenu);
  }

  /**
   * If set then return after save, update or cancel to this page. If not given then return to given list page. As an alternative you can
   * set the returnToPage as a page parameter (if supported by the derived page).
   * @param returnToPage
   */
  public AbstractSecuredPage setReturnToPage(AbstractSecuredPage returnToPage)
  {
    this.returnToPage = returnToPage;
    return this;
  }

  protected void addContentMenuEntry(final ContentMenuEntryPanel panel)
  {
    this.contentMenuEntries.add(panel);
  }

  protected String getNewContentMenuChildId()
  {
    return this.contentMenu.newChildId();
  }

  public void addDropDownMenuEntry(final WebMarkupContainer entry)
  {
    this.dropDownMenuRepeater.add(entry);
  }

  public String getNewDropDownMenuChildId()
  {
    return this.dropDownMenuRepeater.newChildId();
  }

  @Override
  protected void onBeforeRender()
  {
    if (contentMenuRendered == false) {
      if (this.contentMenuEntries.size() > 0) {
        for (final ContentMenuEntryPanel entry : this.contentMenuEntries) {
          this.contentMenu.add(entry);
        }
      }
      contentMenuRendered = true;
    }
    if (dropDownMenuRendered == false) {
      if (this.dropDownMenu.size() > 0) {
        for (final WebMarkupContainer entry : this.dropDownMenuEntries) {
          this.dropDownMenu.add(entry);
        }
      }
      dropDownMenuRendered = true;
    }
    super.onBeforeRender();
  }

  /**
   * This repeating view is only visible if any child was added.
   * @author Kai Reinhard (k.reinhard@micromata.de)
   * 
   */
  protected class MyRepeatingView extends RepeatingView
  {
    private static final long serialVersionUID = 1534625043282794990L;

    boolean hasChilds = false;

    public MyRepeatingView(final String id)
    {
      super(id);
    }

    @Override
    public String newChildId()
    {
      hasChilds = true;
      return super.newChildId();
    }

    @Override
    public boolean isVisible()
    {
      return hasChilds;
    }
  }
}
