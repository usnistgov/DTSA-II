# -*- coding: utf-8 -*-
# DTSA-II/NISTMonte script - Nicholas W. M. Ritchie - 5-Jul-2023
"""A series of scripts for simulating various common geometries using the 3rd generation NISTMonte Monte Carlo simulation algorithms. \
Note that the Monte Carlo algorithm requires densities for all materials.  Usually this is accomplished by:
  1) creating a epq.Material object directly
  2) using createMaterial(...)
  3) using the material(str,density) version of the material utility function
The methods basically implement the same geometries as the GUI but facilitate scripting multiple simulations.
Methods:
+ simulate(mat, [det], [e0], [dose], [withPoisson], [nTraj], [sf], [bf], [xtraParams])
+ sphere(mat, [radius], [det], [e0], [withPoisson], [nTraj], [dose], [sf], [bf], [substrate], [xtraParams])
+ multiFilm(layers, [det], [e0], [withPoisson], [nTraj], [dose], [sf], [bf], [xtraParams])
+ embeddedSphere(mat, radius, substrate, depth, [det], [e0], [withPoisson], [nTraj], [dose], [sf], [bf], [xtraParams])
+ embeddedRectangle(mat, dims, substrate, depth, [det], [e0], [withPoisson], [nTraj], [dose], [sf], [bf], [xtraParams]):
+ interface(primary, offset, secondary, [det], [e0], [withPoisson], [nTraj], [dose], [sf], [bf], [xtraParams])
Internal methods: (for special customization)
+ base(det, e0, withPoisson, nTraj, dose, sf, bf, name, buildSample, buildParams, xtraParams)
xtraParams:
    xtraParams is a mechanism to allow for flexible yet efficient generation of alternative output mechanisms such as emission images, accumulators and phi(rho*z) curves. \
Use the helper functions configureXXX to build xtraParams.
Example:
> xrts=suggestTransitions("SrF2")
> xtraParams={}
> xtraParams.update(configureXRayAccumulators(xrts,charAccum=True,charFluorAccum=True,bremFluorAccum=False))
> xtraParams.update(configureEmissionImages(xrts,1.0e-5,512))
> xtraParams.update(configureContinuumImages( ((2.3,2.5), (4.2,4.4 )), 1.0e-5, 512 ))
> xtraParams.update(configurePhiRhoZ(1.0e-5))
> xtraParams.update(configureTrajectoryImage(1.0e-5,512))
> xtraParams.update(configureVariablePressure(pathLen, gas))
> xtraParams.configureVRML(nElectrons = 40)
The default output path for all files created is the same as 'reportPath()'.  You can specify an alternative location using 'configureOutput(...)'. """
from compiler.pycodegen import TRY_FINALLY
    
# Example:
# 1> xp = { "Transitions" : [transition("Fe K-L3"), transition("Fe K-M3"), transition("Fe L3-M5"), transition("O K-L3")], "Emission Images":5.0e-6, "Characteristic Accumulator":True, "Char Fluor Accumulator":True, "Brem Fluor Accumulator":True, "PhiRhoZ":5.0e-6, "Output" : "Z:/nritchie/Desktop/tmp" }
# 2> display(simulate(material("Fe2O3",5.0), d2, 20.0, nTraj=100, sf=True, bf=True, xtraParams = xp))

# Mac OS X seems to require the next line.

__revision__ = "$Id: mcSimulate3.py Nicholas W. M. Ritchie. Mod JRM $"
__version__ = "0.1.0 - 2023-07-05"

import sys
sys.packageManager.makeJavaPackage("gov.nist.microanalysis.NISTMonte.Gen3", "CharacteristicXRayGeneration3, BremsstrahlungXRayGeneration3, FluorescenceXRayGeneration3, XRayTransport3", None)
import gov.nist.microanalysis.NISTMonte as nm
import gov.nist.microanalysis.NISTMonte.Gen3 as nm3
import gov.nist.microanalysis.EPQLibrary as epq
import gov.nist.microanalysis.EPQLibrary.Detector as epd
import gov.nist.microanalysis.Utility as epu
import gov.nist.microanalysis.EPQTools as ept
import javax.imageio.ImageIO as iio
import java.io as jio
import java.lang as jl
import dtsa2

if 'defaultXtraParams' not in globals():
    defaultXtraParams = {}
if 'defaultBremFluor' not in globals():
    defaultBremFluor = False
if 'defaultCharFluor' not in globals():
    defaultCharFluor = False
if 'defaultNumTraj' not in globals():
    defaultNumTraj = 1000
if 'defaultDose' not in globals():
    defaultDose = 120.0

def configureOutput(path):
    """configureOutput(path)
    Set the directory into which results like images and text output files are written."""
    return {"Output" : path }

def configureXRayAccumulators(xrts, charAccum=True, charFluorAccum=False, bremFluorAccum=False, printRes=False):
    """configureXRayAccumulators(xrts, charAccum = True, charFluorAccum = False, bremFluorAccum = False, printRes=False)
    Configures the x-ray accumulators for characteristic and both bremsstrahlung and characteristic secondary fluorescences. /
    If printRes=True then the results are also displayed in the command window"""
    res = { 'Transitions' : xrts }
    res['Characteristic Accumulator'] = charAccum
    res['Char Fluor Accumulator'] = charFluorAccum
    res['Brem Fluor Accumulator'] = bremFluorAccum
    res['Print Accumulators'] = printRes
    return res

def configureEmissionImages(xrts, dim, size=512):
    """configureEmissionImages(xrts, dim, size =  512)
    Configure emission images for the specified XRayTransition or XRayTransitionSet objects."""
    xrts2 = []
    for xrt in xrts:
        xrts2.append(dtsa2.transition(xrt))
    return { 'Transitions' : xrts2, 'Emission Images' : dim, 'Emission Size' : size}

def configureContinuumImages(energies, dim, size=512):
    """configureContinuumImages( energies, dim, size=512)
    Configure continuum images for the specified energy ranges ( ( e0min, e0max), (e1min, e1max),... )"""
    return { 'Continuum Energies' : energies, 'Continuum Images' : dim, 'Continuum Size' : size }

def configurePhiRhoZ(depth):
    """configurePhiRhoZ(depth)
    Add a phi(rho*z) detector with the specified z dimension in meters."""
    return { 'PhiRhoZ' : depth }

def configureTrajectoryImage(dim, size=512, nTraj=100):
    """configureTrajectoryImage(dim, size=512, nTraj=100)
    Configure trajectory images of the specified dim in meters and size in pixels."""
    return { 'Trajectories' : dim, 'TrajSize' : 512, 'TrajCount':nTraj }

def configurePostfix(post):
    """configurePostfix(post)
    Configure a string that will be appended onto the name of the spectrum and other result attributes."""
    return {'Postfix' : post }

def useGrayScalePalette():
    """useGrayScalePalette()
    Configure DTSA-II to use a gray-scale (rather than heat-map) color palette"""
    nm3.EmissionImageBase.useGrayScalePalette()
    
def useHeatMapPalette():
    """useHeatMapPalette()
    Configure DTSA-II to use a heat-map color palette (default)"""
    nm3.EmissionImageBase.useHeatMapPalette()

def configureVoxelated(dim=20, size=2.e-6, generated=True):
    return { 'Voxelated' : dim, 'GeneratedV' : generated, 'SizeV' : size }

def configureVRML(nElectrons=40):
    return { 'VRML' :  nElectrons }

def toPascal(torr):
    return torr * 133.32237

def toTorr(pascal):
    return pascal / 133.32237

if 'defaultGas' not in globals():
    defaultGas = epq.Gas((epq.Element.H, epq.Element.O,), (2, 1,), toPascal(0.1), 300.0, "Water vapor")

def createMonatomicGas(elm, pascal):
    """createMonatomicGas(elm, pascal)
    Create a gas of single atoms of the specified element at the specified pressure in Pascal and 300 K"""
    return epq.Gas((elm,), (1,), pascal, 300.0, elm.toString() + " gas at %f Pa" % pascal)

def createGas(comp, pascal):
    """createGas(comp, pascal)
    Create a gas from a composition at the specified pressure in Pascal at 300 K.
    Ex: createGas("H2O", 0.1)"""
    return epq.Gas(dtsa2.material(comp), pascal, 300.0)

def configureVariablePressure(pathLength, gas=defaultGas):
    return { 'VP' : (pathLength, gas) }

def suggestTransitionSets(mat, e0=20.0):
    """suggestTransitions(mat, e0=20.0)
    Suggest a list of XRayTransitionSet objects for the specified material."""
    mat = dtsa2.material(mat)
    fams = ("Ka", "Kb", "La", "Lb", "Ma", "Mb")    
    res = []
    for elm in mat.getElementSet():
        for fam in fams:
            xrts = dtsa2.getTransitionSet(elm, fam)
            removeMe = epq.XRayTransitionSet()
            if (xrts.size() > 0):
                for xrt in xrts:
                    ee = epq.FromSI.keV(xrt.getEnergy())
                    if (ee < 0.2) or (ee > 0.95 * e0):
                        removeMe.add(xrt)
            if removeMe.size() > 0:
                xrts.removeAll(removeMe)
            if xrts.size() > 0:
                res.append(xrts)
    return res

def configureGun(gun):
    return { "Gun" : gun }
    
def fixColors(materials):
	return { "Colors": materials }

def configureBeam(x, y, z, szNm):
   """configureBeam(x,y,z, szNm)
   Create xtraParams entries to configure the beam for the simulation.
   Input:
   x, y, z - positions of the beam in meters
   szNm    - the beam diameter in nm (converted to m internally)"""
   return { "PosX" : x, "PosY" : y, "PosZ": z, "nmSize": szNm}

def suggestTransitions(mat, e0=20.0):
    """suggestTransitions(mat, e0=20.0)
    Suggest a list of transitions for the specified material."""
    mat = dtsa2.material(mat)
    xrts = ("%s K-L3", "%s K-M3", "%s L3-M5", "%s L2-N4", "%s M5-N7", "%s M4-N6")
    res = []
    for elm in mat.getElementSet():
        for xrt in xrts:
            try:
                tr = dtsa2.transition(xrt % elm.toAbbrev())
                if tr.exists() and (tr.getEnergy() < 0.95 * epq.ToSI.keV(e0)) and (tr.getEnergy() > epq.ToSI.keV(0.2)):
                    res.append(tr)
            except:
                pass
                # print("%s does not exist" % xrt) # Ignore
    return res

