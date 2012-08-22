/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * Creats a top form-panel to add filter functions or other options.
 * 
 * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
 *
 */
public class TeamCalEditForm extends AbstractEditForm<TeamCalDO, TeamCalEditPage>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamCalEditForm.class);

  private static final long serialVersionUID = 1379614008604844519L;

  @SpringBean(name = "teamCalDao")
  private TeamCalDao teamCalDao;

  @SuppressWarnings("unused")
  private String templateName; // Used by Wicket

  /**
   * @param parentPage
   * @param data
   */
  public TeamCalEditForm(final TeamCalEditPage parentPage, final TeamCalDO data)
  {
    super(parentPage, data);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditForm#init()
   */
  @Override
  protected void init()
  {
    super.init();

    if (isNew() == true){

      gridBuilder.newGrid16();

      // set title
      {
        final FieldsetPanel fs = gridBuilder.newFieldset("Titel");
        final RequiredMaxLengthTextField title = new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data,
            "title"));
        if (isNew() == true) {
          title.add(WicketUtils.setFocus());
        }
        fs.add(title);
      }

      // set description
      {
        final FieldsetPanel fs = gridBuilder.newFieldset(getString("description"));
        fs.add(new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(data, "description"))).setAutogrow();
      }

      // set owner
      {
        data.setOwner(getUser());
        final FieldsetPanel fs = gridBuilder.newFieldset(Model.of("Ersteller").getObject());
        fs.add(new Label(fs.newChildId(), getUser().getUsername() + ""));
      }
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditForm#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return log;
  }

}
