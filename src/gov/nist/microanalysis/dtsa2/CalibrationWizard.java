package gov.nist.microanalysis.dtsa2;

import java.awt.Color;
import java.awt.Frame;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.toedter.calendar.JDateChooser;

import gov.nist.microanalysis.EPQDatabase.Session;
import gov.nist.microanalysis.EPQLibrary.AlgorithmUser;
import gov.nist.microanalysis.EPQLibrary.AtomicShell;
import gov.nist.microanalysis.EPQLibrary.Composition;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.Element;
import gov.nist.microanalysis.EPQLibrary.ISpectrumData;
import gov.nist.microanalysis.EPQLibrary.RegionOfInterestSet;
import gov.nist.microanalysis.EPQLibrary.SpectrumFitResult;
import gov.nist.microanalysis.EPQLibrary.SpectrumFitter8;
import gov.nist.microanalysis.EPQLibrary.SpectrumFitter8.AltEnergyScaleFunction;
import gov.nist.microanalysis.EPQLibrary.SpectrumFitter8.EnergyScaleFunction;
import gov.nist.microanalysis.EPQLibrary.SpectrumFitter8.FanoNoiseWidth;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;
import gov.nist.microanalysis.EPQLibrary.SpectrumUtils;
import gov.nist.microanalysis.EPQLibrary.Strategy;
import gov.nist.microanalysis.EPQLibrary.TransitionEnergy;
import gov.nist.microanalysis.EPQLibrary.XRayTransition;
import gov.nist.microanalysis.EPQLibrary.Detector.DetectorCalibration;
import gov.nist.microanalysis.EPQLibrary.Detector.DetectorProperties;
import gov.nist.microanalysis.EPQLibrary.Detector.EDSCalibration;
import gov.nist.microanalysis.EPQLibrary.Detector.EDSDetector;
import gov.nist.microanalysis.EPQLibrary.Detector.ElectronProbe;
import gov.nist.microanalysis.EPQLibrary.Detector.SDDCalibration;
import gov.nist.microanalysis.EPQLibrary.Detector.SiLiCalibration;
import gov.nist.microanalysis.EPQTools.ErrorDialog;
import gov.nist.microanalysis.EPQTools.JWizardDialog;
import gov.nist.microanalysis.EPQTools.MaterialsCreator;
import gov.nist.microanalysis.EPQTools.SpectrumFileChooser;
import gov.nist.microanalysis.EPQTools.SpectrumPropertyPanel;
import gov.nist.microanalysis.Utility.HalfUpFormat;
import gov.nist.microanalysis.Utility.UtilException;

/**
 * <p>
 * A wizard-style dialog for calibrating EDS detectors
 * </p>
 * <p>
 * Copyright: Pursuant to title 17 Section 105 of the United States Code this
 * software is not subject to copyright protection and is in the public domain
 * </p>
 * <p>
 * Institution: National Institute of Standards and Technology
 * </p>
 * 
 * @author nicholas
 * @version 1.0
 */
