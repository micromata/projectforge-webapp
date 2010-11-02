/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.address;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.AddressExport;
import org.projectforge.address.PersonalAddressDO;
import org.projectforge.address.PersonalAddressDao;
import org.projectforge.common.DateHelper;
import org.projectforge.core.Configuration;
import org.projectforge.web.core.BaseAction;
import org.projectforge.web.core.BaseListActionBean;
import org.projectforge.web.core.ResponseUtils;


@StrictBinding
@UrlBinding("/secure/address/AddressList.action")
@BaseAction(jspUrl = "/WEB-INF/jsp/address/addressList.jsp")
public class AddressListAction extends BaseListActionBean<AddressListFilter, AddressDao, PersonalAddressDO>
{
  private static final Logger log = Logger.getLogger(AddressListAction.class);

  private AddressExport addressExport;

  private PersonalAddressDao personalAddressDao;
  
  private Configuration configuration;

  public AddressListAction()
  {
    this.storeRecentSearchTerms = true;
  }

  public void setAddressExport(AddressExport addressExport)
  {
    this.addressExport = addressExport;
  }

  public void setPersonalAddressDao(PersonalAddressDao personalAddressDao)
  {
    this.personalAddressDao = personalAddressDao;
  }
  
  public void setConfiguration(Configuration configuration)
  {
    this.configuration = configuration;
  }

  /**
   * Embeds all addresses in PersonalAddressDO, so the marked addresses and phone numbers could be marked in the ui.
   * @see org.projectforge.web.core.BaseListActionBean#buildList()
   */
  @Override
  protected List<PersonalAddressDO> buildList()
  {
    if (actionFilter.isNewest() == true && StringUtils.isBlank(actionFilter.getSearchString()) == true) {
      actionFilter.setMaxRows(getPageSize());
    }
    List<AddressDO> addresses = (List<AddressDO>) baseDao.getList(actionFilter);
    Map<Integer, PersonalAddressDO> map = personalAddressDao.getPersonalAddressByAddressId();
    List<PersonalAddressDO> result = new ArrayList<PersonalAddressDO>();
    if (addresses != null) {
      for (AddressDO address : addresses) {
        PersonalAddressDO pa = map.get(address.getId());
        if (pa == null) {
          pa = new PersonalAddressDO();
        }
        pa.setAddress(address);
        result.add(pa);
      }
    }
    addRecentSearchTerm(actionFilter);
    return result;
  }

  @DontValidate
  public Resolution searchAutocomplete()
  {
    String q = getAjaxAutocompleteValue();
    final String result = baseDao.getAutocompletion(q, true, true, "name", "firstName", "organization");
    return getJsonResolution(result);
  }

  
  public boolean isSmsEnabled()
  {
    return StringUtils.isNotEmpty(configuration.getSmsUrl()) == true;
  }

  /**
   * return true if search string in filter is given (not blank), otherwise false.
   * @see org.projectforge.web.core.BaseListActionBean#isShowResultInstantly()
   */
  @Override
  protected boolean isShowResultInstantly()
  {
    return StringUtils.isNotBlank(getActionFilter().getSearchString()) || (getActionFilter().isFilter() == false);
  }

  public void setAddressDao(AddressDao addressDao)
  {
    this.baseDao = addressDao;
  }

  @Override
  protected AddressListFilter createFilterInstance()
  {
    return new AddressListFilter();
  }

  /**
   * Exports the filtered list as table with almost all fields.
   * 
   * @see AddressExport#export(List)
   */
  public Resolution export()
  {
    log.info("Exporting address list.");
    List<PersonalAddressDO> l = getList();
    if (l == null) {
      l = buildList();
    }
    byte[] xls = addressExport.export(l);
    if (xls == null || xls.length == 0) {
      addGlobalError("address.book.hasNoVCards");
      return getInputPage();
    }
    String filename = "ProjectForge-AddressExport_" + DateHelper.getDateAsFilenameSuffix(new Date()) + ".xls";
    return getDownloadResolution(filename, xls);
  }

  public Resolution exportFavoriteVCards()
  {
    log.info("Exporting personal address book.");
    final List<PersonalAddressDO> list = baseDao.getFavoriteVCards();
    if (CollectionUtils.isEmpty(list) == true) {
      addGlobalError("address.book.hasNoVCards");
      return getInputPage();
    }
    return new Resolution() {
      public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception
      {
        String filename = "ProjectForge-PersonalAddressBook_" + DateHelper.getDateAsFilenameSuffix(new Date()) + ".vcf";
        ResponseUtils.prepareDownload(filename, response, getContext().getServletContext(), true);
        response.setCharacterEncoding("utf-8");
        Writer writer = null;
        try {
          writer = new OutputStreamWriter(response.getOutputStream(), "utf-8");
        } catch (UnsupportedEncodingException ex) {
          log.fatal("Exception encountered " + ex, ex);
          throw new RuntimeException(ex);
        }
        baseDao.exportFavoriteVCards(writer, list);
      }
    };
  }

  public Resolution exportFavoritePhoneList()
  {
    log.info("Exporting phone list");
    final List<PersonalAddressDO> list = baseDao.getFavoritePhoneEntries();
    if (CollectionUtils.isEmpty(list) == true) {
      addGlobalError("address.book.hasNoPhoneNumbers");
      return getInputPage();
    }
    return new Resolution() {
      public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception
      {
        String filename = "ProjectForge-PersonalPhoneList_" + DateHelper.getDateAsFilenameSuffix(new Date()) + ".txt";
        ResponseUtils.prepareDownload(filename, response, getContext().getServletContext(), true);
        Writer writer = null;
        try {
          writer = new OutputStreamWriter(response.getOutputStream(), "iso-8859-1");
        } catch (UnsupportedEncodingException ex) {
          log.fatal("Exception encountered " + ex, ex);
          throw new RuntimeException(ex);
        }
        baseDao.exportFavoritePhoneList(writer, list);
      }
    };
  }

  /**
   * Needed only for StrictBinding. If method has same signature as super.getActionFilter then stripes ignores these validate settings
   * (bug?).
   */
  @ValidateNestedProperties( { @Validate(field = "searchString"), @Validate(field = "active"), @Validate(field = "listType"),
      @Validate(field = "uptodate"), @Validate(field = "leaved"), @Validate(field = "departed"), @Validate(field = "outdated"),
      @Validate(field = "nonActive"), @Validate(field = "uninteresting"), @Validate(field = "personaIngrata")})
  public AddressListFilter getFilter()
  {
    return super.getActionFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
