/**
 * gov.nist.microanalysis.Trixy.HTMLReport Created by: nritchie Date: Jun 5,
 * 2007
 */
package gov.nist.microanalysis.dtsa2;

import java.awt.Component;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQTools.ErrorDialog;
import gov.nist.microanalysis.dtsa2.DTSA2.OS;

/**
 * <p>
 * A helper to create the base HTML report object.
 * </p>
 * <p>
 * Copyright: Pursuant to title 17 Section 105 of the United States Code this
 * software is not subject to copyright protection and is in the public domain
 * </p>
 * <p>
 * Institution: National Institute of Standards and Technology
 * </p>
 *
 * @author nritchie
 * @version 1.0
 */
public class HTMLReport {

   private static final String sfBASE_PATH = "Base Path";
   private final String mReportName;
   private File mFile;

   static private Map<String, HTMLReport> mInstances = new TreeMap<>();

   private HTMLReport(String reportName) {
      mReportName = reportName;
      final File f = getFile();
      boolean useCSS = true;
      {
         final File css = new File(f.getParentFile(), "style.css");
         if (!css.exists())
            // Write the style sheet
            try (final PrintWriter osw = new PrintWriter(css)) {
               try (final BufferedReader isr = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("style.css")))) {
                  for (String str = isr.readLine(); str != null; str = isr.readLine())
                     osw.println(str);
               }
            } catch (final Exception e) {
               useCSS = false;
            }
      }
      if (!f.exists())
         try {
            final String date = DateFormat.getDateInstance().format(new Date());
            final String time = DateFormat.getTimeInstance().format(new Date());
            try (final PrintWriter pw = new PrintWriter(f)) {
               pw.println("<html>");
               pw.println(" <head>");
               if (useCSS)
                  pw.println("  <link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />");
               pw.println("  <title>" + reportName + " - " + date + "</title>");
               pw.println(" </head>");
               pw.println(" <body>");
               pw.println(
                     "<table class=\"noborder\"><tr><td><h1>NIST DTSA-II</h1></td><td><p align=\"right\">Power tools for x-ray microanalysis</p></td></tr></table>");
               pw.println("  <table>");
               pw.println("   <tr><th align=\"right\">" + DTSA2.APP_NAME + " Version</th><td>" + DTSA2.getRevision(DTSA2.class) + "</td></tr>");
               pw.println("   <tr><th align=\"right\">EPQ Algorithm Library Version</th><td>" + DTSA2.getRevision(EPQException.class) + "</td></tr>");
               pw.println("   <tr><th align=\"right\">System User</th><td>" + System.getProperty("user.name") + "</td></tr>");
               pw.println("   <tr><th align=\"right\">Date</th><td>" + date + "</td></tr>");
               pw.println("   <tr><th align=\"right\">Time</th><td>" + time + "</td></tr>");
               pw.println("  </table>");
               pw.println("  <br>");
               pw.println(" </body>");
               pw.println("</html>");
            }
         } catch (final FileNotFoundException e) {
            throw new Error("Unable to create the report file.");
         }
      mInstances.put(mReportName, this);
   }

   static public synchronized HTMLReport getInstance(String reportName) {
      HTMLReport res = mInstances.get(reportName);
      if (res == null)
         res = new HTMLReport(reportName);
      return res;
   }

   public synchronized File getFile() {
      if (mFile == null) {
         final String base = getBasePath();
         File file = new File(base);
         assert file.exists();
         assert file.isDirectory();
         assert file.canWrite();
         final Calendar c = Calendar.getInstance();
         final Locale locale = Locale.getDefault();
         final File year = new File(file, Integer.toString(c.get(Calendar.YEAR)));
         final File month = new File(year, c.getDisplayName(Calendar.MONTH, Calendar.LONG, locale));
         final File subDir = new File(month, Integer.toString(c.get(Calendar.DAY_OF_MONTH)) + "-"
               + c.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale) + "-" + Integer.toString(c.get(Calendar.YEAR)));
         if (!(subDir.exists() || subDir.mkdirs())) {
            final Error err = new Error("Unable to create report directory.\n" + subDir.getAbsolutePath());
            ErrorDialog.createErrorMessage(DTSA2.getInstance().getFrame(), "Fatal error", err);
            throw err;
         }
         for (int i = 1; i < 1000; ++i) {
            mFile = new File(subDir, "index" + Integer.toString(i) + ".html");
            if (!mFile.exists())
               break;
         }
      }
      return mFile;
   }

   public static String getBasePath() {
      String tmp = Preferences.userNodeForPackage(HTMLReport.class).get(sfBASE_PATH, null);
      File tmpFile = tmp != null ? new File(tmp) : null;
      if ((tmp == null) || !(tmpFile.exists() && tmpFile.isDirectory() && tmpFile.canWrite())) {
         final JFileChooser fc = new JFileChooser();
         fc.setDialogType(JFileChooser.SAVE_DIALOG);
         fc.setDialogTitle("Select a location to store " + DTSA2.APP_NAME + " reports,");
         fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         String def = System.getProperty("user.home", null);
         assert def != null : "user.home is not defined!!!";
         if (DTSA2.getOS() == OS.OS_WINDOWS)
            def = def + File.separator + "My Documents";
         def = def + "\\" + "DTSA-II Reports";
         File file = new File(def);
         final boolean made = !file.exists();
         if (made)
            file.mkdirs();
         if (def != null)
            fc.setCurrentDirectory(file);
         boolean ok = false;
         while (!ok) {
            if (fc.showDialog(null, "Select") == JFileChooser.APPROVE_OPTION) {
               file = fc.getSelectedFile();
               try {
                  ok = false;
                  if (file.exists() && file.canWrite()) {
                     File tester = File.createTempFile("test", ".txt", file);
                     ok = tester.exists();
                     tester.delete();
                     tmp = file.getCanonicalPath();
                     Preferences.userNodeForPackage(HTMLReport.class).put(sfBASE_PATH, tmp);
                  }
               } catch (IOException e) {
                  ErrorDialog.createErrorMessage(null, "Report Directory Creation Error", "The report directory specified is not writable.",
                        e.getMessage());
               }
               if (made)
                  file.delete();
            }
         }
      }
      return tmp;
   }

   public static boolean setBasePath(String path) {
      final File f = new File(path);
      // Suitable
      boolean set = f.exists() && f.isDirectory() && f.canWrite();
      if (set) {
         // Not same as previous
         String oldPath = Preferences.userNodeForPackage(HTMLReport.class).get(sfBASE_PATH, null);
         if (oldPath != null) {
            final File old = new File(oldPath);
            set = !f.equals(old);
         }
      }
      if (set) {
         JOptionPane.showMessageDialog(DTSA2.getInstance().getFrame(),
               "The change in report directories will take place when " + DTSA2.APP_NAME + " is restarted.", DTSA2.APP_NAME,
               JOptionPane.INFORMATION_MESSAGE);
         Preferences.userNodeForPackage(HTMLReport.class).put(sfBASE_PATH, path);
         return true;
      }
      return false;
   }

   public void openInBrowser(Component c) {
      try {
         Desktop.getDesktop().browse(getFile().toURI());
      } catch (final Exception e) {
         ErrorDialog.createErrorMessage(c, "Open report", e);
      }
   }
}
