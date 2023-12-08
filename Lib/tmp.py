# Name:  classify.py
# Description:  This file serves as a template for developing Python-based
# classification rule files.  The mechanism is extremely general.  Almost
# anything that can be performed in Python can be performed within this simple
# class template.
# The class must be called ClassRule.
# The class must implement the method classify(vals) as described below.
# The constructor must take a single argument, the Zeppelin data set.

# The following is a trivial implementation of the ClassRule.

class ClassRule:

    def __init__(self, zep):
        """The constructor.  It takes one argument, an instance 
        of the Zeppelin class which we intend to classify."""
        self.Zeppelin = zep
        
    def ironRich(self, vals, eps):
        fe = vals.get("Fe",0.0)
        mn = vals.get("Mn",0.0)
        si = vals.get("Si",0.0)
        cr = vals.get("Cr",0.0)
        ni = vals.get("Ni",0.0)
        p = vals.get("P",0.0)
        s = vals.get("S",0.0)
        cu = vals.get("Cu",0.0)
        mo = vals.get("Mo",0.0)
        co = vals.get("Co",0.0)
        if vals.get("O",0.0) > 10.0:
            return "Iron oxide;Iron-rich;Unknown"
        if fe+mo+si+cr+ni+p+s+cu+mo+co<95.0:
            return None
        if (ni>17.0-eps) and (ni<19.0+eps) and (co>7.0-eps) and (co<12.5+eps) and (mo>3.0-eps) and (mo<5+eps):  # Maraging
            if co>11.0:
                return "350-grade;Maraging SS;Stainless steel;Iron-rich;Anthropogenic"
            if co>8.5:
                if mo>4.0:
                    return "300-grade;Maraging SS;Stainless steel;Iron-rich;Anthropogenic"
                else:
                    return "200-grade;Maraging SS;Stainless steel;Iron-rich;Anthropogenic"
            else:
                return "250-grade;Maraging SS;Stainless steel;Iron-rich;Anthropogenic"
        if (mn>5.0) and (si<1.0+eps) and (cr>16.0-eps) and (cr<19+eps) and (ni>0) and (ni<6.0+eps) and (p<eps) and (s<eps):
            return "200-series SS;Austenitic SS;Stainless steel;Iron-rich;Anthropogenic"
        if (mn>2.0-eps) and (mn<2.0+eps) and (si>2.0-eps) and (si<2.0+eps) and (cr>16.0-eps) and (cr<26.0+eps) and (ni>6.0+eps) and (ni<22.0+eps) and (p<eps) and (s<eps):
            return "300-series SS;Austenitic SS;Stainless steel;Iron-rich;Anthropogenic"
        if (mn>1.0-eps) and (mn<1.0+eps) and (si>1.0-eps) and (si<1.0+eps) and (cr>10.0-eps) and (cr<27.0+eps) and (ni<eps) and (p<eps) and (s<eps):
            return "400-series SS;Ferritic/Martensitic SS;Stainless steel;Iron-rich;Anthropogenic"
        if fe>40.0 and cr > 10.0 and ni > 5.0:
            return "Fe-Cr-Ni SS;Stainless steel;Iron-rich;Anthropogenic"
        if fe>90.0:
            return "Iron-rich;Anthropogenic"
        return None
        
    def alRich(self, vals, eps):
        return "Al-rich"
    
    def classify(self, vals):
        """A function which associates the particle identified 
        by the dictionary object vals with a string representing 
        the class name to which the particle is assigned"""
        if vals.get("FIRST_ELEM",0.0)==26.0:
            res = self.ironRich(vals, 1.0)
            if res!=None: 
                return res
        if vals.get("FIRST_ELEM", 0)==13.0:
            res = self.alRich(vals, 1.0)
            if res!=None: 
                return res
        if vals.get("Pu", 0.0)>90.0:
            return "Pu-rich;SNM;Anthropogenic"
        return "Something else"


    def classNames(self):
        """An optional method which serves to ensure that class
        names are always assigned the same integer index.  This 
        facilitates comparison between data sets.  This method
        should return a list of class names"""
        return [ ]