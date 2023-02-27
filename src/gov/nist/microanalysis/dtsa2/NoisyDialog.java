package gov.nist.microanalysis.dtsa2;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import gov.nist.microanalysis.Utility.HalfUpFormat;

/**
 * <p>
 * Title: Trixy
 * </p>
 * <p>
 * Description: Tools for Microanalysis
 * </p>
 * <p>
 * Copyright: Not subject to copyright - 2004
 * </p>
 * <p>
 * Company: National Institute of Standards and Technology
 * </p>
 *
 * @author Nicholas W. M. Ritchie
 * @version 1.0
 */
public class NoisyDialog
   extends JDialog {
   static public final long serialVersionUID = 0x1;

   JPanel jPanel_Main = new JPanel();
   JPanel jPanel_Slider = new JPanel();
   JPanel jPanel_Buttons;
   JLabel jLabel_Scale = new JLabel();
   JSlider jSlider_Scale = new JSlider();
   JLabel jLabel_Position = new JLabel();
   JPanel jPanel_Content = new JPanel();
   JLabel jLabel_Seed = new JLabel();
   JButton jButton_Ok = new JButton();
   JButton jButton_Cancel = new JButton();
   JSpinner jSpinner_Seed = new JSpinner();
   JLabel jLabel_Duplicates = new JLabel();
   JSpinner jSpinner_Duplicates = new JSpinner();

   private boolean mOk = false;

   public NoisyDialog(Frame frame, String string, boolean _boolean) {
      super(frame, string, _boolean);
      try {
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         initialize();
         pack();
      }
      catch(final Exception exception) {
         exception.printStackTrace();
      }
   }

   public NoisyDialog() {
      this(new Frame(), "NoisyDialog", false);
   }

   private double getSliderValue() {
      final int s = jSlider_Scale.getValue();
      return s == 0 ? 0.001 : 0.01 * s;
   }

   private void initialize()
         throws Exception {

      final CellConstraints cc = new CellConstraints();

      jPanel_Main.setLayout(new FormLayout("right:pref, 3dlu, 30dlu, 80dlu", "pref, 4dlu, pref, 4dlu, pref, 4dlu, pref, 8dlu, pref"));
      jPanel_Main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      jLabel_Scale.setDisplayedMnemonic('S');
      jLabel_Scale.setLabelFor(jSlider_Scale);
      jLabel_Scale.setText("Scale");
      jPanel_Main.add(jLabel_Scale, cc.xy(1, 1));

      jSlider_Scale.setPreferredSize(new Dimension(200, 40));
      jSlider_Scale.setMinimum(0);
      jSlider_Scale.setMaximum(100);
      jSlider_Scale.setMajorTickSpacing(20);
      jSlider_Scale.setPaintLabels(true);
      jSlider_Scale.setPaintTicks(true);
      jPanel_Main.add(jSlider_Scale, cc.xyw(3, 1, 2));
      jSlider_Scale.addChangeListener(new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent e) {
            final NumberFormat nf = new HalfUpFormat("0.0%");
            jLabel_Position.setText(nf.format(getSliderValue()));
         }
      });
      final NumberFormat nf = new HalfUpFormat("0.0%");
      jLabel_Position.setText(nf.format(getSliderValue()));

      jPanel_Main.add(jLabel_Position, cc.xyw(3, 3, 2, CellConstraints.CENTER, CellConstraints.CENTER));

      jLabel_Seed.setDisplayedMnemonic('R');
      jLabel_Seed.setText("Random Seed");
      jLabel_Seed.setLabelFor(jSpinner_Seed);
      jLabel_Seed.setHorizontalAlignment(SwingConstants.RIGHT);
      jPanel_Main.add(jLabel_Seed, cc.xy(1, 5));

      jSpinner_Seed.setModel(new SpinnerNumberModel(Long.valueOf((long) (1000 * Math.random())), Long.valueOf(0), Long.valueOf(1000), Long.valueOf(1)));
      jPanel_Main.add(jSpinner_Seed, cc.xy(3, 5));

      jLabel_Duplicates.setDisplayedMnemonic('D');
      jLabel_Duplicates.setText("Duplicates");
      jLabel_Duplicates.setLabelFor(jSpinner_Duplicates);
      jLabel_Duplicates.setHorizontalAlignment(SwingConstants.RIGHT);
      jPanel_Main.add(jLabel_Duplicates, cc.xy(1, 7));

      jSpinner_Duplicates.setModel(new SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(1000), Integer.valueOf(1)));
      jPanel_Main.add(jSpinner_Duplicates, cc.xy(3, 7));

      jButton_Ok.setText("Ok");
      jButton_Ok.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent actionEvent) {
            jButton_Ok_actionPerformed(actionEvent);
         }
      });
      getRootPane().setDefaultButton(jButton_Ok);

      jButton_Cancel.setText("Cancel");
      jButton_Cancel.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent actionEvent) {
            jButton_Cancel_actionPerformed(actionEvent);
         }
      });
      addCancelByEscapeKey();
      final ButtonBarBuilder bbb = new ButtonBarBuilder();
      bbb.addGlue();
      bbb.addButton(jButton_Ok, jButton_Cancel);
      jPanel_Buttons = bbb.build();
      jPanel_Main.add(jPanel_Buttons, cc.xyw(1, 9, 4));

      getContentPane().add(jPanel_Main);
      setResizable(false);
   }

   private void addCancelByEscapeKey() {
      final String CANCEL_ACTION_KEY = "CANCEL_ACTION_KEY";
      final int noModifiers = 0;
      final KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, noModifiers, false);
      final InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      inputMap.put(escapeKey, CANCEL_ACTION_KEY);
      final AbstractAction cancelAction = new AbstractAction() {
         private static final long serialVersionUID = -8169141856705469081L;

         @Override
         public void actionPerformed(ActionEvent e) {
            jButton_Cancel_actionPerformed(null);
         }
      };
      getRootPane().getActionMap().put(CANCEL_ACTION_KEY, cancelAction);
   }

   public void jButton_Ok_actionPerformed(ActionEvent actionEvent) {
      mOk = true;
      this.setVisible(false);
   }

   public void jButton_Cancel_actionPerformed(ActionEvent actionEvent) {
      mOk = false;
      this.setVisible(false);
   }

   public boolean getOk() {
      return mOk;
   }

   public long getSeed() {
      return ((Long) jSpinner_Seed.getValue()).longValue();
   }

   public double getScale() {
      return getSliderValue();

   }

   public int getDuplicates() {
      return ((Integer) jSpinner_Duplicates.getValue()).intValue();
   }
}
