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

import com.expway.tools.automata.ActivityToken;

// TODO UNIFIER AVEC COMPRESSION ACTIVITY TOKEN

public class DecompressionActivityToken implements ActivityToken {

    static final public boolean DEBUG = false;

    static int cptid = 0;
    int id = cptid++;

    int              theNextKey  = 0;
    CptNodeStack     theCptStack = null;

    public DecompressionActivityToken(){}

    // ------------------------------------------------------------
    // GESTION DES COMPTEURS EMPILES

    /** appelle la longueur de l'occurrence courante */
    public int getCpt(){return theCptStack.lastValue();}
    
    /** increment le cpt courant */
    public void decrCpt(){ theCptStack.decrLast();}
    
    /** ajoute un compteur */
    public void addCpt(int occurs){
        // une pile de compteur est necessaire
        if (theCptStack == null) theCptStack = new CptNodeStack();
        // rajoute un compteur dans la pile des compteurs
        theCptStack.push(new CptNode(occurs));
    }
    
    /** ote un compteur */
    public void remCpt(){
        CptNode cn = theCptStack.pop();
    }

    // ------------------------------------------------------------
    
    /** appelé lorsque deux jetons se rejoignent TODO */
    public void mergeIt(ActivityToken at){}

    /** appelé lors de l'exécution d'un automate non déterministe
     * lors de plusieurs transitions avec le même arc */
    public ActivityToken cloneIt(){ return null; }

    /** appelé lorsque le token disparait parce qu'il ne trouve plus d'arc ou aller */
    public void destroy(){}

    /** Affichage */
    public String toString(){
        return "DAT n"+id + " " + theCptStack;
    }

}
