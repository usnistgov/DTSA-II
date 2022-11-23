# DTSA-II/NISTMonte script - Nicholas W. M. Ritchie - 17-Sep-2008
# Identify a user defined detector and initialize configuration parameters
det = findDetector("Bruker 5")
e0 = 15 # keV
nTraj = 20000 # electrons
dose = 150 # nA*sec
auThickness = 10.0e-9 # 10 nanometers

# Define the materials
k412 = epq.Material(epq.Composition([epq.Element.Ca,epq.Element.Fe,epq.Element.O,
                                     epq.Element.Al,epq.Element.Mg, epq.Element.Si],
                                    [0.1090, 0.0774, 0.4276, 0.0491, 0.1166, 0.2120] ),
                                    epq.ToSI.gPerCC(5.0))
au = epq.MaterialFactory.createPureElement(epq.Element.Au)
c = epq.MaterialFactory.createPureElement(epq.Element.C)

# Place the sample at the optimal location for the detector
origin = epu.Math2.multiply(1.0e-3, epq.SpectrumUtils.getSamplePosition(det.getProperties()))

# Iterate over a range of radii (in meters)
for r in [0.1e-6, 0.2e-6, 0.4e-6, 0.8e-6, 1.6e-6]:
   if terminated:
      break
   # Create a simulator and initialize it
   monte = nm.MonteCarloSS()
   monte.setBeamEnergy(epq.ToSI.keV(e0))
   # Create a sphere of Au with a sphere of K412 inside
   center=epu.Math2.plus(origin,[0.0,0.0,-(r+auThickness)])
   sr=monte.addSubRegion(monte.getChamber(),au,nm.Sphere(center,r+auThickness))
   monte.addSubRegion(sr,k412,nm.Sphere(center,r))
   # Place it on a carbon substrate
   monte.addSubRegion(monte.getChamber(), c,      
                      nm.MultiPlaneShape.createSubstrate([0.0,0.0,-1.0], origin) )
   # Add event listeners to model characteristic radiation
   xrel=nm.XRayEventListener2(monte,det)
   monte.addActionListener(xrel)
   ei=nm.EmissionImage.watchDefaultTransitions(xrel, 512, 4.0*r, center)
   # Add event listeners to model bBremsstrahlung
   brem=nm.BremsstrahlungEventListener(monte,det)
   monte.addActionListener(brem)
   # Reset the detector and run the electrons
   det.reset()
   monte.runMultipleTrajectories(nTraj)
   # Get the spectrum and assign properties
   spec=det.getSpectrum(dose*1.0e-9 / (nTraj * epq.PhysicalConstants.ElectronCharge) )
   props=spec.getProperties()
   props.setTextProperty(epq.SpectrumProperties.SpectrumDisplayName, 
                     "%g micron sphere with 10 nm Gold coating on C substrate" % (r/1.0e-6))
   props.setNumericProperty(epq.SpectrumProperties.LiveTime, dose)
   props.setNumericProperty(epq.SpectrumProperties.ProbeCurrent,1.0)
   props.setNumericProperty(epq.SpectrumProperties.BeamEnergy,e0)
   # Write the spectrum to disk and display
   fos = jio.FileOutputStream("%s/R = %g um.msa" % (DefaultOutput, r/1.0e-6))
   noisy=epq.SpectrumUtils.addNoiseToSpectrum(spec,1.0)
   ept.WriteSpectrumAsEMSA1_0.write(noisy,fos,0)
   fos.close()
   display(noisy)
   nm.EmissionImage.dumpToFiles(ei,"%s/R = %g um images" % (DefaultOutput, r/1.0e-6))