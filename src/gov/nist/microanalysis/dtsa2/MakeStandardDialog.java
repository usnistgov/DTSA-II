package gov.nist.microanalysis.dtsa2;

import java.awt.Color;
import java.awt.Frame;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import gov.nist.microanalysis.EPQDatabase.Session;
import gov.nist.microanalysis.EPQLibrary.Composition;
import gov.nist.microanalysis.EPQLibrary.ConductiveCoating;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.EditableSpectrum;
import gov.nist.microanalysis.EPQLibrary.Element;
import gov.nist.microanalysis.EPQLibrary.FromSI;
import gov.nist.microanalysis.EPQLibrary.ISpectrumData;
import gov.nist.microanalysis.EPQLibrary.Material;
import gov.nist.microanalysis.EPQLibrary.MaterialFactory;
import gov.nist.microanalysis.EPQLibrary.RegionOfInterestSet;
import gov.nist.microanalysis.EPQLibrary.RegionOfInterestSet.RegionOfInterest;
import gov.nist.microanalysis.EPQLibrary.SampleShape;
import gov.nist.microanalysis.EPQLibrary.SampleShape.ThinFilm;
import gov.nist.microanalysis.EPQLibrary.SpectrumFitter8;
import gov.nist.microanalysis.EPQLibrary.SpectrumFitter8.AltEnergyScaleFunction;
import gov.nist.microanalysis.EPQLibrary.SpectrumFitter8.FanoNoiseWidth;
import gov.nist.microanalysis.EPQLibrary.SpectrumMath;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;
import gov.nist.microanalysis.EPQLibrary.SpectrumSimulator.BasicSpectrumSimulator;
import gov.nist.microanalysis.EPQLibrary.SpectrumUtils;
import gov.nist.microanalysis.EPQLibrary.StandardBundle;
import gov.nist.microanalysis.EPQLibrary.Detector.EDSDetector;
import gov.nist.microanalysis.EPQTools.ErrorDialog;
import gov.nist.microanalysis.EPQTools.JComboBoxCoating;
import gov.nist.microanalysis.EPQTools.JElementPanel;
import gov.nist.microanalysis.EPQTools.JProgressDialog;
import gov.nist.microanalysis.EPQTools.JTextFieldDouble;
import gov.nist.microanalysis.EPQTools.MaterialsCreator;
import gov.nist.microanalysis.EPQTools.SelectElements;
import gov.nist.microanalysis.EPQTools.SpectrumFileChooser;
import gov.nist.microanalysis.EPQTools.SpectrumPropertyPanel;
import gov.nist.microanalysis.Utility.HalfUpFormat;
import gov.nist.microanalysis.Utility.Math2;

/**
 * <p>
 * A dialog for facilitating the creation of standard spectra from a list of
 * individual spectra.
 * </p>
 * <p>
 * Copyright: Pursuant to title 17 Section 105 of the United States Code this
 * software is not subject to copyright protection and is in the public domain
 * </p>
 * <p>
 * Institution: National Institute of Standards and Technology
 * </p>
 *
 * @author nritchie @version 1.0
 */
