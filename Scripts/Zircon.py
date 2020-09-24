s=dict(Database.getStandards())
mat = material("Zircon+U,Th,Pb")
props = epq.SpectrumProperties()
props.addAll(d2.getProperties())
props.setNumericProperty(epq.SpectrumProperties.BeamEnergy,25.0)
xpp = epq.CorrectionAlgorithm.XPP
xrts=[transition("Zr K-L3"),transition("O K-L3"),transition("Si K-L3"),transition("Pb L3-M5"),transition("Pb M5-N7"),transition("U L3-M5"),transition("U M5-N7"),transition("Th L3-M5"),transition("Th M5-N7")]
mac=epq.MassAbsorptionCoefficient.Default
for xrt in xrts:
	matMac=mac.computeWithUncertaintyEstimate(mat,xrt)
	print "%s\t%f\t%f" % (xrt,matMac.doubleValue(),matMac.uncertainty())
	for name, idx in s.iteritems():
		std=material(name)
		elm = xrt.getElement()
		if std.containsElement(elm):
			stdMac=mac.computeWithUncertaintyEstimate(std,xrt)
			print "%s\t%f\t%11.9f\t%f\t%f" % (std,  std.weightFraction(elm,False), xpp.u_chi(std,mat,xrt,props), stdMac.doubleValue(),stdMac.uncertainty())

