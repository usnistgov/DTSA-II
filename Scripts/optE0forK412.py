stds= ( material("K411"), material("Si"), material("SiO2"), material("Al"), material("Al2O3"), material("CaF2"), material("Fe2O3"), material("Fe"), material("Mg"), material("MgO") )
mat = material("K412")
props = epq.SpectrumProperties()
props.addAll(d2.getProperties())
props.setNumericProperty(epq.SpectrumProperties.BeamEnergy,15.0)
xpp = epq.CorrectionAlgorithm.XPP
xrts=[transition("Si K-L3"),transition("Al K-L3"),transition("Ca K-L3"),transition("Fe K-L3"),transition("Mg K-L3"),transition("Fe L3-M5"), transition("O K-L3")]
mac=epq.MassAbsorptionCoefficient.Default
for xrt in xrts:
	matMac=mac.computeWithUncertaintyEstimate(mat,xrt)
	print "%s\t%f\t%f" % (xrt,matMac.doubleValue(),matMac.uncertainty())
	elm = xrt.getElement()
	for std in stds:
		if std.containsElement(elm):
			for e0 in range(5, 30):
				props.setNumericProperty(epq.SpectrumProperties.BeamEnergy,e0)
				if epq.FromSI.keV(xrt.getEdgeEnergy())<e0:
					stdMac=mac.computeWithUncertaintyEstimate(std,xrt)
					print "%f\t%s\t%f\t%10.9f" % (e0, std,  std.weightFraction(elm,False), xpp.u_chi(std,mat,xrt,props)*stdMac.uncertainty())

