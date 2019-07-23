# -*- coding: utf-8 -*-
# DTSA-II/NISTMonte script - Nicholas W. M. Ritchie
# Author:  Nicholas W. M. Ritchie
# Updated: 29-May-2010

"""A set of utility functions for working at low beam energy.  Set MinMElement and MinLElement to the \
atomic number of the lowest Z element for which you believe you can see M lines and L lines \ 
# respectively. \ All tests are with respect to the K, LIII and MV edges."""

import gov.nist.microanalysis.EPQLibrary as epq
import dtsa2

MinMElement = epq.Element.elmSb
MinLElement = epq.Element.elmSc


def minEdge(elm):
   """Example:  minEdge("Ag")
   Returns the lowest energy edge which is likely to be visible in an 
   EDS detector for the specified element"""
   elm=dtsa2.element(elm)
   if elm.getAtomicNumber()>=MinMElement:
      res=epq.AtomicShell(elm,epq.AtomicShell.MV)
   elif elm.getAtomicNumber()>=MinLElement:
      res=epq.AtomicShell(elm,epq.AtomicShell.LIII)
   else:
      res=epq.AtomicShell(elm,epq.AtomicShell.K)
   return res


def maxEdge(elm, e0, over=1.5):
   """Example:  maxEdge("Ag", 20.0, 1.5)
   Returns the highest energy edge which is excited with the overvoltage
   specified (1.5) for the specified beam energy (20.0)."""
   elm=dtsa2.element(elm)
   e0=epq.ToSI.keV(e0)
   res = None;
   if elm.getAtomicNumber()>MinMElement:
      sh=epq.AtomicShell.MV
      if epq.AtomicShell.getEdgeEnergy(elm,sh)*over<=e0:
         res=epq.AtomicShell(elm,sh)
   if elm.getAtomicNumber()>MinLElement:
      sh=epq.AtomicShell.LIII
      if epq.AtomicShell.getEdgeEnergy(elm,sh)*over<=e0:
         res=epq.AtomicShell(elm,sh)
   sh=epq.AtomicShell.K
   if epq.AtomicShell.getEdgeEnergy(elm,sh)*over<=e0:
      res=epq.AtomicShell(elm,sh)
   return res

def minBeamEnergy(elms,over=1.5):
   """Example:  minBeamEnergy(["Ag","Au", "Cu"],1.3)
   Returns the lowest beam energy required to provide the specified 
   overvoltage (1.3) for all the elements listed."""
   minE=0.0
   if isinstance(elms,str):
      elms=material(elms)
   if isinstance(elms,epq.Composition):
      elms=elms.getElementSet()
   for elm in elms:
      shell=minEdge(elm)
      e=over*shell.getEdgeEnergy()
      if e>minE:
         minE=e
   return epq.FromSI.keV(minE)
  
def ranges(comp, e0, over=1.5):
   """Example: ranges(createMaterial(),5.0,1.5)
   Prints a list of edges, over voltages, and ionization ranges (in meters)
   for the specified material at the specified beam energy in keV (5.0) and 
   required overvoltage (1.5)"""
   print "Shell\tOver\tRange (m)"
   comp=dtsa2.material(comp)
   for elm in comp.getElementSet():
      elm=dtsa2.element(elm)
      sh=maxEdge(elm,e0,over)
      if sh!=None:
         print "%s\t%g\t%e" % (sh, e0/epq.FromSI.keV(sh.getEdgeEnergy()), epq.ElectronRange.KanayaAndOkayama1972.compute(comp,sh,epq.ToSI.keV(e0))/comp.getDensity())
      else:
         print "%s\tNone\tNone" % elm
   
