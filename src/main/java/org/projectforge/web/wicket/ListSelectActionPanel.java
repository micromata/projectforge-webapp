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

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.projectforge.common.ReflectionHelper;
import org.projectforge.web.fibu.ISelectCallerPage;

/**
 * Panel for selecting list page entries for editing and selecting for callers.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@SuppressWarnings("serial")
public class ListSelectActionPanel extends Panel
{
  public static final String LABEL_ID = "label";

  /**
   * Constructor for list view in selection mode.
   * @param id component id
   * @param model model for contact
   * @param caller The calling page.
   * @param selectProperty The property (name) of the caller to select.
   * @param objectId The id of the object to select on click.
   * @param label The label string to show (additional to the row_pointer.png).
   */
  public ListSelectActionPanel(final String id, final IModel< ? > model, final ISelectCallerPage caller, final String selectProperty,
      final Integer objectId, final String label)
  {
    this(id, model, caller, selectProperty, objectId, new Label(LABEL_ID, label));
  }

  /**
   * Constructor for list view in selection mode.
   * @param id component id
   * @param model model for contact
   * @param caller The calling page.
   * @param selectProperty The property (name) of the caller to select.
   * @param objectId The id of the object to select on click.
   * @param label The label to show (additional to the row_pointer.png). The id of the label should be LABEL_ID.
   */
  public ListSelectActionPanel(final String id, final IModel< ? > model, final ISelectCallerPage caller, final String selectProperty,
      final Integer objectId, final Label label)
  {
    super(id, model);
    @SuppressWarnings("unchecked")
    Link< ? > link = new Link("select") {
      public void onClick()
      {
        WicketUtils.setResponsePage(this, caller);
        caller.select(selectProperty, objectId);
      };
    };
    add(link);
    add(label);
  }

  /**
   * Constructor for normal list view for selecting one entry to edit.
   * @param id component id
   * @param model model for contact
   * @param editClass The edit page to redirect to.
   * @param objectId The id of the object to edit in edit page.
   * @param label The label string to show (additional to the row_pointer.png).
   * @param params Pairs of params (key, value).
   * @see WicketUtils#getPageParameters(String[])
   */
  public ListSelectActionPanel(final String id, final IModel< ? > model, final Class< ? extends WebPage> editClass, final Integer objectId,
      final WebPage returnToPage, final String label, final String... params)
  {
    this(id, model, editClass, objectId, returnToPage, new Label(LABEL_ID, label), params);
  }

  /**
   * Constructor for normal list view for selecting one entry to edit.
   * @param id component id
   * @param model model for contact
   * @param editPageClass The edit page to redirect to.
   * @param objectId The id of the object to edit in edit page.
   * @param label The label to show (additional to the row_pointer.png). The id of the label should be LABEL_ID.
   * @param params Pairs of params (key, value).
   * @see WicketUtils#getPageParameters(String[])
   */
  public ListSelectActionPanel(final String id, final IModel< ? > model, final Class< ? extends WebPage> editPageClass,
      final Integer objectId, final WebPage returnToPage, final Label label, final String... params)
  {
    super(id, model);
    @SuppressWarnings("unchecked")
    Link< ? > link = new Link("select") {
      public void onClick()
      {
        final PageParameters pageParams = WicketUtils.getPageParameters(params);
        if (objectId != null) {
          pageParams.add(AbstractEditPage.PARAMETER_KEY_ID, String.valueOf(objectId));
        }
        final AbstractSecuredPage editPage = (AbstractSecuredPage) ReflectionHelper.newInstance(editPageClass, PageParameters.class,
            pageParams);
        if (editPage instanceof AbstractEditPage) {
          ((AbstractEditPage< ? , ? , ? >) editPage).setReturnToPage(returnToPage);
        }
        setResponsePage(editPage);
      };
    };
    add(link);
    add(label);
  }
}
