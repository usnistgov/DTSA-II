# -*- coding: utf-8 -*-
# Name: 	startUp.py
# Purpose:	This script initializes the SEMantics library.   It optionally connects to the
#	TESCAN SEM and provides a mechanism to control the SEM, collect data and process SEM data.
# Modified: 18-May-2020
# Set the SITE to account for site specific hardware variations
NIST, MCCRONE, PNNL, PAS, AEM, ORNL = ( "NIST", "McCRONE", "PNNL", "PAS", "AEM", "ORNL" )
SITE = ORNL

if (SITE == NIST) and (jl.System.getProperty('sun.java.command') == u'gov.nist.microanalysis.dtsa2.DTSA2'):
	print "JAR paths based on workspace."
	sys.path.append("C:\\Users\\nritchie\\Documents\\workspace\\SEMantics\\bin")	
	sys.path.append("C:\\Users\\nritchie\\Documents\\workspace\\Graf\\classes")
	sys.path.append("C:\\Users\\nritchie\\Documents\\workspace\\FastQuant\\bin")
else:
	base = jl.System.getProperty("user.dir")
	print "JAR paths based on user.dir = %s." % base
	sys.path.append("%s\\graf.jar" % base)
	sys.path.append("%s\\FastQuant.jar" % base)
	sys.path.append("%s\\semantics.jar" % base)

import array as javaarray
import com.thoughtworks.xstream as xst
sys.packageManager.makeJavaPackage("gov.nist.microanalysis.Graf", "Graf", None)
import gov.nist.microanalysis.Graf as graf
sys.packageManager.makeJavaPackage("gov.nist.microanalysis.FastQuant", "FastQuant", None)
import gov.nist.microanalysis.FastQuant as fq
sys.packageManager.makeJavaPackage("gov.nist.microanalysis.semantics", "semantics", None)
import gov.nist.microanalysis.semantics as semantics
sys.packageManager.makeJavaPackage("gov.nist.microanalysis.semantics.SharkSEM", "SharkSEM", None)
import gov.nist.microanalysis.semantics.SharkSEM as semss
sys.packageManager.makeJavaPackage("gov.nist.microanalysis.semantics.SharkSEM.DataItems", "DataItems", None)
import gov.nist.microanalysis.semantics.SharkSEM.DataItems as semdi
sys.packageManager.makeJavaPackage("gov.nist.microanalysis.semantics.SharkSEM.Translators", "Translators", None)
import gov.nist.microanalysis.semantics.SharkSEM.Translators as semtr
sys.packageManager.makeJavaPackage("gov.nist.microanalysis.semantics.automated", "automated", None)
import gov.nist.microanalysis.semantics.automated as sema
sys.packageManager.makeJavaPackage("gov.nist.microanalysis.semantics.staging", "staging", None)
import gov.nist.microanalysis.semantics.staging as semstg
import java.awt as jawt
import java.awt.image as jawtimg
import java.awt.geom as jag
import java.text as jtext
sys.packageManager.makeJavaPackage("javax.imageio", "ImageIO", None)
import javax.imageio as iio
import javax.swing.JOptionPane as jop
import javax.swing as jxs
import java.util.concurrent as jutilc
import java.util.prefs.Preferences as jupref
import time
import os
import java.lang as jl
import java.awt.image as jai
import gov.nist.microanalysis.Graf.Zeppelin as gzep
import gov.nist.microanalysis.Graf.Zeppelin.ColumnType as zct

X_AXIS = epq.StageCoordinate.Axis.X
Y_AXIS = epq.StageCoordinate.Axis.Y
Z_AXIS = epq.StageCoordinate.Axis.Z
R_AXIS = epq.StageCoordinate.Axis.R
T_AXIS = epq.StageCoordinate.Axis.T
B_AXIS = epq.StageCoordinate.Axis.B

defaultBounds = (-40.0, -30.0, 40.0, 30.0)

def setDefaultPath(path):
	"""setDefaultPath(path)
	Specify the default directory path into which to write images, spectra and other data."""
	global defaultPath
	jio.File(path).mkdirs()
	defaultPath=path
	print "Session data: %s" % defaultPath
	report("<p>Writing session data to <i>%s</i></p>" % defaultPath)

defaultArchivePath = None
	
if SITE==NIST:
	rootPath = "D:"
	defaultArchivePath = "P:"
elif (SITE==PNNL) or (SITE==PAS) or (SITE==AEM):
	rootPath = "C:\\Users\\Tescan\\My Documents\\Data"
elif SITE==ORNL:
    rootPath = "C:\\Users\\Tescan\\Data"                                
else:  # SITE==MCCRONE:
	rootPath = "C:\\Data"

# Configure this to determine which field images to save.
#   Image 1 => 0x1, Image 2 => 0x2, Image N => 2^(N-1) and Image 1+2 = 0x1 + 0x2 etc.
SAVE_FIELD_MASK = (0x0 if (SITE == AEM) or (SITE == PAS) else 0x3)
# Default images to save using collectImages(...)
DEF_IMAGE_MASK = 0x3
# Mask to use for other functions that save images 
SAVE_IMAGE_MASK = 0x3

if SITE<>PNNL:
	setDefaultPath("%s\\Daily\\%s" % (rootPath, jtext.SimpleDateFormat("dd-MMM-yyyy").format(ju.Date())))
	defaultRulePath = "%s\\Standards\\Combined" % rootPath
	defaultVecPath = "%s\\Standards\\Combined\\25 keV" % rootPath
	nullImagePath = "%s\\Standards\\Null images" % rootPath
	keyPath = "%s\\Standards\\Null images" % rootPath
else:
	setDefaultPath("%s\\Daily\\%s" % (rootPath, jtext.SimpleDateFormat("dd-MMM-yyyy").format(ju.Date())))
	defaultRulePath = "%s\\Standards\\Rule" % rootPath
	defaultVecPath = "%s\\Standards\\Combined\\25 keV" % rootPath
	nullImagePath = "%s\\NullImages" % base
	keyPath = nullImagePath

defaultStds = { "C": "C std.msa", "Al": "Al std.msa", "Na": "NaCl std.msa", "Cl": "NaCl std.msa",
		 "O": "MgO std.msa", "Si": "Si std.msa", "Fe": "Fe std.msa", "Ca": "CaF2 std.msa", 
		 "Cr": "Cr std.msa", "Ni":"Ni std.msa", "Cu": "Cu std.msa", "Ti": "Ti std.msa", "Mn":"Mn std.msa",
		 "Mg": "Mg std.msa", "S": "FeS2 std.msa", "Zn" : "Zn std.msa", "Ba":"BaF2 std.msa", 
#		 "Co":"Co std.msa", "K": "KBr std.msa", "Zr":"Zr std.msa", "Mo":"Mo std.msa", 
#		 "Ag" : "Ag std.msa", "Au" : "Au std.msa", "La" : "LaF3 std.msa", "Pb" : "K227 std.msa"
}

if (SITE==NIST) or (SITE==MCCRONE) or (SITE==ORNL):
	availableDets = ( True, )*4 # False, False, False )
else:
	availableDets = ( True, )*3 # False, False, False )
	
defaultDetCount = len(availableDets)
defaultDetMask = 0
for i in range(0, defaultDetCount):
	if availableDets[i]:
		defaultDetMask = defaultDetMask + (1 << i)

defLED = True

pt_det = []
if SITE==NIST:
	bd="Detector %d"
	det_off=0
	all = "Global - 10 eV/ch"
else:
	bd="EDAX Det %d"
	all = "EDAX All"
	det_off=1
	
for i in range(0, defaultDetCount):
	if availableDets[i]:
		pt_det.append(findDetector(bd % (i+det_off, )))
pt_det_all = findDetector(all)

_saverize = semantics.Saverize()
_saverizeTh = jl.Thread(_saverize)
_saverizeTh.start()

connect = (jop.showConfirmDialog(MainFrame, "Connect to the TESCAN?", "Start-up Script", jop.YES_NO_OPTION) == jop.YES_OPTION)

# Location of the Image Magick executables 'convert' and 'montage' 
if SITE==ORNL:
	IMAGE_MAGICK = "C:\\Program Files\\ImageMagick-6.9.9-Q16"
else:
	IMAGE_MAGICK = "C:\\Program Files\\ImageMagick-6.9.6-Q16"

def pascalToTorr(pa):
	return pa * 0.0075006

def parseCoords(ps):
	"""parseCoords(ps)
	Converts a string containing string representations of stage coordinates into a tuple containing StageCoordinate objects.
	ps = str(pts) where pts is a list or tuple containing StageCoordinate objects"""
	cs = None
	res=[]
	for i in range(0,len(ps)):
		if (cs!=None):
			if ps[i]=='}':
				cs = cs+ps[i]
				try:
					res.append(epq.StageCoordinate.fromString(cs))
				finally:
					cs = None
			else:
				cs = cs + ps[i]
		elif ps[i]=='{':
			cs=ps[i]
	return tuple(res)	
	
