# -*- coding: utf-8 -*-
# DTSA-II/NISTMonte script - Nicholas W. M. Ritchie - 29-May-2010
"""Methods intended to address some basic WDS questions."""

import gov.nist.microanalysis.EPQLibrary as epq
import dtsa2

TwoDSpacing = { 
               # "CER" : 137, 
               # "STE" : 100.4, 
               # "MYR" : 80, 
               "LDE1" : 60.0, 
               "LDE2" : 100.0,
               # "RAP" : 26.121, 
               # "TAP" : 25.757, 
               # "PET" : 8.742,
               "TAPJ" : 25.757, 
               "PETJ" : 8.742, 
               "LIF" : 4.0287,
               # "NaCl" : 2*2.819,
               # "Silicon" : 2*3.136,
               # "Silica 1011" : 2*3.343,
               # "EDDT" : 2*4.402,
               # "ADP" : 2*5.320,
               # "Graphite" : 2*6.70,
               # "KAP" : 2*13.32,
}

RolandCircleRadius = 140.0

def WDS_L(crystal, xrt, n=1, R=RolandCircleRadius):
   """Syntax: WDS_L(crystal, xrt,[n=1], [R=RolandCircleRadius])
   Outputs the WDS L-value for the specified crystal, x-ray transitiion
   transition order and Roland circle."""
   if isinstance(xrt, epq.XRayTransition):
      xrt = xrt.getEnergy()
   return 2.0 * R * n * 1.0e10 * epq.SpectrumUtils.energyToWavelength(xrt) / TwoDSpacing[crystal]

def WDSCrystal(xrt, n=1, R=RolandCircleRadius):
    """Synatx: WDSCrystal(xrt,n=1,[R=RolandCircleRadius])
    Prints a list of the WDS crystals alternatives along with 
    the associated L-value.  (Note: xrt may be an element or 
    an XRayTransitionSet (see getTransitionSet(...))"""
    xrts = []
    if isinstance(xrt, epq.XRayTransition):
        xrts = xrts + [xrt]
    elif isinstance(xrt, str) or isinstance(xrt, epq.Element):
        elm = dtsa2.element(xrt)
        for tr in [epq.XRayTransition.KA1, epq.XRayTransition.LA1, epq.XRayTransition.MA1]:
            if epq.XRayTransition.exists(elm, tr):
                xrts = xrts + [epq.XRayTransition(elm, tr)]
    elif isinstance(xrt, epq.XRayTransitionSet):
        xrts = xrt.getTransitions()          
    else:
        print "Invalid argument: %s" % xrt
        return
    zippo = True
    for cry, twoD in TwoDSpacing.iteritems():
        for tr in xrts:
            L = WDS_L(cry, tr, n, R)
            if (L > 60) and (L < 260):
                if zippo:
                    print "Crystal\tLine\tL Value"
                    zippo = False
                print "%s\t%s\t%3.6g" % (cry, tr, L)
    if zippo:
        print "None"
