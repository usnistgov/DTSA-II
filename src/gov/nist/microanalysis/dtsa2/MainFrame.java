package gov.nist.microanalysis.dtsa2;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import javax.swing.text.EditorKit;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import gov.nist.microanalysis.EPQDatabase.ImportDialog;
import gov.nist.microanalysis.EPQDatabase.SearchWizard;
import gov.nist.microanalysis.EPQDatabase.Session;
import gov.nist.microanalysis.EPQLibrary.BremsstrahlungAnalytic;
import gov.nist.microanalysis.EPQLibrary.Composition;
import gov.nist.microanalysis.EPQLibrary.DerivedSpectrum;
import gov.nist.microanalysis.EPQLibrary.DuaneHuntLimit;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.EditableSpectrum;
import gov.nist.microanalysis.EPQLibrary.Element;
import gov.nist.microanalysis.EPQLibrary.FromSI;
import gov.nist.microanalysis.EPQLibrary.ISpectrumData;
import gov.nist.microanalysis.EPQLibrary.ParticleSignature;
import gov.nist.microanalysis.EPQLibrary.PeakROISearch;
import gov.nist.microanalysis.EPQLibrary.PeakStripping;
import gov.nist.microanalysis.EPQLibrary.RegionOfInterestSet;
import gov.nist.microanalysis.EPQLibrary.SpectrumProperties;
import gov.nist.microanalysis.EPQLibrary.SpectrumSmoothing;
import gov.nist.microanalysis.EPQLibrary.SpectrumUtils;
import gov.nist.microanalysis.EPQLibrary.StandardBundle;
import gov.nist.microanalysis.EPQLibrary.ToSI;
import gov.nist.microanalysis.EPQLibrary.Detector.DetectorCalibration;
import gov.nist.microanalysis.EPQLibrary.Detector.DetectorProperties;
import gov.nist.microanalysis.EPQLibrary.Detector.EDSCalibration;
import gov.nist.microanalysis.EPQLibrary.Detector.EDSDetector;
import gov.nist.microanalysis.EPQLibrary.Detector.ElectronProbe;
import gov.nist.microanalysis.EPQTools.CompositionTableModel;
import gov.nist.microanalysis.EPQTools.ErrorDialog;
import gov.nist.microanalysis.EPQTools.JWizardDialog;
import gov.nist.microanalysis.EPQTools.KLMActionEvent;
import gov.nist.microanalysis.EPQTools.KLMLine;
import gov.nist.microanalysis.EPQTools.KLMTreePanel;
import gov.nist.microanalysis.EPQTools.MaterialsCreator;
import gov.nist.microanalysis.EPQTools.ParticleSignatureTableModel;
import gov.nist.microanalysis.EPQTools.SimpleFileFilter;
import gov.nist.microanalysis.EPQTools.SpecDisplay;
import gov.nist.microanalysis.EPQTools.SpectrumFile;
import gov.nist.microanalysis.EPQTools.SpectrumFileChooser;
import gov.nist.microanalysis.EPQTools.SpectrumPropertyPanel;
import gov.nist.microanalysis.EPQTools.SpectrumRenamer;
import gov.nist.microanalysis.EPQTools.SpectrumToolBar;
import gov.nist.microanalysis.EPQTools.StandardDatabaseEditor;
import gov.nist.microanalysis.EPQTools.WriteSpectrumAsCSV;
import gov.nist.microanalysis.EPQTools.WriteSpectrumAsEMSA1_0;
import gov.nist.microanalysis.EPQTools.WriteSpectrumAsTIFF;
import gov.nist.microanalysis.Utility.CSVReader;
import gov.nist.microanalysis.Utility.DescriptiveStatistics;
import gov.nist.microanalysis.Utility.HalfUpFormat;
import gov.nist.microanalysis.Utility.PrintUtilities;
import gov.nist.microanalysis.Utility.SpectrumPropertiesTableModel;
import gov.nist.microanalysis.Utility.TextUtilities;
import gov.nist.microanalysis.Utility.UncertainValue2;
import gov.nist.microanalysis.dtsa2.JCommandLine.JythonWorker;