def configureBSEDDepth(zMin, zMax):
    return { "BSEDDepth" : (zMin, zMax) }


def estimateRange(mat, e0):
    """estimateRange(mat, e0)
    Estimate the electron range in the specified material at the specified beam energy (keV)"""
    return epq.ElectronRange.KanayaAndOkayama1972.compute(mat, epq.ToSI.keV(e0)) / mat.getDensity()

def base(det, e0, withPoisson, nTraj, dose, sf, bf, name, buildSample, buildParams, xtraParams):
    """base(det, e0, withPoisson, nTraj, dose, sf, bf, name, buildSample, buildParams) represents \
    a generic mechanism for Monte Carlo simulation of x-ray spectra.  The argument buildSample \
    is a method buildSample(monte,origin,buildParams) taking an instance of MonteCarloSS, the \
    position of the origin and a dictionary of build parameters.  This method should construct \
    the sample geometry.  The other arguments are the detector, the beam energy (keV), whether \
    to add Poisson noise, the number of electron trajectories to simulate, whether to simulate \
    characteristic secondary fluorescence and Bremsstrahlung secondary fluorescence, the name \
    to assign to the resulting spectrum."""
    if e0 < 0.1:
        raise "The beam energy must be larger than 0.1 keV."
    if nTraj < 1:
        raise "The number of electron trajectories must be larger than or equal to 1."
    if dose <= 0.0:
        raise "The electron dose must be larger than zero."
    name = name.strip()
    if xtraParams.has_key("Postfix"):
        name = "%s - %s" % (name, xtraParams["Postfix"])
    # Place the sample at the optimal location for the detector
    origin = epq.SpectrumUtils.getSamplePosition(det.getProperties())
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

    xrts = None
    if xtraParams.has_key("Transitions"):
        xrts = xtraParams["Transitions"]

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
    # Add event listeners to model characteristic radiation
    chXR = nm3.CharacteristicXRayGeneration3.create(monte)
    xrel = nm3.XRayTransport3.create(monte, det, chXR)
    brXR = nm3.BremsstrahlungXRayGeneration3.create(monte)
    brem = nm3.XRayTransport3.create(monte, det, brXR)
    chSF, brSF, bremFluor, charFluor = None, None, None, None
    hasCharAcc = xtraParams.has_key('Characteristic Accumulator') and xtraParams['Characteristic Accumulator']
    if sf or hasCharAcc or xtraParams.has_key("Compton"):
        charFluor = nm3.FluorescenceXRayGeneration3.create(monte, chXR)
        if xtraParams.has_key("Compton"):
            charFluor.setIncludeCompton(True)
        chSF = nm3.XRayTransport3.create(monte, det, charFluor)
    hasBremFluorAcc = xtraParams.has_key('Brem Fluor Accumulator') and xtraParams['Brem Fluor Accumulator']
    hasTrans = None
    if bf or hasBremFluorAcc:
        bremFluor = nm3.FluorescenceXRayGeneration3.create(monte, brXR)
        brSF = nm3.XRayTransport3.create(monte, det, bremFluor)
    hasTrans = (xrts!=None)
    if hasTrans:
        if xtraParams.has_key("Emission Images"):
            eis = []
            dim = xtraParams["Emission Images"]
            for xrt in xrts:
                size = xtraParams["Emission Size"]
                ei = nm3.EmissionImage3(size, size, xrt)
                xrel.addXRayListener(ei)
                if chSF:
                    chSF.addXRayListener(ei)
                if brSF:
                    brSF.addXRayListener(ei)
                ei.setXRange(origin[0] - 0.5 * dim, origin[0] + 0.5 * dim)
                ei.setYRange(origin[2] - 0.1 * dim, origin[2] + 0.9 * dim)
                eis.append(ei)
        if hasCharAcc:
            cxra = nm3.XRayAccumulator3(xrts, "Characteristic", dose * 1.0e-9)
            xrel.addXRayListener(cxra)
        hasCharFluorAcc = xtraParams.has_key('Char Fluor Accumulator') and xtraParams['Char Fluor Accumulator']
        if hasCharFluorAcc or chSF or sf:
            cfxra = nm3.XRayAccumulator3(xrts, "Characteristic Fluorescence", dose * 1.0e-9)
            chSF.addXRayListener(cfxra) 
        if hasBremFluorAcc or brSF or bf:
            bfxra = nm3.XRayAccumulator3(xrts, "Continuum Fluorescence", dose * 1.0e-9)
            brSF.addXRayListener(bfxra)
    contImgs = []
    if xtraParams.has_key('Continuum Images'):
        dim = xtraParams['Continuum Images']
        size = xtraParams['Continuum Size']
        energies = xtraParams['Continuum Energies']
        for eMin, eMax in energies:
            ci3 = nm3.ContinuumImage3(size, size, epq.ToSI.keV(eMin), epq.ToSI.keV(eMax))
            ci3.setXRange(origin[0] - 0.5 * dim, origin[0] + 0.5 * dim)
            ci3.setYRange(origin[2] - 0.1 * dim, origin[2] + 0.9 * dim)
            brem.addXRayListener(ci3)
            contImgs.append(ci3)
    doPRZ = xtraParams.has_key("PhiRhoZ")
    if doPRZ:
        depth = xtraParams["PhiRhoZ"]
        prz = nm3.PhiRhoZ3(xrel, origin[2] - 0.1 * depth, origin[2] + 1.1 * depth, 110)
        xrel.addXRayListener(prz)
    voxelated = xtraParams.has_key('Voxelated')
    vox = None
    if voxelated:
        dim = xtraParams['Voxelated']
        gen = xtraParams['GeneratedV']
        size = xtraParams['SizeV']
        vox = nm3.VoxelatedDetector((origin[0], origin[1], origin[2] - 0.1 * size), (size, size, size), (dim, dim, dim), gen)
        for xrt in xrts:
            vox.add(xrt)
        xrel.addXRayListener(vox)
    bsedDepth = xtraParams.has_key("BSEDDepth")
    bDep = None
    if bsedDepth:
        (zDepthMin, zDepthMax)  = xtraParams["BSEDDepth"]
        bDep = nm.BSEDDepthDetector(100, zDepthMin, zDepthMax, origin[2])
        monte.addActionListener(bDep)
    doTraj = xtraParams.has_key('Trajectories')
    if doTraj:
        dim = xtraParams['Trajectories']
        size = xtraParams['TrajSize']
        ti = nm.TrajectoryImage(size, size, dim)
        ti.setXRange(origin[0] - 0.5 * dim, origin[0] + 0.5 * dim)
        ti.setYRange(origin[2] - 0.1 * dim, origin[2] + 0.9 * dim)
        ti.setMaxTrajectories(xtraParams['TrajCount'])
        # Fix colors
        if xtraParams.has_key("Colors"):
        	for mat in xtraParams["Colors"]:
        		ti.assignColor(mat)
        monte.addActionListener(ti)
    defOut = (dtsa2.DefaultOutput if dtsa2.DefaultOutput else dtsa2.reportPath())
    do = ("%s\\%s" % (xtraParams["Output"], dtsa2.normalizeFilename(name)) if xtraParams.has_key("Output") else "%s/%s" % (defOut, dtsa2.normalizeFilename(name)))
    if xtraParams.has_key('Index'):
        do = do + "[%s]" % (str(xtraParams['Index']),)   
    do = do.replace("\\", "/")
    fdo = jio.File(do)
    fdo.mkdirs()
    doVRML = xtraParams.has_key('VRML')
    vrmlWr = None
    if doVRML:
        vrmlFile = jio.File.createTempFile("vrml", ".wrl", fdo)
        print "VRML in " + str(vrmlFile)
        vrmlWr = jio.FileWriter(vrmlFile)
        vrml = nm.TrajectoryVRML(monte, vrmlWr)
        vrml.setDisplayBackscatter(False)
        vrml.setDisplayXRayEvent(True)
        vrml.setMaxTrajectories(xtraParams['VRML'])
        vrml.setTrajectoryWidth(1.0e-9)
        vrml.setMaxRadius(1.0)
        vrml.setEmissive(True)
        vrml.addView("Y-Axis", epu.Math2.plus(origin, (0.0, 5.0e-6, 0.0)), origin)
        vrml.addView("Gun", epu.Math2.plus(origin, (0.0, 0.0, -5.0e-6)), origin)
        vrml.addView("X-Axis", epu.Math2.plus(origin, (-5.0e-6, 0.0, 0.0)), origin)
        vrml.renderSample()
        monte.addActionListener(vrml)
    scatter = None
    if xtraParams.has_key("Scatter"):
        scatter = nm.ScatterStats(epq.ToSI.eV(50.0))
        monte.addActionListener(scatter)
    # Reset the detector and run the electrons
    det.reset()
    monte.runMultipleTrajectories(nTraj)
    # Get the spectrum and assign properties
    spec = det.getSpectrum((dose * 1.0e-9) / (nTraj * epq.PhysicalConstants.ElectronCharge))
    props = spec.getProperties()
    props.setNumericProperty(epq.SpectrumProperties.LiveTime, dose)
    props.setNumericProperty(epq.SpectrumProperties.ProbeCurrent, 1.0)
    props.setNumericProperty(epq.SpectrumProperties.BeamEnergy, e0)
    epq.SpectrumUtils.rename(spec, name)
    if withPoisson:
        spec = epq.SpectrumUtils.addNoiseToSpectrum(spec, 1.0)
    printAcc = xtraParams.has_key('Print Accumulators') and xtraParams['Print Accumulators']
    if printAcc:
        sw0 = jio.StringWriter()
        sw = jio.PrintWriter(sw0)
    if hasTrans or scatter:
        pw = None
        if hasCharAcc or (hasBremFluorAcc and bf) or (hasCharFluorAcc and sf) or scatter:
            jio.File(do).mkdirs()
            pw = jio.PrintWriter("%s/Intensity.csv" % do)
            pw.println(name)
        if hasCharAcc:
            pw.println("Characteristic") 
            cxra.dump(pw)
            if printAcc:
                sw.println("Characteristic") 
                cxra.dump(sw)
        if hasBremFluorAcc and brSF and bf:
            pw.println("Bremsstrahlung Fluorescence")
            bfxra.dump(pw)
            if printAcc:
                sw.println("Bremsstrahlung Fluorescence") 
                bfxra.dump(sw)
        if hasCharFluorAcc and chSF and sf:
            pw.println("Characteristic Fluorescence")
            cfxra.dump(pw)
            if printAcc:
                sw.println("Characteristic Fluorescence")
                cfxra.dump(sw)
        if printAcc:
            print sw0.toString()
            sw.close()
            sw0.close()
        if scatter:
            scatter.header(pw)
            scatter.dump(pw)
        if pw:
            pw.close()
        imgs = []
        if xtraParams.has_key("Emission Images"):
            nm3.EmissionImageBase.scaleEmissionImages(eis)
            print eis
            print do
            nm3.EmissionImage3.dumpToFiles(eis, do)
            print u"Writing emission images to %s" % do
            imgs.extend(eis)
        if xtraParams.has_key("Continuum Images"):
            imgs.extend(contImgs)
            nm3.EmissionImageBase.scaleEmissionImages(imgs)
            print contImgs
            print do
            nm3.ContinuumImage3.dumpToFiles(contImgs, do)
            print u"Writing continuum images to %s" % do
    if doPRZ:
        jio.File(do).mkdirs()
        pw = jio.PrintWriter(u"%s/PhiRhoZ.csv" % do)
        prz.write(pw)
        pw.close()
        print u"Writing emission images to %s" % do
    if doTraj:
        ti.dumpToFile(do)
        print u"Writing trajectory images to %s" % do
    if vrmlWr:
        vrmlWr.close()
    if bDep:
        print u"Backscatter Depth Histogram"
        print bDep.getHistogram().dump("{0,number,0.00E0}");
    if vox:
        jio.File(do).mkdirs()
        objs = list(vox.getAccumulatorObjects())
        xx = {}
        for obj in objs:
            iio.write(vox.createXZSum(400, obj), "png", jio.File(do, "Voxelated[XZ,Sum][%s].png" % obj))
            iio.write(vox.createXYSum(400, obj), "png", jio.File(do, "Voxelated[XY,Sum][%s].png" % obj))
            iio.write(vox.createXZView(400, obj), "png", jio.File(do, "Voxelated[XZ, Max][%s].png" % obj))
            iio.write(vox.createXYView(400, obj), "png", jio.File(do, "Voxelated[XY, Max][%s].png" % obj))
            vox.writeXZPlanar(400, obj, jio.File(do, "Voxilated[XZ,planar,%s].tif" % obj))
            vox.writeXYPlanar(400, obj, jio.File(do, "Voxilated[XY,planar,%s].tif" % obj))
            for f in (0.1, 0.5, 0.8, 0.9):
                iio.write(vox.createXZFraction(400, obj, f), "png", jio.File(do, "Voxelated[XZ,f=%g][%s].png" % (f, obj)))
            xx[obj] = vox.createRadialCDF(origin, obj)
        hdr = "Radius"
        for obj in objs:
            hdr = "%s\t%s" % (hdr, obj)
        print hdr
        first = xx[objs[0]]
        for i, (d, f) in enumerate(first):
            ln = "%g" % d
            for obj in objs:
                rcdf = xx[obj][i]
                ln = "%s\t%g" % (ln, rcdf[1])
            print ln
    # if bremFluor:
        # print "Stats[Scale] = %s" % bremFluor.getScaleStats()
    return dtsa2.wrap(spec)

