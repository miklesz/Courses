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

abstract public class Transition {

   private State from = null, to = null;

   // CONSTRUCTEURS

    /**
     * Constructeurs
     * 
     **/

    public Transition(){
        from = null;to = null;
    }
    
   public Transition(State fromS, State toS){
      setFromState(fromS);
      setToState(toS);
   }

   /** 
    *  met a jour l'etat de départ (from) de la transition
    *   - la transition doit etre "unbindee"
    *   - pour la rajouter au nouvel état il faut appeler la methode "bind"
    */
   public void setFromState(State fs) {
      if (from != null) throw new RuntimeException("Transition is bound : setFromState is impossible");
      from = fs;
   }

   /** pour dissocier la transition de son etat from */
   public void unbind(){
      if (from!=null)
         from.removeTransition(this);
      from = null;
   }

   /** pour dissocier la transition de son etat from et virer son etat to */
   public void fullUnbind(){
       unbind();
       to = null;
   }

   /** pour dissocier la transition de son etat from, 
    * ne s'occupe pas de l'oter de l'etat from
    * met juste le pointeur a 'null' 
    * @see State
    */
   void brutalUnbind(){
      from = null;
   }

   /** pour associer la transition à l'état from */
   public void bind(){
      if (from!=null)
         from.addTransition(this);
   }
   
   /** 
    * met a jour l'etat d'arrivée de la transition
    */
   public void setToState(State ts)   {
      to = ts;
   }

   /**
    * met a jour les etats de depart et de fin de la transition
    */
   public void setStates(State from, State to){
      setFromState(from);
      setToState(to);
   }
   

   /** retourne l'état de départ de la transition */
   public State getFromState() {return from;}
   /** retourne l'état d'arrivé de la transition */
   public State getToState()   {return to;}


   /** Franchissement de la transition
    *
    *     - on passe le token et l'activité en cours à l'état destinataire
    *     - inputtoken = null lors du franchissement si c'est une epsilon transition
    * 
    *  @return l'etat nouvellement activé ou 
    *  @return null si cet etat etait deja active
    */
    State crossInternal(Object inputToken, ActivityToken at,Object applicationContext) throws TransitionException, AutomataException {

        // verification de bonne aloi est ce une epsilon
        if (isEpsilon()) 
            cross(at,applicationContext);
        
        // si ce cross n'est pas du au franchissement "epsilon"
        else if (inputToken!=null) {
            cross(at,applicationContext);
        }
        
        else // si elle n'est pas epsilon et qu'on essaie de lui passer un epsilon
            throw new TransitionException(this,"Transition can not be crossed");
        
        // enfin on met à jour le nouvel état et on retourne ce qu'il faut
        if (!to.internalActivate(at,applicationContext)) // peut emettre une exception pour rejeter le jeton
            return to;            // retour l'etat nouvellement actif
        else
            return null;          // l'etat etait deja actif - on le retourne pas
    }

    /** par défaut deux transitions sont egales si elles lient les memes etats
     * cette methode est appelee lorsque l'on recherche des transitions similaires par exemple
     * deux transitions */
    public boolean equals(Object o){
        if (o instanceof Transition){
            Transition t = (Transition)o;
            return (t.getToState() == to && t.getFromState() == from);
        }
      return false;
   }

    // ------------------------------------------------------------
    // AFFICHAGE
    
    public String toString(){
        String ret = "";
        if (from !=null)
            ret += from.getName();
        else
            ret += "...";

        ret += " -> ";

        if (to !=null)
            ret += to.getName();
        else
            ret += "...";

        return ret;
    }

    public String getLabel(){
        if (isEpsilon()) return "eps";
        else return "???";
    }
   
    // ------------------------------------------------------------
    // CALLBACKS

    /** doit renvoyer true si la transition est une epsilon transition 
        une epsilon transition est une transition qui ne consomme pas un evenement */
    abstract public boolean isEpsilon();

    /** appelé pour savoir si un evenement est accepte par la transition
     * le token ne doit pas être modifié par la méthode @deprecated
     */
    abstract public boolean accept(Object evenement);

    /** appelé pour savoir si la conjonction événement et activité est 
     * susceptible d'être acceptée par la transition le token ne doit
     * pas être modifié par la méthode 
     */
    abstract public boolean accept(ActivityToken at,Object evenement);

    /** appelé pour savoir si la conjonction événement et activité est 
     * susceptible d'être acceptée par la transition le token ne doit
     * pas être modifié par la méthode 
     */
    abstract public boolean accept(ActivityToken at,Object evenement, Object applicationContext);

    /** appelé pour savoir si l'activité est accepté par la transition lorsque 
     * celle ci est une epsilon transition
     */
    abstract public boolean epsilonAccept(ActivityToken at,Object applicationContext);

    /** pour initialiser la transition */
    abstract public void reset(Object applicationContext);
 
   /** callback appelé lorsque la transition est franchie 
     * la méthode peut refuser de laisser passer l'activité en lancant une exception 
     * TODO: vérifier que la transition refuse bien le passage du token */
    abstract public void cross(ActivityToken at,Object applicationContext) throws RejectionException;
    
    /** pour un affichage plus sympa */
    abstract public String screenString();
}
