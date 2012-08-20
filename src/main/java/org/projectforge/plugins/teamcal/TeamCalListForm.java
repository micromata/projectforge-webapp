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
import org.projectforge.web.wicket.AbstractListForm;

/**
 * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
 *
 */
public class TeamCalListForm extends AbstractListForm<TeamCalFilter, TeamCalListPage>
{
  private static final long serialVersionUID = 3659495003810851072L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamCalListForm.class);

  public TeamCalListForm(final TeamCalListPage parentPage){
    super(parentPage);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListForm#newSearchFilterInstance()
   */
  @Override
  protected TeamCalFilter newSearchFilterInstance()
  {
    return new TeamCalFilter();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListForm#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
