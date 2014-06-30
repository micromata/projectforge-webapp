/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.teamcal.event;

import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.plugins.teamcal.admin.TeamCalCache;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * @author Werner Feder (w.feder.extern@micromata.de)
 *
 */
public class LocalInvitationEditForm extends AbstractEditForm<LocalInvitationDO, LocalInvitationEditPage>
{
  private static final long serialVersionUID = 1959971894544537950L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocalInvitationEditForm.class);

  protected DropDownChoice<TeamCalDO> calDropDownChoice;

  public LocalInvitationEditForm(final LocalInvitationEditPage parentPage, final LocalInvitationDO data)
  {
    super(parentPage, data);
  }

  @Override
  protected void init()
  {
    super.init();
    gridBuilder.newGridPanel();
    {
      // Subject
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.event.subject"));
      fs.add(new Label(fs.newChildId(), new PropertyModel<String>(data, "teamEvent.subject")));
    }
    {
      // Start date
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.event.startDate"));
      fs.add(new Label(fs.newChildId(), new PropertyModel<Date>(data, "teamEvent.startDate")));
    }
    {
      // End date
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.event.endDate"));
      fs.add(new Label(fs.newChildId(), new PropertyModel<Date>(data, "teamEvent.endDate")));
    }
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.title.list"));
      {
        final TeamCalCache teamCalCache = new TeamCalCache();
        final TeamCalDO[] ownCals = teamCalCache.getAllOwnCalendars().toArray(new TeamCalDO[0]);
        final LabelValueChoiceRenderer<TeamCalDO> calChoiceRenderer = new LabelValueChoiceRenderer<TeamCalDO>();
        for (int i=0; i < ownCals.length; i++) {
          calChoiceRenderer.addValue(ownCals[i], ownCals[i].getTitle());
        }
        calDropDownChoice = new DropDownChoice<TeamCalDO>(fs.getDropDownChoiceId(),
            new PropertyModel<TeamCalDO>(data, "teamEvent.calendar"), calChoiceRenderer.getValues(), calChoiceRenderer);
        calDropDownChoice.setNullValid(false);
        calDropDownChoice.setRequired(true);
        fs.add(calDropDownChoice);
      }
    }

  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }

}
