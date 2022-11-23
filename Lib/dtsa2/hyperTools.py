"""Tools for processing hyperspectral data sets."""

import gov.nist.microanalysis.EPQLibrary as epq
import gov.nist.microanalysis.EPQTools as ept
import dtsa2 as dt2
import java.lang as jl
import io
import sys

sys.packageManager.makeJavaPackage("gov.nist.microanalysis.FastQuant", "FastQuant", None)
import gov.nist.microanalysis.FastQuant as fq


def openRipple(rpl, e0, i0, liveTime, det):
   """openRipple(rpl, e0, i0, liveTime, det)
   Open a ripple file as an ISpectrumData object with setPosition(x,y) to permit navigating /
through the spectra."""
   sp = epq.SpectrumProperties()
   sp.setDetector(det)
   sp.setNumericProperty(epq.SpectrumProperties.BeamEnergy, e0)
   sp.setNumericProperty(epq.SpectrumProperties.ProbeCurrent, i0)
   sp.setNumericProperty(epq.SpectrumProperties.LiveTime, liveTime)
   return ept.RippleSpectrum(rpl, sp)

def maskRipple(inRpl, outFile, mask):
   """maskRipple(inRpl, outFile, mask)
   Sets the individual data items to zero based on the specified mask.  If mask.getRGB(c,r)>0 /
then copy the contents at(c,r) of inRpl to outFile.rpl.  Otherwise the contents of outFile /
is set to all zeros."""
   outRpl = "%s.rpl" % outFile
   outRaw = "%s.raw" % outFile
   len = rpl.getDepth()
   ty = rpl.getDataType()
   res = ept.RippleFile(rpl.getColumns(), rpl.getRows(), rpl.getDepth(), rpl.getDataType(), rpl.getDataSize(), ept.RippleFile.DONT_CARE_ENDIAN, outRpl, outRaw)
   zero = (0) * len
   for c in xrange(0, rpl.getColumns()):
      for r in xrange(0, rpl.getRows()):
         rpl.setPosition(c, r)
         res.setPosition(c, r)
         if mask.getRGB(c, r) > 0:
            if ty == rpl.FLOAT:
               res.write(rpl.readDouble(len))
            else:
               res.write(rpl.readInt(len))
   return res

