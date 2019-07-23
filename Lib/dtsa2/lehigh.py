# -*- coding: utf-8 -*-
# DTSA-II script - Nicholas W. M. Ritchie - 8-Jun-2010
# This one...
"""A series of utility functions for computing various quantities discussed at the Lehigh Microscopy School.
+ convergenceAngle(workingDistance, realApertureDia, mag) - probe convergence angle 
+ depthOfField(workingDistance, realApertureDia, mag) - depth-of-field calculation
+ thresholdEquation(efficiency, contrast, frameTime) - threshold equation
+ backscatterFraction(mat, e0) - backscatter coefficient
+ electronCount(i0,t) - Number of electrons in a probe of current i0 integrated for t seconds
+ gaussianProbeDiameter(probeCurrent, workingDistance, realApertureDia, beta) - Computes the Gaussian probe diameter
+ minProbeDiameter(probeCurrent, cs, beta=betaW, beamEnergy=20.0) - Computes the minimum probe diameter for the specified current
+ maxProbeCurrent(probeDia, cs, beta=betaW) - Computes the maximum probe current for the specified diameter
+ backscatterCoefficient(mat, e0=20.0) - Computes the approximate backscatter coefficient for the material and beam energy
Note:  Distances are all in meters, energies in keV, times in seconds."""

import dtsa2
import gov.nist.microanalysis.EPQLibrary as epq
import gov.nist.microanalysis.Utility as epu
import java.lang as jl

assumedPixelSize = 1.0e-4

def convergenceAngle(workingDistance, realApertureDia):
	'''convergenceAngle(workingDistance, realApertureDia, mag)
	Computes the convergence angle given the working distance, real aperture diameter and magnification.'''
	return realApertureDia/(2.0*workingDistance) 

def depthOfField(workingDistance, realApertureDia, mag):
	'''depthOfField(convergenceAngle, mag)
	Computes the depth-of-field for the specified convergence angle (radians) and magnification'''
	return (4.0*assumedPixelSize*workingDistance)/(realApertureDia*mag)

def thresholdEquation(efficiency, contrast, frameTime):
	"""thresholdEquation(efficiency, contrast, frameTime)
	Estimates the current required to detect the specified contrast on a detector with the \
specified efficiency with a frame average time specified."""
	return 4.0e-12/(efficiency*contrast*contrast*frameTime)

def backscatterFraction(mat, e0=20.0):
	"""backscatterFraction(mat)
	Computes the backscatter fraction for an element or composition"""
	mat=dtsa2.material(mat)
	return epq.BackscatterCoefficient.Heinrich81.compute(mat,epq.ToSI.keV(e0))

def electronCount(i0,t=1.0):
	"""electronCount(i0, t)
	Returns the number of electrons in i0 amps measured for t seconds."""
	c=(i0*t)/epq.PhysicalConstants.ElectronCharge
	return epu.UncertainValue(c,jl.Math.sqrt(c))

betaW = 5.0e4
betaLaB6 = 1.0e6
betaFEG = 1.0e10

def gaussianProbeDiameter(probeCurrent, workingDistance, realApertureDia, beta=betaW):
    """gaussianProbeDiameter(probeCurrent, workingDistance, realApertureDia, [beta=betaW or betaLaB6 or betaFEG])
    Computes the Gaussian probe diameter as a function of probe current, working distance and real aperture diameter."""
    t=jl.Math.PI*convergenceAngle(workingDistance,realApertureDia)
    return 2.0*jl.Math.sqrt(probeCurrent/(beta*t*t))

def minProbeDiameter(probeCurrent, cs, beta=betaW, beamEnergy=20.0):
    """minProbeDiameter(probeCurrent, cs, [beamEnergy=20.0], [beta=betaW or betaLaB6 or betaFEG])
    Computes the minimum achievable probe diameter for the specified probe current"""
    if beamEnergy<10.0:
        print "WARNING: This equation works best above 10 keV."
    lam = 1.24/jl.Math.sqrt(beamEnergy)
    return k*jl.Math.pow(cs,0.25)*jl.Math.pow(lam,0.75)*jl.Math.pow(1.0+probeCurrent/(beta*lam*lam),0.375)

def maxProbeCurrent(probeDia, cs, beta=betaW):
    """maxProbeCurrent(probeDia, cs, [beta=betaW or betaLaB6 or betaFEG])
    Computes the maximum probe current for the specified probe diameter."""
    return ((3.0*jl.Math.PI*jl.Math.PI)/16.0)*beta*jl.Math.pow(probeDia,2.6666)/jl.Math.pow(cs,0.66666)

def backscatterCoefficient(mat, e0=20.0):
	"""backscatterCoefficient(mat, e0=20.0)
	Computes eta, the backscatter coefficient, for the specified material (element) and beam energy keV."""
	return epq.BackscatterCoefficient.Heinrich81.compute(dtsa2.material(mat),epq.ToSI.keV(e0))