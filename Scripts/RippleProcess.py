# Demonstrates how to load a ripple/raw file as an object called rr
# Author: Nicholas W. M. Ritchie
# Date:   15-May-2008

# First create a SpectrumProperties object with the information necessary 
# to perform a quant on the resulting spectrum.
sp=epq.SpectrumProperties()
sp.setDetector(d1)
sp.setNumericProperty(epq.SpectrumProperties.BeamEnergy,20.0)
sp.setNumericProperty(epq.SpectrumProperties.FaradayBegin,1.0)
sp.setNumericProperty(epq.SpectrumProperties.LiveTime,0.001)
rr=ept.RippleSpectrum("U:/SMSDEx/JeffDavis/Soil Phase/2711 Soil Phase Map/Area2_300x.rpl",sp)


rpl = openRipple("U:/SMSDEx/JeffDavis/Soil Phase/2711 Soil Phase Map/Area2_300x.rpl", 20.0, 1.0, 0.001)
quantify(rpl, stds, refs={}, preferred=(), elmByDiff=None, elmByStoic=None, assumedStoic={}, mask=None, step=1, visualize=False, zaf=None)

for row in range(0,rr.getRows()):
   rr.setPosition(row,128)
   display(rr)