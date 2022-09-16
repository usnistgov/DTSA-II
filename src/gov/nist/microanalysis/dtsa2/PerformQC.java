package gov.nist.microanalysis.dtsa2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import gov.nist.microanalysis.EPQDatabase.Session.AlreadyInDatabaseException;
import gov.nist.microanalysis.EPQDatabase.Session.QCEntry;
import gov.nist.microanalysis.EPQDatabase.Session.QCNormalizeMode;
import gov.nist.microanalysis.EPQDatabase.Session.QCProject;
import gov.nist.microanalysis.EPQLibrary.DuaneHuntLimit;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.Element;
import gov.nist.microanalysis.EPQLibrary.FromSI;
import gov.nist.microanalysis.EPQLibrary.ISpectrumData;
import gov.nist.microanalysis.EPQLibrary.RegionOfInterestSet;
import gov.nist.microanalysis.EPQLibrary.SpectrumFitResult;
import gov.nist.microanalysis.EPQLibrary.SpectrumFitter8;
import gov.nist.microanalysis.EPQLibrary.SpectrumFitter8.EnergyScaleFunction;
import gov.nist.microanalysis.EPQLibrary.SpectrumFitter8.FanoNoiseWidth;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;
import gov.nist.microanalysis.EPQLibrary.SpectrumUtils;
import gov.nist.microanalysis.EPQTools.ControlChart;
import gov.nist.microanalysis.Utility.DescriptiveStatistics;
import gov.nist.microanalysis.Utility.HalfUpFormat;
import gov.nist.microanalysis.Utility.UncertainValue2;
import gov.nist.microanalysis.Utility.UtilException;

/**
 * <p>
 * Extract all the logic for adding a spectrum to a QCProject. This makes it
 * easier to script this process in a way that is compatible with the QCWizard.
 * </p>
 * <p>
 * Copyright: Pursuant to title 17 Section 105 of the United States Code this
 * software is not subject to copyright protection and is in the public domain
 * </p>
 * <p>
 * Institution: National Institute of Standards and Technology
 * </p>
 * 
 * @author Tescan
 * @version 1.0
 */
public class PerformQC {

   private final QCProject mQCProject;
   private final ISpectrumData mSpectrum;

   private transient ISpectrumData mFitSpectrum;
   private transient ISpectrumData mResidual;
   private transient Map<String, UncertainValue2> mResults;
   private transient QCEntry mEntry;
   private transient Map<String, DescriptiveStatistics> mLast10;
   private transient Map<String, DescriptiveStatistics> mFirst10;
   private transient Map<String, DescriptiveStatistics> mAll;

   public PerformQC(QCProject qc, ISpectrumData spec) {
      mQCProject = qc;
      mSpectrum = spec;
   }