def quantify(rpl, stds, refs={}, preferred=(), elmByDiff=None, elmByStoic=None, assumedStoic={}, mask=None, step=1, visualize=False, zaf=None, withUnc=False):
   """quantify(rpl,stds,[refs={}],[preferred=()],[elmByDiff=None],[elmByStoic=None],[assumedStoic={}], [mask=None],[zaf=None], [withUnc=False])
   Quantify a ripple/raw spectrum object based on the standards, references and other parameters specified. /
The arguments are the same as dtsa2.multiQuant.  An additional 'mask' argument allows uninteresting pixels /
to be ignored (not quantified.)  The mask should be an object like a BufferedImage with a getRGB(x,y) method. /
The pixel is ignored if mask.getRGB(x,y)==0. The result is written to a RPL/RAW file in FLOAT format.
> import javax.imageio.ImageIO as io
> mask = io.read(jio.File("c:/image.png"))
> zaf = epq.CorrectionAlgorithm.NullCorrection for k-ratios"""
   oldSt = None
   try:
      if (zaf != None) and isinstance(zaf, epq.CorrectionAlgorithm):
         oldSt = epq.AlgorithmUser.getGlobalStrategy()
         newSt = epq.AlgorithmUser.getGlobalStrategy()
         newSt.addAlgorithm(epq.CorrectionAlgorithm, zaf)
         epq.AlgorithmUser.applyGlobalOverride(newSt)         
      det = rpl.getProperties().getDetector()
      e0 = rpl.getProperties().getNumericProperty(epq.SpectrumProperties.BeamEnergy)
      mq = dt2.multiQuant(det, e0, stds, refs, preferred, elmByDiff, elmByStoic, assumedStoic)
      base = rpl.getProperties().getTextProperty(epq.SpectrumProperties.SourceFile)
      compRpl = base.replace(".rpl", "_comp.rpl")
      compRaw = base.replace(".rpl", "_comp.raw")
      compTxt = base.replace(".rpl", "_comp.txt")
      status = io.open(compTxt, "wt", encoding="UTF-8")
      status.write(u"File:\t%s\n" % base)
      status.write(u"Results:\t%s\n" % compRpl)
      status.write(u"Detector:\t%s\n" % det)
      status.write(u"Beam energy\t%g keV\n" % e0)
      status.write(u"Standards\n")
      i = 0;
      elms = []  # Ensures the plane order is correct
      for elm, std in stds.iteritems():
         status.write(u"\t%d\t%s\t%s\n" % (i, elm, std))
         elms.append(dt2.element(elm))
         i = i + 1
      if len(refs) > 0:
         status.write(u"References\n")
         for xrt, ref in refs.iteritems():
            status.write(u"\t%s\t%s\n" % (xrt, ref))
      if len(preferred) > 0:
         status.write(u"Preferred transitions\n")
         for xrt in preferred:
            tr = dt2.transition(xrt)
            status.write(u"\t%s for %s\n" % (tr, tr.getElement()))
      if elmByDiff:
         status.write(u"Element by difference: %s\n" % elmByDiff)
      if elmByStoic:
         status.write(u"Element by Stoiciometry: %s\n" % elmByStoic)
         status.write(u"Element\tValence\n")
         for elm, stoic in assumedStoic.iteritems():
            status.write(u"\t%s\t%g\n" % (elm, stoic))
      comps = ept.RippleFile((rpl.getColumns() + step - 1) / step, (rpl.getRows() + step - 1) / step, len(stds) + 1, ept.RippleFile.FLOAT, 8, ept.RippleFile.DONT_CARE_ENDIAN, compRpl, compRaw)
      uncert = None
      if withUnc:
          uncRaw = base.replace(".rpl", "_unc.raw")
          uncRpl = base.replace(".rpl", "_unc.rpl")
          uncert = ept.RippleFile((rpl.getColumns() + step - 1) / step, (rpl.getRows() + step - 1) / step, len(stds) + 1, ept.RippleFile.FLOAT, 8, ept.RippleFile.DONT_CARE_ENDIAN, uncRpl, uncRaw)
      dumpIt = False
      if dumpIt:
         dumpRpl = base.replace(".rpl", "_dump.rpl")
         dumpRaw = base.replace(".rpl", "_dump.raw")
         dump = ept.RippleFile((rpl.getColumns() + step - 1) / step, (rpl.getRows() + step - 1) / step, rpl.getChannelCount(), ept.RippleFile.UNSIGNED, 4, ept.RippleFile.DONT_CARE_ENDIAN, dumpRpl, dumpRaw)
      rpl.setSpan(step, step)
      for r in xrange(0, rpl.getRows(), step):
         if dt2.isTerminated():
            break
         dt2.StdOut.append("%d%s" % (r, (", " if r % 16 <> 15 else "\n")))
         dt2.StdOut.flush()
         for c in xrange(0, rpl.getColumns(), step):
            if dt2.isTerminated():
               break
            if visualize:
               dt2.clearSpectra()
               dt2.display(rpl)
            comps.setPosition(c / step, r / step)
            if (mask == None) or ((mask.getRGB(c, r) & 0xFFFFFF) > 0):
                try:
                   rpl.setPosition(c, r)
                   rs = epq.SpectrumUtils.copy(rpl)
                   if dumpIt:
                      # print "%d\t%d\t%d\t%d" % (c, r, c / step, r / step)
                      dump.setPosition(c / step, r / step)
                      dump.write(epq.SpectrumUtils.toIntArray(rs))
                   res = mq.compute(rs)
                   comp = res.getComposition()
                   if visualize:
                      rpl.getProperties().setCompositionProperty(epq.SpectrumProperties.MicroanalyticalComposition, comp)
                      dt2.annotComposition()
                   tmp, unc = [], []
                   sU = 0.0
                   for elm in elms:
                      tmp.append(comp.weightFraction(elm, True))
                      u = comp.weightFractionU(elm, True).uncertainty()
                      unc.append(u)
                      sU = sU + u * u
                   tmp.append(comp.sumWeightFraction())
                   unc.append(jl.Math.sqrt(sU))
                   if dumpIt:
                      print tmp
                   comps.write(tmp)
                   if uncert:
                       uncert.write(unc)
                except (epq.EPQException, Exception, jl.Exception), e:
                   msg = u"row = %d, col = %d failed: %s" % (r, c, e)
                   print msg
                   status.write(msg + u"\n")
                   for elm, std in stds.iteritems():
                      comps.write(0.0)
                      if uncert:
                         uncert.write(unc)
                   if visualize:
                      dt2.setAnnotation("row = %d, col = %d failed." % (r, c))
            else:
               for elm, std in stds.iteritems():
                  comps.write(0.0)
                  if uncert:
                     uncert.write(unc)
               comps.write(1.0)
               if uncert:
                  uncert.write(1.0)
      comps.close()
      if uncert:
         uncert.close()
      if dumpIt:
         dump.close()
      status.close()
   finally:
      if oldSt:
         epq.AlgorithmUser.applyGlobalOverride(oldSt)
   
   print "\nDone!"
   
