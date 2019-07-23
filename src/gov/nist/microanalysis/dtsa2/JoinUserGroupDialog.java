package gov.nist.microanalysis.dtsa2;

import gov.nist.microanalysis.EPQTools.ErrorDialog;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * <p>
 * A dialog for encouraging users to join the User and News Groups.
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
public class JoinUserGroupDialog
   extends JDialog {

   private static final String GROUP_SIGN_UP = "Group Sign-up";

   private static final long serialVersionUID = 4668181716912459929L;

   private static final String URL1 = "https://groups.google.com/forum/embed/?place=forum/dtsa-ii-user-group &showsearch=true&showpopout=true&showtabs=false";
   private static final String URL2 = "https://groups.google.com/forum/embed/?place=forum/dtsa-ii-news-group &showsearch=true&showpopout=true&showtabs=false";

   private final static String mMessage = "<HTML><head>\n"
         + "<style>\n"
         + "<!--\n"
         + "h3.top { font-family: Arial, Helvetica, sans-serif; font-size: 13pt; background-color: #D0D0D0; line-height: 140%; margin-top: 0.05in; margin-bottom: 0.05in; }\n"
         + "p.top  { font-family: Arial, Helvetica, sans-serif; font-size: 11pt; margin-left: 0.5cm; margin-right: 0.1 cm; margin-top: 0.05in; margin-bottom: 0.05in; }\n"
         + "-->"
         + "</style>\n</head><body>"
         + "<h3 class=top>DTSA-II News and User Groups</h3>\n"
         + "<p class=top>Please consider joining one or both of the DTSA-II News Group or the DTSA-II User Group.  These groups "
         + "are intended to facilitate communication between the NIST DTSA-II developers and the DTSA-II user base.</p>\n"
         + "<h3 class=top><a href="
         + URL2
         + ">DTSA-II News Group</a></h3>\n"
         + "<p class=top>Sign up for the News Group if you want occasional (weekly or monthly) messages "
         + "from the DTSA-II developers regarding updates, new features and tips for use of DTSA-II.  "
         + "The DTSA-II News Group will be a low-volume one-directional mechanism for the developers "
         + "to communicate with the user base.</p>\n"
         + "<h3 class=top><a href="
         + URL1
         + ">DTSA-II User Group</a></h3>\n"
         + "<p class=top>Sign up for the User Group if you want two-directional communication with other DTSA-II users. "
         + "is intended to facilitate communication between DTSA-II users.  The User Group will allow users to post questions"
         + " for the developers or other users to answer.  In time , members will be able review an archive of historical questions "
         + "and answers.</p>\n" + "<p class=top>Both groups are hosted by Google Groups.</p></body>";

   private final JEditorPane jEditorPane_Message = new JEditorPane();

   private final JButton jButton_SignUp = new JButton("Sign Up");
   private final JButton jButton_NoThanks = new JButton("No Thanks");

   static public void doSignUp(Frame parent) {
      final Preferences userPref = Preferences.userNodeForPackage(JoinUserGroupDialog.class);
      final long last = userPref.getLong(GROUP_SIGN_UP, -1);
      if((last == -1) || ((System.currentTimeMillis() - last) > (1000L * 3600L * 24L * 60L))) {
         final JoinUserGroupDialog jug = new JoinUserGroupDialog(parent);
         jug.setLocationRelativeTo(parent);
         jug.setVisible(true);
      }
   }

   static public void launchSignUp(Window parent) {
      try {
         Desktop.getDesktop().browse((new URL(URL1)).toURI());
         Desktop.getDesktop().browse((new URL(URL2)).toURI());
         final Preferences userPref = Preferences.userNodeForPackage(JoinUserGroupDialog.class);
         userPref.putLong(GROUP_SIGN_UP, 0);
      }
      catch(final Exception e1) {
         ErrorDialog.createErrorMessage(parent, "Error opening URL", "There was an error opening the URL for DTSA-II User and News Groups", "Please, manually paste the following URLS into your browser to sign up for these user groups\n\t"
               + URL1 + "\n\t" + URL2);
      }
   }

   private void initialize() {
      jEditorPane_Message.setContentType("text/html");
      jEditorPane_Message.setEditable(false);
      jEditorPane_Message.setFont(Font.getFont(Font.SANS_SERIF));
      jEditorPane_Message.setText(mMessage);
      jEditorPane_Message.addHyperlinkListener(new HyperlinkListener() {

         @Override
         public void hyperlinkUpdate(HyperlinkEvent arg0) {
            if(arg0.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
               try {
                  Desktop.getDesktop().browse(arg0.getURL().toURI());
               }
               catch(final Exception e1) {
                  ErrorDialog.createErrorMessage(JoinUserGroupDialog.this.getOwner(), "Error opening URL", "There was an error opening the URL for DTSA-II User and News Groups", "Please, manually paste the following URLS into your browser to sign up for these user groups\n\t"
                        + URL1 + "\n\t" + URL2);
               }
         }
      });
      jButton_NoThanks.addActionListener(new AbstractAction() {
         private static final long serialVersionUID = -6038071094133882800L;

         @Override
         public void actionPerformed(ActionEvent e) {
            final Preferences userPref = Preferences.userNodeForPackage(JoinUserGroupDialog.class);
            userPref.putLong(GROUP_SIGN_UP, System.currentTimeMillis());
            JoinUserGroupDialog.this.setVisible(false);
            JOptionPane.showMessageDialog(JoinUserGroupDialog.this.getOwner(), "You can sign up at any time using the Help - User Group Signup menu item.", "Maybe later?", JOptionPane.INFORMATION_MESSAGE);
         }

      });
      jButton_SignUp.addActionListener(new AbstractAction() {
         private static final long serialVersionUID = -1551348025764685087L;

         @Override
         public void actionPerformed(ActionEvent e) {
            launchSignUp(JoinUserGroupDialog.this.getOwner());
            JoinUserGroupDialog.this.setVisible(false);
         };
      });
      final CellConstraints cc = new CellConstraints();
      final FormLayout fl = new FormLayout("fill:250dlu", "fill:230dlu, 10dlu, pref");
      final PanelBuilder pb = new PanelBuilder(fl);
      pb.add(new JScrollPane(jEditorPane_Message), cc.xy(1, 1));
      final ButtonBarBuilder bbb = new ButtonBarBuilder();
      bbb.addGlue();
      bbb.addButton(jButton_SignUp, jButton_NoThanks);
      bbb.addGlue();
      pb.add(bbb.build(), cc.xy(1, 3));
      final JPanel panel = pb.getPanel();
      panel.setBorder(DTSA2.createEmptyBorder());
      add(panel);

      pack();
   }

   /**
    * Constructs a JoinUserGroupDialog
    * 
    * @param arg0
    */
   public JoinUserGroupDialog(Frame arg0) {
      super(arg0);
      initialize();
   }
}
