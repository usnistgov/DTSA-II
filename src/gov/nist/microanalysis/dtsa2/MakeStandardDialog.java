package gov.nist.microanalysis.dtsa2;

import java.awt.Color;
import java.awt.Frame;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import gov.nist.microanalysis.EPQDatabase.Session;
import gov.nist.microanalysis.EPQLibrary.Composition;
import gov.nist.microanalysis.EPQLibrary.ConductiveCoating;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.EditableSpectrum;
import gov.nist.microanalysis.EPQLibrary.Element;
import gov.nist.microanalysis.EPQLibrary.FromSI;
import gov.nist.microanalysis.EPQLibrary.ISpectrumData;
import gov.nist.microanalysis.EPQLibrary.Material;
import gov.nist.microanalysis.EPQLibrary.MaterialFactory;
import gov.nist.microanalysis.EPQLibrary.RegionOfInterestSet;
import gov.nist.microanalysis.EPQLibrary.RegionOfInterestSet.RegionOfInterest;
import gov.nist.microanalysis.EPQLibrary.SampleShape;
import gov.nist.microanalysis.EPQLibrary.SampleShape.ThinFilm;
import gov.nist.microanalysis.EPQLibrary.SpectrumFitter8;
import gov.nist.microanalysis.EPQLibrary.SpectrumFitter8.AltEnergyScaleFunction;
import gov.nist.microanalysis.EPQLibrary.SpectrumFitter8.FanoNoiseWidth;
import gov.nist.microanalysis.EPQLibrary.SpectrumMath;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties.PropertyId;
import gov.nist.microanalysis.EPQLibrary.SpectrumSimulator.BasicSpectrumSimulator;
import gov.nist.microanalysis.EPQLibrary.SpectrumUtils;
import gov.nist.microanalysis.EPQLibrary.StandardBundle;
import gov.nist.microanalysis.EPQLibrary.ToSI;
import gov.nist.microanalysis.EPQLibrary.XRayTransitionSet;
import gov.nist.microanalysis.EPQLibrary.Detector.EDSDetector;
import gov.nist.microanalysis.EPQTools.ErrorDialog;
import gov.nist.microanalysis.EPQTools.JComboBoxCoating;
import gov.nist.microanalysis.EPQTools.JElementPanel;
import gov.nist.microanalysis.EPQTools.JProgressDialog;
import gov.nist.microanalysis.EPQTools.JTextFieldDouble;
import gov.nist.microanalysis.EPQTools.JWizardDialog;
import gov.nist.microanalysis.EPQTools.MaterialsCreator;
import gov.nist.microanalysis.EPQTools.SelectElements;
import gov.nist.microanalysis.EPQTools.SpectrumFileChooser;
import gov.nist.microanalysis.EPQTools.SpectrumPropertyPanel;
import gov.nist.microanalysis.Utility.HalfUpFormat;
import gov.nist.microanalysis.Utility.Math2;

