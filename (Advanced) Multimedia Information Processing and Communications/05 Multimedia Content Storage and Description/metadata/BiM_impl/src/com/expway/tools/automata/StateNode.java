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

public class StateNode {
   
   // une tentative de pool de StateNode
   
   static private int STATENODEPOOL_SIZE = 70;
   static private StateNode[] stateNodePool = new StateNode[STATENODEPOOL_SIZE];
   static {
      for (int i=0;i<stateNodePool.length;i++)
         stateNodePool[i]=new StateNode();
   }

    // Greg (pour libérer de la place mémoire)
    public static void freeStatic() {
        for (int i=0;i<stateNodePool.length;i++)
            stateNodePool[i]=new StateNode();
    } 
   
   static public StateNode newStateNode(State s){
      for (int i=0;i<stateNodePool.length;i++)
         if(stateNodePool[i]!=null){
            StateNode sn = stateNodePool[i];
            stateNodePool[i] = null;
            sn.setState(s);
            return sn;
         }
      //System.out.println("plus personne dans la pool");
      return new StateNode(s);
   }

   static public void releaseStateNode(StateNode sn){
      for (int i=0;i<stateNodePool.length;i++)
         if(stateNodePool[i] == null){
            stateNodePool[i] = sn;
            return;
         }
      //si il y a plus de place, tant pis
      //System.out.println("plus de place dans la pool de state node : delete ou GC");
   }
   
   public void finalize(){
       //System.out.println("in finalize ; release me ; statenode");
       release();
   }
    
    public void release(){
        //System.out.println("release me");
        next = null;
        prev = null;
        theState = null;
        releaseStateNode(this);
    }
    
    
    // 
    
    // linked list
    private StateNode next = null;
    private StateNode prev = null;
    
   // l'action en question
   private State theState;

   // on est oblige de passer par la methode statique du pool
   private StateNode(State a){
      theState = a;
   }

   private StateNode(){
      theState = null;
   }

   // Accesseurs
   void   setState(State a){theState = a;}
   State getState(){return theState;}

   StateNode getNextStateNode(){return next;}
   StateNode getPrevStateNode(){return prev;}

   // basic linked list commands
   void insertAfter(StateNode a){
      // si je ne suis pas le dernier maillon
      if (this.next !=null)  this.next.prev = a;
      // les liens de la nouvelle action
      a.prev = this;
      a.next = this.next;
      //mis a jour de moi
      this.next = a;
   }

   void addAfter(StateNode a){
      // si je ne suis pas le dernier maillon
      if (this.next !=null)  this.next.prev = a;
      // les liens de la nouvelle action
      a.prev = this;
      //mis a jour de moi
      this.next = a;
   }

   void addBefore(StateNode a){
      // si je ne suis pas le premier maillon
      if (this.prev!=null) this.prev.next = a;
      // les liens de la nouvelle action
      a.prev = this.prev;
      a.next = this;
      //mis a jour de moi
      this.prev = a;
   }

   void switchWithNext(){
      if (next !=null){
         if (prev!=null) prev.next = next;
         next.prev = prev;
         if (next.next !=null) next.next.prev = this;
         StateNode temp = next.next;
         next.next = this;
         prev = next;
         next = temp;
      }
   }

   void switchWithPrev(){
      if (prev != null){
         if (next!=null) next.prev = prev;
         prev.next = next;
         if (prev.prev != null) prev.prev.next = this;
         StateNode temp = prev.prev;
         prev.prev = this;
         next = prev;
         prev = temp;
      }
   }

   void switchWith(StateNode an){
      if (prev != null)   prev.next = an;
      if (an.prev !=null) an.prev.next = this;
      if (next !=null)    next.prev = an;
      if (an.next !=null) an.next.prev = this;

      StateNode temp;

      temp= next;
      next = an.next;
      an.next = temp;
      temp = prev;
      prev = an.prev;
      an.prev = temp;
         
   }
   
   void remove(){
      if (prev !=null) prev.next = next;
      if (next !=null) next.prev = prev;
      next=null;
      prev=null;
   }

   public String toString(){
      if (theState == null)
         return "null";
      return theState.getName();
   }
   

}
