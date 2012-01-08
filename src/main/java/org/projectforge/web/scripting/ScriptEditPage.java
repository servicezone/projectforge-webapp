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

package org.projectforge.web.scripting;

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.scripting.ScriptDO;
import org.projectforge.scripting.ScriptDao;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.EditPage;


@EditPage(defaultReturnPage = ScriptListPage.class)
public class ScriptEditPage extends AbstractEditPage<ScriptDO, ScriptEditForm, ScriptDao> implements ISelectCallerPage
{
  private static final long serialVersionUID = 4156917767160708873L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ScriptEditPage.class);

  @SpringBean(name = "scriptDao")
  private ScriptDao scriptDao;

  @SuppressWarnings("serial")
  public ScriptEditPage(PageParameters parameters)
  {
    super(parameters, "scripting");
    init();
  }
  
  @Override
  protected ScriptDao getBaseDao()
  {
    return scriptDao;
  }

  @Override
  protected ScriptEditForm newEditForm(AbstractEditPage< ? , ? , ? > parentPage, ScriptDO data)
  {
    return new ScriptEditForm(this, data);
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#select(java.lang.String, java.lang.Integer)
   */
  public void select(String property, Object selectedValue)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  public void unselect(String property)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#cancelSelection(java.lang.String)
   */
  public void cancelSelection(String property)
  {
    // Do nothing.
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