   public Map<String, UncertainValue2> compute()
         throws SQLException, EPQException, UtilException, IOException {
      if(mResults == null) {
         final TreeMap<String, UncertainValue2> res = new TreeMap<String, UncertainValue2>();
         final SpectrumFitter8 sf8 = new SpectrumFitter8(mQCProject.getDetector(), mQCProject.getStandard(), mSpectrum);
         final double[] coeff = new double[] {
            mQCProject.getDetector().getZeroOffset(),
            mQCProject.getDetector().getChannelWidth()
         };
         final boolean[] fitP = new boolean[] {
            true,
            true
         };
         sf8.setEnergyScale(new EnergyScaleFunction(coeff, fitP));
         sf8.setResolution(new FanoNoiseWidth(1, 0.12, 6.0));
         sf8.setMultiLineset(sf8.buildWeighted(sf8.getROIS()));
         // sf8.setMultiLineset(sf8.buildIndependent(sf8.getROIS(),10.0));
         final SpectrumFitResult sfr = sf8.compute();
         final double i = SpectrumUtils.getAverageFaradayCurrent(mSpectrum.getProperties(), Double.NaN);
         final double totalCounts = SpectrumUtils.totalCounts(SpectrumUtils.applyZeroPeakDiscriminator(mSpectrum), false);
         final double lt = mSpectrum.getProperties().getNumericProperty(SpectrumProperties.LiveTime);
         double dose = Double.NaN;
         if(Double.isNaN(i)) {
            if(mQCProject.getMode() == QCNormalizeMode.CURRENT)
               throw new EPQException("The QC spectrum does not define the required probe current property.");
         } else {
            dose = i * lt;
            res.put("Dose", new UncertainValue2(dose));
         }
         res.put("Total counts", new UncertainValue2(totalCounts));
         res.put("FWHM @ Mn Ka", sfr.getFWHMatMnKa());
         res.put("Fano Factor", sfr.getFanoFactor());
         res.put("Noise", sfr.getNoise());
         res.put("Zero offset", sfr.getZeroOffset());
         res.put("Channel width", sfr.getChannelWidth());
         res.put("Duane-Hunt", new UncertainValue2(FromSI.keV(DuaneHuntLimit.DefaultDuaneHunt.compute(mSpectrum))));
         final double norm = (mQCProject.getMode() == QCNormalizeMode.CURRENT ? dose : totalCounts);
         assert !Double.isNaN(norm);
         {
            double bremCx = totalCounts;
            for(final Element elm : sf8.getElements())
               bremCx -= SpectrumUtils.totalCounts(sf8.getElementSpectrum(elm), false);
            res.put("Brem Counts", new UncertainValue2(bremCx / norm));
         }
         for(final Element elm : mQCProject.getStandard().getElementSet())
            for(final RegionOfInterestSet.RegionOfInterest roi : sf8.getRegionOfInterests(elm)) {
               final String name = roi.toString();
               res.put(name.substring(0, name.indexOf("[") - 1), UncertainValue2.divide(sfr.getIntegratedIntensity(roi.getXRayTransitionSet(elm)), norm));
            }
         mFitSpectrum = sfr.getFitSpectrum(true);
         mResidual = sfr.getResidual();
         mResults = res;
      }
      return mResults;
   }

   /**
    * Gets the current value assigned to qCProject
    * 
    * @return Returns the qCProject.
    */
   public QCProject getQCProject() {
      return mQCProject;
   }

   /**
    * Gets the current value assigned to spectrum
    * 
    * @return Returns the spectrum.
    */
   public ISpectrumData getSpectrum() {
      return mSpectrum;
   }

   /**
    * Gets the current value assigned to fitSpectrum
    * 
    * @return Returns the fitSpectrum.
    */
   public ISpectrumData getFitSpectrum() {
      return mFitSpectrum;
   }

   /**
    * Gets the current value assigned to residual
    * 
    * @return Returns the residual.
    */
   public ISpectrumData getResidual() {
      return mResidual;
   }

   public QCEntry addToProject(Map<String, UncertainValue2> result)
         throws FileNotFoundException, SQLException, EPQException, IOException, AlreadyInDatabaseException, UtilException {
      if(mEntry == null)
         mEntry = mQCProject.addMeasurement(mSpectrum, result);
      return mEntry;
   }

   private String dsVal(String key, DescriptiveStatistics ds) {
      if(ds != null)
         return uncVal(key, ds.getValue(key));
      else
         return "n/a";
   }

   private String uncVal(String key, UncertainValue2 val) {
      if(key.startsWith("Brem") || key.startsWith("Cu") || key.startsWith("Mn") || key.startsWith("FWHM")
            || key.startsWith("Total") || key.startsWith("Zero") || key.startsWith("Dose") || key.startsWith("Noise")) {
         final DecimalFormat nf = new HalfUpFormat("0.000");
         return val.format(nf);
      } else if(key.startsWith("Channel") || key.startsWith("Duane")) {
         final DecimalFormat nf = new HalfUpFormat("0.0000");
         return val.format(nf);
      } else if(key.startsWith("Fano")) {
         final DecimalFormat nf = new HalfUpFormat("0.000");
         return val.format(nf);
      } else
         return val.toString();
   }

