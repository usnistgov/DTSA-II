# -*- coding: utf-8 -*-
# Name:     bbb.py
# Purpose:  Special code to control a TESCAN MIRA3 to search for and collect rastered spectra from 'blebs'
# Created: 3-Oct-2018
import gov.nist.microanalysis.EPQImage as epqi

def defBlebCriterion(bleb, scale):
    ecd = bleb.getEquivalentCircularDiameter()
    res = (3.1415926*ecd/bleb.getPerimeter()>0.8) and (ecd * scale > 0.0025)
    
class BlebScorer (epqi.Blobber.Scorer):
    
    def __init__(self, scale, minMean, optSize):
        self._scale = scale
        self._minMean = minMean
        self._optSize = optSize
    
    
    def score(self, blob, imageProxy):
        # portion based on mean intensity
        mi = blob.mask(imageProxy).getMeanIntensity()
        x0 = self._minMean/256.0
        sc = 1.0/(1.0 + jl.Math.exp(-16 (mi - x0)))
        # portion based on circularity
        ecd = bleb.getEquivalentCircularDiameter()
        sc = sc * (3.1415926*ecd/bleb.getPerimeter())
        # portion based on size
        sc = sc * jl.Math.exp(-jl.Math.pow((ecd * scale-self._optSize)/ 0.002, 2.0))
        return sc

class BuntinsBlessedBlebs:
    
        def __init__(self, tiling, fov, detIdx = 1):
            self._tiling = tiling
            self._imgDims = ( 512, 512 )
            self._detIdx = detIdx
            self._dwell = 4
            self._fov = fov
            self._threshes = (128,256)
            self._blebFov = 0.005
            self._blebDims = (256, 256 )
            self._blebDwell = 6
            self._acqTime = 60
            self._meetsCriteria = defBlebCriterion
            
            
        def perform(self):
            for fidx, tile in enumerate(tiling):
                moveTo(tile.getCenter())
                time.sleep(1)
                self.analyzeField(fidx)

            
        def analyzeField(self, fidx):
            imgs = collectImages("Field %d" % (fidx, ), fov=self._fov, dims=self._imgDims, dwell=self._dwell)
            blobs = epqi.Blobber(epqi.ImageProxy(imgs[self._detIdx]), self._threshes[0], self._threshes[1])
            rankedBlebs = blobs.getRankedBlobs(self._scorer)
            self.collectBleb(fidx, blob)
                    
        def centerFeature(self, blob):
            oldPos = position()
            com = blob.getCenterOfMass()
            pos = (com.x - self._imgDims[0]/2, com.y - self._imgDims[1]/2)
            newPos =  epq.StageCoordinate(position())
            pixSc = self._fov / self._imgDims
            newPos.set(X_AXIS, oldPos.get(X_AXIS) + pixSc*pos.x)
            newPos.set(Y_AXIS, oldPos.get(Y_AXIS) - pixSc*pos.y)
            moveTo(newPos)
            time.sleep(0.5)
            # Zoom in by a factor of 10 and center the largest bleb
            for zoom in [0.1, 0.01]:
                imgs = collectImages("Center", fov=zoom*self._fov, dims=self._imgDims, dwell=self._dwell)
                blobs = epqi.Blobber(epqi.ImageProxy(imgs[self._detIdx]), self._threshes[0], self._threshes[1])
                lBlob = blobs.getRankedBlobs(self._scorer).get(0)
                lcom = lBlob.getCenterOfMass()
                lpos = (lcom.x - self._imgDims[0]/2, lcom.y - self._imgDims[1]/2)
                lPos =  epq.StageCoordinate(position())
                pixSc = zoom * self._fov / self._imgDims
                lPos.set(X_AXIS, lPos.get(X_AXIS) + pixSc*pos.x)
                lPos.set(Y_AXIS, lPos.get(Y_AXIS) - pixSc*pos.y)
                moveTo(lPos)
                time.sleep(0.5)
            return oldPos
        
        
        def collectBleb(self, fieldIdx, blob):
            oldPos = centerFeature(blob)
            bleb = "Bleb[%d]" % (fieldIdx)
            imgs = collectImages(bleb, fov=self._blebFov, dims=self._blebDims, dwell=self._blebDwell)
            spec = collect2(acqTime=self._acqTime, name=bleb, pc=False, mode='L', disp=False, fov=self._blebFov)
            moveTo(oldPos)
            