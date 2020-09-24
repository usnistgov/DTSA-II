package gov.nist.microanalysis.dtsa2;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import gov.nist.microanalysis.EPQDatabase.Session;
import gov.nist.microanalysis.EPQLibrary.AbsoluteIonizationCrossSection;
import gov.nist.microanalysis.EPQLibrary.AlgorithmClass;
import gov.nist.microanalysis.EPQLibrary.AlgorithmUser;
import gov.nist.microanalysis.EPQLibrary.BremsstrahlungAngularDistribution;
import gov.nist.microanalysis.EPQLibrary.CorrectionAlgorithm;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.FromSI;
import gov.nist.microanalysis.EPQLibrary.ISpectrumData;
import gov.nist.microanalysis.EPQLibrary.MassAbsorptionCoefficient;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;
import gov.nist.microanalysis.EPQLibrary.SpectrumUtils;
import gov.nist.microanalysis.EPQLibrary.Strategy;
import gov.nist.microanalysis.EPQLibrary.ToSI;
import gov.nist.microanalysis.EPQLibrary.Detector.DetectorProperties;
import gov.nist.microanalysis.EPQLibrary.Detector.EDSCalibration;
import gov.nist.microanalysis.EPQLibrary.Detector.EDSDetector;
import gov.nist.microanalysis.EPQLibrary.Detector.ElectronProbe;
import gov.nist.microanalysis.EPQLibrary.Detector.IXRayWindowProperties;
import gov.nist.microanalysis.EPQLibrary.Detector.MicrocalCalibration;
import gov.nist.microanalysis.EPQLibrary.Detector.SDDCalibration;
import gov.nist.microanalysis.EPQLibrary.Detector.SiLiCalibration;
import gov.nist.microanalysis.EPQLibrary.Detector.XRayWindowFactory;
import gov.nist.microanalysis.EPQTools.EPQXStream;
import gov.nist.microanalysis.EPQTools.ErrorDialog;
import gov.nist.microanalysis.EPQTools.JTextFieldDouble;
import gov.nist.microanalysis.EPQTools.JTextFieldInt;
import gov.nist.microanalysis.EPQTools.SimpleFileFilter;
import gov.nist.microanalysis.dtsa2.PreferenceDialog.PreferencePanel;