   public String toHTML()
         throws FileNotFoundException, SQLException, EPQException, IOException, AlreadyInDatabaseException, UtilException {
      final StringBuffer result = new StringBuffer();
      result.append("<h3>QC Measurement Recorded</h3>\n");
      result.append("<p>\n");
      result.append("<table>\n");
      result.append("<tr><th>Spectrum</th><td>" + mSpectrum.toString() + "</td></tr>\n");
      if(mEntry!=null){
         result.append("<tr><th>Status</th></td>Archived</td>");
         result.append("<tr><th>Index</th><td>" + mEntry.getIndex() + "</td></tr>\n");
         result.append("<tr><th>Timestamp</th><td>" + mEntry.getTimestamp().toString() + "</td></tr>\n");
      } else 
         result.append("<tr><th>Status</th></td>Not archived</td>");
      result.append("<tr><th>Project</th><td>" + mQCProject.toString() + "</td></tr>\n");
      result.append("</table>\n");
      result.append("<table>\n");
      result.append("<tr><th>Name</th><th>Value</th><th>First 10</th><th>Last 10</th><th>All</th></tr>");
      for(final Map.Entry<String, UncertainValue2> me : mResults.entrySet()) {
         final String key = me.getKey();
         result.append("<tr><th>" + key + "</th>");
         result.append("<td>" + uncVal(key, me.getValue()) + "</td>");
         result.append("<td>" + dsVal(key, getFirst10().get(key)) + "</td>");
         result.append("<td>" + dsVal(key, getLast10().get(key)) + "</td>");
         result.append("<td>" + dsVal(key, getAll().get(key)) + "</td></tr>\n");
      }
      result.append("</table>");
      result.append("</p>");
      return result.toString();
   }

   /**
    * Gets the current value assigned to last10
    * 
    * @return Returns the last10.
    * @throws EPQException
    * @throws IOException
    * @throws SQLException
    */
   public Map<String, DescriptiveStatistics> getLast10()
         throws SQLException, IOException, EPQException {
      if(mLast10 == null)
         mLast10 = getQCProject().getEntryStatistics(10, true);
      return mLast10;
   }

   /**
    * Gets the current value assigned to first10
    * 
    * @return Returns the first10.
    * @throws EPQException
    * @throws IOException
    * @throws SQLException
    */
   public Map<String, DescriptiveStatistics> getFirst10()
         throws SQLException, IOException, EPQException {
      if(mFirst10 == null)
         mFirst10 = getQCProject().getEntryStatistics(10, false);
      return mFirst10;
   }

   /**
    * Gets the current value assigned to all
    * 
    * @return Returns the all.
    * @throws EPQException
    * @throws IOException
    * @throws SQLException
    */
   public Map<String, DescriptiveStatistics> getAll()
         throws SQLException, IOException, EPQException {
      if(mAll == null)
         mAll = getQCProject().getEntryStatistics(Integer.MAX_VALUE, false);
      return mAll;
   }

   static String escapeHTML(String str) {
      final StringBuffer sb = new StringBuffer();
      for(int i = 0; i < str.length(); ++i) {
         final int c = str.charAt(i);
         if(c > 0x7F)
            sb.append("&#" + Integer.toString(c) + ";");
         else
            sb.append(str.charAt(i));
      }
      return sb.toString();
   }

