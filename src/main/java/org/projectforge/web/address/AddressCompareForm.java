/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.address;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.address.AddressDO;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.bootstrap.GridSize;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 *
 */
public class AddressCompareForm extends AbstractEditForm<AddressDO, AddressComparePage>
{

  /**
   * 
   */
  private static final long serialVersionUID = 4990179617114170795L;
  private final AddressDO dataOld;

  /**
   * @param parentPage
   * @param data
   */
  public AddressCompareForm(final AddressComparePage parentPage, final AddressDO dataNew, final AddressDO dataOld)
  {
    super(parentPage, dataOld);
    this.dataOld = dataOld;
    data = dataNew;
  }

  /**
   * @see org.apache.wicket.Component#onInitialize()
   */
  @Override
  protected void onInitialize()
  {
    super.onInitialize();
    feedbackPanel.error(getString("address.book.vCardImport.existingEntry"));

    // new address
    gridBuilder.newSplitPanel(GridSize.COL50);

    initFields(dataOld, false);

    // existing address
    if (data != null) {
      gridBuilder.newSplitPanel(GridSize.COL50);
      initFields(data, true);
    }
  }

  private void initFields(final AddressDO address, final boolean enabled) {
    final FieldsetPanel fsName = gridBuilder.newFieldset("Name", true);
    fsName.add(new TextField<String>(fsName.getTextFieldId(), new PropertyModel<String>(address, "name")));
    fsName.setEnabled(enabled);
    if (StringUtils.equals(dataOld.getName(), data.getName()) == true) {
      setMark(fsName);
    }

    final FieldsetPanel fsFirstName = gridBuilder.newFieldset("FirstName", true);
    fsFirstName.add(new TextField<String>(fsFirstName.getTextFieldId(), new PropertyModel<String>(address, "firstName")));
    fsFirstName.setEnabled(enabled);
    if (StringUtils.equals(dataOld.getFirstName(), data.getFirstName()) == true) {
      setMark(fsFirstName);
    }

    final FieldsetPanel fsEmailBusiness = gridBuilder.newFieldset("Mail gesch.", true);
    fsEmailBusiness.add(new TextField<String>(fsEmailBusiness.getTextFieldId(), new PropertyModel<String>(address, "email")));
    fsEmailBusiness.setEnabled(enabled);
    if (StringUtils.equals(dataOld.getEmail(), data.getEmail()) == true) {
      setMark(fsEmailBusiness);
    }

    final FieldsetPanel fsBusinessPhone = gridBuilder.newFieldset("Tele gesch.", true);
    fsBusinessPhone.add(new TextField<String>(fsBusinessPhone.getTextFieldId(), new PropertyModel<String>(address, "businessPhone")));
    fsBusinessPhone.setEnabled(enabled);
    if (StringUtils.equals(dataOld.getBusinessPhone(), data.getBusinessPhone()) == true) {
      setMark(fsBusinessPhone);
    }

    final FieldsetPanel fsMobilBusiness = gridBuilder.newFieldset("Mobil gesch.", true);
    fsMobilBusiness.add(new TextField<String>(fsMobilBusiness.getTextFieldId(), new PropertyModel<String>(address, "mobilePhone")));
    fsMobilBusiness.setEnabled(enabled);
    if (StringUtils.equals(dataOld.getMobilePhone(), data.getMobilePhone()) == true) {
      setMark(fsMobilBusiness);
    }

    final FieldsetPanel fsEmailPrivate = gridBuilder.newFieldset("Mail priv.", true);
    fsEmailPrivate.add(new TextField<String>(fsEmailPrivate.getTextFieldId(), new PropertyModel<String>(address, "privateEmail")));
    fsEmailPrivate.setEnabled(enabled);
    if (StringUtils.equals(dataOld.getPrivateEmail(), data.getPrivateEmail()) == true) {
      setMark(fsEmailPrivate);
    }

    final FieldsetPanel fsPrivatePhone = gridBuilder.newFieldset("Tele priv.", true);
    fsPrivatePhone.add(new TextField<String>(fsPrivatePhone.getTextFieldId(), new PropertyModel<String>(address, "privatePhone")));
    fsPrivatePhone.setEnabled(enabled);
    if (StringUtils.equals(dataOld.getPrivatePhone(), data.getPrivatePhone()) == true) {
      setMark(fsPrivatePhone);
    }

    final FieldsetPanel fsMobilPrivate = gridBuilder.newFieldset("Mobil priv.", true);
    fsMobilPrivate.add(new TextField<String>(fsMobilPrivate.getTextFieldId(), new PropertyModel<String>(address, "privateMobilePhone")));
    fsMobilPrivate.setEnabled(enabled);
    if (StringUtils.equals(dataOld.getPrivateMobilePhone(), data.getPrivateMobilePhone()) == true) {
      setMark(fsMobilPrivate);
    }
  }

  /**
   * mark panel, if content already exist.
   * 
   * @param panel
   */
  private void setMark(final FieldsetPanel panel) {
    panel.add(new AttributeModifier("style", new Model<String>("background: lightcyan;")));
  }

  /**
   * 
   */
  public void create()
  {
    data.setId(dataOld.getId());
    getBaseDao().save(data);
    setResponsePage(AddressListPage.class);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditForm#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return null;
  }

}
