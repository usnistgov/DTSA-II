# -*- coding: UTF-8 -*-
# Title: qc.py
# Description:  Functions for simplifying the use of the QC database
# Author: Nicholas W. M. Ritchie

import dtsa2 as d2
import java.io as jio
import gov.nist.microanalysis.EPQLibrary as epq

def createProject(det, comp, beamEnergy, nominalWD, nominalCurrent, normalizeI=True):
   if normalizeI:
      norm = epq.SpectrumFitter7.QCNormalization.CURRENT
   else:
      norm = epq.SpectrumFitter7.QCNormalization.TOTAL_COUNTS
   return d2.Database.createQCProject(det,comp, beamEnergy, norm, nominalWD, nominalCurrent)


def listProjects(det=None):
   """listProjects([det=None])
   List all the projects associated with the specified detector or all detectors in det=None."""
   if det==None:
      dd=d2.dets()
   else:
      dd=(det,)
   print "Index\tDetector\tProject"
   for d in dd:
      qcp=d2.Database.findQCProjects(d)
      for qc in qcp:
         print "%d\t%s\t%s" % ( qc.get(i), d.getDetectorProperties(), qc )

def writeProject(idx, file):
   """writeQCProject(idx, file)
   Write the QC project with the specified index to the specified file."""
   wr=jio.FileWriter(file)
   d2.Database.writeQCProject(idx,wr)
   wr.close()

def getProjectData(idx):
   """getProjectData(idx)
   Get all the data as QCEntry objects for the project specified by idx."""
   return d2.Database.getQCProjectData(idx)

def addMeasurement(spec, mat):
   """addMeasurement(spec, mat)
   Add a QC entry based on the specified spectrum collected from the named material.  /
   The spectrum must define the probe current and detector properties."""
   comp=d2.material("__QC__[%s]" % mat)
   if not comp:
      comp=d2.material(mat)
   if not comp:
      raise "Please specify a known material."
   comp.setName(mat)
   det=spec.getProperties().getDetector()
   if not det:
      raise "Please specify a detector in the spectrum properties."
   det=d2.Database.getEarliestCalibrated(det)
   beamE=spec.getProperties().getNumericWithDefault(epq.SpectrumProperties.BeamEnergy,0.0)
   if beamE==0.0:
      raise "Please specify the beam energy in the spectrum properties."
   qcp=d2.Database.getQCProject(det, comp, beamE)
   mode=d2.Datab
   data = epq.SpectrumFitter7.performQC(det,comp,spec,None)
   d2.Database.addQC(qcp, spec, data)
   print "%s added to %s" % (spec, d2.Database.describeQCProject(qcp))
