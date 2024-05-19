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

import com.expway.tools.expression.*;
import com.expway.tools.automata.*;

public class TypeState extends CompressionState {

    TypeDefinition theContainedType = null;
    
    /** appelé lorsque l'etat contient un pointeur vers un autre content model **/
    /*public TypeState(){
      super();
      }*/

    /** appelé lorsque l'etat contient un pointeur vers un autre content model **/
    public TypeState(TypeDefinition theContainedType){ 
        super();
        this.theContainedType = theContainedType;
        if (theContainedType != null)
            setName(getName()+"{"+theContainedType.getName()+"}");
        else
            setName(getName()+"{null}");

    }
    

   /** appelé lorsqu'un nouveau token a avaler arrive */
   public void consume(Object inputToken,Object applicationContext){
      if (FiniteStateAutomata.debug) System.out.println(getName()+"> token " + inputToken);
   }
 
    /** appelé lorsque l'état est activé. avant en fait... */
    public void activate(ActivityToken at, Object applicationContext){
        if (FiniteStateAutomata.debug) System.out.println(getName()+"> l'état est activé avec : " + at);

        if (applicationContext!=null){
            // en mode compression on recupere le type contenu et on lui reserve une place dans 
            // l'activite de compression
            if (mode == CompressionMode.COMPRESSION){
                TypeEncoder te = ((CompressionContext)applicationContext).getCurrentTypeInstance(theContainedType);
                ((CompressionActivityToken)at).encodeType(te);
            } 
            // en mode decompression on recupere le type contenu 
            else if (mode == CompressionMode.DECOMPRESSION){
                TypeEncoder te = ((DecompressionContext)applicationContext).getCurrentTypeInstance(theContainedType);
            }
            else 
                throw new RuntimeException("Ne devrait pas arriver !!");
        }
    }
   
   /** appelé pour une remise au propre de l'état */
   public void reset(Object applicationContext){
      if (FiniteStateAutomata.debug) System.out.println(getName()+"> reset");
   }

    /** appelé a chaque nouveau consume */
   public void newTurn(Object applicationContext){
      if (FiniteStateAutomata.debug) System.out.println(getName()+"> new turn");
   }

}
