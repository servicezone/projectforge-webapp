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

package org.projectforge.fibu.kost;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang.math.IntRange;
import org.junit.Test;
import org.projectforge.core.Priority;
import org.projectforge.xml.stream.AliasMap;
import org.projectforge.xml.stream.XmlHelper;
import org.projectforge.xml.stream.XmlObjectReader;

public class BusinessAssessmentConfigTest
{
  @Test
  public void testReadXml()
  {
    final AliasMap aliasMap = new AliasMap();
    aliasMap.put(BusinessAssessmentRowConfig.class, "row");
    final XmlObjectReader reader = new XmlObjectReader();
    reader.setAliasMap(aliasMap);
    reader.initialize(BusinessAssessmentConfig.class);
    final BusinessAssessmentConfig bwa = (BusinessAssessmentConfig) reader.read(xml);
    assertEquals(50, bwa.getRows().size());
    {
      final BusinessAssessmentRowConfig row = bwa.getRow("1060");
      assertEquals(Priority.HIGH, row.getPriority());
      assertEquals(0, row.getAccountNumbers().size());
      assertEquals(1, row.getAccountNumberRanges().size());
      final IntRange range = row.getAccountNumberRanges().get(0);
      assertEquals(5700, range.getMinimumInteger());
      assertEquals(5999, range.getMaximumInteger());
    }
    {
      final BusinessAssessmentRowConfig row = bwa.getRow("sonstigeKosten");
      assertEquals(1, row.getAccountNumbers().size());
      assertEquals(6300, (int) row.getAccountNumbers().get(0));
      assertEquals(1, row.getAccountNumberRanges().size());
      final IntRange range = row.getAccountNumberRanges().get(0);
      assertEquals(6800, range.getMinimumInteger());
      assertEquals(6855, range.getMaximumInteger());
    }
    {
      final BusinessAssessmentRowConfig row = bwa.getRow("1312");
      assertEquals(3, row.getAccountNumbers().size());
      assertEquals(6392, (int) row.getAccountNumbers().get(0));
      assertEquals(6895, (int) row.getAccountNumbers().get(1));
      assertEquals(6960, (int) row.getAccountNumbers().get(2));
      assertEquals(0, row.getAccountNumberRanges().size());
    }
    {
      final BusinessAssessmentRowConfig row = bwa.getRow("1390");
      assertEquals(0, row.getAccountNumbers().size());
      assertEquals(0, row.getAccountNumberRanges().size());
    }
  }