public class MakeStandardDialog
   extends
   JDialog {

   private static final long serialVersionUID = -1921213224984265198L;

   private final JTabbedPane jTabbedPane = new JTabbedPane();

   private final JLabel jLabel_Banner = new JLabel("XXX");
   private final JTextField jTextField_Material = new JTextField();
   private final JButton jButton_Material = new JButton("Edit");
   private final JElementPanel jPanel_Element = new JElementPanel();
   private final JTextField jTextField_BeamEnergy = new JTextField();
   private final JTextField jTextField_Dose = new JTextField();
   private final JTextField jTextField_Detector = new JTextField();
   private final JButton jButton_SpecProperties = new JButton("Properties");
   private final JCheckBox jCheckBox_Film = new JCheckBox("Thin film standard");
   private final JTextFieldDouble jTextField_Thickness = new JTextFieldDouble(100.0, 1.0, 1.0e6, "#,##0", "Bulk");
   private final JCheckBox jCheckBox_Coating = new JCheckBox("Conductive coating");
   private final JTextFieldDouble jTextField_CoatingThickness = new JTextFieldDouble(10.0, 0.0, 1000.0, "#,##0.0", "None");
   private final JComboBoxCoating jComboBox_Coating = new JComboBoxCoating(this, DTSA2.getSession());

   private final JTable jTable_Refs = new JTable();
   private final JCheckBox jCheckBox_Carbon = new JCheckBox("Strip carbon contamination?");
   private final JCheckBox jCheckBox_Oxygen = new JCheckBox("Strip oxide layer or contamination?");
   private final JButton jButton_Auto = new JButton("Auto");
   private final JButton jButton_Read = new JButton("Read");
   private final JButton jButton_Clear = new JButton("Clear");

   private final JButton jButton_Save = new JButton("Save");
   private final JButton jButton_Cancel = new JButton("Cancel");

   private final Session mSession;
   private Composition mMaterial = new Composition();
   private final ArrayList<ISpectrumData> mSpectra = new ArrayList<ISpectrumData>();
   private double mBeamEnergy = Double.NaN;
   private double mProbeDose = 0.0;
   private boolean mThinFilmStandard = false;
   private boolean mCoating = false;
   private double mThickness = Double.NaN;
   private double mCoatingThickness = Double.NaN;
   private Material mCoatingMaterial = Material.Null;
   private EDSDetector mDetector = null;
   private ISpectrumData mResult = null;
   private ISpectrumData mBestFit = null;
   private ISpectrumData mRough = null;
   private boolean mSave = false;

   private final Map<Element, StandardBundle> mBundle = new TreeMap<Element, StandardBundle>();
   private final Set<RegionOfInterest> mROI = new HashSet<RegionOfInterest>();
   private final Set<Element> mStrip = new TreeSet<Element>();

   public MakeStandardDialog(final Frame frame, final Session session) {
      super(frame, "Build standard bundle", true);
      mSession = session;
      initialize();
      update();
      pack();
      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
   }

   private void updateThicknessCheckBox() {
      if(mThinFilmStandard) {
         jTextField_Thickness.setEnabled(true);
         jTextField_Thickness.setValue(mThickness * 1.0e9);
         jTextField_Thickness.selectAll();
         jTextField_Thickness.requestFocus();
         jTabbedPane.setEnabledAt(1, false);
      } else {
         jTextField_Thickness.setValue(Double.NaN);
         jTextField_Thickness.setEnabled(false);
         jTabbedPane.setEnabledAt(1, true);
      }
   }

   private void updateCoatingCheckBox() {
      if(mCoating) {
         jCheckBox_Coating.setSelected(true);
         jTextField_CoatingThickness.setEnabled(true);
         if(Double.isNaN(mCoatingThickness))
            mCoatingThickness = 10.0e-9;
         jTextField_CoatingThickness.setValue(mCoatingThickness * 1.0e9);
         jTextField_CoatingThickness.selectAll();
         jTextField_CoatingThickness.requestFocus();
         jComboBox_Coating.setEnabled(true);
         if(mCoatingMaterial.equals(Material.Null))
            mCoatingMaterial = jComboBox_Coating.getItemAt(0);
         jComboBox_Coating.setSelectedItem(mCoatingMaterial);
      } else {
         jCheckBox_Coating.setSelected(false);
         mCoatingMaterial = Material.Null;
         mCoatingThickness = 0.0;
         jTextField_CoatingThickness.setValue(Double.NaN);
         jTextField_CoatingThickness.setEnabled(false);
         jComboBox_Coating.setSelectedItem(mCoatingMaterial);
         jComboBox_Coating.setEnabled(false);
      }
   }

   private void initialize() {
      jTabbedPane.addTab("Standard", buildStdPanel());
      jTabbedPane.addTab("References", buildRefPanel());

      final ButtonBarBuilder bbb = new ButtonBarBuilder();
      bbb.addGlue();
      bbb.addButton(jButton_Save, jButton_Cancel);
      final CellConstraints cc0 = new CellConstraints();
      final FormLayout fl2 = new FormLayout("4dlu, pref, 4dlu", "4dlu, pref, 5dlu, pref, 4dlu");
      setLayout(fl2);
      add(jTabbedPane, cc0.xy(2, 2));
      add(bbb.build(), cc0.xy(2, 4));

      jButton_Cancel.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(final ActionEvent e) {
            mResult = null;
            mSave = false;
            setVisible(false);
         }
      });

      jButton_Save.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(final ActionEvent e) {
            mResult = buildResult();
            mSave = true;
            setVisible(false);
         }
      });
      jTextField_BeamEnergy.setEditable(false);
      jTextField_Dose.setEditable(false);
      jTextField_Detector.setEditable(false);
   }

   protected void updateMaterialField(final Composition res) {
      if(res != null) {
         if((res != null) && (!mMaterial.getElementSet().equals(res.getElementSet()))) {
            jPanel_Element.setAvailableElements(res.getElementSet());
            jPanel_Element.setSelected(res.getElementSet());
         }
         jTextField_Material.setText(res.getElementCount() > 0 ? res.getName() : "");
         jTextField_Material.setBackground(SystemColor.text);
         if(res.getElementCount() < 1)
            jTextField_Material.setBackground(Color.pink);
         else
            jTextField_Material.setBackground(SystemColor.text);
         if(res.containsElement(Element.C)) {
            jCheckBox_Carbon.setSelected(false);
            jCheckBox_Carbon.setEnabled(false);
            removeStrip(Element.C);
         } else {
            jCheckBox_Carbon.setEnabled(true);
            if(jCheckBox_Carbon.isSelected())
               addStrip(Element.C);
         }
         if(res.containsElement(Element.O)) {
            jCheckBox_Oxygen.setSelected(false);
            jCheckBox_Oxygen.setEnabled(false);
            removeStrip(Element.O);
         } else {
            jCheckBox_Oxygen.setEnabled(true);
            if(jCheckBox_Oxygen.isSelected())
               addStrip(Element.O);
         }
         mMaterial = res;
      }
   }

   public void addStrip(final Element elm) {
      mStrip.add(elm);
      for(final StandardBundle sb : mBundle.values())
         sb.addStrip(elm);
      jTable_Refs.setModel(buildRefTable());
   }

   public void removeStrip(final Element elm) {
      mStrip.remove(elm);
      for(final StandardBundle sb : mBundle.values())
         sb.removeStrip(elm);
      jTable_Refs.setModel(buildRefTable());
   }

   protected JPanel buildStdPanel() {
      final FormLayout fl = new FormLayout("right:pref, 5dlu, 100dlu, 5dlu, left:pref", "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref");
      final PanelBuilder pb = new PanelBuilder(fl);
      final CellConstraints cc0 = new CellConstraints(), cc1 = new CellConstraints();
      int row = 1;
      pb.add(jLabel_Banner, cc0.xyw(1, row, 5));
      row += 2;
      pb.add(new JLabel("Material"), cc0.xy(1, row), jTextField_Material, cc1.xy(3, row));
      pb.add(jButton_Material, cc0.xy(5, row));
      row += 2;
      jPanel_Element.addElementChange(new ElementUpdateListener());
      pb.add(new JLabel("Elements"), cc0.xy(1, row), jPanel_Element, cc1.xy(3, row));
      row += 2;
      pb.add(new JLabel("Beam energy"), cc0.xy(1, row), jTextField_BeamEnergy, cc1.xy(3, row));
      pb.addLabel("keV", cc0.xy(5, row));
      row += 2;
      pb.add(new JLabel("Probe Dose"), cc0.xy(1, row), jTextField_Dose, cc1.xy(3, row));
      pb.addLabel("nA\u00B7s", cc0.xy(5, row));
      row += 2;
      pb.add(new JLabel("Detector"), cc0.xy(1, row), jTextField_Detector, cc1.xyw(3, row, 3));
      row += 2;
      pb.add(jButton_SpecProperties, cc0.xy(5, row));
      row += 2;
      pb.add(jCheckBox_Film, cc1.xyw(1, row, 3));
      row += 2;
      pb.add(new JLabel("Thickness"), cc0.xy(1, row), jTextField_Thickness, cc1.xy(3, row));
      pb.addLabel("nm", cc0.xy(5, row));

      row += 2;
      pb.add(jCheckBox_Coating, cc1.xyw(1, row, 3));
      row += 2;
      {
         final FormLayout flc = new FormLayout("60dlu, 5dlu, pref, 5dlu, pref", "pref");
         final PanelBuilder pbc = new PanelBuilder(flc);
         pbc.add(jTextField_CoatingThickness, cc0.xy(1, 1));
         pbc.addLabel("nm of ", cc0.xy(3, 1));
         pbc.add(jComboBox_Coating, cc0.xy(5, 1));
         pb.add(new JLabel("Thickness"), cc0.xy(1, row), pbc.getPanel(), cc1.xyw(3, row, 3));
      }
      pb.opaque(true);
      final JPanel res = pb.getPanel();
      res.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      jTextField_Material.addFocusListener(new FocusAdapter() {
         @Override
         public void focusLost(final FocusEvent e) {
            final String name = jTextField_Material.getText();
            Composition res = null;
            if(mSession != null)
               try {
                  res = mSession.findStandard(name);
               }
               catch(final SQLException e1) {
                  // Ignore it...
               }
            if(res == null)
               try {
                  res = MaterialFactory.createCompound(name);
               }
               catch(final EPQException e1) {
                  // Ignore it..
               }
            if(res == null) {
               final Composition comp = new Composition();
               comp.setName(name);
               res = MaterialsCreator.editMaterial(MakeStandardDialog.this, comp, false);
            }
            if((res == null) || (res.getElementCount() < 1))
               jTextField_Material.setBackground(Color.pink);
            else {
               res.setName(name);
               updateMaterialField(res);
            }
            buildBundles();
            jButton_Save.setEnabled(mMaterial.getElementCount() > 0);
         }

      });

      jButton_Material.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(final ActionEvent e) {
            final Composition res = MaterialsCreator.editMaterial(MakeStandardDialog.this, mMaterial, mSession, false);
            if(res != null)
               updateMaterialField(res);
            buildBundles();
            jButton_Save.setEnabled(mMaterial.getElementCount() > 0);
         }
      });

      jButton_SpecProperties.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(final ActionEvent e) {
            if(getResult() != null) {
               final SpectrumProperties sp = getResult().getProperties();
               sp.addAll(editSpectrumProperties(sp));
               try {
                  mProbeDose = SpectrumUtils.getDose(sp);
                  mBeamEnergy = sp.getNumericWithDefault(SpectrumProperties.BeamEnergy, mBeamEnergy);
                  final DecimalFormat df = new HalfUpFormat("0.0");
                  jTextField_BeamEnergy.setText(df.format(mBeamEnergy));
                  jTextField_Dose.setText(df.format(mProbeDose));
               }
               catch(final EPQException e1) {
                  e1.printStackTrace();
               }

            }
         }
      });

      jCheckBox_Film.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(final ActionEvent e) {
            mThinFilmStandard = jCheckBox_Film.isSelected();
            updateThicknessCheckBox();
            buildBundles();
         }
      });

      jCheckBox_Coating.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(final ActionEvent e) {
            mCoating = jCheckBox_Coating.isSelected();
            updateCoatingCheckBox();
            buildBundles();
         }
      });

      jTextField_Thickness.addValueChange(new ActionListener() {

         @Override
         public void actionPerformed(final ActionEvent arg0) {
            if(jTextField_Thickness.isAltValue())
               mThickness = Double.NaN;
            else
               mThickness = jTextField_Thickness.getValue() * 1.0e-9;
            buildBundles();
         }

      });

      jTextField_CoatingThickness.addValueChange(new AbstractAction() {

         private static final long serialVersionUID = -4454637546890089042L;

         @Override
         public void actionPerformed(final ActionEvent arg0) {
            if(jTextField_CoatingThickness.isAltValue())
               mCoatingThickness = Double.NaN;
            else
               mCoatingThickness = jTextField_CoatingThickness.getValue() * 1.0e-9;
            buildBundles();
         }
      });

      return res;
   }

   private JPanel buildRefPanel() {
      final FormLayout fl = new FormLayout("4dlu, 200dlu, 4dlu", "4dlu, 120dlu, 2dlu, pref, 1dlu, pref, 2dlu, pref, 2dlu");
      final PanelBuilder pb = new PanelBuilder(fl);
      pb.add(new JScrollPane(jTable_Refs), CC.xy(2, 2));
      pb.add(jCheckBox_Carbon, CC.xy(2, 4));
      pb.add(jCheckBox_Oxygen, CC.xy(2, 6));
      final ButtonBarBuilder bbb = new ButtonBarBuilder();
      bbb.addButton(jButton_Auto);
      bbb.addGlue();
      bbb.addButton(jButton_Read, jButton_Clear);
      pb.add(bbb.build(), CC.xy(2, 8));
      pb.opaque(true);

      jCheckBox_Carbon.addActionListener(new CarbonCheckListener());
      jCheckBox_Oxygen.addActionListener(new OxygenCheckListener());

      jButton_Read.addActionListener(new readReferenceListener());
      jButton_Clear.addActionListener(new clearReferenceListener());
      jButton_Auto.addActionListener(new autoReferenceListener());
      return pb.build();
   }

   private class CarbonCheckListener
      extends
      AbstractAction {

      /**
       *
       */
      private static final long serialVersionUID = -1654348459420442991L;

      @Override
      public void actionPerformed(final ActionEvent e) {
         if(jCheckBox_Carbon.isSelected())
            addStrip(Element.C);
         else
            removeStrip(Element.C);
      }
   }
   private class OxygenCheckListener
      extends
      AbstractAction {

      private static final long serialVersionUID = 7269740692906409400L;

      @Override
      public void actionPerformed(final ActionEvent e) {
         if(jCheckBox_Oxygen.isSelected())
            addStrip(Element.O);
         else
            removeStrip(Element.O);
      }
   }

   private class ElementUpdateListener
      extends
      AbstractAction {

      private static final long serialVersionUID = -293259556758837707L;

      @Override
      public void actionPerformed(final ActionEvent e) {
         buildBundles();
      }
   }

   private class readReferenceListener
      extends
      AbstractAction {

      private static final long serialVersionUID = -70114861736447514L;

      @Override
      public void actionPerformed(final ActionEvent e) {
         final int[] rows = jTable_Refs.getSelectedRows();
         if(rows.length > 0) {
            final SpectrumFileChooser sfc = new SpectrumFileChooser(MakeStandardDialog.this, "Open spectrum files...");
            final File dir = new File(DTSA2.getSpectrumDirectory());
            sfc.getFileChooser().setCurrentDirectory(dir);
            sfc.setLocationRelativeTo(MakeStandardDialog.this);
            sfc.setMultiSelectionEnabled(false);
            final int res = sfc.showOpenDialog();
            if(res == JFileChooser.APPROVE_OPTION) {
               DTSA2.updateSpectrumDirectory(sfc.getFileChooser().getCurrentDirectory());
               final ISpectrumData spec = sfc.getSpectra()[0];
               final Set<Element> specElms = spec.getProperties().getElements();
               if((specElms == null) || specElms.isEmpty()) {
                  final Set<Element> selectedElms = SelectElements.selectElements(MakeStandardDialog.this, "Specify the elements contained in the reference spectrum", Element.allElements(), getElementsForRows(rows));
                  spec.getProperties().setElements(selectedElms);
               }
               spec.getProperties().setDetector(mDetector);
               for(final int row : rows)
                  assignReference((RegionOfInterest) jTable_Refs.getModel().getValueAt(row, 0), spec);
            }
            jTable_Refs.setModel(buildRefTable());
         }
      }
   }

   private Set<Element> getElementsForRows(final int[] rows) {
      final Set<Element> res = new TreeSet<Element>();
      for(final int row : rows)
         res.addAll(((RegionOfInterest) jTable_Refs.getModel().getValueAt(row, 0)).getElementSet());
      return res;

   }

   private class clearReferenceListener
      extends
      AbstractAction {

      private static final long serialVersionUID = 3445601205093883019L;

      @Override
      public void actionPerformed(final ActionEvent e) {
         final int[] rows = jTable_Refs.getSelectedRows();
         for(final int row : rows) {
            final RegionOfInterest roi = (RegionOfInterest) jTable_Refs.getModel().getValueAt(row, 0);
            for(final StandardBundle sb : mBundle.values())
               sb.clearReference(roi);
         }
         jTable_Refs.setModel(buildRefTable());
      }
   }

   private class autoReferenceListener
      extends
      AbstractAction {

      private static final long serialVersionUID = 2394910493261610093L;

      @Override
      public void actionPerformed(final ActionEvent e) {
         try {
            final Composition comp = mMaterial.clone();
            if(jCheckBox_Carbon.isSelected())
               comp.addElement(Element.C, 0.001);
            if(jCheckBox_Oxygen.isSelected())
               comp.addElement(Element.O, 0.001);
            mResult = null;
            final ISpectrumData fitSpec = buildResult();
            final SpectrumFitter8 sf = new SpectrumFitter8(mDetector, comp, fitSpec);
            final RegionOfInterestSet rois = sf.getROIS();
            final SpectrumProperties props = mDetector.getCalibration().getProperties();
            // If there is an extended range of energies with
            // characteristic
            // peaks, we should increase the number of fit parameters.
            final double[] coeffs = new double[] {
               props.getNumericWithDefault(SpectrumProperties.EnergyOffset, 0.0),
               props.getNumericWithDefault(SpectrumProperties.EnergyScale, 10.0)
            };
            sf.setEnergyScale(new AltEnergyScaleFunction(coeffs));
            sf.setResolution(new FanoNoiseWidth(6.0));
            sf.setMultiLineset(sf.buildWeighted(rois));
            {
               final JProgressDialog prg = new JProgressDialog(MakeStandardDialog.this, "Performing auto-reference fit");
               prg.perform(new Runnable() {
                  @Override
                  public void run() {
                     try {
                        prg.setProgress(5);
                        sf.compute();
                        for(int i = 0; i < 4; ++i) {
                           prg.setProgress(20 + (i * 20));
                           sf.recompute(10.0, 0.3);
                        }
                        prg.setProgress(100);
                     }
                     catch(final Exception e) {
                        ErrorDialog.createErrorMessage(MakeStandardDialog.this, "Auto Reference Failed", e);
                     }
                  }
               });
            }
            mBestFit = sf.getBestFit();
            mRough = sf.getRoughFit();
            mResult = fitSpec;
            final BasicSpectrumSimulator bss = new BasicSpectrumSimulator();
            for(final StandardBundle sb : mBundle.values())
               sb.updateStandard(mResult);
            for(int row = 0; row < jTable_Refs.getRowCount(); ++row) {
               final RegionOfInterest roi = (RegionOfInterest) jTable_Refs.getModel().getValueAt(row, 0);
               final Element elm = roi.getElementSet().first();
               final EditableSpectrum ref = new EditableSpectrum(sf.getElementSpectrum(elm));
               final ISpectrumData brem = bss.generateSpectrum(new Composition(elm), fitSpec.getProperties(), Collections.emptySet(), true);
               Math2.addInPlace(ref.getCounts(), Math2.multiply(comp.weightFraction(elm, true), SpectrumUtils.toDoubleArray(brem)));
               ref.getProperties().clear(SpectrumProperties.StandardComposition, SpectrumProperties.MicroanalyticalComposition);
               ref.getProperties().setElements(roi.getElementSet());
               SpectrumUtils.rename(ref, "Auto[" + roi + "]");
               assignReference(roi, SpectrumUtils.copy(ref));
            }
            jTable_Refs.setModel(buildRefTable());
         }
         catch(final Throwable th) {
            ErrorDialog.createErrorMessage(MakeStandardDialog.this, "Error computing best fit.", th);
         }
      }
   }

   public SpectrumProperties editSpectrumProperties(final SpectrumProperties sp) {
      final SpectrumPropertyPanel.PropertyDialog dlg = new SpectrumPropertyPanel.PropertyDialog(this, mSession);
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

   public void addSpectrum(final ISpectrumData spec)
         throws EPQException {
      final SpectrumProperties sp = spec.getProperties();
      if(mDetector == null)
         mDetector = (EDSDetector) sp.getDetector();
      else if(!mDetector.equals(sp.getDetector()))
         throw new EPQException("The detector for " + spec.toString() + " does not match the previous spectra.");
      double e0 = sp.getNumericWithDefault(SpectrumProperties.BeamEnergy, Double.NaN);
      double lt = sp.getNumericWithDefault(SpectrumProperties.LiveTime, Double.NaN);
      double pc = SpectrumUtils.getAverageFaradayCurrent(sp, Double.NaN);
      if(Double.isNaN(lt) || Double.isNaN(pc) || Double.isNaN(e0))
         sp.addAll(editSpectrumProperties(sp));
      e0 = sp.getNumericWithDefault(SpectrumProperties.BeamEnergy, Double.NaN);
      lt = sp.getNumericWithDefault(SpectrumProperties.LiveTime, Double.NaN);
      pc = SpectrumUtils.getAverageFaradayCurrent(sp, Double.NaN);
      if(Double.isNaN(lt) || Double.isNaN(pc) || Double.isNaN(e0))
         throw new EPQException("Please provide the beam energy, live time and probe current for each spectrum.");
      if(mSpectra.size() == 0) {
         mBeamEnergy = e0;
         mProbeDose = lt * pc;
         mThickness = getThickness(sp);
         mSpectra.add(spec);
      } else {
         if(Math.abs(mBeamEnergy - e0) > (0.001 * mBeamEnergy))
            throw new EPQException("The beam energy for " + spec.toString() + " does not match the previous spectra.");
         mProbeDose += lt * pc;
         mSpectra.add(spec);
      }
      if(mMaterial.getElementCount() == 0)
         updateMaterialField(sp.getCompositionWithDefault(SpectrumProperties.StandardComposition, mMaterial));
      else if(!sp.getCompositionWithDefault(SpectrumProperties.StandardComposition, mMaterial).almostEquals(mMaterial, 1.0e-5))
         throw new EPQException("The material associated with " + spec
               + " does \nnot match the material associated with previous spectra.");
      final ConductiveCoating cc = (ConductiveCoating) sp.getObjectWithDefault(SpectrumProperties.ConductiveCoating, null);
      if(cc != null) {
         mCoating = true;
         mCoatingThickness = cc.getThickness();
         mCoatingMaterial = cc.getMaterial();
      } else {
         mCoatingThickness = 0.0;
         mCoatingMaterial = Material.Null;
      }

      jPanel_Element.setAvailableElements(mMaterial.getElementSet());
      update();
   }

   private double getThickness(final SpectrumProperties props) {
      if(props.isDefined(SpectrumProperties.SampleShape)) {
         final SampleShape ss = props.getSampleShapeWithDefault(SpectrumProperties.SampleShape, null);
         if(ss instanceof SampleShape.ThinFilm)
            return ((SampleShape.ThinFilm) ss).getThickness();
      }
      return Double.NaN;
   }

   private void update() {
      if(mSpectra.size() == 1)
         jLabel_Banner.setText("Creating a standard from a single spectrum.");
      else
         jLabel_Banner.setText("Creating a standard from " + Integer.valueOf(mSpectra.size()) + " spectra.");
      updateMaterialField(mMaterial);
      buildBundles();
      final DecimalFormat df = new HalfUpFormat("0.0");
      jTextField_BeamEnergy.setText(df.format(mBeamEnergy));
      jTextField_Dose.setText(df.format(mProbeDose));
      jTextField_Detector.setText(mDetector != null ? mDetector.getName() : "--------");
      jCheckBox_Film.setSelected(mThinFilmStandard);
      updateThicknessCheckBox();
      updateCoatingCheckBox();
      jButton_Save.setEnabled(mMaterial.getElementCount() > 0);
      jTextField_Material.requestFocus();
   }

   protected void buildBundles() {
      final ISpectrumData spec = buildResult();
      try {
         mBundle.clear();
         final Set<Element> selected = jPanel_Element.getSelected();
         for(final Element elm : selected) {
            final Set<Element> strip = new TreeSet<Element>();
            if(jCheckBox_Carbon.isSelected())
               strip.add(Element.C);
            if(jCheckBox_Oxygen.isSelected())
               strip.add(Element.O);
            final StandardBundle sb = new StandardBundle(elm, spec, strip);
            if(jCheckBox_Coating.isSelected() && (mCoatingMaterial != null) && (mCoatingMaterial.getElementCount() > 0)
                  && (mCoatingThickness > 0.0)) {
               final ConductiveCoating cc = new ConductiveCoating(mCoatingMaterial, mCoatingThickness);
               sb.addCoating(cc);
            }
            mBundle.put(elm, sb);
            mROI.addAll(sb.getAllRequiredReferences());
         }
         jTable_Refs.setModel(buildRefTable());
      }
      catch(final EPQException e) {
         jLabel_Banner.setText(e.toString());
      }
   }

   private TableModel buildRefTable() {
      final HashMap<RegionOfInterest, ISpectrumData> rois = getAllROIS();
      final Object[][] data = new Object[rois.size()][2];
      int i = 0;
      for(final RegionOfInterest roi : rois.keySet()) {
         data[i][0] = roi;
         final ISpectrumData spec = rois.get(roi);
         data[i][1] = (spec == null ? "None" : spec.toString());
         ++i;
      }
      final DefaultTableModel res = new DefaultTableModel(data, new String[] {
         "Region-of-Interest",
         "Reference Spectrum"
      });
      return res;
   }

   private void assignReference(final RegionOfInterest roi, final ISpectrumData spec) {
      for(final StandardBundle sb : mBundle.values()) {
         final Set<RegionOfInterest> reqRefs = sb.getAllRequiredReferences();
         if(reqRefs.contains(roi))
            try {
               sb.addReference(roi, spec);
            }
            catch(final EPQException e) {
               ErrorDialog.createErrorMessage(this, "Reference error", e);
            }
      }
      jTable_Refs.setModel(buildRefTable());

   }

   protected HashMap<RegionOfInterest, ISpectrumData> getAllROIS() {
      final HashMap<RegionOfInterest, ISpectrumData> rois = new HashMap<RegionOfInterest, ISpectrumData>();
      for(final StandardBundle sb : mBundle.values()) {
         final Set<RegionOfInterest> reqRefs = sb.getAllRequiredReferences();
         for(final RegionOfInterest roi : reqRefs)
            if(!rois.containsKey(roi))
               rois.put(roi, sb.getReference(roi));
            else if(rois.get(roi) == null)
               rois.put(roi, sb.getReference(roi));
      }
      return rois;
   }

   public void clearSpectra() {
      mBeamEnergy = Double.NaN;
      mProbeDose = 0.0;
      mSpectra.clear();
   }

   public ISpectrumData getResult() {
      mResult = buildResult();
      return mResult;
   }

   private ISpectrumData buildResult() {
      ISpectrumData res = mResult;
      if(res == null)
         if((mSpectra.size() > 0) && (mMaterial.getElementCount() > 0)) {
            SpectrumMath sm = null;
            double lt = 0.0, rt = 0.0;
            Date last = null;
            for(final ISpectrumData spec : mSpectra) {
               lt += spec.getProperties().getNumericWithDefault(SpectrumProperties.LiveTime, 0.0);
               rt += spec.getProperties().getNumericWithDefault(SpectrumProperties.RealTime, 0.0);
               final Date dt = spec.getProperties().getTimestampWithDefault(SpectrumProperties.AcquisitionTime, null);
               if(dt != null)
                  if(last == null)
                     last = dt;
                  else if(dt.after(last))
                     last = dt;
               if(sm == null)
                  sm = new SpectrumMath(spec);
               else
                  sm.add(spec, 1.0);
            }
            // Set the probe current to the average but keep the dose correct
            final SpectrumProperties sp = sm.getProperties();
            sp.setDetector(mDetector);
            if(last != null)
               sp.setTimestampProperty(SpectrumProperties.AcquisitionTime, last);
            sp.setNumericProperty(SpectrumProperties.BeamEnergy, mBeamEnergy);
            sp.setCompositionProperty(SpectrumProperties.StandardComposition, mMaterial);
            sp.setNumericProperty(SpectrumProperties.FaradayBegin, mProbeDose / lt);
            sp.setNumericProperty(SpectrumProperties.FaradayEnd, mProbeDose / lt);
            sp.setNumericProperty(SpectrumProperties.LiveTime, lt);
            sp.setNumericProperty(SpectrumProperties.RealTime, rt);
            if(mThinFilmStandard && (!Double.isNaN(mThickness))) {
               final ThinFilm tf = new ThinFilm(Math2.MINUS_Z_AXIS, mThickness);
               sp.setSampleShape(SpectrumProperties.SampleShape, tf);
               if(mMaterial instanceof Material)
                  sp.setNumericProperty(SpectrumProperties.MassThickness, FromSI.cm(mThickness)
                        * FromSI.gPerCC(((Material) mMaterial).getDensity()) * 1.0e6); // &mu;g/cm<sup>2</sup>
            } else
               sp.setSampleShape(SpectrumProperties.SampleShape, new SampleShape.Bulk());
            if(jCheckBox_Coating.isSelected()) {
               if((mCoatingMaterial != null) && (!mCoatingMaterial.equals(Material.Null)) && (mCoatingThickness > 0.0))
                  sp.setConductiveCoating(new ConductiveCoating(mCoatingMaterial, mCoatingThickness));
               else
                  sp.remove(SpectrumProperties.ConductiveCoating);
            } else
               sp.remove(SpectrumProperties.ConductiveCoating);
            SpectrumUtils.rename(sm, mMaterial.getName() + " std");
            res = SpectrumUtils.copy(sm);
         }
      return res;
   }

   public List<ISpectrumData> getSpectra() {
      return Collections.unmodifiableList(mSpectra);
   }

   public ISpectrumData getBestFit() {
      return mBestFit;
   }

   public boolean shouldSave() {
      return mSave;
   }

   /**
    * Returns a map assigning StandardBundle objects to elements.
    *
    * @return Returns the bundle.
    */
   public Map<Element, StandardBundle> getBundle() {
      return mThinFilmStandard ? Collections.emptyMap() : Collections.unmodifiableMap(mBundle);
   }

   /**
    * Gets the current value assigned to rough
    *
    * @return Returns the rough.
    */
   public ISpectrumData getRough() {
      return mRough;
   }
}
