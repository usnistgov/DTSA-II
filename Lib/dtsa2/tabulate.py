# Author: Nicholas W. M. Ritchie
# Tested against: DTSA-II 1795
# Updated: 22-Dec-2008

"""Methods for computing and tabulating compositional summaries.  
+ tabulateCompositions1(...), tabulateCompositions2(...), compStats(...) & compositionStats(...) defined."""

import gov.nist.microanalysis.EPQLibrary as epq
import gov.nist.microanalysis.Utility as epu

import java.util as ju

def compStats(comps, norm=False):
   """Syntax: compStats([comp1,comp2,...,compN],norm=False)
   Compute compositional statistics for the list of Composition objects.  If norm=1, 
   then the Composition objects are normalized to 100%; otherwise not."""
   elms = ju.TreeSet()
   for comp in comps:
      elms.addAll(comp.getElementSet())
   dss = {}
   for elm in elms:
      dss[elm] = epu.DescriptiveStatistics()
   for comp in comps:
      for elm in elms:
         dss[elm].add(comp.weightPercent(elm, norm))
   return dss

def tabulateCompositions1(specs, withErrs=False, normalize=False, prop=epq.SpectrumProperties.MicroanalyticalComposition):
   """Syntax: tabulateCompositions1(specs,[withErrs=False],[normalize=False],[prop=epq.SpectrumProperties.MicroanalyticalComposition])
      Tabulate the compositions associated with the specified collection of spectra.  Rows
      represent elements and columns represent spectra."""
   res = {}
   elms = ju.TreeSet()
   for spec in specs:
      c = spec.getProperties().getObjectWithDefault(prop, None)
      if c != None:
         res[spec] = c
         elms.addAll(c.getElementSet())
   str = "Name"
   for spec in res:
      str = "%s\t%s" % (str, spec)
      if withErrs:
         str = "%s\t" % str
   print str
   for elm in elms:
      str = "%s" % elm.toAbbrev()
      for spec in res:
         u = res[spec].weightFractionU(elm, normalize)
         str = "%s\t%g" % (str, 100 * u.doubleValue())
         if withErrs:
            str = "%s\t%g" % (str, 100.0 * u.uncertainty())
      print str

def tabulateCompositions2(specs, withErrs=False, normalize=False, prop=epq.SpectrumProperties.MicroanalyticalComposition):
   """Syntax: tabulateCompositions2(specs,[withErrs=False],[normalize=False],[prop=epq.SpectrumProperties.MicroanalyticalComposition])
      Tabulate the compositions associated with the specified collection of spectra.  Rows
      represent spectra and columns represent elements."""
   res = {}
   elms = ju.TreeSet()
   for spec in specs:
      c = spec.getProperties().getObjectWithDefault(prop, None)
      if c != None:
         res[spec] = c
         elms.addAll(c.getElementSet())
   str = "Name"
   for elm in elms:
      str = "%s\t%s" % (str, elm.toAbbrev())
      if withErrs:
         str = "%s\td%s" % (str, elm.toAbbrev())
   print str
   for spec in res:
      c = res[spec]
      str = "%s" % spec
      for elm in elms:
         u = c.weightFractionU(elm, normalize)
         str = "%s\t%g" % (str, 100 * u.doubleValue())
         if withErrs:
            str = "%s\t%g" % (str, 100.0 * u.uncertainty())
      print str

def compareCompositions(specs, comp, withSigma=False, normalize=False, prop=epq.SpectrumProperties.MicroanalyticalComposition):
   """Syntax: compareCompositions(specs, comp=createMaterial(), [withSigma=False], [normalize=False], [prop=epq.SpectrumProperties.MicroanalyticalComposition]
   Tabulate the comparison between the compositions of the specified spectra with the specified composition."""
   res = {}
   elms = ju.TreeSet()
   for spec in specs:
      c = spec.getProperties().getObjectWithDefault(prop, None)
      if c != None:
         res[spec] = c
         elms.addAll(c.getElementSet())
   str = "Name"
   for elm in elms:
      str = "%s\t%s-diff\t%s-frac" % (str, elm.toAbbrev(), elm.toAbbrev())
      if withSigma:
         str = "%s\t%s-sig" % (str, elm.toAbbrev())
   print str
   str="Nominal"
   for elm in elms:
       str = "%s\t%g\t" % (str, comp.weightFraction(elm,normalize)*100)
       if withSigma:
           str="%s\t" % str    
   print str
   for spec in res:
      c = res[spec]
      str = "%s" % spec
      for elm in elms:
         u = c.weightFractionU(elm, normalize)
         err = 100 *(u.doubleValue()-comp.weightFraction(elm,normalize))
         str = "%s\t%g\t%g" % (str, err, err/comp.weightFraction(elm,normalize))
         if withSigma:
             if u.uncertainty()>0:
                 str = "%s\t%g" % (str, err / (100.0 * u.uncertainty()))
             else:
                 str = "%s\t-" % str
      print str

def compositionStats(specs, normalize=0, prop=epq.SpectrumProperties.MicroanalyticalComposition):
   """Syntax: compositionStats(specs,[normalize=0],[prop=epq.SpectrumProperties.MicroanalyticalComposition])
      Compute the average, standard deviation and weighted mean of a series of composition measurements.
      The weighted mean is the maximal likelyhood estimator of the mean under the assumption that the
      errors are random and distributed normally."""
   comps = {}
   stats = {}
   uvs = {}
   for spec in specs:
      c = spec.getProperties().getObjectWithDefault(prop, None)
      if c != None:
         comps[spec] = c
         for elm in c.getElementSet():
            if not (elm in stats):
               stats[elm] = epu.DescriptiveStatistics()
               uvs[elm] = ju.ArrayList()
   for spec in comps:
      c = comps[spec]
      for elm in c.getElementSet():
         stats[elm].add(c.weightFraction(elm, normalize))
         uvs[elm].add(c.weightFractionU(elm, normalize))
   print "Elm\tAvg\tStdDev\tWgtMean"
   for elm in stats:
      s = stats[elm]
      try:
         u = epu.UncertainValue.weightedMean(uvs[elm])
         print "%s\t%g\t%g\t%g\t%g" % (elm.toAbbrev(), 100.0 * s.average(), 100.0 * s.standardDeviation(), 100.0 * u.doubleValue(), 100.0 * u.uncertainty())
      except:
         print "%s\t%g\t%g\t-\t-" % (elm.toAbbrev(), 100.0 * s.average(), 100.0 * s.standardDeviation())
