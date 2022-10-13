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
import com.expway.util.URIRegistry;
import com.expway.tools.expression.SimpleTypeDefinition;
import java.util.*;
import java.io.IOException;
import java.io.FileWriter;

/** 
 *  Les compressions transitions n'acceptent qu'un seul événement. 
 *      Pour améliorer la performance on pourrait leur faire accepter plusieurs événements. 
 *      Ca pourrait couper court a certaines duplications inutiles de jetons... (jamais testé)  
 */

public class CompressionTransition extends Transition  {

    /** un peu vieux @deprecated */
    static final public String LAST = "LAST";
    String label = null;

    // UNUSED
    // Collection theAcceptedTokens = null;
    private String oneAcceptedTokenQName = null;
    // CLAUDE: domaine non préfixé
    private String oneAcceptedTokenRawName = null;

    int mode = 0;

    // ------------------------------------------------------------


    public CompressionTransition(String accept){
        super();
        setAccept(accept);
    }
    
    public CompressionTransition(String accept, State fromS, State toS){
        super(fromS,toS);
        setAccept(accept);
    }

    public void setAccept(String s){
        oneAcceptedTokenQName = s;
        if (URIRegistry.hasPrefix(oneAcceptedTokenQName))
            oneAcceptedTokenRawName = URIRegistry.getWithoutPrefix(oneAcceptedTokenQName);
        else if (oneAcceptedTokenQName!=null && !oneAcceptedTokenQName.equals(SimpleTypeDefinition.MIXED_KEY) ){
            System.err.println("--------> " + s);
            Thread.dumpStack();           
        }    
    }
    
    public void setMode(int a){
        mode = a;
    }

    public String getAcceptedTokenQName(){
        return oneAcceptedTokenQName;
    }
    public String getAcceptedTokenRawName(){
        return oneAcceptedTokenRawName;
    }

    // ------------------------------------------------------------

    /** mets a jour l'ensemble des tokens acceptés 
        UNUSED : 
        1- prévu pour améliorer la compression en remontant au plus tot
           l'ensemble des evenements qu'il est possible d'accepter 
        2- servait aussi pour les labels
    
      public void setAcceptedTokens(Collection c){
      if (c==null) theAcceptedTokens = null;
      else {
      Iterator i = c.iterator();
      if (i.hasNext()){
      label = "*" + (String) i.next();
      
      while(i.hasNext())
      label += ", " + (String) i.next();
      
      label += "*" ;
      }
      
      if (theAcceptedTokens==null)
      theAcceptedTokens = new LinkedList();
      
      theAcceptedTokens.addAll(c);
      oneAcceptedTokenQName = null;
      oneAcceptedTokenRawName = null;
      }
      }*/
    
    /** cette transition est elle une epsilon transition 
        tentative : UNE Compression transition n'est jamais epsilon, dans le pire des cas elle accepte tout
        evite de dupliquer les jetons */
    public boolean isEpsilon(){
        //return false;
        if (oneAcceptedTokenQName == null) return true;
        return false;
    }

    public boolean epsilonAccept(ActivityToken at,Object applicationContext){
        return true;
    }

    public boolean accept(ActivityToken theActivity,Object theEventToken, Object theApplicationContext)  {
        //System.out.println("ACCEPT00 of " + this);
        //((CompressionActivityToken)theActivity).log(this + " (ACCEPT00)");
        return accept(theActivity,theEventToken);
    }

    /** 
     * appelé pour savoir si la conjonction événement et activité est 
     * susceptible d'être acceptée par la transition le token ne doit
     * pas être modifié par la méthode 
     */
    public boolean accept(ActivityToken at,Object ot){
        //System.out.println("ACCEPT01 of " + this);
        return accept(ot);
    }

    /** 
     * Appelé pour savoir si un evenement est accepté par la transition
     * le token ne doit pas être modifié par la méthode @deprecated
     */
    // CLAUDE : Modifié pour tenir compte des unqualified element dont on ne peut connaitre le
    //          namespace qu'en les validant
    public boolean accept(Object ot){
        //System.out.println("ACCEPT02 of " + this + " acceptedQName="+oneAcceptedTokenQName);
        //Thread.dumpStack();
        if (ControlledFiniteStateAutomata.debug) System.out.println(this + " accept");
        if (oneAcceptedTokenQName == null) return true;

        // CLAUDE : Modifié pour tenir compte des unqualified element dont on ne peut connaitre le
        //          namespace qu'en les validant
        //          Anciennement (1 ligne):
        //                   if (oneAcceptedTokenQName.equals(ot)) return true;
        
        // si l'événement (le nom de l'élément ou de l'attribut) a un préfixe on teste
        // dans l'espace des noms préfixés
        if (URIRegistry.hasPrefix((String)ot))  return oneAcceptedTokenQName.equals(ot);

        // sinon on teste dans l'espace des noms sans préfixes
        return oneAcceptedTokenRawName.equals(ot);
    }

    /** callback appelé lorsque la transition est franchie 
     * la méthode peut refuser de laisser passer l'activité en lancant une exception 
     */
    public void cross(ActivityToken at,Object applicationContext) throws RejectionException {
        if (ControlledFiniteStateAutomata.debug) System.out.println(this+" cross requested with " + at);
    }
    
    /** pour initialiser la transition */
    public void reset(Object applicationContext) {
        if (ControlledFiniteStateAutomata.debug) System.out.println(this + " reset");
    }

    /** pour un affichage plus sympa */
    public String screenString() {return "CompressionTransition";}

    //

    public String getLabel(){
        if (label != null)
            return label;
        
        if (oneAcceptedTokenQName!=null)
            return oneAcceptedTokenQName;

        return super.getLabel();
    }

}

