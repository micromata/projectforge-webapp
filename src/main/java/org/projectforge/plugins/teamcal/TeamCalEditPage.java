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
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.core.AbstractBaseDO;
import org.projectforge.core.BaseDao;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.AbstractEditPage;

/**
 * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
 *
 */
public class TeamCalEditPage extends AbstractEditPage implements ISelectCallerPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamCalEditPage.class);

  /**
   * @param parameters
   * @param i18nPrefix
   */
  public TeamCalEditPage(final PageParameters parameters, final String i18nPrefix)
  {
    super(parameters, i18nPrefix);
    // TODO Auto-generated constructor stub
  }

  /**
   * 
   */
  private static final long serialVersionUID = -3352981782657771662L;

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#newEditForm(org.projectforge.web.wicket.AbstractEditPage, org.projectforge.core.AbstractBaseDO)
   */
  @Override
  protected AbstractEditForm newEditForm(final AbstractEditPage parentPage, final AbstractBaseDO data)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#select(java.lang.String, java.lang.Object)
   */
  @Override
  public void select(final String property, final Object selectedValue)
  {
    // TODO Auto-generated method stub
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  @Override
  public void unselect(final String property)
  {
    // TODO Auto-generated method stub
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#cancelSelection(java.lang.String)
   */
  @Override
  public void cancelSelection(final String property)
  {
    // TODO Auto-generated method stub
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#getBaseDao()
   */
  @Override
  protected BaseDao getBaseDao()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    // TODO Auto-generated method stub
    return log;
  }
}