/**
 * <p>
 * A dialog for edit various different application preferences and configuration
 * options.
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
public class AppPreferences {

   private static final String DEFAULT_DETECTOR_KEY = "Default Detector";
   private static final String EPQ_JAVA_DOC_DEFAULT = "http://www.cstl.nist.gov/div837/837.02/epq/dtsa2/JavaDoc/index.html";

   // User information
   private String mUserName;
   private final String mBaseReportPath;
   private String mStartupScript = "";
   private String mShutdownScript = "";
   private String mEPQJavaDoc = EPQ_JAVA_DOC_DEFAULT;
   // Algorithms
   private String mCorrectionAlgorithm;
   private String mMACAlgorithm;
   private String mBremAngular;
   private String mIonizationXSec;

   /**
    * Default tolerance for assuming that two spectra are calibrated the same.
    * Use with SpectrumUtils.areCalibratedSimilar.
    */
   static public final double DEFAULT_TOLERANCE = 0.01;

   private final Session mSession;

   static private AppPreferences mInstance = new AppPreferences();

   static private final String CLEAR = " -- None --";

   private class BasicPreferences
      extends
      PreferencePanel {

      private static final long serialVersionUID = -4490213172099255163L;

      private final JTextField jTextField_UserName = new JTextField();
      private final JTextField jTextField_ReportPath = new JTextField();
      private final JButton jButton_ReportPath = new JButton("Browse");
      private final JTextField jTextField_Script = new JTextField();
      private final JButton jButton_ScriptPath = new JButton("Browse");
      private final JTextField jTextField_PostScript = new JTextField();
      private final JButton jButton_PostScriptPath = new JButton("Browse");
      private final JTextField jTextField_EPQDoc = new JTextField();
      private final JButton jButton_EPQDocPath = new JButton("Browse");
      private final JButton jButton_EPQDocDefault = new JButton("Default");

      /**
       * Constructs a UserPreferences
       * 
       * @param pref
       * @param name
       * @param desc
       */
      public BasicPreferences(PreferenceDialog pref) {
         super(pref, "User Information", "General user related information");
         try {
            initialize();
         }
         catch(final Exception ex) {
            ex.printStackTrace();
         }
      }

      private void initialize() {
         final FormLayout fl = new FormLayout("5dlu, right:pref, 5dlu, 150dlu, 3dlu, pref, 3dlu, pref", "pref, 3dlu, pref, 5dlu, pref, 3dlu, pref, 5dlu, pref, 3dlu, pref, 5dlu, pref, 3dlu, pref, 5dlu, pref, 3dlu, pref");
         final PanelBuilder pb = new PanelBuilder(fl, this);
         final CellConstraints cc = new CellConstraints();
         pb.addSeparator("User Information", cc.xyw(1, 1, fl.getColumnCount()));
         pb.addLabel("Your &name", cc.xy(2, 3));
         pb.add(jTextField_UserName, cc.xyw(4, 3, 3));
         pb.addSeparator("HTML Report Location", cc.xyw(1, 5, fl.getColumnCount()));
         pb.addLabel("Base report &path", cc.xy(2, 7));
         pb.add(jTextField_ReportPath, cc.xy(4, 7));
         jTextField_ReportPath.setEditable(false);
         pb.add(jButton_ReportPath, cc.xy(6, 7));

         pb.addSeparator("Start-up script", cc.xyw(1, 9, fl.getColumnCount()));
         pb.addLabel("Python script", cc.xy(2, 11));
         pb.add(jTextField_Script, cc.xy(4, 11));
         jTextField_Script.setEditable(false);
         pb.add(jButton_ScriptPath, cc.xy(6, 11));

         pb.addSeparator("Shut-down script", cc.xyw(1, 13, fl.getColumnCount()));
         pb.addLabel("Python script", cc.xy(2, 15));
         pb.add(jTextField_PostScript, cc.xy(4, 15));
         jTextField_PostScript.setEditable(false);
         pb.add(jButton_PostScriptPath, cc.xy(6, 15));

         pb.addSeparator("EPQ Library JavaDoc Location", cc.xyw(1, 17, fl.getColumnCount()));
         pb.addLabel("JavaDoc location", cc.xy(2, 19));
         pb.add(jTextField_EPQDoc, cc.xy(4, 19));
         jTextField_EPQDoc.setEditable(false);
         pb.add(jButton_EPQDocPath, cc.xy(6, 19));
         pb.add(jButton_EPQDocDefault, cc.xy(8, 19));

         jButton_ReportPath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               final JFileChooser fc = new JFileChooser(mBaseReportPath);
               fc.setDialogType(JFileChooser.SAVE_DIALOG);
               fc.setDialogTitle("Select a location to store " + DTSA2.APP_NAME + " reports,");
               fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
               fc.setFileHidingEnabled(false);
               if(fc.showDialog(BasicPreferences.this, "Select") == JFileChooser.APPROVE_OPTION)
                  jTextField_ReportPath.setText(fc.getSelectedFile().getPath());
            }
         });

         jButton_EPQDocDefault.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               jTextField_EPQDoc.setText(EPQ_JAVA_DOC_DEFAULT);
            }
         });

         jButton_EPQDocPath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               final JFileChooser fc = new JFileChooser(mBaseReportPath);
               fc.setDialogType(JFileChooser.OPEN_DIALOG);
               fc.setDialogTitle("Where is the EPQ library documentation located?");
               fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
               if(fc.showDialog(BasicPreferences.this, "Select") == JFileChooser.APPROVE_OPTION)
                  try {
                     jTextField_EPQDoc.setText(fc.getSelectedFile().getCanonicalPath());
                  }
                  catch(final IOException e1) {
                     jTextField_EPQDoc.setText(EPQ_JAVA_DOC_DEFAULT);
                  }
            }
         });

         class ScriptActionListener
            implements
            ActionListener {

            final private JTextField mField;

            ScriptActionListener(JTextField tf) {
               mField = tf;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
               final JFileChooser jfc = new JFileChooser();
               jfc.setMultiSelectionEnabled(false);
               jfc.setAcceptAllFileFilterUsed(true);
               final SimpleFileFilter sff = new SimpleFileFilter(new String[] {
                  "py",
               }, "Python script");
               jfc.addChoosableFileFilter(sff);
               jfc.setFileFilter(sff);
               jfc.setDialogTitle("Select a script file");
               final File file = new File(mField.getText());
               if(file.isFile())
                  jfc.setSelectedFile(file);
               final int option = jfc.showOpenDialog(BasicPreferences.this);
               String res = CLEAR;
               if(option == JFileChooser.APPROVE_OPTION)
                  res = jfc.getSelectedFile().getAbsolutePath();
               mField.setText(res);
            }
         }

         jButton_ScriptPath.addActionListener(new ScriptActionListener(jTextField_Script));
         jButton_PostScriptPath.addActionListener(new ScriptActionListener(jTextField_PostScript));

         jTextField_ReportPath.setText(getBaseReportPath());
         jTextField_UserName.setText(getUserName());
         jTextField_Script.setText(getStartupScript().equals("") ? CLEAR : getStartupScript());
         jTextField_PostScript.setText(getShutdownScript().equals("") ? CLEAR : getShutdownScript());
         jTextField_EPQDoc.setText(getEPQJavaDoc());
      }

      @Override
      public void commit() {
         setUserName(jTextField_UserName.getText());
         setStartupScript(jTextField_Script.getText().equals(CLEAR) ? "" : jTextField_Script.getText());
         setShutdownScript(jTextField_PostScript.getText().equals(CLEAR) ? "" : jTextField_PostScript.getText());
         setEPQJavaDoc(jTextField_EPQDoc.getText());
         HTMLReport.setBasePath(jTextField_ReportPath.getText());
      }

   }

   private class QuantPreferences
      extends
      PreferencePanel {

      private static final long serialVersionUID = -2082404152395008058L;

      private JComboBox<String> jComboBox_ZAFAlgorithm;
      private JComboBox<String> jComboBox_MAC;
      private JComboBox<String> jComboBox_BremAngular;
      private JComboBox<String> jComboBox_Ionization;

      /**
       * Constructs a QuantPreferences
       * 
       * @param pref
       * @param name
       * @param desc
       */
      public QuantPreferences(PreferenceDialog pref) {
         super(pref, "Quantitative algorithms", "Quantitative algorithm preferences");
         try {
            initialize();
         }
         catch(final Exception ex) {
            ex.printStackTrace();
         }
      }

      private void initialize()
            throws Exception {
         final FormLayout fl = new FormLayout("5dlu, right:pref, 5dlu, 150dlu", "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref");
         setLayout(fl);
         final CellConstraints cc = new CellConstraints();
         add(new JLabel("Correction Algorithm"), cc.xy(2, 1));
         jComboBox_ZAFAlgorithm = new JComboBox<String>();
         jComboBox_ZAFAlgorithm.addItem(CorrectionAlgorithm.XPPExtended.getName());
         jComboBox_ZAFAlgorithm.addItem(CorrectionAlgorithm.PouchouAndPichoir.getName());
         jComboBox_ZAFAlgorithm.addItem(CorrectionAlgorithm.ZAFCorrection.getName());
         jComboBox_ZAFAlgorithm.addItem(CorrectionAlgorithm.NullCorrection.getName());
         jComboBox_ZAFAlgorithm.addItem(CorrectionAlgorithm.Armstrong1982Particle.getName());
         jComboBox_ZAFAlgorithm.setSelectedItem(getCorrectionAlgorithm());
         add(jComboBox_ZAFAlgorithm, cc.xy(4, 1));

         add(new JLabel("Mass Absorption Coefficient"), cc.xy(2, 3));
         jComboBox_MAC = new JComboBox<String>();
         jComboBox_MAC.addItem(MassAbsorptionCoefficient.Sabbatucci2016.getName());
         jComboBox_MAC.addItem(MassAbsorptionCoefficient.Chantler2005.getName());
         jComboBox_MAC.addItem(MassAbsorptionCoefficient.HeinrichDtsa.getName());
         jComboBox_MAC.addItem(MassAbsorptionCoefficient.Heinrich86.getName());
         jComboBox_MAC.addItem(MassAbsorptionCoefficient.BastinHeijligers89.getName());
         jComboBox_MAC.addItem(MassAbsorptionCoefficient.DTSA_CitZAF.getName());
         jComboBox_MAC.addItem(MassAbsorptionCoefficient.Henke82.getName());
         jComboBox_MAC.addItem(MassAbsorptionCoefficient.Henke1993.getName());
         jComboBox_MAC.addItem(MassAbsorptionCoefficient.Pouchou1991.getName());
         jComboBox_MAC.addItem(MassAbsorptionCoefficient.PouchouPichoir88.getName());
         jComboBox_MAC.addItem(MassAbsorptionCoefficient.Ruste79.getName());
         jComboBox_MAC.addItem(MassAbsorptionCoefficient.SuperSet.getName());
         jComboBox_MAC.addItem(MassAbsorptionCoefficient.SuperSet2.getName());
         jComboBox_MAC.addItem(MassAbsorptionCoefficient.Null.getName());
         jComboBox_MAC.setSelectedItem(getMACAlgorithm());
         add(jComboBox_MAC, cc.xy(4, 3));

         jComboBox_BremAngular = new JComboBox<String>();
         add(new JLabel("Bremsstrahlung angular distribution"), cc.xy(2, 5));
         for(final AlgorithmClass alg : BremsstrahlungAngularDistribution.Acosta2002.getAllImplementations())
            jComboBox_BremAngular.addItem(alg.getName());
         jComboBox_BremAngular.setSelectedItem(getBremsstrahlungAngularDistribution());
         add(jComboBox_BremAngular, cc.xy(4, 5));

         jComboBox_Ionization = new JComboBox<String>();
         add(new JLabel("Ionization cross section"), cc.xy(2, 7));
         jComboBox_Ionization.addItem(AbsoluteIonizationCrossSection.BoteSalvat2008.getName());
         jComboBox_Ionization.addItem(AbsoluteIonizationCrossSection.Casnati82.getName());
         jComboBox_Ionization.setSelectedItem(getIonizationCrossSection());
         add(jComboBox_Ionization, cc.xy(4, 7));
      }

      @Override
      public void commit() {
         super.commit();
         setCorrectionAlgorithm(jComboBox_ZAFAlgorithm.getSelectedItem().toString());
         setMACAlgorithm(jComboBox_MAC.getSelectedItem().toString());
         setIonizationCrossSection(jComboBox_Ionization.getSelectedItem().toString());
         setBremsstrahlungAngularDistribution(jComboBox_BremAngular.getSelectedItem().toString());
      }
   }

   private class InstrumentPanel
      extends
      PreferencePanel {

      private static final long serialVersionUID = 5566112298735226722L;

      private final ElectronProbe mInstrument;

      private final JTextField jTextField_Name = new JTextField();
      private final JTextFieldDouble jTextField_MinE0 = new JTextFieldDouble(5.0, 1.0, 500.0);
      private final JTextFieldDouble jTextField_MaxE0 = new JTextFieldDouble(25.0, 1.0, 500.0);

      private JPanel jPanel_Button;

      private InstrumentPanel(PreferenceDialog pref, ElectronProbe ep) {
         super(pref, ep.toString(), "Edit the preferences associated with an e-beam instrument.");
         mInstrument = ep;
         try {
            initialize();
         }
         catch(final Exception ex) {
            ex.printStackTrace();
         }
      }

      private boolean checkDetectorGUID(EDSDetector det) {
         String guid = (String) det.getProperties().getObjectWithDefault(SpectrumProperties.DetectorGUID, null);
         if(guid == null) {
            final DetectorProperties dp = det.getDetectorProperties();
            guid = EPQXStream.generateGUID(dp);
            dp.getProperties().setTextProperty(SpectrumProperties.DetectorGUID, guid);
         }
         final Set<DetectorProperties> allDetectors = mSession.getAllDetectors();
         for(final DetectorProperties other : allDetectors) {
            final String otherGuid = (String) other.getProperties().getObjectWithDefault(SpectrumProperties.DetectorGUID, null);
            if(otherGuid.equals(guid))
               return false;
         }
         return true;
      }

      private void initialize() {
         setLayout(new FormLayout("5dlu, right:pref, 5dlu, 30dlu, 30dlu, right:pref, 5dlu, 30dlu", "20dlu, pref, 10dlu, pref, 20dlu, pref"));
         final CellConstraints cc = new CellConstraints();
         add(new JLabel("Instrument name"), cc.xy(2, 2));
         add(jTextField_Name, cc.xyw(4, 2, 5));
         add(new JLabel("Min beam energy (keV)"), cc.xy(2, 4));
         add(jTextField_MinE0, cc.xy(4, 4));
         add(new JLabel("Max beam energy (keV)"), cc.xy(6, 4));
         add(jTextField_MaxE0, cc.xy(8, 4));
         jTextField_Name.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
               final String newName = jTextField_Name.getText();
               if(newName.length() > 0) {
                  final String name = mInstrument.getProbeProperties().getTextWithDefault(SpectrumProperties.Instrument, null);
                  InstrumentPanel.this.relabel(newName);
                  if((name == null) || (!name.equals(newName))) {
                     mInstrument.setName(newName);
                     mSession.updateInstrument(mInstrument);
                  }
               }
            }
         });

         final JButton siLi = new JButton("Add Si(Li) detector");
         siLi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               try {
                  final EDSDetector det = EDSDetector.createSiLiDetector(2048, 10.0, 132.0);
                  det.setOwner(mInstrument);
                  final PreferenceDialog pd = getPreferenceDialog();
                  pd.addPanel(InstrumentPanel.this, new SiLiPanel(pd, det, true), true);
               }
               catch(final EPQException e1) {
                  e1.printStackTrace();
               }
            }
         });
         final JButton sdd = new JButton("Add SDD detector");
         sdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               try {
                  final EDSDetector det = EDSDetector.createSDDDetector(2048, 5.0, 128.0);
                  det.setOwner(mInstrument);
                  final PreferenceDialog pd = getPreferenceDialog();
                  pd.addPanel(InstrumentPanel.this, new SiLiPanel(pd, det, true), true);
               }
               catch(final EPQException e1) {
                  e1.printStackTrace();
               }
            }
         });

         final JButton ucal = new JButton("Add microcalorimeter");
         ucal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               try {
                  final EDSDetector det = EDSDetector.createMicrocal(8096, 0.8, 5.0);
                  det.setOwner(mInstrument);
                  final PreferenceDialog pd = getPreferenceDialog();
                  pd.addPanel(InstrumentPanel.this, new MicrocalPanel(pd, det, true), true);
               }
               catch(final EPQException e1) {
                  e1.printStackTrace();
               }
            }
         });

         final JButton imprt = new JButton("Import detector");
         imprt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               try {
                  final JFileChooser fc = new JFileChooser();
                  final SimpleFileFilter xdet = new SimpleFileFilter(new String[] {
                     "xdet"
                  }, "XML detector Files (*.xdet)");
                  fc.addChoosableFileFilter(xdet);
                  fc.setAcceptAllFileFilterUsed(true);
                  fc.setFileFilter(xdet);
                  fc.setMultiSelectionEnabled(false);
                  if(fc.showOpenDialog(InstrumentPanel.this) == JFileChooser.APPROVE_OPTION) {
                     final File file = fc.getSelectedFile();
                     final EDSDetector det = EDSDetector.readXML(file);
                     try {
                        boolean ok = checkDetectorGUID(det);
                        while(!ok) {
                           final int res = JOptionPane.showConfirmDialog(InstrumentPanel.this, "It appears like this detector has already been imported?\nShould we continue to import this detector as a new detector?", "Importing a detector", JOptionPane.YES_NO_OPTION);
                           if(res == JOptionPane.YES_OPTION) {
                              final DetectorProperties dp = det.getDetectorProperties();
                              int iter = 0;
                              final String name = det.getDetectorProperties().toString();
                              while(!ok) {
                                 dp.setName(name + " - " + DateFormat.getDateInstance().format(new Date())
                                       + (iter == 0 ? "" : "-" + Integer.toString(iter)));
                                 ++iter;
                                 dp.getProperties().clear(new SpectrumProperties.PropertyId[] {
                                    SpectrumProperties.DetectorGUID
                                 });
                                 ok = checkDetectorGUID(det);
                              }
                           } else
                              return;
                        }
                     }
                     catch(final Exception e1) {
                        return;
                     }
                     det.setOwner(mInstrument);
                     final PreferenceDialog pd = getPreferenceDialog();
                     final SiLiPanel sili = new SiLiPanel(pd, det, true);
                     pd.addPanel(InstrumentPanel.this, sili, true);
                     sili.enableDetector(true);
                     sili.jCheckBox_Enable.setSelected(true);
                  }
               }
               catch(final EPQException e1) {
                  e1.printStackTrace();
               }
            }
         });
         jPanel_Button = new JPanel(new FormLayout("30dlu, pref, 10dlu, pref", "pref, 5dlu, pref"));
         jPanel_Button.add(siLi, cc.xy(2, 1));
         jPanel_Button.add(sdd, cc.xy(4, 1));
         jPanel_Button.add(ucal, cc.xy(2, 3));
         jPanel_Button.add(imprt, cc.xy(4, 3));
         this.add(jPanel_Button, cc.xyw(2, 6, 7));
         // Set data fields
         jTextField_Name.setText(mInstrument.getProbeProperties().getTextWithDefault(SpectrumProperties.Instrument, "?"));
         jTextField_MinE0.setValue(FromSI.keV(mInstrument.getMinBeamEnergy()));
         jTextField_MaxE0.setValue(FromSI.keV(mInstrument.getMaxBeamEnergy()));
      }

      @Override
      public void commit() {
         final boolean modified = jTextField_MinE0.isModified() || jTextField_MaxE0.isModified();
         if(modified) {
            mInstrument.setMinBeamEnergy(ToSI.keV(jTextField_MinE0.getValue()));
            mInstrument.setMaxBeamEnergy(ToSI.keV(jTextField_MaxE0.getValue()));
            mSession.updateInstrument(mInstrument);
         }
         super.commit();
      }
   }

   private class AddInstrument
      extends
      PreferencePanel {

      private static final long serialVersionUID = -4104383724237670723L;

      AddInstrument(PreferenceDialog pref) {
         super(pref, "Instruments and Detectors", "Add or remove e-beam instruments and detectors.");
         try {
            initialize();
         }
         catch(final Exception ex) {
            ex.printStackTrace();
         }
      }

      public void initialize() {
         setLayout(new FormLayout("20dlu, 100dlu, 50dlu, 100dlu", "20dlu, 125dlu, 10dlu, pref"));
         final JTextPane help = new JTextPane();
         help.setContentType("text/html");
         help.setEditable(false);
         help.setText("<HTML><h3 align=right>Overview</h3><p align=left>" + DTSA2.APP_NAME
               + " makes extensive use of user defined detectors to define the instrumentation on which spectra are collected "
               + "or on which sepectra are to be simulated.  In this model, all detectors are associated with instruments.  "
               + "Each instrument may have zero or more detectors.  Detectors definitions contain all the relevant information "
               + "about the performance of the detector including geometry, physical make-up and calibration.  When a detector "
               + "is first constructed, you supply an approximate default calibration.   This calibration may be updated at "
               + "any time using the <i>Calibration alien</i> in the <i>Tools</i> menu.  Multiple calibrations may exist for "
               + "a single detector and the optimal calibration for a specific spectrum is determined by matching the spectrum "
               + "with the calibration with an effective data closest preceding the acquisition time of the spectrum.</p>"
               + "<h3 align=right>Instructions</h3>"
               + "<p align=left>Select an instrument from the pages associated with this preference page or use the <i>Add</i> "
               + "button to create a page associated with a new instrument.   The page will allow you to specify the properties "
               + "of the instrument.  From the instrument page you can edit existing " + "detectors or add new ones." + "</p>");
         help.setBorder(DTSA2.createEmptyBorder());
         final CellConstraints cc = new CellConstraints();
         add(new JScrollPane(help), cc.xyw(2, 2, 3));
         final JButton add = new JButton("Add");
         add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               final PreferenceDialog pd = getPreferenceDialog();
               final InstrumentPanel ip = new InstrumentPanel(pd, new ElectronProbe("Give me a name!"));
               pd.addPanel(AddInstrument.this, ip, true);
               ip.jTextField_Name.selectAll();
               ip.jTextField_Name.requestFocus();
            }
         });
         add(add, cc.xy(3, 4));
         load();
      }

      private void load() {
         final Set<EDSDetector> xrds = mSession.getEDSDetectors(false);
         final TreeMap<ElectronProbe, InstrumentPanel> ei = new TreeMap<ElectronProbe, InstrumentPanel>();
         for(final EDSDetector xrd : xrds) {
            final ElectronProbe ep = xrd.getOwner();
            InstrumentPanel ip = ei.get(ep);
            if(ip == null) {
               ip = new InstrumentPanel(getPreferenceDialog(), ep);
               addChildPanel(ip);
               ei.put(ep, ip);
            }
            try {
               if(xrd.getCalibration() instanceof SiLiCalibration)
                  ip.addChildPanel(new SiLiPanel(getPreferenceDialog(), xrd, false));
               else if(xrd.getCalibration() instanceof MicrocalCalibration)
                  ip.addChildPanel(new MicrocalPanel(getPreferenceDialog(), xrd, false));
            }
            catch(final EPQException e) {
               e.printStackTrace();
            }
         }
      }
   }

   abstract class DetectorPanel
      extends
      PreferencePanel {

      private static final long serialVersionUID = -753371052520033995L;
      protected EDSDetector mDetector;

      protected DetectorPanel(EDSDetector det, PreferenceDialog pref, String desc) {
         super(pref, det.getProperties().getTextWithDefault(SpectrumProperties.DetectorType, "Unknown"), desc);
         mDetector = det;
      }

      DetectorPanel(PreferenceDialog pd, String lable, String desc) {
         super(pd, lable, desc);
      }

      /**
       * Returns the original (pre edited) EDSDetector object
       * 
       * @return EDSDetector
       */
      abstract public EDSDetector getOriginal();

      /**
       * Returns the modified (post-edited) EDSDetector object. Check
       * isModified() to determine if getOriginal() and getModified() represent
       * different detector definitions.
       * 
       * @return EDSDetector
       * @throws EPQException
       */
      abstract public EDSDetector getModified()
            throws EPQException;

      /**
       * Was the original EDSDetector object modified by user input edits?
       * 
       * @return boolean
       */
      abstract public boolean isModified();

   }

   private class SiLiPanel
      extends
      DetectorPanel {

      private static final String AUTO_GENERATE = "Auto Generate";
      private static final String TITLE_BASE = "Detector - ";
      private EDSDetector mOriginal;
      private boolean mNewDetector;
      // Enable/disable detector in UI
      private final JCheckBox jCheckBox_Enable = new JCheckBox();
      // Name
      private final JTextField jTextField_Name = new JTextField();
      private final JTextField jTextField_GUID = new JTextField();
      // Import from spectrum
      private final JButton jButton_Import = new JButton("Import");
      // Location parameters
      private final JLabel jLabel_Geometry = new JLabel();
      private final JTextFieldDouble jTextField_Elevation = new JTextFieldDouble(40.0, -90.0, 90.0);
      private final JTextFieldDouble jTextField_Azimuth = new JTextFieldDouble(0.0, 0.0, 360.0);
      private final JTextFieldDouble jTextField_OptWD = new JTextFieldDouble(10.0, 0.0, 1000.0);
      private final JTextFieldDouble jTextField_DetDistance = new JTextFieldDouble(40.0, 0.0, 1000.0);
      // Crystal paramters
      private final JTextFieldDouble jTextField_Area = new JTextFieldDouble(10.0, 0.01, 1000.0);
      private final JTextFieldDouble jTextField_AuLayer = new JTextFieldDouble(0.0, 0.0, 1000.0);
      private final JTextFieldDouble jTextField_AlLayer = new JTextFieldDouble(0.0, 0.0, 1000.0);
      private final JTextFieldDouble jTextField_NiLayer = new JTextFieldDouble(0.0, 0.0, 1000.0);
      private final JTextFieldDouble jTextField_DeadLayer = new JTextFieldDouble(0.0, 0.0, 1.0, "0.000");
      private final JTextFieldDouble jTextField_Thickness = new JTextFieldDouble(5.0, 0.01, 100.0, "0.00");
      // Configuration parameters
      private final JTextFieldInt jTextField_NChannels = new JTextFieldInt(2048, 10, 65536);
      private final JTextFieldDouble jTextField_ZPD = new JTextFieldDouble(0.0, 0.0, 1000.0);
      // Performance parameters
      private final JTextFieldDouble jTextField_Scale = new JTextFieldDouble(10.0, 0.1, 1000.0);
      private final JTextFieldDouble jTextField_Offset = new JTextFieldDouble(0.0, -10000.0, 10000.0);
      private final JTextFieldDouble jTextField_Resolution = new JTextFieldDouble(130.0, 120.0, 1000.0);
      // Window paramters
      private final JComboBox<String> jComboBox_Window = new JComboBox<String>();
      // Button to export detector
      private final JButton jButton_Export = new JButton("Export detector");

      private static final long serialVersionUID = 3471194954935625380L;

      SiLiPanel(PreferenceDialog pref, EDSDetector det, boolean newDet)
            throws EPQException {
         super(det, pref, "Edit the properties of this detector.");
         mNewDetector = newDet;
         mOriginal = det;
         try {
            initialize();
         }
         catch(final Exception ex) {
            ex.printStackTrace();
         }
      }

      private void enableDetector(boolean enabled) {
         jTextField_Name.setEnabled(enabled);
         jTextField_GUID.setEnabled(isNewDetector() && enabled);
         jTextField_Elevation.setEnabled(enabled);
         jTextField_Azimuth.setEnabled(enabled);
         jTextField_OptWD.setEnabled(enabled);
         jTextField_DetDistance.setEnabled(enabled);
         jTextField_Area.setEnabled(enabled);
         jTextField_AuLayer.setEnabled(enabled);
         jTextField_AlLayer.setEnabled(enabled);
         jTextField_NiLayer.setEnabled(enabled);
         jTextField_DeadLayer.setEnabled(enabled);
         jTextField_Thickness.setEnabled(enabled);
         jTextField_NChannels.setEnabled(isNewDetector() && enabled);
         jTextField_ZPD.setEnabled(enabled);
         jTextField_Scale.setEnabled(isNewDetector() && enabled);
         jTextField_Offset.setEnabled(isNewDetector() && enabled);
         jTextField_Resolution.setEnabled(isNewDetector() && enabled);
         jComboBox_Window.setEnabled(enabled);
         jButton_Import.setEnabled(isNewDetector());
         jButton_Export.setEnabled(true);
      }

      private void initialize() {
         setLayout(new FormLayout("pref", "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"));

         final CellConstraints cc = new CellConstraints();
         final String xFmt = "right:115dlu, 5dlu, 30dlu, 2dlu, 115dlu, 5dlu";

         final PanelBuilder disable = new PanelBuilder(new FormLayout(xFmt, "pref, 2dlu"));
         disable.addLabel("Enable detector", cc.xy(1, 1));
         disable.add(jCheckBox_Enable, cc.xy(3, 1));
         jCheckBox_Enable.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
               final boolean selected = jCheckBox_Enable.isSelected();
               enableDetector(selected);
               final DetectorProperties dp = mOriginal.getDetectorProperties();
               mSession.setEnabled(dp, selected);
               assert mSession.isEnabled(dp) == selected;
            }

         });
         final JPanel disPanel = disable.getPanel();
         disPanel.setBorder(DTSA2.createTitledBorder("Status"));
         add(disPanel, cc.xy(1, 1));

         final PanelBuilder name = new PanelBuilder(new FormLayout(xFmt, "pref, 2dlu, pref, 2dlu"));
         name.addLabel("Detector name", cc.xy(1, 1));
         name.add(jTextField_Name, cc.xyw(3, 1, 3));
         name.addLabel("Globally Unique Identifier", cc.xy(1, 3));
         name.add(jTextField_GUID, cc.xyw(3, 3, 3));
         jTextField_Name.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
               if(jTextField_Name.getText().length() > 0)
                  relabel(TITLE_BASE + jTextField_Name.getText());
            }
         });
         final JPanel namePanel = name.getPanel();
         namePanel.setBorder(DTSA2.createTitledBorder("Name"));
         add(namePanel, cc.xy(1, 3));

         final PanelBuilder imprt = new PanelBuilder(new FormLayout(xFmt, "pref, 2dlu, pref, 2dlu"));
         imprt.addLabel("Import from spectrum", cc.xy(1, 1));
         {
            final ButtonBarBuilder bbb = new ButtonBarBuilder();
            bbb.addButton(jButton_Import);
            bbb.addGlue();
            imprt.add(bbb.build(), cc.xyw(3, 1, 3));
         }
         imprt.addLabel("<html>Often vendor's spectrum files will contain some of the data items in this panel. Use import to populate the panel with the vendor's values. Use care. Vendor's values are sometimes wrong.</html>", cc.xyw(3, 3, 3));
         jButton_Import.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
               final ISpectrumData[] specs = DTSA2.getInstance().getFrame().selectSpectraFromFiles();
               if(specs != null) {
                  int chCount = 0;
                  SpectrumProperties props = null;
                  for(final ISpectrumData spec : specs) {
                     props = SpectrumProperties.merge(props, spec.getProperties());
                     if(chCount == 0)
                        chCount = spec.getChannelCount();
                  }
                  for(final Component c : SiLiPanel.this.getComponents())
                     if(c instanceof JTextField)
                        ((JTextField) c).setBackground(SystemColor.text);
                  final Color updatedColor = Color.yellow;
                  if(props.isDefined(SpectrumProperties.DetectorDescription)) {
                     jTextField_Name.setText(props.getTextWithDefault(SpectrumProperties.DetectorDescription, "Detector"));
                     jTextField_Name.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.DetectorGUID)) {
                     jTextField_GUID.setText(props.getTextWithDefault(SpectrumProperties.DetectorGUID, AUTO_GENERATE));
                     jTextField_GUID.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.Elevation)) {
                     jTextField_Elevation.setValue(props.getNumericWithDefault(SpectrumProperties.Elevation, 35.0));
                     jTextField_Elevation.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.Azimuth)) {
                     jTextField_Azimuth.setValue(props.getNumericWithDefault(SpectrumProperties.Azimuth, 0.0));
                     jTextField_Azimuth.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.WorkingDistance)) {
                     jTextField_OptWD.setValue(props.getNumericWithDefault(SpectrumProperties.WorkingDistance, 20.0));
                     jTextField_OptWD.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.DetectorPosition)
                        && props.isDefined(SpectrumProperties.WorkingDistance))
                     try {
                        jTextField_DetDistance.setValue(SpectrumUtils.getDetectorDistance(props));
                        jTextField_DetDistance.setBackground(updatedColor);
                     }
                     catch(final EPQException e) {
                        // Just ignore it...
                     }
                  if(props.isDefined(SpectrumProperties.DetectorArea)) {
                     jTextField_Area.setValue(props.getNumericWithDefault(SpectrumProperties.DetectorArea, 10.0));
                     jTextField_Area.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.GoldLayer)) {
                     jTextField_AuLayer.setValue(props.getNumericWithDefault(SpectrumProperties.GoldLayer, 0.0));
                     jTextField_AuLayer.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.AluminumLayer)) {
                     jTextField_AlLayer.setValue(props.getNumericWithDefault(SpectrumProperties.AluminumLayer, 0.0));
                     jTextField_AlLayer.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.NickelLayer)) {
                     jTextField_NiLayer.setValue(props.getNumericWithDefault(SpectrumProperties.NickelLayer, 0.0));
                     jTextField_NiLayer.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.DeadLayer)) {
                     jTextField_DeadLayer.setValue(props.getNumericWithDefault(SpectrumProperties.DeadLayer, 0.10));
                     jTextField_DeadLayer.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.ActiveLayer)) {
                     final double thickness = props.getNumericWithDefault(SpectrumProperties.ActiveLayer, 5000.0) / 1000.0;
                     jTextField_Thickness.setValue(thickness);
                     jTextField_Thickness.setBackground(updatedColor);
                  }
                  if(chCount > 0) {
                     jTextField_NChannels.setValue(chCount);
                     jTextField_NChannels.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.ZeroPeakDiscriminator)) {
                     jTextField_ZPD.setValue(props.getNumericWithDefault(SpectrumProperties.ZeroPeakDiscriminator, 0));
                     jTextField_ZPD.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.EnergyScale)) {
                     jTextField_Scale.setValue(props.getNumericWithDefault(SpectrumProperties.EnergyScale, 10.0));
                     jTextField_Scale.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.EnergyOffset)) {
                     jTextField_Offset.setValue(props.getNumericWithDefault(SpectrumProperties.EnergyOffset, 0.0));
                     jTextField_Offset.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.Resolution)) {
                     jTextField_Resolution.setValue(SpectrumUtils.getFWHMAtMnKA(props, 130.0));
                     jTextField_Resolution.setBackground(updatedColor);
                  }
                  getPreferenceDialog().setMessage("Imported properties have been highlighted.");
               }
            }
         });
         final JPanel importPanel = imprt.getPanel();
         importPanel.setBorder(DTSA2.createTitledBorder("Import"));
         add(importPanel, cc.xy(1, 5));

         final PanelBuilder window = new PanelBuilder(new FormLayout(xFmt, "pref, 2dlu"));
         window.addLabel("Window", cc.xy(1, 1));
         window.add(jComboBox_Window, cc.xyw(3, 1, 3));
         jComboBox_Window.setEditable(false);
         final DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
         for(final String s : XRayWindowFactory.WindowTypes)
            cbm.addElement(s);
         // cbm.addElement("User defined window");
         cbm.setSelectedItem(mOriginal.getWindow().getName());
         jComboBox_Window.setModel(cbm);
         final JPanel windowPanel = window.getPanel();
         windowPanel.setBorder(DTSA2.createTitledBorder("Window"));
         add(windowPanel, cc.xy(1, 7));

         final PanelBuilder location = new PanelBuilder(new FormLayout(xFmt, "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 2dlu, pref, 2dlu"));
         jLabel_Geometry.setIcon(new ImageIcon(MainFrame.class.getResource("Detector geometry.PNG")));
         location.add(jLabel_Geometry, cc.xyw(1, 1, 5));
         location.addLabel("Elevation angle", cc.xy(1, 3));
         location.add(jTextField_Elevation, cc.xy(3, 3));
         location.addLabel(SpectrumProperties.Elevation.getUnits(), cc.xy(5, 3));
         location.addLabel("Azimuthal angle", cc.xy(1, 5));
         location.add(jTextField_Azimuth, cc.xy(3, 5));
         location.addLabel(SpectrumProperties.Azimuth.getUnits(), cc.xy(5, 5));
         location.addLabel("Optimal working distance", cc.xy(1, 7));
         location.add(jTextField_OptWD, cc.xy(3, 7));
         location.addLabel(SpectrumProperties.DetectorOptWD.getUnits(), cc.xy(5, 7));
         location.addLabel("Sample-to-detector distance", cc.xy(1, 9));
         location.add(jTextField_DetDistance, cc.xy(3, 9));
         location.addLabel(SpectrumProperties.DetectorDistance.getUnits(), cc.xy(5, 9));
         final JPanel locationPanel = location.getPanel();
         locationPanel.setBorder(DTSA2.createTitledBorder("Position"));
         add(locationPanel, cc.xy(1, 9));

         final PanelBuilder crystal = new PanelBuilder(new FormLayout(xFmt, "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 2dlu"));
         crystal.addLabel("Detector Area", cc.xy(1, 1));
         crystal.add(jTextField_Area, cc.xy(3, 1));
         crystal.addLabel(SpectrumProperties.DetectorArea.getUnits(), cc.xy(5, 1));
         crystal.addLabel("Gold layer", cc.xy(1, 3));
         crystal.add(jTextField_AuLayer, cc.xy(3, 3));
         crystal.addLabel(SpectrumProperties.GoldLayer.getUnits(), cc.xy(5, 3));
         crystal.addLabel("Aluminum layer", cc.xy(1, 5));
         crystal.add(jTextField_AlLayer, cc.xy(3, 5));
         crystal.addLabel(SpectrumProperties.AluminumLayer.getUnits(), cc.xy(5, 5));
         crystal.addLabel("Nickel layer", cc.xy(1, 7));
         crystal.add(jTextField_NiLayer, cc.xy(3, 7));
         crystal.addLabel(SpectrumProperties.NickelLayer.getUnits(), cc.xy(5, 7));
         crystal.addLabel("Dead layer", cc.xy(1, 9));
         crystal.add(jTextField_DeadLayer, cc.xy(3, 9));
         crystal.addLabel(SpectrumProperties.DeadLayer.getUnits(), cc.xy(5, 9));
         crystal.addLabel("Thickness", cc.xy(1, 11));
         crystal.add(jTextField_Thickness, cc.xy(3, 11));
         crystal.addLabel(SpectrumProperties.DetectorThickness.getUnits(), cc.xy(5, 11));
         final JPanel crystalPanel = crystal.getPanel();
         crystalPanel.setBorder(DTSA2.createTitledBorder("Crystal parameters"));
         add(crystalPanel, cc.xy(1, 11));

         final PanelBuilder config = new PanelBuilder(new FormLayout(xFmt, "pref, 5dlu, pref, 2dlu"));
         config.addLabel("Number of channels", cc.xy(1, 1));
         config.add(jTextField_NChannels, cc.xy(3, 1));
         jTextField_NChannels.setToolTipText("<html>This property is not editable for established detectors.");
         config.addLabel("channels", cc.xy(5, 1));
         config.addLabel("Zero strobe discriminator", cc.xy(1, 3));
         config.add(jTextField_ZPD, cc.xy(3, 3));
         config.addLabel("eV", cc.xy(5, 3));
         final JPanel configPanel = config.getPanel();
         configPanel.setBorder(DTSA2.createTitledBorder("Configuration"));
         add(configPanel, cc.xy(1, 13));

         final PanelBuilder performance = new PanelBuilder(new FormLayout(xFmt, "pref, 5dlu, pref, 5dlu, pref, 2dlu"));
         performance.addLabel("Energy scale", cc.xy(1, 1));
         performance.add(jTextField_Scale, cc.xy(3, 1));
         performance.addLabel(SpectrumProperties.EnergyScale.getUnits(), cc.xy(5, 1));
         performance.addLabel("Zero offset", cc.xy(1, 3));
         performance.add(jTextField_Offset, cc.xy(3, 3));
         performance.addLabel(SpectrumProperties.EnergyOffset.getUnits(), cc.xy(5, 3));
         performance.addLabel("Resolution", cc.xy(1, 5));
         performance.add(jTextField_Resolution, cc.xy(3, 5));
         performance.addLabel(SpectrumProperties.Resolution.getUnits() + " FWHM at Mn K\u03B1", cc.xy(5, 5));
         final JPanel perfPanel = performance.getPanel();
         perfPanel.setToolTipText("<html>These properties are not editable for established detectors.<br/>Use the calibration alien to refine them.");
         perfPanel.setBorder(DTSA2.createTitledBorder("Base Performance"));
         add(perfPanel, cc.xy(1, 15));

         {
            final ButtonBarBuilder bbb = new ButtonBarBuilder();
            bbb.addGlue();
            jButton_Export.setToolTipText("Export the detector definition as XML to facilitate sharing this information with colleagues.");
            bbb.addButton(jButton_Export);
            add(bbb.build(), cc.xy(1, 17));
         }
         jButton_Export.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
               final JFileChooser jfc = new JFileChooser();
               jfc.setMultiSelectionEnabled(false);
               jfc.setAcceptAllFileFilterUsed(true);
               final SimpleFileFilter sff = new SimpleFileFilter(new String[] {
                  "xdet",
               }, "XML detector file (*.xdet)");
               jfc.addChoosableFileFilter(sff);
               jfc.setAcceptAllFileFilterUsed(true);
               jfc.setFileFilter(sff);
               jfc.setDialogTitle("Save to XML detector file");
               try {
                  final EDSDetector det = getModified();
                  final File file = new File(det.getName() + ".xdet");
                  jfc.setSelectedFile(file);
                  final int option = jfc.showSaveDialog(SiLiPanel.this);
                  if(option == JFileChooser.APPROVE_OPTION)
                     det.writeXML(jfc.getSelectedFile());
               }
               catch(final Exception e) {
                  ErrorDialog.createErrorMessage(SiLiPanel.this.getPreferenceDialog(), "Error saving detector", e);
               }
            }
         });

         // Update values
         jCheckBox_Enable.setSelected(isNewDetector() || mSession.isEnabled(mOriginal.getDetectorProperties()));
         enableDetector(jCheckBox_Enable.isSelected());
         jTextField_Name.setText(mOriginal.getName());
         relabel(TITLE_BASE + mOriginal.getName());
         {
            final SpectrumProperties dp = mOriginal.getDetectorProperties().getProperties();
            jTextField_GUID.setText(dp.getTextWithDefault(SpectrumProperties.DetectorGUID, AUTO_GENERATE));
            // Location parameters
            jTextField_Elevation.setValue(dp.getNumericWithDefault(SpectrumProperties.Elevation, 40.0));
            jTextField_Azimuth.setValue(dp.getNumericWithDefault(SpectrumProperties.Azimuth, 0.0));
            jTextField_OptWD.setValue(dp.getNumericWithDefault(SpectrumProperties.DetectorOptWD, 15.0));
            jTextField_DetDistance.setValue(dp.getNumericWithDefault(SpectrumProperties.DetectorDistance, 40.0));
            // Crystal paramters
            jTextField_Area.setValue(dp.getNumericWithDefault(SpectrumProperties.DetectorArea, 10.0));
            jTextField_AuLayer.setValue(dp.getNumericWithDefault(SpectrumProperties.GoldLayer, 0.0));
            jTextField_AlLayer.setValue(dp.getNumericWithDefault(SpectrumProperties.AluminumLayer, 0.0));
            jTextField_NiLayer.setValue(dp.getNumericWithDefault(SpectrumProperties.NickelLayer, 0.0));
            jTextField_DeadLayer.setValue(dp.getNumericWithDefault(SpectrumProperties.DeadLayer, 0.1));
            jTextField_Thickness.setValue(dp.getNumericWithDefault(SpectrumProperties.DetectorThickness, 5.0));
            jTextField_NChannels.setValue(mOriginal.getChannelCount());
            jTextField_ZPD.setValue(dp.getNumericWithDefault(SpectrumProperties.ZeroPeakDiscriminator, 0.0));
         }
         {
            final EDSCalibration cp = mOriginal.getCalibration();
            // Configuration parameters
            // Performance parameters
            jTextField_Scale.setValue(cp.getChannelWidth());
            jTextField_Offset.setValue(cp.getZeroOffset());
            jTextField_Resolution.setValue(cp.getLineshape().getFWHMatMnKa());
         }
      }

      private boolean isModified(Component c) {
         if(c instanceof JPanel) {
            final JPanel p = (JPanel) c;
            for(final Component child : p.getComponents())
               if(isModified(child))
                  return true;
         }
         if(c instanceof JTextFieldDouble)
            if(((JTextFieldDouble) c).isModified())
               return true;
         if(c instanceof JTextFieldInt)
            if(((JTextFieldInt) c).isModified())
               return true;
         return false;
      }

      private void clearModified(Component c) {
         if(c instanceof JPanel) {
            final JPanel p = (JPanel) c;
            for(final Component child : p.getComponents())
               clearModified(child);
         }
         if(c instanceof JTextFieldDouble)
            ((JTextFieldDouble) c).setModified(false);
         if(c instanceof JTextFieldInt)
            ((JTextFieldInt) c).setModified(false);
      }

      @Override
      public boolean isModified() {
         if(!jTextField_Name.getText().equals(mOriginal.getName()))
            return true;
         if(jTextField_GUID.getText().equals(AUTO_GENERATE))
            return true;
         if(!((String) jComboBox_Window.getSelectedItem()).equals(mOriginal.getWindow().getName()))
            return true;
         return isModified(this);
      }

      public boolean isNewDetector() {
         return mNewDetector;
      }

      @Override
      public EDSDetector getModified()
            throws EPQException {
         EDSDetector det = mOriginal;
         if(isModified() || isNewDetector()) {
            // New one is based on the old...
            final DetectorProperties dp = new DetectorProperties(det.getDetectorProperties());
            final String name = jTextField_Name.getText();
            if(name.length() > 1)
               dp.setName(name);
            final double elevation = jTextField_Elevation.getValue();
            final double azimuth = jTextField_Azimuth.getValue();
            final double optWd = jTextField_OptWD.getValue();
            final double distance = jTextField_DetDistance.getValue();
            final SpectrumProperties sp = dp.getProperties();
            sp.setDetectorPosition(Math.toRadians(elevation), Math.toRadians(azimuth), 1.0e-3 * distance, 1.0e-3 * optWd);
            final double zpd = jTextField_ZPD.getValue();
            if(zpd != 0.0)
               sp.setNumericProperty(SpectrumProperties.ZeroPeakDiscriminator, zpd);
            else
               sp.removeAll(Collections.singleton(SpectrumProperties.ZeroPeakDiscriminator));
            // Window paramters
            final IXRayWindowProperties newWind = XRayWindowFactory.createWindow((String) jComboBox_Window.getSelectedItem());
            assert newWind != null : "No window for " + (String) jComboBox_Window.getSelectedItem();
            sp.setWindow(newWind != null ? newWind : mOriginal.getWindow());
            // Crystal parameters
            sp.setNumericProperty(SpectrumProperties.DetectorArea, jTextField_Area.getValue());
            sp.setNumericProperty(SpectrumProperties.GoldLayer, jTextField_AuLayer.getValue());
            sp.setNumericProperty(SpectrumProperties.AluminumLayer, jTextField_AlLayer.getValue());
            sp.setNumericProperty(SpectrumProperties.NickelLayer, jTextField_NiLayer.getValue());
            sp.setNumericProperty(SpectrumProperties.DeadLayer, jTextField_DeadLayer.getValue());
            sp.setNumericProperty(SpectrumProperties.DetectorThickness, jTextField_Thickness.getValue());
            sp.setNumericProperty(SpectrumProperties.SolidAngle, SpectrumUtils.getSolidAngle(sp, Double.NaN));
            // Build the calibration
            EDSCalibration ec;
            if(isNewDetector()) {
               dp.setChannelCount(jTextField_NChannels.getValue());
               ec = new SDDCalibration(jTextField_Scale.getValue(), jTextField_Offset.getValue(), jTextField_Resolution.getValue());
               ec.makeBaseCalibration();
            } else
               ec = mOriginal.getCalibration();
            det = EDSDetector.updateDetector(dp, ec);
         }
         return det;
      }

      @Override
      public void commit() {
         super.commit();
         if(isModified() || isNewDetector()) {
            try {
               final EDSDetector mod = getModified();
               final DetectorProperties dp = mod.getDetectorProperties();
               if(isNewDetector()) {
                  // Add a new detector along with the initial calibration...
                  mSession.addDetector(dp);
                  mSession.addCalibration(dp, mod.getCalibration());
                  mNewDetector = false;
                  enableDetector(jCheckBox_Enable.isSelected());
               } else
                  // Update the previous detector to reflect the edited values
                  mSession.updateDetector(mOriginal.getDetectorProperties(), dp);
               mOriginal = mod;
            }
            catch(final EPQException e) {
               e.printStackTrace();
            }
            clearModified(this);
         }
      }

      @Override
      public EDSDetector getOriginal() {
         return mOriginal;
      }

   }

   private class MicrocalPanel
      extends
      DetectorPanel {

      private static final String AUTO_GENERATE = "Auto Generate";
      private static final String TITLE_BASE = "Detector - ";
      private EDSDetector mOriginal;
      private boolean mNewDetector;
      // Enable/disable detector in UI
      private final JCheckBox jCheckBox_Enable = new JCheckBox();
      // Name
      private final JTextField jTextField_Name = new JTextField();
      private final JTextField jTextField_GUID = new JTextField();
      // Import from spectrum
      private final JButton jButton_Import = new JButton("Import");
      // Location parameters
      private final JLabel jLabel_Geometry = new JLabel();
      private final JTextFieldDouble jTextField_Elevation = new JTextFieldDouble(40.0, -90.0, 90.0);
      private final JTextFieldDouble jTextField_Azimuth = new JTextFieldDouble(0.0, 0.0, 360.0);
      private final JTextFieldDouble jTextField_OptWD = new JTextFieldDouble(10.0, 0.0, 1000.0);
      private final JTextFieldDouble jTextField_DetDistance = new JTextFieldDouble(40.0, 0.0, 1000.0);
      // Crystal paramters
      private final JTextFieldDouble jTextField_Area = new JTextFieldDouble(0.01, 0.0000001, 1000.0);
      private final JTextFieldDouble jTextField_Thickness = new JTextFieldDouble(0.001, 0.000001, 100.0, "0.001");
      // Configuration parameters
      private final JTextFieldInt jTextField_NChannels = new JTextFieldInt(8096, 10, 65536);
      private final JTextFieldDouble jTextField_ZPD = new JTextFieldDouble(0.0, 0.0, 1000.0);
      // Performance parameters
      private final JTextFieldDouble jTextField_Scale = new JTextFieldDouble(1.0, 0.01, 100.0);
      private final JTextFieldDouble jTextField_Offset = new JTextFieldDouble(0.0, -10000.0, 10000.0);
      private final JTextFieldDouble jTextField_Resolution = new JTextFieldDouble(6.0, 0.1, 1000.0);
      // Window paramters
      private final JComboBox<String> jComboBox_Window = new JComboBox<String>();
      // Button to export detector
      private final JButton jButton_Export = new JButton("Export detector");

      private static final long serialVersionUID = 3471194954935625380L;

      MicrocalPanel(PreferenceDialog pref, EDSDetector det, boolean newDet)
            throws EPQException {
         super(det, pref, "Edit the properties of this detector.");
         mNewDetector = newDet;
         mOriginal = det;
         try {
            initialize();
         }
         catch(final Exception ex) {
            ex.printStackTrace();
         }
      }

      private void enableDetector(boolean enabled) {
         jTextField_Name.setEnabled(enabled);
         jTextField_GUID.setEnabled(isNewDetector() && enabled);
         jTextField_Elevation.setEnabled(enabled);
         jTextField_Azimuth.setEnabled(enabled);
         jTextField_OptWD.setEnabled(enabled);
         jTextField_DetDistance.setEnabled(enabled);
         jTextField_Area.setEnabled(enabled);
         jTextField_Thickness.setEnabled(enabled);
         jTextField_NChannels.setEnabled(isNewDetector() && enabled);
         jTextField_ZPD.setEnabled(enabled);
         jTextField_Scale.setEnabled(isNewDetector() && enabled);
         jTextField_Offset.setEnabled(isNewDetector() && enabled);
         jTextField_Resolution.setEnabled(isNewDetector() && enabled);
         jComboBox_Window.setEnabled(enabled);
         jButton_Import.setEnabled(isNewDetector());
         jButton_Export.setEnabled(true);
      }

      private void initialize() {
         setLayout(new FormLayout("pref", "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"));

         final CellConstraints cc = new CellConstraints();
         final String xFmt = "right:115dlu, 5dlu, 30dlu, 2dlu, 115dlu, 5dlu";

         final PanelBuilder disable = new PanelBuilder(new FormLayout(xFmt, "pref, 2dlu"));
         disable.addLabel("Enable detector", cc.xy(1, 1));
         disable.add(jCheckBox_Enable, cc.xy(3, 1));
         jCheckBox_Enable.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
               final boolean selected = jCheckBox_Enable.isSelected();
               enableDetector(selected);
               final DetectorProperties dp = mOriginal.getDetectorProperties();
               mSession.setEnabled(dp, selected);
               assert mSession.isEnabled(dp) == selected;
            }

         });
         final JPanel disPanel = disable.getPanel();
         disPanel.setBorder(DTSA2.createTitledBorder("Status"));
         add(disPanel, cc.xy(1, 1));

         final PanelBuilder name = new PanelBuilder(new FormLayout(xFmt, "pref, 2dlu, pref, 2dlu"));
         name.addLabel("Detector name", cc.xy(1, 1));
         name.add(jTextField_Name, cc.xyw(3, 1, 3));
         name.addLabel("Globally Unique Identifier", cc.xy(1, 3));
         name.add(jTextField_GUID, cc.xyw(3, 3, 3));
         jTextField_Name.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
               if(jTextField_Name.getText().length() > 0)
                  relabel(TITLE_BASE + jTextField_Name.getText());
            }
         });
         final JPanel namePanel = name.getPanel();
         namePanel.setBorder(DTSA2.createTitledBorder("Name"));
         add(namePanel, cc.xy(1, 3));

         final PanelBuilder imprt = new PanelBuilder(new FormLayout(xFmt, "pref, 2dlu, pref, 2dlu"));
         imprt.addLabel("Import from spectrum", cc.xy(1, 1));
         {
            final ButtonBarBuilder bbb = new ButtonBarBuilder();
            bbb.addButton(jButton_Import);
            bbb.addGlue();
            imprt.add(bbb.build(), cc.xyw(3, 1, 3));
         }
         imprt.addLabel("<html>Often vendor's spectrum files will contain some of the data items in this panel. Use import to populate the panel with the vendor's values. Use care. Vendor's values are sometimes wrong.</html>", cc.xyw(3, 3, 3));
         jButton_Import.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
               final ISpectrumData[] specs = DTSA2.getInstance().getFrame().selectSpectraFromFiles();
               if(specs != null) {
                  int chCount = 0;
                  SpectrumProperties props = null;
                  for(final ISpectrumData spec : specs) {
                     props = SpectrumProperties.merge(props, spec.getProperties());
                     if(chCount == 0)
                        chCount = spec.getChannelCount();
                  }
                  for(final Component c : MicrocalPanel.this.getComponents())
                     if(c instanceof JTextField)
                        ((JTextField) c).setBackground(SystemColor.text);
                  final Color updatedColor = Color.yellow;
                  if(props.isDefined(SpectrumProperties.DetectorDescription)) {
                     jTextField_Name.setText(props.getTextWithDefault(SpectrumProperties.DetectorDescription, "Detector"));
                     jTextField_Name.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.DetectorGUID)) {
                     jTextField_GUID.setText(props.getTextWithDefault(SpectrumProperties.DetectorGUID, AUTO_GENERATE));
                     jTextField_GUID.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.Elevation)) {
                     jTextField_Elevation.setValue(props.getNumericWithDefault(SpectrumProperties.Elevation, 35.0));
                     jTextField_Elevation.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.Azimuth)) {
                     jTextField_Azimuth.setValue(props.getNumericWithDefault(SpectrumProperties.Azimuth, 0.0));
                     jTextField_Azimuth.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.WorkingDistance)) {
                     jTextField_OptWD.setValue(props.getNumericWithDefault(SpectrumProperties.WorkingDistance, 20.0));
                     jTextField_OptWD.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.DetectorPosition)
                        && props.isDefined(SpectrumProperties.WorkingDistance))
                     try {
                        jTextField_DetDistance.setValue(SpectrumUtils.getDetectorDistance(props));
                        jTextField_DetDistance.setBackground(updatedColor);
                     }
                     catch(final EPQException e) {
                        // Just ignore it...
                     }
                  if(props.isDefined(SpectrumProperties.DetectorArea)) {
                     jTextField_Area.setValue(props.getNumericWithDefault(SpectrumProperties.DetectorArea, 10.0));
                     jTextField_Area.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.ActiveLayer)) {
                     final double thickness = props.getNumericWithDefault(SpectrumProperties.ActiveLayer, 5000.0) / 1000.0;
                     jTextField_Thickness.setValue(thickness);
                     jTextField_Thickness.setBackground(updatedColor);
                  }
                  if(chCount > 0) {
                     jTextField_NChannels.setValue(chCount);
                     jTextField_NChannels.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.ZeroPeakDiscriminator)) {
                     jTextField_ZPD.setValue(props.getNumericWithDefault(SpectrumProperties.ZeroPeakDiscriminator, 0));
                     jTextField_ZPD.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.EnergyScale)) {
                     jTextField_Scale.setValue(props.getNumericWithDefault(SpectrumProperties.EnergyScale, 10.0));
                     jTextField_Scale.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.EnergyOffset)) {
                     jTextField_Offset.setValue(props.getNumericWithDefault(SpectrumProperties.EnergyOffset, 0.0));
                     jTextField_Offset.setBackground(updatedColor);
                  }
                  if(props.isDefined(SpectrumProperties.Resolution)) {
                     jTextField_Resolution.setValue(SpectrumUtils.getFWHMAtMnKA(props, 130.0));
                     jTextField_Resolution.setBackground(updatedColor);
                  }
                  getPreferenceDialog().setMessage("Imported properties have been highlighted.");
               }
            }
         });
         final JPanel importPanel = imprt.getPanel();
         importPanel.setBorder(DTSA2.createTitledBorder("Import"));
         add(importPanel, cc.xy(1, 5));

         final PanelBuilder window = new PanelBuilder(new FormLayout(xFmt, "pref, 2dlu"));
         window.addLabel("Window", cc.xy(1, 1));
         window.add(jComboBox_Window, cc.xyw(3, 1, 3));
         jComboBox_Window.setEditable(false);
         final DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<String>();
         for(final String s : XRayWindowFactory.WindowTypes)
            cbm.addElement(s);
         // cbm.addElement("User defined window");
         cbm.setSelectedItem(mOriginal.getWindow().getName());
         jComboBox_Window.setModel(cbm);
         final JPanel windowPanel = window.getPanel();
         windowPanel.setBorder(DTSA2.createTitledBorder("Window"));
         add(windowPanel, cc.xy(1, 7));

         final PanelBuilder location = new PanelBuilder(new FormLayout(xFmt, "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 2dlu, pref, 2dlu"));
         jLabel_Geometry.setIcon(new ImageIcon(MainFrame.class.getResource("Detector geometry.PNG")));
         location.add(jLabel_Geometry, cc.xyw(1, 1, 5));
         location.addLabel("Elevation angle", cc.xy(1, 3));
         location.add(jTextField_Elevation, cc.xy(3, 3));
         location.addLabel(SpectrumProperties.Elevation.getUnits(), cc.xy(5, 3));
         location.addLabel("Azimuthal angle", cc.xy(1, 5));
         location.add(jTextField_Azimuth, cc.xy(3, 5));
         location.addLabel(SpectrumProperties.Azimuth.getUnits(), cc.xy(5, 5));
         location.addLabel("Optimal working distance", cc.xy(1, 7));
         location.add(jTextField_OptWD, cc.xy(3, 7));
         location.addLabel(SpectrumProperties.DetectorOptWD.getUnits(), cc.xy(5, 7));
         location.addLabel("Sample-to-detector distance", cc.xy(1, 9));
         location.add(jTextField_DetDistance, cc.xy(3, 9));
         location.addLabel(SpectrumProperties.DetectorDistance.getUnits(), cc.xy(5, 9));
         final JPanel locationPanel = location.getPanel();
         locationPanel.setBorder(DTSA2.createTitledBorder("Position"));
         add(locationPanel, cc.xy(1, 9));

         final PanelBuilder crystal = new PanelBuilder(new FormLayout(xFmt, "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 2dlu"));
         crystal.addLabel("Detector Area", cc.xy(1, 1));
         crystal.add(jTextField_Area, cc.xy(3, 1));
         crystal.addLabel(SpectrumProperties.DetectorArea.getUnits(), cc.xy(5, 1));
         // 3, 5, 7
         crystal.addLabel("Thickness", cc.xy(1, 3));
         crystal.add(jTextField_Thickness, cc.xy(3, 3));
         crystal.addLabel(SpectrumProperties.DetectorThickness.getUnits(), cc.xy(5, 3));
         final JPanel crystalPanel = crystal.getPanel();
         crystalPanel.setBorder(DTSA2.createTitledBorder("Crystal parameters"));
         add(crystalPanel, cc.xy(1, 11));

         final PanelBuilder config = new PanelBuilder(new FormLayout(xFmt, "pref, 5dlu, pref, 2dlu"));
         config.addLabel("Number of channels", cc.xy(1, 1));
         config.add(jTextField_NChannels, cc.xy(3, 1));
         jTextField_NChannels.setToolTipText("<html>This property is not editable for established detectors.");
         config.addLabel("channels", cc.xy(5, 1));
         config.addLabel("Zero strobe discriminator", cc.xy(1, 3));
         config.add(jTextField_ZPD, cc.xy(3, 3));
         config.addLabel("eV", cc.xy(5, 3));
         final JPanel configPanel = config.getPanel();
         configPanel.setBorder(DTSA2.createTitledBorder("Configuration"));
         add(configPanel, cc.xy(1, 13));

         final PanelBuilder performance = new PanelBuilder(new FormLayout(xFmt, "pref, 5dlu, pref, 5dlu, pref, 2dlu"));
         performance.addLabel("Energy scale", cc.xy(1, 1));
         performance.add(jTextField_Scale, cc.xy(3, 1));
         performance.addLabel(SpectrumProperties.EnergyScale.getUnits(), cc.xy(5, 1));
         performance.addLabel("Zero offset", cc.xy(1, 3));
         performance.add(jTextField_Offset, cc.xy(3, 3));
         performance.addLabel(SpectrumProperties.EnergyOffset.getUnits(), cc.xy(5, 3));
         performance.addLabel("Resolution", cc.xy(1, 5));
         performance.add(jTextField_Resolution, cc.xy(3, 5));
         performance.addLabel(SpectrumProperties.Resolution.getUnits() + " FWHM at Mn K\u03B1", cc.xy(5, 5));
         final JPanel perfPanel = performance.getPanel();
         perfPanel.setToolTipText("<html>These properties are not editable for established detectors.<br/>Use the calibration alien to refine them.");
         perfPanel.setBorder(DTSA2.createTitledBorder("Base Performance"));
         add(perfPanel, cc.xy(1, 15));

         {
            final ButtonBarBuilder bbb = new ButtonBarBuilder();
            bbb.addGlue();
            jButton_Export.setToolTipText("Export the detector definition as XML to facilitate sharing this information with colleagues.");
            bbb.addButton(jButton_Export);
            add(bbb.build(), cc.xy(1, 17));
         }
         jButton_Export.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
               final JFileChooser jfc = new JFileChooser();
               jfc.setMultiSelectionEnabled(false);
               jfc.setAcceptAllFileFilterUsed(true);
               final SimpleFileFilter sff = new SimpleFileFilter(new String[] {
                  "xdet",
               }, "XML detector file (*.xdet)");
               jfc.addChoosableFileFilter(sff);
               jfc.setAcceptAllFileFilterUsed(true);
               jfc.setFileFilter(sff);
               jfc.setDialogTitle("Save to XML detector file");
               try {
                  final EDSDetector det = getModified();
                  final File file = new File(det.getName() + ".xdet");
                  jfc.setSelectedFile(file);
                  final int option = jfc.showSaveDialog(MicrocalPanel.this);
                  if(option == JFileChooser.APPROVE_OPTION)
                     det.writeXML(jfc.getSelectedFile());
               }
               catch(final Exception e) {
                  ErrorDialog.createErrorMessage(MicrocalPanel.this.getPreferenceDialog(), "Error saving detector", e);
               }
            }
         });

         // Update values
         jCheckBox_Enable.setSelected(isNewDetector() || mSession.isEnabled(mOriginal.getDetectorProperties()));
         enableDetector(jCheckBox_Enable.isSelected());
         jTextField_Name.setText(mOriginal.getName());
         relabel(TITLE_BASE + mOriginal.getName());
         {
            final SpectrumProperties dp = mOriginal.getDetectorProperties().getProperties();
            jTextField_GUID.setText(dp.getTextWithDefault(SpectrumProperties.DetectorGUID, AUTO_GENERATE));
            // Location parameters
            jTextField_Elevation.setValue(dp.getNumericWithDefault(SpectrumProperties.Elevation, 40.0));
            jTextField_Azimuth.setValue(dp.getNumericWithDefault(SpectrumProperties.Azimuth, 0.0));
            jTextField_OptWD.setValue(dp.getNumericWithDefault(SpectrumProperties.DetectorOptWD, 15.0));
            jTextField_DetDistance.setValue(dp.getNumericWithDefault(SpectrumProperties.DetectorDistance, 40.0));
            // Crystal paramters
            jTextField_Area.setValue(dp.getNumericWithDefault(SpectrumProperties.DetectorArea, 10.0));
            jTextField_Thickness.setValue(dp.getNumericWithDefault(SpectrumProperties.DetectorThickness, 5.0));
            jTextField_NChannels.setValue(mOriginal.getChannelCount());
            jTextField_ZPD.setValue(dp.getNumericWithDefault(SpectrumProperties.ZeroPeakDiscriminator, 0.0));
         }
         {
            final EDSCalibration cp = mOriginal.getCalibration();
            // Configuration parameters
            // Performance parameters
            jTextField_Scale.setValue(cp.getChannelWidth());
            jTextField_Offset.setValue(cp.getZeroOffset());
            jTextField_Resolution.setValue(cp.getLineshape().getFWHMatMnKa());
         }
      }

      private boolean isModified(Component c) {
         if(c instanceof JPanel) {
            final JPanel p = (JPanel) c;
            for(final Component child : p.getComponents())
               if(isModified(child))
                  return true;
         }
         if(c instanceof JTextFieldDouble)
            if(((JTextFieldDouble) c).isModified())
               return true;
         if(c instanceof JTextFieldInt)
            if(((JTextFieldInt) c).isModified())
               return true;
         return false;
      }

      private void clearModified(Component c) {
         if(c instanceof JPanel) {
            final JPanel p = (JPanel) c;
            for(final Component child : p.getComponents())
               clearModified(child);
         }
         if(c instanceof JTextFieldDouble)
            ((JTextFieldDouble) c).setModified(false);
         if(c instanceof JTextFieldInt)
            ((JTextFieldInt) c).setModified(false);
      }

      @Override
      public boolean isModified() {
         if(!jTextField_Name.getText().equals(mOriginal.getName()))
            return true;
         if(jTextField_GUID.getText().equals(AUTO_GENERATE))
            return true;
         if(!((String) jComboBox_Window.getSelectedItem()).equals(mOriginal.getWindow().getName()))
            return true;
         return isModified(this);
      }

      public boolean isNewDetector() {
         return mNewDetector;
      }

      @Override
      public EDSDetector getModified()
            throws EPQException {
         EDSDetector det = mOriginal;
         if(isModified() || isNewDetector()) {
            // New one is based on the old...
            final DetectorProperties dp = new DetectorProperties(det.getDetectorProperties());
            final String name = jTextField_Name.getText();
            if(name.length() > 1)
               dp.setName(name);
            final double elevation = jTextField_Elevation.getValue();
            final double azimuth = jTextField_Azimuth.getValue();
            final double optWd = jTextField_OptWD.getValue();
            final double distance = jTextField_DetDistance.getValue();
            final SpectrumProperties sp = dp.getProperties();
            sp.setDetectorPosition(Math.toRadians(elevation), Math.toRadians(azimuth), 1.0e-3 * distance, 1.0e-3 * optWd);
            final double zpd = jTextField_ZPD.getValue();
            if(zpd != 0.0)
               sp.setNumericProperty(SpectrumProperties.ZeroPeakDiscriminator, zpd);
            else
               sp.removeAll(Collections.singleton(SpectrumProperties.ZeroPeakDiscriminator));
            // Window paramters
            final IXRayWindowProperties newWind = XRayWindowFactory.createWindow((String) jComboBox_Window.getSelectedItem());
            assert newWind != null : "No window for " + (String) jComboBox_Window.getSelectedItem();
            sp.setWindow(newWind != null ? newWind : mOriginal.getWindow());
            // Crystal parameters
            sp.setNumericProperty(SpectrumProperties.DetectorArea, jTextField_Area.getValue());
            sp.setNumericProperty(SpectrumProperties.DetectorThickness, jTextField_Thickness.getValue());
            sp.setNumericProperty(SpectrumProperties.SolidAngle, SpectrumUtils.getSolidAngle(sp, Double.NaN));
            // Build the calibration
            EDSCalibration ec;
            if(isNewDetector()) {
               dp.setChannelCount(jTextField_NChannels.getValue());
               ec = new MicrocalCalibration(jTextField_Scale.getValue(), jTextField_Offset.getValue(), jTextField_Resolution.getValue());
               ec.makeBaseCalibration();
            } else
               ec = mOriginal.getCalibration();
            det = EDSDetector.updateDetector(dp, ec);
         }
         return det;
      }

      @Override
      public void commit() {
         super.commit();
         if(isModified() || isNewDetector()) {
            try {
               final EDSDetector mod = getModified();
               final DetectorProperties dp = mod.getDetectorProperties();
               if(isNewDetector()) {
                  // Add a new detector along with the initial calibration...
                  mSession.addDetector(dp);
                  mSession.addCalibration(dp, mod.getCalibration());
                  mNewDetector = false;
                  enableDetector(jCheckBox_Enable.isSelected());
               } else
                  // Update the previous detector to reflect the edited values
                  mSession.updateDetector(mOriginal.getDetectorProperties(), dp);
               mOriginal = mod;
            }
            catch(final EPQException e) {
               e.printStackTrace();
            }
            clearModified(this);
         }
      }

      @Override
      public EDSDetector getOriginal() {
         return mOriginal;
      }
   }

   private class Dialog
      extends
      PreferenceDialog {

      private static final long serialVersionUID = -2554799577753596480L;

      public Dialog(Frame owner) {
         super(owner, DTSA2.APP_NAME + " -  Preferences", true);
         addPanel(new BasicPreferences(this));
         addPanel(new QuantPreferences(this));
         addPanel(new AddInstrument(this));
      }
   };

   static public void editPreferences(Frame owner) {
      final AppPreferences tp = getInstance();
      final Dialog dlg = tp.new Dialog(owner);
      dlg.setLocationRelativeTo(owner);
      dlg.setVisible(true);

   }

   static public AppPreferences getInstance() {
      return mInstance;
   }

   private AppPreferences() {
      final Preferences userPref = Preferences.userNodeForPackage(getClass());
      mUserName = userPref.get("UserName", System.getProperty("user.name"));
      mStartupScript = userPref.get("StartupScript", "");
      mShutdownScript = userPref.get("ShutdownScript", "");
      mEPQJavaDoc = userPref.get("EPQJavaDoc", EPQ_JAVA_DOC_DEFAULT);
      mCorrectionAlgorithm = userPref.get("CorrectionAlgorithm", CorrectionAlgorithm.XPPExtended.getName());
      mMACAlgorithm = userPref.get("MACAlgorithm", MassAbsorptionCoefficient.Default.getName());
      mBremAngular = userPref.get("Bremsstrahlung angular distribution", BremsstrahlungAngularDistribution.Acosta2002L.getName());
      mIonizationXSec = userPref.get("Ionization cross section", AbsoluteIonizationCrossSection.BoteSalvat2008.getName());
      mSession = DTSA2.getSession();
      mBaseReportPath = HTMLReport.getBasePath();
      updateStrategy();
   }

   /**
    * Gets the current value assigned to correctionAlgorithm
    * 
    * @return Returns the correctionAlgorithm.
    */
   public String getCorrectionAlgorithm() {
      return mCorrectionAlgorithm;
   }

   /**
    * Sets the value assigned to correctionAlgorithm.
    * 
    * @param correctionAlgorithm The value to which to set correctionAlgorithm.
    */
   public void setCorrectionAlgorithm(String correctionAlgorithm) {
      if(!mCorrectionAlgorithm.equals(correctionAlgorithm)) {
         mCorrectionAlgorithm = correctionAlgorithm;
         final Preferences userPref = Preferences.userNodeForPackage(getClass());
         userPref.put("CorrectionAlgorithm", mCorrectionAlgorithm);
         updateStrategy();
      }
   }

   public String getIonizationCrossSection() {
      return mIonizationXSec;
   }

   public void setIonizationCrossSection(String alg) {
      if(!mIonizationXSec.equals(alg)) {
         mIonizationXSec = alg;
         final Preferences userPref = Preferences.userNodeForPackage(getClass());
         userPref.put("Ionization cross section", mIonizationXSec);
         updateStrategy();
      }
   }

   public String getBremsstrahlungAngularDistribution() {
      return mBremAngular;
   }

   public void setBremsstrahlungAngularDistribution(String alg) {
      if(!mBremAngular.equals(alg)) {
         mBremAngular = alg;
         final Preferences userPref = Preferences.userNodeForPackage(getClass());
         userPref.put("Bremsstrahlung angular distribution", mBremAngular);
         updateStrategy();
      }
   }

   /**
    * Gets the current value assigned to mACAlgorithm
    * 
    * @return Returns the mACAlgorithm.
    */
   public String getMACAlgorithm() {
      return mMACAlgorithm;
   }

   /**
    * Sets the value assigned to mACAlgorithm.
    * 
    * @param algorithm The value to which to set mACAlgorithm.
    */
   public void setMACAlgorithm(String algorithm) {
      if(!mMACAlgorithm.equals(algorithm)) {
         mMACAlgorithm = algorithm;
         final Preferences userPref = Preferences.userNodeForPackage(getClass());
         userPref.put("MACAlgorithm", mMACAlgorithm);
         updateStrategy();
      }
   }

   /**
    * Gets the current value assigned to userName
    * 
    * @return Returns the userName.
    */
   public String getUserName() {
      return mUserName;
   }

   public String getStartupScript() {
      return mStartupScript;
   }

   /**
    * Sets the value assigned to startup script path
    * 
    * @param path
    */
   public void setStartupScript(String path) {
      if(!mStartupScript.equals(path)) {
         mStartupScript = path;
         final Preferences userPref = Preferences.userNodeForPackage(getClass());
         userPref.put("StartupScript", mStartupScript);
      }
   }

   public String getShutdownScript() {
      return mShutdownScript;
   }

   /**
    * Sets the value assigned to shutdown script path.
    * 
    * @param path
    */
   public void setShutdownScript(String path) {
      if(!mShutdownScript.equals(path)) {
         mShutdownScript = path;
         final Preferences userPref = Preferences.userNodeForPackage(getClass());
         userPref.put("ShutdownScript", mShutdownScript);
      }
   }

   public void setEPQJavaDoc(String path) {
      if(!mEPQJavaDoc.equals(path)) {
         mEPQJavaDoc = path;
         final Preferences userPref = Preferences.userNodeForPackage(getClass());
         userPref.put("EPQJavaDoc", mEPQJavaDoc);
      }
   }

   public String getEPQJavaDoc() {
      return mEPQJavaDoc;
   }

   public void openEPQJavaDoc() {
      try {
         openURL(getEPQJavaDoc());
      }
      catch(final Exception e1) {
         try {
            openURL(EPQ_JAVA_DOC_DEFAULT);
         }
         catch(final Exception e2) {
            ErrorDialog.createErrorMessage(DTSA2.getInstance().getFrame(), "Open EPQ library documentation", e2);
         }
      }

   }

   private void openURL(String path) {
      try {
         if(path.startsWith("http://")) {
            final URL url = new URL(path);
            Desktop.getDesktop().browse(url.toURI());
         } else {
            final File f = new File(path, "index.html");
            Desktop.getDesktop().browse(f.toURI());
         }
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }

   /**
    * Sets the value assigned to userName.
    * 
    * @param userName The value to which to set userName.
    */
   public void setUserName(String userName) {
      if(!mUserName.equals(userName)) {
         mUserName = userName;
         final Preferences userPref = Preferences.userNodeForPackage(getClass());
         userPref.put("UserName", mUserName);
      }
   }

   /**
    * Gets the current value assigned to the base report path
    * 
    * @return String
    */
   public String getBaseReportPath() {
      return mBaseReportPath;
   }

   /**
    * Sets the value assigned to the base report path.
    * 
    * @param baseReportPath
    */
   public void setBaseReportPath(String baseReportPath) {
      if(!mBaseReportPath.equals(baseReportPath))
         HTMLReport.setBasePath(baseReportPath);
   }

   public AlgorithmClass lookUp(List<AlgorithmClass> lac, String name) {
      for(final AlgorithmClass ac : lac)
         if(ac.getName().equals(name))
            return ac;
      return null;
   }

   private void updateStrategy() {
      final Strategy strat = new Strategy();
      {
         final AlgorithmClass ac = lookUp(CorrectionAlgorithm.NullCorrection.getAllImplementations(), mCorrectionAlgorithm);
         if(ac != null)
            strat.addAlgorithm(CorrectionAlgorithm.class, ac);
      }
      {
         final AlgorithmClass ac = lookUp(MassAbsorptionCoefficient.Null.getAllImplementations(), mMACAlgorithm);
         if(ac != null)
            strat.addAlgorithm(MassAbsorptionCoefficient.class, ac);
      }
      {
         final AlgorithmClass ac = lookUp(AbsoluteIonizationCrossSection.BoteSalvat2008.getAllImplementations(), mIonizationXSec);
         if(ac != null)
            strat.addAlgorithm(AbsoluteIonizationCrossSection.class, ac);
      }
      {
         final AlgorithmClass ac = lookUp(BremsstrahlungAngularDistribution.Acosta2002.getAllImplementations(), mBremAngular);
         if(ac != null)
            strat.addAlgorithm(BremsstrahlungAngularDistribution.class, ac);
      }
      AlgorithmUser.applyGlobalOverride(strat);
   }

   public DetectorProperties getDefaultDetector() {
      try {
         final Preferences userPref = Preferences.userNodeForPackage(getClass());
         final String det = userPref.get(DEFAULT_DETECTOR_KEY, null);
         if(det != null)
            for(final DetectorProperties dp : DTSA2.getSession().getDetectors())
               if(dp.toString().equals(det))
                  return dp;
      }
      catch(final Exception e) {
         e.printStackTrace();
      }
      return null;
   }

   public void setDefaultDetector(DetectorProperties det) {
      final Preferences userPref = Preferences.userNodeForPackage(getClass());
      if(det != null)
         userPref.put(DEFAULT_DETECTOR_KEY, det.toString());
      else
         userPref.remove(DEFAULT_DETECTOR_KEY);
   }

}
