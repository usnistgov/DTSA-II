package gov.nist.microanalysis.dtsa2;

import java.awt.AWTEvent;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * <p>
 * Title: EPQ - Electron Probe Quantitation tool kit
 * </p>
 * <p>
 * Description: A series of tools for electron probe quantitation.
 * </p>
 * <p>
 * Copyright: Pursuant to title 17 Section 105 of the United States Code this
 * software is not subject to copyright protection and is in the public domain
 * </p>
 * <p>
 * Company: National Institute of Standards and Technology
 * </p>
 * 
 * @author Nicholas W. M. Ritchie
 * @version 1.0
 */

public class MainFrame_AboutBox
   extends JDialog {
   static public final long serialVersionUID = 0x13867f6abL;

   public MainFrame_AboutBox(Frame parent) {
      super(parent);
      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      try {
         initialize();
         pack();
      }
      catch(final Exception e) {
         e.printStackTrace();
      }
   }

   MainFrame_AboutBox() {
      this(null);
   }

   // Component initialization
   private void initialize()
         throws Exception {

      final JLabel icon = new JLabel();
      icon.setVerticalTextPosition(SwingConstants.BOTTOM);
      icon.setHorizontalTextPosition(SwingConstants.CENTER);
      icon.setForeground(SystemColor.textText);
      icon.setIcon(new ImageIcon(MainFrame.class.getResource("about.png")));
      icon.setText(DTSA2.getRevision(DTSA2.class) + " revision");

      final JEditorPane htmlPane = new JEditorPane();
      htmlPane.setBackground(SystemColor.window);
      htmlPane.setContentType("text/html");
      htmlPane.setPage(MainFrame_AboutBox.class.getResource("EPQ_title.html"));
      htmlPane.setEditable(false);
      htmlPane.setKeymap(null);
      htmlPane.addHyperlinkListener(new HyperlinkListener() {
         @Override
         public void hyperlinkUpdate(HyperlinkEvent e) {
            if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
               try {
                  Desktop.getDesktop().browse(e.getURL().toURI());
               }
               catch(final Exception ex) {
                  ex.printStackTrace();
               }
         }
      });

      final JScrollPane scrollPane = new JScrollPane();
      scrollPane.setBorder(DTSA2.createDefaultBorder());
      scrollPane.getViewport().add(htmlPane);
      scrollPane.setFocusable(false);

      final JButton okButton = new JButton("Done");
      okButton.addActionListener(new AbstractAction() {
         static private final long serialVersionUID = 0xabcdef39652L;

         @Override
         public void actionPerformed(ActionEvent e) {
            cancel();
         }
      });
      addCancelByEscapeKey();

      {
         final JPanel main = new JPanel(new FormLayout("5dlu, pref, 5dlu, 350dlu, 5dlu", "5dlu, 300dlu, 5dlu, pref, 5dlu"));
         final CellConstraints cc = new CellConstraints();
         main.add(icon, cc.xy(2, 2, CellConstraints.CENTER, CellConstraints.CENTER));
         main.add(scrollPane, cc.xy(4, 2, CellConstraints.FILL, CellConstraints.FILL));
         final ButtonBarBuilder bbb = new ButtonBarBuilder();
         bbb.addGlue();
         bbb.addButton(okButton);
         main.add(bbb.build(), cc.xyw(2, 4, 3));
         getContentPane().add(main, null);
      }
      setTitle("About " + DTSA2.APP_NAME + " and EPQ");
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

   // Overridden so we can exit when window is closed
   @Override
   protected void processWindowEvent(WindowEvent e) {
      if(e.getID() == WindowEvent.WINDOW_CLOSING)
         cancel();
      super.processWindowEvent(e);
   }

   // Close the dialog
   void cancel() {
      dispose();
   }
}
