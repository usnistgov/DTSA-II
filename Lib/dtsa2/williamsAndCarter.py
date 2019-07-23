# -*- coding: utf-8 -*-
# DTSA-II script - Nicholas W. M. Ritchie - 8-Jun-2010

import gov.nist.microanalysis.EPQLibrary as epq
import java.lang as jl

"""Utility functions drawn from the Williams and Carter TEM book."""

def electronWavelength(e0):
   """electronWavelength(e0)
   Computes the wavelength in meters for an electron with the specified energy (in keV)"""
   m0 = epq.PhysicalConstants.ElectronMass
   h = epq.PhysicalConstants.PlanckConstant
   c = epq.PhysicalConstants.SpeedOfLight
   e0 = epq.ToSI.keV(e0)
   return h/jl.Math.sqrt(2*m0*e0*(1.0+e0/(2.0*m0*c*c)))


   