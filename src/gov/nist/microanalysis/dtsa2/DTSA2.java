package gov.nist.microanalysis.dtsa2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import gov.nist.microanalysis.EPQDatabase.ReferenceDatabase;
import gov.nist.microanalysis.EPQDatabase.Session;
import gov.nist.microanalysis.EPQLibrary.StandardsDatabase2;
import gov.nist.microanalysis.EPQTools.ErrorDialog;
import gov.nist.microanalysis.EPQTools.SwingUtils;

/**
 * <p>
 * Title: EPQ - Electron Probe Quantitation tool kit
 * </p>
 * <p>
 * Description: A series of tools for electron probe quantitation.
 * </p>
 * <p>
 * Copyright: Pursuant to title 17 Section 105 of the United States Code this
 * software is not subject to copyright protection and is in the public domain
 * </p>
 * <p>
 * Company: National Institute of Standards and Technology
 * </p>
 * 
 * @author Nicholas W. M. Ritchie
 * @version 1.0
 */

public class DTSA2 {

   private static final String STANDARDS_DB2 = "standards.sd2.xml";
   private static final String STANDARDS_BAK2 = "standards.sd2.bak";

   static class BuildSession
      extends
      SwingWorker<Session, Integer> {

      /**
       * @see javax.swing.SwingWorker#doInBackground()
       */
      @Override
      protected Session doInBackground()
            throws Exception {
         final String path = HTMLReport.getBasePath() + File.separatorChar + "Database v2";
         final Session ses = new Session(path);
         if(ses.isNew())
            try {
               ses.defaultInitialization();
            }
            catch(final SQLException e) {
               e.printStackTrace();
            }
         ses.getDetectors();
         ReferenceDatabase.getInstance(ses);
         return ses;
      }

   }

   static public final String APP_NAME = "NIST DTSA-II";
   static public final boolean INCLUDE_USER_GROUP = false;
   static private final String SPECTRUM_DIR = "Default spectrum directory";
   static private DTSA2 mApplication;
   private final MainFrame mFrame;
   private static Session mSession;

   static public String getSpectrumDirectory() {
      final Preferences userPref = Preferences.userNodeForPackage(DTSA2.class);
      return userPref.get(SPECTRUM_DIR, System.getProperty("user.home"));
   }

   static public void updateSpectrumDirectory(File path) {
      final Preferences userPref = Preferences.userNodeForPackage(DTSA2.class);
      if(path.exists())
         userPref.put(SPECTRUM_DIR, path.toString());
   }

   // Construct the application
   private DTSA2() {
      mFrame = new MainFrame();
      mFrame.pack();
      final URL url = DTSA2.class.getResource("icon.png");
      if(url != null)
         try {
            final ImageIcon ii = new ImageIcon(url);
            mFrame.setIconImage(ii.getImage());
         }
         catch(final RuntimeException e) {
            // Ignore it...
         }
      mFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      mFrame.setVisible(true);
      mFrame.flushHtml();
   }

   static synchronized public DTSA2 getInstance() {
      if(mApplication == null)
         mApplication = new DTSA2();
      return mApplication;
   }

   public MainFrame getFrame() {
      return mFrame;
   }

   static public HTMLReport getReport() {
      return HTMLReport.getInstance(APP_NAME + " Report");
   }

   static public StandardsDatabase2 getStandardsDatabase() {
      final File path = new File(HTMLReport.getBasePath(), STANDARDS_DB2);
      try {
         return StandardsDatabase2.read(path);
      }
      catch(final Throwable th1) {
         // Try the backup
         try {
            final StandardsDatabase2 sdb = StandardsDatabase2.read(new File(HTMLReport.getBasePath(), STANDARDS_BAK2));
            sdb.write(path);
            return sdb;
         }
         catch(final Throwable th2) {
            return createDefaultSDB(path);
         }
      }
   }

