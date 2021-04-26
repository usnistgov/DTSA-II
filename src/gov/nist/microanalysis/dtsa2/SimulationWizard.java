package gov.nist.microanalysis.dtsa2;

import java.awt.Color;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import gov.nist.microanalysis.EPQDatabase.Session;
import gov.nist.microanalysis.EPQLibrary.Composition;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.ElectronRange;
import gov.nist.microanalysis.EPQLibrary.Element;
import gov.nist.microanalysis.EPQLibrary.FromSI;
import gov.nist.microanalysis.EPQLibrary.Gas;
import gov.nist.microanalysis.EPQLibrary.ISpectrumData;
import gov.nist.microanalysis.EPQLibrary.Material;
import gov.nist.microanalysis.EPQLibrary.MaterialFactory;
import gov.nist.microanalysis.EPQLibrary.PhysicalConstants;
import gov.nist.microanalysis.EPQLibrary.SampleShape;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;
import gov.nist.microanalysis.EPQLibrary.SpectrumSimulator;
import gov.nist.microanalysis.EPQLibrary.SpectrumUtils;
import gov.nist.microanalysis.EPQLibrary.ToSI;
import gov.nist.microanalysis.EPQLibrary.XRayTransition;
import gov.nist.microanalysis.EPQLibrary.XRayTransitionSet;
import gov.nist.microanalysis.EPQLibrary.Detector.DetectorCalibration;
import gov.nist.microanalysis.EPQLibrary.Detector.DetectorProperties;
import gov.nist.microanalysis.EPQLibrary.Detector.EDSCalibration;
import gov.nist.microanalysis.EPQLibrary.Detector.EDSDetector;
import gov.nist.microanalysis.EPQLibrary.Detector.ElectronProbe;
import gov.nist.microanalysis.EPQTools.EPQXStream;
import gov.nist.microanalysis.EPQTools.ErrorDialog;
import gov.nist.microanalysis.EPQTools.JWizardDialog;
import gov.nist.microanalysis.EPQTools.JWizardProgressPanel;
import gov.nist.microanalysis.EPQTools.MaterialsCreator;
import gov.nist.microanalysis.EPQTools.WriteSpectrumAsEMSA1_0;
import gov.nist.microanalysis.NISTMonte.AnnularDetector;
import gov.nist.microanalysis.NISTMonte.BasicMaterialModel;
import gov.nist.microanalysis.NISTMonte.CylindricalShape;
import gov.nist.microanalysis.NISTMonte.GaussianBeam;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.Region;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.Shape;
import gov.nist.microanalysis.NISTMonte.MultiPlaneShape;
import gov.nist.microanalysis.NISTMonte.OverscanElectronGun;
import gov.nist.microanalysis.NISTMonte.ShapeDifference;
import gov.nist.microanalysis.NISTMonte.Sphere;
import gov.nist.microanalysis.NISTMonte.TrajectoryImage;
import gov.nist.microanalysis.NISTMonte.TrajectoryVRML;
import gov.nist.microanalysis.NISTMonte.Gen3.BremsstrahlungXRayGeneration3;
import gov.nist.microanalysis.NISTMonte.Gen3.CharacteristicXRayGeneration3;
import gov.nist.microanalysis.NISTMonte.Gen3.ComptonXRayGeneration3;
import gov.nist.microanalysis.NISTMonte.Gen3.EmissionImage3;
import gov.nist.microanalysis.NISTMonte.Gen3.EmissionImageBase;
import gov.nist.microanalysis.NISTMonte.Gen3.FluorescenceXRayGeneration3;
import gov.nist.microanalysis.NISTMonte.Gen3.VoxelatedDetector;
import gov.nist.microanalysis.NISTMonte.Gen3.VoxelatedDetector.AtomicShellType;
import gov.nist.microanalysis.NISTMonte.Gen3.XRayAccumulator3;
import gov.nist.microanalysis.NISTMonte.Gen3.XRayTransport3;
import gov.nist.microanalysis.Utility.HalfUpFormat;
import gov.nist.microanalysis.Utility.Math2;
import gov.nist.microanalysis.Utility.Transform3D;

/**
 * <p>
 * This class implements a wizard for configuring spectrum simulation
 * calculations.
 * </p>
 * <p>
 * Copyright: Pursuant to title 17 Section 105 of the United States Code this
 * software is not subject to copyright protection and is in the public domain
 * </p>
 * <p>
 * Institution: National Institute of Standards and Technology
 * </p>
 * 
 * @author Nicholas @version 1.0
 */
public class SimulationWizard extends JWizardDialog {

	static private final long serialVersionUID = 0x44;

	enum SimulationMode {
		AnalyticalBulk, //
		MCBulk, //
		MCFilm, //
		MCSphere, //
		MCCube, //
		MCInclusion, //
		MCInterface, //
		MCSquarePyramid, //
		MCCylinderOnSide, //
		MCCylinderOnEnd, //
		MCHemisphere, //
		MCRectangularPrism, //
		MCTriangularPrism, //
	};

	enum VPGas {
		HELIUM("Helium") {
			@Override
			public Gas toGas(double pressure) {
				return new Gas(new Element[] { Element.He }, new int[] { 1 }, pressure, 300.0, "Helium");
			}
		},
		OXYGEN("Oxygen") {
			@Override
			public Gas toGas(double pressure) {
				return new Gas(new Element[] { Element.O }, new int[] { 2 }, pressure, 300.0, "Oxygen");
			}
		},
		NITROGEN("Nitrogen") {
			@Override
			public Gas toGas(double pressure) {
				return new Gas(new Element[] { Element.N }, new int[] { 2 }, pressure, 300.0, "Nitrogen");
			}
		},
		ARGON("Argon") {
			@Override
			public Gas toGas(double pressure) {
				return new Gas(new Element[] { Element.Ar }, new int[] { 1 }, pressure, 300.0, "Argon");
			}
		},
		WATER("Water vapor") {
			@Override
			public Gas toGas(double pressure) {
				return new Gas(new Element[] { Element.H, Element.O }, new int[] { 2, 1 }, pressure, 300.0,
						"water vapor");
			}
		};

		private final String mName;

		private VPGas(String name) {
			mName = name;
		}

		@Override
		public String toString() {
			return mName;
		}

		abstract public Gas toGas(double pressure);

	}

	private final SimulationThread mThread = new SimulationThread();
	private final SimMode mSimMode = new SimMode(this);
	private final SimGeometry mGeometry = new SimGeometry(this);
	private final SimConfiguration mConfig = new SimConfiguration(this);
	private final SimOther mOther = new SimOther(this);
	private final SimVP mVariablePressure = new SimVP(this);
	private final JWizardProgressPanel<ISpectrumData[]> mProgress = new JWizardProgressPanel<ISpectrumData[]>(this,
			"Computing the requested spectra...", mThread);
	private Session mSession;

	private class SimulationThread extends SwingWorker<ISpectrumData[], Integer> {
		private SimulationMode mMode;
		private Material mObjectMaterial;
		private Material mSubstrateMaterial;
		private EDSDetector mDetector;
		private double mScale;
		private double mScale2 = 1.0e-6;
		private double mRotate = 0.0;
		private double mIncidentAngle = 0.0;
		private double mBeamEnergy;
		private double mProbeDose;
		private int mReplicas;
		private File mVrmlFile;
		private boolean mGenImages;
		private int mExtraElectrons;
		private boolean mOverscan;
		private boolean mCharacteristic;
		private boolean mBremsstrahlung;
		private boolean mCharFluor;
		private boolean mBremFluor;
		// Version 3 Simulation
		private XRayAccumulator3 mXRayAccumulator3;
		private XRayAccumulator3 mXRayAccumulator3BF;
		private XRayAccumulator3 mXRayAccumulator3CF;
		private ArrayList<EmissionImage3> mEmissionImages3;
		private VoxelatedDetector mVoxelatedDetector;
		private final AnnularDetector[] mAnnularDetector = new AnnularDetector[3];
		// Both Version 2 and 3
		private TreeMap<String, String> mEmissionFiles;
		// Variable pressure
		private boolean mEnableVP;
		private double mVPPathLength; // in meters
		private double mVPPressure; // in pascal
		private VPGas mVPGas;

