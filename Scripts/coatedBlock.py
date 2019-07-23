import dtsa2.mcSimulate3 as mc3

def coatedBlock(mat, height, width, coating, thickness, substrate, det, e0=20.0, withPoisson=True, nTraj=1000, dose=120.0, sf=False, bf=False, xtraParams={}):   
   """coatedBlock(mat, height, width, coating, thickness, substrate, det, e0=20.0, withPoisson=True, nTraj=1000, dose=120.0, sf=False, bf=False, substrate=None, xtraParams={})
Monte Carlo simulate a spectrum from a block shaped particle of the specified material (mat) and height (z in m) and width (x and y in m). \
The block and subtrate is coated in a material 'coating' of the specified thickness which fully encapsulates the particle and covers the substrate too."""
   def buildBlock(monte, origin, buildParams):
      height = buildParams["Height"]
      width  = buildParams["Width"]
      subMat = buildParams["Substrate"]
      mat = buildParams["Material"]
      coating = buildParams["Coating"]
      thickness = buildParams["Thickness"]
      coatedCube = nm.MultiPlaneShape.createBlock([width+2.0*thickness, width+2.0*thickness, height+thickness], epu.Math2.plus(origin, [0.0, 0.0, 0.5 * (height+thickness)]), 0.0, 0.0, 0.0)
      sr1=monte.addSubRegion(monte.getChamber(), coating, coatedCube)
      cube = nm.MultiPlaneShape.createBlock([width, width, height], epu.Math2.plus(origin, [0.0, 0.0, thickness + 0.5 * height]), 0.0, 0.0, 0.0)
      monte.addSubRegion(sr1, mat, cube)
      monte.addSubRegion(monte.getChamber(), coating, nm.MultiPlaneShape.createFilm([0.0, 0.0, -1.0], epu.Math2.plus(origin, [0.0, 0.0, height+thickness]), thickness))
      monte.addSubRegion(monte.getChamber(), subMat, nm.MultiPlaneShape.createSubstrate([0.0, 0.0, -1.0], epu.Math2.plus(origin, [0.0, 0.0, height+2.0*thickness])))
   tmp = u"MC simulation of a [%0.2f,%0.2f,%0.2f] micron block of %s%s coated with %s at %0.1f keV%s%s" % (width * 1.0e6, width * 1.0e6, height * 1.0e6, mat, (" on %s" % substrate if substrate else ""), coating, e0, (" + CSF" if sf else ""), (" + BSF" if bf else ""))
   params = {"Substrate": substrate, "Width" : width, "Height" : height, "Material" : mat, "Coating" : coating, "Thickness" : thickness}
   return mc3.base(det, e0, withPoisson, nTraj, dose, sf, bf, tmp, buildBlock, params, xtraParams)

# Stuff you change....   
substrate = material("Fe",8.0)
coating = material("Cu",7.0)
block = material("Al",3.0)   
elements = "FeCuAl"  # List the elements in the substrate, coating and block...

xrts=mc3.suggestTransitions(elements)
xp = mc3.configureEmissionImages(xrts,2.0e-6,512)
# xp.update(mc3.configureXRayAccumulators(xrts,charAccum=True,charFluorAccum=True,bremFluorAccum=False))
xp.update(mc3.configureTrajectoryImage(2.0e-6,512))

display(coatedBlock(block,0.1e-6,0.2e-6,coating,0.05e-6,substrate,d1,e0=20.0, nTraj=1000, xtraParams=xp))