   public static void generateReport(QCProject qcp, File file, Collection<String> items)
         throws IOException, SQLException, EPQException {
      final int HEIGHT = 256, WIDTH = 1024;
      final TreeMap<String, ControlChart> charts = new TreeMap<String, ControlChart>();
      final TreeSet<QCEntry> entries = qcp.getEntries();
      for(final QCEntry qce : entries) {
         final Map<String, UncertainValue2> data = qce.getData();
         for(final String item : items) {
            ControlChart cc = charts.get(item);
            if(cc == null) {
               cc = new ControlChart(HEIGHT, WIDTH);
               cc.setName(item);
               charts.put(item, cc);
            }
            final UncertainValue2 uv = data.get(item);
            if(uv != null)
               cc.addDatum(qce.getTimestamp(), uv);
         }
      }
      try (final FileOutputStream out = new FileOutputStream(file)) {
         final File path = file.getParentFile();
         try (final PrintWriter pw = new PrintWriter(out)) {
            final String date = DateFormat.getDateInstance().format(new Date());
            pw.println("<html>");
            pw.println(" <head>");
            pw.println("  <link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />");
            pw.println("  <title>Quality Control Report - " + date + "</title>");
            pw.println(" </head>");
            pw.println(" <body>");
            pw.println("  <h1>Quality Control Report - " + date + "</h1>");
            pw.println("  <p><table cellpadding = 1>");
            pw.println(escapeHTML("   <tr><th align=\"right\">" + DTSA2.APP_NAME + " Version</th><td>"
                  + DTSA2.getRevision(DTSA2.class) + "</td></tr>"));
            pw.println(escapeHTML("   <tr><th align=\"right\">EPQ Version</th><td>" + DTSA2.getRevision(EPQException.class)
                  + "</td></tr>"));
            pw.println(escapeHTML("   <tr><th align=\"right\">Operator</th><td>" + System.getProperty("user.name")
                  + "</td></tr>"));
            pw.println("  </table></p>");
            pw.println("  <br>");
            pw.println("  <p><table cellpadding=1>");
            pw.println("  <tr><th align=\"right\">Item</th><th>Value</th></tr>");
            pw.println(escapeHTML("  <tr><td align=\"right\">Detector</td><td>" + qcp.getDetector() + "</td></tr>"));
            pw.println(escapeHTML("  <tr><td align=\"right\">Beam Energy</td><td>"
                  + (new HalfUpFormat("0.0 keV")).format(qcp.getBeamEnergy()) + "</td></tr>"));
            pw.println(escapeHTML("  <tr><td align=\"right\">Material</td><td>" + qcp.getStandard().toString() + "</td></tr>"));
            pw.println("  </table></p>");

            for(final Map.Entry<String, ControlChart> me : charts.entrySet()) {
               pw.println("<h2>" + escapeHTML(me.getKey()) + "</h2>");
               final ControlChart cc = me.getValue();
               cc.autoRange();
               cc.update();
               final File tmp = File.createTempFile("ControlChart", ".png", path);
               ImageIO.write(cc, "png", tmp);
               pw.print("<p><img width=\"" + Integer.toString(WIDTH) + "\" height=\"" + Integer.toString(HEIGHT) + "\"");
               pw.println(" src=\"" + tmp.toURI().toURL().toExternalForm() + "\" alt=\"" + me.getKey() + "\" /></p>");
            }
            pw.println("<h2>Data table</h2>");
            pw.print("<table><tr><th>Date</th>");
            for(final String item : items)
               pw.print("<th>" + escapeHTML(item) + "</th>");
            pw.println("</tr>");
            final Calendar c = Calendar.getInstance();
            final DateFormat df = DateFormat.getInstance();
            final NumberFormat nf = new HalfUpFormat("0.000E0");
            for(final QCEntry entry : entries) {
               c.setTime(entry.getTimestamp());
               pw.print("<td>" + df.format(c.getTime()) + "</td>");
               final Map<String, UncertainValue2> data = entry.getData();
               for(final String item : items) {
                  final UncertainValue2 uv = data.get(item);
                  pw.print("<td>");
                  if(uv != null) {
                     pw.print(nf.format(uv.doubleValue()));
                     if(uv.uncertainty() > 0.0) {
                        pw.print("</br>");
                        pw.print(nf.format(uv.uncertainty()));
                     }
                  }
                  pw.print("</td>");
               }
               pw.println("</tr>");
            }
            pw.print("</table>");
            pw.println(" </body>");
            pw.println("</html>");
         }
      }
   }
}
