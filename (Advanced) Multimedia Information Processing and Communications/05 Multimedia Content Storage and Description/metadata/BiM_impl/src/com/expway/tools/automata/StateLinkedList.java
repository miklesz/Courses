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

public class StateLinkedList {

    private StateNode firstState=null, lastState=null;

   /** retourne vrai si la liste ne contient aucun noeud */
   public boolean isEmpty(){ return firstState == null; }

   /** rajoute la liste sll a la fin. la liste sll est "resetée" */
   public void append(StateLinkedList sll){
      //System.out.println("append " + this + " with " + sll);
      if (sll.isEmpty()) return;
      if (isEmpty()){
         firstState = sll.firstState;
         lastState = sll.lastState;
      } else {
         //System.out.println("    a) " + this);
         lastState.addAfter(sll.firstState);
         //System.out.println("    b) " + this);
         lastState = sll.lastState;
         //System.out.println("    c) " + this);
      }
      sll.reset();
   }

   /** libere tous les noeuds de la liste, les renvoie dans la pool */
   public void clear(){
      StateNode current = null;
      while((current = getAndRemoveFirstStateNode()) != null)
         current.release();
      reset();
   }
   
   /** 
    * remet a null la liste, ne s'occupe pas de liberer les noeuds, 
    * le GC le fera. C'est normal, il se peut que les noeuds soient 
    * utilisés ailleurs par une autre liste - c'est le cas lorsque 
    * l'on a mergé deux listes avec append 
    */
   public void reset(){
      firstState = null;
      lastState = null;
   }
   
   // METHODES "STATES"

   /** rajoute un etat au debut de la liste */
   public void addState(State s){
      addStateNode(StateNode.newStateNode(s));
   }
   
   /** rajoute un etat à la fin de la liste */
   public void appendState(State s){
      appendStateNode(StateNode.newStateNode(s));
   }

   /** retourne vrai si la liste contient l'etat s */
   public boolean contains(State s){
      StateNode current = firstState;
      while (current!=null){
         if (current.getState() == s){
            return true;
         }
         current = current.getNextStateNode();
      }
      return false;
   }

   StateNode getStateNode(State s){
      StateNode current = firstState;
      while (current!=null){
         if (current.getState() == s){
            return current;
         }
         current = current.getNextStateNode();
      }
      return null;
   }

   public StateLinkedListEnumeration elements(){
      return new StateLinkedListEnumeration(this);
   }

   // METHODES "STATENODES"

   StateNode getFirstStateNode(){ return firstState; }
   StateNode getLastStateNode() { return lastState; }
   
   /** prend et ote le premier noeud de la liste */
   StateNode getAndRemoveFirstStateNode(){
      if (firstState == null) return null;
      StateNode an = firstState;
      firstState = firstState.getNextStateNode();
      an.remove();
      return an;
   }

   /** ote le noeud de la liste */
    void remove(StateNode an){
      if (an == firstState) firstState = an.getNextStateNode();
      if (an == lastState) lastState = an.getPrevStateNode();
      an.remove();
   }

   /** rajoute un noeud en debut de liste */
    void addStateNode(StateNode an){
      if (firstState == null)
         {
            firstState = an;
            lastState = an;
         }
      else
         {
            firstState.addBefore(an);
            firstState = an;
         }
   }
   
   /** rajoute un noeud en fin de liste */
    void appendStateNode(StateNode an){
      if (firstState == null)
         {
            firstState = an;
            lastState = an;
         }
      else
         {
            lastState.insertAfter(an);
            lastState = an;
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
      StateNode an = firstState;
      while(an!=null){
         r+="->"+an;
         an = an.getNextStateNode();
      }
      return r;
   }

}
