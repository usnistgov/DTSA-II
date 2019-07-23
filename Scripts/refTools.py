def possibleRefs(elm, min=0.1, fwhm=135.0, max=5):
	elm = element(elm)
	elmRois = epq.ReferenceMaterial.getElementROIs(elm, fwhm)
	res = { }
	for roi in elmRois:
		res[roi] = []
		for std in Database.getStandards().keySet():
			std=material(std)
			if std.weightFraction(elm, False)>min:
				rm=epq.ReferenceMaterial(elm, std, fwhm)
				usable = rm.getUsableROIs()
				if usable and usable.contains(roi):
					res[roi].append(std)
	def compW(mat1, mat2):
		wf1, wf2 = mat1.weightFraction(elm,False), mat2.weightFraction(elm,False)
		if wf1>wf2:
			return -1
		elif wf1<wf2:
			return 1
		else:
			return 0
	for roi in res.keys():
		tmp = res[roi]
		tmp.sort(compW)
		res[roi]=tmp[0:max]
	return res

def analyze(elm, mat):
	elm=element(elm)
	mat=material(mat)
	rm=epq.ReferenceMaterial(elm,mat,135.0)
	ur = rm.getUsableROIs()
	uur = rm.getUnusableROIs()
	print "Elm\tMat\tGood\tBad\tTotal"
	print "%s\t%s\t%d\t%d\t%d" % (elm, mat, ur.size() if ur else 0, uur.size() if uur else 0, rm.getROIs().size())