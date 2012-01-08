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

package org.projectforge.web.wicket.layout;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

/**
 * Represents a field set panel. A form or page can contain multiple field sets.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class FieldSetLPanel extends Panel
{
  private static final long serialVersionUID = 5436255594609615176L;

  private boolean hasChildren;

  /**
   * The markup wicket id of the heading label.
   */
  public static final String HEADING_ID = "heading";

  private Label headingLabel;

  private RepeatingView groupRepeater;

  private WebMarkupContainer fieldSetContainer;

  /**
   * @see AbstractFormRenderer#createFieldSetLPanel(String)
   */
  FieldSetLPanel(final String id)
  {
    super(id);
    add(fieldSetContainer = new WebMarkupContainer("fieldset"));
  }

  /**
   * @see AbstractFormRenderer#createFieldSetPanel(String, String)
   */
  FieldSetLPanel(final String id, final String heading)
  {
    this(id);
    setHeading(heading);
  }

  public FieldSetLPanel add(final GroupLPanel groupPanel)
  {
    hasChildren = true;
    groupRepeater.add(groupPanel);
    return this;
  }

  public boolean hasChildren()
  {
    return hasChildren;
  }

  public String newChildId()
  {
    if (groupRepeater == null) {
      init();
    }
    return groupRepeater.newChildId();
  }

  /**
   * Should only be called manually if no children are added to this field set. Otherwise it'll be initialized at the first call of
   * newChildId().
   */
  public FieldSetLPanel init()
  {
    if (groupRepeater != null) {
      return this;
    }
    if (this.headingLabel != null) {
      fieldSetContainer.add(this.headingLabel);
    } else {
      fieldSetContainer.add(new Label(HEADING_ID, "[invisible]").setVisible(false));
    }
    groupRepeater = new RepeatingView("groupRepeater");
    fieldSetContainer.add(groupRepeater);
    return this;
  }

  public void setHeading(final Label headingLabel)
  {
    this.headingLabel = headingLabel;
  }

  public void setHeading(final String heading)
  {
    if (heading != null) {
      this.headingLabel = new Label(HEADING_ID, heading);
    } else {
      this.headingLabel = null;
    }
  }

  public WebMarkupContainer getFieldSetContainer()
  {
    return fieldSetContainer;
  }
}
