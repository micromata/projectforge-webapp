/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.address;

import org.apache.log4j.Logger;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.PersonalAddressDao;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.EditPage;

/**
 * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
 * 
 */
@SuppressWarnings("serial")
@EditPage(defaultReturnPage = AddressListPage.class)
public class AddressImportPage extends AbstractEditPage<AddressDO, AddressImportForm, AddressDao>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddressImportPage.class);

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  @SpringBean(name = "personalAddressDao")
  private PersonalAddressDao personalAddressDao;

  /**
   * @param parameters
   */
  public AddressImportPage(final PageParameters parameters)
  {
    super(parameters, "address.book.vCardImport");
    init();
  }

  @Override
  protected void onInitialize()
  {
    super.onInitialize();

  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#create()
   */
  @Override
  protected void create()
  {
    getForm().create();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#getBaseDao()
   */
  @Override
  protected AddressDao getBaseDao()
  {
    return addressDao;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return log;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#newEditForm(org.projectforge.web.wicket.AbstractEditPage,
   *      org.projectforge.core.AbstractBaseDO)
   */
  @Override
  protected AddressImportForm newEditForm(final AbstractEditPage< ? , ? , ? > parentPage, final AddressDO data)
  {
    return new AddressImportForm(this, data);
  }

}