if connect:
	if SITE==NIST:
		_vendor = semss.ScanningElectronMicroscope.EDSVendor.Pulsetor
	else:
		_vendor = semss.ScanningElectronMicroscope.EDSVendor.EDAX
	_sem = semss.ScanningElectronMicroscope(_vendor)
	sem = _sem
	_sem.initializeEDS(defaultDetMask)
	_stg = _sem.getStage()
	stg = _stg
	_img = _sem.getAcquireImage()
	img = _img
	opt = _sem.getOptics()
	_opt = opt
	_ts = _sem.getTescanSEM()
	ts = _ts
	_eds = _sem.getAcquireEDS()
	eds = _eds
	_pt = sem.getPulseTorEDS()
	_afafb = None
	
	pt = _pt
	cx = 0
	for i in range(0, defaultDetCount):
		if availableDets[i]:
			_sem.setDetector(cx, pt_det[cx])
			cx = cx + 1
	
	cx = 0
	for i in range(0, defaultDetCount):
		if availableDets[i]:
			print "     %s - Gain: %f" % (pt_det[cx], _pt.getADCGain(i))
			cx = cx + 1
	report("<p>Connected to TESCAN with %d SDD</p>" % defaultDetCount)
	
	# Initialize the PulseTor detectors
	
	def activeDetectors(mask=defaultDetMask):
		res = []
		for i in range(0, 8):
			if (1 << i) & mask:
				res.append(i)
		return tuple(res)
	
	print "Active detectors: (%s)" % ", ".join("(%d @ %s)" % (d, _pt.getResolutionMode(d)) for d in activeDetectors())
	
	def setResolution(res, mask=defaultDetMask):
		"""setResolution(res, mask=defaultDetMask)
		Set the EDS detector resolution to one of 'Best', 'Medium', 'Fast', 'VeryFast', 'Adaptive'"""
		_pt.setResolutionMode(_pt.ResolutionMode.valueOf(res), mask)
		
		
	def turnOff():
		"""turnOff()
		Turns the instrument (high voltage, scanning etc.) off."""
		_sem.mainPower(False)
		
	def vent():
		"""vent()
		Turn off the beam and went the chamber"""
		turnOff()
		time.sleep(1.0)
		# _pt.enablePeltier(defaultDetMask, False)
		# time.sleep(60.0)
		_ts.vacVent()
	
	def pump():
		"""pump()
		Evacuate the sample chamber"""
		_ts.vacPump()
		time.sleep(30.0)
		_pt.enablePeltier(defaultDetMask, True)
	
	def collectImages(name, fov=1.0, dims=(512, 512), dwell=4, subRaster=None, path=None, rotation=0.0, markCenter=False, writeMask=DEF_IMAGE_MASK):
		"""collectImages(
			name,            # Base file name
			fov=1.0,         # Field of view in mm
			dims=(512, 512), # Image dimensions in pixels
			dwell=4,         # Dwell index
			subRaster=None,  # A rectangle within dims to limit the image
			path=None,       # If path=None => defaultPath otherwise => path
			rotation=0.0,    # Image rotation in degrees
			markCenter=False,# Place a marker at the center of the image
			writeMask=0xFF   # Which images to write 0x1 -> 1, 0x2 -> 2, 0x4 -> 3 0x3 -> 1 & 2
		)
		Collect and save images to a file named 'name' with the specified 'fov' (in mm) and image dimensions 'dims', scan speed 'dwell'."""		
		global terminated
		if terminated:
			return
		ci = _img.getConfigureImage()
		ci.setImageDimensions(dims[0], dims[1])
		ci.setRotation(rotation)
		if subRaster:
			if not isinstance(subRaster, jawt.Rectangle):
				subRaster = jawt.Rectangle(subRaster[0], subRaster[1], subRaster[2] - subRaster[0] - 1, subRaster[3] - subRaster[1] - 1)
			ci.setSubRaster(subRaster)
		ci.setScanSpeed(dwell)
		ci.setFieldOfView(fov)
		res = _img.acquire()
		if markCenter:
			for bi in res:
				bi.applyCenterCrossHair()
		path = (path if path else defaultPath)
		if name:
			write(res, name, path, writeMask=writeMask)
			logImage(path, name, "Images", fov, dims, dwell, sem.beamEnergy/1000.0)
		# report("<p>Collected images <i>%s</i> - %0.1f &mu;m FOV %d &times; %d at dwell %d</p>" % (name, 1000.0*fov, dims[0], dims[1], dwell))
		return res
	
	def logImage(path, name, typ, fov, dims, dwell, e0, frameCount=1):
		"""Used internally to log the acquisition of images and spectrum images."""
		ff = jio.File(path, "images.txt")
		header = not ff.exists()
		fos = jio.FileOutputStream(ff, True)
		try:
			pw = jio.PrintWriter(fos)
			try:
				if header:
					pw.append("Name\tType\tFOV\tXDim\tYDim\tDwell\tFrame Count\tBeam Energy\t")
					pw.append("X\tY\tZ\tR\tT\tB\n")
				pw.append("%s\t%s\t%1.5f\t%d\t%d\t%d\t%d\t%1.3f" % (name, typ, fov, dims[0], dims[1], dwell, frameCount, e0))
				pos = _stg.getPosition()
				pw.append("\t%1.4f\t%1.4f\t%1.3f\t%1.2f\t%1.2f\t%1.2f\n" % (pos.get(X_AXIS), pos.get(Y_AXIS), pos.get(Z_AXIS), pos.get(R_AXIS), pos.get(T_AXIS), pos.get(B_AXIS)))
			finally:
				pw.flush()
		finally:
			fos.close()
	
	def collectSI(name, fov, frameCount=1, dwell=9, dims=(1024, 1024), vectorSet=None, subRaster=None, path=None, rotation=0.0):
		"""collectSI(name, fov, frameCount=1, dwell=9, dims=(1024, 1024), subRaster=None, path=defaultPath, rotation=0.0)
		Collect x-ray spectrum image data, write the results to a directory called 'name' in 'path'.
		fov is the horizontal field-of-view in mm, 
		frameCount is number of frame scans
		dwell is the integer scan speed index (9=1 ms/pixel)
		dims in (pixels,pixels)
		subRaster (optional) are coordinates of 4 pixels bounding the desired sub-scan region"""
		global terminated
		if terminated:
			return
		_ts.chamberLed(False)
		asi = sem.getAcquireSpectrumImage()
		ci=asi.getConfigureImage()
		ci.setImageDimensions(dims[0], dims[1])
		ci.setScanSpeed(dwell)
		ci.setFieldOfView(fov)
		ci.setRotation(rotation)
		if subRaster:
			if not isinstance(subRaster, jawt.Rectangle):
				subRaster = jawt.Rectangle(subRaster[0], subRaster[1], subRaster[2] - subRaster[0] - 1, subRaster[3] - subRaster[1] - 1)
			ci.setSubRaster(subRaster)
		asi.setDesiredFrameCount(frameCount)
		outPath = "%s/%s" % ((path if path else defaultPath), name)
		jio.File(outPath).mkdirs()
		fos = jio.FileOutputStream(jio.File(outPath, "map.ptx"))
		try:
			if vectorSet and isinstance(vectorSet, epq.VectorSet):
				asi.setVectorSet(vectorSet)
			asi.start(fos)
			id = 0
			while True:
				if terminated:
					asi.cleanup()
					break
				msg = sem.take()
				if isinstance(msg, semss.DataItems.ImageDatum):
					write(msg, "Image", outPath)
					id = id + 1
				elif isinstance(msg, semss.DataItems.SpectrumDatum):
					write(msg, "Spectrum", outPath, fmt="msa")
				elif isinstance(msg, semss.DataItems.DoneDatum):
					break
		finally:
			fos.close()
		_ts.chamberLed(defLED)
		logImage((path if path else defaultPath), name, "SI", fov, dims, dwell, sem.beamEnergy, frameCount)
		report("<p>Collected SI <i>%s</i> %0.1f &mu;m FOV %d &times; %d with %d frames at dwell %d </p>" % (name, 1000.0*fov, dims[0], dims[1], frameCount, dwell ))

		
	def collectSIs(pts, dwell=6, dims=(1024, 1024)):
		"""Collects a sequence of spectrum images.  Each item in 'pts' is a list containing 
	[ 'name', stgPos, fieldOfView, workingDistance ].
	The algorithm moves to "stgPos", sets the image width to fieldOfView (in mm), sets the focal distance to "workingDistance". The resulting spectrum image is recorded in a directory named "name"."""
		try:
			for name, pos, fov, wd in pts:
				if terminated:
					return
				try:
					moveTo(pos)
					sem.getOptics().setWorkingDistance(wd)
					collectSI(name, fov, dwell=dwell, dims=dims)
				except Exception, e:
					print e
		finally:
			turnOff()
			
	def moveTo(pnt, xyOnly=False):
		"""moveTo(pnt, xyOnly=false) - (Works)
		Moves the stage to the specified stage point.  If pnt is a ISpectrumData derived object then the stage /
is moved to the StagePosition associated with the spectrum."""
		global terminated
		if terminated:
			return
		if isinstance(pnt, epq.ISpectrumData):
			pnt = pnt.getProperties().getObjectWithDefault(epq.SpectrumProperties.StagePosition, None)
		if not pnt:
			return
		if xyOnly:
			newPt = epq.StageCoordinate()
			newPt.set(X_AXIS, pnt.get(X_AXIS))
			newPt.set(Y_AXIS, pnt.get(Y_AXIS))
			_stg.setPosition(newPt)
			time.sleep(0.1)
			while (_stg.getStatus() == _stg.StageStatus.MOVING) and (not terminated):
				time.sleep(0.1)
		else:
			start = epq.StageCoordinate(position())
			if pnt.get(Z_AXIS) > start.get(Z_AXIS):
				# Move to larger Z first
				start.set(Z_AXIS, pnt.get(Z_AXIS))
				_stg.setPosition(start)
				time.sleep(0.1)
				while (_stg.getStatus() == _stg.StageStatus.MOVING) and (not terminated):
					time.sleep(0.1)
			newPt = epq.StageCoordinate(pnt)
			newPt.set(Z_AXIS, jl.Math.max(pnt.get(Z_AXIS), start.get(Z_AXIS)))
			_stg.setPosition(newPt)
			time.sleep(0.1)
			while (_stg.getStatus() == _stg.StageStatus.MOVING) and (not terminated):
				time.sleep(0.1)
			if newPt.get(Z_AXIS) != pnt.get(Z_AXIS):
				# Move closer last
				_stg.setPosition(pnt)
				time.sleep(0.1)
				while (_stg.getStatus() == _stg.StageStatus.MOVING) and (not terminated):
					time.sleep(0.1)

	def moveZ(z, warn=True):
		"""moveZ(z, [warn=False])
	Move only the z stage coordinate. Setting warn = True warns the user if the move is more than 1 mm."""
		pos = position()
		if warn and (pos.get(Z_AXIS)-z>1.0):
			msg = "Move Z axis to %0.3f mm from %0.3f mm?" % (z, pos.get(Z_AXIS),)
			if jop.showConfirmDialog(MainFrame, msg, "Large Z motion", jop.YES_NO_OPTION) <> jop.YES_OPTION:
				return
		pos.set(Z_AXIS, z)
		moveTo(pos, xyOnly=False)
					
	def home():
		"""home()
		Return the stage to (0.0, 0.0)"""
		newPt = position()
		newPt.set(X_AXIS, 0.0)
		newPt.set(Y_AXIS, 0.0)
		newPt.set(Z_AXIS, jl.Math.max(40.0, newPt.get(Z_AXIS)))
		moveTo(newPt)
		
			
	def position():
		"""position()
		Waits for the stage to stop moving then returns the current stage coordinate."""
		while (_stg.getStatus() == _stg.StageStatus.MOVING) and (not terminated):
			time.sleep(0.01)
		return _stg.getPosition()
			
	def collect2(acqTime=60, name=None, pc=True, mode='L', disp=True, forcePC=False, comp=None, path=None, fov=None):
		"""collect2(acqTime=60, name=None, pc=True, mode='L', disp=True, forcePC=False, comp=None, path=None)
	Collect a single combined EDS spectrum for the specified live time or real time.  Name the spectrum as specified and display.
pc = True to collect the probe current from the stage position "faraday"
mode = "L"|"R"|"C" for live-time, real-time or counts (changes how acqTime is interpreted)
disp=True to display the spectrum as it is acquired
forcePC = True to force the probe current to be measured with each spectrum rather than once every 5 minutes
comp: If defined as a epq.Composition object then this composition is assigned to the "StandardMaterial" property of the spectrum.
path: An alternative path into which to save the spectrum context data
fov: An optional field of view width to which to set the SEM imaging while collecting the spectrum."""
		global terminated
		if terminated:
			return
		# tt = _pt.getTemperature(0)
		# if (tt < -31.3) or (tt > -29.7):
		# 	print "Warning: The detectors may not be at the correct temperature (T=%2.1f != 30.0)" % tt
		start_ts = ju.Date()
		if name == None:
			name = "Spectrum"
		try:
			if (comp == None) and name.endswith(" std"):
				comp = material(name[0:len(name) - 4])
		except:
			comp = None
		name = "%s[%s]" % (name, time().ctime())
		_ts.chamberLed(False)
		stg_pos = _stg.getPosition()
		time.sleep(0.1)
		if pc:
			fb = updatedPC(interval=(0 if forcePC else 300))
		clear()
		hv = _ts.hvGetVoltage() / 1000.0
		if fov:
			oldVf = _ts.getViewField()
			oldSp = _ts.scGetSpeed()
			_ts.scSetSpeed(2)
			_ts.setViewField(fov)
		if mode == 'R':
			_eds.start(_eds.Mode.RealTime, acqTime)
		elif mode == 'L':
			_eds.start(_eds.Mode.LiveTime, acqTime)
		else:
			_eds.start(_eds.Mode.Counts, acqTime)
		done = False
		rep = None
		while not done:
			time.sleep(jl.Math.min(1.0, 1.3 * acqTime))
			if terminated:
				_eds.stop()
			done = _eds.done()
			if disp or done:
				spec = None
				detCx=0
				for det in range(0, defaultDetCount):
					if availableDets[det]:
						detCx=detCx+1
						tmp = _eds.getSpectrum(det)
						tmp.getProperties().setDetector(pt_det_all)
						if pc and fb:
							sp = tmp.getProperties()
							sp.setNumericProperty(sp.FaradayBegin, fb.average())
						if spec==None:
							spec=epq.SpectrumMath(tmp)
						else:
							spec.add(tmp,1.0)
				epq.SpectrumUtils.rename(spec, "%s[all]" % (name))
				if rep:
					DataManager.replaceSpectrum(rep, spec)
				else:
					display(spec)
				rep=spec
		if pc:
			fe = updatedPC(interval=(0 if forcePC else 300))
		if spec:
			sp = spec.getProperties()
			sp.setTimestampProperty(epq.SpectrumProperties.AcquisitionTime, start_ts)
			sp.setObjectProperty(epq.SpectrumProperties.StagePosition, stg_pos)
			sp.setNumericProperty(epq.SpectrumProperties.WorkingDistance, _opt.getWorkingDistance())
			if isinstance(comp, str):
				comp = material(comp)
			if isinstance(comp, epq.Composition):
				sp.setCompositionProperty(epq.SpectrumProperties.StandardComposition, comp)
			if pc:
				if fb:
					sp.setNumericProperty(sp.FaradayBegin, fb.average())
				if fe:
					sp.setNumericProperty(sp.FaradayEnd, fe.average())
		_ts.chamberLed(defLED)
		if fov:
			_ts.setViewField(oldVf)
			_ts.scSetSpeed(oldSp)
		logSpectrum((path if path else defaultPath), acqTime, name, mode)
		if isinstance(comp, epq.Composition):
			report("<p>Collected spectrum <i>%s</i> from %s for %0.1f s %s at %0.1f keV</p>" % (name, comp, acqTime, mode, hv))
		else:
			report("<p>Collected spectrum <i>%s</i> for %0.1f s %s at %0.1f keV</p>" % (name, acqTime, mode, hv))
		wspec=wrap(spec)
		DataManager.replaceSpectrum(rep, wspec)
		return wspec
		
	def collect2ts(acqTime=60, name=None, pc=True, mode='L', disp=True, forcePC=False, comp=None, path=None, fov=None):
		"""collect2ts(acqTime=60, name=None, pc=True, mode='L', disp=True, forcePC=False, comp=None, path=None)
	Collect a time sequence of spectra representing the combined EDS spectrum for the specified live time or real time.  Name the spectrum as specified and display.
pc = True to collect the probe current from the stage position "faraday"
mode = "L"|"R"|"C" for live-time, real-time or counts (changes how acqTime is interpreted)
disp=True to display the spectrum as it is acquired
forcePC = True to force the probe current to be measured with each spectrum rather than once every 5 minutes
comp: If defined as a epq.Composition object then this composition is assigned to the "StandardMaterial" property of the spectrum.
path: An alternative path into which to save the spectrum context data
fov: An optional field of view width to which to set the SEM imaging while collecting the spectrum."""
		global terminated
		if terminated:
			return
		# tt = _pt.getTemperature(0)
		# if (tt < -31.3) or (tt > -29.7):
		# 	print "Warning: The detectors may not be at the correct temperature (T=%2.1f != 30.0)" % tt
		start_ts = ju.Date()
		if name == None:
			name = "Spectrum"
		try:
			if (comp == None) and name.endswith(" std"):
				comp = material(name[0:len(name) - 4])
		except:
			comp = None
		name = "%s[%s]" % (name, time().ctime())
		_ts.chamberLed(False)
		stg_pos = _stg.getPosition()
		time.sleep(0.1)
		if pc:
			fb = updatedPC(interval=0)
			msg = "Is the analysis point centered in the image?"
			if jop.showConfirmDialog(MainFrame, msg, "Restoring analysis position", jop.YES_NO_OPTION) <> jop.YES_OPTION:
				return
		clear()
		hv = _ts.hvGetVoltage() / 1000.0
		if fov:
			oldVf = _ts.getViewField()
			oldSp = _ts.scGetSpeed()
			_ts.scSetSpeed(2)
			_ts.setViewField(fov)
		if mode == 'R':
			_eds.start(_eds.Mode.RealTime, acqTime)
		elif mode == 'L':
			_eds.start(_eds.Mode.LiveTime, acqTime)
		else:
			_eds.start(_eds.Mode.Counts, acqTime)
		done = False
		tsIdx=0
		specs=[]
		while not done:
			time.sleep(jl.Math.min(1.0, 1.3 * acqTime))
			if terminated:
				_eds.stop()
			done = _eds.done()
			spec = None
			detCx=0
			for det in range(0, defaultDetCount):
				if availableDets[det]:
					detCx=detCx+1
					tmp = _eds.getSpectrum(det)
					tmp.getProperties().setDetector(pt_det_all)
					if pc and fb:
						sp = tmp.getProperties()
						sp.setNumericProperty(sp.FaradayBegin, fb.average())
					if spec==None:
						spec=epq.SpectrumMath(tmp)
					else:
						spec.add(tmp,1.0)
			epq.SpectrumUtils.rename(spec, "%s[all][%d]" % (name, tsIdx))
			sp = spec.getProperties()
			sp.setTimestampProperty(epq.SpectrumProperties.AcquisitionTime, start_ts)
			sp.setObjectProperty(epq.SpectrumProperties.StagePosition, stg_pos)
			sp.setNumericProperty(epq.SpectrumProperties.WorkingDistance, _opt.getWorkingDistance())
			if isinstance(comp, str):
				comp = material(comp)
			if isinstance(comp, epq.Composition):
				sp.setCompositionProperty(epq.SpectrumProperties.StandardComposition, comp)
			if pc and fb:
				sp.setNumericProperty(sp.FaradayBegin, fb.average())
			tsIdx=tsIdx+1
			dup=epq.SpectrumUtils.copy(spec)
			if tsIdx % 10 == 0:
				clear()
			display(dup)
			specs.append(wrap(dup))
		if pc:
			fe = updatedPC(interval=(0 if forcePC else 300))
			for spec in specs:
				sp.setNumericProperty(sp.FaradayEnd, fe.average())
		_ts.chamberLed(defLED)
		if fov:
			_ts.setViewField(oldVf)
			_ts.scSetSpeed(oldSp)
		logSpectrum((path if path else defaultPath), acqTime, name, mode)
		if isinstance(comp, epq.Composition):
			report("<p>Collected %d time series spectra <i>%s</i> from %s for %0.1f s %s at %0.1f keV</p>" % (len(specs), name, comp, acqTime, mode, hv))
		else:
			report("<p>Collected %d time series spectra <i>%s</i> for %0.1f s %s at %0.1f keV</p>" % (len(specs), name, acqTime, mode, hv))
		return tuple(specs)
		
	def collect(acqTime=60, name=None, pc=True, mode='L', disp=True, forcePC=False, comp=None, path=None, fov=None):
		"""collect(acqTime=60, name=None, pc=True, mode="L", disp=True, forcePC=False, comp=None, path=None)
	Simultaneously collect an EDS spectrum from each detector for the specified live time or real time.  Name the spectra as specified and display.
pc = True to collect the probe current from the stage position "faraday"
mode = "L"|"R"|"C" for live-time, real-time or counts (changes how acqTime is interpreted)
disp=True to display the spectrum as it is acquired
forcePC = True to force the probe current to be measured with each spectrum rather than once every 5 minutes
comp: If defined as a epq.Composition object then this composition is assigned to the "StandardMaterial" property of the spectrum.
path: An alternative path into which to save the spectrum context data
fov: An optional field of view width to which to set the SEM imaging while collecting the spectrum."""
		global terminated
		if terminated:
			return ()
		# tt = _pt.getTemperature(0)
		# if (tt < -31.3) or (tt > -29.7):
		# 	print "Warning: The detectors may not be at the correct temperature (T=%2.1f != 30.0)" % tt
		start_ts = ju.Date()
		if name == None:
			name = "Spectrum"
		try:
			if (comp == None) and name.endswith(" std"):
				comp = material(name[0:len(name) - 4])
		except:
			comp = None
		name = "%s[%s]" % (name, time().ctime())
		_ts.chamberLed(False)
		stg_pos = _stg.getPosition()
		time.sleep(0.1)
		if pc:
			fb = updatedPC(interval=(0 if forcePC else 300))
		clear()
		specs = [None] * defaultDetCount
		hv = _ts.hvGetVoltage() / 1000.0
		if fov:
			oldVf = _ts.getViewField()
			oldSp = _ts.scGetSpeed()
			_ts.scSetSpeed(2)
			_ts.setViewField(fov)
		if mode == 'R':
			_eds.start(_eds.Mode.RealTime, acqTime)
		elif mode == 'L':
			_eds.start(_eds.Mode.LiveTime, acqTime)
		else:
			_eds.start(_eds.Mode.Counts, acqTime)
		done = False
		while not done:
			time.sleep(jl.Math.min(1.0, 1.3 * acqTime))
			if terminated:
				_eds.stop()
			done = _eds.done()
			if disp or done:
				rep = {}
				for det in range(0, defaultDetCount):
					if availableDets[det]:
						tmp = _eds.getSpectrum(det)
						epq.SpectrumUtils.rename(tmp, "%s[%d]" % (name, det))
						if pc and fb:
							sp = tmp.getProperties()
							sp.setNumericProperty(sp.FaradayBegin, fb.average())
						if specs[det] and disp:
							rep[specs[det]] = tmp
							specs[det] = tmp
						else:
							specs[det] = tmp
							if disp:
								display(tmp)
				if len(rep) > 0:					
					DataManager.replaceSpectra(rep)
		if pc:
			fe = updatedPC(interval=(0 if forcePC else 300))
		for spec in specs:
			if not spec:
				continue
			sp = spec.getProperties()
			sp.setTimestampProperty(epq.SpectrumProperties.AcquisitionTime, start_ts)
			sp.setObjectProperty(epq.SpectrumProperties.StagePosition, stg_pos)
			sp.setNumericProperty(epq.SpectrumProperties.WorkingDistance, _opt.getWorkingDistance())
			if isinstance(comp, str):
				comp = material(comp)
			if isinstance(comp, epq.Composition):
				sp.setCompositionProperty(epq.SpectrumProperties.StandardComposition, comp)
			if pc:
				if fb:
					sp.setNumericProperty(sp.FaradayBegin, fb.average())
				if fe:
					sp.setNumericProperty(sp.FaradayEnd, fe.average())
		_ts.chamberLed(defLED)
		if fov:
			_ts.setViewField(oldVf)
			_ts.scSetSpeed(oldSp)
		logSpectrum((path if path else defaultPath), acqTime, name, mode)
		if isinstance(comp, epq.Composition):
			report("<p>Collected %d spectra <i>%s</i> from %s for %0.1f s %s at %0.1f keV</p>" % (len(specs), name, comp, acqTime, mode, hv))
		else:
			report("<p>Collected %d spectra <i>%s</i> for %0.1f s %s at %0.1f keV</p>" % (len(specs), name, acqTime, mode, hv))
		return tuple(specs)
		
	def collectPoints2(fov, pts, baseName, acqTime=60, pc=True, mode='L', disp=True, path=None):
		"""collectPoints2(fov, pts, baseName, acqTime=60, pc=True, mode='L', disp=True, path=None)
		Collect a series of spectra by moving the beam (in point mode) around the field-of-view as specified by 'pts'.
		'pts' is a list of ( x, y) coordinates in the range [0,1] which specify where in the field of view to place the beam.
		(0,0) is UL, (1,1) is LR."""
		oldVf = _ts.getViewField()
		oldSp = _ts.scGetSpeed()
		try:
			if pc:
				pcb=updatedPC(interval=0)
			collectImages(baseName, fov, dims=(1024,1024),dwell=5,path=path, writeMask=SAVE_IMAGE_MASK)
			_ts.setViewField(fov)
			_ts.scStopScan()
			for i, pnt in enumerate(pts):
				_ts.scSetBeamPosition(pnt[0],pnt[1])
				time.sleep(0.1)
				spec=collect2(acqTime, "%s[Pt %i](%0.3g,%0.3g)" % (baseName, i, pnt[0],pnt[1]), mode='L', pc=False, disp=disp, path=path, fov=None)
				if pc and spec:
					sp = spec.getProperties()
					sp.setNumericProperty(sp.FaradayBegin, pcb.average())
			if pc:
				print updatedPC(interval=0)
		finally:
			_ts.setViewField(oldVf)
			_ts.scSetSpeed(oldSp)
	
	
	def calibrate(det, measured, desired=10.0):
		"""calibrate(det, measured, desired=10.0)
		Calibrates the EDS detector where 'measured' is the measured gain and 'desired' is the desired gain (nominally 10.0 eV/ch)"""
		print "Detector %d:\nBefore: %f" % (det, _pt.getADCGain(det))
		_pt.setADCGain(1 << det, _pt.getADCGain(det) * measured / desired)
		print "After:  %f" % (_pt.getADCGain(det))
		return _pt.getADCGain(det)
		
	def updateCalibration():
		cal=readCalibrations()
		for i in range(0,min(len(cal),len(availableDets))):
			if (cal[i]>1.0) and availableDets[i]:
				_pt.setADCGain(1 << i, cal[i])
				print "Detector %d gain set to %f (%f)" % (i, _pt.getADCGain(i), cal[i])
	
	def logSpectrum(path, acqTime, name, mode):
		"""Used internally to log the acquisition of spectra"""
		ff = jio.File(path, "spectra.txt")
		header = not ff.exists()
		fos = jio.FileOutputStream(ff, True)
		try:
			pw = jio.PrintWriter(fos)
			try:
				if header:
					pw.append("Name\tAcq Time\tMode")
					pw.append("\tX\tY\tZ\tR\tT\tB\n")
				pw.append("%s\t%1.1f\t%s" % (name, acqTime, mode))
				pos = _stg.getPosition()
				pw.append("\t%1.3f\t%1.3f\t%1.3f\t%1.2f\t%1.2f\t%1.2f\n" % (pos.get(X_AXIS), pos.get(Y_AXIS), pos.get(Z_AXIS), pos.get(R_AXIS), pos.get(T_AXIS), pos.get(B_AXIS)))
			finally:
				pw.flush()
		finally:
			fos.close()
		
	def measurePC(restore=True):
		"""measurePC(restore=True) - (Works)
		If the global variable 'faraday' is a stage point, then this function moves to this stage point collects a measurement of the sample current and returns a DescriptiveStatistics object containing the result."""
		global faraday
		global terminated
		if terminated:
			return None
		if globals().has_key("faraday"):
			ds = epu.DescriptiveStatistics()
			prevPos = _stg.getPosition()
			prevVF = _ts.getViewField()
			moveTo(faraday)
			_ts.setViewField(0.0001)
			time.sleep(1.0)
			for i in range(0, 10):
				time.sleep(0.1)
				ds.add(0.001 * _ts.getIAbsorbed())
			print u'Probe current: %f \u00B1 %f nA' % (ds.average(), ds.standardDeviation())
			if restore:
				moveTo(prevPos)
			_ts.setViewField(prevVF)
			return ds
		else:
			return None
	lastPCUpdate = None
	lastPCValue = None
	
	def shouldUpdatePC(interval):
		global lastPCUpdate, lastPCValue
		return globals().has_key("faraday") and ((lastPCUpdate == None) or (abs(time.time() - lastPCUpdate) > interval))
	
	def updatedPC(interval=1800):
		"""updatedPC([interval=1800]) - Works
		Returns a measurement of the probe current, updating the measurement if more than 'interval' seconds have elapsed."""
		global lastPCUpdate, lastPCValue
		if shouldUpdatePC(interval):
			lastPCValue = measurePC()
			lastPCUpdate = time.time()
		return lastPCValue

	def trueRule(tvm):
		return True
	
	def falseRule(tvm):
		return False
		
	def falseTerminate(tvm, zep):
		return False
		
	def oneInN(tvm, n=100):
		return tvm[gzep.APA_PARTNUM] % n == 0
		
	def largerThan(tvm, dMax=10.0):
		return tvm[gzep.APA_DMAX] > dMax
		
	def fieldNumber(tvm):
		return tvm[gzep.APA_FIELDNUM]
		
	def particleNumber(tvm):
		return tvm[gzep.APA_PARTNUM]

def buildZepp(project, sample, index=0, dataFile="data.hdz"):
	"""buildZepp(project, sample, index=0, dataFile="data.hdz")
Construct a Zepp object corresponding to the specified project and sample.  The index specifies which timestamped analysis to read.  The default
index of 0 loads the most recent, index=1, 2, 3,... first, second third...., index = -1, -2,... second from last, third from last,...."""
	path="%s//Projects//%s//%s//APA" % ( rootPath, project, sample )
	dirs = []
	for f in os.listdir(path): 
		ff = path+os.path.sep+f 
		if os.path.isdir(ff) and f.startswith("Analysis "):
			dirs.append(f)                   
	dirs.sort()
	if index<=0:
		return Zepp(path+os.path.sep+dirs[-1+index]+os.path.sep+dataFile)
	else:
		return Zepp(path+os.path.sep+dirs[index-1]+os.path.sep+dataFile)        
		
