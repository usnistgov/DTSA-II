# Functions for computing compositional statistics
# Authour: Nicholas W. M. Ritchie 
# Created: 14-May-2008
import java.util as ju

def compStats(comps, norm=0):
   """Syntax: compStats([comp1,comp2,...,compN],norm=1)
   Compute compositional statistics for the list of Composition objects.  If norm=1, 
   then the Composition objects are normalized to 100%; otherwise not."""
   elms=ju.TreeSet()
   for comp in comps:
      elms.addAll(comp.getElementSet())
   dss=dict()
   for elm in elms:
      dss[elm] = epu.DescriptiveStatistics()
   for comp in comps:
      for elm in elms:
         dss[elm].add(comp.weightPercent(elm,norm))
   return dss

def quantStats(specs, prop=epq.SpectrumProperties.MicroanalyticalComposition):
   """Syntax: quantStatus([spec1,spec2,...,specN],prop=epq.SpectrumProperties.MicroanalyticalComposition)
   Computes the average Composition from the specified list of ScriptableSpectrum objects where
   the specified Composition property is extracted."""
   comps=[]
   str = None
   for spec in specs:
      c=spec.getProperties().getCompositionWithDefault(prop,None)
      if c!=None:
         if str==None:
            str=spec.toString()
         else:
            str="%s, %s" % (str, spec.toString())
         comps=comps+[c]
   print "Quantitative statistics from %d spectra (%s)\n  [%s]" % (len(comps), prop, str)         
   cs=compStats(comps)
   print "Z\tElement\tAverage\tStdDev"
   print "\t\t (%)\t (%)"
   for elm, ds in cs.iteritems():
      print "%d\t%s\t%2.3f\t%2.3f" % ( elm.getAtomicNumber(), elm.toAbbrev(), ds.average()*100.0, ds.standardDeviation()*100.0 )

