package gov.nist.microanalysis.dtsa2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.FormLayout;
import com.toedter.calendar.JDateChooser;

import gov.nist.microanalysis.EPQDatabase.Session;
import gov.nist.microanalysis.EPQDatabase.Session.QCNormalizeMode;
import gov.nist.microanalysis.EPQDatabase.Session.QCProject;
import gov.nist.microanalysis.EPQLibrary.Composition;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.Element;
import gov.nist.microanalysis.EPQLibrary.ISpectrumData;
import gov.nist.microanalysis.EPQLibrary.MaterialFactory;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;
import gov.nist.microanalysis.EPQLibrary.SpectrumUtils;
import gov.nist.microanalysis.EPQLibrary.Detector.DetectorCalibration;
import gov.nist.microanalysis.EPQLibrary.Detector.DetectorProperties;
import gov.nist.microanalysis.EPQLibrary.Detector.EDSCalibration;
import gov.nist.microanalysis.EPQLibrary.Detector.EDSDetector;
import gov.nist.microanalysis.EPQTools.ErrorDialog;
import gov.nist.microanalysis.EPQTools.JWizardDialog;
import gov.nist.microanalysis.EPQTools.MaterialsCreator;
import gov.nist.microanalysis.EPQTools.SimpleFileFilter;
import gov.nist.microanalysis.EPQTools.SpectrumFileChooser;
import gov.nist.microanalysis.EPQTools.SpectrumPropertyPanel;
import gov.nist.microanalysis.Utility.DescriptiveStatistics;
import gov.nist.microanalysis.Utility.HalfUpFormat;
import gov.nist.microanalysis.Utility.UncertainValue2;

/**
 * @author nicholas
 */
