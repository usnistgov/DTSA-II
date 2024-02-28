package gov.nist.microanalysis.dtsa2;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.HeadlessException;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.text.rtf.RTFEditorKit;

import org.python.core.PyObject;
import org.python.util.InteractiveConsole;

import gov.nist.microanalysis.EPQLibrary.ISpectrumData;
import gov.nist.microanalysis.EPQTools.SimpleFileFilter;
import gov.nist.microanalysis.dtsa2.DTSA2.OS;

/**
 * <p>
 * A GUI based command line editor designed to provide a user mechanism to edit
 * commands to and results from Jython.
 * </p>
 * <p>
 * Copyright: Copyright Nicholas W. M. Ritchie (c) 2004
 * </p>
 * <p>
 * Company: Duck-and-Cover
 * </p>
 *
 * @author Nicholas W. M. Ritchie
 * @version 1.0
 */
public class JCommandLine extends JTextPane {
   private static final long serialVersionUID = 0x1DEF423;
   private int mCmdOffset = Integer.MAX_VALUE;
   private final JythonWorker mJythonWorker;

   private int mLastChar = 10;
   private String mCurrentCommand = null;
   private final CommandBuffer mCmdBuffer;
   private int mCmdIndex = 1;
   private int mNextTemp = 1;
   private final boolean mMoreInput = false;
   private final Style mErrorStyle;
   private final Style mOutputStyle;
   private final Style mStatusStyle;
   private Writer mArchivalWriter;
   private final JPopupMenu jPopupMenu_Main;

   static private final String COMMAND = "Command";
   static private final String PROMPT = "Prompt";
   static private final String INNER_ERROR = "InternalError";
   static private final String RESET_CMD = "#-#-#-RESET-#-#-";
   static private final String NO_TIME = "#-#-#-NO_TIME-#-#-";

   /**
    * Specify a writer to use to save an archival version of the information
    * output to this pane.
    *
    * @param wr
    */
   public void setArchivalWriter(Writer wr) {
      mArchivalWriter = wr;
   }

   public Writer getArchivalWriter() {
      return mArchivalWriter;
   }

   private void writeException(String str) {
      final Document doc = getDocument();
      try {
         doc.insertString(doc.getLength(), str, getStyle(INNER_ERROR));
      } catch (final BadLocationException ex) {
      }
   }

   private class StyledWriter extends Writer {

      private final Style mStyle;
      private boolean mClosed = false;

      private StyledWriter(Style st) {
         mStyle = st;
      }

      @Override
      public void close() throws IOException {
         mClosed = true;
      }

      @Override
      public void flush() throws IOException {
      }

      @Override
      public void write(char[] cbuf, int off, int len) throws IOException {
         if (!mClosed)
            JCommandLine.this.write(mStyle, new String(cbuf, off, len));
      }
   }

   public Writer getStandardOutput() {
      return new StyledWriter(mOutputStyle);
   }

   public Writer getStandardError() {
      return new StyledWriter(mErrorStyle);
   }

   /**
    * @param item
    * @param mBuffer
    */
   private void write(Style style, String mBuffer) {
      try {
         final Document doc = getDocument();
         doc.insertString(doc.getLength(), mBuffer, style);
         mLastChar = mBuffer.charAt(mBuffer.length() - 1);
      } catch (final BadLocationException ex) {
         // Ignore it...
      }
   }

   public void writeError(String str) {
      write(mErrorStyle, str);
   }

   public void writeStatus(String str) {
      if (mLastChar != 10)
         write(mStatusStyle, "\n");
      write(mStatusStyle, str);
   }

   public void writeOutput(String str) {
      write(mOutputStyle, str);
   }

   public void writeCommand(String str) {
      final Document doc = getDocument();
      try {
         doc.remove(mCmdOffset, doc.getLength() - mCmdOffset);
         doc.insertString(mCmdOffset, str, getStyle(COMMAND));
         doc.insertString(mCmdOffset, "\n", getStyle(COMMAND));
      } catch (final BadLocationException ex) {
      }
   }

   private static String twoDigits(int n) {
      assert n >= 0;
      assert n < 100;
      return n > 9 ? Integer.toString(n) : "0" + Integer.toString(n);
   }

   private static String deltaString(long delta) {
      delta /= 100;
      final int hrs = (int) (delta / (60 * 60 * 10));
      final int mins = (int) ((delta / (60 * 10)) % 60);
      final int secs = (int) ((delta / 10) % 60);
      final int tenths = (int) (delta % 10);
      return Integer.toString(hrs) + ":" + twoDigits(mins) + ":" + twoDigits(secs) + "." + Integer.toString(tenths);
   }

