import java.io as jio
import gov.nist.microanalysis.NISTMonte as nm
import gov.nist.microanalysis.EPQLibrary as epq
import dtsa2 as d2

def stemCell(h2oThickness, objMat, objDiameter, objDepth, e0, beamOffset=0.0, detectorDistance=0.008, detectorRadius=0.02, detRingCount=100, nTraj=10000):
	"""stemCell(h2oThickness, objMat, objDiameter, objDepth, e0, [beamOffset=0.0], [detectorDistance=0.008], [detectorRadius=0.02], [detRingCount=100], [nTraj=10000]):
	Model scattering from a spherical object embedded in a suspended water film.
	Example: 
		> import dtsa2.stemCell as sc
		> sc.stemCell(1.0e-6, material("Au",10.0), 2.0e-7, 4.0e-7, 100.0,nTraj=1000)"""
	monte = nm.MonteCarloSS()
	monte.setBeamEnergy(epq.ToSI.keV(e0))
	beam = nm.GaussianBeam(1.0e-10)
	beam.setCenter( ( beamOffset, 0.0, -0.01  ) )
	monte.setElectronGun(beam)
	h2o = d2.material("H2O",1.0)
	h2oThickness=max(h2oThickness, objDiameter)
	objDepth = max(0.5*objDiameter, min(h2oThickness-0.5*objDiameter, objDepth))
	h2oSr=monte.addSubRegion(monte.getChamber(), h2o, nm.MultiPlaneShape.createFilm((0.0, 0.0, -1.0), (0.0,0.0,0.0), h2oThickness))
	monte.addSubRegion(h2oSr, objMat, nm.Sphere( (0.0, 0.0, objDepth), 0.5*objDiameter) )
	ann = nm.AnnularDetector(detectorRadius, detRingCount, (0.0, 0.0, detectorDistance), (0.0, 0.0, -1.0)) 
	monte.addActionListener(ann)
	monte.runMultipleTrajectories(nTraj)
	header = "Parameters:\nWater thickness\t%g nm\nSphere material\t%s\nSphere diameter\t%g nm\nSphere center depth\t%g nm\nBeam offset\t%g nm\nDetector distance\t%g mm\nDetector radius\t%g mm\nE0\t%g keV" % (1.0e9*h2oThickness, objMat.descriptiveString(False), 1.0e9*objDiameter, 1.0e9*objDepth, 1.0e9*beamOffset, 1.0e3*detectorDistance, 1.0e3*detectorRadius, e0 )
	print header
	return (header, ann)


def reportAnnularDetector(ann, header, fileName):
	defOut = (d2.DefaultOutput if d2.DefaultOutput else d2.reportPath())
	tmpFile = jio.File(defOut, filename)
	fos = jio.FileOutputStream(tmpFile)
	try:
		osw = jio.OutputStreamWriter(fos)
		if header:
			osw.append(header+"\n")
		ann.dump(osw)
		osw.flush()
	finally:
		fos.close()
	print "Annular Detector output written to "+ tmpFile
