package gov.nist.microanalysis.dtsa2;

import gov.nist.microanalysis.EPQTools.JWizardDialog;

import java.awt.Font;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A mechanism for displaying user preferences in a unified fashion.
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
public class PreferenceDialog
   extends JDialog {

   private static final long serialVersionUID = 0x95962961118L;

   private final JTree mPreferenceTree = new JTree();
   private DefaultMutableTreeNode mRoot;
   private final JLabel mMessageLabel = new JLabel();
   private final JLabel mBannerLabel = new JLabel();
   private JScrollPane mContentPane = null;

   /**
    * Constructs a PreferenceDialog
    * 
    * @throws HeadlessException
    */
   public PreferenceDialog()
         throws HeadlessException {
      super();
      try {
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         initialize();
         pack();
      }
      catch(final Exception ex) {
         ex.printStackTrace();
      }
   }

   private void initialize() {
      setLayout(new FormLayout("5dlu, 150dlu, 3dlu, 300dlu, 5dlu", "5dlu, pref, 5dlu, 200dlu, 5dlu, pref, 5dlu, pref, 5dlu"));
      final CellConstraints cc = new CellConstraints();
      final Border bdr = BorderFactory.createCompoundBorder(DTSA2.createDefaultBorder(), DTSA2.createEmptyBorder());

      mRoot = new DefaultMutableTreeNode("Root");
      mPreferenceTree.setModel(new DefaultTreeModel(mRoot));
      mPreferenceTree.setRootVisible(false);
      mPreferenceTree.setExpandsSelectedPaths(true);
      final TreeSelectionModel tsm = new DefaultTreeSelectionModel();
      tsm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      mPreferenceTree.setSelectionModel(tsm);
      tsm.addTreeSelectionListener(new TreeSelectionListener() {
         @Override
         public void valueChanged(TreeSelectionEvent e) {
            final DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) mPreferenceTree.getLastSelectedPathComponent();
            if(dmtn != null) {
               final Object obj = dmtn.getUserObject();
               mBannerLabel.setText(obj.toString());
               if(obj instanceof PreferencePanel) {
                  final PreferencePanel pp = (PreferencePanel) obj;
                  if(mContentPane != null) {
                     remove(mContentPane);
                     mContentPane = null;
                  }
                  mMessageLabel.setText(pp.mDescription);
                  mContentPane = new JScrollPane(pp);
                  final CellConstraints cc = new CellConstraints();
                  pp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                  add(mContentPane, cc.xy(4, 4, CellConstraints.FILL, CellConstraints.FILL));
               } else
                  mMessageLabel.setText("Unexpected panel");
            }
         }
      });

      final JScrollPane sp = new JScrollPane(mPreferenceTree);
      add(sp, cc.xywh(2, 2, 1, 5));
      {
         mBannerLabel.setText("User preferences");
         final Font def = mBannerLabel.getFont();
         mBannerLabel.setFont(new Font(def.getName(), def.getStyle(), 2 * def.getSize()));
         mBannerLabel.setIcon(new ImageIcon(JWizardDialog.class.getResource("ClipArt/alien_sm.png")));
         mBannerLabel.setBorder(bdr);
         add(mBannerLabel, cc.xy(4, 2));
      }
      {
         final JPanel msgPanel = new JPanel(new FormLayout("5dlu, left:pref, 5dlu", "1dlu, center:15dlu, 1dlu"));
         msgPanel.setBorder(bdr);
         msgPanel.add(mMessageLabel, cc.xy(2, 2));
         add(msgPanel, cc.xyw(4, 6, 1));
      }
      {
         final JButton ok = new JButton("OK");
         ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               commit(mRoot);
               setVisible(false);
            }
         });
         final JButton cancel = new JButton("Cancel");
         cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               setVisible(false);
            }
         });
         final JButton apply = new JButton("Apply");
         apply.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               commit(mRoot);
            }
         });
         final ButtonBarBuilder bbb = new ButtonBarBuilder();
         bbb.addGlue();
         bbb.addButton(ok, cancel, apply);
         add(bbb.build(), cc.xyw(2, 8, 3));
         getRootPane().setDefaultButton(ok);
         setResizable(false);
      }
   }

   private void commit(DefaultMutableTreeNode node) {
      final Object obj = node.getUserObject();
      if(obj instanceof PreferencePanel)
         ((PreferencePanel) obj).commit();
      for(int cx = node.getChildCount() - 1; cx >= 0; --cx)
         commit((DefaultMutableTreeNode) node.getChildAt(cx));
   }

   /**
    * Adds a new PreferencePanel to the parent TreeNode. If the PreferencePanel
    * has children then these children are added recursively.
    * 
    * @param parent
    * @param pp
    * @return The DefaultMutableTreeNode representing the child PreferencePanel
    */
   private DefaultMutableTreeNode addNode(DefaultMutableTreeNode parent, PreferencePanel pp) {
      final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(pp);
      for(final PreferencePanel ch : pp.mChildren)
         addNode(newNode, ch);
      parent.add(newNode);
      return newNode;
   }

   /**
    * Recursively checks child tree nodes to find the parent. When the parent is
    * found the child is added to the parent.
    */
   private void addNode(DefaultMutableTreeNode base, PreferencePanel parent, PreferencePanel child, boolean selected) {
      final DefaultMutableTreeNode cn = findPanel(base, parent);
      if(cn != null) {
         final DefaultMutableTreeNode newNode = addNode(cn, child);
         parent.mChildren.add(child);
         mPreferenceTree.setModel(new DefaultTreeModel(mRoot));
         if(selected)
            mPreferenceTree.setSelectionPath(new TreePath(newNode.getPath()));
      }
   }

   /**
    * Adds a child PreferencePanel to the specified parent PreferencePanel.
    * 
    * @param parent
    * @param child
    */
   public void addPanel(PreferencePanel parent, PreferencePanel child, boolean selected) {
      addNode(mRoot, parent, child, selected);
   }

   /**
    * Adds a preferences panel at the base level.
    * 
    * @param pp
    */
   public void addPanel(PreferencePanel pp) {
      addNode(mRoot, pp);
      mPreferenceTree.setModel(new DefaultTreeModel(mRoot));
      for(int row = 0; row < mPreferenceTree.getRowCount(); ++row)
         mPreferenceTree.expandRow(row);

   }

   static public class PreferencePanel
      extends JPanel {

      static private final long serialVersionUID = 0x123123L;

      private final PreferenceDialog mParent;
      private final String mDescription;
      private String mLabel;
      private final ArrayList<PreferencePanel> mChildren = new ArrayList<PreferencePanel>();

      PreferencePanel(PreferenceDialog pref, String label, String desc) {
         mParent = pref;
         mLabel = label;
         mDescription = desc;
      }

      public void commitAll() {
         commit();
         for(final PreferencePanel pp : mChildren)
            pp.commitAll();
      }

      /**
       * Override to apply and commit all modifications to the preferences on
       * this page.
       */
      public void commit() {
         assert permitExit();
      }

      /**
       * Is the data on this page valid? Should the PreferenceDialog allow the
       * user to switch to another page?
       */
      public boolean permitExit() {
         return true;
      }

      /**
       * Returns any child preference panels associated with this preference
       * panel. The child panels may too have child panels.
       * 
       * @return PreferencePanel[]
       */
      public PreferencePanel[] getChildren() {
         final PreferencePanel[] res = new PreferencePanel[mChildren.size()];
         return mChildren.toArray(res);
      }

      /**
       * Add a child PreferencePanel to this panel. Eventually this mechanism
       * will be made dynamic to allow children to be added while the user
       * modifies the PreferenceDialog.
       * 
       * @param child
       */
      public void addChildPanel(PreferencePanel child) {
         mChildren.add(child);
      }

      /**
       * Gets the current value assigned to description
       * 
       * @return Returns the description.
       */
      public String getDescription() {
         return mDescription;
      }

      /**
       * Gets the current value assigned to name
       * 
       * @return Returns the name.
       */
      @Override
      public String toString() {
         return mLabel;
      }

      /**
       * Gets the preference dialog to which this panel belongs
       * 
       * @return Returns the parent.
       */
      protected PreferenceDialog getPreferenceDialog() {
         return mParent;
      }

      /**
       * Provide a new label for this page. This label is used in the header and
       * in the tree view.
       * 
       * @param label
       */
      public void relabel(String label) {
         mLabel = label;
         final PreferenceDialog pd = getPreferenceDialog();
         pd.mBannerLabel.setText(label);
         final TreePath sel = pd.mPreferenceTree.getSelectionPath();
         pd.mPreferenceTree.setModel(new DefaultTreeModel(pd.mRoot));
         pd.mPreferenceTree.setSelectionPath(sel);
      }
   }

   /**
    * Constructs a PreferenceDialog
    * 
    * @param owner
    * @throws HeadlessException
    */
   public PreferenceDialog(Frame owner)
         throws HeadlessException {
      super(owner, "Preferences", true);
      try {
         initialize();
         pack();
      }
      catch(final Exception ex) {
         ex.printStackTrace();
      }
   }

   /**
    * Constructs a PreferenceDialog
    * 
    * @param owner
    * @param modal
    * @throws HeadlessException
    */
   public PreferenceDialog(Frame owner, boolean modal)
         throws HeadlessException {
      super(owner, "Preferences", modal);
      try {
         initialize();
         pack();
      }
      catch(final Exception ex) {
         ex.printStackTrace();
      }
   }

   /**
    * Constructs a PreferenceDialog
    * 
    * @param owner
    * @param title
    * @param modal
    * @throws HeadlessException
    */
   public PreferenceDialog(Frame owner, String title, boolean modal)
         throws HeadlessException {
      super(owner, title, modal);
      try {
         initialize();
         pack();
      }
      catch(final Exception ex) {
         ex.printStackTrace();
      }
   }

   public void setMessage(String msg) {
      mMessageLabel.setText(msg);
   }

   private DefaultMutableTreeNode findPanel(DefaultMutableTreeNode root, PreferencePanel pp) {
      if(root.getUserObject() == pp)
         return root;
      for(int cx = root.getChildCount() - 1; cx >= 0; --cx) {
         final DefaultMutableTreeNode node = findPanel((DefaultMutableTreeNode) root.getChildAt(cx), pp);
         if(node != null)
            return node;
      }
      return null;
   }
}
