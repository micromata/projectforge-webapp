/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.wicket;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.components.DateTimePanel;
import org.projectforge.web.wicket.components.DateTimePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.components.TooltipImage;

public abstract class AbstractListForm<F extends BaseSearchFilter, P extends AbstractListPage< ? , ? , ? >> extends
    AbstractSecuredForm<F, P>
{
  private static final long serialVersionUID = 1304394324524767035L;

  protected F searchFilter;

  protected abstract F newSearchFilterInstance();

  protected Integer pageSize;

  private Label modifiedSearchExpressionLabel;

  protected Fragment pageSizeFragment;

  private String modificationSince;

  protected DateTimePanel startDateTimePanel;

  protected DateTimePanel stopDateTimePanel;

  @SpringBean(name = "userGroupCache")
  protected UserGroupCache userGroupCache;

  /**
   * List to create action buttons in the desired order before creating the RepeatingView.
   */
  protected List<Component> actionButtons = new ArrayList<Component>();

  // Needed for generating RepeatingView in onBeforeRender() if not already generated.
  private boolean actionButtonsRendered = false;

  private RepeatingView actionButtonsView;

  private WebMarkupContainer extendedFilter;

  private SingleButtonPanel cancelButtonPanel;

  private SingleButtonPanel resetButtonPanel;

  private SingleButtonPanel searchButtonPanel;

  private SingleButtonPanel nextButtonPanel;

  protected WebMarkupContainer filterContainer;

  public static DropDownChoice<Integer> getPageSizeDropDownChoice(final String id, final Locale locale, final IModel<Integer> model,
      final int minValue, final int maxValue)
  {
    final LabelValueChoiceRenderer<Integer> pageSizeChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
    final NumberFormat nf = NumberFormat.getInstance(locale);
    for (final int size : new int[] { 3, 5, 10, 25, 50, 100, 200, 500, 1000}) {
      if (size >= minValue && size <= maxValue) {
        pageSizeChoiceRenderer.addValue(size, nf.format(size));
      }
    }
    final DropDownChoice<Integer> pageSizeChoice = new DropDownChoice<Integer>("pageSize", model, pageSizeChoiceRenderer.getValues(),
        pageSizeChoiceRenderer);
    pageSizeChoice.setNullValid(false);
    return pageSizeChoice;
  }

  public AbstractListForm(P parentPage)
  {
    super(parentPage);
    getSearchFilter();
  }

  @SuppressWarnings( { "unchecked", "serial"})
  @Override
  protected void init()
  {
    super.init();
    filterContainer = new WebMarkupContainer("filter") {
      @Override
      public boolean isVisible()
      {
        return isFilterVisible();
      }
    };
    add(filterContainer);
    setModel(new CompoundPropertyModel(searchFilter));
    final WebMarkupContainer searchFilterRow = new WebMarkupContainer("searchFilterRow") {
      @Override
      public boolean isVisible()
      {
        if (isFilterVisible() == false) {
          return false;
        }
        return isSearchFilterVisible();
      }
    };
    filterContainer.add(searchFilterRow);
    final WebMarkupContainer searchCell = new WebMarkupContainer("searchCell") {
      @Override
      protected void onComponentTag(ComponentTag tag)
      {
        tag.put("colspan", parentPage.colspan - 1);
      }
    };
    searchFilterRow.add(searchCell);
    final Component searchField = createSearchTextField();
    WicketUtils.addTooltip(searchField, getString("search.string.info.title"), getParentPage().getSearchToolTip());
    searchCell.add(searchField);
    ExternalLink extendedSearchLink = new ExternalLink("toggleExtendedFilter", "#");
    searchCell.add(extendedSearchLink);
    addExtendedFilter(searchCell);
    if (parentPage.getBaseDao().isHistorizable() == false) {
      extendedSearchLink.setVisible(false);
      extendedFilter.setVisible(false);
    }

    final ExternalLink handbuchVolltextsucheLink = new ExternalLink("handbuchVolltextsucheLink",
        getUrl("/secure/doc/Handbuch.html#label_volltextsuche"));
    searchCell.add(handbuchVolltextsucheLink);
    handbuchVolltextsucheLink.add(new TooltipImage("fulltextSearchTooltipImage", getResponse(), WebConstants.IMAGE_HELP,
        getString("tooltip.lucene.link")));
    final String helpKeyboardImageTooltip = getHelpKeyboardImageTooltip();
    final Component helpKeyboardImage;
    if (helpKeyboardImageTooltip != null) {
      helpKeyboardImage = new TooltipImage("helpKeyboardImage", getResponse(), WebConstants.IMAGE_HELP_KEYBOARD, helpKeyboardImageTooltip);
    } else {
      helpKeyboardImage = new PresizedImage("helpKeyboardImage", getResponse(), WebConstants.IMAGE_HELP_KEYBOARD).setVisible(false);
    }
    searchCell.add(helpKeyboardImage);
    final Model<String> modifiedSearchExpressionModel = new Model<String>() {
      @Override
      public String getObject()
      {
        return getModifiedSearchExpressionLabel();
      }
    };
    modifiedSearchExpressionLabel = new Label("modifiedSearchExpression", modifiedSearchExpressionModel) {
      @Override
      public boolean isVisible()
      {
        return StringUtils.isNotBlank(searchFilter.getSearchString()) == true;
      }
    };
    modifiedSearchExpressionLabel.setEscapeModelStrings(false);
    searchCell.add(modifiedSearchExpressionLabel);

    pageSizeFragment = new Fragment("pageSize", "pageSizeFragment", this);
    filterContainer.add(pageSizeFragment);

    final DropDownChoice pageSizeChoice = getPageSizeDropDownChoice("pageSize", getLocale(), new PropertyModel<Integer>(this, "pageSize"),
        25, 1000);
    pageSizeFragment.add(pageSizeChoice);
    final WebMarkupContainer buttonCell = new WebMarkupContainer("buttonCell") {
      public boolean isTransparentResolver()
      {
        return true;
      }

      protected void onComponentTag(ComponentTag tag)
      {
        tag.put("colspan", parentPage.colspan);
      }
    };
    add(buttonCell);
    actionButtonsView = new RepeatingView("actionButtons");
    buttonCell.add(actionButtonsView.setRenderBodyOnly(true));

    final Button cancelButton = new Button("button", new Model<String>(getString("cancel"))) {
      @Override
      public final void onSubmit()
      {
        getParentPage().onCancelSubmit();
      }
    };
    cancelButton.add(WebConstants.BUTTON_CLASS_CANCEL);
    cancelButton.setDefaultFormProcessing(false);
    cancelButtonPanel = new SingleButtonPanel(getNewActionButtonChildId(), cancelButton);
    addActionButton(cancelButtonPanel);

    final Button resetButton = new Button("button", new Model<String>(getString("reset"))) {
      @Override
      public final void onSubmit()
      {
        getParentPage().onResetSubmit();
      }
    };
    resetButton.add(WebConstants.BUTTON_CLASS_RESET);
    resetButton.setDefaultFormProcessing(false);
    resetButtonPanel = new SingleButtonPanel(getNewActionButtonChildId(), resetButton);
    addActionButton(resetButtonPanel);

    final Button nextButton = new Button("button", new Model<String>(getString("next"))) {
      @Override
      public final void onSubmit()
      {
        getParentPage().onNextSubmit();
      }
    };
    nextButton.add(WebConstants.BUTTON_CLASS_DEFAULT);
    nextButtonPanel = new SingleButtonPanel(getNewActionButtonChildId(), nextButton);
    addActionButton(nextButtonPanel);

    final Button searchButton = new Button("button", new Model<String>(getString("search"))) {
      @Override
      public final void onSubmit()
      {
        getParentPage().onSearchSubmit();
      }
    };
    searchButton.add(WebConstants.BUTTON_CLASS_DEFAULT);
    searchButtonPanel = new SingleButtonPanel(getNewActionButtonChildId(), searchButton);
    addActionButton(searchButtonPanel);

    setDefaultButton(searchButton);
    setComponentsVisibility();
  }

  @SuppressWarnings("serial")
  private void addExtendedFilter(final MarkupContainer parent)
  {
    extendedFilter = new WebMarkupContainer("extendedFilter");
    parent.add(extendedFilter);
    final UserSelectPanel userSelectPanel = new UserSelectPanel("modifiedByUser", new Model<PFUserDO>() {
      @Override
      public PFUserDO getObject()
      {
        return userGroupCache.getUser(searchFilter.getModifiedByUserId());
      }

      @Override
      public void setObject(final PFUserDO object)
      {
        if (object == null) {
          searchFilter.setModifiedByUserId(null);
        } else {
          searchFilter.setModifiedByUserId(object.getId());
        }
      }
    }, parentPage, "modifiedByUserId");
    extendedFilter.add(userSelectPanel);
    userSelectPanel.setDefaultFormProcessing(false);
    userSelectPanel.init().withAutoSubmit(true);
    extendedFilter.add(new CheckBox("useModificationFilter", new PropertyModel<Boolean>(searchFilter, "useModificationFilter")));
    startDateTimePanel = new DateTimePanel("startTimeOfLastModification", new PropertyModel<Date>(searchFilter,
        "startTimeOfLastModification"), (DateTimePanelSettings) DateTimePanelSettings.get().withCallerPage(parentPage).withSelectProperty(
        "startDateOfLastModification").withSelectPeriodMode(true), DatePrecision.MINUTE);
    extendedFilter.add(startDateTimePanel);
    stopDateTimePanel = new DateTimePanel("stopTimeOfLastModification",
        new PropertyModel<Date>(searchFilter, "stopTimeOfLastModification"), (DateTimePanelSettings) DateTimePanelSettings.get()
            .withCallerPage(parentPage).withSelectProperty("stopDateOfLastModification").withSelectPeriodMode(true), DatePrecision.MINUTE);
    stopDateTimePanel.setRequired(false);
    extendedFilter.add(stopDateTimePanel);
    final Label datesAsUTCLabel = new DatesAsUTCLabel("modificationDatesAsUTC") {
      @Override
      public Date getStartTime()
      {
        return searchFilter.getStartTimeOfLastModification();
      }

      @Override
      public Date getStopTime()
      {
        return searchFilter.getStopTimeOfLastModification();
      }
    };
    extendedFilter.add(datesAsUTCLabel);
    // DropDownChoice for convenient selection of time periods.
    final LabelValueChoiceRenderer<String> timePeriodChoiceRenderer = new LabelValueChoiceRenderer<String>();
    timePeriodChoiceRenderer.addValue("lastMinute", getString("search.lastMinute"));
    timePeriodChoiceRenderer.addValue("lastMinutes:10", PFUserContext.getLocalizedMessage("search.lastMinutes", 10));
    timePeriodChoiceRenderer.addValue("lastMinutes:30", PFUserContext.getLocalizedMessage("search.lastMinutes", 30));
    timePeriodChoiceRenderer.addValue("lastHour", getString("search.lastHour"));
    timePeriodChoiceRenderer.addValue("lastHours:4", PFUserContext.getLocalizedMessage("search.lastHours", 4));
    timePeriodChoiceRenderer.addValue("today", getString("search.today"));
    timePeriodChoiceRenderer.addValue("sinceYesterday", getString("search.sinceYesterday"));
    timePeriodChoiceRenderer.addValue("lastDays:3", PFUserContext.getLocalizedMessage("search.lastDays", 3));
    timePeriodChoiceRenderer.addValue("lastDays:7", PFUserContext.getLocalizedMessage("search.lastDays", 7));
    timePeriodChoiceRenderer.addValue("lastDays:14", PFUserContext.getLocalizedMessage("search.lastDays", 14));
    timePeriodChoiceRenderer.addValue("lastDays:30", PFUserContext.getLocalizedMessage("search.lastDays", 30));
    timePeriodChoiceRenderer.addValue("lastDays:60", PFUserContext.getLocalizedMessage("search.lastDays", 60));
    timePeriodChoiceRenderer.addValue("lastDays:90", PFUserContext.getLocalizedMessage("search.lastDays", 90));
    @SuppressWarnings("unchecked")
    final DropDownChoice modificationSinceChoice = new DropDownChoice("modificationSince", new PropertyModel(this, "modificationSince"),
        timePeriodChoiceRenderer.getValues(), timePeriodChoiceRenderer);
    modificationSinceChoice.setNullValid(true);
    modificationSinceChoice.setRequired(false);
    extendedFilter.add(modificationSinceChoice);
  }

  /**
   * Creates a simple TextField and sets the focus on it. Overwrite this method if you want to add for example an auto completion text field
   * (ajax).
   */
  protected Component createSearchTextField()
  {
    final TextField<String> searchField = new TextField<String>("searchString", new PropertyModel<String>(searchFilter, "searchString"));
    searchField.add(new FocusOnLoadBehavior());
    return searchField;
  }

  public void addActionButton(final Component entry)
  {
    this.actionButtons.add(entry);
  }

  public void prependActionButton(final Component entry)
  {
    this.actionButtons.add(0, entry);
  }

  public String getNewActionButtonChildId()
  {
    return this.actionButtonsView.newChildId();
  }

  @Override
  public void onBeforeRender()
  {
    super.onBeforeRender();
    if (searchFilter.isUseModificationFilter() == true) {
      extendedFilter.add(new SimpleAttributeModifier("style", ""));
    } else {
      extendedFilter.add(new SimpleAttributeModifier("style", "display: none;"));
    }
    if (actionButtonsRendered == false) {
      if (this.actionButtons.size() > 0) {
        for (final Component entry : this.actionButtons) {
          this.actionButtonsView.add(entry);
        }
      }
      actionButtonsRendered = true;
    }
  }

  protected void setComponentsVisibility()
  {
    if (parentPage.isMassUpdateMode() == true) {
      cancelButtonPanel.setVisible(true);
      searchButtonPanel.setVisible(false);
      resetButtonPanel.setVisible(false);
      nextButtonPanel.setVisible(true);
    } else {
      if (parentPage.isSelectMode() == false) {
        // Show cancel button only in select mode.
        cancelButtonPanel.setVisible(false);
      }
      searchButtonPanel.setVisible(true);
      resetButtonPanel.setVisible(true);
      nextButtonPanel.setVisible(false);
    }
  }

  /**
   * onchange, onclick or submit without button.
   * @see org.apache.wicket.markup.html.form.Form#onSubmit()
   */
  @Override
  protected void onSubmit()
  {
    super.onSubmit();
    if (modificationSince != null) {
      final int pos = modificationSince.indexOf(':');
      final Integer number;
      if (pos >= 0) {
        number = NumberHelper.parseInteger(modificationSince.substring(pos + 1));
      } else {
        number = null;
      }
      final DateHolder dateHolder = new DateHolder(DatePrecision.MINUTE);
      if ("lastMinute".equals(modificationSince) == true) {
        dateHolder.add(Calendar.MINUTE, -1);
      } else if (modificationSince.startsWith("lastMinutes:") == true) {
        dateHolder.add(Calendar.MINUTE, -number);
      } else if ("lastHour".equals(modificationSince) == true) {
        dateHolder.add(Calendar.HOUR, -1);
      } else if (modificationSince.startsWith("lastHours:") == true) {
        dateHolder.add(Calendar.HOUR, -number);
      } else if ("today".equals(modificationSince) == true) {
        dateHolder.setBeginOfDay();
      } else if ("sinceYesterday".equals(modificationSince) == true) {
        dateHolder.add(Calendar.DAY_OF_YEAR, -1);
        dateHolder.setBeginOfDay();
      } else if (modificationSince.startsWith("lastDays") == true) {
        dateHolder.add(Calendar.DAY_OF_YEAR, -number);
        dateHolder.setBeginOfDay();
      }
      searchFilter.setStartTimeOfLastModification(dateHolder.getDate());
      startDateTimePanel.markModelAsChanged();
      searchFilter.setStopTimeOfLastModification(null);
      stopDateTimePanel.markModelAsChanged();
      modificationSince = null;
    }
    getParentPage().onSearchSubmit();
  }

  @SuppressWarnings("unchecked")
  public F getSearchFilter()
  {
    if (this.searchFilter == null) {
      if (getParentPage().isStoreFilter() == true) {
        Object filter = getParentPage().getUserPrefEntry(this.getClass().getName() + ":Filter");
        if (filter != null) {
          if (filter.getClass().equals(newSearchFilterInstance().getClass()) == true) {
            try {
              this.searchFilter = (F) filter;
            } catch (ClassCastException ex) {
              // No output needed, info message follows:
            }
            if (this.searchFilter == null) {
              // Probably a new software release results in an incompability of old and new filter format.
              getLogger().info(
                  "Could not restore filter from user prefs: (old) filter type "
                      + filter.getClass().getName()
                      + " is not assignable to (new) filter type "
                      + newSearchFilterInstance().getClass().getName()
                      + " (OK, probably new software release).");
            }
          }
        }
      }
    }
    if (this.searchFilter == null) {
      this.searchFilter = newSearchFilterInstance();
      this.searchFilter.reset();
      if (getParentPage().isStoreFilter() == true) {
        getParentPage().putUserPrefEntry(this.getClass().getName() + ":Filter", this.searchFilter, true);
      }
    }
    return this.searchFilter;
  }

  /**
   * The page size of display tag (result table).
   */
  public Integer getPageSize()
  {
    if (pageSize == null) {
      pageSize = (Integer) getParentPage().getUserPrefEntry(this.getClass().getName() + ":pageSize");
    }
    if (pageSize == null) {
      pageSize = 50;
    }
    return pageSize;
  }

  /**
   * For convenience combo box with quick select of often used time periods.
   */
  public String getModificationSince()
  {
    return modificationSince;
  }

  public void setModificationSince(String modificationSince)
  {
    this.modificationSince = modificationSince;
  }

  public void setPageSize(Integer pageSize)
  {
    this.pageSize = pageSize;
    getParentPage().putUserPrefEntry(this.getClass().getName() + ":pageSize", this.pageSize, true);
  }

  public Fragment getPageSizeFragment()
  {
    return pageSizeFragment;
  }

  protected String getModifiedSearchExpressionLabel()
  {
    return "<br/>" + getString("search.lucene.expression") + " " + escapeHtml(BaseDao.modifySearchString(searchFilter.getSearchString()));
  }

  /**
   * Any given de-serialized filter will be set from parent page.
   * @param filter
   */
  @SuppressWarnings("unchecked")
  void setFilter(final Object filter)
  {
    searchFilter = (F) filter;
  }

  /**
   * Used by search cell to define visibility.
   * @return True if not overload.
   */
  protected boolean isFilterVisible()
  {
    return true;
  }

  /**
   * Used by search cell to define visibility of search input string and extended search filter.
   * @return True if not overload.
   */
  protected boolean isSearchFilterVisible()
  {
    return true;
  }

  /**
   * If the derived class returns a text, the keyboard image right to the search field will be shown with the returned string as tool-tip. <br/>
   * If the derived class uses the store-recent-search-terms-functionality then a generic tool-tip about this functionality is used.<br/>
   * Otherwise the image is invisible (default).
   */
  protected String getHelpKeyboardImageTooltip()
  {
    if (parentPage.isRecentSearchTermsStorage() == true) {
      return getString("tooltip.autocomplete.recentSearchTerms");
    } else {
      return null;
    }
  }

  /** This class uses the logger of the extended class. */
  protected abstract Logger getLogger();
}
