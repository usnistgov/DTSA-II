/**
 * <p>
 * Title: gov.nist.microanalysis.Trixy.RescaleDialog.java
 * </p>
 * <p>
 * Description:
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
package gov.nist.microanalysis.dtsa2;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Displays a dialog suitable for entering a third-order polynominal such as
 * might be useful for LinearizeSpectrum.
 */
public class RescaleDialog
   extends JDialog {
   private static final long serialVersionUID = 0x42;

   JPanel jPanel_Main = new JPanel();
   JPanel jPanel_Buttons = new JPanel();
   JPanel jPanel_Polynomial = new JPanel();
   JButton jButton_Ok = new JButton();
   JButton jButton_Cancel = new JButton();
   JTextField jTextField_Zero = new JTextField();
   JLabel jLabel_Zero = new JLabel();
   JTextField jTextField_One = new JTextField();
   JLabel jLabel_One = new JLabel();
   JTextField jTextField_Two = new JTextField();
   JLabel jLabel_Two = new JLabel();
   JTextField jTextField_Three = new JTextField();
   JLabel jLabel_Three = new JLabel();
   JLabel jLabel_Intro = new JLabel();
   JLabel jLabel_Width = new JLabel();
   JTextField jTextField_Width = new JTextField();
   JLabel jLabel_eV = new JLabel();

   private final double[] mPolynomial = new double[4];
   private boolean mOk = false;

   /**
    * Constructs a RescaleDialog
    */
   public RescaleDialog(Frame frame, String string, boolean _boolean) {
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

   public RescaleDialog() {
      this(new Frame(), "NoisyDialog", false);
   }

   public void initialize() {
      jLabel_Intro.setText("E'(E) = ");
      jTextField_Zero.setText("0.0");
      jLabel_Zero.setText("+");
      jTextField_One.setText("1.0");
      jLabel_One.setText("E +");
      jTextField_Two.setText("0.0");
      jLabel_Two.setText("E\u00B2 +");
      jTextField_Three.setText("0.0");
      jLabel_Three.setText("E\u00B3");

      final FormLayout fl = new FormLayout("right:30dlu, 2dlu, 30dlu, center:10dlu, 30dlu, center:15dlu, 30dlu, center:17dlu, 30dlu, 2dlu, left:25dlu", "pref, 4dlu, pref");
      final CellConstraints cc = new CellConstraints();
      jPanel_Polynomial.setLayout(fl);
      jPanel_Polynomial.add(jLabel_Intro, cc.xy(1, 1));
      jPanel_Polynomial.add(jTextField_Zero, cc.xy(3, 1));
      jPanel_Polynomial.add(jLabel_Zero, cc.xy(4, 1));
      jPanel_Polynomial.add(jTextField_One, cc.xy(5, 1));
      jPanel_Polynomial.add(jLabel_One, cc.xy(6, 1));
      jPanel_Polynomial.add(jTextField_Two, cc.xy(7, 1));
      jPanel_Polynomial.add(jLabel_Two, cc.xy(8, 1));
      jPanel_Polynomial.add(jTextField_Three, cc.xy(9, 1));
      jPanel_Polynomial.add(jLabel_Three, cc.xy(11, 1));
      jPanel_Polynomial.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      jLabel_Width.setText("Channel width");
      jTextField_Width.setText("10.0");
      jLabel_eV.setText("eV");

      jPanel_Polynomial.add(jLabel_Width, cc.xyw(6, 3, 3));
      jPanel_Polynomial.add(jTextField_Width, cc.xy(9, 3));
      jPanel_Polynomial.add(jLabel_eV, cc.xy(11, 3));

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

      jPanel_Main.setLayout(new FormLayout("4dlu, pref, 4dlu", "4dlu, pref, 4dlu, pref, 4dlu"));
      jPanel_Main.add(jPanel_Polynomial, cc.xy(2, 2));
      jPanel_Main.add(jPanel_Buttons, cc.xy(2, 4));

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
         private static final long serialVersionUID = 408458927767650699L;

         @Override
         public void actionPerformed(ActionEvent e) {
            cancel();
         }
      };
      getRootPane().getActionMap().put(CANCEL_ACTION_KEY, cancelAction);
   }

   public void jButton_Ok_actionPerformed(ActionEvent actionEvent) {
      try {
         final NumberFormat nf = NumberFormat.getInstance();
         mPolynomial[0] = nf.parse(jTextField_Zero.getText().trim()).doubleValue();
         mPolynomial[1] = nf.parse(jTextField_One.getText().trim()).doubleValue();
         mPolynomial[2] = nf.parse(jTextField_Two.getText().trim()).doubleValue();
         mPolynomial[3] = nf.parse(jTextField_Three.getText().trim()).doubleValue();
         mOk = true;
         setVisible(false);
      }
      catch(final ParseException e) {
      }
   }

   private void cancel() {
      mOk = false;
      setVisible(false);
   }

   public void jButton_Cancel_actionPerformed(ActionEvent actionEvent) {
      cancel();
   }

   // Overridden so we can exit when window is closed
   @Override
   protected void processWindowEvent(WindowEvent e) {
      if(e.getID() == WindowEvent.WINDOW_CLOSING)
         cancel();
      super.processWindowEvent(e);
   }

   public boolean getOk() {
      return mOk;
   }

   /**
    * setChannelWidth - in eV
    * 
    * @param d
    */
   public void setChannelWidth(double d) {
      if((d > 0.0) && (d < 1000.0)) {
         final NumberFormat nf = NumberFormat.getInstance();
         nf.setGroupingUsed(false);
         nf.setMaximumFractionDigits(2);
         jTextField_Width.setText(nf.format(d));
      }
   }

   /**
    * getChannelWidth - In eV
    * 
    * @return double
    */
   public double getChannelWidth() {
      try {
         return NumberFormat.getInstance().parse(jTextField_Width.getText()).doubleValue();
      }
      catch(final ParseException e) {
         return 10.0;
      }
   }

   /**
    * getPolynomial - Returns the polynomial coefficients entered by the user.
    * 
    * @return double[]
    */
   public double[] getPolynomial() {
      return mPolynomial.clone();
   }
}