   private void createCmdLine(String cmd) {
      try {
         final StyledDocument doc = (StyledDocument) getDocument();
         final int len = doc.getLength();
         if (mMoreInput)
            doc.insertString(len, (mLastChar == 10 ? "" : "\n") + Integer.toString(mCmdIndex) + "+", getStyle(PROMPT));
         else
            doc.insertString(len, (mLastChar == 10 ? "" : "\n") + Integer.toString(mCmdIndex) + ">", getStyle(PROMPT));
         mLastChar = 0;
         mCmdOffset = doc.getLength() + 1;
         doc.insertString(mCmdOffset - 1, " " + cmd, getStyle(COMMAND));
         this.setCaretPosition(mCmdOffset);
      } catch (final BadLocationException ex) {
      }
   }

   public void executeNT(String cmd) {
      try {
         mJythonWorker.executeNT(cmd);
      } catch (final InterruptedException e) {
         writeError(e.toString());
      }
   }

   public void execute(String cmd) {
      try {
         mJythonWorker.execute(cmd);
      } catch (final InterruptedException e) {
         writeError(e.toString());
      }
   }

   public void run(File cmd) {
      try {
         mJythonWorker.execute(cmd);
      } catch (final Exception e) {
         writeError(e.toString());
      }
   }

   @Override
   public void grabFocus() {
      requestFocusInWindow();
      // Set the caret position at the end unless a region is selected
      if ((mCmdOffset != Integer.MAX_VALUE) && ((getSelectionEnd() - getSelectionStart()) == 0))
         setCaretPosition(getDocument().getLength());
   }

   public void setTabs(int charactersPerTab) {
      final FontMetrics fm = getFontMetrics(getFont());
      final int charWidth = fm.charWidth('w');
      final int tabWidth = charWidth * charactersPerTab;

      final TabStop[] tabs = new TabStop[10];
      for (int j = 0; j < tabs.length; j++) {
         final int tab = j + 1;
         tabs[j] = new TabStop(tab * tabWidth);
      }

      final TabSet tabSet = new TabSet(tabs);
      final SimpleAttributeSet attributes = new SimpleAttributeSet();
      StyleConstants.setTabSet(attributes, tabSet);
      final int length = getDocument().getLength();
      getStyledDocument().setParagraphAttributes(0, length, attributes, true);
   }

