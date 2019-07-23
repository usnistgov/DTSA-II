package gov.nist.microanalysis.dtsa2;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.html.HTMLDocument;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import gov.nist.microanalysis.EPQDatabase.ReferenceDatabase;
import gov.nist.microanalysis.EPQDatabase.Session;
import gov.nist.microanalysis.EPQLibrary.Composition;
import gov.nist.microanalysis.EPQLibrary.CompositionFromKRatios;
import gov.nist.microanalysis.EPQLibrary.CompositionFromKRatios.ElementByDifference;
import gov.nist.microanalysis.EPQLibrary.CompositionFromKRatios.OxygenByStoichiometry;
import gov.nist.microanalysis.EPQLibrary.CompositionFromKRatios.UnmeasuredElementRule;
import gov.nist.microanalysis.EPQLibrary.CompositionFromKRatios.WatersOfCrystallization;
import gov.nist.microanalysis.EPQLibrary.CompositionOptimizer;
import gov.nist.microanalysis.EPQLibrary.EPMAOptimizer.OptimizedStandard;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.Element;
import gov.nist.microanalysis.EPQLibrary.FromSI;
import gov.nist.microanalysis.EPQLibrary.ISpectrumData;
import gov.nist.microanalysis.EPQLibrary.Material;
import gov.nist.microanalysis.EPQLibrary.MaterialFactory;
import gov.nist.microanalysis.EPQLibrary.QuantificationOptimizer;
import gov.nist.microanalysis.EPQLibrary.QuantificationOptimizer2;
import gov.nist.microanalysis.EPQLibrary.QuantificationOutline;
import gov.nist.microanalysis.EPQLibrary.QuantificationOutline.ReferenceMaterial;
import gov.nist.microanalysis.EPQLibrary.QuantificationPlan;
import gov.nist.microanalysis.EPQLibrary.QuantificationPlan.Acquisition;
import gov.nist.microanalysis.EPQLibrary.QuantifyUsingStandards;
import gov.nist.microanalysis.EPQLibrary.RegionOfInterestSet;
import gov.nist.microanalysis.EPQLibrary.RegionOfInterestSet.RegionOfInterest;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;
import gov.nist.microanalysis.EPQLibrary.SpectrumSimulator;
import gov.nist.microanalysis.EPQLibrary.SpectrumUtils;
import gov.nist.microanalysis.EPQLibrary.StandardsDatabase2;
import gov.nist.microanalysis.EPQLibrary.StandardsDatabase2.StandardBlock2;
import gov.nist.microanalysis.EPQLibrary.ToSI;
import gov.nist.microanalysis.EPQLibrary.Detector.DetectorCalibration;
import gov.nist.microanalysis.EPQLibrary.Detector.DetectorProperties;
import gov.nist.microanalysis.EPQLibrary.Detector.EDSCalibration;
import gov.nist.microanalysis.EPQLibrary.Detector.EDSDetector;
import gov.nist.microanalysis.EPQLibrary.Detector.ElectronProbe;
import gov.nist.microanalysis.EPQTools.CompositionTableModel;
import gov.nist.microanalysis.EPQTools.EPQXStream;
import gov.nist.microanalysis.EPQTools.JStoichiometryTable;
import gov.nist.microanalysis.EPQTools.JTextFieldDouble;
import gov.nist.microanalysis.EPQTools.JWizardDialog;
import gov.nist.microanalysis.EPQTools.MajorMinorTraceEditor;
import gov.nist.microanalysis.EPQTools.MaterialsCreator;
import gov.nist.microanalysis.EPQTools.SelectElements;
import gov.nist.microanalysis.Utility.EachRowEditor;
import gov.nist.microanalysis.Utility.HalfUpFormat;
import gov.nist.microanalysis.Utility.PrintUtilities;
import gov.nist.microanalysis.Utility.TextUtilities;
import gov.nist.microanalysis.Utility.UncertainValue2;

/**
 * <p>
 * Description: A wizard for optimizing x-ray microanalysis measurements. The
 * output from this wizard is an experiment plan which suggests
 * </p>
 * <ol>
 * <li>A beam energy (keV)</li>
 * <li>A standard for each element in the material</li>
 * <li>Optimal x-ray transition for quantification</li>
 * <li>A suggested list of standard blocks</li>
 * <li>(EDS) References for each standard requiring a reference</li>
 * <li>(EDS) Suggested dose (nA s)</li>
 * <li>(EDS) A simulated spectrum showing</li>
 * <li>(WDS) Suggested crystal and approximate position</li>
 * </ol>
 * <p>
 * The input for the wizard is an estimate of the composition of the material.
 * The better the initial estimate, the better the tool is able to optimize the
 * measurement. However, a good guess is often sufficient for excellent results.
 * <p>
 * Copyright: Pursuant to title 17 Section 105 of the United States Code this
 * software is not subject to copyright protection and is in the public domain
 * </p>
 * <p>
 * Institution: National Institute of Standards and Technology
 * </p>
 * 
 * @author Nicholas
 * @version 1.0
 */
