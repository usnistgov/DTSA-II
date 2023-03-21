/**
 *
 */
package gov.nist.microanalysis.dtsa2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author nritchie
 *
 */
public class CommandBuffer {

   private File mArchive;
   private int mLength;
   private ArrayList<String> mBuffer = new ArrayList<>();
   private int mIndex;

   public CommandBuffer(File archive, int length) {
      mArchive = archive;
      mLength = length;
      if (mArchive.isFile()) {
         try {
            BufferedReader fr = new BufferedReader(new FileReader(mArchive, java.nio.charset.Charset.forName("UTF8")));
            try {
               while (fr.ready()) {
                  String line = fr.readLine();
                  if (line.length() > 0)
                     mBuffer.add(line.replace("\\n", "\n"));
               }
            } finally {
               fr.close();
            }
         } catch (IOException e1) {
            System.err.print("Unable to open command history");
         }
      }
      mIndex = mBuffer.size();
   }

   public void add(String cmd) {
      int idx = mBuffer.indexOf(cmd);
      if (idx == -1) {
         mBuffer.add(cmd);
         mIndex = mBuffer.size();
      } else
         mIndex = idx;
   }

   public String previous() {
      mIndex = Math.max(-1, mIndex - 1);
      return current();
   }

   public String next() {
      mIndex = Math.min(mBuffer.size(), mIndex + 1);
      return current();
   }

   public String current() {
      if ((mIndex >= 0) && (mIndex < mBuffer.size()))
         return mBuffer.get(mIndex);
      else
         return "";
   }

   public String search(String cmd) {
      int st = Math.min(mIndex - 1, mBuffer.size() - 1);
      // Search backwards
      for (int i = st; i >= 0; --i) {
         if (mBuffer.get(i).startsWith(cmd)) {
            mIndex = i;
            return mBuffer.get(mIndex);
         }
      }
      // Search forwards
      for (int i = mBuffer.size() - 1; i > st; --i) {
         if (mBuffer.get(i).startsWith(cmd)) {
            mIndex = i;
            return mBuffer.get(mIndex);
         }
      }
      return cmd;
   }

   public void write() throws IOException {
      FileWriter fw = new FileWriter(mArchive, java.nio.charset.Charset.forName("UTF8"));
      try {
         for (int i = Math.max(0, mBuffer.size() - mLength); i < mBuffer.size(); ++i) {
            fw.write(mBuffer.get(i).replace("\n", "\\n"));
            fw.write("\n");
         }
         fw.flush();
      } finally {
         fw.close();
      }
   }

}
