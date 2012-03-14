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

package org.projectforge.web.wicket.flowlayout;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * Represents a simple text panel enclosed in a div element.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class DivTextPanel extends Panel
{
  private static final long serialVersionUID = 4900467057369413432L;

  public static final String WICKET_ID = "text";

  private final Label label;

  public DivTextPanel(final String id, final String text, final Behavior... behaviors)
  {
    super(id);
    label = new Label(WICKET_ID, text);
    init(behaviors);
  }

  public DivTextPanel(final String id, final IModel<String> text, final Behavior... behaviors)
  {
    super(id);
    label = new Label(WICKET_ID, text);
    init(behaviors);
  }

  public DivTextPanel(final String id, final Label label)
  {
    super(id);
    this.label = label;
    add(label);
  }

  /**
   * Calls setRenderBodyOnly(false) and setOutputMarkupId(true) for the enclosed label.
   * @return the label
   */
  public Label getLabel4Ajax()
  {
    label.setRenderBodyOnly(false).setOutputMarkupId(true);
    return label;
  }

  /**
   * @return the label
   */
  public Label getLabel()
  {
    return label;
  }

  /**
   * @see org.apache.wicket.Component#setMarkupId(java.lang.String)
   */
  @Override
  public DivTextPanel setMarkupId(final String markupId)
  {
    label.setOutputMarkupId(true);
    label.setMarkupId(markupId);
    return this;
  }

  private void init(final Behavior... behaviors)
  {
    label.add(behaviors);
    add(label);
  }
}
