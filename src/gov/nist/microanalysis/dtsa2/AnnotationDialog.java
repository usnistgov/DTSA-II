package gov.nist.microanalysis.dtsa2;

import gov.nist.microanalysis.EPQTools.ErrorDialog;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * <p>
 * A dialog to permit users to enter an annotation into the HTML report
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
public class AnnotationDialog
   extends JDialog {

   private static final long serialVersionUID = 1027339695019341726L;

   private JTextPane jTextPane_Annot;
   private boolean mOk;

   private AnnotationDialog(Frame fm) {
      super(fm, "Enter an annotation for the report", true);
      try {
         initialize();
         pack();
         jTextPane_Annot.requestFocusInWindow();
         mOk = false;

      }
      catch(final Exception ex) {
         ErrorDialog.createErrorMessage(fm, "Annotation dialog", ex);
      }
   }

   private void initialize() {
      final CellConstraints cc = new CellConstraints();
      final JPanel pnl = new JPanel(new FormLayout("fill:250dlu:grow", "pref, 5dlu, fill:200dlu:grow, 5dlu, pref"));
      jTextPane_Annot = new JTextPane();
      jTextPane_Annot.setContentType("text/html");
      final JButton bold = new JButton("<html><b>B</b>");
      bold.addActionListener(jTextPane_Annot.getActionMap().get("font-bold"));
      final JButton italic = new JButton("<html><i>i</i>");
      italic.addActionListener(jTextPane_Annot.getActionMap().get("font-italic"));
      final JButton underline = new JButton("<html><u>u</i>");
      underline.addActionListener(jTextPane_Annot.getActionMap().get("font-underline"));
      final JPanel btns = new JPanel(new FormLayout("pref, 2dlu, pref, 2dlu, pref", "pref"));
      btns.add(bold, cc.xy(1, 1));
      btns.add(italic, cc.xy(3, 1));
      btns.add(underline, cc.xy(5, 1));
      pnl.add(btns, cc.xy(1, 1));
      pnl.add(new JScrollPane(jTextPane_Annot), cc.xy(1, 3));
      final JButton ok = new JButton("Ok");
      ok.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            mOk = true;
            setVisible(false);
         }
      });
      final JButton cancel = new JButton("Cancel");
      cancel.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            mOk = false;
            setVisible(false);
         }
      });
      final ButtonBarBuilder bbb = new ButtonBarBuilder();
      bbb.addGlue();
      bbb.addButton(ok, cancel);
      pnl.add(bbb.build(), cc.xy(1, 5));
      pnl.setBorder(DTSA2.createEmptyBorder());
      setContentPane(pnl);
   }

   private String getResult() {
      return mOk ? jTextPane_Annot.getText() : null;
   }

   public static String getAnnotation(Frame fm) {
      class AnnotRunnable
         implements Runnable {
         String mResult;
         Frame mFrame;

         AnnotRunnable(Frame fm) {
            mFrame = fm;
         }

         @Override
         public void run() {
            final AnnotationDialog ad = new AnnotationDialog(mFrame);
            ad.setLocationRelativeTo(mFrame);
            ad.setVisible(true);
            mResult = ad.getResult();
         }
      }
      ;
      final AnnotRunnable ar = new AnnotRunnable(fm);
      if(SwingUtilities.isEventDispatchThread())
         ar.run();
      else
         try {
            SwingUtilities.invokeAndWait(ar);
         }
         catch(final Exception e) {
            e.printStackTrace();
         }
      return ar.mResult;
   }
}
