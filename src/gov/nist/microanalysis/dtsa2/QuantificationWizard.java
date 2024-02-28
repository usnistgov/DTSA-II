package gov.nist.microanalysis.dtsa2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import gov.nist.microanalysis.EPQDatabase.ResultDialog;
import gov.nist.microanalysis.EPQDatabase.Session;
import gov.nist.microanalysis.EPQLibrary.AlgorithmUser;
import gov.nist.microanalysis.EPQLibrary.Composition;
import gov.nist.microanalysis.EPQLibrary.CompositionFromKRatios;
import gov.nist.microanalysis.EPQLibrary.CompositionFromKRatios.UnmeasuredElementRule;
import gov.nist.microanalysis.EPQLibrary.CorrectionAlgorithm;
import gov.nist.microanalysis.EPQLibrary.DuaneHuntLimit;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.Element;
import gov.nist.microanalysis.EPQLibrary.FromSI;
import gov.nist.microanalysis.EPQLibrary.ISpectrumData;
import gov.nist.microanalysis.EPQLibrary.KRatioSet;
import gov.nist.microanalysis.EPQLibrary.MassAbsorptionCoefficient;
import gov.nist.microanalysis.EPQLibrary.Material;
import gov.nist.microanalysis.EPQLibrary.MaterialFactory;
import gov.nist.microanalysis.EPQLibrary.QuantifyUsingSTEMinSEM;
import gov.nist.microanalysis.EPQLibrary.QuantifyUsingSTEMinSEM.Result;
import gov.nist.microanalysis.EPQLibrary.QuantifyUsingStandards;
import gov.nist.microanalysis.EPQLibrary.QuantifyUsingZetaFactors;
import gov.nist.microanalysis.EPQLibrary.RegionOfInterestSet;
import gov.nist.microanalysis.EPQLibrary.RegionOfInterestSet.RegionOfInterest;
import gov.nist.microanalysis.EPQLibrary.SampleShape;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;
import gov.nist.microanalysis.EPQLibrary.SpectrumSimulator;
import gov.nist.microanalysis.EPQLibrary.SpectrumUtils;
import gov.nist.microanalysis.EPQLibrary.StandardBundle;
import gov.nist.microanalysis.EPQLibrary.ToSI;
import gov.nist.microanalysis.EPQLibrary.XRayTransition;
import gov.nist.microanalysis.EPQLibrary.XRayTransitionSet;
import gov.nist.microanalysis.EPQLibrary.Detector.DetectorCalibration;
import gov.nist.microanalysis.EPQLibrary.Detector.DetectorProperties;
import gov.nist.microanalysis.EPQLibrary.Detector.EDSCalibration;
import gov.nist.microanalysis.EPQLibrary.Detector.EDSDetector;
import gov.nist.microanalysis.EPQLibrary.Detector.ElectronProbe;
import gov.nist.microanalysis.EPQTools.ErrorDialog;
import gov.nist.microanalysis.EPQTools.JStoichiometryTable;
import gov.nist.microanalysis.EPQTools.JTextFieldDouble;
import gov.nist.microanalysis.EPQTools.JWizardDialog;
import gov.nist.microanalysis.EPQTools.MaterialsCreator;
import gov.nist.microanalysis.EPQTools.SampleShapeDialog;
import gov.nist.microanalysis.EPQTools.SelectElements;
import gov.nist.microanalysis.EPQTools.SimpleFileFilter;
import gov.nist.microanalysis.EPQTools.SpectrumFile;
import gov.nist.microanalysis.EPQTools.SpectrumFileChooser;
import gov.nist.microanalysis.EPQTools.SpectrumPropertyPanel;
import gov.nist.microanalysis.Utility.DescriptiveStatistics;
import gov.nist.microanalysis.Utility.EachRowEditor;
import gov.nist.microanalysis.Utility.ElementComboBoxModel;
import gov.nist.microanalysis.Utility.HalfUpFormat;
import gov.nist.microanalysis.Utility.LazyEvaluate;
import gov.nist.microanalysis.Utility.Math2;
import gov.nist.microanalysis.Utility.Pair;
import gov.nist.microanalysis.Utility.Transform3D;
import gov.nist.microanalysis.Utility.UncertainValue2;

