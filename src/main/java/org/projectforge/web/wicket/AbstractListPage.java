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

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.common.NumberHelper;
import org.projectforge.common.RecentQueue;
import org.projectforge.common.ReflectionHelper;
import org.projectforge.common.StringHelper;
import org.projectforge.core.BaseDO;
import org.projectforge.core.UserException;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.components.ContentMenuEntryPanel;

public abstract class AbstractListPage<F extends AbstractListForm< ? , ? >, D extends org.projectforge.core.IDao< ? >, O> extends
AbstractSecuredPage implements ISelectCallerPage
{
  private static final long serialVersionUID = 622509418161777195L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractListPage.class);

  public static final String PARAMETER_KEY_STORE_FILTER = "storeFilter";

  public static final String PARAMETER_KEY_FILTER = "f";

  public static final String PARAMETER_KEY_SEARCH_STRING = PARAMETER_KEY_FILTER + ".s";

  public static final String PARAMETER_HIGHLIGHTED_ROW = "row";

  protected static final String[] BOOKMARKABLE_FILTER_PROPERTIES = new String[] { "searchString|s", "useModificationFilter|mod",
    "modifiedByUserId|mUser", "startTimeOfLastModification|mStart", "stopTimeOfLastModification|mStop", "deleted|del"};

  protected static final String[] BOOKMARKABLE_FORM_PROPERTIES = new String[] { "pageSize"};

  protected static final String[] mergeStringArrays(final String[] a1, final String a2[])
  {
    final String[] result = new String[a1.length + a2.length];
    int pos = 0;
    for (final String str : a1) {
      result[pos++] = str;
    }
    for (final String str : a2) {
      result[pos++] = str;
    }
    return result;
  }

  protected F form;

  protected DataTable<O> dataTable;

  protected List<O> list;

  protected Serializable highlightedRowId;

  protected ISelectCallerPage caller;

  protected String selectProperty;

  protected String i18nPrefix;

  protected ContentMenuEntryPanel newItemMenuEntry;

  protected ContentMenuEntryPanel massUpdateMenuEntry;

  protected ContentMenuEntryPanel selectAllMenuEntry;

  protected ContentMenuEntryPanel deselectAllMenuEntry;

  protected boolean storeFilter = true;

  private boolean massUpdateMode = false;

  /**
   * Change this value if the recent search terms should be stored. Should be set in setup-method of derived page class.
   */
  protected String recentSearchTermsUserPrefKey = null;

  protected RecentQueue<String> recentSearchTermsQueue;

  protected static void addRowClick(final Item< ? > cellItem)
  {
    final Item< ? > row = (cellItem.findParent(Item.class));
    WicketUtils.addRowClick(row);
  }

  /**
   * @param cellItem
   * @param massUpdate If true then a mouse click on the row should (de)activate the check box to select the row for the mass update,
   *          otherwise this method calls addRowClick(Item).
   * @see #addRowClick(Item)
   */
  protected static void addRowClick(final Item< ? > cellItem, final boolean massUpdate)
  {
    if (massUpdate == true) {
      final Item< ? > row = (cellItem.findParent(Item.class));
      row.add(AttributeModifier.replace("onclick", "javascript:rowCheckboxClick(this);"));
    } else {
      addRowClick(cellItem);
    }
  }

  protected AbstractListPage(final PageParameters parameters, final String i18nPrefix)
  {
    this(parameters, null, null, i18nPrefix);
  }

  protected AbstractListPage(final ISelectCallerPage caller, final String selectProperty, final String i18nPrefix)
  {
    this(new PageParameters(), caller, selectProperty, i18nPrefix);
  }

  protected AbstractListPage(final PageParameters parameters, final ISelectCallerPage caller, final String selectProperty,
      final String i18nPrefix)
  {
    super(parameters);
    if (parameters.get(PARAMETER_KEY_STORE_FILTER) != null) {
      final Boolean flag = WicketUtils.getAsBooleanObject(parameters, PARAMETER_KEY_STORE_FILTER);
      if (flag != null && flag == false) {
        storeFilter = false;
      }
    }
    if (parameters.get(PARAMETER_HIGHLIGHTED_ROW) != null) {
      this.highlightedRowId = WicketUtils.getAsInteger(parameters, PARAMETER_HIGHLIGHTED_ROW);
    }
    this.i18nPrefix = i18nPrefix;
    this.caller = caller;
    this.selectProperty = selectProperty;
    setup();
    preInit();
    evaluatePageParameters(parameters);
  }

  /**
   * Is called before the form is initialized in constructor. Overwrite this method if any variables etc. should be set before
   * initialization.
   */
  protected void setup()
  {
  }

  /**
   * Highlight the row representing the data object with the given id.
   * @param highlightedRowId
   */
  public void setHighlightedRowId(final Serializable highlightedRowId)
  {
    this.highlightedRowId = highlightedRowId;
  }

  public Serializable getHighlightedRowId()
  {
    return highlightedRowId;
  }

  @Override
  public void renderHead(final IHeaderResponse response)
  {
    super.renderHead(response);
    response.renderCSSReference("styles/table.css");
  }

  private F getForm()
  {
    if (form == null) {
      form = newListForm(this);
    }
    return form;
  }

  /**
   * @param rowDataId If the current row data is equals to the hightlightedRow then the style will contain highlighting.
   * @param highlightedRowId The current row to highlight (id of the data object behind the row).
   * @param isDeleted Is this entry deleted? Then the deleted style will be added.
   * @return
   */
  protected static StringBuffer getCssStyle(final Serializable rowDataId, final Serializable highlightedRowId, final boolean isDeleted)
  {
    final StringBuffer buf = new StringBuffer();
    if (rowDataId == null) {
      return buf;
    }
    if (rowDataId instanceof Integer == false) {
      log.warn("Error in calling getCssStyle: Integer expected instead of " + rowDataId.getClass());
    }
    if (highlightedRowId != null && rowDataId != null && ObjectUtils.equals(highlightedRowId, rowDataId) == true) {
      buf.append(WicketUtils.getHighlightedRowCssStyle());
    }
    if (isDeleted == true) {
      buf.append("text-decoration: line-through;");
    }
    return buf;
  }

  /**
   * @param rowDataId If the current row data is equals to the hightlightedRow then the style will contain highlighting.
   * @param isDeleted Is this entry deleted? Then the deleted style will be added.
   * @return
   */
  protected StringBuffer getCssStyle(final Serializable rowDataId, final boolean isDeleted)
  {
    return getCssStyle(rowDataId, this.highlightedRowId, isDeleted);
  }

  /**
   * Evaluates the page parameters and sets the search filter, if parameters are given.
   * @param parameters
   */
  protected void evaluatePageParameters(final PageParameters parameters)
  {
    WicketUtils.evaluatePageParameters(form.searchFilter, parameters, PARAMETER_KEY_FILTER, getBookmarkableFilterProperties());
    WicketUtils.evaluatePageParameters(form, parameters, null, getBookmarkableFormProperties());
  }

  protected String[] getBookmarkableFilterProperties()
  {
    return BOOKMARKABLE_FILTER_PROPERTIES;
  }

  protected String[] getBookmarkableFormProperties()
  {
    return BOOKMARKABLE_FORM_PROPERTIES;
  }

  @SuppressWarnings("serial")
  private void preInit()
  {
    getForm();
    body.add(form);
    form.init();
    if (isSelectMode() == false && (accessChecker.isDemoUser() == true || getBaseDao().hasInsertAccess(getUser()) == true)) {
      newItemMenuEntry = new ContentMenuEntryPanel(getNewContentMenuChildId(), new Link<Object>("link") {
        @Override
        public void onClick()
        {
          redirectToEditPage(null);
        };
      }, getString("add"));
      newItemMenuEntry.setAccessKey(WebConstants.ACCESS_KEY_ADD).setTooltip(getString(WebConstants.ACCESS_KEY_ADD_TOOLTIP_TITLE),
          getString(WebConstants.ACCESS_KEY_ADD_TOOLTIP));
      addContentMenuEntry(newItemMenuEntry);
    }
    final Label hintQuickSelectLabel = new Label("hintQuickSelect", new Model<String>(getString("hint.selectMode.quickselect"))) {
      @Override
      public boolean isVisible()
      {
        return isSelectMode();
      }
    };
    if (isSupportsMassUpdate() == true) {
      massUpdateMenuEntry = new ContentMenuEntryPanel(getNewContentMenuChildId(), new Link<Object>("link") {
        @Override
        public void onClick()
        {
          setMassUpdateMode(true);
        };
      }, getString("massUpdate"));
      addContentMenuEntry(massUpdateMenuEntry);

      ExternalLink link = new ExternalLink("link", "#");
      link.add(AttributeModifier.replace("onclick", "javascript:selectAll();"));
      selectAllMenuEntry = new ContentMenuEntryPanel(getNewContentMenuChildId(), link, getString("selectAll"));
      selectAllMenuEntry.setVisible(false);
      addContentMenuEntry(selectAllMenuEntry);

      link = new ExternalLink("link", "#");
      link.add(AttributeModifier.replace("onclick", "javascript:deselectAll();"));
      deselectAllMenuEntry = new ContentMenuEntryPanel(getNewContentMenuChildId(), link, getString("deselectAll"));
      deselectAllMenuEntry.setVisible(false);
      addContentMenuEntry(deselectAllMenuEntry);
    }
    form.add(hintQuickSelectLabel);
    addTopRightMenu();
    addTopPanel();
    addBottomPanel("bottomPanel");
    init();
    createDataTable();
  }

  /**
   * Will be called by the constructors.
   */
  protected abstract void init();

  /**
   * For list pages which supports mass update, please implement this method.
   */
  protected void createDataTable()
  {
  }

  /**
   * Called if the user clicks on the "new" (new entry) link.
   * @param params nullable or set by derived class methods before calling super.onNewClick();
   * @return The edit page (response page). The return value has no effect. It's only useful for derived class methods which calls
   *         super.onNewClick();
   */
  protected AbstractEditPage< ? , ? , ? > redirectToEditPage(PageParameters params)
  {
    if (params == null) {
      params = new PageParameters();
    }
    final Class< ? > editPageClass = getClass().getAnnotation(ListPage.class).editPage();
    final AbstractEditPage< ? , ? , ? > editPage = (AbstractEditPage< ? , ? , ? >) ReflectionHelper.newInstance(editPageClass,
        PageParameters.class, params);
    editPage.setReturnToPage(AbstractListPage.this);
    setResponsePage(editPage);
    return editPage;
  }

  protected abstract D getBaseDao();

  /**
   * @return true, if response page is set for redirect (e. g. for successful quick selection), otherwise false.
   */
  @SuppressWarnings("unchecked")
  protected boolean onSearchSubmit()
  {
    log.debug("onSearchSubmit");
    refresh();
    if (isSelectMode() == true) {
      getList();
      if (list != null && list.size() == 1) {
        // Quick select:
        final O obj = list.get(0);
        caller.select(selectProperty, ((BaseDO<Integer>) obj).getId());
        WicketUtils.setResponsePage(this, caller);
        return true;
      }
    } else {
      final String searchString = form.searchFilter.getSearchString();
      if (searchString != null && searchString.matches("id:[0-9]+") == true) {
        final Integer id = NumberHelper.parseInteger(searchString.substring(3));
        if (id != null) {
          final PageParameters pageParams = new PageParameters();
          pageParams.add(AbstractEditPage.PARAMETER_KEY_ID, String.valueOf(id));
          redirectToEditPage(pageParams);
          return true;
        }
      }
    }
    return false;
  }

  protected void onResetSubmit()
  {
    log.debug("onResetSubmit");
    form.getSearchFilter().reset();
    refresh();
    form.clearInput();
  }

  /**
   * User has pressed the cancel button. If in selection mode then redirect to the caller.
   */
  protected void onCancelSubmit()
  {
    log.debug("onCancelSubmit");
    if (isSelectMode() == true && caller != null) {
      WicketUtils.setResponsePage(this, caller);
      caller.cancelSelection(selectProperty);
    } else if (isMassUpdateMode() == true) {
      setMassUpdateMode(false);
    }
  }

  public void setMassUpdateMode(final boolean mode)
  {
    massUpdateMenuEntry.setVisible(!mode);
    selectAllMenuEntry.setVisible(mode);
    deselectAllMenuEntry.setVisible(mode);
    newItemMenuEntry.setVisible(!mode);
    this.massUpdateMode = mode;
    form.remove(dataTable);
    createDataTable();
    form.setComponentsVisibility();
  }

  protected void onNextSubmit()
  {
    setResponsePage(new MessagePage("message.notYetImplemented"));
  }

  /**
   * Called, if the list must be refreshed. Sets list to null and page size of data table.
   */
  public void refresh()
  {
    list = null; // Force reload of list
    dataTable.setItemsPerPage(form.getPageSize());
    addRecentSearchTerm();
  }

  @SuppressWarnings("unchecked")
  public List<O> getList()
  {
    if (list == null) {
      try {
        list = (List<O>) getBaseDao().getList(form.getSearchFilter());
      } catch (final Exception ex) {
        if (ex instanceof UserException) {
          final UserException userException = (UserException) ex;
          error(getLocalizedMessage(userException.getI18nKey(), userException.getParams()));
        } else {
          log.error(ex.getMessage(), ex);
        }
        list = new ArrayList<O>();
      }
    }
    return list;
  }

  protected abstract F newListForm(AbstractListPage< ? , ? , ? > parentPage);

  protected String getSearchToolTip()
  {
    return getLocalizedMessage("search.string.info", getSearchFields());
  }

  @SuppressWarnings("serial")
  protected void addTopRightMenu()
  {
    log.warn("****** WICKET 1.5 ********: topRightMenu");
    // if (isSelectMode() == false && ((getBaseDao() instanceof BaseDao< ? >) || providesOwnRebuildDatabaseIndex() == true)) {
    // dropDownMenu.setVisible(true);
    // new AbstractReindexTopRightMenu(this, accessChecker.isLoggedInUserMemberOfAdminGroup()) {
    // @Override
    // protected void rebuildDatabaseIndex(final boolean onlyNewest)
    // {
    // if (providesOwnRebuildDatabaseIndex() == true) {
    // ownRebuildDatabaseIndex(onlyNewest);
    // } else {
    // if (onlyNewest == true) {
    // ((BaseDao< ? >) getBaseDao()).rebuildDatabaseIndex4NewestEntries();
    // } else {
    // ((BaseDao< ? >) getBaseDao()).rebuildDatabaseIndex();
    // }
    // }
    // }
    //
    // @Override
    // protected String getString(final String i18nKey)
    // {
    // return NewAbstractListPage.this.getString(i18nKey);
    // }
    // };
    // } else {
    // dropDownMenu.setVisible(false);
    // }
  }

  protected boolean providesOwnRebuildDatabaseIndex()
  {
    return false;
  }

  protected void ownRebuildDatabaseIndex(final boolean onlyNewest)
  {
  }

  /**
   * Override this method if you need a top panel. The default top panel is empty and not visible.
   */
  protected void addTopPanel()
  {
    final Panel topPanel = new EmptyPanel("topPanel");
    topPanel.setVisible(false);
    form.add(topPanel);
  }

  /**
   * Override this method if you need a bottom panel. The default bottom panel is empty and not visible.
   */
  protected void addBottomPanel(final String id)
  {
    final Panel bottomPanel = new EmptyPanel(id);
    bottomPanel.setVisible(false);
    form.add(bottomPanel);
  }

  public boolean isMassUpdateMode()
  {
    return massUpdateMode;
  }

  /**
   * Overwrite this method if your list page does support mass update.
   * @return false at default.
   */
  public boolean isSupportsMassUpdate()
  {
    return false;
  }

  /**
   * Later: Try AjaxFallBackDatatable again.
   * @param columns
   * @param sortProperty
   * @param ascending
   * @return
   */
  protected DataTable<O> createDataTable(final List<IColumn<O>> columns, final String sortProperty, final SortOrder sortOrder)
  {
    final int pageSize = form.getPageSize();
    return new DefaultDataTable<O>("table", columns, createSortableDataProvider(sortProperty, sortOrder), pageSize);
    // return new AjaxFallbackDefaultDataTable<O>("table", columns, createSortableDataProvider(sortProperty, ascending), pageSize);
  }

  /**
   * At default a new SortableDOProvider is returned. Overload this method e. g. for avoiding LazyInitializationExceptions due to sorting.
   * @param sortProperty
   * @param ascending
   */
  protected ISortableDataProvider<O> createSortableDataProvider(final String sortProperty, final SortOrder sortOrder)
  {
    return new ListPageSortableDataProvider(sortProperty, sortOrder);
  }

  /**
   * For displaying the hibernate search fields. Returns list as csv. These fields the user can directly address in his search string, e. g.
   * street:marie.
   * @return
   * @see org.projectforge.core.BaseDao#getSearchFields()
   */
  public String getSearchFields()
  {
    return StringHelper.listToString(", ", getBaseDao().getSearchFields());
  }

  /**
   * @return true, if this page is called for selection by a caller otherwise false.
   */
  public boolean isSelectMode()
  {
    return this.caller != null;
  }

  /**
   * Calls getString(key) with key "[i18nPrefix].title.list" or "[i18nPrefix].title.list.select" dependent weather the list is shown for
   * browsing or selecting (select mode).
   * @see org.projectforge.web.wicket.AbstractUnsecureBasePage#getTitle()
   * @see #isSelectMode()
   */
  @Override
  protected String getTitle()
  {
    if (isSelectMode() == true) {
      return getString(i18nPrefix + ".title.list.select");
    } else {
      return getString(i18nPrefix + ".title.list");
    }
  }

  protected abstract IModel<O> getModel(O object);

  /**
   * If false then the action filter will not be stored (the previous stored filter will be preserved). true is default.
   */
  public boolean isStoreFilter()
  {
    return storeFilter;
  }

  /**
   * Does nothing at default. If overload, don't forget to call super.cancelSelection(String) if no property matches.
   * @see org.projectforge.web.fibu.ISelectCallerPage#cancelSelection(java.lang.String)
   */
  public void cancelSelection(final String property)
  {
    // Do nothing.
  }

  /**
   * Does nothing at default. If overload, don't forget to call super.cancelSelection(String) if no property matches.
   * @see org.projectforge.web.wicket.AbstractListPage#select(java.lang.String, java.lang.Object)
   */
  public void select(final String property, final Object selectedValue)
  {
    if ("modifiedByUserId".equals(property) == true) {
      form.getSearchFilter().setModifiedByUserId((Integer) selectedValue);
      form.getSearchFilter().setUseModificationFilter(true);
      refresh();
    } else {
      log.error("Property '" + property + "' not supported for selection in class " + getClass().getName() + ".");
    }
  }

  /**
   * 
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  public void unselect(final String property)
  {
    if ("modifiedByUserId".equals(property) == true) {
      form.getSearchFilter().setModifiedByUserId(null);
      form.getSearchFilter().setUseModificationFilter(true);
      refresh();
    } else {
      log.error("Property '" + property + "' not supported for selection in class " + getClass().getName() + ".");
    }
  }

  @SuppressWarnings("unchecked")
  public RecentQueue<String> getRecentSearchTermsQueue()
  {
    if (recentSearchTermsQueue == null) {
      recentSearchTermsQueue = (RecentQueue<String>) getUserPrefEntry(this.recentSearchTermsUserPrefKey);
    }
    if (recentSearchTermsQueue == null) {
      recentSearchTermsQueue = new RecentQueue<String>();
      if (isRecentSearchTermsStorage() == true) {
        putUserPrefEntry(this.recentSearchTermsUserPrefKey, recentSearchTermsQueue, true);
      }
    }
    return recentSearchTermsQueue;
  }

  /**
   * Adds the search string to the recent list, if filter is from type BaseSearchFilter and the search string is not blank and not from type
   * id:4711.
   * @param Filter The search filter.
   */
  protected void addRecentSearchTerm()
  {
    if (StringUtils.isNotBlank(form.searchFilter.getSearchString()) == true) {
      final String s = form.searchFilter.getSearchString();
      if (s.startsWith("id:") == false || StringUtils.isNumeric(s.substring(3)) == false) {
        // OK, search string is not from type id:4711
        getRecentSearchTermsQueue().append(s);
      }
    }
  }

  /**
   * @return True, if the user-pref-key for storing the recent search terms is given, otherwise false.
   */
  public boolean isRecentSearchTermsStorage()
  {
    return this.recentSearchTermsUserPrefKey != null;
  }

  /**
   * Tiny helper method.
   * @param propertyName
   * @param sortable
   * @return return sortable ? propertyName : null;
   */
  protected static String getSortable(final String propertyName, final boolean sortable)
  {
    return sortable ? propertyName : null;
  }

  public class ListPageSortableDataProvider extends MySortableDataProvider<O>
  {
    private static final long serialVersionUID = 6940805267003006161L;

    public ListPageSortableDataProvider(final String property, final SortOrder sortOrder)
    {
      super(property, sortOrder);
    }

    @Override
    public List<O> getList()
    {
      return AbstractListPage.this.getList();
    }

    @Override
    protected IModel<O> getModel(final O object)
    {
      return AbstractListPage.this.getModel(object);
    }
  }
}
