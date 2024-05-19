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

public class ActivityTokenLinkedList {

   private ActivityTokenNode firstActivityToken=null, lastActivityToken=null;

   /** retourne vrai si la liste ne contient aucun noeud */
   public boolean isEmpty(){ return firstActivityToken == null; }

   /** rajoute la liste sll a la fin. la liste sll est "resetée" */
   public void append(ActivityTokenLinkedList sll){
      //System.out.println("append " + this + " with " + sll);
      if (sll.isEmpty()) return;
      if (isEmpty()){
         firstActivityToken = sll.firstActivityToken;
         lastActivityToken = sll.lastActivityToken;
      } else {
         //System.out.println("    a) " + this);
         lastActivityToken.addAfter(sll.firstActivityToken);
         //System.out.println("    b) " + this);
         lastActivityToken = sll.lastActivityToken;
         //System.out.println("    c) " + this);
      }
      sll.reset();
   }

   /** libere tous les noeuds de la liste, les renvoie dans la pool */
   public void clear(){
      ActivityTokenNode current = null;
      while((current = getAndRemoveFirstActivityTokenNode()) != null)
         current.release();
      reset();
   }
   
   /** 
    * remet a null la liste, ne s'occupe pas de liberer les noeuds, 
    * le GC le fera. C'est normal, il se peut que les noeuds soient 
    * utilisés ailleurs par une autre liste - c'est le cas lorsque 
    * l'on a mergé deux listes 
    */
   final public void reset(){
      firstActivityToken = null;
      lastActivityToken = null;
   }
   
   // METHODES "STATES"

   /** rajoute un etat au debut de la liste */
   public void addActivityToken(ActivityToken s){
      addActivityTokenNode(ActivityTokenNode.newActivityTokenNode(s));
   }
   
   /** rajoute un etat à la fin de la liste */
   public void appendActivityToken(ActivityToken s){
      appendActivityTokenNode(ActivityTokenNode.newActivityTokenNode(s));
   }

   /** retourne vrai si la liste contient l'etat s */
   public boolean contains(ActivityToken s){
      ActivityTokenNode current = firstActivityToken;
      while (current!=null){
         if (current.getActivityToken() == s){
            return true;
         }
         current = current.getNextActivityTokenNode();
      }
      return false;
   }

   ActivityTokenNode getActivityTokenNode(ActivityToken s){
      ActivityTokenNode current = firstActivityToken;
      while (current!=null){
         if (current.getActivityToken() == s){
            return current;
         }
         current = current.getNextActivityTokenNode();
      }
      return null;
   }

   public ActivityTokenLinkedListEnumeration elements(){
      return new ActivityTokenLinkedListEnumeration(this);
   }

   // METHODES "NODES"

    /** accesseur */
    ActivityTokenNode getFirstActivityTokenNode(){ return firstActivityToken; }
    /** accesseur */
    ActivityToken getFirstActivityToken(){return getFirstActivityTokenNode().getActivityToken();}

    /** accesseur */
    ActivityTokenNode getLastActivityTokenNode() { return lastActivityToken; }
    /** accesseur */
    ActivityToken getLastActivityToken(){return getLastActivityTokenNode().getActivityToken();}
   
   /** prend et ote le premier noeud de la liste */
   ActivityTokenNode getAndRemoveFirstActivityTokenNode(){
      if (firstActivityToken == null) return null;
      ActivityTokenNode an = firstActivityToken;
      firstActivityToken = firstActivityToken.getNextActivityTokenNode();
      an.remove();

      return an;
   }

   /** prend et ote le premier noeud de la liste */
   ActivityToken getAndRemoveFirstActivityToken(){
      if (firstActivityToken == null) return null;
      ActivityTokenNode an = firstActivityToken;
      firstActivityToken = firstActivityToken.getNextActivityTokenNode();
      an.remove();
      ActivityToken at = an.getActivityToken();
      an.releaseActivityTokenNode(an);
      return at;
   }

   /** ote le noeud de la liste */
    void remove(ActivityTokenNode an){
      if (an == firstActivityToken) firstActivityToken = an.getNextActivityTokenNode();
      if (an == lastActivityToken) lastActivityToken = an.getPrevActivityTokenNode();

      an.remove();
   }

   /** rajoute un noeud en debut de liste */
    void addActivityTokenNode(ActivityTokenNode an){
      if (firstActivityToken == null)
         {
            firstActivityToken = an;
            lastActivityToken = an;
         }
      else
         {
            firstActivityToken.addBefore(an);
            firstActivityToken = an;
         }
   }
   
   /** rajoute un noeud en fin de liste */
    void appendActivityTokenNode(ActivityTokenNode an){
      if (firstActivityToken == null)
         {
            firstActivityToken = an;
            lastActivityToken = an;
         }
      else
         {
            lastActivityToken.insertAfter(an);
            lastActivityToken = an;
         }
   }

   /* JAMAIS TESTE AVEC LES CHAINES DE CARAC 
      void addSorted(ActivityTokenNode an)
      {
      // recherche du lieu d'insertion
      if (firstActivityToken == null)
      {
      firstActivityToken = an;
      lastActivityToken = an;
      }
      else
      {
      // ajout a la fin
      // optimisation car cela rique d'etre souvent le cas
      if (an.getActivityToken().getName().compareTo(lastActivityToken.getName())>0)
      {
      lastActivityToken.addAfter(an);
      lastActivityToken = an;
      }
      // ajout au debut
      else if (!an.getActivityToken().getName().compareTo(lastActivityToken.getName())>0)
      {
      firstActivityToken.addBefore(an);
      firstActivityToken = an;
      }
      else                     
      {
      // recherche du lieu d'insertion
      // pas besoin de tester "null" car on ne peut jamais arriver a la fin
      // grace au test precedent
      ActivityTokenNode current = firstActivityToken;
      while(an.getActivityToken().getName().compareTo(lastActivityToken.getName())>0)
      current = current.getNextActivityTokenNode();
      current.addBefore(an);
      }
      }
      }
   */
   //

   public String toString(){
      String r = "";
      ActivityTokenNode an = firstActivityToken;
      while(an!=null){
         r+="->"+an;
         an = an.getNextActivityTokenNode();
      }
      return r;
   }

}
