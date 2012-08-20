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
import org.projectforge.web.wicket.AbstractEditForm;

/**
 * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
 *
 */
public class TeamCalEditForm extends AbstractEditForm<TeamCalDO, TeamCalEditPage>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamCalEditForm.class);
  /**
   * @param parentPage
   * @param data
   */
  public TeamCalEditForm(final TeamCalEditPage parentPage, final TeamCalDO data)
  {
    super(parentPage, data);
    // TODO Auto-generated constructor stub
  }

  /**
   * 
   */
  private static final long serialVersionUID = 1379614008604844519L;

  /**
   * @see org.projectforge.web.wicket.AbstractEditForm#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    // TODO Auto-generated method stub
    return log;
  }

}