		public String asHTML() {
			final StringBuffer sb = new StringBuffer();
			final NumberFormat nf3 = new HalfUpFormat("0.000");
			final NumberFormat nf6 = new HalfUpFormat("0.000000");
			final NumberFormat nf1 = new HalfUpFormat("0.0");
			sb.append("<h2>Simulation Configuration</h2>");
			sb.append("<p><table>");
			switch (mMode) {
			case AnalyticalBulk:
				sb.append("<tr><th align=right>Simulation mode</th><td>Analytical model of a bulk sample</td></tr>");
				break;
			case MCBulk:
				sb.append("<tr><th align=right>Simulation mode</th><td>Monte Carlo model of a bulk sample</td></tr>");
				break;
			case MCCube:
				sb.append("<tr><th align=right>Simulation mode</th><td>Monte Carlo model of a cube sample</td></tr>");
				break;
			case MCFilm:
				sb.append(
						"<tr><th align=right>Simulation mode</th><td>Monte Carlo model of a thin film sample</td></tr>");
				break;
			case MCInclusion:
				sb.append("<tr><th align=right>Simulation mode</th><td>Monte Carlo model of an inclusion</td></tr>");
				break;
			case MCSphere:
				sb.append(
						"<tr><th align=right>Simulation mode</th><td>Monte Carlo model of a spherical sample</td></tr>");
				break;
			case MCInterface:
				sb.append(
						"<tr><th align=right>Simulation mode</th><td>Monte Carlo model of a junction between two materials</td></tr>");
			case MCCylinderOnSide:
				sb.append(
						"<tr><th align=right>Simulation mode</th><td>Monte Carlo model of a cylinder on its side</td></tr>");
				break;
			case MCCylinderOnEnd:
				sb.append(
						"<tr><th align=right>Simulation mode</th><td>Monte Carlo model of a cylinder on its base</td></tr>");
				break;
			case MCHemisphere:
				sb.append(
						"<tr><th align=right>Simulation mode</th><td>Monte Carlo model of a hemispherical cap</td></tr>");
				break;
			case MCRectangularPrism:
				sb.append(
						"<tr><th align=right>Simulation mode</th><td>Monte Carlo model of a rectangular block</td></tr>");
				break;
			case MCSquarePyramid:
				sb.append(
						"<tr><th align=right>Simulation mode</th><td>Monte Carlo model of a pyramid with a square base</td></tr>");
				break;
			case MCTriangularPrism:
				sb.append(
						"<tr><th align=right>Simulation mode</th><td>Monte Carlo model of a triangular prism</td></tr>");
				break;
			default:
				break;
			}
			switch (mMode) {
			case AnalyticalBulk:
			case MCBulk:
				sb.append("<tr><th align=right>Material</th><td>" + mSubstrateMaterial.toHTMLTable() + "</td></tr>");
				break;
			case MCCube:
			case MCFilm:
			case MCInclusion:
			case MCSphere:
			case MCCylinderOnSide:
			case MCCylinderOnEnd:
			case MCHemisphere:
			case MCRectangularPrism:
			case MCSquarePyramid:
			case MCTriangularPrism:
				if (mSubstrateMaterial.equals(Material.Null))
					sb.append("<tr><th align=right>Substrate material</th><td>No substrate</td></tr>");
				else
					sb.append("<tr><th align=right>Substrate material</th><td>" + mSubstrateMaterial.toHTMLTable()
							+ "</td></tr>");
				sb.append(
						"<tr><th align=right>Object Material</th><td>" + mObjectMaterial.toHTMLTable() + "</td></tr>");
				break;
			case MCInterface:
				if (mSubstrateMaterial.equals(Material.Null))
					sb.append("<tr><th align=right>Secondary material</th><td>No substrate</td></tr>");
				else
					sb.append("<tr><th align=right>Secondary material</th><td>" + mSubstrateMaterial.toHTMLTable()
							+ "</td></tr>");
				sb.append(
						"<tr><th align=right>Primary Material</th><td>" + mObjectMaterial.toHTMLTable() + "</td></tr>");
				break;
			default:
				break;

			}
			switch (mMode) {
			case AnalyticalBulk:
			case MCBulk:
				break;
			case MCCube:
				sb.append(
						"<tr><th align=right>Edge length</th><td>" + nf3.format(mScale * 1.0e6) + " \u00B5m</td></tr>");
				break;
			case MCFilm:
				sb.append("<tr><th align=right>Film thickness</th><td>" + nf3.format(mScale * 1.0e6)
						+ " \u00B5m</td></tr>");
				break;
			case MCInclusion:
				sb.append("<tr><th align=right>Inclusion width</th><td>" + nf3.format(mScale * 1.0e6)
						+ " \u00B5m</td></tr>");
				sb.append("<tr><th align=right>Inclusion thickness</th><td>" + nf3.format(mScale2 * 1.0e6)
						+ " \u00B5m</td></tr>");
				break;
			case MCSphere:
			case MCHemisphere:
				sb.append("<tr><th align=right>Sphere diameter</th><td>" + nf3.format(mScale * 1.0e6)
						+ " \u00B5m</td></tr>");
				break;
			case MCInterface:
				sb.append("<tr><th align=right>Distance from interface</th><td>" + nf3.format(mScale * 1.0e6)
						+ " \u00B5m</td></tr>");
				break;
			case MCCylinderOnSide:
			case MCCylinderOnEnd:
				sb.append("<tr><th align=right>Diameter</th><td>" + nf3.format(mScale * 1.0e6) + " \u00B5m</td></tr>");
				sb.append("<tr><th align=right>Length</th><td>" + nf3.format(mScale2 * 1.0e6) + " \u00B5m</td></tr>");
				break;
			case MCRectangularPrism:
				sb.append("<tr><th align=right>Depth</th><td>" + nf3.format(mScale * 1.0e6) + " \u00B5m</td></tr>");
				sb.append(
						"<tr><th align=right>Thickness</th><td>" + nf3.format(mScale2 * 1.0e6) + " \u00B5m</td></tr>");
				break;
			case MCSquarePyramid:
				sb.append("<tr><th align=right>Base edge</th><td>" + nf3.format(mScale * 1.0e6) + " \u00B5m</td></tr>");
				sb.append("<tr><th align=right>Height</th><td>" + nf3.format(mScale2 * 1.0e6) + " \u00B5m</td></tr>");
				break;
			case MCTriangularPrism:
				sb.append("<tr><th align=right>Length</th><td>" + nf3.format(mScale * 1.0e6) + " \u00B5m</td></tr>");
				sb.append("<tr><th align=right>Height</th><td>" + nf3.format(mScale2 * 1.0e6) + " \u00B5m</td></tr>");
				break;

			}
			sb.append("<tr><th align=right>Sample rotation</th><td>" + nf1.format(Math.toDegrees(mRotate))
					+ "\u00B0</td></tr>");
			sb.append("<tr><th align=right>Sample tilt</th><td>" + nf1.format(Math.toDegrees(mIncidentAngle))
					+ "\u00B0</td></tr>");
			sb.append("<tr><th align=right>Beam energy</th><td>" + nf1.format(FromSI.keV(mBeamEnergy))
					+ " keV</td></tr>");
			sb.append("<tr><th align=right>Probe dose</th><td>" + nf1.format(mProbeDose / ToSI.NANO)
					+ " nA\u00B7s</td></tr>");
			sb.append("<tr><th align=right>Instrument</th><td>" + mDetector.getOwner().toString() + " </td></tr>");
			sb.append("<tr><th align=right>Detector</th><td>" + mDetector.getName() + " </td></tr>");
			sb.append(
					"<tr><th align=right>Calibration</th><td>" + mDetector.getCalibration().toString() + " </td></tr>");
			sb.append("<tr><th align=right>Overscan</th><td>" + Boolean.toString(mThread.mOverscan) + "</td></tr>");
			sb.append("<tr><th align=right>Vacuum conditions</th><td>");
			if (mEnableVP)
				sb.append(nf3.format(1000.0 * mVPPathLength) + " mm of " + mVPGas + " at " + nf1.format(mVPPressure)
						+ " pascal");
			else
				sb.append("High vacuum");
			sb.append("</td></tr>");
			if (mReplicas > 0)
				sb.append("<tr><th align=right>Replicas (with Poisson noise)</th><td>" + Integer.toString(mReplicas)
						+ " </td></tr>");
			sb.append("</table></p>\n");
			if (isDone())
				try {
					sb.append("<h2>Simulation Results</h2>");
					sb.append("<p><table>");
					final ISpectrumData[] specs = get();
					final File base = DTSA2.getReport().getFile().getParentFile();
					for (int i = 0; i < specs.length; ++i) {
						sb.append("<tr><th align=right>Result ");
						sb.append(i + 1);
						final ISpectrumData spec = specs[i];
						try {
							final File f = File.createTempFile("mc_spec", ".msa", base);
							try (final FileOutputStream fos = new FileOutputStream(f)) {
								WriteSpectrumAsEMSA1_0.write(spec, fos, WriteSpectrumAsEMSA1_0.Mode.COMPATIBLE);
							}
							spec.getProperties().setTextProperty(SpectrumProperties.SourceFile, f.toString());
							sb.append("</th><td><A HREF=\"");
							sb.append(f.toURI().toURL().toExternalForm());
							sb.append("\">");
							sb.append(spec.toString());
							sb.append("</A></td></tr>");
						} catch (final Exception ex) {
							sb.append("</th><td>");
							sb.append(spec.toString());
							sb.append("/td></tr>");
						}

					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			if (mVrmlFile != null) {
				sb.append("<tr><th align=right>Trajectory view</th><td><A HREF=\"");
				try {
					sb.append(mVrmlFile.toURI().toURL().toExternalForm());
				} catch (final MalformedURLException e) {
					sb.append("dead link");
				}
				sb.append("\">");
				sb.append("VRML World View File");
				sb.append("</A></td></tr>");
			}
			if (mXRayAccumulator3 != null) {
				sb.append("<tr><th>" + mXRayAccumulator3.toString() + "</th><td>");
				sb.append(mXRayAccumulator3.toHTML());
				sb.append("</td></tr>");
			}
			if (mXRayAccumulator3CF != null) {
				sb.append("<tr><th>" + mXRayAccumulator3CF.toString() + "</th><td>");
				sb.append(mXRayAccumulator3CF.toHTML());
				sb.append("</td></tr>");
			}
			if (mXRayAccumulator3BF != null) {
				sb.append("<tr><th>" + mXRayAccumulator3BF.toString() + "</th><td>");
				sb.append(mXRayAccumulator3BF.toHTML());
				sb.append("</td></tr>");
			}
			if (mXRayAccumulator3 != null) {
				if (mXRayAccumulator3CF != null) {
					sb.append("<tr><th>Comparing " + mXRayAccumulator3.toString() + " to <br/>"
							+ mXRayAccumulator3CF.toString() + "</th><td>");
					sb.append(mXRayAccumulator3.compareAsHTML(mXRayAccumulator3CF));
					sb.append("</td></tr>");
				}
				if (mXRayAccumulator3BF != null) {
					sb.append("<tr><th>Comparing " + mXRayAccumulator3.toString() + " to <br/>"
							+ mXRayAccumulator3BF.toString() + "</th><td>");
					sb.append(mXRayAccumulator3.compareAsHTML(mXRayAccumulator3BF));
					sb.append("</td></tr>");
				}
			}
			sb.append("</table>");
			if ((mEmissionFiles != null) && (mEmissionFiles.size() > 0)) {
				sb.append("<h3>Emission Images</h3>");
				sb.append("<table>\n");
				final int IMGS_PER_ROW = 3;
				int i = 0;
				for (final Map.Entry<String, String> me : mEmissionFiles.entrySet()) {
					if ((i % IMGS_PER_ROW) == 0)
						sb.append("<tr>");
					sb.append("<td align=center><img src=\"");
					sb.append(me.getValue());
					sb.append("\" alt=");
					sb.append(me.getKey().toString());
					sb.append(" width = 256 height = 256 /><br/>");
					sb.append(me.getKey().toString());
					sb.append("</td>");
					if ((i % IMGS_PER_ROW) == 2)
						sb.append("</tr>\n");
					++i;
				}
				// Check if we didn't just end a row...
				if ((i % IMGS_PER_ROW) != 0)
					sb.append("</tr>\n");
				sb.append("</table></p>\n");
			}
			final AnnularDetector ad0 = mAnnularDetector[0];
			if (ad0 != null) {
				sb.append("<h3>VP Scatter Data</h3>");
				final double totECx = mAnnularDetector[2].totalElectronCount();
				sb.append("<p>Electron trajectory count = " + (int) totECx + "</p>");
				sb.append("<p><table>\n");
				sb.append("<tr>");
				sb.append("<th>Ring</th>");
				sb.append("<th>Inner Radius<br/>&micro;m</th>");
				sb.append("<th>Outer Radius<br/>&micro;m</th>");
				sb.append("<th>Ring area<br/>&micro;m<sup>2</sup></th>");
				sb.append("<th>Electron Count</th>");
				sb.append("<th>Electron Fraction</th>");
				sb.append("<th>Cumulative</th>");
				sb.append("</tr>\n");
				{ // Undeflected...
					sb.append("<tr>");
					sb.append("<td>Undeflected</td>");
					sb.append("<td> - </td>");
					sb.append("<td> - </td>");
					sb.append("<td> - </td>");
					sb.append("<td>" + ad0.detectedElectronCount(0) + "</td>");
					sb.append("<td>" + nf3.format(ad0.detectedElectronCount(0) / totECx) + "</td>");
					sb.append("<td> - </td>");
					sb.append("</tr>\n");
				}
				{
					double sum = 0.0;
					final AnnularDetector ad1 = mAnnularDetector[1];
					for (int i = 0; i < ad1.numberOfRings(); ++i) {
						sb.append("<tr>");
						sb.append("<td>" + Integer.toString(i + 1) + "</td>");
						sb.append("<td>" + nf1.format(1.0e6 * ad1.innerRadius(i)) + "</td>");
						sb.append("<td>" + nf1.format(1.0e6 * ad1.outerRadius(i)) + "</td>");
						sb.append("<td>" + nf1.format(ad1.ringArea(i) * 1.0e12) + "</td>");
						sb.append("<td>" + ad1.detectedElectronCount(i) + "</td>");
						sb.append("<td>" + nf3.format(ad1.detectedElectronCount(i) / totECx) + "</td>");
						sum += ad1.detectedElectronCount(i) / totECx;
						sb.append("<td>" + nf1.format(100.0 * sum) + "%</td>");
						sb.append("</tr>\n");
					}
					final AnnularDetector ad2 = mAnnularDetector[2];
					for (int i = 1; i < ad2.numberOfRings(); ++i) {
						sb.append("<tr>");
						sb.append("<td>" + Integer.toString(ad1.numberOfRings() + i) + "</td>");
						sb.append("<td>" + nf1.format(1.0e6 * ad2.innerRadius(i)) + "</td>");
						sb.append("<td>" + nf1.format(1.0e6 * ad2.outerRadius(i)) + "</td>");
						sb.append("<td>" + nf1.format(ad2.ringArea(i) * 1.0e12) + "</td>");
						sb.append("<td>" + ad2.detectedElectronCount(i) + "</td>");
						sb.append("<td>" + nf3.format(ad2.detectedElectronCount(i) / totECx) + "</td>");
						sum += ad2.detectedElectronCount(i) / totECx;
						sb.append("<td>" + nf1.format(100.0 * sum) + "%</td>");
						sb.append("</tr>\n");
					}
				}
				sb.append("</table></p>\n");
			}
			if (mVoxelatedDetector != null) {
				sb.append("<h3>Fractional Emission Depths and Volumes</h3>\n");
				final Set<VoxelatedDetector.XRayType> acc = mVoxelatedDetector.getAccumulatorObjects();
				sb.append("<table>");
				sb.append(
						"<tr><th>Ionization<br/>Edge</th><th>Ionization<br/>Energy<br/>(keV)</th>"
						+ "<th>F(50 &#37;)<br/>Depth<br/>(&micro;m)</th><th>F(90 &#37;)<br/>Depth<br/>(&micro;m)</th><th>F(99.9 &#37;)<br/>Depth<br/>(&micro;m)</th>"
						+ "<th>F(50 &#37;)<br/>Radial<br/>(&micro;m)</th><th>F(90 &#37;)<br/>Radial<br/>(&micro;m)</th><th>F(99.9 &#37;)<br/>Radial<br/>(&micro;m)</th>"
						+ "<th>F(50 &#37;)<br/>Volume<br/>(&micro;m)<sup>3</sup></th><th>F(90 &#37;)<br/>Volume<br/>(&micro;m)<sup>3</sup></th><th>F(99.9 &#37;)<br/>Volume<br/>(&micro;m)<sup>3</sup></th>"
						+ "</tr>\n");
				final double[] origin = new double[] { 0.0, 0.0, 0.0 };
				for (final VoxelatedDetector.XRayType obj : acc) {
					final AtomicShellType sh = obj instanceof AtomicShellType ? (AtomicShellType) obj : null;
					if ((sh != null) && (sh.getEdgeEnergy() >= ToSI.keV(0.1))) {
						sb.append("<tr><td>");
						sb.append(sh.toString());
						sb.append("</td><td>");
						sb.append(nf3.format(FromSI.keV(sh.getEdgeEnergy())));
						sb.append("</td><td>");
						for(double f : new double[] { 0.5, 0.9, 0.999}){
							double[] mm= mVoxelatedDetector.getFractionalGenerationDepth(obj, f);
							sb.append(nf3.format(mm[0] * 1.0e6)+" &lt; z &lt; "+nf3.format(mm[1] * 1.0e6));
							sb.append("</td><td>");
						}
						for(double f : new double[] { 0.5, 0.9, 0.999}){
							double[] mm= mVoxelatedDetector.getFractionalGenerationRadius(origin, obj, f);
							sb.append(nf3.format(mm[0] * 1.0e6)+" &lt; r &lt; "+nf3.format(mm[1] * 1.0e6));
							sb.append("</td><td>");
						}
						for(double f : new double[] { 0.5, 0.9, 0.999}){
							final double v = mVoxelatedDetector.getFractionalGenerationVolume(obj, f)*1.0e18;
							String n = v < 0.01 ? nf6.format(v) :nf3.format(v);
							sb.append("v &asymp; "+ n);
							sb.append("</td><td>");
						}
						sb.append("</td></tr>\n");
					}
				}
				sb.append("</table>\n");
				sb.append("Distances and volumes are approximate due to limits imposed by voxelation.  This is particularly a problem at high beam energies.</p>\n");
			}
			return sb.toString();
		}

		private SimulationThread() {
			mMode = SimulationMode.AnalyticalBulk;
			mObjectMaterial = new Material(3.0);
			mSubstrateMaterial = new Material(5.0);
			mDetector = null;
			mScale = 1.0e-6;
			mOverscan = false;
			mBeamEnergy = ToSI.keV(20.0);
			mProbeDose = 60.0 * ToSI.NANO; // 1 nA * 60 seconds
			mIncidentAngle = 0.0;
			mReplicas = 1;
			mGenImages = false;
			mExtraElectrons = 1;
			mCharacteristic = true;
			mBremsstrahlung = true;
			mCharFluor = false;
			mBremFluor = false;
		}

		private void progress(int p) {
			publish(new Integer[] { Integer.valueOf(p) });
		}

		final double[] overScanBox(double x, double y, double rotate) {
			return Transform3D.rotate(Math2.v3(x, y, 0.0), rotate, 0.0, 0.0);
		}

		@Override
		protected ISpectrumData[] doInBackground() throws Exception {
			mVrmlFile = null;
			mXRayAccumulator3 = null;
			mXRayAccumulator3BF = null;
			mXRayAccumulator3CF = null;
			mEmissionImages3 = null;
			ISpectrumData spec = null, rawSpec = null;
			final SpectrumProperties props = new SpectrumProperties();
			props.setNumericProperty(SpectrumProperties.BeamEnergy, FromSI.keV(mBeamEnergy));
			props.setNumericProperty(SpectrumProperties.FaradayBegin, 1.0);
			props.setNumericProperty(SpectrumProperties.LiveTime, mProbeDose * FromSI.NANO);
			props.setDetector(mDetector);
			double[] sNorm;
			{ // Compute the surface normal
				// Tilt towards 0 degrees azimuth
				sNorm = Math2.v3(Math.sin(mIncidentAngle), 0.0, -Math.cos(mIncidentAngle));
				assert Math.abs(Math2.angleBetween(sNorm, Math2.MINUS_Z_AXIS) - Math.abs(mIncidentAngle)) < 1.0e-5;
				assert Math.abs(Math2.magnitude(sNorm) - 1.0) < 1.0e-8;
			}
			props.setSampleShape(SpectrumProperties.SampleShape, new SampleShape.Bulk(sNorm));
			if (mMode == SimulationMode.AnalyticalBulk) {
				mProgress.setRange(0, 100);
				progress(50);
				try {
					props.setCompositionProperty(SpectrumProperties.StandardComposition, mSubstrateMaterial);
					spec = SpectrumSimulator.Basic.generateSpectrum(mSubstrateMaterial, props, mThread.mBremsstrahlung);
				} catch (final Throwable epq) {
					setExceptionText(epq);
					setProgress(100);
					return null;
				}
				progress(100);
			} else
				try {
					mDetector.reset();
					props.setDetector(mDetector);
					final MonteCarloSS mc = new MonteCarloSS();
					mc.setBeamEnergy(mBeamEnergy);
					String specDesc = "MC simulation of ";
					final NumberFormat nf = new HalfUpFormat("0.000");
					// Use the working distance or optimal working distance as the
					// sample position
					final double[] origin = SpectrumUtils.getSamplePosition(props);
					props.setNumericProperty(SpectrumProperties.WorkingDistance, 1.0e3 * origin[2]);
					final Set<Element> elms = new TreeSet<Element>();
					// sc is used to size the emission images
					double sc = 1.0e-4;
					MonteCarloSS.ElectronGun gun = new GaussianBeam(1.0e-8);
					TrajectoryImage ti = null;
					Region chamber = mc.getChamber();
					if (mEnableVP) {
						final double dim = 0.5 * MonteCarloSS.ChamberRadius;
						final double[] dims = Math2.plus(Math2.v3(dim, dim, dim), Math2.z3(2.0 * mVPPathLength));
						final double[] pt = Math2.plus(origin, Math2.z3(0.5 * dim));
						final Shape shape = MultiPlaneShape.createBlock(dims, pt, 0.0, 0.0, 0.0);
						final BasicMaterialModel msm = new BasicMaterialModel(mVPGas.toGas(mVPPressure));
						chamber = mc.addSubRegion(chamber, msm, shape);
					}
					boolean canRotate = false;
					switch (mMode) {
					case MCBulk: {
						sc = ElectronRange.KanayaAndOkayama1972.computeMeters(mSubstrateMaterial, mBeamEnergy);
						mScale = sc;
						final double dim = Math.max(10.0 * mScale, 1.0e-4);
						final double[] dims = Math2.v3(dim, dim, dim);
						final double[] pos = Math2.plus(origin, Math2.z3(0.5 * dim));
						final MonteCarloSS.Shape shape = MultiPlaneShape.createBlock(dims, pos, 0, 0, 0);
						mc.addSubRegion(chamber, mSubstrateMaterial, shape);
						elms.addAll(mSubstrateMaterial.getElementSet());
						specDesc = specDesc + "bulk " + mSubstrateMaterial.toString();
						break;
					}
					case MCCube: {
						{
							final double rSub = mSubstrateMaterial.equals(Material.Null) ? 0.0
									: ElectronRange.KanayaAndOkayama1972.computeMeters(mSubstrateMaterial, mBeamEnergy);
							final double rObj = ElectronRange.KanayaAndOkayama1972.computeMeters(mObjectMaterial,
									mBeamEnergy);
							sc = Math.min(mScale, rObj) + Math.max((1.1 - (mScale / rObj)) * rSub, rObj / 10.0);
						}
						final double[] center = Math2.plus(origin, Math2.z3(mScale / 2.0));
						final MonteCarloSS.Shape cube = MultiPlaneShape.createBlock(Math2.v3(mScale, mScale, mScale),
								center, 0, 0, 0);
						mc.addSubRegion(chamber, mObjectMaterial, cube);
						elms.addAll(mObjectMaterial.getElementSet());
						if (!mSubstrateMaterial.equals(Material.Null)) {
							final double rSub = mSubstrateMaterial.equals(Material.Null) ? 0.0
									: ElectronRange.KanayaAndOkayama1972.computeMeters(mSubstrateMaterial, mBeamEnergy);
							final double rObj = ElectronRange.KanayaAndOkayama1972.computeMeters(mObjectMaterial,
									mBeamEnergy);
							sc = mScale + Math.max((1.1 - (mScale / rObj)) * rSub, mScale / 10.0);
							elms.addAll(mSubstrateMaterial.getElementSet());
							final double dim = Math.max(5.0 * rSub, 1.0e-2);
							final double[] dims = Math2.v3(dim, dim, dim);
							final MonteCarloSS.Shape shape = MultiPlaneShape.createBlock(dims,
									Math2.plus(center, Math2.z3((mScale + dim) / 2.0)), 0.0, 0.0, 0.0);
							mc.addSubRegion(chamber, mSubstrateMaterial, shape);
						}
						specDesc = specDesc + " a " + nf.format(1.0e6 * mScale) + " \u00B5m cube of "
								+ mObjectMaterial.toString() + " on " + mSubstrateMaterial.toString();
						if (mThread.mOverscan) {
							final double[] over = overScanBox(1.1 * mScale, 1.1 * mScale, mRotate);
							gun = new OverscanElectronGun(over[0], over[1]);
							specDesc = specDesc + " (overscan)";
						}
						canRotate = true;
						break;
					}
					case MCFilm: {
						double dim;
						{
							final double rSub = mSubstrateMaterial.equals(Material.Null) ? 0.0
									: ElectronRange.KanayaAndOkayama1972.computeMeters(mSubstrateMaterial, mBeamEnergy);
							final double rObj = ElectronRange.KanayaAndOkayama1972.computeMeters(mObjectMaterial,
									mBeamEnergy);
							sc = Math.min(mScale, rObj) + Math.max((1.1 - (mScale / rObj)) * rSub, rObj / 3.0);
							dim = 5.0 * Math.max(Math.max(rObj, rSub), 1.0e-2);
						}
						{
							final double[] dims = Math2.v3(dim, dim, mScale);
							final MonteCarloSS.Shape film = MultiPlaneShape.createBlock(dims,
									Math2.plus(origin, Math2.z3(0.5 * mScale)), 0.0, 0.0, 0.0);
							mc.addSubRegion(chamber, mObjectMaterial, film);
						}
						elms.addAll(mObjectMaterial.getElementSet());
						if (mSubstrateMaterial.equals(Material.Null))
							specDesc = specDesc + " a " + nf.format(1.0e6 * mScale) + " \u00B5m film of "
									+ mObjectMaterial.toString();
						else {
							elms.addAll(mSubstrateMaterial.getElementSet());
							{
								final double[] dims = Math2.v3(dim, dim, dim);
								final MonteCarloSS.Shape shape = MultiPlaneShape.createBlock(dims,
										Math2.plus(origin, Math2.z3(mScale + 0.5 * dim)), 0.0, 0.0, 0.0);
								mc.addSubRegion(chamber, mSubstrateMaterial, shape);
							}
							specDesc = specDesc + " a " + nf.format(1.0e6 * mScale) + " \u00B5m film of "
									+ mObjectMaterial.toString() + " on " + mSubstrateMaterial.toString();
						}
						break;
					}
					case MCInclusion: {
						{
							final double rSub = mSubstrateMaterial.equals(Material.Null) ? 0.0
									: ElectronRange.KanayaAndOkayama1972.computeMeters(mSubstrateMaterial, mBeamEnergy);
							final double rObj = ElectronRange.KanayaAndOkayama1972.computeMeters(mObjectMaterial,
									mBeamEnergy);
							sc = Math.min(0.5 * mScale, rObj) + Math.max((1.1 - (mScale / rObj)) * rSub, rObj / 3.0);
						}
						final double rSub = mSubstrateMaterial.equals(Material.Null) ? 0.0
								: ElectronRange.KanayaAndOkayama1972.computeMeters(mSubstrateMaterial, mBeamEnergy);
						final double rObj = ElectronRange.KanayaAndOkayama1972.computeMeters(mObjectMaterial,
								mBeamEnergy);
						sc = mScale + Math.max((1.1 - (mScale / rObj)) * rSub, mScale / 10.0);
						final double dim = Math.max(5.0 * rSub, 1.0e-2);
						final double so2 = 0.5 * mScale2;
						// Simple model using embedded objects
						MonteCarloSS.Region region = chamber;
						if (!mSubstrateMaterial.equals(Material.Null)) {
							final MonteCarloSS.Shape shape = MultiPlaneShape.createBlock(Math2.v3(dim, dim, dim),
									Math2.plus(origin, Math2.z3(0.5 * dim)));
							region = mc.addSubRegion(chamber, mSubstrateMaterial, shape);
							elms.addAll(mSubstrateMaterial.getElementSet());
						}
						// MonteCarloSS.Shape inclusion = new
						// ShapeDifference(new Sphere(origin, mScale2),
						// MultiPlaneShape.createBlock(Math2.v3(mScale2,
						// mScale2, 0.5*mScale2), Math2.minus(origin,
						// Math2.z3(0.25*mScale2))));
						final MonteCarloSS.Shape inclusion = MultiPlaneShape
								.createBlock(Math2.v3(mScale, mScale, mScale2), Math2.plus(origin, Math2.z3(so2)));
						elms.addAll(mObjectMaterial.getElementSet());
						mc.addSubRegion(region, mObjectMaterial, inclusion);
						specDesc = specDesc + " a " + nf.format(1.0e6 * mScale) + " \u00B5m wide, "
								+ nf.format(1.0e6 * mScale2) + " thick inclusion of " + mObjectMaterial.toString()
								+ " in " + mSubstrateMaterial.toString();
						if (mThread.mOverscan) {
							final double[] over = overScanBox(1.1 * mScale, 1.1 * mScale, mRotate);
							gun = new OverscanElectronGun(over[0], over[1]);
							specDesc = specDesc + " (overscan)";
						}
						canRotate = true;
						break;
					}
					case MCSphere: {
						{
							final double rSub = mSubstrateMaterial.equals(Material.Null) ? 0.0
									: ElectronRange.KanayaAndOkayama1972.computeMeters(mSubstrateMaterial, mBeamEnergy);
							final double rObj = ElectronRange.KanayaAndOkayama1972.computeMeters(mObjectMaterial,
									mBeamEnergy);
							sc = Math.min(mScale, rObj) + Math.max((1.1 - (mScale / rObj)) * rSub, rObj / 10.0);
						}
						final double[] center = Math2.plus(origin, Math2.z3(0.5 * mScale));
						final MonteCarloSS.Shape sphere = new Sphere(center, 0.5 * mScale);
						mc.addSubRegion(chamber, mObjectMaterial, sphere);
						elms.addAll(mObjectMaterial.getElementSet());
						final double dim = Math.max(10.0 * mScale, 1.0e-2);
						final double[] dims = Math2.v3(dim, dim, dim);
						final MonteCarloSS.Shape shape = MultiPlaneShape.createBlock(dims,
								Math2.plus(center, Math2.z3((mScale + dim) / 2.0)), 0.0, 0.0, 0.0);
						if (!mSubstrateMaterial.equals(Material.Null)) {
							mc.addSubRegion(chamber, mSubstrateMaterial, shape);
							elms.addAll(mSubstrateMaterial.getElementSet());
						}
						specDesc = specDesc + " a " + nf.format(1.0e6 * mScale) + " \u00B5m diameter sphere of "
								+ mObjectMaterial.toString() + " on " + mSubstrateMaterial.toString();
						if (mThread.mOverscan) {
							gun = new OverscanElectronGun(mScale, mScale);
							specDesc = specDesc + " (overscan)";
						}
						break;
					}
					case MCInterface: {
						sc = ElectronRange.KanayaAndOkayama1972.computeMeters(mSubstrateMaterial, mBeamEnergy);
						final double dim = Math.max(100.0 * sc, 1.0e-2);
						if (sc < (4.0 * mScale))
							sc = 4.0 * mScale;
						final double[] dims = Math2.v3(0.5 * dim, dim, dim);
						if (!mObjectMaterial.equals(Material.Null)) {
							elms.addAll(mObjectMaterial.getElementSet());
							final double[] pos = Math2.v3(origin[0] - ((0.25 * dim) - mScale), origin[1],
									origin[2] + (0.5 * dim));
							final MonteCarloSS.Shape shape = MultiPlaneShape.createBlock(dims, pos, 0, 0, 0);
							mc.addSubRegion(chamber, mObjectMaterial, shape);
						}
						if (!mSubstrateMaterial.equals(Material.Null)) {
							final double[] pos = Math2.v3(origin[0] + ((0.25 * dim) + mScale), origin[1],
									origin[2] + (0.5 * dim));
							final MonteCarloSS.Shape shape = MultiPlaneShape.createBlock(dims, pos, 0, 0, 0);
							mc.addSubRegion(chamber, mSubstrateMaterial, shape);
						}
						elms.addAll(mSubstrateMaterial.getElementSet());
						specDesc = nf.format(1.0e6 * mScale) + " \u00B5m from a " + mObjectMaterial.toString() + "/"
								+ mSubstrateMaterial.toString() + " interface.";
						canRotate = true;
						break;
					}
					case MCCylinderOnSide: {
						{
							final double rSub = mSubstrateMaterial.equals(Material.Null) ? 0.0
									: ElectronRange.KanayaAndOkayama1972.computeMeters(mSubstrateMaterial, mBeamEnergy);
							final double rObj = ElectronRange.KanayaAndOkayama1972.computeMeters(mObjectMaterial,
									mBeamEnergy);
							sc = Math.min(mScale, rObj) + Math.max((1.1 - (mScale / rObj)) * rSub, rObj / 10.0);
						}
						final double[] center = Math2.plus(origin, Math2.z3(0.5 * mScale));
						final double[] halfLen = Math2.y3(-1.0 * mScale2);
						final MonteCarloSS.Shape cylinder = new CylindricalShape(Math2.minus(center, halfLen),
								Math2.plus(center, halfLen), 0.5 * mScale);
						mc.addSubRegion(chamber, mObjectMaterial, cylinder);
						elms.addAll(mObjectMaterial.getElementSet());
						if (!mSubstrateMaterial.equals(Material.Null)) {
							final double dim = Math.max(10.0 * mScale, 1.0e-2);
							final double[] dims = Math2.v3(dim, dim, dim);
							final MultiPlaneShape shape = MultiPlaneShape.createBlock(dims,
									Math2.plus(origin, Math2.z3(mScale + 0.5 * dim)), 0.0, 0.0, 0.0);
							mc.addSubRegion(chamber, mSubstrateMaterial, shape);
							elms.addAll(mSubstrateMaterial.getElementSet());
						}
						specDesc = specDesc + " a " + nf.format(1.0e6 * mScale) + " \u00B5m dia fiber of "
								+ mObjectMaterial.toString() + " on " + mSubstrateMaterial.toString();
						if (mThread.mOverscan) {
							final double[] over = overScanBox(mScale, mScale2, mRotate);
							gun = new OverscanElectronGun(over[0], over[1]);
							specDesc = specDesc + " (overscan)";
						}
						canRotate = true;
						break;
					}
					case MCCylinderOnEnd: {
						{
							final double rSub = mSubstrateMaterial.equals(Material.Null) ? 0.0
									: ElectronRange.KanayaAndOkayama1972.computeMeters(mSubstrateMaterial, mBeamEnergy);
							final double rObj = ElectronRange.KanayaAndOkayama1972.computeMeters(mObjectMaterial,
									mBeamEnergy);
							sc = Math.min(mScale, rObj) + Math.max((1.1 - (mScale / rObj)) * rSub, rObj / 10.0);
						}
						final double[] len = Math2.z3(mScale2);
						final MonteCarloSS.Shape cylinder = new CylindricalShape(origin, Math2.plus(origin, len),
								0.5 * mScale);
						mc.addSubRegion(chamber, mObjectMaterial, cylinder);
						elms.addAll(mObjectMaterial.getElementSet());
						final double dim = Math.max(10.0 * mScale, 1.0e-2);
						final double[] dims = Math2.v3(dim, dim, dim);
						final MonteCarloSS.Shape shape = MultiPlaneShape.createBlock(dims,
								Math2.plus(origin, Math2.z3(mScale2 + 0.5 * dim)), 0.0, 0.0, 0.0);
						if (!mSubstrateMaterial.equals(Material.Null)) {
							mc.addSubRegion(chamber, mSubstrateMaterial, shape);
							elms.addAll(mSubstrateMaterial.getElementSet());
						}
						specDesc = specDesc + " a " + nf.format(1.0e6 * mScale) + " \u00B5m dia., "
								+ nf.format(1.0e6 * mScale2) + " \u00B5m high can of " + mObjectMaterial.toString()
								+ " on " + mSubstrateMaterial.toString();
						if (mThread.mOverscan) {
							gun = new OverscanElectronGun(mScale, mScale);
							specDesc = specDesc + " (overscan)";
						}
						break;
					}
					case MCHemisphere: {
						{
							final double rSub = mSubstrateMaterial.equals(Material.Null) ? 0.0
									: ElectronRange.KanayaAndOkayama1972.computeMeters(mSubstrateMaterial, mBeamEnergy);
							final double rObj = ElectronRange.KanayaAndOkayama1972.computeMeters(mObjectMaterial,
									mBeamEnergy);
							sc = Math.min(mScale, rObj) + Math.max((1.1 - (mScale / rObj)) * rSub, rObj / 10.0);
						}
						final double[] center = Math2.plus(origin, Math2.z3(0.5 * mScale));
						final MonteCarloSS.Shape hemi = new ShapeDifference(new Sphere(center, 0.5 * mScale),
								MultiPlaneShape.createSubstrate(Math2.MINUS_Z_AXIS, center));
						mc.addSubRegion(chamber, mObjectMaterial, hemi);
						elms.addAll(mObjectMaterial.getElementSet());
						if (!mSubstrateMaterial.equals(Material.Null)) {
							final double dim = Math.max(10.0 * mScale, 1.0e-2);
							final double[] dims = Math2.v3(dim, dim, dim);
							final MonteCarloSS.Shape shape = MultiPlaneShape.createBlock(dims,
									Math2.plus(center, Math2.z3(0.5 * dim)), 0.0, 0.0, 0.0);
							mc.addSubRegion(chamber, mSubstrateMaterial, shape);
							elms.addAll(mSubstrateMaterial.getElementSet());
						}
						specDesc = specDesc + " a " + nf.format(1.0e6 * mScale) + " \u00B5m dia sphere of "
								+ mObjectMaterial.toString() + " on " + mSubstrateMaterial.toString();
						if (mThread.mOverscan) {
							gun = new OverscanElectronGun(mScale, mScale);
							specDesc = specDesc + " (overscan)";
						}
						break;
					}
					case MCRectangularPrism: {
						{
							final double rSub = mSubstrateMaterial.equals(Material.Null) ? 0.0
									: ElectronRange.KanayaAndOkayama1972.computeMeters(mSubstrateMaterial, mBeamEnergy);
							final double rObj = ElectronRange.KanayaAndOkayama1972.computeMeters(mObjectMaterial,
									mBeamEnergy);
							sc = Math.min(mScale, rObj) + Math.max((1.1 - (mScale / rObj)) * rSub, rObj / 10.0);
						}
						final double[] center = Math2.plus(origin, Math2.z3(0.5 * mScale2));
						final MonteCarloSS.Shape rectPrism = MultiPlaneShape
								.createBlock(Math2.v3(mScale, mScale, mScale2), center, 0.0, 0.0, 0.0);
						mc.addSubRegion(chamber, mObjectMaterial, rectPrism);
						elms.addAll(mObjectMaterial.getElementSet());
						final double dim = Math.max(10.0 * mScale, 1.0e-2);
						final double[] dims = Math2.v3(dim, dim, dim);
						final MonteCarloSS.Shape shape = MultiPlaneShape.createBlock(dims,
								Math2.plus(center, Math2.z3((mScale + dim) / 2.0)), 0.0, 0.0, 0.0);
						if (!mSubstrateMaterial.equals(Material.Null)) {
							mc.addSubRegion(chamber, mSubstrateMaterial, shape);
							elms.addAll(mSubstrateMaterial.getElementSet());
						}
						specDesc = specDesc + " a " + nf.format(1.0e6 * mScale) + " \u00B5m base, "
								+ nf.format(1.0e6 * mScale2) + " \u00B5m high block of " + mObjectMaterial.toString()
								+ " on " + mSubstrateMaterial.toString();
						if (mThread.mOverscan) {
							final double[] over = overScanBox(mScale, mScale, mRotate);
							gun = new OverscanElectronGun(over[0], over[1]);
							specDesc = specDesc + " (overscan)";
						}
						canRotate = true;
						break;
					}
					case MCSquarePyramid: {
						{
							final double rSub = mSubstrateMaterial.equals(Material.Null) ? 0.0
									: ElectronRange.KanayaAndOkayama1972.computeMeters(mSubstrateMaterial, mBeamEnergy);
							final double rObj = ElectronRange.KanayaAndOkayama1972.computeMeters(mObjectMaterial,
									mBeamEnergy);
							sc = Math.min(mScale, rObj) + Math.max((1.1 - (mScale / rObj)) * rSub, rObj / 10.0);
						}
						final MonteCarloSS.Shape sqPyramid = MultiPlaneShape.createSquarePyramid(origin, mScale,
								mScale2);
						mc.addSubRegion(chamber, mObjectMaterial, sqPyramid);
						elms.addAll(mObjectMaterial.getElementSet());
						final double dim = Math.max(10.0 * mScale, 1.0e-2);
						if (!mSubstrateMaterial.equals(Material.Null)) {
							final MonteCarloSS.Shape shape = MultiPlaneShape.createBlock(Math2.v3(dim, dim, dim),
									Math2.plus(origin, Math2.z3(0.5 * dim + mScale2)), 0.0, 0.0, 0.0);
							mc.addSubRegion(chamber, mSubstrateMaterial, shape);
							elms.addAll(mSubstrateMaterial.getElementSet());
						}
						specDesc = specDesc + " a " + nf.format(1.0e6 * mScale) + " \u00B5m base, "
								+ nf.format(1.0e6 * mScale2) + " \u00B5m high square pyramid of "
								+ mObjectMaterial.toString() + " on " + mSubstrateMaterial.toString();
						if (mThread.mOverscan) {
							final double[] over = overScanBox(mScale, mScale, mRotate);
							gun = new OverscanElectronGun(over[0], over[1]);
							specDesc = specDesc + " (overscan)";
						}
						canRotate = true;
						break;
					}
					case MCTriangularPrism: {
						{
							final double rSub = mSubstrateMaterial.equals(Material.Null) ? 0.0
									: ElectronRange.KanayaAndOkayama1972.computeMeters(mSubstrateMaterial, mBeamEnergy);
							final double rObj = ElectronRange.KanayaAndOkayama1972.computeMeters(mObjectMaterial,
									mBeamEnergy);
							sc = Math.min(mScale, rObj) + Math.max((1.1 - (mScale / rObj)) * rSub, rObj / 10.0);
						}
						final MultiPlaneShape rectPrism = MultiPlaneShape.createTriangularPrism(origin, mScale,
								mScale2);
						mc.addSubRegion(chamber, mObjectMaterial, rectPrism);
						elms.addAll(mObjectMaterial.getElementSet());
						if (!mSubstrateMaterial.equals(Material.Null)) {
							final double dim = Math.max(10.0 * mScale, 1.0e-2);
							final double[] dims = Math2.v3(dim, dim, dim);
							final MonteCarloSS.Shape shape = MultiPlaneShape.createBlock(dims,
									Math2.plus(origin, Math2.z3((mScale + dim) / 2.0)), 0.0, 0.0, 0.0);
							mc.addSubRegion(chamber, mSubstrateMaterial, shape);
							elms.addAll(mSubstrateMaterial.getElementSet());
						}
						specDesc = specDesc + " a " + nf.format(1.0e6 * mScale) + " \u00B5m base, "
								+ nf.format(1.0e6 * mScale2) + " \u00B5m length equilateral prism of "
								+ mObjectMaterial.toString() + " on " + mSubstrateMaterial.toString();
						if (mThread.mOverscan) {
							final double[] over = overScanBox(mScale, mScale2, mRotate);
							gun = new OverscanElectronGun(over[0], over[1]);
							specDesc = specDesc + " (overscan)";
						}
						canRotate = true;
						break;
					}
					default:
						break;
					}
					// Set the gun and start point
					gun.setCenter(Math2.z3(-0.99 * MonteCarloSS.ChamberRadius));
					mc.setElectronGun(gun);
					// Determine the number of trajectories
					final int trajectories = computeTrajectoryCount();
					mProgress.setRange(0, trajectories);
					Arrays.fill(mAnnularDetector, null);
					if (mEnableVP) {
						mAnnularDetector[0] = new AnnularDetector(1.0e-7, 1, origin, Math2.MINUS_Z_AXIS);
						mAnnularDetector[1] = new AnnularDetector(50.0e-6, 20, origin, Math2.MINUS_Z_AXIS);
						mAnnularDetector[2] = new AnnularDetector(1000.0e-6, 20, origin, Math2.MINUS_Z_AXIS);
						for (int i = 0; i < mAnnularDetector.length; ++i)
							mc.addActionListener(mAnnularDetector[i]);
					}
					{
						CharacteristicXRayGeneration3 cxg = null;
						BremsstrahlungXRayGeneration3 bxg = null;
						FluorescenceXRayGeneration3 fxgC = null, fxgB = null;
						XRayTransport3 ctr = null, ctrFl = null, brFl = null;
						if (mCharacteristic || mCharFluor)
							cxg = CharacteristicXRayGeneration3.create(mc);
						if (mCharacteristic)
							ctr = XRayTransport3.create(mc, mDetector, cxg);
						if (mBremsstrahlung || mBremFluor)
							bxg = BremsstrahlungXRayGeneration3.create(mc);
						if (mBremsstrahlung)
							XRayTransport3.create(mc, mDetector, bxg);
						if (mCharFluor) {
							fxgC = FluorescenceXRayGeneration3.create(mc, cxg);
							ctrFl = XRayTransport3.create(mc, mDetector, fxgC);
							XRayTransport3.create(mc, mDetector, ComptonXRayGeneration3.create(mc, cxg));
						}
						if (mBremFluor) {
							fxgB = FluorescenceXRayGeneration3.create(mc, bxg);
							brFl = XRayTransport3.create(mc, mDetector, fxgB);
							XRayTransport3.create(mc, mDetector, ComptonXRayGeneration3.create(mc, bxg));
						}
						if ((ctr != null) || (ctrFl != null)) {
							final String[] families = new String[] { XRayTransitionSet.K_ALPHA,
									XRayTransitionSet.K_BETA, XRayTransitionSet.L_ALPHA, XRayTransitionSet.L_BETA,
									XRayTransitionSet.M_ALPHA };
							final Set<XRayTransitionSet> xrtss = new TreeSet<XRayTransitionSet>();
							for (final Element elm : elms)
								for (final String family : families) {
									final XRayTransitionSet xrts = new XRayTransitionSet(elm, family, 0.0, ToSI.eV(50.0),
											mBeamEnergy / 1.1);
									if (xrts.size() > 0)
										xrtss.add(xrts);
								}
							if (ctr != null) {
								mXRayAccumulator3 = new XRayAccumulator3(xrtss, "Characteristic", mProbeDose);
								ctr.addXRayListener(mXRayAccumulator3);
							}
							if (ctrFl != null) {
								mXRayAccumulator3CF = new XRayAccumulator3(xrtss, "Characteristic Fluorescence",
										mProbeDose);
								ctrFl.addXRayListener(mXRayAccumulator3CF);
							}
							if (brFl != null) {
								mXRayAccumulator3BF = new XRayAccumulator3(xrtss, "Bremsstrahlung Fluorescence",
										mProbeDose);
								brFl.addXRayListener(mXRayAccumulator3BF);
							}
							if (mEnableVP)
								sc *= 10.0;
							if (mGenImages && ((ctr != null) || (ctrFl != null))) {
								mEmissionImages3 = new ArrayList<EmissionImage3>();
								for (final XRayTransitionSet xrts : xrtss) {
									final XRayTransition xrt = xrts.getWeighiestTransition();
									final EmissionImage3 ei = new EmissionImage3(256, 256, xrt);
									ei.setXRange(-sc + origin[0], sc + origin[0]);
									ei.setYRange((-0.2 * sc) + origin[2], (1.8 * sc) + origin[2]);
									if (ctr != null)
										ctr.addXRayListener(ei);
									if (ctrFl != null)
										ctrFl.addXRayListener(ei);
									mEmissionImages3.add(ei);
								}
								ti = new TrajectoryImage(1024, 1024, sc);
								ti.setXRange(-sc + origin[0], sc + origin[0]);
								ti.setYRange((-0.2 * sc) + origin[2], (1.8 * sc) + origin[2]);
								ti.setMaxTrajectories(100);
								mc.addActionListener(ti);
							}
							if ((ctr != null) && (mMode == SimulationMode.MCBulk)) {
								final double[] size = Math2.v3(2.0 * sc, 2.0 * sc, 2.0 * sc);
								final int[] dims = new int[] { 200, 200, 200 };
								mVoxelatedDetector = new VoxelatedDetector(origin, size, dims, false);
								mVoxelatedDetector.addShells(xrtss);
								ctr.addXRayListener(mVoxelatedDetector);
							}

						}
						try {
							final File base = DTSA2.getReport().getFile().getParentFile();
							mVrmlFile = File.createTempFile("vrml", ".wrl", base);
							try (final FileWriter vrmlWr = new FileWriter(mVrmlFile)) {
								final TrajectoryVRML vrml = new TrajectoryVRML(mc, vrmlWr);
								vrml.setDisplayBackscatter(false);
								vrml.setDisplayXRayEvent(true);
								vrml.setMaxTrajectories(25);
								vrml.setTrajectoryWidth(Math.max(1.0e-10, Math.min(mScale / 10.0, 5.0e-9)));
								vrml.setMaxRadius(1.0);
								vrml.setEmissive(true);
								vrml.addView("Y-Axis", Math2.plus(origin, Math2.y3(5.0e-6)), origin);
								vrml.addView("Gun", Math2.plus(origin, Math2.z3(-5.0e-6)), origin);
								vrml.addView("X-Axis", Math2.plus(origin, Math2.x3(-5.0e-6)), origin);
								vrml.renderSample();
								final double[] scaleOffset = Math2.v3(Math.max(1.2 * mScale, 2.0e-6), 0,
										-Math.max(1.2 * mScale, 2.0e-6));
								vrml.addScaleMarker(1.0e-6, Math2.plus(origin, scaleOffset));
								mc.addActionListener(vrml);
								// Rotate the sample into the requested orientation
								final double th = Math.acos(-sNorm[2]);
								final double a = -Math.max(Math.min(sNorm[0] / Math.sin(th), 1.0), -1.0);
								final double psi = (th != 0.0 ? Math.acos(a) : 0.0);
								mc.rotate(origin, canRotate ? mRotate : 0.0, th, psi);
								final double[] tr = Transform3D.rotate(Math2.MINUS_Z_AXIS, 0.0, th, psi);
								assert Math2.distance(tr, sNorm) < 1.0e-6;
								// Excute the trajectories
								final int INCREMENT = trajectories / 100;
								for (int i = 0; i < trajectories; i += INCREMENT) {
									if (isCancelled())
										break;
									mc.runMultipleTrajectories(INCREMENT);
									progress(i);
								}
								if (!isCancelled())
									progress(trajectories);
								if (vrmlWr != null)
									vrmlWr.flush();
							}
							mEmissionFiles = null;
							if (mEmissionImages3 != null) {
								mEmissionFiles = new TreeMap<String, String>();
								final Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
								if (writers.hasNext()) {
									final ImageWriter writer = writers.next();
									EmissionImageBase.scaleEmissionImages(mEmissionImages3);
									for (final EmissionImage3 ei : mEmissionImages3) {
										final File f = File.createTempFile("ei(" + ei.getTransition().toString() + ")_",
												".png", base);
										try (final ImageOutputStream ios = ImageIO.createImageOutputStream(f)) {
											writer.setOutput(ios);
											writer.write(ei.getImage());
										}
										mEmissionFiles.put(ei.getTransition().toString(), f.toURI().toURL().toString());
									}
								}
							}
							if (ti != null) {
								final File tf = File.createTempFile("trajectory", ".png", base);
								ti.dump(tf);
								mEmissionFiles.put("Trajectories", tf.toURI().toURL().toString());
							}
							/*
							 * Scale the spectrum from TRAJECTORIES electrons to the requested beam dose.
							 * Update the spectrum properties to reflect the simulated parameters.
							 */
							final double doseScale = mProbeDose / (trajectories * PhysicalConstants.ElectronCharge);
							spec = mDetector.getSpectrum(doseScale);
							rawSpec = mDetector.getRawXRayData(doseScale);
							final SpectrumProperties specProps = spec.getProperties(),
									rawProps = rawSpec.getProperties();
							specProps.addAll(props);
							rawProps.addAll(props);
							SpectrumUtils.rename(spec, specDesc);
							SpectrumUtils.rename(rawSpec, "Emitted[" + specDesc + "]");
							if (mMode == SimulationMode.MCBulk) {
								specProps.setCompositionProperty(SpectrumProperties.StandardComposition,
										mSubstrateMaterial);
								rawProps.setCompositionProperty(SpectrumProperties.StandardComposition,
										mSubstrateMaterial);
							}
							if ((mMode == SimulationMode.MCFilm) && (mSubstrateMaterial.equals(Material.Null))) {
								specProps.setCompositionProperty(SpectrumProperties.StandardComposition,
										mObjectMaterial);
								specProps.setSampleShape(SpectrumProperties.SampleShape,
										new SampleShape.ThinFilm(sNorm, mScale));
								specProps.setNumericProperty(SpectrumProperties.MassThickness,
										FromSI.cm(mScale) * FromSI.gPerCC(mObjectMaterial.getDensity()) * 1.0e6); // &mu;g/cm<sup>2</sup>
								rawProps.setCompositionProperty(SpectrumProperties.StandardComposition,
										mObjectMaterial);
								rawProps.setSampleShape(SpectrumProperties.SampleShape,
										new SampleShape.ThinFilm(sNorm, mScale));
								rawProps.setNumericProperty(SpectrumProperties.MassThickness,
										FromSI.cm(mScale) * FromSI.gPerCC(mObjectMaterial.getDensity()) * 1.0e6); // &mu;g/cm<sup>2</sup>
							}
						} catch (final Throwable ex) {
							setExceptionText(ex);
						}

					}

				} catch (final Throwable ex) {
					setExceptionText(ex);
					return null;
				}
			try {
				ISpectrumData[] res = null;
				if (spec != null)
					if (mReplicas > 0) {
						res = new ISpectrumData[mReplicas + (rawSpec != null ? 1 : 0)];
						for (int i = 0; i < mReplicas; ++i) {
							res[i] = SpectrumUtils.addNoiseToSpectrum(spec, 1.0);
							SpectrumUtils.rename(res[i], res[i].toString() + " #" + Integer.toString(i + 1));
						}
						if (rawSpec != null)
							res[mReplicas] = rawSpec;
					} else {
						res = new ISpectrumData[rawSpec != null ? 2 : 1];
						res[0] = spec;
						if (rawSpec != null)
							res[1] = rawSpec;
					}
				return res;
			} catch (final Throwable ex) {
				setExceptionText(ex);
				return null;
			}
		}

		protected int computeTrajectoryCount() {
			final boolean thin = ((mMode == SimulationMode.MCSphere) || (mMode == SimulationMode.MCCube)
					|| (mMode == SimulationMode.MCFilm) || (mMode == SimulationMode.MCInclusion))
					&& ((mScale * mObjectMaterial.getDensity()) < (1.0e-6 * ToSI.gPerCC(10.0)))
					&& mSubstrateMaterial.equals(Material.Null);
			final int trajectories = (thin
					? (int) Math.round(
							ElectronRange.KanayaAndOkayama1972.computeMeters(mObjectMaterial, mBeamEnergy) / mScale)
							* 1000
					: 1000) * mExtraElectrons
					* (int) Math.round(Math2.bound(Math.sqrt(mProbeDose * FromSI.NANO / 60.0), 1.0, 16.0));
			return trajectories;
		}

		@Override
		protected void process(java.util.List<Integer> chunks) {
			final int max = chunks.get(chunks.size() - 1);
			mProgress.setProgress(max);
		}

		@Override
		protected void done() {
			enableFinish(true);
		}
	};

	public class SimMode extends JWizardPanel {
		static private final long serialVersionUID = 0x34;

		private final ButtonGroup mModeButtonGroup = new ButtonGroup();
		private final JRadioButton mAnalytical = new JRadioButton("Analytical model of a bulk, homogeneous material");
		private final JRadioButton mMCBulk = new JRadioButton("Monte Carlo model of a bulk, homogeneous material");
		private final JRadioButton mMCFilm = new JRadioButton(
				"Monte Carlo model of a film on a bulk, homogeneous substrate");
		private final JRadioButton mMCSphere = new JRadioButton(
				"Monte Carlo model of a sphere on a bulk, homogeneous substrate");
		private final JRadioButton mMCCube = new JRadioButton(
				"Monte Carlo model of a cube on a bulk, homogeneous substrate");
		private final JRadioButton mMCInclusion = new JRadioButton(
				"Monte Carlo model of an inclusion in a bulk, homogeneous substrate");
		private final JRadioButton mMCInterface = new JRadioButton("Monte Carlo model of a beam near an interface");
		private final JRadioButton mMCSquarePyramid = new JRadioButton(
				"Monte Carlo model of a pyramid with a square base");
		private final JRadioButton mMCCylinderSide = new JRadioButton("Monte Carlo model of a cylinder on its side");
		private final JRadioButton mMCCylinderTop = new JRadioButton("Monte Carlo model of a cylinder on end");
		private final JRadioButton mMCHemisphere = new JRadioButton("Monte Carlo model of a hemispherical cap");
		private final JRadioButton mMCRectangularPrism = new JRadioButton("Monte Carlo model of a block");
		private final JRadioButton mMCTriangularPrism = new JRadioButton("Monte Carlo model of an equilateral prism");

		private SimMode(SimulationWizard wiz) {
			super(wiz);
			initialize();
			mSession = DTSA2.getSession();
		}

		private void initialize() {
			final FormLayout fl = new FormLayout("5dlu, pref",
					"pref, 1dlu, pref, 1dlu, pref, 1dlu, pref, 1dlu, pref, 1dlu, pref, 1dlu, pref, 1dlu, pref, 1dlu, pref, 1dlu, pref, 1dlu, pref, 1dlu, pref, 1dlu, pref, 1dlu, pref");
			final PanelBuilder pb = new PanelBuilder(fl);
			final CellConstraints cc = new CellConstraints();
			pb.add(mAnalytical, cc.xy(2, 1));
			pb.add(mMCBulk, cc.xy(2, 3));
			pb.add(mMCFilm, cc.xy(2, 5));
			pb.add(mMCSphere, cc.xy(2, 7));
			pb.add(mMCCube, cc.xy(2, 9));
			pb.add(mMCInclusion, cc.xy(2, 11));
			pb.add(mMCInterface, cc.xy(2, 13));
			pb.add(mMCSquarePyramid, cc.xy(2, 15));
			pb.add(mMCCylinderSide, cc.xy(2, 17));
			pb.add(mMCCylinderTop, cc.xy(2, 19));
			pb.add(mMCHemisphere, cc.xy(2, 21));
			pb.add(mMCRectangularPrism, cc.xy(2, 23));
			pb.add(mMCTriangularPrism, cc.xy(2, 25));

			mModeButtonGroup.add(mAnalytical);
			mModeButtonGroup.add(mMCBulk);
			mModeButtonGroup.add(mMCFilm);
			mModeButtonGroup.add(mMCSphere);
			mModeButtonGroup.add(mMCCube);
			mModeButtonGroup.add(mMCInclusion);
			mModeButtonGroup.add(mMCInterface);
			mModeButtonGroup.add(mMCSquarePyramid);
			mModeButtonGroup.add(mMCCylinderSide);
			mModeButtonGroup.add(mMCCylinderTop);
			mModeButtonGroup.add(mMCHemisphere);
			mModeButtonGroup.add(mMCRectangularPrism);
			mModeButtonGroup.add(mMCTriangularPrism);

			mAnalytical.setSelected(true);
			mAnalytical.setToolTipText("<HTML>Generate a simulated spectrum using a fast analytical expression.");
			mMCBulk.setToolTipText(
					"<HTML>Generate a simulated spectrum for a flat, polished sample using a Monte Carlo model.");
			mMCFilm.setToolTipText(
					"<HTML>Generate a simulated spectrum for a a flat, polished film over bulk sample using a Monte Carlo model.");
			mMCSphere.setToolTipText(
					"<HTML>Generate a simulated spectrum for a spherical sample on a bulk substrate using a Monte Carlo model.");
			mMCCube.setToolTipText(
					"<HTML>Generate a simulated spectrum for a cubic sample on a bulk substrate using a Monte Carlo model.");
			mMCInclusion.setToolTipText(
					"<HTML>Generate a simulated spectrum for an inclusion on the surface of bulk sustrate using a Monte Carlo model.");
			mMCInterface.setToolTipText(
					"<HTML>Generate a simulated spectrum for an interface between two materials using a Monte Carlo model.");
			mMCSquarePyramid.setToolTipText("<HTML>Generate a simulated spectrum for a pyramid with a square base.");
			mMCCylinderSide
					.setToolTipText("<HTML>Generate a simulated spectrum for a cylinder on its side (like a fiber.)");
			mMCCylinderTop.setToolTipText("<HTML>Generate a simulated spectrum for a cylider on its end (like a can.)");
			mMCHemisphere.setToolTipText(
					"<HTML>Generate a simulated spectrum for a hemispherical cap resting on a substrate.");
			mMCRectangularPrism.setToolTipText(
					"<HTML>Generate a simulated spectrum for a block with a square base and independent height.");
			mMCTriangularPrism.setToolTipText("<HTML>Generate a simulated spectrum for a equilateral prism");

			final Preferences userPref = Preferences.userNodeForPackage(SimulationWizard.class);
			int mode = userPref.getInt("SimMode", SimulationMode.AnalyticalBulk.ordinal());
			setMode(SimulationMode.values()[Math2.bound(mode, 0, SimulationMode.values().length)]);
			JPanel base = new JPanel();
			base.setLayout(new FormLayout("300dlu", "155dlu"));
			base.add(new JScrollPane(pb.getPanel()), cc.xy(1, 1));
			this.add(base);
		}

		@Override
		public void onShow() {
			getWizard().setMessageText("Select the type of spectrum simulation to perform.");
			getWizard().setNextPanel(mGeometry, "Configure sample");
			getWizard().enableFinish(false);
		}

		@Override
		public boolean permitNext() {
			return true;
		}

		@Override
		public void onHide() {
			mThread.mMode = getMode();
			final Preferences userPref = Preferences.userNodeForPackage(SimulationWizard.class);
			userPref.putInt("SimMode", mThread.mMode.ordinal());
		}

		SimulationMode getMode() {
			SimulationMode res = SimulationMode.AnalyticalBulk;
			if (mMCBulk.isSelected())
				res = SimulationMode.MCBulk;
			if (mMCFilm.isSelected())
				res = SimulationMode.MCFilm;
			if (mMCSphere.isSelected())
				res = SimulationMode.MCSphere;
			if (mMCCube.isSelected())
				res = SimulationMode.MCCube;
			if (mMCInclusion.isSelected())
				res = SimulationMode.MCInclusion;
			if (mMCInterface.isSelected())
				res = SimulationMode.MCInterface;
			if (mMCCylinderSide.isSelected())
				res = SimulationMode.MCCylinderOnSide;
			if (mMCCylinderTop.isSelected())
				res = SimulationMode.MCCylinderOnEnd;
			if (mMCHemisphere.isSelected())
				res = SimulationMode.MCHemisphere;
			if (mMCRectangularPrism.isSelected())
				res = SimulationMode.MCRectangularPrism;
			if (mMCSquarePyramid.isSelected())
				res = SimulationMode.MCSquarePyramid;
			if (mMCTriangularPrism.isSelected())
				res = SimulationMode.MCTriangularPrism;
			return res;
		}

		void setMode(SimulationMode mode) {
			switch (mode) {
			case AnalyticalBulk:
				mAnalytical.setSelected(true);
				break;
			case MCBulk:
				mMCBulk.setSelected(true);
				break;
			case MCCube:
				mMCCube.setSelected(true);
				break;
			case MCCylinderOnEnd:
				mMCCylinderTop.setSelected(true);
				break;
			case MCCylinderOnSide:
				mMCCylinderSide.setSelected(true);
				break;
			case MCFilm:
				mMCFilm.setSelected(true);
				break;
			case MCHemisphere:
				mMCHemisphere.setSelected(true);
				break;
			case MCInclusion:
				mMCInclusion.setSelected(true);
				break;
			case MCInterface:
				mMCInterface.setSelected(true);
				break;
			case MCRectangularPrism:
				mMCRectangularPrism.setSelected(true);
				break;
			case MCSphere:
				mMCSphere.setSelected(true);
				break;
			case MCSquarePyramid:
				mMCSquarePyramid.setSelected(true);
				break;
			case MCTriangularPrism:
				mMCTriangularPrism.setSelected(true);
				break;
			}
		}
	}

	public class SimConfiguration extends JWizardPanel {
		static private final long serialVersionUID = 0x45;

		private final JTextField jTextField_BeamEnergy = new JTextField();
		private final JTextField jTextField_ProbeDose = new JTextField();
		private final JTextField jTextField_IncidentAngle = new JTextField();
		private final JComboBox<ElectronProbe> jComboBox_Instrument = new JComboBox<ElectronProbe>();
		private final JComboBox<DetectorProperties> jComboBox_Detector = new JComboBox<DetectorProperties>();
		private final JComboBox<DetectorCalibration> jComboBox_Calibration = new JComboBox<DetectorCalibration>();
		private final NumberFormat mFormatter = new HalfUpFormat("0.0");

		private SimConfiguration(SimulationWizard sw) {
			super(sw);
			initialize();
		}

		@Override
		public void onHide() {
			// Save current values as preferences
			final Preferences userPref = Preferences.userNodeForPackage(SimulationWizard.class);
			userPref.putDouble("Beam energy", mThread.mBeamEnergy);
			userPref.putDouble("Probe dose", mThread.mProbeDose);
			userPref.putDouble("Incident angle", Math.toDegrees(mThread.mIncidentAngle));
			if ((jComboBox_Detector.getSelectedItem() instanceof DetectorProperties)
					&& (jComboBox_Calibration.getSelectedItem() instanceof EDSCalibration)) {
				mThread.mDetector = getEDSDetector();
				userPref.put("Calibration", mThread.mDetector.getCalibration().toString());
			}
		}

		private void initialize() {
			// Get previous values
			final Preferences userPref = Preferences.userNodeForPackage(SimulationWizard.class);
			mThread.mBeamEnergy = userPref.getDouble("Beam energy", ToSI.keV(20.0));
			mThread.mProbeDose = userPref.getDouble("Probe dose", 60.0 * ToSI.NANO); // 1
			// nA
			mThread.mIncidentAngle = Math.toRadians(userPref.getDouble("Incident angle", 0.0));
			final DetectorProperties dp = AppPreferences.getInstance().getDefaultDetector();
			if (dp != null) {
				EDSDetector xrd = null;
				final String calStr = userPref.get("Calibration", null);
				for (final DetectorCalibration dc : mSession.getCalibrations(dp))
					if (dc.toString().equals(calStr) && (dc instanceof EDSCalibration)) {
						xrd = EDSDetector.createDetector(dp, (EDSCalibration) dc);
						break;
					}
				if (xrd == null) {
					final DetectorCalibration dc = mSession.getSuitableCalibration(dp,
							new Date(System.currentTimeMillis()));
					if (dc instanceof EDSCalibration)
						xrd = EDSDetector.createDetector(dp, (EDSCalibration) dc);
				}
				if (xrd != null)
					mThread.mDetector = xrd;
			}
			// Initialize panel
			final FormLayout fl = new FormLayout("5dlu, right:pref, 5dlu, 50dlu, 3dlu, pref, 50dlu, 5dlu",
					"pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref");
			final PanelBuilder pb = new PanelBuilder(fl, this);
			final CellConstraints cc = new CellConstraints();
			pb.addSeparator("Instrument Parameters", cc.xyw(1, 1, 7));

			pb.addLabel("Instrument", cc.xy(2, 3));
			pb.add(jComboBox_Instrument, cc.xyw(4, 3, 4));
			final DefaultComboBoxModel<ElectronProbe> dcbm = new DefaultComboBoxModel<ElectronProbe>(
					mSession.getCurrentProbes().toArray(new ElectronProbe[0]));
			jComboBox_Instrument.setModel(dcbm);

			jComboBox_Instrument.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					updateDetectors(null);
				}
			});