public class CalibrationWizard
   extends
   JWizardDialog {

   private static final long serialVersionUID = 3517408203501633898L;

   private final ModePanel jWizardPanel_Mode = new ModePanel(this);
   private final DetectorPanel jWizardPanel_Detector = new DetectorPanel(this);
   private final ManualPanel jWizardPanel_Manual = new ManualPanel(this);
   private final FitPanel jWizardPanel_FitSpectrum = new FitPanel(this);
   private final JWizardPanel jWizardPanel_BAM_CRM = new JWizardPanel(this);
   private final FitResultPanel jWizardPanel_FitResult = new FitResultPanel(this);
   private final FitProgressPanel jProgressPanel_Progress = new FitProgressPanel(this);
   private final LimitsPanel jWizardPanel_Limits = new LimitsPanel(this);

   private static final String[] STANDARDS = new String[] {
      "Mn standard",
      "Cu standard",
      "Zn standard",
      "Fe standard",
      "BAM CRM standard",
      "K3189",
      "BAM EDS-TM002"
   };

   private Session getSession() {
      return DTSA2.getSession();

   }

   public class ModePanel
      extends
      JWizardPanel {

      private static final long serialVersionUID = 7994619405938718356L;

      private final JRadioButton jRadioButton_FitSpectrum = new JRadioButton("Calibrate using an elemental reference");
      private final JRadioButton jRadioButton_Manual = new JRadioButton("Manually enter detector calibration");
      private final JRadioButton jRadioButton_BAM_CRM = new JRadioButton("Calibrate using the BAM EDS CRM");
      private final JRadioButton jRadioButton_Administer = new JRadioButton("Review and administer available calibrations.");
      private final JRadioButton jRadioButton_Limits = new JRadioButton("Specify which element's lines are visible");

      public ModePanel(JWizardDialog wiz) {
         super(wiz);
         initialize();
      }

      private void initialize() {
         final PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu, pref", "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"), this);
         final CellConstraints cc = new CellConstraints();
         jRadioButton_Manual.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               getWizard().setNextPanel(jWizardPanel_Manual, "Manual calibration");
            }
         });
         jRadioButton_FitSpectrum.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               getWizard().setNextPanel(jWizardPanel_FitSpectrum, "Measured spectrum");
            }
         });
         jRadioButton_BAM_CRM.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               getWizard().setNextPanel(jWizardPanel_BAM_CRM, "BAM CRM");
            }
         });
         jRadioButton_Limits.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               getWizard().setNextPanel(jWizardPanel_Limits, "Set element limits");
            }
         });

         jRadioButton_BAM_CRM.setEnabled(false);
         jRadioButton_Administer.setEnabled(false);
         jRadioButton_Manual.setToolTipText("<HTML>Calibrate using nominal parameters entered by hand.");
         jRadioButton_BAM_CRM.setToolTipText("<HTML>Calibrate using the EDS reference material created by<br>the BAM (Bundesanstalt f&uuml;r Materialforschung und -pr&uuml;fung)");
         jRadioButton_FitSpectrum.setToolTipText("<HTML>Calibrate against a Mn or Cu pure elemental reference spectrum.");
         jRadioButton_Administer.setToolTipText("<HTML>Enable and disable instrument calibrations.");
         jRadioButton_Limits.setToolTipText("<HTML>Specify the minimum Z for each characteristic line family.");
         int row = 1;
         pb.addSeparator("Specify a calibration method", cc.xyw(1, row, 2));
         pb.add(jRadioButton_FitSpectrum, cc.xy(2, row += 2));
         pb.add(jRadioButton_Manual, cc.xy(2, row += 2));
         pb.add(jRadioButton_Limits, cc.xy(2, row += 2));
         pb.add(jRadioButton_BAM_CRM, cc.xy(2, row += 2));
         pb.addSeparator("Administer calibrations", cc.xyw(1, row += 2, 2));
         pb.add(jRadioButton_Administer, cc.xy(2, row += 2));
         final ButtonGroup bg = new ButtonGroup();
         bg.add(jRadioButton_FitSpectrum);
         bg.add(jRadioButton_Manual);
         bg.add(jRadioButton_BAM_CRM);
         bg.add(jRadioButton_Limits);
         bg.add(jRadioButton_Administer);
         jRadioButton_FitSpectrum.setSelected(true);
      }

      @Override
      public void onShow() {
         if(jWizardPanel_Detector.mDetector != null)
            jRadioButton_Limits.setEnabled(jWizardPanel_Detector.mDetector.getCalibration() instanceof SiLiCalibration);
         getWizard().setMessageText("Select a spectrum calibration method");
         getWizard().setNextPanel(jWizardPanel_FitSpectrum, "Measured spectrum");
         getWizard().enableFinish(false);
      }

      @Override
      public boolean permitNext() {
         if(jRadioButton_Limits.isSelected()) {
            final Object obj = jWizardPanel_Detector.jComboBox_Detector.getSelectedItem();
            if(obj instanceof EDSDetector) {
               final EDSDetector det = (EDSDetector) obj;
               mResultCalibration = det.getCalibration();
            }
         }
         return true;
      }

      @Override
      public void onHide() {

      }
   }

   public class DetectorPanel
      extends
      JWizardPanel {

      private static final long serialVersionUID = 2996757631404932405L;

      private final JComboBox<ElectronProbe> jComboBox_Instrument = new JComboBox<ElectronProbe>();
      private final JComboBox<EDSDetector> jComboBox_Detector = new JComboBox<EDSDetector>();

      private EDSDetector mDetector = null;
      private DetectorProperties mDefaultDetector = null;

      public DetectorPanel(JWizardDialog wiz) {
         super(wiz);
         initialize();
      }

      private void initialize() {
         final PanelBuilder pb = new PanelBuilder(new FormLayout("right:100dlu, 5dlu, 200dlu", "pref, 10dlu, pref, 5dlu, pref"), this);
         final CellConstraints cc = new CellConstraints();
         pb.addSeparator("Instrument and Detector", cc.xyw(1, 1, 3));
         pb.addLabel("Instrument", cc.xy(1, 3));
         pb.add(jComboBox_Instrument, cc.xy(3, 3));
         final DefaultComboBoxModel<ElectronProbe> dcbm = new DefaultComboBoxModel<ElectronProbe>(getSession().getCurrentProbes().toArray(new ElectronProbe[0]));
         jComboBox_Instrument.setModel(dcbm);
         jComboBox_Instrument.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               updateDetectors();
            }
         });
         pb.addLabel("Detector", cc.xy(1, 5));
         pb.add(jComboBox_Detector, cc.xy(3, 5));
         updateDetectors();
      }

      private void updateDetectors() {
         final DetectorProperties dp = mDefaultDetector;
         mDefaultDetector = null;
         if(dp != null)
            jComboBox_Instrument.setSelectedItem(dp.getOwner());
         final Object obj = jComboBox_Instrument.getSelectedItem();
         if(obj instanceof ElectronProbe) {
            final EDSDetector[] dets = getSession().getCurrentEDSDetectors((ElectronProbe) obj).toArray(new EDSDetector[0]);
            final DefaultComboBoxModel<EDSDetector> dcbm = new DefaultComboBoxModel<EDSDetector>(dets);
            jComboBox_Detector.setModel(dcbm);
            if(dp != null)
               for(final EDSDetector det : dets)
                  if(det.getDetectorProperties() == dp) {
                     jComboBox_Detector.setSelectedItem(det);
                     break;
                  }
         }
      }

      private void setDetector(DetectorProperties dp) {
         mDefaultDetector = dp;
         updateDetectors();
      }

      @Override
      public void onShow() {
         getWizard().setMessageText("Select a detector to calibrate");
         getWizard().setNextPanel(jWizardPanel_Mode, "Calibration method");
         getWizard().enableFinish(false);
      }

      @Override
      public boolean permitNext() {
         mDetector = null;
         if(jComboBox_Detector.getSelectedItem() instanceof EDSDetector) {
            final EDSDetector dp = (EDSDetector) jComboBox_Detector.getSelectedItem();
            mDetector = dp;
         }
         if(mDetector == null)
            setMessageText("Please specify an x-ray detector.");
         return (mDetector != null);
      }

      @Override
      public void onHide() {

      }
   }

   public class FitPanel
      extends
      JWizardPanel {

      private JComboBox<Object> jComboBox_Material;
      private JTextField jTextField_Spectrum;
      private JButton jButton_SelectSpectrum;
      private JComboBox<String> jComboBox_FitOrder;
      private JDateChooser jDateChoose_Effective;
      private JTextField jTextField_LiveTime;
      private JTextField jTextField_ProbeCurrent;

      private ISpectrumData mReference;
      private int mFitOrderSelected = 0;

      private static final long serialVersionUID = -5082875641613694408L;

      public SpectrumProperties editSpectrumProperties(SpectrumProperties sp, Session ses) {
         final SpectrumPropertyPanel.PropertyDialog dlg = new SpectrumPropertyPanel.PropertyDialog(CalibrationWizard.this, ses);
         final SpectrumProperties.PropertyId[] required = new SpectrumProperties.PropertyId[] {
            SpectrumProperties.BeamEnergy,
            SpectrumProperties.FaradayBegin,
            SpectrumProperties.FaradayEnd,
            SpectrumProperties.LiveTime
         };
         dlg.setRequiredProperties(Arrays.asList(required));
         dlg.addSpectrumProperties(sp);
         dlg.setLocationRelativeTo(this);
         dlg.setVisible(true);
         return dlg.getSpectrumProperties();
      }

      public FitPanel(JWizardDialog wiz) {
         super(wiz);
         initialize();
      }

      private void initialize() {
         final Session ses = getSession();
         final Vector<Object> standards = new Vector<Object>();
         for(final String std : STANDARDS)
            try {
               final Composition c = ses.findStandard(std);
               if(c != null)
                  standards.add(c);
            }
            catch(final SQLException e) {
               // Ignore it...
            }
         standards.add("New material");
         jComboBox_Material = new JComboBox<Object>(standards);
         jComboBox_Material.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               Object obj = jComboBox_Material.getSelectedItem();
               if(!(obj instanceof Composition)) {
                  obj = MaterialsCreator.createMaterial(CalibrationWizard.this, DTSA2.getSession(), false);
                  if(obj instanceof Composition) {
                     jComboBox_Material.addItem(obj);
                     jComboBox_Material.setSelectedItem(obj);
                  } else
                     jComboBox_Material.setSelectedIndex(0);
               }
            }
         });
         jTextField_Spectrum = new JTextField();
         jTextField_Spectrum.setEditable(false);
         {
            jComboBox_FitOrder = new JComboBox<String>();
            final String def = "Linear (default)";
            final ComboBoxModel<String> model = new DefaultComboBoxModel<String>(new String[] {
               def,
               "Quadratic",
               "Cubic",
               "Quartic",
               "Quintic",
               "Square root"
            });
            model.setSelectedItem(def);
            jComboBox_FitOrder.setModel(model);
         }
         jButton_SelectSpectrum = new JButton("Select");
         jButton_SelectSpectrum.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               final SpectrumFileChooser sfc = new SpectrumFileChooser(CalibrationWizard.this, "Select a reference spectrum");
               sfc.setMultiSelectionEnabled(false);
               final File dir = new File(DTSA2.getSpectrumDirectory());
               sfc.getFileChooser().setCurrentDirectory(dir);
               sfc.setLocationRelativeTo(CalibrationWizard.this);
               final int res = sfc.showOpenDialog();
               if(res == JFileChooser.APPROVE_OPTION) {
                  final ISpectrumData ref = sfc.getSpectra()[0];
                  double e0 = ref.getProperties().getNumericWithDefault(SpectrumProperties.BeamEnergy, Double.NaN) * 1.0e3;
                  while(Double.isNaN(e0)) {
                     ref.getProperties().addAll(editSpectrumProperties(ref.getProperties(), getSession()));
                     e0 = ref.getProperties().getNumericWithDefault(SpectrumProperties.BeamEnergy, Double.NaN) * 1.0e3;
                  }
                  final boolean ok = (e0 >= 1.5e4)
                        || (JOptionPane.showConfirmDialog(CalibrationWizard.this, "<html>The beam energy on this spectrum is less than the suggested 15 keV.<br><br>Use it none the less?", "Calibration", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
                  if(ok) {
                     mReference = SpectrumUtils.applyEDSDetector(jWizardPanel_Detector.mDetector, ref);
                     final Composition stdComp = mReference.getProperties().getCompositionWithDefault(SpectrumProperties.StandardComposition, null);
                     if(stdComp != null) {
                        jComboBox_Material.addItem(stdComp);
                        jComboBox_Material.setSelectedItem(stdComp);
                     }
                     jTextField_Spectrum.setText(mReference.toString());
                     final Date dt = mReference.getProperties().getTimestampWithDefault(SpectrumProperties.AcquisitionTime, new Date());
                     jDateChoose_Effective.setDate(dt);
                     {
                        final NumberFormat nf = new HalfUpFormat("0.000");
                        jTextField_ProbeCurrent.setText(nf.format(SpectrumUtils.getAverageFaradayCurrent(mReference.getProperties(), 1.0)));
                     }
                     {
                        final NumberFormat nf = new HalfUpFormat("0.00");
                        final double lt = mReference.getProperties().getNumericWithDefault(SpectrumProperties.LiveTime, 60.0);
                        jTextField_LiveTime.setText(nf.format(lt));
                     }
                     final HalfUpFormat nf = new HalfUpFormat("0.0");
                     setMessageText("The reference beam energy is " + nf.format(e0 / 1000.0) + " keV.");
                     setNextPanel(jProgressPanel_Progress, "Fit Results");
                  } else {
                     setErrorText("Please select a spectrum collected at greater then 15 keV.");
                     setNextPanel(null, "Finish");
                  }
                  DTSA2.updateSpectrumDirectory(sfc.getFileChooser().getCurrentDirectory());
               }
            }
         });
         jDateChoose_Effective = new JDateChooser();
         jTextField_LiveTime = new JTextField();
         jTextField_ProbeCurrent = new JTextField();
         final PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu, right:50dlu, 3dlu, fill:150dlu, 3dlu, 20dlu", "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"), this);
         final CellConstraints cc = new CellConstraints();
         pb.addSeparator("Specify the material", cc.xyw(1, 5, 6));
         pb.addLabel("Material", cc.xy(2, 7));
         pb.add(jComboBox_Material, cc.xyw(4, 7, 3));

         pb.addSeparator("Specify a spectrum", cc.xyw(1, 1, 6));
         pb.addLabel("Spectrum", cc.xy(2, 3));
         pb.add(jTextField_Spectrum, cc.xy(4, 3));
         pb.add(jButton_SelectSpectrum, cc.xy(6, 3));
         {
            final PanelBuilder param = new PanelBuilder(new FormLayout("right:50dlu, 3dlu, 30dlu, 3dlu, 17dlu, right:67dlu, 3dlu, 30dlu, 3dlu, 14dlu", "pref, 5dlu, pref"));
            param.addLabel("Live time", cc.xy(1, 1));
            param.add(jTextField_LiveTime, cc.xy(3, 1));
            param.addLabel("sec.", cc.xy(5, 1));
            param.addLabel("Probe current", cc.xy(6, 1));
            param.add(jTextField_ProbeCurrent, cc.xy(8, 1));
            param.addLabel("nA", cc.xy(10, 1));
            param.addLabel("Fit type", cc.xy(1, 3));
            param.add(jComboBox_FitOrder, cc.xyw(3, 3, 7));
            pb.add(param.getPanel(), cc.xyw(2, 9, 5));
         }
         pb.addSeparator("Specify an effective date", cc.xyw(1, 11, 6));
         pb.addLabel("Effective date", cc.xy(2, 13));
         pb.add(jDateChoose_Effective, cc.xyw(4, 13, 3));
      }

      @Override
      public boolean permitNext() {
         final boolean res = (mReference != null) && (jComboBox_Material.getSelectedItem() instanceof Composition);
         if(res) {
            final SpectrumProperties sp = mReference.getProperties();
            sp.setCompositionProperty(SpectrumProperties.StandardComposition, (Composition) jComboBox_Material.getSelectedItem());
            jProgressPanel_Progress.setFitWorker(new FitWorker(mReference));
            mFitOrderSelected = jComboBox_FitOrder.getSelectedIndex();
            mEffectiveDate = jDateChoose_Effective.getDate();
         } else
            setMessageText("Please specify a material and a reference spectrum.");
         return res;
      }

   }

   public class FitWorker
      extends
      SwingWorker<SpectrumFitter8, String> {

      final private ISpectrumData mSpectrum;
      private ISpectrumData mFitSpectrum;
      private final int mIterations = System.getProperty("user.name").equals("nritchie") ? 5 : 2;

      FitWorker(ISpectrumData spec) {
         mSpectrum = spec;
      }

      @Override
      public void done() {
         try {
            jProgressPanel_Progress.setProgress(100);
            jWizardPanel_FitResult.setFit(get());
         }
         catch(final Exception e) {
            ErrorDialog.createErrorMessage(CalibrationWizard.this, "Calibration alien", e);
         }
      }

      /**
       * @return SpectrumFitter8
       * @throws Exception
       * @see javax.swing.SwingWorker#doInBackground()
       */
      @Override
      protected SpectrumFitter8 doInBackground()
            throws Exception {
         final Strategy strat = new Strategy();
         strat.addAlgorithm(TransitionEnergy.class, TransitionEnergy.Default);
         AlgorithmUser.applyGlobalOverride(strat);
         try {
            final SpectrumProperties sp = mSpectrum.getProperties();
            final SpectrumFitter8 sf = new SpectrumFitter8((EDSDetector) sp.getDetector(), sp.getCompositionProperty(SpectrumProperties.StandardComposition), mSpectrum);
            final RegionOfInterestSet rois = sf.getROIS();
            final SpectrumProperties props = sp.getDetector().getCalibration().getProperties();
            // If there is an extended range of energies with characteristic
            // peaks, we should increase the number of fit parameters.
            final double[] coeffs = new double[] {
               props.getNumericWithDefault(SpectrumProperties.EnergyOffset, 0.0),
               props.getNumericWithDefault(SpectrumProperties.EnergyScale, 10.0)
            };
            if(jWizardPanel_FitSpectrum.mFitOrderSelected < 5)
               // 0->Linear, 1->Quadratic, 2->Cubic, 3->Quartic, 4->Quintic,
               // 5->Square root
               sf.setEnergyScale(new EnergyScaleFunction(coeffs, jWizardPanel_FitSpectrum.mFitOrderSelected + 2));
            else
               sf.setEnergyScale(new AltEnergyScaleFunction(coeffs));
            sf.setResolution(new FanoNoiseWidth(6.0));
            sf.setMultiLineset(sf.buildWeighted(rois));
            sf.setActionListener(new ActionListener() {

               int mPrev = 0;
               int mIteration = 0;

               @Override
               public void actionPerformed(ActionEvent e) {
                  // We go through twice...
                  if((mPrev == 100) && (e.getID() < 100))
                     ++mIteration;
                  jProgressPanel_Progress.setProgress((e.getID() / mIterations) + ((100 * mIteration) / mIterations));
                  mPrev = e.getID();
               }
            });
            FileWriter mrFitsFile = null;
            try {
               if(System.getProperty("user.name").equals("nritchie")) {
                  final File elmFits = new File(HTMLReport.getBasePath(), "mostRecentFit.csv");
                  mrFitsFile = new FileWriter(elmFits, elmFits.exists());
               }
               SpectrumFitResult results = sf.compute();
               if(mrFitsFile != null)
                  mrFitsFile.write(results.tabulateResults());
               for(int i = 1; i < mIterations; ++i) {
                  results = sf.recompute(10.0, 0.3);
                  if(mrFitsFile != null)
                     try {
                        mrFitsFile.write(results.tabulateResults());
                        mrFitsFile.flush();
                     }
                     catch(final Exception e1) {
                        e1.printStackTrace();
                     }
               }
            }
            finally {
               if(mrFitsFile != null)
                  mrFitsFile.close();
            }
            mFitSpectrum = sf.getBestFit();
            final DataManager dm = DataManager.getInstance();
            dm.removeSpectrum(mSpectrum);
            dm.addSpectrum(mSpectrum, true);
            dm.addSpectrum(mFitSpectrum, true, mSpectrum);
            dm.addSpectrum(sf.getCharacteristicSpectrum(), true, mSpectrum);
            dm.addSpectrum(sf.getBremsstrahlungSpectrum(), true, mSpectrum);
            jProgressPanel_Progress.setProgress(100);
            return sf;
         }
         finally {
            AlgorithmUser.clearGlobalOverride();
         }

      }
   };

   public class FitProgressPanel
      extends
      JProgressPanel {

      private static final long serialVersionUID = 1207417763604043955L;

      private FitWorker mWorker;

      /**
       * Constructs a FitProgressPanel
       * 
       * @param wiz
       */
      public FitProgressPanel(JWizardDialog wiz) {
         super(wiz);
      }

      @Override
      public void onShow() {
         mFit = null;
         mWorker.execute();
         setProgress(0);
         enableFinish(false);
      }

      @Override
      public void setProgress(int pct) {
         super.setProgress(pct);
         if(pct == 100)
            setNextPanel(jWizardPanel_FitResult, "Fit results");
      }

      public void setFitWorker(FitWorker fw) {
         mWorker = fw;
      }

      public FitWorker getFitWorker() {
         return mWorker;
      }
   }

   public class FitResultPanel
      extends
      JWizardPanel {

      private static final long serialVersionUID = -3139178312592947721L;

      private final JTextField jTextField_Fano = new JTextField();
      private final JTextField jTextField_Noise = new JTextField();
      private final JTextField jTextField_FWHM = new JTextField();
      private final JTextField jTextField_ZeroOffset = new JTextField();
      private final JTextField jTextField_ChannelWidth = new JTextField();
      private final JCheckBox jCheckBox_AddToDB = new JCheckBox("Add to database", true);
      private final JCheckBox jCheckBox_OutputIndividualElements = new JCheckBox("Output elemental fits", false);

      /**
       * Constructs a FitResultPanel
       * 
       * @param wiz
       */
      public FitResultPanel(JWizardDialog wiz) {
         super(wiz);
         initialize();
      }

      private void initialize() {
         final PanelBuilder pb = new PanelBuilder(new FormLayout("right:50dlu, 5dlu, 100dlu, 5dlu, pref", "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"), this);
         final CellConstraints cc = new CellConstraints();
         pb.addSeparator("Energy calibration", cc.xyw(1, 1, 5));
         pb.addLabel("Fano factor", cc.xy(1, 3));
         pb.add(jTextField_Fano, cc.xy(3, 3));
         pb.addLabel("Noise", cc.xy(1, 5));
         pb.add(jTextField_Noise, cc.xy(3, 5));
         pb.addLabel("eV", cc.xy(5, 5));
         pb.addLabel("FWHM", cc.xy(1, 7));
         pb.add(jTextField_FWHM, cc.xy(3, 7));
         pb.addLabel("eV", cc.xy(5, 7));
         pb.addSeparator("Resolution calibration", cc.xyw(1, 9, 5));
         pb.addLabel("Zero offset", cc.xy(1, 11));
         pb.addLabel("eV", cc.xy(5, 11));
         pb.add(jTextField_ZeroOffset, cc.xy(3, 11));
         pb.addLabel("eV/channel", cc.xy(5, 13));
         pb.addLabel("Channel width", cc.xy(1, 13));
         pb.add(jTextField_ChannelWidth, cc.xy(3, 13));
         pb.add(jCheckBox_AddToDB, cc.xy(3, 15));
         jCheckBox_AddToDB.addActionListener(new AbstractAction() {

            private static final long serialVersionUID = 5869352272112226829L;

            @Override
            public void actionPerformed(ActionEvent arg0) {
               updateNextPanel();
            }
         });

         pb.add(jCheckBox_OutputIndividualElements, cc.xy(5, 15));
      }

      private void updateNextPanel() {
         if(jCheckBox_AddToDB.isSelected()) {
            CalibrationWizard.this.setNextPanel(jWizardPanel_Limits, "Specify element limits");
            enableFinish(false);
         } else {
            CalibrationWizard.this.setNextPanel(null, "Finish");
            enableFinish(true);
         }
      }

      public void setFit(SpectrumFitter8 sf)
            throws EPQException, UtilException {
         mFit = sf;
         final NumberFormat nf2 = new HalfUpFormat("0.00");
         final NumberFormat nf4 = new HalfUpFormat("0.0000");
         final SpectrumFitResult results = mFit.getLastResult();
         jTextField_Fano.setText(results.getFanoFactor().format(nf4));
         jTextField_Noise.setText(results.getNoise().format(nf2));
         jTextField_FWHM.setText(results.getFWHMatMnKa().format(nf2));
         jTextField_ZeroOffset.setText(results.getZeroOffset().format(nf2));
         jTextField_ChannelWidth.setText(results.getChannelWidth().format(nf4));
         mHTMLResult = results.toHTML();
         jCheckBox_AddToDB.setEnabled(jWizardPanel_FitSpectrum.mFitOrderSelected == 0);
         jCheckBox_AddToDB.setSelected(jWizardPanel_FitSpectrum.mFitOrderSelected == 0);
         final File elmFits = new File(HTMLReport.getBasePath(), "elementFits.csv");
         try (final FileWriter fw = new FileWriter(elmFits, elmFits.exists())) {
            fw.write(results.tabulateResults());
            fw.flush();
         }
         catch(final IOException e) {
            e.printStackTrace();
         }
      }

      @Override
      public void onShow() {
         updateNextPanel();
      }

      @Override
      public boolean permitNext() {
         assert jWizardPanel_Detector.mDetector != null;
         boolean result = true;
         if(mFit != null) {
            if(jCheckBox_AddToDB.isSelected()) {
               try {
                  mResultCalibration = mFit.getLastResult().getCalibration();
               }
               catch(final Exception e) {
                  e.printStackTrace();
                  result = false;
               }
            } else {
               mResultCalibration = null;
            }
            if(jWizardPanel_FitResult.jCheckBox_OutputIndividualElements.isSelected()) {
               final DataManager dm = DataManager.getInstance();
               for(final Element elm : mFit.getElements())
                  try {
                     dm.addSpectrum(mFit.getElementSpectrum(elm), true, mFit.getSpectrum());
                  }
                  catch(final Exception e) {
                     e.printStackTrace();
                  }
            }
         }
         return result;
      }

   }

   public class ManualPanel
      extends
      JWizardPanel {

      private static final long serialVersionUID = -1890526644480564803L;

      JTextField jTextField_FWHM = new JTextField();
      JTextField jTextField_ChannelWidth = new JTextField();
      JTextField jTextField_Offset = new JTextField();
      JDateChooser jDateChoose_Date = new JDateChooser();
      private final NumberFormat mFormat = new HalfUpFormat("0.0");

      private double mFWHM = 132.0;
      private double mChannelWidth = 10.0;
      private double mZeroOffset = 0.0;
      private final Date mStartDate = new Date();

      public ManualPanel(JWizardDialog wiz) {
         super(wiz);
         initialize();
      }

      private void initialize() {
         final PanelBuilder pb = new PanelBuilder(new FormLayout("right:75dlu, 5dlu, 100dlu, 3dlu, pref", "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"), this);
         final CellConstraints cc = new CellConstraints();
         pb.addSeparator("Detector resolution", cc.xyw(1, 1, 5));
         pb.addLabel("FWHM @ " + (new XRayTransition(Element.Mn, XRayTransition.KA1)).toString(), cc.xy(1, 3));
         pb.add(jTextField_FWHM, cc.xy(3, 3));
         jTextField_FWHM.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
               mFWHM = parseDouble(jTextField_FWHM, 1.0, 1000.0, mFWHM, "0.0");
            }
         });
         pb.addLabel("eV", cc.xy(5, 3));
         pb.addSeparator("Energy calibration", cc.xyw(1, 5, 5));
         pb.addLabel("Channel width", cc.xy(1, 7));
         pb.add(jTextField_ChannelWidth, cc.xy(3, 7));
         jTextField_ChannelWidth.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
               mChannelWidth = parseDouble(jTextField_ChannelWidth, 0.1, 100.0, mChannelWidth, "0.0000");
            }
         });
         pb.addLabel("eV/channel", cc.xy(5, 7));
         pb.addLabel("Zero offset", cc.xy(1, 9));
         pb.add(jTextField_Offset, cc.xy(3, 9));
         jTextField_Offset.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
               mZeroOffset = parseDouble(jTextField_Offset, -5000.0, 5000.0, mZeroOffset, "0.0");
            }
         });
         pb.addLabel("eV", cc.xy(5, 9));
         pb.addSeparator("Calibration takes effect", cc.xyw(1, 11, 5));
         pb.addLabel("Start date", cc.xy(1, 13));
         jDateChoose_Date.setToolTipText("The calibration takes effect at 12:00:01 AM of the specified date.");
         jDateChoose_Date.setDate(mStartDate);
         pb.add(jDateChoose_Date, cc.xy(3, 13));
         // Get last calibration as default...
         final Object obj = jWizardPanel_Detector.jComboBox_Detector.getSelectedItem();
         if(obj instanceof DetectorProperties) {
            final DetectorProperties dp = (DetectorProperties) obj;
            final DetectorCalibration dc = getSession().getMostRecentCalibration(dp);
            if(dc instanceof EDSCalibration) {
               final EDSCalibration ec = (EDSCalibration) dc;
               mFWHM = ec.getLineshape().getFWHMatMnKa();
               mChannelWidth = ec.getChannelWidth();
               mZeroOffset = ec.getZeroOffset();
            }
         }
      }

      @Override
      public void onShow() {
         getWizard().setMessageText("Enter the calibration data");
         getWizard().setNextPanel(jWizardPanel_Limits, "Specify element limits");
         getWizard().enableFinish(true);
         final Object obj = jWizardPanel_Detector.jComboBox_Detector.getSelectedItem();
         if(obj instanceof EDSDetector) {
            final EDSDetector det = (EDSDetector) obj;
            final EDSCalibration ec = det.getCalibration();
            mChannelWidth = ec.getChannelWidth();
            mZeroOffset = ec.getZeroOffset();
            mFWHM = ec.getLineshape().getFWHMatMnKa();
         }
         NumberFormat nf = new HalfUpFormat("0.0000");
         jTextField_ChannelWidth.setText(nf.format(mChannelWidth));
         jTextField_FWHM.setText(mFormat.format(mFWHM));
         jTextField_Offset.setText(mFormat.format(mZeroOffset));
      }

      private double parseDouble(JTextField dbl, double min, double max, double def, String fmt) {
         double res = def;
         final NumberFormat nf = new HalfUpFormat(fmt);
         try {
            CalibrationWizard.this.setErrorText("");
            dbl.setBackground(SystemColor.text);
            res = mFormat.parse(dbl.getText()).doubleValue();
            if(res < min)
               res = def;
            if(res > max)
               res = def;
            dbl.setText(nf.format(res));
         }
         catch(final ParseException e) {
            dbl.setBackground(Color.pink);
            CalibrationWizard.this.setErrorText("Invalid entry");
            dbl.setText(nf.format(res));
         }
         return res;
      }

      @Override
      public boolean permitNext() {
         mResultCalibration = new SDDCalibration(mChannelWidth, mZeroOffset, mFWHM);
         {
            final StringBuffer sb = new StringBuffer();
            sb.append("<H3>Manual Calibration: <i>");
            sb.append(jWizardPanel_Detector.jComboBox_Detector.getSelectedItem().toString());
            sb.append("</i></H3>\n");
            sb.append("<TABLE><TR><TH>Property</TH><TH>Value</TH></TR>\n");
            sb.append("<TR><TH>Channel width</TH><TD>");
            NumberFormat nf = new HalfUpFormat("0.0000", true);
            sb.append(nf.format(mChannelWidth));
            sb.append(" eV/ch</TD></TR>\n");
            sb.append("<TR><TH>Zero offset</TH><TD>");
            nf = new HalfUpFormat("0.0", true);
            sb.append(nf.format(mZeroOffset));
            sb.append(" eV</TD></TR>\n");
            sb.append("<TR><TH>FWHM at Mn K\u03B1</TH><TD>");
            sb.append(nf.format(mFWHM));
            sb.append(" eV</TD></TR>\n");
            sb.append("<TR><TH>Effective</TH><TD>");
            sb.append(DateFormat.getDateInstance().format(mResultCalibration.getActiveDate()));
            sb.append("</TD></TR></TABLE>\n");
            mHTMLResult = sb.toString();
         }
         return true;
      }
   }

   private class LimitsPanel
      extends
      JWizardPanel {

      private final JComboBox<Element> jComboBox_KLine = new JComboBox<Element>();
      private final JComboBox<Element> jComboBox_LLine = new JComboBox<Element>();
      private final JComboBox<Element> jComboBox_MLine = new JComboBox<Element>();
      private final JComboBox<Element> jComboBox_NLine = new JComboBox<Element>();

      private static final long serialVersionUID = -8281440631161048625L;

      public LimitsPanel(JWizardDialog wiz) {
         super(wiz);
         init();
      }

      private void init() {
         final FormLayout fl = new FormLayout("right:pref, 5dlu, 100dlu", "pref, 10dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref");
         final PanelBuilder pb = new PanelBuilder(fl, this);
         final CellConstraints cc0 = new CellConstraints(), cc1 = new CellConstraints();
         pb.addSeparator("Specify minimum visible element", cc0.xyw(1, 1, 3));
         pb.add(new JLabel("K family of lines"), cc0.xy(1, 3), jComboBox_KLine, cc1.xy(3, 3));
         pb.add(new JLabel("L family of lines"), cc0.xy(1, 5), jComboBox_LLine, cc1.xy(3, 5));
         pb.add(new JLabel("M family of lines"), cc0.xy(1, 7), jComboBox_MLine, cc1.xy(3, 7));
         pb.add(new JLabel("N family of lines"), cc0.xy(1, 9), jComboBox_NLine, cc1.xy(3, 9));
      }

      @Override
      public void onShow() {
         if(mResultCalibration instanceof SiLiCalibration) {
            final EDSCalibration cal = mResultCalibration;
            final SiLiCalibration sili = (SiLiCalibration) cal;
            {
               final DefaultComboBoxModel<Element> dcbm = new DefaultComboBoxModel<Element>(Element.range(Element.Li, Element.Si));
               dcbm.setSelectedItem(sili.getFirstVisible(AtomicShell.KFamily));
               jComboBox_KLine.setModel(dcbm);
            }
            {
               final DefaultComboBoxModel<Element> dcbm = new DefaultComboBoxModel<Element>(Element.range(Element.S, Element.Mo));
               dcbm.setSelectedItem(sili.getFirstVisible(AtomicShell.LFamily));
               jComboBox_LLine.setModel(dcbm);
            }
            {
               final DefaultComboBoxModel<Element> dcbm = new DefaultComboBoxModel<Element>(Element.range(Element.Br, Element.Uub));
               dcbm.setSelectedItem(sili.getFirstVisible(AtomicShell.MFamily));
               jComboBox_MLine.setModel(dcbm);
            }
            {
               final DefaultComboBoxModel<Element> dcbm = new DefaultComboBoxModel<Element>(Element.range(Element.Pb, Element.Uub));
               dcbm.setSelectedItem(sili.getFirstVisible(AtomicShell.NFamily));
               jComboBox_NLine.setModel(dcbm);
            }

            jComboBox_KLine.setEnabled(true);
            jComboBox_LLine.setEnabled(true);
            jComboBox_MLine.setEnabled(true);
            jComboBox_NLine.setEnabled(true);
         } else {
            jComboBox_KLine.setEnabled(false);
            jComboBox_LLine.setEnabled(false);
            jComboBox_MLine.setEnabled(false);
            jComboBox_NLine.setEnabled(false);
         }
         getWizard().setMessageText("Enter the visible line data");
         getWizard().setNextPanel(null, "Finish");
         getWizard().enableFinish(true);
      }

      @Override
      public void onHide() {
         if(isFinished()) {
            final EDSCalibration cal = mResultCalibration;
            StringBuffer sb = new StringBuffer();
            if(cal instanceof SiLiCalibration) {
               final SiLiCalibration sili = (SiLiCalibration) cal.clone();
               sili.setFirstVisible(AtomicShell.KFamily, (Element) jComboBox_KLine.getSelectedItem());
               sili.setFirstVisible(AtomicShell.LFamily, (Element) jComboBox_LLine.getSelectedItem());
               sili.setFirstVisible(AtomicShell.MFamily, (Element) jComboBox_MLine.getSelectedItem());
               sili.setFirstVisible(AtomicShell.NFamily, (Element) jComboBox_NLine.getSelectedItem());
               sb.append("<p><table><tr><th>Family</th><th>First Visible Element</th></tr>");
               sb.append("<tr><td>K-family</td><td>" + sili.getFirstVisible(AtomicShell.KFamily) + "</td></tr>");
               sb.append("<tr><td>L-family</td><td>" + sili.getFirstVisible(AtomicShell.LFamily) + "</td></tr>");
               sb.append("<tr><td>M-family</td><td>" + sili.getFirstVisible(AtomicShell.MFamily) + "</td></tr>");
               sb.append("</table></p>");
               final DetectorProperties dp = jWizardPanel_Detector.mDetector.getDetectorProperties();
               final Calendar c = Calendar.getInstance();
               c.setTime(mEffectiveDate);
               c.set(Calendar.HOUR, 0);
               c.set(Calendar.MINUTE, 0);
               c.set(Calendar.SECOND, 1);
               c.set(Calendar.MILLISECOND, 0);
               Session sess = DTSA2.getSession();
               // Increment each calibration on any given day by one hour...
               for(DetectorCalibration calib : sess.getCalibrations(dp)) {
                  final Calendar c2 = Calendar.getInstance();
                  c2.setTime(calib.getActiveDate());
                  if((c.get(Calendar.YEAR) == c2.get(Calendar.YEAR))
                        && (c.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR))) {
                     if(c.get(Calendar.HOUR) <= c2.get(Calendar.HOUR))
                        c.set(Calendar.HOUR, c2.get(Calendar.HOUR) + 1);
                  }
               }
               sili.setActiveDate(c.getTime());
               sess.addCalibration(dp, sili);
               sb.append("<p><b>Calibration name:</b> " + sili.toString() + "</p>");
               mHTMLResult = mHTMLResult + sb.toString();
            }
         }
      }
   };

   private SpectrumFitter8 mFit;
   private String mHTMLResult;
   private EDSCalibration mResultCalibration;
   private Date mEffectiveDate = new Date();

   public CalibrationWizard(Frame owner) {
      super(owner, "Calibrate an EDS detector", true);
      initialize();
   }

   public void setDetector(DetectorProperties det) {
      jWizardPanel_Detector.setDetector(det);
   }

   private void initialize() {
      setActivePanel(jWizardPanel_Detector, "Select a detector");
      pack();
   }

   public String toHTML() {
      return mHTMLResult != null ? mHTMLResult : "";
   }
}