class Zepp:
	"""The Zepp class is designed as a wrapper around a Zeppelin (AFA) data set.
Various methods allow you to reload data and acquire relocated images, spectra and spectrum images."""
	
	def __init__(self, zepFile):
		if isinstance(zepFile, jio.File):
			self._path=zepFile
			self._zep=None
		if isinstance(zepFile, str):
			zepFile = jio.File(zepFile)
			assert zepFile.isFile()
			self._path = zepFile
			self._zep = None
		elif isinstance(zepFile, graf.Zeppelin):
			self._path=zepFile.getFile()
			self._zep = zepFile
			self._zep.remapParticleImageFiles(self._path.getParentFile())
		
	def getZ(self):
		"""z.getZ()
Returns the Zeppelin Java object associated with this Zepp object."""
		if not self._zep:
			self._zep = gzep(self._path)
			self._zep.remapParticleImageFiles(self._path.getParentFile())
		return self._zep
		
	def __str__(self):
		collected = self._path.getParentFile()
		sample = collected.getParentFile()
		project = collected.getParentFile()
		return "Data for " + sample.getName() +" from project "+project.getName() + " collected at "+collected.getName();
		
	def __len__(self):
		return self.getZ().getParticleCount()
		
	def getRelocated(self):
		"""z.getRelocated()
Returns a Java IO File object representing Relocated directory."""
		if not self._path.exists():
			raise "No such Zeppelin file."
		f=jio.File(self._path.getParentFile(), "Relocated")
		if not f.exists():
			f.mkdirs()
		return f

	def SItoMap(self, partNum, vecs, postFix=None):
		"""z.SItoMap(partNum, vecs, postfix=None)
Process a spectrum image into maps.  vecs is constructed using buildVectors(...)"""
		fn = "P%0.5d%s" % (partNum , ("" if not postFix else " - "+postFix), )
		SItoMap(fn, vecs, path=self.getRelocated())

	def SItoSum(self, partNum, mask=None, subSample=4, postFix=None):
		"""z.SItoMap(partNum, [mask=None],[subSample=4])
Process a spectrum image into a masked sum. see help(SItoSum)"""
		fn = "P%0.5d%s" % (partNum , ("" if not postFix else " - "+postFix), )
		SItoSum(fn, mask, subSample, path=self.getRelocated())
		
	def SItoSums(self, partNum, masks=None, subSample=4, postFix=None):
		"""z.SItoMap(partNum, [mask=None],[subSample=4])
Process a spectrum image into masked sum spectra. see help(SItoSums)"""
		fn = "P%0.5d%s" % (partNum , ("" if not postFix else " - "+postFix), )
		SItoSums(fn, mask, subSample, path=self.getRelocated())

	def SItoRPL(self, partNum, postFix=None):
		"""z.SItoRPL(partNum)
Process a spectrum image into masked sum spectra. see help(SItoSums)"""
		fn = "P%0.5d%s" % (partNum , ("" if not postFix else " - "+postFix), )
		SItoRPL(fn, path=self.getRelocated())
		
	def display(self, rowNum):
		"""z.display(rowNum)
Display the particle spectrum and images in rowNum."""
		display(self.get(rowNum))
		
	def get(self, partNum):
		"""z.get(partNum)
Get the spectrum associated with the specified row number"""
		if (partNum<1) or (partNum>len(self)):
			raise "Part number out of bounds..."
		z = self.getZ()
		res = wrap(z.getSpectrum(partNum - 1, True))
		res.getProperties().setDetector(pt_det_all)
		col =  z.findToken(graf.Zeppelin.APA_CLASS)
		if col<>graf.Zeppelin.NO_COLUMN:
			res.rename("%s - %s" % (res, z.getDatumAsString(partNum-1, col)))
		return res

	def quantify(self, stds, e0 = 25.0, strip=["O", "C"], stdsPath = None):
		"""z.quantify(stds,[strip="0","C"], [stdsPath = defaultVecPath])
		where stds = { "Cu":"Cu std.msa", "Fe":"Fe std.msa", ...}
		Returns a new Zepp object representing the requantified particle data."""
		mllsq = epq.MLLSQSignature(pt_det_all, epq.ToSI.keV(e0))
		mllsq.clearStripped()
		mllsq.addStripped(map(element, strip))
		stdsPath = (stdsPath if stdsPath else defaultVecPath)
		for elm, std in stds.iteritems():
			if isinstance(std, str):
				std=readSpectrum("%s/%s" % (stdsPath, std))
			comp = std.getProperties().getCompositionProperty(epq.SpectrumProperties.StandardComposition)
			# print "Adding %s as standard for %s" % (std, elm)
			mllsq.addReference(element(elm), comp, std)
		pm=jxs.ProgressMonitor(MainFrame,"Please wait while the data is quantified", "Quantifying",0,100)
		res = Zepp(self.getZ().applySignature(mllsq, 1.0e8, pm))
		pm.close()
		return res
		
	def save(self, filename):
		"""z.save(filename)
		Save the Zeppelin file to the specified file."""
		self.getZ().write(filename)

	def classify(self, ruleSet):
		"""z.classify(ruleSet)
		Classify the Zeppelin data set using the specified rules"""
		self.getZ().evaluateClass(ruleSet)
		
	def archive(self, dest=defaultArchivePath):
		"""z.archive(destPath)
		Copies data files to 'path'"""
		from shutil import copytree # copytree(src, dest)
		if dest:
			src = self._path.getParentFile()
			if not src.isDirectory():
				print "ERROR: Source directory does not exist."
				return
			# src = "root"+"/project"+"/sample"+"/apa"+"/timestamp"
			apa = src.getParentFile()
			sample = apa.getParentFile()
			proj = sample.getParentFile()
			destF=jio.File(jio.File(jio.File(jio.File(dest, proj.getName()),sample.getName()),apa.getName()),src.getName())
			if destF.isDirectory():
				print "ERROR: Destination directory already exists - %s" % (destF.getAbsolutePath())
				return
			destF.getParentFile().mkdirs()
			copytree(src.getAbsolutePath(), destF.getAbsolutePath())
			print "%s archived to %s" % (src, destF)
			return

	if connect and _ts.hasRCALicense():
		def updatePC(self):
			"""z.updatePC()
	Updates the probe current if necessary and then asks to recenter particle"""
			if shouldUpdatePC(300):
				lastPCUpdate = None
				updatedPC(interval=300)
				if jop.showConfirmDialog(MainFrame, "Centered and focused?", "Relocate", jop.YES_NO_OPTION) <> jop.YES_OPTION:
					return
		
		def relocate(self, partNum, liveTime, fov, edsFov=0.001, dims=(256,256), dwell=6):
			"""z.relocate(partNum, liveTime, fov, edsFov=0.001, dims=(256, 256), dwell=6)
	An easy method to save a relocated image/spectrum in a TIFF file so that Graf can find and load it."""
			rel = self.getRelocated().getAbsolutePath()
			fn = "%0.5d" % (partNum , )
			self.updatePC()
			imgs=collectImages(fn, fov=fov, dims=dims, dwell=dwell, path=rel, writeMask=SAVE_IMAGE_MASK)
			spec=collect2(liveTime, name=fn, fov=edsFov)
			if spec:
				props = spec.getProperties()
				if len(imgs) > 0:
					props.setImageProperty(props.MicroImage, imgs[1])
				if len(imgs) > 1:
					props.setImageProperty(props.MicroImage2, imgs[0])
			write(spec, fn, rel )	# msa
			write(spec, fn, rel, fmt="tif")
			
		def collect(self, liveTime, partNum, postFix=None, fov=None):
			"""z.collect(liveTime, partNum, postFix="")
	Collects and saves a spectrum associated with the specified particle number in the 'Relocated' directory. postFix is appended on the filename to allow uniquely labeling inclusions etc."""
			rel = self.getRelocated()
			fn = "%0.5d%s" % (partNum , ("" if not postFix else " - "+postFix), )
			self.updatePC()
			spec=collect2(liveTime, name=fn, fov=fov)
			write(spec, fn, rel.getAbsolutePath() )
			
		def collectSI(self, partNum, fov, dwell=9, dims=(1024,1024), rotation=0.0, postFix=None):
			"""z.collectSI(partNum, fov, dwell=9, dims=(1024,1024), postFix="")
	Collects a spectrum image of a relocated particle 'partNum' with the specified field-of-view (fov), dims and dwell.
	The images are saved in the RCA project directory under 'Relocated'"""			
			path = self.getRelocated().getAbsolutePath()
			fn = "P%0.5d%s" % (partNum , ("" if not postFix else " - "+postFix), )
			collectSI(fn, fov, frameCount=1, dwell=dwell, dims=dims, path=path, rotation=rotation)
		
		def collectImages(self, partNum, fov, dims=(512,512), dwell=6, postFix=None, markCenter=False, writeMask=SAVE_IMAGE_MASK):
			"""z.collectImages(partNum, fov, dims=(512,512), dwell=5, postFix="")
	Collects images of a relocated particle 'partNum' with the specified field-of-view (fov), dims and dwell.
	The images are saved in the RCA project directory under 'Relocated'"""
			path = self.getRelocated().getAbsolutePath()
			fn = "%0.5d%s" % (partNum , (" - I" if not postFix else " - "+postFix), )
			collectImages(fn, fov=fov, dims=dims, dwell=dwell, path=path, writeMask=writeMask, markCenter=markCenter)
			
		def collectZoom(self, partNum, fov, dims=(512,512), dwell=6, writeMask=SAVE_IMAGE_MASK):
			"""z.collectImages(partNum, fov, dims=(512,512), dwell=5, postFix="")
	Collects images of a relocated particle 'partNum' with the specified field-of-view (fov), dims and dwell.
	The images are saved in the RCA project directory under 'Relocated'"""
			for fv in [ fov, 0.1, 1.0]:
				self.collectImages(partNum, fv, dims, dwell, "FOV = %g" % fv, markCenter=True, writeMask=writeMask)
		
		def moveTo(self, partNum):
			"""z.moveTo(self, rowNum)
	Move the stage to the particle at rowNum."""
			if (partNum<1) or (partNum>len(self)):
				raise "Part number out of bounds..."
			sc=self.getZ().getStageCoordinate(partNum-1)
			moveTo(sc, xyOnly=True)
			
		def translate(self):
			"""z.translate()
	Use the coordinate relocation dialog to translate the particle coordinates into a new rotated/translated coordinate system."""
			rd = ept.RelocationDialog(MainFrame, True)
			rd.setLocationRelativeTo(MainFrame)
			rd.setVisible(True)
			res = rd.getResult()
			if res:
				print "Setting the coordinate transform to "+res.toString()
				self.getZ().setTranslation(res)
		
		
