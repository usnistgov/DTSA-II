/**
 * gov.nist.microanalysis.dtsa2.SpectrumFitterWizard Created by: nicholas Date:
 * Jun 13, 2008
 */
package gov.nist.microanalysis.dtsa2;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import gov.nist.microanalysis.EPQLibrary.Element;
import gov.nist.microanalysis.EPQLibrary.Detector.DetectorCalibration;
import gov.nist.microanalysis.EPQLibrary.Detector.DetectorProperties;
import gov.nist.microanalysis.EPQLibrary.Detector.ElectronProbe;
import gov.nist.microanalysis.EPQTools.JPeriodicTable;
import gov.nist.microanalysis.EPQTools.JWizardDialog;

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
 * @author nicholas
 * @version 1.0
 */
public class SpectrumFitterWizard
   extends JWizardDialog {

   private static final long serialVersionUID = -8154385198977846444L;

   private final JWizardPanel jWizardPanel_Intro = new IntroPanel(this);
   private final JWizardPanel jWizardPanel_Element = new ElementPanel(this);
   private final JWizardPanel jWizardPanel_Options = new OptionsPanel(this);

   public SpectrumFitterWizard(Frame parent) {
      super(parent, "Spectrum Fitter", true);
      try {
         initialize();
      }
      catch(final Exception ex) {
         ex.printStackTrace();
      }
   }

   private void initialize() {
      setActivePanel(jWizardPanel_Intro, "Non-linear spectrum fitter");
      setNextPanel(jWizardPanel_Element, "Select elements to fit");
      pack();
      enableFinish(false);
   }

   class IntroPanel
      extends JWizardPanel {
      private static final long serialVersionUID = 8148357270640139179L;
      private JTextField jTextField_Spectrum;
      private JTextField jTextField_BeamEnergy;
      private JComboBox<ElectronProbe> jComboBox_Instrument;
      private JComboBox<DetectorProperties> jComboBox_Detector;
      private JComboBox<DetectorCalibration> jComboBox_Calibration;
      private JTextField jTextField_Composition;
      private JButton jButton_Composition;

      private IntroPanel(JWizardDialog parent) {
         super(parent);
         initialize();
      }

      private void initialize() {
         final PanelBuilder pb = new PanelBuilder(new FormLayout("right:pref, 3dlu, 50dlu, 3dlu, 130dlu, 3dlu, pref", "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"), this);
         final CellConstraints cc = new CellConstraints();
         pb.addLabel("Spectrum", cc.xy(1, 1));
         jTextField_Spectrum = new JTextField();
         jTextField_Spectrum.setEditable(false);
         pb.add(jTextField_Spectrum, cc.xyw(3, 1, 5));
         pb.addLabel("Beam energy", cc.xy(1, 3));
         jTextField_BeamEnergy = new JTextField();
         jTextField_BeamEnergy.setEditable(false);
         pb.add(jTextField_BeamEnergy, cc.xy(3, 3));
         pb.addLabel("keV", cc.xy(5, 3));

         pb.addLabel("Instrument", cc.xy(1, 5));
         jComboBox_Instrument = new JComboBox<ElectronProbe>();
         pb.add(jComboBox_Instrument, cc.xyw(3, 5, 5));
         pb.addLabel("Detector", cc.xy(1, 7));
         jComboBox_Detector = new JComboBox<DetectorProperties>();
         pb.add(jComboBox_Detector, cc.xyw(3, 7, 5));
         pb.addLabel("Calibration", cc.xy(1, 9));
         jComboBox_Calibration = new JComboBox<DetectorCalibration>();
         pb.add(jComboBox_Calibration, cc.xyw(3, 9, 5));

         pb.addLabel("Material composition", cc.xy(1, 11));
         jTextField_Composition = new JTextField();
         jTextField_Composition.setEditable(false);
         pb.add(jTextField_Composition, cc.xyw(3, 11, 3));
         jButton_Composition = new JButton("Edit");
         pb.add(jButton_Composition, cc.xy(7, 11));
      }

      @Override
      public void onShow() {
         setMessageText("You are about to fit this spectrum against theoretical line shapes.");
      }

   }

   class ElementPanel
      extends JWizardPanel {
      private static final long serialVersionUID = 1942138017681022623L;

      public JPeriodicTable jPeriodicTable_Elements;

      public ElementPanel(JWizardDialog parent) {
         super(parent);
         jPeriodicTable_Elements = new JPeriodicTable();
         for(int z = 1; z < 4; ++z)
            jPeriodicTable_Elements.setEnabled(Element.byAtomicNumber(z), false);
         for(int z = Element.elmPu + 1; z < Element.elmEndOfElements; ++z)
            jPeriodicTable_Elements.setEnabled(Element.byAtomicNumber(z), false);
         jPeriodicTable_Elements.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
               if(jPeriodicTable_Elements.getSelected().size() > 0)
                  setNextPanel(jWizardPanel_Options, "Fit options");
               else
                  setNextPanel(null, "Fit options");
            }

         });
         add(jPeriodicTable_Elements);
      }

      @Override
      public void onShow() {
         if(jPeriodicTable_Elements.getSelected().size() > 0)
            setNextPanel(jWizardPanel_Options, "Fit options");
         else
            setNextPanel(null, "Fit options");
         setMessageText("Specify all the elements represented in this spectrum.");
         enableFinish(false);
      }

      @Override
      public boolean permitNext() {
         return jPeriodicTable_Elements.getSelected().size() > 0;
      }
   }

   public class OptionsPanel
      extends JWizardPanel {

      private static final long serialVersionUID = 1903594360734299689L;

      private JRadioButton jRadioButton_FitFano;
      private JRadioButton jRadioButton_DefaultFano;
      private JTextField jTextField_Fano;
      private JRadioButton jRadioButton_FitNoise;
      private JRadioButton jRadioButton_DefaultNoise;
      private JTextField jTextField_Noise;
      private JRadioButton jRadioButton_FitZero;
      private JRadioButton jRadioButton_DefaultZero;
      private JTextField jTextField_Zero;
      private JRadioButton jRadioButton_FitGain;
      private JRadioButton jRadioButton_DefaultGain;
      private JTextField jTextField_Gain;
      private JRadioButton jRadioButton_FitLineWeights;
      private JRadioButton jRadioButton_DefaultLineWeights;

      OptionsPanel(JWizardDialog parent) {
         super(parent);
         initialize();
      }

      private void initialize() {
         final PanelBuilder pb = new PanelBuilder(new FormLayout("right:pref, 10dlu, pref, 10dlu, pref, 3dlu, 20dlu, 3dlu, pref", "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"), this);
         final CellConstraints cc = new CellConstraints();
         pb.addSeparator("Detector resolution and calibration", cc.xyw(1, 1, 9));
         {
            final int line = 3;
            final ButtonGroup bg = new ButtonGroup();
            pb.addLabel("Fano factor", cc.xy(1, line));
            jRadioButton_FitFano = new JRadioButton("Fit");
            pb.add(jRadioButton_FitFano, cc.xy(3, line));
            jRadioButton_DefaultFano = new JRadioButton("Default from detector - ");
            pb.add(jRadioButton_DefaultFano, cc.xy(5, line));
            bg.add(jRadioButton_FitFano);
            bg.add(jRadioButton_DefaultFano);
            jTextField_Fano = new JTextField();
            jTextField_Fano.setEditable(false);
            pb.add(jTextField_Fano, cc.xy(7, line));
            pb.addLabel("", cc.xy(9, line));
         }
         {
            final int line = 5;
            final ButtonGroup bg = new ButtonGroup();
            pb.addLabel("Dectector noise factor", cc.xy(1, line));
            jRadioButton_FitNoise = new JRadioButton("Fit");
            pb.add(jRadioButton_FitNoise, cc.xy(3, line));
            jRadioButton_DefaultNoise = new JRadioButton("Default from detector - ");
            pb.add(jRadioButton_DefaultNoise, cc.xy(5, line));
            bg.add(jRadioButton_FitNoise);
            bg.add(jRadioButton_DefaultNoise);
            jTextField_Noise = new JTextField();
            jTextField_Noise.setEditable(false);
            pb.add(jTextField_Noise, cc.xy(7, line));
            pb.addLabel("eV", cc.xy(9, line));
         }
         {
            final int line = 7;
            final ButtonGroup bg = new ButtonGroup();
            pb.addLabel("Zero offset", cc.xy(1, line));
            jRadioButton_FitZero = new JRadioButton("Fit");
            pb.add(jRadioButton_FitZero, cc.xy(3, line));
            jRadioButton_DefaultZero = new JRadioButton("Default from detector - ");
            pb.add(jRadioButton_DefaultZero, cc.xy(5, line));
            bg.add(jRadioButton_FitZero);
            bg.add(jRadioButton_DefaultZero);
            jTextField_Zero = new JTextField();
            jTextField_Zero.setEditable(false);
            pb.add(jTextField_Zero, cc.xy(7, line));
            pb.addLabel("eV", cc.xy(9, line));
         }
         {
            final int line = 9;
            final ButtonGroup bg = new ButtonGroup();
            pb.addLabel("Gain", cc.xy(1, line));
            jRadioButton_FitGain = new JRadioButton("Fit");
            pb.add(jRadioButton_FitGain, cc.xy(3, line));
            jRadioButton_DefaultGain = new JRadioButton("Default from detector - ");
            pb.add(jRadioButton_DefaultGain, cc.xy(5, line));
            bg.add(jRadioButton_FitGain);
            bg.add(jRadioButton_DefaultGain);
            jTextField_Gain = new JTextField();
            pb.add(jTextField_Gain, cc.xy(7, line));
            jTextField_Gain.setEditable(false);
            pb.addLabel("eV/channel", cc.xy(9, line));
         }
         pb.addSeparator("Spectrum peak shapes", cc.xyw(1, 11, 9));
         {
            final int line = 13;
            final ButtonGroup bg = new ButtonGroup();
            pb.addLabel("X-ray line weights", cc.xy(1, line));
            jRadioButton_FitLineWeights = new JRadioButton("Fit");
            pb.add(jRadioButton_FitLineWeights, cc.xy(3, line));
            jRadioButton_DefaultLineWeights = new JRadioButton("Defaults from table");
            pb.add(jRadioButton_DefaultLineWeights, cc.xy(5, line));
            bg.add(jRadioButton_FitLineWeights);
            bg.add(jRadioButton_DefaultLineWeights);
         }
         initializeDefaults();
      }

      private void initializeDefaults() {
         jRadioButton_FitFano.setSelected(true);
         jRadioButton_FitNoise.setSelected(true);
         jRadioButton_FitZero.setSelected(true);
         jRadioButton_FitGain.setSelected(true);
         jRadioButton_FitLineWeights.setSelected(true);

      }

      @Override
      public void onShow() {
         enableFinish(true);
      }

   };

   public static void main(String[] args) {
      try {
         // Set these regardless (no harm in it)
         System.setProperty("apple.laf.useScreenMenuBar", "true");
         System.setProperty("apple.laf.smallTabs", "true");
         // Set up special look-and-feels
         if(true) {
            final String laf = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(laf);
         } else
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
      }
      catch(final Exception e) {
         try {
            e.printStackTrace();
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         }
         catch(final Exception e1) {
            e1.printStackTrace();
         }
      }
      JFrame.setDefaultLookAndFeelDecorated(false);
      final JWizardDialog wd = new SpectrumFitterWizard(null);
      wd.setVisible(true);
      System.exit(0);
   }

}
