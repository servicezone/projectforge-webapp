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

import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.lang.ClassUtils;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.common.DateFormats;
import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;
import org.projectforge.common.StringHelper;
import org.projectforge.common.TimeNotation;

/**
 * Model for date and time of day components.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class DateTimePanel extends FormComponentPanel<Date>
{
  private static final long serialVersionUID = -3835388673051184738L;

  private static final LabelValueChoiceRenderer<Integer> HOURS_OF_DAY_RENDERER_12;

  private static final LabelValueChoiceRenderer<Integer> HOURS_OF_DAY_RENDERER_24;

  private static final LabelValueChoiceRenderer<Integer> MINUTES_1_RENDERER;

  private static final LabelValueChoiceRenderer<Integer> MINUTES_15_RENDERER;

  static {
    HOURS_OF_DAY_RENDERER_24 = new LabelValueChoiceRenderer<Integer>();
    for (int i = 0; i <= 23; i++) {
      HOURS_OF_DAY_RENDERER_24.addValue(i, StringHelper.format2DigitNumber(i));
    }
    HOURS_OF_DAY_RENDERER_12 = new LabelValueChoiceRenderer<Integer>();
    HOURS_OF_DAY_RENDERER_12.addValue(0, "12 AM");
    for (int i = 1; i <= 11; i++) {
      HOURS_OF_DAY_RENDERER_12.addValue(i, StringHelper.format2DigitNumber(i) + " AM");
    }
    HOURS_OF_DAY_RENDERER_12.addValue(12, "12 PM");
    for (int i = 1; i <= 12; i++) {
      HOURS_OF_DAY_RENDERER_12.addValue(i + 12, StringHelper.format2DigitNumber(i) + " PM");
    }
    MINUTES_1_RENDERER = new LabelValueChoiceRenderer<Integer>();
    for (int i = 0; i <= 59; i++) {
      MINUTES_1_RENDERER.addValue(i, StringHelper.format2DigitNumber(i));
    }
    MINUTES_15_RENDERER = new LabelValueChoiceRenderer<Integer>();
    MINUTES_15_RENDERER.addValue(0, "00");
    MINUTES_15_RENDERER.addValue(15, "15");
    MINUTES_15_RENDERER.addValue(30, "30");
    MINUTES_15_RENDERER.addValue(45, "45");
  }

  private DateHolder dateHolder;

  private boolean isNull;

  private final DatePanel datePanel;

  private final DropDownChoice<Integer> hourOfDayDropDownChoice;

  private final DropDownChoice<Integer> minuteDropDownChoice;

  protected boolean modelMarkedAsChanged;

  protected DateTimePanelSettings settings;

  public static LabelValueChoiceRenderer<Integer> getHourOfDayRenderer()
  {
    return getHourOfDayRenderer(DateFormats.ensureAndGetDefaultTimeNotation());
  }

  public static LabelValueChoiceRenderer<Integer> getHourOfDayRenderer(final TimeNotation timeNotation)
  {
    return timeNotation == TimeNotation.H12 ? HOURS_OF_DAY_RENDERER_12 : HOURS_OF_DAY_RENDERER_24;
  }

  /**
   * 0, 1, ...59 or 0, 15, 30, 45 dependent on DatePrecision.
   * @return
   */
  public static LabelValueChoiceRenderer<Integer> getMinutesRenderer(final DatePrecision precision)
  {
    if (precision == DatePrecision.MINUTE_15) {
      return MINUTES_15_RENDERER;
    }
    return MINUTES_1_RENDERER;
  }

  /**
   * @param id
   * @param model
   * @param precision
   * @param settings.tabIndex Use tabIndex as html tab index of date field, hours and minutes.
   */
  public DateTimePanel(final String id, final IModel<Date> model, final DateTimePanelSettings settings, final DatePrecision precision)
  {
    super(id, model);
    this.settings = settings;
    setType(settings.targetType);
    dateHolder = new DateHolder(model.getObject(), precision);
    final PropertyModel<Date> dateFieldModel = new PropertyModel<Date>(this, "date");
    add(datePanel = new DatePanel("date", dateFieldModel, settings));
    datePanel.setRequired(settings.required);
    hourOfDayDropDownChoice = new DropDownChoice<Integer>("hourOfDay", new PropertyModel<Integer>(this, "hourOfDay"),
        getHourOfDayRenderer().getValues(), getHourOfDayRenderer());
    hourOfDayDropDownChoice.setNullValid(!settings.required);
    hourOfDayDropDownChoice.setRequired(settings.required);
    add(hourOfDayDropDownChoice);
    minuteDropDownChoice = new DropDownChoice<Integer>("minute", new PropertyModel<Integer>(this, "minute"), getMinutesRenderer(
        dateHolder.getPrecision()).getValues(), getMinutesRenderer(dateHolder.getPrecision()));
    minuteDropDownChoice.setNullValid(!settings.required);
    minuteDropDownChoice.setRequired(settings.required);
    add(minuteDropDownChoice);
    if (settings.tabIndex != null) {
      datePanel.dateField.add(new SimpleAttributeModifier("tabindex", String.valueOf(settings.tabIndex)));
      hourOfDayDropDownChoice.add(new SimpleAttributeModifier("tabindex", String.valueOf(settings.tabIndex + 1)));
      minuteDropDownChoice.add(new SimpleAttributeModifier("tabindex", String.valueOf(settings.tabIndex + 2)));
    }
  }

  /**
   * @param field
   * @param amount
   * @see DateHolder#add(int, int)
   */
  public void add(final int field, final int amount)
  {
    dateHolder.add(field, amount);
  }

  public DateHolder getDateHolder()
  {
    return dateHolder;
  }

  public void setDate(final Date date)
  {
    if (date == null && settings.required == false) {
      isNull = true;
    } else {
      this.dateHolder.setDate(date);
    }
  }

  public void setDate(final long millis)
  {
    this.dateHolder.setDate(millis);
  }

  public void setDay(final DateHolder dateHolder)
  {
    this.dateHolder.setDay(dateHolder.getCalendar());
  }

  public Date getDate()
  {
    if (isNull == true) {
      return null;
    }
    return dateHolder.getDate();
  }

  public Timestamp getTimestamp()
  {
    if (isNull == true) {
      return null;
    }
    return this.dateHolder.getTimestamp();
  }

  public Integer getHourOfDay()
  {
    if (isNull == true) {
      return null;
    }
    return dateHolder.getHourOfDay();
  }

  public void setHourOfDay(Integer hourOfDay)
  {
    if (hourOfDay != null) {
      dateHolder.setHourOfDay(hourOfDay);
    }
  }

  public Integer getMinute()
  {
    if (isNull == true) {
      return null;
    }
    return dateHolder.getMinute();
  }

  public void setMinute(Integer minute)
  {
    if (minute != null) {
      dateHolder.setMinute(minute);
    }
  }

  /**
   * Work around: If you change the model call this method, so onBeforeRender calls DateField.modelChanged() for updating the form text
   * field.
   */
  public void markModelAsChanged()
  {
    modelMarkedAsChanged = true;
    datePanel.markModelAsChanged();
  }

  public DatePanel getDatePanel()
  {
    return datePanel;
  }

  public DateTextField getDateField()
  {
    return datePanel.getDateField();
  }

  public void setFocus()
  {
    datePanel.setFocus();
  }

  @Override
  protected void onBeforeRender()
  {
    final Date date = (Date) getModelObject();
    if (date != null) {
      dateHolder.setDate(date);
      isNull = false;
    } else if (settings.required == false) {
      isNull = true;
    }
    if (modelMarkedAsChanged == true) {
      hourOfDayDropDownChoice.modelChanged();
      minuteDropDownChoice.modelChanged();
      modelMarkedAsChanged = false;
    }
    super.onBeforeRender();
  }

  @Override
  protected void convertInput()
  {
    if (isNull == true) {
      setConvertedInput(null);
      return;
    }
    final Date date = (Date) datePanel.getConvertedInput();
    if (date != null) {
      isNull = false;
      getDateHolder().setDate(date);
      final Integer hours = (Integer) hourOfDayDropDownChoice.getConvertedInput();
      final Integer minutes = (Integer) minuteDropDownChoice.getConvertedInput();
      if (hours != null) {
        dateHolder.setHourOfDay(hours);
      }
      if (minutes != null) {
        dateHolder.setMinute(minutes);
      }
      if (ClassUtils.isAssignable(getType(), Timestamp.class) == true) {
        setConvertedInput(dateHolder.getTimestamp());
      } else {
        setConvertedInput(dateHolder.getDate());
      }
    } else if (settings.required == false) {
      isNull = true;
      setConvertedInput(null);
    }
  }

  public String toString()
  {
    if (isNull == true) {
      return null;
    }
    return dateHolder.getDate().toString();
  }
}
