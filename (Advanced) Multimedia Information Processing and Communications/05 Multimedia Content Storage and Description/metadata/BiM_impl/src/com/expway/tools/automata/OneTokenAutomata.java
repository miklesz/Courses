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

public class OneTokenAutomata extends FiniteStateAutomata {
    
    static int AutomataSaveId = 0;

    ActivityToken theActivity = null;
    State activeState = null;

    public OneTokenAutomata(){}
    public OneTokenAutomata(FiniteStateAutomata fsa){
        super(fsa);}

    public void reset(){
        theActivity = null;
        activeState = null;
    }

    /** le premier etat initial rencontré est activé */
    public void reset(ActivityToken at) throws AutomataException {
        theActivity = at;
        activeState = getFirstStartState();
    }


    /** a chaque consume le token avance sur la premiere
        transition qui l'accepte */

    public boolean consume() throws AutomataException {

        //System.out.println("     new decoding consume ");
        
        newTurn(applicationContext);
        
        State lastActiveState = activeState;
        TransitionLinkedListEnumeration tlle = activeState.transitions();
        while (tlle.hasMoreElements()){
            activeState = null;
            Transition t = tlle.nextElement();
            //System.out.println("        Request accept of " + t);
            if (t.accept(theActivity,null,applicationContext)){
                try {
                    t.cross(theActivity,applicationContext);
                    activeState = t.getToState();
                    activeState.activate(theActivity,applicationContext);
                    break;
                } catch (RejectionException re){System.out.println("une rejection de derniere minute");}
                //System.out.println("        ...accepted");
            }
            //else
            //System.out.println("        ...rejected");

        }


        if (activeState == null){
     		
            throw new AutomataException("Transition impossible");
        }

        endTurn(applicationContext);

        //System.out.println("     end decoding consume ");

        if (activeState.isFinalState())
            return true;
        return false;


    }

    /** appele au debut de chaque tour */
    public void newTurn(Object applicationContext){}

    /** appele a la fin de chaque tour */
    public void endTurn(Object applicationContext){}

}