   public void saveAs() {
      final JFileChooser jfc = new JFileChooser();
      SimpleFileFilter ff = new SimpleFileFilter(new String[]{"rtf",}, "Rich Text File");
      jfc.addChoosableFileFilter(ff);
      jfc.setFileFilter(ff);
      jfc.setDialogTitle("Save script pane as...");
      jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
      final int option = jfc.showSaveDialog(JCommandLine.this);
      if (option == JFileChooser.APPROVE_OPTION) {
         File file = jfc.getSelectedFile();
         if (!file.getName().toLowerCase().endsWith(".rtf"))
            file = new File(file.getPath() + ".rtf");
         try {
            StyledDocument doc = getStyledDocument();
            OutputStream out = new FileOutputStream(file);
            this.getEditorKit().write(out, doc, 0, doc.getLength());
            out.close();
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   private boolean isControlEquivalent(KeyEvent ke) {
         return DTSA2.getOS() == OS.OS_MAC ? ke.isMetaDown() : ke.isControlDown(); 
   }
   
   /**
    * JCommandLine - The default constructor for JCommandLine
    *
    * @throws HeadlessException
    */
   @SuppressWarnings("unchecked")
   public JCommandLine() throws HeadlessException {
      super();
      setEditorKit(new RTFEditorKit());
      setDocument(getEditorKit().createDefaultDocument());
      setBackground(Color.white);
      mJythonWorker = new JythonWorker();
      boolean assertsEnabled = false;
      assert assertsEnabled = true; // Intentional side effect!!!
      if (!assertsEnabled) {
         final File f = new File(System.getProperty("java.class.path"));
         final File pc = new File(f.getParentFile(), ".jython");
         if (!pc.isDirectory())
            pc.mkdirs();
         System.setProperty("python.cachedir", pc.toString());
      }
      setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
      final Style cmd = addStyle(COMMAND, getStyle("default"));
      StyleConstants.setForeground(cmd, Color.blue);
      final Style prmt = addStyle(PROMPT, getStyle("default"));
      StyleConstants.setForeground(prmt, Color.lightGray);
      final Style err = addStyle(INNER_ERROR, getStyle("default"));
      StyleConstants.setForeground(err, Color.RED);
      mErrorStyle = addStyle("__ERROR__", getStyle("default"));
      StyleConstants.setForeground(mErrorStyle, Color.RED);
      StyleConstants.setBackground(mErrorStyle, getBackground());
      mOutputStyle = addStyle("__OUTPUT__", getStyle("default"));
      StyleConstants.setForeground(mOutputStyle, Color.BLACK);
      StyleConstants.setBackground(mOutputStyle, getBackground());
      mStatusStyle = addStyle("__STATUS__", getStyle("default"));
      StyleConstants.setForeground(mStatusStyle, Color.BLUE);
      StyleConstants.setBackground(mStatusStyle, getBackground());

      setTabs(4);

      addKeyListener(new java.awt.event.KeyAdapter() {
         // I'll insert my own returns and customize the delete key
         @Override
         public void keyPressed(KeyEvent e) {
            final int ss = Math.min(getSelectionEnd(), getSelectionStart());
            final int se = Math.max(getSelectionEnd(), getSelectionStart());
            switch (e.getKeyCode()) {
               case KeyEvent.VK_BACK_SPACE : {
                  if (se < (mCmdOffset + 1)) {
                     e.consume();
                     return;
                  }
                  if (ss < mCmdOffset) {
                     setSelectionStart(mCmdOffset);
                     setSelectionEnd(se);
                  }
                  break;
               }
               case KeyEvent.VK_DELETE : {
                  if (se < mCmdOffset) {
                     e.consume();
                     return;
                  }
                  if (ss < mCmdOffset) {
                     setSelectionStart(mCmdOffset);
                     setSelectionEnd(se);
                  }
                  break;
               }
               case KeyEvent.VK_UP :
               case KeyEvent.VK_DOWN : {
                  if (isControlEquivalent(e))
                     e.consume();
                  break;
               }
               case KeyEvent.VK_LEFT :
                  if (se < (mCmdOffset + 1)) {
                     e.consume();
                     return;
                  }
                  break;
               case KeyEvent.VK_HOME :
               case KeyEvent.VK_PAGE_UP :
                  setCaretPosition(mCmdOffset);
                  e.consume();
                  return;
               // Kill the enter key
               case KeyEvent.VK_ENTER :
                  if (isControlEquivalent(e))
                     e.consume();
                  break;
            }
         }

         // Handle the ESC, Ctrl-Up and Ctrl-Down keys
         @Override
         public void keyReleased(KeyEvent e) {
            switch (e.getKeyCode()) {
               case KeyEvent.VK_ESCAPE : {
                  if (isControlEquivalent(e) || e.isShiftDown()) {
                     try {
                        mJythonWorker.execute(RESET_CMD);
                     } catch (final InterruptedException e1) {
                        writeError(e1.toString());
                     }
                     e.consume();
                  } else {
                     final Document doc = getDocument();
                     try {
                        doc.remove(mCmdOffset, doc.getLength() - mCmdOffset);
                        setCaretPosition(mCmdOffset);
                        e.consume();
                     } catch (final BadLocationException ex) {
                     }
                  }
                  break;
               }
               case KeyEvent.VK_SPACE :
                  if (isControlEquivalent(e)) {
                     performCommandSearch();
                     e.consume();
                  }
                  break;
               case KeyEvent.VK_UP : {
                  if (isControlEquivalent(e) || e.isAltDown()) {
                     e.consume();
                     final Document doc = getDocument();
                     try {
                        doc.remove(mCmdOffset, doc.getLength() - mCmdOffset);
                        doc.insertString(mCmdOffset, mCmdBuffer.previous(), getStyle(COMMAND));
                        setCaretPosition(doc.getLength());
                     } catch (final BadLocationException ex1) {
                     }
                  }
                  break;
               }
               case KeyEvent.VK_DOWN : {
                  if (isControlEquivalent(e) || e.isAltDown()) {
                     e.consume();
                     final Document doc = getDocument();
                     try {
                        doc.remove(mCmdOffset, doc.getLength() - mCmdOffset);
                        doc.insertString(mCmdOffset, mCmdBuffer.next(), getStyle(COMMAND));
                        setCaretPosition(doc.getLength());
                     } catch (final BadLocationException ex1) {
                     }
                  }
                  break;
               }
               case KeyEvent.VK_EQUALS : {
                  if ((e.isShiftDown()) && (isControlEquivalent(e) || e.isAltDown())) {
                     Font f = getFont();
                     setFont(f.deriveFont(f.getSize2D()+2.0f));
                  }
                  break;
               }
               case KeyEvent.VK_MINUS: {
                  if ((e.isShiftDown()) && (isControlEquivalent(e) || e.isAltDown())) {
                     Font f = getFont();
                     setFont(f.deriveFont(f.getSize2D()-2.0f));
                  }
                  break;
               }
            }
         }

         private void performCommandSearch() {
            final Document doc = getDocument();
            try {
               final int cp = getCaretPosition();
               String curr = doc.getText(mCmdOffset, cp - mCmdOffset);
               doc.remove(mCmdOffset, doc.getLength() - mCmdOffset);
               doc.insertString(mCmdOffset, mCmdBuffer.search(curr), getStyle(COMMAND));
               setCaretPosition(cp);
            } catch (BadLocationException e1) {
               e1.printStackTrace();
            }
         }

         // Disable keyboard input if we are not currently on the command
         // line...
         @Override
         public void keyTyped(KeyEvent e) {
            final int ss = Math.min(getSelectionEnd(), getSelectionStart());
            final int se = Math.max(getSelectionEnd(), getSelectionStart());
            // Works inside the debugger but not outside...
            if ((se == mCmdOffset) && (e.getKeyChar() == KeyEvent.VK_BACK_SPACE)) {
               e.consume();
               return;
            }
            if (ss < mCmdOffset) {
               if (se < mCmdOffset) {
                  e.consume();
                  return;
               }
               setSelectionStart(mCmdOffset);
               setSelectionEnd(se);
            }
            if ((isControlEquivalent(e) || e.isAltDown()) && ((e.getKeyChar() == '\n') || (e.getKeyChar() == '\r'))) {
               try {
                  executeCurrentCommand();
               } catch (final BadLocationException ex) {
                  writeException("Command line error: " + ex.toString());
               }
               e.consume();
            }
         }
      });

      jPopupMenu_Main = new JPopupMenu();
      {
         final JMenuItem mi = new JMenuItem("Copy");
         mi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               copy();
            }
         });
         jPopupMenu_Main.add(mi);
      }
      {
         final JMenuItem mi = new JMenuItem("Select all");
         mi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               selectAll();
            }
         });
         jPopupMenu_Main.add(mi);
      }
      {
         final JMenuItem mi = new JMenuItem("Save as...");
         mi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               saveAs();
            }
         });
         jPopupMenu_Main.add(mi);
      }

      addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger())
               jPopupMenu_Main.show(JCommandLine.this, e.getX(), e.getY());
         }

         @Override
         public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger())
               jPopupMenu_Main.show(JCommandLine.this, e.getX(), e.getY());
         }
      });

      // jPopupMenu_Main
      setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
      setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
      writeOutput(InteractiveConsole.getDefaultBanner() + "\n");

      mCmdBuffer = new CommandBuffer(new File(HTMLReport.getBasePath(), "history.txt"), 100);
   }

   public JythonWorker getJythonWorker() {
      return mJythonWorker;
   }

   /**
    * cut - Override cut to copy the entire selected region but only delete the
    * portion within the command line.
    */
   @Override
   public void cut() {
      final StringSelection ss = new StringSelection(getSelectedText());
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
      int st = getSelectionStart();
      int ed = getSelectionEnd();
      if (st > ed) {
         final int t = st;
         st = ed;
         ed = t;
      }
      if (ed < mCmdOffset)
         return;
      if (st < mCmdOffset)
         st = mCmdOffset;
      setSelectionStart(st);
      setSelectionEnd(ed);
      replaceSelection("");
   }

   /**
    * addBanner - Add a banner string to the top of the JCommandLine panel.
    *
    * @param banner
    *           String
    */
   void addBanner(String banner) {
      if (banner.charAt(banner.length() - 1) != '\n')
         banner += "\n";
      final Document doc = getDocument();
      final String bStyle = "BANNER";
      Style st = getStyle(bStyle);
      if (st == null) {
         st = this.addStyle(bStyle, getStyle("default"));
         StyleConstants.setBold(st, true);
      }
      try {
         doc.insertString(0, banner, st);
      } catch (final BadLocationException ex) {
      }
      if (mCmdOffset != Integer.MAX_VALUE) {
         mCmdOffset += banner.length();
         setCaretPosition(mCmdOffset);
      }
   }

   /**
    * Terminate a script executing in the background if one exists
    */
   public void terminateScript() {
      mJythonWorker.set("terminated", Boolean.valueOf(true));
   }

   private void executeCurrentCommand() throws BadLocationException {
      final Document doc = getDocument();
      final int len = doc.getLength();
      final String cmd = doc.getText(mCmdOffset, len - mCmdOffset);
      mCmdIndex++;
      doc.insertString(len, "\n", getStyle("COMMAND"));
      if (mArchivalWriter != null)
         try {
            mArchivalWriter.write(Integer.toString(Math.max(1, mCmdIndex)) + "> " + cmd + "\n");
            mArchivalWriter.flush();
         } catch (final IOException e) {
            e.printStackTrace();
         }
      // To inhibit writing more text...
      mCmdOffset = Integer.MAX_VALUE;
      mLastChar = 10;
      try {
         mJythonWorker.execute(cmd);
      } catch (final Throwable ex) {
         writeException(ex.toString());
      }
      mCurrentCommand = (mCurrentCommand == null ? cmd : mCurrentCommand + "\n" + cmd);
      if (!mMoreInput) {
         mCmdBuffer.add(mCurrentCommand);
         mCurrentCommand = null;
         try {
            mCmdBuffer.write();
         } catch (IOException e) {
            e.printStackTrace();
         }

      }
   }

   enum Mode {
      StdOut, StdErr, NewCommand
   }

   class JythonWorkerOutput {

      final String mOutput;
      final Mode mMode;

      JythonWorkerOutput(Mode mode, String res) {
         mMode = mode;
         mOutput = res;
      }

      Mode getMode() {
         return mMode;
      }

      @Override
      public String toString() {
         return mOutput;
      }
   }

   /**
    * @author nicholas
    */
   public class JythonWorker extends SwingWorker<String, JythonWorkerOutput> {

      private final InteractiveConsole mConsole;
      private final BlockingDeque<String> mCommands;
      private final OutputWriter mStdErr;
      private final OutputWriter mStdOut;

      private class OutputWriter extends Writer {

         private final Mode mMode;
         private boolean mClosed;

         OutputWriter(Mode mode) {
            mMode = mode;
            mClosed = false;
         }

         @Override
         public void close() throws IOException {
            mClosed = true;
         }

         @Override
         public void flush() throws IOException {
            if (mArchivalWriter != null)
               mArchivalWriter.flush();
         }

         @Override
         public void write(char[] cbuf, int off, int len) throws IOException {
            if ((!mClosed) && (cbuf != null) && (len > 0))
               publish(new JythonWorkerOutput(mMode, new String(cbuf, off, len)));
            if (mArchivalWriter != null)
               mArchivalWriter.write(cbuf, off, len);
         }

         @Override
         public void write(String str) {
            if ((!mClosed) && (str != null) && (str.length() > 0))
               publish(new JythonWorkerOutput(mMode, str));
            if (mArchivalWriter != null)
               try {
                  mArchivalWriter.write(str);
               } catch (final IOException e) {
                  // Just ignore it...
               }
         }
      }

      public void addSpectrum(ISpectrumData spec, int idx) throws InterruptedException {
         final String name = "s" + Integer.toString(idx);
         final String tmp = "__safdsdfsadjfhhjuuhnb__" + Integer.toString(++mNextTemp);
         mConsole.set(name, null);
         mConsole.set(tmp, spec);
         mConsole.exec(name + "=wrap(" + tmp + ")");
         mConsole.set(tmp, null);
      }

      public void removeSpectrum(int idx) throws InterruptedException {
         final String name = "s" + Integer.toString(idx);
         mConsole.set(name, null);
      }

      public void execute(String cmd) throws InterruptedException {
         mCommands.putLast(cmd);
      }

      public void executeNT(String cmd) throws InterruptedException {
         mCommands.putLast(NO_TIME + "\n" + cmd);
      }

      public void execute(File file) throws IOException, InterruptedException {
         if (file.isFile()) {
            String name = file.getName();
            name = name.replaceAll(".py$", "");
            name = name.replaceAll(".jy$", "");
            final File dir = new File(file.getParent(), name + " Results");
            dir.mkdirs();
            final String DEF_OUT = "DefaultOutput";
            final StringBuffer cmd = new StringBuffer();
            final String fullDir = dir.getCanonicalPath().replace("\\", "/");
            final String pyPath = file.getCanonicalPath().replace("\\", "/");
            cmd.append("print\n");
            cmd.append("print \"Running " + pyPath + "\"\n");
            cmd.append(DEF_OUT + "=\"" + fullDir + "\"\n");
            cmd.append("f=open(\"" + pyPath + "\")\n");
            cmd.append("globals()[\"_script\"]=f.read()\n");
            cmd.append("f.close()\n");
            cmd.append("del f\n");
            cmd.append("execfile(\"" + pyPath + "\")\n");
            execute(cmd.toString());
            // Note: delete() doesn't delete when the directory is not empty. We make use of this fact.
            dir.delete();
         } else
            writeError(file.toString() + " does not appear to be a file.");
      }

      /**
       *
       */
      public JythonWorker() {
         mCommands = new LinkedBlockingDeque<>();
         mConsole = new InteractiveConsole();
         mStdErr = new OutputWriter(Mode.StdErr);
         mConsole.setErr(mStdErr);
         mStdOut = new OutputWriter(Mode.StdOut);
         mConsole.setOut(mStdOut);
         mCommands.add("import sys");
         final String path = System.getProperty("user.dir").replace('\\', '/');
         mCommands.add("sys.path.append(\"" + path + "/Lib\")");
         // mCommands.add("sys.add_extdir(\"" + path + "\")");
      }

      /*
       * (non-Javadoc)
       *
       * @see javax.swing.SwingWorker#doInBackground()
       */
      @Override
      protected String doInBackground() throws Exception {
         long start = Long.MAX_VALUE;
         boolean newCmd = false;
         while (!isCancelled())
            try {
               if (mCommands.isEmpty() && newCmd) {
                  publish(new JythonWorkerOutput(Mode.NewCommand, ""));
                  newCmd = false;
               }
               final String cmd = mCommands.takeFirst();
               if (cmd.equals(RESET_CMD)) {
                  // Allows the user to reset the command line if it gets
                  // out of sync.
                  mConsole.resetbuffer();
                  publish(new JythonWorkerOutput(Mode.StdErr, "\nPython interpreter reset."));
                  newCmd = true;
               } else {
                  final String[] lines = cmd.split("\n");
                  final boolean timeIt = !((lines.length > 0) && (lines[0].equals(NO_TIME)));
                  newCmd = newCmd || timeIt;
                  if (timeIt) {
                     mConsole.exec("if globals().has_key(\"MainFrame\"):\n\tMainFrame.setEnableUI(False)");
                     mConsole.exec("terminated=False");
                     if ((lines.length > 1) && (start == Long.MAX_VALUE))
                        start = System.currentTimeMillis();
                  }
                  try {
                     for (final String line : lines) {
                        if ((line.length() > 0) && (!line.equals(NO_TIME)))
                           mConsole.push(line);
                        if (timeIt) {
                           // Implements the termination mechanism
                           final PyObject t = mConsole.get("terminated");
                           if ((t != null) && t.__nonzero__())
                              break;
                        }
                     }
                     // The next line ensures that we return back to zero
                     // inset
                     mConsole.push("");
                  } finally {
                     if ((start != Long.MAX_VALUE) && (mCommands.isEmpty())) {
                        mConsole.exec("print \"Elapse: " + deltaString(System.currentTimeMillis() - start) + "\"");
                        start = Long.MAX_VALUE;
                     }
                     mConsole.exec("if globals().has_key(\"MainFrame\"):\n\tMainFrame.setEnableUI(True)");
                     mConsole.exec("if globals().has_key(\"terminated\"):\n\tdel terminated");
                  }
               }
            } catch (final Throwable e) {
               mStdErr.write(e.toString());
            }
         return "Done!";
      }

      @Override
      protected void process(List<JythonWorkerOutput> output) {
         for (final JythonWorkerOutput item : output) {
            final String mBuffer = item.toString();
            switch (item.getMode()) {
               case StdOut :
                  write(mOutputStyle, mBuffer);
                  break;
               case StdErr :
                  write(mErrorStyle, mBuffer);
                  break;
               case NewCommand :
                  createCmdLine(mBuffer);
                  break;
            }
         }
      }

      public void set(String name, Object value) {
         mConsole.set(name, value);
      }
   }
}
