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

package org.projectforge.fibu;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.projectforge.access.AccessChecker;
import org.projectforge.common.DateFormatType;
import org.projectforge.common.DateFormats;
import org.projectforge.common.NumberHelper;
import org.projectforge.export.ContentProvider;
import org.projectforge.export.ExportColumn;
import org.projectforge.export.ExportSheet;
import org.projectforge.export.ExportWorkbook;
import org.projectforge.export.I18nExportColumn;
import org.projectforge.export.PropertyMapping;
import org.projectforge.export.XlsContentProvider;
import org.projectforge.registry.Registry;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;

/**
 * For excel export.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class OrderExport
{
  private class MyContentProvider extends XlsContentProvider
  {
    public MyContentProvider(final ExportWorkbook workbook)
    {
      super(workbook);
    }

    @Override
    public org.projectforge.export.ContentProvider newInstance()
    {
      return new MyContentProvider(this.workbook);
    }
  };

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OrderExport.class);

  protected AccessChecker accessChecker;

  private AuftragDao auftragDao;

  private RechnungCache rechnungCache;

  private enum Col
  {
    NUMMER, NUMBER_OF_POSITIONS, DATE, ORDER_DATE, STATUS, PROJECT, PROJECT_CUSTOMER, TITLE, NETSUM, INVOICED, TO_BE_INVOICED, COMPLETELY_INVOICED, INVOICES, CONTACT_PERSON, REFERENCE, COMMENT;
  }

  protected ExportColumn[] createColumns()
  {
    return new ExportColumn[] { //
        new I18nExportColumn(Col.NUMMER, "fibu.auftrag.nummer.short", XlsContentProvider.LENGTH_ID),
        new I18nExportColumn(Col.NUMBER_OF_POSITIONS, "fibu.auftrag.positions", XlsContentProvider.LENGTH_ID),
        new I18nExportColumn(Col.DATE, "fibu.auftrag.datum", XlsContentProvider.LENGTH_DATE),
        new I18nExportColumn(Col.ORDER_DATE, "fibu.auftrag.beauftragungsdatum", XlsContentProvider.LENGTH_DATE),
        new I18nExportColumn(Col.STATUS, "status", 10),
        new I18nExportColumn(Col.PROJECT, "fibu.projekt", XlsContentProvider.LENGTH_STD),
        new I18nExportColumn(Col.PROJECT_CUSTOMER, "fibu.kunde", XlsContentProvider.LENGTH_STD),
        new I18nExportColumn(Col.TITLE, "address.title", XlsContentProvider.LENGTH_STD),
        new I18nExportColumn(Col.NETSUM, "fibu.auftrag.nettoSumme", XlsContentProvider.LENGTH_CURRENCY), //
        new I18nExportColumn(Col.INVOICED, "fibu.fakturiert", XlsContentProvider.LENGTH_CURRENCY), //
        new I18nExportColumn(Col.TO_BE_INVOICED, "fibu.tobeinvoiced", XlsContentProvider.LENGTH_CURRENCY),
        new I18nExportColumn(Col.COMPLETELY_INVOICED, "fibu.auftrag.vollstaendigFakturiert", XlsContentProvider.LENGTH_BOOLEAN),
        new I18nExportColumn(Col.INVOICES, "fibu.rechnungen", XlsContentProvider.LENGTH_STD),
        new I18nExportColumn(Col.CONTACT_PERSON, "contactPerson", XlsContentProvider.LENGTH_STD),
        new I18nExportColumn(Col.REFERENCE, "fibu.common.reference", XlsContentProvider.LENGTH_STD),
        new I18nExportColumn(Col.COMMENT, "comment", XlsContentProvider.LENGTH_COMMENT)};
  }

  protected void addOrderMapping(final PropertyMapping mapping, final AuftragDO order, final Object... params)
  {
    auftragDao.calculateInvoicedSum(order);
    mapping.add(Col.NUMMER, order.getNummer());
    mapping.add(Col.NUMBER_OF_POSITIONS, "#" + (order.getPositionen() != null ? order.getPositionen().size() : "0"));
    mapping.add(Col.DATE, order.getAngebotsDatum());
    mapping.add(Col.ORDER_DATE, order.getBeauftragungsDatum());
    mapping.add(Col.STATUS, order.getAuftragsStatus() != null ? PFUserContext.getLocalizedString(order.getAuftragsStatus().getI18nKey())
        : "");
    mapping.add(Col.PROJECT, order.getProjektAsString());
    final ProjektDO project = order.getProjekt();
    final String projectCustomer = KundeFormatter.formatKundeAsString(project != null ? project.getKunde() : null, order.getKundeText());
    mapping.add(Col.PROJECT_CUSTOMER, projectCustomer);
    mapping.add(Col.TITLE, order.getTitel());
    final BigDecimal netSum = order.getNettoSumme();
    final BigDecimal invoicedSum = order.getFakturiertSum();
    final BigDecimal toBeInvoicedSum = netSum.subtract(invoicedSum);
    mapping.add(Col.NETSUM, netSum);
    addCurrency(mapping, Col.INVOICED, invoicedSum);
    addCurrency(mapping, Col.TO_BE_INVOICED, toBeInvoicedSum);
    mapping.add(Col.COMPLETELY_INVOICED, order.isVollstaendigFakturiert() == true ? "x" : "");
    final Set<RechnungsPositionVO> invoicePositions = rechnungCache.getRechnungsPositionVOSetByAuftragId(order.getId());
    final StringBuilder sb = new StringBuilder();
    if (invoicePositions != null) {
      String delimiter = "";
      for (final RechnungsPositionVO pos : invoicePositions) {
        sb.append(delimiter).append(pos.getRechnungNummer());
        delimiter = ", ";
      }
    }
    mapping.add(Col.INVOICES, sb.toString());
    final PFUserDO contactPerson = Registry.instance().getUserGroupCache().getUser(order.getContactPersonId());
    mapping.add(Col.CONTACT_PERSON, contactPerson != null ? contactPerson.getFullname() : "");
    mapping.add(Col.REFERENCE, order.getReferenz());
    mapping.add(Col.COMMENT, order.getBemerkung());
  }

  private void addCurrency(final PropertyMapping mapping, final Col col, final BigDecimal value)
  {
    if (NumberHelper.isNotZero(value) == true) {
      mapping.add(col, value);
    } else {
      mapping.add(col, "");
    }
  }

  protected String getSheetTitle()
  {
    return PFUserContext.getLocalizedString("fibu.auftrag.auftraege");
  }

  /**
   * Exports the filtered list as table with almost all fields. For members of group FINANCE_GROUP (PF_Finance) and MARKETING_GROUP
   * (PF_Marketing) all addresses are exported, for others only those which are marked as personal favorites.
   * @param Used by sub classes such as AddressCampaignValueExport.
   * @throws IOException
   */
  public byte[] export(final List<AuftragDO> list, final Object... params)
  {
    if (CollectionUtils.isEmpty(list) == true) {
      return null;
    }
    log.info("Exporting order list.");
    final ExportColumn[] columns = createColumns();
    final ExportWorkbook xls = new ExportWorkbook();
    final ContentProvider contentProvider = new MyContentProvider(xls);
    // create a default Date format and currency column
    xls.setContentProvider(contentProvider);

    final String sheetTitle = getSheetTitle();
    final ExportSheet sheet = xls.addSheet(sheetTitle);
    final ContentProvider sheetProvider = sheet.getContentProvider();
    sheetProvider.putFormat(XlsContentProvider.FORMAT_CURRENCY, Col.NETSUM, Col.INVOICED, Col.TO_BE_INVOICED);
    sheetProvider.putFormat(DateFormats.getExcelFormatString(DateFormatType.DATE), Col.DATE, Col.ORDER_DATE);

    sheet.createFreezePane(1, 1);
    sheet.setColumns(columns);

    for (final AuftragDO order : list) {
      final PropertyMapping mapping = new PropertyMapping();
      addOrderMapping(mapping, order, params);
      sheet.addRow(mapping.getMapping(), 0);
    }
    return xls.getAsByteArray();
  }

  public void setAccessChecker(final AccessChecker accessChecker)
  {
    this.accessChecker = accessChecker;
  }

  public void setAuftragDao(final AuftragDao auftragDao)
  {
    this.auftragDao = auftragDao;
  }

  public void setRechnungCache(final RechnungCache rechnungCache)
  {
    this.rechnungCache = rechnungCache;
  }
}