if connect and _ts.hasRCALicense():

		POINT_MODE = "Point mode EDS"
		FIXED_SPACING = "Fixed spacing EDS"
		FIXED_DIM = "Fixed dimension EDS"
		CHORD_RASTER = "Chord raster EDS"

		class RCA:
			"""The RCA class implements rotating chord particle analysis."""
			
			def __init__(self, project, sample, analyst, analysis=None, path=None):
				"""RCA(project, sample, [analysis])
	Creates an object for performing rotating chord-style automated particle analysis using the default settings for search, measure and other configuration settings.			
	   project - The name of the project with which this analysis is associated
	   sample - The name of the sample on which this analysis is being performed
	   analysis - An optional name for the analysis (Otherwise a date stamp is used.)"""
				self._project = project
				self._sample = sample
				self._analyst = analyst
				if not analysis:
					analysis = "Analysis "+jtext.SimpleDateFormat("yyyy-MM-dd HH.mm.ss.Z").format(ju.Date())
				self._analysis = analysis
				if not path:
					path = "%s\\Projects\\%s\\%s\\APA\\%s" % (rootPath, self._project, self._sample, self._analysis)
				self._path = path
				if not jio.File(self._path).mkdirs():
					raise "Unable to create data output directory: %s" % self._path
				self._transform = semss.RCATransform(_ts)
				self.selectDetector("BSED")
				self._blackLevel=None
				self._gain=None
				self._searchLow = 128
				self._searchHigh = 255
				self._searchDwell = 4000
				self._maxPart = 100000
				self._maxPartPerField = self._maxPart
				self._measureLow = 64
				self._measureHigh = 255
				self._measureDwell = 4000
				self._measureStep = 8
				self._beamIntensity = None
				self.configEDS(None, None, 0.4)
				self.setMorphologyCriterion(semtr.RcaTranslator.AreaCriterion(10.0, 1.0e6))
				self.setFieldOfView(0.1, 9)
				self._zep = None
				self._debug = False
				self._collectImages = False
				self._collectPartImages = trueRule
				self._terminateAnalysis = falseTerminate
				self._pImgDim = 64
				self._pImgStack = []
				self._pSIStack = []
				self._collectSI = falseRule
				self._processSIStack = None
				self._SIDwell = 10
				self._SIDim = 64
				self._EDSMode = POINT_MODE
				self._ParticleCount = 0
				self._debug=False
				self._debugPw = None
				self._stageZ = None
				
			def setSearchThreshold(self, low, high=255, dwell=4000, maxPart=100000, maxPartPerField = 10000):
				"""SetSearchThreshold(low,[high],[dwell],[maxPart])
	Specify search parameters.
	   low - Low level intensity threshold [0,high)
	   high - High level intensity threshold [low+1,255] (nominal 255)
	   dwell - Pixel measurement dwell in ns (nominal 4000)
	   maxPart - Maximum number of particles per field"""
				self._searchLow = low
				if self._measureLow > low:
					self._measureLow = low
				if self._measureHigh < high:
					self._measureHigh = high
				self._searchHigh = high
				self._searchDwell = dwell
				self._maxPart = maxPart
				self._maxPartPerField = maxPartPerField
				
			def setMeasureThreshold(self, low, high=255, dwell=4000, measureStep=8):
				"""SetMeasureThreshold(low,[high],[dwell],[measureStep])
	Specify measure parameters.
	   low - Low level intensity threshold [0,high)
	   high - High level intensity threshold [low+1,255] (nominal 255)
	   dwell - Pixel measurement dwell in ns (nominal 4000)
	   measureStep - Particle measurement step size (nominal 8)"""
				self._measureLow = low
				if self._searchLow < low:
					self._searchLow = low
				if self._searchHigh > high:
					self._searchHigh = high
				self._measureHigh = high
				self._measureDwell = dwell
				self._measureStep = measureStep
				
			def collectImages(self, particle=trueRule, pImgDim=64, field=False, imgDim=1024):
				"""collectImages([particle=True],[pImgDim=64],[field=False],[imgDim=1024]
	Specify which optional types of particles to collect.
		particle - Collect an image of each particle? "particle" is a function which takes as an argument a dictionary mapping particle properties into values. If the function evaluates True then an image is collected.
		pImgDim - Dimension of the particle image in pixels (square image)
		field - Collect an image of each stage field? 
		imgDim - Dimensions of the field image in pixels"""			
				self._collectImages = field
				self._imgDim = imgDim
				self._collectPartImages = (particle if not isinstance(particle, bool) else (trueRule if particle else falseRule))
				self._pImgDim = pImgDim
				
			def setStageZ(self, stageZ):
				"""setStageZ(stageZ)
	Specifies how the stage Z position is determined.  If 'stageZ' is a number, then the z-stage position is held fixed at z=stageZ.  //
		'stageZ' could be a function taking a single argument that represents an X-Y position.  The function is then responsible //
		for computing the z stage position."""
				self._stageZ = stageZ
				
			def moveStage(self, pos):
				z = None
				if isinstance(self._stageZ, float):
					z=self._stageZ
				elif callable(self._stageZ):
					z=self._stageZ(pos)
				if z:
					pos.set(Z_AXIS, z)
				# print pos
				_stg.moveTo(pos)
					
			def configEDS(self, vecs, rules=None, realTime=0.4, mode=POINT_MODE):
				"""configEDS(vecs, [rules=None], [realTime=0.4])
	Configure EDS acquisition parameters.
		vecs - Specify a vector set to process the incoming x-ray data (if vecs == None then no x-ray data is collected.)
		rules - Specify a rule set to assign particles to a final class (post EDS)
		realTime - EDS acquisition real time in seconds"""
				self._edsRealTime = realTime
				self._edsDwell = 100000
				self._collectEDS = (vecs <> None)
				self._vecs = (vecs if self._collectEDS else None)
				self._rules = rules
				self._EDSMode = mode
				
			def configSI(self,  collectSIFunc, dwell=10, dim=64):
				"""configSI(collectSIFunc, dwell=10, dims=64)
	Configure the optional acquisition of a x-ray spectrum image. /
	If the function collectSIFunc(tvm) evaluates true then a spectrum image with the specified dwell and max dimensions (dim) /
	will be collected following the end-of-frame."""
				self._collectSI = collectSIFunc
				self._SIDwell = dwell
				self._SIDim = dim
				self._processSIStack = SIProcessor()
				self._processSIStack.start()

			
			def setMorphologyCriterion(self, crit):
				"""setMorphologyCriterion(crit)
	Specify a RCATranslator.ParticleCriterion derived class to determine whether to measure a particle based on morphology criteria.
crit - An instance of a RCATranslator.ParticleCriterion derived class.  The functions linearCriterion(..), 
areaCriterion(...), maxCriterion(...) build common criteria.""" 			
				self._morphologyCrit = crit
				
			def setFieldOfView(self, fov, overlap=1.0, ppmRes=11):
				"""setFieldOfView(fov, [overlap=1.0], [ppmRes=11])
	Specify the analysis field size in mm.
		fov - Field-of-view size in mm
		overlap - <1 to overlap fields and >1 to leave a border around fields
		ppmRes - Specifies the size of the search grid (grid dimensions = 2^ppmRes).  Add 1 to account for borders."""		
				self._fov = fov
				self._overlap = (overlap if overlap>0.1 and overlap<10.0 else 1.0)
				self._rcaFov = self._transform.rcaFov(fov) 
				self._ppmResolution = ppmRes
				
			def searchDimension(self):
				"""searchDimension()
	Returns the dimenions of the search grid in pixels."""
				return pow(2, self._ppmResolution-1)
				
			def selectDetector(self, det="BSED"):
				"""selectDetector([det="BSED"])
	Specify which detector is used to perform search and measure.
		det = "SED" or "BSED" """
				self._imgDet = (0 if det[0] == 'S' else 1)
				
			def getDetector(self):
				"""getDetector()
	Returns the name of the detector used to perform search and measure."""
				return "BSED" if self._imgDet == 1 else "SED"
			
			def setBrightnessContrast(self, brightness, contrast):
				self._blackLevel=brightness
				self._gain=contrast
				
			def getBrightnessContrast(self):
				res=_ts.dtGetGainBlack(self._imgDet)
				self._gain=res[0]
				self._blackLevel=res[1]
				
			def setBeamIntensity(self, bi):
				self._beamIntensity = bi
			
			def buildZep(self):
				"""Internal use only....
	buildZep() - Build a Zeppelin data file to contain the specified analysis with associated vector set."""
				self._zep = gzep()
				z = self._zep
				z.addColumn(gzep.PARTNUM, u"1" , zct.INT16, 0)
				z.addColumn(gzep.FIELDNUM, u"1", zct.INT16, 0)
				z.addColumn(gzep.MAGFIELDNUM, u"1", zct.INT16, 0)
				z.addColumn(gzep.XABS, u"mm", zct.FLOAT, 0.0)
				z.addColumn(gzep.YABS, u"mm", zct.FLOAT, 0.0)
				z.addColumn(gzep.XCENT, u"", zct.INT16, 0.0)
				z.addColumn(gzep.YCENT, u"", zct.INT16, 0.0)
				z.addColumn(gzep.XFERET, u"µm", zct.FLOAT, 0.0)
				z.addColumn(gzep.YFERET, u"µm", zct.FLOAT, 0.0)
				z.addColumn(gzep.DAVE, u"µm", zct.FLOAT, 0.0)
				z.addColumn(gzep.DMAX, u"µm", zct.FLOAT, 0.0)
				z.addColumn(gzep.DMIN, u"µm", zct.FLOAT, 0.0)
				z.addColumn(gzep.DPERP, u"µm", zct.FLOAT, 0.0)
				z.addColumn(gzep.ASPECT, "1", zct.FLOAT, 0.0)
				z.addColumn(gzep.AREA, u"µm", zct.FLOAT, 0.0)
				z.addColumn(gzep.PERIM, u"µm", zct.FLOAT, 0.0)
				z.addColumn(gzep.ORIENT, u"°", zct.FLOAT, 0.0)
				z.addColumn(gzep.MAG, u"1", zct.INT16, 0)
				z.addColumn(gzep.VIDEO, u"1", zct.INT16, 0)
				if self._rules:
					z.addColumn(gzep.CLASS, u"1", zct.INT16, 0)
					z.setHeaderItems(self._rules)
				if self._vecs <> None:
					vecs = self._vecs.getVectors()
					z.addColumn(gzep.ATOMICNUM1, u"1", zct.INT16, 0)
					z.addColumn(gzep.ATOMICNUM2, u"1", zct.INT16, 0)
					z.addColumn(gzep.ATOMICNUM3, u"1", zct.INT16, 0)
					z.addColumn(gzep.ATOMICNUM4, u"1", zct.INT16, 0)
					z.addColumn(gzep.PCT1, u"%(k)", zct.FLOAT, 0.0)
					z.addColumn(gzep.PCT2, u"%(k)", zct.FLOAT, 0.0)
					z.addColumn(gzep.PCT3, u"%(k)", zct.FLOAT, 0.0)
					z.addColumn(gzep.PCT4, u"%(k)", zct.FLOAT, 0.0)
					z.addColumn(gzep.EDSTIME, u"s", zct.FLOAT, 0.0)
					z.addColumn(gzep.TOTALCOUNTS, u"1", zct.INT16, 0)
					z.addColumn(gzep.TYPE4ET, u"1", zct.INT16, 0)
					elms = []
					for vec in vecs:
						z.addColumn(vec.getElement().toAbbrev(), u"%(k)", zct.FLOAT, 0.0)
						elms.append(vec.getElement())
					z.setHeaderItems(elms)
				imgDir = jio.File(self._path)
				imgDir.mkdirs()
				z.remapParticleImageFiles(imgDir)
				z.setHeaderItem("OPERATOR", self._analyst)
				z.setHeaderItem("ACCELERATING_VOLTAGE", "%4g kV" % (_ts.hvGetVoltage() / 1000.0))
				z.setHeaderItem("DESCRIPTION", "%s %s" % (self._project, self._sample))
				dt = ju.Date()
				z.setHeaderItem("ANALYSIS_DATE", jtext.SimpleDateFormat("MM/dd/yyyy").format(dt))
				z.setHeaderItem("START_TIME", jtext.SimpleDateFormat("hh:mm:ss a z").format(dt))
				z.setHeaderItem("SAMPLE", self._sample)
				z.setHeaderItem("PROJECT", self._project)
				z.setHeaderItem("COMPANY", "DOC NIST MML - 637.02")
				z.setHeaderItem("INSTRUMENT", "NIST's TESCAN MIRA3 in 217 F101")
				z.setHeaderItem("PROBE_CURRENT", "%g nA" % (updatedPC(300).average() , ) )
				z.setHeaderItem("RULE_FILE", u"%s" % ( self._rules, ))
				z.setHeaderItem("VEC_FILE", u"%s"  % (self._vecs, ))
				z.setHeaderItem("STAGE_FILE", "tiling.xml")
				z.setHeaderItem("TILING", tiling.toString())
				z.setHeaderItem("TECHNIQUE", "RCA - "+self._EDSMode)
				z.setHeaderItem("WORKING_DISTANCE", "%g" % (_ts.getWD()))
				z.write(self._path + "/data.hdz")
				_afafb.setZeppelin(self._zep)
				self._ParticleCount=0

			def addParticle(self, datum, field):
				"""Internal use only....
	addParticle(self, datum, field)
		Perform the bookkeeping associated with a new particle."""
				if self._zep == None:
					self.buildZep()
				z = self._zep
				termP=False
				if datum and isinstance(datum, semdi.RcaDatum) and not(datum.isDuplicate() or datum.isDiscarded()):
					scale = 1.0e6
					vals = []
					pIdx=z.getParticleCount() + 1
					self._ParticleCount = pIdx
					vals.append(pIdx)
					vals.append(field) # FIELDNUM
					vals.append(1) # MAGFIELDNUM
					stgPos = datum.getParticlePosition()
					ct = datum.getCentroid()
					vals.append(stgPos.get(X_AXIS))
					vals.append(stgPos.get(Y_AXIS))
					vals.append(ct.x)
					vals.append(ct.y)
					feret = datum.getFeret()
					vals.append(feret[0] * scale)
					vals.append(feret[1] * scale)
					vals.append(datum.dAvg() * scale)
					vals.append(datum.dMax() * scale)
					vals.append(datum.dMin() * scale)
					vals.append(datum.dPerp() * scale)
					vals.append((datum.dMax() / datum.dMin() if datum.dMin() > 0 else 1.0))
					vals.append(datum.area() * scale * scale)
					vals.append(datum.perimeter() * scale)
					vals.append(datum.orientation())
					vals.append(datum.getMag()) # mag
					vals.append(datum.meanIntensity())
					if self._rules:
						vals.append(0) # CLASS (set below...)
					spec, ps = None, None
					macro = datum.getMacroImage(32)
					spec = datum.getSpectrum()
					if self._vecs and spec:
						totalCx=epq.SpectrumUtils.totalCounts(spec, True)
						if totalCx==0:
							print "No x-ray counts for particle %d" % pIdx
							ff = jio.File(self._path, "RCADatum[%d].txt" % pIdx)
							fos = jio.FileOutputStream(ff, True)
							wr = jio.PrintWriter(fos)
							datum.dump(wr)
							wr.close()
						kr = self._vecs.getKRatios(spec)
						props = spec.getProperties()
						props.setKRatioProperty(epq.SpectrumProperties.KRatios, kr)
						ps = epq.ParticleSignature(kr, [epq.Element.C], [epq.Element.O])
						props.setParticleSignatureProperty(epq.SpectrumProperties.ParticleSignature, ps)
						props.setImageProperty(epq.SpectrumProperties.MicroImage, ps.createBarGraph(256,0.01))
						props.setNumericProperty(epq.SpectrumProperties.RealTime, self._edsRealTime)
						elms = tuple(ps.getNthElement(i) for i in range(0, 4))
						for i in range(0, 4):
							vals.append(elms[i].getAtomicNumber())
						for i in range(0, 4):
							vals.append(ps.get(elms[i]) * 100.0)
						vals.append(self._edsRealTime) #EDSTIME
						vals.append(totalCx)
						vals.append(0) # TYPE4ET
						klmElms=[]
						for elm in self._vecs.getElements():
							vals.append(ps.get(elm) * 100.0)
							if ps.get(elm)>0.01:
								klmElms.append(elm)
					else:
						props.setImageProperty(epq.SpectrumProperties.MicroImage, macro)
					pNum = z.addRow(vals) 
					row = pNum - 1 
					className = "Unclassified"
					tvm = z.getParticleData(row)
					className = "Unclassified"
					if self._rules:
						rule = self._rules.evaluate(tvm)
						z.setDatumByToken(row, graf.Zeppelin.APA_CLASS, rule)
						className = self._rules.ruleName(rule)
					acqPImage = self._collectPartImages(tvm)
					termP = self._terminateAnalysis(tvm, z)
					if acqPImage:
						self.collectParticleImage(pNum, datum.bounds(), spec)
					if self._collectSI(tvm):
						self.collectPartSI(pNum, datum.bounds())
					if spec:
						epq.SpectrumUtils.rename(spec, "P%0.4d - %s" % (pNum, className))
						spec.getProperties().setObjectProperty(epq.SpectrumProperties.StagePosition, stgPos)
						if not acqPImage: # Write spectrum
							z.writeSpectrum(spec, pNum)
					elif macro: # Image only
						write(macro, "%0.5d", path="%s/mag0" % (self._path), fmt="tif")
					if spec:
						clear()
						display(spec)
						MainFrame.showKLMLines(klmElms)
					if row>=0:
						_afafb.update(row)
					# print "Particle: %d - %.2g um (average diameter) of %s" % (pNum, datum.dAvg() * scale, className)
				return termP
				
			def collectParticleImage(self, pNum, bounds, spec):
				"""Internal used only...			
	collectParticleImage(pNum, bounds, spec):
		Collect a particle image."""
				xtra = bounds.width/2
				expBounds = jawt.Rectangle(bounds.x - xtra, bounds.y - xtra, bounds.width + 2 * xtra, bounds.height + 2 * xtra)
				ra = self._transform.rcaToSubraster(expBounds, self._pImgDim)
				self._pImgStack.append( (pNum, self._rcaFov, ra.getImageDimensions(), ra.getSubRaster(), spec) )
				
			def collectPartSI(self, pNum, bounds):
				"""Internal use only..."""
				xtra = bounds.width / 4
				expBounds = jawt.Rectangle(bounds.x - xtra, bounds.y - xtra, bounds.width + 2 * xtra, bounds.height + 2 * xtra)
				ra = self._transform.rcaToSubraster(expBounds, self._pImgDim)
				self._pSIStack.append( (pNum, self._rcaFov, ra.getImageDimensions(), ra.getSubRaster()) )
			
			def path(self):
				return self._path
				
			def setTerminateRule(self, termRule):
				"""setTerminateRule(termRule)
	Sets a function that can termininate the analysis prematurely by returning True.
	def termRule(tvm) where tvm is a token-value map."""
				self._terminateAnalysis = termRule
						
			def collectField(self, fieldNumber):
				"""Internal used only...
	collectField(fieldNumber)
	Search and measure the particles in this field."""						
				# Create the field map...
				global terminated
				if terminated:
					return
				termP=False
				jl.System.gc()
				mapSize = int(self._imgDim * self._transform.imageScale())
				map = jai.BufferedImage(mapSize, mapSize, jai.BufferedImage.TYPE_3BYTE_BGR)
				mg = map.createGraphics()
				ri = self._transform.getImageRectangle()
				af = jag.AffineTransform(float(mapSize) / float(ri.width), 0.0, 0.0, float(mapSize) / float(ri.height), -float(ri.x) * float(mapSize) / float(ri.width), -float(ri.y) * float(mapSize) / float(ri.height))
				mg.setTransform(af)
				mg.setColor(jawt.Color.yellow)
				mg.setComposite(jawt.AlphaComposite.getInstance(jawt.AlphaComposite.SRC_OVER, 0.5))
				mg.setFont(jawt.Font("Serif", jawt.Font.PLAIN, 800))
				# mg.setRenderingHint(jawt.RenderingHint.KEY_ANTIALIASING, jawt.RenderingHint.VALUE_ANTIALIAS_ON)
				# mg.setRenderingHint(jawt.RenderingHint.KEY_TEXT_ANTIALIASING, jawt.RenderingHint.VALUE_ANTIALIAS_ON)
				self.debug("CollectField 00")
				try:
					scale = 0.001 * _ts.getViewField() / float(ri.width)
					if self._collectEDS:
						rt = semtr.RcaTranslator(_sem, pt_det_all, scale)
					else:
						rt = semtr.RcaTranslator(_sem, scale)	
					if self._debug:
						debug = jio.PrintStream(jio.File(self._path, "debug.txt"))
						rt.setDebugStream(debug)
					rt.setMorphologyCriterion(self._morphologyCrit)
					rt.setStagePosition(_stg.getPosition())
					_sem.add(rt)
					r = self._transform.getRCARectangle()
					rca = semss.RCAConfiguration(self._imgDet, r.width / 2, self._ppmResolution, r, self._searchDwell, self._searchLow, self._searchHigh, self._measureDwell, self._measureLow, self._measureHigh, self._measureStep)
					mg.draw(rca.getRegion())
					if self._collectImages:
						ra = self._transform.rcaToSubraster(r, self._imgDim)
						imgs = collectImages("%04d" % (fieldNumber+1, ), self._rcaFov, dims=ra.getImageDimensions(), dwell=3, path="%s/FIELDS" % self._path, subRaster=ra.getSubRaster(), rotation=0.0, writeMask = SAVE_FIELD_MASK)
						ii = imgs[1]
						mg.drawImage(ii, r.x, r.y, r.x + r.width, r.y + r.height, 0, 0, ii.getWidth(), ii.getHeight(), None)
						_afafb.updateFieldImage(map)
					_ts.rcaInit(rca)
					if self._collectEDS:
						if self._EDSMode==POINT_MODE:
							_ts.rcaEDXPoint(self._edsDwell)
						elif self._EDSMode==FIXED_SPACING:
							_ts.rcaEDXFixedSpacing(self._edsDwell, 8, 0, 0)
						elif self._EDSMode==FIXED_DIM:
							_ts.rcaEDXFixedDimension(self._edsDwell, 64, 0, 0)
						elif self._EDSMode==CHORD_RASTER:
							_ts.rcaEDXChordRaster(self._edsDwell, self._measureLow, self._measureHigh)
						_sem.initMapping()
					if self._blackLevel and self._gain:
						_ts.dtSetGainBlack(self._imgDet, self._gain,self._blackLevel)
					if self._beamIntensity:
						_ts.setPCContinual(21.0-self._beamIntensity)
					nPart=0
					self.debug("CollectField 01")
					_ts.rcaNextParticle(False, int(self._edsRealTime * 1000))
					self.debug("CollectField 02")
					termP=False
					while (rt.getStatus() != semtr.RcaTranslator.RCAStatus.AllComplete) and (self._ParticleCount<self._maxPart):
						datum = _sem.poll()
						if datum and isinstance(datum, semdi.RcaDatum):
							self.debug("CollectField 03 : "+str(datum))
							if not (datum.isDiscarded() or datum.isDuplicate()):
								nPart=nPart+1
							if not datum.isDuplicate():
								datum.draw(mg, self._ParticleCount)
							termP = self.addParticle(datum, fieldNumber)
						else:
							_afafb.updateFieldImage(map)
							jl.Thread.sleep(10)
						if terminated or termP:
							break
						if nPart >= self._maxPartPerField:
							break
				finally:
					self.debug("CollectField 04")
					if self._collectEDS:
						_sem.finalizeMapping()
					datum = _sem.poll()
					while datum:
						if datum and isinstance(datum, semdi.RcaDatum):
							if (self._ParticleCount < self._maxPart) and (nPart < self._maxPartPerField):
								if not (datum.isDiscarded() or datum.isDuplicate()):
									nPart=nPart+1
								datum.draw(mg, self._ParticleCount)
								self.addParticle(datum, fieldNumber)
								_afafb.updateFieldImage(map)
						datum = _sem.poll()
					self.debug("CollectField 05")
					self._zep.setHeaderItem("MAG_FMT","Mag Fields Particles Time Area")
					self._zep.setHeaderItem("MAG0","%0.0f %d %d %0.2f %0.3f" % 
						(3.5*25.4/self._fov, fieldNumber, self._ParticleCount, self._timer.inSeconds()/60.0, self._fov*self._fov*fieldNumber ))
					self._zep.setHeaderItem("LOW_MAG","0")
					self._zep.setHeaderItem("MAGNIFICATIONS","1")
					self._zep.write(self._zep.getFile().toString())
					_sem.remove(rt)
					_ts.rcaFinish()
					write(map, "%0.4d" % fieldNumber, path="%s/FIELDS" % self._path)
					if self._debug:
						debug.close()
					# mg.discard()
				try:
					self.debug("CollectField 06")
					for pNum, rcaFov, pixDim, sr, spec in self._pImgStack:
						self.debug("CollectField 07: Particle %d - dim = %s, sr = %s" % ( pNum, pixDim, sr ))
						imgs = collectImages(None, fov=rcaFov, dims=pixDim, dwell=4, subRaster=sr, path="%s/mag0" % (self._path,))
						if imgs:
							self.debug("CollectField 07: P%i imgs" % (pNum, ))
							_afafb.updateParticleImage(imgs[0])
							if spec:
								props = spec.getProperties()
								if len(imgs) > 1:
									props.setImageProperty(props.MicroImage, imgs[1]) # BSED
									props.setImageProperty(props.MicroImage2, imgs[0]) # SED
								else:
									props.setImageProperty(props.MicroImage,imgs[0])
								self._zep.writeSpectrum(spec, pNum)
							else:
								self.debug("CollectField 07: Missing image for P%i" % (pNum, ))
								print "Missing spectrum for particle %d" % ( pNum )
					self.debug("CollectField 08")
				finally:
					self._pImgStack = []
				try:
					for pNum, rcaFov, pixDim, sr in self._pSIStack:
						siName = "SI%d" % pNum
						siPath = "%s/si" % self._path
						si = collectSI(siName, rcaFov, frameCount=1, dwell=self._SIDwell, dim=pixDim, subRaster=sr, path=siPath)
						self._processSIStack.add(siName, siPath, self._vecs)
				finally:
					self._pSIStack = []
				self.debug("CollectField 07")
				return termP
			
			def summarize(self, tiling=None):
				"""Internal use only...
	summarize(tiling)
	Summarize the analysis configuration to the command output window."""
				i0 = (updatedPC(300) if globals().has_key('faraday') else None)
				tmp = u"Analysis Summary ================================\n"
				tmp = tmp + u"       Project: %s\n" % self._project
				tmp = tmp + u"        Sample: %s\n" % self._sample
				tmp = tmp + u"      Analysis: %s\n" % self._analysis
				tmp = tmp + u"    Instrument: %s's TESCAN MIRA3\n" % (SITE, )
				tmp = tmp + u"      Operator: %s\n" % self._analyst
				tmp = tmp + u"Search and Measure ==============================\n"
				tmp = tmp + u"           FOV: %g mm × %g mm FOV\n" % (self._fov, self._fov)
				if self._overlap>1.0:
					tmp = tmp + u"        Border: %0.3g%% un-analyzed border region\n" % ( (self._overlap-1.0)*100.0, )
				elif self._overlap<1.0:
					tmp = tmp + u"       Overlap: %0.3g%% overlap of adjacent fields\n" % ( (1.0 - self._overlap)*100.0, )
				tmp = tmp + u"      Detector: %s\n" % (self.getDetector())
				sed, bsed = _ts.dtGetGainBlack(0), _ts.dtGetGainBlack(1)
				tmp = tmp + u"          BSED: Contrast %0.2f Brightness: %0.2f\n" % (bsed[0], bsed[1]) 
				tmp = tmp + u"           SED: Contrast %0.2f Brightness: %0.2f\n" % (sed[0], sed[1]) 
				tmp = tmp + u"        Search: %d to %d at %d pixels × %d pixels at %g µs/pixel\n" % (self._searchLow, self._searchHigh, self.searchDimension(), self.searchDimension(), self._searchDwell / 1000)
				tmp = tmp + u"       Measure: %d to %d at %d µs/pixel\n" % (self._measureLow, self._measureHigh, self._measureDwell / 1000)
				tmp = tmp + u"          Step: %d\n" % self._measureStep
				tmp = tmp + u"    Morphology: %s\n" % self._morphologyCrit
				tmp = tmp + u" Max Particles: %d\n" % self._maxPart
				tmp = tmp + u" Max per field: %d\n" % self._maxPartPerField
				tmp = tmp + u"      Beam Int: %0.2f\n" % _ts.getPCContinual()
				tmp = tmp + u"          Spot: %0.2f nm\n" % _ts.getSpotSize()
				if i0:
					tmp = tmp + u"       Faraday: %0.2f nA\n" % i0.average()
				else:
					tmp = tmp + u"          IAbs: %0.2f nA\n" % (_ts.getIAbsorbed() / 1000.0)
				tmp = tmp + u"        Beam E: %0.2f keV\n" % (_ts.hvGetVoltage() / 1000.0)
				tmp = tmp + u"            WD: %0.3f mm\n" % _ts.getWD()
				tmp = tmp + u"Vacuum ==========================================\n"
				tmp = tmp + u"       VP Mode: %s\n" % _ts.vacGetVPMode()
				tmp = tmp + u"       Chamber: %0.5g torr\n" % pascalToTorr(_ts.vacGetPressure(0))
				tmp = tmp + u"        Column: %0.5g torr\n" % pascalToTorr(_ts.vacGetPressure(1))
				tmp = tmp + u"           Gun: %0.5g torr\n" % pascalToTorr(_ts.vacGetPressure(2))
				tmp = tmp + u"Images ==========================================\n"
				if self._collectImages:
					tmp = tmp + u"         Field: Collect at %d pixels × %d pixels\n" % (self._imgDim, self._imgDim)
				else:
					tmp = tmp + u"         Field: Don't collect\n"
				tmp = tmp + u"      Particle: Evaluate %s at %d pixels × %d pixels\n" % (self._collectPartImages.func_name, self._pImgDim, self._pImgDim)
				if self._collectEDS:
					tmp = tmp + u"EDS Configuration ==============================\n"
					tmp = tmp + u"           EDS: %g s Real time\n" % self._edsRealTime
					tmp = tmp + u"          Mode: %s\n" % self._EDSMode
					tmp = tmp + u"       Vectors: %s\n" % self._vecs
					tmp = tmp + u"      Elements: %s\n" % (", ".join("%s" % v.toAbbrev() for v in self._vecs.getElements()))
					tmp = tmp + u"         Rules: %s\n" % self._rules
				if self._collectSI:
					tmp = tmp + u"SI Collection ==================================\n"
					tmp = tmp + u"          When: Based on rule evaluation\n"
					tmp = tmp + u"    Dimensions: %d\n" % self._SIDim
					tmp = tmp + u"         Dwell: %d\n" % self._SIDwell
				if tiling:
					tmp = tmp + u"Tiling ========================================\n"
					tmp = tmp + u"   Description: %s\n" % tiling
					tmp = tmp + u"         Tiles: %d of %g mm × %g mm\n" % (tiling.size(), tiling.getTileDimension()[0], tiling.getTileDimension()[0])
					tmp = tmp + u"          Area: %g\n" % (tiling.getArea(), )
				print tmp
				fos = jio.FileOutputStream(jio.File(self._path, "configuration.txt"))
				try:
					osw = jio.OutputStreamWriter(fos, "UTF-8")
					osw.write(tmp)
					osw.flush()
				finally:
					fos.close()
					
			def toLatex(self, tiling=None):
				"""Internal use only...
				toLatex(tiling)
				Summarize the analysis configuration to the command output window."""
				i0 = (updatedPC(300) if globals().has_key('faraday') else None)
				tmp="\\begin{tabular}{rl}"
				tmp=tmp + "\\colspan{2}{Analysis Summary}\\\\\n"
				tmp = tmp + u"   Project & %s\\\\\n" % self._project
				tmp = tmp + u"   Sample & %s\\\\\n" % self._sample
				tmp = tmp + u"   Analysis & %s\\\\\n" % self._analysis
				tmp = tmp + u"   Instrument & TESCAN MIRA3 at %s \\\\\n" % (SITE, )
				tmp = tmp + u"   Operator & %s\\\\\n" % self._analyst
				tmp = tmp + u"   \\colspan{2}{Search and Measure}\\\\\n"
				tmp = tmp + u"   FOV & $\\SI{%g}{\\milli\\meter} \\times \\SI{%g}{\\milli\\meter}$ FOV\\\\\n" % (self._fov, self._fov)
				if self._overlap>1.0:
					tmp = tmp + u"   Border & \\SI{%0.3g}{\\%} un-analyzed border region\\\\\n" % ( (self._overlap-1.0)*100.0, )
				elif self._overlap<1.0:
					tmp = tmp + u"   Overlap & \\SI{%0.3g}{\\%} overlap of adjacent fields\\\\\n" % ( (1.0 - self._overlap)*100.0, )
				tmp = tmp + u"   Detector & %s\\\\\n" % (self.getDetector())
				sed, bsed = _ts.dtGetGainBlack(0), _ts.dtGetGainBlack(1)
				tmp = tmp + u"   BSED & Contrast: \\num{%0.2f} Brightness: \\num{%0.2f}\\\\\n" % (bsed[0], bsed[1]) 
				tmp = tmp + u"   SED &  Contrast: \\num{%0.2f} Brightness: \\num{%0.2f}\\\\\n" % (sed[0], sed[1]) 
				tmp = tmp + u"   Search & %d to %d at $\\SI{%d}{pixels} \\times \\SI{%d}{pixels}$ at \\SI{%g}{\micro\second\per pixel}\\\\\n" % (self._searchLow, self._searchHigh, self.searchDimension(), self.searchDimension(), self._searchDwell / 1000)
				tmp = tmp + u"   Measure & %d to %d at \\SI{%d}{\\micro\\second\\per pixel}\\\\\n" % (self._measureLow, self._measureHigh, self._measureDwell / 1000)
				tmp = tmp + u"   Step & %d\\\\\n" % self._measureStep
				tmp = tmp + u"   Morphology & %s\\\\\n" % self._morphologyCrit
				tmp = tmp + u"   Max Particles & %d\\\\\n" % self._maxPart
				tmp = tmp + u"   Max per field & %d\\\\\n" % self._maxPartPerField
				tmp = tmp + u"   Beam Intensity & \\num{%0.2f}\\\\\n" % _ts.getPCContinual()
				tmp = tmp + u"   Spot & \\SI{%0.2f}{\\nano\\meter}\\\\\n" % _ts.getSpotSize()
				if i0:
					tmp = tmp + u"   Faraday & \\SI{%0.2f}{\\nano\\ampere}\\\\\n" % i0.average()
				else:
					tmp = tmp + u"   IAbs & \\SI{%0.2f}{\\nano\\ampere}\\\\\n" % (_ts.getIAbsorbed() / 1000.0)
				tmp = tmp + u"   Beam Energy & \\SI{%0.2f}{\\keV}\\\\\n" % (_ts.hvGetVoltage() / 1000.0)
				tmp = tmp + u"   Working Distance & \\SI{%0.3f}{\\milli\\meter}\\\\\n" % _ts.getWD()
				tmp = tmp + u"   \colspan{2}{Vacuum}\\\\\n"
				tmp = tmp + u"   VP Mode & %s\\\\\n" % _ts.vacGetVPMode()
				tmp = tmp + u"   Chamber& \\SI{%0.5g}{\\torr}\\\\\n" % pascalToTorr(_ts.vacGetPressure(0))
				tmp = tmp + u"   Column & \\SI{%0.5g}{\\torr}\\\\\n" % pascalToTorr(_ts.vacGetPressure(1))
				tmp = tmp + u"   Gun & \\SI{%0.5g}{\\torr}\\\\\n" % pascalToTorr(_ts.vacGetPressure(2))
				tmp = tmp + u"   \colspan{2}{Images}\\\\\n"
				if self._collectImages:
					tmp = tmp + u"   Field & Collect at $\\SI{%d}{pixels} \\times \\SI{%d}{pixels}$\\\\\n" % (self._imgDim, self._imgDim)
				else:
					tmp = tmp + u"   Field & Don't collect\n"
				tmp = tmp + u"   Particle & Evaluate %s at $\\SI{%d}{pixels} \\times \\SI{%d}{pixels}$\\\\\n" % (self._collectPartImages.func_name, self._pImgDim, self._pImgDim)
				if self._collectEDS:
					tmp = tmp + u"   \\colspan{2}{EDS Configuration}\\\\\n"
					tmp = tmp + u"   EDS & \\SI{%g}{\\second} Real time\\\\\n" % self._edsRealTime
					tmp = tmp + u"   Mode & %s\\\\\n" % self._EDSMode
					tmp = tmp + u"   Vectors & %s\\\\\n" % self._vecs
					tmp = tmp + u"   Elements & %s\\\\\n" % (", ".join("%s" % v.toAbbrev() for v in self._vecs.getElements()))
					tmp = tmp + u"   Rules & %s\\\\\n" % self._rules
				if self._collectSI:
					tmp = tmp + u"   \\colspan{2}{SI Collection}\\\\\n"
					tmp = tmp + u"   When & Based on rule evaluation\\\\\n"
					tmp = tmp + u"   Dimensions & $\\SI{%d}{pixels} \\times \\SI{%d}{pixels}$\\\\\n" % (self._SIDim, self._SIDim)
					tmp = tmp + u"   Dwell & %d\\\\\n" % self._SIDwell
				if tiling:
					tmp = tmp + u"   \\colspan{2}{Tiling}\\\\\n"
					tmp = tmp + u"   Description & %s\\\\\n" % tiling
					tmp = tmp + u"   Tiles & %d of $\\SI{%g}{\\milli\\meter} \\times \\SI{%g}{\\milli\\meter}$\\\\\n" % (tiling.size(), tiling.getTileDimension()[0], tiling.getTileDimension()[0])
					tmp = tmp + u"   Area: \\SI{%g}{\\milli\\meter\\squared}\\\\\n" % (tiling.getArea(), )
					tmp = tmp + u"\end{tabular}"
				tmp.replace(u"α",u"$\alpha$")

				fos = jio.FileOutputStream(jio.File(self._path, "%s.tex" % self._sample))
				try:
					osw = jio.OutputStreamWriter(fos, "ASCII")
					osw.write(tmp)
					osw.flush()
				finally:
					fos.close()
					
				
			def postSummary(self, tos = None):
				"""Internal use only...
	Summarize the results of the analysis to the command output window."""
				if not tos:
					fos = jio.PrintWriter(jio.File(self._path, "summary.txt"),"UTF-8")
					try:
						self.postSummary(fos)
						fos.flush()
					finally:
						fos.close()
				elif self._zep:
					res = u"Summary Analysis Report\n"
					res = res + u"     Path: %s\n" % self._path
					res = res + u"    Total: %d particles\n" % self._zep.getParticleCount()
					if self._rules and (self._zep.getParticleCount() > 0):
						res = res+ u"Particles by Class\n"
						for i in range(0, self._rules.ruleCount()):
							cx = self._zep.getClassMemberCount(self._rules, i)
							res = res + u"  %20s: %d particles\n" % (self._rules.ruleName(i), cx)
					print res
					tos.println(res)
				else:
					print "No Zeppelin to summarize."
			
			def deleteParticles(self):
				"""deleteParticles()
	Clear all the mappings of particle spectra to 'PXXXX' as have been associated with this analysis."""
				for i in range(0, self._zep.getParticleCount()):
					exec "if globals().has_key(\"P%0.4d\"):\n\tdelete P%0.4d" % (i , i)
					
			def display(self, rowNum):
				"""display(rowNum)
	Display the particle spectrum and images in rowNum."""
				spec = self.get(rowNum)
				spec.getProperties().setDetector(pt_det_all)
				display(spec)
			
			def getZeppelin(self):
				"""getZeppelin()
		Returns a Zepp object that can be used to interact with the data set."""
				return (Zepp(self._zep.getFile()) if self._zep else None)
				
			def setDebug(self, b=True):
				self._debug=b
				self._debugPw=None
			
			def debug(self, ss):
				"""debug(str)
		For internal use primarily!"""
				if self._debug:
					if not self._debugPw:
						ff = jio.File(self._path, "debug.txt")
						fos = jio.FileOutputStream(ff, True)
						self._debugPw = jio.PrintWriter(fos)
					if self._debugPw:
						try:
							if not ss.endswith("\n"):
								ss=ss+"\n"
							self._debugPw.append("%s: %s" % (jtext.SimpleDateFormat("yyyy-MM-dd HH.mm.ss.Z").format(ju.Date()), ss))
						finally:
							self._debugPw.flush()

				
			def perform(self, tiling, startField=1, endField=10000):
				"""perform(self, tiling, [startField=1]):
	Perform RCA APA on the specified tiling.
		tiling - A tiling on which to perform APA
		startField - Specify which field to start the analysis (allows partial restarts)
		endField - The maximum field number to analyze."""
				if terminated:
					return
				global _afafb
				if not _afafb:
					_afafb=sema.AFAFeedbackPanel()
					MainFrame.addUtilityTab("AFA",_afafb,"AFA")
				self.debug("Starting RCA on %s" % str(tiling))
				writeTextFile("%s/script.py" % self._path, _script)
				# Check for wide field mode
				if _ts.smGetMode()==3:
					old = _ts.smEnumModes()[_ts.smGetMode()]
					_ts.smSetMode(1) # DEPTH mode
					print "NOTIFICATION: Scan mode set to %s from %s" % ( _ts.smEnumModes()[_ts.smGetMode()], old, )
				oldBlank = _ts.scGetBlanker()
				_ts.scSetBlanker(0)
				_ts.guiSetScanning(False)
				_ts.scStopScan()
				# scale the image view field to account for the RCA DAC margin
				_ts.setViewField(self._rcaFov)
				_ts.setGeometry("Image Rotation", 0.0, 0.0)
				self.buildZep()
				_ts.chamberLed(False)
				# Configure the tile dimensions
				tiling.setTileDimension([self._fov, self._fov])
				# Write the tiling to a file as XML
				tos = jio.PrintStream(jio.File(self._path, "tiling.xml"))
				tos.println(toXML(tiling))
				tos.close()
				self.summarize(tiling)
				if SITE==NIST:
				    self.toLatex(tiling)
				write(semstg.TilingUtilities.createMap(tiling, 2048), "Summary Map", self._path) # updated at end...
				tos2 = jio.PrintStream(jio.File(self._path, "progress.txt"))
				print "Analysis Starting...\n"
				tos2.println("Analysis Starting...\n")
				self._timer = Elapse(tiling.size() - startField)
				if startField > 1:
					print "Starting analysis at field %d" % startField
					tos2.println("Starting analysis at field %d" % startField)
				dispTiling = semstg.DisplayTiling(tiling, 4096, ( self._fov/self._overlap, self._fov/self._overlap) )
				try:
					for i, tile in enumerate(dispTiling):
						self.debug("Tile %d - 00" % (i, ))
						field = i + 1
						if field >= startField:
							self.debug("Tile %d - 01" % (i, ))
							self.moveStage(tile.getCenter())
							# _stg.moveTo(tile.getCenter())
							self.debug("Tile %d - 02" % (i, ))
							ttmmpp = "Field %d of %d:\t%s\t%s\t%0.2f nA" % (field, tiling.size(), tile.getCenter(), self._timer.updateString(field - startField), (_ts.getIAbsorbed() / 1000.0))
							tos2.println(ttmmpp)
							time.sleep(0.2)
							_afafb.updateField(field, jl.Math.min(endField,tiling.size()) - startField + 1, self._zep.getParticleCount())
							self.debug("Tile %d - 03" % (i, ))
							termP = self.collectField(field)
							self.debug("Tile %d - 04" % (i, ))
							DataManager.clearSpectrumList()
							MainFrame.clearKLMs()
						if self._ParticleCount>=self._maxPart:
							print "\nAnalysis terminated by max-particle count criterion (%d>=%d)" % (self._ParticleCount, self._maxPart)
							tos2.println("\nAnalysis terminated by max-particle count criterion (%d>=%d)" % (self._ParticleCount, self._maxPart))
							break
						if field  >= endField:
							print "\nAnalysis terminated by end field criterion (%d).\n" % endField
							tos2.println("\nAnalysis terminated by end field criterion (%d).\n" % endField)
							break
						if termP:
							print "\nAnalysis terminated by terminate rule."
							break
						if terminated:
							print "\nAnalysis terminated prematurely by operator at field %d.\n" % field
							tos2.println("\nAnalysis terminated prematurely by operator at field %d.\n" % field )
							break
					self.debug("Tile %d - 06" % (i, ))
				except jl.Throwable, th:
					print str(th)
					th.printStackTrace()
				finally:
					pCol = jawt.Color(0, 0, 0, 64)
					for row in range(0, self._zep.getParticleCount()):
						dispTiling.plotParticle(self._zep.getStageCoordinate(row), pCol)
					write(dispTiling.getImage(),"Summary Map", self._path)
					_ts.guiSetScanning(True)
					if not terminated:
						print "\nAnalysis complete.\n"
						tos2.println("\nAnalysis complete.\n")
					tos2.flush()
					tos2.close()
					if globals().has_key('faraday'):
						updatedPC()
					_ts.chamberLed(defLED)
					_ts.scSetBlanker(oldBlank)
					if self._processSIStack:
						self._processSIStack.add( None, None, None )
					self._debugPw = None

				
		def buildRCA(project, sample, vecs, rules, fov = 0.512, morph = None, analyst = None, realTime=0.3, path = None):
			"""buildRCA(project, sample, vecs, rules, fov = 0.512, morph = semtr.RcaTranslator.AreaCriterion(0.5, 4.0e4))
		Builds a basic, default RCA object.  This object can be used directly to perform an RCA 
	analysis or it can be modified to change the default analysis options."""
			rca=RCA(project, sample, analyst, path=path)
			rca.setFieldOfView(0.512, ppmRes=11)
			rca.configEDS(vecs, rules, realTime)
			if not morph:
				morph=semtr.RcaTranslator.AreaCriterion(0.5, 4.0e4)
			rca.setMorphologyCriterion(morph)
			rca.setMeasureThreshold(low=32, high = 255, dwell = 4000, measureStep = 8)
			rca.setSearchThreshold(low=96, high = 255, dwell = 1000)
			rca.collectImages(True, 128, True, 1024)
			return rca