def buildBulk(monte, chamber, origin, buildParams):
    mat = buildParams["Material"]
    monte.addSubRegion(chamber, mat, nm.MultiPlaneShape.createSubstrate([0.0, 0.0, -1.0], origin))

def simulate(mat, det, e0=20.0, dose=defaultDose, withPoisson=True, nTraj=defaultNumTraj, sf=defaultCharFluor, bf=defaultBremFluor, xtraParams=defaultXtraParams):
    """simulate(mat,det,[e0=20.0],[withPoisson=True],[nTraj=defaultNumTraj],[dose=defaultDose],[sf=defaultCharFluor],[bf=defaultBremFluor],[xtraParams=defaultXtraParams])
    Simulate a bulk spectrum for the material mat on the detector det at beam energy e0 (in keV).  If \
    sf then simulate characteristic secondary fluorescence. If bf then simulate bremsstrahlung secondary \
    fluorescence. nTraj specifies the number of electron trajectories. dose is in nA*sec."""
    mat = dtsa2.material(mat)
    if not isinstance(mat, epq.Material):
        print u"Please provide a material with a density - %s" % mat
    tmp = u"MC simulation of bulk %s at %0.1f keV%s%s" % (mat, e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    print tmp
    res = base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildBulk, { "Material" : mat }, xtraParams)
    res.getProperties().setCompositionProperty(epq.SpectrumProperties.StandardComposition, mat)
    return res

def buildCoatedBlock(monte, chamber, origin, buildParams):
    height = buildParams["Height"]
    width = buildParams["Width"]
    subMat = buildParams["Substrate"]
    mat = buildParams["Material"]
    coating = buildParams["Coating"]
    thickness = buildParams["Thickness"]
    coatedCube = nm.MultiPlaneShape.createBlock([width + 2.0 * thickness, width + 2.0 * thickness, height + thickness], epu.Math2.plus(origin, [0.0, 0.0, 0.5 * height + thickness]), 0.0, 0.0, 0.0)
    sr1 = monte.addSubRegion(chamber, coating, coatedCube)
    cube = nm.MultiPlaneShape.createBlock([width, width, height], epu.Math2.plus(origin, [0.0, 0.0, thickness + 0.5 * height]), 0.0, 0.0, 0.0)
    monte.addSubRegion(sr1, mat, cube)
    monte.addSubRegion(chamber, coating, nm.MultiPlaneShape.createFilm([0.0, 0.0, -1.0], epu.Math2.plus(origin, [0.0, 0.0, height + thickness]), thickness))
    monte.addSubRegion(chamber, subMat, nm.MultiPlaneShape.createSubstrate([0.0, 0.0, -1.0], epu.Math2.plus(origin, [0.0, 0.0, height + 2.0 * thickness])))

def coatedBlock(mat, height, width, coating, thickness, substrate, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, xtraParams=defaultXtraParams):    
    """coatedBlock(mat, height, width, coating, thickness, substrate, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, substrate=None, xtraParams={})
    Monte Carlo simulate a spectrum from a block shaped particle of the specified material (mat) and height (z in m) and width (x and y in m). \
    The block and subtrate is coated in a material 'coating' of the specified thickness which fully encapsulates the particle and covers the substrate too."""
    tmp = u"MC simulation of a [%0.2f,%0.2f,%0.2f] micron block of %s%s coated with %s at %0.1f keV%s%s" % (width * 1.0e6, width * 1.0e6, height * 1.0e6, mat, (" on %s" % substrate if substrate else ""), coating, e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    params = {"Substrate": substrate, "Width" : width, "Height" : height, "Material" : mat, "Coating" : coating, "Thickness" : thickness}
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildCoatedBlock, params, xtraParams)

def buildBlock(monte, chamber, origin, buildParams):
    height = buildParams["Height"]
    width = buildParams["Width"]
    subMat = buildParams["Substrate"]
    mat = buildParams["Material"]
    cube = nm.MultiPlaneShape.createBlock([width, width, height], epu.Math2.plus(origin, [0.0, 0.0, 0.5 * height]), 0.0, 0.0, 0.0)
    monte.addSubRegion(chamber, mat, cube)
    if subMat:
        monte.addSubRegion(chamber, subMat, nm.MultiPlaneShape.createSubstrate([0.0, 0.0, -1.0], epu.Math2.plus(origin, [0.0, 0.0, height])))

def block(mat, height, width, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, substrate=None, xtraParams=defaultXtraParams):    
    """block(mat, height, width, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, substrate=None, xtraParams={})
    Monte Carlo simulate a spectrum from a block shaped particle of the specified material (mat) and height (z in m) and width (x and y in m). \
    If substrate != None then substrate specifies the Material for an infinitely thick substrate immediately \
    below the particle."""
    tmp = u"MC simulation of a [%0.2f,%0.2f,%0.2f] micron block of %s%s at %0.1f keV%s%s" % (width * 1.0e6, width * 1.0e6, height * 1.0e6, mat, (" on %s" % substrate if substrate else ""), e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildBlock, {"Substrate": substrate, "Width" : width, "Height" : height, "Material" : mat}, xtraParams)

def buildSlab(monte, chamber, origin, buildParams):
    thick = buildParams["Thick"]
    width = buildParams["Width"]
    length = buildParams["Length"]
    subMat = buildParams["Substrate"]
    mat = buildParams["Material"]
    cube = nm.MultiPlaneShape.createBlock([width, length, thick],
                                          epu.Math2.plus(origin,
                                          [0.0, 0.0, 0.5 * thick]),
                                          0.0, 0.0, 0.0)
    monte.addSubRegion(chamber, mat, cube)
    if subMat:
        monte.addSubRegion(chamber, subMat, nm.MultiPlaneShape.createSubstrate([0.0, 0.0, -1.0], epu.Math2.plus(origin, [0.0, 0.0, thick])))


def slab(mat, thick, width, length, det, e0=20.0, withPoisson=True,
         nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor,
         bf=defaultBremFluor, substrate=None,
         xtraParams=defaultXtraParams):
    """
    slab(mat, thick, width, length, det, e0=20.0, withPoisson=True,
         nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor,
         bf=defaultBremFluor, substrate=None,
         xtraParams=defaultXtraParams)

    Monte Carlo simulate a spectrum from a slab shaped particle of the
    specified material (mat) and thick (z in m) and width (x in m) and
    lenght (in m).

    If substrate != None then substrate specifies the Material for an
    infinitely thick substrate immediately below the slab.
    """
    tmp = u"MC simulation of a [%0.2f,%0.2f,%0.2f] micron slab of %s%s at %0.1f keV%s%s" % (width * 1.0e6, length * 1.0e6, thick * 1.0e6, mat, (" on %s" % substrate if substrate else ""), e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildSlab, {"Substrate": substrate, "Width" : width, "Length" : length, "Thick" : thick, "Material" : mat}, xtraParams)


def buildCoatedSubstrate(monte, chamber, origin, buildParams):
    sc = 1.0e-6
    subMat = buildParams["Substrate"]
    coating = buildParams["Coating"]
    thickness = buildParams["Thickness"]
    monte.addSubRegion(chamber, coating,
                       nm.MultiPlaneShape.createFilm([0.0, 0.0, -1.0],
                       epu.Math2.plus(origin, [0.0, 0.0, sc * thickness]),
                       sc * thickness))
    monte.addSubRegion(chamber, subMat,
        nm.MultiPlaneShape.createSubstrate([0.0, 0.0, -1.0],
        epu.Math2.plus(origin, [0.0, 0.0, 2.0 * sc * thickness])))

