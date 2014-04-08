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


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.FormOfAddress;
import org.projectforge.address.contact.ContactDO;
import org.projectforge.address.contact.ContactDao;
import org.projectforge.address.contact.ContactType;
import org.projectforge.address.contact.EmailValue;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.bootstrap.GridSize;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.flowlayout.FieldProperties;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;


/**
 * This is the edit formular page.
 * @author Werner Feder (werner.feder@t-online.de)
 */
public class ContactEditForm extends AbstractEditForm<ContactDO, ContactEditPage>
{

  private static final long serialVersionUID = 7930242750045989712L;

  private static final Logger log = Logger.getLogger(ContactEditForm.class);

  @SpringBean(name = "contactDao")
  private ContactDao contactDao;

  private EmailsPanel emailsPanel;

  private List<EmailValue> emails;

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

    gridBuilder.newSplitPanel(GridSize.COL50);

    // name
    FieldsetPanel fs = gridBuilder.newFieldset(ContactDO.class, "name");
    final RequiredMaxLengthTextField name = new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data,
        "name"));
    fs.add(name);

    // firstname
    fs = gridBuilder.newFieldset(ContactDO.class, "firstname");
    final MaxLengthTextField firstname = new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "firstname"));
    fs.add(firstname);

    // form
    final FieldProperties<FormOfAddress> props = new FieldProperties<FormOfAddress>("address.form", new PropertyModel<FormOfAddress>(data, "form"));
    fs = gridBuilder.newFieldset(props);
    final LabelValueChoiceRenderer<FormOfAddress> formChoiceRenderer = new LabelValueChoiceRenderer<FormOfAddress>(parentPage, FormOfAddress.values());
    fs.addDropDownChoice(props.getModel(), formChoiceRenderer.getValues(), formChoiceRenderer).setRequired(true).setNullValid(false);

    // title
    fs = gridBuilder.newFieldset(ContactDO.class, "title");
    final MaxLengthTextField title = new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "title"));
    fs.add(title);

    // birthday
    fs = gridBuilder.newFieldset(ContactDO.class, "birthday");
    fs.add(new DatePanel(fs.newChildId(), new PropertyModel<Date>(data, "birthday"), DatePanelSettings.get().withTargetType(
        java.sql.Date.class)));


    // Emails
    emails = contactDao.readEmailValues(getData().getEmailValues());
    if ( emails == null) {
      emails = new ArrayList<EmailValue>();
      final EmailValue e1 = new EmailValue().setEmail("werner.feder@its-feder.de").setContactType(ContactType.BUSINESS.getI18nKey());
      emails.add(e1);
      final EmailValue e2 = new EmailValue().setEmail("werner.feder@t-online.de").setContactType(ContactType.PRIVATE.getI18nKey());
      emails.add(e2);
    }
    fs.add(emailsPanel = new EmailsPanel(fs.newChildId(), emails));

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
