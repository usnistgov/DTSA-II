# -*- coding: utf-8 -*-
# DTSA-II/NISTMonte script - Nicholas W. M. Ritchie - 5-Jul-2023
from compiler.pycodegen import TRY_FINALLY
    
# Example:
# 1> xp = { "Transitions" : [transition("Fe K-L3"), transition("Fe K-M3"), transition("Fe L3-M5"), transition("O K-L3")], "Emission Images":5.0e-6, "Characteristic Accumulator":True, "Char Fluor Accumulator":True, "Brem Fluor Accumulator":True, "PhiRhoZ":5.0e-6, "Output" : "Z:/nritchie/Desktop/tmp" }
# 2> display(simulate(material("Fe2O3",5.0), d2, 20.0, nTraj=100, sf=True, bf=True, xtraParams = xp))

# Mac OS X seems to require the next line.
__revision__ = "$Id: visualize.py Nicholas W. M. Ritchie. Mod JRM $"
__version__ = "0.0.4 - 2023-09-13"

import sys
sys.packageManager.makeJavaPackage("gov.nist.microanalysis.NISTMonte.Gen3", "CharacteristicXRayGeneration3, BremsstrahlungXRayGeneration3, FluorescenceXRayGeneration3, XRayTransport3", None)
import gov.nist.microanalysis.NISTMonte as nm
#import gov.nist.microanalysis.NISTMonte.Gen3 as nm3
import gov.nist.microanalysis.EPQLibrary as epq
#import gov.nist.microanalysis.EPQLibrary.Detector as epd
import gov.nist.microanalysis.Utility as epu
import gov.nist.microanalysis.EPQTools as ept
import javax.imageio.ImageIO as iio
import java.io as jio
import java.lang as jl
import dtsa2
import dtsa2.mcSimulate3 as mc3

def visualize(e0, nTraj, name, buildSample, buildParams, xtraParams):
    """visualize(e0, nTraj, name, buildSample, buildParams, xtraParams) represents \
    a generic mechanism to generated images to visualize Monte Carlo trajectories."""
    if e0 < 0.1:
        raise "The beam energy must be larger than 0.1 keV."
    if nTraj < 1:
        raise "The number of electron trajectories must be larger than or equal to 1."
    # Place the sample at the optimal location for the detector
    origin = [ 0.0, 0.0, 0.0 ]
    # Create a simulator and initialize it
    monte = nm.MonteCarloSS()
    if xtraParams.has_key("Gun"):
        gun = xtraParams["Gun"]
        beam = nm.GaussianBeam(gun * 1.0e-9)
        beam.setCenter([0.0, 0.0, -0.099])
        monte.setElectronGun(beam)
    if xtraParams.has_key("PosX"):
        beamX = xtraParams["PosX"]
        beamY = xtraParams["PosY"]
        beamZ = xtraParams["PosZ"]
        beamNM = xtraParams["nmSize"]
        beam = nm.GaussianBeam(beamNM * 1.0e-9)
        beam.setCenter([beamX, beamY, beamZ]) 
        monte.setElectronGun(beam)
    chamber = monte.getChamber()
    if xtraParams.has_key("VP"):
        pathLength, gas = xtraParams["VP"]
        dim = 0.5 * nm.MonteCarloSS.ChamberRadius;
        dims = epu.Math2.plus(epu.Math2.v3(dim, dim, dim), epu.Math2.z3(2.0 * pathLength))
        pt = epu.Math2.plus(origin, epu.Math2.z3(0.5 * dim));
        shape = nm.MultiPlaneShape.createBlock(dims, pt, 0.0, 0.0, 0.0);
        msm = nm.BasicMaterialModel(gas);
        chamber = monte.addSubRegion(chamber, msm, shape);
    monte.setBeamEnergy(epq.ToSI.keV(e0))
    buildSample(monte, chamber, origin, buildParams)
    dim = xtraParams['Scale']
    ti = nm.TrajectoryImage(1024, 1024, dim)
    ti.setXRange(origin[0] - 0.5 * dim, origin[0] + 0.5 * dim)
    ti.setYRange(origin[2] - 0.05 * dim, origin[2] + 0.95 * dim)
    ti.setMaxTrajectories(nTraj)
    monte.addActionListener(ti)
    defOut = (dtsa2.DefaultOutput if dtsa2.DefaultOutput else dtsa2.reportPath())
    do = ("%s\\%s" % (xtraParams["Output"], dtsa2.normalizeFilename(name)) if xtraParams.has_key("Output") else "%s/%s" % (defOut, dtsa2.normalizeFilename(name)))
    do = do.replace("\\", "/")
    fdo = jio.File(do)
    fdo.mkdirs()
    print "Output path: %s\n" % (fdo, )
    doVRML = False # xtraParams.has_key('VRML')
    vrmlWr = None
    if doVRML:
        vrmlFile = jio.File.createTempFile("vrml", ".wrl", fdo)
        print "VRML in " + str(vrmlFile)
        vrmlWr = jio.FileWriter(vrmlFile)
        vrml = nm.TrajectoryVRML(monte, vrmlWr)
        vrml.setDisplayBackscatter(False)
        vrml.setDisplayXRayEvent(True)
        vrml.setMaxTrajectories(nTraj)
        vrml.setTrajectoryWidth(1.0e-9)
        vrml.setMaxRadius(1.0)
        vrml.setEmissive(True)
        vrml.addView("Y-Axis", epu.Math2.plus(origin, (0.0, 5.0e-6, 0.0)), origin)
        vrml.addView("Gun", epu.Math2.plus(origin, (0.0, 0.0, -5.0e-6)), origin)
        vrml.addView("X-Axis", epu.Math2.plus(origin, (-5.0e-6, 0.0, 0.0)), origin)
        vrml.renderSample()
        monte.addActionListener(vrml)
    for tr in xrange(1,nTraj):
        if dtsa2.terminated:
            break
        monte.runTrajectory();
        fdo = jio.File(do)
        fdo.mkdirs()
        ti.dump(jio.File(do,"//%s[%02d].png"% (name, tr,) ))
    if vrmlWr:
        vrmlWr.close()
 
       
# See mcSimulate3 for build parameters
#import dtsa2
#import dtsa2.mcSimulate3 as mc3
#import dtsa2.visualize as dv
#buildParams = { "Height": 1.0e-5, "Width": 1.0e-5, "Substrate": dtsa2.material("C", 2.0), "Material": dtsa2.material("SiO2", 2.5), "Radius": 1.8e-6 }
#dv.visualize(20.0, 100, "Block", mc3.buildBlock, buildParams, { "Scale": 0.7e-5 })
#dv.visualize(20.0, 5, "Substrate", mc3.buildBulk, buildParams, { "Scale": 2.0e-5 })
#dv.visualize(20.0, 100, "Sphere", mc3.buildSphere, buildParams, { "Scale": 0.7e-5 })
