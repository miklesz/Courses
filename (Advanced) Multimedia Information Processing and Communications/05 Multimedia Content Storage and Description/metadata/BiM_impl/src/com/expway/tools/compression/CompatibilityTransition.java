/***********************************************************************
This software module was originally developed by Cédric Thiénot (Expway)
Claude Seyrat (Expway) and Grégoire Pau (Expway) in the course of 
development of the MPEG-7 Systems (ISO/IEC 15938-1) standard. 

This software module is an implementation of a part of one or more 
MPEG-7 Systems (ISO/IEC 15938-1) tools as specified by the 
MPEG-7 Systems (ISO/IEC 15938-1) standard. 

ISO/IEC gives users of the MPEG-7 Systems (ISO/IEC 15938-1) free license
to this software module or modifications thereof for use in hardware or
software products claiming conformance to the MPEG-7 Systems 
(ISO/IEC 15938-1). 

Those intending to use this software module in hardware or software 
products are advised that its use may infringe existing patents.

The original developer of this software module and his/her company, the
subsequent editors and their companies, and ISO/IEC have no liability
for use of this software module or modifications thereof in an
implementation. 

Copyright is not released for non MPEG-7 Systems (ISO/IEC 15938-1) 
conforming products. 

Expway retains full right to use the code for his/her own purpose, 
assign or donate the code to a third party and to inhibit third parties 
from using the code for non MPEG-7 Systems (ISO/IEC 15938-1) conforming 
products. 

This copyright notice must be included in all copies or derivative works.

Copyright Expway © 2001.
************************************************************************/

package com.expway.tools.compression;

import com.expway.tools.automata.*;
import java.util.Collection;
import java.io.IOException;

/** une transition codante : son franchissement code quelque chose dans le flux */

public class CompatibilityTransition extends CompressionTransition {

    IntegerCoding versionCoding = null;
    IntegerCoding typeCoding = null;
    
    //
    public CompatibilityTransition(String s){
        super(s);
    }

    public CompatibilityTransition(String s,State from, State to){
        super(s,from,to);
    }

    //

    public void setCodings(IntegerCoding version, IntegerCoding type){
        versionCoding = version;
        typeCoding = type;
    }

    /** callback appelé lorsque la transition est franchie la méthode
     *  peut refuser de laisser passer l'activité en lancant une exception 
     */
    public boolean accept(ActivityToken at,Object evenement,Object applicationContext) {
        if (FiniteStateAutomata.debug) System.out.println(this+" accept requested with " + at);
        // on est en mode decompression
        if (mode == CompressionMode.DECOMPRESSION){
            throw new RuntimeException("Pas prévu pour marcher en décompression");
        }
        else
            return super.accept(at,evenement,applicationContext);
    }

    /** callback appelé lorsque la transition est franchie la méthode
     *  peut refuser de laisser passer l'activité en lancant une exception 
     */
    public void cross(ActivityToken at,Object applicationContext) throws RejectionException {
        if (FiniteStateAutomata.debug) System.out.println(this+" cross requested with " + at);

        // on est en mode compression
        if (mode == CompressionMode.COMPRESSION){
            ((CompressionActivityToken)at).encodeCompatiblePieceHeader(versionCoding,typeCoding);
        }

    }

    public String getLabel(){
        return "Compat ["+versionCoding+"|"+typeCoding+"]";
    }
    
}