def coatedSubstrate(coating, thickness, substrate, det, e0=20.0,
                    withPoisson=True, nTraj=defaultNumTraj,
                    dose=defaultDose, sf=defaultCharFluor,
                    bf=defaultBremFluor, xtraParams=defaultXtraParams):
    """coatedSubstrate(coating, thickness, substrate, det, e0=20.0,
                       withPoisson=True, nTraj=defaultNumTraj,
                       dose=defaultDose, sf=defaultCharFluor,
                       bf=defaultBremFluor, xtraParams=defaultXtraParams)

    Simulate a spectrum of a coating on a substrate.
    Monte Carlo simulate a spectrum from a 'substrate' material coated
    with 'coating' of the specified thickness (in microns)."""
    fSt = u"MC simulation of %s coated with %g microns of %s at %0.1f keV%s%s"
    tmp = fSt % (substrate , thickness, coating, e0,
                   (" + CSF" if sf else ""),
                   (" + BSF" if bf else "")
                  )
    params = {"Substrate" : substrate,
              "Coating"   : coating,
              "Thickness" : thickness }
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildCoatedSubstrate, params, xtraParams)

def buildMultiBlocks(monte, chamber, origin, buildParams):
    blocks = buildParams["Blocks"]
    for block in blocks:
        cube = nm.MultiPlaneShape.createBlock(block[0], epu.Math2.plus(origin, block[1]), 0.0, 0.0, 0.0)
        monte.addSubRegion(chamber, block[2], cube)


def multiblock(blocks, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, xtraParams=defaultXtraParams):
    """multiblock(blocks, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, substrate=None, xtraParams={})
    Monte Carlo simulate a spectrum from a collection of blocks.  Each block is defined by a triplet = ( dims[3], offset[3], mat ) where \
    dims is the block dimension as three lengths in meters, offset is the center of the block as a position in meters and mat is the material."""
    tmp = u"MC simulation of %d blocks at %0.1f keV%s%s" % (len(blocks), e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildMultiBlocks, {"Blocks": blocks }, xtraParams)

def buildSphere(monte, chamber, origin, buildParams):
    radius = buildParams["Radius"]
    subMat = buildParams["Substrate"]
    mat = buildParams["Material"]
    sphere = nm.Sphere(epu.Math2.plus(origin, [0.0, 0.0, radius]), radius)
    monte.addSubRegion(chamber, mat, sphere)
    if subMat:
        monte.addSubRegion(chamber, subMat, nm.MultiPlaneShape.createSubstrate([0.0, 0.0, -1.0], epu.Math2.plus(origin, [0.0, 0.0, 2.0 * radius])))

    
def sphere(mat, radius, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, substrate=None, xtraParams=defaultXtraParams):
    """sphere(mat, radius, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, substrate=None, xtraParams={})
    Monte Carlo simulate a spectrum from a spherical particle of the specified material (mat) and radius (in m). \
    If substrate != None then substrate specifies the Material for an infinitely thick substrate immediately \
    below the particle."""
    if radius < 0.0:
        raise "The sphere radius must be larger than zero."
    tmp = u"MC simulation of a %0.2f micron radius sphere of %s%s at %0.1f keV%s%s" % (radius * 1.0e6, mat, (" on %s" % substrate if substrate else ""), e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildSphere, {"Substrate": substrate, "Radius" : radius, "Material" : mat}, xtraParams)

def buildCoatedSphere(monte, chamber, origin, buildParams):
    radius = buildParams["Radius"]
    subMat = buildParams["Substrate"]
    mat = buildParams["Material"]
    coating = buildParams["Coating"]
    thickness = buildParams["Thickness"]
    coatSphere = nm.Sphere(epu.Math2.plus(origin, [0.0, 0.0, radius + thickness]), radius + thickness)
    srC = monte.addSubRegion(chamber, coating, coatSphere)
    sphere = nm.Sphere(epu.Math2.plus(origin, [0.0, 0.0, radius + thickness]), radius)
    monte.addSubRegion(srC, mat, sphere)
    if subMat:
        monte.addSubRegion(chamber, subMat, nm.MultiPlaneShape.createSubstrate([0.0, 0.0, -1.0], epu.Math2.plus(origin, [0.0, 0.0, 2.0 * radius])))


def coatedSphere(mat, radius, coating, thickness, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, substrate=None, xtraParams=defaultXtraParams):
    """sphere(mat, radius, coating, thickness, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, substrate=None, xtraParams={})
    Monte Carlo simulate a spectrum from a spherical particle of the specified material (mat) and radius (in m). \
    If substrate != None then substrate specifies the Material for an infinitely thick substrate immediately \
    below the particle."""
    if radius < 0.0:
        raise "The sphere radius must be larger than zero."
    if thickness < 0.0:
        raise "The coating thickness must be larger than zero."
    tmp = u"MC simulation of a %0.2f micron sphere of %s coated with %0.2f microns of %s%s at %0.1f keV%s%s" % (radius * 1.0e6, mat, thickness * 1.0e6, coating, (" on %s" % substrate if substrate else ""), e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildCoatedSphere, {"Substrate": substrate, "Radius" : radius, "Material" : mat, "Coating" : coating, "Thickness" : thickness}, xtraParams)

def buildFilm(monte, chamber, origin, buildParams):
    sr = chamber
    pos = origin
    for (mat, thickness,) in buildParams["Layers"]:
        if thickness <= 0.0:
            raise "The layer thickness must be larger than zero."
        monte.addSubRegion(sr, mat, nm.MultiPlaneShape.createFilm([0.0, 0.0, -1.0], pos, thickness))
        pos = epu.Math2.plus(pos, [0.0, 0.0, thickness + 1.0e-12])


def multiFilm(layers, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, xtraParams=defaultXtraParams):
    """multiFilm(layers, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, xtraParams={}):
    Monte Carlo simulate a spectrum from a multilayer thin film.  Layers is a iterable list of \ 
    [material,thickness]. Note the materials must have associated densities."""
    tmp = u"MC simulation of a multilayer film [%s] at %0.1f keV%s%s" % (",".join("%0.0f nm of %s" % (1.0e9 * layer[1], layer[0]) for layer in layers), e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildFilm, {"Layers": layers }, xtraParams)

def buildEmbeddedSphere(monte, chamber, origin, buildParams):
    mat = buildParams["Material"]
    radius = buildParams["Radius"]
    subMat = buildParams["Substrate"]
    depth = buildParams["Depth"]
    sr = monte.addSubRegion(chamber, subMat, nm.MultiPlaneShape.createSubstrate([0.0, 0.0, -1.0], origin))
    monte.addSubRegion(sr, mat, nm.Sphere(epu.Math2.plus(origin, [0.0, 0.0, depth + radius]), radius))
        
def embeddedSphere(mat, radius, substrate, depth, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, xtraParams=defaultXtraParams):
    """embeddedSphere(mat, radius, substrate, depth, det, [e0=20.0], [withPoisson=True], [nTraj=defaultNumTraj], [dose = 120.0], [sf=defaultCharFluor], [bf=defaultBremFluor], [substrate=None])
    Monte Carlo simulate a spectrum from a spherical particle of the specified material (mat) and radius (in m) embedded in a substrate (Material) at depth (in m)."""
    if depth < 0.0:
        raise "The depth parameter must be greater than zero."
    if radius < 0.0:
        raise "The sphere radius must be larger than zero."
    tmp = u"MC simulation of a %0.2f micron sphere of %s embedded %0.2f microns in %s at %0.1f keV%s%s" % (radius * 1.0e6, mat, depth * 1.0e6, substrate, e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildEmbeddedSphere, {"Substrate": substrate, "Radius" : radius, "Depth" : depth, "Material" : mat}, xtraParams)


def buildEmbeddedRectange(monte, chamber, origin, buildParams):
    mat = buildParams["Material"]
    subMat = buildParams["Substrate"]
    dims = buildParams["Dimension"]
    depth = buildParams["Depth"]
    sr = monte.addSubRegion(chamber, subMat, nm.MultiPlaneShape.createSubstrate([0.0, 0.0, -1.0], origin))
    pos = epu.Math2.plus(origin, [0.0, 0.0, 0.5 * dims[2] + depth])
    monte.addSubRegion(sr, mat, nm.MultiPlaneShape.createBlock(dims, pos, 0.0, 0.0, 0.0))

def embeddedRectangle(mat, dims, substrate, depth, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, xtraParams=defaultXtraParams):
    """embeddedRectangle(mat, dims, substrate, depth, det, [e0=20.0], [withPoisson=True], [nTraj=defaultNumTraj], [dose = 120.0], [sf=defaultCharFluor], [bf=defaultBremFluor])"""
    if isinstance(dims, float):
        dims = [dims, dims, dims]
    tmp = u"MC simulation of a [%0.2f microns,%0.2f microns,%0.2f microns] block of %s embedded %0.2f microns in %s at %0.1f keV%s%s" % (dims[0] * 1.0e6, dims[1] * 1.0e6, dims[2] * 1.0e6, mat, depth * 1.0e6, substrate, e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildEmbeddedRectange, {"Substrate": substrate, "Dimension" : dims, "Depth" : depth, "Material" : mat}, xtraParams)


def buildEmbeddedCylinder(monte, chamber, origin, buildParams):
    mat = buildParams["Material"]
    subMat = buildParams["Substrate"]
    radius = buildParams["Radius"]
    depth = buildParams["Depth"]
    if subMat:
        sr = monte.addSubRegion(chamber, subMat, nm.MultiPlaneShape.createSubstrate([0.0, 0.0, -1.0], origin))
    else:
        sr = chamber
    end1 = epu.Math2.plus(origin, [0.0, 0.0, depth])
    cyl = nm.CylindricalShape(origin, end1, radius);
    monte.addSubRegion(sr, mat, cyl)


def embeddedCylinder(mat, radius, substrate, depth, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, xtraParams=defaultXtraParams):
    """embeddedCylinder(mat, radius, substrate, depth, det, [e0=20.0], [withPoisson=True], [nTraj=defaultNumTraj], [dose = 120.0], [sf=defaultCharFluor], [bf=defaultBremFluor])"""
    tmp = u"MC simulation of a %0.2f micron vertical cylinder of %s embedded in %s at %0.1f keV%s%s" % (1.0e6 * radius, mat, substrate, e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildEmbeddedCylinder, {"Substrate": substrate, "Radius" : radius, "Depth" : depth, "Material" : mat}, xtraParams)


