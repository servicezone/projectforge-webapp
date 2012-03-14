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

package org.projectforge.web.wicket.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Response;
import org.projectforge.web.wicket.ImageDef;
import org.projectforge.web.wicket.PresizedImage;
import org.projectforge.web.wicket.WicketUtils;


public class TooltipImage extends PresizedImage
{
  private static final long serialVersionUID = 1333929048394636569L;

  public TooltipImage(final String id, final Response response, final String relativePath, final String tooltip)
  {
    super(id, response, relativePath);
    WicketUtils.addTooltip(this, tooltip).add(AttributeModifier.replace("border", "0"));
  }

  public TooltipImage(final String id, final Response response, final ImageDef imageDef, final String tooltip)
  {
    super(id, response, imageDef);
    WicketUtils.addTooltip(this, tooltip).add(AttributeModifier.replace("border", "0"));
  }

  public TooltipImage(final String id, final Response response, final String relativePath, final IModel<String> tooltip)
  {
    super(id, response, relativePath);
    WicketUtils.addTooltip(this, tooltip).add(AttributeModifier.replace("border", "0"));
  }

  public TooltipImage(final String id, final Response response, final ImageDef imageDef, final IModel<String> tooltip)
  {
    super(id, response, imageDef);
    WicketUtils.addTooltip(this, tooltip).add(AttributeModifier.replace("border", "0"));
  }
}