else:
	print "No RCA license found."
			
	def stagePoints():
		"""stagePoints()
Allows the user to move the stage around and mark various stage points.  The resulting list of stage coordinates is /
returned as a list."""
		res = []
		msg = "Move the stage to the next outline point.\nSelect 'OK' to record a point or 'Cancel' when the list is complete."
		add = True
		while add:
			 add = (jop.showConfirmDialog(MainFrame, msg, "Collect a stage point?", jop.OK_CANCEL_OPTION) == jop.OK_OPTION)
			 if add:
				pos = _stg.getPosition()
				res.append(pos)
				print "Adding %s" % pos
		print "Stage point list contains %d points." % len(res)
		return res
			
		

import threading
import Queue
		
class SIProcessor (threading.Thread):
	""""A class used to process spectrum images in the background as acquisition of RCA data continues."""

	def __init__(self, subSample=1, thresh=0.2):
		threading.Thread.__init__(self)
		self._queue = jutilc.LinkedBlockingDeque()
		self._terminated=False
		self._subSample = subSample
		self._thresh=thresh
	
	def add(self, name, path, vecs):
		self._queue.addLast( (name, path, vecs, ) )
		
	def terminate(self):
		self._terminated = True
	
	def run(self):
		name, path, vecs = self.next()
		while name:
			try:
				# print "Processing SI: %s" % name
				SItoRPL(name)
				SItoMap(name, vecs, path=path, subSample=self._subSample, thresh=self._thresh, label=True)
			except jl.Throwable, ex:
				print "Processing SI %s at %s failed" % (name, path)
				print ex
			name, path, vecs = self.next()

	def next(self):
		tmp = None
		while not tmp:
			if self._terminated:
				break
			tmp = self._queue.pollFirst(100, jutilc.TimeUnit.MILLISECONDS)
		return (tmp if tmp else (None, None, None))
		
def getSIDimensions(siFile):
	"""getSIDimensions(siFile)
	Return a tuple containing the ( width, height, subRaster ) for the specified spectrum image."""
	fis = jio.FileInputStream(siFile)
	try:
		hdr = semss.MessageTranslator.readHeader(fis)
		return (hdr.getWidth(), hdr.getHeight(), hdr.getRaster())
	finally:
		fis.close()
	return None
	
def validateStds(stds, path=None):
	"""validateStds(stds, path=None):
	Takes a map of element name to standard spectrum file and creates a new map in which missing spectra have been removed."""
	path = (path if path else defaultVecPath)
	res={}
	for elm, std in stds.iteritems():
		if jio.File(path, std).exists():
			res[elm]=std
	return res
	
def buildVectors(stds, path=None, strip=(), det = pt_det_all ):
	"""buildVectors(stds, path=None,strip=())
	Construct a set of Schamber-style fast quant vectors
	stds = { "Fe" : "Fe std", "Cr": "Cr std", "Cd":s101 ... }
	path = "/home/nicholas/standards" or similar (None -> defaultPath)
	strip= ("C", "O", ...) a list of elements to strip (must also be in stds)"""
	path = (path if path else defaultVecPath)
	procStds = {}
	strip = [element(elm) for elm in strip]
	std0 = None
	for elm, std in stds.iteritems():
		if isinstance(std, str):
			std = readSpectrum("%s/%s" % (path, std))
		elif isinstance(std, ScriptableSpectrum):
			std = std.wrapped
		procStds[element(elm)] = std
		if not std0:
			std0 = wrap(std)
	sv = fq.SchamberVectors(det, epq.ToSI.keV(std0.beamEnergy()))
	for elm, std in procStds.iteritems():
		sv.addStandard(elm, std, elm in strip)
	return sv.getVectorSet()
	
def SItoRPL(siName, subSample=4, mask=defaultDetMask, eVperCh=10.0, chCount=2048, path=None):
	"""SItoRPL(siName, subSample=4, mask=defaultDetMask, eVperCh=10.0, chCount=2048, path=defaultPath)
	Converts a raw SI dump file into a RPL/RAW file pair."""
	path = (path if path else defaultPath)
	siFile = "%s/%s/map.ptx" % (path, siName )
	print siFile
	dims = getSIDimensions(siFile)
	mt = semss.MessageTranslator()
	rt = semtr.RippleTranslator(mt, dims[0], dims[1], chCount, subSample, eVperCh, mask, dims[2], "%s/%s/map[%d].raw" % (path, siName, mask))
	try:
		mt.add(rt)
		mt.process(jio.File(siFile))
		rt.close()
		msg = mt.poll()
		while msg:
			print msg
			msg = mt.poll()
		spec = wrap(rt.getMaxPixel(pt_det_all))
		spec.rename("MaxPixel[%s]" % siName)
		write(spec, "MaxPixel[%s]" % siName, "%s/%s" % (path, siName))
		display(spec)
	finally:
		rt.close()
	
def checkTiling(tiling, bounds=defaultBounds):
	"""checkTiling(tiling, bounds=(-40.0, -40.0, 40.0, 40.0))
	Iterates through a tiling checking that the central point is within the specified stage coordinate bounds."""
	b = True
	for tile in tiling:
		c = tile.getCenter()
		x, y = c.get(X_AXIS), c.get(Y_AXIS)
		if (x < bounds[0]) or (x > bounds[2]) or (y < bounds[1]) or (y > bounds[3]):
			b = False
			print "Tile out-of-bounds: %s" % tile
	if not b:
		print "This tiling goes out of the bounds = %s" % bounds
	return b

def moveToTile(tile, xyOnly=False, bounds=defaultBounds):
	"""moveToTile(tile, bounds=(-40.0, -30.0, 40.0, 30.0)):
	Moves the stage to this tile and returns true if it is in bounds.  Returns false if the tile is out of bounds."""
	c = tile.getCenter()
	x, y = c.get(X_AXIS), c.get(Y_AXIS)
	b = not ((x < bounds[0]) or (x > bounds[2]) or (y < bounds[1]) or (y > bounds[3]))
	if b:
		moveTo(c, xyOnly)
	return b
	
def summarizeSI(siName, path=None):
	"""SItoRPL(siName, subSample=4, mask=defaultDetMask, eVperCh=10.0, chCount=2048, mmSize=256, path=defaultPath)
	Summarizes the contents of the specified spectrum image in text."""
	path = (path if path else defaultPath)
	siFile = "%s/%s/map.ptx" % (path, siName)
	return semss.MessageTranslator().summarize(jio.File(siFile))
	
def labelImage(bi, label):
	"""labelImage(bi, label)
	Write a text label onto an image bi."""
	gr = bi.createGraphics()
	gr.setColor(jawt.Color.white)
	gr.setFont(jawt.Font(jawt.Font.SANS_SERIF, jawt.Font.BOLD, bi.getWidth() / 12))
	fm = gr.getFontMetrics()
	gr.drawString(label, bi.getWidth() - (fm.stringWidth(label) + 4) , bi.getHeight() - fm.getHeight() / 2)
	
def SItoMap(siName, vecSet, path=None, mask=defaultDetMask, imgMask=None, subSample=4, thresh=0.2, label=True):
	"""SItoMap(siName, vecSet, path=None, mask=defaultDetMask, subSample=4, thresh=0.2, label=True):)
	Converts a SI file (map.ptx) into a series of x-ray map images using the /
	vector set 'vecSet'.
imgMask is an image of the same dimensions as the SI. Only pixels which are non-zero in the mask are processed
subsample: groups together blocks of pixel in the SI before processing them
thresh: sets all k-ratios below 0.2 to zero in the thresholded resulting images
label=True to label each image with the element"""
	path = (path if path else defaultPath)
	siFile = "%s/%s/map.ptx" % (path, siName)
	dims = getSIDimensions(siFile)
	siFileFile = jio.File(siFile)
	mt = semss.MessageTranslator()
	rt = semtr.MapTranslator(mt, dims[0], dims[1], subSample, vecSet, mask, dims[2])
	if imgMask:
		if isinstance(imgMask, str):
			imgMask = iio.ImageIO.read(jio.File("%s/%s/%s" % (path, siName, imgMask)))
			rt.setMask(imgMask)
	mt.add(rt)
	mt.process(siFileFile)
	msg = mt.poll()
	while msg:
		print msg
		msg = mt.poll()
	mi = rt.getMapImage()
	for i in range(0, rt.getDepth()):
		elm = rt.getElement(i)
		write(mi.getLog3BandImage(i, label), "%s[%d]" % (elm, mask), "%s/%s" % (path, siName))
		write(mi.getLogImage(i, label), "%s[Log][%d]" % (elm, mask), "%s/%s" % (path, siName))
		write(mi.getNormKRatioImage(i, label), "%s[Linear][%d]" % (elm, mask), "%s/%s" % (path, siName))
		write(mi.getMask(i, thresh, label), "%s[Thresh=%3.2f][%d]" % (elm, thresh, mask), "%s/%s" % (path, siName))
	write(mi.getKRatioSumImage(), "Analytic Total[%d]" % (mask), "%s/%s" % (path, siName))
	write(mi.getKRatioSummaryImage(), "K-ratio map[%d]" % (mask), "%s/%s" % (path, siName))
	time.sleep(0.1)
	mapsToMontage(siName, vecSet, path=path, mask=mask, width=4)
	