def buildInterface(monte, chamber, origin, buildParams):
    mat = buildParams["Material"]
    secondary = buildParams["Secondary"]
    offset = buildParams["Offset"]
    dim = 0.001
    dims = [dim, dim, dim]
    cp = epu.Math2.plus(origin, [-0.5 * dim + offset, 0.0, 0.5 * dim])
    cs = epu.Math2.plus(origin, [0.5 * dim + offset, 0.0, 0.5 * dim])
    monte.addSubRegion(chamber, mat, nm.MultiPlaneShape.createBlock(dims, cp, 0.0, 0.0, 0.0))
    monte.addSubRegion(chamber, secondary, nm.MultiPlaneShape.createBlock(dims, cs, 0.0, 0.0, 0.0))
    

def interface(primary, offset, secondary, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=True, bf=True, xtraParams=defaultXtraParams):
    """interface(primary, offset, secondary, det, [e0=20.0], [withPoisson=True], [nTraj=defaultNumTraj], [dose = 120.0], [sf=True], [bf=True], [xtraParams={}])
    Monte Carlo simulate a spectrum from beam placed in a primary material offset a distance from a secondary material. \
    Note: That for this simulation (in contrast with the others) sf=True and bf=True since secondary fluorescence is often a critical component in the spectrum measured at an interface.
    + primary - Material which electrons strike (positive offset)
    + offset - Offset from interface in meters
    + secondary - Material on the other side of the interface"""
    tmp = u"MC simulation of a probe placed in %s %0.2f microns from %s at %0.1f keV%s%s" % (primary, offset * 1.0e6, secondary, e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildInterface, {"Secondary" : secondary, "Offset" : offset, "Material" : primary}, xtraParams)

def buildCrenellated(monte, chamber, origin, buildParams):
    width = buildParams["Width"]
    depth = buildParams["Depth"]
    material = buildParams["Material"]
    size = 50.0e-6
    n = int(size / width + 0.5)
    print u"width = %3g µm depth = %3g µm, size = %3g µm, nSlices = %d" % (width * 1.0e6, depth * 1.0e6, size * 1.0e6, n)
    for i in xrange(-n, n + 1):
        slice = nm.MultiPlaneShape.createBlock([width, size, size], epu.Math2.plus(origin, [i * width, 0.0, 0.5 * size + (depth if i % 2 == 0 else 0.0)]), 0.0, 0.0, 0.0)
        monte.addSubRegion(chamber, material, slice)

def crenellated(width, depth, material, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, xtraParams=defaultXtraParams):
    """crenellated(width, depth, material, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, substrate=None, xtraParams={})
    Monte Carlo simulate a spectrum from a crenellated (think top of castle battlements) array of blocks.  This model is intended to model surface roughness.  With the width and depth of the features specified."""
    tmp = u"MC simulation of crenellated[W=%g, D=%g, %s] at %0.1f keV%s%s" % (width, depth, material, e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildCrenellated, {"Width":width, "Depth":depth, "Material":material }, xtraParams)

def buildVerticalLayers(monte, chamber, origin, buildParams):
    width = buildParams["Width"]
    materials = buildParams["Materials"]
    size = 12.0e-6
    n = int(size / width + 0.5)
    print u"width = %3g µm, size = %3g µm, nSlices = %d" % (width * 1.0e6, size * 1.0e6, n)
    for i in xrange(-n, n + 1):
        mat = materials[i % len(materials)]
        # print "%d\t%s" % (i, mat)
        slice = nm.MultiPlaneShape.createBlock([width, size, size], epu.Math2.plus(origin, [(i + 0.5) * width, 0.0, 0.5 * size]), 0.0, 0.0, 0.0)
        monte.addSubRegion(chamber, mat, slice)

def verticalLayers(width, materials, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, xtraParams=defaultXtraParams):
    """verticalLayers(width, materials, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, substrate=None, xtraParams={})
    Simulates an alternating stack of vertical layers of 'width'.  The layers rotate through the 'materials' which is a list of epq.Material-objects. The layers are in the y-z-plane and are spaced along the x-axis."""
    tmp = "MC simulation of vertical layers[W=%g, %s] at %0.1f keV%s%s" % (width, materials, e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildVerticalLayers, {"Width":width, "Materials":materials }, xtraParams)

def buildHorizontalLayers(monte, chamber, origin, buildParams):
    depth = buildParams["Depth"]
    materials = buildParams["Materials"]
    size = 12.0e-6
    n = int(size / depth + 0.5)
    print u"depth = %3g µm, size = %3g µm, nSlices = %d" % (depth * 1.0e6, size * 1.0e6, n)
    for i in xrange(0, n):
        mat = materials[i % len(materials)]
        # print "%d\t%s" % (i, mat)
        slice = nm.MultiPlaneShape.createBlock([size, size, depth], epu.Math2.plus(origin, [0.0, 0.0, (i + 0.5) * depth]), 0.0, 0.0, 0.0)
        monte.addSubRegion(chamber, mat, slice)

def horizontalLayers(depth, materials, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, xtraParams=defaultXtraParams):
    """horizontalLayers(depth, materials, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, substrate=None, xtraParams={})
    Simulates an alternating stack of horizontal layers of 'depth'.  The layers rotate through the 'materials' which is a list of epq.Material-objects. The layers are in the x-y-plane and are spaced along the z-axis."""
    tmp = "MC simulation of horizontal layers[D=%g, %s] at %0.1f keV%s%s" % (depth, materials, e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildHorizontalLayers, {"Depth":depth, "Materials":materials }, xtraParams)

def buildTwoParticles(monte, chamber, origin, buildParams):
    p1Mat, p1Radius = buildParams["P1"]
    p2Mat, p2Radius = buildParams["P2"]
    separation = buildParams["Separation"]
    substrate = buildParams["Substrate"]
    sphere1 = nm.Sphere(epu.Math2.plus(origin, [0.0, 0.0, p1Radius]), p1Radius)
    monte.addSubRegion(chamber, p1Mat, sphere1)
    sphere2 = nm.Sphere(epu.Math2.plus(origin, [separation, 0.0, 2.0 * p1Radius - p2Radius]), p2Radius)
    monte.addSubRegion(chamber, p2Mat, sphere2)
    if substrate:
        monte.addSubRegion(chamber, substrate, nm.MultiPlaneShape.createSubstrate([0.0, 0.0, -1.0], epu.Math2.plus(origin, [0.0, 0.0, 2.0 * p1Radius])))

def twoParticles(p1Mat, p1Radius, p2Mat, p2Radius, separation, substrate, det, e0=20, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=defaultCharFluor, bf=defaultBremFluor, xtraParams=defaultXtraParams):
    """twoParticles(p1Mat, p1Radius, p2Mat, p2Radius, separation, substrate, det, [e0=20], [withPoisson=True], [nTraj=defaultNumTraj], [dose=defaultDose], [sf=defaultCharFluor], [bf=defaultBremFluor], [xtraParams=defaultXtraParams])
    Simulates two spherical particles whose centers are separated by a distance 'separation' (along X-axis).
    The particles have materials p1Mat and p2Mat and radii p1Radius and p2Radius.  The beam is incident on the center of particle 1.
    The particles rest on an infinite substrate of material 'substrate'.
    Example: display(mc3.twoParticles( material("Cu",8.0), 2.0e-7, material("Ni",8.0), 2.0e-7, 4.0e-7, material("C",1.5), d1, e0=20.0))"""
    if separation < p1Radius + p2Radius:
        print "Warning: The particle centers are separated by less than the sum of the radii."
    params = { "P1" : (p1Mat, p1Radius,), "P2" : (p2Mat, p2Radius,), "Separation": separation, "Substrate" : substrate }
    tmp = "MC simulation of two particles separated by %g microns at %0.1f keV%s%s" % (separation * 1.0e6, e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildTwoParticles, params, xtraParams)

def buildRipples(monte, chamber, origin, buildParams):
    mat = buildParams["Material"]
    spacing = buildParams["Spacing"]
    depth = buildParams["Depth"]
    dim = 50.0e-6
    n = int(0.5 * dim / spacing)
    for i in range(-n, n + 1):
        mps = nm.MultiPlaneShape()
        mps.addPlane([-1.0, 0.0, 0.0], epu.Math2.plus(origin, [(i - 1) * spacing, 0.0, 0.5 * dim]))
        mps.addPlane([1.0, 0.0, 0.0], epu.Math2.plus(origin, [i * spacing, 0.0, 0.5 * dim]))
        mps.addPlane([0.0, 0.0, 1.0], epu.Math2.plus(origin, [(i + 0.5) * spacing, 0.0, dim]))
        den = jl.Math.sqrt(depth * depth + spacing * spacing)
        mps.addPlane([(-1.0 if i % 2 == 0 else 1.0) * depth / den, 0.0, -spacing / den], epu.Math2.plus(origin, [(i - 0.5) * spacing, 0.0, 0.5 * depth]))
        monte.addSubRegion(chamber, mat, mps)


def ripples(material, spacing, depth, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=True, bf=True, xtraParams=defaultXtraParams):
    """ripples(material, spacing, depth, det, [e0=20.0], [withPoisson=True], [nTraj=defaultNumTraj], [dose = 120.0], [sf=True], [bf=True], [xtraParams={}])
    Monte Carlo simulate a spectrum from beam placed in a primary material offset a distance from a secondary material. \
    Note: That for this simulation (in contrast with the others) sf=True and bf=True since secondary fluorescence is often a critical component in the spectrum measured at an interface.
    + material - Composition of material 
    + spacing - spacing between peak and trough
    + depth - Depth of trough"""
    tmp = u"MC simulation of a rippled surface (%0.2f, %0.2f) of %s at %0.1f keV%s%s" % (1.0e6 * spacing, 1.0e6 * depth, material, e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildRipples, { "Spacing" : spacing, "Depth" : depth, "Material" : material }, xtraParams)