/**
 * <p>
 * Title: The primary display window class for the DTSA2 application.
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

public class MainFrame extends JFrame {

	static private class RecentFile extends File {

		private static final long serialVersionUID = -1604465919552701752L;

		RecentFile(String file) {
			super(file);
		}

		@Override
		public String toString() {
			return getName();
		}
	}

	static private class RecentPyModel extends DefaultComboBoxModel<RecentFile> {

		private static final long serialVersionUID = 3349320405633474918L;

		private final ArrayList<RecentFile> mFiles = new ArrayList<RecentFile>();
		private RecentFile mSelected = null;

		private RecentPyModel() {
			super();
			final Preferences pref = Preferences.userNodeForPackage(getClass());
			for (int i = 0; i < 10; ++i) {
				final String recentI = pref.get("Recent[" + Integer.toString(i) + "]", null);
				if (recentI != null) {
					final RecentFile fileI = new RecentFile(recentI);
					if (fileI.exists() && (!mFiles.contains(fileI)))
						mFiles.add(fileI);
				}
			}
		}

		public void add(String filename) {
			final RecentFile rf = new RecentFile(filename);
			if (rf.exists() && (!mFiles.contains(rf))) {
				mFiles.add(0, rf);
				final Preferences pref = Preferences.userNodeForPackage(getClass());
				for (int i = 0; (i < 10) && (i < mFiles.size()); ++i)
					try {
						pref.put("Recent[" + Integer.toString(i) + "]", mFiles.get(i).getCanonicalPath());
					} catch (final IOException e) {
						// Ignore it...
					}
				fireContentsChanged(this, 0, getSize());
			}
		}

		@Override
		public RecentFile getElementAt(int index) {
			return mFiles.get(index);
		}

		@Override
		public RecentFile getSelectedItem() {
			return mSelected;
		}

		@Override
		public void setSelectedItem(Object arg0) {
			if (mFiles.contains(arg0))
				mSelected = (RecentFile) arg0;

		}

		@Override
		public int getSize() {
			return mFiles.size();
		}
	}

	private static final String NONE_STRING = "-- None --";
	private static final String PREVIOUS_EPQ_VERSION = "PREVIOUS_EPQ_VERSION";
	private static final String PREVIOUS_TRIXY_VERSION = "PREVIOUS_TRIXY_VERSION";

	/**
	 * The maximum number of spectra which may be displayed simultaneously
	 */
	private static int MAX_DISPLAYED_SPECTRA = 100;
	static public final long serialVersionUID = 0x1;
	static final boolean INCLUDE_DIAG_MENU_ITEMS = false;
	private final JLabel jStatusBar_Main = new JLabel();
	private final JTabbedPane jTabbedPane_Utility = new JTabbedPane();
	private final SpecDisplay jSpecDisplay_Main = new SpecDisplay();
	private final SpectrumToolBar jSpectrumToolBar_Main = new SpectrumToolBar(jSpecDisplay_Main);
	private final JPanel jPanel_Spectrum = new JPanel();
	private final JPanel jPanel_SpecList = new JPanel();
	private final JButton jButton_SpecUp = createButton("up1_sm.png", "Move the selected spectra up in the list.");
	private final JButton jButton_SpecDown = createButton("down1_sm.png",
			"Move the selected spectra down in the list.");
	private final JButton jButton_SpecGroup = createButton("group.png", "Group the selected spectra.");
	private final JButton jButton_SpecDelete = createButton("red_x.png", "Delete the selected spectra.");
	private final JList<ISpectrumData> jList_Spectrum = new JList<ISpectrumData>();
	private final JPopupMenu jPopupMenu_SpectrumList = new JPopupMenu();
	private final JComboBox<Object> jComboBox_Instrument = new JComboBox<Object>();
	private final JComboBox<Object> jComboBox_Detector = new JComboBox<Object>();
	private final JPanel jPanel_Composition = new JPanel();
	private final TitledBorder jBorder_Composition = DTSA2.createTitledBorder("Composition");
	private final JTable jTable_SpecComposition = new JTable();
	private JTextPane jTextPane_Log = null;

	private final JTable jTable_SpecProperties = new JTable();
	private final JCommandLine jCommandLine_Main = new JCommandLine();
	private final JButton jButton_OpenPy = new JButton();
	private final JButton jButton_Stop = new JButton();
	private final JComboBox<RecentFile> jComboBox_PrevPy = new JComboBox<RecentFile>();
	private final JButton jButton_Play = new JButton();
	private RecentPyModel mRecentPyModel = null;
	private boolean mPyDisabled = false;
	private final JMenuBar jMenuBar_Main = new JMenuBar();
	private final KLMTreePanel jKLMTreePanel = new KLMTreePanel();

	// File menu
	private final JMenu jMenu_File = new JMenu();
	private final JMenuItem jMenuItem_Open = new JMenuItem();
	private final JMenuItem jMenuItem_OpenReport = new JMenuItem();
	private final JMenuItem jMenuItem_ExecuteScript = new JMenuItem();
	private final JMenuItem jMenuItem_Import = new JMenuItem();
	private final JMenuItem jMenuItem_ImportIntoDB = new JMenuItem();
	private final JMenuItem jMenuItem_SearchDB = new JMenuItem();
	private final JMenuItem jMenuItem_SaveAs = new JMenuItem();
	private final JMenuItem jMenuItem_SaveAll = new JMenuItem();
	private final JMenu jMenu_Print = new JMenu();
	private final JMenuItem jMenuItem_PrintReport = new JMenuItem();
	private final JMenuItem jMenuItem_PrintSpectra = new JMenuItem();
	private final JMenuItem jMenuItem_Preferences = new JMenuItem();
	private final JMenuItem jMenuItem_StandardsDatabase = new JMenuItem();
	private final JMenuItem jMenuFile_Exit = new JMenuItem();

	private final JMenu jMenu_Export = new JMenu();
	private final JMenuItem jMenuItem_BatchExport = new JMenuItem();
	private final JMenuItem jMenuItem_BatchExportEMSA = new JMenuItem();

	// Process menu
	private final JMenu jMenu_Process = new JMenu();
	private final JMenuItem jMenuItem_SubSample = new JMenuItem();
	private final JMenuItem jMenuItem_FitBackground = new JMenuItem();
	private final JMenuItem jMenuItem_StripBackground = new JMenuItem();
	private final JMenuItem jMenuItem_Rescale = new JMenuItem();
	private final JMenuItem jMenuItem_Smooth = new JMenuItem();
	private final JMenuItem jMenuItem_Trim = new JMenuItem();
	private final JMenuItem jMenuItem_PeakSearch = new JMenuItem();

	// Tools menu
	private final JMenu jMenu_Tools = new JMenu();
	private final JMenuItem jMenuItem_EditSpectrumProperties = new JMenuItem();
	private final JMenuItem jMenuItem_Quant = new JMenuItem();
	private final JMenuItem jMenuItem_Simulation = new JMenuItem();
	private final JMenuItem jMenuItem_Calibration = new JMenuItem();
	private final JMenuItem jMenuItem_Optimize = new JMenuItem();
	private final JMenuItem jMenuItem_QC = new JMenuItem();
	private final JMenuItem jMenuItem_Material = new JMenuItem();
	private final JMenuItem jMenuItem_StandardBuilder = new JMenuItem();

	private final JMenu jMenu_Report = new JMenu();
	private final JMenuItem jMenuItem_SpectrumToReport = new JMenuItem();
	private final JMenuItem jMenuItem_Annotation = new JMenuItem();
	private final JMenuItem jMenuItem_OpenInBrowser = new JMenuItem();

	private final JMenuItem jMenuItem_SetKLMs = new JMenuItem("Set KLMs");
	// private final JMenu jMenu_PlugIn = new JMenu();
	// private final JMenuItem jMenuItem_Install = new JMenuItem();

	// Help menu
	private final JMenu jMenu_Help = new JMenu();
	private final JMenuItem jMenuItem_HelpSite = new JMenuItem();
	private final JMenuItem jMenuItem_HelpJython = new JMenuItem();
	private final JMenuItem jMenuItem_HelpPython = new JMenuItem();
	private final JMenuItem jMenuItem_HelpEPQ = new JMenuItem();
	private final JMenuItem jMenuItem_ProbeUserGroup = new JMenuItem();
	private final JMenuItem jMenuItem_HelpUserGroup = new JMenuItem();
	private final JMenuItem jMenuItem_HelpAbout = new JMenuItem();

	private final JPopupMenu jPopupMenu_Tabs = new JPopupMenu();

	private JSplitPane jSplitPane_MainVert;

	private final DataManager mDataManager = DataManager.getInstance();

	private File mPreviousScript = null;

	private JPanel jPanel_Command;

	// private JXMapViewer jPanel_MapViewer;

	private static final String POSITION_HEIGHT = "Main window\\height";
	private static final String POSITION_WIDTH = "Main window\\width";
	private static final String POSITION_LEFT = "Main window\\left";
	private static final String POSITION_TOP = "Main window\\top";
	private static final String SPLITTER_POSITION = "Main window\\splitter";

	private Date mSessionStarted = new Date();
	private int mLastMark = 0;
	private ArrayList<String> mStartUp = new ArrayList<String>();

	private static final Color FOREGROUND_COLOR = SystemColor.textText;

	private static final Color BACKGROUND_COLOR = SystemColor.text;
	private static final Color PANEL_COLOR = (new JPanel()).getBackground();

	static public String wordWrap(String str, int width) {
		return wordWrap(str, width, "<br>", "-", false);
	}

	private JTextPane getTextPane_Log() {
		if (jTextPane_Log == null)
			try {
				jTextPane_Log = createReportPage(DTSA2.getReport().getFile());
			} catch (final IOException e) {
				ErrorDialog.createErrorMessage(MainFrame.this, "Error creating report", e);
				throw new Error(e);
			}
		return jTextPane_Log;
	}

	/**
	 * Word-wrap a string.
	 * 
	 * @param str         String to word-wrap
	 * @param width       int to wrap at
	 * @param delim       String to use to separate lines
	 * @param split       String to use to split a word greater than width long
	 * @param delimInside whether or not delim should be included in chunk before
	 *                    length reaches width.
	 * @return String that has been word wrapped
	 */
	static public String wordWrap(String str, int width, String delim, String split, boolean delimInside) {
		final int sz = str.length();

		// / shift width up one. mainly as it makes the logic easier
		width++;

		// our best guess as to an initial size
		final StringBuffer buffer = new StringBuffer(((sz / width) * delim.length()) + sz);

		// every line might include a delim on the end
		if (delimInside)
			width = width - delim.length();
		else
			width--;

		int idx = -1;
		String substr = null;

		// beware: i is rolled-back inside the loop
		for (int i = 0; i < sz; i += width) {

			// on the last line
			if (i > (sz - width)) {
				buffer.append(str.substring(i));
				break;
			}

			// the current line
			substr = str.substring(i, i + width);

			// is the delim already on the line
			idx = substr.indexOf(delim);
			if (idx != -1) {
				buffer.append(substr.substring(0, idx));
				buffer.append(delim);
				i -= width - idx - delim.length();
				// Erase a space after a delim. Is this too obscure?
				if (substr.length() > (idx + 1))
					if (substr.charAt(idx + 1) != '\n')
						if (Character.isWhitespace(substr.charAt(idx + 1)))
							i++;
				continue;
			}

			idx = -1;

			// figure out where the last space is
			final char[] chrs = substr.toCharArray();
			for (int j = width; j > 0; j--)
				if (Character.isWhitespace(chrs[j - 1])) {
					idx = j;
					break;
				}

			// idx is the last whitespace on the line.
			if (idx == -1) {
				for (int j = width; j > 0; j--)
					if (chrs[j - 1] == '-') {
						idx = j;
						break;
					}
				if (idx == -1) {
					buffer.append(substr);
					buffer.append(delim);
				} else {
					if (idx != width)
						idx++;
					buffer.append(substr.substring(0, idx));
					buffer.append(delim);
					i -= width - idx;
				}
			} else {
				// insert spaces
				buffer.append(substr.substring(0, idx));
				for (int ii = width; ii < idx; ++ii)
					buffer.append(" ");
				buffer.append(delim);
				i -= width - idx;
			}
		}
		return buffer.toString();
	}

	// Construct the frame
	public MainFrame() {
		super();
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		try {
			initialize();
			otherInit();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		// Set the size of the window...
		if (!System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
			final Preferences userPref = Preferences.userNodeForPackage(MainFrame.class);
			{
				final Rectangle bounds = getBounds();
				final int top = userPref.getInt(POSITION_TOP, (int) bounds.getX());
				final int left = userPref.getInt(POSITION_LEFT, (int) bounds.getY());
				final int pos_width = Math.max(userPref.getInt(POSITION_WIDTH, 0), (15 * 1024) / 16);
				final int pos_height = Math.max(userPref.getInt(POSITION_HEIGHT, 0), (15 * 768) / 16);
				setLocation(top, left);
				setPreferredSize(new Dimension(pos_width, pos_height));
				final int location = Math.max(userPref.getInt(SPLITTER_POSITION, pos_height / 3), 100);
				jSplitPane_MainVert.setDividerLocation(location);
			}
		} else
			setExtendedState(MAXIMIZED_BOTH);
	}

	/**
	 * A method for invoking a Runnable object on the Event Dispatch thread. If the
	 * current thread is the Event Dispatch thread then the Runnable is just run.
	 * Otherwise the SwingUtilities method invokeAndWait is called.
	 * 
	 * @param as
	 */
	public void invokeCarefully(Runnable as) {
		if (SwingUtilities.isEventDispatchThread())
			as.run();
		else
			try {
				SwingUtilities.invokeAndWait(as);
			} catch (final Exception e) {
				ErrorDialog.createErrorMessage(MainFrame.this, DTSA2.APP_NAME, e);
			}
	}

	private JTextPane createReportPage(File file) throws IOException {
		final JTextPane res = new JTextPane();
		res.setEditable(false);
		res.setContentType("text/html");
		((HTMLDocument) res.getDocument()).setAsynchronousLoadPriority(-1);
		res.setPage(file.toURI().toURL());

		res.addHyperlinkListener(new HyperlinkListener() {

			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				try {
					if (e.getEventType() == EventType.ACTIVATED) {
						String fn = e.getURL().getPath();
						if (fn.endsWith(".msa")) {
							fn = fn.replace("%20", " ");
							importSpectra(SpectrumFile.open(fn), false);
						} else if (fn.endsWith(".wrl"))
							try {
								Desktop.getDesktop().browse(e.getURL().toURI());
							} catch (final Exception e1) {
								e1.printStackTrace();
							}
					}
				} catch (final EPQException e1) {
					e1.printStackTrace();
				}
			}

		});
		final JPopupMenu pm = new JPopupMenu();
		final JMenuItem mi = new JMenuItem("Open in browser");
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				DTSA2.getReport().openInBrowser(MainFrame.this);
			}

		});
		pm.add(mi);

		res.add(pm);
		res.addMouseListener(new PopupMenuMouseAdapter(res, pm));
		return res;
	}

	private static class PopupMenuMouseAdapter extends MouseAdapter {
		private final JComponent mReport;
		private final JPopupMenu mMenu;

		private PopupMenuMouseAdapter(JComponent report, JPopupMenu menu) {
			mReport = report;
			mMenu = menu;
		}

		@Override
		public void mousePressed(MouseEvent me) {
			if (me.isPopupTrigger()) {
				mMenu.show(mReport, me.getX(), me.getY());

			}
		}

		@Override
		public void mouseReleased(MouseEvent me) {
			if (me.isPopupTrigger())
				mMenu.show(mReport, me.getX(), me.getY());
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.isPopupTrigger())
				mMenu.show(mReport, e.getX(), e.getY());
		}

	}

	/**
	 * Writes a block of HTML at the end of the current report.
	 * 
	 * @param html
	 */
	public synchronized void appendHTML(String html) {
		// mStartUp deals with a problem writing HTML before the window is
		// displayed.
		if (mStartUp != null)
			mStartUp.add(html);
		else
			try {

				final HTMLDocument doc = (HTMLDocument) getTextPane_Log().getDocument();
				// Find the document body and insert just before this...
				javax.swing.text.Element body = null;
				{
					javax.swing.text.Element root = doc.getDefaultRootElement();
					for (int i = 0; i < root.getElementCount(); i++) {
						javax.swing.text.Element element = root.getElement(i);
						if (element.getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.BODY) {
							body = element;
							break;
						}
					}
					assert body != null;
				}
				if (mSessionStarted != null) {
					final Preferences userPref = Preferences.userNodeForPackage(MainFrame.class);
					final boolean trixy = !DTSA2.getRevision(DTSA2.class)
							.equals(userPref.get(PREVIOUS_TRIXY_VERSION, "none"));
					final boolean epq = !DTSA2.getRevision(EPQException.class)
							.equals(userPref.get(PREVIOUS_EPQ_VERSION, "none"));
					if (trixy || epq) {
						final HTMLList list = new HTMLList();
						list.setHeader("Software update notification");
						if (trixy)
							list.add("DTSA2 updated to revision " + DTSA2.getRevision(DTSA2.class) + ".");
						if (epq)
							list.add("The EPQ library updated to revision " + DTSA2.getRevision(EPQException.class)
									+ ".");
						doc.insertBeforeEnd(body, list.toString());
						userPref.put(PREVIOUS_TRIXY_VERSION, DTSA2.getRevision(DTSA2.class));
						userPref.put(PREVIOUS_EPQ_VERSION, DTSA2.getRevision(EPQException.class));
					}
					if (DTSA2.INCLUDE_USER_GROUP)
						JoinUserGroupDialog.doSignUp(MainFrame.this);
					mSessionStarted = null;
				}
				final String mark = "<A NAME=\"ITEM_" + Long.toHexString(++mLastMark) + "\" />\n";
				doc.insertBeforeEnd(body, mark + html);
				// Write the result to disk
				final EditorKit hek = getTextPane_Log().getEditorKit();
				try (final Writer out = new OutputStreamWriter(new FileOutputStream(DTSA2.getReport().getFile()),
						java.nio.charset.Charset.forName("ISO-8859-1"))) {
					hek.write(out, doc, 0, doc.getLength());
				}
				scrollToLastHTMLMark();

			} catch (final Exception e) {
				e.printStackTrace();
			}
	}

	public void appendImage(BufferedImage bi, String title, String caption) {
		try {
			final File outfile = File.createTempFile("image", ".png", DTSA2.getReport().getFile().getParentFile());
			ImageIO.write(bi, "png", outfile);
			final StringBuffer sb = new StringBuffer();
			if (title != null)
				sb.append("<br><h2>" + title + "</h2>");
			final double scale = jSpecDisplay_Main.getWidth() / bi.getWidth();
			sb.append("<img");
			if (scale < 1.0) {
				sb.append("width=\"" + Integer.toString((int) Math.round(bi.getWidth() * scale)) + "\"");
				sb.append(" height=\"" + Integer.toString((int) Math.round(bi.getHeight() * scale)) + "\"");
			}
			sb.append(" src=\"");
			sb.append(outfile.toURI().toURL().toExternalForm());
			sb.append("\" alt=\"" + title + "\" /></p>");
			if (caption != null)
				sb.append("<p>Caption: " + caption);
			appendHTML(sb.toString());
		} catch (final Exception e) {
			ErrorDialog.createErrorMessage(this, "Error adding image to report", e);
		}
	}

	/**
	 * Call once after all the windows have been created to write items to the HTML
	 * report.
	 */
	public void flushHtml() {
		if (mStartUp != null) {
			final ArrayList<String> tmp = mStartUp;
			mStartUp = null;
			for (final String str : tmp)
				appendHTML(str);
		}
	}

	private void scrollToLastHTMLMark() {
		if (mLastMark > 0) {
			Runnable outer = new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
					Runnable th = new Runnable() {
						@Override
						public void run() {
							getTextPane_Log().scrollToReference("ITEM_" + Long.toHexString(mLastMark));
						}
					};
					SwingUtilities.invokeLater(th);
				}
			};
			Thread th = new Thread(outer);
			th.start();
		}
	}

	public void showKLMLines(Collection<Element> elms) {
		clearKLMs();
		jKLMTreePanel.parseElementField(Element.toString(elms, true));
	}

	public void clearKLMs() {
		jSpecDisplay_Main.clearKLMs();
		try {
			jKLMTreePanel.clearAll();
		} catch (EPQException e) {
			// Ignore
		}
	}

	private void otherInit() {
		final Preferences userPref = Preferences.userNodeForPackage(MainFrame.class);
		try {
			jKLMTreePanel.setElement(Element.byAtomicNumber(userPref.getInt("KLMDialog\\Atomic Number", 6)));
		} catch (final EPQException e) {
			// No big deal
		}
		final JPopupMenu menu = jSpecDisplay_Main.getDefaultMenu();
		jSpecDisplay_Main.setDefaultMenu(menu);
		jSpecDisplay_Main.setRegionMenu(jSpecDisplay_Main.getDefaultRegionMenu());
		jSpecDisplay_Main.zoomToAll();
		final Font f = jSpecDisplay_Main.getFont();
		jSpecDisplay_Main.setFont(new Font(f.getName(), f.getStyle(), (5 * f.getSize()) / 4));
		jList_Spectrum.setForeground(FOREGROUND_COLOR);
		jList_Spectrum.setBackground(BACKGROUND_COLOR);
		jList_Spectrum.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		if (true) {
			final JMenuItem sa = new JMenuItem("Select All");
			sa.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					jButtonAll_actionPerformed(e);
				}
			});
			jPopupMenu_SpectrumList.add(sa);
			final JMenuItem sn = new JMenuItem("Select None");
			sn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					jButtonNone_actionPerformed(e);
				}
			});
			jPopupMenu_SpectrumList.add(sn);
			final JMenuItem cs = new JMenuItem("Clear Selected");
			cs.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					jButtonClear_actionPerformed(e);
				}
			});
			jPopupMenu_SpectrumList.add(cs);
			final JMenuItem esp = new JMenuItem("Edit spectrum properties");
			esp.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					toolsEditSpectrumProperties();
				}
			});
			jPopupMenu_SpectrumList.add(esp);
			final JMenuItem am = new JMenuItem("Assign material");
			am.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					toolsEditMaterial();
				}
			});
			jPopupMenu_SpectrumList.add(am);
			final JMenuItem ms = new JMenuItem("Make 'Standard Bundle'");
			ms.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					toolsMakeStandard();
				}
			});
			jPopupMenu_SpectrumList.add(ms);

			final JMenuItem rn = new JMenuItem("Rename...");
			rn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					toolsRenameSpectrum();
				}
			});
			jPopupMenu_SpectrumList.add(rn);

			final JMenuItem sat = new JMenuItem("Sort by acquisition time");
			sat.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					mDataManager.sortSpectra(new DataManager.TimestampSort());
				}
			});
			jPopupMenu_SpectrumList.add(sat);

			final JMenuItem san = new JMenuItem("Sort by name");
			san.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					mDataManager.sortSpectra(new DataManager.NameSort());
				}
			});
			jPopupMenu_SpectrumList.add(san);

			final JMenuItem sad = new JMenuItem("Sort by detector/time");
			sad.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					mDataManager.sortSpectra(new DataManager.DetectorSort(new DataManager.TimestampSort()));
				}
			});
			jPopupMenu_SpectrumList.add(sad);

			jList_Spectrum.addMouseListener(new PopupMenuMouseAdapter(jList_Spectrum, jPopupMenu_SpectrumList));
		}

		class SpectrumSelectionListener implements ListSelectionListener, ActionListener {
			private boolean mInside = false;

			@Override
			public void valueChanged(ListSelectionEvent lse) {
				if (!mInside) {
					mInside = true;
					try {
						final List<ISpectrumData> sel = new ArrayList<ISpectrumData>();
						for (final ISpectrumData obj : jList_Spectrum.getSelectedValuesList())
							sel.add(obj);
						mDataManager.setSelected(sel);
						updateDisplayedSpectra();
						MainFrame.this.jStatusBar_Main.setText(mDataManager.getSelectedCount() + " spectra selected.");
					} finally {
						mInside = false;
					}
				}
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!mInside) {
					mInside = true;
					try {
						final int event = e.getID();
						if (DataManager.isSet(event, DataManager.ADD_SPECTRUM)
								|| DataManager.isSet(event, DataManager.REMOVE_SPECTRUM)
								|| DataManager.isSet(event, DataManager.UPDATE_SPECTRUM))
							jList_Spectrum.setListData(mDataManager.spectrumList().toArray(new ISpectrumData[0]));
						jList_Spectrum.clearSelection();
						final ListModel<ISpectrumData> lm = jList_Spectrum.getModel();
						int min = -1;
						for (int i = 0; i < lm.getSize(); ++i) {
							final ISpectrumData spec = lm.getElementAt(i);
							if (mDataManager.isSelected(spec)) {
								if (min == -1)
									min = i;
							} else if (min != -1) {
								jList_Spectrum.addSelectionInterval(min, i - 1);
								min = -1;
							}
						}
						if (min != -1)
							jList_Spectrum.addSelectionInterval(min, lm.getSize() - 1);
						updateDisplayedSpectra();
					} finally {
						mInside = false;
					}
				}
			}
		}

		final SpectrumSelectionListener ssl = new SpectrumSelectionListener();
		jList_Spectrum.addListSelectionListener(ssl);
		mDataManager.addActionListener(ssl);
	}

	private void updateDetector(DetectorProperties det) {
		Object selInst, selDet;
		if (det != null) {
			selInst = det.getOwner();
			selDet = det;
		} else {
			selInst = jComboBox_Instrument.getSelectedItem();
			selDet = jComboBox_Detector.getSelectedItem();
		}
		if (selInst == null)
			selInst = NONE_STRING;
		if (selDet == null)
			selDet = NONE_STRING;
		// Update instrument panel
		{
			ElectronProbe[] ep = null;
			try {
				ep = DTSA2.getSession().getCurrentProbes().toArray(new ElectronProbe[0]);
				final DefaultComboBoxModel<Object> dcbm = new DefaultComboBoxModel<Object>(ep);
				dcbm.addElement(NONE_STRING);
				jComboBox_Instrument.setModel(dcbm);
			} catch (final Exception e1) {
				ErrorDialog.createErrorMessage(null, "Initialize database", e1);
			}
		}
		{
			// Update detector panel
			final DefaultComboBoxModel<Object> dcbm = new DefaultComboBoxModel<Object>();
			if (selInst instanceof ElectronProbe) {
				for (final DetectorProperties dp : DTSA2.getSession().getDetectors())
					if (dp.getOwner().equals(selInst))
						dcbm.addElement(dp);
				if (det != null)
					dcbm.setSelectedItem(det);
			} else
				dcbm.addElement(NONE_STRING);
			jComboBox_Detector.setModel(dcbm);
		}
		jComboBox_Instrument.setSelectedItem(selInst);
		jComboBox_Detector.setSelectedItem(selDet);
		updateDefaultDetector();
	}

	private void updateDefaultDetector() {
		final Object sel = jComboBox_Detector.getSelectedItem();
		AppPreferences.getInstance()
				.setDefaultDetector(sel instanceof DetectorProperties ? (DetectorProperties) sel : null);
		if (sel instanceof DetectorProperties)
			jCommandLine_Main.getJythonWorker().set("defaultDetector", sel);

	}

	private final Icon getIcon(String name) {
		final URL url = SpecDisplay.class.getResource("ClipArt/" + name);
		return new ImageIcon(url);
	}

	private JButton createButton(String icon, String toolTip) {
		final JButton btn = new JButton();
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setFocusable(false);
		final Icon i = getIcon(icon);
		btn.setIcon(i);
		btn.setToolTipText(toolTip);
		btn.setPreferredSize(new Dimension(i.getIconWidth(), i.getIconHeight()));
		return btn;
	}

	// Component initialization
	private void initialize() throws Exception {

		jMenuItem_HelpSite.setMnemonic(KeyEvent.VK_W);
		jMenuItem_HelpSite.setText(DTSA2.APP_NAME + " web site...");
		jMenuItem_HelpSite.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				helpOpenWebSite();
			}
		});

		jMenuItem_HelpJython.setMnemonic(KeyEvent.VK_J);
		jMenuItem_HelpJython.setText("Jython Book web site...");
		jMenuItem_HelpJython.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					final URL url = new URL("http://www.jython.org/jythonbook/en/1.0/index.html");
					Desktop.getDesktop().browse(url.toURI());
				} catch (final Exception e) {
					ErrorDialog.createErrorMessage(MainFrame.this, "Open Jython web site", e);
				}
			}
		});

		jMenuItem_HelpPython.setMnemonic(KeyEvent.VK_P);
		jMenuItem_HelpPython.setText("Python  tutorial...");
		jMenuItem_HelpPython.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					final URL url = new URL("http://docs.python.org/2/");
					Desktop.getDesktop().browse(url.toURI());
				} catch (final Exception e) {
					ErrorDialog.createErrorMessage(MainFrame.this, "Open Python tutorial", e);
				}
			}
		});

		jMenuItem_HelpEPQ.setMnemonic(KeyEvent.VK_P);
		jMenuItem_HelpEPQ.setText("EPQ library documentation...");
		jMenuItem_HelpEPQ.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				AppPreferences.getInstance().openEPQJavaDoc();
			}
		});
		jMenuItem_ProbeUserGroup.setText("DTSA-II User's Forum");
		jMenuItem_ProbeUserGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					final URL url = new URL("http://probesoftware.com/smf/index.php?board=32.0");
					Desktop.getDesktop().browse(url.toURI());
				} catch (final Exception e) {
					ErrorDialog.createErrorMessage(MainFrame.this, "Open DTSA-II Forum", e);
				}
			}
		});

		if (DTSA2.INCLUDE_USER_GROUP) {
			jMenuItem_HelpUserGroup.setMnemonic(KeyEvent.VK_U);
			jMenuItem_HelpUserGroup.setText("User Group Signup...");
			jMenuItem_HelpUserGroup.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					JoinUserGroupDialog.launchSignUp(MainFrame.this);
				}
			});
		}
		jMenuItem_HelpAbout.setMnemonic(KeyEvent.VK_A);
		jMenuItem_HelpAbout.setText("About " + DTSA2.APP_NAME + "...");
		jMenuItem_HelpAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				helpAbout();
			}
		});

		jMenu_Help.setMnemonic(KeyEvent.VK_H);
		jMenu_Help.setText("Help");
		jMenu_Help.add(jMenuItem_HelpSite);
		jMenu_Help.add(jMenuItem_HelpEPQ);
		jMenu_Help.add(jMenuItem_HelpJython);
		jMenu_Help.add(jMenuItem_HelpPython);
		jMenu_Help.add(jMenuItem_ProbeUserGroup);
		if (DTSA2.INCLUDE_USER_GROUP)
			jMenu_Help.add(jMenuItem_HelpUserGroup);

		jMenuItem_EditSpectrumProperties.setMnemonic(KeyEvent.VK_E);
		jMenuItem_EditSpectrumProperties.setText("Edit spectrum properties");
		jMenuItem_EditSpectrumProperties.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				toolsEditSpectrumProperties();
			}
		});

		jMenuItem_Quant.setMnemonic(KeyEvent.VK_Q);
		jMenuItem_Quant.setText("Quantification alien...");
		jMenuItem_Quant.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				toolsQuantificationAlien();
			}
		});

		jMenuItem_Simulation.setMnemonic(KeyEvent.VK_S);
		jMenuItem_Simulation.setText("Simulation alien...");
		jMenuItem_Simulation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				toolsSimulationAlien();
			}
		});

		jMenuItem_Calibration.setMnemonic(KeyEvent.VK_C);
		jMenuItem_Calibration.setText("Calibration alien...");
		jMenuItem_Calibration.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				toolsCalibrationAlien();
			}
		});

		jMenuItem_Optimize.setMnemonic(KeyEvent.VK_O);
		jMenuItem_Optimize.setText("Optimization alien...");
		jMenuItem_Optimize.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				toolsOptimizeAlien();
			}
		});

		jMenuItem_QC.setMnemonic(KeyEvent.VK_Q);
		jMenuItem_QC.setText("Quality control alien...");
		jMenuItem_QC.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				toolsQCAlien();
			}
		});

		jMenuItem_Material.setMnemonic(KeyEvent.VK_M);
		jMenuItem_Material.setText("Assign material...");
		jMenuItem_Material.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				toolsEditMaterial();
			}
		});

		jMenuItem_StandardBuilder.setText("Make 'Standard Bundle'");
		jMenuItem_StandardBuilder.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				toolsMakeStandard();
			}
		});

		jMenuItem_Annotation.setMnemonic(KeyEvent.VK_N);
		jMenuItem_Annotation.setText("Report note...");
		jMenuItem_Annotation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reportAnnotation();
			}
		});

		jMenuItem_SpectrumToReport.setMnemonic(KeyEvent.VK_S);
		jMenuItem_SpectrumToReport.setText("Add spectrum display to report");
		jMenuItem_SpectrumToReport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reportSpectra();
			}
		});

		jMenuItem_StandardsDatabase.setMnemonic(KeyEvent.VK_E);
		jMenuItem_StandardsDatabase.setText("Edit standards database...");
		jMenuItem_StandardsDatabase.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				toolsEditStandards();
			}
		});

		jMenuItem_OpenInBrowser.setMnemonic(KeyEvent.VK_O);
		jMenuItem_OpenInBrowser.setText("Open in browser");
		jMenuItem_OpenInBrowser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DTSA2.getReport().openInBrowser(MainFrame.this);
			}
		});

		/*
		 * jMenuItem_Install.setMnemonic(KeyEvent.VK_I);
		 * jMenuItem_Install.setText("Install plug-in");
		 * jMenuItem_Install.addActionListener(new ActionListener() {
		 * 
		 * @Override public void actionPerformed(ActionEvent e) { // installPlugin(); }
		 * });
		 */

		jMenu_Tools.setMnemonic(KeyEvent.VK_T);
		jMenu_Tools.setText("Tools");
		addToolsMenuItem(jMenuItem_Quant);
		addToolsMenuItem(jMenuItem_Simulation);
		addToolsMenuItem(jMenuItem_Calibration);
		addToolsMenuItem(jMenuItem_Optimize);
		addToolsMenuItem(jMenuItem_QC);
		jMenu_Tools.addSeparator();
		addToolsMenuItem(jMenuItem_EditSpectrumProperties);
		addToolsMenuItem(jMenuItem_Material);
		addToolsMenuItem(jMenuItem_StandardBuilder);
		jMenu_Tools.addSeparator();
		addToolsMenuItem(jMenuItem_StandardsDatabase);

		jMenu_Report.setMnemonic(KeyEvent.VK_R);
		jMenu_Report.setText("Report");
		addReportMenuItem(jMenuItem_Annotation);
		addReportMenuItem(jMenuItem_SpectrumToReport);
		addReportMenuItem(jMenuItem_OpenInBrowser);

		/*
		 * jMenu_PlugIn.setMnemonic(KeyEvent.VK_P); jMenu_PlugIn.setText("Plug-Ins");
		 * addPlugInMenuItem(jMenuItem_Install);
		 */

		jMenuItem_SaveAs.setMnemonic(KeyEvent.VK_A);
		jMenuItem_SaveAs.setText("Save As");
		jMenuItem_SaveAs.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		jMenuItem_SaveAs.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fileSaveAs();
			}
		});

		jMenuItem_SaveAll.setText("Save Selected");
		jMenuItem_SaveAll.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fileSaveSelected();
			}
		});

		jMenu_Print.setText("Print");
		jMenu_Print.setMnemonic(KeyEvent.VK_P);

		jMenuItem_PrintReport.setText("Report");
		jMenuItem_PrintReport.setMnemonic(KeyEvent.VK_R);
		jMenuItem_PrintReport.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PrintUtilities.printComponent(getTextPane_Log());
			}
		});
		jMenu_Print.add(jMenuItem_PrintReport);

		jMenuItem_PrintSpectra.setText("Spectra");
		jMenuItem_PrintSpectra.setMnemonic(KeyEvent.VK_S);
		jMenuItem_PrintSpectra.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

		jMenuItem_PrintSpectra.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PrintUtilities.printComponent(jSpecDisplay_Main);
			}
		});
		jMenu_Print.add(jMenuItem_PrintSpectra);

		jMenuItem_Preferences.setMnemonic(KeyEvent.VK_P);
		jMenuItem_Preferences.setText("Preferences");
		jMenuItem_Preferences.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				filePreferences();
			}
		});

		jMenu_Export.setText("Batch export");
		jMenu_Export.setMnemonic(KeyEvent.VK_B);

		jMenuItem_BatchExport.setMnemonic(KeyEvent.VK_C);
		jMenuItem_BatchExport.setText("Export as CSV");
		jMenuItem_BatchExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				fileBatchExportCSV();
			}
		});

		jMenuItem_BatchExportEMSA.setMnemonic(KeyEvent.VK_M);
		jMenuItem_BatchExportEMSA.setText("Export as EMSA 1.0");
		jMenuItem_BatchExportEMSA.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				fileBatchExportEMSA();
			}
		});
		jMenu_Export.add(jMenuItem_BatchExport);
		jMenu_Export.add(jMenuItem_BatchExportEMSA);

		jMenuItem_Open.setMnemonic(KeyEvent.VK_O);
		jMenuItem_Open.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		jMenuItem_Open.setText("Open...");
		jMenuItem_Open.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fileOpen();
			}
		});

		jMenuItem_OpenReport.setMnemonic(KeyEvent.VK_R);
		jMenuItem_OpenReport.setText("Open Report...");
		jMenuItem_OpenReport.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fileOpenReport();
			}
		});

		jMenuItem_ExecuteScript.setText("Execute Script...");
		jMenuItem_ExecuteScript.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				jTabbedPane_Utility.setSelectedComponent(jPanel_Command);
				openPythonScript();
			}
		});

		jMenuItem_Import.setMnemonic(KeyEvent.VK_I);
		jMenuItem_Import.setText("Import from CSV...");
		jMenuItem_Import.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fileImport();
			}
		});

		jMenuItem_ImportIntoDB.setMnemonic(KeyEvent.VK_D);
		jMenuItem_ImportIntoDB.setText("Import into database...");
		jMenuItem_ImportIntoDB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final ImportDialog id = new ImportDialog(MainFrame.this, DTSA2.getSession());
				id.setDetector(AppPreferences.getInstance().getDefaultDetector());
				id.setLocationRelativeTo(MainFrame.this);
				id.setVisible(true);
				appendHTML(id.getReport());
			}
		});

		jMenuItem_SearchDB.setMnemonic(KeyEvent.VK_S);
		jMenuItem_SearchDB.setText("Search database...");
		jMenuItem_SearchDB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final Session ses = DTSA2.getSession();
				final SearchWizard sw = new SearchWizard(ses);
				sw.setModal(true);
				sw.setLocationRelativeTo(MainFrame.this);
				sw.setVisible(true);
				final HTMLList list = new HTMLList();
				for (final ISpectrumData spec : sw.getSpectra()) {
					final double res = DuaneHuntLimit.DefaultDuaneHunt.compute(spec);
					if (!Double.isNaN(res))
						spec.getProperties().setNumericProperty(SpectrumProperties.DuaneHunt, FromSI.keV(res));
					addSpectrum(spec, true);
					list.add("Imported <i>" + spec.toString() + "</i> from the database.");
				}
				appendHTML(list.toString());
			}
		});

		jMenu_File.setMnemonic(KeyEvent.VK_F);
		jMenu_File.setText("File");
		jMenu_File.add(jMenuItem_Open);
		jMenu_File.add(jMenuItem_OpenReport);
		jMenu_File.add(jMenuItem_SaveAs);
		jMenu_File.add(jMenuItem_SaveAll);
		jMenu_File.add(jMenuItem_Import);
		jMenu_File.add(jMenu_Export);
		jMenu_File.add(jMenuItem_ExecuteScript);
		jMenu_File.addSeparator();
		jMenu_File.add(jMenuItem_ImportIntoDB);
		jMenu_File.add(jMenuItem_SearchDB);
		jMenu_File.addSeparator();
		jMenu_File.add(jMenu_Print);
		jMenu_File.addSeparator();
		jMenu_File.add(jMenuItem_Preferences);

		jMenuFile_Exit.setMnemonic(KeyEvent.VK_X);
		jMenuFile_Exit.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		jMenuFile_Exit.setText("Exit");
		jMenuFile_Exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fileExit();
			}
		});
		jMenu_File.addSeparator();
		jMenu_File.add(jMenuFile_Exit);

		jMenu_Help.add(jMenuItem_HelpAbout);

		jMenuItem_SubSample.setActionCommand("Sub-sample spectrum...");
		jMenuItem_SubSample.setMnemonic(KeyEvent.VK_S);
		jMenuItem_SubSample.setText("Sub-sample spectrum");
		jMenuItem_SubSample.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				processSubSample();
			}
		});

		jMenuItem_FitBackground.setMnemonic(KeyEvent.VK_F);
		jMenuItem_FitBackground.setText("Fit background");
		jMenuItem_FitBackground.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				processFitBackground();
			}
		});

		jMenuItem_StripBackground.setMnemonic(KeyEvent.VK_S);
		jMenuItem_StripBackground.setText("Strip background");
		jMenuItem_StripBackground.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				processStripBackground();
			}
		});

		jMenuItem_Rescale.setText("Linearize energy axis...");
		jMenuItem_Rescale.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				processLinearizeEnergyAxis();
			}
		});

		jMenuItem_Smooth.setText("Smooth (Savitzky-Golay)");
		jMenuItem_Smooth.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				processSmooth();
			}
		});

		jMenuItem_Trim.setText("Trim");
		jMenuItem_Trim.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				processTrimSpectra();
			}
		});

		jMenuItem_PeakSearch.setText("Peak search");
		jMenuItem_PeakSearch.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				processPeakSearch();
			}
		});

		jMenu_Process.setMnemonic(KeyEvent.VK_P);
		jMenu_Process.setText("Process");
		addProcessMenuItem(jMenuItem_SubSample);
		addProcessMenuItem(jMenuItem_FitBackground);
		addProcessMenuItem(jMenuItem_StripBackground);
		addProcessMenuItem(jMenuItem_Rescale);
		addProcessMenuItem(jMenuItem_Smooth);
		addProcessMenuItem(jMenuItem_Trim);
		addProcessMenuItem(jMenuItem_PeakSearch);

		jMenuBar_Main.add(jMenu_File);
		jMenuBar_Main.add(jMenu_Process);
		jMenuBar_Main.add(jMenu_Tools);
		jMenuBar_Main.add(jMenu_Report);
		// jMenuBar_Main.add(jMenu_PlugIn);
		jMenuBar_Main.add(jMenu_Help);

		setJMenuBar(jMenuBar_Main);

		setSize(new Dimension(642, 429));
		setTitle(DTSA2.APP_NAME + " - Power Tools for Microanalysis");

		// final CellConstraints cc = new CellConstraints();

		jButton_SpecUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mDataManager.moveSelectedUp();
			}
		});
		jButton_SpecDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mDataManager.moveSelectedDown();
			}
		});
		jButton_SpecGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mDataManager.group();
			}
		});
		jButton_SpecDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jButtonClear_actionPerformed(e);
			}
		});

		jComboBox_Instrument.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateDetector(null);
			}
		});

		jComboBox_Detector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateDefaultDetector();
			}
		});
		final PanelBuilder pb1 = new PanelBuilder(new FormLayout("175dlu", "pref, 2dlu, fill:pref:grow"),
				jPanel_SpecList);
		JPanel defDet = new JPanel(new FormLayout("fill:pref:grow", "pref, 2dlu, pref"));
		defDet.add(jComboBox_Instrument, CC.xy(1, 1));
		defDet.add(jComboBox_Detector, CC.xy(1, 3));
		defDet.setBorder(DTSA2.createTitledBorder("Default Detector"));
		JPanel specList = new JPanel(new FormLayout("168dlu", "fill:pref:grow, 2dlu, pref"));
		specList.add(new JScrollPane(jList_Spectrum), CC.xy(1, 1));
		{
			PanelBuilder bbb = new PanelBuilder(new FormLayout(
					"fill:pref:grow, pref, 10dlu, pref, 10dlu, pref, 10dlu,pref, fill:pref:grow", "pref"));
			bbb.add(jButton_SpecDown, CC.xy(2, 1));
			bbb.add(jButton_SpecUp, CC.xy(4, 1));
			bbb.add(jButton_SpecGroup, CC.xy(6, 1));
			bbb.add(jButton_SpecDelete, CC.xy(8, 1));
			specList.add(bbb.getPanel(), CC.xy(1, 3));
		}
		specList.setBorder(DTSA2.createTitledBorder("Spectrum list"));
		pb1.add(defDet, CC.xy(1, 1));
		pb1.add(specList, CC.xy(1, 3));

		updateDetector(AppPreferences.getInstance().getDefaultDetector());
		jList_Spectrum.setToolTipText("Right click for a context menu");

		jKLMTreePanel.setSession(DTSA2.getSession());
		jKLMTreePanel.setBorder(DTSA2.createTitledBorder("KLM Lines"));
		jKLMTreePanel.addVisibleLinesActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				assert e instanceof KLMActionEvent;
				if (e instanceof KLMActionEvent)
					jSpecDisplay_Main.handleKLMActionEvent((KLMActionEvent) e);
			}
		});
		jKLMTreePanel.addTemporaryLinesActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final KLMActionEvent kae = (KLMActionEvent) e;
				final Set<KLMLine> lines = jKLMTreePanel.getTemporaryLines();
				switch (kae.getAction()) {
				case REMOVE_LINES:
					// jSpecDisplay_Main.setTemporaryKLMs(lines);
					break;
				case ADD_LINES:
					jSpecDisplay_Main.setTemporaryKLMs(lines);
					break;
				case CLEAR_ALL:
					break;
				}
			}
		});

		{
			jTable_SpecComposition.setForeground(FOREGROUND_COLOR);
			jTable_SpecComposition.setBackground(BACKGROUND_COLOR);
			// jTable_SpecComposition.setMinimumSize(new Dimension(100, 100));
			jTable_SpecComposition.setModel(new CompositionTableModel(null, false));
			final JPopupMenu pum = new JPopupMenu();
			final JMenuItem copy = new JMenuItem("Copy");
			final JMenuItem html = new JMenuItem("Copy to HTML");
			final JMenuItem setklms = new JMenuItem("Set KLMs");
			copy.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (jTable_SpecComposition.getModel() instanceof CompositionTableModel) {
						final CompositionTableModel model = (CompositionTableModel) jTable_SpecComposition.getModel();
						final StringSelection ss = new StringSelection(model.toString());
						getToolkit().getSystemClipboard().setContents(ss, null);
					}
					if (jTable_SpecComposition.getModel() instanceof ParticleSignatureTableModel) {
						final ParticleSignatureTableModel model = (ParticleSignatureTableModel) jTable_SpecComposition
								.getModel();
						final StringSelection ss = new StringSelection(model.toString());
						getToolkit().getSystemClipboard().setContents(ss, null);
					}

				}
			});
			html.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (jTable_SpecComposition.getModel() instanceof CompositionTableModel) {
						final CompositionTableModel model = (CompositionTableModel) jTable_SpecComposition.getModel();
						final StringSelection ss = new StringSelection(model.toHTML());
						getToolkit().getSystemClipboard().setContents(ss, null);
					}
					if (jTable_SpecComposition.getModel() instanceof ParticleSignatureTableModel) {
						final ParticleSignatureTableModel model = (ParticleSignatureTableModel) jTable_SpecComposition
								.getModel();
						final StringSelection ss = new StringSelection(model.toHTML());
						getToolkit().getSystemClipboard().setContents(ss, null);
					}
				}
			});
			setklms.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (jTable_SpecComposition.getModel() instanceof CompositionTableModel) {
						final CompositionTableModel model = (CompositionTableModel) jTable_SpecComposition.getModel();
						showKLMLines(model.getComposition().getElementSet());
					}
					if (jTable_SpecComposition.getModel() instanceof ParticleSignatureTableModel) {
						final ParticleSignatureTableModel model = (ParticleSignatureTableModel) jTable_SpecComposition
								.getModel();
						showKLMLines(model.getSignature().getAllElements());
					}
				}
			});
			pum.add(copy);
			pum.add(html);
			pum.add(setklms);
			jTable_SpecComposition.addMouseListener(new PopupMenuMouseAdapter(jTable_SpecComposition, pum));
		}

		jPanel_Composition.setLayout(new FormLayout("default", "default, 2dlu, fill:pref:grow"));
		jPanel_Composition.add(jKLMTreePanel, CC.xy(1, 1));
		{
			final JScrollPane sp = new JScrollPane(jTable_SpecComposition);
			sp.setPreferredSize(new Dimension(100, 100));
			sp.setBorder(jBorder_Composition);
			sp.setBackground(PANEL_COLOR);
			jPanel_Composition.add(sp, CC.xy(1, 3));
		}

		jStatusBar_Main.setText(DTSA2.APP_NAME + " - based on the Electron Probe Quant algorithm library");
		jStatusBar_Main.setForeground(SystemColor.controlText);

		ToolTipManager.sharedInstance().setDismissDelay(10000);
		jTable_SpecProperties.setForeground(FOREGROUND_COLOR);
		jTable_SpecProperties.setBackground(BACKGROUND_COLOR);

		jTable_SpecProperties.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				final int sr = jTable_SpecProperties.getSelectedRow();
				if (sr >= 0) {
					final TableModel model = jTable_SpecProperties.getModel();
					final Object val = model.getValueAt(sr, 1);
					final String header = model.getValueAt(sr, 0).toString();
					final String str = header + " = " + val.toString();
					jStatusBar_Main.setText(str);
					jTable_SpecProperties.setToolTipText(wordWrap("<html>" + str, 40));
					if (val instanceof Composition) {
						updateCompositionTable(header, (Composition) val);
						jMenuItem_SetKLMs.setEnabled(jTable_SpecProperties.getSelectedRowCount() == 1);
					} else if (val instanceof ParticleSignature) {
						updateParticleSignature((ParticleSignature) val);
						jMenuItem_SetKLMs.setEnabled(jTable_SpecProperties.getSelectedRowCount() == 1);
					} else if (header == "Element List")
						jMenuItem_SetKLMs.setEnabled(jTable_SpecProperties.getSelectedRowCount() == 1);
					else
						jMenuItem_SetKLMs.setEnabled(false);
				} else
					jMenuItem_SetKLMs.setEnabled(false);
			}
		});
		final JPopupMenu pum = new JPopupMenu();
		final JMenuItem copy = new JMenuItem("Copy");
		copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final int[] rows = jTable_SpecProperties.getSelectedRows();
				final StringBuffer sb = new StringBuffer();
				final TableModel model = jTable_SpecProperties.getModel();
				for (final int sr : rows)
					if (sr >= 0) {
						if (sb.length() > 0)
							sb.append("\n");
						sb.append(model.getValueAt(sr, 0).toString());
						sb.append("\t");
						sb.append(model.getValueAt(sr, 1).toString());
					}
				if (sb.length() > 0) {
					final StringSelection ss = new StringSelection(sb.toString());
					getToolkit().getSystemClipboard().setContents(ss, ss);
				}
			}
		});
		pum.add(copy);
		final JMenuItem copyvalue = new JMenuItem("Copy value(s)");
		copyvalue.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final int[] rows = jTable_SpecProperties.getSelectedRows();
				final StringBuffer sb = new StringBuffer();
				final TableModel model = jTable_SpecProperties.getModel();
				for (final int sr : rows)
					if (sr >= 0) {
						if (sb.length() > 0)
							sb.append("\n");
						sb.append(model.getValueAt(sr, 1).toString());
					}
				if (sb.length() > 0) {
					final StringSelection ss = new StringSelection(sb.toString());
					getToolkit().getSystemClipboard().setContents(ss, ss);
				}
			}
		});
		pum.add(copyvalue);
		jMenuItem_SetKLMs.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 9129506582520064690L;

			@Override
			public void actionPerformed(ActionEvent e) {
				final int[] rows = jTable_SpecProperties.getSelectedRows();
				if (rows.length == 1) {
					final TableModel model = jTable_SpecProperties.getModel();
					int row = rows[0];
					Object obj = model.getValueAt(row, 1);
					if (model.getValueAt(row, 0).toString() == "Element List")
						showKLMLines(Element.parseElementString(obj.toString()));
					else if (obj instanceof Composition)
						showKLMLines(((Composition) obj).getElementSet());
					else if (obj instanceof ParticleSignature)
						showKLMLines(((ParticleSignature) obj).getAllElements());

				}
			}
		});
		pum.add(jMenuItem_SetKLMs);
		jTable_SpecProperties.addMouseListener(new PopupMenuMouseAdapter(jTable_SpecProperties, pum));

		{
			final JScrollPane sp = new JScrollPane(jTable_SpecProperties);
			sp.setBackground(PANEL_COLOR);
			sp.setBorder(DTSA2.createTitledBorder("Spectrum Properties"));
			jPanel_Spectrum.setLayout(new FormLayout("175dlu, default:grow, default", "fill:default:grow"));
			jPanel_Spectrum.add(jPanel_SpecList, CC.xy(1, 1));
			jPanel_Spectrum.add(sp, CC.xy(2, 1));
			jPanel_Spectrum.add(jPanel_Composition, CC.xy(3, 1));
		}
		jPanel_Command = new JPanel(new BorderLayout());

		jButton_OpenPy.setText("Open");
		jButton_OpenPy.setToolTipText("Open and run a Python script file.");
		jButton_OpenPy.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = 5130508190825691790L;

			@Override
			public void actionPerformed(ActionEvent e) {
				openPythonScript();
			}

		});

		jButton_Stop.setText("Terminate");
		jButton_Stop.setToolTipText("Request that a script terminate execution.");
		jButton_Stop.addActionListener(new AbstractAction() {

			private static final long serialVersionUID = -7587081379423939674L;

			@Override
			public void actionPerformed(ActionEvent e) {
				jCommandLine_Main.terminateScript();
			}
		});
		jButton_Stop.setEnabled(false);

		jComboBox_PrevPy.setModel(mRecentPyModel = new RecentPyModel());
		if (mRecentPyModel.getSize() > 0)
			mRecentPyModel.setSelectedItem(mRecentPyModel.getElementAt(0));
		jComboBox_PrevPy.setToolTipText("Select a recent script to rerun.");
		final AbstractAction pyActionListener = new AbstractAction() {

			private static final long serialVersionUID = -5513291816424696859L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				runRecentPy();
			}
		};
		jComboBox_PrevPy.addActionListener(pyActionListener);

		final Icon playIcon = new ImageIcon(getClass().getResource("play.png"));
		jButton_Play.setIcon(playIcon);
		jButton_Play.setToolTipText("Rerun the selected Python script.");
		jButton_Play.setEnabled(false);
		jButton_Play.addActionListener(pyActionListener);

		final JPanel btnPanel = new JPanel(new FormLayout("pref, 5dlu, pref, 5dlu, 100 dlu, 5dlu, pref", "pref"));
		btnPanel.add(jButton_OpenPy, CC.xy(1, 1));
		btnPanel.add(jButton_Stop, CC.xy(3, 1));
		btnPanel.add(jComboBox_PrevPy, CC.xy(5, 1));
		btnPanel.add(jButton_Play, CC.xy(7, 1));

		jPanel_Command.add(btnPanel, BorderLayout.NORTH);
		jPanel_Command.add(new JScrollPane(jCommandLine_Main), BorderLayout.CENTER);

		addUtilityTab("Spectrum", jPanel_Spectrum, "Windows for interacting with spectra");
		final JScrollPane tab = new JScrollPane(getTextPane_Log());
		addUtilityTab("Report", tab, "An HTML summary of this DTSA-II session");
		{
			final ImageIcon ii = new ImageIcon(MainFrame.class.getResource("python.png"));
			jTabbedPane_Utility.addTab("Command", ii, jPanel_Command,
					"A command line for controlling DTSA-II in Python");
		}
		/*
		 * if(System.getProperty("user.name").equalsIgnoreCase("nritchie")) {
		 * jPanel_MapViewer = new JXMapViewer(); // Create a TileFactoryInfo for
		 * OpenStreetMap TileFactoryInfo info = new OSMTileFactoryInfo();
		 * DefaultTileFactory tileFactory = new DefaultTileFactory(info);
		 * jPanel_MapViewer.setTileFactory(tileFactory); // Use 8 threads in parallel to
		 * load the tiles tileFactory.setThreadPoolSize(8); MouseInputListener mia = new
		 * PanMouseInputListener(jPanel_MapViewer);
		 * jPanel_MapViewer.addMouseListener(mia);
		 * jPanel_MapViewer.addMouseMotionListener(mia);
		 * jPanel_MapViewer.addMouseListener(new CenterMapListener(jPanel_MapViewer));
		 * jPanel_MapViewer.addMouseWheelListener(new
		 * ZoomMouseWheelListenerCursor(jPanel_MapViewer));
		 * jPanel_MapViewer.addKeyListener(new PanKeyListener(jPanel_MapViewer)); final
		 * ImageIcon ii = new ImageIcon(MainFrame.class.getResource("osm.png"));
		 * jTabbedPane_Utility.addTab("Map", ii, new JScrollPane(jPanel_MapViewer),
		 * "An OpenStreetMaps-based map view."); jPanel_MapViewer.setZoom(4);
		 * jPanel_MapViewer.setCenterPosition(new GeoPosition(39.13260, -77.21694)); }
		 */

		jTabbedPane_Utility.addMouseListener(new PopupMenuMouseAdapter(jTabbedPane_Utility, jPopupMenu_Tabs));

		// Force the focus to the command line window
		jTabbedPane_Utility.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (jTabbedPane_Utility.getSelectedComponent() == jPanel_Command)
					jCommandLine_Main.grabFocus();
			}
		});
		initCommandLine();

		jTabbedPane_Utility.setSelectedComponent(jPanel_Spectrum);

		jSpecDisplay_Main.setShowImage(true);
		jSpecDisplay_Main.setMinimumSize(new Dimension(200, 250));

		DropTargetListener dropTargetListener = new DropTargetAdapter() {

			@Override
			public void drop(DropTargetDropEvent dtde) {
				try {
					DataFlavor flavor = DataFlavor.javaFileListFlavor;
					if (dtde.getTransferable().isDataFlavorSupported(flavor)) {
						dtde.acceptDrop(DnDConstants.ACTION_COPY);
						Object data = dtde.getTransferable().getTransferData(flavor);
						if (data instanceof List<?>) {
							final List<?> files = (List<?>) data;
							final List<ISpectrumData> success = new ArrayList<ISpectrumData>();
							final List<Object> failed = new ArrayList<Object>();
							for (Object file : files) {
								try {
									if (file instanceof File) {
										ISpectrumData[] specs = SpectrumFile.open((File) file);
										for (ISpectrumData spec : specs)
											success.add(spec);
									} else
										failed.add(file);
								} catch (Exception e) {
									failed.add(file);
								}
							}
							if (success.size() > 0)
								importSpectra(success.toArray(new ISpectrumData[success.size()]), true);
							if (failed.size() > 0) {
								ErrorDialog.createErrorMessage(MainFrame.this, "Drag-and-Drop",
										"Unable to open " + failed.size() + " dropped files.", failed.toString());
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		jSpecDisplay_Main.setDropTarget(
				new DropTarget(jSpecDisplay_Main, DnDConstants.ACTION_COPY_OR_MOVE, dropTargetListener, true, null));
		jList_Spectrum.setDropTarget(
				new DropTarget(jList_Spectrum, DnDConstants.ACTION_COPY_OR_MOVE, dropTargetListener, true, null));

		final File colorCsv = new File(HTMLReport.getBasePath(), "specColors.csv");
		if (colorCsv.isFile()) {
			final Color[] colors = SpecDisplay.loadColors(colorCsv);
			if (colors != null)
				jSpecDisplay_Main.setSpectrumColors(colors);
		}

		final JPanel specPanel = new JPanel();
		specPanel.setLayout(new FormLayout("default, default:grow", "fill:default:grow"));
		specPanel.add(jSpectrumToolBar_Main, CC.xy(1, 1));
		specPanel.add(jSpecDisplay_Main, CC.xy(2, 1));

		jSplitPane_MainVert = new JSplitPane();
		jSplitPane_MainVert.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPane_MainVert.setRequestFocusEnabled(true);
		jSplitPane_MainVert.add(specPanel, JSplitPane.TOP);
		jSplitPane_MainVert.add(jTabbedPane_Utility, JSplitPane.BOTTOM);
		jSplitPane_MainVert.setResizeWeight(0.5);

		final JPanel contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(jStatusBar_Main, BorderLayout.SOUTH);
		contentPane.add(jSplitPane_MainVert, BorderLayout.CENTER);
	}

	/**
	 * Initialize the Python representation of the detectors within the global
	 * variable of the interactive console.
	 */
	public void initDetectors() {
		jCommandLine_Main.executeNT("init_dets(globals())");
	}

	/**
	 * Description
	 */
	private void initCommandLine() {
		try {
			try (final InputStream is = getClass().getResourceAsStream("initialize.py")) {
				final StringBuffer cmd = new StringBuffer();
				final InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
				final BufferedReader br = new BufferedReader(isr);
				while (br.ready()) {
					cmd.append(br.readLine());
					cmd.append("\n");
				}
				jCommandLine_Main.execute(cmd.toString());
			}
		} catch (final Exception e1) {
			jCommandLine_Main.writeError(e1.getMessage());
		}
		try {
			final String startUp = AppPreferences.getInstance().getStartupScript();
			if (!startUp.equals("")) {
				final File sf = new File(startUp);
				if (sf.isFile() && sf.canRead())
					jCommandLine_Main.run(sf);
			}
		} catch (final Throwable e) {
			jCommandLine_Main.writeError(e.getMessage());
		}
		final JythonWorker jythonWorker = jCommandLine_Main.getJythonWorker();
		mDataManager.setJythonWorker(jythonWorker);
		try {
			final File tf = File.createTempFile("cmd", ".txt", DTSA2.getReport().getFile().getParentFile());
			final FileWriter fw = new FileWriter(tf);
			jCommandLine_Main.setArchivalWriter(fw);
			appendHTML("<h3>Command Line</h3><ul><li>Output file: <i>" + tf.getAbsolutePath() + "</i></li></ul>");
		} catch (final IOException e) {
			ErrorDialog.createErrorMessage(this, "Error creating the command line log", e);
		}
		jythonWorker.execute();
		jStatusBar_Main.setText("Welcome to " + DTSA2.APP_NAME + " - " + DTSA2.getRevision(DTSA2.class) + " revision");
	}

	public SpectrumProperties doEditSpectrumProperties(Set<SpectrumProperties> sps, Session ses) {
		final SpectrumPropertyPanel.PropertyDialog dlg = new SpectrumPropertyPanel.PropertyDialog(this, ses);
		for (final SpectrumProperties sp : sps)
			dlg.addSpectrumProperties(sp);
		dlg.setLocationRelativeTo(this);
		dlg.setVisible(true);
		return dlg.getSpectrumProperties();
	}

	public ISpectrumData[] selectSpectraFromFiles() {
		final SpectrumFileChooser sfc = new SpectrumFileChooser(this, "Open spectrum files...");
		final File dir = new File(DTSA2.getSpectrumDirectory());
        sfc.setMultiSelectionEnabled(true);
		sfc.getFileChooser().setCurrentDirectory(dir);
		sfc.setLocationRelativeTo(this);
		if(sfc.showOpenDialog() == JFileChooser.APPROVE_OPTION) {
			DTSA2.updateSpectrumDirectory(sfc.getFileChooser().getCurrentDirectory());
			return sfc.getSpectra();
		}
		return null;
	}

	public static File selectFileDestination(String fn, Component parent) {
		// Clean up some common file name issues...
		fn = normalizeFilename(fn);
		final String dir = DTSA2.getSpectrumDirectory();
		final JFileChooser jfc = new JFileChooser(dir);
		jfc.addChoosableFileFilter(new SimpleFileFilter(new String[] { "csv", }, "CSV Text File"));
		jfc.addChoosableFileFilter(
				new SimpleFileFilter(new String[] { "tia.msa" }, "TIA Compatible EMSA/MAS Standard"));
		jfc.addChoosableFileFilter(new SimpleFileFilter(new String[] { "tif", "tiff" }, "ASPEX-style TIFF Spectrum"));
		final SimpleFileFilter emsa = new SimpleFileFilter(new String[] { "msa", "emsa", "txt" },
				"EMSA/MAS Standard File");
		jfc.addChoosableFileFilter(emsa);
		jfc.setFileFilter(emsa);
		jfc.setSelectedFile(new File(dir, fn));
		jfc.setDialogTitle("Save " + fn + " as...");
		final int option = jfc.showSaveDialog(parent);
		if (option == JFileChooser.APPROVE_OPTION) {
			File res = jfc.getSelectedFile();
			if (res != null) {
				if (jfc.getFileFilter() instanceof SimpleFileFilter)
					res = ((SimpleFileFilter) jfc.getFileFilter()).forceExtension(res);
				DTSA2.updateSpectrumDirectory(res.getParentFile());
				return res;
			}
		}
		return null;
	}

	public File selectOutputPath() {
		JFileChooser chooser = new JFileChooser();
		final String dir = DTSA2.getSpectrumDirectory();
		chooser.setCurrentDirectory(new java.io.File(dir));
		chooser.setDialogTitle("Select Output Directory");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			final File res = chooser.getSelectedFile();
			if ((res != null) && (res.isDirectory())) {
				DTSA2.updateSpectrumDirectory(res);
				return res;
			}
		}
		return null;
	}

	/**
	 * Actions to perform when exiting the program...
	 */
	public void fileExit() {
		final Preferences userPref = Preferences.userNodeForPackage(MainFrame.class);
		{
			final Rectangle bounds = getBounds();
			userPref.putInt(POSITION_WIDTH, (int) bounds.getWidth());
			userPref.putInt(POSITION_HEIGHT, (int) bounds.getHeight());
			userPref.putInt(POSITION_TOP, (int) bounds.getX());
			userPref.putInt(POSITION_LEFT, (int) bounds.getY());
			userPref.putInt(SPLITTER_POSITION, jSplitPane_MainVert.getDividerLocation());
		}
		userPref.putInt("KLMDialog\\Atomic Number", Element.elmH);
		if (mSessionStarted == null)
			appendHTML("<p><b>Session Terminated:</b> " + DateFormat.getTimeInstance().format(new Date()) + "</p>");
		performShutdownScript();
		setVisible(false);
		System.exit(0);
	}

	private void performShutdownScript() {
		try {
			final String shutdown = AppPreferences.getInstance().getShutdownScript();
			if (!shutdown.equals("")) {
				final File sf = new File(shutdown);
				if (sf.isFile() && sf.canRead())
					jCommandLine_Main.run(sf);
			}
		} catch (final Throwable e) {
			jCommandLine_Main.writeError(e.getMessage());
		}
	}

	public void filePreferences() {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				AppPreferences.editPreferences(MainFrame.this);
				initDetectors();
				updateDetector(null);
			}
		};
		invokeCarefully(r);
	}

	public void toolsEditStandards() {
		final StandardDatabaseEditor sdbe = new StandardDatabaseEditor(MainFrame.this, DTSA2.getStandardsDatabase(),
				DTSA2.getSession());
		sdbe.setLocationRelativeTo(MainFrame.this);
		sdbe.setVisible(true);
		if (sdbe.isOk() && sdbe.getDatabase().isModified()) {
			DTSA2.updateStandardsDatabase(sdbe.getDatabase());
			jStatusBar_Main.setText("Standards database updated.");
		}
	}

	/*
	 * private class PluginAction extends AbstractAction { private static final long
	 * serialVersionUID = -6928508033213127087L; private final PyFunction mFunction;
	 * public PluginAction(String name, PyFunction func) { super(name); mFunction =
	 * func; }
	 * 
	 * @Override public void actionPerformed(ActionEvent arg0) { try {
	 * mFunction.__call__(); } catch(Error e) { } } }; public void
	 * installPlugIn(String menuItemName, PyFunction func) { jMenu_PlugIn.add(new
	 * PluginAction(menuItemName, func)); }
	 */

	/**
	 * Help -&gt; About menu item
	 */
	public void helpAbout() {
		final MainFrame_AboutBox dlg = new MainFrame_AboutBox(this);
		dlg.setLocationRelativeTo(this);
		dlg.setModal(true);
		dlg.setVisible(true);
	}

	public void helpOpenWebSite() {
		try {
			final URL url = new URL("http://www.cstl.nist.gov/div837/837.02/epq/dtsa2/index.html");
			Desktop.getDesktop().browse(url.toURI());
		} catch (final Exception e) {
			ErrorDialog.createErrorMessage(this, "Open web site", e);
		}
	}

	// Overridden so we can exit when window is closed
	@Override
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING)
			fileExit();
	}

	/**
	 * File -&gt; Open menu item
	 */
	public void fileOpen() {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				jTabbedPane_Utility.setSelectedComponent(jPanel_Spectrum);
				importSpectra(selectSpectraFromFiles(), true);
			}
		};
		invokeCarefully(r);
	}

	public void fileOpenReport() {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				final String dir = DTSA2.getReport().getFile().getParentFile().getParent();
				final JFileChooser jfc = new JFileChooser(dir);
				jfc.addChoosableFileFilter(new SimpleFileFilter(new String[] { "html", }, "HTML Report File"));
				jfc.setDialogTitle("Open " + DTSA2.APP_NAME + " report file");
				jfc.setMultiSelectionEnabled(false);
				final int option = jfc.showOpenDialog(MainFrame.this);
				if (option == JFileChooser.APPROVE_OPTION)
					try {
						final File f = jfc.getSelectedFile();
						final JTextPane tp = createReportPage(f);
						final String name = f.getName();
						final Matcher m = Pattern.compile("index([0-9]*).html").matcher(name);
						final String title = "Report" + (m.find() ? " " + m.group(1) : "") + ": "
								+ f.getParentFile().getName();
						for (int i = 0; i < jTabbedPane_Utility.getTabCount(); ++i)
							if (jTabbedPane_Utility.getTitleAt(i).equals(title)) {
								jTabbedPane_Utility.setSelectedIndex(i);
								return;
							}
						final JScrollPane sp = new JScrollPane(tp);
						jTabbedPane_Utility.setSelectedComponent(jTabbedPane_Utility.add(title, sp));
						final JMenuItem mi = new JMenuItem("Remove " + title);
						class TrashReport implements ActionListener {

							private final JMenuItem mMenuItem;
							private final Component mTabComponent;

							TrashReport(JMenuItem mi, Component c) {
								mTabComponent = c;
								mMenuItem = mi;
							}

							@Override
							public void actionPerformed(ActionEvent e) {
								jTabbedPane_Utility.remove(mTabComponent);
								jPopupMenu_Tabs.remove(mMenuItem);
							}

						}
						mi.addActionListener(new TrashReport(mi, sp));

						jPopupMenu_Tabs.add(mi);
					} catch (final IOException e) {
						ErrorDialog.createErrorMessage(MainFrame.this, "Open report", e);
					}
			}
		};
		invokeCarefully(r);
	}

	public void openSpectrumFile(File f) throws EPQException {
		importSpectra(SpectrumFile.open(f), true);
	}

	private void importSpectra(final ISpectrumData[] specs, boolean scrollTo) {
		if (specs != null) {
			final Session ses = DTSA2.getSession();
			final DetectorProperties det = AppPreferences.getInstance().getDefaultDetector();
			final HTMLList list = new HTMLList();
			boolean dontApplyToAll = false;
			for (int j = 0; j < specs.length; ++j) {
				ISpectrumData sd = specs[j];
				if (det != null) {
					final Date ts = sd.getProperties().getTimestampWithDefault(SpectrumProperties.AcquisitionTime,
							new Date());
					final DetectorCalibration dc = ses.getSuitableCalibration(det, ts);
					if (dc instanceof EDSCalibration) {
						boolean assign = SpectrumUtils.areCalibratedSimilar(dc.getProperties(), sd,
								AppPreferences.DEFAULT_TOLERANCE);
						if (!dontApplyToAll) {
							if (!assign) {
								final int opt = JOptionPane.showConfirmDialog(MainFrame.this,
										"<html>The calibration of <i>" + sd.toString() + "</i><br>"
												+ "does not seem to be similar to the default detector.<br><br>"
												+ "Apply the default detector none the less?",
										"Spectrum open", JOptionPane.YES_NO_CANCEL_OPTION);
								if (opt == JOptionPane.CANCEL_OPTION) {
									if ((j == 0) && (specs.length > 1))
										if (JOptionPane.showConfirmDialog(MainFrame.this,
												"Cancel all " + Integer.toString(specs.length) + " spectra?",
												"File Open", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
											break;
									continue;
								}
								if (opt == JOptionPane.NO_OPTION)
									if ((j == 0) && (specs.length > 1))
										dontApplyToAll = JOptionPane.showConfirmDialog(MainFrame.this,
												"Don't apply the default detector to all "
														+ Integer.toString(specs.length) + " spectra?",
												"File Open", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
								assign = (opt == JOptionPane.YES_OPTION);
							}
							if (assign)
								sd = SpectrumUtils
										.applyEDSDetector(EDSDetector.createDetector(det, (EDSCalibration) dc), sd);
						}
					}
				}
				final double res = DuaneHuntLimit.DefaultDuaneHunt.compute(sd);
				if (!Double.isNaN(res))
					sd.getProperties().setNumericProperty(SpectrumProperties.DuaneHunt, FromSI.keV(res));
				addSpectrum(sd, true);
				list.add("Opened " + sd.getProperties().asURL(sd));
			}
			if (list.size() > 0) {
				if (list.size() > 1)
					list.setHeader("Opening spectra from disk...");
				else
					list.setHeader("Opening spectrum from disk...");
				appendHTML(list.toString());
			}
		}
	}

	private ISpectrumData readUCalFile(File file) throws EPQException {
		final ArrayList<double[]> lines = new ArrayList<double[]>();
		try {
			try (final FileInputStream fis = new FileInputStream(file)) {
				try (final InputStreamReader isr = new InputStreamReader(fis, Charset.forName("US-ASCII"))) {
					try (final BufferedReader br = new BufferedReader(isr, 2048)) {
						// Toss first line...
						br.readLine();
						final NumberFormat nf = NumberFormat.getInstance(Locale.US);
						while (br.ready()) {
							final String line = br.readLine();
							if (line != null) {
								final String[] items = line.split("\t", 5);
								try {
									final double[] nitems = new double[items.length];
									for (int i = 0; i < items.length; ++i)
										nitems[i] = nf.parse(items[i]).doubleValue();
									lines.add(nitems);
								} catch (final ParseException e) {
									throw new EPQException(e);
								}
							}
						}
					}
				}
			}
		} catch (final IOException e) {
			return null;
		}
		final EditableSpectrum es = new EditableSpectrum(lines.size(), 1.0, 0.0);
		for (int i = 0; i < lines.size(); i++)
			es.setCounts(i, lines.get(i)[1]);
		final SpectrumProperties sp = es.getProperties();
		sp.setTextProperty(SpectrumProperties.DetectorDescription, "uCal");
		sp.setTimestampProperty(SpectrumProperties.AcquisitionTime, new Date(file.lastModified()));
		SpectrumUtils.rename(es, file.getName());
		return es;
	}

	/**
	 * Description
	 */
	public void fileImport() {
		final String dir = DTSA2.getSpectrumDirectory();
		final JFileChooser fc = new JFileChooser(dir);
		final SimpleFileFilter csvFF = new SimpleFileFilter(new String[] { "csv" }, "CSV Files");
		final SimpleFileFilter edaxFF = new SimpleFileFilter(new String[] { "csv" }, "EDAX CSV files (10 eV/ch)");
		final SimpleFileFilter ucalFF = new SimpleFileFilter(new String[] { "txt" }, "uCal TSV files (1 eV/ch)");

		fc.addChoosableFileFilter(csvFF);
		fc.addChoosableFileFilter(edaxFF);
		fc.addChoosableFileFilter(ucalFF);
		fc.setAcceptAllFileFilterUsed(true);
		fc.setFileFilter(csvFF);
		fc.setMultiSelectionEnabled(true);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			final File[] files = fc.getSelectedFiles();
			for (final File file : files)
				if (fc.getFileFilter() == ucalFF)
					try {
						// Read in a uCal spectrum assuming 1 eV/ch
						addSpectrum(readUCalFile(file), true);
					} catch (final EPQException e) {
						e.printStackTrace();
					}
				else {
					final CSVReader csv = new CSVReader.FileReader(file, false);
					final double[][] tbl = csv.getResource();
					double w0 = 10.0, off0 = 0.0; // eV
					double mult = 1.0;
					int col = 0;
					if (tbl.length > 1) {
						if (fc.getFileFilter() != edaxFF) {
							off0 = tbl[0][0];
							// Use the first element to determine the width per
							// channel
							w0 = (tbl[tbl.length - 1][0] - tbl[0][0]) / tbl.length;
							if (w0 < 0.1)
								mult = 1000.0;
						}
						col = 1;
					}
					final EditableSpectrum es = new EditableSpectrum(tbl.length, mult * w0, mult * off0);
					for (int i = 0; i < tbl.length; ++i)
						es.setCounts(i, tbl[i][col]);
					SpectrumUtils.rename(es, file.getName());
					addSpectrum(es, true);
				}
		}
	}

	public void writeErrorLog(String str) {
		appendHTML("<p class=\"error\">ERROR: " + TextUtilities.normalizeHTML(str) + "</p>");
		jStatusBar_Main.setText("An error occured. See the log for details.");
	}

	public void toolsRenameSpectrum() {
		List<ISpectrumData> specs = jList_Spectrum.getSelectedValuesList();
		if (specs.size() > 0) {
			final Preferences userPref = Preferences.userRoot();
			final String rule = userPref.get("RENAMER", "$PREV$ $COMP$ $E0$ $I0$ $I$");
			RenameDialog rd = new RenameDialog(MainFrame.this, rule);
			rd.setLocationRelativeTo(jList_Spectrum.getParent());
			rd.setVisible(true);
			if (rd.isOk()) {
				SpectrumRenamer sn = new SpectrumRenamer(rd.getName());
				StringBuffer sb = new StringBuffer();
				sb.append("<table>\n\t<tr><th>Old name</th><th>New name</th></tr>\n");
				for (ISpectrumData spec : specs) {
					final String newName = sn.computerizer(spec.getProperties());
					sb.append("\t<tr><td>" + spec.toString() + "</td><td>" + newName + "</td></tr>\n");
					SpectrumUtils.rename(spec, newName);
				}
				sb.append("</table>");
				appendHTML(sb.toString());
				userPref.put("RENAMER", rd.getName());
			}
			mDataManager.notifyUpdated();
		}
	}

	private static class RenameDialog extends JDialog {

		private static final long serialVersionUID = 8712540673745236074L;
		private String mResult;
		final private JTextField jTextField_Name;

		private RenameDialog(Frame fm, String name) {
			super(fm);
			setUndecorated(true);
			setOpacity(0.80F);
			setTitle("Rename spectrum");
			PanelBuilder pb = new PanelBuilder(new FormLayout("100dlu, 2dlu, pref, 2dlu, pref", "pref"));
			jTextField_Name = new JTextField(name);
			jTextField_Name.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						mResult = null;
						RenameDialog.this.setVisible(false);
					} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						mResult = jTextField_Name.getText();
						RenameDialog.this.setVisible(false);
					}
				}
			});
			JButton ok = new JButton("\u2713");
			ok.addActionListener(new AbstractAction() {

				private static final long serialVersionUID = -1733997090311936802L;

				@Override
				public void actionPerformed(ActionEvent e) {
					mResult = jTextField_Name.getText();
					setVisible(false);
				}
			});
			JButton cancel = new JButton("\u2718");
			cancel.addActionListener(new AbstractAction() {

				private static final long serialVersionUID = 3840241556783104777L;

				@Override
				public void actionPerformed(ActionEvent e) {
					mResult = null;
					setVisible(false);
				}
			});
			pb.add(jTextField_Name, CC.xy(1, 1));
			pb.add(ok, CC.xy(3, 1));
			pb.add(cancel, CC.xy(5, 1));
			JPanel panel = pb.getPanel();
			panel.setBorder(DTSA2.createEmptyBorder());
			add(panel);
			pack();
			setModal(true);
			jTextField_Name.requestFocus();
			class RenameAction extends AbstractAction {

				private static final long serialVersionUID = 1826496723123L;
				private final String mToken;

				RenameAction(String mi, String token) {
					super(mi);
					mToken = token;
				}

				@Override
				public void actionPerformed(ActionEvent arg0) {
					StringBuffer sb = new StringBuffer();
					final int cp = jTextField_Name.getCaretPosition();
					final String text = jTextField_Name.getText();
					if ((cp >= 0) && (cp < text.length())) {
						sb.append(text.substring(0, cp));
						sb.append(mToken);
						sb.append(text.substring(cp));
						jTextField_Name.setText(sb.toString());
						jTextField_Name.setCaretPosition(cp + mToken.length());
					}
				}

			}
			JPopupMenu menu = new JPopupMenu();
			menu.add(new RenameAction("Index", "$I$"));
			for (SpectrumRenamer.Rule sr : SpectrumRenamer.Rule.values())
				menu.add(new RenameAction(sr.getDescription(), sr.getToken()));
			jTextField_Name.addMouseListener(new PopupMenuMouseAdapter(jTextField_Name, menu));
		}

		@Override
		public String getName() {
			return mResult;
		}

		public boolean isOk() {
			return mResult != null;
		}
	}

	/**
	 * Tools-&gt;Make standard menu item
	 */
	public void toolsMakeStandard() {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					final List<ISpectrumData> specs = getSelectedSpectra();
					if (specs.size() > 0) {
						final MakeStandardDialog msd = new MakeStandardDialog(MainFrame.this, DTSA2.getSession());
						for (final ISpectrumData sd : specs)
							msd.addSpectrum(sd);
						msd.setLocationRelativeTo(MainFrame.this);
						msd.setVisible(true);
						if (msd.shouldSave()  && (!msd.isCancelled())) {
							final ISpectrumData res = msd.getResult();
							if (res != null) {
								mDataManager.addSpectrum(res, true);
								if (msd.getBestFit() != null)
									mDataManager.addSpectrum(msd.getBestFit(), true, res);
								final HalfUpFormat hu = new HalfUpFormat("#,##0.0");
								final HalfUpFormat hu3 = new HalfUpFormat("0.000");
								final StringBuffer hl = new StringBuffer();
								hl.append("<h3>Making " + res.toString() + "</h3>\n");
								DescriptiveStatistics cpnas = new DescriptiveStatistics();
								final List<ISpectrumData> spectra = msd.getSelected();
								final RegionOfInterestSet rois = msd.computeROIS();
								for (final ISpectrumData spec : spectra)
									cpnas.add(SpectrumUtils.totalCounts(spec, true)
											/ SpectrumUtils.getDose(spec.getProperties()));
								if (spectra.size() > 1) {
									hl.append("<p><table>\n");
									hl.append(
											"<tr><th>Spectrum</th><th>Beam Energy<br>(keV)</th><th>Probe Current<br>(nA)</th><th>Live Time<br>(s)</th><th>Counts/(nA\u00B7S)</th><th>Score</th></tr>\n");
									for (final ISpectrumData spec : spectra) {
										final SpectrumProperties sp = spec.getProperties();
										hl.append("<tr>");
										hl.append("<td>" + spec.toString() + "</td>");
										hl.append("<td>" + hu.format(
												sp.getNumericWithDefault(SpectrumProperties.BeamEnergy, Double.NaN))
												+ "</td>");
										hl.append("<td>"
												+ hu3.format(SpectrumUtils.getAverageFaradayCurrent(sp, Double.NaN))
												+ "</td>");
										hl.append("<td>" + hu.format(
												sp.getNumericWithDefault(SpectrumProperties.LiveTime, Double.NaN))
												+ "</td>");
										final UncertainValue2 uv2 = UncertainValue2.divide(UncertainValue2
												.createGaussian(SpectrumUtils.totalCounts(spec, true), spec.toString()),
												SpectrumUtils.getDose(sp));
										hl.append("<td>" + uv2.format(hu) + "</td>");
										hl.append("<td>" + hu.format(MakeStandardDialog.score(spec, spectra, rois)) + "</td>");
										hl.append("</tr>\n");
									}
									hl.append("</table></p>");
									hl.append("<p><table>\n");
								}
								// Report the standard properties...
								hl.append(
										"<tr><th>Spectrum</th><th>Beam Energy<br>(keV)</th><th>Probe Current<br>(nA)</th><th>Live Time<br>(s)</th><th>Counts/(nA\u00B7S)</th></tr>\n");
								{
									final SpectrumProperties sp = res.getProperties();
									hl.append("<tr>");
									hl.append("<td>" + res.toString() + "</td>");
									hl.append("<td>"
											+ hu.format(
													sp.getNumericWithDefault(SpectrumProperties.BeamEnergy, Double.NaN))
											+ "</td>");
									hl.append(
											"<td>" + hu3.format(SpectrumUtils.getAverageFaradayCurrent(sp, Double.NaN))
													+ "</td>");
									hl.append("<td>"
											+ hu.format(
													sp.getNumericWithDefault(SpectrumProperties.LiveTime, Double.NaN))
											+ "</td>");
									final UncertainValue2 uv2 = UncertainValue2.divide(UncertainValue2
											.createGaussian(SpectrumUtils.totalCounts(res, true), res.toString()),
											SpectrumUtils.getDose(sp));
									hl.append("<td>"+ uv2.format(hu)+ "</td>");
									hl.append("</tr>\n");
								}
								hl.append("</table></p>");
								final List<File> files = MainFrame.this.saveStandards(msd.getBundle());
								File msafile = new File(DTSA2.getSpectrumDirectory(), normalizeFilename(msd.getResult().toString()));
								if (files.size() > 0) {
									hl.append("<ul>\n");
									for (File file : files)
										hl.append("<li>Standard saved as " + file.getName() + "</li>\n");
									hl.append("</ul>\n");
									try {
										exportSpectrumAsEMSA(msd.getResult(), msafile.getAbsolutePath());
										hl.append("<li>Spectrum saved as " + msafile.getName() + "</li>\n");
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
								appendHTML(hl.toString());
							}
						}
					} else
						throw new EPQException("Please select one or more spectra to convert into a standard.");
				} catch (final EPQException e) {
					JOptionPane.showMessageDialog(MainFrame.this, e.getMessage(), "Error creating a standard",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		};
		invokeCarefully(r);
	}

	/**
	 * Tools-&gt;Edit spectrum properties menu item
	 */
	public void toolsEditSpectrumProperties() {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				final Set<SpectrumProperties> sps = new HashSet<SpectrumProperties>();
				for (final ISpectrumData sd : getSelectedSpectra())
					sps.add(sd.getProperties());
				if (sps.size() > 0) {
					final SpectrumProperties res = doEditSpectrumProperties(sps, DTSA2.getSession());
					final EDSDetector det = (res.getDetector() instanceof EDSDetector ? (EDSDetector) res.getDetector()
							: null);
					final List<ISpectrumData> sel = new ArrayList<ISpectrumData>();
					for (final ISpectrumData sd : getSelectedSpectra()) {
						final SpectrumProperties sp = sd.getProperties();
						sp.addAll(res);
						final ISpectrumData tmp = SpectrumUtils.applyEDSDetector(det, sd);
						if (sd != tmp)
							mDataManager.replaceSpectrum(sd, tmp);
						sel.add(tmp);
					}
					mDataManager.setSelected(sel);
					displaySpectrumProperties();
				}
			}
		};
		invokeCarefully(r);
	}

	/**
	 * Tools-&gt;Quantify menu item
	 */
	public void toolsQuantificationAlien() {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				final QuantificationWizard wiz = new QuantificationWizard(MainFrame.this);
				wiz.setLocationRelativeTo(MainFrame.this);
				wiz.setSession(DTSA2.getSession());
				if (wiz.showWizard() == JWizardDialog.FINISHED) {
					final List<ISpectrumData> resSpectra = wiz.getResultSpectra();
					if (resSpectra.size() > 0) {
						ISpectrumData assoc = null;
						for (final ISpectrumData spec : resSpectra) {
							if ((spec instanceof DerivedSpectrum)
									&& (((DerivedSpectrum) spec).getBaseSpectrum() != null))
								assoc = ((DerivedSpectrum) spec).getBaseSpectrum();
							addSpectrum(spec, true, assoc);
						}
					}
					appendHTML(wiz.getResultHTML());
				}
			}
		};
		invokeCarefully(r);

	}

	void jButtonAll_actionPerformed(ActionEvent e) {
		mDataManager.select(mDataManager.spectrumList(), true);
	}

	void jButtonNone_actionPerformed(ActionEvent e) {
		mDataManager.clearSelections();
	}

	public void updateDisplayedSpectra() {
		jSpecDisplay_Main.clearAllSpectra();
		int i = 0;
		for (final ISpectrumData spec : getSelectedSpectra()) {
			jSpecDisplay_Main.addSpectrum(spec);
			++i;
			if (i >= MAX_DISPLAYED_SPECTRA)
				break;
		}
		jSpecDisplay_Main.rescaleV();
		displaySpectrumProperties();
		jStatusBar_Main.setText(mDataManager.getSelectedCount() + " spectra selected");
	}

	private void displaySpectrumProperties() {
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				final SpectrumPropertiesTableModel tm = new SpectrumPropertiesTableModel(getSelectedSpectra());
				jTable_SpecProperties.setModel(tm);
				String name = "Composition";
				Composition comp = tm.getProperties()
						.getCompositionWithDefault(SpectrumProperties.MicroanalyticalComposition, null);
				if (comp != null)
					name = SpectrumProperties.MicroanalyticalComposition.toString();
				else {
					comp = tm.getProperties().getCompositionWithDefault(SpectrumProperties.StandardComposition, null);
					if (comp != null)
						name = SpectrumProperties.StandardComposition.toString();
				}
				updateCompositionTable(name, comp);
			}
		};
		invokeCarefully(r);
	}

	private void updateCompositionTable(String label, Composition comp) {
		jBorder_Composition.setTitle(label);
		jTable_SpecComposition.setModel(new CompositionTableModel(comp, false));
	}

	private void updateParticleSignature(ParticleSignature ps) {
		jBorder_Composition.setTitle("Particle Signature");
		jTable_SpecComposition.setModel(new ParticleSignatureTableModel(ps));
	}

	void jButtonClear_actionPerformed(ActionEvent e) {
		final HTMLList list = new HTMLList();
		list.setHeader("Clearing spectra from memory...");
		for (final ISpectrumData sd : getSelectedSpectra()) {
			list.add("<i>" + sd.toString() + "</i> removed.");
			mDataManager.removeSpectrum(sd);
		}
		if (list.size() > 0)
			appendHTML(list.toString());
	}

	/**
	 * File -&gt; Save As menu item
	 */
	public void fileSaveAs() {
		final HTMLList list = new HTMLList();
		list.setHeader("Saving spectra");
		int errCx = 0;
		for (final ISpectrumData sd : getSelectedSpectra()) {
			final String fn = normalizeFilename(sd.toString());
			final File f = selectFileDestination(fn.substring(0, Math.min(210, fn.length())), this);
			if (f != null)
				try {
					if (f.getName().endsWith(".csv")) {
						exportSpectrumAsCSV(sd, f.getCanonicalPath());
						list.add("<i>" + sd.toString() + "</i> written to <i>" + f.toString()
								+ "</i> as a comma-separated values formatted file.");
					} else if (f.getName().endsWith(".tia.msa")) {
						exportSpectrumAsTiaEMSA(sd, f.getCanonicalPath());
						list.add("<i>" + sd.toString() + "</i> written to <i>" + f.toString()
								+ "</i> as a TIA compatible EMSA/MSA 1.0 formatted file.");
					} else if (f.getName().endsWith(".tif")) {
						exportSpectrumAsTIFF(sd, f.getCanonicalPath());
						list.add("<i>" + sd.toString() + "</i> written to <i>" + f.toString()
								+ "</i> as a TIFF spectrum file.");
					} else {
						exportSpectrumAsEMSA(sd, f.getCanonicalPath());
						list.add("<i>" + sd.toString() + "</i> written to <i>" + f.toString()
								+ "</i> as a EMSA/MSA 1.0 formatted file.");
					}
				} catch (final Exception ex) {
					++errCx;
					list.addError("<i>" + sd.toString() + "</i> not written to <i>" + f.toString() + "</i>");
					list.setError("One or more spectra not written successfully.");
				}
		}
		if (list.size() > 0) {
			appendHTML(list.toString());
			if (errCx > 0)
				ErrorDialog
						.createErrorMessage(
								this, "File Save Errors", Integer.toString(errCx)
										+ (errCx == 1 ? " spectrum" : " spectra") + " not written successfully.",
								"<html>" + list.toString());
		}
	}

	public void fileSaveSelected() {
		final HTMLList list = new HTMLList();
		list.setHeader("Saving spectra");
		int errCx = 0;
		List<ISpectrumData> selected = getSelectedSpectra();
		File dir = selectOutputPath();
		if (dir != null) {
			for (final ISpectrumData sd : selected) {
				final String fn = normalizeFilename(sd.toString());
				File f = new File(dir, fn.substring(0, Math.min(210, fn.length())));
				if (f != null)
					try {
						exportSpectrumAsEMSA(sd, f.getCanonicalPath());
						list.add("<i>" + sd.toString() + "</i> written to <i>" + f.toString()
								+ "</i> as a EMSA/MSA 1.0 formatted file.");
					} catch (final Exception ex) {
						++errCx;
						list.addError("<i>" + sd.toString() + "</i> not written to <i>" + f.toString() + "</i>");
						list.setError("One or more spectra not written successfully.");
					}
			}
			if (list.size() > 0)

			{
				appendHTML(list.toString());
				if (errCx > 0)
					ErrorDialog
							.createErrorMessage(
									this, "File Save Errors", Integer.toString(errCx)
											+ (errCx == 1 ? " spectrum" : " spectra") + " not written successfully.",
									"<html>" + list.toString());
			}
		}
	}

	/**
	 * Save a single spectrum to an EMSA file.
	 * 
	 * @param sd
	 * @return true on success, false otherwise.
	 */
	public boolean saveStandardAsEMSA(ISpectrumData sd) {
		final String fn = normalizeFilename(sd.toString());
		final String dir = DTSA2.getSpectrumDirectory();
		final JFileChooser jfc = new JFileChooser(dir);
		final SimpleFileFilter emsa = new SimpleFileFilter(new String[] { "msa" }, "EMSA/MAS Standard File");
		jfc.addChoosableFileFilter(emsa);
		jfc.setFileFilter(emsa);
		jfc.setSelectedFile(new File(dir, fn));
		jfc.setDialogTitle("Save " + fn + " as...");
		final int option = jfc.showSaveDialog(MainFrame.this);
		if (option == JFileChooser.APPROVE_OPTION) {
			File res = jfc.getSelectedFile();
			if (res != null) {
				if (jfc.getFileFilter() instanceof SimpleFileFilter)
					res = ((SimpleFileFilter) jfc.getFileFilter()).forceExtension(res);
				DTSA2.updateSpectrumDirectory(res.getParentFile());
				try {
					final String path = res.getCanonicalPath();
					exportSpectrumAsEMSA(sd, path);
					sd.getProperties().setTextProperty(SpectrumProperties.SourceFile, path);
					return true;
				} catch (final Exception ex) {
					ErrorDialog.createErrorMessage(MainFrame.this, "Error saving spectrum", ex);
				}
			}
		}
		return false;
	}

	/**
	 * Replaces common forbidden characters with alternatives to ensure that
	 * filenames are acceptable to the file system.
	 *
	 * @param fn
	 * @return String
	 */
	private static String normalizeFilename(String fn) {
		return fn.replace(":", "").replace('\\', '-').replace('/', '-');
	}

	public File saveStandard(Element elm, StandardBundle bundle, File res) {
		if( res.exists() && !res.isDirectory()) {
			Date mod = new Date(res.lastModified());
			res.renameTo(new File(replaceExtension(res.getAbsolutePath(), " - "+formatDate(mod)+".zstd")));
		}
		try {
			bundle.write(res);
			return res;
		} catch (final Exception ex) {
			ErrorDialog.createErrorMessage(MainFrame.this, "Error saving spectrum", ex);
		}
		return null;
	}

	public List<File> saveStandards(Map<Element, StandardBundle> bundles) {
		final String dir = DTSA2.getSpectrumDirectory();
		final JFileChooser jfc = new JFileChooser(dir);
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jfc.setDialogTitle("Save standards to...");
		jfc.setSelectedFile(new File(dir));
		final int option = jfc.showSaveDialog(MainFrame.this);
		ArrayList<File> res = new ArrayList<>();
		if (option == JFileChooser.APPROVE_OPTION) {
			File seldir = jfc.getSelectedFile();
			DTSA2.updateSpectrumDirectory(seldir);
			for (Map.Entry<Element, StandardBundle> me : bundles.entrySet()) {
				ISpectrumData sd = me.getValue().getStandard();
				final String fn = normalizeFilename(me.getKey().toAbbrev() + " std - " + sd.toString().replace(" std", "")+".zstd");
				File f = saveStandard(me.getKey(), me.getValue(), new File(seldir, fn));
				if (f != null)
					res.add(f);
			}
		}
		return res;
	}

	/**
	 * Process-&gt;Duplicate menu item
	 */
	public void processDuplicate() {
		final HTMLList list = new HTMLList();
		list.setHeader("Duplicating spectra");
		for (final ISpectrumData sd : getSelectedSpectra()) {
			final ISpectrumData newSpec = SpectrumUtils.copy(sd);
			addSpectrum(newSpec, true);
			list.add("Spectrum <i>" + sd + "</i> duplicated as <i>" + newSpec + "</i>");
		}
		if (list.size() > 0)
			appendHTML(list.toString());
	}

	public void centerDialog(Dialog d) {
		d.setLocationRelativeTo(this);
	}

	/**
	 * Process-&gt;SubSample menu item
	 */
	public void processSubSample() {
		if (mDataManager.getSelectedCount() > 0) {
			final NoisyDialog nd = new NoisyDialog(this, "Sub-sample settings", true);
			nd.setLocationRelativeTo(this);
			nd.setVisible(true);
			if (nd.getOk()) {
				final Random r = new Random(nd.getSeed());
				final HTMLList list = new HTMLList();
				list.setHeader("Sub-sampling spectra");
				for (final ISpectrumData sd : getSelectedSpectra())
					for (int j = nd.getDuplicates() - 1; j >= 0; --j) {
						final double liveTime = sd.getProperties().getNumericWithDefault(SpectrumProperties.LiveTime,
								Double.NaN);
						if (!Double.isNaN(liveTime)) {
							ISpectrumData newSpec;
							try {
								newSpec = SpectrumUtils.subSampleSpectrum(sd, liveTime * nd.getScale(), r.nextLong());
								addSpectrum(newSpec, true, sd);
								list.add("The spectrum <i>" + sd + "</i> was sub-sampled into " + newSpec.toString());
							} catch (final EPQException e) {
								list.addError("The spectrum <i>" + sd + "</i> was <b>not</b> sub-sampled: "
										+ e.getLocalizedMessage());
								list.setError("One or more spectra was not sub-sampled correctly");
							}
						} else {
							list.addError("The spectrum <i>" + sd
									+ "</i> was <b>not</b> sub-sampled.  The live time was unavailable.");
							list.setError("One or more spectra was not sub-sampled correctly");
						}
					}
				if (list.size() > 0)
					appendHTML(list.toString());
			}
		}
	}
	
	
	public static String formatDate(Date fn) {
		DateFormat df =new SimpleDateFormat("yyyyMMdd HHmmss z");
		return df.format(fn);
	}

	/**
	 * Replaces or appends a new extension onto the specified file name.
	 * 
	 * @param name   The file name
	 * @param newExt The new extension starting with a '.' (ie. ".msa")
	 * @return The full filename with extension
	 */
	public static String replaceExtension(String name, String newExt) {
		String res;
		final int p = name.lastIndexOf('.');
		if (p != -1)
			res = name.substring(0, p) + newExt;
		else
			res = name + newExt;
		return res;
	}

	public static String exportSpectrumAsCSV(ISpectrumData sd, String filename) throws Exception {
		if (!filename.toUpperCase().endsWith(".CSV"))
			filename = filename + ".csv";
		File file = new File(filename);
		if(file.exists() && !file.isDirectory()) {
			Date mod = new Date(file.lastModified());
			file.renameTo(new File(replaceExtension(filename, " - "+formatDate(mod)+".csv")));
		}
		try (final FileOutputStream os = new FileOutputStream(file)) {
			WriteSpectrumAsCSV.write(sd, os);
		}
		return file.getName();
	}

	public static String exportSpectrumAsEMSA(ISpectrumData sd, String filename) throws Exception {
		final String fu = filename.toUpperCase();
		if (!(fu.endsWith(".MSA") || fu.endsWith(".EMSA") || fu.endsWith(".TXT")))
			filename = filename + ".msa";
		File file = new File(filename);
		if(file.exists() && !file.isDirectory()) {
			Date mod = new Date(file.lastModified());
			file.renameTo(new File(replaceExtension(filename, " - "+formatDate(mod)+".msa")));
		}
		try (final FileOutputStream os = new FileOutputStream(file)) {
			WriteSpectrumAsEMSA1_0.write(sd, os, WriteSpectrumAsEMSA1_0.Mode.COMPATIBLE);
		}
		return file.getName();
	}

	public static String exportSpectrumAsTIFF(ISpectrumData sd, String filename) throws Exception {
		final String fu = filename.toUpperCase();
		if (!(fu.endsWith(".TIFF") || fu.endsWith(".TIF")))
			filename = filename + ".tif";
		File file = new File(filename);
		if(file.exists() && !file.isDirectory()) {
			Date mod = new Date(file.lastModified());
			file.renameTo(new File(replaceExtension(filename, " - "+formatDate(mod)+".tif")));
		}
		try (final FileOutputStream os = new FileOutputStream(file)) {
			WriteSpectrumAsTIFF.write(sd, os);
		}
		return file.getName();
	}

	public static String exportSpectrumAsTiaEMSA(ISpectrumData sd, String filename) throws Exception {
		final String fu = filename.toUpperCase();
		if (!(fu.endsWith(".TIA.MSA") || fu.endsWith(".TIA.EMSA") || fu.endsWith(".TIA.TXT")))
			filename = filename + ".tia.msa";
		File file = new File(filename);
		if(file.exists() && !file.isDirectory()) {
			Date mod = new Date(file.lastModified());
			file.renameTo(new File(replaceExtension(filename, " - "+formatDate(mod)+".msa")));
		}
		try (final FileOutputStream os = new FileOutputStream(file)) {
			WriteSpectrumAsEMSA1_0.write(sd, os, WriteSpectrumAsEMSA1_0.Mode.FOR_TIA);
		}
		return file.getName();
	}

	/**
	 * File-&gt;BatchExportCSV menu item
	 */
	public void fileBatchExportCSV() {
		final JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Specify a directory into which to export the spectra.");
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		final int choice = fc.showOpenDialog(this);
		if (choice == JFileChooser.APPROVE_OPTION) {
			final File path = fc.getSelectedFile();
			final HTMLList list = new HTMLList();
			list.setHeader("Exporting spectra to <i>" + path.getAbsolutePath() + "</i> as CSV");
			for (final ISpectrumData spec : getSelectedSpectra())
				try {
					File file = new File(path, spec.toString());
					if(file.exists() && !file.isDirectory()) {
						Date mod = new Date(file.lastModified());
						file.renameTo(new File(replaceExtension(file.getAbsolutePath(), " - "+formatDate(mod)+".csv")));
					}
					final String fn = exportSpectrumAsCSV(spec, file.getAbsolutePath());
					if (fn != null)
						list.add(
								"The spectrum <i>" + spec.toString() + "</i> was exported as CSV to <i>" + fn + "</i>");
					else {
						list.addError("Unable to export <i>" + spec.toString() + "/i as a CSV file.");
						list.setError("One or more files failed to export correctly");
					}
				} catch (final Exception ex) {
					// Shouldn't happen
				}
			if (list.size() > 0)
				appendHTML(list.toString());
		}
	}

	/**
	 * File-&gt;Batch Export EMSA menu item
	 */
	public void fileBatchExportEMSA() {

		final JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Specify a directory into which to export the spectra.");
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		final int choice = fc.showOpenDialog(this);
		if (choice == JFileChooser.APPROVE_OPTION) {
			final HTMLList list = new HTMLList();
			final File path = fc.getSelectedFile();
			list.setHeader("Exporting spectra to <i>" + path.getAbsolutePath() + "</i> as EMSA 1.0");
			int i = 1;
			for (final ISpectrumData spec : getSelectedSpectra())
				try {
					try {
						final String name = TextUtilities.normalizeFilename(spec.toString() + "[" + i + "]");
						File file = new File(path, name);
						if(file.exists() && !file.isDirectory()) {
							Date mod = new Date(file.lastModified());
							String newname = replaceExtension(file.getAbsolutePath(), " - "+formatDate(mod)+".msa");
							file.renameTo(new File(newname));
							list.add("Renaming existing file to <i>"+newname+"</i>.");
						}
						final String fn = exportSpectrumAsEMSA(spec, file.getAbsolutePath());
						list.add("The spectrum <i>" + spec.toString() + "</i> was exported as EMSA to <i>" + fn
								+ "</i>");
					} catch (final Exception ex) {
						final String name = "Batch[" + i + "]";
						File ff = new File(path, name);
						final String fn = exportSpectrumAsEMSA(spec, ff.getAbsolutePath());
						list.add("The spectrum <i>" + spec.toString() + "</i> was exported as EMSA to <i>" + fn
								+ "</i>");
					}
				} catch (final Exception ex) {
					list.addError("Unable to export <i>" + spec.toString() + "/i as an EMSA file.");
					list.setError("One or more files failed to export correctly");
				}
			if (list.size() > 0) {
				appendHTML(list.toString());
				ErrorDialog.createErrorMessage(MainFrame.this, "Batch export",
						"There was one or more errors batch exporting spectra.", list.toString());
			}
		}
	}

	private List<ISpectrumData> getSelectedSpectra() {
		return new ArrayList<ISpectrumData>(mDataManager.getSelected());
	}

	public void processStripBackground() {
		final HTMLList list = new HTMLList();
		list.setHeader("Stripping background from spectra");
		for (final ISpectrumData sd : getSelectedSpectra()) {
			final ISpectrumData res = PeakStripping.Clayton1987
					.getStrippedSpectrum(SpectrumUtils.applyZeroPeakDiscriminator(sd));
			list.add("Stripping <i>" + sd.toString() + "</i> to create <i>" + res.toString() + "</i>");
			addSpectrum(res, true, sd);
		}
		if (list.size() > 0)
			appendHTML(list.toString());
	}

	public void processFitBackground() {
		final BremsstrahlungAnalytic ba = new BremsstrahlungAnalytic.Lifshin1974Model();
		final HTMLList list = new HTMLList();
		list.setHeader("Bremsstrahlung fit");
		for (final ISpectrumData sd : getSelectedSpectra())
			try {
				final ArrayList<String> missing = new ArrayList<String>();
				final SpectrumProperties sp = sd.getProperties();
				if (!sp.isDefined(SpectrumProperties.BeamEnergy))
					missing.add("beam energy");
				final double e0 = ToSI.eV(SpectrumUtils.getBeamEnergy(sd));
				final double toa = SpectrumUtils.getTakeOffAngle(sp);
				if (Double.isNaN(toa))
					missing.add("take-off angle");
				final Object obj = sp.getObjectWithDefault(SpectrumProperties.Detector, null);
				EDSDetector det = null;
				if (obj instanceof EDSDetector)
					det = (EDSDetector) obj;
				else
					missing.add("a detector");
				if (missing.size() > 0) {
					final StringBuffer sb = new StringBuffer();
					sb.append("Please provide the following pieces of information for ");
					sb.append(sd.toString());
					sb.append(":\n   ");
					for (final String str : missing) {
						sb.append(str);
						sb.append(", ");
					}
					sb.delete(sb.length() - 2, sb.length());
					JOptionPane.showMessageDialog(this, sb.toString(), "Background fit", JOptionPane.WARNING_MESSAGE);
					break;
				}
				Composition comp = sp.getCompositionWithDefault(SpectrumProperties.StandardComposition, null);
				if (comp == null)
					comp = sp.getCompositionWithDefault(SpectrumProperties.MicroanalyticalComposition, null);
				if (comp == null) {
					comp = createMaterial();
					sp.setCompositionProperty(SpectrumProperties.StandardComposition, comp);
				}
				if (comp != null) {
					ba.initialize(comp, e0, toa);
					final SpecDisplay.Regions r = jSpecDisplay_Main.getRegions();
					final ArrayList<int[]> rois = new ArrayList<int[]>();
					for (int i = r.size() - 1; i >= 0; --i) {
						final SpecDisplay.Region rr = r.get(i);
						rois.add(new int[] { SpectrumUtils.channelForEnergy(sd, rr.getLowEnergy()),
								SpectrumUtils.channelForEnergy(sd, rr.getHighEnergy()) });
					}
					if (rois.size() > 0) {
						final ISpectrumData bkg = ba.fitBackground(det, sd, rois);
						bkg.getProperties().setTextProperty(SpectrumProperties.SpectrumDisplayName,
								"Brem[" + sd.toString() + ", manual rois]");
						mDataManager.addSpectrum(bkg);
					} else {
						final ISpectrumData bkg = ba.fitBackground2(det, sd, comp);
						bkg.getProperties().setTextProperty(SpectrumProperties.SpectrumDisplayName,
								"Brem[" + sd.toString() + ", auto rois]");
						mDataManager.addSpectrum(bkg);
					}
				}
			} catch (final EPQException ex) {
				list.addError("Failure fitting <i>" + TextUtilities.normalizeHTML(sd.toString()) + "</i>"
						+ TextUtilities.normalizeHTML(ex.getMessage()));
				list.setError("One or more errors occured during the fit process.");
			}

	}

	private void addSpectrum(ISpectrumData spec, boolean selected) {
		addSpectrum(spec, selected, null);
	}

	private void addSpectrum(ISpectrumData spec, boolean selected, ISpectrumData assoc) {
		class AddSpec implements Runnable {
			ISpectrumData mSpectrum;
			ISpectrumData mAssociated;

			boolean mSelected;

			AddSpec(ISpectrumData spec, boolean selected, ISpectrumData assoc) {
				mSpectrum = spec;
				mSelected = selected;
				mAssociated = assoc;
			}

			@Override
			public void run() {
				mDataManager.addSpectrum(mSpectrum, mSelected, mAssociated);
			}
		}
		invokeCarefully(new AddSpec(spec, selected, assoc));
	}

	public void processLinearizeEnergyAxis() {
		final List<ISpectrumData> selected = getSelectedSpectra();
		if (!selected.isEmpty()) {
			final RescaleDialog rd = new RescaleDialog(this, "Linearize energy axis", true);
			rd.setLocationRelativeTo(this);
			rd.setChannelWidth(selected.iterator().next().getChannelWidth());
			rd.setVisible(true);
			if (rd.getOk())
				for (final ISpectrumData sd : selected)
					addSpectrum(SpectrumUtils.linearizeSpectrum(sd, rd.getPolynomial(), rd.getChannelWidth()), true,
							sd);
		}
	}

	public void processSmooth() {
		final HTMLList list = new HTMLList();
		list.setHeader("Smoothing spectra");
		for (final ISpectrumData sd : getSelectedSpectra()) {
			final ISpectrumData spec = SpectrumSmoothing.SavitzkyGolay5.compute(sd);
			addSpectrum(spec, true, sd);
			list.add("<i>" + TextUtilities.normalizeHTML(sd.toString()) + "</i> smoothed to <i>"
					+ TextUtilities.normalizeHTML(spec.toString()) + "</i>");
		}
		if (list.size() > 0)
			appendHTML(list.toString());
	}

	public void processTrimSpectra() {
		final HTMLList list = new HTMLList();
		list.setHeader("Trim spectra");
		for (final ISpectrumData sd : getSelectedSpectra()) {
			addSpectrum(jSpecDisplay_Main.trimSpectrum(sd), true, sd);
			list.add("<i>" + TextUtilities.normalizeHTML(sd.toString()) + "</i> trimmed.");
		}
		if (list.size() > 0)
			appendHTML(list.toString());
	}

	public void processPeakSearch() {
		final HTMLList list = new HTMLList();
		list.setHeader("Peak search spectra");
		for (final ISpectrumData sd : getSelectedSpectra()) {
			final ISpectrumData peaks = PeakROISearch.GaussianSearch.computeAsSpectrum(sd, 3.0);
			peaks.getProperties().setTextProperty(SpectrumProperties.SpectrumDisplayName,
					"Peaks[" + sd.toString() + "]");
			list.add("Peaks in <i>" + TextUtilities.normalizeHTML(sd.toString()) + "</i> recorded in <i>"
					+ TextUtilities.normalizeHTML(peaks.toString()) + "</i>");
			addSpectrum(peaks, true, sd);
		}
		if (list.size() > 0)
			appendHTML(list.toString());
	}

	public File getScriptFile() {
		class Chooser implements Runnable {
			File mResult = null;

			@Override
			public void run() {
				final JFileChooser fc = new JFileChooser();
				final FileNameExtensionFilter filter = new FileNameExtensionFilter("Python scripts", "py", "jy");
				fc.setAcceptAllFileFilterUsed(true);
				fc.setFileFilter(filter);
				final Preferences userPref = Preferences.userNodeForPackage(MainFrame.class);
				final String path = userPref.get("ScriptPath", null);
				if (path != null) {
					final File f = new File(path);
					if (f.exists())
						fc.setCurrentDirectory(f);
				}
				mResult = null;
				if (fc.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
					mResult = fc.getSelectedFile();
					mPreviousScript = fc.getSelectedFile();
					userPref.put("ScriptPath", mPreviousScript.getParent());
				}
			}
		}
		final Chooser ch = new Chooser();
		invokeCarefully(ch);
		return ch.mResult;
	}

	/**
	 * Displays a file chooser to permit the user to select a python script to
	 * execute
	 */
	public void openPythonScript() {
		final File f = getScriptFile();
		if (f != null) {
			String path = f.getAbsolutePath();
			mPyDisabled = true;
			try {
				mRecentPyModel.add(path);
			} finally {
				mPyDisabled = false;
			}
			if (File.separatorChar == '\\')
				path = path.replace('\\', '/');
			jCommandLine_Main.run(f);
		}
	}

	private void runRecentPy() {
		if (!mPyDisabled) {
			final RecentFile rf = (RecentFile) jComboBox_PrevPy.getSelectedItem();
			if ((rf != null) && rf.exists()) {
				String path = rf.getAbsolutePath();
				if (File.separatorChar == '\\')
					path = path.replace('\\', '/');
				jCommandLine_Main.run(rf);
			}
		}
	}

	public void runPythonScript(File f) {
		try {
			jCommandLine_Main.run(f);
		} catch (final Throwable e) {
			jCommandLine_Main.writeError(e.toString());
		}
		mPreviousScript = f;
	}

	public Writer getStandardOutput() {
		return jCommandLine_Main.getStandardOutput();
	}

	public Writer getStandardError() {
		return jCommandLine_Main.getStandardError();
	}

	public void runPythonScript(String filename) {
		final File f = new File(filename);
		if (f.canRead())
			runPythonScript(f);
		else
			jCommandLine_Main.writeError("ERROR: " + filename + " can not be read.");
	}

	/**
	 * Opens a MaterialsCreator dialog in the Event Dispatch thread regardless of
	 * which thread it was opened in.
	 * 
	 * @return Composition
	 */
	public Composition editMaterial(Composition comp) {
		return MaterialsCreator.editMaterial(MainFrame.this, comp, DTSA2.getSession(), false);
	}

	/**
	 * Opens a MaterialsCreator dialog in the Event Dispatch thread regardless of
	 * which thread it was opened in.
	 * 
	 * @return Composition
	 */
	public Composition createMaterial() {
		// MaterialsCreator is already thread savvy
		return MaterialsCreator.createMaterial(MainFrame.this, DTSA2.getSession(), false);
	}

	/**
	 * Add a menu item to the end of the Tools menu.
	 * 
	 * @param mi
	 */
	public void addToolsMenuItem(JMenuItem mi) {
		jMenu_Tools.add(mi);
	}

	public void addReportMenuItem(JMenuItem mi) {
		jMenu_Report.add(mi);
	}

	/*
	 * public void addPlugInMenuItem(JMenuItem mi) { jMenu_PlugIn.add(mi); }
	 */

	/**
	 * Add a menu item to the end of the Process menu.
	 * 
	 * @param mi
	 */
	public void addProcessMenuItem(JMenuItem mi) {
		jMenu_Process.add(mi);
	}

	/**
	 * Executes the Simulation Alien in the Event Dispatch thread regardless of
	 * which thread it was called from.
	 */
	public void toolsSimulationAlien() {
		final Runnable th = new Runnable() {
			@Override
			public void run() {
				final SimulationWizard sw = new SimulationWizard(MainFrame.this);
				centerDialog(sw);
				sw.setVisible(true);
				final ISpectrumData[] res = sw.getResults();
				if (res != null)
					for (final ISpectrumData spec : res)
						addSpectrum(spec, true);
				appendHTML(sw.asHTML());
			}
		};
		invokeCarefully(th);
	}

	/**
	 * Executes the Calibration Alien in the EventDispatch thread.
	 */
	public void toolsCalibrationAlien() {
		final Runnable th = new Runnable() {
			@Override
			public void run() {
				final CalibrationWizard cw = new CalibrationWizard(MainFrame.this);
				final Object obj = MainFrame.this.jComboBox_Detector.getSelectedItem();
				assert (obj instanceof DetectorProperties);
				cw.setDetector((DetectorProperties) obj);
				centerDialog(cw);
				cw.setVisible(true);
				if (cw.isFinished()) {
					appendHTML(cw.toHTML());
					initDetectors();
				}
			}
		};
		final Object obj = MainFrame.this.jComboBox_Detector.getSelectedItem();
		if (obj instanceof DetectorProperties)
			invokeCarefully(th);
		else {
			final String msg = "Please specify which detector to calibrate using the \"Default Detector\" drop list box.";
			ErrorDialog.createErrorMessage(MainFrame.this, "Calibration Alien", msg, msg);
		}
	}

	public void toolsOptimizeAlien() {
		final Runnable th = new Runnable() {
			@Override
			public void run() {
				final Object obj = MainFrame.this.jComboBox_Detector.getSelectedItem();
				if (obj instanceof DetectorProperties) {
					final DetectorProperties dp = (DetectorProperties) obj;
					final DetectorCalibration dc = DTSA2.getSession().getMostRecentCalibration(dp);
					final EDSDetector det = EDSDetector.createDetector((DetectorProperties) obj, (EDSCalibration) dc);
					final OptimizationWizard eow = new OptimizationWizard(MainFrame.this, det, DTSA2.getSession());
					centerDialog(eow);
					eow.setVisible(true);
					if (eow.isFinished()) {
						appendHTML(eow.toHTML(DTSA2.getReport().getFile().getParentFile()));
						for (final ISpectrumData spec : eow.simulateSpectra())
							MainFrame.this.addSpectrum(spec, true);
					}
				}
			}
		};
		invokeCarefully(th);
	}

	public void toolsQCAlien() {
		final Runnable th = new Runnable() {
			@Override
			public void run() {
				final QCWizard cw = new QCWizard(MainFrame.this, DTSA2.getSession());
				final Object obj = MainFrame.this.jComboBox_Detector.getSelectedItem();
				assert (obj instanceof DetectorProperties);
				cw.setDetector((DetectorProperties) obj);
				centerDialog(cw);
				cw.setVisible(true);
				if (cw.isFinished())
					appendHTML(cw.getHTMLResults());
			}
		};
		final Object obj = MainFrame.this.jComboBox_Detector.getSelectedItem();
		if (obj instanceof DetectorProperties)
			invokeCarefully(th);
		else {
			final String msg = "Please specify which detector to calibrate using the \"Default Detector\" drop list box.";
			ErrorDialog.createErrorMessage(MainFrame.this, "Calibration Alien", msg, msg);
		}
	}

	public void toolsEditMaterial() {
		Composition mat = null;
		final List<ISpectrumData> specs = getSelectedSpectra();
		if (specs.size() > 0) {
			{
				final ISpectrumData sd = specs.iterator().next();
				mat = sd.getProperties().getCompositionWithDefault(SpectrumProperties.StandardComposition, null);
			}
			for (final ISpectrumData sd : specs) {
				final Composition c = sd.getProperties()
						.getCompositionWithDefault(SpectrumProperties.StandardComposition, null);
				if ((c == null) || (!c.equals(mat))) {
					mat = null;
					break;
				}
			}
		}
		mat = MaterialsCreator.editMaterial(this, mat, DTSA2.getSession(), "Assign material", false);
		if ((mat != null) && (mat.getElementCount() > 0)) {
			final HTMLList list = new HTMLList();
			StringBuffer sb = new StringBuffer();
			sb.append("<h2>Assign material: " + mat.toString() + "</h2>\n");
			sb.append(mat.toHTMLTable());
			for (final ISpectrumData sd : getSelectedSpectra()) {
				sd.getProperties().setCompositionProperty(SpectrumProperties.StandardComposition, mat);
				list.add("Standard composition[" + sd.toString() + "] = " + mat.toString());
			}
			if (list.size() > 0)
				sb.append(list.toString());
			appendHTML(sb.toString());
			mDataManager.notifyUpdated();
		}
	}

	public void reportAnnotation() {
		final String res = AnnotationDialog.getAnnotation(this);
		if (res != null) {
			final StringBuffer sb = new StringBuffer();
			sb.append("<p><font color=\"#C0C0C0\">");
			sb.append(res);
			sb.append("</font></p>");
			appendHTML(sb.toString());
		}
	}

	public void reportSpectra() {
		try {
			final File outfile = File.createTempFile("spectra", ".png", DTSA2.getReport().getFile().getParentFile());
			final Dimension dim = jSpecDisplay_Main.saveAs(outfile, "png");
			final StringBuffer sb = new StringBuffer();
			sb.append("<br><h2>Spectrum Display</h2>");
			final double scale = jSpecDisplay_Main.getWidth() / dim.getWidth();
			sb.append("<img width=\"" + Integer.toString((int) Math.round(dim.getWidth() * scale)) + "\"");
			sb.append(" height=\"" + Integer.toString((int) Math.round(dim.getHeight() * scale)) + "\"");
			sb.append(" src=\"");
			sb.append(outfile.toURI().toURL().toExternalForm());
			sb.append("\" alt=\"Spectrum Display\" /></p>");
			appendHTML(sb.toString());
		} catch (final Exception e) {
			ErrorDialog.createErrorMessage(this, "Error adding spectra to report", e);
		}
	}

	private void recursivelyEnable(Container c, boolean enable) {
		for (final Component cc : c.getComponents()) {
			if (cc instanceof Container)
				recursivelyEnable((Container) cc, enable);
			cc.setEnabled(enable);
		}
	}

	public void setEnableUI(boolean enable) {
		recursivelyEnable(jMenuBar_Main, enable);
		jPopupMenu_SpectrumList.setEnabled(enable);
		jButton_OpenPy.setEnabled(enable);
		jButton_Stop.setEnabled(!enable);
		jComboBox_PrevPy.setEnabled(enable);
		jButton_Play.setEnabled(enable && (jComboBox_PrevPy.getSelectedItem() instanceof RecentFile));

	}

	public void setSpectrumText(String str) {
		jSpecDisplay_Main.setTextAnnotation(str);
	}

	public SpecDisplay getSpectrumDisplay() {
		return jSpecDisplay_Main;
	}

	/**
	 * Adds a tab containing the component to the utility panel with the specified
	 * title and toolTip.
	 * 
	 * @param title
	 * @param component
	 * @param toolTip
	 */
	public void addUtilityTab(String title, Component component, String toolTip) {
		jTabbedPane_Utility.addTab(title, null, component, toolTip);
	}

	/**
	 * Removes the last user added tab with the specified title.
	 * 
	 * @param title
	 */
	public void removeUtilityTab(String title) {
		for (int i = jTabbedPane_Utility.getTabCount() - 1; i >= 3; --i)
			if (jTabbedPane_Utility.getTitleAt(i).equals(title)) {
				jTabbedPane_Utility.remove(i);
				return;
			}
	}

	/**
	 * Bring the last tab with the specified title to the forefront.
	 * 
	 * @param title
	 */
	public void selectUtilityTab(String title) {
		for (int i = jTabbedPane_Utility.getTabCount() - 1; i >= 0; --i)
			if (jTabbedPane_Utility.getTitleAt(i).equals(title)) {
				jTabbedPane_Utility.setSelectedIndex(i);
				return;
			}
	}

	public static void setMaxSpectra(int n) {
		MAX_DISPLAYED_SPECTRA = n;
	}
}
