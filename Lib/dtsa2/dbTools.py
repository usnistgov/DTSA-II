# -*- coding: utf-8 -*-
# DTSA-II/NISTMonte script - Nicholas W. M. Ritchie - 29-May-2010

import dtsa2
import gov.nist.microanalysis.EPQLibrary.Detector as epd
import gov.nist.microanalysis.EPQLibrary as epq
from telnetlib import DET

def standardExists(comp):
    """standardExists(comp)
    Is a composition with the same name as 'comp' present in the database."""
    return dtsa2.Database.findStandard(comp.getName()) != None

def addStandard(comp):
    """addStandard(comp)
    Add a material or composition defined by comp to the database."""
    dtsa2.Database.addStandard(comp)

def deleteDetector(det):
    """deleteDetector(det) - Removes a detector from the internal database.  Use with care as associated spectra will be left hanging."""
    dtsa2.Database.deleteDetector(dtsa2.Database.findDetector(det.getDetectorProperties()))
    
def createDetector(name, nCh = 2048, evPerCh=10.0, fwhm=135.0, toa=40.0,detToSample=10.0, optWD=12.0, owner = None, xtraProps = {}):
    """createDetector(name, nCh = 2048, evPerCh=10.0, fwhm=135.0, toa=40.0,detToSample=10.0, optWD=12.0, owner = None, xtraProps = {})
Create an in memory representation of an EDS detector with the specified properties.  You can modify the properties to \
customize the detector.
+ name - User friendly name
+ nCh - Number of channels
+ evPerCh - Channel width
+ fwhm - Full width half max at Mn Ka
+ toa - Take off angle in degrees
+ detToSample - Detector to sample distance in mm
+ optWD - Optimal working distance in mm
+ owner - Which instrument owns this detector
+ xtraProps - Extra numeric properties to define"""
    # Create a new detector
    dd=epd.EDSDetector.createSiLiDetector(nCh,evPerCh,fwhm)
    dd.getDetectorProperties().setName(name)
    dp=dd.getDetectorProperties().getProperties()

    # Take-off -> 25 deg, sample-det distant -> 9.22 mm, optimal working distance-> 2.2 mm
    dp.setDetectorPosition(toa*3.1415926/180.0, 0.0, detToSample*1.0e-3, optWD*1.0e-3)
    # Set the detector to point anti-parallel to the x-axis.
    dp.setArrayProperty(epq.SpectrumProperties.DetectorOrientation,[-1.0,0.0,0.0])

    for name, val in xtraProps:
        dp.setNumericProperty(name,val)
    if not owner:
        if len(dtsa2.dets) > 0:
            baseDet = dtsa2.dets[0]
            # owner = dtsa2.dets()[0].getOwner()
            owner = baseDet.getOwner()
            dd.setOwner(owner)
    return dd

def addDetector(dd):
    """Add a detector to the database.  (Create one with createDetector.)"""
    dtsa2.Database.addDetector(dd)
    
    
def listQCProjects():
    """listQCProjects()
List all the QC projects defined in the database."""
    qcdets=dtsa2.Database.findDetectorsWithQCProjects()
    all=[]
    for det in qcdets:
        all.extend(dtsa2.Database.findQCProjects(det))
    print "Index\tSize\tName"
    for i in all:
        print "%d\t%d\t%s" % (i.getIndex(), i.getEntries().size(), i)
 
def deleteQCProject(index):
    dtsa2.Database.deleteQCProject(index)