/**
 * <p>
 * A dialog for facilitating the creation of standard spectra from a list of
 * individual spectra.
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
public class MakeStandardDialog extends JWizardDialog {

	private static final long serialVersionUID = -1921213224984265198L;

	public class DosePanel extends JWizardPanel {

		private static final long serialVersionUID = -6656187111184510436L;

		private final JTable jTable_Dose = new JTable();
		private final JTextField jTextField_ProbeCurrent = new JTextField();
		private final JTextField jTextField_LiveTime = new JTextField();
		private final JButton jButton_UpdatePC = new JButton("Update");
		private final JButton jButton_UpdateLT = new JButton("Update");

		public DosePanel(JWizardDialog wiz) {
			super(wiz);
			initialize();
		}

		private void initialize() {
			final FormLayout fl = new FormLayout("50dlu, 5dlu, pref, 25dlu, 50dlu, 5dlu, pref",
					"100dlu, 5dlu, pref, 5dlu, pref, 5dlu, pref");
			final PanelBuilder pb = new PanelBuilder(fl);
			final CellConstraints cc0 = new CellConstraints();
			pb.add(new JScrollPane(jTable_Dose), cc0.xyw(1, 1, 7));
			pb.addSeparator("Update selected spectra", CC.xyw(1, 3, 7));
			pb.addLabel("Probe Current", cc0.xyw(1, 5, 3));
			pb.addLabel("Live Time", cc0.xyw(5, 5, 3));
			pb.add(jTextField_ProbeCurrent, cc0.xy(1, 7));
			pb.add(jButton_UpdatePC, cc0.xy(3, 7));
			pb.add(jTextField_LiveTime, cc0.xy(5, 7));
			pb.add(jButton_UpdateLT, cc0.xy(7, 7));
			add(pb.getPanel());

			jButton_UpdatePC.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					int[] sels = jTable_Dose.getSelectedRows();
					double val = -1.0;
					try {
						val = Double.parseDouble(jTextField_ProbeCurrent.getText());
					} catch (NumberFormatException nfe) {
						MakeStandardDialog.this.setErrorText("Please specify a positive number.");
					}
					for (PropertyId pid : new PropertyId[] { //
							SpectrumProperties.FaradayBegin,
							SpectrumProperties.FaradayEnd }) {
						for (int sel : sels)
							if (val > 0.0)
								mSpectra.get(sel).getProperties().setNumericProperty(pid, val);
							else
								mSpectra.get(sel).getProperties().clear(pid);
					}
					update();
				}
			});
			jButton_UpdateLT.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					int[] sels = jTable_Dose.getSelectedRows();
					double val = -1.0;
					try {
						val = Double.parseDouble(jTextField_LiveTime.getText());
					} catch (NumberFormatException nfe) {
						MakeStandardDialog.this.setErrorText("Please specify a positive number.");
					}
					PropertyId pid = SpectrumProperties.LiveTime;
					for (int sel : sels)
						if (val > 0.0)
							mSpectra.get(sel).getProperties().setNumericProperty(pid, val);
						else
							mSpectra.get(sel).getProperties().clear(pid);
					update();
				}
			});

		}

		public void onShow() {
			update();
			setNextPanel(mStandardPanel, "Standard Properties");
			enableFinish(false);
		}

		public boolean permitNext() {
			boolean ok = checkDoses(mSpectra);
			if (ok)
				MakeStandardDialog.this.setMessageText("");
			else
				MakeStandardDialog.this.setErrorText("Please specify a probe current and live time for all spectra.");
			return ok;
		}

		private void update() {
			DefaultTableModel model = new DefaultTableModel(new Object[] { "Spectrum", "Probe Current", "Live Time" },
					0);
			for (ISpectrumData spec : mSpectra) {
				model.addRow(new Object[] { spec,
						spec.getProperties().getTextWithDefault(SpectrumProperties.FaradayBegin, "missing"),
						spec.getProperties().getTextWithDefault(SpectrumProperties.LiveTime, "missing") });
			}
			jTable_Dose.setModel(model);
		}
	}

	public static boolean checkDoses(Collection<ISpectrumData> specs) {
		for (ISpectrumData spec : specs) {
			SpectrumProperties sp = spec.getProperties();
			final double fb = sp.getNumericWithDefault(SpectrumProperties.FaradayBegin, -1.0);
			final double fe = sp.getNumericWithDefault(SpectrumProperties.FaradayEnd, fb);
			final double lt = sp.getNumericWithDefault(SpectrumProperties.LiveTime, -1.0);
			if ((fe < 0.0) || (lt < 0.0))
				return false;
		}
		return true;
	}

	public class StandardPanel extends JWizardPanel {
		private static final long serialVersionUID = 1L;

		private final JTextField jTextField_Material = new JTextField();
		private final JButton jButton_Material = new JButton("Edit");
		private final JElementPanel jPanel_Element = new JElementPanel();
		private final JTextField jTextField_BeamEnergy = new JTextField();
		private final JTextField jTextField_Dose = new JTextField();
		private final JTextField jTextField_Detector = new JTextField();
		private final JButton jButton_SpecProperties = new JButton("Properties");

		public StandardPanel(JWizardDialog wiz) {
			super(wiz);
			initialize();
		}

		public void onShow() {
			if (mSpectra.size() == 1)
				setMessageText("Creating a standard from a single spectrum.");
			else
				setMessageText("Creating a standard from " + Integer.valueOf(mSpectra.size()) + " spectra.");
			updateMaterialField(mMaterial);
			buildBundles();
			final DecimalFormat df = new HalfUpFormat("0.0");
			jTextField_BeamEnergy.setText(df.format(mBeamEnergy));
			jTextField_Dose.setText(df.format(getProbeDose()));
			jTextField_Detector.setText(mDetector != null ? mDetector.getName() : "--------");
			jTextField_Material.requestFocus();

			setNextPanel(mFilterPanel, "Select Spectra");
			enableFinish(false);
		}

		protected void updateMaterialField(final Composition res) {
			if (res != null) {
				if ((res != null) && (!mMaterial.getElementSet().equals(res.getElementSet()))) {
					jPanel_Element.setAvailableElements(res.getElementSet());
					jPanel_Element.setSelected(res.getElementSet());
				}
				jTextField_Material.setText(res.getElementCount() > 0 ? res.getName() : "");
				jTextField_Material.setBackground(SystemColor.text);
				if (res.getElementCount() < 1)
					jTextField_Material.setBackground(Color.pink);
				else
					jTextField_Material.setBackground(SystemColor.text);
				if (res.containsElement(Element.C)) {
					mSpecialPanel.jCheckBox_Carbon.setSelected(false);
					mSpecialPanel.jCheckBox_Carbon.setEnabled(false);
					removeStrip(Element.C);
				} else {
					mSpecialPanel.jCheckBox_Carbon.setEnabled(true);
					if (mSpecialPanel.jCheckBox_Carbon.isSelected())
						addStrip(Element.C);
				}
				if (res.containsElement(Element.O)) {
					mSpecialPanel.jCheckBox_Oxygen.setSelected(false);
					mSpecialPanel.jCheckBox_Oxygen.setEnabled(false);
					removeStrip(Element.O);
				} else {
					mSpecialPanel.jCheckBox_Oxygen.setEnabled(true);
					if (mSpecialPanel.jCheckBox_Oxygen.isSelected())
						addStrip(Element.O);
				}
				mMaterial = res;
			}
		}

		public void addStrip(final Element elm) {
			mStrip.add(elm);
			for (final StandardBundle sb : mBundle.values())
				sb.addStrip(elm);
			mReferencePanel.jTable_Refs.setModel(buildRefTable());
		}

		public void removeStrip(final Element elm) {
			mStrip.remove(elm);
			for (final StandardBundle sb : mBundle.values())
				sb.removeStrip(elm);
			mReferencePanel.jTable_Refs.setModel(buildRefTable());
		}

		protected void initialize() {
			final FormLayout fl = new FormLayout("right:pref, 5dlu, 100dlu, 5dlu, left:pref",
					"pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu");
			final PanelBuilder pb = new PanelBuilder(fl);
			final CellConstraints cc0 = new CellConstraints(), cc1 = new CellConstraints();
			int row = 1;
			pb.add(new JLabel("Material"), cc0.xy(1, row), jTextField_Material, cc1.xy(3, row));
			pb.add(jButton_Material, cc0.xy(5, row));
			row += 2;
			jPanel_Element.addElementChange(new ElementUpdateListener());
			pb.add(new JLabel("Elements"), cc0.xy(1, row), jPanel_Element, cc1.xy(3, row));
			row += 2;
			pb.add(new JLabel("Beam energy"), cc0.xy(1, row), jTextField_BeamEnergy, cc1.xy(3, row));
			pb.addLabel("keV", cc0.xy(5, row));
			row += 2;
			pb.add(new JLabel("Probe Dose"), cc0.xy(1, row), jTextField_Dose, cc1.xy(3, row));
			pb.addLabel("nA\u00B7s", cc0.xy(5, row));
			row += 2;
			pb.add(new JLabel("Detector"), cc0.xy(1, row), jTextField_Detector, cc1.xyw(3, row, 3));
			row += 2;
			pb.add(jButton_SpecProperties, cc0.xy(5, row));
			row += 2;
			pb.opaque(true);
			final JPanel res = pb.getPanel();
			res.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			jTextField_Material.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e) {
					final String name = jTextField_Material.getText();
					Composition res = null;
					if (mSession != null)
						try {
							res = mSession.findStandard(name);
						} catch (final SQLException e1) {
							// Ignore it...
						}
					if (res == null)
						try {
							res = MaterialFactory.createCompound(name);
						} catch (final EPQException e1) {
							// Ignore it..
						}
					if (res == null) {
						final Composition comp = new Composition();
						comp.setName(name);
						res = MaterialsCreator.editMaterial(MakeStandardDialog.this, comp, false);
					}
					if ((res == null) || (res.getElementCount() < 1))
						jTextField_Material.setBackground(Color.pink);
					else {
						res.setName(name);
						updateMaterialField(res);
					}
					buildBundles();
					enableNext(true);
				}

			});

			jButton_Material.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final Composition res = MaterialsCreator.editMaterial(MakeStandardDialog.this, mMaterial, mSession,
							false);
					if (res != null)
						updateMaterialField(res);
					buildBundles();
					enableNext(true);
				}
			});

			jButton_SpecProperties.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					if (MakeStandardDialog.this.getResult() != null) {
						final SpectrumProperties sp = MakeStandardDialog.this.getResult().getProperties();
						sp.addAll(editSpectrumProperties(sp));
						mBeamEnergy = sp.getNumericWithDefault(SpectrumProperties.BeamEnergy, mBeamEnergy);
						final DecimalFormat df = new HalfUpFormat("0.0");
						jTextField_BeamEnergy.setText(df.format(mBeamEnergy));
						jTextField_Dose.setText(df.format(getProbeDose()));
					}
				}
			});

			jTextField_BeamEnergy.setEditable(false);
			jTextField_Dose.setEditable(false);
			jTextField_Detector.setEditable(false);

			addInScrollPane(pb.getPanel());
		}

		public boolean permitNext() {
			return mMaterial.getElementCount() > 0;
		}

		private class ElementUpdateListener extends AbstractAction {

			private static final long serialVersionUID = -293259556758837707L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				buildBundles();
			}
		}

		public SpectrumProperties editSpectrumProperties(final SpectrumProperties sp) {
			final SpectrumPropertyPanel.PropertyDialog dlg = new SpectrumPropertyPanel.PropertyDialog(
					MakeStandardDialog.this, mSession);
			final SpectrumProperties.PropertyId[] required = new SpectrumProperties.PropertyId[] {
					SpectrumProperties.BeamEnergy, SpectrumProperties.FaradayBegin, SpectrumProperties.FaradayEnd,
					SpectrumProperties.LiveTime };
			dlg.setRequiredProperties(Arrays.asList(required));
			dlg.addSpectrumProperties(sp);
			dlg.setLocationRelativeTo(this);
			dlg.setVisible(true);
			return dlg.getSpectrumProperties();
		}

	}

	public class FilterPanel extends JWizardPanel {

		private static final long serialVersionUID = 766500480758803915L;
		private final JTable jFilterTable = new JTable();
		private final AbstractAction jRemove = new AbstractAction("Remove") {

			private static final long serialVersionUID = 5500175270249158790L;

			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i : jFilterTable.getSelectedRows())
					mSelected.set(i, Boolean.FALSE);
				updateSelected();
			}
		};

		private final AbstractAction jRestore = new AbstractAction("Restore") {

			private static final long serialVersionUID = -8522789583374061625L;

			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i : jFilterTable.getSelectedRows())
					mSelected.set(i, Boolean.TRUE);
				updateSelected();
			}
		};

		public FilterPanel(JWizardDialog wiz) {
			super(wiz);
			initialize();
		}

		private void initialize() {
			FormLayout fl = new FormLayout("5dlu, 250dlu, 5dlu, pref, 5dlu", "5dlu, pref, 5dlu, 120dlu, 5dlu");
			PanelBuilder pb = new PanelBuilder(fl);
			pb.addLabel("Spectra with low scores are more similar to the mean spectrum.", CC.xy(2, 2));
			pb.add(new JScrollPane(jFilterTable), CC.xy(2, 4));
			ButtonStackBuilder bsb = new ButtonStackBuilder() //
					.addButton(jRemove)//
					.addRelatedGap()//
					.addButton(jRestore);
			pb.add(bsb.build(), CC.xy(4, 4));
			jFilterTable.setToolTipText("If one scores are much larger than the result, it probably doesn't belong.\n"
					+ "Removing it will probably improve all the scores. Closer to 1 is better.");
			add(pb.build());
			jFilterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}

		public void onShow() {
			setMessageText("Select which spectra to include in the bundle.");
			setNextPanel(mSpecialPanel, "Special Options");
			updateSelected();
			enableFinish(false);
		}

		private void updateSelected() {
			DecimalFormat nf = new DecimalFormat("#.##");
			RegionOfInterestSet rois = computeROIS();
			DefaultTableModel model = new DefaultTableModel(new Object[] { "Spectrum", "Score", "Included" }, 0);
			ArrayList<ISpectrumData> sel = new ArrayList<>();
			for (int i = 0; i < mSpectra.size(); ++i)
				if (mSelected.get(i))
					sel.add(mSpectra.get(i));
			double[] scores = new double[mSpectra.size()];
			int selCount = 0;
			for (int i = 0; i < mSpectra.size(); ++i)
				if (mSelected.get(i)) {
					++selCount;
					scores[i] = score(mSpectra.get(i), sel, rois);
				}
			final String[] html = new String[scores.length];
			final double ss = Math2.sum(scores);
			for (int i = 0; i < scores.length; ++i) {
				if (scores[i] > 0.0) {
					final double sc = scores[i] / ((ss - scores[i]) / (selCount - 1));
					if (sc < 2.0)
						html[i] = "<html><font color=\"black\">" + nf.format(scores[i]) + "</font>";
					else if (sc < 5.0)
						html[i] = "<html><font color=\"orange\">" + nf.format(scores[i]) + "</font>";
					else
						html[i] = "<html><font color=\"red\">" + nf.format(scores[i]) + "</font>";
				} else
					html[i] = "N/A";
			}
			for (int i = 0; i < mSpectra.size(); ++i)
				model.addRow(new Object[] { //
						mSpectra.get(i).toString(), //
						html[i], //
						mSelected.get(i).toString() //
				});
			jFilterTable.setModel(model);
		}

		public boolean permitNext() {
			int i = 0;
			for (Boolean b : mSelected)
				if (b)
					++i;
			if (i == 0)
				setErrorText("Please include at least one spectrum.");
			else
				setMessageText(i == 1 ? "One spectrum." : "Summing " + i + " spectra.");
			return i > 0;
		}
	}

	public RegionOfInterestSet computeROIS() {
		RegionOfInterestSet rois = new RegionOfInterestSet(mDetector.getDetectorLineshapeModel(), 0.01);
		double e0 = Double.MAX_VALUE;
		for (ISpectrumData s : mSpectra)
			e0 = Math.min(e0, SpectrumUtils.getBeamEnergy(s));
		for (Element elm : mMaterial.getElementSet())
			rois.add(new XRayTransitionSet(elm, ToSI.eV(50.0), ToSI.eV(e0)));
		return rois;
	}

	public static double score(ISpectrumData spec, Collection<ISpectrumData> specs, RegionOfInterestSet rois) {
		ArrayList<ISpectrumData> cspecs = new ArrayList<>(specs);
		cspecs.remove(spec);
		try {
			return Math.sqrt(SpectrumUtils.measureDissimilarity(spec, cspecs, rois));
		} catch (EPQException e) {
			return Double.NaN;
		}
	}

	public class SpecialPanel extends JWizardPanel {
		private static final long serialVersionUID = 6638178754912473235L;

		private final JCheckBox jCheckBox_Film = new JCheckBox("Thin film standard");
		private final JTextFieldDouble jTextField_Thickness = new JTextFieldDouble(100.0, 1.0, 1.0e6, "#,##0", "Bulk");
		private final JCheckBox jCheckBox_Coating = new JCheckBox("Conductive coating");
		private final JTextFieldDouble jTextField_CoatingThickness = new JTextFieldDouble(10.0, 0.0, 1000.0, "#,##0.0",
				"None");
		private final JComboBoxCoating jComboBox_Coating = new JComboBoxCoating(this, DTSA2.getSession());

		private final JCheckBox jCheckBox_Carbon = new JCheckBox("Strip carbon contamination?");
		private final JCheckBox jCheckBox_Oxygen = new JCheckBox("Strip oxide layer or contamination?");

		private class CarbonCheckListener extends AbstractAction {

			private static final long serialVersionUID = -1654348459420442991L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (jCheckBox_Carbon.isSelected())
					mStandardPanel.addStrip(Element.C);
				else
					mStandardPanel.removeStrip(Element.C);
			}
		}

		private class OxygenCheckListener extends AbstractAction {

			private static final long serialVersionUID = 7269740692906409400L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (jCheckBox_Oxygen.isSelected())
					mStandardPanel.addStrip(Element.O);
				else
					mStandardPanel.removeStrip(Element.O);
			}
		}

		public SpecialPanel(JWizardDialog wiz) {
			super(wiz);
			initialize();
		}

		public void onShow() {

			jCheckBox_Film.setSelected(mThinFilmStandard);
			updateThicknessCheckBox();
			updateCoatingCheckBox();

			setNextPanel(mReferencePanel, "Specify References");
			enableFinish(true);
			mSave = true;
		}

		private void initialize() {

			final FormLayout fl = new FormLayout("right:pref, 5dlu, 100dlu, 5dlu, left:50dlu",
					"5dlu, pref, 5dlu, pref, 20dlu, pref, 5dlu, pref, 20dlu, pref, 5dlu, pref, 5dlu");
			PanelBuilder pb = new PanelBuilder(fl);
			final CellConstraints cc0 = new CellConstraints(), cc1 = new CellConstraints();
			int row = 2;
			pb.add(jCheckBox_Film, cc1.xyw(1, row, 3));
			row += 2;
			pb.add(new JLabel("Thickness"), cc0.xy(1, row), jTextField_Thickness, cc1.xy(3, row));
			pb.addLabel("nm", cc0.xy(5, row));
			row += 2;
			pb.add(jCheckBox_Coating, cc1.xyw(1, row, 3));
			row += 2;
			{
				final FormLayout flc = new FormLayout("60dlu, 5dlu, pref, 5dlu, left:80dlu", "pref");
				final PanelBuilder pbc = new PanelBuilder(flc);
				pbc.add(jTextField_CoatingThickness, cc0.xy(1, 1));
				pbc.addLabel("nm of ", cc0.xy(3, 1));
				pbc.add(jComboBox_Coating, cc0.xy(5, 1));
				pb.add(new JLabel("Thickness"), cc0.xy(1, row), pbc.getPanel(), cc1.xyw(3, row, 3));
			}
			row += 2;
			pb.add(jCheckBox_Carbon, CC.xyw(1, row, 3));
			row += 2;
			pb.add(jCheckBox_Oxygen, CC.xyw(1, row, 3));

			jCheckBox_Film.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					mThinFilmStandard = jCheckBox_Film.isSelected();
					updateThicknessCheckBox();
					buildBundles();
				}
			});

			jCheckBox_Coating.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					mCoating = jCheckBox_Coating.isSelected();
					updateCoatingCheckBox();
					buildBundles();
				}
			});

			jTextField_Thickness.addValueChange(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent arg0) {
					if (jTextField_Thickness.isAltValue())
						mThickness = Double.NaN;
					else
						mThickness = jTextField_Thickness.getValue() * 1.0e-9;
					buildBundles();
				}

			});

			jTextField_CoatingThickness.addValueChange(new AbstractAction() {

				private static final long serialVersionUID = -4454637546890089042L;

				@Override
				public void actionPerformed(final ActionEvent arg0) {
					if (jTextField_CoatingThickness.isAltValue())
						mCoatingThickness = Double.NaN;
					else
						mCoatingThickness = jTextField_CoatingThickness.getValue() * 1.0e-9;
					buildBundles();
				}
			});
			jCheckBox_Carbon.addActionListener(new CarbonCheckListener());
			jCheckBox_Oxygen.addActionListener(new OxygenCheckListener());

			add(pb.getPanel());
		}

		private void updateThicknessCheckBox() {
			if (mThinFilmStandard) {
				jTextField_Thickness.setEnabled(true);
				jTextField_Thickness.setValue(mThickness * 1.0e9);
				jTextField_Thickness.selectAll();
				jTextField_Thickness.requestFocus();
			} else {
				jTextField_Thickness.setValue(Double.NaN);
				jTextField_Thickness.setEnabled(false);
			}
		}

		private void updateCoatingCheckBox() {
			if (mCoating) {
				jCheckBox_Coating.setSelected(true);
				jTextField_CoatingThickness.setEnabled(true);
				if (Double.isNaN(mCoatingThickness))
					mCoatingThickness = 10.0e-9;
				jTextField_CoatingThickness.setValue(mCoatingThickness * 1.0e9);
				jTextField_CoatingThickness.selectAll();
				jTextField_CoatingThickness.requestFocus();
				jComboBox_Coating.setEnabled(true);
				if (mCoatingMaterial.equals(Material.Null))
					mCoatingMaterial = jComboBox_Coating.getItemAt(0);
				jComboBox_Coating.setSelectedItem(mCoatingMaterial);
			} else {
				jCheckBox_Coating.setSelected(false);
				mCoatingMaterial = Material.Null;
				mCoatingThickness = 0.0;
				jTextField_CoatingThickness.setValue(Double.NaN);
				jTextField_CoatingThickness.setEnabled(false);
				jComboBox_Coating.setSelectedItem(mCoatingMaterial);
				jComboBox_Coating.setEnabled(false);
			}
		}
	}

	public class ReferencePanel extends JWizardPanel {
		private static final long serialVersionUID = 7874679381844948698L;

		private final JLabel jLabel_Refs = new JLabel("XXX");
		private final JTable jTable_Refs = new JTable();
		private final JButton jButton_Auto = new JButton("Auto");
		private final JButton jButton_Read = new JButton("Select");
		private final JButton jButton_Clear = new JButton("Clear");

		public ReferencePanel(JWizardDialog wiz) {
			super(wiz);
			initialize();
		}

		private void initialize() {
			final FormLayout fl = new FormLayout("5dlu, 250dlu, 5dlu, pref, 5dlu", "5dlu, pref, 5dlu, 120dlu, 5dlu");
			final PanelBuilder pb = new PanelBuilder(fl);
			pb.add(jLabel_Refs, CC.xy(2, 2));
			pb.add(new JScrollPane(jTable_Refs), CC.xy(2, 4));
			final ButtonStackBuilder bbb = new ButtonStackBuilder();
			bbb.addButton(jButton_Read);
			bbb.addButton(jButton_Clear);
			bbb.addUnrelatedGap();
			bbb.addButton(jButton_Auto);
			pb.add(bbb.build(), CC.xywh(4, 4, 1, 1));
			pb.opaque(true);

			jButton_Read.addActionListener(new readReferenceListener());
			jButton_Clear.addActionListener(new clearReferenceListener());
			jButton_Auto.addActionListener(new autoReferenceListener());
			add(pb.build());
		}

		public void onShow() {
			if (jTable_Refs.getRowCount() == 0) {
				jLabel_Refs.setText("<html><b>No references are required!</b>");
				jTable_Refs.setVisible(false);
				jButton_Auto.setVisible(false);
				jButton_Read.setVisible(false);
				jButton_Clear.setVisible(false);
			} else {
				jLabel_Refs.setText("Please provide references for " + jTable_Refs.getRowCount() + " peak regions.");
				jTable_Refs.setVisible(true);
				jButton_Auto.setVisible(true);
				jButton_Read.setVisible(true);
				jButton_Clear.setVisible(true);
				setMessageText("Including references now will save effort later.");
			}
		}

		public boolean permitNext() {
			if (jTable_Refs.getRowCount() > 0) {
				int missing = 0;
				for (StandardBundle sb : mBundle.values())
					for (RegionOfInterest roi : sb.getAllRequiredReferences())
						if (sb.getReference(roi) == null)
							++missing;
				if (missing > 0)
					return JOptionPane.showConfirmDialog(MakeStandardDialog.this,
							"Are you sure you want to create a bundle with missing references?", "Create Bundle",
							JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
			}
			return true;
		}

		private class readReferenceListener extends AbstractAction {

			private static final long serialVersionUID = -70114861736447514L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (jTable_Refs.getSelectedRowCount() == 0) {
					JOptionPane.showMessageDialog(MakeStandardDialog.this, "Please select one or more peak to assign.",
							"Select", JOptionPane.WARNING_MESSAGE);
					return;
				}
				final int[] rows = jTable_Refs.getSelectedRows();
				if (rows.length > 0) {
					final SpectrumFileChooser sfc = new SpectrumFileChooser(MakeStandardDialog.this,
							"Open spectrum files...");
					final File dir = new File(DTSA2.getSpectrumDirectory());
					sfc.getFileChooser().setCurrentDirectory(dir);
					sfc.setLocationRelativeTo(MakeStandardDialog.this);
					sfc.setMultiSelectionEnabled(false);
					final int res = sfc.showOpenDialog();
					if (res == JFileChooser.APPROVE_OPTION) {
						DTSA2.updateSpectrumDirectory(sfc.getFileChooser().getCurrentDirectory());
						final ISpectrumData spec = sfc.getSpectra()[0];
						final Set<Element> specElms = spec.getProperties().getElements();
						if ((specElms == null) || specElms.isEmpty()) {
							final Set<Element> selectedElms = SelectElements.selectElements(MakeStandardDialog.this,
									"Specify the elements contained in the reference spectrum", Element.allElements(),
									getElementsForRows(rows));
							spec.getProperties().setElements(selectedElms);
						}
						spec.getProperties().setDetector(mDetector);
						for (final int row : rows)
							assignReference((RegionOfInterest) jTable_Refs.getModel().getValueAt(row, 0), spec);
					}
					jTable_Refs.setModel(buildRefTable());
				}
			}
		}

		private Set<Element> getElementsForRows(final int[] rows) {
			final Set<Element> res = new TreeSet<Element>();
			for (final int row : rows)
				res.addAll(((RegionOfInterest) jTable_Refs.getModel().getValueAt(row, 0)).getElementSet());
			return res;

		}

		private class clearReferenceListener extends AbstractAction {

			private static final long serialVersionUID = 3445601205093883019L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (jTable_Refs.getSelectedRowCount() == 0) {
					JOptionPane.showMessageDialog(MakeStandardDialog.this, "Please select one or more peak to clear.",
							"Clear", JOptionPane.WARNING_MESSAGE);
					return;
				}
				final int[] rows = jTable_Refs.getSelectedRows();
				for (final int row : rows) {
					final RegionOfInterest roi = (RegionOfInterest) jTable_Refs.getModel().getValueAt(row, 0);
					for (final StandardBundle sb : mBundle.values())
						sb.clearReference(roi);
				}
				jTable_Refs.setModel(buildRefTable());
			}
		}

		private class autoReferenceListener extends AbstractAction {

			private static final long serialVersionUID = 2394910493261610093L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					if (jTable_Refs.getSelectedRowCount() == 0) {
						JOptionPane.showMessageDialog(MakeStandardDialog.this,
								"Please select one or more peak to auto-fit.", "Auto-Fit", JOptionPane.WARNING_MESSAGE);
						return;
					} else {
						JOptionPane.showMessageDialog(MakeStandardDialog.this,
								"Please use auto-fit references only when a reference can't be measured.\n"
										+ "The quant results are unlikely to be as reliable.  This is particularly\n"
										+ "true when there are challenging interferences in the standard or unknown.",
								"Auto-Fit", JOptionPane.WARNING_MESSAGE);
					}
					final Composition comp = mMaterial.clone();
					if (mSpecialPanel.jCheckBox_Carbon.isSelected())
						comp.addElement(Element.C, 0.001);
					if (mSpecialPanel.jCheckBox_Oxygen.isSelected())
						comp.addElement(Element.O, 0.001);
					mResult = null;
					final ISpectrumData fitSpec = buildResult();
					final SpectrumFitter8 sf = new SpectrumFitter8(mDetector, comp, fitSpec);
					final RegionOfInterestSet rois = sf.getROIS();
					final SpectrumProperties props = mDetector.getCalibration().getProperties();
					// If there is an extended range of energies with
					// characteristic
					// peaks, we should increase the number of fit parameters.
					final double[] coeffs = new double[] {
							props.getNumericWithDefault(SpectrumProperties.EnergyOffset, 0.0),
							props.getNumericWithDefault(SpectrumProperties.EnergyScale, 10.0) };
					sf.setEnergyScale(new AltEnergyScaleFunction(coeffs));
					sf.setResolution(new FanoNoiseWidth(6.0));
					sf.setMultiLineset(sf.buildWeighted(rois));
					{
						final JProgressDialog prg = new JProgressDialog(MakeStandardDialog.this,
								"Performing auto-reference fit");
						prg.perform(new Runnable() {
							@Override
							public void run() {
								try {
									prg.setProgress(5);
									sf.compute();
									for (int i = 0; i < 4; ++i) {
										prg.setProgress(20 + (i * 20));
										sf.recompute(10.0, 0.3);
									}
									prg.setProgress(100);
								} catch (final Exception e) {
									ErrorDialog.createErrorMessage(MakeStandardDialog.this, "Auto Reference Failed", e);
								}
							}
						});
					}
					mBestFit = sf.getBestFit();
					mRough = sf.getRoughFit();
					mResult = fitSpec;
					final BasicSpectrumSimulator bss = new BasicSpectrumSimulator();
					for (final StandardBundle sb : mBundle.values())
						sb.updateStandard(mResult);
					ArrayList<String> auto = new ArrayList<>();
					for (int row : jTable_Refs.getSelectedRows()) {
						final RegionOfInterest roi = (RegionOfInterest) jTable_Refs.getModel().getValueAt(row, 0);
						final Element elm = roi.getElementSet().first();
						final EditableSpectrum ref = new EditableSpectrum(sf.getElementSpectrum(elm));
						final ISpectrumData brem = bss.generateSpectrum(new Composition(elm), fitSpec.getProperties(),
								Collections.emptySet(), true);
						Math2.addInPlace(ref.getCounts(),
								Math2.multiply(comp.weightFraction(elm, true), SpectrumUtils.toDoubleArray(brem)));
						ref.getProperties().clear(SpectrumProperties.StandardComposition,
								SpectrumProperties.MicroanalyticalComposition);
						ref.getProperties().setElements(roi.getElementSet());
						SpectrumUtils.rename(ref, "Auto[" + roi + "]");
						auto.add(roi.toString());
						assignReference(roi, SpectrumUtils.copy(ref));
					}
					jTable_Refs.setModel(buildRefTable());
					setMessageText(String.join(", ", auto) + " assigned to auto-references.");
				} catch (final Throwable th) {
					ErrorDialog.createErrorMessage(MakeStandardDialog.this, "Error computing best fit.", th);
				}
			}
		}
	}

	private final DosePanel mDosePanel = new DosePanel(this);
	private final StandardPanel mStandardPanel = new StandardPanel(this);
	private final FilterPanel mFilterPanel = new FilterPanel(this);
	private final SpecialPanel mSpecialPanel = new SpecialPanel(this);
	private final ReferencePanel mReferencePanel = new ReferencePanel(this);

	private final Session mSession;
	private Composition mMaterial = new Composition();
	private final ArrayList<ISpectrumData> mSpectra = new ArrayList<ISpectrumData>();
	private final ArrayList<Boolean> mSelected = new ArrayList<Boolean>();
	private double mBeamEnergy = Double.NaN;
	private boolean mThinFilmStandard = false;
	private boolean mCoating = false;
	private double mThickness = Double.NaN;
	private double mCoatingThickness = Double.NaN;
	private Material mCoatingMaterial = Material.Null;
	private EDSDetector mDetector = null;
	private ISpectrumData mResult = null;
	private ISpectrumData mBestFit = null;
	private ISpectrumData mRough = null;
	private boolean mSave = false;

	private final Map<Element, StandardBundle> mBundle = new TreeMap<Element, StandardBundle>();
	private final Set<RegionOfInterest> mROI = new HashSet<RegionOfInterest>();
	private final Set<Element> mStrip = new TreeSet<Element>();

	public MakeStandardDialog(final Frame frame, final Session session) {
		super(frame, "Build standard bundle", true);
		mSession = session;
		initialize();
		pack();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	private void initialize() {
		setActivePanel(mDosePanel, "Standard Dose");
		enableFinish(false);
	}

	public void addSpectrum(final ISpectrumData spec) throws EPQException {
		final SpectrumProperties sp = spec.getProperties();
		if (mDetector == null)
			mDetector = (EDSDetector) sp.getDetector();
		else if (!mDetector.equals(sp.getDetector()))
			throw new EPQException("The detector for " + spec.toString() + " does not match the previous spectra.");
		double e0 = sp.getNumericWithDefault(SpectrumProperties.BeamEnergy, Double.NaN);
		e0 = sp.getNumericWithDefault(SpectrumProperties.BeamEnergy, Double.NaN);
		if (Double.isNaN(e0))
			throw new EPQException("Please provide the beam energy for each spectrum.");
		if (mSpectra.size() == 0) {
			mBeamEnergy = e0;
			mThickness = getThickness(sp);
			mSpectra.add(spec);
		} else {
			if (Math.abs(mBeamEnergy - e0) > (0.001 * mBeamEnergy))
				throw new EPQException(
						"The beam energy for " + spec.toString() + " does not match the previous spectra.");
			mSpectra.add(spec);
		}
		mSelected.add(Boolean.TRUE);
		if (mMaterial.getElementCount() == 0)
			mStandardPanel.updateMaterialField(
					sp.getCompositionWithDefault(SpectrumProperties.StandardComposition, mMaterial));
		else if (!sp.getCompositionWithDefault(SpectrumProperties.StandardComposition, mMaterial)
				.almostEquals(mMaterial, 1.0e-5))
			throw new EPQException("The material associated with " + spec
					+ " does \nnot match the material associated with previous spectra.");
		final ConductiveCoating cc = (ConductiveCoating) sp.getObjectWithDefault(SpectrumProperties.ConductiveCoating,
				null);
		if (cc != null) {
			mCoating = true;
			mCoatingThickness = cc.getThickness();
			mCoatingMaterial = cc.getMaterial();
		} else {
			mCoatingThickness = 0.0;
			mCoatingMaterial = Material.Null;
		}
		mStandardPanel.jPanel_Element.setAvailableElements(mMaterial.getElementSet());
		setActivePanel(mDosePanel, "Standard Dose");
	}

	private double getProbeDose() {
		double dose = 0.0;
		try {
			for (ISpectrumData spec : getSelected())
				dose += SpectrumUtils.getDose(spec.getProperties());
		} catch (EPQException e1) {
			e1.printStackTrace();
		}
		return dose;
	}

	private double getThickness(final SpectrumProperties props) {
		if (props.isDefined(SpectrumProperties.SampleShape)) {
			final SampleShape ss = props.getSampleShapeWithDefault(SpectrumProperties.SampleShape, null);
			if (ss instanceof SampleShape.ThinFilm)
				return ((SampleShape.ThinFilm) ss).getThickness();
		}
		return Double.NaN;
	}

	protected void buildBundles() {
		final ISpectrumData spec = buildResult();
		try {
			mBundle.clear();
			final Set<Element> selected = mStandardPanel.jPanel_Element.getSelected();
			for (final Element elm : selected) {
				final Set<Element> strip = new TreeSet<Element>();
				if (mSpecialPanel.jCheckBox_Carbon.isSelected())
					strip.add(Element.C);
				if (mSpecialPanel.jCheckBox_Oxygen.isSelected())
					strip.add(Element.O);
				final StandardBundle sb = new StandardBundle(elm, spec, strip);
				if (mSpecialPanel.jCheckBox_Coating.isSelected() && (mCoatingMaterial != null)
						&& (mCoatingMaterial.getElementCount() > 0) && (mCoatingThickness > 0.0)) {
					final ConductiveCoating cc = new ConductiveCoating(mCoatingMaterial, mCoatingThickness);
					sb.addCoating(cc);
				}
				mBundle.put(elm, sb);
				mROI.addAll(sb.getAllRequiredReferences());
			}
			mReferencePanel.jTable_Refs.setModel(buildRefTable());
		} catch (final EPQException e) {
			setErrorText(e.toString());
		}
	}

	private final class ROICompare implements Comparator<RegionOfInterest> {

		@Override
		public int compare(RegionOfInterest r1, RegionOfInterest r2) {
			final int z1 = r1.getElementSet().first().getAtomicNumber();
			final int z2 = r2.getElementSet().first().getAtomicNumber();
			if (z1 == z2)
				try {
					return Double.compare(r1.getAllTransitions().getWeighiestTransition().getEnergy(),
							r2.getAllTransitions().getWeighiestTransition().getEnergy());
				} catch (EPQException e) {
					return 0;
				}
			else
				return Integer.compare(z1, z2);
		}

	}

	private TableModel buildRefTable() {
		final HashMap<RegionOfInterest, ISpectrumData> rois = getAllROIS();
		final TreeSet<RegionOfInterest> sorted = new TreeSet<RegionOfInterest>(new ROICompare());
		sorted.addAll(rois.keySet());
		final Object[][] data = new Object[rois.size()][2];
		int i = 0;
		for (final RegionOfInterest roi : sorted) {
			data[i][0] = roi;
			final ISpectrumData spec = rois.get(roi);
			data[i][1] = (spec == null ? "None" : spec.toString());
			++i;
		}
		final DefaultTableModel res = new DefaultTableModel(data,
				new String[] { "Region-of-Interest", "Reference Spectrum" });
		return res;
	}

	private void assignReference(final RegionOfInterest roi, final ISpectrumData spec) {
		for (final StandardBundle sb : mBundle.values()) {
			final Set<RegionOfInterest> reqRefs = sb.getAllRequiredReferences();
			if (reqRefs.contains(roi))
				try {
					if (!sb.isSuitableAsReference(roi, spec)) {
						sb.isSuitableAsReference(roi, spec);
						if (JOptionPane.showConfirmDialog(MakeStandardDialog.this,
								"This reference does not appear to be suitable as a reference\n" + //
										" for " + roi + ".  Are you sure you want to use it?",
								"Assign reference", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION)
							return;
					}
					sb.addReference(roi, SpectrumUtils.copy(spec));
				} catch (final EPQException e) {
					ErrorDialog.createErrorMessage(this, "Reference error", e);
				}
		}
		mReferencePanel.jTable_Refs.setModel(buildRefTable());

	}

	protected HashMap<RegionOfInterest, ISpectrumData> getAllROIS() {
		final HashMap<RegionOfInterest, ISpectrumData> rois = new HashMap<RegionOfInterest, ISpectrumData>();
		for (final StandardBundle sb : mBundle.values()) {
			final Set<RegionOfInterest> reqRefs = sb.getAllRequiredReferences();
			for (final RegionOfInterest roi : reqRefs)
				if (!rois.containsKey(roi))
					rois.put(roi, sb.getReference(roi));
				else if (rois.get(roi) == null)
					rois.put(roi, sb.getReference(roi));
		}
		return rois;
	}

	public void clearSpectra() {
		mBeamEnergy = Double.NaN;
		mSpectra.clear();
	}

	public ISpectrumData getResult() {
		mResult = buildResult();
		return mResult;
	}

	private ISpectrumData buildResult() {
		ISpectrumData res = mResult;
		if (res == null)
			if ((mSpectra.size() > 0) && (mMaterial.getElementCount() > 0)) {
				SpectrumMath sm = null;
				double lt = 0.0, rt = 0.0;
				Date last = null;
				for (int i = 0; i < mSpectra.size(); ++i) {
					if (mSelected.get(i)) {
						final ISpectrumData spec = mSpectra.get(i);
						lt += spec.getProperties().getNumericWithDefault(SpectrumProperties.LiveTime, Double.NaN);
						rt += spec.getProperties().getNumericWithDefault(SpectrumProperties.RealTime, Double.NaN);
						assert !(Double.isNaN(lt) || Double.isNaN(rt));
						final Date dt = spec.getProperties().getTimestampWithDefault(SpectrumProperties.AcquisitionTime,
								null);
						if (dt != null)
							if (last == null)
								last = dt;
							else if (dt.after(last))
								last = dt;
						if (sm == null)
							sm = new SpectrumMath(spec);
						else
							sm.add(spec, 1.0);
					}
				}
				// Set the probe current to the average but keep the dose correct
				final SpectrumProperties sp = sm.getProperties();
				sp.setDetector(mDetector);
				if (last != null)
					sp.setTimestampProperty(SpectrumProperties.AcquisitionTime, last);
				sp.setNumericProperty(SpectrumProperties.BeamEnergy, mBeamEnergy);
				sp.setCompositionProperty(SpectrumProperties.StandardComposition, mMaterial);
				final double pd = getProbeDose();
				sp.setNumericProperty(SpectrumProperties.FaradayBegin, pd / lt);
				sp.setNumericProperty(SpectrumProperties.FaradayEnd, pd / lt);
				sp.setNumericProperty(SpectrumProperties.LiveTime, lt);
				sp.setNumericProperty(SpectrumProperties.RealTime, rt);
				if (mThinFilmStandard && (!Double.isNaN(mThickness))) {
					final ThinFilm tf = new ThinFilm(Math2.MINUS_Z_AXIS, mThickness);
					sp.setSampleShape(SpectrumProperties.SampleShape, tf);
					if (mMaterial instanceof Material)
						sp.setNumericProperty(SpectrumProperties.MassThickness,
								FromSI.cm(mThickness) * FromSI.gPerCC(((Material) mMaterial).getDensity()) * 1.0e6); // &mu;g/cm<sup>2</sup>
				} else
					sp.setSampleShape(SpectrumProperties.SampleShape, new SampleShape.Bulk());
				if (mSpecialPanel.jCheckBox_Coating.isSelected()) {
					if ((mCoatingMaterial != null) && (!mCoatingMaterial.equals(Material.Null))
							&& (mCoatingThickness > 0.0))
						sp.setConductiveCoating(new ConductiveCoating(mCoatingMaterial, mCoatingThickness));
					else
						sp.remove(SpectrumProperties.ConductiveCoating);
				} else
					sp.remove(SpectrumProperties.ConductiveCoating);
				SpectrumUtils.rename(sm, mMaterial.getName() + " std");
				res = SpectrumUtils.copy(sm);
			}
		return res;
	}

	public List<ISpectrumData> getSpectra() {
		return Collections.unmodifiableList(mSpectra);
	}

	public List<ISpectrumData> getSelected() {
		ArrayList<ISpectrumData> res = new ArrayList<ISpectrumData>();
		for (int i = 0; i < mSelected.size(); ++i)
			if (mSelected.get(i))
				res.add(mSpectra.get(i));
		return res;
	}

	public ISpectrumData getBestFit() {
		return mBestFit;
	}

	public boolean shouldSave() {
		return mSave;
	}

	/**
	 * Returns a map assigning StandardBundle objects to elements.
	 *
	 * @return Returns the bundle.
	 */
	public Map<Element, StandardBundle> getBundle() {
		return mThinFilmStandard ? Collections.emptyMap() : Collections.unmodifiableMap(mBundle);
	}

	/**
	 * Gets the current value assigned to rough
	 *
	 * @return Returns the rough.
	 */
	public ISpectrumData getRough() {
		return mRough;
	}
}
