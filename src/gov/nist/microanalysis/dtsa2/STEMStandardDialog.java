package gov.nist.microanalysis.dtsa2;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import gov.nist.microanalysis.EPQDatabase.Session;
import gov.nist.microanalysis.EPQLibrary.Composition;
import gov.nist.microanalysis.EPQLibrary.Element;
import gov.nist.microanalysis.EPQLibrary.FromSI;
import gov.nist.microanalysis.EPQLibrary.ISpectrumData;
import gov.nist.microanalysis.EPQLibrary.Material;
import gov.nist.microanalysis.EPQLibrary.SampleShape;
import gov.nist.microanalysis.EPQLibrary.SampleShape.ThinFilm;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;
import gov.nist.microanalysis.EPQLibrary.SpectrumUtils;
import gov.nist.microanalysis.EPQLibrary.ToSI;
import gov.nist.microanalysis.EPQTools.JElementPanel;
import gov.nist.microanalysis.EPQTools.JMaterialPanel;
import gov.nist.microanalysis.EPQTools.JTextFieldDouble;
import gov.nist.microanalysis.Utility.Math2;

/**
 * <p>
 * A dialog for entering data necessary to use a spectrum as a standard for STEM
 * quant.
 * </p>
 * <p>
 * display(dt2.STEMStandardDialog.edit(MainFrame, Database, s1))
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
public class STEMStandardDialog extends JDialog {

   private static final long      serialVersionUID         = 5438391888923212129L;

   private final JLabel           jLabel_Elements          = new JLabel("Elements");
   private final JLabel           jLabel_Spectrum          = new JLabel("Spectrum");
   private final JLabel           jLabel_Material          = new JLabel("Material");
   private final JLabel           jLabel_Density           = new JLabel("Density");
   private final JLabel           jLabel_Thickness         = new JLabel("Thickness");
   private final JLabel           jLabel_MassThickness     = new JLabel("Mass Thickness");
   private final JLabel           jLabel_Current           = new JLabel("Probe current");
   private final JLabel           jLabel_LiveTime          = new JLabel("Live Time");
   private final JLabel           jLabel_Dose              = new JLabel("Dose");

   private final JElementPanel    jPanel_Elements          = new JElementPanel();
   private final JTextField       jTextField_Spectrum      = new JTextField();
   private final JMaterialPanel   jPanel_Material;
   private final JTextFieldDouble jTextField_Density       = new JTextFieldDouble(1.0, 0.01, 25.0, "0.0");
   private final JTextFieldDouble jTextField_Thickness     = new JTextFieldDouble(100.0, 1.0, 1.0e6, "#,##0");
   private final JTextField       jTextField_MassThickness = new JTextField();
   private final JTextFieldDouble jTextField_Current       = new JTextFieldDouble(1.0, 1.0, 1.0e5, "0.00");
   private final JTextFieldDouble jTextField_LiveTime      = new JTextFieldDouble(60.0, 0.01, 36000.0, "0.0");
   private final JTextField       jTextField_Dose          = new JTextField();

   private final Session          mSession;
   private ISpectrumData          mSpectrum;
   private final Set<Element>     mAlreadyHaveStandards    = new TreeSet<Element>();

   private String buildRows(int n) {
      String SPACER = ", 2dlu, ";
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < n; ++i) {
         sb.append("p");
         if (i < n - 1)
            sb.append(SPACER);
      }
      return sb.toString();
   }

   private class OkAction extends AbstractAction {

      private static final long serialVersionUID = -8580263031467365296L;

      OkAction() {
         super("Ok");
      }

      /**
       * @param e
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.
       *      ActionEvent)
       */
      @Override
      public void actionPerformed(ActionEvent e) {
         final SpectrumProperties props = mSpectrum.getProperties();
         Material mat = jPanel_Material.getMaterial();
         props.setCompositionProperty(SpectrumProperties.StandardComposition, mat);
         props.setStandardizedElements(jPanel_Elements.getSelected());
         // Live time in seconds
         props.setNumericProperty(SpectrumProperties.LiveTime, jTextField_LiveTime.getValue());
         // Current in pA
         props.setNumericProperty(SpectrumProperties.FaradayBegin, jTextField_Current.getValue() / 1000.0);
         props.setNumericProperty(SpectrumProperties.FaradayEnd, jTextField_Current.getValue() / 1000.0);
         // Density in g/cm3
         mat.setDensity(ToSI.gPerCC(jTextField_Density.getValue()));
         // Thickness in meters
         final double th = jTextField_Thickness.getValue() * 1.0e-9;
         props.setSampleShape(SpectrumProperties.SampleShape, new SampleShape.ThinFilm(Math2.MINUS_Z_AXIS, th));
         props.setNumericProperty(SpectrumProperties.MassThickness,
               FromSI.cm(th) * FromSI.gPerCC(mat.getDensity()) * 1.0e6); // &mu;g/cm<sup>2</sup>
         STEMStandardDialog.this.setVisible(false);
      }
   }

   private class CancelAction extends AbstractAction {

      private static final long serialVersionUID = -5946577585577435851L;

      CancelAction() {
         super("Cancel");
      }

      /**
       * @param e
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.
       *      ActionEvent)
       */
      @Override
      public void actionPerformed(ActionEvent e) {
         STEMStandardDialog.this.setVisible(false);
      }
   }

   private class MaterialAction extends AbstractAction {

      private static final long serialVersionUID = 2853116608014938272L;

      /**
       * @param e
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.
       *      ActionEvent)
       */
      @Override
      public void actionPerformed(ActionEvent e) {
         jTextField_Density.setValue(FromSI.gPerCC(jPanel_Material.getMaterial().getDensity()));
         updateMassThickness();
      }
   }

   private class MassThicknessAction extends AbstractAction {

      private static final long serialVersionUID = -8615675872186730054L;

      /**
       * @param e
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.
       *      ActionEvent)
       */
      @Override
      public void actionPerformed(ActionEvent e) {
         jPanel_Material.getMaterial().setDensity(ToSI.gPerCC(jTextField_Density.getValue()));
         updateMassThickness();
      }
   }

   private class DoseAction extends AbstractAction {

      private static final long serialVersionUID = -807531937036940641L;

      /**
       * @param e
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.
       *      ActionEvent)
       */
      @Override
      public void actionPerformed(ActionEvent e) {
         updateDose();
      }
   }

   private JPanel getPanel() {
      final FormLayout layout = new FormLayout("right:pref, 5dlu, 40dlu, 5dlu, 80dlu, 2dlu, pref", buildRows(9));
      final PanelBuilder pb = new PanelBuilder(layout);
      int row = 1;
      pb.add(jLabel_Spectrum, CC.xy(1, row), jTextField_Spectrum, CC.xyw(3, row, 5));
      row += 2;
      pb.add(jLabel_Material, CC.xy(1, row), jPanel_Material, CC.xyw(3, row, 5));
      jPanel_Material.setToolTipText("What is the composition of the standard material?");
      jPanel_Material.addMaterialChange(new MaterialAction());
      row += 2;
      pb.add(jLabel_Elements, CC.xy(1, row), jPanel_Elements, CC.xyw(3, row, 5));
      jPanel_Elements.setToolTipText("For what elements is this spectrum a standard?");
      row += 2;
      pb.add(jLabel_Density, CC.xy(1, row), jTextField_Density, CC.xy(3, row));
      jTextField_Density.setToolTipText("What is the density of the standard material?");
      jTextField_Density.addValueChange(new MassThicknessAction());
      pb.addLabel("g/cm³", CC.xy(5, row));
      row += 2;
      pb.add(jLabel_Thickness, CC.xy(1, row), jTextField_Thickness, CC.xy(3, row));
      jTextField_Thickness.setToolTipText("What is the thickness of the standard sample?");
      jTextField_Thickness.addValueChange(new MassThicknessAction());
      pb.addLabel("nm", CC.xy(5, row));
      row += 2;
      pb.add(jLabel_MassThickness, CC.xy(1, row), jTextField_MassThickness, CC.xy(3, row));
      jTextField_MassThickness.setEnabled(false);
      pb.addLabel("μg/cm²", CC.xy(5, row));
      row += 2;
      pb.add(jLabel_Current, CC.xy(1, row), jTextField_Current, CC.xy(3, row));
      jTextField_Current.setToolTipText("What was the sample current during acquisition of the spectrum?");
      jTextField_Current.addValueChange(new DoseAction());
      pb.addLabel("pA", CC.xy(5, row));
      row += 2;
      pb.add(jLabel_LiveTime, CC.xy(1, row), jTextField_LiveTime, CC.xy(3, row));
      jTextField_LiveTime.setToolTipText("How long was the spectrum acquisition period (live time, not real time)?");
      jTextField_LiveTime.addValueChange(new DoseAction());
      pb.addLabel("s", CC.xy(5, row));
      row += 2;
      pb.add(jLabel_Dose, CC.xy(1, row), jTextField_Dose, CC.xy(3, row));
      jTextField_Dose.setEnabled(false);
      pb.addLabel("nA·s", CC.xy(5, row));
      return pb.getPanel();
   }

   /**
    * Constructs a STEMStandardDialog
    */
   public STEMStandardDialog(Window win, Session sess) {
      super(win, "STEM Standard Properties", ModalityType.DOCUMENT_MODAL);
      mSession = sess;
      jPanel_Material = new JMaterialPanel(mSession, true, false);
      JPanel panel = getPanel();
      final int WIDTH = 15;
      panel.setBorder(BorderFactory.createEmptyBorder(WIDTH, WIDTH, WIDTH, WIDTH));
      add(panel);
      ButtonBarBuilder bbb = new ButtonBarBuilder();
      bbb.addGlue();
      final JButton jButton_Ok = new JButton(new OkAction());
      bbb.addButton(jButton_Ok);
      bbb.addRelatedGap();
      bbb.addButton(new JButton(new CancelAction()));
      JPanel buttons = bbb.getPanel();
      buttons.setBorder(BorderFactory.createEmptyBorder(WIDTH, WIDTH, WIDTH / 2, WIDTH));
      add(buttons, BorderLayout.SOUTH);
      JRootPane rootPane = SwingUtilities.getRootPane(jButton_Ok);
      rootPane.setDefaultButton(jButton_Ok);
      pack();
   }

   /**
    * Sets the elements which already have standards supplied so that they won't
    * be permitted. @param elms
    */
   public void setAlreadyHaveStandards(Collection<Element> elms) {
      mAlreadyHaveStandards.clear();
      mAlreadyHaveStandards.addAll(elms);

   }

   public void initialize(ISpectrumData spec, Collection<Element> prevStdized) {
      mSpectrum = spec;
      mAlreadyHaveStandards.clear();
      if (prevStdized != null)
         mAlreadyHaveStandards.addAll(prevStdized);
      if (mSpectrum != null) {
         final SpectrumProperties sp = mSpectrum.getProperties();
         jTextField_Spectrum.setText(mSpectrum.toString());
         {
            Composition comp = sp.getCompositionWithDefault(SpectrumProperties.StandardComposition, null);
            if (comp != null) {
               final Material mat = comp instanceof Material ? (Material) comp : new Material(comp, ToSI.gPerCC(1.0));
               jPanel_Material.setComposition(mat);
               final Set<Element> avail = new TreeSet<Element>(comp.getElementSet());
               avail.removeAll(mAlreadyHaveStandards);
               jPanel_Elements.setAvailableElements(avail);
               final Set<Element> stdized = new TreeSet<Element>(sp.getStandardizedElements());
               stdized.removeAll(mAlreadyHaveStandards);
               if ((stdized.size() == 0) && (avail.size() == 1))
                  stdized.addAll(avail);
               jPanel_Elements.setSelected(stdized);
            }
         }
         final double pc = SpectrumUtils.getAverageFaradayCurrent(sp, 0.0);
         jTextField_Current.setValue(1000.0 * pc);
         final double lt = sp.getNumericWithDefault(SpectrumProperties.LiveTime, 0.0);
         jTextField_LiveTime.setValue(lt);
         updateDose();
         final SampleShape ss = sp.getSampleShapeWithDefault(SpectrumProperties.SampleShape, null);
         double thickness = 100.0;
         if (ss instanceof ThinFilm) {
            ThinFilm tf = (ThinFilm) ss;
            thickness = tf.getThickness() * 1.0e9;
         }
         jTextField_Thickness.setValue(thickness);
         final Material mat = jPanel_Material.getMaterial();
         jTextField_Density
               .setValue((mat != null) && (!mat.equals(Material.Null)) ? FromSI.gPerCC(mat.getDensity()) : 1.0);
         updateMassThickness();
      }
   }

   private void updateDose() {
      final double val = jTextField_LiveTime.getValue() * jTextField_Current.getValue() * 0.001;
      final DecimalFormat df = new DecimalFormat("#,##0.0");
      jTextField_Dose.setText(df.format(val));

   }

   private void updateMassThickness() {
      final double val = 1.0e-7 * jTextField_Thickness.getValue() * jTextField_Density.getValue() * 1.0e6;
      final DecimalFormat df = new DecimalFormat("#,##0.000");
      jTextField_MassThickness.setText(df.format(val));
   }

   public static ISpectrumData edit(Window parent, Session sess, ISpectrumData spec, Collection<Element> prevStdized) {
      ISpectrumData dup = SpectrumUtils.copy(spec);
      STEMStandardDialog ssd = new STEMStandardDialog(parent, sess);
      ssd.initialize(dup, prevStdized);
      ssd.setLocationRelativeTo(parent);
      ssd.setVisible(true);
      return dup;
   }
}
