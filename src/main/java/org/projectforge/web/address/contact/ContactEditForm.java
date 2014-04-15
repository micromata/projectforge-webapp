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

package org.projectforge.web.address.contact;


import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.contact.ContactDO;
import org.projectforge.address.contact.ContactDao;
import org.projectforge.address.contact.ContactEntryDO;
import org.projectforge.address.contact.PersonalContactDao;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.bootstrap.GridSize;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;


/**
 * This is the edit formular page.
 * @author Werner Feder (werner.feder@t-online.de)
 */
public class ContactEditForm extends AbstractEditForm<ContactDO, ContactEditPage>
{

  private static final long serialVersionUID = 7930242750045989712L;

  private static final Logger log = Logger.getLogger(ContactEditForm.class);

  private EmailsPanel emailsPanel;

  private PhonesPanel phonesPanel;

  private ImsPanel imsPanel;

  private ContactEntryPanel contactEntryPanel;

  @SpringBean(name = "personalContactDao")
  private PersonalContactDao personalContactDao;

  protected ContactPageSupport contactEditSupport;

  @SpringBean(name = "contactDao")
  private ContactDao contactDao;

  /**
   * @param parentPage
   * @param data
   */
  public ContactEditForm(final ContactEditPage parentPage, final ContactDO data)
  {
    super(parentPage, data);
  }

  @Override
  public void init()
  {
    super.init();
    contactEditSupport = new ContactPageSupport(this, gridBuilder, (ContactDao) getBaseDao(), personalContactDao, data);

    gridBuilder.newSplitPanel(GridSize.COL75);

    contactEditSupport.addName();
    contactEditSupport.addFirstName();
    final FieldsetPanel fs = (FieldsetPanel)contactEditSupport.addFormOfAddress();
    final DivPanel checkBoxPanel = fs.addNewCheckBoxDiv();
    checkBoxPanel.addCheckBox(new PropertyModel<Boolean>(contactEditSupport.personalContact, "favoriteCard"), getString("favorite"),
        getString("address.tooltip.vCardList"));
    contactEditSupport.addTitle();
    contactEditSupport.addOrganization();
    contactEditSupport.addWebsite();

    contactEditSupport.addBirthday();
    contactEditSupport.addLanguage();
    contactEditSupport.addContactStatus();
    contactEditSupport.addAddressStatus();

    contactEditSupport.addFingerPrint();
    contactEditSupport.addPublicKey();

    // Emails
    FieldsetPanel fs2 = gridBuilder.newFieldset(ContactDO.class, "emailValues").suppressLabelForWarning();
    emailsPanel = new EmailsPanel(fs.newChildId(), getData().getEmailValues());
    fs2.add(emailsPanel);

    // Phones
    fs2 = gridBuilder.newFieldset(ContactDO.class, "phoneValues").suppressLabelForWarning();
    fs2.add(phonesPanel = new PhonesPanel(fs.newChildId(), getData().getPhoneValues()));

    // Instant Messaging Entries
    fs2 = gridBuilder.newFieldset(ContactDO.class, "imValues").suppressLabelForWarning();
    fs2.add(imsPanel = new ImsPanel(fs.newChildId(), getData().getImValues()));

    // Contacts
    fs2 = gridBuilder.newFieldset(ContactDO.class, "contacts").suppressLabelForWarning();
    fs2.add(contactEntryPanel = new ContactEntryPanel(fs.newChildId(), data, new PropertyModel<List<ContactEntryDO>>(data, "contacts")));

  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditForm#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return log;
  }

  public EmailsPanel getEmailsPanel()
  {
    return emailsPanel;
  }

  public PhonesPanel getPhonesPanel()
  {
    return phonesPanel;
  }

  public ImsPanel getImsPanel()
  {
    return imsPanel;
  }

  public ContactEntryPanel getContactEntryPanel()
  {
    return contactEntryPanel;
  }

}
