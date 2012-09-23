/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.web.user;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.user.Login;
import org.projectforge.user.PFUserFilter;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

public class UserListForm extends AbstractListForm<PFUserFilter, UserListPage>
{
  private static final long serialVersionUID = 7625173316784007696L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserListForm.class);

  @Override
  protected void init()
  {
    super.init();
    gridBuilder.newColumnsPanel();
    {
      gridBuilder.newColumnPanel(DivType.COL_60);
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("label.options"), true).setNoLabelFor();

      {
        // DropDownChoice deactivated
        final LabelValueChoiceRenderer<Boolean> deactivatedRenderer = new LabelValueChoiceRenderer<Boolean>();
        deactivatedRenderer.addValue(false, getString("user.activated"));
        deactivatedRenderer.addValue(true, getString("user.deactivated"));
        final DropDownChoice<Boolean> deactivatedChoice = new DropDownChoice<Boolean>(fs.getDropDownChoiceId(), new PropertyModel<Boolean>(
            getSearchFilter(), "deactivatedUser"), deactivatedRenderer.getValues(), deactivatedRenderer);
        deactivatedChoice.setNullValid(true);
        fs.add(deactivatedChoice, true).setTooltip(getString("user.deactivated"));
      }
      if (Login.getInstance().hasExternalUsermanagementSystem() == true) {
        {
          // DropDownChoice restricted
          final LabelValueChoiceRenderer<Boolean> restrictedRenderer = new LabelValueChoiceRenderer<Boolean>();
          restrictedRenderer.addValue(false, getString("user.restricted.not"));
          restrictedRenderer.addValue(true, getString("user.restricted"));
          final DropDownChoice<Boolean> restrictedChoice = new DropDownChoice<Boolean>(fs.getDropDownChoiceId(),
              new PropertyModel<Boolean>(getSearchFilter(), "restrictedUser"), restrictedRenderer.getValues(), restrictedRenderer);
          restrictedChoice.setNullValid(true);
          fs.add(restrictedChoice, true).setTooltip(getString("user.restrictedUser"));
        }
        {
          // DropDownChoice localUser
          final LabelValueChoiceRenderer<Boolean> localUserRenderer = new LabelValueChoiceRenderer<Boolean>();
          localUserRenderer.addValue(false, getString("user.localUser.not"));
          localUserRenderer.addValue(true, getString("user.localUser"));
          final DropDownChoice<Boolean> localUserChoice = new DropDownChoice<Boolean>(fs.getDropDownChoiceId(),
              new PropertyModel<Boolean>(getSearchFilter(), "localUser"), localUserRenderer.getValues(), localUserRenderer);
          localUserChoice.setNullValid(true);
          fs.add(localUserChoice, true).setTooltip(getString("user.localUser"));
        }
      }
      final DivPanel checkBoxPanel = fs.addNewCheckBoxDiv();
      checkBoxPanel.add(createOnlyDeletedCheckBoxPanel(checkBoxPanel.newChildId()));
    }
    {
      // DropDownChoice page size
      gridBuilder.newColumnPanel(DivType.COL_40);
      addPageSizeFieldset();
    }
  }

  public UserListForm(final UserListPage parentPage)
  {
    super(parentPage);
  }

  @Override
  protected PFUserFilter newSearchFilterInstance()
  {
    return new PFUserFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
