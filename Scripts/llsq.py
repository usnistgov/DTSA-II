# A test for the LinearLeastSquares implementation against a
# data set from Aster et al (Parameter Estimation and Inverse Problems.)

class Aster2_1(epu.LinearLeastSquares):
	
	def fitFunctionCount(self):
		return 3
	
	def fitFunction(self, xi, afunc):
		afunc[0]=1
		afunc[1]=xi
		afunc[2]=0.5*xi*xi
	

x= tuple(range(1,11))
y= ( 109.4, 187.5, 267.5, 331.9, 386.1, 428.4, 452.2, 498.1, 512.3, 513 )
dy = ( 8, ) *10
	
aa=Aster2_1(x, y, dy)
print "Results with 1 sigma error bars: %s" % aa.getResults()
print "Confidence intervals (95%%): %s" % aa.confidenceIntervals(aa.INTERVAL_MODE.ONE_D_INTERVAL,0.95)
print "Chi-Sqr: %g" % aa.chiSquared()