			pb.addLabel("Detector", cc.xy(2, 5));
			jComboBox_Detector.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					updateCalibrations(null);
				}
			});
			pb.add(jComboBox_Detector, cc.xyw(4, 5, 4));
			jComboBox_Detector.setToolTipText("<HTML>Specifying the detector specifies the geometry, crystal and window"
					+ "<br/>characteristics to emulate a specific instrument and detector.");

			pb.addLabel("Calibration", cc.xy(2, 7));
			pb.add(jComboBox_Calibration, cc.xyw(4, 7, 4));
			jComboBox_Calibration
					.setToolTipText("<HTML>Specify the performance characteristics of the chosen detector.");

			updateDetectors(null);

			pb.addLabel("Beam Energy", cc.xy(2, 9));
			pb.add(jTextField_BeamEnergy, cc.xy(4, 9));
			jTextField_BeamEnergy.setToolTipText("<HTML>Specify the incident electron beam energy in keV.");
			pb.addLabel("keV", cc.xy(6, 9));

			pb.addLabel("Probe Dose (current\u00B7time)", cc.xy(2, 11));
			jTextField_ProbeDose
					.setToolTipText("<HTML>Specify the product of the probe current and live time as the probe dose.");
			pb.add(jTextField_ProbeDose, cc.xy(4, 11));
			pb.addLabel("nA\u00B7second", cc.xy(6, 11));

			pb.addLabel("Incident Angle", cc.xy(2, 13));
			pb.add(jTextField_IncidentAngle, cc.xy(4, 13));
			jTextField_IncidentAngle.setToolTipText(
					"<HTML>0&deg; represents the sample normal to the beam. Positive tilt<br />is towards azimuth 0&deg;. Negative tilt is towards azimuth 180&deg;");
			pb.addLabel("\u00B0", cc.xy(6, 13));
		}

		private void updateDetectors(EDSDetector det) {
			if (det != null) {
				jComboBox_Instrument.setSelectedItem(det.getOwner());
				final Set<DetectorProperties> dets = mSession.getDetectors(det.getOwner());
				final DefaultComboBoxModel<DetectorProperties> dcbm = new DefaultComboBoxModel<DetectorProperties>(
						dets.toArray(new DetectorProperties[0]));
				dcbm.setSelectedItem(det.getDetectorProperties());
				jComboBox_Detector.setModel(dcbm);
				updateCalibrations(det);
			} else if (jComboBox_Instrument.getSelectedItem() instanceof ElectronProbe) {
				final Object selDet = jComboBox_Detector.getSelectedItem();
				final ElectronProbe ep = (ElectronProbe) jComboBox_Instrument.getSelectedItem();
				final Set<DetectorProperties> dets = mSession.getDetectors(ep);
				final DefaultComboBoxModel<DetectorProperties> dcbm = new DefaultComboBoxModel<DetectorProperties>(
						dets.toArray(new DetectorProperties[0]));
				if (dets.contains(selDet))
					dcbm.setSelectedItem(selDet);
				else
					dcbm.setSelectedItem(dets.size() > 0 ? dets.iterator().next() : null);
				jComboBox_Detector.setModel(dcbm);
				updateCalibrations(det);
			} else
				jComboBox_Detector.removeAll();
		}

		@Override
		public boolean permitNext() {
			jTextField_BeamEnergy.setBackground(SystemColor.window);
			jTextField_IncidentAngle.setBackground(SystemColor.window);
			mThread.mDetector = getEDSDetector();
			boolean res = (mThread.mDetector != null);
			final NumberFormat nf = NumberFormat.getInstance();
			try {
				mThread.mBeamEnergy = ToSI.keV(nf.parse(jTextField_BeamEnergy.getText()).doubleValue());
				if ((mThread.mBeamEnergy > ToSI.keV(1000.0)) || (mThread.mBeamEnergy < ToSI.keV(0.1))) {
					getWizard().setErrorText("Beam energy must be between 100 eV and 1 MeV.");
					res = false;
				}
			} catch (final ParseException e) {
				getWizard().setErrorText("Error parsing the beam energy");
				jTextField_BeamEnergy.setText(mFormatter.format(FromSI.keV(mThread.mBeamEnergy)));
				jTextField_BeamEnergy.setBackground(Color.PINK);
				res = false;
			}
			if (res)
				try {
					mThread.mProbeDose = ToSI.NANO * nf.parse(jTextField_ProbeDose.getText()).doubleValue();
					if ((mThread.mProbeDose < 1.0e-12) || (mThread.mProbeDose > 1.0)) {
						getWizard().setErrorText("The dose must be between 1 pA\u00B0second and 1 A\u00B0second.");
						res = false;
					}
				} catch (final ParseException e) {
					getWizard().setErrorText("Error parsing the probe dose.");
					jTextField_ProbeDose.setText(mFormatter.format(FromSI.NANO * mThread.mProbeDose));
					jTextField_ProbeDose.setBackground(Color.PINK);
					res = false;
				}
			if (res)
				try {
					mThread.mIncidentAngle = Math.toRadians(nf.parse(jTextField_IncidentAngle.getText()).doubleValue());
					if ((mThread.mIncidentAngle <= Math.toRadians(-90.0))
							|| (mThread.mIncidentAngle >= Math.toRadians(90.0))) {
						getWizard().setErrorText("The incident angle must be between -90\u00B0 and 90\u00B0.");
						res = false;
					}
				} catch (final ParseException e) {
					getWizard().setErrorText("Error parsing the incident angle");
					jTextField_IncidentAngle.setText(mFormatter.format(Math.toDegrees(mThread.mIncidentAngle)));
					jTextField_IncidentAngle.setBackground(Color.PINK);
					res = false;
				}
			return res;
		}

		private EDSDetector getEDSDetector() {
			final DetectorProperties dp = (DetectorProperties) jComboBox_Detector.getSelectedItem();
			final EDSCalibration calib = (EDSCalibration) jComboBox_Calibration.getSelectedItem();
			return (dp != null) && (calib != null) ? EDSDetector.createDetector(dp, calib) : null;
		}

		private void updateCalibrations(EDSDetector det) {
			Object last = det != null ? last = det.getCalibration() : jComboBox_Calibration.getSelectedItem();
			if (jComboBox_Detector.getSelectedItem() instanceof DetectorProperties) {
				final DetectorProperties dp = (DetectorProperties) jComboBox_Detector.getSelectedItem();
				final List<DetectorCalibration> cals = mSession.getCalibrations(dp);
				final DefaultComboBoxModel<DetectorCalibration> dcbm = new DefaultComboBoxModel<DetectorCalibration>(
						cals.toArray(new DetectorCalibration[0]));
				if (cals.contains(last))
					dcbm.setSelectedItem(last);
				else
					dcbm.setSelectedItem(mSession.getSuitableCalibration(dp, new Date(System.currentTimeMillis())));
				jComboBox_Calibration.setModel(dcbm);
			} else
				jComboBox_Calibration.removeAll();
		}

		@Override
		public void onShow() {
			jTextField_BeamEnergy.setText(mFormatter.format(FromSI.keV(mThread.mBeamEnergy)));
			jTextField_ProbeDose.setText(mFormatter.format(FromSI.NANO * mThread.mProbeDose));
			jTextField_IncidentAngle.setText(mFormatter.format(Math.toDegrees(mThread.mIncidentAngle)));
			updateDetectors(mThread.mDetector);
			updateCalibrations(mThread.mDetector);
			getWizard().setNextPanel(mOther, "Other options");
			getWizard().enableFinish(false);
		}
	}

	public class SimVP extends JWizardPanel {

		private final JCheckBox jCheckBox_Enable = new JCheckBox("Enable variable pressure");
		private final JComboBox<VPGas> jComboBox_Gas = new JComboBox<VPGas>();
		private final JTextField jTextField_Length = new JTextField();
		private final JTextField jTextField_Pressure = new JTextField();
		private JPanel jPanel_VP = null;

		public SimVP(JWizardDialog wiz) {
			super(wiz);
			initialize();
		}

		private void initialize() {
			final CellConstraints cc0 = new CellConstraints(), cc1 = new CellConstraints();
			setLayout(new FormLayout("200dlu", "pref, 10dlu, pref"));
			this.add(jCheckBox_Enable, cc0.xy(1, 1));

			final FormLayout fl = new FormLayout("5dlu, right:pref, 5dlu, 50dlu, 5dlu, left:pref, 5dlu",
					"pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref");
			final PanelBuilder pb = new PanelBuilder(fl);
			pb.addLabel("&Gas", cc0.xy(2, 1));
			pb.add(new JLabel("Gas"), cc0.xy(2, 1), jComboBox_Gas, cc1.xyw(4, 1, 3));
			pb.add(new JLabel("Path length"), cc0.xy(2, 3), jTextField_Length, cc1.xy(4, 3));
			jTextField_Length.setToolTipText(
					"The z-distance the electron travels in the gas before intersecting the optimal working distance.");
			pb.addLabel("mm", cc0.xy(6, 3));
			pb.add(new JLabel("Pressure"), cc0.xy(2, 5), jTextField_Pressure, cc1.xy(4, 5));
			jTextField_Pressure.setToolTipText("The gas pressure in pascal (1 Torr ~ 133 pascal)");
			pb.addLabel("pascal", cc0.xy(6, 5));
			jPanel_VP = pb.getPanel();
			jPanel_VP.setBorder(DTSA2.createTitledBorder("Variable Pressure Options"));
			this.add(jPanel_VP, cc0.xy(1, 3));

			jCheckBox_Enable.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					enableVP(jCheckBox_Enable.isSelected());
				}
			});
			final DefaultComboBoxModel<VPGas> model = new DefaultComboBoxModel<VPGas>(VPGas.values());
			jComboBox_Gas.setModel(model);
		}

		double validateDouble(JTextField tf, double min, double max, String errMsg) {
			tf.setBackground(SystemColor.window);
			final NumberFormat nf = NumberFormat.getInstance();
			double res = Double.NaN;
			try {
				res = nf.parse(tf.getText()).doubleValue();
				if ((res < min) || (res > max)) {
					getWizard().setErrorText(errMsg);
					res = Double.NaN;
					tf.setBackground(Color.PINK);
				}
			} catch (final ParseException e) {
				res = Double.NaN;
				getWizard().setErrorText("Misformed number");
				tf.setBackground(Color.PINK);
			}
			return res;
		}

		public void enableVP(boolean b) {
			jCheckBox_Enable.setSelected(b);
			jComboBox_Gas.setEnabled(b);
			jTextField_Length.setEnabled(b);
			jTextField_Pressure.setEnabled(b);
		}

		@Override
		public void onShow() {
			final Preferences userPref = Preferences.userNodeForPackage(SimulationWizard.class);
			final boolean vp = userPref.getBoolean("EnableVP", false);
			jCheckBox_Enable.setSelected(vp);
			enableVP(vp);
			jComboBox_Gas.setSelectedIndex(userPref.getInt("Gas", 0));
			final NumberFormat df = new HalfUpFormat("0.000");
			jTextField_Length.setText(df.format(userPref.getDouble("Path Length", 10.0)));
			jTextField_Pressure.setText(df.format(userPref.getDouble("Pressure", 100.0)));
			getWizard().setNextPanel(mProgress, "Perform Simulation");
			getWizard().enableFinish(false);
		}

		@Override
		public boolean permitNext() {
			final double path = validateDouble(jTextField_Length, 0.001, 100.0,
					"Valid path lengths are between 0.001 mm and 100.0 mm.");
			final double pressure = validateDouble(jTextField_Pressure, 1.0e-7, 1.0e6,
					"Valid pressures are between 1.0e-7 and 1.0e6 pascal.");
			return !(Double.isNaN(path) || Double.isNaN(pressure));
		}

		@Override
		public void onHide() {
			final Preferences userPref = Preferences.userNodeForPackage(SimulationWizard.class);
			mThread.mEnableVP = jCheckBox_Enable.isSelected();
			userPref.putBoolean("EnableVP", jCheckBox_Enable.isSelected());
			mThread.mVPGas = (SimulationWizard.VPGas) jComboBox_Gas.getSelectedItem();
			userPref.putInt("Gas", jComboBox_Gas.getSelectedIndex());
			final double pathLen = validateDouble(jTextField_Length, 0.001, 100.0,
					"Valid path lengths are between 0.001 mm and 100.0 mm.");
			if (!Double.isNaN(pathLen)) {
				userPref.putDouble("Path Length", pathLen);
				mThread.mVPPathLength = 0.001 * pathLen; // mm to meters
			}
			mThread.mVPPressure = validateDouble(jTextField_Pressure, 1.0e-7, 1.0e6,
					"Valid pressures are between 1.0e-7 and 1.0e6 pascal.");
			if (!Double.isNaN(mThread.mVPPressure))
				userPref.putDouble("Pressure", mThread.mVPPressure);
		}

		private static final long serialVersionUID = -2608824770868058496L;

	};

	public class SimOther extends JWizardPanel {

		private static final long serialVersionUID = -303750288614337274L;
		private final JCheckBox jCheckBox_AddNoise = new JCheckBox("Apply simulated count statistics");
		private final JTextField jTextField_Count = new JTextField("Instance count");
		private final JComboBox<String> jComboBox_Extra = new JComboBox<String>();
		private final JCheckBox jCheckBox_CharFluor = new JCheckBox("Characteristic secondary");
		private final JCheckBox jCheckBox_BremFluor = new JCheckBox("Bremsstrahlung secondary");
		private final JCheckBox jCheckBox_Bremsstrahlung = new JCheckBox("Bremsstrahlung primary");
		private final JCheckBox jCheckBox_Characteristic = new JCheckBox("Characteristic primary");

		private int mReplicas = 1;

		public SimOther(SimulationWizard wiz) {
			super(wiz);
			initialize();
		}

		private void initialize() {
			final FormLayout fl = new FormLayout("5dlu, pref, 5dlu, 50dlu, 30dlu, pref, 5dlu",
					"pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref");
			final PanelBuilder pb = new PanelBuilder(fl, this);
			final CellConstraints cc = new CellConstraints();
			pb.addSeparator("Noise parameters", cc.xyw(1, 1, 6));
			pb.add(jCheckBox_AddNoise, cc.xyw(2, 3, 5));
			jCheckBox_AddNoise.setToolTipText(
					"<HTML>Check this box to add pseudo-randomly generate Poisson noise to the simulated spectrum.");
			jCheckBox_AddNoise.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					jTextField_Count.setEnabled(jCheckBox_AddNoise.isSelected());
				}
			});
			pb.addLabel("Instance count", cc.xy(2, 5));
			jTextField_Count.setToolTipText(
					"<HTML>This number of spectra will be created each with slightly different count statistics.");
			pb.add(jTextField_Count, cc.xy(4, 5));
			for (int i = 0; i < 5; ++i)
				if (i == 0)
					jComboBox_Extra.addItem("Run the default number of electron trajectories");
				else
					jComboBox_Extra
							.addItem("Run " + Integer.toString(1 << (2 * i)) + "\u00D7 as many electron trajectories");
			jComboBox_Extra.setToolTipText(
					"<html>The simulation time scales linearly with the number of electron trajectories<br/>but the spectrum quality scales as the square root.  Running more electrons<br/>doesn't change the number of x-rays in the spectrum.");
			pb.add(jComboBox_Extra, cc.xyw(2, 7, 5));
			pb.addSeparator("X-ray generation modes", cc.xyw(1, 9, 6));
			pb.add(jCheckBox_Characteristic, cc.xyw(2, 11, 3));
			pb.add(jCheckBox_Bremsstrahlung, cc.xyw(6, 11, 1));
			pb.add(jCheckBox_CharFluor, cc.xyw(2, 13, 3));
			pb.add(jCheckBox_BremFluor, cc.xyw(6, 13, 1));
		}

		@Override
		public void onHide() {
			// Save current values as preferences
			final Preferences userPref = Preferences.userNodeForPackage(SimulationWizard.class);
			userPref.putBoolean("Noise", jCheckBox_AddNoise.isSelected());
			userPref.putBoolean("Characteristic", jCheckBox_Characteristic.isSelected());
			userPref.putBoolean("Bremsstrahlung", jCheckBox_Bremsstrahlung.isSelected());
			userPref.putBoolean("CharFluor", jCheckBox_CharFluor.isSelected());
			userPref.putBoolean("BremFluor", jCheckBox_BremFluor.isSelected());
			userPref.putInt("Replicas", mReplicas);
			mThread.mReplicas = jCheckBox_AddNoise.isSelected() ? mReplicas : 0;
			mThread.mGenImages = true;
			mThread.mExtraElectrons = 1 << (2 * jComboBox_Extra.getSelectedIndex());
			mThread.mCharacteristic = jCheckBox_Characteristic.isSelected();
			mThread.mBremsstrahlung = jCheckBox_Bremsstrahlung.isSelected();
			mThread.mCharFluor = jCheckBox_CharFluor.isSelected();
			mThread.mBremFluor = jCheckBox_BremFluor.isSelected();
		}

		@Override
		public void onShow() {
			final Preferences userPref = Preferences.userNodeForPackage(SimulationWizard.class);
			jCheckBox_AddNoise.setSelected(userPref.getBoolean("Noise", true));
			if (mThread.mMode == SimulationMode.AnalyticalBulk) {
				jCheckBox_Characteristic.setSelected(true);
				jCheckBox_Characteristic.setEnabled(false);
			} else
				jCheckBox_Characteristic.setSelected(userPref.getBoolean("Characteristic", true));
			jCheckBox_Bremsstrahlung.setSelected(userPref.getBoolean("Bremsstrahlung", true));
			jCheckBox_CharFluor.setSelected(userPref.getBoolean("CharFluor", false));
			jCheckBox_CharFluor.setVisible(mThread.mMode != SimulationMode.AnalyticalBulk);
			jCheckBox_BremFluor.setSelected(userPref.getBoolean("BremFluor", false));
			jCheckBox_BremFluor.setVisible(mThread.mMode != SimulationMode.AnalyticalBulk);
			mReplicas = userPref.getInt("Replicas", 10);
			jTextField_Count.setText(Integer.toString(mReplicas));
			jTextField_Count.setEnabled(jCheckBox_AddNoise.isSelected());
			jComboBox_Extra.setSelectedIndex(Integer.highestOneBit(mThread.mExtraElectrons) / 2);
			if (mThread.mMode == SimulationMode.AnalyticalBulk)
				getWizard().setNextPanel(mProgress, "Perform Simulation");
			else
				getWizard().setNextPanel(mVariablePressure, "Configure VP");
			getWizard().enableFinish(false);
		}

		@Override
		public boolean permitNext() {
			boolean res = true;
			if (jCheckBox_AddNoise.isSelected())
				try {
					mReplicas = Integer.parseInt(jTextField_Count.getText());
					if ((mReplicas < 1) || (mReplicas > 1000)) {
						mReplicas = Math.max(Math.min(mReplicas, 1000), 1);
						getWizard().setErrorText("The instance count must be between 1 and 1000.");
						jTextField_Count.setText(Integer.toString(mReplicas));
					}
				} catch (final NumberFormatException nfx) {
					getWizard().setErrorText("Unable to parse the instance count.");
					jTextField_Count.setText(Integer.toString(mReplicas));
					jTextField_Count.setBackground(Color.PINK);
					res = false;
				}
			return res;
		}
	}

	public class SimGeometry extends JWizardPanel {
		static private final long serialVersionUID = 0x45;

		private JLabel jLabel_Object;
		private final JTextField jTextField_ObjectMaterial = new JTextField();
		private JLabel jLabel_Substrate;
		private final JTextField jTextField_SubstrateMaterial = new JTextField();
		private Color mMatBackColor;
		private final JButton jButton_ObjectMaterial = new JButton("Edit");
		private final JButton jButton_SubstrateMaterial = new JButton("Edit");
		private final JButton jButton_NoSubstrate = new JButton("None");
		private JLabel jLabel_Scale;
		private JLabel jLabel_Scale2;
		private JLabel jLabel_Micron;
		private JLabel jLabel_Micron2;
		private JLabel jLabel_Rotate;
		private JLabel jLabel_Degree;

		private final JTextField jTextField_Scale = new JTextField();
		private final JTextField jTextField_Scale2 = new JTextField();
		private final JTextField jTextField_Rotate = new JTextField();
		private final JCheckBox jCheckBox_Overscan = new JCheckBox("Overscan particle");
		private final NumberFormat mFormatter = new HalfUpFormat("0.000");

		private SimGeometry(SimulationWizard sw) {
			super(sw);
			initialize();
		}

		@Override
		public void onHide() {
			// Save current values as preferences
			final EPQXStream xs = EPQXStream.getInstance();
			final Preferences userPref = Preferences.userNodeForPackage(SimulationWizard.class);
			userPref.putDouble("Scale", mThread.mScale);
			userPref.putDouble("Scale2", mThread.mScale2);
			userPref.putDouble("Rotation", Math.toDegrees(mThread.mRotate));
			userPref.putBoolean("Overscan", mThread.mOverscan);
			final String keyObj = "Object material";
			try {
				final String obj = xs.toXML(mThread.mObjectMaterial);
				userPref.put(keyObj, obj);
			} catch (final IllegalArgumentException iae) {
				userPref.remove(keyObj);
			}
			final String keySubs = "Substrate material";
			try {
				final String subs = xs.toXML(mThread.mSubstrateMaterial);
				userPref.put(keySubs, subs);
			} catch (final IllegalArgumentException iae) {
				userPref.remove(keySubs);
			}
		}

		private void initialize() {
			// Get previous values
			final Preferences userPref = Preferences.userNodeForPackage(SimulationWizard.class);
			try {
				final EPQXStream xs = EPQXStream.getInstance();
				mThread.mObjectMaterial = (Material) xs.fromXML(
						userPref.get("Object material", xs.toXML(MaterialFactory.createPureElement(Element.Cu))));
				mThread.mSubstrateMaterial = (Material) xs.fromXML(
						userPref.get("Substrate material", xs.toXML(MaterialFactory.createPureElement(Element.C))));
			} catch (final EPQException e1) {
				mThread.mObjectMaterial = Material.Null;
				mThread.mSubstrateMaterial = Material.Null;
			}
			mThread.mScale = userPref.getDouble("Scale", 1.0e-6);
			mThread.mScale2 = userPref.getDouble("Scale2", 1.0e-6);
			mThread.mRotate = Math.toRadians(userPref.getDouble("Rotation", 0.0));
			mThread.mOverscan = userPref.getBoolean("Overscan", false);
			// Initialize panel
			final FormLayout fl = new FormLayout("5dlu, right:pref, 5dlu, 50dlu, 3dlu, pref, 3dlu, pref",
					"pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref");
			final PanelBuilder pb = new PanelBuilder(fl, this);
			final CellConstraints cc = new CellConstraints();
			pb.addSeparator("Materials and Scale", cc.xyw(1, 1, 8));

			jLabel_Substrate = pb.addLabel("Substrate / matrix material", cc.xy(2, 3));
			jLabel_Substrate.setDisplayedMnemonic(KeyEvent.VK_S);
			jLabel_Substrate.setLabelFor(jButton_SubstrateMaterial);
			pb.add(jTextField_SubstrateMaterial, cc.xy(4, 3));
			jTextField_SubstrateMaterial.setEditable(false);
			pb.add(jButton_SubstrateMaterial, cc.xy(6, 3));
			jButton_SubstrateMaterial.setToolTipText("<HTML>Specify a material for the bulk / substrate.");
			jButton_SubstrateMaterial.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final Composition comp = MaterialsCreator.editMaterial(getWizard(), mThread.mSubstrateMaterial,
							mSession, true);
					if (comp != null) {
						final Material m = comp instanceof Material ? (Material) comp
								: new Material(comp, ToSI.gPerCC(5.0));
						if (m.getDensity() < 1.0e-6) {
							final NumberFormat df = new HalfUpFormat("0.0");
							ErrorDialog.createErrorMessage(SimulationWizard.this, "Density too low!",
									"The density of the specified material is too low.",
									"The specified material has a density of "
											+ df.format(FromSI.gPerCC(m.getDensity()))
											+ ".  This density is too low to simulate.");
						} else {
							mThread.mSubstrateMaterial = m;
							jTextField_SubstrateMaterial.setText(mThread.mSubstrateMaterial.toString());
						}
					} else
						setErrorText("No modification made to the substrate material.");
				}
			});
			pb.add(jButton_NoSubstrate, cc.xy(8, 3));
			jButton_NoSubstrate.setToolTipText("Specifies no substrate material.  This is equivalent to no substrate.");
			jButton_NoSubstrate.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mThread.mSubstrateMaterial = Material.Null;
					jTextField_SubstrateMaterial.setText(mThread.mSubstrateMaterial.toString());
				}
			});

			jLabel_Object = pb.addLabel("Film or object's material", cc.xy(2, 5));
			jLabel_Object.setDisplayedMnemonic(KeyEvent.VK_O);
			jLabel_Object.setLabelFor(jButton_ObjectMaterial);
			pb.add(jTextField_ObjectMaterial, cc.xy(4, 5));
			jTextField_ObjectMaterial.setEditable(false);
			pb.add(jButton_ObjectMaterial, cc.xy(6, 5));
			jButton_ObjectMaterial.setToolTipText("<HTML>Specify a material for the film, sphere, cube or inclusion.");
			jButton_ObjectMaterial.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final Material m = (Material) MaterialsCreator.editMaterial(getWizard(), mThread.mObjectMaterial,
							mSession, true);
					if (m != null) {
						if (m.getDensity() < 1.0e-6)
							ErrorDialog.createErrorMessage(SimulationWizard.this, "Density too low!",
									"The density of the specified material is too low.",
									"The specified material has a density of "
											+ Double.toString(FromSI.gPerCC(m.getDensity()))
											+ ".  This density is too low to simulate.");
						else {
							mThread.mObjectMaterial = m;
							jTextField_ObjectMaterial.setText(mThread.mObjectMaterial.toString());
						}
					} else
						setErrorText("No modification made to the object material.");
				}
			});

			jLabel_Scale = pb.addLabel("   Object's scale (diameter, thickness or width)", cc.xy(2, 7));
			jLabel_Scale.setLabelFor(jTextField_Scale);
			pb.add(jTextField_Scale, cc.xy(4, 7));
			jLabel_Micron = pb.addLabel("\u00B5m", cc.xy(6, 7));

			jLabel_Scale2 = pb.addLabel("  Secondary scale", cc.xy(2, 9));
			jLabel_Micron2 = pb.addLabel("\u00B5m", cc.xy(6, 9));
			jLabel_Scale2.setLabelFor(jTextField_Scale2);
			pb.add(jTextField_Scale2, cc.xy(4, 9));

			jLabel_Rotate = pb.addLabel("Sample rotation", cc.xy(2, 11));
			jLabel_Degree = pb.addLabel("\u00B0", cc.xy(6, 11));
			jLabel_Rotate.setLabelFor(jTextField_Rotate);
			pb.add(jTextField_Rotate, cc.xy(4, 11));

			mMatBackColor = jTextField_ObjectMaterial.getBackground();
			jCheckBox_Overscan.setToolTipText(
					"<HTML>Check this box to simulate rastering over the particle.<br/>Otherwise the beam will be placed at the center of the particle.");
			pb.add(jCheckBox_Overscan, cc.xyw(4, 13, 3));
			jCheckBox_Overscan.setSelected(mThread.mOverscan);
		}

		@Override
		public boolean permitNext() {
			jTextField_ObjectMaterial.setBackground(mMatBackColor);
			jTextField_SubstrateMaterial.setBackground(mMatBackColor);
			if (!mThread.mSubstrateMaterial.equals(Material.Null)) {
				if (mThread.mSubstrateMaterial.getElementCount() == 0) {
					getWizard().setErrorText("Please specify a material for the substrate.");
					jTextField_SubstrateMaterial.setBackground(Color.PINK);
					return false;
				}
				if (mThread.mSubstrateMaterial.getDensity() < 1.0e-3) {
					getWizard().setErrorText("The density of the substrate material is too low.");
					jTextField_SubstrateMaterial.setBackground(Color.PINK);
					return false;
				}
			}
			if (jButton_ObjectMaterial.isEnabled()) {
				if (mThread.mObjectMaterial.getElementCount() == 0) {
					getWizard().setErrorText("Please specify a material for the object.");
					jTextField_ObjectMaterial.setBackground(Color.PINK);
					return false;
				}
				if (mThread.mObjectMaterial.getDensity() < 1.0e-3) {
					getWizard().setErrorText("The density of the object material is too low.");
					jTextField_ObjectMaterial.setBackground(Color.PINK);
					return false;
				}
			}
			final NumberFormat nf = NumberFormat.getInstance();
			try {
				mThread.mScale = 1.0e-6 * nf.parse(jTextField_Scale.getText()).doubleValue();
				if ((mThread.mScale < 0.1e-9) && (mThread.mScale > 1.0e-3)) {
					getWizard().setErrorText("The scale must be between 0.1 nm and 1.0 mm");
					return false;
				}
			} catch (final ParseException e) {
				getWizard().setErrorText("Error parsing the scale");
				jTextField_Scale.setText(mFormatter.format(1.0e6 * mThread.mScale));
				jTextField_Scale.setBackground(Color.PINK);
				return false;
			}
			try {
				mThread.mScale2 = 1.0e-6 * nf.parse(jTextField_Scale2.getText()).doubleValue();
				if ((mThread.mScale2 < 0.1e-9) && (mThread.mScale2 > 1.0e-3)) {
					getWizard().setErrorText("The scale must be between 0.1 nm and 1.0 mm");
					return false;
				}
			} catch (final ParseException e) {
				getWizard().setErrorText("Error parsing the scale");
				jTextField_Scale2.setText(mFormatter.format(1.0e6 * mThread.mScale2));
				jTextField_Scale2.setBackground(Color.PINK);
				return false;
			}

			try {
				mThread.mRotate = Math.toRadians(nf.parse(jTextField_Rotate.getText()).doubleValue());
				if ((mThread.mRotate < Math.toRadians(-360.0)) && (mThread.mRotate > Math.toRadians(360.0))) {
					getWizard().setErrorText("The rotation must be between -360\u00B0 and 360\u00B0");
					return false;
				}
			} catch (final ParseException e) {
				getWizard().setErrorText("Error parsing the rotation");
				jTextField_Rotate.setText(mFormatter.format(Math.toDegrees(mThread.mRotate)));
				jTextField_Rotate.setBackground(Color.PINK);
				return false;
			}

			mThread.mOverscan = jCheckBox_Overscan.isSelected();
			return true;
		}

		@Override
		public void onShow() {
			jTextField_Scale.setText(mFormatter.format(1.0e6 * mThread.mScale));
			jTextField_Scale2.setText(mFormatter.format(1.0e6 * mThread.mScale2));
			jTextField_Rotate.setText(mFormatter.format(Math.toDegrees(mThread.mRotate)));
			jTextField_ObjectMaterial.setText(mThread.mObjectMaterial.toString());
			jTextField_SubstrateMaterial.setText(mThread.mSubstrateMaterial.toString());
			final SimulationMode mode = mSimMode.getMode();
			switch (mode) {
			case AnalyticalBulk:
			case MCBulk:
				jLabel_Object.setVisible(false);
				jTextField_ObjectMaterial.setVisible(false);
				jButton_ObjectMaterial.setVisible(false);
				jLabel_Scale.setVisible(false);
				jTextField_Scale.setVisible(false);
				jLabel_Micron.setVisible(false);
				break;
			case MCCube:
			case MCCylinderOnSide:
			case MCCylinderOnEnd:
			case MCFilm:
			case MCHemisphere:
			case MCInclusion:
			case MCInterface:
			case MCRectangularPrism:
			case MCSphere:
			case MCSquarePyramid:
			case MCTriangularPrism:
				jLabel_Object.setVisible(true);
				jTextField_ObjectMaterial.setVisible(true);
				jButton_ObjectMaterial.setVisible(true);
				jLabel_Scale.setVisible(true);
				jTextField_Scale.setVisible(true);
				jLabel_Micron.setVisible(true);
				break;
			}
			switch (mode) {
			case AnalyticalBulk:
			case MCBulk:
			case MCHemisphere:
			case MCSphere:
			case MCCylinderOnEnd:
			case MCFilm: {
				boolean b = false;
				jLabel_Rotate.setVisible(b);
				jLabel_Degree.setVisible(b);
				jTextField_Rotate.setVisible(b);
				break;
			}
			case MCCube:
			case MCCylinderOnSide:
			case MCInclusion:
			case MCInterface:
			case MCRectangularPrism:
			case MCSquarePyramid:
			case MCTriangularPrism: {
				boolean b = true;
				jLabel_Rotate.setVisible(b);
				jLabel_Degree.setVisible(b);
				jTextField_Rotate.setVisible(b);
				break;
			}

			}
			switch (mode) {
			case AnalyticalBulk:
			case MCBulk:
			case MCCube:
			case MCFilm:
			case MCHemisphere:
			case MCInterface:
			case MCSphere:
				jTextField_Scale2.setVisible(false);
				jLabel_Scale2.setVisible(false);
				jLabel_Micron2.setVisible(false);
				break;
			case MCCylinderOnSide:
			case MCCylinderOnEnd:
			case MCInclusion:
			case MCRectangularPrism:
			case MCSquarePyramid:
			case MCTriangularPrism:
				jTextField_Scale2.setVisible(true);
				jLabel_Scale2.setVisible(true);
				jLabel_Micron2.setVisible(true);
				break;

			}
			switch (mode) {
			case AnalyticalBulk:
			case MCBulk:
				jLabel_Substrate.setText("Bulk material");
				jLabel_Substrate.setDisplayedMnemonic(KeyEvent.VK_B);
				jCheckBox_Overscan.setVisible(false);
				break;
			case MCCube:
				jLabel_Substrate.setText("Substrate material");
				jLabel_Substrate.setDisplayedMnemonic(KeyEvent.VK_S);
				jLabel_Object.setText("Cube material");
				jLabel_Object.setDisplayedMnemonic(KeyEvent.VK_C);
				jLabel_Scale.setText("Cube height / width / depth");
				jLabel_Scale.setDisplayedMnemonic(KeyEvent.VK_H);
				jCheckBox_Overscan.setVisible(true);
				break;
			case MCFilm:
				jLabel_Substrate.setText("Substrate material");
				jLabel_Substrate.setDisplayedMnemonic(KeyEvent.VK_S);
				jLabel_Object.setText("Film material");
				jLabel_Object.setDisplayedMnemonic(KeyEvent.VK_F);
				jLabel_Scale.setText("Film thickness");
				jLabel_Scale.setDisplayedMnemonic(KeyEvent.VK_T);
				jCheckBox_Overscan.setVisible(false);
				break;
			case MCInclusion:
				jLabel_Substrate.setText("Substrate material");
				jLabel_Substrate.setDisplayedMnemonic(KeyEvent.VK_S);
				jLabel_Object.setText("Inclusion material");
				jLabel_Object.setDisplayedMnemonic(KeyEvent.VK_I);
				jLabel_Scale.setText("Inclusion width");
				jLabel_Scale.setDisplayedMnemonic(KeyEvent.VK_D);
				jLabel_Scale2.setText("Inclusion thickness");
				jCheckBox_Overscan.setVisible(true);
				break;
			case MCSphere:
				jLabel_Substrate.setText("Substrate material");
				jLabel_Substrate.setDisplayedMnemonic(KeyEvent.VK_S);
				jLabel_Object.setText("Sphere material");
				jLabel_Object.setDisplayedMnemonic(KeyEvent.VK_P);
				jLabel_Scale.setText("Sphere diameter");
				jLabel_Scale.setDisplayedMnemonic(KeyEvent.VK_D);
				jCheckBox_Overscan.setVisible(true);
				break;
			case MCHemisphere:
				jLabel_Substrate.setText("Substrate material");
				jLabel_Substrate.setDisplayedMnemonic(KeyEvent.VK_S);
				jLabel_Object.setText("Hemisphere material");
				jLabel_Object.setDisplayedMnemonic(KeyEvent.VK_P);
				jLabel_Scale.setText("Hemisphere diameter");
				jLabel_Scale.setDisplayedMnemonic(KeyEvent.VK_D);
				jCheckBox_Overscan.setVisible(true);
				break;
			case MCInterface:
				jLabel_Substrate.setText("Secondary material");
				jLabel_Substrate.setDisplayedMnemonic(KeyEvent.VK_S);
				jLabel_Object.setText("Primary material");
				jLabel_Object.setDisplayedMnemonic(KeyEvent.VK_P);
				jLabel_Scale.setText("Distance from interface");
				jLabel_Scale.setDisplayedMnemonic(KeyEvent.VK_D);
				jCheckBox_Overscan.setVisible(false);
				break;
			case MCCylinderOnSide:
			case MCCylinderOnEnd:
				jLabel_Substrate.setText("Substrate material");
				jLabel_Substrate.setDisplayedMnemonic(KeyEvent.VK_S);
				jLabel_Object.setText("Cylinder material");
				jLabel_Object.setDisplayedMnemonic(KeyEvent.VK_P);
				jLabel_Scale.setText("Cylinder diameter");
				jLabel_Scale.setDisplayedMnemonic(KeyEvent.VK_D);
				jLabel_Scale2.setText("Cylinder length");
				jCheckBox_Overscan.setVisible(true);
				break;
			case MCRectangularPrism:
				jLabel_Substrate.setText("Substrate material");
				jLabel_Substrate.setDisplayedMnemonic(KeyEvent.VK_S);
				jLabel_Object.setText("Block material");
				jLabel_Object.setDisplayedMnemonic(KeyEvent.VK_P);
				jLabel_Scale.setText("Block base");
				jLabel_Scale.setDisplayedMnemonic(KeyEvent.VK_D);
				jLabel_Scale2.setText("Block height");
				jCheckBox_Overscan.setVisible(true);
				break;
			case MCSquarePyramid:
				jLabel_Substrate.setText("Substrate material");
				jLabel_Substrate.setDisplayedMnemonic(KeyEvent.VK_S);
				jLabel_Object.setText("Pyramid material");
				jLabel_Object.setDisplayedMnemonic(KeyEvent.VK_P);
				jLabel_Scale.setText("Pyramid base");
				jLabel_Scale.setDisplayedMnemonic(KeyEvent.VK_D);
				jLabel_Scale2.setText("Pyramid height");
				jCheckBox_Overscan.setVisible(true);
				break;
			case MCTriangularPrism:
				jLabel_Substrate.setText("Substrate material");
				jLabel_Substrate.setDisplayedMnemonic(KeyEvent.VK_S);
				jLabel_Object.setText("Prism material");
				jLabel_Object.setDisplayedMnemonic(KeyEvent.VK_P);
				jLabel_Scale.setText("Prism edge");
				jLabel_Scale.setDisplayedMnemonic(KeyEvent.VK_D);
				jLabel_Scale2.setText("Prism length");
				jCheckBox_Overscan.setVisible(true);
				break;
			default:
				break;
			}
			getWizard().setMessageText("Specify the sample material and scale.");
			getWizard().setNextPanel(mConfig, "Instrument configuration");
			getWizard().enableFinish(false);
		}
	}

	/**
	 * Constructs a SimulationWizard @throws HeadlessException
	 */
	public SimulationWizard() throws HeadlessException {
		super();
		initialize();
	}

	/**
	 * Constructs a SimulationWizard @param owner
	 */
	public SimulationWizard(Frame owner) {
		super(owner, "Spectrum simulation", true);
		initialize();
	}

	private void initialize() {
		setActivePanel(mSimMode, "Simulation Mode");
		pack();
	}

	public ISpectrumData[] getResults() {
		return mProgress.getResults();
	}

	public String asHTML() {
		if (getResults() != null)
			return mThread.asHTML();
		return "";
	}
}
