package gov.nist.microanalysis.dtsa2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import gov.nist.microanalysis.EPQLibrary.ISpectrumData;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;
import gov.nist.microanalysis.dtsa2.JCommandLine.JythonWorker;

/**
 * <p>
 * Title: EPQ - Electron Probe Quantitation tool kit
 * </p>
 * <p>
 * Description: The DataManager has the principle ownership of all data items.
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
public class DataManager {
   /**
    * ADD_SPECTRUM - A spectrum was added to the spectrum list.
    */
   public static final int ADD_SPECTRUM = 1;
   /**
    * REMOVE_SPECTRUM - A spectrum was removed from the spectrum list.
    */
   public static final int REMOVE_SPECTRUM = 2;
   /**
    * UPDATE_SPECTRUM - The spectrum at the specified index has been modified
    */
   public static final int UPDATE_SPECTRUM = 4;
   /**
    * SELECTION_CHANGE - The set of selected spectra has been modified
    */
   public static final int SELECTION_CHANGE = 8;
   /**
    * REORDERED_SPECTRA - Changed the order of the spectra
    */
   public static final int REORDER_SPECTRA = 9;

   // This class is a singleton...
   private static DataManager mInstance = null;

   /**
    * The index of the next spectrum index in Jython ie "s%d" % nextIndex
    */
   private int mNextIndex = 1;

   private DataManager() {
   }

   private final ArrayList<ISpectrumData> mSpectra = new ArrayList<ISpectrumData>();
   private final HashMap<ISpectrumData, Integer> mSpectrumIndex = new HashMap<ISpectrumData, Integer>();
   private final ArrayList<ISpectrumData> mSelected = new ArrayList<ISpectrumData>();
   private boolean mNotify = true;
   private JythonWorker mJython;

   private final Set<ActionListener> mListeners = new HashSet<ActionListener>();

   private void fireActionListener(int action) {

      if((!mListeners.isEmpty()) && mNotify) {
         final ActionEvent ae = new ActionEvent(this, action, "Data Manager");
         for(final ActionListener al : mListeners)
            try {
               al.actionPerformed(ae);
            }
            catch(final Exception e) {
            }
      }
   }

   public void enableEvents(boolean b) {
      mNotify = b;
   }

   public boolean eventsEnabled() {
      return mNotify;
   }

   /**
    * getInstance - Get the singleton instance of this class.
    * 
    * @return DataManager
    */
   public static DataManager getInstance() {
      if(mInstance == null)
         mInstance = new DataManager();
      mInstance.validate();
      return mInstance;
   }

   /**
    * addActionListener - This class fires when the contents of the data manager
    * change.
    * 
    * @param ae ActionListener
    */
   public void addActionListener(ActionListener ae) {
      mListeners.add(ae);
   }

   /**
    * removeActionListener - Remove the specified action listener.
    * 
    * @param ae ActionListener
    */
   public void removeActionListener(ActionListener ae) {
      mListeners.remove(ae);
   }

   /**
    * clearAllActionListeners - Clears all action listeners.
    */
   public void clearAllActionListeners() {
      mListeners.clear();
   }

   /**
    * addSpectrum - Add a spectrum to the spectrum list but don't select it.
    * 
    * @param sd ISpectrumData
    */
   public void addSpectrum(ISpectrumData sd) {
      addSpectrum(sd, false);
   }

   /**
    * addSpectrum - Add a spectrum to the spectrum list and optionally select
    * it.
    * 
    * @param sd ISpectrumData
    */
   public void addSpectrum(ISpectrumData sd, boolean select) {
      addSpectrum(sd, select, null);
   }

   /**
    * addSpectrum - Add a spectrum to the spectrum list and optionally select
    * it.
    * 
    * @param sd ISpectrumData
    */
   public void addSpectrum(ISpectrumData sd, boolean select, ISpectrumData assoc) {
      int event = 0;
      if((sd != null) && (!mSpectra.contains(sd))) {
         final Integer i = Integer.valueOf(mNextIndex++);
         // Place the spectrum on the list
         if((assoc != null) && mSpectra.contains(assoc))
            mSpectra.add(mSpectra.indexOf(assoc) + 1, sd);
         else
            mSpectra.add(sd);
         // Add the spectrum to the index list
         mSpectrumIndex.put(sd, i);
         // Add the spectrum to the Jython environment
         if(mJython != null)
            try {
               mJython.addSpectrum(sd, i.intValue());
            }
            catch(final InterruptedException e) {
               e.printStackTrace();
            }
         event = ADD_SPECTRUM;
      }
      // Notify others...
      if(select) {
         mSelected.add(sd);
         event |= SELECTION_CHANGE;
      }
      if(event != 0)
         fireActionListener(event);
      validate();
   }

   /**
    * Shifts the selected spectra up in the spectrum list
    */
   public void moveSelectedUp() {
      final ArrayList<ISpectrumData> sel = new ArrayList<ISpectrumData>();
      for(final ISpectrumData spec : mSpectra)
         if(mSelected.contains(spec))
            sel.add(spec);
      boolean update = false;
      int prev = 0;
      for(final ISpectrumData spec : sel) {
         final int i = mSpectra.indexOf(spec);
         if(i > prev)
            if(!sel.contains(mSpectra.get(i - 1))) {
               mSpectra.set(i, mSpectra.set(i - 1, mSpectra.get(i)));
               update = true;
               prev = i;
            }
      }
      if(update)
         fireActionListener(REORDER_SPECTRA);
      validate();
   }

   /**
    * Shifts the selected spectra down in the spectrum list
    */
   public void moveSelectedDown() {
      final ArrayList<ISpectrumData> sel = new ArrayList<ISpectrumData>();
      for(final ISpectrumData spec : mSpectra)
         if(mSelected.contains(spec))
            sel.add(spec);
      boolean update = false;
      for(int j = sel.size() - 1; j >= 0; --j) {
         final ISpectrumData spec = sel.get(j);
         final int i = mSpectra.indexOf(spec);
         if(((i + 1) < mSpectra.size()) && (!sel.contains(mSpectra.get(i + 1)))) {
            mSpectra.set(i, mSpectra.set(i + 1, mSpectra.get(i)));
            update = true;
         }
      }
      if(update)
         fireActionListener(REORDER_SPECTRA);
      validate();
   }

   /**
    * Groups all selected spectra into a contiguous selection.
    */
   public void group() {
      int first = -1;
      final ArrayList<ISpectrumData> sel = new ArrayList<ISpectrumData>();
      for(int i = 0; i < mSpectra.size(); ++i) {
         final ISpectrumData spec = mSpectra.get(i);
         if(mSelected.contains(spec)) {
            if(first == -1)
               first = i;
            sel.add(spec);
         }
      }
      mSpectra.removeAll(sel);
      for(final ISpectrumData spec : sel)
         mSpectra.add(first++, spec);
      if(sel.size() > 1)
         fireActionListener(REORDER_SPECTRA);
      validate();
   }

   /**
    * removeSpectrum - Remove the specified spectrum from the spectrum list.
    * 
    * @param sd ISpectrumData
    */
   public void removeSpectrum(ISpectrumData sd) {
      final Integer index = mSpectrumIndex.get(sd);
      if(index != null) {
         if(mJython != null)
            try {
               mJython.removeSpectrum(index.intValue());
            }
            catch(final InterruptedException e) {
               e.printStackTrace();
            }
         mSpectra.remove(sd);
         mSpectrumIndex.remove(sd);
         int event = REMOVE_SPECTRUM;
         if(mSelected.contains(sd)) {
            mSelected.remove(sd);
            event |= SELECTION_CHANGE;
         }
         fireActionListener(event);
      }
      validate();
   }

   /**
    * replaceSpectrum - Replace the old spectrum with the newSp spectrum. If old
    * was selected then newSp will be selected; otherwise newSp is not selected.
    * 
    * @param old ISpectrumData
    * @param newSp ISpectrumData
    */
   public void replaceSpectrum(ISpectrumData old, ISpectrumData newSp) {
      final Integer index = mSpectrumIndex.get(old);
      if(index != null) {
         assert mSpectra.contains(old);
         final int selIdx = mSelected.indexOf(old);
         final int specIdx = mSpectra.indexOf(old);
         // Put newSp in place of old
         mSpectra.set(specIdx, newSp);
         mSpectrumIndex.remove(old);
         mSpectrumIndex.put(newSp, index);
         int event = UPDATE_SPECTRUM;
         if(selIdx >= 0) {
            mSelected.set(selIdx, newSp);
            // event |= SELECTION_CHANGE;
         }
         if(mJython != null)
            try {
               mJython.addSpectrum(newSp, index.intValue());
            }
            catch(final InterruptedException e) {
               e.printStackTrace();
            }
         fireActionListener(event);
         validate();
      } else
         addSpectrum(newSp);
   }

   /**
    * replaceSpectra - Replace a group of old spectra with new spectra. The map
    * is old to new.
    * 
    * @param oldToNew
    */
   public void replaceSpectra(Map<ISpectrumData, ISpectrumData> oldToNew) {
      int event = UPDATE_SPECTRUM;
      for(final Map.Entry<ISpectrumData, ISpectrumData> me : oldToNew.entrySet()) {
         final ISpectrumData old = me.getKey();
         final ISpectrumData newSp = me.getValue();
         final Integer index = mSpectrumIndex.get(old);
         if(index != null) {
            assert mSpectra.contains(old);
            final int selIdx = mSelected.indexOf(old);
            final int specIdx = mSpectra.indexOf(old);
            mSpectra.set(specIdx, newSp);
            mSpectrumIndex.remove(old);
            mSpectrumIndex.put(newSp, index);
            if(selIdx >= 0) {
               mSelected.set(selIdx, newSp);
               event |= SELECTION_CHANGE;
            }
            if(mJython != null)
               try {
                  mJython.addSpectrum(newSp, index.intValue());
               }
               catch(final InterruptedException e) {
                  e.printStackTrace();
               }
         }
      }
      fireActionListener(event);
      validate();
   }

   /**
    * containsSpectrum - Does the data manager contain the specified spectrum?
    * 
    * @param spec ISpectrumData
    * @return boolean
    */
   public boolean containsSpectrum(ISpectrumData spec) {
      return mSpectra.contains(spec);
   }

   /**
    * spectrumList - Returns the spectra ISpectrumData items in the spectrum
    * list as an immutable List.
    * 
    * @return Collection&lt;ISpectrumData&gt;
    */
   public Collection<ISpectrumData> spectrumList() {
      return Collections.unmodifiableList(mSpectra);
   }

   /**
    * Vadidate the consistency of the mSpectrum, mSpectrumIndex and mSelected.
    */
   private void validate() {
      // Every selected spectrum is also listed
      for(final ISpectrumData spec : mSelected)
         assert mSpectra.contains(spec);
      // Every spectrum in the index is listed
      for(final ISpectrumData spec : mSpectrumIndex.keySet())
         assert mSpectra.contains(spec);
      // Every listed spectrum is in the index
      for(final ISpectrumData spec : mSpectra)
         assert mSpectrumIndex.containsKey(spec);
   }

   /**
    * clearSpectrumList - Clear all spectra from the spectrum list.
    */
   public void clearSpectrumList() {
      if(mJython != null)
         for(final Map.Entry<ISpectrumData, Integer> me : mSpectrumIndex.entrySet())
            try {
               mJython.removeSpectrum(me.getValue().intValue());
            }
            catch(final InterruptedException e) {
               e.printStackTrace();
            }
      mSpectra.clear();
      mSpectrumIndex.clear();
      int event = REMOVE_SPECTRUM;
      if(!mSelected.isEmpty()) {
         mSelected.clear();
         event |= SELECTION_CHANGE;
      }
      fireActionListener(event);
      validate();
   }

   /**
    * Returns a unmodifiable list containing all selected ISpectrumData objects.
    * 
    * @return List&lt;ISpectrumData&gt;
    */
   public List<ISpectrumData> getSelected() {
      // Should this be a weak list? Probably not necessary...
      return Collections.unmodifiableList(mSelected);
   }

   /**
    * Set the selected spectra to the specified set.
    * 
    * @param specs
    */
   public void setSelected(Collection<ISpectrumData> specs) {
      int event = mSelected.isEmpty() ? 0 : SELECTION_CHANGE;
      mSelected.clear();
      for(final ISpectrumData spec : specs)
         if(mSpectra.contains(spec)) {
            mSelected.add(spec);
            event = SELECTION_CHANGE;
         }
      if(event != 0)
         fireActionListener(event);
      validate();
   }

   /**
    * Select or deselect the specified collection of spectra.
    * 
    * @param specs
    * @param select
    */
   public void select(Collection<ISpectrumData> specs, boolean select) {
      int event = 0;
      for(final ISpectrumData spec : specs)
         if(mSpectra.contains(spec) && select) {
            if(!mSelected.contains(spec)) {
               event = SELECTION_CHANGE;
               mSelected.add(spec);
            }
         } else if(mSelected.contains(spec)) {
            mSelected.remove(spec);
            event = SELECTION_CHANGE;
         }
      if(event != 0)
         fireActionListener(event);
      validate();
   }

   /**
    * Mark the specified spectrum as selected or unselected as specified by the
    * boolean argument <code>select</code>.
    * 
    * @param spec
    * @param select
    */
   public void select(ISpectrumData spec, boolean select) {
      assert mSpectra.contains(spec);
      int event = 0;
      if(mSpectra.contains(spec) && select) {
         if(!mSelected.contains(spec)) {
            mSelected.add(spec);
            event = SELECTION_CHANGE;
         }
      } else if(mSelected.contains(spec)) {
         mSelected.remove(spec);
         event = SELECTION_CHANGE;
      }
      if(event != 0)
         fireActionListener(event);
      validate();
   }

   /**
    * Remove all spectra from the selected list.
    */
   public void clearSelections() {
      if(!mSelected.isEmpty()) {
         mSelected.clear();
         fireActionListener(SELECTION_CHANGE);
      }
      validate();
   }

   /**
    * An event fired when someone updates the properties of a spectrum.
    */
   public void notifyUpdated() {
      fireActionListener(UPDATE_SPECTRUM);
   }

   /**
    * Is the specified spectrum in the selected set?
    * 
    * @param spec
    * @return boolean
    */
   public boolean isSelected(ISpectrumData spec) {
      return mSelected.contains(spec);
   }

   static public boolean isSet(int event, int flag) {
      return (event & flag) != 0;
   }

   /**
    * Returns the number of selected spectra.
    * 
    * @return int
    */
   public int getSelectedCount() {
      return mSelected.size();
   }

   /**
    * Permit registration of the Python interpreter with the DataManager so the
    * DataManager can register and unregister spectra with the interpreter.
    * 
    * @param interpreter
    */
   void setJythonWorker(JythonWorker worker) {
      mJython = worker;
   }

   /**
    * A string containing a row per spectrum with the Jython name and the
    * descriptive name.
    * 
    * @return String
    */
   public String listSpectra() {
      final StringBuffer res = new StringBuffer();
      final TreeMap<Integer, ISpectrumData> ordered = new TreeMap<Integer, ISpectrumData>();
      for(final Map.Entry<ISpectrumData, Integer> me : mSpectrumIndex.entrySet())
         ordered.put(me.getValue(), me.getKey());
      for(final Map.Entry<Integer, ISpectrumData> me : ordered.entrySet()) {
         if(res.length() > 0)
            res.append("\n");
         res.append("s");
         res.append(me.getKey().toString());
         res.append("\t");
         res.append(me.getValue().toString());
      }
      return res.toString();
   }

   public boolean isEmpty() {
      return mSpectra.isEmpty();
   }

   public static class TimestampSort
      implements
      Comparator<ISpectrumData> {

      final static Date DEFAULT = new Date(0);

      @Override
      public int compare(ISpectrumData arg0, ISpectrumData arg1) {
         final SpectrumProperties sp0 = arg0.getProperties();
         final SpectrumProperties sp1 = arg1.getProperties();
         final Date ts0 = sp0.getTimestampWithDefault(SpectrumProperties.AcquisitionTime, DEFAULT);
         final Date ts1 = sp1.getTimestampWithDefault(SpectrumProperties.AcquisitionTime, DEFAULT);
         return ts0.compareTo(ts1);
      }
   }

   public static class NameSort
      implements
      Comparator<ISpectrumData> {

      @Override
      public int compare(ISpectrumData arg0, ISpectrumData arg1) {
         return arg0.toString().compareTo(arg1.toString());
      }
   }

   public static class DetectorSort
      implements
      Comparator<ISpectrumData> {

      private final Comparator<ISpectrumData> mSecondary;

      public DetectorSort(Comparator<ISpectrumData> backup) {
         mSecondary = backup;
      }

      @Override
      public int compare(ISpectrumData arg0, ISpectrumData arg1) {
         final SpectrumProperties sp0 = arg0.getProperties();
         final SpectrumProperties sp1 = arg1.getProperties();
         final Object det0 = sp0.getObjectWithDefault(SpectrumProperties.Detector, null);
         final Object det1 = sp1.getObjectWithDefault(SpectrumProperties.Detector, null);
         final String name0 = det0 != null ? det0.toString() : "Unknown";
         final String name1 = det1 != null ? det1.toString() : "Unknown";
         final int res = name0.compareTo(name1);
         if((res == 0) && (mSecondary != null))
            return mSecondary.compare(arg0, arg1);
         return res;
      }
   }

   public void sortSpectra(Comparator<? super ISpectrumData> comp) {
      mSpectra.sort(comp);
      fireActionListener(REORDER_SPECTRA);
      validate();
   }

}