def buildScratched(monte, chamber, origin, buildParams):
    mat = buildParams["Material"]
    spacing = buildParams["Spacing"]
    depth = buildParams["Depth"]
    dim = 50.0e-6
    n = int(0.5 * dim / spacing)
    # subs=nm.MultiPlaneShape.createBlock([dim,dim,dim], epu.Math2.plus(origin,[0.0,0.0,depth+0.5*dim]))
    subs = nm.SimpleBlock(epu.Math2.plus(origin, [-0.5 * dim, -0.5 * dim, depth]), epu.Math2.plus(origin, [0.5 * dim, 0.5 * dim, depth + dim]))
    monte.addSubRegion(chamber, mat, subs)
    pp = []
    for i in range(-n, n + 1):
        pp.append((2 * jl.Math.random() - 1) * n * spacing)
    pp.sort()
    prev = -0.5 * dim
    s = 0
    for p in pp:
        if p > prev:
            c1 = epu.Math2.plus(origin, [prev, -0.5 * dim, 0.0])
            c2 = epu.Math2.plus(origin, [p, 0.5 * dim, depth])
            s = s + (p - prev)
            monte.addSubRegion(chamber, mat, nm.SimpleBlock(c1, c2))
        prev = p + depth
    print "Coverage: %0.1f %%" % (100.0 * s / (2.0 * n * spacing),)


def scratched(material, spacing, depth, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=True, bf=True, xtraParams=defaultXtraParams):
    """scratch(material, spacing, depth, det, [e0=20.0], [withPoisson=True], [nTraj=defaultNumTraj], [dose = 120.0], [sf=True], [bf=True], [xtraParams={}])
    Monte Carlo simulate a spectrum from beam placed in a primary material offset a distance from a secondary material. \
    + material - Composition of material 
    + spacing - spacing between peak and trough
    + depth - Depth of trough"""
    tmp = u"MC simulation of a scratched surface (%0.2f, %0.2f) of %s at %0.1f keV%s%s" % (1.0e6 * spacing, 1.0e6 * depth, material, e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildScratched, { "Spacing" : spacing, "Depth" : depth, "Material" : material }, xtraParams)


def buildRough(monte, chamber, origin, buildParams):
    mat = buildParams["Material"]
    coverage = buildParams["Coverage"]
    scale = buildParams["Scale"]
    dim = buildParams["Size"]
    n = int(dim / scale) / 2 + 1
    xDisp, yDisp = (-0.5 + jl.Math.random()) * scale, (-0.5 + jl.Math.random()) * scale
    for x in range(-n, n):
        xx = x * scale + xDisp
        for y in range(-n, n):
            yy = y * scale + yDisp
            if jl.Math.random() <= coverage:
                c1 = epu.Math2.plus(origin, [xx, yy, 0.0])
                c2 = epu.Math2.plus(origin, [xx + scale, yy + scale, scale])
                monte.addSubRegion(chamber, mat, nm.SimpleBlock(c1, c2))
    c1, c2 = epu.Math2.plus(origin, [-1.0e-3, -1.0e-3, scale]), epu.Math2.plus(origin, [1.0e-3, 1.0e-3, 1.0e-3])
    monte.addSubRegion(chamber, mat, nm.SimpleBlock(c1, c2))
    
def rough(material, coverage, scale, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=True, bf=True, xtraParams=defaultXtraParams):
    """rough(material, coverage, scale, det, [e0=20.0], [withPoisson=True], [nTraj=defaultNumTraj], [dose = 120.0], [sf=True], [bf=True], [xtraParams={}])
    Monte Carlo simulate a spectrum from a rough surface with roughness modeled as square pillars of the specified scale and fractional coverage.
    The features are also offset by a randomized x,y offset of size approximately scale to ensure that the beam doesn't always strike at the same sort of a position.
    + material - Composition of material 
    + coverage of pillars on surface (0.0 to 1.0 -> 0% to 100%)
    + scale - height and width of pillars
    + depth - Depth of trough"""
    tmp = u"MC simulation of a %0.2lg um %d%% coverage rough surface of %s at %0.1f keV%s%s" % (1.0e6 * scale, int(100.0 * coverage), material, e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildRough, { "Scale" : scale, "Coverage" : coverage, "Size" : 1.0e-5, "Material" : material }, xtraParams)


def buildPartialSphere(monte, chamber, origin, buildParams):
    mat = buildParams["Material"]
    radius = buildParams["Radius"]
    top = buildParams["Top"]
    bottom = buildParams["Bottom"]
    assert top < bottom
    ts = nm.TruncatedSphere(epu.Math2.plus(origin, (0.0, 0.0, -top)), radius, top, bottom)
    monte.addSubRegion(chamber, mat, ts)
    substrate = buildParams["Substrate"]
    if substrate:
        c1, c2 = epu.Math2.plus(origin, (-1.0e-3, -1.0e-3, bottom - top)) , epu.Math2.plus(origin, (1.0e-3, 1.0e-3, 1.0e-3 + bottom - top))
        monte.addSubRegion(chamber, substrate, nm.SimpleBlock(c1, c2))


def partialSphere(material, radius, top, bottom, substrate, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=True, bf=True, xtraParams=defaultXtraParams):
    """partialSphere(material, radius, top, bottom, substate, det, [e0=20.0], [withPoisson=True], [nTraj=defaultNumTraj], [dose = 120.0], [sf=True], [bf=True], [xtraParams={}])
    Monte Carlo simulate a spectrum from a rough surface with roughness modeled as square pillars of the specified scale and fractional coverage.
    The features are also offset by a randomized x,y offset of size approximately scale to ensure that the beam doesn't always strike at the same sort of a position.
    + material - Composition of material 
    + radius - sphere radius
    + top, bottom - distance up or down to trim top/bottom of sphere (in range -radius, radius)
    + substrate - substrate material or None"""
    tmp = u"MC simulation of a %0.2lg um truncated sphere of %s at %0.1f keV%s%s" % (1.0e6 * radius, material, e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildPartialSphere, { "Radius" : radius, "Top" : top, "Bottom" : bottom, "Substrate":substrate, "Material" : material }, xtraParams)


def buildSphereBed(monte, chamber, origin, buildParams):
    mat = buildParams["Material"]
    radius = buildParams["Radius"]
    substrate = buildParams["Substrate"]
    randomize = buildParams["Randomize"]
    dim = 30.0e-6 / (0.001 * mat.getDensity())
    n = jl.Math.max(4, int(0.5 * dim / radius) + 1)
    print n
    if randomize:
        offset = epu.Math2.plus(origin, ((2.0 * jl.Math.random() - 1.0) * radius, (2.0 * jl.Math.random() - 1.0) * radius, 0.0))
    else:
        offset = origin
    sqrt3r = jl.Math.sqrt(3.0) * radius
    for i in range(-n, n):
        shapes = []
        for j in range(-n, n):
            sp = nm.BoundedShapes.boundedSphere(epu.Math2.plus(offset, ((2.0 * i + j % 2) * radius, sqrt3r * j, radius)), radius)
            shapes.append(sp)
        monte.addSubRegion(chamber, mat, nm.BoundedShapes(shapes))
    if substrate:
        c1, c2 = epu.Math2.plus(origin, (-1.0e-3, -1.0e-3, 2.0 * radius)) , epu.Math2.plus(origin, (1.0e-3, 1.0e-3, 1.0e-3 + 2.0 * radius))
        monte.addSubRegion(chamber, substrate, nm.SimpleBlock(c1, c2))


def sphereBed(material, radius, substrate, randomize, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=True, bf=True, xtraParams=defaultXtraParams):
    """sphereBed(material, radius, substate, randomize, det, [e0=20.0], [withPoisson=True], [nTraj=defaultNumTraj], [dose = 120.0], [sf=True], [bf=True], [xtraParams={}])
    Monte Carlo simulate a spectrum from a rough surface made up of close packed spheres.
    + material - Composition of material 
    + radius - sphere radius
    + substrate - Substrate material below a single plane of spheres"""
    tmp = u"MC simulation of a %0.2lg um%ssphere bed of %s at %0.1f keV%s%s" % (1.0e6 * radius, (" rand " if randomize else " "), material, e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildSphereBed, { "Radius" : radius, "Substrate":substrate, "Material" : material, "Randomize" : randomize }, xtraParams)


def buildSphereMatrix(monte, chamber, origin, buildParams):
    mat = buildParams["Material"]
    radius = buildParams["Radius"]
    randomize = buildParams["Randomize"]
    dim = 30.0e-6 / (0.001 * mat.getDensity())
    n = jl.Math.max(4, int(0.5 * dim / radius) + 1)
    print n
    if randomize:
        offset = epu.Math2.plus(origin, ((2.0 * jl.Math.random() - 1.0) * radius, (2.0 * jl.Math.random() - 1.0) * radius, 0.0))
    else:
        offset = origin
    jk = jl.Math.sqrt(3.0) * radius
    kk = 2.0 * jl.Math.sqrt(6) / 3.0 * radius
    for k in range(0, n):
        for i in range(-n, n):
            bss = []
            for j in range(-n, n):
                sp = nm.BoundedShapes.boundedSphere(epu.Math2.plus(offset, ((2.0 * i + (j + k) % 2) * radius, jk * (j + (k % 3) / 3.0), kk * k)), radius)
                bss.append(sp)
            monte.addSubRegion(chamber, mat, nm.BoundedShapes(bss))
    
    
def sphereMatrix(material, radius, randomize, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=True, bf=True, xtraParams=defaultXtraParams):
    """sphereBed(material, radius, substate, randomize, det, [e0=20.0], [withPoisson=True], [nTraj=defaultNumTraj], [dose = 120.0], [sf=True], [bf=True], [xtraParams={}])
    Monte Carlo simulate a spectrum from a rough surface made up of close packed spheres.
    + material - Composition of material 
    + radius - sphere radius
    + substrate - Substrate material below a single plane of spheres"""
    tmp = u"MC simulation of a %0.2lg um%ssphere matrix of %s at %0.1f keV%s%s" % (1.0e6 * radius, (" rand " if randomize else " "), material, e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildSphereMatrix, { "Radius" : radius, "Material" : material, "Randomize" : randomize }, xtraParams)


