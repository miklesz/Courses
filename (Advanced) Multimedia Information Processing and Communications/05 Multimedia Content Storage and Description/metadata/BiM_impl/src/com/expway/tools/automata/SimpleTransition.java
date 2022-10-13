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


package com.expway.tools.automata;

import java.util.*;


 public class SimpleTransition extends Transition {

     private Object acceptedToken = null;
     
    /**
     * Constructeurs
     * 
     * L'objet accept est le token accepte par la transition
     * Sa methode "compare" sera utilisee avec celle de l'evenement entrant
     *
     **/
     public SimpleTransition(Object accept){
         super(); 
         init(accept);
     }
     
     public SimpleTransition(Object accept, State fromS, State toS){
         super(fromS,toS);
         init(accept);
     }
     
     private void init(Object accept){
         acceptedToken = accept;
     }
     
     /** retourne le token accepté par la transition */
     public boolean isEpsilon(){ return acceptedToken==null;}
     
     /** retourne le token accepté par la transition */
     public Object getAcceptedToken(){ return acceptedToken;}
     
     /** callback appelé lorsque pour demander à la transition si elle accepte d'être franchie */
     public boolean accept(Object theEventToken)  {
         return theEventToken.equals(acceptedToken);
     }

     /** callback appelé lorsque pour demander à la transition si elle accepte d'être franchie @deprecated*/
     public boolean accept(ActivityToken theActivity,Object theEventToken)  {
         return accept(theEventToken);
     }

     /** callback appelé lorsque pour demander à la transition si elle accepte d'être franchie */
     public boolean accept(ActivityToken theActivity,Object theEventToken, Object theApplicationContext)  {
         return accept(theActivity,theEventToken);
     }
    
     public boolean epsilonAccept(ActivityToken at,Object applicationContext){
         return true;
     }

    /** callback appelé lorsque la transition est franchie 
     * la méthode peut refuser de laisser passer l'activité en lancant une exception 
     * TODO: vérifier que la transition refuse bien le passage du token */
      public void cross(ActivityToken at,Object applicationContext) throws RejectionException {
          if (FiniteStateAutomata.debug) System.out.println(this+" cross requested with " + at);
      }
    /** pour initialiser la transition */
      public void reset(Object applicationContext) {
          if (FiniteStateAutomata.debug) System.out.println(this + " reset");
      }

    /** pour un affichage plus sympa */
      public String screenString() {return "SimpleTransition";}

     
    public String toString(){
        String ret = "";
        State from=getFromState(),to=getToState();
        if (from!=null)
            ret += from.getName() + to.getName();
        else
            ret += "...";
        
        if (acceptedToken == null)
            ret += " - epsilon -> " ;
        else
            ret += " - " + acceptedToken + " -> " ;
        
        if (to !=null)
            ret += to.getName();
        else
            ret += "...";

        return ret;
    }
     
     public String getLabel(){
         if (isEpsilon()) return "£";
         else return ""+getAcceptedToken();
     }

}

