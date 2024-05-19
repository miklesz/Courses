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

public class TransitionLinkedList {

   private TransitionNode firstTransition=null, lastTransition=null;

   /** retourne vrai si la liste ne contient aucun noeud */
   public boolean isEmpty(){ return firstTransition == null; }

   /** rajoute la liste sll a la fin. la liste sll est "resetée" */
   public void append(TransitionLinkedList sll){
      if (sll.isEmpty()) return;
      if (isEmpty()){
         firstTransition = sll.firstTransition;
         lastTransition = sll.lastTransition;
      } else {
         lastTransition.addAfter(sll.firstTransition);
         lastTransition = sll.lastTransition;
      }
      sll.reset();
   }

   /** libere tous les noeuds de la liste, les renvoie dans la pool */
   public void clear(){
      TransitionNode current = null;
      while((current = getAndRemoveFirstTransitionNode()) != null)
         current.release();
      reset();
   }
   
   /** 
    * remet a null la liste, ne s'occupe pas de liberer les noeuds, 
    * le GC le fera. C'est normal, il se peut que les noeuds soient 
    * utilisés ailleurs par une autre liste - c'est le cas lorsque 
    * l'on a mergé deux listes 
    */
   public void reset(){
      firstTransition = null;
      lastTransition = null;
   }
   
   // METHODES "STATES"

   /** rajoute un transition au debut de la liste */
   public void addTransition(Transition s){
      addTransitionNode(TransitionNode.newTransitionNode(s));
   }

   /** rajoute une transition au debut de la liste */
   public void removeTransition(Transition s){
      TransitionNode tn = getTransitionNode(s);
      if (tn!=null) remove(tn); // petite securite
   }
   
   /** rajoute une transition à la fin de la liste */
   public void appendTransition(Transition s){
      appendTransitionNode(TransitionNode.newTransitionNode(s));
   }

   /** retourne vrai si la liste contient la transition s */
   public boolean contains(Transition s){
      TransitionNode current = firstTransition;
      while (current!=null){
         if (current.getTransition() == s){
            return true;
         }
         current = current.getNextTransitionNode();
      }
      return false;
   }

   TransitionNode getTransitionNode(Transition s){
      TransitionNode current = firstTransition;
      while (current!=null){
         if (current.getTransition() == s){
            return current;
         }
         current = current.getNextTransitionNode();
      }
      return null;
   }

   public TransitionLinkedListEnumeration elements(){
      return new TransitionLinkedListEnumeration(this);
   }

   // METHODES "STATENODES"

   TransitionNode getFirstTransitionNode(){ return firstTransition; }
   TransitionNode getLastTransitionNode() { return lastTransition; }
   
   /** prend et ote le premier noeud de la liste */
   TransitionNode getAndRemoveFirstTransitionNode(){
      if (firstTransition == null) return null;
      TransitionNode an = firstTransition;
      firstTransition = firstTransition.getNextTransitionNode();
      an.remove();
      return an;
   }

   /** ote le noeud de la liste */
    void remove(TransitionNode an){
      if (an == firstTransition) firstTransition = an.getNextTransitionNode();
      if (an == lastTransition) lastTransition = an.getPrevTransitionNode();
      an.remove();
   }

   /** rajoute un noeud en debut de liste */
    void addTransitionNode(TransitionNode an){
      if (firstTransition == null)
         {
            firstTransition = an;
            lastTransition = an;
         }
      else
         {
            firstTransition.addBefore(an);
            firstTransition = an;
         }
   }
   
   /** rajoute un noeud en fin de liste */
    void appendTransitionNode(TransitionNode an){
      if (firstTransition == null)
         {
            firstTransition = an;
            lastTransition = an;
         }
      else
         {
            lastTransition.addAfter(an);
            lastTransition = an;
         }
   }

   /* JAMAIS TESTE AVEC LES CHAINES DE CARAC 
      void addSorted(StateNode an)
      {
      // recherche du lieu d'insertion
      if (firstState == null)
      {
      firstState = an;
      lastState = an;
      }
      else
      {
      // ajout a la fin
      // optimisation car cela rique d'etre souvent le cas
      if (an.getState().getName().compareTo(lastState.getName())>0)
      {
      lastState.addAfter(an);
      lastState = an;
      }
      // ajout au debut
      else if (!an.getState().getName().compareTo(lastState.getName())>0)
      {
      firstState.addBefore(an);
      firstState = an;
      }
      else                     
      {
      // recherche du lieu d'insertion
      // pas besoin de tester "null" car on ne peut jamais arriver a la fin
      // grace au test precedent
      StateNode current = firstState;
      while(an.getState().getName().compareTo(lastState.getName())>0)
      current = current.getNextStateNode();
      current.addBefore(an);
      }
      }
      }
   */
   //

   public String toString(){
      String r = "";
      TransitionNode an = firstTransition;
      while(an!=null){
         r+="->"+an;
         an = an.getNextTransitionNode();
      }
      return r;
   }

}