def SItoImages(siName, dets=[0,1], path=None):
	"""SItoImages(siName, path=None)
Extracts the image data out of a spectrum image."""
	path = (path if path else defaultPath)
	siFile = "%s/%s/map.ptx" % (path, siName)
	dims = getSIDimensions(siFile)
	siFileFile = jio.File(siFile)
	mt = semss.MessageTranslator()
	it = semtr.ImageTranslator(dets, dims[0],dims[1])
	mt.add(it)
	mt.process(siFileFile)
	msg = mt.poll()
	while msg:
		if isinstance(msg, semdi.ImageDatum):
			write(msg, "ImageDup", "%s/%s" % (path, siName), fmt="png")
	
def processSIs(basePath, baseName, vecs, mask=defaultDetMask, imgMask=None, subSample=4, thresh=0.2, label=True):
	"""processSIs(basePath, baseName, vecs, mask=defaultDetMask, imgMask=None, subSample=4, thresh=0.2, label=True)
	Process all the spectrum images in the basePath with the baseName using the specified vectors."""
	if terminated:
		return
	bp = jio.File(basePath)
	for fi in bp.listFiles():
		if fi.isDirectory() and fi.getName().startswith(baseName):
			print "Processing: %s" % fi.getName()
			SItoMap(fi.getName(), vecs, path = bp, mask=mask, imgMask=imgMask, subSample=subSample, thresh=thresh, label=label)
			if terminated:
				break
	
def SItoSum(siName, mask=None, subSample=4, path=None):
	"""SItoSum(siName, mask=None, subSample=4, path=None):
	Computes the sum spectrum from the specified spectrum image.  If mask!=None then the image associated with mask \
is used to mask the data (non-zero pixels are included).  mask may be the name of a mask image in the same directory \
as siName or a BufferedImage object.  Make sure subSample matches the subSample used to create the mask image."""
	if terminated:
		return
	path = (path if path else defaultPath)
	siFile = "%s/%s/map.ptx" % (path, siName)
	name = "Image"
	if isinstance(mask, str):
		name = mask
		mask = iio.ImageIO.read(jio.File("%s/%s/%s" % (path, siName, mask)))
	dims = getSIDimensions(siFile)
	siFileFile = jio.File(siFile)
	mt = semss.MessageTranslator()
	st = semtr.SpectrumTranslator(mt, dims[0], dims[1], 0, defaultDetCount, pt_det_all)
	if isinstance(mask, jawt.image.BufferedImage):
		st.setMask(mask, subSample)
	mt.add(st)
	mt.process(siFileFile)
	res = mt.poll()
	while res:
		res = mt.poll()
	specs = st.getResult()
	write(specs.getSumSpectrum(), ("Masked[%s]" % name if mask else "Sum"), "%s/%s" % (path, siName), fmt="msa")
	return specs.getSumSpectrum()

def SItoSums(siName, masks, subSample=4, path=None):
	"""SItoSums(siName, masks=(), subSample=4, path=None):
	Similar to SItoSum except takes an collection of masks to permit processing a single SI against multiple masks
in a more efficient manner.  Computes a set of masked sum spectrum from the specified spectrum image and mask collection. \
The items in masks should be images in the directory path/siName."""
	if terminated:
		return
	path = (path if path else defaultPath)
	siFile = "%s/%s/map.ptx" % (path, siName)
	siFileFile = jio.File(siFile)
	mt = semss.MessageTranslator()
	dims = getSIDimensions(siFile)
	maskDict = {}
	for mask in masks:
		st = semtr.SpectrumTranslator(mt, dims[0], dims[1], 0, defaultDetCount, pt_det_all)
		st.setMask(iio.ImageIO.read(jio.File("%s/%s/%s" % (path, siName, mask))), subSample)
		mt.add(st)
		maskDict[mask] = st
	mt.process(siFileFile)
	tmp = mt.poll()
	while tmp:
		tmp = mt.poll()
	res = {}
	for name, st in maskDict.iteritems():
		specs = st.getResult()
		ss = specs.getSumSpectrum()
		epq.SpectrumUtils.rename(ss, "Masked[%s]" % name)
		write(ss, "Masked[%s]" % name, "%s/%s" % (path, siName), fmt="msa")
		res[name] = ss
	return res

def execute(cmd, wait=False):
	"""execute(cmd)
	Passes a command to the operating system to execute in a separate process"""
	pb = jl.ProcessBuilder([cmd])
	p = pb.start()
	if wait:
		p.waitFor()
		
def executeCmd(cmd, wait=False):
	"""executeCmd(cmd)
	Passes a command to cmd.exe to execute in a separate process"""
	pb = jl.ProcessBuilder(["cmd.exe", "/C", cmd])
	p = pb.start()
	if wait:
		return p.waitFor()

def mapsToMontage(name, vecs, path=None, mask=defaultDetMask, width=4, base=""):
	"""mapsToMontage(name, vecs, path=defaultPath, mask=defaultDetMask, width=4, base="")
	Convert the output of SItoMap(...) to a single montaged image. /
	Use base = ""[Linear]"" for montage of linear gray-scale maps."""
	_saverize.waitUntilAllSaved()
	path = (path if path else defaultPath)
	if isinstance(path, str):
		path = path.replace("/", "\\")	
	tmp = '"%s\\montage.exe" -tile %dx%d -geometry +1+1' % ( IMAGE_MAGICK, width + 1, (vecs.vectors.size() + width - 1) / width)
	for u, v in enumerate(vecs.vectors):
		tmp = '%s "%s\\%s\\%s%s[%d].png"' % (tmp, path, name, v.element, base, mask)
		if (u + 1) % width == 0:
			tmp = '%s "%s\\%s.png"' % (tmp, keyPath, "log3key" if u + 1 == width else "log3spacer")
	tmp = '%s "%s\\%s\\montage%s[%d].png"' % (tmp, path, name, base, mask)
	execute(tmp, True)

def doTiling(tiling, elms, path=None, size=256, mask=15, base="%s", imagesTo=False):
	"""doTiling(tiling, elms, path=None, size=256, mask=15, base="%s", imagesTo=False)"""
	path = defaultPath
	results = "Results[%d]" % (size)
	jio.File(path, results).mkdirs()
	imgFiles = []
	for elm in elms:
		imgFiles.append(("%s[%d].png" % (base % element(elm), mask), size))
	imgFiles.append(("Analytic Total[%d].png" % mask, size))
	imgFiles.append(("K-ratio map[%d].png" % mask, size))
	if imagesTo:
		imgFiles.append(("Image[0][Ch1].png", 1024,))
		imgFiles.append(("Image[0][Ch2].png", 1024,))
	for baseImg, imgDim in imgFiles:
		nullImage = "%s/null%d.png" % (nullImagePath, imgDim)
		r, c = -1, -1
		rows, imgs = [], None
		for i, tile in enumerate(tiling):
			if terminated:
				break
			ti = tile.getTileIndex()
			if ti[0] <> r:
				if imgs:
					rows.append(imgs)
				imgs = []
				r = ti[0]
				c = -1
			for col in range(c, ti[1] - 1):
				imgs.append(nullImage)
			addThis = "%s/Tile[%s]/%s" % (path, tile.getIndex(), baseImg)
			imgs.append((addThis if jio.File(addThis).exists() else nullImage))
			c = ti[1]
		if imgs:
			rows.append(imgs)
		maxLen = 0
		for row in rows:
			maxLen = max(maxLen, len(row))
		for row in rows:
			while len(row) < maxLen:
				row.append(nullImage)
		final = "\"%s\\montage.exe\" -tile 1x%d -geometry +0+0 " % (IMAGE_MAGICK, len(rows))
		for i, row in enumerate(rows):
			tmp = ""
			for img in row:
				tmp = "\"%s\" %s" % (img, tmp)
			tmp = "\"%s\\montage.exe\" -tile %dx1 -geometry +0+0 %s \"%s\\row[%d].png\"" % (IMAGE_MAGICK, maxLen, tmp, path, i)
			print tmp
			execute(tmp.replace("/", "\\"), True)
			# result = "%s%s\n" % (result, tmp.replace("/","\\"))
			final = "%s \"%s\\row[%d].png\"" % (final, path, i)
		final = "%s \"%s\\%s\\%s\"" % (final, path, results, baseImg)
		print final
		execute(final.replace("/", "\\"), True)
		# result = "%s%s\n" % (result, final.replace("/","\\"))

def mapsToOverlay(name, elms, colorspace="RGB", thresh=0.2, path=None, label=True, mask=defaultDetMask):
	"""MapsToOverlay(name, elms, colorspace="RGB"|"CYM"|"XYZ", thresh=0.2, path=defaultPath, mask=defaultDetMask):
	Converts up to three maps into a single composite colored image with a label at the bottom"""
	path = (path if path else defaultPath)
	path = path.replace("/", "\\")
	elmStr = []
	tmp = ""
	for i in range(0, min(len(elms), 3)):
		elm = element(elms[i])
		if thresh == 0.0:
			elmStr.append('"%s\\%s\\%s[Linear][%d].png"' % (path, name, elm.toString(), mask))
		elif thresh<0.0:
			elmStr.append('"%s\\%s\\%s[Log][%d].png"' % (path, name, elm.toString(), mask))
		else:
			elmStr.append('"%s\\%s\\%s[Thresh=%3.2f][%d].png"' % (path, name, elm.toString(), thresh, mask))
		tmp = "%s%s=%s" % ((tmp + ", " if tmp else ""), elm.toAbbrev(), colorspace[i])
	if label:
		cmd = ('"%s\\convert" -background black ' % IMAGE_MAGICK) + " ".join(elmStr) + (' -set colorspace %s -combine -background gray label:"%s" -gravity center -append "%s\\%s\\overlay[%s].png"' % (colorspace, tmp, path, name, tmp))
	else:
		cmd = ('"%s\\convert" -background black ' % IMAGE_MAGICK) + " ".join(elmStr) + (' -set colorspace %s -combine -gravity center -append "%s\\%s\\overlay[%s].png"' % (colorspace, path, name, tmp))
	execute(cmd, True)
	
def processSI(name, vecs, path=None, subSample=4):
	"""processSI(name, vecs=None,path=defaultPath)
	Perform the standard default processing on an SI"""
	if terminated:
		return
	path = (path if path else defaultPath)
	if terminated:
		return
	SItoRPL(name, path=path, subSample=subSample)
	if terminated:
		return
	SItoMap(name, vecs, path=path, subSample=subSample)
	if terminated:
		return
	mapsToMontage(name, vecs, path=path)
	if terminated:
		return
	animateSIImages(name, path=path)
	
def animateSIImages(name, path=None):
	files = []
	path = (path if path else defaultPath)
	path = path.replace("/", "\\") + "\\" + name
	for ch in range(1, 3):
		tmp = '"%s\\convert" -delay 50 ' % IMAGE_MAGICK
		for i in range(0, 1000):
			f = jio.File(path, "Image[%d][Ch%d].png" % (i, ch))
			if f.isFile():
				tmp = '%s "%s\\%s"' % (tmp, path, "Image[%d][Ch%d].png" % (i, ch))
			else:
				break
		tmp = '%s -loop 0 "%s\\Image[Ch%d].gif"' % (tmp, path, ch)
		execute(tmp, True)

def write(objs, name, path=None, fmt="msa", writeMask=0xFF):
	"""write(objs,name,[path], [fmt="msa"])
	Write a spectrum, an image or an array of these to disk file(s).  The results will be located \
	in 'defaultPath' unless some other path is specified"""
	path = (path if path else defaultPath)
	name = name.replace(":","_").replace("\\","_").replace(".","_")
	if isinstance(objs, javaarray.array) or isinstance(objs, tuple) or isinstance(objs, list):
		for i, obj in enumerate(objs):
			if isinstance(obj, ept.ScaledImage):
				if isinstance(obj, ept.ScaledImage):
					di = (int(obj.getDetectorIndex()) if obj.getDetectorIndex() else i)
					if writeMask & (1<<di):
						write(obj, "%s[%d]" % (name, di, ), path, fmt=fmt)
			else:
				write(obj, "%s[%d]" % (name, i), path, fmt=fmt)
	elif isinstance(objs, semss.DataItems.ImageDatum):
		for i in range(0, objs.getCount()):
			write(objs.getImage(i), "%s[%d][%s]" % (name, objs.getFrameId(), objs.getName(i)), path, fmt=fmt)
	elif isinstance(objs, semss.DataItems.SpectrumDatum):
		for i in range(0, objs.getCount()):
			write(objs.getSpectrum(i), "%s[%d]" % (name, i), path, fmt=fmt)
	elif isinstance(objs, ept.ScaledImage) or isinstance(objs, jawtimg.BufferedImage):
		if fmt=='tif':
			_saverize.addAsTIFF(jio.File(path, "%s.tif" % name), objs)
		else:
			_saverize.addAsPNG(jio.File(path, "%s.png" % name), objs)
	elif isinstance(objs, epq.ISpectrumData):
		if fmt=='tif':
			_saverize.addAsTIFF(jio.File(path, name+".tif"), objs)
		else:
			emsa = ept.WriteSpectrumAsEMSA1_0()
			os = jio.FileOutputStream("%s/%s.msa" % (path, name))
			try:
				emsa.write(objs, os, emsa.Mode.COMPATIBLE)
                # report("<p>Spectrum <i>%s</i> written to <i>%s\\%s.msa</i></p>" % (objs, path, name))
			finally:
				os.close()

def annotateImages(path = None):
	"""annotateImages([path=defaultPath])
	Adds micron markers to the images in the specified path (None = defaultPath)."""
	if not path:
		path = defaultPath
	f = open("%s/images.txt" % path)
	try:
		hdr = f.readline()
		items = hdr.strip("\n").split("\t")
		first=True
		for tmp in f:
			data = tmp.strip("\n").split("\t")
			m = {}
			for i in range(0, len(items)):
				m[items[i].upper()] = data[i]
			img = []
			for i in range(0, 4):
				if m["TYPE"]=="SI":
					fpt=jio.File(path, m["NAME"])
					i0 = jio.File(fpt, "Image[0][[%d]].png" % (i, ))
					outFile = jio.File(fpt, "Image[0][[%d]][SC].png" % (i, ))
				else:
					i0 = jio.File(path, "%s[%d].png" % (m["NAME"], i))
					outFile = jio.File(path, "%s[%d][SC].png" % (m["NAME"], i))
				if i0.isFile() and (not outFile.isFile()):
					bi = iio.ImageIO.read(i0)
					fov = float(m["FOV"])
					sc = epq.StageCoordinate()
					sc.set(X_AXIS, float(m["X"]))
					sc.set(Y_AXIS, float(m["Y"]))
					sc.set(Z_AXIS, float(m["Z"]))
					if first:
						print "Filename\tField-of-View"
						first=False
					print "%s\t%3.1f um" % (i0.name, 1000.0*fov )
					si = ept.ScaledImage(bi, fov * 0.001, fov * 0.001, 0.0, sc, str(i))
					si.applyMicronBar()
					iio.ImageIO.write(si, "png", outFile)
	finally:
		f.close()

if connect:
	def markFiducial(sample, fids):
		"""markFiducial(sample, fids)
		Mark a fiducial point.  'sample' is the sample name, fids is a list to contain the stage coordinates."""
		n = len(fids) + 1
		fids.append(_stg.getPosition())
		imgs = collectImages("%s F%d 1_0 mm" % (sample, n), fov=1.0, dims=(512, 512), markCenter=True, writeMask=SAVE_IMAGE_MASK)
		for img in imgs:
			img.applyCenterCrossHair()
		imgs=collectImages("%s F%d 0_1 mm" % (sample, n), fov=0.1, dims=(512, 512), markCenter=True, writeMask=SAVE_IMAGE_MASK)
		for img in imgs:
			img.applyCenterCrossHair()
	
	def markPoint(pts, comp, withImg=False):
		"""markPoint(pts, comp, [withImg=False])
		Mark an analysis point. 'sample' is the sample name, 'pts' is a point list(), 'comp' is the point name."""
		next = (comp, [])
		add = True
		for pnt in pts:
			if pnt[0] == comp:
				next = pnt
				add = False
				break
		if add:
			pts.append(next)
		next[1].append(_stg.getPosition())
		if withImg:
			imgLabel = "%s %s[%d]" % (sample, comp, len(next[1]))
			imgs = collectImages(imgLabel, fov=0.5, dims=(256, 256), markCenter=True, writeMask=SAVE_IMAGE_MASK)
			for img in imgs:
				img.applyCenterCrossHair()
			write(imgs, imgLabel)

if True:			
	def markGrid(std, pts, comp, spacing =0.1, withImg = False):
		assert len(pts)>=2
		spacing = jl.Math.max(0.01,jl.Math.abs(spacing))
		pt0, pt1 = (pts[0].get(X_AXIS), pts[0].get(Y_AXIS), pts[0].get(Z_AXIS)), (pts[1].get(X_AXIS), pts[1].get(Y_AXIS), pts[1].get(Z_AXIS))
		xDim, yDim = int(jl.Math.abs((pt1[0]-pt0[0])/spacing)), int(jl.Math.abs((pt1[1]-pt0[1])/spacing))
		print "[%d x %d] grid" % (xDim, yDim)
		xStep, yStep = (spacing if pt1[0]>pt0[0] else -spacing), (spacing if pt1[1]>pt0[1] else spacing)
		home = _stg.getPosition()
		for x in range(0, xDim):
			for y in range(0, yDim):
				pnt = home.clone()
				pnt.set(X_AXIS, pt0[0] + x*xStep)
				pnt.set(Y_AXIS, pt0[1] + y*yStep)
				_stg.moveTo(pnt)
				markPoint(std, comp, withImg)

	def traverse(ends, std, comp, n):
		"""traverse(ends, std, n)
		Marks a series of n points from end[0] to end[1]. Stores the result to a list of points in std associated with the composition comp."""
		xStep, yStep = (ends[1].get(X_AXIS)-ends[0].get(X_AXIS))/(n-1.0), (ends[1].get(Y_AXIS)-ends[0].get(Y_AXIS))/(n-1.0)
		for i in range(0, n):
			pnt = ends[0].clone()
			pnt.set(X_AXIS, ends[0].get(X_AXIS) + i*xStep)
			pnt.set(Y_AXIS, ends[0].get(Y_AXIS) + i*yStep)
			_stg.moveTo(pnt)
			markPoint(std, comp, False)
	def scatter(pts, comp, withImg=False, n=5, d=0.1):
		"""scatter(pts, comp, withImg=False, n=5, d=0.1)
		Adds 'n' points to the point list 'pts' associated with the named material 'comp' selected at random a distance 'd' from the current stage point."""
		home = _stg.getPosition()
		for i in range(0, n):
			if i <> 0:
				pnt = home.clone()
				th = 2.0 * jl.Math.PI * jl.Math.random()
				pnt.set(X_AXIS, pnt.get(X_AXIS) + jl.Math.cos(th) * d * jl.Math.random())
				pnt.set(Y_AXIS, pnt.get(Y_AXIS) + jl.Math.sin(th) * d * jl.Math.random())
				_stg.moveTo(pnt)
			markPoint(pts, comp, withImg)

class Elapse:
	"""A simple elapse timer to estimate ETA from elapse time."""
	
	def __init__(self, nItems):
		self._nItems = nItems
		self._start = jl.System.currentTimeMillis()
		
	def inSeconds(self):
		return 0.001 * (jl.System.currentTimeMillis() - self._start)
	
	def update(self, completed):
		"""update(completed)
		completed:  The number of items completed
        Estimates the ETA based on the number of items completed since the Elapse object was constucted."""
		print "Completed: %d of %d" % (completed, self._nItems)
		now = jl.System.currentTimeMillis()
		elapse = self.inSeconds()
		print "   Elapse: %d:%02d:%02d" % (int(elapse) / 3600, (int(elapse) / 60) % 60, int(elapse) % 60)
		if completed > 0:
			eta = int((elapse * (self._nItems - completed)) / completed)
			print "      ETA: %d:%02d:%02d" % (eta / 3600, (eta / 60) % 60, eta % 60)
			
	def updateString(self, completed):
		now = jl.System.currentTimeMillis()
		elapse = 0.001 * (now - self._start)
		tmp = "TBD"
		if completed > 0:
			eta = int((elapse * (self._nItems - completed)) / completed)
			tmp = "%d:%02d:%02d" % (eta / 3600, (eta / 60) % 60, eta % 60)
		return "Elapse: %d:%02d:%02d  ETA: %s" % (int(elapse) / 3600, (int(elapse) / 60) % 60, int(elapse) % 60, tmp)
		
			