public class QCWizard
   extends JWizardDialog {

   private static final long serialVersionUID = -6929660016246884181L;

   private final WelcomePanel jWizardPanel_Welcome = new WelcomePanel(this);
   private final NewProjectPanel jWizardPanel_NewProject = new NewProjectPanel(this);
   private final SelectProjectPanel jWizardPanel_SelectProject = new SelectProjectPanel(this);
   private final MeasurementPanel jWizardPanel_Measurement = new MeasurementPanel(this);
   private final ResultPanel jWizardPanel_Results = new ResultPanel(this);
   private final ExportPanel jWizardPanel_Export = new ExportPanel(this);
   private final ReportPanel jWizardPanel_Report = new ReportPanel(this);

   private final Session mSession;

   private Mode mMode = Mode.MEASUREMENT;
   private transient QCProject mProject = null;
   private transient DetectorProperties mDetectorProperties = null;
   private ISpectrumData mSpectrum = null;

   private static enum Mode {
      NEW_PROJECT, MEASUREMENT, REPORT, SPECTRA, LIMITS, EXPORT
   }

   private final StringBuffer mHTMLResult = new StringBuffer();

   private class WelcomePanel
      extends JWizardPanel {

      private static final long serialVersionUID = 7454038715660815765L;

      private final JRadioButton jRadioButton_NewProject = new JRadioButton("Create new QC project.");
      private final JRadioButton jRadioButton_Measurement = new JRadioButton("Add a QC measurement to a project");
      private final JRadioButton jRadioButton_Report = new JRadioButton("Generate a detailed QC report");
      private final JRadioButton jRadioButton_Spectra = new JRadioButton("Select historical spectra to reopen");
      private final JRadioButton jRadioButton_Limits = new JRadioButton("Specify control limits");
      private final JRadioButton jRadioButton_Export = new JRadioButton("Export the data from a QC project");

      public WelcomePanel(JWizardDialog wiz) {
         super(wiz, "Welcome");
         initialize();
      }

      private void initialize() {
         final FormLayout fl = new FormLayout("20dlu, pref, 20dlu", "5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu");
         final PanelBuilder pb = new PanelBuilder(fl);
         final ButtonGroup bg = new ButtonGroup();
         bg.add(jRadioButton_NewProject);
         bg.add(jRadioButton_Measurement);
         bg.add(jRadioButton_Report);
         bg.add(jRadioButton_Spectra);
         bg.add(jRadioButton_Limits);
         bg.add(jRadioButton_Export);
         final ActionListener welcomeListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               selectAction();
            }
         };
         pb.add(jRadioButton_NewProject, CC.xy(2, 2));
         jRadioButton_NewProject.addActionListener(welcomeListener);
         pb.add(jRadioButton_Measurement, CC.xy(2, 4));
         jRadioButton_Measurement.addActionListener(welcomeListener);
         pb.add(jRadioButton_Report, CC.xy(2, 6));
         jRadioButton_Report.addActionListener(welcomeListener);
         pb.add(jRadioButton_Spectra, CC.xy(2, 8));
         jRadioButton_Spectra.addActionListener(welcomeListener);
         pb.add(jRadioButton_Limits, CC.xy(2, 10));
         jRadioButton_Limits.addActionListener(welcomeListener);
         pb.add(jRadioButton_Export, CC.xy(2, 12));
         jRadioButton_Export.addActionListener(welcomeListener);
         bg.setSelected(jRadioButton_Measurement.getModel(), true);
         mMode = Mode.MEASUREMENT;
         jRadioButton_Spectra.setEnabled(false);
         jRadioButton_Limits.setEnabled(false);
         final JPanel pbPanel = pb.getPanel();
         pbPanel.setBorder(DTSA2.createTitledBorder("Select a QC task"));
         add(pbPanel);
      }

      private void selectAction() {
         if(jRadioButton_Export.isSelected())
            mMode = Mode.EXPORT;
         else if(jRadioButton_Limits.isSelected())
            mMode = Mode.LIMITS;
         else if(jRadioButton_NewProject.isSelected())
            mMode = Mode.NEW_PROJECT;
         else if(jRadioButton_Measurement.isSelected())
            mMode = Mode.MEASUREMENT;
         else if(jRadioButton_Report.isSelected())
            mMode = Mode.REPORT;
         else if(jRadioButton_Spectra.isSelected())
            mMode = Mode.SPECTRA;
         if(mMode == Mode.NEW_PROJECT)
            setNextPanel(jWizardPanel_NewProject);
         else
            setNextPanel(jWizardPanel_SelectProject);
      }

      @Override
      public void onShow() {
         selectAction();
         QCWizard.this.enableNext(true);
         QCWizard.this.enableFinish(false);
      }
   }

   private class NewProjectPanel
      extends JWizardPanel {

      private static final long serialVersionUID = 7315979072518760442L;

      private final JComboBox<DetectorProperties> jComboBox_Detector = new JComboBox<>();
      private final JTextField jTextField_Material = new JTextField();
      private final JTextField jTextField_BeamEnergy = new JTextField();
      private final JTextField jTextField_NominalI = new JTextField();
      private final JTextField jTextField_NominalWD = new JTextField();
      private final JButton jButton_EditMaterial = new JButton("Edit");
      private final JCheckBox jCheckBox_Current = new JCheckBox("Use probe current normalization (recommended)");

      private DetectorProperties mDetector = null;
      private Composition mMaterial = MaterialFactory.createPureElement2(Element.Cu);
      private double mBeamEnergy = 20.0;
      private double mNominalWD = Double.NaN;
      private double mNominalI = 1.0;
      private QCNormalizeMode mCurrentMode = QCNormalizeMode.CURRENT;
      private boolean mCreateQCProject = false;

      public NewProjectPanel(JWizardDialog wiz) {
         super(wiz, "Configure a new QCproject");
         init();
      }

      private void init() {
         final FormLayout fl = new FormLayout("20dlu, right:pref, 5dlu, 50dlu, 3dlu, pref, 30dlu, 20dlu", "5dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 5dlu");
         final PanelBuilder pb = new PanelBuilder(fl);
         jButton_EditMaterial.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               final Composition res = MaterialsCreator.editMaterial(QCWizard.this, mMaterial, mSession, "Specify a material", false);
               if(res != null)
                  mMaterial = res;
               jTextField_Material.setText(mMaterial.toString());
            }
         });
         pb.add(new JLabel("EDS detector"), CC.xy(2, 2), jComboBox_Detector, CC.xyw(4, 2, 3));

         pb.add(new JLabel("Material"), CC.xy(2, 4), jTextField_Material, CC.xy(4, 4));
         pb.add(jButton_EditMaterial, CC.xy(6, 4));
         pb.add(new JLabel("Beam energy"), CC.xy(2, 6), jTextField_BeamEnergy, CC.xy(4, 6));
         pb.addLabel("keV", CC.xy(6, 6));
         pb.add(new JLabel("Nominal working distance"), CC.xy(2, 8), jTextField_NominalWD, CC.xy(4, 8));
         pb.addLabel("mm", CC.xy(6, 8));
         pb.add(new JLabel("Nominal probe current"), CC.xy(2, 10), jTextField_NominalI, CC.xy(4, 10));
         pb.addLabel("nA", CC.xy(6, 10));
         pb.add(jCheckBox_Current, CC.xyw(2, 12, 6));
         final JPanel pbPanel = pb.getPanel();
         pbPanel.setBorder(DTSA2.createTitledBorder("Specify parameters for the QC project"));
         add(pbPanel);
      }

      @Override
      public boolean permitNext() {
         boolean next = true;
         final NumberFormat nf = NumberFormat.getNumberInstance();
         try {
            jTextField_BeamEnergy.setBackground(SystemColor.text);
            final double e0 = nf.parse(jTextField_BeamEnergy.getText()).doubleValue();
            if((e0 < 1.0) || (e0 > 500.0))
               throw new EPQException("The beam energy is out-of-range.");
            mBeamEnergy = e0;
         }
         catch(final Exception e) {
            jTextField_BeamEnergy.setBackground(Color.pink);
            next = false;
         }
         try {
            jTextField_NominalWD.setBackground(SystemColor.text);
            final double wd = nf.parse(jTextField_NominalWD.getText()).doubleValue();
            if((wd < 0.0) || (wd > 1000.0))
               throw new EPQException("The nominal working distance is out-of-range.");
            mNominalWD = wd;
         }
         catch(final Exception e) {
            jTextField_NominalWD.setBackground(Color.pink);
            next = false;
         }
         try {
            jTextField_NominalI.setBackground(SystemColor.text);
            final double i = nf.parse(jTextField_NominalI.getText()).doubleValue();
            if((i < 0.0001) || (i > 100000.0))
               throw new EPQException("The nominal probe current is out-of-range.");
            mNominalI = i;
         }
         catch(final Exception e) {
            jTextField_NominalI.setBackground(Color.pink);
            next = false;
         }
         mDetector = (DetectorProperties) jComboBox_Detector.getSelectedItem();
         mCurrentMode = (jCheckBox_Current.isSelected() ? QCNormalizeMode.CURRENT : QCNormalizeMode.TOTAL_COUNTS);
         mCreateQCProject = next;
         return next;
      }

      @Override
      public void onHide() {
         try {
            if(mCreateQCProject) {
               final QCProject qcp = mSession.createQCProject(mDetector, mMaterial, mBeamEnergy, mCurrentMode, mNominalWD, mNominalI);
               final NumberFormat nf = new HalfUpFormat("0.0");
               mHTMLResult.append("<h3>New QC Project Created</h3>");
               mHTMLResult.append("<p><table>");
               mHTMLResult.append("<tr><th>Summary</th><td>" + qcp.toString() + "</td></tr>");
               mHTMLResult.append("<tr><th>Index</th><td>" + Integer.toString(qcp.getIndex()) + "</td></tr>");
               mHTMLResult.append("<tr><th>Detector</th><td>" + mDetector.toString() + "</td></tr>");
               mHTMLResult.append("<tr><th>Material</th><td>" + mMaterial.toString() + "</td></tr>");
               mHTMLResult.append("<tr><th>Beam energy</th><td>" + nf.format(mBeamEnergy) + "</td></tr>");
               mHTMLResult.append("<tr><th>Nominal Working Distance</th><td>" + nf.format(mNominalWD) + "</td></tr>");
               mHTMLResult.append("<tr><th>Nominal Probe Current</th><td>" + nf.format(mNominalI) + "</td></tr>");
               mHTMLResult.append("<tr><th>Normalization method</th><td>" + mCurrentMode.toString() + "</td></tr>");
               mHTMLResult.append("</table></p>");
            }
         }
         catch(final Exception e) {
            ErrorDialog.createErrorMessage(QCWizard.this, "Error creating a QC project", e);
         }
      }

      private void updateFields() {
         final NumberFormat nf = new HalfUpFormat("0.0");
         jTextField_Material.setText(mMaterial.toString());
         jTextField_BeamEnergy.setText(nf.format(mBeamEnergy));
         jTextField_NominalI.setText(nf.format(mNominalI));
         jCheckBox_Current.setSelected(mCurrentMode == QCNormalizeMode.CURRENT);
         jTextField_NominalWD.setText(nf.format(mNominalWD));
      }

      @Override
      public void onShow() {
         assert mMode == Mode.NEW_PROJECT;
         jTextField_Material.setEditable(false);
         jTextField_BeamEnergy.setEditable(true);
         jTextField_NominalI.setEditable(true);
         jTextField_NominalWD.setEditable(true);
         final Set<DetectorProperties> dp = mSession.getAllDetectors();
         final DefaultComboBoxModel<DetectorProperties> cbm = new DefaultComboBoxModel<>(dp.toArray(new DetectorProperties[0]));
         jComboBox_Detector.setModel(cbm);
         final DetectorProperties sel = dp.iterator().next();
         cbm.setSelectedItem(sel);
         mNominalWD = sel.getProperties().getNumericWithDefault(SpectrumProperties.DetectorOptWD, 12.0);
         updateFields();
         QCWizard.this.enableNext(false);
         QCWizard.this.enableFinish(true);
      }

   }

   private class SelectProjectPanel
      extends JWizardPanel {

      private final JComboBox<DetectorProperties> jComboBox_Detector = new JComboBox<>();
      private final JComboBox<QCProject> jComboBox_Project = new JComboBox<>();
      private final JTextField jTextField_Material = new JTextField();
      private final JTextField jTextField_BeamEnergy = new JTextField();
      private final JTextField jTextField_NominalI = new JTextField();
      private final JTextField jTextField_NominalWD = new JTextField();
      private final JCheckBox jCheckBox_Current = new JCheckBox("Use probe current normalization");

      private static final long serialVersionUID = -1090276644792187234L;

      public SelectProjectPanel(JWizardDialog wiz) {
         super(wiz, "Select a QC project");
         init();
      }

      private void init() {
         final FormLayout fl = new FormLayout("20dlu, right:pref, 5dlu, 50dlu, 3dlu, pref, 100dlu, 20dlu", "5dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 5dlu");
         final PanelBuilder pb = new PanelBuilder(fl);
         pb.add(new JLabel("EDS detector"), CC.xy(2, 2), jComboBox_Detector, CC.xyw(4, 2, 4));
         pb.add(new JLabel("QC Project"), CC.xy(2, 4), jComboBox_Project, CC.xyw(4, 4, 4));
         pb.add(new JLabel("Material"), CC.xy(2, 6), jTextField_Material, CC.xy(4, 6));
         pb.add(new JLabel("Beam energy"), CC.xy(2, 8), jTextField_BeamEnergy, CC.xy(4, 8));
         pb.addLabel("keV", CC.xy(6, 8));
         pb.add(new JLabel("Nominal working distance"), CC.xy(2, 10), jTextField_NominalWD, CC.xy(4, 10));
         pb.addLabel("mm", CC.xy(6, 10));
         pb.add(new JLabel("Nominal probe current"), CC.xy(2, 12), jTextField_NominalI, CC.xy(4, 12));
         pb.addLabel("nA", CC.xy(6, 12));
         pb.add(jCheckBox_Current, CC.xyw(2, 14, 5));
         // pb.setBorder(BorderFactory.createTitledBorder("Specify parameters for the QC project"));
         add(pb.getPanel());

         jComboBox_Detector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               mDetectorProperties = (DetectorProperties) jComboBox_Detector.getSelectedItem();
               updateDetector();
            }
         });

         jComboBox_Project.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
               mProject = (QCProject) jComboBox_Project.getSelectedItem();
               updateDetector();
            }
         });
         jTextField_Material.setEnabled(false);
         jTextField_BeamEnergy.setEnabled(false);
         jTextField_NominalI.setEnabled(false);
         jTextField_NominalWD.setEnabled(false);
         jCheckBox_Current.setEnabled(false);
      }

      private void updateDetector() {
         boolean clearFields = false;
         if(mDetectorProperties != null) {
            DefaultComboBoxModel<QCProject> cbm;
            try {
               final Set<QCProject> proj = mSession.findQCProjects(mDetectorProperties);
               cbm = new DefaultComboBoxModel<>(proj.toArray(new QCProject[0]));
               if((mProject == null) || (!mProject.getDetector().getDetectorProperties().equals(mDetectorProperties)))
                  mProject = (proj.size() > 0 ? proj.iterator().next() : null);
            }
            catch(final SQLException e) {
               cbm = new DefaultComboBoxModel<>();
            }
            if(mProject != null)
               cbm.setSelectedItem(mProject);
            jComboBox_Project.setModel(cbm);
            final NumberFormat nf = new HalfUpFormat("0.0");
            if(mProject != null) {
               jTextField_BeamEnergy.setText(nf.format(mProject.getBeamEnergy()));
               jTextField_NominalI.setText(nf.format(mProject.getNominalI()));
               jTextField_NominalWD.setText(nf.format(mProject.getNominalWD()));
               try {
                  jTextField_Material.setText(mProject.getStandard().toString());
               }
               catch(final SQLException e) {
                  jTextField_Material.setText("");
               }
               jCheckBox_Current.setSelected(mProject.getMode() == QCNormalizeMode.CURRENT);
            } else
               clearFields = true;

         } else {
            jComboBox_Project.setModel(new DefaultComboBoxModel<QCProject>());
            clearFields = true;
         }
         if(clearFields) {
            jTextField_Material.setText("");
            jTextField_BeamEnergy.setText("");
            jTextField_NominalI.setText("");
            jTextField_NominalWD.setText("");
            jCheckBox_Current.setSelected(true);
         }
         enableNext(!clearFields);
      }

      @Override
      public void onShow() {
         try {
            final Set<DetectorProperties> dps = mSession.findDetectorsWithQCProjects();
            final DefaultComboBoxModel<DetectorProperties> cbm = new DefaultComboBoxModel<>(dps.toArray(new DetectorProperties[0]));
            if((mDetectorProperties == null) || (!dps.contains(mDetectorProperties)))
               mDetectorProperties = dps.iterator().next();
            if(dps.size() > 0)
               cbm.setSelectedItem(mDetectorProperties);
            jComboBox_Detector.setModel(cbm);
            getWizard().enableFinish(false);
            updateDetector();
         }
         catch(final SQLException e) {
            e.printStackTrace();
         }
         switch(mMode) {
            case MEASUREMENT:
               setNextPanel(jWizardPanel_Measurement);
               enableFinish(false);
               enableNext(true);
               break;
            case EXPORT:
               setNextPanel(jWizardPanel_Export);
               enableFinish(false);
               enableNext(true);
            case LIMITS:
               break;
            case REPORT:
               setNextPanel(jWizardPanel_Report);
               enableFinish(false);
               enableNext(true);
               break;
            case SPECTRA:
               setNextPanel(null);
               enableFinish(true);
               enableNext(false);
               break;
            case NEW_PROJECT:
               assert false;
         }
      }
   }

   private class MeasurementPanel
      extends JWizardPanel {

      private static final long serialVersionUID = -2134117303082538242L;

      private final JTextField jTextField_Spectrum = new JTextField();
      private final JButton jButton_Spectrum = new JButton("Open");
      private final JTextField jTextField_Current = new JTextField();
      private final JButton jButton_Properties = new JButton("Properties");
      private final JDateChooser jDateChooser_Timestamp = new JDateChooser();
      private final JTextField jTextField_Livetime = new JTextField();

      private MeasurementPanel(JWizardDialog wiz) {
         super(wiz, "Select a measured spectrum");
         init();
      }

      private void init() {
         final FormLayout fl = new FormLayout("20dlu, pref, 2dlu, 60dlu, 2dlu, pref, 2dlu, pref, 20dlu", "10dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 10dlu");
         final PanelBuilder pb = new PanelBuilder(fl);
         pb.add(new JLabel("Spectrum"), CC.xy(2, 2), jTextField_Spectrum, CC.xyw(4, 2, 3));
         pb.add(jButton_Spectrum, CC.xy(8, 2));
         pb.add(new JLabel("Probe current"), CC.xy(2, 4), jTextField_Current, CC.xy(4, 4));
         pb.add(jButton_Properties, CC.xy(6, 4));
         pb.add(new JLabel("Live time"), CC.xy(2, 6), jTextField_Livetime, CC.xy(4, 6));
         pb.add(new JLabel("Acquired"), CC.xy(2, 8), jDateChooser_Timestamp, CC.xy(4, 8));
         final JPanel panel = pb.getPanel();
         panel.setBorder(DTSA2.createTitledBorder("QC Spectrum"));
         add(panel);
         jTextField_Current.setEditable(false);
         jTextField_Livetime.setEditable(false);
         jTextField_Spectrum.setEditable(false);
         jButton_Spectrum.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               final SpectrumFileChooser sfc = new SpectrumFileChooser(QCWizard.this, "Open spectrum files...");
               final File dir = new File(DTSA2.getSpectrumDirectory());
               sfc.getFileChooser().setCurrentDirectory(dir);
               sfc.setLocationRelativeTo(QCWizard.this);
               sfc.setMultiSelectionEnabled(false);
               final int res = sfc.showOpenDialog();
               if(res == JFileChooser.APPROVE_OPTION) {
                  DTSA2.updateSpectrumDirectory(sfc.getFileChooser().getCurrentDirectory());
                  final ISpectrumData spec = sfc.getSpectra()[0];
                  final SpectrumProperties sp = spec.getProperties();
                  final Date ts = sp.getTimestampWithDefault(SpectrumProperties.AcquisitionTime, new Date());
                  final DetectorCalibration dc = mSession.getSuitableCalibration(mDetectorProperties, ts);
                  if(dc instanceof EDSCalibration) {
                     boolean assign = SpectrumUtils.areCalibratedSimilar(dc.getProperties(), spec, AppPreferences.DEFAULT_TOLERANCE);
                     if(!assign) {
                        final int opt = JOptionPane.showConfirmDialog(QCWizard.this, "<html>The calibration of <i>"
                              + spec.toString() + "</i><br>" + "does not seem to be similar to the default detector.<br><br>"
                              + "Should it be used for the QC none-the-less?", "QC spectrum open", JOptionPane.YES_NO_OPTION);
                        assign = (opt == JOptionPane.YES_OPTION);
                     }
                     if(assign) {
                        final Composition comp = sp.getCompositionWithDefault(SpectrumProperties.StandardComposition, null);
                        try {
                           final Composition std = mProject.getStandard();
                           if((comp != null) && (comp.almostEquals(std, 0.1))) {
                              final int opt = JOptionPane.showConfirmDialog(QCWizard.this, "<html>The spectrum reports that it was collected from "
                                    + comp.toString()
                                    + " not <br>"
                                    + std.toString()
                                    + " as the project requires.<br><br>"
                                    + "<b>Should it be used for the QC none-the-less?</b>", "QC spectrum open", JOptionPane.YES_NO_OPTION);
                              assign = (opt == JOptionPane.YES_OPTION);
                           }
                        }
                        catch(final Exception e2) {
                           e2.printStackTrace();
                        }
                     }
                     if(assign) {
                        final double e0 = sp.getNumericWithDefault(SpectrumProperties.BeamEnergy, mProject.getBeamEnergy());
                        sp.setNumericProperty(SpectrumProperties.BeamEnergy, e0);
                        if(!mProject.matchesBeamEnergy(e0)) {
                           final NumberFormat nf = new HalfUpFormat("0.0");
                           final int opt = JOptionPane.showConfirmDialog(QCWizard.this, "<html>The spectrum was collected at "
                                 + nf.format(e0) + "kev, not +" + nf.format(mProject.getBeamEnergy())
                                 + " keV as expected<br><br>" + "<b>Should it be used for the QC none-the-less?</b>", "QC spectrum open", JOptionPane.YES_NO_OPTION);
                           assign = (opt == JOptionPane.YES_OPTION);
                        }
                     }
                     if(assign)
                        mSpectrum = SpectrumUtils.applyEDSDetector(EDSDetector.createDetector(mDetectorProperties, (EDSCalibration) dc), spec);
                  }
                  updateSpectrum();
               }
            }
         });
         jButton_Properties.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               final SpectrumPropertyPanel.PropertyDialog pd = new SpectrumPropertyPanel.PropertyDialog(QCWizard.this, mSession);
               pd.addSpectrumProperties(mSpectrum.getProperties());
               pd.setLocationRelativeTo(QCWizard.this);
               pd.setVisible(true);
               if(pd.isOk()) {
                  mSpectrum.getProperties().addAll(pd.getSpectrumProperties());
                  updateSpectrum();
               }
            }
         });
      }

      @Override
      public void onShow() {
         setNextPanel(jWizardPanel_Results);
         updateSpectrum();
         QCWizard.this.enableNext(true);
         QCWizard.this.enableFinish(false);
      }

      @Override
      public boolean permitNext() {
         boolean res = (mSpectrum != null);
         if(res) {
            jTextField_Spectrum.setBackground(SystemColor.text);
            final SpectrumProperties sp = mSpectrum.getProperties();
            final boolean lt = !Double.isNaN(sp.getNumericWithDefault(SpectrumProperties.LiveTime, Double.NaN));
            jTextField_Livetime.setBackground(lt ? SystemColor.text : Color.PINK);
            res &= lt;
            sp.setTimestampProperty(SpectrumProperties.AcquisitionTime, jDateChooser_Timestamp.getDate());
            final boolean fc = !Double.isNaN(SpectrumUtils.getAverageFaradayCurrent(sp, Double.NaN));
            jTextField_Current.setBackground(fc ? SystemColor.text : Color.PINK);
            res &= fc;
         } else {
            jTextField_Spectrum.setBackground(Color.PINK);
            jTextField_Livetime.setBackground(SystemColor.text);
            jDateChooser_Timestamp.setBackground(SystemColor.text);
            jTextField_Current.setBackground(SystemColor.text);

         }
         return res;
      }

      public void updateSpectrum() {
         if(mSpectrum != null) {
            final SpectrumProperties sp = mSpectrum.getProperties();
            jTextField_Spectrum.setText(mSpectrum.toString());
            final double i = SpectrumUtils.getAverageFaradayCurrent(sp, Double.NaN);
            jTextField_Current.setText(NumberFormat.getInstance().format(i) + " nA");
            final Date time = sp.getTimestampWithDefault(SpectrumProperties.AcquisitionTime, new Date());
            jDateChooser_Timestamp.setDate(time);
            jTextField_Livetime.setText(sp.getTextWithDefault(SpectrumProperties.LiveTime, "n/a"));
         } else {
            jTextField_Spectrum.setText("");
            jTextField_Current.setText("");
            jDateChooser_Timestamp.setDate(new Date());
            jTextField_Livetime.setText("");
         }
         permitNext();
      }
   }

   private class ResultPanel
      extends JWizardPanel {
      /*
       * Load the project results and summarized them. Compare this result with
       * the overall average, the first 10 records and the last ten records.
       */

      private static final long serialVersionUID = -2600504323164082813L;

      private final JTable jTable_Results = new JTable();
      private final TableModel mModel = new DefaultTableModel(new Object[] {
         "Name",
         "QC",
         "Value",
         "Average(All)",
         "Avg(First 10)",
         "Avg(Last 10)"
      }, 15);
      private FitThread mFitThread;

      private class FitThread
         extends SwingWorker<Map<String, UncertainValue2>, Object> {

         private final PerformQC mPerformQC;

         private transient Map<String, UncertainValue2> mResults;

         private FitThread(QCProject qc, ISpectrumData spec, JTable resultTable) {
            mPerformQC = new PerformQC(qc, spec);
         }

         @Override
         protected Map<String, UncertainValue2> doInBackground()
               throws Exception {
            try {
               mResults = mPerformQC.compute();
               DataManager.getInstance().addSpectrum(mSpectrum, true);
               DataManager.getInstance().addSpectrum(mPerformQC.getFitSpectrum(), true, mSpectrum);
               DataManager.getInstance().addSpectrum(mPerformQC.getResidual(), true, mSpectrum);
            }
            catch(final Exception e) {
               mResults = null;
               e.printStackTrace();
            }
            return mResults;
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

         @Override
         protected void done() {
            if(mResults != null)
               try {
                  final DefaultTableModel model = new DefaultTableModel(new Object[] {
                     "Name",
                     "Value",
                     "Average(All)",
                     "Avg(First 10)",
                     "Avg(Last 10)"
                  }, mResults.size());
                  int row = 0;
                  for(final String name : mResults.keySet()) {
                     model.setValueAt(name, row, 0);
                     model.setValueAt(uncVal(name, mResults.get(name)), row, 1);
                     model.setValueAt(dsVal(name, mPerformQC.getAll().get(name)), row, 2);
                     model.setValueAt(dsVal(name, mPerformQC.getFirst10().get(name)), row, 3);
                     model.setValueAt(dsVal(name, mPerformQC.getLast10().get(name)).toString(), row, 4);
                     ++row;
                  }
                  jTable_Results.setModel(model);
               }
               catch(final Exception e) {
                  e.printStackTrace();
               }
         }
      }

      private ResultPanel(JWizardDialog wiz) {
         super(wiz, "Review the results");
         init();
      }

      private void init() {
         setLayout(new BorderLayout());
         jTable_Results.setModel(mModel);
         add(new JScrollPane(jTable_Results), BorderLayout.CENTER);
      }

      @Override
      public void onShow() {
         mFitThread = new FitThread(mProject, mSpectrum, jTable_Results);
         mFitThread.execute();
         QCWizard.this.enableNext(false);
         QCWizard.this.enableFinish(true);
      }

      @Override
      public void onHide() {
         final Map<String, UncertainValue2> res = mFitThread.mResults;
         if(isFinished() && (mSpectrum != null) && (res != null))
            try {
               mFitThread.mPerformQC.addToProject(res);
               mHTMLResult.append(mFitThread.mPerformQC.toHTML());
            }
            catch(final Exception e) {
               ErrorDialog.createErrorMessage(QCWizard.this, "Error writing QC results", e);
            }
      }
   }

   private class ExportPanel
      extends JWizardPanel {

      private static final long serialVersionUID = -1172668443738619514L;

      private final JTextField jTextField_Filename = new JTextField();
      private final JButton jButton_Select = new JButton("Export");

      private File mFile = null;

      private ExportPanel(JWizardDialog wd) {
         super(wd, "Specify where to export results");
         init();
      }

      private void init() {
         final FormLayout fl = new FormLayout("20dlu, right:pref, 5dlu, 100dlu, 5dlu, pref, 20dlu", "10dlu, pref,10dlu");
         final PanelBuilder pb = new PanelBuilder(fl);
         pb.add(new JLabel("Export file name"), CC.xy(2, 2), jTextField_Filename, CC.xy(4, 2));
         pb.add(jButton_Select, CC.xy(6, 2));
         add(pb.getPanel());

         jTextField_Filename.setEditable(false);
         jButton_Select.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
               final Preferences pref = Preferences.userNodeForPackage(QCWizard.class);
               final String defFn = pref.get("Export file", System.getProperty("user.home") + "/default.csv");
               final JFileChooser fc = new JFileChooser();
               fc.setFileFilter(new SimpleFileFilter(new String[] {
                  "csv",
                  "tsv"
               }, "Text table files"));
               fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
               fc.setSelectedFile(new File(defFn));
               if(fc.showSaveDialog(QCWizard.this) == JFileChooser.APPROVE_OPTION) {
                  mFile = fc.getSelectedFile();
                  pref.put("Export file", mFile.toString());
                  jTextField_Filename.setText(mFile.toString());
                  enableFinish(true);
               }
            }
         });
      }

      @Override
      public void onHide() {
         try (final FileWriter wr = new FileWriter(mFile)) {
            mProject.write(wr);
         }
         catch(final Exception e) {
            ErrorDialog.createErrorMessage(QCWizard.this, "Error exporting QC data", e);
         }
      }

   }

   private class ReportPanel
      extends JWizardPanel {

      private ArrayList<JCheckBox> mItems;
      private static final long serialVersionUID = 2286667470156242838L;

      private JScrollPane jScrollPane_ReportItems = new JScrollPane();

      public ReportPanel(QCWizard wiz) {
         super(wiz, "Configure a report");
         init();
      }

      private void init() {
         // Nothing to do...
      }

      @Override
      public void onShow() {
         try {
            QCWizard.this.setErrorText("");
            final PanelBuilder pb = new PanelBuilder(new FormLayout("fill:200dlu", "fill:120dlu"));
            final Set<String> items = mProject.getItemNames();
            mItems = new ArrayList<>();
            final DefaultFormBuilder dfb = new DefaultFormBuilder(new FormLayout("pref", ""));
            dfb.lineGapSize(new ConstantSize(0, ConstantSize.PIXEL));
            final Preferences pref = Preferences.userNodeForPackage(QCWizard.class);
            for(final String item : items) {
               final JCheckBox cb = new JCheckBox(item);
               cb.setSelected(pref.getBoolean("Report Item/" + cb.getText(), false));
               mItems.add(cb);
               dfb.append(cb);
               dfb.nextLine();
            }
            jScrollPane_ReportItems = new JScrollPane(dfb.getPanel());
            pb.add(jScrollPane_ReportItems, CC.xy(1, 1));
            this.removeAll();
            final JPanel panel = pb.getPanel();
            panel.setBorder(DTSA2.createTitledBorder("Report items"));
            this.add(panel);
            QCWizard.this.enableNext(false);
            QCWizard.this.enableFinish(true);
         }
         catch(final Exception e) {
            e.printStackTrace();
         }
      }

      @Override
      public boolean permitNext() {
         for(final JCheckBox cb : mItems)
            if(cb.isSelected())
               return true;
         QCWizard.this.setErrorText("Select at least one item for the report.");
         return false;
      }

      @Override
      public void onHide() {
         if(QCWizard.this.isFinished()) {
            final Preferences pref = Preferences.userNodeForPackage(QCWizard.class);
            final ArrayList<String> items = new ArrayList<>();
            for(final JCheckBox cb : mItems) {
               if(cb.isSelected())
                  items.add(cb.getText());
               pref.putBoolean("Report Item/" + cb.getText(), cb.isSelected());
            }
            final File path = DTSA2.getReport().getFile().getParentFile();
            try {
               final File report = File.createTempFile("QCReport", ".htm", path);
               generateReport(report, items);
               Desktop.getDesktop().browse(report.toURI());
            }
            catch(final Exception e) {
               ErrorDialog.createErrorMessage(QCWizard.this, "Error generating QC report", e);
            }
         }
      }
   }

   /**
    * @param owner
    * @param session
    */
   public QCWizard(Frame owner, Session session) {
      super(owner, "Quality control alien", true);
      mSession = session;
      mMode = Mode.MEASUREMENT;
      setActivePanel(jWizardPanel_Welcome);
   }

   /**
    * @param owner
    * @param session
    */
   public QCWizard(Dialog owner, Session session) {
      super(owner, "Quality control alien", true);
      mSession = session;
      mMode = Mode.MEASUREMENT;
      setActivePanel(jWizardPanel_Welcome);
   }

   public String getHTMLResults() {
      return mHTMLResult.toString();
   }

   public void setDetector(DetectorProperties det) {
      mDetectorProperties = det;
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

   public void generateReport(File file, Collection<String> items)
         throws IOException, SQLException, EPQException {
      PerformQC.generateReport(mProject, file, items);
   }
}