/**
 * <p>
 * A wizard for guiding users through the process of quantifying microanalytical
 * data. Depending upon the path the user specifies, various different panels
 * will ask the user to specify pieces of input information.
 * </p>
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
public class QuantificationWizard extends JWizardDialog {

   private static final long serialVersionUID = -6603251642619377671L;

   public enum ResultMode {
      WEIGHT_PERCENT, NORM_WEIGHT_PERCENT, ATOMIC_PERCENT
   }

   abstract private class JQuantPath extends JWizardPath {
      protected double mBeamEnergy; // in SI
      protected EDSDetector mDetector;

      /**
       * @return in Joules (SI)
       */
      public double getBeamEnergy() {
         return mBeamEnergy;
      }

      public EDSDetector getDetector() {
         return mDetector;
      }

      public ElectronProbe getProbe() {
         return mDetector != null ? mDetector.getOwner() : null;
      }
   }

   private class KRatioPath extends JWizardPath {

      protected class KConditionsPanel extends JWizardPanel {
         static private final long serialVersionUID = 0x45;
         private final JLabel jLabel_Intro = new JLabel("<html>Please specify the experimental conditions under which the k-ratios were measured.");
         private final JLabel jLabel_Energy = new JLabel("Beam energy");
         private final JLabel jLabel_TakeOff = new JLabel("Take-off angle");
         private final JLabel jLabel_Tilt = new JLabel("Sample tilt");

         private final JTextField jTextField_Energy = new JTextField();
         private final JTextField jTextField_TakeOff = new JTextField();
         private final JTextField jTextField_Tilt = new JTextField();
         private final JCheckBox jCheckBox_RefTilted = new JCheckBox("Inclined standard");

         private final JLabel jLabel_EnergyUnit = new JLabel("keV");
         private final JLabel jLabel_TakeOffUnit = new JLabel("\u00B0 (degrees)");
         private final JLabel jLabel_TiltUnit = new JLabel("\u00B0 (degrees)");

         private double mEnergy = ToSI.keV(20.0);
         private double mTilt = Math.toRadians(0.0);
         private double mTakeOffAngle = Math.toRadians(40.0);
         private boolean mRefTilted = true;

         private static final double TILT_THRESH = 0.1; // degrees
         private static final double TAKE_OFF_THRESH = 0.1; // degrees

         private void validateRefTiltedCheck() {
            double tilt, takeOff;
            final NumberFormat nf = NumberFormat.getInstance();
            try {
               tilt = Math.abs(nf.parse(jTextField_Tilt.getText()).doubleValue());
            } catch (final ParseException e1) {
               tilt = mTilt;
            }
            try {
               takeOff = Math.abs(nf.parse(jTextField_TakeOff.getText()).doubleValue());
            } catch (final ParseException e1) {
               takeOff = mTakeOffAngle;
            }
            final boolean mustCheck = (takeOff <= TAKE_OFF_THRESH);
            final boolean shouldntCheck = (tilt <= TILT_THRESH);
            final boolean shouldEnable = !(shouldntCheck || mustCheck);
            if (shouldEnable != jCheckBox_RefTilted.isEnabled()) {
               jCheckBox_RefTilted.setEnabled(shouldEnable);
               jCheckBox_RefTilted.setSelected(true);
            }
            if (mustCheck)
               jCheckBox_RefTilted.setSelected(true);
         }

         protected KConditionsPanel(JWizardDialog wiz) {
            super(wiz, "Specify experimental conditions",
                  new FormLayout("right:100dlu, 10dlu, 30dlu, 2dlu, 50dlu", "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"));
            final CellConstraints cc = new CellConstraints();
            add(jLabel_Intro, cc.xyw(1, 1, 5));
            add(jLabel_Energy, cc.xy(1, 3));
            add(jTextField_Energy, cc.xy(3, 3));
            add(jLabel_EnergyUnit, cc.xy(5, 3));
            add(jLabel_TakeOff, cc.xy(1, 5));
            add(jTextField_TakeOff, cc.xy(3, 5));
            add(jLabel_TakeOffUnit, cc.xy(5, 5));
            add(jLabel_Tilt, cc.xy(1, 7));
            add(jTextField_Tilt, cc.xy(3, 7));
            add(jLabel_TiltUnit, cc.xy(5, 7));
            add(jCheckBox_RefTilted, cc.xyw(3, 9, 3));

            jTextField_Energy.setToolTipText("<html>The energy at which the electrons strike the sample in kiloelectron volts (keV).");
            jTextField_TakeOff.setToolTipText(
                  "<html>The elevation angle of the detector measured from the<br>" + "plane perpendicular to the incident beam (in degrees).");
            jTextField_Tilt.setToolTipText("<html>The inclination of the sample.  Zero degrees represents the<br>"
                  + "beam striking normal to the surface.  Positive angles represent<br>"
                  + "tilt towards the detector and negative angles represent tilts<br>" + "away fromt the detector.");
            jCheckBox_RefTilted.setToolTipText("<html><b>Only relevant if the sample is inclined.</b> Was the standard tilted at the same<br>"
                  + "angle as the unknown? Alternatively, the standard may be mounted normal to the beam.<br>"
                  + "Usually, it is best if the standards are recorded at the same inclination as the unknown.");

            jTextField_Energy.setBackground(SystemColor.window);
            jTextField_TakeOff.setBackground(SystemColor.window);
            jTextField_Tilt.setBackground(SystemColor.window);
            // Select the text to facilitate editing
            {
               final FocusListener fl = new FocusAdapter() {
                  @Override
                  public void focusGained(FocusEvent e) {
                     final Component c = e.getComponent();
                     if (c instanceof JTextField) {
                        final JTextField tf = (JTextField) c;
                        tf.selectAll();
                     }
                  }
               };
               jTextField_Energy.addFocusListener(fl);
            }
            // Select the text to facilitate editing
            {
               final FocusListener fl = new FocusAdapter() {
                  @Override
                  public void focusGained(FocusEvent e) {
                     final Component c = e.getComponent();
                     if (c instanceof JTextField) {
                        final JTextField tf = (JTextField) c;
                        tf.selectAll();
                     }
                  }

                  @Override
                  public void focusLost(FocusEvent e) {
                     validateRefTiltedCheck();
                     if ((e.getComponent() == jTextField_Tilt) && jCheckBox_RefTilted.isEnabled())
                        jCheckBox_RefTilted.requestFocusInWindow();
                  }
               };
               jTextField_Tilt.addFocusListener(fl);
               jTextField_TakeOff.addFocusListener(fl);

            }
         }

         @Override
         public void onShow() {
            assert QuantificationWizard.this.mQuantMode == QuantMode.KRATIO;
            getWizard().setNextPanel(jWizardPanel_Standard.get());
            getWizard().enableFinish(false);
            getWizard().setMessageText("Specify the conditions under which the data was collected.");
            final NumberFormat nf = NumberFormat.getInstance();
            jTextField_Energy.setText(nf.format(FromSI.keV(mEnergy)));
            jTextField_Tilt.setText(nf.format(Math.toDegrees(mTilt)));
            jTextField_TakeOff.setText(nf.format(Math.toDegrees(mTakeOffAngle)));
            validateRefTiltedCheck();
            jTextField_Energy.requestFocusInWindow();
         }

         @Override
         public boolean permitNext() {
            boolean res = true;
            final NumberFormat nf = NumberFormat.getInstance();
            try {
               jTextField_TakeOff.setBackground(SystemColor.window);
               final double angle = Math.toRadians(nf.parse(jTextField_TakeOff.getText()).doubleValue());
               if ((angle < (-Math.PI / 2.0)) || (angle > (Math.PI / 2.0)))
                  throw new EPQException("Take-off angle should be between -90\u00B0 and 90\u00B0.");
               mTakeOffAngle = angle;
            } catch (final Exception e) {
               getWizard().setErrorText(e.getMessage());
               jTextField_TakeOff.setBackground(Color.PINK);
               jTextField_TakeOff.requestFocusInWindow();
               res = false;
            }
            try {
               jTextField_Tilt.setBackground(SystemColor.window);
               final double angle = Math.toRadians(nf.parse(jTextField_Tilt.getText()).doubleValue());
               if ((angle < (-Math.PI / 2.0)) || (angle > (Math.PI / 2.0)))
                  throw new EPQException("Tilt angle should be between -90\u00B0 and 90\u00B0.");
               mTilt = angle;
            } catch (final Exception e) {
               getWizard().setErrorText(e.getMessage());
               jTextField_Tilt.setBackground(Color.PINK);
               jTextField_Tilt.requestFocusInWindow();
               res = false;
            }
            try {
               jTextField_Energy.setBackground(SystemColor.window);
               final double e = ToSI.keV(nf.parse(jTextField_Energy.getText()).doubleValue());
               if ((e < ToSI.keV(0.1)) || (e > ToSI.keV(100.0)))
                  throw new EPQException("The beam energy should be between 0.1 keV and 100 keV");
               mEnergy = e;
            } catch (final Exception e) {
               getWizard().setErrorText(e.getMessage());
               jTextField_Energy.setBackground(Color.PINK);
               jTextField_Energy.requestFocusInWindow();
               res = false;
            }
            validateRefTiltedCheck();
            mRefTilted = jCheckBox_RefTilted.isSelected();
            if ((mTakeOffAngle + (mRefTilted ? mTilt : 0.0)) <= Math.toRadians(TAKE_OFF_THRESH)) {
               getWizard().setErrorText("The take-off angle plus the tilt must be greater than 0\u00B0.");
               res = false;
            }
            return res;
         }

         /**
          * Gets the current value assigned to energy
          *
          * @return Returns the energy (in Joules)
          */
         public double getEnergy() {
            return mEnergy;
         }

         /**
          * Gets the current value assigned to takeOffAngle
          *
          * @return Returns the takeOffAngle in radians.
          */
         public double getTakeOffAngle() {
            return mTakeOffAngle;
         }

         /**
          * Gets the current value assigned to tilt
          *
          * @return Returns the tilt.
          */
         public double getTilt() {
            return mTilt;
         }

         /**
          * Is the standard normal or tilted?
          *
          * @return true if the standard is tilted at the same angle as the
          *         unknown
          */
         public boolean isStandardTilted() {
            return mRefTilted;
         }

      }

      private class KStandardPanel extends JWizardPanel {

         private static final long serialVersionUID = -3084284046850546804L;

         private final class EditCellAction implements ActionListener {
            private Element mPrevElement = Element.None;

            @Override
            public void actionPerformed(ActionEvent e) {
               @SuppressWarnings("unchecked")
               final JComboBox<Element> ecb = (JComboBox<Element>) e.getSource();
               final Element curr = (Element) ecb.getSelectedItem();
               if (curr != mPrevElement) {
                  final SortedSet<Element> unused = unusedElements();
                  unused.remove(curr);
                  int row = -1;
                  // Update all the other element list boxes to remove curr
                  for (int r = jTableModel_Standard.getRowCount() - 1; r >= 0; --r) {
                     @SuppressWarnings("unchecked")
                     final JComboBox<Element> cb = (JComboBox<Element>) ((DefaultCellEditor) jEachRowEditor_ElementEditor.getEditorAt(r))
                           .getComponent();
                     if (e.getSource() == cb) {
                        row = r;
                        continue;
                     }
                     Element elm = getElementCell(r);
                     if (elm.equals(Element.None))
                        elm = unused.first();
                     unused.add(elm);
                     cb.setModel(new ElementComboBoxModel(unused));
                     unused.remove(elm);
                  }
                  mPrevElement = curr;
                  updateMaterialList(row);
               }
            }
         }

         private final class SelectElementsFromTableAction extends AbstractAction {
            private static final long serialVersionUID = 3147221797563173659L;

            @Override
            public void actionPerformed(ActionEvent e) {
               final SelectElements se = new SelectElements(QuantificationWizard.this, "Select elements...");
               if (jTable_Standards.getRowCount() > 1)
                  for (int r = 0; r < jTable_Standards.getRowCount(); ++r) {
                     final Object obj = jTable_Standards.getValueAt(r, ELEMENT_COL);
                     if (obj instanceof Element)
                        se.setSelected((Element) obj);
                  }
               se.setLocationRelativeTo(QuantificationWizard.this);
               se.setVisible(true);
               final Set<Element> elms = se.getElements();
               jTable_Standards.editCellAt(-1, -1);
               for (int r = jTable_Standards.getRowCount() - 1; r >= 0; --r) {
                  final Object obj = jTable_Standards.getValueAt(r, ELEMENT_COL);
                  if (elms.contains(obj))
                     elms.remove(obj);
                  else
                     jTableModel_Standard.removeRow(r);
               }
               for (final Element elm : elms) {
                  addRow(elm);
                  updateMaterialList(jTable_Standards.getRowCount() - 1);
               }
            }
         }

         private final class ClearStandardsAction extends AbstractAction {
            final static private long serialVersionUID = 0x1;

            @Override
            public void actionPerformed(ActionEvent ae) {
               for (int r = jTable_Standards.getRowCount() - 1; r >= 0; --r)
                  jTableModel_Standard.removeRow(r);
               addBlankRow();
            }
         }

         private final class RemoveStandardAction extends AbstractAction {
            final static private long serialVersionUID = 0x1;

            @Override
            public void actionPerformed(ActionEvent ae) {
               final int r = jTable_Standards.getSelectedRow();
               jTable_Standards.getSelectionModel().clearSelection();
               if ((r >= 0) && (r < jTable_Standards.getRowCount()))
                  jTableModel_Standard.removeRow(r);
               if (jTable_Standards.getRowCount() == 0)
                  addBlankRow();
               invalidate();
            }
         }

         private final class AddBlankRowAction extends AbstractAction {
            final static private long serialVersionUID = 0x1;

            @Override
            public void actionPerformed(ActionEvent ae) {
               addBlankRow();
            }
         }

         private final class UpdateMaterialListAction extends AbstractAction {
            final static private long serialVersionUID = 0x1;

            @Override
            public void actionPerformed(ActionEvent ae) {
               @SuppressWarnings("unchecked")
               final JComboBox<Composition> cb = (JComboBox<Composition>) ae.getSource();
               final int r = jTable_Standards.getSelectedRow();
               if ((r == -1) || (getElementCell(r).equals(Element.None)))
                  return;
               if (cb.getSelectedIndex() == (cb.getItemCount() - 1)) {
                  // New material...
                  final Composition newMat = MaterialsCreator.createMaterial(getWizard(), mSession, false);
                  if (newMat != null) {
                     final Element el = getElementCell(r);
                     if (newMat.containsElement(el)) {
                        cb.insertItemAt(newMat, 0);
                        cb.setSelectedIndex(0);
                        jTable_Standards.getModel().setValueAt(newMat, jTable_Standards.getSelectedRow(), COMPOSITION_COL);
                        getWizard().setMessageText("Material " + newMat.toString() + " created.");
                     } else {
                        Toolkit.getDefaultToolkit().beep();
                        getWizard().setErrorText("This material does not contain the specified element.");
                     }
                  } else
                     getWizard().setErrorText("No material constructed.");
               }
            }
         }

         KStandardPanel(JWizardDialog wiz) {
            super(wiz, "Specify the standards", new FormLayout("250dlu, 10dlu, pref", "top:120dlu"));
            try {
               initialize();
            } catch (final Exception ex) {
               ex.printStackTrace();
            }
         }

         private void initialize() {
            final CellConstraints cc = new CellConstraints();
            jTable_Standards = new JTable(jTableModel_Standard);
            // jTable_Standards.setForeground(SystemColor.textText);
            // Element column
            jEachRowEditor_ElementEditor = new EachRowEditor(jTable_Standards);
            jTable_Standards.getColumnModel().getColumn(ELEMENT_COL).setCellEditor(jEachRowEditor_ElementEditor);
            // Composition column
            jEachRowEditor_CompositionEditor = new EachRowEditor(jTable_Standards);
            jTable_Standards.getColumnModel().getColumn(COMPOSITION_COL).setCellEditor(jEachRowEditor_CompositionEditor);
            {
               final TableColumn col2 = jTable_Standards.getColumnModel().getColumn(CURRENT_COL);
               final JTextField tf = new JTextField();
               tf.setToolTipText("Enter the average probe current during the acquisition of the standard.");
               col2.setCellEditor(new DefaultCellEditor(tf));
            }
            final JScrollPane scrollPane = new JScrollPane(jTable_Standards);
            addBlankRow();
            add(scrollPane, cc.xy(1, 1));
            {
               final JPanel btnPanel = new JPanel(new FormLayout("pref", "pref, 4dlu, pref, 4dlu, pref, 10dlu, pref"));
               final JButton add = new JButton("Add");
               add.addActionListener(new AddBlankRowAction());
               final JButton remove = new JButton("Remove");
               remove.addActionListener(new RemoveStandardAction());
               final JButton clear = new JButton("Clear");
               clear.addActionListener(new ClearStandardsAction());
               final JButton multi = new JButton("Table");
               multi.addActionListener(new SelectElementsFromTableAction());
               btnPanel.add(add, cc.xy(1, 1));
               btnPanel.add(remove, cc.xy(1, 3));
               btnPanel.add(clear, cc.xy(1, 5));
               btnPanel.add(multi, cc.xy(1, 7));
               add(btnPanel, cc.xy(3, 1));
            }
         }

         @Override
         public void onShow() {
            assert QuantificationWizard.this.mQuantMode == QuantMode.KRATIO;
            getWizard().setMessageText("Specify standard materials for each element in the unknown.");
            getWizard().setNextPanel(jWizardPanel_KRatio.get());
            getWizard().enableFinish(false);
         }

         @Override
         public boolean permitNext() {
            mMaterialMap = new TreeMap<>();
            mCurrentMap = new TreeMap<>();
            try {
               final TableCellEditor tce = jTable_Standards.getCellEditor();
               if (tce != null)
                  tce.stopCellEditing();
               assert !jTable_Standards.isEditing();
               for (int r = 0; r < jTable_Standards.getRowCount(); ++r) {
                  final Element el = (Element) jTable_Standards.getValueAt(r, ELEMENT_COL);
                  final Composition comp = (Composition) jTable_Standards.getValueAt(r, COMPOSITION_COL);
                  final String current = (String) jTable_Standards.getValueAt(r, CURRENT_COL);
                  final Number iProbe = NumberFormat.getInstance().parse(current);
                  if ((el == Element.None) || (comp == NO_MATERIAL))
                     throw new EPQException("You must specify an element and an associated standard material.");
                  if (iProbe.doubleValue() <= 1.0e-6)
                     throw new EPQException("The probe current must be larger than 1 fA");
                  if (iProbe.doubleValue() >= 1.0e6)
                     throw new EPQException("The probe current must be less than 1 mA");
                  mMaterialMap.put(el, comp);
                  mCurrentMap.put(el, iProbe);
               }
               if (mMaterialMap.size() == 0)
                  throw new EPQException("You must specify at least one element, material and probe current.");
               return true;
            } catch (final Exception e) {
               getWizard().setErrorText(e.getMessage());
               mMaterialMap = null;
               mCurrentMap = null;
            }
            return false;
         }

         public Map<Element, Composition> getReferences() {
            return Collections.unmodifiableMap(mMaterialMap);
         }

         public Map<Element, Number> getProbeCurrents() {
            return Collections.unmodifiableMap(mCurrentMap);
         }

         static private final int ELEMENT_COL = 0;
         static private final int COMPOSITION_COL = 1;
         static private final int CURRENT_COL = 2;

         private final Material NO_MATERIAL = (Material) MaterialFactory.createMaterial(MaterialFactory.Nothing);

         private Map<Element, Composition> mMaterialMap;
         private Map<Element, Number> mCurrentMap;

         private final DefaultTableModel jTableModel_Standard = new DefaultTableModel(new Object[]{"Element", "Material", "Probe (nA)"}, 0);
         private JTable jTable_Standards;
         private EachRowEditor jEachRowEditor_ElementEditor;
         private EachRowEditor jEachRowEditor_CompositionEditor;

         private void addRow(Element elm) {
            final SortedSet<Element> unused = unusedElements();
            jTableModel_Standard.addRow(new Object[]{elm, NO_MATERIAL, "1.0"});
            final int r = jTableModel_Standard.getRowCount() - 1;
            {
               final JComboBox<Element> ecb = new JComboBox<>();
               ecb.setModel(new ElementComboBoxModel(unused));
               ecb.addActionListener(new EditCellAction());
               jEachRowEditor_ElementEditor.setEditorAt(r, new DefaultCellEditor(ecb));
            }
            jTable_Standards.getModel().setValueAt(elm, r, ELEMENT_COL);
            jTable_Standards.setEditingRow(r);
            jTable_Standards.setEditingColumn(ELEMENT_COL);
            jTable_Standards.requestFocusInWindow();
         }

         private void addBlankRow() {
            addRow(unusedElements().first());
         }

         private TreeSet<Element> unusedElements() {
            final TreeSet<Element> elms = new TreeSet<>(Element.allElements());
            for (int r = 0; r < jTableModel_Standard.getRowCount(); ++r)
               elms.remove(getElementCell(r));
            return elms;
         }

         private void updateMaterialList(int r) {
            if (r >= 0) {
               final Element el = getElementCell(r);
               {
                  final JComboBox<Composition> cb = new JComboBox<>();
                  cb.addActionListener(new UpdateMaterialListAction());
                  if (el != Element.None) {
                     final Vector<Composition> comps = new Vector<>(MaterialFactory.getCommonStandards(el));
                     comps.add(NEW_MATERIAL);
                     cb.setModel(new DefaultComboBoxModel<>(comps));
                     jTable_Standards.getModel().setValueAt(comps.get(0), r, COMPOSITION_COL);
                  }
                  jEachRowEditor_CompositionEditor.setEditorAt(r, new DefaultCellEditor(cb));
               }
            }
         }

         private Element getElementCell(int r) {
            Element el = Element.None;
            final Object val = jTableModel_Standard.getValueAt(r, ELEMENT_COL);
            if (val instanceof Element)
               el = (Element) val;
            else if (val instanceof String)
               el = Element.byName((String) val);
            return el;
         }

      }

      protected class KRatioPanel extends JWizardPanel {
         static private final long serialVersionUID = 0x45;
         JTable mTable;
         static private final int ELEMENT_COL = 0;
         static private final int LINE_COL = 1;
         static private final int KRATIO_COL = 2;
         static private final int CURRENT_COL = 3;

         private TreeMap<Element, XRayTransitionSet> mTransitionMap;
         private TreeMap<Element, Number> mKRatioMap;
         private TreeMap<Element, Number> mCurrentMap;

         DefaultTableModel jTableModel_KRatio = new DefaultTableModel(new String[]{"Element", "Line", "K-Ratio", "Probe (nA)"}, 0) {
            static private final long serialVersionUID = 0xa34d66666L;

            @Override
            public boolean isCellEditable(int row, int col) {
               return col != ELEMENT_COL;
            }
         };

         private EachRowEditor jEachRowEditor_TransitionEditor;
         private JTextField jTextField_KRatio;
         private JTextField jTextField_Current;

         private void addRow(Element el) {
            jTableModel_KRatio.addRow(new Object[]{el, XRayTransitionSet.EMPTY, "0.0", "1.0"});
            final ArrayList<String> c = XRayTransitionSet.getBasicFamilies(el, 0.95 * jWizardPanel_Conditions.get().getEnergy());
            final DefaultComboBoxModel<String> dcmb = new DefaultComboBoxModel<>(c.toArray(new String[0]));
            final JComboBox<String> jcb = new JComboBox<>(dcmb);
            final int row = jTableModel_KRatio.getRowCount() - 1;
            jEachRowEditor_TransitionEditor.setEditorAt(row, new DefaultCellEditor(jcb));
            if (!c.isEmpty())
               jTableModel_KRatio.setValueAt(c.iterator().next(), row, LINE_COL);
         }

         KRatioPanel(JWizardDialog wiz) {
            super(wiz, "Specify lines and k-ratios", new FormLayout("240dlu", "top:120dlu"));
            final CellConstraints cc = new CellConstraints();
            mTable = new JTable(jTableModel_KRatio);
            mTable.setSurrendersFocusOnKeystroke(true);
            // mTable.setForeground(SystemColor.textText);
            {
               final TableColumn col1 = mTable.getColumnModel().getColumn(LINE_COL);
               jEachRowEditor_TransitionEditor = new EachRowEditor(mTable);
               col1.setCellEditor(jEachRowEditor_TransitionEditor);
            }
            {
               final TableColumn col2 = mTable.getColumnModel().getColumn(KRATIO_COL);
               jTextField_KRatio = new JTextField();
               jTextField_KRatio.setToolTipText("<HTML>Enter the k-ratio relative to the standard as a decimal fraction (<i>not in percent</i>).");
               jTextField_KRatio.addFocusListener(new FocusAdapter() {
                  @Override
                  public void focusGained(FocusEvent e) {
                     jTextField_KRatio.selectAll();
                  }
               });
               jTextField_KRatio.addKeyListener(new KeyAdapter() {
                  @Override
                  public void keyPressed(KeyEvent e) {
                     final int key = e.getKeyCode();
                     if ((key == KeyEvent.VK_ENTER) || (key == KeyEvent.VK_TAB)) {
                        assert mTable.isEditing();
                        mTable.editCellAt(mTable.getEditingRow(), CURRENT_COL);
                     }
                  }
               });
               final DefaultCellEditor dce = new DefaultCellEditor(jTextField_KRatio);
               col2.setCellEditor(dce);
            }
            {
               final TableColumn col3 = mTable.getColumnModel().getColumn(CURRENT_COL);
               jTextField_Current = new JTextField();
               jTextField_Current.setToolTipText("Enter the average probe current during the acquisition of the unknown material.");
               jTextField_Current.addFocusListener(new FocusAdapter() {

                  @Override
                  public void focusGained(FocusEvent e) {
                     jTextField_Current.selectAll();
                  }
               });
               jTextField_Current.addKeyListener(new KeyAdapter() {
                  @Override
                  public void keyPressed(KeyEvent e) {
                     final int key = e.getKeyCode();
                     if ((key == KeyEvent.VK_ENTER) || (key == KeyEvent.VK_TAB)) {
                        assert mTable.isEditing();
                        mTable.editCellAt((mTable.getEditingRow() + 1) % mTable.getRowCount(), KRATIO_COL);
                     }
                  }
               });
               col3.setCellEditor(new DefaultCellEditor(jTextField_Current));
            }

            final JScrollPane scrollPane = new JScrollPane(mTable);

            add(scrollPane, cc.xy(1, 1));
         }

         @Override
         public void onShow() {
            assert QuantificationWizard.this.mQuantMode == QuantMode.KRATIO;
            // Add any elements that are not currently displayed
            final ArrayList<Element> elms = new ArrayList<>(jWizardPanel_Standard.get().getReferences().keySet());
            for (int i = mTable.getRowCount() - 1; i >= 0; --i) {
               final Element el = (Element) mTable.getValueAt(i, ELEMENT_COL);
               if (elms.contains(el))
                  elms.remove(el);
               else
                  jTableModel_KRatio.removeRow(i);
            }
            if (elms.size() > 0)
               for (final Element elm : elms)
                  addRow(elm);
            // Place the edit focus on the first k-ratio
            if (mTable.getRowCount() > 0) {
               mTable.requestFocusInWindow();
               mTable.setRowSelectionInterval(0, 0);
               mTable.editCellAt(0, KRATIO_COL);
               jTextField_KRatio.requestFocusInWindow();
            }
            getWizard().setNextPanel(jWizardPanel_Results.get());
            getWizard().enableFinish(false);
            getWizard().setMessageText("Specify the line, k-ratio and probe current for each element.");
         }

         @Override
         public boolean permitNext() {
            final NumberFormat nf = NumberFormat.getInstance();
            mTransitionMap = new TreeMap<>();
            mKRatioMap = new TreeMap<>();
            mCurrentMap = new TreeMap<>();
            // Cancel table editing when active...
            final TableCellEditor tce = mTable.getCellEditor();
            if (tce != null)
               tce.stopCellEditing();
            assert !mTable.isEditing();
            try {
               // double kRsum = 0.0;
               for (int i = mTable.getRowCount() - 1; i >= 0; --i) {
                  final Element el = (Element) mTable.getValueAt(i, ELEMENT_COL);
                  assert (el.isValid());
                  final XRayTransitionSet xrts = new XRayTransitionSet(el, (String) mTable.getValueAt(i, LINE_COL));
                  Number kRs, iS;
                  try {
                     kRs = nf.parse((String) mTable.getValueAt(i, KRATIO_COL));
                  } catch (final ParseException pe) {
                     throw new EPQException("Error parsing the k-ratio for " + el.toString());
                  }
                  if ((kRs.doubleValue() < 0.0) || (kRs.doubleValue() > 1000.0))
                     throw new EPQException("The k-ratio must be zero or larger but less than 1000.0.");
                  // kRsum += kRs.doubleValue();
                  try {
                     iS = nf.parse((String) mTable.getValueAt(i, CURRENT_COL));
                  } catch (final ParseException e) {
                     throw new EPQException("Error parsing the probe current for " + el.toString());
                  }
                  if ((iS.doubleValue() < 1.0e-6) || (iS.doubleValue() > 1.0e6))
                     throw new EPQException("<html>The probe current must be larger than 10<sup>-6</sup> nA and smaller than 10<sup>6</sup> nA.");
                  mTransitionMap.put(el, xrts);
                  mKRatioMap.put(el, kRs);
                  mCurrentMap.put(el, iS);
               }
               return true;
            } catch (final EPQException e) {
               getWizard().setErrorText(e.getMessage());
            }
            return false;
         }

         public KRatioSet getKRatioSet() {
            final KRatioSet krs = new KRatioSet();
            for (int r = 0; r < jTableModel_KRatio.getRowCount(); ++r) {
               final Element el = (Element) jTableModel_KRatio.getValueAt(r, 0);
               if (!el.isValid())
                  continue;
               final XRayTransitionSet xrts = new XRayTransitionSet(el, (String) jTableModel_KRatio.getValueAt(r, 1));
               if (xrts.size() == 0)
                  continue;
               try {
                  final double v = NumberFormat.getInstance().parse((String) jTableModel_KRatio.getValueAt(r, 2)).doubleValue();
                  krs.addKRatio(xrts, v, 0.0);
               } catch (final ParseException e) {
               }
            }
            return krs;
         }

         /**
          * Gets the current value assigned to currentMap
          *
          * @return Returns the currentMap.
          */
         public TreeMap<Element, Number> getCurrentMap() {
            return mCurrentMap;
         }

         /**
          * Gets the current value assigned to kRatioMap
          *
          * @return Returns the kRatioMap.
          */
         public TreeMap<Element, Number> getKRatioMap() {
            return mKRatioMap;
         }

         /**
          * Gets the current value assigned to transitionMap
          *
          * @return Returns the transitionMap.
          */
         public TreeMap<Element, XRayTransitionSet> getTransitionMap() {
            return mTransitionMap;
         }
      }

      private class KResultsPanel extends JWizardPanel {
         private final class CopyToClipboardLongAction extends AbstractAction {
            private static final long serialVersionUID = 0x46;

            @Override
            public void actionPerformed(ActionEvent ae) {
               final StringBuffer sb = new StringBuffer(2048);
               final NumberFormat deg = new HalfUpFormat("0.0\u00B0");
               final NumberFormat keV = new HalfUpFormat("0.00 keV");
               sb.append("Quantitative results from k-ratios\nConditions\n");
               sb.append("\tTilt        = " + deg.format(Math.toDegrees(jWizardPanel_Conditions.get().getTilt())) + "\n");
               sb.append("\tTake-off    = " + deg.format(Math.toDegrees(jWizardPanel_Conditions.get().getTakeOffAngle())) + "\n");
               sb.append("\tBeam Energy = " + keV.format(FromSI.keV(jWizardPanel_Conditions.get().getEnergy())) + "\n");
               sb.append(createLongComment() + "\n");
               final Clipboard clp = Toolkit.getDefaultToolkit().getSystemClipboard();
               final StringSelection ss = new StringSelection(sb.toString());
               clp.setContents(ss, ss);
            }
         }

         private final class CopyToClipboardShortAction extends AbstractAction {
            private static final long serialVersionUID = 0x46;

            @Override
            public void actionPerformed(ActionEvent ae) {
               final Clipboard clp = Toolkit.getDefaultToolkit().getSystemClipboard();
               final StringSelection ss = new StringSelection(createShortComment());
               clp.setContents(ss, ss);
            }
         }

         private static final long serialVersionUID = 0x46;
         private Composition mComposition;
         private JTable jTable_Results;
         private final DefaultTableModel jTableModel_Results = new DefaultTableModel(
               new Object[]{"Element", "Mass Frac", "Norm Mass Frac", "Atomic Frac"}, 0);
         private JLabel jLabel_Algorithm;
         private final JButton jButton_ShortClipboard = new JButton("Copy (Short)");
         private final JButton jButton_LongClipboard = new JButton("Copy (Long)");

         private final StringBuffer mHTMLResults = new StringBuffer(2048);

         private void computeQuantFromKRatio() throws EPQException {
            final SpectrumProperties props = new SpectrumProperties();
            final double[] ssn = Transform3D.rotate(Math2.MINUS_Z_AXIS, 0.0, -jWizardPanel_Conditions.get().getTilt(), 0.0);
            props.setSampleShape(SpectrumProperties.SampleShape, new SampleShape.Bulk(ssn));
            assert Math.abs(CorrectionAlgorithm.getTilt(props) - jWizardPanel_Conditions.get().getTilt()) < 1.0e-6;
            props.setNumericProperty(SpectrumProperties.WorkingDistance, 0.0);
            props.setDetectorPosition(jWizardPanel_Conditions.get().getTakeOffAngle(), 0.0, 50.0e-3, 0.0);
            assert Math.abs(SpectrumUtils.getTakeOffAngle(props) - jWizardPanel_Conditions.get().getTakeOffAngle()) < 1.0e-6;
            props.setNumericProperty(SpectrumProperties.BeamEnergy, FromSI.keV(jWizardPanel_Conditions.get().getEnergy()));
            final KRatioSet krs = jWizardPanel_KRatio.get().getKRatioSet();
            final CompositionFromKRatios czc = new CompositionFromKRatios();
            final SpectrumProperties refProps = new SpectrumProperties(props);
            if (!jWizardPanel_Conditions.get().isStandardTilted())
               refProps.setSampleShape(SpectrumProperties.SampleShape, new SampleShape.Bulk(Math2.MINUS_Z_AXIS));

            final Map<Element, Composition> refm = jWizardPanel_Standard.get().getReferences();
            final Map<Element, XRayTransitionSet> xrtm = jWizardPanel_KRatio.get().getTransitionMap();
            final Map<Element, Number> krm = jWizardPanel_KRatio.get().getKRatioMap();
            final Map<Element, Number> kI = jWizardPanel_KRatio.get().getCurrentMap();
            final Map<Element, Number> rI = jWizardPanel_Standard.get().getProbeCurrents();
            for (final Element el : refm.keySet()) {
               final XRayTransitionSet xrts = xrtm.get(el);
               czc.addStandard(xrts, refm.get(el), refProps);
               final double kr = ((krm.get(el)).doubleValue() * (rI.get(el)).doubleValue()) / (kI.get(el)).doubleValue();
               krs.addKRatio(xrts, kr, 0.0);
            }
            // czc.useAutomaticOptimalTransitions(false);
            mComposition = czc.compute(krs, props);
            final NumberFormat nf = new HalfUpFormat("0.00000");
            for (final Element el : mComposition.getElementSet()) {
               final double wgt = mComposition.weightFraction(el, false);
               final double norm = mComposition.weightFraction(el, true);
               final double atm = mComposition.atomicPercent(el);
               jTableModel_Results.addRow(new Object[]{el, nf.format(wgt), nf.format(norm), nf.format(atm)});
            }
            final CorrectionAlgorithm ca = AlgorithmUser.getDefaultCorrectionAlgorithm();
            final MassAbsorptionCoefficient mac = AlgorithmUser.getDefaultMAC();
            jLabel_Algorithm.setText("<html><b>Algorithm:</b> " + ca.getName() + "<br><b>MAC:</b> " + mac.getName());
            appendHTML();
         }

         private String createShortComment() {
            final StringBuffer[] sb = new StringBuffer[4];
            for (int i = 0; i < sb.length; ++i)
               sb[i] = new StringBuffer(256);
            sb[0].append("Element\t");
            sb[1].append("Mass Frac\t");
            sb[2].append("Norm Mass Frac\t");
            sb[3].append("Atomic Frac\t");
            final NumberFormat nf = new HalfUpFormat("0.00000");
            for (final Iterator<Element> i = mComposition.getElementSet().iterator(); i.hasNext();) {
               final Element el = i.next();
               sb[0].append(el.toString());
               sb[1].append(nf.format(mComposition.weightFraction(el, false)));
               sb[2].append(nf.format(mComposition.weightFraction(el, true)));
               sb[3].append(nf.format(mComposition.atomicPercent(el)));
               if (i.hasNext())
                  for (final StringBuffer element : sb)
                     element.append("\t");
            }
            for (int i = 1; i < sb.length; ++i) {
               sb[0].append("\n");
               sb[0].append(sb[i]);
            }
            return sb[0].toString();
         }

         private String createLongComment() {
            final StringBuffer[] sb = new StringBuffer[6];
            for (int i = 0; i < sb.length; ++i)
               sb[i] = new StringBuffer(256);
            sb[0].append("Element\t");
            sb[1].append("K-ratio\t");
            sb[2].append("Line\t");
            sb[3].append("Mass Frac\t");
            sb[4].append("Norm Mass Frac\t");
            sb[5].append("Atomic Frac\t");
            final NumberFormat nf = new HalfUpFormat("0.00000");
            final NumberFormat nf2 = new HalfUpFormat("0.0000");
            final Map<Element, Number> krs = jWizardPanel_KRatio.get().getKRatioMap();
            final Map<Element, XRayTransitionSet> trm = jWizardPanel_KRatio.get().getTransitionMap();
            for (final Iterator<Element> i = mComposition.getElementSet().iterator(); i.hasNext();) {
               final Element el = i.next();
               sb[0].append(el.toString());
               sb[1].append(nf2.format(krs.get(el)));
               sb[2].append((trm.get(el)).toString());
               sb[3].append(nf.format(mComposition.weightFraction(el, false)));
               sb[4].append(nf.format(mComposition.weightFraction(el, true)));
               sb[5].append(nf.format(mComposition.atomicPercent(el)));
               if (i.hasNext())
                  for (final StringBuffer element : sb)
                     element.append("\t");
            }
            for (int i = 1; i < sb.length; ++i) {
               sb[0].append("\n");
               sb[0].append(sb[i]);
            }
            return sb[0].toString();
         }

         private void appendHTML() {
            final NumberFormat nf = new HalfUpFormat("0.00000");
            final NumberFormat nf2 = new HalfUpFormat("0.0000");
            mHTMLResults.append("<H2>Quantitative results: Composition from k-ratios</H2>");
            { // Header
               final NumberFormat deg = new HalfUpFormat("0.0\u00B0");
               final NumberFormat keV = new HalfUpFormat("0.00 keV");
               mHTMLResults.append("<P><TABLE>\n");
               mHTMLResults.append(" <TR><TH COLSPAN=3>Conditions</TH></TR>\n");
               mHTMLResults.append(" <TR><TD>Tilt</TD><TD WIDTH=10></TD><TD>" + deg.format(Math.toDegrees(jWizardPanel_Conditions.get().getTilt()))
                     + "</TD></TR>\n");
               mHTMLResults.append(" <TR><TD>Take-off</TD><TD WIDTH=10></TD><TD>"
                     + deg.format(Math.toDegrees(jWizardPanel_Conditions.get().getTakeOffAngle())) + "</TD></TR>\n");
               mHTMLResults.append(" <TR><TD>Beam Energy</TD><TD WIDTH=10></TD><TD>"
                     + keV.format(FromSI.keV(jWizardPanel_Conditions.get().getEnergy())) + "</TD></TR>\n");
               mHTMLResults.append("</TABLE></P>\n");
               mHTMLResults.append("<P><TABLE>\n");
               mHTMLResults.append(" <TR><TH COLSPAN=3>Algorithms</TH></TR>\n");
               {
                  final CorrectionAlgorithm ca = AlgorithmUser.getDefaultCorrectionAlgorithm();
                  if (ca != null)
                     mHTMLResults.append(" <TR><TD>Correction</TD><TD WIDTH=10></TD><TD>" + ca.getName() + "</TD></TR>\n");
               }
               {
                  final MassAbsorptionCoefficient mac = AlgorithmUser.getDefaultMAC();
                  if (mac != null)
                     mHTMLResults.append(" <TR><TD>MAC</TD><TD WIDTH=10></TD><TD>" + mac.getName() + "</TD></TR>\n");
               }
               mHTMLResults.append("</TABLE></P>\n");
            }
            final TreeMap<Element, Number> krs = jWizardPanel_KRatio.get().getKRatioMap();
            final TreeMap<Element, XRayTransitionSet> trm = jWizardPanel_KRatio.get().getTransitionMap();
            final Set<Element> elms = krs.keySet();
            mHTMLResults.append("<P><TABLE>\n");
            mHTMLResults.append("<TR><TH COLSPAN=" + Integer.toString(elms.size() + 1) + ">Results</TH></TR>");
            mHTMLResults.append("<TR><TH>Element</TH>");
            for (final Element elm : elms)
               mHTMLResults.append("<TD>" + elm.toString() + "</TD>");
            mHTMLResults.append("</TR>\n");
            mHTMLResults.append("<TR><TH>Line</TH>");
            for (final Element elm : elms)
               mHTMLResults.append("<TD>" + trm.get(elm).toString() + "</TD>");
            mHTMLResults.append("</TR>\n");
            mHTMLResults.append("<TR><TH>K-ratio</TH>");
            for (final Element elm : elms)
               mHTMLResults.append("<TD>" + nf2.format(krs.get(elm)) + "</TD>");
            mHTMLResults.append("</TR>\n");
            mHTMLResults.append("<TR><TH>Mass Fraction</TH>");
            for (final Element elm : elms)
               mHTMLResults.append("<TD>" + nf.format(mComposition.weightFraction(elm, false)) + "</TD>");
            mHTMLResults.append("</TR>\n");
            mHTMLResults.append("<TR><TH>Normalized<br>Mass Fraction</TH>");
            for (final Element elm : elms)
               mHTMLResults.append("<TD>" + nf.format(mComposition.weightFraction(elm, true)) + "</TD>");
            mHTMLResults.append("</TR>\n");
            mHTMLResults.append("<TR><TH>Atomic Percent</TH>");
            for (final Element elm : elms)
               mHTMLResults.append("<TD>" + nf.format(mComposition.atomicPercent(elm)) + "</TD>");
            mHTMLResults.append("</TR>\n");
            mHTMLResults.append("</TABLE></P>\n");
         }

         KResultsPanel(JWizardDialog wiz) {
            super(wiz, "Specify k-ratios");
            try {
               initialize();
            } catch (final Exception ex) {
               ex.printStackTrace();
            }
         }

         private void initialize() {
            setLayout(new FormLayout("200dlu, 10dlu, pref", "top:100dlu, 5dlu, pref"));
            jTable_Results = new JTable(jTableModel_Results);
            // jTable_Results.setForeground(SystemColor.textText);
            jTable_Results.setEnabled(false);
            final JScrollPane pane = new JScrollPane(jTable_Results);
            final CellConstraints cc = new CellConstraints();
            add(pane, cc.xy(1, 1));
            jLabel_Algorithm = new JLabel("<html><b>Algorithm</b> ?<br><b>MAC</b> ?");
            add(jLabel_Algorithm, cc.xy(1, 3));

            final JPanel btnPanel = new JPanel(new FormLayout("pref", "pref, 5dlu, pref"));
            jButton_ShortClipboard.setToolTipText("Copy just the results to the clipboard.");
            jButton_ShortClipboard.addActionListener(new CopyToClipboardShortAction());

            jButton_LongClipboard.setToolTipText("Copy the input parameters and results to the clipboard.");
            jButton_LongClipboard.addActionListener(new CopyToClipboardLongAction());
            btnPanel.add(jButton_ShortClipboard, cc.xy(1, 1));
            btnPanel.add(jButton_LongClipboard, cc.xy(1, 3));
            add(btnPanel, cc.xy(3, 1));
         }

         @Override
         public void onShow() {
            assert QuantificationWizard.this.mQuantMode == QuantMode.KRATIO;
            try {
               jTableModel_Results.setRowCount(0);
               computeQuantFromKRatio();
               getWizard().setMessageText("The k-ratios specified suggest this composition.");
            } catch (final EPQException e) {
               getWizard().setErrorText(e.getMessage());
            }
            getWizard().enableFinish(true);
         }

         public String toHTML() {
            return mHTMLResults.toString();
         }
      }

      private final LazyEvaluate<KConditionsPanel> jWizardPanel_Conditions = new LazyEvaluate<>() {

         @Override
         protected KConditionsPanel compute() {
            return new KConditionsPanel(QuantificationWizard.this);
         }
      };

      private final LazyEvaluate<KRatioPanel> jWizardPanel_KRatio = new LazyEvaluate<>() {

         @Override
         protected KRatioPanel compute() {
            return new KRatioPanel(QuantificationWizard.this);
         }
      };

      private final LazyEvaluate<KResultsPanel> jWizardPanel_Results = new LazyEvaluate<>() {

         @Override
         protected KResultsPanel compute() {
            return new KResultsPanel(QuantificationWizard.this);
         }
      };
      private final LazyEvaluate<KStandardPanel> jWizardPanel_Standard = new LazyEvaluate<>() {

         @Override
         protected KStandardPanel compute() {
            return new KStandardPanel(QuantificationWizard.this);
         }
      };

      @Override
      public String toHTML() {
         return jWizardPanel_Results.get().toHTML();
      }

      @Override
      public JWizardPanel firstPanel() {
         return jWizardPanel_Conditions.get();
      }

   }

   private class LLSQPath extends JQuantPath {

      /**
       * Allows the user to specify an Instrument, Detector/Calibration and beam
       * energy for the unknown spectrum.
       */
      public class LLSQInstrumentPanel extends GenericInstrumentPanel {

         private static final long serialVersionUID = -5432773600725361704L;

         public LLSQInstrumentPanel(JWizardDialog wiz) {
            super(wiz, jWizardPanel_LLSQStandard.get());
         }

         @Override
         public boolean permitNext() {
            final boolean res = super.permitNext();
            if (res) {
               final EDSDetector det = buildDetector();
               final double beamEnergy = ToSI.keV(getBeamEnergy_keV());
               if ((mQuantUsingStandards == null) || (!Math2.approxEquals(mQuantUsingStandards.getBeamEnergy(), beamEnergy, 0.01))
                     || (det != mQuantUsingStandards.getDetector())) {
                  mQuantUsingStandards = new QuantifyUsingStandards(det, beamEnergy, false, AppPreferences.getInstance().useVariableFF());
                  mQuantUsingStandards.setOFudge(AppPreferences.getInstance().useOFudge());
                  LLSQPath.this.mBeamEnergy = beamEnergy;
                  LLSQPath.this.mDetector = det;
               }
            }

            return res;
         }

         @Override
         protected ElectronProbe getProbe() {
            return LLSQPath.this.getProbe();
         }

         @Override
         protected EDSDetector getDetector() {
            return LLSQPath.this.getDetector();
         }

      }

      private class LLSQStandardPanel extends BaseStandardPanel {

         private static final long serialVersionUID = 7170687200993717164L;

         public LLSQStandardPanel(QuantificationWizard parent) {
            super(parent, LLSQPath.this);
         }

         @Override
         public boolean permitNext() {
            try {
               if (jTable_Standards.getRowCount() > 0) {
                  final TableModel tm = jTable_Standards.getModel();
                  mQuantUsingStandards.clearStandards();
                  for (int r = 0; r < tm.getRowCount(); ++r) {
                     final Element elm = getElement(r);
                     final ISpectrumData spec = getSpectrum(r);
                     if (spec == null) {
                        getWizard().setErrorText("Specify a spectrum in row " + Integer.toString(r + 1));
                        return false;
                     }
                     final Composition comp = getComposition(r);
                     final boolean valid = comp.containsElement(elm);
                     if (!valid) {
                        getWizard().setErrorText("The material in row " + Integer.toString(r + 1) + " does not contain " + elm.toString());
                        return false;
                     }
                     try {
                        mQuantUsingStandards.addStandard(elm, comp, getStripElements(r).getElements(), spec);
                     } catch (final EPQException e) {
                        getWizard().setErrorText("ERROR: " + e.getMessage());
                        return false;
                     }
                  }
                  final Set<RegionOfInterest> reqRefs = mQuantUsingStandards.getAllRequiredReferences(false);
                  for (Map.Entry<RegionOfInterest, ISpectrumData> me : mReferencePool.entrySet()) {
                     RegionOfInterest roi = bestMatch(me.getKey(), reqRefs);
                     if ((roi != null) && (mQuantUsingStandards.getReference(roi) == null)) {
                        ISpectrumData ref = me.getValue();
                        try {
                           final Set<Element> elms = ref.getProperties().getElements();
                           if (mQuantUsingStandards.suitableAsReference(elms).contains(roi))
                              mQuantUsingStandards.addReference(roi, ref, elms);
                        } catch (EPQException e) {
                           ErrorDialog.createErrorMessage(QuantificationWizard.this, "Error setting reference from standard file.", e);
                        }
                     }
                  }

                  return true;
               }
            } catch (final RuntimeException e) {
               getWizard().setErrorText("Fix the error in this table.");
            }
            return false;
         }

         @Override
         public void onShow() {
            assert QuantificationWizard.this.mQuantMode == QuantMode.MLSQ;
            assert LLSQPath.this.getBeamEnergy() > ToSI.keV(0.1);
            assert LLSQPath.this.getBeamEnergy() < ToSI.keV(500.0);
            jButton_AddDatabase.setEnabled(mSession != null);
            // Clear and reenter all data in this panel...
            jTableModel_Standards.setRowCount(0);
            final Map<Element, ISpectrumData> stds = mQuantUsingStandards.getStandardSpectra();
            for (final ISpectrumData std : new TreeSet<>(stds.values())) {
               final Set<Element> elms = new TreeSet<>();
               for (final Map.Entry<Element, ISpectrumData> me : stds.entrySet())
                  if (me.getValue() == std)
                     elms.add(me.getKey());
               final Composition comp = std.getProperties().getCompositionWithDefault(SpectrumProperties.StandardComposition, null);
               assert comp != null : "Composition not set in onShow";
               for (Element elm : elms)
                  addRow(std, comp, elm, mQuantUsingStandards.getStripElements(elm));
            }
            setMessageText("Specify standard spectra and the associated elements and compositions.");
            setNextPanel(jWizardPanel_LLSQOther.get());
            enableFinish(false);
         }

      }

      /**
       * Allows the user to specify reference spectra for element in the
       * unknown.
       */
      private class LLSQReferencePanel extends JWizardPanel {

         private final class SelectStripElementAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
               selectStripElement();
            }
         }

         private final class ClearReferenceAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
               final int row = jTable_Reference.getSelectedRow();
               if (row != -1) {
                  jTableModel_Reference.setValueAt(MISSING, row, SPECTRUM_COL);
                  updateSignalToNoise(row);
               }
            }
         }

         private final class SpecifyReferenceFromDatabaseAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
               specifyReferenceFromDatabase();
            }
         }

         private final class SpecifyReferenceFileAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent ae) {
               specifyReferenceFile();
            }
         }

         private static final String MISSING = "<html><font color=red>Missing</font>";

         DefaultTableModel jTableModel_Reference = new DefaultTableModel(new Object[]{"Region-of-Interest", "Spectrum", "S/N"}, 0) {
            static private final long serialVersionUID = 0xa34d3243456L;

            @Override
            public boolean isCellEditable(int row, int col) {
               return false;
            }
         };

         private final int ROI_COL = 0;
         private final int SPECTRUM_COL = 1;
         private final int SN_COL = 2;

         private final JTable jTable_Reference = new JTable(jTableModel_Reference);
         private final JButton jButton_Specify = new JButton("File...");
         private final JButton jButton_SpecifyDB = new JButton("Database...");
         private final JButton jButton_Clear = new JButton("Remove");
         private final JButton jButton_Strip = new JButton("Strip");

         private static final long serialVersionUID = 386623234L;

         public LLSQReferencePanel(JWizardDialog wiz) {
            super(wiz, "Specify reference spectra", new FormLayout("250dlu, 10dlu, pref", "top:120dlu"));
            try {
               initialize();
            } catch (final Exception ex) {
               ex.printStackTrace();
            }
         }

         private void initialize() {
            final CellConstraints cc = new CellConstraints();
            // jTable_Reference.setForeground(SystemColor.textText);
            // jTable_Reference.setBackground(SystemColor.text);
            jTable_Reference.getColumnModel().getColumn(SN_COL).setPreferredWidth(10);
            jTable_Reference.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            add(new JScrollPane(jTable_Reference), cc.xy(1, 1));
            {
               final JPanel btnPanel = new JPanel(new FormLayout("pref", "pref, 3dlu, pref, 10dlu, pref, 10dlu, pref, 3dlu, pref"));
               jButton_Specify.addActionListener(new SpecifyReferenceFileAction());
               jButton_Specify
                     .setToolTipText("<html>Specify a different spectrum from a file to use as a shape<br>reference for the selected element(s).");
               btnPanel.add(jButton_Specify, cc.xy(1, 1));

               jButton_SpecifyDB.setToolTipText(
                     "<html>Specify a different spectrum from the database to use as a<br>shape reference for the selected element(s).");
               jButton_SpecifyDB.addActionListener(new SpecifyReferenceFromDatabaseAction());
               btnPanel.add(jButton_SpecifyDB, cc.xy(1, 3));

               jButton_Clear.addActionListener(new ClearReferenceAction());
               btnPanel.add(jButton_Clear, cc.xy(1, 5));
               jButton_Clear.setToolTipText(
                     "<html>Remove a transition family from the fitting process.<br>You must retain at least one reference per element.");

               jButton_Strip.addActionListener(new SelectStripElementAction());
               jButton_Strip.setToolTipText(
                     "<html>Specify an element to fit but not quantify.<br>You will also need to specify a reference spectrum for this element.");
               btnPanel.add(jButton_Strip, cc.xy(1, 7));

               add(btnPanel, cc.xy(3, 1));
            }
         }

         private void selectStripElement() {
            final SelectElements se = new SelectElements(QuantificationWizard.this, "Specify one or more element to strip.");
            for (final Element elm : mQuantUsingStandards.getStandards().keySet())
               se.setEnabled(elm, false);
            for (final UnmeasuredElementRule uer : mQuantUsingStandards.getUnmeasuredElementRules())
               se.setEnabled(uer.getElement(), false);
            se.setLocationRelativeTo(QuantificationWizard.this);
            se.setVisible(true);
            for (final Element elm : se.getElements())
               if (!mQuantUsingStandards.isStripped(elm)) {
                  mQuantUsingStandards.addElementToStrip(elm);
                  final RegionOfInterestSet rois = mQuantUsingStandards.getStandardROIS(elm);
                  boolean avail = false;
                  for (final RegionOfInterestSet.RegionOfInterest roi : rois) {
                     if (mQuantUsingStandards.getReference(roi) != null) {
                        avail = true;
                        break;
                     }
                     if (!avail) {
                        jTableModel_Reference.insertRow(0, new Object[]{roi, MISSING, "-"});
                        updateSignalToNoise(jTableModel_Reference.getRowCount() - 1);
                     }
                  }
               }
         }

         private void specifyReferenceFile() {
            final TreeMap<Integer, RegionOfInterestSet.RegionOfInterest> rois = new TreeMap<>();
            for (int r = jTable_Reference.getRowCount() - 1; r >= 0; --r)
               if (jTable_Reference.isRowSelected(r))
                  rois.put(Integer.valueOf(r), (RegionOfInterestSet.RegionOfInterest) jTable_Reference.getValueAt(r, ROI_COL));
            if (rois.size() > 0) {
               final StringBuffer msg = new StringBuffer("Select a reference for ");
               boolean first = true;
               for (final RegionOfInterestSet.RegionOfInterest roi : rois.values()) {
                  if (!first)
                     msg.append(", ");
                  msg.append(roi.toString());
                  first = false;
               }
               final ISpectrumData[] specs = selectSpectra(false);
               if (specs.length > 0) {
                  // TODO Pick one out of a multiple spectrum set
                  ISpectrumData spec = specs[0];
                  if (!SpectrumUtils.areCalibratedSimilar(mQuantUsingStandards.getDetector().getProperties(), spec,
                        AppPreferences.DEFAULT_TOLERANCE)) {
                     final int res = JOptionPane.showConfirmDialog(QuantificationWizard.this,
                           "<html></i>" + spec.toString() + "</i> does not seem to be calibrated similar to </i>"
                                 + mQuantUsingStandards.getDetector().toString() + "</i><br>Use it none-the-less?",
                           "Poor reference choice?", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                     if (res == JOptionPane.NO_OPTION)
                        return;
                  }
                  for (final Map.Entry<Integer, RegionOfInterestSet.RegionOfInterest> me : rois.entrySet()) {
                     final RegionOfInterestSet.RegionOfInterest roi = me.getValue();
                     final int sn = (int) Math.round(SpectrumUtils.computeSignalToNoise(roi, spec));
                     // Actual beam current or live time is not important for
                     // references but it must be defined for the filter
                     final SpectrumProperties sp = spec.getProperties();
                     sp.setNumericProperty(SpectrumProperties.ProbeCurrent, sp.getNumericWithDefault(SpectrumProperties.ProbeCurrent, 1.0));
                     sp.setNumericProperty(SpectrumProperties.LiveTime, sp.getNumericWithDefault(SpectrumProperties.LiveTime, 60.0));
                     final StringBuffer warning = new StringBuffer("<HTML>");
                     boolean ok = true;
                     if (sn < 100) {
                        warning.append("The signal-to-noise for <i>" + spec.toString() + "</i> around <i>" + roi.toString() + "</i> is poor. (S/N = "
                              + Integer.toString(sn) + "<br>");
                        ok = false;
                     }
                     assert roi.getElementSet().size() == 1;
                     Set<Element> elms = spec.getProperties().getElements();
                     if (elms == null) {
                        final SelectElements se = new SelectElements(QuantificationWizard.this, "Specify the elements in the reference");
                        se.setSelected(roi.getElementSet().first());
                        se.setLocationRelativeTo(QuantificationWizard.this);
                        se.setMultiSelect(true);
                        se.setVisible(true);
                        elms = se.getElements();
                        spec.getProperties().setElements(elms);
                     }
                     if (!mQuantUsingStandards.suitableAsReference(elms).contains(roi)) {
                        warning.append(spec + " is not suitable as a reference for " + roi + "<br/>");
                        ok = false;
                     }
                     if (!ok) {
                        warning.append("<b>Use it none-the-less?<b>");
                        final int res = JOptionPane.showConfirmDialog(QuantificationWizard.this, warning, "Poor reference choice?",
                              JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                        ok = (res == JOptionPane.YES_OPTION);
                     }
                     if (ok) {
                        final int row = me.getKey().intValue();
                        jTableModel_Reference.setValueAt(spec, row, SPECTRUM_COL);
                        updateSignalToNoise(row);
                     }
                  }
               }
            }
         }

         private void specifyReferenceFromDatabase() {
            assert mSession != null;
            final TreeMap<Integer, RegionOfInterestSet.RegionOfInterest> rois = new TreeMap<>();
            for (int r = jTable_Reference.getRowCount() - 1; r >= 0; --r)
               if (jTable_Reference.isRowSelected(r))
                  rois.put(Integer.valueOf(r), (RegionOfInterestSet.RegionOfInterest) jTable_Reference.getValueAt(r, ROI_COL));
            if (rois.size() > 0) {
               final TreeSet<Element> elms = new TreeSet<>();
               for (final RegionOfInterestSet.RegionOfInterest roi : rois.values())
                  elms.addAll(roi.getElementSet());
               try {
                  final StringBuffer msg = new StringBuffer("Select a reference for ");
                  msg.append(elms.toString());
                  final ResultDialog rd = new ResultDialog(QuantificationWizard.this, msg.toString(), true);
                  rd.setSingleSelect(true);
                  rd.setSpectra(mSession.findReferences(mQuantUsingStandards.getDetector().getDetectorProperties(),
                        FromSI.keV(mQuantUsingStandards.getBeamEnergy()), elms));
                  rd.setLocationRelativeTo(QuantificationWizard.this);
                  if (rd.showDialog()) {
                     final ISpectrumData spec = rd.getSpectra().get(0);
                     for (final Map.Entry<Integer, RegionOfInterestSet.RegionOfInterest> me : rois.entrySet()) {
                        final RegionOfInterestSet.RegionOfInterest roi = me.getValue();
                        final int sn = (int) Math.round(SpectrumUtils.computeSignalToNoise(roi, spec));
                        if (spec.getProperties().getElements() == null)
                           spec.getProperties().setElements(roi.getElementSet());
                        if (sn < 100) {
                           final int res = JOptionPane.showConfirmDialog(QuantificationWizard.this,
                                 "<html></i>" + spec.toString() + "</i> does not seem to be a good reference for </i>" + roi.toString()
                                       + "</i><br>Use it none-the-less?",
                                 "Poor reference choice?", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                           if (res == JOptionPane.NO_OPTION)
                              return;
                        }
                        final int row = me.getKey().intValue();
                        jTableModel_Reference.setValueAt(spec, row, SPECTRUM_COL);
                        updateSignalToNoise(row);
                     }
                  }
               } catch (final Exception e) {
                  ErrorDialog.createErrorMessage(QuantificationWizard.this, "Select a reference", e);
               }
            }
         }

         @Override
         public void onShow() {
            assert QuantificationWizard.this.mQuantMode == QuantMode.MLSQ;
            // Basic stuff
            jButton_SpecifyDB.setEnabled(mSession != null);
            // Fill in available and required references
            int r = 0;
            for (final RegionOfInterestSet.RegionOfInterest roi : mQuantUsingStandards.getUnsatisfiedReferences()) {
               jTableModel_Reference.setRowCount(r + 1);
               jTableModel_Reference.setValueAt(roi, r, ROI_COL);
               jTableModel_Reference.setValueAt(MISSING, r, SPECTRUM_COL);
               updateSignalToNoise(r);
               ++r;
            }
            for (final RegionOfInterest roi : mQuantUsingStandards.getSatisfiedReferences()) {
               jTableModel_Reference.setRowCount(r + 1);
               jTableModel_Reference.setValueAt(roi, r, ROI_COL);
               jTableModel_Reference.setValueAt(mQuantUsingStandards.getReferenceSpectrum(roi), r, SPECTRUM_COL);
               updateSignalToNoise(r);
               ++r;
            }
            setMessageText("Specify reference spectra (as necessary.)");
            setNextPanel(jWizardPanel_LLSQQuantLine.get());
            jButton_SpecifyDB.setEnabled(mSession != null);
            getWizard().enableFinish(false);
         }

         @Override
         public boolean permitNext() {
            for (int r = jTableModel_Reference.getRowCount() - 1; r >= 0; --r) {
               final RegionOfInterestSet.RegionOfInterest roi = (RegionOfInterestSet.RegionOfInterest) jTableModel_Reference.getValueAt(r, ROI_COL);
               if (jTableModel_Reference.getValueAt(r, SPECTRUM_COL) != MISSING) {
                  final ISpectrumData spec = (ISpectrumData) jTableModel_Reference.getValueAt(r, SPECTRUM_COL);
                  try {
                     final Composition comp = SpectrumUtils.getComposition(spec);
                     if (comp != null)
                        mQuantUsingStandards.addReference(roi, spec, comp);
                     else {
                        Set<Element> elms = spec.getProperties().getElements();
                        if (elms == null)
                           elms = roi.getElementSet();
                        mQuantUsingStandards.addReference(roi, spec, elms);
                     }
                  } catch (final EPQException e) {
                     ErrorDialog.createErrorMessage(QuantificationWizard.this, "Reference Error", e);
                  }
               } else
                  mQuantUsingStandards.clearReference(roi);
            }
            final Set<RegionOfInterest> unsatisfiedReferences = mQuantUsingStandards.getUnsatisfiedReferences();
            if (unsatisfiedReferences.size() > 0) {
               QuantificationWizard.this.setExtendedError("At least one required reference is missing.",
                     "Each element for which there is a standard must have at least one fully specified characteristic line set."
                           + "  A fully specified line set is one for the line is unobsructed on the standard or for which a reference is"
                           + "provided for each element that obstructs the line.");
               return false;
            }
            return true;
         }

         private void updateSignalToNoise(int row) {
            if ((jTableModel_Reference.getValueAt(row, ROI_COL) instanceof RegionOfInterestSet.RegionOfInterest)
                  && (jTableModel_Reference.getValueAt(row, SPECTRUM_COL) instanceof ISpectrumData)) {
               final RegionOfInterestSet.RegionOfInterest roi = (RegionOfInterestSet.RegionOfInterest) jTableModel_Reference.getValueAt(row, ROI_COL);
               final ISpectrumData spec = (ISpectrumData) jTableModel_Reference.getValueAt(row, SPECTRUM_COL);
               final int sn = (int) Math.round(SpectrumUtils.computeSignalToNoise(roi, spec));
               if (sn < 100)
                  jTableModel_Reference.setValueAt("<html><font color=red>Poor " + Integer.toString(sn) + "</font>", row, SN_COL);
               else if (sn < 250)
                  jTableModel_Reference.setValueAt("<html><font color=orange>Ok " + Integer.toString(sn) + "</font>", row, SN_COL);
               else
                  jTableModel_Reference.setValueAt("<html><font color=green>Good " + Integer.toString(sn) + "</font>", row, SN_COL);
            } else
               jTableModel_Reference.setValueAt("-", row, SN_COL);
         }
      }

      /**
       * Allows the user to specify "Other Element Rules" to permit the
       * calculation of quantities of unmeasured elements.
       */
      private class LLSQOtherElement extends JWizardPanel {

         private final class WatersOfCrystalizationAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
               jComboBox_ByDifference.setEnabled(false);
               jTable_Stoichiometry.setEnabled(true);
            }
         }

         private final class ElementByStoichiometryAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
               jComboBox_ByDifference.setEnabled(false);
               jTable_Stoichiometry.setEnabled(true);
            }
         }

         private final class ElementByDifferenceAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
               jComboBox_ByDifference.setEnabled(true);
               jTable_Stoichiometry.setEnabled(false);
            }
         }

         private final class NoExtraElementAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
               jComboBox_ByDifference.setEnabled(false);
               jTable_Stoichiometry.setEnabled(false);
            }
         }

         private static final long serialVersionUID = 6343402510332633956L;

         private final JComboBox<Element> jComboBox_ByDifference = new JComboBox<>();
         private JRadioButton jRadioButton_None;
         private JRadioButton jRadioButton_ByStoichiometry;
         private JRadioButton jRadioButton_WatersOfCrystallization;
         private JRadioButton jRadioButton_Difference;
         private final JStoichiometryTable jTable_Stoichiometry = new JStoichiometryTable();

         public LLSQOtherElement(JWizardDialog wiz) {
            super(wiz, "Specify other element rules", new FormLayout("pref, 5dlu, 150dlu", "pref, 5dlu, pref, 5dlu, pref, 5dlu, top:70dlu"));
            try {
               initialize();
            } catch (final Exception ex) {
               ex.printStackTrace();
            }
         }

         private void initialize() {
            final CellConstraints cc = new CellConstraints();
            jRadioButton_None = new JRadioButton("No extra element");

            jRadioButton_Difference = new JRadioButton("Element by difference");

            jRadioButton_ByStoichiometry = new JRadioButton("Oxygen by stoichiometry");

            jRadioButton_WatersOfCrystallization = new JRadioButton("Waters of Crystallization");

            add(jRadioButton_None, cc.xy(1, 1));
            jRadioButton_None.addActionListener(new NoExtraElementAction());
            add(jRadioButton_Difference, cc.xy(1, 3));
            jRadioButton_Difference.addActionListener(new ElementByDifferenceAction());
            add(jComboBox_ByDifference, cc.xy(3, 3));

            add(jRadioButton_ByStoichiometry, cc.xy(1, 5));
            jRadioButton_ByStoichiometry.addActionListener(new ElementByStoichiometryAction());
            add(new JScrollPane(jTable_Stoichiometry), cc.xywh(3, 5, 1, 3));

            add(jRadioButton_WatersOfCrystallization, cc.xy(1, 7));
            jRadioButton_WatersOfCrystallization.addActionListener(new WatersOfCrystalizationAction());

            final ButtonGroup rg = new ButtonGroup();
            rg.add(jRadioButton_None);
            rg.add(jRadioButton_Difference);
            rg.add(jRadioButton_ByStoichiometry);
            rg.add(jRadioButton_WatersOfCrystallization);
            rg.setSelected(jRadioButton_None.getModel(), true);

            jComboBox_ByDifference.setEnabled(false);
            jComboBox_ByDifference.setEditable(false);
            // jTable_Stoichiometry.setForeground(SystemColor.textText);
         }

         @Override
         public void onShow() {
            assert QuantificationWizard.this.mQuantMode == QuantMode.MLSQ;
            final Collection<Element> exclude = jWizardPanel_LLSQStandard.get().usedElements();
            setElements(exclude);
            if (!exclude.contains(Element.O))
               jComboBox_ByDifference.setSelectedItem(Element.O);
            setMessageText("Specify how to handle unmeasured elements");
            setNextPanel(jWizardPanel_LLSQReference.get());
            getWizard().enableFinish(false);
         }

         public void setElements(Collection<Element> elms) {
            final ElementComboBoxModel cbm = new ElementComboBoxModel();
            cbm.allBut(elms);
            jComboBox_ByDifference.setModel(cbm);
            final boolean stoic = !elms.contains(Element.O);
            jTable_Stoichiometry.setElements(elms);
            jRadioButton_ByStoichiometry.setEnabled(stoic);
            jRadioButton_WatersOfCrystallization.setEnabled(!stoic);
            if ((!stoic) && jRadioButton_ByStoichiometry.isSelected())
               jRadioButton_None.setSelected(true);
            jTable_Stoichiometry.setEnabled(jRadioButton_ByStoichiometry.isSelected() || jRadioButton_WatersOfCrystallization.isSelected());
         }

         @Override
         public boolean permitNext() {
            mQuantUsingStandards.clearUnmeasuredElementRules();
            {
               final Element elmByStoic = getElementByStoichiometry();
               if (!elmByStoic.equals(Element.None)) {
                  final CompositionFromKRatios.OxygenByStoichiometry ebs;
                  if (jRadioButton_WatersOfCrystallization.isSelected())
                     ebs = new CompositionFromKRatios.WatersOfCrystallization(mQuantUsingStandards.getMeasuredElements());
                  else
                     ebs = new CompositionFromKRatios.OxygenByStoichiometry(mQuantUsingStandards.getMeasuredElements());
                  ebs.setOxidizer(jTable_Stoichiometry.getOxidizer());
                  mQuantUsingStandards.addUnmeasuredElementRule(ebs);
               }
            }
            {
               final Element elm = getElementByDifference();
               if (!elm.equals(Element.None))
                  mQuantUsingStandards.addUnmeasuredElementRule(new CompositionFromKRatios.ElementByDifference(elm));
            }
            return true;
         }

         private Element getElementByDifference() {
            return jRadioButton_Difference.isSelected() ? (Element) jComboBox_ByDifference.getSelectedItem() : Element.None;
         }

         private Element getElementByStoichiometry() {
            return jRadioButton_ByStoichiometry.isSelected() || jRadioButton_WatersOfCrystallization.isSelected() ? Element.O : Element.None;
         }

      }

      /**
       * Allows the user to specify which line family to use to quantify a
       * spectrum.
       */
      private class LLSQQuantLine extends JWizardPanel {

         private static final long serialVersionUID = 2122704871890972576L;

         private final int ELEMENT_COL = 0;
         private final int LINE_COL = 1;

         private final String AUTO = "Auto";

         DefaultTableModel jTableModel_Lines = new DefaultTableModel(new Object[]{"Element", "Line Family",}, 0) {
            private static final long serialVersionUID = 5193881319552329153L;

            @Override
            public boolean isCellEditable(int row, int col) {
               final Element elm = (Element) getValueAt(row, ELEMENT_COL);
               return (col == LINE_COL) && (mQuantUsingStandards.getStandardROIS(elm).size() > 1);
            }
         };

         private final JTable jTable_Lines = new JTable(jTableModel_Lines);
         private final JButton jButton_Default = new JButton("Default");
         private final EachRowEditor jEachRowEditor_Lines = new EachRowEditor(jTable_Lines);

         public LLSQQuantLine(JWizardDialog wiz) {
            super(wiz, "Specify lines to quantify.", new FormLayout("280dlu, pref", "140dlu, 5dlu, pref"));
            initialize();
         }

         private int findRow(Element elm) {
            for (int i = 0; i < jTable_Lines.getRowCount(); ++i)
               if (elm.equals(jTableModel_Lines.getValueAt(i, 0)))
                  return i;
            return -1;
         }

         private void setDefaultLines() {
            for (final Element elm : mQuantUsingStandards.getStandards().keySet()) {
               final RegionOfInterest prefRoi = mQuantUsingStandards.getDefaultROI(elm);
               mQuantUsingStandards.setPreferredROI(prefRoi);
               final int row = findRow(elm);
               if (row >= 0)
                  jTableModel_Lines.setValueAt(prefRoi, row, LINE_COL);
            }
         }

         private void initialize() {
            // jTable_Lines.setForeground(SystemColor.textText);
            add(new JScrollPane(jTable_Lines), CC.xyw(1, 1, 2));
            add(jButton_Default, CC.xy(2, 3));
            jTable_Lines.getColumnModel().getColumn(LINE_COL).setCellEditor(jEachRowEditor_Lines);
            jButton_Default.setToolTipText("Selects a line-family for each element based on the \"U>2 rule-of-thumb\".");
            jButton_Default.addActionListener(new ActionListener() {

               @Override
               public void actionPerformed(ActionEvent arg0) {
                  setDefaultLines();
               }

            });
         }

         @Override
         public void onShow() {
            assert QuantificationWizard.this.mQuantMode == QuantMode.MLSQ;
            jTableModel_Lines.setRowCount(0);
            for (final Element elm : mQuantUsingStandards.getStandards().keySet()) {
               final RegionOfInterestSet rois = mQuantUsingStandards.getStandardROIS(elm);
               if (rois.size() == 1) {
                  final RegionOfInterest roi = rois.iterator().next();
                  jTableModel_Lines.addRow(new Object[]{elm, roi});
               } else {
                  jTableModel_Lines.addRow(new Object[]{elm, AUTO});
                  final JComboBox<Object> cb = new JComboBox<>();
                  cb.addItem(AUTO);
                  for (final RegionOfInterest roi : rois)
                     cb.addItem(roi);
                  final int row = jTableModel_Lines.getRowCount() - 1;
                  jEachRowEditor_Lines.setEditorAt(row, new DefaultCellEditor(cb));
                  final RegionOfInterest prefRoi = mQuantUsingStandards.getPreferredROI(elm);
                  if (prefRoi == null)
                     jTableModel_Lines.setValueAt(AUTO, row, LINE_COL);
                  else
                     jTableModel_Lines.setValueAt(prefRoi, row, LINE_COL);
               }
            }
            getWizard().setMessageText("Specify the line family used to quantify each element.");
            setNextPanel(jWizardPanel_LLSQUnknown.get());
            getWizard().enableFinish(false);
         }

         @Override
         public boolean permitNext() {
            mQuantUsingStandards.clearPreferredROIs();
            for (int row = 0; row < jTableModel_Lines.getRowCount(); ++row) {
               final Object roi = jTableModel_Lines.getValueAt(row, LINE_COL);
               if (jTableModel_Lines.isCellEditable(row, LINE_COL) && (roi != AUTO))
                  mQuantUsingStandards.setPreferredROI((RegionOfInterest) roi);
            }
            return true;
         }
      }

      private class LLSQUnknownPanel extends JWizardPanel {

         /**
          * <p>
          * Implements the sample shape button.
          * </p>
          *
          * @author Nicholas
          * @version 1.0
          */
         private final class SampleShapeAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent arg0) {
               final int[] rows = jTable_Unknown.getSelectedRows();
               if (rows.length == 0)
                  QuantificationWizard.this.setMessageText("Select a spectrum first...");
               else {
                  final ISpectrumData firstSpec = (ISpectrumData) jTable_Unknown.getValueAt(rows[0], 0);
                  final SampleShapeDialog ssd = new SampleShapeDialog(QuantificationWizard.this);
                  {
                     final SpectrumProperties sp = firstSpec.getProperties();
                     ssd.setSampleShape(sp.getSampleShapeWithDefault(SpectrumProperties.SampleShape, null));
                     ssd.setDensity(ToSI.gPerCC(sp.getNumericWithDefault(SpectrumProperties.SpecimenDensity, 3.0)));
                     ssd.enableShapes(AlgorithmUser.getDefaultCorrectionAlgorithm());
                  }
                  QuantificationWizard.this.centerDialog(ssd);
                  ssd.setTitle("Select a Sample Shape");
                  ssd.setVisible(true);
                  if (ssd.isOk()) {
                     final SampleShape newShape = ssd.getSampleShape();
                     for (final int row : rows) {
                        final ISpectrumData spec = (ISpectrumData) jTable_Unknown.getValueAt(row, 0);
                        final SpectrumProperties sp = spec.getProperties();
                        sp.setSampleShape(SpectrumProperties.SampleShape, newShape);
                        sp.setNumericProperty(SpectrumProperties.SpecimenDensity, FromSI.gPerCC(ssd.getDensity()));
                        jTable_Unknown.setValueAt(newShape, row, 3);
                     }
                  }
               }
            }
         }

         /**
          * <p>
          * Implements the edit button.
          * </p>
          *
          * @author Nicholas
          * @version 1.0
          */
         private final class EditAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent arg0) {
               final int[] rows = jTable_Unknown.getSelectedRows();
               final ArrayList<ISpectrumData> specs = new ArrayList<>();
               final SpectrumPropertyPanel.PropertyDialog pd = new SpectrumPropertyPanel.PropertyDialog(QuantificationWizard.this, mSession);
               pd.setLocationRelativeTo(QuantificationWizard.this);
               for (final int row : rows) {
                  final Object spec = jTable_Unknown.getValueAt(row, 0);
                  assert spec instanceof ISpectrumData;
                  specs.add((ISpectrumData) spec);
                  pd.addSpectrumProperties(((ISpectrumData) spec).getProperties());
               }
               pd.setVisible(true);
               if (pd.isOk()) {
                  for (final ISpectrumData spec : specs)
                     spec.getProperties().addAll(pd.getSpectrumProperties());
                  jTable_Unknown.setModel(new SpectrumTable(mSpectra));
               }
            }
         }

         /**
          * <p>
          * Implements the remove button.
          * </p>
          *
          * @author Nicholas
          * @version 1.0
          */
         private final class RemoveAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
               final int[] rows = jTable_Unknown.getSelectedRows();
               for (final int row : rows) {
                  final Object spec = jTable_Unknown.getValueAt(row, 0);
                  assert spec instanceof ISpectrumData;
                  mSpectra.remove(spec);
               }
               jTable_Unknown.setModel(new SpectrumTable(mSpectra));
            }
         }

         /**
          * <p>
          * Implements the add button.
          * </p>
          *
          * @author Nicholas
          * @version 1.0
          */
         private final class AddAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
               final SpectrumFileChooser sfc = new SpectrumFileChooser(QuantificationWizard.this, "Select spectra files");
               sfc.setMultiSelectionEnabled(true);
               sfc.setLocationRelativeTo(QuantificationWizard.this);
               final String def = mUserPref.get(REFERENCE_DIR, System.getProperty("user.home"));
               final File dir = new File(mUserPref.get(UNKNOWN_DIR, def));
               sfc.getFileChooser().setCurrentDirectory(dir);
               if (sfc.showOpenDialog() == JFileChooser.APPROVE_OPTION) {
                  final File[] files = sfc.getFileChooser().getSelectedFiles();
                  assert files.length > 0;
                  mUserPref.put(UNKNOWN_DIR, files[0].getParent());
                  final NumberFormat nf = new HalfUpFormat("##.0");
                  final ArrayList<String> errs = new ArrayList<>();
                  for (final File file : files)
                     if (file.canRead())
                        try {
                           final ISpectrumData[] spex = SpectrumFile.open(file);
                           for (ISpectrumData spec : spex) {
                              final SpectrumProperties sp = spec.getProperties();
                              if (!sp.isDefined(SpectrumProperties.BeamEnergy)) {
                                 sp.setNumericProperty(SpectrumProperties.BeamEnergy, FromSI.keV(getBeamEnergy()));
                                 getWizard().setMessageText("Setting the beam energy to " + nf.format(FromSI.keV(getBeamEnergy())) + " keV.");
                              }
                              if (!validateRequiredProperties(spec)) {
                                 errs.add(spec.toString() + " is missing the probe current, live time or beam energy.");
                                 continue;
                              }
                              if (Math.abs(FromSI.keV(getBeamEnergy()) - sp.getNumericWithDefault(SpectrumProperties.BeamEnergy, 0.0)) > 0.1) {
                                 errs.add(spec.toString() + " was not acquired at the specified beam energy.");
                                 continue;
                              }
                              mSpectra.add(spec);
                           }
                        } catch (final EPQException e1) {
                           errs.add("Unable to open " + file.getName());
                        }
                  if (errs.size() > 0)
                     if (errs.size() == 1)
                        getWizard().setMessageText(errs.get(0));
                     else {
                        final StringBuffer sb = new StringBuffer();
                        sb.append(errs.get(0));
                        for (int i = 1; i < errs.size(); ++i)
                           sb.append("\n" + errs.get(i));
                        getWizard().setExtendedError("There were " + Integer.toString(errs.size()) + " configuration problems.", sb.toString());
                     }
                  jTable_Unknown.setModel(new SpectrumTable(mSpectra));
               }
            }
         }

         private class SpectrumTable extends DefaultTableModel {

            private static final long serialVersionUID = 3954215966258640172L;

            SpectrumTable(Collection<ISpectrumData> specs) {
               super(new String[]{"Name", "Live Time", "Probe (nA)", "Duane-Hunt", "Shape"}, 0);
               for (final ISpectrumData spec : specs)
                  addRow(spec);
            }

            public void addRow(ISpectrumData spec) {
               final SpectrumProperties sp = spec.getProperties();
               final double probe = SpectrumUtils.getAverageFaradayCurrent(sp, Double.NaN);
               final double liveTime = sp.getNumericWithDefault(SpectrumProperties.LiveTime, 60.0);
               double dh = sp.getNumericWithDefault(SpectrumProperties.DuaneHunt, Double.NaN);
               if (!sp.isDefined(SpectrumProperties.SampleShape))
                  sp.setSampleShape(SpectrumProperties.SampleShape, new SampleShape.Bulk());
               if (Double.isNaN(dh)) {
                  dh = FromSI.keV(DuaneHuntLimit.DefaultDuaneHunt.compute(spec));
                  if (!Double.isNaN(dh))
                     sp.setNumericProperty(SpectrumProperties.DuaneHunt, dh);
               }
               final double e0 = sp.getNumericWithDefault(SpectrumProperties.BeamEnergy, Double.NaN);
               final NumberFormat nf1 = new HalfUpFormat("0.0");
               final NumberFormat nf3 = new HalfUpFormat("0.000");
               addRow(new Object[]{spec, Double.isNaN(liveTime) ? "Missing" : nf1.format(liveTime),
                     Double.isNaN(probe) ? "Missing" : nf3.format(probe),
                     Double.isNaN(dh) ? "?" : (duaneHuntThreshold(dh, e0) ? nf3.format(dh) : "<html><font color=red>" + nf3.format(dh) + "</font>"),
                     sp.getSampleShapeWithDefault(SpectrumProperties.SampleShape, null)});
            }
         }

         private JTable jTable_Unknown;
         private final Set<ISpectrumData> mSpectra = new TreeSet<>();
         private boolean mFirstShow = true;
         private final double[] COL_WIDTHS = new double[]{0.3, 0.15, 0.15, 0.15, 0.15};

         private void initialize() {
            setLayout(new FormLayout("240dlu, 10dlu, pref", "top:120dlu"));
            final CellConstraints cc = new CellConstraints();
            jTable_Unknown = new JTable();
            // jTable_Unknown.setForeground(SystemColor.textText);
            jTable_Unknown.setModel(new SpectrumTable(mSpectra));
            add(new JScrollPane(jTable_Unknown), cc.xy(1, 1));
            {
               final TableColumnModel cm = jTable_Unknown.getColumnModel();
               final int total = cm.getTotalColumnWidth();
               assert COL_WIDTHS.length == cm.getColumnCount();
               for (int i = 0; i < COL_WIDTHS.length; ++i)
                  cm.getColumn(i).setPreferredWidth((int) Math.round(COL_WIDTHS[i] * total));
            }
            {
               final JPanel btnPanel = new JPanel(new FormLayout("pref", "pref, 3dlu, pref, 20dlu, pref, 3dlu, pref"));
               final JButton addBtn = new JButton("Add file");
               addBtn.addActionListener(new AddAction());
               btnPanel.add(addBtn, cc.xy(1, 1));

               final JButton removeBtn = new JButton("Remove");
               removeBtn.addActionListener(new RemoveAction());
               btnPanel.add(removeBtn, cc.xy(1, 3));

               final JButton editBtn = new JButton("Properties");
               editBtn.addActionListener(new EditAction());

               btnPanel.add(editBtn, cc.xy(1, 5));

               final JButton shapeBtn = new JButton("Sample Shape");
               shapeBtn.addActionListener(new SampleShapeAction());

               btnPanel.add(shapeBtn, cc.xy(1, 7));

               add(btnPanel, cc.xy(3, 1));
            }
         }

         /**
          * Constructs a UnknownSpectrumPanel
          *
          * @param wiz
          */
         public LLSQUnknownPanel(JWizardDialog wiz) {
            super(wiz, "Specify unknowns and properties");
            try {
               initialize();
            } catch (final RuntimeException ex) {
            }
         }

         @Override
         public void onShow() {
            assert QuantificationWizard.this.mQuantMode == QuantMode.MLSQ;
            if (mFirstShow) {
               // First time around add the spectra selected in the DataManager
               // (main screen)
               for (final ISpectrumData spec : mInputSpectra)
                  if (Math.abs(ToSI.eV(SpectrumUtils.getBeamEnergy(spec)) - getBeamEnergy()) < (getBeamEnergy() / 100.0))
                     mSpectra.add(spec);
               jTable_Unknown.setModel(new SpectrumTable(mSpectra));
               mFirstShow = false;
            }
            getWizard().setMessageText("Specify the unknown spectra");
            getWizard().setNextPanel(jWizardPanel_LLSQResults.get());
         }

         @Override
         public boolean permitNext() {
            boolean res = mSpectra.size() > 0;
            if (!res)
               getWizard().setErrorText("Specify at least one spectrum to analyze");
            else {
               final ArrayList<String> errs = new ArrayList<>();
               final EDSDetector det = jWizardPanel_LLSQInstrument.get().getDetector();
               assert det != null : "The detector can not be null.";
               for (final ISpectrumData spec : mSpectra)
                  if (spec.getProperties().getDetector() != det)
                     errs.add("The detector is nor correct for spectrum " + spec.toString() + ".");
               for (final ISpectrumData spec : mSpectra) {
                  res &= validateRequiredProperties(spec);
                  errs.add("Spectrum " + spec.toString() + " is missing one or more of the required properties.");
               }
               if (!res)
                  errs.add("You must specify the probe current, beam energy & live time for each spectrum.");
               final CorrectionAlgorithm ca = AlgorithmUser.getDefaultCorrectionAlgorithm();
               for (final ISpectrumData spec : mSpectra) {
                  final SpectrumProperties sp = spec.getProperties();
                  final SampleShape ss = sp.getSampleShapeWithDefault(SpectrumProperties.SampleShape, null);
                  final Class<? extends SampleShape> css = (ss != null ? ss.getClass() : SampleShape.Bulk.class);
                  if (!ca.supports(css)) {
                     res = false;
                     errs.add(spec.toString() + " - " + ca.toString() + " does not support the shape " + ss);
                  }
               }
               {
                  final StringBuffer warn = new StringBuffer();
                  final NumberFormat nf = new HalfUpFormat("0.000");
                  double minDh = Double.MAX_VALUE, maxDh = -Double.MAX_VALUE;
                  for (final ISpectrumData spec : mSpectra) {
                     final double dh = spec.getProperties().getNumericWithDefault(SpectrumProperties.DuaneHunt, Double.NaN);
                     final double e0 = spec.getProperties().getNumericWithDefault(SpectrumProperties.BeamEnergy, Double.NaN);
                     minDh = Math.min(dh, minDh);
                     maxDh = Math.max(dh, maxDh);
                     // assert !Double.isNaN(dh);
                     assert !Double.isNaN(e0);
                     final double delta = e0 - dh;
                     if (!duaneHuntThreshold(dh, e0))
                        warn.append("The Duane-Hunt limit for the unknown \"" + spec + "\" is " + nf.format(delta) + " keV less than beam energy.\n");
                  }
                  boolean first = true;
                  for (final ISpectrumData spec : mQuantUsingStandards.getStandardSpectra().values()) {
                     final double dh = spec.getProperties().getNumericWithDefault(SpectrumProperties.DuaneHunt, Double.NaN);
                     final double e0 = spec.getProperties().getNumericWithDefault(SpectrumProperties.BeamEnergy, Double.NaN);
                     // assert !Double.isNaN(dh);
                     assert !Double.isNaN(e0);
                     final double delta = e0 - dh;
                     if (!duaneHuntThreshold(dh, e0)) {
                        if (first) {
                           if (minDh == maxDh)
                              warn.append("The Duane-Hunt limit for the unknown spectrum is " + nf.format(minDh) + " keV.\n");
                           else
                              warn.append("The Duane-Hunt limits for the unknown spectra range from " + nf.format(minDh) + " to " + nf.format(maxDh)
                                    + " keV.\n");
                           warn.append("The nominal beam energy is " + nf.format(e0) + " keV.\n");
                           first = false;
                        }
                        warn.append("The Duane-Hunt limit for the standard \"" + spec + "\" differs by " + nf.format(delta)
                              + " keV from the beam energy.\n");
                     }
                  }
                  if (warn.length() > 0) {
                     warn.append(
                           "  A low Duane-Hunt limit suggests sample charging and can degrade the accuracy of the quantitative results.\n    Should we proceed?");
                     if (JOptionPane.showConfirmDialog(QuantificationWizard.this, warn.toString(), "Duane-Hunt limit warning",
                           JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                        res = false;
                        errs.add("Evidence of sample charging on one or more spectra.");
                     }
                  }
               }
               if (errs.size() > 0)
                  if (errs.size() == 1)
                     getWizard().setErrorText(errs.get(0));
                  else {
                     final StringBuffer sb = new StringBuffer();
                     sb.append(errs.get(0));
                     for (int i = 1; i < errs.size(); ++i) {
                        sb.append("\n");
                        sb.append(errs.get(i));
                     }
                     getWizard().setExtendedError(Integer.toString(errs.size()) + " configuration problems.",
                           "Please correct these configuration problems", sb.toString());
                  }
            }
            return res;
         }

         static private final long serialVersionUID = 0x1286dea33456L;
      }

      private class LLSQResults extends JWizardPanel {

         private final class ShowMenuMouseAction extends MouseAdapter {
            @Override
            public void mousePressed(MouseEvent e) {
               if (e.isPopupTrigger())
                  jPopupMenu_Output.show(QuantificationWizard.this, e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
               if (e.isPopupTrigger())
                  jPopupMenu_Output.show(QuantificationWizard.this, e.getX(), e.getY());
            }
         }

         private final class CopyCompositionToClipboardAction implements ActionListener {

            private final ResultMode mMode;

            CopyCompositionToClipboardAction(ResultMode rm) {
               mMode = rm;
            }

            @Override
            public void actionPerformed(ActionEvent arg0) {
               final Clipboard clp = Toolkit.getDefaultToolkit().getSystemClipboard();
               final StringSelection ss = new StringSelection(mModel.asTable(mMode));
               clp.setContents(ss, ss);
            }
         }

         private final class SetResultModeAction implements ActionListener {

            private final ResultMode mMode;

            SetResultModeAction(ResultMode rm) {
               mMode = rm;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
               mModel.setNormalized(mMode);
            }
         }

         private class CompositionTable extends AbstractTableModel {
            private static final long serialVersionUID = 0L;
            private final ArrayList<ISpectrumData> mResults = new ArrayList<>();
            private final ArrayList<Composition> mCompositions = new ArrayList<>();
            private final ArrayList<Element> mElements = new ArrayList<>();
            private ResultMode mMode = ResultMode.WEIGHT_PERCENT;
            private final NumberFormat mFormat = new HalfUpFormat("0.0000");

            void add(ISpectrumData spec, Composition comp) {
               mResults.add(spec);
               mCompositions.add(comp);
               final int s = mElements.size();
               for (final Element el : comp.getElementSet())
                  if (!mElements.contains(el))
                     mElements.add(el);
               if (mElements.size() != s) {
                  Collections.sort(mElements);
                  fireTableStructureChanged();
               } else
                  fireTableRowsInserted(mResults.size() - 1, mResults.size() - 1);
            }

            void clear() {
               mResults.clear();
               mCompositions.clear();
               mElements.clear();
               fireTableStructureChanged();
            }

            @Override
            public int getColumnCount() {
               return mElements.size() + 2;
            }

            @Override
            public int getRowCount() {
               return mResults.size();
            }

            @Override
            public Object getValueAt(int row, int col) {
               switch (col) {
                  case 0 :
                     return mResults.get(row).toString();
                  case 1 :
                     return mCompositions.get(row).sumWeightFractionU().format(mFormat);
                  default :
                     final Element elm = mElements.get(col - 2);
                     final Composition comp = mCompositions.get(row);
                     switch (mMode) {
                        case ATOMIC_PERCENT :
                           return comp.atomicPercentU(elm).format(mFormat);
                        case NORM_WEIGHT_PERCENT :
                           return comp.weightFractionU(elm, true).format(mFormat);
                        default :
                           return comp.weightFractionU(elm, false).format(mFormat);
                     }
               }
            }

            @Override
            public String getColumnName(int col) {
               switch (col) {
                  case 0 :
                     return "Spectrum";
                  case 1 :
                     return "Sum";
                  default :
                     return mElements.get(col - 2).toAbbrev();
               }
            }

            public void setNormalized(ResultMode mode) {
               if (mode != mMode) {
                  mMode = mode;
                  fireTableDataChanged();
               }
            }

            public String toHTML() {
               final StringWriter sw = new StringWriter(4096);
               if (mResults.size() > 0) {
                  if (mQuantUsingStandards != null) {
                     final PrintWriter pw = new PrintWriter(sw);
                     final File parentFile = DTSA2.getReport().getFile().getParentFile();
                     pw.println("<DIV></DIV><H2>Composition from Standards Spectra Fit to Unknown</H2>");
                     pw.print(mQuantUsingStandards.toHTML(parentFile));
                     // Top row
                     pw.print("<H3>Results</H3>");
                     // Result rows
                     pw.print(mQuantUsingStandards.tabulateResults(mResults, parentFile, mResultSpectra));
                  }
               }
               return sw.toString();
            }

            public String asTable(ResultMode mode) {
               final StringWriter sw = new StringWriter(4096);
               if (mResults.size() > 0) {
                  final PrintWriter pw = new PrintWriter(sw);
                  final NumberFormat nf2 = new HalfUpFormat("0.0000");
                  // Header row
                  pw.print("Spectrum\t");
                  for (final Element el : mElements) {
                     pw.print("\t");
                     pw.print(el.toAbbrev());
                     pw.print("\t");
                     pw.print("d(" + el.toAbbrev() + ")");
                  }
                  if (mode != ResultMode.ATOMIC_PERCENT)
                     pw.print("\tSum");
                  pw.print("\n");
                  // Result rows
                  final Map<Element, DescriptiveStatistics> stats = new TreeMap<>();
                  for (final Element elm : mElements)
                     stats.put(elm, new DescriptiveStatistics());
                  for (int row = 0; row < mResults.size(); ++row) {
                     // Separator line between spectra
                     final ISpectrumData spec = mResults.get(row);
                     final Composition comp = mCompositions.get(row);
                     pw.print(spec);
                     switch (mode) {
                        case WEIGHT_PERCENT : {
                           for (final Element elm : mElements) {
                              pw.print("\t");
                              final UncertainValue2 res = comp.weightFractionU(elm, false);
                              stats.get(elm).add(res.doubleValue());
                              pw.print(nf2.format(res.doubleValue()));
                              pw.print("\t");
                              pw.print(nf2.format(res.uncertainty()));
                           }
                           pw.print("\t");
                           pw.print(nf2.format(comp.sumWeightFraction()));
                           pw.print("\n");
                           break;
                        }
                        case NORM_WEIGHT_PERCENT : {
                           for (final Element elm : mElements) {
                              pw.print("\t");
                              final UncertainValue2 res = comp.weightFractionU(elm, true);
                              stats.get(elm).add(res.doubleValue());
                              pw.print(nf2.format(res.doubleValue()));
                              pw.print("\t");
                              pw.print(nf2.format(res.uncertainty()));
                           }
                           pw.print("\t");
                           pw.print(nf2.format(comp.sumWeightFraction()));
                           pw.print("\n");
                           break;
                        }
                        case ATOMIC_PERCENT : {
                           for (final Element elm : mElements) {
                              final UncertainValue2 res = comp.atomicPercentU(elm);
                              stats.get(elm).add(res.doubleValue());
                              pw.print("\t");
                              pw.print(nf2.format(res.doubleValue()));
                              pw.print("\t");
                              pw.print(nf2.format(res.uncertainty()));
                           }
                           pw.print("\n");
                           break;
                        }
                     }
                  }
                  if (mResults.size() > 1) {
                     pw.print("Average");
                     for (final Element elm : mElements) {
                        final DescriptiveStatistics ds = stats.get(elm);
                        pw.print("\t");
                        pw.print(nf2.format(ds.average()));
                        pw.print("\t");
                     }
                     pw.print("\n");
                  }
                  if (mResults.size() > 2) {
                     pw.print("Standard Deviation");
                     for (final Element elm : mElements) {
                        final DescriptiveStatistics ds = stats.get(elm);
                        pw.print("\t");
                        pw.print(nf2.format(ds.standardDeviation()));
                        pw.print("\t");
                     }
                     pw.print("\n");
                  }
               }
               return sw.toString();
            }
         }

         private static final long serialVersionUID = -7744221929956249604L;

         private final JTable jTable_Results = new JTable();
         private CompositionTable mModel;
         private final JPopupMenu jPopupMenu_Output = new JPopupMenu();

         /**
          * Constructs a LLSQResults
          *
          * @param wiz
          */
         public LLSQResults(JWizardDialog wiz) {
            super(wiz, "Results");
            try {
               initialize();
            } catch (final Exception ex) {
               ex.printStackTrace();
            }
         }

         private void initialize() {
            final FormLayout fl = new FormLayout("pref", "pref, 3dlu, 80dlu");
            final CellConstraints cc = new CellConstraints();
            final PanelBuilder pb = new PanelBuilder(fl, this);
            {
               final PanelBuilder pb1 = new PanelBuilder(
                     new FormLayout("default, max(pref;80dlu), 3dlu, max(pref;80dlu), 3dlu, max(pref;80dlu), default, 50dlu", "pref"));
               pb1.getPanel().setBorder(DTSA2.createTitledBorder("Normalization"));
               final JRadioButton wgtPct = new JRadioButton("Mass fraction");
               wgtPct.addActionListener(new SetResultModeAction(ResultMode.WEIGHT_PERCENT));
               pb1.add(wgtPct, cc.xy(2, 1));
               final JRadioButton normWgtPct = new JRadioButton("Normalized mass fraction");
               normWgtPct.addActionListener(new SetResultModeAction(ResultMode.NORM_WEIGHT_PERCENT));
               pb1.add(normWgtPct, cc.xy(4, 1));
               final JRadioButton atomicPct = new JRadioButton("Atomic percent");
               atomicPct.addActionListener(new SetResultModeAction(ResultMode.ATOMIC_PERCENT));
               pb1.add(atomicPct, cc.xy(6, 1));
               final ButtonGroup group = new ButtonGroup();
               group.add(wgtPct);
               group.add(normWgtPct);
               group.add(atomicPct);
               wgtPct.setSelected(true);
               pb.add(pb1.getPanel(), cc.xy(1, 1));
            }
            mModel = new CompositionTable();
            // jTable_Results.setForeground(SystemColor.textText);
            jTable_Results.setModel(mModel);
            final JScrollPane sp = new JScrollPane(jTable_Results);
            sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            pb.add(new JScrollPane(jTable_Results), cc.xy(1, 3));

            final JMenuItem massFrac = new JMenuItem("Copy (Mass fraction)");
            final JMenuItem normMassFrac = new JMenuItem("Copy (Normalized mass fraction)");
            final JMenuItem atomFrac = new JMenuItem("Copy (atom fraction)");

            massFrac.addActionListener(new CopyCompositionToClipboardAction(ResultMode.WEIGHT_PERCENT));

            normMassFrac.addActionListener(new CopyCompositionToClipboardAction(ResultMode.NORM_WEIGHT_PERCENT));

            atomFrac.addActionListener(new CopyCompositionToClipboardAction(ResultMode.ATOMIC_PERCENT));

            jPopupMenu_Output.add(massFrac);
            jPopupMenu_Output.add(normMassFrac);
            jPopupMenu_Output.add(atomFrac);
            jTable_Results.add(jPopupMenu_Output);
            jTable_Results.addMouseListener(new ShowMenuMouseAction());
         }

         @Override
         public void onShow() {
            assert QuantificationWizard.this.mQuantMode == QuantMode.MLSQ;
            getWizard().setNextPanel(null);
            enableInputMethods(false);
            enableFinish(false);
            setBackEnabled(false);
            mModel.clear();
            mResultSpectra.clear();
            clearMessageText();
            final PerformLLSQQuant pq = new PerformLLSQQuant(mQuantUsingStandards, jWizardPanel_LLSQUnknown.get().mSpectra);
            pq.execute();
         }

         @Override
         public boolean permitNext() {
            return true;
         }

         public void addResult(final ISpectrumData spec) {
            final Composition comp = spec.getProperties().getCompositionWithDefault(SpectrumProperties.MicroanalyticalComposition, Material.Null);
            mModel.add(spec, comp);
         }
      }

      /**
       * Initializes a QuantifySpectra object and collects the unknowns in
       * preparation for spawning a thread to compute the quantifications.
       */
      private class PerformLLSQQuant extends SwingWorker<ArrayList<String>, ISpectrumData[]> {

         final private ArrayList<ISpectrumData> mUnknowns;
         final private QuantifyUsingStandards mQuant;
         final private SpectrumSimulator mSimulator = SpectrumSimulator.Basic;

         PerformLLSQQuant(QuantifyUsingStandards quant, Collection<ISpectrumData> unknowns) {
            super();
            mQuant = quant;
            mUnknowns = new ArrayList<>(unknowns);
         }

         @Override
         public ArrayList<String> doInBackground() {
            final ArrayList<String> errors = new ArrayList<>();
            for (final ISpectrumData unk : mUnknowns)
               try {
                  final QuantifyUsingStandards.Result results = mQuant.compute(unk);
                  final ISpectrumData[] res = new ISpectrumData[3];
                  res[0] = unk;
                  res[1] = results.getResidual();
                  try {
                     res[2] = mSimulator.generateSpectrum(results.getComposition(), results.getUnknown().getProperties(), true);
                  } catch (final Throwable ex) {
                     errors.add("Error computing the simulated spectrum: " + ex.toString());
                  }
                  if (results.getWarningMessage() != null) {
                     errors.add(unk.toString() + "\n" + results.getWarningMessage());
                  }
                  publish(res);
               } catch (final Throwable e) {
                  final StringWriter sw = new StringWriter();
                  final PrintWriter pw = new PrintWriter(sw);
                  pw.append("Error fitting " + unk.toString() + ": " + e.getMessage());
                  e.printStackTrace(pw);
                  errors.add(sw.toString());
               }
            return errors;
         }

         @Override
         protected void process(List<ISpectrumData[]> results) {
            for (final ISpectrumData[] unk : results) {
               jWizardPanel_LLSQResults.get().addResult(unk[0]);
               mResultSpectra.add(unk[0]);
               final ISpectrumData residual = unk[1];
               if (residual != null)
                  mResultSpectra.add(residual);
               final ISpectrumData simulation = unk[2];
               if (simulation != null)
                  mResultSpectra.add(simulation);
            }
         }

         @Override
         protected void done() {
            enableInputMethods(true);
            enableFinish(true);
            setBackEnabled(true);
            try {
               final ArrayList<String> errs = get();
               if (errs.size() > 0) {
                  final StringBuffer sb = new StringBuffer();
                  for (final String err : errs) {
                     sb.append(err);
                     sb.append("\n");
                  }
                  QuantificationWizard.this.setExtendedErrors("Errors occured while quantifying the spectra.",
                        errs.size() + " errors during quantification.", errs);
               }
            } catch (final Exception e) {
               e.printStackTrace();
            }
         }

      }

      private QuantifyUsingStandards mQuantUsingStandards;

      private final LazyEvaluate<LLSQStandardPanel> jWizardPanel_LLSQStandard = new LazyEvaluate<>() {

         @Override
         protected LLSQStandardPanel compute() {
            return new LLSQStandardPanel(QuantificationWizard.this);
         }
      };
      private final LazyEvaluate<LLSQInstrumentPanel> jWizardPanel_LLSQInstrument = new LazyEvaluate<>() {

         @Override
         protected LLSQInstrumentPanel compute() {
            return new LLSQInstrumentPanel(QuantificationWizard.this);
         }
      };
      private final LazyEvaluate<LLSQReferencePanel> jWizardPanel_LLSQReference = new LazyEvaluate<>() {

         @Override
         protected LLSQReferencePanel compute() {
            return new LLSQReferencePanel(QuantificationWizard.this);
         }
      };
      private final LazyEvaluate<LLSQQuantLine> jWizardPanel_LLSQQuantLine = new LazyEvaluate<>() {

         @Override
         protected LLSQQuantLine compute() {
            return new LLSQQuantLine(QuantificationWizard.this);
         }
      };
      private final LazyEvaluate<LLSQOtherElement> jWizardPanel_LLSQOther = new LazyEvaluate<>() {

         @Override
         protected LLSQOtherElement compute() {
            return new LLSQOtherElement(QuantificationWizard.this);
         }
      };

      private final LazyEvaluate<LLSQUnknownPanel> jWizardPanel_LLSQUnknown = new LazyEvaluate<>() {

         @Override
         protected LLSQUnknownPanel compute() {
            return new LLSQUnknownPanel(QuantificationWizard.this);
         }
      };
      private final LazyEvaluate<LLSQResults> jWizardPanel_LLSQResults = new LazyEvaluate<>() {

         @Override
         protected LLSQResults compute() {
            return new LLSQResults(QuantificationWizard.this);
         }
      };

      @Override
      public String toHTML() {
         return jWizardPanel_LLSQResults.get().mModel.toHTML();
      }

      @Override
      public JWizardPanel firstPanel() {
         return this.jWizardPanel_LLSQInstrument.get();
      }
   }

   private class STEMPath extends JQuantPath {

      private QuantifyUsingZetaFactors mQuantUsingZetaFactors;

      private class STEMInstrumentPanel extends GenericInstrumentPanel {

         private static final long serialVersionUID = -2859842081135901006L;

         /**
          * Constructs a STEMInstrumentPanel
          *
          * @param wiz
          * @param nextPanel
          * @param next
          */
         public STEMInstrumentPanel(JWizardDialog wiz) {
            super(wiz, jWizardPanel_STEMStandards.get());
         }

         @Override
         public boolean permitNext() {
            final boolean res = super.permitNext();
            if (res) {
               final EDSDetector det = buildDetector();
               final double beamEnergy = ToSI.keV(getBeamEnergy_keV());
               if ((mQuantUsingZetaFactors == null) || (!Math2.approxEquals(mQuantUsingZetaFactors.getBeamEnergy(), beamEnergy, 0.01))
                     || (det != mQuantUsingZetaFactors.getDetector()))
                  mQuantUsingZetaFactors = new QuantifyUsingZetaFactors(det, beamEnergy);
               STEMPath.this.mBeamEnergy = beamEnergy;
               STEMPath.this.mDetector = det;
            }
            return res;
         }

         @Override
         protected EDSDetector getDetector() {
            return STEMPath.this.getDetector();
         }

         @Override
         protected ElectronProbe getProbe() {
            return STEMPath.this.getProbe();
         }
      }

      private class STEMStandardsPanel extends JWizardPanel {

         private static final long serialVersionUID = 6927463327214068204L;

         protected final JTable jTable_Standards = new JTable();
         protected final Action action_Add = new AddAction();
         protected final Action action_Remove = new RemoveAction();
         protected final Action action_Edit = new EditAction();

         private class AddAction extends AbstractAction {

            private static final long serialVersionUID = -1593644197045744319L;

            private AddAction() {
               super("Add");
            }

            /**
             * @param arg0
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed(ActionEvent arg0) {
               final ISpectrumData[] specs = selectSpectra(true);
               for (final ISpectrumData spec : specs) {
                  boolean open = true;
                  if (!SpectrumUtils.areCalibratedSimilar(mQuantUsingZetaFactors.getDetector().getProperties(), spec,
                        AppPreferences.DEFAULT_TOLERANCE))
                     open = (JOptionPane.showConfirmDialog(
                           QuantificationWizard.this, "<html>The calibration of <i>" + spec.toString() + "</i><br>"
                                 + "does not seem to be similar to the unknown.<br><br>" + "Use it none the less?",
                           "Spectrum open", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
                  if (open)
                     addSpectrum(spec);
               }
            }
         }

         private class RemoveAction extends AbstractAction {

            private static final long serialVersionUID = -6900948294886555803L;

            private RemoveAction() {
               super("Remove");
            }

            /**
             * @param arg0
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed(ActionEvent arg0) {
               final int[] rows = jTable_Standards.getSelectedRows();
               final List<ISpectrumData> selected = new ArrayList<>();
               for (final int row : rows)
                  selected.add(mStandards.get(row));
               mStandards.removeAll(selected);
               jTable_Standards.clearSelection();
               jTable_Standards.setModel(new StandardTableModel(mStandards));

            }
         }

         private class EditAction extends AbstractAction {

            private static final long serialVersionUID = -1366082302818358976L;

            private EditAction() {
               super("Edit");
            }

            /**
             * @param arg0
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed(ActionEvent arg0) {
               final int[] selected = jTable_Standards.getSelectedRows();
               for (final int sel : selected) {
                  ISpectrumData std = mStandards.get(sel);
                  Set<Element> prevStdized = getPreviouslyStandardized();
                  prevStdized.removeAll(std.getProperties().getStandardizedElements());
                  final ISpectrumData edited = editSpectrum(std, prevStdized);
                  mStandards.set(sel, edited);
               }
               jTable_Standards.setModel(new StandardTableModel(mStandards));
            }
         }

         private final Object[] COLUMN_NAMES = {"Spectrum", "Composition", "Elements", "Dose", "Mass-Thickness"};

         private class StandardTableModel extends DefaultTableModel {

            private static final long serialVersionUID = -7793243447602287617L;

            private final ArrayList<ISpectrumData> mSpectra;

            StandardTableModel(Collection<ISpectrumData> specs) {
               super(COLUMN_NAMES, specs.size());
               mSpectra = new ArrayList<>(specs);
            }

            /**
             * @param arg0
             * @param arg1
             * @return
             * @see javax.swing.table.TableModel#getValueAt(int, int)
             */
            @Override
            public Object getValueAt(int row, int col) {
               final ISpectrumData spec = mSpectra.get(row);
               final SpectrumProperties props = spec.getProperties();
               switch (col) {
                  case 0 : // Spectrum
                     return spec.toString();
                  case 1 : // Composition
                     return props.getCompositionWithDefault(SpectrumProperties.StandardComposition, Material.Null);
                  case 2 : { // Elements
                     final Set<Element> elms = props.getStandardizedElements();
                     final StringBuffer sb = new StringBuffer();
                     boolean first = true;
                     for (final Element elm : elms) {
                        if (!first)
                           sb.append(", ");
                        sb.append(elm.toAbbrev());
                        first = false;
                     }
                     return sb.toString();
                  }
                  case 3 : // Dose
                  {
                     final DecimalFormat df = new HalfUpFormat("#,##0.0");
                     try {
                        return df.format(SpectrumUtils.getDose(props)) + " nA·s";
                     } catch (final EPQException e) {
                        return "?";
                     }
                  }
                  case 4 : // Mass thickness
                  {
                     final Composition comp = props.getCompositionWithDefault(SpectrumProperties.StandardComposition, Material.Null);
                     final double density = (comp instanceof Material ? ((Material) comp).getDensity() : Double.NaN);
                     final SampleShape ss = props.getSampleShapeWithDefault(SpectrumProperties.SampleShape, null);
                     if ((ss instanceof SampleShape.ThinFilm) && (!Double.isNaN(density))) {
                        final SampleShape.ThinFilm tf = (SampleShape.ThinFilm) ss;
                        final DecimalFormat df = new HalfUpFormat("#,##0.0");
                        final double mt = FromSI.gPerCC(density) * tf.getThickness() * 1.0e2;
                        return df.format(mt * 1.0e6) + " μg/cm²";
                     }
                     return "?";
                  }
                  default :
                     return "WTF";

               }
            }
         }

         private final ArrayList<ISpectrumData> mStandards = new ArrayList<>();

         /**
          * Does this spectrum have all the properties necessary to be a
          * standard?
          *
          * @param spec
          * @return true if it does.
          */
         private boolean isSuitableAsStandard(ISpectrumData spec) {
            final SpectrumProperties props = spec.getProperties();
            final Composition comp = props.getCompositionWithDefault(SpectrumProperties.StandardComposition, null);
            if ((comp == null) || !(comp instanceof Material))
               return false;
            // Get the list of elements which this spectrum could provide
            // standards
            Set<Element> elms = props.getStandardizedElements();
            if (elms.size() == 0) {
               if (comp.getElementSet().size() == 1) {
                  elms = comp.getElementSet();
                  props.setStandardizedElements(comp.getElementSet());
               }
            }
            if (elms.size() == 0)
               return false;
            try {
               final double dose = SpectrumUtils.getDose(props);
               if (dose < 0.001)
                  return false;
            } catch (final EPQException e) {
               return false;
            }
            final SampleShape ss = props.getSampleShapeWithDefault(SpectrumProperties.SampleShape, null);
            if ((ss == null) || (!(ss instanceof SampleShape.ThinFilm)))
               return false;
            return true;
         }

         private TreeSet<Element> getPreviouslyStandardized() {
            final TreeSet<Element> stdized = new TreeSet<>();
            for (final ISpectrumData spec : mStandards)
               stdized.addAll(spec.getProperties().getStandardizedElements());
            return stdized;
         }

         private void addSpectrum(ISpectrumData spec) {
            if (!mStandards.contains(spec)) {
               final TreeSet<Element> prev = getPreviouslyStandardized();
               if (!isSuitableAsStandard(spec))
                  spec = editSpectrum(spec, prev);
               if (isSuitableAsStandard(spec)) {
                  Set<Element> stdized = new TreeSet<>(spec.getProperties().getStandardizedElements());
                  for (Element elm : prev)
                     stdized.remove(elm);
                  if (stdized.size() == 0) {
                     getWizard().setErrorText("There are already standards defined for " + spec.getProperties().getStandardizedElements() + ".");
                  } else {
                     mStandards.add(spec);
                     final StandardTableModel stm = new StandardTableModel(mStandards);
                     jTable_Standards.setModel(stm);
                  }
               }
            }
         }

         private ISpectrumData editSpectrum(ISpectrumData spec, Collection<Element> prevStdized) {
            return STEMStandardDialog.edit(QuantificationWizard.this, mSession, spec, prevStdized);
         }

         private void init() {
            final FormLayout fl = new FormLayout("300dlu, 10dlu, pref", "160dlu");
            setLayout(fl);
            add(new JScrollPane(jTable_Standards), CC.xy(1, 1));
            {
               final ButtonStackBuilder bbb = new ButtonStackBuilder();
               bbb.addButton(action_Add);
               bbb.addRelatedGap();
               bbb.addButton(action_Remove);
               bbb.addUnrelatedGap();
               bbb.addButton(action_Edit);
               bbb.addGlue();
               add(bbb.getPanel(), CC.xy(3, 1));
            }
         }

         /**
          * Constructs a STEMStandardsPanel
          *
          * @param wiz
          */
         public STEMStandardsPanel(JWizardDialog wiz) {
            super(wiz, "Specify thin-film standards");
            init();
         }

         @Override
         public void onShow() {
            assert QuantificationWizard.this.mQuantMode == QuantMode.ZETAFACTOR;
            jTable_Standards.setModel(new StandardTableModel(mStandards));
            getWizard().setNextPanel(jWizardPanel_STEMStrip.get());
         }

         @Override
         public boolean permitNext() {
            assert mQuantUsingZetaFactors != null;
            try {
               for (ISpectrumData std : mStandards) {
                  final SpectrumProperties props = std.getProperties();
                  for (Element elm : props.getStandardizedElements())
                     mQuantUsingZetaFactors.assignStandard(elm, std);
               }
            } catch (EPQException e) {
               ErrorDialog.createErrorMessage(QuantificationWizard.this, "Error with standards", e);
               return false;
            }
            return true;
         }

      }

      private class STEMUnknownsPanel extends JWizardPanel {

         private static final long serialVersionUID = 6430877038737989195L;

         private final JTable jTable_Unknown = new JTable();

         private final Object[] COLUMN_NAMES = {"Spectrum", "Dose", "Beam Energy"};

         private final List<ISpectrumData> mSpectra = new ArrayList<>();
         private boolean mFirstShow = true;

         private final class EditUnknownAction extends AbstractAction {
            private static final long serialVersionUID = 16146534775823917L;

            private EditUnknownAction() {
               super("Edit");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
               final int[] rows = jTable_Unknown.getSelectedRows();
               for (int r : rows) {
                  final Object obj = mSpectra.get(r);
                  if (obj instanceof ISpectrumData) {
                     final ISpectrumData spec = SpectrumUtils.copy((ISpectrumData) obj);
                     final SpectrumPropertyPanel.PropertyDialog pd = new SpectrumPropertyPanel.PropertyDialog(QuantificationWizard.this, mSession);
                     pd.setLocationRelativeTo(QuantificationWizard.this);
                     pd.addSpectrumProperties(spec.getProperties());
                     pd.setVisible(true);
                     if (pd.isOk())
                        mSpectra.set(r, spec);
                     jTable_Unknown.setModel(new UnknownTableModel(mSpectra));
                  }
               }
            }
         }

         private final class RemoveUnknownAction extends AbstractAction {
            private static final long serialVersionUID = 6032560216144220652L;

            private RemoveUnknownAction() {
               super("Remove");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
               final int[] rows = jTable_Unknown.getSelectedRows();
               ArrayList<ISpectrumData> removeMe = new ArrayList<>();
               for (int row : rows)
                  removeMe.add(mSpectra.get(row));
               mSpectra.removeAll(removeMe);
               jTable_Unknown.setModel(new UnknownTableModel(mSpectra));
            }
         }

         private class UnknownTableModel extends DefaultTableModel {

            private static final long serialVersionUID = -7793243447602287617L;

            private final ArrayList<ISpectrumData> mSpectra;

            UnknownTableModel(Collection<ISpectrumData> specs) {
               super(COLUMN_NAMES, specs.size());
               mSpectra = new ArrayList<>(specs);
            }

            /**
             * @param arg0
             * @param arg1
             * @return
             * @see javax.swing.table.TableModel#getValueAt(int, int)
             */
            @Override
            public Object getValueAt(int row, int col) {
               final ISpectrumData spec = mSpectra.get(row);
               final SpectrumProperties props = spec.getProperties();
               switch (col) {
                  case 0 : // Spectrum
                     return spec.toString();
                  case 1 : // Dose
                  {
                     final DecimalFormat df = new HalfUpFormat("#,##0.0");
                     try {
                        return df.format(SpectrumUtils.getDose(props)) + " nA·s";
                     } catch (final EPQException e) {
                        return "?";
                     }
                  }
                  case 2 : // // Beam energy
                  {
                     final double e0keV = SpectrumUtils.getBeamEnergy(spec) / 1000.0;
                     final DecimalFormat df = new HalfUpFormat("#,##0.0");
                     return df.format(e0keV) + " keV";
                  }
                  default :
                     return "WTF";

               }
            }
         }

         private void updateTable() {
            jTable_Unknown.setModel(new UnknownTableModel(mSpectra));
         }

         private final Action action_Remove = new RemoveUnknownAction();

         private final Action action_Edit = new EditUnknownAction();

         /**
          * Constructs a STEMUnknownsPanel
          *
          * @param wiz
          */
         public STEMUnknownsPanel(JWizardDialog wiz) {
            super(wiz, "Specify unknown's properties");
            init();
         }

         private void init() {
            final FormLayout fl = new FormLayout("300dlu, 10dlu, pref", "160dlu");
            setLayout(fl);
            add(new JScrollPane(jTable_Unknown), CC.xy(1, 1));
            {
               final ButtonStackBuilder bbb = new ButtonStackBuilder();
               bbb.addButton(action_Remove);
               bbb.addUnrelatedGap();
               bbb.addButton(action_Edit);
               bbb.addGlue();
               add(bbb.getPanel(), CC.xy(3, 1));
            }
         }

         @Override
         public void onShow() {
            assert QuantificationWizard.this.mQuantMode == QuantMode.ZETAFACTOR;
            if (mFirstShow) {
               // First time around add the spectra selected in the DataManager
               // (main screen)
               for (final ISpectrumData spec : mInputSpectra)
                  if (Math.abs(ToSI.eV(SpectrumUtils.getBeamEnergy(spec)) - getBeamEnergy()) < (getBeamEnergy() / 100.0))
                     mSpectra.add(spec);
               updateTable();
               mFirstShow = false;
            }
            getWizard().setMessageText("Specify the unknown spectra");
            getWizard().setNextPanel(jWizardPanel_STEMResults.get());
         }

         private boolean ready(ISpectrumData spec) {
            try {
               SpectrumUtils.getDose(spec.getProperties());
            } catch (EPQException e) {
               getWizard().setMessageText("Please specify the dose (probe current and live time.)");
               return false;
            }
            return true;
         }

         @Override
         public boolean permitNext() {
            for (ISpectrumData spec : mSpectra)
               if (!ready(spec))
                  return false;
            return true;
         }

      }

      private class STEMStripPanel extends JWizardPanel {

         private final Map<Element, ISpectrumData> mStrips = new TreeMap<>();

         private final class ActionAdd extends AbstractAction {
            private static final long serialVersionUID = -801842747705200415L;

            private ActionAdd(String name) {
               super(name);
            }

            @Override
            public void actionPerformed(ActionEvent arg0) {
               TreeSet<Element> prevs = jWizardPanel_STEMStandards.get().getPreviouslyStandardized();
               ISpectrumData[] specs = selectSpectra(true);
               for (final ISpectrumData spec : specs) {
                  SpectrumProperties props = spec.getProperties();
                  TreeSet<Element> elms = new TreeSet<>(props.getElements());
                  for (Element prev : prevs)
                     elms.remove(prev);
                  if (elms.size() == 1)
                     addStrip(elms.iterator().next(), spec);
                  else {
                     final Set<Element> sel = SelectElements.selectElements(QuantificationWizard.this, "Select elements", elms,
                           Collections.<Element>emptySet());
                     for (Element elm : sel)
                        addStrip(elm, spec);
                  }
               }
            }
         }

         private final class ActionRemove extends AbstractAction {
            private static final long serialVersionUID = -3425437693433987632L;

            private ActionRemove(String name) {
               super(name);
            }

            @Override
            public void actionPerformed(ActionEvent arg0) {

               if (jTable_Strip.getSelectedRowCount() == 0) {
                  final Set<Element> remove = SelectElements.selectElements(QuantificationWizard.this, "Select elements to remove", mStrips.keySet(),
                        Collections.<Element>emptyList());
                  for (Element elm : remove)
                     mStrips.remove(elm);
               } else {
                  final int[] rows = jTable_Strip.getSelectedRows();
                  for (int row : rows)
                     mStrips.remove(jTable_Strip.getValueAt(row, 0));
               }
               jTable_Strip.setModel(new StripTableModel(mStrips));
            }
         }

         private class StripTableModel extends DefaultTableModel {

            private static final long serialVersionUID = -4017699498048494110L;

            StripTableModel(Map<Element, ISpectrumData> strips) {
               super(new Object[]{"Element", "Spectrum"}, strips.size());
               int i = 0;
               for (Map.Entry<Element, ISpectrumData> me : strips.entrySet()) {
                  setValueAt(me.getKey(), i, 0);
                  setValueAt(me.getValue(), i, 1);
                  ++i;
               }
            }
         }

         private void addStrip(Element elm, ISpectrumData spec) {
            mStrips.put(elm, spec);
            jTable_Strip.setModel(new StripTableModel(mStrips));
         }

         private static final long serialVersionUID = 7413982614977149080L;

         private final AbstractAction action_Add = new ActionAdd("Add");

         private final AbstractAction action_Remove = new ActionRemove("Remove");

         private final JTable jTable_Strip = new JTable();

         /**
          * Constructs a STEMStripPanel
          *
          * @param wiz
          */
         public STEMStripPanel(JWizardDialog wiz) {
            super(wiz, "Other elements to strip");
            init();
         }

         private void init() {
            final FormLayout fl = new FormLayout("300dlu, 10dlu, pref", "160dlu");
            setLayout(fl);
            add(new JScrollPane(jTable_Strip), CC.xy(1, 1));
            {
               final ButtonStackBuilder bbb = new ButtonStackBuilder();
               bbb.addButton(action_Add);
               bbb.addRelatedGap();
               bbb.addButton(action_Remove);
               bbb.addGlue();
               add(bbb.getPanel(), CC.xy(3, 1));
            }
         }

         @Override
         public void onShow() {
            assert QuantificationWizard.this.mQuantMode == QuantMode.ZETAFACTOR;
            getWizard().setMessageText("Specify the elements to strip");
            setNextPanel(jWizardPanel_STEMUnknowns.get());
         }

         @Override
         public boolean permitNext() {
            assert mQuantUsingZetaFactors != null;
            for (Map.Entry<Element, ISpectrumData> me : mStrips.entrySet())
               mQuantUsingZetaFactors.assignStrip(me.getKey(), me.getValue());
            return true;
         }

      }

      private class PerformSTEMQuant extends SwingWorker<ArrayList<String>, ISpectrumData[]> {

         final private ArrayList<ISpectrumData> mUnknowns;
         final private QuantifyUsingZetaFactors mQuant;

         PerformSTEMQuant(QuantifyUsingZetaFactors quant, Collection<ISpectrumData> unknowns) {
            super();
            mQuant = quant;
            mUnknowns = new ArrayList<>(unknowns);
         }

         @Override
         public ArrayList<String> doInBackground() {
            final ArrayList<String> errors = new ArrayList<>();
            for (final ISpectrumData unk : mUnknowns)
               try {
                  final QuantifyUsingZetaFactors.Result results = mQuant.compute(unk, true);
                  final ISpectrumData[] res = new ISpectrumData[2];
                  res[0] = unk;
                  res[1] = results.getResidual();
                  publish(res);
               } catch (final Throwable e) {
                  final StringWriter sw = new StringWriter();
                  final PrintWriter pw = new PrintWriter(sw);
                  pw.append("Error fitting " + unk.toString() + ": " + e.getMessage());
                  e.printStackTrace(pw);
                  errors.add(sw.toString());
               }
            return errors;
         }

         @Override
         protected void process(List<ISpectrumData[]> results) {
            for (final ISpectrumData[] unk : results) {
               jWizardPanel_STEMResults.get().addResult(unk[0], unk[1]);
               mResultSpectra.add(unk[0]);
               final ISpectrumData residual = unk[1];
               if (residual != null)
                  mResultSpectra.add(residual);
            }
         }

         @Override
         protected void done() {
            enableInputMethods(true);
            enableFinish(true);
            setBackEnabled(true);
            try {
               final ArrayList<String> errs = get();
               if (errs.size() > 0) {
                  final StringBuffer sb = new StringBuffer();
                  for (final String err : errs) {
                     sb.append(err);
                     sb.append("\n");
                  }
                  QuantificationWizard.this.setExtendedError("Errors occured while quantifying the spectra", sb.toString());
               }
            } catch (final Exception e) {
               e.printStackTrace();
            }
         }

      }

      private class STEMResultsPanel extends JWizardPanel {

         private final class ShowTableMenuMouseAction extends MouseAdapter {
            @Override
            public void mousePressed(MouseEvent e) {
               if (e.isPopupTrigger())
                  jPopup_Mode.show(QuantificationWizard.this, e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
               if (e.isPopupTrigger())
                  jPopup_Mode.show(QuantificationWizard.this, e.getX(), e.getY());
            }
         }

         private final class CopyCompositionToClipboardAction implements ActionListener {

            private final ResultMode mMode;

            CopyCompositionToClipboardAction(ResultMode rm) {
               mMode = rm;
            }

            @Override
            public void actionPerformed(ActionEvent arg0) {
               final Clipboard clp = Toolkit.getDefaultToolkit().getSystemClipboard();
               final StringSelection ss = new StringSelection(jModel_Results.asTable(mMode));
               clp.setContents(ss, ss);
            }
         }

         private class STEMResultsModel extends AbstractTableModel {
            private static final long serialVersionUID = 0L;
            private final ArrayList<ISpectrumData> mResults = new ArrayList<>();
            private final ArrayList<ISpectrumData> mResiduals = new ArrayList<>();
            private final ArrayList<Composition> mCompositions = new ArrayList<>();
            private final ArrayList<Number> mMassThickness = new ArrayList<>();
            private final ArrayList<Element> mElements = new ArrayList<>();
            private ResultMode mMode = ResultMode.WEIGHT_PERCENT;
            private final NumberFormat mFormat = new HalfUpFormat("0.0000");

            void add(ISpectrumData spec, ISpectrumData residual, Composition comp, Number massThickness) {
               mResults.add(spec);
               mResiduals.add(residual);
               mCompositions.add(comp);
               mMassThickness.add(massThickness);
               final int s = mElements.size();
               for (final Element el : comp.getElementSet())
                  if (!mElements.contains(el))
                     mElements.add(el);
               if (mElements.size() != s) {
                  Collections.sort(mElements);
                  fireTableStructureChanged();
               } else
                  fireTableRowsInserted(mResults.size() - 1, mResults.size() - 1);
            }

            void clear() {
               mResults.clear();
               mCompositions.clear();
               mElements.clear();
               fireTableStructureChanged();
            }

            @Override
            public int getColumnCount() {
               return mElements.size() + 2;
            }

            @Override
            public int getRowCount() {
               return mResults.size();
            }

            @Override
            public Object getValueAt(int row, int col) {
               switch (col) {
                  case 0 :
                     return mResults.get(row).toString();
                  case 1 : {
                     HalfUpFormat nf = new HalfUpFormat("#,##0.0");
                     return UncertainValue2.format(nf, mMassThickness.get(row));
                  }
                  default :
                     final Element elm = mElements.get(col - 2);
                     final Composition comp = mCompositions.get(row);
                     switch (mMode) {
                        case ATOMIC_PERCENT :
                           return comp.atomicPercentU(elm).format(mFormat);
                        case NORM_WEIGHT_PERCENT :
                           return comp.weightFractionU(elm, true).format(mFormat);
                        default :
                           return comp.weightFractionU(elm, false).format(mFormat);
                     }
               }
            }

            @Override
            public String getColumnName(int col) {
               switch (col) {
                  case 0 :
                     return "Spectrum";
                  case 1 :
                     return "Mass-thickness";
                  default :
                     return mElements.get(col - 2).toAbbrev();
               }
            }

            private class NormalizeTableAction extends AbstractAction {

               private static final long serialVersionUID = -4028868694524444420L;
               private final ResultMode mNewResultMode;

               NormalizeTableAction(ResultMode mode, String title) {
                  super(title);
                  mNewResultMode = mode;
               }

               @Override
               public void actionPerformed(ActionEvent arg0) {
                  if (!mMode.equals(mNewResultMode)) {
                     mMode = mNewResultMode;
                     fireTableDataChanged();
                  }
               }
            }

            public String toHTML() {
               final StringWriter sw = new StringWriter(4096);
               if ((mResults.size() > 0) && (mQuantUsingZetaFactors != null)) {
                  final PrintWriter pw = new PrintWriter(sw);
                  pw.println("<DIV></DIV><H2>Composition from Standards Spectra Fit to STEM Unknown</H2>");
                  pw.print(mQuantUsingZetaFactors.toHTML());
                  // Top row
                  pw.print("<H3>Results</H3>");
                  // Result rows
                  pw.print(mQuantUsingZetaFactors.tabulateResults(mResults, DTSA2.getReport().getFile().getParentFile(), mResiduals));
               }
               return sw.toString();
            }

            public String asTable(ResultMode mode) {
               final StringWriter sw = new StringWriter(4096);
               if (mResults.size() > 0) {
                  final PrintWriter pw = new PrintWriter(sw);
                  final NumberFormat nf2 = new HalfUpFormat("0.0000");
                  // Header row
                  pw.print("Spectrum\tQuantity");
                  for (final Element el : mElements) {
                     pw.print("\t");
                     pw.print(el.toAbbrev());
                     pw.print("\t");
                     pw.print("d(" + el.toAbbrev() + ")");
                  }
                  if (mode != ResultMode.ATOMIC_PERCENT)
                     pw.print("\tSum");
                  pw.print("\n");
                  // Result rows
                  final Map<Element, DescriptiveStatistics> stats = new TreeMap<>();
                  for (final Element elm : mElements)
                     stats.put(elm, new DescriptiveStatistics());
                  for (int row = 0; row < mResults.size(); ++row) {
                     // Separator line between spectra
                     final ISpectrumData spec = mResults.get(row);
                     final Composition comp = mCompositions.get(row);
                     pw.print(spec);
                     switch (mode) {
                        case WEIGHT_PERCENT : {
                           for (final Element elm : mElements) {
                              pw.print("\t");
                              final UncertainValue2 res = comp.weightFractionU(elm, false);
                              stats.get(elm).add(res.doubleValue());
                              pw.print(nf2.format(res.doubleValue()));
                              pw.print("\t");
                              pw.print(nf2.format(res.uncertainty()));
                           }
                           pw.print("\t");
                           pw.print(nf2.format(comp.sumWeightFraction()));
                           pw.print("\n");
                           break;
                        }
                        case NORM_WEIGHT_PERCENT : {
                           for (final Element elm : mElements) {
                              pw.print("\t");
                              final UncertainValue2 res = comp.weightFractionU(elm, true);
                              stats.get(elm).add(res.doubleValue());
                              pw.print(nf2.format(res.doubleValue()));
                              pw.print("\t");
                              pw.print(nf2.format(res.uncertainty()));
                           }
                           pw.print("\t");
                           pw.print(nf2.format(comp.sumWeightFraction()));
                           pw.print("\n");
                           break;
                        }
                        case ATOMIC_PERCENT : {
                           for (final Element elm : mElements) {
                              final UncertainValue2 res = comp.atomicPercentU(elm);
                              stats.get(elm).add(res.doubleValue());
                              pw.print("\t");
                              pw.print(nf2.format(res.doubleValue()));
                              pw.print("\t");
                              pw.print(nf2.format(res.uncertainty()));
                           }
                           pw.print("\n");
                           break;
                        }
                     }
                  }
                  if (mResults.size() > 1) {
                     pw.print("Average");
                     for (final Element elm : mElements) {
                        final DescriptiveStatistics ds = stats.get(elm);
                        pw.print("\t");
                        pw.print(nf2.format(ds.average()));
                        pw.print("\t");
                     }
                     pw.print("\n");
                  }
                  if (mResults.size() > 2) {
                     pw.print("Standard Deviation");
                     for (final Element elm : mElements) {
                        final DescriptiveStatistics ds = stats.get(elm);
                        pw.print("\t");
                        pw.print(nf2.format(ds.standardDeviation()));
                        pw.print("\t");
                     }
                     pw.print("\n");
                  }
               }
               return sw.toString();
            }
         }

         private static final long serialVersionUID = 5827359399157061229L;

         private final JTable jTable_Results = new JTable();
         private final STEMResultsModel jModel_Results = new STEMResultsModel();
         private final JPopupMenu jPopup_Mode = new JPopupMenu();

         /**
          * Constructs a STEMResultsPanel
          *
          * @param wiz
          */
         public STEMResultsPanel(JWizardDialog wiz) {
            super(wiz, "Results");
            init();
            jTable_Results.setModel(jModel_Results);
         }

         private void init() {
            add(new JScrollPane(jTable_Results), BorderLayout.CENTER);

            final JMenuItem massFrac = new JMenuItem();
            final JMenuItem normMassFrac = new JMenuItem("Copy as normalized mass fraction)");
            final JMenuItem atomFrac = new JMenuItem("Copy as atom fraction)");

            massFrac.addActionListener(new CopyCompositionToClipboardAction(ResultMode.WEIGHT_PERCENT));

            normMassFrac.addActionListener(new CopyCompositionToClipboardAction(ResultMode.NORM_WEIGHT_PERCENT));

            atomFrac.addActionListener(new CopyCompositionToClipboardAction(ResultMode.ATOMIC_PERCENT));

            jPopup_Mode.add(jModel_Results.new NormalizeTableAction(ResultMode.WEIGHT_PERCENT, "Weight fraction"));
            jPopup_Mode.add(jModel_Results.new NormalizeTableAction(ResultMode.NORM_WEIGHT_PERCENT, "Normalized weight fraction"));
            jPopup_Mode.add(jModel_Results.new NormalizeTableAction(ResultMode.ATOMIC_PERCENT, "Atomic fraction"));
            jTable_Results.add(jPopup_Mode);
            jTable_Results.addMouseListener(new ShowTableMenuMouseAction());
         }

         public void addResult(final ISpectrumData spec, final ISpectrumData residual) {
            final Composition comp = spec.getProperties().getCompositionWithDefault(SpectrumProperties.MicroanalyticalComposition, Material.Null);
            final Number massThickness = spec.getProperties().getNumericWithDefault(SpectrumProperties.MassThickness, UncertainValue2.ZERO);
            jModel_Results.add(spec, residual, comp, massThickness);
         }

         @Override
         public void onShow() {
            assert QuantificationWizard.this.mQuantMode == QuantMode.ZETAFACTOR;
            getWizard().setNextPanel(null);
            enableInputMethods(false);
            enableFinish(false);
            setBackEnabled(false);
            jModel_Results.clear();
            final PerformSTEMQuant pq = new PerformSTEMQuant(mQuantUsingZetaFactors, jWizardPanel_STEMUnknowns.get().mSpectra);
            pq.execute();
         }
      }

      private final LazyEvaluate<STEMInstrumentPanel> jWizardPanel_STEMInstrument = new LazyEvaluate<>() {

         @Override
         protected STEMInstrumentPanel compute() {
            return new STEMInstrumentPanel(QuantificationWizard.this);
         }
      };
      private final LazyEvaluate<STEMStandardsPanel> jWizardPanel_STEMStandards = new LazyEvaluate<>() {
         @Override
         protected STEMStandardsPanel compute() {
            return new STEMStandardsPanel(QuantificationWizard.this);
         }
      };
      private final LazyEvaluate<STEMStripPanel> jWizardPanel_STEMStrip = new LazyEvaluate<>() {
         @Override
         protected STEMStripPanel compute() {
            return new STEMStripPanel(QuantificationWizard.this);
         }
      };
      private final LazyEvaluate<STEMUnknownsPanel> jWizardPanel_STEMUnknowns = new LazyEvaluate<>() {

         @Override
         protected STEMUnknownsPanel compute() {
            return new STEMUnknownsPanel(QuantificationWizard.this);
         }
      };
      private final LazyEvaluate<STEMResultsPanel> jWizardPanel_STEMResults = new LazyEvaluate<>() {

         @Override
         protected STEMResultsPanel compute() {
            return new STEMResultsPanel(QuantificationWizard.this);
         }
      };

      @Override
      public String toHTML() {
         return jWizardPanel_STEMResults.get().jModel_Results.toHTML();
      }

      @Override
      public JWizardPanel firstPanel() {
         return jWizardPanel_STEMInstrument.get();
      }

   }

   private class STEMinSEMPath extends JQuantPath {

      private QuantifyUsingSTEMinSEM mSTEMinSEMQuant;

      private List<Result> mResults;

      /**
       * Allows the user to specify standard spectra for elements in the
       * unknown.
       */
      private class SiSStandardPanel extends JWizardPanel {
         static private final long serialVersionUID = 0x1286dea34234L;
         static private final int SPECTRUM_COL = 0;
         static private final int ELEMENT_COL = 1;
         static private final int COMPOSITION_COL = 4;
         private final double[] COL_WIDTHS = new double[]{0.20, 0.14, 0.16, 0.16, 0.17, 0.17};

         private final class EditSpectrumPropertiesAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
               editSpectrumProperties();
            }
         }

         private final class ClearPanelAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
               clearPanel();
            }
         }

         private final class RemoveRowAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
               removeRow();
            }
         }

         private final class AddRowFromDatabaseAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent ae) {
               addRowFromDatabase();
            }
         }

         private final class AddRowFromFileAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent ae) {
               addRowFromSpectrumFile();
            }
         }

         private final class AddRowFromStandardAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent ae) {
               addRowFromStandard();
            }
         }

         private final class EditMaterialAction extends AbstractAction {
            final static private long serialVersionUID = 0x1;

            @Override
            public void actionPerformed(ActionEvent ae) {
               @SuppressWarnings("unchecked")
               final JComboBox<Composition> src = (JComboBox<Composition>) ae.getSource();
               // getWizard().clearMessageText();
               final int r = jTable_Standards.getSelectedRow();
               if (r == -1) {
                  getWizard().setErrorText("No row selected.");
                  return;
               }
               boolean isValid = (src.getSelectedIndex() < (src.getItemCount() - 1));
               if (!isValid) {
                  final Composition newMat = MaterialsCreator.createMaterial(getWizard(), DTSA2.getSession(), false);
                  if (newMat != null) {
                     final StringBuffer errs = null;
                     isValid = newMat.containsElement(getElement(r));
                     if (!isValid) {
                        Toolkit.getDefaultToolkit().beep();
                        getWizard().setExtendedError("Inappropriate standard.", "This material does not contain the specified element(s).");
                        return;
                     }
                     final DefaultComboBoxModel<Composition> dcbm = (DefaultComboBoxModel<Composition>) src.getModel();
                     dcbm.insertElementAt(newMat, src.getItemCount() - 1);
                     jTableModel_Standards.setValueAt(newMat, r, COMPOSITION_COL);
                     if (errs == null)
                        getWizard().setMessageText("The composition has been set to " + newMat.toString());
                  } else {
                     final DefaultComboBoxModel<Composition> dcbm = (DefaultComboBoxModel<Composition>) src.getModel();
                     jTableModel_Standards.setValueAt(dcbm.getElementAt(0), r, COMPOSITION_COL);
                     getWizard().setMessageText("No material constructed.");
                  }
               }
            }
         }

         DefaultTableModel jTableModel_Standards = new DefaultTableModel(
               new Object[]{"Spectrum", "Element", "Probe Current", "Live time", "Composition", "Duane-Hunt"}, 0) {
            static private final long serialVersionUID = 0xa34d643456L;

            @Override
            public boolean isCellEditable(int row, int col) {
               return col == COMPOSITION_COL;
            }
         };
         protected final JTable jTable_Standards = new JTable(jTableModel_Standards);
         private EachRowEditor jEachRowEditor_Composition = new EachRowEditor(jTable_Standards);
         protected JButton jButton_AddDatabase;
         protected final Map<RegionOfInterest, ISpectrumData> mReferencePool = new TreeMap<>();

         protected ISpectrumData getSpectrum(int r) {
            return (ISpectrumData) jTableModel_Standards.getValueAt(r, SPECTRUM_COL);
         }

         protected Composition getComposition(int r) {
            return (Composition) jTableModel_Standards.getValueAt(r, COMPOSITION_COL);
         }

         protected Element getElement(int r) {
            return (Element) jTableModel_Standards.getValueAt(r, ELEMENT_COL);
         }

         protected Set<Element> usedElements() {
            final Set<Element> elms = new HashSet<>();
            for (int r = 0; r < jTableModel_Standards.getRowCount(); ++r)
               elms.add(getElement(r));
            return elms;
         }

         private void addRowFromSpectrumFile() {
            final ISpectrumData[] specs = selectSpectra(true);
            for (final ISpectrumData spec : specs) {
               boolean open = true;
               if (!SpectrumUtils.areCalibratedSimilar(STEMinSEMPath.this.getDetector().getProperties(), spec, AppPreferences.DEFAULT_TOLERANCE))
                  open = (JOptionPane.showConfirmDialog(
                        QuantificationWizard.this, "<html>The calibration of <i>" + spec.toString() + "</i><br>"
                              + "does not seem to be similar to the unknown.<br><br>" + "Use it none the less?",
                        "Spectrum open", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
               if (open)
                  addSpectrum(spec);
            }
         }

         private void addRowFromStandard() {
            final StandardBundle[] specs = selectStandardBundles(STEMinSEMPath.this.getDetector(), true);
            for (final StandardBundle spec : specs) {
               boolean open = true;
               if (!SpectrumUtils.areCalibratedSimilar(STEMinSEMPath.this.getDetector().getProperties(), spec.getStandard(),
                     AppPreferences.DEFAULT_TOLERANCE))
                  open = (JOptionPane.showConfirmDialog(
                        QuantificationWizard.this, "<html>The calibration of <i>" + spec.toString() + "</i><br>"
                              + "does not seem to be similar to the unknown.<br><br>" + "Use it none the less?",
                        "DTSA-II Standard Select", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
               if (open)
                  addSpectrum(spec);
               mReferencePool.putAll(spec.getReferences());
            }
         }

         private void addRowFromDatabase() {
            if (mSession != null) {
               final SelectElements se = new SelectElements(QuantificationWizard.this,
                     "Select elements for which to select standards from the database");
               se.setMultiSelect(true);
               se.setLocationRelativeTo(QuantificationWizard.this);
               se.setVisible(true);
               final Collection<Element> elms = se.getElements();
               final StringBuffer errs = new StringBuffer();
               for (final Element elm : elms)
                  try {
                     final Collection<Session.SpectrumSummary> res = mSession.findStandards(STEMinSEMPath.this.getDetector().getDetectorProperties(),
                           FromSI.keV(STEMinSEMPath.this.getBeamEnergy()), elm);
                     final ResultDialog rd = new ResultDialog(QuantificationWizard.this, "Select a standard spectrum for " + elm.toString(), true);
                     rd.setSingleSelect(true);
                     rd.setLocationRelativeTo(QuantificationWizard.this);
                     rd.setSpectra(res);
                     if (rd.showDialog())
                        addSpectrum(rd.getSpectra().get(0), elm, false);
                     else
                        errs.append("No standard selected for " + elm.toString() + "\n");
                  } catch (final Exception e) {
                     ErrorDialog.createErrorMessage(QuantificationWizard.this, "Select a standard spectrum for " + elm.toString(), e);
                     errs.append(e.getMessage() + "\n");
                  }
               if (errs.length() > 0)
                  getWizard().setExtendedError("There was at least one error while selecting standards", errs.toString());
            }
         }

         private void addSpectrum(StandardBundle spec) {
            addSpectrum(spec.getStandard(), spec.getElement(), true);
         }

         private void addSpectrum(ISpectrumData spec, Element stdElm, boolean isQuantified) {
            final NumberFormat df = new HalfUpFormat("#0.0");
            final SpectrumProperties sp = spec.getProperties();
            // Check again to see whether the required properties are defined
            if (!validateRequiredProperties(spec)) {
               getWizard().setMessageText(spec + " is missing the probe current, live time and/or beam energy");
               return;
            }
            final double e0 = ToSI.keV(sp.getNumericWithDefault(SpectrumProperties.BeamEnergy, Double.NaN));
            assert !Double.isNaN(e0);
            if (Math.abs(e0 - STEMinSEMPath.this.getBeamEnergy()) > ToSI.keV(0.1)) {
               final String message = "The beam energy of the selected spectrum (" + df.format(FromSI.keV(e0)) + " keV) does\n"
                     + "not match the beam energy of the previously selected spectra.  It is a bad\n"
                     + "idea to mix beam energies. Use this spectrum none-the-less?";
               final String title = "Beam energy mismatch";
               final int answer = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
               if (answer == JOptionPane.NO_OPTION)
                  return;
            }
            final Composition comp = sp.getCompositionWithDefault(SpectrumProperties.StandardComposition, Material.Null);
            addRow(spec, comp, stdElm);
         }

         private void addSpectrum(ISpectrumData spec) {
            final SpectrumProperties sp = spec.getProperties();
            final Composition comp = sp.getCompositionWithDefault(SpectrumProperties.StandardComposition, Material.Null);
            // Get the element(s) for which this spectrum is a standard
            final SelectElements se = new SelectElements(QuantificationWizard.this,
                  "Select the element(s) for which " + spec.toString() + " is a standard.");
            Set<Element> elms = null;
            if (!comp.equals(Material.Null)) {
               final Set<Element> used = usedElements();
               final Set<Element> avail = new TreeSet<>(comp.getElementSet());
               avail.removeAll(used);
               if (avail.size() == 0) {
                  JOptionPane.showMessageDialog(getWizard(), "The spectrum " + spec.toString() + " can not act as standard for any unused elements.",
                        "Standard redundancy", JOptionPane.INFORMATION_MESSAGE);
                  return;
               } else if (avail.size() == 1) {
                  elms = new TreeSet<>();
                  elms.addAll(avail);
               } else {
                  se.enableAll(false);
                  for (final Element elm : avail)
                     se.setEnabled(elm, true);
               }
            } else {
               se.enableAll(true);
               for (final Element elm : usedElements())
                  se.setEnabled(elm, false);
            }
            if (elms == null) {
               getWizard().centerDialog(se);
               se.setVisible(true);
               elms = se.getElements();
            }
            for (Element elm : elms)
               addSpectrum(spec, elm, true);
         }

         protected void addRow(ISpectrumData spec, Composition comp, Element elm) {
            final SpectrumProperties sp = spec.getProperties();
            final double fc = SpectrumUtils.getAverageFaradayCurrent(sp, Double.NaN);
            final double lt = sp.getNumericWithDefault(SpectrumProperties.LiveTime, Double.NaN);
            final NumberFormat nf1 = new HalfUpFormat("0.0");
            final NumberFormat nf3 = new HalfUpFormat("0.000");
            final double dh = FromSI.keV(DuaneHuntLimit.DefaultDuaneHunt.compute(spec));
            final double e0 = sp.getNumericWithDefault(SpectrumProperties.BeamEnergy, Double.NaN);
            sp.setNumericProperty(SpectrumProperties.DuaneHunt, dh);
            jTableModel_Standards.addRow(new Object[]{spec, elm, nf3.format(fc), nf1.format(lt), comp,
                  (duaneHuntThreshold(dh, e0) ? nf3.format(dh) : "<html><font color=red>" + nf3.format(dh) + "</font>")});
            final int row = jTableModel_Standards.getRowCount() - 1;
            final JComboBox<Composition> cb = new JComboBox<>();
            Composition first = null;
            if (!comp.equals(Material.Null)) {
               cb.addItem(comp);
               first = comp;
            }
            final ArrayList<Composition> refs = new ArrayList<>(MaterialFactory.getCommonStandards(elm));
            if ((refs.size() == 0) && (first == null)) {
               final Composition mat = MaterialsCreator.createMaterial(getWizard(), DTSA2.getSession(), false);
               if ((mat != null) && (!mat.equals(Material.Null)))
                  refs.add(mat);
            }
            for (final Composition ref : refs) {
               cb.addItem(ref);
               if (first == null)
                  first = ref;
            }
            cb.addItem(NEW_MATERIAL);
            cb.addActionListener(new EditMaterialAction());
            if (first != null)
               cb.setSelectedItem(first);
            jEachRowEditor_Composition.setEditorAt(row, new DefaultCellEditor(cb));
            jTableModel_Standards.setValueAt(first, row, COMPOSITION_COL);
            jTable_Standards.setRowSelectionInterval(row, row);
            getWizard().setMessageText(spec.toString() + " assigned as a standard for " + getElement(row));
         }

         private void removeRow() {
            final int r = jTable_Standards.getSelectedRow();
            if (r >= 0) {
               jTableModel_Standards.removeRow(r);
               jEachRowEditor_Composition.removeEditor(r);
            }
         }

         private void editSpectrumProperties() {
            final int r = jTable_Standards.getSelectedRow();
            if (r >= 0) {
               final Object obj = jTableModel_Standards.getValueAt(r, 0);
               if (obj instanceof ISpectrumData) {
                  final ISpectrumData spec = (ISpectrumData) obj;
                  final SpectrumPropertyPanel.PropertyDialog pd = new SpectrumPropertyPanel.PropertyDialog(QuantificationWizard.this, mSession);
                  pd.setLocationRelativeTo(QuantificationWizard.this);
                  pd.addSpectrumProperties(spec.getProperties());
                  pd.setVisible(true);
                  if (pd.isOk()) {
                     jTableModel_Standards.setValueAt(spec, r, 0);
                     final SpectrumProperties newProps = pd.getSpectrumProperties();
                     final SpectrumProperties sp = spec.getProperties();
                     sp.addAll(pd.getSpectrumProperties());
                     if (newProps.getDetector() instanceof EDSDetector)
                        jTableModel_Standards.setValueAt(spec, r, 0);
                     final double fc = SpectrumUtils.getAverageFaradayCurrent(sp, Double.NaN);
                     final double lt = sp.getNumericWithDefault(SpectrumProperties.LiveTime, Double.NaN);
                     final NumberFormat nf1 = new HalfUpFormat("0.0");
                     final NumberFormat nf3 = new HalfUpFormat("0.000");
                     jTableModel_Standards.setValueAt(nf3.format(fc), r, 2);
                     jTableModel_Standards.setValueAt(nf1.format(lt), r, 3);
                  }
               }
            }
         }

         private void clearPanel() {
            getWizard().clearMessageText();
            jTableModel_Standards.setRowCount(0);
            jEachRowEditor_Composition = new EachRowEditor(jTable_Standards);
         }

         protected SiSStandardPanel(JWizardDialog wiz, JQuantPath path) {
            super(wiz, "Select standards", new FormLayout("270dlu, 5dlu, pref", "top:140dlu"));
            mPath = path;
            try {
               initialize();
            } catch (final RuntimeException e) {
            }
         }

         private void initialize() {
            // jTable_Standards.setForeground(SystemColor.textText);
            {
               final TableColumnModel cm = jTable_Standards.getColumnModel();
               final int total = cm.getTotalColumnWidth();
               assert COL_WIDTHS.length == cm.getColumnCount();
               for (int i = 0; i < COL_WIDTHS.length; ++i)
                  cm.getColumn(i).setPreferredWidth((int) Math.round(COL_WIDTHS[i] * total));
            }
            // jTable_Standards.getColumnModel().getColumn(0).
            final JScrollPane pane = new JScrollPane(jTable_Standards);
            jTable_Standards.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            jTable_Standards.getColumnModel().getColumn(COMPOSITION_COL).setCellEditor(jEachRowEditor_Composition);
            final CellConstraints cc = new CellConstraints();
            add(pane, cc.xy(1, 1));
            {
               final JPanel btnPanel = new JPanel(new FormLayout("pref", "pref, 3dlu, pref, 3dlu, pref, 15dlu, pref, 3dlu, pref, 15dlu, pref"));

               final JButton std = new JButton("Standard..");
               std.setToolTipText("Select a \"Standard Bundle\" from which to load an elemental standard and references.");
               std.addActionListener(new AddRowFromStandardAction());
               btnPanel.add(std, cc.xy(1, 1));

               final JButton add = new JButton("Spectrum..");
               add.setToolTipText("Select a spectrum from a file to serve as an elemental standard.");
               add.addActionListener(new AddRowFromFileAction());
               btnPanel.add(add, cc.xy(1, 3));

               jButton_AddDatabase = new JButton("Database..");
               jButton_AddDatabase.setToolTipText("Select a spectrum from the database to serve as an elemental standard.");
               jButton_AddDatabase.addActionListener(new AddRowFromDatabaseAction());
               btnPanel.add(jButton_AddDatabase, cc.xy(1, 5));

               final JButton remove = new JButton("Remove");
               remove.setToolTipText("Remove the selected spectrum or spectra from the list of elemental standards.");
               remove.addActionListener(new RemoveRowAction());
               btnPanel.add(remove, cc.xy(1, 7));

               final JButton clear = new JButton("Clear");
               clear.setToolTipText("Clear all spectra from the standards list.");
               clear.addActionListener(new ClearPanelAction());
               btnPanel.add(clear, cc.xy(1, 9));

               final JButton edit = new JButton("Properties");
               edit.setToolTipText("Edit the spectrum properties.");
               edit.addActionListener(new EditSpectrumPropertiesAction());
               btnPanel.add(edit, cc.xy(1, 11));

               add(btnPanel, cc.xy(3, 1));
            }
         }

         @Override
         public boolean permitNext() {
            try {
               if (jTable_Standards.getRowCount() > 0) {
                  final TableModel tm = jTable_Standards.getModel();
                  mSTEMinSEMQuant = new QuantifyUsingSTEMinSEM(STEMinSEMPath.this.getDetector(), STEMinSEMPath.this.getBeamEnergy());
                  for (int r = 0; r < tm.getRowCount(); ++r) {
                     final Element elm = getElement(r);
                     final ISpectrumData spec = getSpectrum(r);
                     if (spec == null) {
                        getWizard().setErrorText("Specify a spectrum in row " + Integer.toString(r + 1));
                        return false;
                     }
                     final Composition comp = getComposition(r);
                     final boolean valid = comp.containsElement(elm);
                     if (!valid) {
                        getWizard().setErrorText("The material in row " + Integer.toString(r + 1) + " does not contain " + elm.toString());
                        return false;
                     }
                     try {
                        mSTEMinSEMQuant.addStandard(elm, comp, spec);
                     } catch (final EPQException e) {
                        getWizard().setErrorText("ERROR: " + e.getMessage());
                        return false;
                     }
                  }
                  return true;
               }
            } catch (final Exception e) {
               getWizard().setExceptionText("Fix the error in this table.", e);
            }
            return false;
         }

         @Override
         public void onShow() {
            assert QuantificationWizard.this.mQuantMode == QuantMode.STEMinSEM;
            assert STEMinSEMPath.this.getBeamEnergy() > ToSI.keV(0.1);
            assert STEMinSEMPath.this.getBeamEnergy() < ToSI.keV(500.0);
            jButton_AddDatabase.setEnabled(mSession != null);
            // Clear and reenter all data in this panel...
            jTableModel_Standards.setRowCount(0);
            final Map<Element, ISpectrumData> stds = mSTEMinSEMQuant.getStandardSpectra();
            for (final ISpectrumData std : new TreeSet<>(stds.values())) {
               final Set<Element> elms = new TreeSet<>();
               for (final Map.Entry<Element, ISpectrumData> me : stds.entrySet())
                  if (me.getValue() == std)
                     elms.add(me.getKey());
               final Composition comp = std.getProperties().getCompositionWithDefault(SpectrumProperties.StandardComposition, null);
               assert comp != null : "Composition not set in onShow";
               for (Element elm : elms)
                  addRow(std, comp, elm);
            }
            setMessageText("Specify standard spectra and the associated elements and compositions.");
            setNextPanel(jWizardPanel_LayerSelection.get());
            enableFinish(false);
         }
      }

      public class STEMinSEMInstrumentPanel extends GenericInstrumentPanel {

         private static final long serialVersionUID = -5432773600725361704L;

         public STEMinSEMInstrumentPanel(JWizardDialog wiz) {
            super(wiz, jWizardPanel_STEMinSEMStandard.get());
         }

         @Override
         public boolean permitNext() {
            boolean res = super.permitNext();
            if (res) {
               try {
                  final EDSDetector det = buildDetector();
                  final double beamEnergy = ToSI.keV(getBeamEnergy_keV());
                  mSTEMinSEMQuant = new QuantifyUsingSTEMinSEM(det, beamEnergy);
                  STEMinSEMPath.this.mBeamEnergy = beamEnergy;
                  STEMinSEMPath.this.mDetector = det;
               } catch (EPQException e) {
                  res = false;
                  e.printStackTrace();
               }
            }
            return res;
         }

         @Override
         protected ElectronProbe getProbe() {
            return STEMinSEMPath.this.getProbe();
         }

         @Override
         protected EDSDetector getDetector() {
            return STEMinSEMPath.this.getDetector();
         }
      }

      private class LayerSelectionPanel extends JWizardPanel {

         private static final long serialVersionUID = -1078123954283293363L;

         private final JTable jTable_Layer = new JTable();
         private DefaultTableModel jTableModel_Layer;
         private Map<Element, Integer> mLayers = null;
         EachRowEditor mLayerEditor;

         private final static String[] COLUMN_NAMES = new String[]{"Element", "Layer"};

         public LayerSelectionPanel(JWizardDialog wiz) {
            super(wiz, "Assign elements to layers");
            mLayers = new HashMap<>();
            mLayerEditor = new EachRowEditor(jTable_Layer);
            PanelBuilder pb = new PanelBuilder(new FormLayout("150dlu", "150dlu"));
            pb.add(new JScrollPane(jTable_Layer), CC.rc(1, 1));
            this.add(pb.getPanel());
         }

         private String layerToString(int val) {
            return val <= 0 ? "Stripped" : Integer.toString(val);
         }

         private int stringToLayer(String str) {
            return str == "Stripped" ? 0 : Integer.parseInt(str);
         }

         @Override
         public void onShow() {
            Set<Element> elms = STEMinSEMPath.this.mSTEMinSEMQuant.getFitSpectra().keySet();
            for (Element elm : new HashSet<>(mLayers.keySet()))
               if (!elms.contains(elm))
                  mLayers.remove(elm);
            for (Element elm : elms)
               if (!mLayers.containsKey(elm))
                  mLayers.put(elm, 1);
            jTableModel_Layer = new DefaultTableModel(COLUMN_NAMES, 0);
            for (Element elm : elms)
               jTableModel_Layer.addRow(new Object[]{elm, layerToString(mLayers.get(elm))});
            Vector<String> items = new Vector<>();
            for (int i = 0; i <= mLayers.size(); ++i)
               items.add(layerToString(i));
            JComboBox<String> jcb = new JComboBox<>(items);
            mLayerEditor.setDefaultEditor(jcb);
            jTable_Layer.setModel(jTableModel_Layer);
            jTable_Layer.getColumnModel().getColumn(1).setCellEditor(mLayerEditor);
            getWizard().setNextPanel(jWizardPanel_Results.get());
         }

         @Override
         public boolean permitNext() {
            final int len = jTable_Layer.getRowCount();
            final int[] assigned = new int[len + 1];
            Map<Element, Integer> layers = new HashMap<>();
            for (int row = 0; row < len; ++row) {
               Element elm = (Element) jTableModel_Layer.getValueAt(row, 0);
               int layer = stringToLayer((String) jTableModel_Layer.getValueAt(row, 1));
               assigned[layer]++;
               layers.put(elm, layer);
            }
            if (assigned[0] == len) {
               getWizard().setErrorText("All elements are being stripped.");
               return false; // All stripped
            }
            int sum = 0;
            for (int i = assigned.length - 1; i >= 1; i--) {
               if ((sum > 0) && (assigned[i] == 0)) {
                  getWizard().setErrorText("Layer " + i + " is not assigned any elements. Don't skip a layer.");
                  return false; // Missing layer
               }
               sum += assigned[i];
            }
            getWizard().setMessageText("");
            mLayers.clear();
            mLayers.putAll(layers);
            STEMinSEMPath.this.mSTEMinSEMQuant.setLayers(mLayers);
            return true;
         }
      }

      private class ResultPanel extends JWizardPanel {

         private static final long serialVersionUID = -5238531341629556061L;

         private static final String[] COLUMN_NAMES = new String[]{"Spectrum", "Layer", "Mass-Thickness", "Element", "Mass-fraction"};
         private static final double[] COLUMN_WIDTHS = new double[]{0.45, 0.10, 0.15, 0.15, 0.15};

         private final JTable jTable_Results = new JTable();
         private final DefaultTableModel jTableModel = new DefaultTableModel(COLUMN_NAMES, 0);

         public ResultPanel(JWizardDialog wiz) {
            super(wiz, "STEM-in-SEM results");
            PanelBuilder pb = new PanelBuilder(new FormLayout("250dlu", "150dlu"));
            pb.add(new JScrollPane(jTable_Results), CC.rc(1, 1));
            jTable_Results.setModel(jTableModel);
            this.add(pb.getPanel());
            {
               final TableColumnModel cm = jTable_Results.getColumnModel();
               final int total = cm.getTotalColumnWidth();
               assert COLUMN_WIDTHS.length == cm.getColumnCount();
               for (int i = 0; i < COLUMN_WIDTHS.length; ++i)
                  cm.getColumn(i).setPreferredWidth((int) Math.round(COLUMN_WIDTHS[i] * total));
            }
         }

         @Override
         public void onShow() {
            StringBuilder sb = new StringBuilder();
            mProcessedSpectra = new ArrayList<>(mInputSpectra);
            jTableModel.setRowCount(0);
            mResults = new ArrayList<>();
            QuantificationWizard.this.mResultSpectra.clear();
            DecimalFormat df1 = new DecimalFormat("0.0");
            DecimalFormat df2 = new DecimalFormat("0.000");
            setMessageText("Mass-thickness is in µg/cm² (1  µg/cm² = 10 nm/(g/cm³))");
            for (ISpectrumData spec : QuantificationWizard.this.mProcessedSpectra)
               try {
                  Result res = mSTEMinSEMQuant.compute(spec);
                  mResults.add(res);
                  QuantificationWizard.this.mResultSpectra.add(res.getResidual());
                  SpectrumProperties sp = spec.getProperties();
                  List<Pair<Composition, Double>> lyrs = res.getLayers();
                  if (lyrs.size() == 1) {
                     sp.setObjectProperty(SpectrumProperties.MicroanalyticalComposition, lyrs.get(0).first);
                     sp.setNumericProperty(SpectrumProperties.MassThickness, lyrs.get(0).second * 1.0e5);
                  } else {
                     sp.remove(SpectrumProperties.MicroanalyticalComposition);
                     sp.remove(SpectrumProperties.MassThickness);
                  }
                  sp.setObjectProperty(SpectrumProperties.MultiLayerMeasurement, res);
                  int layer = 1;
                  for (Pair<Composition, Double> lyr : lyrs) {
                     final Composition comp = lyr.first;
                     for (Element elm : comp.getElementSet()) {
                        jTableModel.addRow(new Object[]{ //
                              spec.toString(), //
                              Integer.valueOf(layer), //
                              df1.format(lyr.second * 1.0e5), // µg/cm^2
                              elm.toAbbrev(), //
                              df2.format(comp.weightFraction(elm, false)) // mass-fraction
                        });
                     }

                     ++layer;
                  }
               } catch (EPQException e) {
                  sb.append(e.getMessage());
               }
            setNextPanel(null);
            getWizard().enableFinish(true);
         }

         @Override
         public boolean permitNext() {

            return true;
         }

      }

      /**
       * Allows the user to specify an Instrument, Detector/Calibration and beam
       * energy for the unknown spectrum.
       */
      private final LazyEvaluate<STEMinSEMInstrumentPanel> jWizardPanel_STEMinSEMInstrument = new LazyEvaluate<>() {
         @Override
         protected STEMinSEMInstrumentPanel compute() {
            return new STEMinSEMInstrumentPanel(QuantificationWizard.this);
         }
      };

      private final LazyEvaluate<SiSStandardPanel> jWizardPanel_STEMinSEMStandard = new LazyEvaluate<>() {

         @Override
         protected SiSStandardPanel compute() {
            return new SiSStandardPanel(QuantificationWizard.this, STEMinSEMPath.this);
         }
      };

      private final LazyEvaluate<LayerSelectionPanel> jWizardPanel_LayerSelection = new LazyEvaluate<>() {
         protected LayerSelectionPanel compute() {
            return new LayerSelectionPanel(QuantificationWizard.this);
         }
      };

      private final LazyEvaluate<ResultPanel> jWizardPanel_Results = new LazyEvaluate<>() {
         protected ResultPanel compute() {
            return new ResultPanel(QuantificationWizard.this);
         }
      };

      @Override
      public String toHTML() {
         return mSTEMinSEMQuant.toHTML() + //
               "<h2>Results</h2>" + //
               Result.toHTML(mResults);
      }

      @Override
      public JWizardPanel firstPanel() {
         return jWizardPanel_STEMinSEMInstrument.get();
      }

   }

   static private final Composition NEW_MATERIAL = createNewMaterial();

   private static final Composition createNewMaterial() {
      final Composition res = Material.Null;
      res.setName("-- NEW MATERIAL --");
      return res;
   }

   static private final String REFERENCE_DIR = "Reference directory";
   static private final String UNKNOWN_DIR = "Unknowns directory";

   private double mDefaultBeamEnergy = ToSI.keV(20.0);
   private Session mSession;

   enum QuantMode {
      NONE, MLSQ, KRATIO, ZETAFACTOR, STEMinSEM
   }

   private QuantMode mQuantMode = QuantMode.NONE;
   private JWizardPath mPath = null;

   private final ArrayList<ISpectrumData> mInputSpectra;

   private ArrayList<ISpectrumData> mProcessedSpectra;
   // Result data items
   private final ArrayList<ISpectrumData> mResultSpectra = new ArrayList<>();

   private final Preferences mUserPref = Preferences.userNodeForPackage(QuantificationWizard.class);

   /**
    * Constructs a QuantificationWizard
    *
    * @param owner
    */
   public QuantificationWizard(Frame owner, Collection<ISpectrumData> spectra) {
      super(owner, "Quantification Alien", true);
      mInputSpectra = new ArrayList<>(spectra);
      try {
         initialize();
         pack();
      } catch (final Exception ex) {
         ex.printStackTrace();
      }
   }

   /**
    * Ensure that LiveTime, (FaradayBegin || FaradayEnd) and BeamEnergy are all
    * defined...
    *
    * @param spec
    * @return boolean true if all are defined...
    */
   private boolean validateRequiredProperties(ISpectrumData spec) {
      final SpectrumProperties sp = spec.getProperties();
      boolean ok = sp.isDefined(SpectrumProperties.LiveTime) && sp.isDefined(SpectrumProperties.ProbeCurrent)
            && sp.isDefined(SpectrumProperties.BeamEnergy);
      if (!ok) {
         final SpectrumPropertyPanel.PropertyDialog dlg = new SpectrumPropertyPanel.PropertyDialog(this, mSession);
         final SpectrumProperties.PropertyId[] required = new SpectrumProperties.PropertyId[]{SpectrumProperties.BeamEnergy,
               SpectrumProperties.ProbeCurrent, SpectrumProperties.LiveTime};
         dlg.setRequiredProperties(Arrays.asList(required));
         dlg.disableDetectorProperties();
         dlg.addSpectrumProperties(sp);
         dlg.setTitle("Properties for " + spec.toString());
         dlg.showPane(SpectrumPropertyPanel.CONDITIONS_PANEL);
         centerDialog(dlg);
         dlg.setVisible(true);
         ok = dlg.isOk();
         if (ok)
            sp.addAll(dlg.getSpectrumProperties());
      }
      return ok;
   }

   private final LazyEvaluate<IntroPanel> jWizardPanel_Intro = new LazyEvaluate<>() {
      @Override
      protected IntroPanel compute() {
         return new IntroPanel(QuantificationWizard.this);
      }
   };

   /**
    * Allows the user to select which operation to perform.
    */
   private class IntroPanel extends JWizardPanel {
      private final class UpdateQuantModeAction extends AbstractAction {
         final static private long serialVersionUID = 0x123a8c8c4L;

         @Override
         public void actionPerformed(ActionEvent e) {
            updateQuantMode();
         }
      }

      private static final long serialVersionUID = 0x44;
      private final JLabel mIntro = new JLabel(
            "<html>Select the mode which best describes the operation you wish to perform.  The mode you select will determine what "
                  + "information you will be asked to provide and what information will be computed.");
      private final JRadioButton jRadioButton_KRatio = new JRadioButton("Determine the composition from k-ratios");
      private final JRadioButton jRadioButton_MLSQ = new JRadioButton(
            "Determine the composition of an \'unknown\' spectrum by MLLSQ fitting to standards");
      private final JRadioButton jRadioButton_STEM = new JRadioButton("Quantify a STEM spectrum using MLLSQ fitting and \u03B6-factors");
      private final JRadioButton jRadioButton_STEMinSEM = new JRadioButton("Quantify a STEM-in-SEM spectrum using bulk standards");

      private IntroPanel(JWizardDialog wiz) {
         super(wiz, "Select an operation");
         try {
            initialize();
         } catch (final Exception ex) {
            ex.printStackTrace();
         }
      }

      private void updateQuantMode() {
         if (jRadioButton_MLSQ.isSelected()) {
            mPath = new LLSQPath();
            mQuantMode = QuantMode.MLSQ;
         } else if (jRadioButton_KRatio.isSelected()) {
            mPath = new KRatioPath();
            mQuantMode = QuantMode.KRATIO;
         } else if (jRadioButton_STEM.isSelected()) {
            mPath = new STEMPath();
            mQuantMode = QuantMode.ZETAFACTOR;
         } else if (jRadioButton_STEMinSEM.isSelected()) {
            mPath = new STEMinSEMPath();
            mQuantMode = QuantMode.STEMinSEM;
         } else {
            mPath = null;
            mQuantMode = QuantMode.NONE;
         }
         if (mPath != null)
            getWizard().setNextPanel(mPath.firstPanel());
      }

      private void initialize() {
         setLayout(new FormLayout("300dlu", "pref, 10dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref"));
         final CellConstraints cc = new CellConstraints();
         jRadioButton_MLSQ.setSelected(true);
         add(mIntro, cc.xy(1, 1));
         add(jRadioButton_MLSQ, cc.xy(1, 3));
         add(jRadioButton_KRatio, cc.xy(1, 5));
         add(jRadioButton_STEM, cc.xy(1, 7));
         add(jRadioButton_STEMinSEM, cc.xy(1, 9));

         final AbstractAction rbAction = new UpdateQuantModeAction();

         jRadioButton_KRatio.addActionListener(rbAction);
         jRadioButton_MLSQ.addActionListener(rbAction);
         jRadioButton_STEM.addActionListener(rbAction);
         jRadioButton_STEMinSEM.addActionListener(rbAction);

         final ButtonGroup group = new ButtonGroup();
         group.add(jRadioButton_KRatio);
         group.add(jRadioButton_MLSQ);
         group.add(jRadioButton_STEM);
         group.add(jRadioButton_STEMinSEM);

         // jRadioButton_STEMinSEM.setEnabled(false);

         jRadioButton_KRatio.setToolTipText(
               "<html>You will be asked to manually enter the composition<br>of standard materials and the associated k-ratios.<br>The k-ratios will be corrected for matrix effects.");
         jRadioButton_MLSQ.setToolTipText(
               "<html>You will be asked to provide standard spectra which<br>will be fit using multiple-linear least-squares<br>to the unknown spectrum.  The result can be corrected<br>for matrix effects.");
         jRadioButton_STEM.setToolTipText(
               "<html>You will be asked to provide thin-film standard spectra which<br>will be fit to an unknown spectrum.  The result will be quantified using Watanabe's ζ-factors.");
         jRadioButton_STEM.setToolTipText(
               "<html>You will be asked to provide bulk standard spectra which<br>will be fit to an unknown spectrum.  The result will be quantified.");
      }

      @Override
      public void onShow() {
         getWizard().setMessageText("Select an analysis mode.");
         updateQuantMode();
         getWizard().enableFinish(false);
      }
   }

   /**
    * Allows the user to specify an Instrument, Detector/Calibration and beam
    * energy for the unknown spectrum.
    */
   public abstract class GenericInstrumentPanel extends JWizardPanel {

      private final class OnUpdateDetectorAction implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent e) {
            updateCalibrations(null);
         }
      }

      private final class OnUpdateInstrumentAction extends AbstractAction {
         private static final long serialVersionUID = -51520409728313274L;

         @Override
         public void actionPerformed(ActionEvent e) {
            updateDetectors(null);
         }
      }

      private final JComboBox<ElectronProbe> jComboBox_Instrument = new JComboBox<>();
      private final JComboBox<DetectorProperties> jComboBox_Detector = new JComboBox<>();
      private final JComboBox<EDSCalibration> jComboBox_Calibration = new JComboBox<>();
      private final JTextFieldDouble jTextField_BeamEnergy = new JTextFieldDouble(1.0, 1.0, 512.0);

      private static final long serialVersionUID = -6481374858932055638L;
      private boolean mFirstShow = true;

      private final JWizardPanel mNext;

      public GenericInstrumentPanel(JWizardDialog wiz, JWizardPanel next) {
         super(wiz, "Specify instrumental paramters");
         mNext = next;
         try {
            initialize();
         } catch (final Exception ex) {
            ex.printStackTrace();
         }
      }

      protected double getBeamEnergy_keV() {
         return jTextField_BeamEnergy.getValue();
      }

      private void initialize() {
         final FormLayout fl = new FormLayout("10dlu, right:pref, 3dlu, 40dlu, 3dlu, 150dlu, 3dlu, pref",
               "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref");
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
         pb.addSeparator("Setting", cc.xyw(1, 11, 8));
         pb.addLabel("at a beam energy of", cc.xy(2, 13));
         pb.add(jTextField_BeamEnergy, cc.xy(4, 13));
         pb.addLabel("keV.", cc.xy(6, 13));
         mFirstShow = true;
         getWizard().enableFinish(false);
      }

      protected EDSDetector buildDetector() {
         final DetectorProperties props = (DetectorProperties) jComboBox_Detector.getSelectedItem();
         final EDSCalibration calib = (EDSCalibration) jComboBox_Calibration.getSelectedItem();
         return EDSDetector.createDetector(props, calib);
      }

      @Override
      public boolean permitNext() {
         final ElectronProbe probe = (ElectronProbe) jComboBox_Instrument.getSelectedItem();
         final EDSDetector det = buildDetector();
         assert det.getOwner() == probe;
         final boolean res = (probe != null);
         if (!res)
            getWizard().setErrorText("Please specify an instrument and detector.");
         return res;
      }

      abstract protected ElectronProbe getProbe();

      abstract protected EDSDetector getDetector();

      @Override
      public void onShow() {
         assert (QuantificationWizard.this.mQuantMode == QuantMode.MLSQ) || (QuantificationWizard.this.mQuantMode == QuantMode.STEMinSEM);
         if (mFirstShow) {
            DetectorProperties defProps = AppPreferences.getInstance().getDefaultDetector();
            DetectorCalibration defCal = null;
            if (defProps != null) {
               defCal = mSession.getMostRecentCalibration(defProps);
               for (final ISpectrumData spec : mInputSpectra)
                  if (spec.getProperties().getDetector() instanceof EDSDetector) {
                     final EDSDetector det = (EDSDetector) spec.getProperties().getDetector();
                     defProps = det.getDetectorProperties();
                     defCal = det.getCalibration();
                     break;
                  }
            }
            {
               final Set<ElectronProbe> eps = mSession.getCurrentProbes();
               final DefaultComboBoxModel<ElectronProbe> dcmb = new DefaultComboBoxModel<>();
               for (final ElectronProbe pr : eps)
                  dcmb.addElement(pr);
               dcmb.setSelectedItem(defProps != null ? defProps.getOwner() : eps.iterator().next());
               jComboBox_Instrument.setModel(dcmb);
               updateDetectors(defProps);
               updateCalibrations(defCal);
               jComboBox_Instrument.addActionListener(new OnUpdateInstrumentAction());

               jComboBox_Detector.addActionListener(new OnUpdateDetectorAction());
            }
            jTextField_BeamEnergy.setValue(FromSI.keV(mDefaultBeamEnergy));
            jTextField_BeamEnergy.setBackground(SystemColor.window);
            mFirstShow = false;
         }
         getWizard().setNextPanel(mNext);
         getWizard().enableNext(true);
      }

      private void updateDetectors(DetectorProperties defDp) {
         final ElectronProbe newProbe = (ElectronProbe) jComboBox_Instrument.getSelectedItem();
         final DefaultComboBoxModel<DetectorProperties> dcmb = new DefaultComboBoxModel<>();
         if ((newProbe != null) && (newProbe != getProbe())) {
            for (final DetectorProperties dp : mSession.getDetectors(newProbe))
               dcmb.addElement(dp);
            dcmb.setSelectedItem(defDp != null ? defDp : (dcmb.getSize() > 0 ? dcmb.getElementAt(0) : null));
         }
         jComboBox_Detector.setModel(dcmb);
         updateCalibrations(null);
      }

      private void updateCalibrations(DetectorCalibration defCal) {
         final DetectorProperties newDet = (DetectorProperties) jComboBox_Detector.getSelectedItem();
         final DefaultComboBoxModel<EDSCalibration> dcmb = new DefaultComboBoxModel<>();
         if ((newDet != null) && ((getDetector() == null) || (newDet != getDetector().getDetectorProperties()))) {
            for (final DetectorCalibration dc : mSession.getCalibrations(newDet))
               if (dc instanceof EDSCalibration)
                  dcmb.addElement((EDSCalibration) dc);
            dcmb.setSelectedItem(defCal != null ? defCal : (dcmb.getSize() > 0 ? dcmb.getElementAt(0) : null));
         }
         jComboBox_Calibration.setModel(dcmb);
      }
   }

   /**
    * Allows the user to specify standard spectra for elements in the unknown.
    */
   private class BaseStandardPanel extends JWizardPanel {
      static private final long serialVersionUID = 0x1286dea34234L;
      static private final int SPECTRUM_COL = 0;
      static private final int ELEMENT_COL = 1;
      static private final int COMPOSITION_COL = 4;
      static private final int STRIP_COL = 5;
      private final double[] COL_WIDTHS = new double[]{0.28, 0.10, 0.12, 0.12, 0.25, 0.10, 0.13};

      private final JQuantPath mPath;

      private final class EditSpectrumPropertiesAction implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent e) {
            editSpectrumProperties();
         }
      }

      private final class ClearPanelAction implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent e) {
            clearPanel();
         }
      }

      private final class RemoveRowAction implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent e) {
            removeRow();
         }
      }

      private final class AddRowFromDatabaseAction implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent ae) {
            addRowFromDatabase();
         }
      }

      private final class AddRowFromFileAction implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent ae) {
            addRowFromSpectrumFile();
         }
      }

      private final class AddRowFromStandardAction implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent ae) {
            addRowFromStandard();
         }
      }

      private final class EditMaterialAction extends AbstractAction {
         final static private long serialVersionUID = 0x1;

         @Override
         public void actionPerformed(ActionEvent ae) {
            @SuppressWarnings("unchecked")
            final JComboBox<Composition> src = (JComboBox<Composition>) ae.getSource();
            // getWizard().clearMessageText();
            final int r = jTable_Standards.getSelectedRow();
            if (r == -1) {
               getWizard().setErrorText("No row selected.");
               return;
            }
            boolean isValid = (src.getSelectedIndex() < (src.getItemCount() - 1));
            if (!isValid) {
               final Composition newMat = MaterialsCreator.createMaterial(getWizard(), DTSA2.getSession(), false);
               if (newMat != null) {
                  final StringBuffer errs = null;
                  isValid = newMat.containsElement(getElement(r));
                  if (!isValid) {
                     Toolkit.getDefaultToolkit().beep();
                     getWizard().setExtendedError("Inappropriate standard.", "This material does not contain the specified element(s).");
                     return;
                  }
                  final DefaultComboBoxModel<Composition> dcbm = (DefaultComboBoxModel<Composition>) src.getModel();
                  dcbm.insertElementAt(newMat, src.getItemCount() - 1);
                  jTableModel_Standards.setValueAt(newMat, r, COMPOSITION_COL);
                  if (errs == null)
                     getWizard().setMessageText("The composition has been set to " + newMat.toString());
               } else {
                  final DefaultComboBoxModel<Composition> dcbm = (DefaultComboBoxModel<Composition>) src.getModel();
                  jTableModel_Standards.setValueAt(dcbm.getElementAt(0), r, COMPOSITION_COL);
                  getWizard().setMessageText("No material constructed.");
               }
            }
         }
      }

      private class ElementSet {
         Set<Element> mElements = new TreeSet<>();

         ElementSet(Collection<Element> elms) {
            mElements.addAll(elms);
         }

         @Override
         public String toString() {
            final StringBuffer sb = new StringBuffer();
            final Iterator<Element> i = mElements.iterator();
            if (i.hasNext()) {
               sb.append(i.next().toAbbrev());
               while (i.hasNext()) {
                  sb.append(", ");
                  sb.append(i.next().toAbbrev());
               }
            } else
               sb.append("None");
            return sb.toString();
         }

         Set<Element> getElements() {
            return mElements;
         }
      }

      DefaultTableModel jTableModel_Standards = new DefaultTableModel(
            new Object[]{"Spectrum", "Element", "Probe (nA)", "Live time", "Composition", "Strip", "Duane-Hunt"}, 0) {
         static private final long serialVersionUID = 0xa34d643456L;

         @Override
         public boolean isCellEditable(int row, int col) {
            return col == COMPOSITION_COL;
         }
      };
      protected final JTable jTable_Standards = new JTable(jTableModel_Standards);
      private EachRowEditor jEachRowEditor_Composition = new EachRowEditor(jTable_Standards);
      protected JButton jButton_AddDatabase;
      protected final Map<RegionOfInterest, ISpectrumData> mReferencePool = new TreeMap<>();

      protected ISpectrumData getSpectrum(int r) {
         return (ISpectrumData) jTableModel_Standards.getValueAt(r, SPECTRUM_COL);
      }

      protected Composition getComposition(int r) {
         return (Composition) jTableModel_Standards.getValueAt(r, COMPOSITION_COL);
      }

      protected Element getElement(int r) {
         return (Element) jTableModel_Standards.getValueAt(r, ELEMENT_COL);
      }

      protected Set<Element> usedElements() {
         final Set<Element> elms = new HashSet<>();
         for (int r = 0; r < jTableModel_Standards.getRowCount(); ++r)
            elms.add(getElement(r));
         return elms;
      }

      protected ElementSet getStripElements(int r) {
         return (ElementSet) jTableModel_Standards.getValueAt(r, STRIP_COL);
      }

      private void addRowFromSpectrumFile() {
         final ISpectrumData[] specs = selectSpectra(true);
         for (final ISpectrumData spec : specs) {
            boolean open = true;
            if (!SpectrumUtils.areCalibratedSimilar(mPath.getDetector().getProperties(), spec, AppPreferences.DEFAULT_TOLERANCE))
               open = (JOptionPane.showConfirmDialog(
                     QuantificationWizard.this, "<html>The calibration of <i>" + spec.toString() + "</i><br>"
                           + "does not seem to be similar to the unknown.<br><br>" + "Use it none the less?",
                     "Spectrum open", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
            if (open)
               addSpectrum(spec);
         }
      }

      private void addRowFromStandard() {
         final StandardBundle[] specs = selectStandardBundles(mPath.getDetector(), true);
         for (final StandardBundle spec : specs) {
            boolean open = true;
            if (!SpectrumUtils.areCalibratedSimilar(mPath.getDetector().getProperties(), spec.getStandard(), AppPreferences.DEFAULT_TOLERANCE))
               open = (JOptionPane.showConfirmDialog(
                     QuantificationWizard.this, "<html>The calibration of <i>" + spec.toString() + "</i><br>"
                           + "does not seem to be similar to the unknown.<br><br>" + "Use it none the less?",
                     "DTSA-II Standard Select", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
            if (open)
               addSpectrum(spec);
            mReferencePool.putAll(spec.getReferences());
         }
      }

      protected RegionOfInterest bestMatch(RegionOfInterest roi, Set<RegionOfInterest> reqRefs) {
         assert roi.getElementSet().size() == 1;
         RegionOfInterest best = null;
         double bestScore = 0.95;
         for (RegionOfInterest reqRoi : reqRefs) {
            assert reqRoi.getElementSet().size() == 1;
            double score = score(reqRoi, roi);
            if (score > bestScore) {
               bestScore = score;
               best = reqRoi;
            }
         }
         return best;
      }

      private double score(RegionOfInterest roi1, RegionOfInterest roi2) {
         final Set<XRayTransition> xrts1 = new TreeSet<>(roi1.getAllTransitions().getTransitions());
         final XRayTransitionSet xrts2 = roi2.getAllTransitions();
         double sum = 0.0, all = 0.0;
         for (XRayTransition xrt : xrts2.getTransitions()) {
            all += xrt.getNormalizedWeight();
            if (xrts1.contains(xrt)) {
               sum += xrt.getNormalizedWeight();
               xrts1.remove(xrt);
            }
         }
         for (XRayTransition xrt : xrts1)
            all += xrt.getNormalizedWeight();
         return sum / all;
      }

      private void addRowFromDatabase() {
         if (mSession != null) {
            final SelectElements se = new SelectElements(QuantificationWizard.this,
                  "Select elements for which to select standards from the database");
            se.setMultiSelect(true);
            se.setLocationRelativeTo(QuantificationWizard.this);
            se.setVisible(true);
            final Collection<Element> elms = se.getElements();
            final StringBuffer errs = new StringBuffer();
            for (final Element elm : elms)
               try {
                  final Collection<Session.SpectrumSummary> res = mSession.findStandards(mPath.getDetector().getDetectorProperties(),
                        FromSI.keV(mPath.getBeamEnergy()), elm);
                  final ResultDialog rd = new ResultDialog(QuantificationWizard.this, "Select a standard spectrum for " + elm.toString(), true);
                  rd.setSingleSelect(true);
                  rd.setLocationRelativeTo(QuantificationWizard.this);
                  rd.setSpectra(res);
                  if (rd.showDialog())
                     addSpectrum(rd.getSpectra().get(0), elm, Collections.emptySet());
                  else
                     errs.append("No standard selected for " + elm.toString() + "\n");
               } catch (final Exception e) {
                  ErrorDialog.createErrorMessage(QuantificationWizard.this, "Select a standard spectrum for " + elm.toString(), e);
                  errs.append(e.getMessage() + "\n");
               }
            if (errs.length() > 0)
               getWizard().setExtendedError("There was at least one error while selecting standards", errs.toString());
         }
      }

      private void addSpectrum(StandardBundle spec) {
         addSpectrum(spec.getStandard(), spec.getElement(), spec.getStrippedElements());
      }

      private void addSpectrum(ISpectrumData spec, Element stdElm, Set<Element> stripElms) {
         final NumberFormat df = new HalfUpFormat("#0.0");
         final SpectrumProperties sp = spec.getProperties();
         // Check again to see whether the required properties are defined
         if (!validateRequiredProperties(spec)) {
            getWizard().setMessageText(spec + " is missing the probe current, live time and/or beam energy");
            return;
         }
         final double e0 = ToSI.keV(sp.getNumericWithDefault(SpectrumProperties.BeamEnergy, Double.NaN));
         assert !Double.isNaN(e0);
         if (Math.abs(e0 - mPath.getBeamEnergy()) > ToSI.keV(0.1)) {
            final String message = "The beam energy of the selected spectrum (" + df.format(FromSI.keV(e0)) + " keV) does\n"
                  + "not match the beam energy of the previously selected spectra.  It is a bad\n"
                  + "idea to mix beam energies. Use this spectrum none-the-less?";
            final String title = "Beam energy mismatch";
            final int answer = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (answer == JOptionPane.NO_OPTION)
               return;
         }
         final Composition comp = sp.getCompositionWithDefault(SpectrumProperties.StandardComposition, Material.Null);
         addRow(spec, comp, stdElm, stripElms);
      }

      private void addSpectrum(ISpectrumData spec) {
         final SpectrumProperties sp = spec.getProperties();
         final Composition comp = sp.getCompositionWithDefault(SpectrumProperties.StandardComposition, Material.Null);
         // Get the element(s) for which this spectrum is a standard
         final SelectElements se = new SelectElements(QuantificationWizard.this,
               "Select the element(s) for which " + spec.toString() + " is a standard.");
         Set<Element> elms = null;
         if (!comp.equals(Material.Null)) {
            final Set<Element> used = usedElements();
            final Set<Element> avail = new TreeSet<>(comp.getElementSet());
            avail.removeAll(used);
            if (avail.size() == 0) {
               JOptionPane.showMessageDialog(getWizard(), "The spectrum " + spec.toString() + " can not act as standard for any unused elements.",
                     "Standard redundancy", JOptionPane.INFORMATION_MESSAGE);
               return;
            } else if (avail.size() == 1) {
               elms = new TreeSet<>();
               elms.addAll(avail);
            } else {
               se.enableAll(false);
               for (final Element elm : avail)
                  se.setEnabled(elm, true);
            }
         } else {
            se.enableAll(true);
            for (final Element elm : usedElements())
               se.setEnabled(elm, false);
         }
         if (elms == null) {
            getWizard().centerDialog(se);
            se.setVisible(true);
            elms = se.getElements();
         }
         for (Element elm : elms)
            addSpectrum(spec, elm, Collections.emptySet());
      }

      protected void addRow(ISpectrumData spec, Composition comp, Element elm, Set<Element> strip) {
         final SpectrumProperties sp = spec.getProperties();
         final double fc = SpectrumUtils.getAverageFaradayCurrent(sp, Double.NaN);
         final double lt = sp.getNumericWithDefault(SpectrumProperties.LiveTime, Double.NaN);
         final NumberFormat nf1 = new HalfUpFormat("0.0");
         final NumberFormat nf3 = new HalfUpFormat("0.000");
         final double dh = FromSI.keV(DuaneHuntLimit.DefaultDuaneHunt.compute(spec));
         final double e0 = sp.getNumericWithDefault(SpectrumProperties.BeamEnergy, Double.NaN);
         sp.setNumericProperty(SpectrumProperties.DuaneHunt, dh);
         jTableModel_Standards.addRow(new Object[]{spec, elm, nf3.format(fc), nf1.format(lt), comp, new ElementSet(strip),
               (duaneHuntThreshold(dh, e0) ? nf3.format(dh) : "<html><font color=red>" + nf3.format(dh) + "</font>")});
         final int row = jTableModel_Standards.getRowCount() - 1;
         final JComboBox<Composition> cb = new JComboBox<>();
         Composition first = null;
         if (!comp.equals(Material.Null)) {
            cb.addItem(comp);
            first = comp;
         }
         final ArrayList<Composition> refs = new ArrayList<>(MaterialFactory.getCommonStandards(elm));
         if ((refs.size() == 0) && (first == null)) {
            final Composition mat = MaterialsCreator.createMaterial(getWizard(), DTSA2.getSession(), false);
            if ((mat != null) && (!mat.equals(Material.Null)))
               refs.add(mat);
         }
         for (final Composition ref : refs) {
            cb.addItem(ref);
            if (first == null)
               first = ref;
         }
         cb.addItem(NEW_MATERIAL);
         cb.addActionListener(new EditMaterialAction());
         if (first != null)
            cb.setSelectedItem(first);
         jEachRowEditor_Composition.setEditorAt(row, new DefaultCellEditor(cb));
         jTableModel_Standards.setValueAt(first, row, COMPOSITION_COL);
         jTable_Standards.setRowSelectionInterval(row, row);
         getWizard().setMessageText(spec.toString() + " assigned as a standard for " + getElement(row));
      }

      private void removeRow() {
         final int r = jTable_Standards.getSelectedRow();
         if (r >= 0) {
            jTableModel_Standards.removeRow(r);
            jEachRowEditor_Composition.removeEditor(r);
         }
      }

      private void editSpectrumProperties() {
         final int r = jTable_Standards.getSelectedRow();
         if (r >= 0) {
            final Object obj = jTableModel_Standards.getValueAt(r, 0);
            if (obj instanceof ISpectrumData) {
               final ISpectrumData spec = (ISpectrumData) obj;
               final SpectrumPropertyPanel.PropertyDialog pd = new SpectrumPropertyPanel.PropertyDialog(QuantificationWizard.this, mSession);
               pd.setLocationRelativeTo(QuantificationWizard.this);
               pd.addSpectrumProperties(spec.getProperties());
               pd.setVisible(true);
               if (pd.isOk()) {
                  jTableModel_Standards.setValueAt(spec, r, 0);
                  final SpectrumProperties newProps = pd.getSpectrumProperties();
                  final SpectrumProperties sp = spec.getProperties();
                  sp.addAll(pd.getSpectrumProperties());
                  if (newProps.getDetector() instanceof EDSDetector)
                     jTableModel_Standards.setValueAt(spec, r, 0);
                  final double fc = SpectrumUtils.getAverageFaradayCurrent(sp, Double.NaN);
                  final double lt = sp.getNumericWithDefault(SpectrumProperties.LiveTime, Double.NaN);
                  final NumberFormat nf1 = new HalfUpFormat("0.0");
                  final NumberFormat nf3 = new HalfUpFormat("0.000");
                  jTableModel_Standards.setValueAt(nf3.format(fc), r, 2);
                  jTableModel_Standards.setValueAt(nf1.format(lt), r, 3);
               }
            }
         }
      }

      private void clearPanel() {
         getWizard().clearMessageText();
         jTableModel_Standards.setRowCount(0);
         jEachRowEditor_Composition = new EachRowEditor(jTable_Standards);
      }

      protected BaseStandardPanel(JWizardDialog wiz, JQuantPath path) {
         super(wiz, "Select standards", new FormLayout("270dlu, 5dlu, pref", "top:140dlu"));
         mPath = path;
         try {
            initialize();
         } catch (final RuntimeException e) {
         }
      }

      private void initialize() {
         // jTable_Standards.setForeground(SystemColor.textText);
         {
            final TableColumnModel cm = jTable_Standards.getColumnModel();
            final int total = cm.getTotalColumnWidth();
            assert COL_WIDTHS.length == cm.getColumnCount();
            for (int i = 0; i < COL_WIDTHS.length; ++i)
               cm.getColumn(i).setPreferredWidth((int) Math.round(COL_WIDTHS[i] * total));
         }
         // jTable_Standards.getColumnModel().getColumn(0).
         final JScrollPane pane = new JScrollPane(jTable_Standards);
         jTable_Standards.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         jTable_Standards.getColumnModel().getColumn(COMPOSITION_COL).setCellEditor(jEachRowEditor_Composition);
         final CellConstraints cc = new CellConstraints();
         add(pane, cc.xy(1, 1));
         {
            final JPanel btnPanel = new JPanel(new FormLayout("pref", "pref, 3dlu, pref, 3dlu, pref, 15dlu, pref, 3dlu, pref, 15dlu, pref"));

            final JButton std = new JButton("Standard..");
            std.setToolTipText("Select a \"Standard Bundle\" from which to load an elemental standard and references.");
            std.addActionListener(new AddRowFromStandardAction());
            btnPanel.add(std, cc.xy(1, 1));

            final JButton add = new JButton("Spectrum..");
            add.setToolTipText("Select a spectrum from a file to serve as an elemental standard.");
            add.addActionListener(new AddRowFromFileAction());
            btnPanel.add(add, cc.xy(1, 3));

            jButton_AddDatabase = new JButton("Database..");
            jButton_AddDatabase.setToolTipText("Select a spectrum from the database to serve as an elemental standard.");
            jButton_AddDatabase.addActionListener(new AddRowFromDatabaseAction());
            btnPanel.add(jButton_AddDatabase, cc.xy(1, 5));

            final JButton remove = new JButton("Remove");
            remove.setToolTipText("Remove the selected spectrum or spectra from the list of elemental standards.");
            remove.addActionListener(new RemoveRowAction());
            btnPanel.add(remove, cc.xy(1, 7));

            final JButton clear = new JButton("Clear");
            clear.setToolTipText("Clear all spectra from the standards list.");
            clear.addActionListener(new ClearPanelAction());
            btnPanel.add(clear, cc.xy(1, 9));

            final JButton edit = new JButton("Properties");
            edit.setToolTipText("Edit the spectrum properties.");
            edit.addActionListener(new EditSpectrumPropertiesAction());
            btnPanel.add(edit, cc.xy(1, 11));

            add(btnPanel, cc.xy(3, 1));
         }
      }
   }

   protected void initialize() throws Exception {
      setActivePanel(jWizardPanel_Intro.get());
      pack();
      {
         mDefaultBeamEnergy = ToSI.keV(20.0);
         if (mInputSpectra.size() > 0)
            mDefaultBeamEnergy = ToSI.eV(SpectrumUtils.getBeamEnergy(mInputSpectra.get(0)));
      }
   }

   /**
    * Returns a list of residual and simulated spectra.
    *
    * @return List&lt;ISpectrumData&gt;
    */
   public List<ISpectrumData> getResultSpectra() {
      return Collections.unmodifiableList(mResultSpectra);
   }

   /**
    * Get the results of the quantitative analysis in HTML form suitable for
    * presenting in the report.
    *
    * @return String containing HTML
    */
   public String getResultHTML() {
      return mPath.toHTML();
   }

   public void setSession(Session ses) {
      mSession = ses;
   }

   public Session getSession() {
      return mSession;
   }

   /**
    * Check the Duane-Hunt against the beam energy. Return false if the
    * Duane-Hunt differs substantially from the beam energy.
    *
    * @param dh
    *           in keV
    * @param e0
    *           in keV
    * @return True if the Duane-Hunt looks ok...
    */
   public boolean duaneHuntThreshold(double dh, double e0) {
      if (Double.isNaN(dh) || Double.isNaN(e0))
         return false;
      final double delta = e0 - dh;
      return delta < Math.min(0.25, 0.02 * e0);
   }

   private ISpectrumData[] selectSpectra(boolean multi) {
      final SpectrumFileChooser sfc = new SpectrumFileChooser(this, "Open spectra");
      sfc.setMultiSelectionEnabled(multi);
      final File dir = new File(mUserPref.get(REFERENCE_DIR, System.getProperty("user.home")));
      sfc.getFileChooser().setCurrentDirectory(dir);
      centerDialog(sfc);
      final int res = sfc.showOpenDialog();
      if (res == JFileChooser.APPROVE_OPTION) {
         mUserPref.put(REFERENCE_DIR, sfc.getFileChooser().getCurrentDirectory().toString());
         return sfc.getSpectra();
      } else
         return new ISpectrumData[0];
   }

   private StandardBundle[] selectStandardBundles(EDSDetector det, boolean multi) {
      final File dir = new File(mUserPref.get(REFERENCE_DIR, System.getProperty("user.home")));
      JFileChooser jfc = new JFileChooser(dir);
      jfc.setMultiSelectionEnabled(multi);
      jfc.addChoosableFileFilter(new SimpleFileFilter(new String[]{"zstd"}, "DTSA-II standard file"));
      jfc.setDialogTitle("Open DTSA-II Standard File");
      jfc.setAcceptAllFileFilterUsed(true);
      final int open = jfc.showOpenDialog(QuantificationWizard.this);
      ArrayList<StandardBundle> res = new ArrayList<>();
      if (open == JFileChooser.APPROVE_OPTION) {
         mUserPref.put(REFERENCE_DIR, jfc.getCurrentDirectory().toString());
         File[] selF = jfc.getSelectedFiles();
         for (File f : selF)
            try {
               res.add(StandardBundle.read(f, det));
            } catch (IOException | EPQException e) {
               ErrorDialog.createErrorMessage(QuantificationWizard.this, "Open DTSA-II Standard File", e);
            }
      }
      return res.toArray(new StandardBundle[res.size()]);
   }

}