def buildMoguls(monte, chamber, origin, buildParams):
    mat = buildParams["Material"]
    radius = 2.0 * buildParams["Height"]
    randomize = buildParams["Randomize"]
    coverage = buildParams["Coverage"]
    dim = 30.0e-6 / (0.001 * mat.getDensity())
    height = 0.5 * radius
    width = jl.Math.sqrt(height * (2.0 * radius - height))
    n = jl.Math.max(4, int(0.5 * dim / width) + 1)
    sqrt3w = jl.Math.sqrt(3.0) * width
    if randomize:
        offset = epu.Math2.plus(origin, ((2.0 * jl.Math.random() - 1.0) * width, (2.0 * jl.Math.random() - 1.0) * width, 0.0))
    else:
        offset = origin
    if buildParams.has_key("Optimize") and buildParams["Optimize"]:
        for i in range(-n, n):
            shapes = []
            for j in range(-n, n):
                if jl.Math.random() < coverage:
                    sp = nm.BoundedShapes.boundedTruncatedSphere(epu.Math2.plus(offset, ((2.0 * i + j % 2) * width, sqrt3w * j, radius)), radius, -radius, height - radius)
                    shapes.append(sp)
            if len(shapes) > 0:
                monte.addSubRegion(chamber, mat, nm.BoundedShapes(shapes))
    else:
        for i in range(-n, n):
            for j in range(-n, n):
                if jl.Math.random() < coverage:
                    sp = nm.TruncatedSphere(epu.Math2.plus(offset, ((2.0 * i + j % 2) * width, sqrt3w * j, radius)), radius, -radius, height - radius)
                    monte.addSubRegion(chamber, mat, sp)
    c1, c2 = epu.Math2.plus(origin, (-1.0e-3, -1.0e-3, height)) , epu.Math2.plus(origin, (1.0e-3, 1.0e-3, 1.0e-3))
    monte.addSubRegion(chamber, mat, nm.SimpleBlock(c1, c2))

def moguls(material, height, randomize, coverage, det, e0=20.0, withPoisson=True, nTraj=defaultNumTraj, dose=defaultDose, sf=True, bf=True, optimize=True, xtraParams=defaultXtraParams):
    """moguls(material, radius, randomize, det, [e0=20.0], [withPoisson=True], [nTraj=defaultNumTraj], [dose = 120.0], [sf=True], [bf=True], [optimize=True], [xtraParams={}])
    Monte Carlo simulate a spectrum from a rough surface made up of close packed spheres.
    + material - Composition of material 
    + height - mogul height = 0.5 * mogul radius
    + randomize - randomize the beam start position?
    + coverage - fractional likelihood of each bump existing (0.0 to 1.0)"""
    tmp = u"MC simulation of a %0.2lg um %d%% %smogul bed of %s at %0.1f keV%s%s" % (1.0e6 * height, int(100.0*coverage), (" rand " if randomize else " "), material, e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildMoguls, { "Coverage" : coverage, "Optimize": optimize, "Height" : height, "Material" : material, "Randomize" : randomize }, xtraParams)

def zaf(comp, det, e0, nTraj=defaultNumTraj, stds={}):
    """zaf(comp, det, e0, nTraj=defaultNumTraj, stds={})
    Example: mc3.zaf(material("Al2O3",1),d2,10.0,stds={ "Al":"Al", "O":"MgO" })
    The Monte Carlo equivalent of dtsa2.zaf(comp,det,e0,stds) in the base DTSA-II scripting package. \
    Tabulates the ZAF corrections as computed from Monte Carlo simulations of the generated and emitted x-ray \
    intensities normalized relative to the specified standards (or if no standards are specified relative to \
    pure elements)"""
    def doMonte(cc):
        monte = nm.MonteCarloSS()
        monte.setBeamEnergy(epq.ToSI.keV(e0))
        chamber = monte.getChamber() # JRM added...
        dose = 60.0
        monte.addSubRegion(chamber, cc, nm.MultiPlaneShape.createSubstrate([0.0, 0.0, -1.0], origin))
        # Add event listeners to model characteristic radiation
        chXR = nm3.CharacteristicXRayGeneration3.create(monte)
        chTr = nm3.XRayTransport3.create(monte, det, chXR)
        xrel = nm3.XRayAccumulator3(xrts, "Characteristic", dose) # JRM add dose
        chTr.addXRayListener(xrel)
        fxg3 = nm3.FluorescenceXRayGeneration3.create(monte, chXR)
        chSFTr = nm3.XRayTransport3.create(monte, det, fxg3)
        chSF = nm3.XRayAccumulator3(xrts, "Secondary", dose) # JRM add dose
        chSFTr.addXRayListener(chSF)
        det.reset()
        monte.runMultipleTrajectories(nTraj)
        return (xrel, chSF)
    comp = dtsa2.material(comp, 1.0)
    print u"Material\t%s" % comp.descriptiveString(0)
    print u"Detector\t%s" % det
    print u"Algorithm\t%s" % "Monte Carlo (Gen3)"
    print u"MAC\t%s" % epq.AlgorithmUser.getDefaultMAC().getName()
    print u"E0\t%0.1f keV" % e0
    print u"Take-off\t%g%s" % (jl.Math.toDegrees(epq.SpectrumUtils.getTakeOffAngle(det.getProperties())), epq.SpectrumProperties.TakeOffAngle.getUnits())
    elms = comp.getElementSet()
    allStds = {}
    for elm, std in stds.iteritems():
        allStds[dtsa2.element(elm)] = dtsa2.material(std, 1.0)
    for elm in elms:
        if not allStds.has_key(elm):
            allStds[elm] = dtsa2.material(elm.toAbbrev(), 1.0)
    origin = epq.SpectrumUtils.getSamplePosition(det.getProperties())
    xrts = dtsa2.majorTransitions(comp, e0, thresh=0.8)
    xrel, chSF = {}, {}
    for elm in elms:
        xrel[elm], chSF[elm] = doMonte(allStds[elm]) 
    # Run unknown
    xrelu, chSFu = doMonte(dtsa2.material(comp, 1.0))
    print u"\nIUPAC\tSeigbahn\tStandard\tEnergy\t ZAF\t  Z\t  A\t  F\tk-ratio"
    for elm in elms:
        xrels = xrel[elm]
        chSFs = chSF[elm]
        cu = comp.weightFraction(elm, False)
        cs = allStds[elm].weightFraction(elm, False)
        for xrt in xrts:
            if xrt.getElement().equals(elm) and (xrels.getEmitted(xrt) > 0.0) and (xrelu.getEmitted(xrt) > 0.0):
                try:
                    # print "%g\t%g\t%g\t%g" % (xrelu.getGenerated(xrt), xrelu.getEmitted(xrt),xrels.getGenerated(xrt), xrels.getEmitted(xrt) )
                    a = (xrelu.getEmitted(xrt) * xrels.getGenerated(xrt)) / (xrelu.getGenerated(xrt) * xrels.getEmitted(xrt))
                    z = (cs * xrelu.getGenerated(xrt)) / (cu * xrels.getGenerated(xrt)) 
                    f = (1.0 + chSFu.getEmitted(xrt) / xrelu.getEmitted(xrt)) / (1.0 + chSFs.getEmitted(xrt) / xrels.getEmitted(xrt))
                    k = (xrelu.getEmitted(xrt) / xrels.getEmitted(xrt)) * f
                    eTr = epq.FromSI.keV(xrt.getEnergy())
                    print u"%s\t%s\t%s\t%2.4f\t%1.4f\t%1.4f\t%1.4f\t%1.4f\t%1.4f" % (xrt, xrt.getSiegbahnName(), allStds[elm], eTr, z * a * f, z, a, f, k)
                except:
                    print u"%s - %s" % (elm, xrt)


def buildTilted(monte, chamber, origin, buildParams):
    tilt = buildParams["Tilt"]
    mat = buildParams["Material"]
    shape = nm.MultiPlaneShape.createSubstrate([0.0, jl.Math.sin(tilt), -jl.Math.cos(tilt)], origin)
    monte.addSubRegion(chamber, mat, shape)


def backscatter(mat, e0, nTraj=100000, buildSample=buildBulk, params={}):
    """backscatter(mat, e0, nTraj=100000, buildSample=buildBulk)
    Simulate backscatter from the specified material at the specified beam energy."""
    defOut = (dtsa2.DefaultOutput if dtsa2.DefaultOutput else dtsa2.reportPath())
    monte = nm.MonteCarloSS()
    monte.setBeamEnergy(epq.ToSI.keV(e0))
    p = params.copy()
    p["Material"] = mat
    buildSample(monte, monte.getChamber(), (0.0, 0.0, 0.0), p)
    bs0 = nm.BackscatterStats(monte, 100)
    monte.addActionListener(bs0)
    ann = nm.AnnularDetector(1.0e-3, 10, (0.0, 0.0, -1.0e-3), (0.0, 0.0, 1.0)) 
    monte.addActionListener(ann)
    monte.runMultipleTrajectories(nTraj)
    tmpFile = jio.File.createTempFile("Backscatter", ".csv", jio.File(defOut))
    print u"Results -> %s" % tmpFile
    fos = jio.FileOutputStream(tmpFile)
    try:
        osw = jio.OutputStreamWriter(fos)
        osw.append("Parameters:\n")
        osw.append("E0\t%g keV\n" % e0)
        for k, v in p.iteritems():
            osw.append("%s\t%s\n" % (k, v))
        ann.dump(osw)
        osw.flush()
        bs0.dump(fos)
    finally:
        fos.close()
    return (bs0.backscatterFraction(), bs0.forwardscatterFraction())


def energyDeposition(layers, e0, nTraj=1000, path=None):
    defOut = (path if path else (dtsa2.DefaultOutput if dtsa2.DefaultOutput else dtsa2.reportPath()))
    monte = nm.MonteCarloSS()
    monte.setBeamEnergy(epq.ToSI.keV(e0))
    def buildFilm(monte, chamber, origin, buildParams):
        sr = chamber
        pos = origin
        for (mat, thickness,) in buildParams["Layers"]:
            if thickness <= 0.0:
                raise "The layer thickness must be larger than zero."
            monte.addSubRegion(sr, mat, nm.MultiPlaneShape.createFilm([0.0, 0.0, -1.0], pos, thickness))
        pos = epu.Math2.plus(pos, [0.0, 0.0, thickness])
    origin = (0.0, 0.0, 0.0)
    buildFilm(monte, monte.getChamber(), origin, { "Layers": layers })
    pt = origin
    nVoxels = 10
    ells = []
    for (mat, thick) in layers:
        ell = nm.EnergyLossListener(pt, (1.0e-3, 1.0e-3, thick / nVoxels), (1, 1, nVoxels))
        monte.addActionListener(ell)
        ells.append((mat, thick, ell,))
        pt = epu.Math2.plus(pt, (0.0, 0.0, thick))
    monte.runMultipleTrajectories(nTraj)
    for mat, thick, ell in ells:
        tmpFile = jio.File(jio.File(defOut), "%s - %g nm.csv" % (str(mat), thick * 1.0e9))
        print u"Results -> %s" % tmpFile
        fos = jio.FileOutputStream(tmpFile)
        try:
            osw = jio.OutputStreamWriter(fos)
            osw.append("Parameters:\n")
            osw.append("E0\t%g keV\n" % e0)
            osw.append("Layers: %s\n" % str(layers))
            ell.dumpVoxels(osw, True)
            osw.flush()
        finally:
            fos.close()

