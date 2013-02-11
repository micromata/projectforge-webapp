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

import org.apache.commons.lang3.ObjectUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

/**
 * Represents a text panel showing an accept and a discard image to accept or discard changes.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class DiffAcceptDiscardPanel<T> extends Panel
{
  private static final long serialVersionUID = -791300271068233781L;

  private IconLinkPanel acceptLink, discardLink;

  /**
   * Constructs two PropertyModels.
   * @param id
   * @param formComponent
   * @param newDataObject
   * @param oldDataObject
   * @param propertyExpression
   */
  public DiffAcceptDiscardPanel(final String id, final FormComponent<T> formComponent, final Object newDataObject, final Object oldDataObject,
      final String propertyExpression)
  {
    this(id, formComponent, new PropertyModel<T>(newDataObject, propertyExpression), new PropertyModel<T>(oldDataObject, propertyExpression));
  }

  @SuppressWarnings("serial")
  public DiffAcceptDiscardPanel(final String id, final FormComponent<T> formComponent, final IModel<T> newValue, final IModel<T> oldValue)
  {
    super(id);
    add(acceptLink = new IconLinkPanel("acceptImage", IconType.ACCEPT, new AjaxLink<Void>(IconLinkPanel.LINK_ID) {
      /**
       * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
       */
      @Override
      public void onClick(final AjaxRequestTarget target)
      {
        acceptLink.setVisible(false);
        discardLink.setVisible(false);
        target.add(acceptLink, discardLink);
      }
    }) {
      /**
       * @see org.apache.wicket.Component#isVisible()
       */
      @Override
      public boolean isVisible()
      {
        return ObjectUtils.equals(newValue.getObject(), oldValue.getObject());
      }
    });
    acceptLink.setOutputMarkupId(true);
    add(discardLink = new IconLinkPanel("discardImage", IconType.ACCEPT, new AjaxLink<Void>(IconLinkPanel.LINK_ID) {
      /**
       * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
       */
      @Override
      public void onClick(final AjaxRequestTarget target)
      {
        acceptLink.setVisible(false);
        discardLink.setVisible(false);
        formComponent.getModel().setObject(oldValue.getObject());
        formComponent.modelChanged();
        target.add(formComponent, acceptLink, discardLink);
      }
    }) {
      /**
       * @see org.apache.wicket.Component#isVisible()
       */
      @Override
      public boolean isVisible()
      {
        return ObjectUtils.equals(newValue.getObject(), oldValue.getObject());
      }
    });
    discardLink.setOutputMarkupId(true);
    discardLink.setTooltip(new Model<String>() {
      /**
       * @see org.apache.wicket.model.Model#getObject()
       */
      @Override
      public String getObject()
      {
        return convertToString(oldValue.getObject());
      }
    });
  }

  protected String convertToString(final T value)
  {
    return String.valueOf(value);
  }
}