def maxPixel(rpl):
   """maxPixel(rpl)
   Computes the max pixel spectrum for the specified ripple/raw spectrum object."""
   xs = epq.ExtremumSpectrum()
   for r in xrange(0, rpl.getRows()):
      dt2.StdOut.append(".")
      if dt2.terminated:
         break
      for c in xrange(0, rpl.getColumns()):
         rpl.setPosition(r, c)
         xs.include(rpl)
   return xs

def rippleToTIFF(rpl, minLayer=0, maxLayer=4294967295):
   """rippleToTIFF(rpl, min=0, max=4294967295)
   Converts a RPL/RAW file pair to a TIFF image representing the sum of layers minLayer to maxLayer.  
   The rpl argument is the full path of the RPL file.  min and max bound the layers to export."""
   rf = ept.RippleFile(rpl, True)
   tiff = rpl.replace(".rpl", "-[%d,%d].tif" % (minLayer, maxLayer,))
   rf.layersToTIFF(tiff, minLayer, maxLayer + 1, True)


def buildVectors(stds, path=None, strip=(), det = None):
    """buildVectors(stds, path=None,strip=())
    Construct a set of Schamber-style fast quant vectors
    stds = { "Fe" : "Fe std", "Cr": "Cr std", "Cd":s101 ... }
    path = "/home/nicholas/standards" or similar (None -> defaultPath)
    strip= ("C", "O", ...) a list of elements to strip (must also be in stds)"""
    procStds = {}
    strip = [element(elm) for elm in strip]
    e0=None
    for elm, std in stds.iteritems():
        if isinstance(std, str):
            path = (path if path else defaultVecPath)
            std = readSpectrum("%s/%s" % (path, std))
        elif isinstance(std, dt2.ScriptableSpectrum):
            std = std.wrapped
            det = (det if det else std.getProperties().detector)
        procStds[dt2.element(elm)] = std
        e0 = (e0 if e0 else epq.ToSI.keV(dt2.wrap(std).beamEnergy()))
    sv = fq.SchamberVectors(det, e0)
    for elm, std in procStds.iteritems():
        sv.addStandard(elm, std, elm in strip)
    return sv.getVectorSet()

      
def rippleToMaps(rpl, vecSet, step=1):
    """RippleToMaps(rpl, vecSet, step=1)
    Applies a vector set to a RPL/RAW file pair producing a dictionary of MapImage objects /
indexed by RegionOfInterest objects."""
    return rpl.process(vecs, step)
    #width = rpl.getColumns()
    #height = rpl.getRows()
    #vecs = vecSet.getVectors()
    #planes = epq.MapImage(width, height, vecSet.getROIs(), vecSet.toString(), epq.MapImage.DataType.K_RATIOS)
    #rpl.setSpan(step, step)
    #for r in xrange(0, height, step):
    #    if dt2.isTerminated():
    #        break
    #    for c in xrange(0, width, step):
    #        if dt2.isTerminated():
    #            break
    #        rpl.setPosition(r, c)
    #        krs = vecSet.getKRatios(rpl)
    #        kra = []
    #        for vec in vecs:
    #            kra.append(krs.getKRatio(vec.getROI().getAllTransitions()))
    #        planes.inc(c, r, kra)
    #return planes



def rippleToLog3(rpl, vecs, step=1, path=None):
    path = (path if path else defaultPath)
    mi=rpl.process(vecs, step)
    mi.save(path,"RPL",True,True,True,True)