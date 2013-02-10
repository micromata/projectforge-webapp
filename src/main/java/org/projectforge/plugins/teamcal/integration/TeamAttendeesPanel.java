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

package org.projectforge.plugins.teamcal.integration;

import java.util.List;

import org.apache.poi.hssf.record.formula.functions.T;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.plugins.teamcal.event.TeamEventAttendeeDO;
import org.projectforge.web.CSSColor;
import org.projectforge.web.wicket.flowlayout.IconLinkPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class TeamAttendeesPanel extends Panel
{
  private static final long serialVersionUID = 5951744897882589488L;

  private final List<TeamEventAttendeeDO> attendees;

  /**
   * @param id
   */
  @SuppressWarnings("serial")
  public TeamAttendeesPanel(final String id, final List<TeamEventAttendeeDO> attendees)
  {
    super(id);
    this.attendees = attendees;
    final WebMarkupContainer mainContainer = new WebMarkupContainer("main");
    add(mainContainer);
    final RepeatingView repeater = new RepeatingView("li");
    mainContainer.add(repeater);
    for (final TeamEventAttendeeDO attendee : attendees) {
      final WebMarkupContainer item = new WebMarkupContainer(repeater.newChildId());
      repeater.add(item);
      item.add(new AjaxEditableLabel<String>("editableLabel", new PropertyModel<String>(attendee, "url")));
      item.add(new Label("status", "invisible").setVisible(false));
      item.add(new IconLinkPanel("action", IconType.REMOVE_SIGN, new AjaxLink<T>(IconLinkPanel.LINK_ID) {
        /**
         * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        public void onClick(final AjaxRequestTarget target)
        {
        }
      }));
    }
    final WebMarkupContainer item = new WebMarkupContainer(repeater.newChildId());
    repeater.add(item);
    item.add(new Label("editableLabel", "invisible").setVisible(false));
    item.add(new Label("status", "invisible").setVisible(false));
    item.add(new IconLinkPanel("action", IconType.PLUS_SIGN, new AjaxLink<T>(IconLinkPanel.LINK_ID) {
      /**
       * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
       */
      @Override
      public void onClick(final AjaxRequestTarget target)
      {
      }
    }).setColor(CSSColor.GREEN));
  }
}
