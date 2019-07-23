package gov.nist.microanalysis.dtsa2;

import gov.nist.microanalysis.EPQLibrary.DerivedSpectrum;
import gov.nist.microanalysis.EPQLibrary.ISpectrumData;

/**
 * <p>
 * Title: AbsSpectrum
 * </p>
 * <p>
 * Description: Returns the absolute value of the spectrum channel data
 * </p>
 * <p>
 * Copyright: Not subject to copyright - 2004
 * </p>
 * <p>
 * Company: National Institute of Standards and Technology
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class AbsSpectrum
   extends DerivedSpectrum {
   public AbsSpectrum(ISpectrumData src) {
      super(src);
   }

   /**
    * getCounts
    * 
    * @param i int
    * @return double
    */
   @Override
   public double getCounts(int i) {
      return Math.abs(mSource.getCounts(i));
   }

   @Override
   public String toString() {
      return "Abs[" + mSource.toString() + "]";

   }
}