public class OptimizationWizard
   extends
   JWizardDialog {

   public class IntroductionPanel
      extends
      JWizardPanel {

      private static final long serialVersionUID = 5292911814090992349L;

      JRadioButton jRadioButton_OptimizeExp = new JRadioButton("Optimize an EDS measurement (expert mode)");
      JRadioButton jRadioButton_OptimizeEDS = new JRadioButton("Optimize an EDS measurement");
      JRadioButton jRadioButton_OptimizeWDS = new JRadioButton("Optimize a WDS measurement");
      JRadioButton jRadioButton_OptimizeMixed = new JRadioButton("Optimize a WDS+EDS experiment");
      JRadioButton jRadioButton_AssignReferences = new JRadioButton("Assign elemental references");
      JRadioButton jRadioButton_FindStandards = new JRadioButton("Find standards");
      ButtonGroup buttonGroup_RB = new ButtonGroup();

      public IntroductionPanel() {
         super(OptimizationWizard.this);
         this.setName("Welcome");
         jRadioButton_OptimizeEDS.setToolTipText("Optimize a measurement made with an energy dispersive spectrometer.");
         jRadioButton_OptimizeExp.setToolTipText("Optimize a measurement made with an energy dispersive spectrometer. Manually select standards.");
         jRadioButton_OptimizeWDS.setToolTipText("Optimize a measurement made with a wavelength dispersive spectrometer.");
         jRadioButton_OptimizeMixed.setToolTipText("Optimize a measurement using both wavelength and energy dispersive spectrometers.");
         jRadioButton_AssignReferences.setToolTipText("Assign the default material to use as a reference for the specified element.");
         jRadioButton_FindStandards.setToolTipText("Find the standard blocks in which a specified list of materials are found.");
         buttonGroup_RB.add(jRadioButton_OptimizeExp);
         buttonGroup_RB.add(jRadioButton_OptimizeEDS);
         buttonGroup_RB.add(jRadioButton_OptimizeWDS);
         buttonGroup_RB.add(jRadioButton_OptimizeMixed);
         buttonGroup_RB.add(jRadioButton_AssignReferences);
         buttonGroup_RB.add(jRadioButton_FindStandards);
         jRadioButton_OptimizeExp.setSelected(true);
         init();
      }

      private void init() {
         final FormLayout fl = new FormLayout("pref", "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref");
         this.setLayout(fl);
         final CellConstraints cc = new CellConstraints();
         jRadioButton_OptimizeEDS.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
               setNextPanel(jWizardPanel_EstComposition, "Estimate material");
               mMode = Mode.OptimizeEDS;
            }
         });

         jRadioButton_OptimizeExp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
               setNextPanel(jWizardPanel_Instrument, "Specify a detector");
               mMode = Mode.OptimizeExpert;
            }
         });

         jRadioButton_OptimizeWDS.setEnabled(false);
         jRadioButton_OptimizeMixed.setEnabled(false);
         jRadioButton_FindStandards.setEnabled(true);
         jRadioButton_FindStandards.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
               setNextPanel(jWizardPanel_SelectMaterials, "Specify materials");
               mMode = Mode.FindStandards;
            }
         });
         add(jRadioButton_OptimizeExp, cc.xy(1, 1));
         add(jRadioButton_OptimizeEDS, cc.xy(1, 3));
         add(jRadioButton_OptimizeWDS, cc.xy(1, 5));
         add(jRadioButton_OptimizeMixed, cc.xy(1, 7));
         add(jRadioButton_FindStandards, cc.xy(1, 9));
      }
   }

   /**
    * Allows the user to specify an Instrument, Detector/Calibration and beam
    * energy for the unknown spectrum.
    */
   public class InstrumentDetectorPanel
      extends
      JWizardPanel {

      private final JComboBox<ElectronProbe> jComboBox_Instrument = new JComboBox<ElectronProbe>();
      private final JComboBox<DetectorProperties> jComboBox_Detector = new JComboBox<DetectorProperties>();
      private final JComboBox<EDSCalibration> jComboBox_Calibration = new JComboBox<EDSCalibration>();
      private final JTextFieldDouble jTextField_BeamEnergy = new JTextFieldDouble();

      private static final long serialVersionUID = -6481374858932055638L;
      private boolean mFirstShow = true;

      public InstrumentDetectorPanel() {
         super(OptimizationWizard.this);
         try {
            initialize();
         }
         catch(final Exception ex) {
            ex.printStackTrace();
         }
      }

      private void initialize() {
         final FormLayout fl = new FormLayout("10dlu, right:pref, 3dlu, 40dlu, 3dlu, 80dlu, 3dlu, pref", "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref");
         final PanelBuilder pb = new PanelBuilder(fl, this);
         final CellConstraints cc = new CellConstraints();
         pb.addSeparator("Instrument", cc.xyw(1, 1, 8));
         pb.addLabel("Acquired on the", cc.xy(2, 3));
         pb.add(jComboBox_Instrument, cc.xyw(4, 3, 3));
         pb.addSeparator("Detector", cc.xyw(1, 5, 8));
         pb.addLabel("using the", cc.xy(2, 7));
         pb.add(jComboBox_Detector, cc.xyw(4, 7, 3));
         pb.addLabel("with calibration", cc.xy(2, 9));
         pb.add(jComboBox_Calibration, cc.xyw(4, 9, 3));

         pb.addSeparator("Instrument parameters", cc.xyw(1, 11, 8));
         pb.addLabel("Beam energy", cc.xy(2, 13));
         pb.add(jTextField_BeamEnergy, cc.xyw(4, 13, 3));

         mFirstShow = true;
         getWizard().enableFinish(false);
      }

      @Override
      public void onShow() {
         if(mFirstShow) {
            DetectorProperties defProps = AppPreferences.getInstance().getDefaultDetector();
            DetectorCalibration defCal = null;
            if(defProps != null) {
               defCal = mSession.getMostRecentCalibration(defProps);
               for(final ISpectrumData spec : DataManager.getInstance().getSelected())
                  if(spec.getProperties().getDetector() instanceof EDSDetector) {
                     final EDSDetector det = (EDSDetector) spec.getProperties().getDetector();
                     defProps = det.getDetectorProperties();
                     defCal = det.getCalibration();
                     break;
                  }
            }
            {
               final Set<ElectronProbe> eps = mSession.getCurrentProbes();
               final DefaultComboBoxModel<ElectronProbe> dcmb = new DefaultComboBoxModel<ElectronProbe>();
               for(final ElectronProbe pr : eps)
                  dcmb.addElement(pr);
               dcmb.setSelectedItem(defProps != null ? defProps.getOwner() : eps.iterator().next());
               jComboBox_Instrument.setModel(dcmb);
               updateDetectors(defProps);
               updateCalibrations(defCal);
               jComboBox_Instrument.addActionListener(new AbstractAction() {
                  private static final long serialVersionUID = -51520409728313274L;

                  @Override
                  public void actionPerformed(final ActionEvent e) {
                     updateDetectors(null);
                  }
               });

               jComboBox_Detector.addActionListener(new ActionListener() {
                  @Override
                  public void actionPerformed(final ActionEvent e) {
                     updateCalibrations(null);
                  }
               });
            }
            mFirstShow = false;
         }

         if(mMode.equals(Mode.OptimizeEDS))
            getWizard().setNextPanel(jWizardPanel_EstComposition, "Specify standard spectra");
         else if(mMode.equals(Mode.OptimizeExpert))
            getWizard().setNextPanel(jWizardPanel_SelectStdExp, "Select standards");
      }

      @Override
      public boolean permitNext() {
         final ElectronProbe probe = (ElectronProbe) jComboBox_Instrument.getSelectedItem();
         final DetectorProperties props = (DetectorProperties) jComboBox_Detector.getSelectedItem();
         final EDSCalibration calib = (EDSCalibration) jComboBox_Calibration.getSelectedItem();
         final EDSDetector det = EDSDetector.createDetector(props, calib);
         final double beamEnergy = jTextField_BeamEnergy.getValue();
         assert det.getOwner() == probe;
         final boolean res = (probe != null);
         if(res == false)
            getWizard().setErrorText("Please specify an instrument and detector.");
         if(res) {
            mDetector = det;
            mBeamEnergy = ToSI.keV(beamEnergy);
         }
         return res;
      }

      private void updateDetectors(final DetectorProperties defDp) {
         final ElectronProbe newProbe = (ElectronProbe) jComboBox_Instrument.getSelectedItem();
         if(newProbe != null) {
            final DefaultComboBoxModel<DetectorProperties> dcmb = new DefaultComboBoxModel<DetectorProperties>();
            jTextField_BeamEnergy.initialize(15.0, FromSI.keV(newProbe.getMinBeamEnergy()), FromSI.keV(newProbe.getMaxBeamEnergy()));
            for(final DetectorProperties dp : mSession.getDetectors(newProbe))
               dcmb.addElement(dp);
            dcmb.setSelectedItem(defDp != null ? defDp : (dcmb.getSize() > 0 ? dcmb.getElementAt(0) : null));
            jComboBox_Detector.setModel(dcmb);
            updateCalibrations(null);
         }
      }

      private void updateCalibrations(final DetectorCalibration defCal) {
         final DetectorProperties newDet = (DetectorProperties) jComboBox_Detector.getSelectedItem();
         final DefaultComboBoxModel<EDSCalibration> dcmb = new DefaultComboBoxModel<EDSCalibration>();
         if(newDet != null) {
            for(final DetectorCalibration dc : mSession.getCalibrations(newDet))
               if(dc instanceof EDSCalibration)
                  dcmb.addElement((EDSCalibration) dc);
            dcmb.setSelectedItem(defCal != null ? defCal : (dcmb.getSize() > 0 ? dcmb.getElementAt(0) : null));
         }
         jComboBox_Calibration.setModel(dcmb);
      }
   }

   private class EstimateCompositionPanel
      extends
      JWizardPanel {

      private static final double TRACE = 0.001;
      private static final long serialVersionUID = -6177624872137694722L;
      private final JTable jTable_EstComp = new JTable();
      private final JButton jButton_Comp = new JButton("Edit");
      private final JButton jButton_Trace = new JButton("Trace");
      private JLabel jLabel_Detector = null;

      public EstimateCompositionPanel() {
         super(OptimizationWizard.this);
         init();
      }

      private void init() {
         jButton_Comp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
               editCompositionAction();
            }
         });
         jButton_Trace.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
               addTraceAction();
            }
         });
         jTable_EstComp.setToolTipText("<html>The estimated composition is used as a starting point for<br>"
               + "the optimization process. The better the estimate the more<br>"
               + "reliable the optimization.  However, a very good optimization<br>"
               + "can be performed with a crude estimate.  (This might actually<br>"
               + "be a suitable use for a standardless quantification.)");
         final FormLayout fl = new FormLayout("10dlu, 150dlu, 5dlu, pref, 10dlu", "pref, 5dlu, 75dlu, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref");
         final PanelBuilder pb = new PanelBuilder(fl);
         final CellConstraints cc = new CellConstraints();
         pb.addSeparator("Estimated composition", cc.xyw(1, 1, 5));
         pb.add(new JScrollPane(jTable_EstComp), cc.xy(2, 3));
         final PanelBuilder buttons = new PanelBuilder(new FormLayout("pref", "pref, 5dlu, pref"));
         buttons.add(jButton_Comp, cc.xy(1, 1));
         buttons.add(jButton_Trace, cc.xy(1, 3));
         pb.add(buttons.getPanel(), cc.xy(4, 3));
         pb.addSeparator("Detector", cc.xyw(1, 5, 5));
         jLabel_Detector = pb.addROLabel("Unknown", cc.xyw(2, 7, 4));
         this.add(pb.getPanel());
      }

      void update() {
         final StringBuffer ds = new StringBuffer();
         ds.append(mDetector != null ? mDetector.toString() : "Unknown");
         ds.append(" on ");
         ds.append((mDetector != null) && (mDetector.getOwner() != null) ? mDetector.getOwner().toString() : "unknown");
         jLabel_Detector.setText(ds.toString());
         if(mEstComposition != null) {
            final DefaultTableModel dfm = new DefaultTableModel(new Object[] {
               "Element",
               "Mass Fraction"
            }, 0) {
               private static final long serialVersionUID = 1L;

               @Override
               public boolean isCellEditable(final int r, final int c) {
                  return false;
               }
            };
            final HalfUpFormat df = new HalfUpFormat("0.00000");
            for(final Element elm : mEstComposition.getElementSet())
               dfm.addRow(new Object[] {
                  elm,
                  df.format(mEstComposition.weightFraction(elm, false))
               });
            jTable_EstComp.setModel(dfm);
         }
      }

      private void editCompositionAction() {
         final MaterialsCreator mc = new MaterialsCreator(OptimizationWizard.this, "Estimate composition", true);
         mc.setMaterial(mEstComposition);
         mc.setSession(mSession);
         mc.setInhibitUpdate();
         mc.setRequireDensity(false);
         mc.setLocationRelativeTo(OptimizationWizard.this);
         mc.setModal(true);
         mc.setVisible(true);
         updateComposition(mc.getMaterial());
         if(mEstComposition != null)
            update();
      }

      private void addTraceAction() {
         final SelectElements se = new SelectElements(OptimizationWizard.this, "Select the trace elements");
         se.enableAll(false);
         se.enableElements(Element.H, Element.Am, true);
         for(final Element elm : mEstComposition.getElementSet())
            se.setEnabled(elm, false);
         se.setVisible(true);
         final Set<Element> elms = se.getElements();
         if(elms.size() > 0) {
            final double norm = mEstComposition.sumWeightFraction() - (TRACE * elms.size());
            final Composition newComp = new Composition();
            for(final Element elm : mEstComposition.getElementSet())
               newComp.addElement(elm, mEstComposition.weightFraction(elm, false) / norm);
            for(final Element elm : elms)
               newComp.addElement(elm, TRACE / norm);
            updateComposition(newComp);
            update();
         }
      }

      @Override
      public boolean permitNext() {
         final boolean res = (mEstComposition != null) && (mEstComposition.getElementCount() > 1);
         mOptimizer = res ? new CompositionOptimizer(mDetector, mEstComposition, mStandards) : null;
         return mOptimizer != null;
      }

      @Override
      public void onShow() {
         update();
         setNextPanel(jWizardPanel_Blocks, "Select standard blocks");
      }
   };

   class CheckListItem {
      private final String mLabel;
      private boolean mIsSelected = false;

      public CheckListItem(final String label) {
         this.mLabel = label;
      }

      public boolean isSelected() {
         return mIsSelected;
      }

      public void setSelected(final boolean isSelected) {
         this.mIsSelected = isSelected;
      }

      @Override
      public String toString() {
         return mLabel;
      }
   }

   class CheckListRenderer
      extends
      JCheckBox
      implements
      ListCellRenderer<CheckListItem> {

      private static final long serialVersionUID = -2772183311699556951L;

      @Override
      public Component getListCellRendererComponent(final JList<? extends CheckListItem> list, final CheckListItem value, final int index, final boolean isSelected, final boolean hasFocus) {
         setEnabled(list.isEnabled());
         setSelected(value.isSelected());
         setFont(list.getFont());
         setBackground(list.getBackground());
         setForeground(list.getForeground());
         setText(value.toString());
         return this;
      }
   }

   private class SelectBlocksPanel
      extends
      JWizardPanel {

      private static final long serialVersionUID = 8738771800233144836L;
      private final JList<CheckListItem> jList_Standards = new JList<CheckListItem>();
      private final JButton jButton_All = new JButton("Select all");
      private final JButton jButton_Invert = new JButton("Invert selection");

      private SelectBlocksPanel() {
         super(OptimizationWizard.this);
         init();
      }

      private void init() {
         jList_Standards.setCellRenderer(new CheckListRenderer());
         jList_Standards.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         jList_Standards.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent event) {
               @SuppressWarnings("unchecked")
               final JList<CheckListItem> list = (JList<CheckListItem>) event.getSource();
               // Get index of item clicked
               final int index = list.locationToIndex(event.getPoint());
               final CheckListItem item = list.getModel().getElementAt(index);
               // Toggle selected state
               item.setSelected(!item.isSelected());
               // Repaint cell
               list.repaint(list.getCellBounds(index, index));
            }
         });
         jButton_All.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
               final DefaultListModel<CheckListItem> dlm = (DefaultListModel<CheckListItem>) jList_Standards.getModel();
               for(int i = 0; i < dlm.getSize(); ++i) {
                  final CheckListItem cli = dlm.get(i);
                  cli.setSelected(true);
               }
               jList_Standards.repaint();
            }
         });

         jButton_Invert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
               final DefaultListModel<CheckListItem> dlm = (DefaultListModel<CheckListItem>) jList_Standards.getModel();
               for(int i = 0; i < dlm.getSize(); ++i) {
                  final CheckListItem cli = dlm.get(i);
                  cli.setSelected(!cli.isSelected());
               }
               jList_Standards.repaint();
            }
         });

         final FormLayout fl = new FormLayout("170dlu, 5dlu, pref", "pref, 5dlu, pref, 100dlu");
         setLayout(fl);
         add(new JScrollPane(jList_Standards), CC.xywh(1, 1, 1, 4));
         add(jButton_All, CC.xy(3, 1));
         add(jButton_Invert, CC.xy(3, 3));
      }

      @Override
      public void onShow() {
         final DefaultListModel<CheckListItem> dlm = new DefaultListModel<CheckListItem>();
         if(mStandards != null)
            for(final String bn : mStandards.getBlockNames()) {
               final CheckListItem cli = new CheckListItem(bn);
               cli.setSelected(true);
               dlm.addElement(cli);
            }
         jList_Standards.setModel(dlm);
         setNextPanel(jWizardPanel_SelectStandard, "Select standards.");
      }

      @Override
      public boolean permitNext() {
         final ArrayList<StandardBlock2> blks = new ArrayList<StandardBlock2>();
         final DefaultListModel<CheckListItem> dlm = (DefaultListModel<CheckListItem>) jList_Standards.getModel();
         for(int i = 0; i < dlm.size(); ++i) {
            final CheckListItem cli = dlm.get(i);
            if(!cli.isSelected())
               blks.add(mStandards.getBlock(cli.toString()));
         }
         mOptimizer.setExclusionList(blks);
         return true;
      }

   };

   // Select the material, the ROI, the beam energy
   private class SelectStandardPanel
      extends
      JWizardPanel {

      private static final long serialVersionUID = -2247781803732630782L;

      private final JComboBox<BeamEnergy> jComboBox_BeamEnergy = new JComboBox<BeamEnergy>();
      // Columns are element, ROI, material

      private static final int ELEMENT_COL = 0;
      // private static final int ROI_COL = 1;
      private static final int MATERIAL_COL = 1;
      private static final int SCORE_COL = 2;

      private final JTable jTable_Standard = new JTable() {
         private static final long serialVersionUID = 221703199102488115L;

         @Override
         public String getToolTipText(final MouseEvent e) {
            String tip = null;
            final int rowIndex = rowAtPoint(e.getPoint());
            for(final Map.Entry<JComboBox<OptimizedStandard>, Integer> me : mToRow.entrySet())
               if(me.getValue().intValue() == rowIndex) {
                  final Object obj = me.getKey().getSelectedItem();
                  if(obj instanceof OptimizedStandard) {
                     final OptimizedStandard os = (OptimizedStandard) obj;
                     tip = os.toolTipText();
                     break;
                  }
               }
            return tip;
         }
      };
      private final EachRowEditor jEachRowEditor_Material = new EachRowEditor(jTable_Standard);
      private final Map<JComboBox<OptimizedStandard>, Integer> mToRow = new HashMap<JComboBox<OptimizedStandard>, Integer>();

      private DefaultTableModel mTableModel;

      private List<Element> mElements;

      public SelectStandardPanel() {
         super(OptimizationWizard.this);
         init();
      }

      private void init() {
         final CellConstraints cc = new CellConstraints();
         setLayout(new FormLayout("pref, 5dlu, 200dlu", "pref, 5dlu, 100dlu"));
         add(new JLabel("Beam energy"), cc.xy(1, 1));
         add(jComboBox_BeamEnergy, cc.xy(3, 1));
         add(new JScrollPane(jTable_Standard), cc.xyw(1, 3, 3));

         jComboBox_BeamEnergy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
               updateTable();
            }
         });
      }

      private class BeamEnergy
         implements
         Comparable<BeamEnergy> {

         private final double mValue;

         public BeamEnergy(final double val) {
            mValue = val;
         }

         public double getValue() {
            return mValue;
         }

         @Override
         public String toString() {
            final NumberFormat nf = new HalfUpFormat("0.0 keV");
            return nf.format(FromSI.keV(mValue));
         }

         @Override
         public int compareTo(final BeamEnergy o) {
            return Double.compare(mValue, o.mValue);
         }
      }

      public double getBeamEnergy() {
         return ((BeamEnergy) (jComboBox_BeamEnergy.getSelectedItem())).getValue();
      }

      @Override
      public void onShow() {
         updateBeamEnergy();
         updateTable();
         setNextPanel(jWizardPanel_SelectReference, "Select references");
         enableFinish(false);
      }

      private void updateBeamEnergy() {
         final List<Double> energies = mOptimizer.suggestBeamEnergies(true);
         final DefaultComboBoxModel<BeamEnergy> dcbm = new DefaultComboBoxModel<BeamEnergy>();
         for(final double energy : energies)
            dcbm.addElement(new BeamEnergy(energy));
         jComboBox_BeamEnergy.setModel(dcbm);
         jComboBox_BeamEnergy.setSelectedIndex(0);
      }

      private void fillTable() {
         final double beamEnergy = getBeamEnergy();
         mElements = new ArrayList<Element>(mEstComposition.getElementSet());
         for(final Element elm : mElements)
            try {
               final List<OptimizedStandard> stds = mOptimizer.getOptimizedStandards(elm, beamEnergy, 60.0e-9);
               process(elm, stds);
            }
            catch(final EPQException e) {
               e.printStackTrace();
            }
      }

      private void process(final Element elm, final List<OptimizedStandard> stds)
            throws EPQException {
         OptimizedStandard os;
         final NumberFormat nf = new HalfUpFormat("0.0");
         final int row = mElements.indexOf(elm);
         if(stds.size() > 0) {
            os = stds.get(0);
            mTableModel.addRow(new Object[] {
               elm,
               os,
               nf.format(os.getScore())
            });
            mOptimizer.assignStandard(elm, os);
            final JComboBox<OptimizedStandard> ecb = new JComboBox<OptimizedStandard>();
            for(final OptimizedStandard std : stds)
               ecb.addItem(std);
            ecb.addActionListener(new ActionListener() {
               @Override
               public void actionPerformed(final ActionEvent e) {
                  @SuppressWarnings("unchecked")
                  final JComboBox<OptimizedStandard> jcb = (JComboBox<OptimizedStandard>) e.getSource();
                  final int r = mToRow.get(jcb);
                  final OptimizedStandard os = (OptimizedStandard) jcb.getSelectedItem();
                  try {
                     mOptimizer.assignStandard(os.getElement(), os);
                     final NumberFormat nf = new HalfUpFormat("0.0");
                     jTable_Standard.setValueAt(nf.format(os.getScore()), r, SCORE_COL);
                     jcb.setToolTipText(os.toolTipText());
                  }
                  catch(final EPQException e1) {
                     e1.printStackTrace();
                  }
               }
            });
            ecb.setToolTipText(os.toolTipText());
            jEachRowEditor_Material.setEditorAt(row, new DefaultCellEditor(ecb));
            mToRow.put(ecb, row);
         } else
            mTableModel.addRow(new Object[] {
               elm,
               "No suitable standard",
               "~"
            });
      }

      private void updateTable() {
         mTableModel = new DefaultTableModel(new Object[] {
            "Element",
            "Standard",
            "Score"
         }, 0);
         fillTable();
         jTable_Standard.setModel(mTableModel);
         jTable_Standard.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         final TableColumnModel cm = jTable_Standard.getColumnModel();
         final int total = cm.getTotalColumnWidth();
         cm.getColumn(MATERIAL_COL).setCellEditor(jEachRowEditor_Material);
         cm.getColumn(ELEMENT_COL).setPreferredWidth((1 * total) / 10);
         cm.getColumn(MATERIAL_COL).setPreferredWidth((8 * total) / 10);
         cm.getColumn(SCORE_COL).setPreferredWidth(total - ((9 * total) / 10));
      }
   }

   private class SelectReferencePanel
      extends
      JWizardPanel {

      private static final long serialVersionUID = 464249581199062088L;

      static final int ROI_COL = 0;
      static final int MATERIAL_COL = 1;
      static final int MASS_FRAC_COL = 2;
      static final int OPTIONAL_COL = 3; // OPTIONAL / REQUIRED
      static final int STANDARD_COL = 4; // ALSO USED AS STANDARD?

      private final JTable jTable_References = new JTable();
      private final EachRowEditor jEditor_Material = new EachRowEditor(jTable_References);
      private final Map<JComboBox<Composition>, Integer> mToRow = new HashMap<JComboBox<Composition>, Integer>();

      public SelectReferencePanel() {
         super(OptimizationWizard.this);
         init();
      }

      private void init() {
         setLayout(new FormLayout("250dlu", "140dlu"));
         add(new JScrollPane(jTable_References), CC.xy(1, 1));
      }

      private void updateTable() {
         final DefaultTableModel dtm = new DefaultTableModel(new Object[] {
            "Region-of-Interest",
            "Material",
            "K-ratio",
            "Required?",
            "Standard?"
         }, 0);
         final NumberFormat nf = new HalfUpFormat("0.000");
         int row = 0;
         for(final RegionOfInterest roi : mOptimizer.getAllReferences()) {
            final JComboBox<Composition> jcb = new JComboBox<Composition>();
            mToRow.put(jcb, row);
            Composition mat = mOptimizer.getReference(roi);
            if(mat == null)
               mat = Material.Null;
            try {
               for(final Composition comp : mOptimizer.suggestReferences(roi)) {
                  if(mat == Material.Null)
                     mat = comp;
                  jcb.addItem(comp);
               }
               jEditor_Material.setEditorAt(row, new DefaultCellEditor(jcb));
               jcb.addActionListener(new ActionListener() {
                  @Override
                  public void actionPerformed(final ActionEvent e) {
                     @SuppressWarnings("unchecked")
                     final JComboBox<Composition> jcb = (JComboBox<Composition>) e.getSource();
                     final Composition comp = (Composition) jcb.getSelectedItem();
                     try {
                        final NumberFormat nf = new HalfUpFormat("0.000");
                        assert mToRow.containsKey(jcb);
                        final RegionOfInterest roi = (RegionOfInterest) jTable_References.getValueAt(mToRow.get(jcb), ROI_COL);
                        mOptimizer.assignReference(roi, comp);
                        final int row = mToRow.get(jcb);
                        jTable_References.setValueAt(nf.format(comp.weightFraction(roi.getElementSet().first(), false)), row, MASS_FRAC_COL);
                        jTable_References.setValueAt(mOptimizer.isRequired(roi) ? "Required" : "Optional", row, OPTIONAL_COL);
                        jTable_References.setValueAt(mOptimizer.isStandard(comp) ? "Yes" : "No", row, STANDARD_COL);
                     }
                     catch(final EPQException e1) {
                        e1.printStackTrace();
                     }
                  }
               });
            }
            catch(final EPQException e) {
               e.printStackTrace();
            }
            if(mat != null)
               dtm.addRow(new Object[] {
                  roi,
                  mat,
                  nf.format(mat.weightFraction(roi.getElementSet().first(), false)),
                  mOptimizer.isRequired(roi) ? "Required" : "Optional",
                  mOptimizer.isStandard(mat) ? "Yes" : "No"
               });
            try {
               if(mat != null)
                  mOptimizer.assignReference(roi, mat);
            }
            catch(final EPQException e1) {
               e1.printStackTrace();
            }
            ++row;
         }
         jTable_References.setModel(dtm);
         final TableColumnModel cm = jTable_References.getColumnModel();
         cm.getColumn(MATERIAL_COL).setCellEditor(jEditor_Material);
      }

      @Override
      public void onShow() {
         updateTable();
         setNextPanel(jWizardPanel_Results, "Summary results");
         enableFinish(false);
      }
   };

   private class SelectMaterialsPanel
      extends
      JWizardPanel {

      private static final long serialVersionUID = -316782184969068750L;
      private final JList<Composition> jList_Materials = new JList<Composition>();

      public SelectMaterialsPanel() {
         super(OptimizationWizard.this);
         init();
      }

      private void init() {
         final PanelBuilder pb = new PanelBuilder(new FormLayout("250dlu", "pref, 5dlu, 120dlu"));
         pb.addSeparator("Select one or more materials", CC.xy(1, 1));
         pb.add(new JScrollPane(jList_Materials), CC.xy(1, 3));
         add(pb.getPanel());
      }

      @Override
      public void onShow() {
         final ArrayList<Composition> comps = new ArrayList<Composition>(mStandards.allCompositions());
         Collections.sort(comps, new Comparator<Composition>() {
            @Override
            public int compare(final Composition arg0, final Composition arg1) {
               int res = arg0.toString().compareTo(arg1.toString());
               if(res == 0)
                  res = arg0.compareTo(arg1);
               return res;
            }

         });
         final DefaultListModel<Composition> dlm = new DefaultListModel<Composition>();
         for(final Composition comp : comps)
            dlm.addElement(comp);
         jList_Materials.setModel(dlm);
         jList_Materials.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         setNextPanel(jWizardPanel_Results, "Summary results");
         enableFinish(false);
      }

      public Set<Composition> getSelectedMaterials() {
         final TreeSet<Composition> res = new TreeSet<Composition>();
         for(final Object obj : jList_Materials.getSelectedValuesList())
            if(obj instanceof Composition)
               res.add((Composition) obj);
         return Collections.unmodifiableSet(res);
      }

      public String toHTML() {
         final Set<Composition> comps = getSelectedMaterials();
         final StringBuffer sb = new StringBuffer();
         sb.append("<h2>Find standards</h2>");
         sb.append("<p><table>");
         sb.append("<tr><th>Material</th><th>Description</th><th>Blocks</th></tr>");
         for(final Composition comp : comps) {
            final List<StandardBlock2> src = mStandards.find(comp);
            sb.append("<th>" + comp.toString() + "</th>");
            sb.append("<td>" + comp.toHTMLTable() + "</td>");
            sb.append("<td>" + TextUtilities.toList(src) + "</td></tr>");
         }
         sb.append("</table></p>");
         sb.append("<h3>Minimal block set</h3>");
         sb.append("<ul><li>Optimal: " + mStandards.suggestBlocks(comps, null) + "</li></ul>");
         return sb.toString();
      }
   };

   private class ResultsPanel
      extends
      JWizardPanel {

      private static final long serialVersionUID = 1382545139749510217L;

      private final JTextPane jTextPane_Summary = new JTextPane();
      private final JCheckBox jCheckBox_Simulate = new JCheckBox("Simulate unknown, standard and reference spectra");

      public ResultsPanel() {
         super(OptimizationWizard.this);
         init();
      }

      private void init() {
         final PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu, 245dlu", "pref, 5dlu, fill:75dlu, 5dlu, pref, 5dlu, pref"));
         pb.addSeparator("Summary report", CC.xyw(1, 1, 2));
         pb.add(new JScrollPane(jTextPane_Summary), CC.xy(2, 3));
         pb.addSeparator("Report Options", CC.xyw(1, 5, 2));
         pb.add(jCheckBox_Simulate, CC.xy(2, 7));
         add(pb.getPanel());
      }

      private void updateReport() {
         final StringBuffer sb = new StringBuffer();
         sb.append("<HTML>");
         switch(mMode) {
            case OptimizeEDS: {
               final NumberFormat nf = new DecimalFormat("0.0");
               sb.append("<table>\n");
               sb.append("<tr><th align=\"right\">Standards</th><td>");
               sb.append(TextUtilities.toList(mOptimizer.getStandards()));
               sb.append("</td></tr>");
               final ArrayList<Composition> al = new ArrayList<Composition>(mOptimizer.getReferences());
               al.removeAll(mOptimizer.getStandards());
               sb.append("<tr><th align=\"right\">References</th><td>");
               sb.append(al.size() > 0 ? TextUtilities.toList(al) : "--None required--");
               sb.append("</td></tr>");
               sb.append("<tr><th align=\"right\">Beam Energy</th><td>");
               sb.append(nf.format(FromSI.keV(mOptimizer.getBeamEnergy())));
               sb.append(" keV</td></tr>");
               sb.append("<tr><th align=\"right\">Dose</th><td>");
               sb.append(nf.format(60.0));
               sb.append(" nA&middot;s</td></tr>");
               sb.append("</table>");
               jCheckBox_Simulate.setEnabled(true);
            }
               break;
            case FindStandards:
               jCheckBox_Simulate.setEnabled(false);
               final Set<Composition> comps = jWizardPanel_SelectMaterials.getSelectedMaterials();
               sb.append("<table>");
               sb.append("<tr><th>Material</th><th>Blocks</th></tr>");
               for(final Composition comp : comps) {
                  final List<StandardBlock2> src = mStandards.find(comp);
                  sb.append("<th>" + comp.toString() + "</th>");
                  sb.append("<td>" + TextUtilities.toList(src) + "</td></tr>");
               }
               sb.append("</table>");
               sb.append("<p>Optimal: " + mStandards.suggestBlocks(comps, null) + "</p>");
               break;
            default:
               break;
         }
         jTextPane_Summary.setContentType("text/html");
         jTextPane_Summary.setText(sb.toString());
      }

      @Override
      public void onShow() {
         updateReport();
         setNextPanel(null, "Done");
         enableFinish(true);
      }
   };

   public Set<ISpectrumData> simulateSpectra() {
      final TreeSet<ISpectrumData> res = new TreeSet<ISpectrumData>();
      if(jWizardPanel_Results.jCheckBox_Simulate.isSelected()) {
         final SpectrumSimulator ss = new SpectrumSimulator.BasicSpectrumSimulator();
         final SpectrumProperties sp = new SpectrumProperties();
         sp.setNumericProperty(SpectrumProperties.BeamEnergy, FromSI.keV(mOptimizer.getBeamEnergy()));
         sp.setDetector(mOptimizer.getDetector());
         sp.setNumericProperty(SpectrumProperties.FaradayBegin, 1.0);
         sp.setNumericProperty(SpectrumProperties.LiveTime, 60.0);
         for(final Composition comp : mOptimizer.getAllMaterials())
            try {
               final ISpectrumData spec = SpectrumUtils.addNoiseToSpectrum(ss.generateSpectrum(comp, sp, true), 1.0);
               SpectrumUtils.rename(spec, comp.toString() + (mOptimizer.isStandard(comp) ? " std" : " ref"));
               res.add(spec);
            }
            catch(final EPQException e) {
               e.printStackTrace();
            }
         {
            try {
               final ISpectrumData spec = SpectrumUtils.addNoiseToSpectrum(ss.generateSpectrum(mEstComposition, sp, true), 1.0);
               SpectrumUtils.rename(spec, mEstComposition.descriptiveString(false));
               res.add(spec);
            }
            catch(final EPQException e) {
               e.printStackTrace();
            }
         }
      }
      return Collections.unmodifiableSet(res);
   }

   private class SelectStandardExpPanel
      extends
      JWizardPanel {

      private static final long serialVersionUID = -4588569758666305684L;

      private class StandardsTableModel
         extends
         DefaultTableModel {

         private static final long serialVersionUID = 5773383510926965470L;

         StandardsTableModel(final QuantificationOutline qp2) {
            super(new String[] {
               "Element",
               "Standard",
               "Precision"
            }, 0);
            final NumberFormat nf4 = new DecimalFormat("0.######");
            for(final Element elm : qp2.getMeasuredElements())
               addRow(new Object[] {
                  elm.toAbbrev(),
                  qp2.getStandard(elm),
                  nf4.format(qp2.getDesiredPrecision(elm))
               });
         }
      }

      private class AddAction
         extends
         AbstractAction {

         private static final long serialVersionUID = -2821363716296894666L;

         protected AddAction() {
            super("Add");
         }

         @Override
         public void actionPerformed(final ActionEvent e) {
            final String elmStr = jTextField_Elements.getText();
            final Set<Element> elms = Element.parseElementString(elmStr);
            jTextField_Elements.setBackground(elms.size() > 0 ? SystemColor.text : Color.pink);
            final String compStr = jTextField_Composition.getText();
            final Composition comp = strToComp(compStr);
            jTextField_Composition.setBackground(comp != null ? SystemColor.text : Color.pink);
            final double precision = jTextField_Precision.getValue();
            if(mQuantOutline == null)
               mQuantOutline = new QuantificationOutline(mDetector, mBeamEnergy);
            StringBuffer missed = new StringBuffer();
            if((elms.size() > 0) && (comp != null) && (precision > 0.0) && (precision < 1.0)) {
               for(final Element elm : elms)
                  if(comp.containsElement(elm))
                     mQuantOutline.addStandard(elm, comp, Collections.emptySet(), precision);
                  else {
                     if(missed.length() > 0)
                        missed.append(", ");
                     missed.append(elm.toAbbrev());
                  }
               jTextField_Elements.setText(missed.toString());
               jTextField_Composition.setText("");
               jTextField_Elements.requestFocus();
               updateStandards();
            }
         }
      };

      private void updateStandards() {
         jTable_Standards.setModel(new StandardsTableModel(mQuantOutline));
         final TableColumnModel cm = jTable_Standards.getColumnModel();
         cm.getColumn(0).setPreferredWidth(40);
         OptimizationWizard.this.enableNext(mQuantOutline.getMeasuredElements().size() > 0);
      }

      private class RemoveAction
         extends
         AbstractAction {

         private static final long serialVersionUID = -7454144478386772758L;

         protected RemoveAction() {
            super("Remove");
         }

         @Override
         public void actionPerformed(final ActionEvent e) {
            final int[] rows = jTable_Standards.getSelectedRows();
            final TableModel tm = jTable_Standards.getModel();
            if(tm instanceof StandardsTableModel) {
               final StandardsTableModel stm = (StandardsTableModel) tm;
               for(final int row : rows) {
                  final Element elm = Element.byName(stm.getValueAt(row, 0).toString());
                  if(!elm.equals(Element.None))
                     mQuantOutline.removeStandard(elm);
               }
               updateStandards();
            }
         }
      };

      private class ClearAction
         extends
         AbstractAction {

         private static final long serialVersionUID = -8656990451393495807L;

         protected ClearAction() {
            super("Clear");
         }

         @Override
         public void actionPerformed(final ActionEvent e) {
            mQuantOutline.clearStandards();
            updateStandards();
         }
      };

      private final JTable jTable_Standards = new JTable();
      private final JTextField jTextField_Elements = new JTextField();
      private final JTextField jTextField_Composition = new JTextField();
      private final JTextFieldDouble jTextField_Precision = new JTextFieldDouble();
      private final JButton jButton_Add = new JButton(new AddAction());
      private final JButton jButton_Remove = new JButton(new RemoveAction());
      private final JButton jButton_Clear = new JButton(new ClearAction());

      public SelectStandardExpPanel() {
         super(OptimizationWizard.this);
         initialize();
      }

      private void initialize() {
         final FormLayout layout = new FormLayout("pref, 5dlu, 50dlu, 5dlu, pref, 5dlu, 50dlu, 5dlu, pref, 5dlu, 40dlu, 5dlu, pref", "pref, 5dlu, pref, 85dlu, 5dlu, pref");
         final PanelBuilder pb = new PanelBuilder(layout, this);
         final int BASE_LINE = 6;
         pb.add(new JScrollPane(jTable_Standards), CC.xywh(1, 1, 11, BASE_LINE - 2));
         pb.add(jButton_Remove, CC.xyw(13, 1, 1));
         pb.add(jButton_Clear, CC.xyw(13, 3, 1));
         pb.addLabel("Elements", CC.xyw(1, BASE_LINE, 1));
         pb.add(jTextField_Elements, CC.xyw(3, BASE_LINE, 1));
         pb.addLabel("Material", CC.xyw(5, BASE_LINE, 1));
         pb.add(jTextField_Composition, CC.xyw(7, BASE_LINE, 1));
         pb.addLabel("Precision", CC.xyw(9, BASE_LINE, 1));
         pb.add(jTextField_Precision, CC.xyw(11, BASE_LINE, 1));
         pb.add(jButton_Add, CC.xyw(13, BASE_LINE, 1));
         jTextField_Precision.setValue(0.01);
         jTextField_Elements.requestFocus();
      }

      @Override
      public boolean permitNext() {
         return mQuantOutline.getMeasuredElements().size() > 0;
      }

      @Override
      public void onShow() {
         OptimizationWizard.this.setMessageText("Specify the elements to be measured and the associated standard.");
         setNextPanel(jWizardPanel_OtherElementsPanel, "Select other element rules");
         enableNext((mQuantOutline != null) && (mQuantOutline.getMeasuredElements().size() > 0));
         enableFinish(false);

      }
   }

   private class OtherElementsPanel
      extends
      JWizardPanel {

      private class NoneAction
         extends
         AbstractAction {

         private static final long serialVersionUID = 3296513503107915920L;

         private NoneAction() {
            super("No other elements");
         }

         @Override
         public void actionPerformed(final ActionEvent arg0) {
            jTable_Stoichiometry.setEnabled(false);
         }
      };

      private class StoichiometryAction
         extends
         AbstractAction {

         private static final long serialVersionUID = 5457997540626618291L;

         public StoichiometryAction() {
            super("Oxygen by stoichiometry");
         }

         @Override
         public void actionPerformed(final ActionEvent arg0) {
            jTable_Stoichiometry.setEnabled(true);
         }
      };

      private class DifferenceAction
         extends
         AbstractAction {

         private static final long serialVersionUID = 4986426930516496223L;

         private DifferenceAction() {
            super("Element by difference");
         }

         @Override
         public void actionPerformed(final ActionEvent arg0) {
            jTable_Stoichiometry.setEnabled(false);
         }
      };

      private class WatersOfCrystallizationAction
         extends
         AbstractAction {

         private static final long serialVersionUID = -4185500220696938016L;

         private WatersOfCrystallizationAction() {
            super("Waters-of-crystallization");
         }

         @Override
         public void actionPerformed(final ActionEvent arg0) {
            jTable_Stoichiometry.setEnabled(true);
         }
      };

      private final JTextField jTextField_Strip = new JTextField();
      private final JComboBox<Element> jComboBox_Difference = new JComboBox<Element>();
      private final JRadioButton jRadioButton_None = new JRadioButton(new NoneAction());
      private final JRadioButton jRadioButton_Stoichiometry = new JRadioButton(new StoichiometryAction());
      private final JRadioButton jRadioButton_Difference = new JRadioButton(new DifferenceAction());
      private final JRadioButton jRadioButton_WatersOfCrystallization = new JRadioButton(new WatersOfCrystallizationAction());
      private final JStoichiometryTable jTable_Stoichiometry = new JStoichiometryTable();

      public OtherElementsPanel() {
         super(OptimizationWizard.this);
         init();
      }

      private void init() {
         final ButtonGroup bg = new ButtonGroup();
         bg.add(jRadioButton_None);
         bg.add(jRadioButton_Difference);
         bg.add(jRadioButton_Stoichiometry);
         bg.add(jRadioButton_WatersOfCrystallization);

         final FormLayout layout = new FormLayout("5dlu, pref, 5dlu, 80dlu, 10dlu, 150dlu", "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 30dlu");
         final PanelBuilder pb = new PanelBuilder(layout, this);
         final int ALL_COLS = 6;
         pb.addSeparator("Peaks to strip", CC.xyw(1, 1, ALL_COLS));
         pb.addLabel("Elements", CC.xy(2, 3));
         pb.add(jTextField_Strip, CC.xy(4, 3));
         pb.addSeparator("Other element rules", CC.xyw(1, 5, ALL_COLS));
         pb.add(jRadioButton_None, CC.xyw(2, 7, 3));
         pb.add(jRadioButton_Difference, CC.xyw(2, 9, 3));
         pb.add(jComboBox_Difference, CC.xyw(6, 9, 1));
         pb.add(jRadioButton_Stoichiometry, CC.xyw(2, 11, 3));
         pb.add(jRadioButton_WatersOfCrystallization, CC.xyw(2, 13, 3));
         pb.add(new JScrollPane(jTable_Stoichiometry), CC.xywh(6, 11, 1, 4));

         jTextField_Strip.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(final FocusEvent e) {
               final Set<Element> elms = new TreeSet<Element>(Element.parseElementString(jTextField_Strip.getText()));
               elms.removeAll(mQuantOutline.getMeasuredElements());
               jTextField_Strip.setText(Element.toString(elms, true));
               mQuantOutline.clearElementsToStrip();
               for(final Element elm : elms)
                  mQuantOutline.addElementToStrip(elm);
            }

            @Override
            public void focusGained(final FocusEvent e) {

            }
         });
      }

      @Override
      public void onShow() {
         final List<UnmeasuredElementRule> rules = mQuantOutline.getUnmeasuredElementRules();
         final UnmeasuredElementRule rule = rules.size() > 0 ? rules.get(0) : null;
         final Set<Element> elms = new TreeSet<Element>(Arrays.asList(Element.range(Element.H, Element.Pu)));
         final Set<Element> measuredElements = mQuantOutline.getMeasuredElements();
         elms.removeAll(measuredElements);
         final DefaultComboBoxModel<Element> elmModel = new DefaultComboBoxModel<Element>(elms.toArray(new Element[elms.size()]));
         final Element difElm = (rule != null) && (rule instanceof CompositionFromKRatios.ElementByDifference)
               ? ((ElementByDifference) rule).getElement()
               : elms.iterator().next();
         elmModel.setSelectedItem(difElm);
         jComboBox_Difference.setModel(elmModel);
         jTextField_Strip.setText(Element.toString(mQuantOutline.getStrippedElements(), true));
         if(rules.size() == 0)
            jRadioButton_None.setSelected(true);
         else if(rule instanceof ElementByDifference)
            jRadioButton_Difference.setSelected(true);
         else if(rule instanceof OxygenByStoichiometry)
            jRadioButton_Stoichiometry.setSelected(true);
         else if(rule instanceof WatersOfCrystallization)
            jRadioButton_WatersOfCrystallization.setSelected(true);
         jRadioButton_Stoichiometry.setEnabled(!measuredElements.contains(Element.O));
         jRadioButton_WatersOfCrystallization.setEnabled(measuredElements.contains(Element.O));
         jTable_Stoichiometry.setElements(measuredElements);
         OptimizationWizard.this.setMessageText("Specify special processing options.");
         setNextPanel(jWizardPanel_SelectRefExp, "Select references");
      }

      @Override
      public boolean permitNext() {
         boolean permit = false;
         if(jRadioButton_Difference.isSelected() && (jComboBox_Difference.getSelectedItem() instanceof Element)) {
            final UnmeasuredElementRule uer = new ElementByDifference((Element) jComboBox_Difference.getSelectedItem());
            mQuantOutline.clearUnmeasuredElementRules();
            mQuantOutline.addUnmeasuredElementRule(uer);
            permit = true;
         } else if(jRadioButton_None.isSelected()) {
            mQuantOutline.clearUnmeasuredElementRules();
            permit = true;
         } else if(jRadioButton_Stoichiometry.isSelected()) {
            final OxygenByStoichiometry uer = new OxygenByStoichiometry(mQuantOutline.getMeasuredElements());
            uer.setOxidizer(jTable_Stoichiometry.getOxidizer());
            mQuantOutline.clearUnmeasuredElementRules();
            mQuantOutline.addUnmeasuredElementRule(uer);
            permit = true;
         } else if(jRadioButton_WatersOfCrystallization.isSelected()) {
            final WatersOfCrystallization uer = new WatersOfCrystallization(mQuantOutline.getMeasuredElements());
            uer.setOxidizer(jTable_Stoichiometry.getOxidizer());
            mQuantOutline.clearUnmeasuredElementRules();
            mQuantOutline.addUnmeasuredElementRule(uer);
            permit = true;
         }
         return permit;
      }

      private static final long serialVersionUID = -6431830024822522174L;

   }

   private class SelectReferenceExpPanel
      extends
      JWizardPanel {

      private class ApplyAction
         extends
         AbstractAction {

         private static final long serialVersionUID = -7457759687870859525L;

         ApplyAction() {
            super("Apply");
         }

         @Override
         public void actionPerformed(final ActionEvent e) {
            final int[] rows = jTable_Reference.getSelectedRows();
            final Composition ref = strToComp(jTextField_Reference.getText().trim());
            if(ref != null) {
               for(final int row : rows) {
                  final RegionOfInterest roi = (RegionOfInterest) jTable_Reference.getValueAt(row, 0);
                  try {
                     mQuantOutline.addReference(roi, ref);
                  }
                  catch(final EPQException e1) {
                     e1.printStackTrace();
                  }
               }
               jTable_Reference.setModel(new ReferenceTableModel());
            }
         }

      }

      private class ReferenceTableModel
         extends
         DefaultTableModel {

         private static final long serialVersionUID = -6899788074545151942L;

         private ReferenceTableModel() {
            super(getReferenceData(), HEADER);
         }
      }

      private Object[][] getReferenceData() {
         if(mQuantOutline != null) {
            final Set<RegionOfInterest> arr = mQuantOutline.getAllRequiredReferences(true);
            final Object[][] res = new Object[arr.size()][HEADER.length];
            int i = 0;
            for(final RegionOfInterest roi : arr) {
               final ReferenceMaterial ref = mQuantOutline.getReference(roi);
               res[i][0] = roi;
               res[i][1] = ref;
               res[i][2] = mQuantOutline.isStandard(ref) ? "Yes" : "";
               ++i;
            }
            return res;
         }
         return new Object[0][0];
      }

      private final JTable jTable_Reference = new JTable(new ReferenceTableModel());
      private final JTextField jTextField_Reference = new JTextField();
      private final JButton jButton_Apply = new JButton(new ApplyAction());

      private final String[] HEADER = new String[] {
         "ROI",
         "Material",
         "Standard?"
      };

      public SelectReferenceExpPanel() {
         super(OptimizationWizard.this);
         init();
      }

      private void init() {
         final FormLayout layout = new FormLayout("120dlu, pref, 5dlu, 50dlu, 5dlu, pref", "120dlu, 5dlu, pref");
         final PanelBuilder pb = new PanelBuilder(layout, this);
         pb.add(new JScrollPane(jTable_Reference), CC.xyw(1, 1, 6));
         pb.addLabel("Material", CC.xy(2, 3));
         pb.add(jTextField_Reference, CC.xy(4, 3));
         pb.add(jButton_Apply, CC.xy(6, 3));
      }

      private static final long serialVersionUID = -7624565078487759150L;

      @Override
      public void onShow() {
         final ReferenceDatabase rd = ReferenceDatabase.getInstance(mSession);
         mQuantOutline.applySuggestedReferences(rd.getDatabase());
         jTable_Reference.setModel(new ReferenceTableModel());
         setNextPanel(jWizardPanel_SpecifyUnknown, "Specify the unknown");
         OptimizationWizard.this.setMessageText("You have the option to change the default references.");
      }
   }

   private class SpecifyUnknownExpPanel
      extends
      JWizardPanel {

      private final JTable jTable_Composition = new JTable();
      private final JButton jButton_Composition = new JButton("Composition");
      private final JButton jButton_Approx = new JButton("Major/Minor/Trace");

      private static final long serialVersionUID = 428817610436442636L;

      private class CompositionAction
         extends
         AbstractAction {

         private static final long serialVersionUID = 4501086633662345776L;

         public CompositionAction() {
            super("Composition");
         }

         @Override
         public void actionPerformed(ActionEvent arg0) {
            if(mEstComposition != null)
               updateComposition(MaterialsCreator.editMaterial(OptimizationWizard.this, mEstComposition, mSession, false));
            else
               updateComposition(MaterialsCreator.createMaterial(OptimizationWizard.this, mSession, false));
            update();
         }
      };

      private class ApproxAction
         extends
         AbstractAction {

         private static final long serialVersionUID = 7614164549696653340L;

         public ApproxAction() {
            super("Major/Minor/Trace");
         }

         @Override
         public void actionPerformed(ActionEvent e) {
            final MajorMinorTraceEditor mmt = new MajorMinorTraceEditor(OptimizationWizard.this);
            mmt.setLocationRelativeTo(OptimizationWizard.this);
            if(mEstComposition != null)
               mmt.setComposition(mEstComposition);
            mmt.setVisible(true);
            updateComposition(mmt.getComposition());
            update();
         }

      }

      private void update() {
         jTable_Composition.setModel(new CompositionTableModel(mEstComposition, true, true));
      }

      public SpecifyUnknownExpPanel() {
         super(OptimizationWizard.this);
         init();
      }

      private void init() {
         jButton_Composition.addActionListener(new CompositionAction());
         jButton_Approx.addActionListener(new ApproxAction());
         final FormLayout layout = new FormLayout("200dlu, 5dlu, pref", "pref, 5dlu, pref, 85dlu");
         final PanelBuilder pb = new PanelBuilder(layout, this);
         pb.add(new JScrollPane(jTable_Composition), CC.xywh(1, 1, 1, 4));
         pb.add(jButton_Composition, CC.xyw(3, 1, 1));
         pb.add(jButton_Approx, CC.xyw(3, 3, 1));

      }

      @Override
      public void onShow() {
         setNextPanel(jWizardPanel_SelectTransitionExp, "Select transitions");
         update();
      }

      @Override
      public boolean permitNext() {
         final Set<Element> extraElms = new TreeSet<Element>(mEstComposition.getElementSet());
         extraElms.removeAll(mQuantOutline.getUnknownElements());
         if(extraElms.size() > 0) {
            JOptionPane.showMessageDialog(OptimizationWizard.this, "The elements " + extraElms.toString()
                  + " in the unknown are not quantified.\nGo back and add a standard.", "Missing elements", JOptionPane.ERROR_MESSAGE);
            return false;
         }
         return true;
      }
   }

   private class SelectTransitionPanel
      extends
      JWizardPanel {

      private static final long serialVersionUID = 7585401016139902190L;

      private static final int NUM_COLS = 6;

      private class ROIItemListener
         implements
         ItemListener {

         private final Element mElement;

         private ROIItemListener(Element elm) {
            mElement = elm;
         }

         @Override
         public void itemStateChanged(ItemEvent e) {
            assert e.getItem() instanceof RegionOfInterest;
            final RegionOfInterest roi = (RegionOfInterest) e.getItem();
            if((roi != null) && (!roi.equals(mSelected.get(mElement)))) {
               mSelected.put(mElement, roi);
               updateTable();
            }
         }
      }

      private Object[][] getData() {
         if(mEstComposition != null)
            try {
               // Initialize mSelected
               if(mSelected == null)
                  mSelected = new TreeMap<Element, RegionOfInterest>();
               for(final Element elm : mQuantOutline.getMeasuredElements())
                  if((!mSelected.containsKey(elm)) && mEstComposition.containsElement(elm)) {
                     double bestU = Double.MAX_VALUE;
                     final RegionOfInterestSet srois = mQuantOutline.getStandardROIS(elm);
                     if(srois.size() > 1)
                        for(final RegionOfInterest roi : srois) {
                           final UncertainValue2 uv = mQuantOutline.massFraction(mEstComposition, roi);
                           if(uv.uncertainty() < bestU) {
                              mSelected.put(elm, roi);
                              bestU = uv.uncertainty();
                           }
                        }
                     else
                        mSelected.put(elm, srois.iterator().next());
                  }
               // Use mSelected to set preferred rois
               for(final Element elm : mQuantOutline.getMeasuredElements()) {
                  final RegionOfInterest roi = mSelected.get(elm);
                  if(roi != null)
                     mQuantOutline.setPreferredROI(roi);
               }
               // Optimize the measurement
               final QuantificationOptimizer qo = new QuantificationOptimizer2(mQuantOutline, SpectrumSimulator.Basic);
               mEstComposition.setName(UNKNOWN);
               mQuantPlan = qo.compute(mEstComposition);
               final Object[][] res = new Object[mQuantOutline.getMeasuredElements().size()][NUM_COLS];
               int i = 0;
               final NumberFormat pct = new HalfUpFormat("0.0 %");
               final NumberFormat nf = new HalfUpFormat("0.0");
               for(final Element elm : mQuantOutline.getMeasuredElements()) {
                  final RegionOfInterest selected = mSelected.get(elm);
                  final QuantificationPlan.Acquisition std = mQuantPlan.find(mQuantOutline.getStandard(elm));
                  final QuantificationPlan.Acquisition unk = mQuantPlan.find(mEstComposition);
                  final UncertainValue2 uv = mQuantOutline.massFraction(mEstComposition, selected);
                  res[i][0] = elm.toAbbrev();
                  res[i][1] = selected;
                  res[i][2] = pct.format(uv.uncertainty() / uv.doubleValue());
                  res[i][3] = nf.format(std != null ? std.getDose() : Double.NaN) + " nA\u00B7s";
                  res[i][4] = nf.format(unk != null ? unk.getDose() : Double.NaN) + " nA\u00B7s";
                  ++i;
               }
               setNextPanel(jWizardPanel_ExpertReport, "View summary report");
               enableNext(mSelected != null);
               return res;
            }
            catch(final EPQException e) {
               enableNext(false);
               OptimizationWizard.this.setExceptionText(e.getMessage(), e);
               return nullRows();
            }
         else
            return nullRows();
      }

      private void updateTable() {
         jTable_Transitions.setModel(new TransitionTableModel());
         jTable_Transitions.getColumnModel().getColumn(1).setCellEditor(eachRowEditor_ROI);
         int i = 0;
         for(final Element elm : mQuantOutline.getMeasuredElements()) {
            final RegionOfInterest selected = mSelected.get(elm);
            final JComboBox<RegionOfInterest> rcb = new JComboBox<RegionOfInterestSet.RegionOfInterest>();
            for(final RegionOfInterest roi : mQuantOutline.getStandardROIS(elm))
               rcb.addItem(roi);
            rcb.setSelectedItem(selected);
            rcb.addItemListener(new ROIItemListener(elm));
            eachRowEditor_ROI.setEditorAt(i, new DefaultCellEditor(rcb));
            ++i;
         }

      }

      private Object[][] nullRows() {
         final Object[][] res = new Object[1][5];
         for(int i = 0; i < res[0].length; ++i)
            res[0][i] = "";
         return res;
      }

      private class SelectUnknownAction
         extends
         AbstractAction {

         private static final long serialVersionUID = -1297266875866064784L;

         SelectUnknownAction() {
            super("...");
         }

         @Override
         public void actionPerformed(final ActionEvent arg0) {
            mEstComposition = MaterialsCreator.editMaterial(OptimizationWizard.this, mEstComposition, mSession, false);
            jTextField_Unknown.setText(mEstComposition != null ? mEstComposition.getName() : "N/A");
            OptimizationWizard.this.clearMessageText();
            OptimizationWizard.this.setMessageText("Select one ROI per element...");
            updateTable();
         }
      }

      private final JTextField jTextField_Unknown = new JTextField();
      private final JButton jButton_Unknown = new JButton(new SelectUnknownAction());
      private final JTable jTable_Transitions = new JTable();
      private final EachRowEditor eachRowEditor_ROI = new EachRowEditor(jTable_Transitions);

      private Map<Element, RegionOfInterest> mSelected;

      private class TransitionTableModel
         extends
         DefaultTableModel {

         private static final long serialVersionUID = 5575164758592228404L;

         private TransitionTableModel() {
            super(getData(), new String[] {
               "Element",
               "ROI",
               "U[combined]",
               "Dose[std]",
               "Dose[unk]"
            });
         }

      };

      public SelectTransitionPanel() {
         super(OptimizationWizard.this);
         init();
      }

      private void init() {
         final FormLayout layout = new FormLayout("pref, 5dlu, 60dlu, 5dlu, pref, right:150dlu", "pref, 5dlu, 120dlu");
         final PanelBuilder pb = new PanelBuilder(layout, this);
         pb.addLabel("Estimated unknown", CC.xy(1, 1));
         pb.add(jTextField_Unknown, CC.xy(3, 1));
         jTextField_Unknown.setEditable(false);
         pb.add(jButton_Unknown, CC.xy(5, 1));
         pb.add(new JScrollPane(jTable_Transitions), CC.xyw(1, 3, 6));
         setNextPanel(jWizardPanel_ExpertReport, "View summary report");
         OptimizationWizard.this.setMessageText("Provide an estimate of the composition of the unknown.");
      }

      @Override
      public void onShow() {
         updateTable();
         jTextField_Unknown.setText(mEstComposition != null ? mEstComposition.getName() : "N/A");
      }
   }

   private class ExpertReportPanel
      extends
      JWizardPanel {

      private class PrintAction
         extends
         AbstractAction {

         private static final long serialVersionUID = -5459854505150885133L;

         PrintAction() {
            super("Print");
         }

         @Override
         public void actionPerformed(final ActionEvent arg0) {
            PrintUtilities.printComponent(jTextPane_Report);
         }
      };

      private class OpenAction
         extends
         AbstractAction {

         private static final long serialVersionUID = 7080933027302649656L;

         OpenAction() {
            super("Open");
         }

         @Override
         public void actionPerformed(final ActionEvent arg0) {
            try {
               Desktop.getDesktop().browse(mReportFile.toURI());
            }
            catch(IOException e) {
               e.printStackTrace();
            }
         }
      }

      private class SimulateAction
         extends
         AbstractAction {

         private static final long serialVersionUID = -8182315668545728886L;

         SimulateAction() {
            super("Simulate");
         }

         @Override
         public void actionPerformed(final ActionEvent arg0) {
            if(mQuantPlan != null) {
               final SpectrumSimulator ss = new SpectrumSimulator.BasicSpectrumSimulator();
               try {
                  final Map<Acquisition, ISpectrumData> res = mQuantPlan.simulate(ss);
                  for(final ISpectrumData spec : res.values())
                     DataManager.getInstance().addSpectrum(spec, true);
                  QuantifyUsingStandards qus = mQuantPlan.buildQuantifyUsingStandards(res);
                  ArrayList<QuantifyUsingStandards.Result> results = new ArrayList<QuantifyUsingStandards.Result>();
                  List<ISpectrumData> unknowns = mQuantPlan.simulateUnknown(ss, 9);
                  for(final ISpectrumData spec : unknowns) {
                     DataManager.getInstance().addSpectrum(spec, true);
                     results.add(qus.compute(spec));
                  }
                  mQuantResults = "<h3>Simulated Optimized Spectrum Quantification Results</h3>\n"
                        + qus.tabulateResults(unknowns, DTSA2.getReport().getFile().getParentFile(), null);
               }
               catch(final EPQException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
            }

         }
      }

      private static final long serialVersionUID = 2221284872968426900L;
      private final JTextPane jTextPane_Report = new JTextPane();
      private final JButton jButton_Print = new JButton(new PrintAction());
      private final JButton jButton_Simulate = new JButton(new SimulateAction());
      private final JButton jButton_Open = new JButton(new OpenAction());
      private File mReportFile;

      private String mQuantResults = null;

      public String getQuantResults() {
         return mQuantResults != null ? mQuantResults : "";
      }

      private ExpertReportPanel() {
         super(OptimizationWizard.this);
         init();
      }

      private void init() {
         final FormLayout layout = new FormLayout("fill:320dlu", "fill:135dlu, 5dlu, pref");
         final PanelBuilder pb = new PanelBuilder(layout, this);
         pb.add(new JScrollPane(jTextPane_Report), CC.xy(1, 1));
         final ButtonBarBuilder bbb = new ButtonBarBuilder();
         bbb.addButton(jButton_Print);
         bbb.addRelatedGap();
         bbb.addButton(jButton_Open);
         bbb.addGlue();
         bbb.addButton(jButton_Simulate);
         pb.add(bbb.getPanel(), CC.xy(1, 3));
      }

      @Override
      public void onShow() {
         final File parentFile = DTSA2.getReport().getFile().getParentFile();
         final String html = TextUtilities.wrapHTMLHeader("Quantification Planner Report", mQuantPlan.toHTML(parentFile, true), "style.css");
         mReportFile = null;
         try {
            mReportFile = File.createTempFile("QuantPlan", ".html", parentFile);
            try (final FileWriter fw = new FileWriter(mReportFile, false)) {
               fw.write(html);
            }
         }
         catch(final IOException e1) {
            e1.printStackTrace();
         }
         if(mReportFile != null) {
            final File cssFile = new File(mReportFile.getParentFile(), "style.css");
            if(!cssFile.exists())
               // Write the style sheet
               try {
                  try (final PrintWriter osw = new PrintWriter(cssFile)) {
                     try (final BufferedReader isr = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("style.css")))) {
                        for(String str = isr.readLine(); str != null; str = isr.readLine())
                           osw.println(str);
                     }
                  }
               }
               catch(final Exception e) {
                  // Ignore it...
               }
            try {
               jTextPane_Report.setEditable(false);
               jTextPane_Report.setContentType("text/html");
               ((HTMLDocument) jTextPane_Report.getDocument()).setAsynchronousLoadPriority(-1);

               final URI uri = mReportFile.toURI();
               if(uri != null) {
                  final URL url = uri.toURL();
                  if(url != null)
                     jTextPane_Report.setPage(url);
               }
               OptimizationWizard.this.setMessageText("This report is saved as " + mReportFile.getName());
            }
            catch(final Exception e) {
               e.printStackTrace();
            }
         }
         OptimizationWizard.this.enableFinish(true);
      }

      @Override
      public boolean permitNext() {
         return true;
      }
   };

   enum Mode {
      OptimizeEDS, OptimizeWDS, OptimizeMixed, OptimizeExpert, FindStandards;
   }

   private static final long serialVersionUID = -7377008108176546296L;

   private static final String UNKNOWN = "Unknown";

   private final IntroductionPanel jWizardPanel_Introduction = new IntroductionPanel();

   private final InstrumentDetectorPanel jWizardPanel_Instrument = new InstrumentDetectorPanel();
   private final EstimateCompositionPanel jWizardPanel_EstComposition = new EstimateCompositionPanel();
   private final SelectBlocksPanel jWizardPanel_Blocks = new SelectBlocksPanel();
   private final SelectStandardPanel jWizardPanel_SelectStandard = new SelectStandardPanel();
   private final SelectReferencePanel jWizardPanel_SelectReference = new SelectReferencePanel();
   private final SelectMaterialsPanel jWizardPanel_SelectMaterials = new SelectMaterialsPanel();
   private final OtherElementsPanel jWizardPanel_OtherElementsPanel = new OtherElementsPanel();
   private final ResultsPanel jWizardPanel_Results = new ResultsPanel();

   private final SelectStandardExpPanel jWizardPanel_SelectStdExp = new SelectStandardExpPanel();
   private final SelectReferenceExpPanel jWizardPanel_SelectRefExp = new SelectReferenceExpPanel();
   private final SelectTransitionPanel jWizardPanel_SelectTransitionExp = new SelectTransitionPanel();
   private final SpecifyUnknownExpPanel jWizardPanel_SpecifyUnknown = new SpecifyUnknownExpPanel();
   private final ExpertReportPanel jWizardPanel_ExpertReport = new ExpertReportPanel();

   private EDSDetector mDetector;
   private double mBeamEnergy = 15.0;
   private Composition mEstComposition = new Composition(new Element[0], new double[0], UNKNOWN);
   private final StandardsDatabase2 mStandards = DTSA2.getStandardsDatabase();
   private final Session mSession;
   private CompositionOptimizer mOptimizer;

   private QuantificationOutline mQuantOutline;
   private QuantificationPlan mQuantPlan;

   private Mode mMode = Mode.OptimizeExpert;

   private void updateComposition(Composition c) {
      if(c != null) {
         c.setName(UNKNOWN);
         final Preferences prefs = Preferences.userNodeForPackage(OptimizationWizard.class);
         prefs.put(UNKNOWN, EPQXStream.getInstance().toXML(c));
         mEstComposition = c;
      }
   }

   /**
    * Constructs a ExperimentOptimizationWIzard
    * 
    * @param owner
    * @param detector
    * @param session
    */
   public OptimizationWizard(final Frame owner, final EDSDetector detector, final Session session) {
      super(owner, "Optimize a measurement", true);
      this.setActivePanel(jWizardPanel_Introduction, "Select an optimization");
      this.enableFinish(false);
      this.enableNext(true);
      mDetector = detector;
      mSession = session;
      final Preferences prefs = Preferences.userNodeForPackage(OptimizationWizard.class);
      final String unk = prefs.get(UNKNOWN, null);
      Composition tmp = null;
      if(unk != null) {
         final Object obj = EPQXStream.getInstance().fromXML(unk);
         if(obj instanceof Composition)
            tmp = (Composition) obj;
      }
      if(tmp != null)
         mEstComposition = tmp;
      setNextPanel(jWizardPanel_Instrument, "Initialize EDS");
   }

   public String toHTML(File path) {
      switch(mMode) {
         case OptimizeEDS:
            return mOptimizer.toHTML();
         case OptimizeExpert:
            return mQuantPlan.toHTML(path, true) + "\n" + jWizardPanel_ExpertReport.getQuantResults();
         case FindStandards:
            return jWizardPanel_SelectMaterials.toHTML();
         default:
            assert false;
            return "";
      }

   }

   private Composition strToComp(final String compStr) {
      Composition comp = null;
      if(mSession != null)
         try {
            comp = mSession.findStandard(compStr.trim());
         }
         catch(final SQLException e1) {
            // assume it isn't in the database...
         }
      if(comp == null)
         try {
            comp = MaterialFactory.createCompound(compStr);
         }
         catch(final EPQException ex) {
            // Assume that it is a name not a compound
         }
      if(comp == null) {
         final Composition tmp = new Composition();
         tmp.setName(compStr);
         comp = MaterialsCreator.editMaterial(OptimizationWizard.this, tmp, mSession, false);
      }
      return comp;
   }
}
