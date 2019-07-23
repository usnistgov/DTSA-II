# This script demonstrates that three different ways of estimating the fit uncertainty
# give essentially the same values.
# 1. Variance in repeated MLLSQ fits of simulated spectra
# 2. Calculations based on count statistics in fit peaks
# 3. Covariance matrix output from MLLSQ fits

import gov.nist.microanalysis.EPQDatabase.ReferenceDatabase as rd
import java.text as jt

simq = epq.SimulateQuantification()		

ITERATIONS = 10

doIt = 0

def dumpRes(res):
	df = jt.DecimalFormat("0.00000")
	for roi in res.keySet():
		print "%s\t%s\t%s" % (roi, res[roi], res[roi].formatLong(df))

def simulateQP(qp, unk, unkDose):
	nf = jt.DecimalFormat("0.0000")
	ds = {}
	tmp = "Sum"
	for elm in unk.getElementSet():
		ds[elm] = epu.DescriptiveStatistics()
		tmp = "%s\t%s\td%s" % (tmp, elm, elm)
	print tmp
	qq = simq.buildQuantifyUsingStandards(qp)
	for i in range(0, ITERATIONS):
		ans = qq.compute(simq.getUnknown(qp, unk, unkDose))
		res = ans.getComposition()
		tmp = nf.format(unk.sumWeightFraction())
		for elm in unk.getElementSet():
			wp = res.weightFractionU(elm, False)
			tmp = "%s\t%s\t%s" % (tmp, nf.format(wp.doubleValue()), nf.format(wp.getComponent("I[unk,%s]" % elm.toAbbrev())))
			ds[elm].add(wp.doubleValue())
		if i < 10:
			print tmp + " based on LLSQ"
	print "Summary"
	tmp = "Stats" 
	for elm in unk.getElementSet():
		tmp = tmp + "\t%s\t%s" % (nf.format(ds[elm].average()), nf.format(ds[elm].standardDeviation()))
	print tmp + " based on iterations of fit"
	tmp = "Estimated"
	okrs = ans.getUnknown().getProperties().getKRatioProperty(epq.SpectrumProperties.OptimalKRatios);
	mf = qp.massFractions(unk, unkDose)
	for elm in unk.getElementSet():
		xrts = okrs.getTransitions(elm).iterator().next()
		for me in mf.entrySet():
			if me.getKey().getXRayTransitionSet(elm).equals(xrts):
				wf = me.getValue()
				tmp = tmp + "\t%s\t%s" % (nf.format(wf.doubleValue()), nf.format(wf.getComponent("I[unk,%s]" % elm.toAbbrev())))
	print tmp + " based on simulation"
	
		
dose = 12000.0
unkDose = 60.0
refDose = 1.0e6
if doIt < 1:
	print "Test 1"
	qp = epq.QuantificationPlanner2(d1, epq.ToSI.keV(15.0))
	print qp.getRequiredReferences(element("O"), material("K411"))
	qp.addStandard(element("Fe"), material("K411"))
	qp.addStandard(element("Ca"), material("K411"))
	qp.addStandard(element("Mg"), material("K411"))
	qp.addStandard(element("Si"), material("K411"))
	qp.addStandard(element("O"), material("K411"))
	qp.addStandard(element("Al"), material("Al2O3"));
	for elm in qp.getMeasuredElements():
		qp.setStandardDose(elm, dose)
	print "Satisfied %s" % qp.getSatisfiedReferences()
	print "Unsatisfied %s" % qp.getUnsatisfiedReferences()
	print qp.getReferences()
	qp.addElementToStrip(element("C"))
	print "Added K411 for O, strip C"
	print "Satisfied %s" % qp.getSatisfiedReferences()
	print "Unsatisfied %s" % qp.getUnsatisfiedReferences()
	qp.addReference(material("Fe"), refDose)
	qp.addReference(material("C"), refDose)
	qp.addReference(material("CaF2"), refDose)
	qp.addReference(material("Al2O3"), refDose);
	print "Added references Fe, C, CaF2, Al2O3"
	print "Satisfied %s" % qp.getSatisfiedReferences()
	print "Unsatisfied %s" % qp.getUnsatisfiedReferences()
	print qp.getReferences()
	print qp.getAllRequiredReferences(False)
	report(qp.toHTML())
	
	simulateQP(qp, material("K412"), unkDose)		
	dumpRes(qp.massFractions(material("K412"), unkDose))

if doIt < 2:
	print "\n\nTest 2"
	qp = epq.QuantificationPlanner2(d1, epq.ToSI.keV(15.0))
	qp.addStandard(element("Fe"), material("Fe"))
	qp.addStandard(element("Ca"), material("CaF2"))
	qp.addStandard(element("Mg"), material("Mg"))
	qp.addStandard(element("Si"), material("Si"))
	qp.addStandard(element("O"), material("Al2O3"))
	qp.addStandard(element("Al"), material("Al2O3"))
	for elm in qp.getMeasuredElements():
		qp.setStandardDose(elm, dose)
	qp.addElementToStrip(element("C"))
	qp.addReference(material("C"), refDose)
	print "Satisfied %s" % qp.getSatisfiedReferences()
	print "Unsatisfied %s" % qp.getUnsatisfiedReferences()
	print qp.getAllRequiredReferences(False)
	report(qp.toHTML())
	
	simulateQP(qp, material("K412"), unkDose)
	dumpRes(qp.massFractions(material("K412"), unkDose))

if doIt < 3:
	print "\n\nTest 3"
	qp = epq.QuantificationPlanner2(d1, epq.ToSI.keV(15.0))
	qp.addStandard(element("Ba"), material("Benitoite"), 0.01)
	qp.addStandard(element("Ti"), material("Benitoite"), 0.01)
	qp.addStandard(element("Si"), material("Benitoite"), 0.01)
	qp.addStandard(element("O"), material("Benitoite"), 0.01)
	for elm in qp.getMeasuredElements():
		for eroi in qp.getStandardROIS():
			qp.optimize
	for roi in 

		
	print "Pre\n\tSatisfied %s" % qp.getSatisfiedReferences()
	print "\tUnsatisfied %s" % qp.getUnsatisfiedReferences()
	qp.applySuggestedReferences(rd.getInstance(None).getDatabase(), dose)
	print "Post\n\tSatisfied %s" % qp.getSatisfiedReferences()
	print "\tUnsatisfied %s" % qp.getUnsatisfiedReferences()
	print qp.getAllRequiredReferences(False)
	report(qp.toHTML())
	
	simulateQP(qp, material("K2466"), unkDose)
	dumpRes(qp.massFractions(material("K2466"), unkDose))

	
	for elm in qp.getMeasuredElements():
		jl.System.err.println(elm)
		qp.setDesiredPrecision(elm, 0.01)
		for roi in qp.getStandardROIS(elm):
			qp.optimizeDoses(roi)