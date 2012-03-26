/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
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
import org.projectforge.web.wicket.flowlayout.CheckBoxPanel;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldSetIconPosition;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.GridBuilder;
import org.projectforge.web.wicket.flowlayout.HiddenInputPanel;
import org.projectforge.web.wicket.flowlayout.HtmlCommentPanel;
import org.projectforge.web.wicket.flowlayout.IconPanel;
import org.projectforge.web.wicket.flowlayout.IconType;
import org.projectforge.web.wicket.flowlayout.InputPanel;
import org.projectforge.web.wicket.flowlayout.MyComponentsRepeater;

public abstract class AbstractListForm<F extends BaseSearchFilter, P extends AbstractListPage< ? , ? , ? >> extends
AbstractSecuredForm<F, P>
{
  private static final long serialVersionUID = 1304394324524767035L;

  public static final String I18N_ONLY_DELETED = "onlyDeleted";

  public static final String I18N_ONLY_DELETED_TOOLTIP = "onlyDeleted.tooltip";

  protected F searchFilter;

  protected abstract F newSearchFilterInstance();

  protected Integer pageSize;

  protected GridBuilder gridBuilder;

  private DivPanel extendedFilter;

  private Label modifiedSearchExpressionLabel;

  private String modificationSince;

  protected DateTimePanel startDateTimePanel;

  protected DateTimePanel stopDateTimePanel;

  @SpringBean(name = "userGroupCache")
  protected UserGroupCache userGroupCache;

  /**
   * List to create action buttons in the desired order before creating the RepeatingView.
   */
  protected MyComponentsRepeater<Component> actionButtons;

  private SingleButtonPanel cancelButtonPanel;

  protected SingleButtonPanel resetButtonPanel;

  protected SingleButtonPanel searchButtonPanel;

  private SingleButtonPanel nextButtonPanel;

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
    final DropDownChoice<Integer> pageSizeChoice = new DropDownChoice<Integer>(id, model, pageSizeChoiceRenderer.getValues(),
        pageSizeChoiceRenderer);
    pageSizeChoice.setNullValid(false);
    return pageSizeChoice;
      }

  public AbstractListForm(final P parentPage)
  {
    super(parentPage);
    getSearchFilter();
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback") {
      /**
       * @see org.apache.wicket.Component#isVisible()
       */
      @Override
      public boolean isVisible()
      {
        return hasError();
      }
    };
    feedbackPanel.setOutputMarkupId(true);
    add(feedbackPanel);

    if (isFilterVisible() == false) {
      add(WicketUtils.getInvisibleComponent("filter"));
    } else {
      final RepeatingView filter = new RepeatingView("filter");
      add(filter);
      gridBuilder = newGridBuilder(filter).newGrid16();
      {
        // Fieldset search filter
        final FieldsetPanel fs = gridBuilder.newFieldset(getString("searchFilter"), true);
        if (parentPage.getBaseDao().isHistorizable() == true) {
          final RepeatingView repeater = new RepeatingView(FieldsetPanel.DESCRIPTION_SUFFIX_ID);
          fs.setDescriptionSuffix(repeater);
          IconPanel icon = new IconPanel(repeater.newChildId(), IconType.CIRCLE_PLUS, getString("filter.extendedSearch"))
          .setOnClick("javascript:showExtendedFilter();");
          icon.setMarkupId("showExtendedFilter");
          repeater.add(icon);
          icon = new IconPanel(repeater.newChildId(), IconType.CIRCLE_MINUS, getString("filter.extendedSearch"))
          .setOnClick("javascript:hideExtendedFilter();");
          icon.setMarkupId("hideExtendedFilter");
          repeater.add(icon);
        }
        final TextField< ? > searchTextField = createSearchTextField();
        fs.add(searchTextField);
        fs.setLabelFor(searchTextField);
        final Model<String> modifiedSearchExpressionModel = new Model<String>() {
          @Override
          public String getObject()
          {
            return getModifiedSearchExpressionLabel();
          }
        };
        final DivPanel div = new DivPanel(fs.newChildId());
        div.add(AttributeModifier.append("class", "modifiedSearchExpressionLabel"));
        fs.add(div);
        modifiedSearchExpressionLabel = new Label(DivPanel.CHILD_ID, modifiedSearchExpressionModel) {
          @Override
          public boolean isVisible()
          {
            return StringUtils.isNotBlank(searchFilter.getSearchString()) == true;
          }
        };
        modifiedSearchExpressionLabel.setEscapeModelStrings(false);
        div.add(modifiedSearchExpressionLabel);

        fs.addHelpIcon(getString("tooltip.lucene.link"), FieldSetIconPosition.TOP_RIGHT).setOnClickLocation(WebConstants.DOC_LINK_HANDBUCH_LUCENE, true);
        final String helpKeyboardImageTooltip = getHelpKeyboardImageTooltip();
        if (helpKeyboardImageTooltip != null) {
          fs.addKeyboardHelpIcon(helpKeyboardImageTooltip);
        }
      }
    }
    if (parentPage.getBaseDao().isHistorizable() == true && isFilterVisible() == true) {
      addExtendedFilter();
    }
    final WebMarkupContainer buttonCell = new WebMarkupContainer("buttonCell");
    add(buttonCell);
    actionButtons = new MyComponentsRepeater<Component>("actionButtons");
    buttonCell.add(actionButtons.getRepeatingView());

    final Button cancelButton = new Button("button", new Model<String>("cancel")) {
      @Override
      public final void onSubmit()
      {
        getParentPage().onCancelSubmit();
      }
    };
    cancelButton.setDefaultFormProcessing(false);
    cancelButtonPanel = new SingleButtonPanel(getNewActionButtonChildId(), cancelButton, getString("cancel"), SingleButtonPanel.CANCEL);
    addActionButton(cancelButtonPanel);

    final Button resetButton = new Button("button", new Model<String>("reset")) {
      @Override
      public final void onSubmit()
      {
        getParentPage().onResetSubmit();
      }
    };
    resetButton.setDefaultFormProcessing(false);
    resetButtonPanel = new SingleButtonPanel(getNewActionButtonChildId(), resetButton, getString("reset"), SingleButtonPanel.RESET);
    addActionButton(resetButtonPanel);

    final Button nextButton = new Button("button", new Model<String>("next")) {
      @Override
      public final void onSubmit()
      {
        getParentPage().onNextSubmit();
      }
    };
    nextButtonPanel = new SingleButtonPanel(getNewActionButtonChildId(), nextButton, getString("next"), SingleButtonPanel.DEFAULT_SUBMIT);
    addActionButton(nextButtonPanel);

    final Button searchButton = new Button("button", new Model<String>("search")) {
      @Override
      public final void onSubmit()
      {
        getParentPage().onSearchSubmit();
      }
    };
    searchButtonPanel = new SingleButtonPanel(getNewActionButtonChildId(), searchButton, getString("search"),
        SingleButtonPanel.DEFAULT_SUBMIT);
    addActionButton(searchButtonPanel);

    setComponentsVisibility();
  }

  public void addPageSizeFieldset()
  {
    // DropDownChoice page size
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("label.pageSize"));
    fs.add(getPageSizeDropDownChoice(fs.getDropDownChoiceId(), getLocale(), new PropertyModel<Integer>(this, "pageSize"), 25, 1000));
  }

  @SuppressWarnings("serial")
  private void addExtendedFilter()
  {
    extendedFilter = new DivPanel(gridBuilder.newColumnsPanelId());
    gridBuilder.addColumnsPanel(extendedFilter);
    extendedFilter.setMarkupId("extendedFilter");
    {
      gridBuilder.newColumnPanel(DivType.COL_60);
      final FieldsetPanel fieldset = gridBuilder.newFieldset(getString("timePeriod"), getString("lastUpdate"), true);
      fieldset.add(new HiddenInputPanel(fieldset.newChildId(), new HiddenField<Boolean>(InputPanel.WICKET_ID, new PropertyModel<Boolean>(
          searchFilter, "useModificationFilter"))).setHtmlId("useModificationFilter"));

      startDateTimePanel = new DateTimePanel(fieldset.newChildId(), new PropertyModel<Date>(searchFilter, "startTimeOfLastModification"),
          (DateTimePanelSettings) DateTimePanelSettings.get().withSelectProperty("startDateOfLastModification").withSelectPeriodMode(true),
          DatePrecision.MINUTE);
      fieldset.add(startDateTimePanel);
      fieldset.setLabelFor(startDateTimePanel);
      stopDateTimePanel = new DateTimePanel(fieldset.newChildId(), new PropertyModel<Date>(searchFilter, "stopTimeOfLastModification"),
          (DateTimePanelSettings) DateTimePanelSettings.get().withSelectProperty("stopDateOfLastModification").withSelectPeriodMode(true),
          DatePrecision.MINUTE);
      stopDateTimePanel.setRequired(false);
      fieldset.add(stopDateTimePanel);
      final HtmlCommentPanel comment = new HtmlCommentPanel(fieldset.newChildId(), new DatesAsUTCModel() {
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
      });
      fieldset.add(comment);
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
      final DropDownChoice<String> modificationSinceChoice = new DropDownChoice<String>(fieldset.getDropDownChoiceId(),
          new PropertyModel<String>(this, "modificationSince"), timePeriodChoiceRenderer.getValues(), timePeriodChoiceRenderer);
      modificationSinceChoice.setNullValid(true);
      modificationSinceChoice.setRequired(false);
      fieldset.add(modificationSinceChoice, true);
    }

    {
      gridBuilder.newColumnPanel(DivType.COL_40);
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("modifiedBy"), getString("user"));

      final UserSelectPanel userSelectPanel = new UserSelectPanel(fs.newChildId(), new Model<PFUserDO>() {
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
      fs.add(userSelectPanel);
      userSelectPanel.setDefaultFormProcessing(false);
      userSelectPanel.init().withAutoSubmit(true);
    }
  }

  /**
   * Creates a simple TextField and sets the focus on it. Overwrite this method if you want to add for example an auto completion text field
   * (ajax). Please don't forget to call addSearchFieldTooltip() in your method!
   */
  protected TextField< ? > createSearchTextField()
  {
    final TextField<String> searchField = new TextField<String>(InputPanel.WICKET_ID, new PropertyModel<String>(getSearchFilter(),
        "searchString"));
    createSearchFieldTooltip(searchField);
    searchField.add(WicketUtils.setFocus());
    return searchField;
  }

  protected void createSearchFieldTooltip(final Component field)
  {
    WicketUtils.addTooltip(field, getString("search.string.info.title"), getParentPage().getSearchToolTip());
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
    return this.actionButtons.newChildId();
  }

  @Override
  public void onBeforeRender()
  {
    super.onBeforeRender();
    actionButtons.render();
  }

  protected void setComponentsVisibility()
  {
    if (parentPage.isMassUpdateMode() == true) {
      cancelButtonPanel.setVisible(true);
      searchButtonPanel.setVisible(false);
      resetButtonPanel.setVisible(false);
      nextButtonPanel.setVisible(true);
      setDefaultButton(nextButtonPanel.getButton());
    } else {
      if (parentPage.isSelectMode() == false) {
        // Show cancel button only in select mode.
        cancelButtonPanel.setVisible(false);
      }
      searchButtonPanel.setVisible(true);
      resetButtonPanel.setVisible(true);
      nextButtonPanel.setVisible(false);
      setDefaultButton(searchButtonPanel.getButton());
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
    parentPage.setRequestCycleMetaData();
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
        final Object filter = getParentPage().getUserPrefEntry(this.getClass().getName() + ":Filter");
        if (filter != null) {
          if (filter.getClass().equals(newSearchFilterInstance().getClass()) == true) {
            try {
              this.searchFilter = (F) filter;
            } catch (final ClassCastException ex) {
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

  @SuppressWarnings("serial")
  protected CheckBoxPanel createOnlyDeletedCheckBoxPanel(final String id)
  {
    return new CheckBoxPanel(id, new PropertyModel<Boolean>(getSearchFilter(), "deleted"), getString(I18N_ONLY_DELETED)) {
      @Override
      protected boolean wantOnSelectionChangedNotifications()
      {
        return true;
      };

      @Override
      protected void onSelectionChanged(final Boolean newSelection)
      {
        parentPage.refresh();
      };

    }.setTooltip(getString(I18N_ONLY_DELETED_TOOLTIP));
  }

  public CheckBoxPanel createAutoRefreshCheckBoxPanel(final String id, final IModel<Boolean> model, final String label)
  {
    return createAutoRefreshCheckBoxPanel(id, model, label, null);
  }

  @SuppressWarnings("serial")
  protected CheckBoxPanel createAutoRefreshCheckBoxPanel(final String id, final IModel<Boolean> model, final String label,
      final String tooltip)
  {
    final CheckBoxPanel checkBoxPanel = new CheckBoxPanel(id, model, label) {
      @Override
      protected boolean wantOnSelectionChangedNotifications()
      {
        return true;
      };

      @Override
      protected void onSelectionChanged(final Boolean newSelection)
      {
        parentPage.refresh();
      };

    };
    if (tooltip != null) {
      checkBoxPanel.setTooltip(tooltip);
    }
    return checkBoxPanel;
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

  public void setModificationSince(final String modificationSince)
  {
    this.modificationSince = modificationSince;
  }

  public void setPageSize(final Integer pageSize)
  {
    this.pageSize = pageSize;
    if (getParentPage().isStoreFilter() == true) {
      getParentPage().putUserPrefEntry(this.getClass().getName() + ":pageSize", this.pageSize, true);
    }
  }

  protected String getModifiedSearchExpressionLabel()
  {
    return getString("search.lucene.expression") + " " + escapeHtml(BaseDao.modifySearchString(searchFilter.getSearchString()));
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