def bsed(mat, e0, angle, nTraj=100000, eFrac=0.9, scale=1.0e-6, filename=None, params={}):
    """bsed(mat, e0, angle, nTraj=100000, eFrac=0.9, scale=1.0e-6, filename = None, params={})
    mat = material("Si",2.33)
    e0 = 20.0 # keV
    angle = 3.1415926/4
    nTraj = 100000
    eFrac = 0.9
    scale = 1.0e-6  Linear dimensions of the full detector area
    filename = None - Define for a custom output filename like "backscatter - 55 deg.csv"
    Detects electrons with energies greater than e0*eFrac that pass through a plane
     located immediately above a bulk sample of 'mat' tilted at 'angle'."""
    defOut = (dtsa2.DefaultOutput if dtsa2.DefaultOutput else dtsa2.reportPath())
    p = params.copy()
    p["Tilt"] = angle
    p["Material"] = mat
    monte = nm.MonteCarloSS()
    monte.setBeamEnergy(epq.ToSI.keV(e0))
    origin = (0.0, 0.0, 0.0)
    buildTilted(monte, monte.getChamber(), origin, p)
    bs0 = nm.BackscatterStats(monte, 100)
    monte.addActionListener(bs0)
    ann = nm.AnnularDetector(scale, 100, origin, (0.0, 0.0, 1.0)) 
    monte.addActionListener(ann)
    normal = (0.0, jl.Math.sin(angle), -jl.Math.cos(angle))
    bs1 = nm.BSEDScatterDetector((0.0, 0.0, -1.0e-11), normal, scale / 100, 100, eFrac)
    monte.addActionListener(bs1)
    monte.runMultipleTrajectories(nTraj)
    if not filename:
        tmpFile = jio.File.createTempFile("Backscatter", ".csv", jio.File(defOut))
    else:
        tmpFile = jio.File(jio.File(defOut), filename)
    print u"%s" % tmpFile
    fos = jio.FileOutputStream(tmpFile)
    try:
        osw = jio.OutputStreamWriter(fos)
        osw.append("Parameters:\n")
        osw.append("E0\t%g keV\n" % e0)
        for k, v in p.iteritems():
            osw.append("%s\t%s\n" % (k, v))
        ann.dump(osw)
        osw.flush()
        bs1.dump(osw)
        osw.flush()
        bs0.dump(fos)
        fos.flush()
    finally:
        fos.close()
    return (bs0.backscatterFraction(), bs0.forwardscatterFraction())

def bsedAngular(mat, e0, nTraj=100000, eFrac=0.9, filename=None, params={}):
    """bsed(mat, e0, angle, nTraj=100000, eFrac=0.9, scale=1.0e-6, filename = None, params={})
    mat = material("Si",2.33)
    e0 = 20.0 # keV
    angle = 3.1415926/4
    nTraj = 100000
    eFrac = 0.9
    scale = 1.0e-6  Linear dimensions of the full detector area
    filename = None - Define for a custom output filename like "backscatter - 55 deg.csv"
    Constructs a histogram of scatter angles for scatter events for all backscattered and 
    non-backscattered electron trajectories.  Outputs the result to 'filename'."""
    defOut = (dtsa2.DefaultOutput if dtsa2.DefaultOutput else dtsa2.reportPath())
    monte = nm.MonteCarloSS()
    monte.setBeamEnergy(epq.ToSI.keV(e0))
    origin = (0.0, 0.0, 0.0)
    p = params.copy()
    p["Tilt"] = 0.0
    p["Material"] = mat
    buildTilted(monte, monte.getChamber(), origin, p)
    bs0 = nm.BackscatterAngleHistogram(eFrac*epq.ToSI.keV(e0), True)
    monte.addActionListener(bs0)
    bs1 = nm.BackscatterStats(monte, 100)
    monte.addActionListener(bs1)
    monte.runMultipleTrajectories(nTraj)
    if not filename:
        tmpFile=jio.File.createTempFile("angular", ".csv", jio.File(defOut))
    else:
        tmpFile = jio.File(jio.File(defOut), filename)
    print u"%s" % tmpFile
    pw = jio.PrintWriter(tmpFile)
    try:
        bs0.dump(pw)
    finally:
        pw.close()
    print "Backscatter fraction: %g" % ( bs1.backscatterFraction(), )
    print "Scatter angle statistics (in degrees)"
    bsf, nbsf = bs0.getBackscatterDS(), bs0.getNonBackscatterDS()
    print "      Avg, Max, StdDev"
    print "  BS: %g, %g, %g" % ( jl.Math.toDegrees(bsf.average()), jl.Math.toDegrees(bsf.maximum()), jl.Math.toDegrees(bsf.standardDeviation()) ) 
    print " NBS: %g, %g, %g" % ( jl.Math.toDegrees(nbsf.average()), jl.Math.toDegrees(nbsf.maximum()), jl.Math.toDegrees(nbsf.standardDeviation()) ) 
    return ( bsf, nbsf )  
    
    
def buildWedge(monte, chamber, origin, buildParams):
    matA = buildParams["MaterialA"]
    matB = buildParams["MaterialB"]
    matC = buildParams.get("MaterialC", matA)
    t = buildParams["Thickness"]
    rotation = jl.Math.toRadians(buildParams.get("Rotation", 0.0))
    theta = jl.Math.toRadians(buildParams["Tilt"])
    ct, st = jl.Math.cos(theta), jl.Math.sin(theta)
    vA = nm.MultiPlaneShape()
    vA.addPlane([-1.0,0.0,0.0], epu.Math2.plus(origin, [-1.0e-4, 0.0, 0.0]))
    vA.addPlane([ct,0.0,st], epu.Math2.plus(origin, [-0.5*t, 0.0, 0.0]))
    vA.addPlane([0.0,0.0,-1.0], epu.Math2.plus(origin, [0.0, 0.0, 0.0]))
    vA.addPlane([0.0,0.0,1.0], epu.Math2.plus(origin, [0.0, 0.0, 1.0e-4]))
    vA.addPlane([-1.0,0.0,0.0], epu.Math2.plus(origin, [-1.0e-4, 0.0, 0.0]))
    vA.addPlane([1.0,0.0,0.0], epu.Math2.plus(origin, [1.0e-4, 0.0, 0.0]))
    vA.rotate(origin, 0.0, 0.0, rotation)
    monte.addSubRegion(chamber, matA, vA)
    vB = nm.MultiPlaneShape()
    vB.addPlane([-ct,0.0,-st], epu.Math2.plus(origin, [-0.5*t, 0.0, 0.0]))
    vB.addPlane([ct,0.0,st], epu.Math2.plus(origin, [0.5*t, 0.0, 0.0]))
    vB.addPlane([0.0,0.0,-1.0], epu.Math2.plus(origin, [0.0, 0.0, 0.0]))
    vB.addPlane([0.0,0.0,1.0], epu.Math2.plus(origin, [0.0, 0.0, 1.0e-4]))
    vB.addPlane([-1.0,0.0,0.0], epu.Math2.plus(origin, [-1.0e-4, 0.0, 0.0]))
    vB.addPlane([1.0,0.0,0.0], epu.Math2.plus(origin, [1.0e-4, 0.0, 0.0]))
    vB.rotate(origin, 0.0, 0.0, rotation)
    monte.addSubRegion(chamber, matB, vB)
    vC = nm.MultiPlaneShape()
    vC.addPlane([1.0,0.0,0.0], epu.Math2.plus(origin, [1.0e-4, 0.0, 0.0]))
    vC.addPlane([-ct,0.0,-st], epu.Math2.plus(origin, [0.5*t, 0.0, 0.0]))
    vC.addPlane([0.0,0.0,-1.0], epu.Math2.plus(origin, [0.0, 0.0, 0.0]))
    vC.addPlane([0.0,0.0,1.0], epu.Math2.plus(origin, [0.0, 0.0, 1.0e-4]))
    vC.addPlane([-1.0,0.0,0.0], epu.Math2.plus(origin, [-1.0e-4, 0.0, 0.0]))
    vC.addPlane([1.0,0.0,0.0], epu.Math2.plus(origin, [1.0e-4, 0.0, 0.0]))
    vC.rotate(origin, 0.0, 0.0, rotation)
    monte.addSubRegion(chamber, matC, vC)


def simulateWedge(matA, matB, matC, tilt, thickness, det, e0=20.0, rotation = 0.0, dose=defaultDose, withPoisson=True, nTraj=defaultNumTraj, sf=defaultCharFluor, bf=defaultBremFluor, xtraParams=defaultXtraParams):
    """simulateWedge(matA, matB, matC, theta, thickness, det, e0=20.0, rotation = 0.0, dose=defaultDose, withPoisson=True, nTraj=defaultNumTraj, sf=defaultCharFluor, bf=defaultBremFluor, xtraParams=defaultXtraParams):
    Simulate a spectrum from a tilted wedge of `matA` and `thickness` (measured along surface) at an angle `tilt` on the detector det at beam energy e0 (in keV).  \
    rotation rotates the sample around the beam axis/origin.  If sf then simulate characteristic secondary fluorescence. If bf then simulate bremsstrahlung secondary \
    fluorescence. nTraj specifies the number of electron trajectories. dose is in nA*sec."""
    params = { 
        "MaterialA" : material(matA), 
        "MaterialB" : material(matB),  
        "MaterialC" : material(matC), 
        "Tilt" : tilt,
        "Thickness" : thickness,
        "Rotation" : rotation
    }
    if not ( isinstance(matA, epq.Material) and isinstance(matB, epq.Material) and isinstance(matC, epq.Material)):
        print u"Please provide a material with a density - %s, %s, %s" % ( matA, matB, matC)
    tmp = u"MC simulation of %g um wedge at %g degrees of %s at %0.1f keV%s%s" % (thickness/1.0e-6, tilt, matB, e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
    print tmp
    return base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildWedge, params, xtraParams)
    

    