  private static final String xml = XmlHelper
      .replaceQuotes(XmlHelper.XML_HEADER + "\n" //
          + "<businessAssessment>\n" //
          + "  <rows>\n" //
          + "    <!-- Empty row: -->\n" //
          + "    <row no='1010' />\n" //
          + "    <row no='1020' id='umsatzErloese' accountRange='4000-4799' priority='middle' title='Umsatzerlöse' />\n" //
          + "    <row no='1040' id='bestVerdg' priority='low' title='Best.Verdg. FE/UE' />\n" //
          + "    <row no='1045' id='aktEigenleistungen' priority='low' title='Akt.Eigenleistungen' />\n" //
          + "    <row no='1050' />\n" //
          + "    <row no='1051' id='gesamtleistung' value='=umsatzErloese+bestVerdg+aktEigenleistungen' priority='high' title='Gesamtleistung' />\n" //
          + "    <row no='1052' />\n" //
          + "    <row no='1060' id='matWareneinkauf' accountRange='5700-5999' priority='high' title='Mat./Wareneinkauf' />\n" //
          + "    <row no='1070' />\n" //
          + "    <row no='1080' id='rohertrag' value='=gesamtleistung+matWareneinkauf' priority='high' title='Rohertrag' />\n" //
          + "    <row no='1081' />\n" //
          + "    <row no='1090' id='soBetrErloese' accountRange='4830,4947' priority='low' title='So. betr. Erlöse' />\n" //
          + "    <row no='1091' />\n" //
          + "    <row no='1092' id='betrieblRohertrag' priority='middle' title='Betriebl. Rohertrag' />\n" //
          + "    <row no='1093' />\n" //
          + "    <row no='1094' id='kostenarten' priority='low' title='Kostenarten' />\n" //
          + "    <row no='1100' id='personalkosten' accountRange='6000-6199' priority='high' title='Personalkosten' />\n" //
          + "    <row no='1120' id='raumkosten' accountRange='6310-6350' priority='low' title='Raumkosten' />\n" //
          + "    <row no='1140' id='betrieblSteuern' accountRange='7685' priority='low' title='Betriebl. Steuern' />\n" //
          + "    <row no='1150' id='versichBeitraege' accountRange='6400-6430' priority='low' title='Versich./Beiträge' />\n" //
          + "    <row no='1160' id='fremdleistungen' accountRange='7800' priority='low' title='Fremdleistungen' />\n" //
          + "    <row no='1180' id='kfzKosten' accountRange='6520-6599' priority='low' title='Kfz-Kosten (o. St.)' />\n" //
          + "    <row no='1200' id='werbeReisekosten' accountRange='6600-6699' priority='low' title='Werbe-/Reisekosten' />\n" //
          + "    <row no='1220' id='kostenWarenabgabe' accountRange='6740' priority='low' title='Kosten Warenabgabe' />\n" //
          + "    <row no='1240' id='abschreibungen' accountRange='6200-6299' priority='low' title='Abschreibungen' />\n" //
          + "    <row no='1250' id='reparaturInstandh' accountRange='6470-6490' priority='low' title='Reparatur/Instandh.' />\n" //
          + "    <row no='1260' id='sonstigeKosten' accountRange='6300,6800-6855' priority='low' title='sonstige Kosten' />\n" //
          + "    <row no='1280' id='gesamtKosten'\n" //
          + "      value='=personalkosten+raumkosten+betrieblSteuern+versichBeitraege+fremdleistungen+kfzKosten+werbeReisekosten+kostenWarenabgabe+abschreibungen+reparaturInstandh+sonstigeKosten'\n" //
          + "      priority='high' title='Gesamtkosten' />\n" //
          + "    <row no='1290' />\n" //
          + "    <row no='1300' id='betriebsErgebnis' value='=rohertrag+gesamtKosten' priority='high' title='Betriebsergebnis' />\n" //
          + "    <row no='1301' />\n" //
          + "    <row no='1310' id='zinsaufwand' accountRange='7305,7310' priority='low' indent='2' title='Zinsaufwand' />\n" //
          + "    <row no='1312' id='sonstNeutrAufw' accountRange='6392,6895,6960' priority='low' indent='2' title='Sonst. neutr. Aufw.' />\n" //
          + "    <row no='1320' id='neutralerAufwand' value='=zinsaufwand+sonstNeutrAufw' priority='low' title='Neutraler Aufwand' />\n" //
          + "    <row no='1321' />\n" //
          + "    <row no='1322' id='zinsertraege' accountRange='7100,7110' priority='low' indent='2' title='Zinserträge' />\n" //
          + "    <row no='1323' id='sonstNeutrErtr' accountRange='4845,4855,4925,4930,4937,4960,4970,4975' priority='low' indent='2' title='Sonst. neutr. Ertr' />\n" //
          + "    <row no='1324' id='verrKalkKosten' priority='low' indent='2' title='Verr. kalk. Kosten' />\n" //
          + "    <row no='1330' id='neutralerErtrag' priority='low' title='Neutraler Ertrag' />\n" //
          + "    <row no='1331' />\n" //
          + "    <row no='1340' id='kontenklUnbesetzt' priority='low' title='Kontenkl. unbesetzt' />\n" //
          + "    <row no='1342' />\n" //
          + "    <row no='1345' id='ergebnisVorSteuern' value='=betriebsErgebnis+neutralerAufwand+neutralerErtrag' priority='high' title='Ergebnis vor Steuern' />\n" //
          + "    <row no='1350' />\n" //
          + "    <row no='1355' id='steuernEinkUErtr' accountRange='7600-7640' priority='low' title='Steuern Eink.u.Ertr' />\n" //
          + "    <row no='1360' />\n" //
          + "    <row no='1380' id='vorlaeufigesErgebnis' value='=ergebnisVorSteuern+steuernEinkUErtr' priority='high' title='Vorläufiges Ergebnis' />\n" //
          + "    <row no='1390' value='='/>\n" //
          + "    <row no='' id='erfolgsquote' priority='high' title='Erfolgsquote' />\n" //
          + "    <row no='' id='relativePerformance' priority='high' title='relative Performance' />\n" //
          + "  </rows>\n" //
          + "</businessAssessment>");
}
