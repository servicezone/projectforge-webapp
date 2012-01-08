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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.DateFormatType;
import org.projectforge.common.DateFormats;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.AbstractBaseDO;
import org.projectforge.core.BaseDao;
import org.projectforge.core.DisplayHistoryEntry;
import org.projectforge.core.ExtendedBaseDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.admin.WizardPage;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.task.TaskTreePage;
import org.projectforge.web.user.UserFormatter;
import org.projectforge.web.user.UserPropertyColumn;

public abstract class AbstractEditPage<O extends AbstractBaseDO< ? >, F extends AbstractEditForm<O, ? >, D extends BaseDao<O>> extends
AbstractSecuredPage implements IEditPage<O, D>
{
  public static final String PARAMETER_KEY_ID = "id";

  public static final String PARAMETER_KEY_DATA_PRESET = "__data";

  protected F form;

  protected List<DisplayHistoryEntry> historyEntries;

  protected boolean showHistory = getBaseDao().isHistorizable();

  protected boolean showModificationTimes = true;

  protected String i18nPrefix;

  protected WebMarkupContainer topMenuPanel;

  protected WebMarkupContainer bottomPanel;

  @SpringBean(name = "userGroupCache")
  protected UserGroupCache userGroupCache;

  @SpringBean(name = "userFormatter")
  protected UserFormatter userFormatter;

  @SpringBean(name = "dateTimeFormatter")
  protected DateTimeFormatter dateTimeFormatter;

  protected EditPageSupport<O, D> editPageSupport;

  public AbstractEditPage(final PageParameters parameters, final String i18nPrefix)
  {
    super(parameters);
    this.i18nPrefix = i18nPrefix;
  }

  protected void init()
  {
    init(null);
  }

  @SuppressWarnings( { "serial", "unchecked"})
  protected void init(O data)
  {
    final StringBuffer buf = new StringBuffer();
    buf.append("function showDeleteQuestionDialog() {\n").append("  return window.confirm('");
    if (getBaseDao().isHistorizable() == true) {
      buf.append(getString("question.markAsDeletedQuestion"));
    } else {
      buf.append(getString("question.deleteQuestion"));
    }
    buf.append("');\n}\n");
    body.add(new Label("showDeleteQuestionDialog", buf.toString()).setEscapeModelStrings(false));
    final Integer id = getPageParameters().getAsInteger(PARAMETER_KEY_ID);
    if (data == null) {
      if (NumberHelper.greaterZero(id) == true) {
        data = getBaseDao().getById(id);
      }
      if (data == null) {
        data = (O) getPageParameters().get(PARAMETER_KEY_DATA_PRESET);
        if (data == null) {
          data = getBaseDao().newInstance();
        }
      }
    }
    form = newEditForm(this, data);

    body.add(form);
    form.init();
    if (form.isNew() == true) {
      showHistory = false;
      showModificationTimes = false;
    }
    final List<IColumn<DisplayHistoryEntry>> columns = new ArrayList<IColumn<DisplayHistoryEntry>>();
    final CellItemListener<DisplayHistoryEntry> cellItemListener = new CellItemListener<DisplayHistoryEntry>() {
      public void populateItem(final Item<ICellPopulator<DisplayHistoryEntry>> item, final String componentId,
          final IModel<DisplayHistoryEntry> rowModel)
      {
        // Later a link should show the history entry as popup.
        item.add(new AttributeModifier("class", true, new Model<String>("notrlink")));
      }
    };
    final DatePropertyColumn<DisplayHistoryEntry> timestampColumn = new DatePropertyColumn<DisplayHistoryEntry>(dateTimeFormatter,
        getString("timestamp"), null, "timestamp", cellItemListener);
    timestampColumn.setDatePattern(DateFormats.getFormatString(DateFormatType.TIMESTAMP_SHORT_MINUTES));
    columns.add(timestampColumn);
    columns.add(new UserPropertyColumn<DisplayHistoryEntry>(getString("user"), null, "user", cellItemListener)
        .withUserFormatter(userFormatter));
    columns
    .add(new CellItemListenerPropertyColumn<DisplayHistoryEntry>(getString("history.entryType"), null, "entryType", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<DisplayHistoryEntry>(getString("history.propertyName"), null, "propertyName",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<DisplayHistoryEntry>(getString("history.newValue"), null, "newValue", cellItemListener) {
      @Override
      public void populateItem(final Item<ICellPopulator<DisplayHistoryEntry>> item, final String componentId,
          final IModel<DisplayHistoryEntry> rowModel)
      {
        if (rowModel.getObject().getNewValue() == null) {
          item.add(new Label(componentId, ""));
        } else {
          item.add(new Label(componentId, rowModel.getObject().getNewValue()));
        }
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<DisplayHistoryEntry>(getString("history.oldValue"), null, "oldValue", cellItemListener) {
      @Override
      public void populateItem(final Item<ICellPopulator<DisplayHistoryEntry>> item, final String componentId,
          final IModel<DisplayHistoryEntry> rowModel)
      {
        if (rowModel.getObject().getOldValue() == null) {
          item.add(new Label(componentId, ""));
        } else {
          item.add(new Label(componentId, rowModel.getObject().getOldValue()));
        }
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    final IColumn<DisplayHistoryEntry>[] colArray = columns.toArray(new IColumn[columns.size()]);
    final IDataProvider<DisplayHistoryEntry> dataProvider = new ListDataProvider<DisplayHistoryEntry>(getHistory());
    final DataTable<DisplayHistoryEntry> dataTable = new DataTable<DisplayHistoryEntry>("historyTable", colArray, dataProvider, 100) {
      @Override
      protected Item<DisplayHistoryEntry> newRowItem(final String id, final int index, final IModel<DisplayHistoryEntry> model)
      {
        return new OddEvenItem<DisplayHistoryEntry>(id, index, model);
      }

      @Override
      public boolean isVisible()
      {
        return showHistory;
      }
    };
    final HeadersToolbar headersToolbar = new HeadersToolbar(dataTable, null);
    dataTable.addTopToolbar(headersToolbar);
    body.add(dataTable);
    final WebMarkupContainer table = new WebMarkupContainer("timeOfModifications");
    table.setVisible(showModificationTimes);
    body.add(table);
    final Label timeOfCreationLabel = new Label("timeOfCreation", dateTimeFormatter.getFormattedDateTime(data.getCreated()));
    timeOfCreationLabel.setRenderBodyOnly(true);
    table.add(timeOfCreationLabel);
    final Label timeOfLastUpdateLabel = new Label("timeOfLastUpdate", dateTimeFormatter.getFormattedDateTime(data.getLastUpdate()));
    timeOfLastUpdateLabel.setRenderBodyOnly(true);
    table.add(timeOfLastUpdateLabel);
    onPreEdit();
    evaluatePageParameters(getPageParameters());
    addBottomPanel();
    this.editPageSupport = new EditPageSupport<O, D>(this, getBaseDao(), getData());
  }

  @Override
  protected void onAfterRender()
  {
    super.onAfterRender();
    showHistory = false;
  }

  protected List<DisplayHistoryEntry> getHistory()
  {
    if (historyEntries == null) {
      historyEntries = getBaseDao().getDisplayHistoryEntries(getData());
    }
    return historyEntries;
  }

  /**
   * Override this method if some initial data or fields have to be set. onPreEdit will be called on both, on adding new data objects and on
   * updating existing data objects. The decision on adding or updating depends on getData().getId() != null.
   */
  protected void onPreEdit()
  {
  }

  /**
   * Will be called before the data object will be stored. Does nothing at default. Any return value is not yet supported.
   */
  public AbstractBasePage onSaveOrUpdate()
  {
    // Do nothing at default
    return null;
  }

  /**
   * Will be called before the data object will be deleted or marked as deleted. Here you can add validation errors manually. If this method
   * returns a resolution then a redirect to this resolution without calling the baseDao methods will done. <br/>
   * Here you can do validations with add(Global)Error or manipulate the data object before storing to the database etc.
   */
  public AbstractBasePage onDelete()
  {
    // Do nothing at default
    return null;
  }

  /**
   * Will be called before the data object will be restored (undeleted). Here you can add validation errors manually. If this method returns
   * a resolution then a redirect to this resolution without calling the baseDao methods will done. <br/>
   * Here you can do validations with add(Global)Error or manipulate the data object before storing to the database etc.
   */
  public AbstractBasePage onUndelete()
  {
    // Do nothing at default
    return null;
  }

  /**
   * Will be called directly after storing the data object (insert, update, delete). If any page is returned then proceed a redirect to this
   * given page.
   */
  public AbstractBasePage afterSaveOrUpdate()
  {
    // Do nothing at default.
    return null;
  }

  /**
   * Will be called directly after storing the data object (insert). Any return value is not yet supported.
   */
  public AbstractBasePage afterSave()
  {
    // Do nothing at default.
    return null;
  }

  /**
   * Will be called directly after storing the data object (update).
   * @param modified true, if the object was modified, otherwise false.If a not null resolution is returned, then the resolution will be
   *          returned to stripes controller.
   * @see BaseDao#update(ExtendedBaseDO)
   */
  public AbstractBasePage afterUpdate(final boolean modified)
  {
    // Do nothing at default.
    return null;
  }

  /**
   * Will be called directly after deleting the data object (delete or update deleted=true). Any return value is not yet supported.
   */
  @Override
  public WebPage afterDelete()
  {
    // Do nothing at default.
    return null;
  }

  /**
   * Will be called directly after un-deleting the data object (update deleted=false). Any return value is not yet supported.
   */
  @Override
  public WebPage afterUndelete()
  {
    // Do nothing at default.
    return null;
  }

  /**
   * If user tried to add a new object and an error was occurred the edit page is shown again and the object id is cleared (set to null).
   */
  public void clearIds()
  {
    getData().setId(null);
  }

  @Override
  public void setResponsePageAndHighlightedRow(final WebPage page)
  {
    if (getData().getId() != null) {
      if (page instanceof AbstractListPage< ? , ? , ? >) {
        // Force reload/refresh of calling AbstractListPage, otherwise the data object will not be updated.
        ((AbstractListPage< ? , ? , ? >) page).setHighlightedRowId(getHighlightedRowId());
        ((AbstractListPage< ? , ? , ? >) page).refresh();
      } else if (returnToPage instanceof TaskTreePage) {
        // Force reload/refresh of calling AbstractListPage, otherwise the data object will not be updated.
        ((TaskTreePage) page).setHighlightedRowId((Integer) getHighlightedRowId());
        ((TaskTreePage) page).refresh();
      } else if (returnToPage instanceof WizardPage) {
        ((WizardPage) returnToPage).setCreatedObject(getData());
      }
    }
    setResponsePage(page);
  }

  /**
   * Overwrite this, if getData().getId() should not be used.
   */
  protected Serializable getHighlightedRowId()
  {
    return getData().getId();
  }

  protected void cancel()
  {
    getLogger().debug("onCancel");
    setResponsePage();
  }

  /**
   * User has clicked the save button for storing a new item.
   */
  protected void create()
  {
    this.editPageSupport.create();
  }

  /**
   * User has clicked the update button for updating an existing item.
   */
  protected void update()
  {
    this.editPageSupport.update();
  }

  /**
   * User has clicked the update button for updating an existing item.
   */
  protected void updateAndNext()
  {
    this.editPageSupport.updateAndNext();
  }

  protected void undelete()
  {
    this.editPageSupport.undelete();
  }

  protected void markAsDeleted()
  {
    this.editPageSupport.markAsDeleted();
  }

  protected void delete()
  {
    this.editPageSupport.delete();
  }

  protected void reset()
  {
    getLogger().debug("onReset");
    // Later: Clearing all fields and restoring data base object.
    throw new UnsupportedOperationException("Reset button not supported.");
  }

  /**
   * Sets the list page (declared as annotation) as response or, if given, the returnToPage.
   */
  public void setResponsePage()
  {
    if (this.returnToPage != null) {
      setResponsePageAndHighlightedRow(this.returnToPage);
    } else {
      final EditPage ann = getClass().getAnnotation(EditPage.class);
      final Class< ? extends AbstractSecuredPage> redirectPage;
      if (ann != null && ann.defaultReturnPage() != null) {
        redirectPage = getClass().getAnnotation(EditPage.class).defaultReturnPage();
      } else {
        redirectPage = WicketUtils.getDefaultPage();
      }
      final PageParameters params = new PageParameters();
      params.put(AbstractListPage.PARAMETER_HIGHLIGHTED_ROW, getData().getId());
      setResponsePage(redirectPage, params);
    }
  }

  /**
   * @return false, if not overridden.
   */
  public boolean isUpdateAndNextSupported()
  {
    return false;
  }

  /**
   * Convenience method.
   * @see AbstractEditForm#getData()
   */
  protected O getData()
  {
    if (form == null || form.getData() == null) {
      getLogger().error("Data of form is null. Maybe you have forgotten to call AbstractEditPage.init() in constructor.");
    }
    return form.getData();
  }

  /**
   * Checks weather the id of the data object is given or not.
   * @return true if the user wants to create a new data object or false for an already existing object.
   */
  public boolean isNew()
  {
    if (form == null) {
      getLogger().error("Data of form is null. Maybe you have forgotten to call AbstractEditPage.init() in constructor.");
    }
    return (getData() == null || getData().getId() == null);
  }

  /**
   * Override this for additional content at the bottom (above the history table). Example in
   * {@link org.projectforge.web.gantt.GanttChartEditPage#addBottomPanel()}.
   */
  protected void addBottomPanel()
  {
    bottomPanel = new WebMarkupContainer("bottomPanel");
    bottomPanel.setVisible(false);
    body.add(bottomPanel);
  }

  /**
   * Calls getString(key) with key "[i18nPrefix].title.edit" or "[i18nPrefix].title.add" dependent weather the data object is already
   * existing or new.
   * @see org.projectforge.web.wicket.AbstractBasePage#getTitle()
   */
  @Override
  protected String getTitle()
  {
    return getString(getTitleKey(i18nPrefix, isNew()));
  }

  /**
   * @param i18nPrefix
   * @param isNew
   * @return i18nPrefix + ".title.add" if isNew is true or i18nPrefix + ".title.edit" otherwise.
   */
  public static String getTitleKey(final String i18nPrefix, final boolean isNew)
  {
    if (isNew == true) {
      return i18nPrefix + ".title.add";
    } else {
      return i18nPrefix + ".title.edit";
    }
  }

  /**
   * Evaluates the page parameters and sets the search filter, if parameters are given.
   * @param parameters
   */
  protected void evaluatePageParameters(final PageParameters parameters)
  {
    WicketUtils.evaluatePageParameters(getData(), parameters, "p", getBookmarkableProperties());
    if (getBookmarkableSelectProperties() != null) {
      WicketUtils.evaluatePageParameters(this, parameters, "p", getBookmarkableSelectProperties());
    }
  }

  /**
   * Overwrite this method if you want to add required page parameters for your bookmarks (basic direct link).
   * @return null at default.
   */
  @Override
  protected PageParameters getBookmarkRequiredPageParameters()
  {
    final PageParameters parameters = new PageParameters();
    if (getData().getId() != null) {
      parameters.put("id", getData().getId());
    }
    return parameters;
  }

  /**
   * Adds the filter as page parameter.
   * @see org.projectforge.web.wicket.AbstractBasePage#getBookmarkPageExtendedParameters()
   */
  @Override
  protected PageParameters getBookmarkPageExtendedParameters()
  {
    final PageParameters pageParameters = new PageParameters(getPageParameters());
    pageParameters.remove("id"); // Don't show id if other extended parameters are given.
    WicketUtils.putPageParameters(getData(), pageParameters, "p", getBookmarkableProperties());
    WicketUtils.putPageParameters(getData(), pageParameters, "p", getBookmarkableSelectProperties());
    return pageParameters;
  }

  protected String[] getBookmarkableProperties()
  {
    return null;
  }

  /**
   * Properties set via ISelectCallerPage.
   */
  protected String[] getBookmarkableSelectProperties()
  {
    return null;
  }

  @Override
  public boolean isAlreadySubmitted()
  {
    return alreadySubmitted;
  }

  @Override
  public void setAlreadySubmitted(final boolean alreadySubmitted)
  {
    this.alreadySubmitted = alreadySubmitted;
  }

  protected abstract D getBaseDao();

  protected abstract Logger getLogger();

  protected abstract F newEditForm(AbstractEditPage< ? , ? , ? > parentPage, O data);
}