   static private StandardsDatabase2 createDefaultSDB(File path) {
      try {
         try (final InputStream fis = DTSA2.class.getResourceAsStream(STANDARDS_DB2)) {
            if(fis == null)
               throw new IOException("Standard file missing.");
            try (final FileOutputStream fos = new FileOutputStream(path)) {
               final byte[] buffer = new byte[4096];
               for(int len = fis.read(buffer); len > 0; len = fis.read(buffer))
                  fos.write(buffer, 0, len);
            }
         }
         return StandardsDatabase2.read(path);
      }
      catch(final IOException e) {
         final String name = System.getProperty("user.name") + "'s standards";
         final StandardsDatabase2 res = new StandardsDatabase2(name);
         try {
            res.write(path);
         }
         catch(final IOException ex) {
            ErrorDialog.createErrorMessage(getInstance().getFrame(), "Error saving a standards database", ex);
         }
         return res;
      }
   }

   static public void updateStandardsDatabase(StandardsDatabase2 sdb) {
      final File path = new File(HTMLReport.getBasePath(), STANDARDS_DB2);
      if(path.isFile())
         path.renameTo(new File(HTMLReport.getBasePath(), STANDARDS_BAK2));
      try {
         sdb.write(path);
      }
      catch(final IOException e) {
         ErrorDialog.createErrorMessage(DTSA2.getInstance().getFrame(), "Saving standards database", e);
      }
   }

   static public synchronized Session getSession() {
      try {
         if(mSession == null) {
            final BuildSession sb = new BuildSession();
            sb.execute();
            mSession = sb.get();
         }
      }
      catch(final Exception e) {
         ErrorDialog.createErrorMessage(null, APP_NAME + " Database", e);
      }
      return mSession;
   }

   public static String getRevision(Class<?> cls) {
      String res = "???";
      try (final BufferedReader isr = new BufferedReader(new InputStreamReader(cls.getResourceAsStream("revision"), "US-ASCII"))) {
         res = isr.readLine();
      }
      catch(final Exception e) {
      }
      return res;
   }

   public static TitledBorder createTitledBorder(String name) {
      return SwingUtils.createTitledBorder(name);
   }

   public static Border createDefaultBorder() {
      return SwingUtils.createDefaultBorder();
   }

   public static Border createEmptyBorder() {
      return SwingUtils.createEmptyBorder();
   }

   public static int[] getJavaVersion() {
      final String tmp = System.getProperty("java.version");
      final String[] javaVer = tmp.split("\\.");
      final String[] last = javaVer[javaVer.length - 1].split("_");
      int len = javaVer.length - 1 + last.length;
      int[] res = new int[len];
      for(int i = 0; i < javaVer.length - 1; ++i)
         res[i] = Integer.parseInt(javaVer[i]);
      for(int i = 0; i < last.length; ++i)
         res[i + javaVer.length - 1] = Integer.parseInt(last[i]);
      return res;
   }

   public static boolean versionHigherThanOrEqualTo(int[] current, int[] test) {
      for(int i = 0; i < current.length; ++i) {
         if(current[i] < test[i])
            return false;
         if(current[i] > test[i])
            return true;
      }
      // All equal to test..
      return true;
   }

   public static boolean argExists(String[] args, String argItem) {
      for(String arg : args)
         if(arg.startsWith(argItem))
            return true;
      return false;
   }

   // Main method
   public static void main(String[] args) {
      final String os = System.getProperty("os.name").toLowerCase();
      if((os.indexOf("mac") >= 0) || (os.indexOf("os x") >= 0)) {
         System.setProperty("apple.laf.useScreenMenuBar", "true");
         System.setProperty("apple.laf.smallTabs", "true");
      }
      // This eliminates an exception in Jython 2.7.0
      // "console: Failed to install '':
      // java.nio.charset.UnsupportedCharsetException: cp0"
      System.setProperty("python.console.encoding", "UTF-8");
      try {
         final String osName = System.getProperty("os.name");
         // Windows 10 Creator's Update introduced a bug that causes the system
         // look-and-feel to crash for JRE versions less than 1.8.0_144. Use
         // Nimbus look-and-feel instead.
         final int[] testVer = new int[] {
            1,
            8,
            0,
            144
         };
         if((osName.endsWith("10") && (!versionHigherThanOrEqualTo(getJavaVersion(), testVer))) || argExists(args, "-nimbus"))
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
         else
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch(final Exception e) {
         try {
            e.printStackTrace();
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
         }
         catch(final Exception e1) {
            e1.printStackTrace();
         }
      }
      JFrame.setDefaultLookAndFeelDecorated(false);
      AppPreferences.getInstance();
      /*
       * Start a background thread to initialize the database.
       */
      DTSA2.getInstance();
   }
}