if connect:
	def collectSpectra(sample, pts, liveTime=60.0, fov=0.005):
		"""collectSpectra(sample, pts, liveTime=60.0, fov=0.005)
		Collect spectra on the named sample at each of the stage points in pts for the specified live time and field-of-view.  The \
results are written to the defaultDir."""
		for i, pnt in enumerate(pts):
			if terminated:
				return
			moveTo(pnt)
			name = "%s Auto%d.msa" % (sample, i)
			collectImages(name, 0.256, (1024, 1024), writeMask=SAVE_IMAGE_MASK)
			_ts.setViewField(fov)
			write(collect(liveTime, name), name, fmt="msa")

	def collectStandards(sample, pts, liveTime=60.0, fov=0.005, disp = True, combine=True, offenze=True):
		"""collectStandards(sample, pts, liveTime=60.0, fov=0.005, combine=True, offenze)
		Collect standard spectra from the named sample at the specified points for the specified livetime.  
"pts" is a list containing a tuple, the first element of which is a material name and the second element of which is a list of StageCoordinate objects. 
"combine=True" to build a spectrum representing the sum of the spectra.
"offenze" turns the instrument power off at the end of the operation."""
		global faraday
		global lastPCUpdate
		lastPCUpdate=None	# force PC update initially
		if not globals().has_key("faraday"):
			print "Please define the global variable 'faraday=stg.getPosition()' for the location of the faraday cup."
			return
		el = Elapse(len(pts))
		completed = 0
		startPt = _stg.getPosition()
		prev = startPt.clone()
		for item in pts:
			if terminated:
				break
			try:
				comp = material(item[0])
			except:
				comp = None
			for i, pnt in enumerate(item[1]):
				done = False
				while not (done or terminated):
					try:
						name = "%s %s[%d]" % (sample, item[0], i)
						newPt = startPt.clone()
						newPt.set(X_AXIS, pnt.get(X_AXIS))
						newPt.set(Y_AXIS, pnt.get(Y_AXIS))
						newPt.set(Z_AXIS, pnt.get(Z_AXIS))
						newPt.set(R_AXIS, pnt.get(R_AXIS))
						_stg.moveTo(newPt)
						collectImages(name, 0.256, (512, 512), writeMask=SAVE_IMAGE_MASK)
						specs = collect(liveTime, name=name, pc=True, mode='L', disp=disp, forcePC=False, fov=fov)
						if combine and len(specs)>1:
							sum=epq.SpectrumMath(specs[0])
							for sp in specs[1:]:
								sum.add(sp,1.0)
							epq.SpectrumUtils.rename(sum, "%s[all]" % (name))
							sum.getProperties().setDetector(pt_det_all)
							specs=list(specs)
							specs.append(sum)
						clear()
						display(specs)
					except jl.Throwable, ex:
						print "Failed: retrying..."
						print ex
						time.sleep(10.0)
						_stg.moveTo(prev)
						done = False
					else:
						if terminated:
							break
						for spec in specs:
							sp = spec.getProperties()
							if comp:
								sp.setCompositionProperty(epq.SpectrumProperties.StandardComposition, comp)
							sp.setObjectProperty(epq.SpectrumProperties.StagePosition, _stg.getPosition())
						write(specs, "%s" % name, fmt="msa")
						prev = pnt
						done = True
			completed = completed + 1
			el.update(completed)
		if offenze and (not terminated):
			turnOff()

def remap(oldFids, newFids, pts):
	"""remap(oldFids, newFids, pts):
	Remaps pts into the translated and rotated coordinate system defined by newFids with respect to the old coordinate system /
defined by oldFids."""
	td2 = epu.Translate2D()
	assert len(oldFids) == len(newFids)
	cps = []
	for i in range(0, len(oldFids)):
		oldSp = oldFids[i]
		newSp = newFids[i]
		cps.append(td2.createCalibrationPoint(oldSp.get(X_AXIS), oldSp.get(Y_AXIS), newSp.get(X_AXIS), newSp.get(Y_AXIS)))
	td2.calibrate(cps)
	print "%s" % td2
	print "Error = %f" % td2.error(cps)
	newPts = []
	for item in pts:
		tmp = []
		for pnt in item[1]:
			tmp.append(epq.StageCoordinate(pnt, td2))
		newPts.append((item[0], tmp,))
	return newPts

if connect:
	def extractStd(pts, name):
		"""extractStd(pts, name):
		Extract the stage points associated with the specified name from the standard point list."""
		res = []
		for pnt in pts:
			if pnt[0] == name:
				for sp in pnt[1]:
					res.append(sp)
		return res

	def removeStd(pts, name):
		"""removeStd(pts, name):
	Remove the stage points associated with the specified name from the standard point list."""
		res = []
		for pnt in pts:
			if pnt[0] != name:
				res.append(pnt)
		return res

	def validatePts(sample, pts):
		"""validatePts(sample, pts)
		Validate the points in the point list by driving to each one and asking whether the point is ok.  If the point is not ok /
	then you can select to delete the point or to replace the point."""  
		newPts = []
		for i, item in enumerate(pts):
			mat, sps = item
			newItem = (mat, [])
			for j, sp in enumerate(sps):
				print "%s\tS\t%d\t%s\t%d\t%f\t%f" % (sample, i + 1, item[0], j + 1, sp.get(X_AXIS), sp.get(Y_AXIS))
				_stg.moveTo(sp)
				res = jop.showConfirmDialog(MainFrame, "Is this point ok for %s?" % item[0], "Stage point validation", jop.YES_NO_CANCEL_OPTION)
				if res == jop.YES_OPTION:
					newItem[1].append(sp)
				elif res == jop.NO_OPTION:
					res == jop.showConfirmDialog(MainFrame, "Move to a better point and then select Ok to\nreplace the point or Cancel to delete the point.", "Stage point validation", jop.OK_CANCEL_OPTION)
					if res == jop.OK_OPTION:
						newItem[1].append(_stg.getPosition())
				else:
					return None
			newPts.append(newItem)
		return newPts

def dumpPts(sample, pts=None, fids=None):
	"""dumpPts(sample, pts=None, fids=None)
	Outputs a point list and a fiducial list to the console in tab-delimited form.  'pts' is in the form created by markPoint(...) or scatter(...) and /
'fids' is in the form of markFiducial(...)""" 
	print "Sample\tType\tIndex\tMaterial\tSub-Index\tX\tY"
	if pts:
		for i, item in enumerate(pts):
			mat, sps = item
			for j, sp in enumerate(sps):
				print "%s\tS\t%d\t%s\t%d\t%f\t%f" % (sample, i + 1, item[0], j + 1, sp.get(X_AXIS), sp.get(Y_AXIS))
	if fids:
		for i, sp in enumerate(fids):
			print "%s\tF\t%d\t%s\t%d\t%f\t%f" % (sample, i + 1, "-", 0, sp.get(X_AXIS), sp.get(Y_AXIS))

def readPts(ptsStr):	
	"""sample, pts, fids = readPts(ptsStr)
	readPts is the inverse of dumpPts(...).  readPts takes a string containing a tab delimited coordinate list /
in the form 'Sample\tType\tIndex\tMaterial\tSub-Index\tX\tY'""" 
	fids = []
	pts = []
	last = None
	for line in ptsStr.split("\n"):
		items = line.split("\t")
		sample, type, index, mat, subindex, x, y = items
		if sample == "Sample":
			continue
		ptList = None
		if type == "S":
			if (last == None) or (last[0] != mat):
				last = (mat, [])
				pts.append(last)
			ptList = last[1]
		elif type == "F":
			ptList = fids
		ptList.append(epq.StageCoordinate.fromString("{X:%s,Y:%s}" % (x, y)))
	return (sample, pts, fids)

def randomize(pts, d=0.02, n=4):
	"""randomize(pts, d=0.02, n=4)
	Takes a standard acquisition definition and for each stage point in the list creates 'n' points randomly dispersed around the point within a radius of 'd' mm."""
	res = []
	for item in pts:
		newPts = []
		for oldPt in item[1]:
			for xx in range(0, n):
				newPt = oldPt.clone()
				th = 2.0 * jl.Math.PI * jl.Math.random()
				newPt.set(X_AXIS, oldPt.get(X_AXIS) + jl.Math.cos(th) * d)
				newPt.set(Y_AXIS, oldPt.get(Y_AXIS) + jl.Math.sin(th) * d)
				newPts.append(newPt)
			newItem = (item[0], newPts)
		res.append(newItem)
	return res
	
def buildXStream():
	xs = xst.XStream()
	xs.alias("StageCoordinate", epq.StageCoordinate)
	xs.alias("Axis", epq.StageCoordinate.Axis)
	xs.alias("OpticState", semss.OpticState)
	return xs

def toXML(obj):
	"""toXML(obj): 
	Converts an object into a text-based XML representation of the object."""
	return buildXStream().toXML(obj)

def fromXML(tmp):
	"""fromXML(tmp):
	Takes a text-based XML representation of an object as created by toXML(...) and creates a binary object. toXML(...) and fromXML(..,) are inverses of each other."""
	return buildXStream().fromXML(tmp)

def setProjectBasedPaths(project, sample, mode="Manual"):
	"""setProjectBasedPaths(project, sample)
	Changes the defaultPath – the path into which image and spectrum data is written by default."""
	setDefaultPath("%s/Projects/%s/%s/%s" % (rootPath, project, sample, mode))
	
def updateZ(pts, newZ):
	"""updateZ(pts, newZ)
	Change the Z stage position to the specified value for all points in the point list pts"""
	for pnt in pts:
		pnt.set(Z_AXIS, newZ)
	return pts

if connect:
	def collectQC(lt=30.0, path=None):
		"""collectQC(lt=30.0, path='QC+time')
		Collect QC spectra and process them.  
	If the results indicate that the calibration is good, the spectra and results can be added to the QC database.
	If the results don't indicate the calibration is good, the calibration can be updated to improve the calibration."""
		if terminated:
			return
		std = "Pure copper"
		if (SITE==NIST) or (SITE==ORNL):
			e0 = 20.0
		else:
			e0 = 25.0
		if not path:
			path = "%s\\QC\\%s" % (rootPath, jtext.SimpleDateFormat("dd-MMM-yyyy H-m-s Z").format(ju.Date()))
		outPath = jio.File(path)
		outPath.mkdirs()
		if not globals().has_key("faraday"):
			print "Please define the position of the faraday cup."
			return
		if abs(_ts.hvGetVoltage() - e0 * 1.0e3) > 100.0:
			print "Please set the beam energy to %0.1f keV" % e0
			return
		i0 = updatedPC(10.0)
		if (i0.average() < 0.4) or (i0.average() > 1.2):
			print "The probe current (%0.3f nA) is out of range (0.4 nA to 1.2 nA)." % i0.average()
			return
		print "Collecting %0.1f live time second spectra please wait..." % lt
		clear()
		specs = collect(lt, std)
 		if terminated:
			return
		cals=readCalibrations()
		for i, spec in enumerate(specs):
			if terminated:
				return
			if not spec:
				continue
			ok = True
			det = spec.getProperties().getDetector()
			if not det:
				print "Not adding %s because the correct detector can not be found."
				ok = False
			print "\nDetector: %s" % det
			spec.getProperties().setNumericProperty(epq.SpectrumProperties.FaradayBegin, i0.average())
			if ok:
				qcp = Database.getQCProject(det, std, e0)
				pqc = dt2.PerformQC(qcp, spec)
				res = pqc.compute()
				clear()
				display(pqc.getResidual())
				print "\nProperty\tValue"
				for key, val in dict(res).iteritems():
					print u"%s\t%s" % (key, val)
				cw = res.get("Channel width").doubleValue()
				if cw:
					okp = jop.showConfirmDialog(MainFrame, "Update calibration to %g eV/ch?" % cw, "Update calibration?", jop.YES_NO_OPTION)
					if okp == jop.YES_OPTION:
						cals[i]=calibrate(i, cw)
					else:
						add = jop.showConfirmDialog(MainFrame, "Add this spectrum to the QC database?", "collectQC()", jop.YES_NO_OPTION)
						if add== jop.YES_OPTION:
							pqc.addToProject()
				f = jio.File(outPath, "%s.html" % det.getName())
				dt2.PerformQC.generateReport(qcp, f, res.keySet())
				print "Report written to: %s" % f
				wrap(spec).save("%s/QC[%s][%s].msa" % (path, det.getName(), std))
				report(pqc.toHTML())
		if terminated:
			return
		writeCalibrations(cals)
	
	def readCalibrations():
		cal=[]
		for i in range(0, defaultDetCount):
			cal.append(_pt.getADCGain(i))
		try:
			tmp=jupref.userNodeForPackage(_pt.getClass()).get("SDDCal",None)
			if tmp:
				cal=fromXML(tmp)
		except:
			print "Error loading SDD calibrations."
		return cal
		
	def writeCalibrations(cals):
		tmp=jupref.userNodeForPackage(_pt.getClass()).put("SDDCal",toXML(cals))
		
	# configureEDS()
	setResolution("Medium")
	updateCalibration()
		
if connect:
	def collectTiledImages(tiling, mags=((2.0, 0,), (0.5, 0,)), overlap=0.9, imgDim= 1024, imgDwell=4, seed=0xBADF00D):
		"""collectTiledImage(tiling, mags[=(( 2.0, 0, ),(0.5, 0, ))], overlap[=0.9], seed[=0xBADF00D])
		Collect a set of images for the specified tiling at the specified mags.
	tiling: semstg.CircularTiling, semstg.BoundaryTiling etc
	mags  = (( FOV0, maxTiles0), (FOV1, maxTiles1),...) where FOVX in mm and maxTiles=0 is full set
	overlap: extent of field overlap 
	seed: random seed for when maxTiles < tiling.size()"""
		global defaultPath
		if terminated:
			return
		tileCx = 0
		for fov, cx in mags:
			tiling.setTileDimension([overlap * fov, overlap * fov])
			print "FOV = %g\tCount = %g\tArea = %g\t%s" % (fov, (tiling.size() if cx == 0 else cx), tiling.getTileArea(), ("Ok" if checkTiling(tiling) else "Outside"))
			tileCx = tileCx + (tiling.size() if cx == 0 else cx)
			if not checkTiling(tiling):
				print "The tilings are not entirely with the bounds of the stage's travel."
				return

		elapse = Elapse(tileCx)
		tileCx = 0

		start = jl.System.currentTimeMillis()
		path = defaultPath + "/Analysis %s" % (jtext.SimpleDateFormat("yyyy-MM-dd HH.mm.ss.Z").format(ju.Date()))
		jio.File(path).mkdirs()
		# writeTextFile("%s/script.py" % path, _script)
		tf = textFile("%s/tiles.txt" % path)
		if globals().has_key("fps") and (not globals().has_key("pts")):
			pts = []
			for fp in fps:
				pts.append(fp.getStage())

		try:
			tf.append("MAG\tFIELD\tFOV\tAREA\tTOTAL_AREA\tX\tY\tZ\n")
			for mi, mag in enumerate(mags):
				if terminated:
					break
				fov = mag[0]
				_sem.tescanSEM.setViewField(fov)
				maxTiles = mag[1]
				mag = mi
				tiling.setTileDimension([overlap * fov, overlap * fov])
				area = tiling.getTileArea()
				if maxTiles > 0:
					tiling = semstg.RandomSubSet(tiling, maxTiles, seed)
				for i, tile in enumerate(tiling):
					if terminated:
						break
					if moveToTile(tile, xyOnly=True):
						rPt = _stg.getPosition()
						time.sleep(0.5)
						collectImages("Tile[%d,%d]" % (mag, i) , fov, dims=(imgDim, imgDim), dwell=imgDwell, path=path, writeMask=SAVE_IMAGE_MASK)
						tf.append("%d\t%d\t%g\t%g\t%g\t%g\t%g\t%g\n" % (mag, i, fov, fov * fov, area, rPt.get(X_AXIS), rPt.get(Y_AXIS), rPt.get(Z_AXIS)))
						tf.flush()
					tileCx = tileCx + 1
					if tileCx % 10 == 0:
						print "%s\tI = %0.3f" % (elapse.updateString(tileCx), _ts.getIAbsorbed() / 1000.0)
				# write(tiling.draw(2048), "Tiling[MAG=%d]" % mag, path=path)
				write(semstg.TilingUtilities.createMap(tiling, 2048), "Tiling[MAG=%d]" % mag, path=path)
		finally:
			tf.close()
		print elapse.updateString(tileCx)
			
			
	def collectTiledSIs(tiling, mags=((2.0, 0,), (0.5, 0,)), overlap=0.9, imgDim= 1024, imgDwell=9, seed=0xBADF00D):
		"""collectTiledImage(tiling, sample, mags[=(( 2.0, 0, ),(0.5, 0, ))], overlap[=0.9], seed[=0xBADF00D])
		Collect a set of images for the specified tiling on the named sample at the specified mags.
	tiling: semstg.CircularTiling, semstg.BoundaryTiling etc
	mags  = (( FOV0, maxTiles0), (FOV1, maxTiles1),...) where FOVX in mm and maxTiles=0 is full set
	overlap: extent of field overlap 
	seed: random seed for when maxTiles < tiling.size()"""
		global defaultPath
		tileCx = 0
		for fov, cx in mags:
			tiling.setTileDimension([overlap * fov, overlap * fov])
			print "FOV = %g\tCount = %g\tArea = %g\t%s" % (fov, (tiling.size() if cx == 0 else cx), tiling.getTileArea(), ("Ok" if checkTiling(tiling) else "Outside"))
			tileCx = tileCx + (tiling.size() if cx == 0 else cx)
			if not checkTiling(tiling):
				print "The tilings are not entirely with the bounds of the stage's travel."
				return

		elapse = Elapse(tileCx)
		tileCx = 0

		start = jl.System.currentTimeMillis()
		path = defaultPath + "/Analysis %s" % (jtext.SimpleDateFormat("yyyy-MM-dd HH.mm.ss.Z").format(ju.Date()))
		jio.File(path).mkdirs()
		tf = textFile("%s/tiles.txt" % path)
		if globals().has_key("fps") and (not globals().has_key("pts")):
			pts = []
			for fp in fps:
				pts.append(fp.getStage())

		try:
			tf.append("MAG\tFIELD\tFOV\tAREA\tTOTAL_AREA\tX\tY\tZ\n")
			for mi, mag in enumerate(mags):
				if terminated:
					break
				fov = mag[0]
				_sem.tescanSEM.setViewField(fov)
				maxTiles = mag[1]
				mag = mi
				tiling.setTileDimension([overlap * fov, overlap * fov])
				area = tiling.getTileArea()
				if maxTiles > 0:
					tiling = semstg.RandomSubSet(tiling, maxTiles, seed)
				for i, tile in enumerate(tiling):
					if terminated:
						break
					if moveToTile(tile, xyOnly=True):
						rPt = _stg.getPosition()
						time.sleep(0.5)
						collectSI("Tile[%d,%d]" % (mag, i) , fov, frameCount=1, dwell=imgDwell, dims=(imgDim, imgDim), path=path)
						tf.append("%d\t%d\t%g\t%g\t%g\t%g\t%g\t%g\n" % (mag, i, fov, fov * fov, area, rPt.get(X_AXIS), rPt.get(Y_AXIS), rPt.get(Z_AXIS)))
						tf.flush()
					tileCx = tileCx + 1
					if tileCx % 10 == 0:
						print "%s\tI = %0.3f" % (elapse.updateString(tileCx), _ts.getIAbsorbed() / 1000.0)
				write(semstg.TilingUtilities.createMap(tiling, 2048), "Tiling[MAG=%d]" % mag, path=path)
		finally:
			tf.close()
		print elapse.updateString(tileCx)
			


def startIn(seconds, proc):
	"""startIn(seconds, proc)
	Execute the function 'proc' after a 'seconds' delay.  Timing is approximate (slightly more then specified)"""
	print "Starting in %d:%02d:%02d" % (seconds / 3600, (seconds / 60) % 60, seconds % 60) 
	for i in xrange(seconds, 0, -1):
		 time.sleep(1.0)
		 if (seconds < 60) or ((seconds < 3600)and(i % 60 == 0)) or (i % 600 == 0):
		 	print "%d:%02d:%02d" % (i / 3600, (i / 60) % 60, i % 60) 
		 if terminated:
		 	return
	proc()

