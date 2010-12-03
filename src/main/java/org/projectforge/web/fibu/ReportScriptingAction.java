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

package org.projectforge.web.fibu;



public class ReportScriptingAction //extends BaseActionBean
{
//  private static final Logger log = Logger.getLogger(ReportObjectivesAction.class);
//
//  private static final String JSP_URL = "/WEB-INF/jsp/fibu/reportScripting.jsp";
//
//  private Configuration configuration;
//  
//  private AddressDao addressDao;
//
//  private AuftragDao auftragDao;
//
//  private BuchungssatzDao buchungssatzDao;
//
//  private EingangsrechnungDao eingangsrechnungDao;
//
//  private EmployeeDao employeeDao;
//
//  private EmployeeSalaryDao employeeSalaryDao;
//  
//  private Kost1Dao kost1Dao;
//
//  private Kost2ArtDao kost2ArtDao;
//
//  private Kost2Dao kost2Dao;
//  
//  private KostZuweisungDao kostZuweisungDao;
//
//  private KundeDao kundeDao;
//
//  private ProjektDao projektDao;
//
//  private RechnungDao rechnungDao;
//  
//  private TaskDao taskDao;
//  
//  private TaskTree taskTree;
//
//  private TimesheetDao timesheetDao;
//
//  private UserDao userDao;
//
//  private FileBean uploadFile;
//
//  private GroovyExecutor groovyExecutor;
//
//  private GroovyResult groovyResult;
//
//  private Map<String, Object> scriptVariables;
//
//  private ReportScriptingStorage storage;
//
//  private List<String> lines;
//
//  public ReportStorage getReportStorage()
//  {
//    return ReportObjectivesAction.getReportStorage(getContext());
//  }
//
//  public ReportScriptingStorage getStorage()
//  {
//    storage = (ReportScriptingStorage) getContext().getEntry(ReportScriptingStorage.class.getName());
//    if (storage == null) {
//      storage = new ReportScriptingStorage();
//      getContext().putEntry(ReportScriptingStorage.class.getName(), storage, false);
//    }
//    return storage;
//  }
//
//  public FileBean getUploadFile()
//  {
//    return uploadFile;
//  }
//
//  public void setUploadFile(FileBean uploadFile)
//  {
//    this.uploadFile = uploadFile;
//  }
//
//  @Validate
//  public String getGroovyScript()
//  {
//    return getStorage().getGroovyScript();
//  }
//
//  public void setGroovyScript(String groovyScript)
//  {
//    getStorage().setGroovyScript(groovyScript);
//  }
//
//  public GroovyResult getGroovyResult()
//  {
//    return groovyResult;
//  }
//
//  /**
//   * Escapes all html characters. If Groovy result is from type string then all \n will be replaced by <br/>.
//   * @return
//   */
//  public String getGroovyResultAsHtmlString()
//  {
//    if (groovyResult == null) {
//      return null;
//    }
//    return groovyResult.getResultAsHtmlString();
//  }
//
//  public String getFilename()
//  {
//    return getStorage().getLastAddedFilename();
//  }
//
//  @DefaultHandler
//  public Resolution init()
//  {
//    accessChecker.checkIsUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
//    return getInputPage();
//  }
//
//  /**
//   * Suffix jrxml: Reads and compiles the uploaded jrxml file.<br/> Suffix xls: Stores the upload Excel file for later use-age.
//   * @return
//   */
//  @DontValidate
//  public Resolution upload()
//  {
//    accessChecker.checkIsUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
//    accessChecker.checkDemoUser();
//    if (uploadFile == null) {
//      addGlobalError("fibu.reporting.jasper.error.fileDoesNotExist");
//      return getInputPage();
//    }
//    JasperReport report = null;
//    boolean delete = false;
//    try {
//      if (uploadFile.getFileName().endsWith(".jrxml") == true) {
//        delete = true;
//        InputStream is = uploadFile.getInputStream();
//        report = JasperCompileManager.compileReport(is);
//      } else if (uploadFile.getFileName().endsWith(".xls") == true) {
//        StringBuffer buf = new StringBuffer();
//        buf.append("report_").append(FileHelper.createSafeFilename(PFUserContext.getUser().getUsername(), 20)).append(".xls");
//        File file = new File(configuration.getWorkingDirectory(), buf.toString());
//        uploadFile.save(file);
//        getStorage().setFilename(uploadFile.getFileName(), file.getAbsolutePath());
//      }
//    } catch (IOException ex) {
//      addGlobalError("error", ex.getMessage());
//      log.error(ex.getMessage(), ex);
//      return getInputPage();
//    } catch (JRException ex) {
//      addGlobalError("error", ex.getMessage());
//      log.error(ex.getMessage(), ex);
//      return getInputPage();
//    } finally {
//      if (delete == true) {
//        try {
//          uploadFile.delete();
//        } catch (IOException ex) {
//          addGlobalError("error", ex.getMessage());
//          log.error(ex.getMessage(), ex);
//          return getInputPage();
//        }
//      }
//    }
//    if (report != null) {
//      getStorage().setJasperReport(report, uploadFile.getFileName());
//    }
//    return getInputPage();
//  }
//
//  public BwaZeileId[] getDefinedBwaZeilen()
//  {
//    return BwaZeileId.values();
//  }
//
//  public String[] getAdditionalBwaValues()
//  {
//    return Bwa.getAdditionalValues();
//  }
//
//  public List<LabelValueBean<String, Class<?>>> getScriptVariables()
//  {
//    if (MapUtils.isEmpty(scriptVariables) == true) {
//      scriptVariables = new HashMap<String, Object>();
//      scriptVariables.put("reportStorage", null);
//      scriptVariables.put("reportScriptingStorage", null);
//      scriptVariables.put("appId", Version.APP_ID);
//      scriptVariables.put("appVersion", getAppVersion());
//      scriptVariables.put("appRelease", getAppReleaseDate());
//
//      scriptVariables.put("addressDao", new ScriptingDao<AddressDO>(addressDao));
//      scriptVariables.put("auftragDao", new ScriptingDao<AuftragDO>(auftragDao));
//      scriptVariables.put("buchungssatzDao", new ScriptingDao<BuchungssatzDO>(buchungssatzDao));
//      scriptVariables.put("eingangsrechnungDao", new ScriptingDao<EingangsrechnungDO>(eingangsrechnungDao));
//      scriptVariables.put("employeeDao", new EmployeeScriptingDao(employeeDao));
//      scriptVariables.put("employeeSalaryDao", new ScriptingDao<EmployeeSalaryDO>(employeeSalaryDao));
//      scriptVariables.put("kost1Dao", new Kost1ScriptingDao(kost1Dao));
//      scriptVariables.put("kost2Dao", new ScriptingDao<Kost2DO>(kost2Dao));
//      scriptVariables.put("kost2ArtDao", new ScriptingDao<Kost2ArtDO>(kost2ArtDao));
//      scriptVariables.put("kostZuweisungDao", new ScriptingDao<KostZuweisungDO>(kostZuweisungDao));
//      scriptVariables.put("kundeDao", new ScriptingDao<KundeDO>(kundeDao));
//      scriptVariables.put("projektDao", new ScriptingDao<ProjektDO>(projektDao));
//      scriptVariables.put("rechnungDao", new ScriptingDao<RechnungDO>(rechnungDao));
//      scriptVariables.put("reportList", null);
//      scriptVariables.put("taskDao", new ScriptingDao<TaskDO>(taskDao));
//      scriptVariables.put("taskTree", new ScriptingTaskTree(taskTree));
//      scriptVariables.put("timesheetDao", new ScriptingDao<TimesheetDO>(timesheetDao));
//      scriptVariables.put("userDao", new ScriptingDao<PFUserDO>(userDao));
//    }
//    List<LabelValueBean<String, Class<?>>> result = new ArrayList<LabelValueBean<String, Class<?>>>();
//    SortedSet<String> set = new TreeSet<String>();
//    set.addAll(scriptVariables.keySet());
//    for (String key : set) {
//      Object obj = scriptVariables.get(key);
//      Class<?> clazz = null;
//      if (obj != null) {
//        clazz = obj.getClass();
//      }
//      LabelValueBean<String, Class<?>> lv = new LabelValueBean<String, Class<?>>(key, clazz);
//      result.add(lv);
//    }
//    return result;
//  }
//
//  public Resolution execute()
//  {
//    accessChecker.checkIsUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
//    accessChecker.checkDemoUser();
//    ReportGeneratorList reportGeneratorList = new ReportGeneratorList();
//    getScriptVariables();
//    scriptVariables.put("reportStorage", getReportStorage());
//    scriptVariables.put("reportScriptingStorage", getStorage());
//    scriptVariables.put("reportList", reportGeneratorList);
//    if (StringUtils.isNotBlank(getStorage().getGroovyScript()) == true) {
//      groovyResult = groovyExecutor.execute(getStorage().getGroovyScript(), scriptVariables);
//      if (groovyResult.hasException() == true) {
//        addError("groovyScript", "exception.groovyError", String.valueOf(groovyResult.getException()));
//        return getInputPage();
//      }
//      if (groovyResult.hasResult() == true) {
//        final Object result = groovyResult.getResult();
//        if (result instanceof ExportWorkbook == true) {
//          return excelExport();
//        } else if (groovyResult.getResult() instanceof ReportGeneratorList == true) {
//          reportGeneratorList = (ReportGeneratorList) groovyResult.getResult();
//          return jasperReport(reportGeneratorList);
//        } else if (result instanceof ExportJFreeChart) {
//          return jFreeChartExport(); 
//        }
//      }
//    } else if (getStorage().getJasperReport() != null) {
//      return jasperReport();
//    }
//    return getInputPage();
//  }
//
//  /**
//   * Gets the lines of the groovy script for displaying line number etc.
//   * @return
//   */
//  public List<String> getLines()
//  {
//    if (lines != null) {
//      return lines;
//    }
//    lines = new ArrayList<String>();
//    String groovyScript = getGroovyScript();
//    StringBuffer line = new StringBuffer();
//    for (int i = 0; i < groovyScript.length(); i++) {
//      char c = groovyScript.charAt(i);
//      if (c == '\n') {
//        lines.add(HtmlHelper.escapeXml(line.toString()));
//        line = new StringBuffer();
//      } else {
//        line.append(c);
//      }
//    }
//    lines.add(HtmlHelper.escapeXml(line.toString()));
//    return lines;
//  }
//
//  public boolean isException()
//  {
//    return this.groovyResult != null && this.groovyResult.hasException();
//  }
//
//  /**
//   * Creates the reports for the entries.
//   * @param reportGeneratorList
//   * @return
//   */
//  private Resolution jasperReport(ReportGeneratorList reportGeneratorList)
//  {
//    if (CollectionUtils.isEmpty(reportGeneratorList.getReports()) == true) {
//      addError("groovyScript", "fibu.reporting.jasper.error.reportListIsEmpty");
//      return getInputPage();
//    }
//    Map<String, Object> parameters = new HashMap<String, Object>();
//    ReportGenerator report = reportGeneratorList.getReports().get(0);
//    Collection< ? > beanCollection = report.getBeanCollection();
//    parameters = report.getParameters();
//    return jasperReport(parameters, beanCollection);
//  }
//
//  /**
//   * Default report from reportStorage. Uses the current report and puts the bwa values in parameter map.
//   */
//  private Resolution jasperReport()
//  {
//    if (getReportStorage() == null || getReportStorage().getRoot() == null || getReportStorage().getRoot().isLoad() == false) {
//      addGlobalError("fibu.reporting.jasper.error.reportDataDoesNotExist");
//      return getInputPage();
//    }
//    Map<String, Object> parameters = new HashMap<String, Object>();
//    Report report = getReportStorage().getCurrentReport();
//    Collection< ? > beanCollection = report.getBuchungssaetze();
//    Bwa.putBwaWerte(parameters, report.getBwa());
//    return jasperReport(parameters, beanCollection);
//  }
//
//  private Resolution jasperReport(Map<String, Object> parameters, Collection< ? > beanCollection)
//  {
//    JasperReport jasperReport = getStorage().getJasperReport();
//    JasperPrint jp = null;
//    try {
//      jp = JasperFillManager.fillReport(jasperReport, parameters, new JRBeanCollectionDataSource(beanCollection));
//    } catch (JRException ex) {
//      addGlobalError("error", ex.getMessage());
//      log.error(ex.getMessage(), ex);
//      return getInputPage();
//    }
//    final JasperPrint jasperPrint = jp;
//    return new Resolution() {
//      public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception
//      {
//        StringBuffer buf = new StringBuffer();
//        buf.append("pf_report_");
//        buf.append(DateHelper.getTimestampAsFilenameSuffix(getContext().now())).append(".pdf");
//        ResponseUtils.prepareDownload(buf.toString(), response, getContext().getServletContext(), true);
//        JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
//        response.getOutputStream().flush();
//      }
//    };
//  }
//
//  private Resolution excelExport()
//  {
//    final ExportWorkbook workbook = (ExportWorkbook) groovyResult.getResult();
//    return new Resolution() {
//      public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception
//      {
//        StringBuffer buf = new StringBuffer();
//        buf.append("pf_report_");
//        buf.append(DateHelper.getTimestampAsFilenameSuffix(getContext().now())).append(".xls");
//        ResponseUtils.prepareDownload(buf.toString(), response, getContext().getServletContext(), true);
//        workbook.write(response.getOutputStream());
//        response.getOutputStream().flush();
//      }
//    };
//  }
//
//  private Resolution jFreeChartExport()
//  {
//    final ExportJFreeChart exportJFreeChart = (ExportJFreeChart)groovyResult.getResult();
//    final JFreeChart chart = exportJFreeChart.getJFreeChart();
//    final int width = exportJFreeChart.getWidth();
//    final int height = exportJFreeChart.getHeight();
//    return new Resolution() {
//      public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception
//      {
//        StringBuffer buf = new StringBuffer();
//        buf.append("pf_chart_");
//        buf.append(DateHelper.getTimestampAsFilenameSuffix(getContext().now()));
//        ResponseUtils.prepareDownload(buf.toString(), response, getContext().getServletContext(), true);
//        if (exportJFreeChart.getImageType() == JFreeChartImageType.PNG) {
//          ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, width, height);
//          buf.append(".png");
//        } else {
//          ChartUtilities.writeChartAsJPEG(response.getOutputStream(), chart, width, height);
//          buf.append(".jpg");
//        }
//        response.getOutputStream().flush();
//      }
//    };
//  }
//
//  private Resolution getInputPage()
//  {
//    return new ForwardResolution(JSP_URL);
//  }
//
//  public void setConfiguration(Configuration configuration)
//  {
//    this.configuration = configuration;
//  }
//
//  public void setAddressDao(AddressDao addressDao)
//  {
//    this.addressDao = addressDao;
//  }
//  
//  public void setAuftragDao(AuftragDao auftragDao)
//  {
//    this.auftragDao = auftragDao;
//  }
//
//  public void setEingangsrechnungDao(EingangsrechnungDao eingangsrechnungDao)
//  {
//    this.eingangsrechnungDao = eingangsrechnungDao;
//  }
//
//  public void setEmployeeDao(EmployeeDao employeeDao)
//  {
//    this.employeeDao = employeeDao;
//  }
//  
//  public void setEmployeeSalaryDao(EmployeeSalaryDao employeeSalaryDao)
//  {
//    this.employeeSalaryDao = employeeSalaryDao;
//  }
//
//  public void setBuchungssatzDao(BuchungssatzDao buchungssatzDao)
//  {
//    this.buchungssatzDao = buchungssatzDao;
//  }
//
//  public void setKost1Dao(Kost1Dao kost1Dao)
//  {
//    this.kost1Dao = kost1Dao;
//  }
//
//  public void setKost2ArtDao(Kost2ArtDao kost2ArtDao)
//  {
//    this.kost2ArtDao = kost2ArtDao;
//  }
//
//  public void setKost2Dao(Kost2Dao kost2Dao)
//  {
//    this.kost2Dao = kost2Dao;
//  }
//  
//  public void setKostZuweisungDao(KostZuweisungDao kostZuweisungDao)
//  {
//    this.kostZuweisungDao = kostZuweisungDao;
//  }
//
//  public void setKundeDao(KundeDao kundeDao)
//  {
//    this.kundeDao = kundeDao;
//  }
//
//  public void setProjektDao(ProjektDao projektDao)
//  {
//    this.projektDao = projektDao;
//  }
//
//  public void setRechnungDao(RechnungDao rechnungDao)
//  {
//    this.rechnungDao = rechnungDao;
//  }
//
//  public void setUserDao(UserDao userDao)
//  {
//    this.userDao = userDao;
//  }
//  
//  public void setTaskDao(TaskDao taskDao)
//  {
//    this.taskDao = taskDao;
//  }
//  
//  public void setTaskTree(TaskTree taskTree)
//  {
//    this.taskTree = taskTree;
//  }
//
//  public void setTimesheetDao(TimesheetDao timesheetDao)
//  {
//    this.timesheetDao = timesheetDao;
//  }
//
//  public void setGroovyExecutor(GroovyExecutor groovyExecutor)
//  {
//    this.groovyExecutor = groovyExecutor;
//  }
}
