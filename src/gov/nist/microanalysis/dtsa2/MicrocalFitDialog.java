/**
 * gov.nist.microanalysis.dtsa2.MicrocalFitDialog Created by: nritchie Date: Jun
 * 8, 2009
 */
package gov.nist.microanalysis.dtsa2;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.Element;
import gov.nist.microanalysis.EPQLibrary.ISpectrumData;
import gov.nist.microanalysis.EPQLibrary.MicrocalSpectrumFitter;
import gov.nist.microanalysis.EPQLibrary.RegionOfInterestSet;
import gov.nist.microanalysis.EPQLibrary.SpectrumUtils;
import gov.nist.microanalysis.EPQTools.JWizardDialog;
import gov.nist.microanalysis.Utility.EachRowEditor;
import gov.nist.microanalysis.Utility.HalfUpFormat;
import gov.nist.microanalysis.Utility.ProgressEvent;

/**
 * <p>
 * Description
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
public class MicrocalFitDialog
   extends JWizardDialog {

   private static final long serialVersionUID = -6253117164000896668L;

   private final MicrocalSpectrumFitter mFitter;
   private double[] mFirstEnergyFit;
   private double[] mFinalEnergyFit;

   private static final String[] SETUP_TABLE_COLNAMES = new String[] {
      "E_disp",
      "Width",
      "Element",
      "ROI",
      "Fit E?",
   };
   private static int E_DISP_COL = 0;
   private static int WIDTH_COL = 1;
   private static int ELM_COL = 2;
   private static int ROI_COL = 3;
   private static int FIT_E_COL = 4;

   private class SetupPanel
      extends JWizardPanel {

      private static final long serialVersionUID = 6398697603630673499L;
      private JTable jTable_Setup;
      private JComboBox<Element> jComboBox_Elements;
      private JComboBox<Boolean> jComboBox_FitE;
      private EachRowEditor jEachRowEditor_Lines;
      private JTextField jTextField_Width;
      private boolean mFirst = true;
      private final NumberFormat mParse = NumberFormat.getInstance();

      SetupPanel(JWizardDialog wiz) {
         super(wiz, "Setup", new BorderLayout());
         try {
            initialize();
         }
         catch(final Exception ex) {
            ex.printStackTrace();
         }
      }

      private double getWidthValue(int er) {
         try {
            final String wid = (String) jTable_Setup.getValueAt(er, WIDTH_COL);
            final double width = mParse.parse(wid).doubleValue();
            return width;
         }
         catch(final Exception ex) {
            jTable_Setup.setValueAt("8.0", er, WIDTH_COL);
         }
         return 8.0;
      }

      private void updateElement(int er) {
         if(er >= 0) {
            final Element elmObj = (Element) jComboBox_Elements.getSelectedItem();
            final double width = getWidthValue(er);
            final JComboBox<RegionOfInterestSet.RegionOfInterest> jcb = new JComboBox<>();
            if(elmObj instanceof Element) {
               final Element elm = elmObj;
               final RegionOfInterestSet rois = mFitter.getElementROIS(elm, width);
               for(final RegionOfInterestSet.RegionOfInterest roi : rois)
                  jcb.addItem(roi);
            }
            jEachRowEditor_Lines.setEditorAt(er, new DefaultCellEditor(jcb));
            if(jcb.getItemCount() > 0) {
               final Object sel = jcb.getItemAt(0);
               jcb.setSelectedIndex(0);
               jTable_Setup.setValueAt(sel, er, ROI_COL);
            }
         }
      }

      private void initialize()
            throws Exception {
         jTable_Setup = new JTable() {

            private static final long serialVersionUID = -1267162533892997285L;

            @Override
            public boolean isCellEditable(int row, int col) {
               return (col == ELM_COL) || (col == ROI_COL) || (col == WIDTH_COL) || (col == FIT_E_COL);
            }

         };
         final DefaultTableModel tm = new DefaultTableModel(SETUP_TABLE_COLNAMES, 1);
         jTable_Setup.setModel(tm);
         jTable_Setup.setForeground(SystemColor.textText);
         add(new JScrollPane(jTable_Setup), BorderLayout.CENTER);

         jComboBox_Elements = new JComboBox<>();
         jComboBox_Elements.addItem(Element.None);
         for(int z = Element.elmBe; z < Element.elmFm; ++z)
            jComboBox_Elements.addItem(Element.byAtomicNumber(z));
         jComboBox_Elements.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               final int er = jTable_Setup.getSelectedRow();
               updateElement(er);
            }
         });

         jTextField_Width = new JTextField();
         jTextField_Width.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent arg0) {
               final int er = jTable_Setup.getSelectedRow();
               updateElement(er);
            }

         });

         jEachRowEditor_Lines = new EachRowEditor(jTable_Setup);
         jEachRowEditor_Lines.setDefaultEditor(new JComboBox<RegionOfInterestSet.RegionOfInterest>());

         jComboBox_FitE = new JComboBox<>();
         jComboBox_FitE.addItem(Boolean.TRUE);
         jComboBox_FitE.addItem(Boolean.FALSE);
         jComboBox_FitE.setSelectedIndex(0);
      }

      @Override
      public void onShow() {
         if(mFirst) {
            final List<Double> peakPos = mFitter.getPeakPositions();
            final DefaultTableModel tm = new DefaultTableModel(SETUP_TABLE_COLNAMES, peakPos.size());
            final NumberFormat df = new HalfUpFormat("0.0");
            {
               int row = 0;
               for(final Double pp : peakPos) {
                  final double e = SpectrumUtils.minEnergyForChannel(mFitter.getSpectrum(), pp.intValue());
                  tm.setValueAt(df.format(e), row, E_DISP_COL);
                  tm.setValueAt("8.0", row, WIDTH_COL);
                  tm.setValueAt(Boolean.TRUE, row, FIT_E_COL);
                  ++row;
               }
            }
            jTable_Setup.setModel(tm);
            mFirst = false;
         }
         mFirstEnergyFit = null;
         final TableColumnModel columnModel = jTable_Setup.getColumnModel();
         columnModel.getColumn(ELM_COL).setCellEditor(new DefaultCellEditor(jComboBox_Elements));
         columnModel.getColumn(WIDTH_COL).setCellEditor(new DefaultCellEditor(jTextField_Width));
         columnModel.getColumn(ROI_COL).setCellEditor(jEachRowEditor_Lines);
         columnModel.getColumn(FIT_E_COL).setCellEditor(new DefaultCellEditor(jComboBox_FitE));
         getWizard().setNextPanel(jWizardPanel_FirstEnergyFit);
         getWizard().setBackEnabled(false);
         getWizard().enableFinish(false);
      }

      @Override
      public boolean permitNext() {
         boolean res = false;
         try {
            int cx = 0;
            mFitter.clearLines();
            final NumberFormat nf = NumberFormat.getInstance();
            for(int r = 0; r < jTable_Setup.getRowCount(); ++r) {
               final double eDisp = nf.parse((String) jTable_Setup.getValueAt(r, E_DISP_COL)).doubleValue();
               final double channel = SpectrumUtils.channelForEnergy(mFitter.getSpectrum(), eDisp);
               final Object roiObj = jTable_Setup.getValueAt(r, ROI_COL);
               final Boolean fitE = (Boolean) jTable_Setup.getValueAt(r, FIT_E_COL);
               final double width = nf.parse((String) jTable_Setup.getValueAt(r, WIDTH_COL)).doubleValue();
               if(roiObj instanceof RegionOfInterestSet.RegionOfInterest) {
                  ++cx;
                  mFitter.addLine(channel, (RegionOfInterestSet.RegionOfInterest) roiObj, fitE.booleanValue(), width);
               }
            }
            res = (cx >= 3);
            try {
               mFirstEnergyFit = mFitter.performEnergyFit0();
            }
            catch(final EPQException ex) {
               getWizard().setExceptionText("Error fitting energy", ex);
               res = false;
            }
         }
         catch(final ParseException e) {
            // Ok to ignore
         }
         return res;
      }

   }

   private class EnergyFitPanel
      extends JWizardPanel {

      private static final long serialVersionUID = -2622439709539215031L;

      protected JTextField jTextField_Offset = new JTextField();
      protected JTextField jTextField_Gain = new JTextField();
      protected JTextField jTextField_Quadratic = new JTextField();

      private void initialize() {
         final CellConstraints cc0 = new CellConstraints();
         final CellConstraints cc1 = new CellConstraints();
         final PanelBuilder pb = new PanelBuilder(new FormLayout("right:pref, 10dlu, 50dlu, 2dlu, left:pref", "pref, 5dlu, pref, 5dlu, pref"));
         pb.addLabel("Offset", cc0.xy(1, 1), jTextField_Offset, cc1.xy(3, 1));
         pb.addLabel("eV", cc0.xy(5, 1));
         pb.addLabel("Gain", cc0.xy(1, 3), jTextField_Gain, cc1.xy(3, 3));
         pb.addLabel("eV/ch", cc0.xy(5, 3));
         pb.addLabel("Quadratic", cc0.xy(1, 5), jTextField_Quadratic, cc1.xy(3, 5));
         pb.addLabel("<html>\u00D7 10<sup>-6</sup> eV/ch<sup>2</sup>", cc0.xy(5, 5));
         jTextField_Offset.setEditable(false);
         jTextField_Gain.setEditable(false);
         jTextField_Quadratic.setEditable(false);
         add(pb.getPanel());
      }

      public EnergyFitPanel(JWizardDialog wiz, String banner) {
         super(wiz, banner);
         try {
            initialize();
         }
         catch(final Exception ex) {
            ex.printStackTrace();
         }

      }

      @Override
      public boolean permitNext() {
         return true;
      }
   }

   private class FirstEnergyFitPanel
      extends EnergyFitPanel {

      private static final long serialVersionUID = -8973574065448312104L;

      private FirstEnergyFitPanel(JWizardDialog wiz) {
         super(wiz, "Preliminary Energy Fit");
      }

      @Override
      public void onShow() {
         if(mFirstEnergyFit != null) {
            {
               final NumberFormat df = new HalfUpFormat("0.0");
               jTextField_Offset.setText(df.format(mFirstEnergyFit[0]));
               jTextField_Quadratic.setText(df.format(mFirstEnergyFit[2] * 1.0e6));
            }
            {
               final NumberFormat df = new HalfUpFormat("0.0000");
               jTextField_Gain.setText(df.format(mFirstEnergyFit[1]));
            }
         } else {
            jTextField_Offset.setText("?");
            jTextField_Offset.setEditable(true);
            jTextField_Gain.setText("?");
            jTextField_Gain.setEditable(true);
            jTextField_Quadratic.setText("?");
            jTextField_Quadratic.setEditable(true);
         }
         getWizard().setNextPanel(jWizardPanel_Progress);
         getWizard().setBackEnabled(true);
      }

      @Override
      public boolean permitNext() {
         if(jTextField_Offset.isEditable())
            try {
               final NumberFormat nf = NumberFormat.getInstance();
               final double off = nf.parse(jTextField_Offset.getText()).doubleValue();
               final double gain = nf.parse(jTextField_Gain.getText()).doubleValue();
               final double quad = nf.parse(jTextField_Quadratic.getText()).doubleValue();
               final double[] coeff = new double[] {
                  off,
                  gain,
                  quad
               };
               mFitter.setInitialEneryCalibration(coeff);
            }
            catch(final ParseException e) {
               getWizard().setExceptionText("Error parsing calibration", e);
               return false;
            }

         return true;
      }
   }

   private class FinalEnergyFitPanel
      extends EnergyFitPanel {

      private static final long serialVersionUID = -7309847525835541843L;

      private FinalEnergyFitPanel(JWizardDialog wiz) {
         super(wiz, "Final Energy Fit");
      }

      @Override
      public void onShow() {
         if(mFinalEnergyFit != null) {
            {
               final NumberFormat df = new HalfUpFormat("0.0");
               jTextField_Offset.setText(df.format(mFinalEnergyFit[0]));
            }
            {
               final NumberFormat df = new HalfUpFormat("0.0000");
               jTextField_Gain.setText(df.format(mFinalEnergyFit[1]));
            }
            {
               final NumberFormat df = new HalfUpFormat("0.0");
               jTextField_Quadratic.setText(df.format(mFinalEnergyFit[2] * 1.0e6));
            }
         } else {
            jTextField_Offset.setText("?");
            jTextField_Gain.setText("?");
            jTextField_Quadratic.setText("?");
         }
         getWizard().setNextPanel(null);
         getWizard().enableFinish(true);
         getWizard().setBackEnabled(true);
      }

   }

   private class ProgressPanel
      extends JWizardDialog.JProgressPanel {

      private static final long serialVersionUID = -6336154109886338830L;

      public ProgressPanel(JWizardDialog wiz) {
         super(wiz, "Fit Progress");
      }

      @Override
      public void onShow() {
         getWizard().setNextPanel(jWizardPanel_FinalEnergyFit);
         getWizard().setBackEnabled(false);
         getWizard().enableFinish(false);
         getWizard().enableNext(false);
         final SwingWorker<Object, Integer> sw = new SwingWorker<>() {
            
            @Override
            public void process(List<Integer> vals) {
               int progress = 0;
               for(final Integer val : vals)
                  if(val.intValue() > progress)
                     progress = val.intValue();
               ProgressPanel.this.setProgress(progress);
            }

            @Override
            protected Object doInBackground()
                  throws Exception {
               final ActionListener al = new ActionListener() {
                  @Override
                  public void actionPerformed(ActionEvent e) {
                     final ProgressEvent pe = (ProgressEvent) e;
                     int progress = pe.getProgress();
                     publish(Integer.valueOf(progress));
                     if(progress == 100)
                        getWizard().setNextPanel(jWizardPanel_FinalEnergyFit);

                  }
               };
               mFitter.addProgressListener(al);
               try {
                  mFinalEnergyFit = null;
                  mFitter.compute();
                  mFinalEnergyFit = mFitter.performEnergyFit1();
               }
               catch(final Exception e1) {
                  getWizard().setExceptionText("Error during peak fit", e1);
               }
               mFitter.removeProgressListener(al);
               getWizard().setBackEnabled(true);
               getWizard().enableFinish(false);
               getWizard().enableNext(true);
               return null;
            }
         };
         sw.execute();
      }
   }

   private final SetupPanel jWizardPanel_Setup = new SetupPanel(this);
   private final FirstEnergyFitPanel jWizardPanel_FirstEnergyFit = new FirstEnergyFitPanel(this);
   private final ProgressPanel jWizardPanel_Progress = new ProgressPanel(this);
   private final FinalEnergyFitPanel jWizardPanel_FinalEnergyFit = new FinalEnergyFitPanel(this);

   public MicrocalFitDialog(Frame frame, ISpectrumData spec) {
      super(frame, "Microcalorimeter Spectrum Fitter");
      mFitter = new MicrocalSpectrumFitter(spec);
      this.setActivePanel(jWizardPanel_Setup);
      this.setModal(true);
   }

   public MicrocalSpectrumFitter getFitter() {
      return mFitter;
   }

}
