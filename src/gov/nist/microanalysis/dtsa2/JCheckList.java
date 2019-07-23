package gov.nist.microanalysis.dtsa2;

import java.awt.Component;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * <p>
 * Title: EPQ - Electron Probe Quantitation tool kit
 * </p>
 * <p>
 * Description: Extends JList to encompass a list of check boxes.
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

public class JCheckList
   extends JList<Object> {
   static public final long serialVersionUID = 0x1;

   private static Border mNoFocusBorder = new EmptyBorder(1, 1, 1, 1);

   private class CheckListCellRenderer
      extends JCheckBox
      implements ListCellRenderer<Object> {
      static public final long serialVersionUID = 0x1;

      private CheckListCellRenderer() {
         setOpaque(true);
         setForeground(SystemColor.textText);
         setBorder(mNoFocusBorder);
      }

      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
         setText(value.toString());
         setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
         setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

         final CheckBoxData data = (CheckBoxData) value;
         setSelected(data.isSelected());

         setFont(list.getFont());
         setBorder((cellHasFocus) ? UIManager.getBorder("List.focusCellHighlightBorder") : mNoFocusBorder);

         return this;
      }
   }

   private class CheckListener
      implements MouseListener, KeyListener {
      private final JCheckList mParent;

      private CheckListener(JCheckList parent) {
         mParent = parent;
      }

      @Override
      public void mouseClicked(MouseEvent e) {
         if(e.getX() < 20)
            doCheck();
      }

      @Override
      public void mousePressed(MouseEvent e) {
      }

      @Override
      public void mouseReleased(MouseEvent e) {
      }

      @Override
      public void mouseEntered(MouseEvent e) {
      }

      @Override
      public void mouseExited(MouseEvent e) {
      }

      @Override
      public void keyPressed(KeyEvent e) {
         if(e.getKeyChar() == KeyEvent.VK_SPACE)
            doCheck();
      }

      @Override
      public void keyTyped(KeyEvent e) {
      }

      @Override
      public void keyReleased(KeyEvent e) {
      }

      private void doCheck() {
         final int index = mParent.getSelectedIndex();
         if(index < 0)
            return;
         final CheckBoxData data = (CheckBoxData) mParent.getModel().getElementAt(index);
         data.invertSelected();
         mParent.repaint();
         final ListSelectionListener[] lsl = mParent.getListSelectionListeners();
         final ListSelectionEvent lse = new ListSelectionEvent(data.mObject, index, index, false);
         for(final ListSelectionListener element : lsl)
            element.valueChanged(lse);
      }
   }

   public class CheckBoxData {
      private final Object mObject;
      private boolean mSelected;

      public CheckBoxData(Object obj, boolean selected) {
         mObject = obj;
         mSelected = selected;
      }

      public String getName() {
         return mObject.toString();
      }

      public void setSelected(boolean selected) {
         mSelected = selected;
      }

      public void invertSelected() {
         mSelected = !mSelected;
      }

      public boolean isSelected() {
         return mSelected;
      }

      @Override
      public String toString() {
         return mObject.toString();
      }

      public Object getObject() {
         return mObject;
      }

   }

   public JCheckList() {
      super();
      setCellRenderer(new CheckListCellRenderer());
      final CheckListener cl = new CheckListener(this);
      addMouseListener(cl);
      addKeyListener(cl);
   }

   public JCheckList(Object[] p0) {
      super();
      setCellRenderer(new CheckListCellRenderer());
      final CheckListener cl = new CheckListener(this);
      addMouseListener(cl);
      addKeyListener(cl);
      for(int i = 0; i < p0.length; ++i)
         p0[i] = new CheckBoxData(p0[i].toString(), false);
      this.setListData(p0);
   }

   public JCheckList(Vector<Object> p0) {
      super();
      setCellRenderer(new CheckListCellRenderer());
      final CheckListener cl = new CheckListener(this);
      addMouseListener(cl);
      addKeyListener(cl);
      for(int i = 0; i < p0.size(); ++i)
         p0.set(i, new CheckBoxData(p0.get(i).toString(), false));
      this.setListData(p0);
   }

   public JCheckList(ListModel<Object> p0) {
      super(p0);
      assert (false);
      setCellRenderer(new CheckListCellRenderer());
      final CheckListener cl = new CheckListener(this);
      addMouseListener(cl);
      addKeyListener(cl);
   }

   @Override
   public void setListData(Object[] objs) {
      for(int i = 0; i < objs.length; ++i)
         objs[i] = new CheckBoxData(objs[i], false);
      super.setListData(objs);
   }

   /**
    * isChecked - Is the check box at this index selected?
    * 
    * @param index int
    * @return boolean
    */
   public boolean isChecked(int index) {
      if(index < getModel().getSize()) {
         final CheckBoxData cbd = (CheckBoxData) getModel().getElementAt(index);
         return cbd.isSelected();
      }
      return false;
   }

   public void setChecked(int index, boolean b) {
      if(index < getModel().getSize()) {
         final CheckBoxData cbd = (CheckBoxData) getModel().getElementAt(index);
         cbd.setSelected(b);
      }
   }

   public Object getObject(int index) {
      if(index < getModel().getSize()) {
         final CheckBoxData cbd = (CheckBoxData) getModel().getElementAt(index);
         return cbd.mObject;
      }
      return null;
   }

}