def replicates(name, dur=1.0, reps=100, fov=0.010):
	"""replicates(name, [dur=1.0], [reps=100], [fov=0.005])
	Collect reps replicate spectra for dur liveTime from field-of-view fov (in mm).  This is useful for determining whether a sample changes during acqusition."""
	res = []
	global defLED
	oldDef = defLED
	defLED = False
	oldVf = _ts.getViewField()
	oldSp = _ts.scGetSpeed()
	_ts.scSetSpeed(2)
	_ts.scScanXY(64, 64, 0, 0, 64, 64, False)
	sumSpecs = []
	_ts.setViewField(fov)
	try:
		time.sleep(1.0)
		for i in xrange(0, reps):
			if terminated:
				break
			_ts.setViewField(fov)
			tmp = collect(dur, name="%s[T%d]" % (name, i), pc=(i == 0), disp=False, forcePC=False)
			res.append(tmp)
			for j in range(0, len(tmp)):
				if len(sumSpecs) <= j:
					sumSpecs.append(epq.SpectrumMath(tmp[j]))
				else:
					sumSpecs[j].add(tmp[j], 1.0)
			display(tmp)
			if terminated:
				break
	finally:
		_ts.setViewField(oldVf)
		_ts.scSetSpeed(oldSp)
		_ts.scScanXY(512, 512, 0, 0, 512, 512, False)
		defLED = oldDef
		_ts.chamberLed(defLED)
		for i, s in enumerate(sumSpecs):
			epq.SpectrumUtils.rename(s, "%s[%d] sum" % (name, i))
		display(sumSpecs)
	return res

def repliQuant(specss, e0, stds, refs={}, preferred=(), elmByDiff=None, elmByStoic=None, assumedStoic={}):
	"""repliQuant(specss, e0, stds, refs = {}, preferred=(), elmByDiff=None, elmByStoic=None, assumedStoic = {}):
	Quantify the data returned from replicates."""
	res = [ ]
	for deti in range(0, len(specss[0])):
		detstds = {}
		for elm, std in stds.iteritems():
			detstds[elm] = std[deti]
		mq = multiQuant(pt_det[deti], e0, detstds, refs, preferred, elmByDiff, elmByStoic, assumedStoic)
		tmp = []		
		for specs in specs:
			spec = specs[deti]
			mq.compute(spec)
			tmp.append(spec)
		tabulate(tmp, withErrs=True, precision=5)
	return tuple(res)

def collectMe(pts, lt=300, name="Spectrum", volatile=False, fov=0.2):
	"""collectMe(pts, lt=300, name="Spectrum", volatile=False, fov=0.2)
	Collect an image and spectrum from each point"""
	if terminated:
		return
	res = []
	for i, pnt in enumerate(pts):
		moveTo(pnt)
		time.sleep(1.0)
		collectImages("%s%d" % (name, i), fov, writeMask=SAVE_IMAGE_MASK)
		if volatile:
			res.append(collect(10.0, "%s%d_before" % (name, i), fov=0.005, forcePC=True))
		res.append(collect(lt, "%s%d" % (name, i), fov=0.005, forcePC=(not volatile)))
		if volatile:
			res.append(collect(10.0, "%s%d_after" % (name, i), fov=0.005))
		if terminated:
			break
	return tuple(res)

def filterByDetector(specs, det):
	"""filterByDetector(specs, det)
	Returns a subset of specs containing only those spectra collected on det."""
	result = []
	for spec in specs:
		if spec.getProperties().getDetector() == det:
			result.append(spec)
	return tuple(result)

def select(specs):
	"""select(specs)
	Select and display the specified spectra ."""
	DataManager.clearSelections()
	DataManager.select(specs, True)
	
def toLine(pt0, pt1, nPts):
	"""toLine(pt0, pt1, nPts):
	Takes two stage points (pt0 and pt1) and creates a line of 'nPts' stage points forming a dotted line between them."""
	res = []
	for n in range(0, nPts + 1):
		inc = [(n * (pt1.get(X_AXIS) - pt0.get(X_AXIS))) / nPts, (n * (pt1.get(Y_AXIS) - pt0.get(Y_AXIS))) / nPts, 0.0]
		tr = epu.Translate2D(inc, 0.0)
		res.append(epq.StageCoordinate(pt0, tr))
	return tuple(res)


def toLine2(pt0, pt1, dp):
	res = []
	dx = pt1.get(X_AXIS) - pt0.get(X_AXIS)
	dy = pt1.get(Y_AXIS) - pt0.get(Y_AXIS)
	norm = jl.Math.sqrt(dx*dx+dy*dy)
	nPts= norm / dp
	for n in range(0, nPts + 1):
		inc = [n * dp * dx / norm, n * dp * dy / norm, 0.0]
		tr = epu.Translate2D(inc, 0.0)
		res.append(epq.StageCoordinate(pt0, tr))
	return tuple(res)

def cr(radius):
	"""cr(radius)
	Define a tuple of three points outlining a circle based on the current stage position and a radius."""
	center=position()
	res=[]
	res.append(epq.StageCoordinate(center, epu.Translate2D([0, -radius, 0.0], 0.0)))
	res.append(epq.StageCoordinate(center, epu.Translate2D([-0.866*radius, 0.5*radius, 0.0], 0.0)))
	res.append(epq.StageCoordinate(center, epu.Translate2D([-0.866*radius, -0.5*radius, 0.0], 0.0)))
	return tuple(res)

def combine(specs):
	"""combine(specs) - Intended to sum spectra aquired using collect() into a single sum spectrum."""
	res=None
	name = None
	for spec in specs:
		specName=str(spec)
		if not name:
			name = specName
		else:
			i = 0
			while name[i]==specName[i]:
				i=i+1
				if i>=len(name) or i>=len(specName):
					break
			name=specName[0:i]
		if not res:
			res=epq.SpectrumMath(spec)
		else:
			res.add(spec,1.0)
	res.getProperties().setDetector(pt_det_all)
	if len(name)==0:
		name="Combined["
	epq.SpectrumUtils.rename(res,name+"C]")
	return res

def dump(comps, prop=epq.SpectrumProperties.MicroanalyticalComposition):
	"""dump(comps,[prop=epq.SpectrumProperties.MicroanalyticalComposition])
	Dumps either a collection of compositions or a collection of compositions of ISpectrumData objects."""
	tmp=[]
	elms = []
	for comp in comps:
		try:
			if isinstance(comp,epq.ISpectrumData):
				xxx = comp.getProperties().getPropertyWithDefault(prop, None)
				xxx.setName(comp.toString())
				comp = xxx
			if not comp:
				continue
			xxx = material(comp)
			if xxx:
				tmp.append(xxx)
				for elm in xxx.getElementSet():
					if not elm in elms:
						elms.append(elm)
		except:
			continue
	ss="Material\t"
	for elm in elms:
		ss="%s\t%s" % (ss, elm.toAbbrev())
	print ss
	ss="Material\t"
	for elm in elms:
		ss="%s\t%s" % (ss, elm.getAtomicNumber())
	print ss
	for comp in tmp:
		ss="%s\t%s" % ( comp.toString(), comp.descriptiveString(False))
		for elm in elms:
			ss="%s\t%g" % (ss, 100.0*comp.weightPercent(elm, False))
		print ss
		
UNDERFILL = semstg.BaseTiling.Mode.Underfill
CENTERINSIDE = semstg.BaseTiling.Mode.CenterInside
COVERS = semstg.BaseTiling.Mode.Covers		
		
def circularTiling(pts, mode=UNDERFILL, serpentine=False, fov = 0.5):
	""""circularTiling(pts, mode=UNDERFILL, serpentine=False, fov = 0.5 )
	Creates a tiling from a list of three StageCoordinate points from around the perimeter of the circle."""
	res = semstg.CircularTiling(pts, mode, serpentine)
	res.setTileDimension([fov,fov])
	return res
	
def rectangularTiling(pts, mode=UNDERFILL, serpentine=False, fov = 0.5 ):
	""""rectangularTiling(pts, mode=UNDERFILL, serpentine=False, fov = 0.5 )
	Creates a tiling from a list of two StageCoordinate points at outer extremeties of a rectangle aligned with the stage axes."""
	res = semstg.RectangularTiling(pts, mode, serpentine)
	res.setTileDimension([fov,fov])
	return res

def boundaryTiling(pts, mode=UNDERFILL, serpentine=False, fov = 0.5 ):
	""""boundaryTiling(pts, mode=UNDERFILL, serpentine=False, fov = 0.5 )
	Creates a tiling from a list of StageCoordinate points from around the perimeter of an object. The points aren't reordered."""
	res = semstg.BoundaryTiling(pts, mode, serpentine)
	res.setTileDimension([fov,fov])
	return res
	
def linearTiling(pts, overlap=0.0, fov = 0.5):
	""""linearTiling(pts, overlap=0.0, fov = 0.5)
	Creates a tiling from a list of StageCoordinate points.  The tiles are constructed by drawing lines between the points and placing tiles at the appropriate spacing along the lines."""
	res = semstg.LinearTiling(pts, overlap)
	res.setTileDimension([fov,fov])
	return res
	
def polygonalTiling(pts, mode=UNDERFILL, serpentine=False, fov = 0.5):
	""""polygonalTiling(pts, mode=UNDERFILL, serpentine=False )
	Creates a tiling from a list of StageCoordinate points from around the perimeter of an object. The points are reordered to maximize the area inside."""
	res = semstg.PolygonalTiling(pts, mode, serpentine)
	res.setTileDimension([fov,fov])
	return res
	
def randomSubSetOfATiling(tiling, maxFields, seed=None):
	"""randomSubSetTiling(tiling, maxFields):
	Adapts a tiling to analyze at most maxFields chosen in an order that attempts to minimize stage travel distance.  Use this to analyze a pre-established number of tiles chosen at random."""
	if (maxFields>0) and (tiling.size()>maxFields):
		if not seed:
			seed = int(jl.System.currentTimeMillis()%0xFFFFFFF)
		return semstg.RandomSubSet(tiling, maxFields,seed)
	else:
		return tiling

def randomizedTiling(tiling, seed=None):
	"""randomizedTiling(tiling, seed=None):
	Adapts a tiling to select the tiles in a random order.  Use this to select an unspecified number of tiles until some other criterion is met (such as number of particles or time)."""
	if not seed:
		seed = int(jl.System.currentTimeMillis()%0xFFFFFFF)
	return semstg.RandomizeOrder(tiling, seed)

def displayTiling(tiling, dim=2048, fov=None):
	"""displayTiling(tiling, dim=2048, fov=None)
	Wraps a tiling with code that tracks the position of the stage as it moves and draws the resulting tiling and visitation record to an image file."""
	if fov:
		tiling.setTileDimension([fov,fov])
		return semstg.DisplayTiling(tiling, dim, fov)
	else:
		return semstg.DisplayTiling(tiling, dim)
	
def everyNthTile(tiling, n, start=0, end=100000):
	"""everyNthTile(tiling, n, start=0, end=100000)
	Adapts a tiling to analyze only every n-th tile from start to end."""
	return semstg.AdapterTiling(tiling, start, end, n)
	
def multiTiling( tilings ):
	"""multiTiling( tilings )
	Creates a single tiling from a collection of multiple region tilings."""
	return semstg.MultiRegion(tilings)
	
def dumpTiling(tiling, filename, fov=None, path = None):
	"""dumpTiling(tiling, filename, fov=None, path = None)
	Creates a 2048 pixel image showing the layout of the tiles in the specified tiling."""
	tiling.setTileDimension([fov,fov])
	dt=displayTiling(tiling, dim=2048, fov=fov)
	path = (path if path else defaultPath)
	write(dt.getImage(),filename, path)
	
	
def areaCriterion(min, max=4.0e4, diag=False):
	"""areaCriterion(min, max=4.0e4, diag=False)
	Creates a morphology rule based on particle area."""
	res = semtr.RcaTranslator.AreaCriterion(min, max)
	return (res if not diag else semtr.RcaTranslator.Diagnostic(res, StdOut))
		
	
def lengthCriterion(min, max=1.0e3, diag=False):
	"""lengthCriterion(min, max=4.0e4, diag=False)
	Creates a morphology rule based on average diameter."""
	res = semtr.RcaTranslator.DAverageCriterion(min, max)
	return (res if not diag else semtr.RcaTranslator.Diagnostic(res, StdOut))
	
def maxCriterion(min, max=1.0e3, diag=False):
	"""maxCriterion(min, max=1.0e3, diag=False)
	Creates a morphology rule based on maximum diameter."""
	res = semtr.RcaTranslator.DMaxCriterion(min, max)
	return (res if not diag else semtr.RcaTranslator.Diagnostic(res, StdOut))
	
def aspectCriterion(min, max=4.0e4, diag=False):
	"""aspectCriterion(min, max=4.0e4, diag=False)
	Creates a morphology rule based on aspect ratio (dMax/dPerp)."""
	res = semtr.RcaTranslator.AspectCriterion(min, max)
	return (res if not diag else semtr.RcaTranslator.Diagnostic(res, StdOut))
	
def abbrevName(name, maxLen=80, filler="--"):
	if len(name)>maxLen:
		fr = 2*maxLen/3 - 2
		return name[0:fr]+"--"+name[fr+len(filler)-maxLen:len(name)]
	else:
		return name
	
def mapTiling(tiling, hdim=1024):
	"""mapTiling(tiling, hdim=1024):
	Draws an image containing a map of the tiles in the specified tiling."""
	map = semstg.TilingUtilities.createMap(tiling, hdim)
	write(map, "%s map" % (abbrevName(str(tiling)), ), defaultPath)

def reportMemory(performGC=True):
	if performGC:
		jl.System.gc()
	rt=jl.Runtime.getRuntime()
	print "Memory usage: %g MB free out of %g MB allocated and %g MB total" % (rt.freeMemory()/1048576.0, rt.totalMemory()/1048576.0, rt.maxMemory()/1048576.0)

def computePlane(a, b, c):
	"""computePlane(a,b,c)
	Parameterize a plane defined by three points a, b, c in terms of a normal and a value d = dot(n,a).
If p is a point on the plane then dot(n,p)=d. Knowing any two coordinates we can solve for the third."""
	def dot(a, b):
		return a[0]*b[0]+a[1]*b[1]+a[2]*b[2]
	bma = (b.get(X_AXIS)-a.get(X_AXIS), b.get(Y_AXIS)-a.get(Y_AXIS), b.get(Z_AXIS)-a.get(Z_AXIS) )
	cma = (c.get(X_AXIS)-a.get(X_AXIS), c.get(Y_AXIS)-a.get(Y_AXIS), c.get(Z_AXIS)-a.get(Z_AXIS) )
	#bma = (b[0]-a[0], b[1]-a[1], b[2]-a[2] )
	#cma = (c[0]-a[0], c[1]-a[1], c[2]-a[2] )
	# Check if the three points are almost in a line
	angle = jl.Math.toDegrees(jl.Math.acos(jl.Math.abs(dot(bma,cma))/jl.Math.sqrt(dot(bma,bma)*dot(cma,cma))))
	if ((angle>-20) and (angle<20)) or ((angle>160) and (angle<200)):
		return (None, None)
	bmaXcma = (bma[1]*cma[2]-bma[2]*cma[1], bma[2]*cma[0]-bma[0]*cma[2], bma[0]*cma[1]-bma[1]*cma[0])
	nbmaXcma = jl.Math.sqrt(dot(bmaXcma,bmaXcma))
	n = map(lambda p : p/nbmaXcma, bmaXcma)
	return (n, dot((a.get(X_AXIS), a.get(Y_AXIS), a.get(Z_AXIS)) ,n))  # Normal, distance along normal

def meshZ(point, points):
	"""meshZ(point, points)
	Computes the z-height for 'point' from a list of x,y,z stage positions called 'points'.  The x,z,z stage positions form a mesh.  
	The closes three 'points' to 'point' are used to define a plane that is used to interpolate the x-y position in 'point' into a z-height.  
	meshZ returns a float, the z-height at x-y coordinates in 'point'."""
	def cmpDist(x,y):
		if x[0]<y[0]:
			return -1
		elif x[0]>y[0]:
			return 1
		else:
			return 0
	def computeZ(n, d, p):
		return (d-(n[0]*p[0]+n[1]*p[1]))/n[2]
	assert len(points)>2
	res = 10.0
	distPts = []
	minZ, maxZ = (100.0, -100.0)
	for pnt in points:
		dist = jl.Math.pow(point.get(X_AXIS)-pnt.get(X_AXIS),2.0)+jl.Math.pow(point.get(Y_AXIS)-pnt.get(Y_AXIS),2.0)
		distPts.append( (dist, pnt) )
		z = pnt.get(Z_AXIS)
		if z<minZ:
			minZ=z
		if z>maxZ:
			maxZ=z
	distPts.sort(cmp=cmpDist)
	res = 0.5*(minZ+maxZ) # default
	#print distPts[0], distPts[1], distPts[2], distPts[len(distPts)-1]
	for i in range(2, len(distPts)):
		n, d = computePlane(distPts[0][1], distPts[1][1], distPts[i][1])
		if n:
			#print i
			break
	if n:
		#print n, d
		res = computeZ(n, d, (point.get(X_AXIS), point.get(Y_AXIS)))
	if res<minZ:
		print "Truncating Z = %f to %f - minZ" % (res, minZ)
		return minZ
	elif res>maxZ:
		print "Truncating Z = %f to %f - maxZ" % (res, maxZ)
		return maxZ
	else:
		return res
		
def compareSpecs(specs):
	"""compareSpecs(s1, s2)
	Generates a number which measures how similar two spectra are which should be approximately 1.0 for spectra that differ only in count statistics and then sorts the spectra according to similarity."""
	def cmp(a, b):
		if a[0]<b[0]:
			return -1
		elif a[0]>b[0]:
			return 1
		else:
			return 0
	specs=map(wrap, specs)
	n=map(lambda s: s.probeCurrent()*s.liveTime(), specs)
	nTot = reduce(lambda x,y : x+y, n)
	sum=[0.0, ]*len(specs)
	minCh=specs[0].channel(100.0)
	maxCh = specs[0].channel(specs[0].beamEnergy()*1000)
	for i in range(minCh, maxCh):
		c=[]
		avg=0.0
		for spec in specs:
			cx=spec.getCounts(i)
			c.append(cx)
			avg+=cx
		if avg>0.0:
			ss = "%d" % i
			for i in range(0, len(specs)):
				cx=c[i]
				cxa = avg*n[i]/nTot
				d = jl.Math.sqrt(jl.Math.pow(cx-cxa,2.0)/cxa)
				ss = "%s\t%f" % (ss, d)
				sum[i]=sum[i]+d
			# print ss
	res = []
	for i in range(0, len(specs)):
		res.append( (sum[i]/(maxCh-minCh), specs[i]))
	res.sort(cmp)
	return res

	
# Import pyRules to help reclassify data sets
import dtsa2.pyRules as pr

def buildPoints(n=3):
	"""buildPoints([n=3])"""
	pts=[]
	for i in range(0,n):
		ok = (jop.showConfirmDialog(MainFrame, "Locate point %d of %d points." % (i+1, n), "Build point list", jop.OK_CANCEL_OPTION) == jop.YES_OPTION)
		if ok:
			pts.append(position())
		else:
			break
	return pts

# Simple backup script based on
# https://codereview.stackexchange.com/questions/101616/simple-backup-script-in-python
# Adapted: NWMR 22-May-2018

def backup(src, dest, verbose = False):
	"""backup(src, dest, [verbose = False])
	Copy the new or modified files in the directory src to the directory dest."""
	import os
	import shutil
	import sys
	def size_if_newer(source, target):
		"""If newer returns size else returns False"""
		src_stat = os.stat(source)
		try:
			target_ts = os.stat(target).st_mtime
		except:
			target_ts = 0
		return src_stat.st_size if (src_stat.st_mtime-target_ts > 1) else False
		
	def sync_file(source, target, verbose):
		size = size_if_newer(source, target)
		if size:
			transfer_file(source, target, verbose)
			return 1
		elif verbose:
			print "Skipping %s" % (source, )
			return 0

	def transfer_file(source, target, verbose):
		try:
			shutil.copy2(source, target)
			if verbose:
				print "Copied %s to %s" % (source, target)
		except:
			os.makedirs(os.path.dirname(target))
			transfer_file(source, target, verbose)
	# Now do it...
	for path, _, files in os.walk(src):
		destpath = dest + path[len(src):]
		if terminated:
			break
		if len(files)>0:
			print "Syncing %d files in %s to %s." % ( len(files), path, destpath)
			skipped, copied = 0, 0
			for file in files:
				r = sync_file(path + "/" + file, destpath + "/" + file, verbose)
				if r==1:
					copied=copied+1
				else:
					skipped=skipped+1
				if terminated:
					break
			print "%d copied, %d skipped" % (copied, skipped)

def archive(project, dest = defaultArchivePath):
	"""archive(project, [dest=defaultArchivePath])
	Useful for backing up a project to a secondary storage location.
	Copies the most recent version of all the files in 'project' into the defaultArchivePath.
	project may be a single project name or a list or tuple of project names."""
	if not dest:
		print "Please specify 'dest' or define 'defaultArchivePath'."
		return
	if isinstance(project, (list, tuple)):
		for pr in project:
			archive(pr)
	else:
		backup(rootPath+"\\Projects\\"+project, dest+"\\"+project, False)

reportMemory()