/***********************************************************************
This software module was originally developed by C�dric Thi�not (Expway)
Claude Seyrat (Expway) and Gr�goire Pau (Expway) in the course of 
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

Copyright Expway � 2001.
************************************************************************/

package com.expway.tools.automata;

import java.util.*;
import java.io.*;

 public class FiniteStateAutomata implements ApplicationContextAware {

    static final public boolean debug = false;

    /** un objet "application" qui sera pass� � tout le monde*/
    Object applicationContext = null;
    public void setApplicationContext(Object o){applicationContext = o;}
    public Object getApplicationContext(){return applicationContext;}

    /** tous les �tats */
    StateLinkedList states = new StateLinkedList();

    public FiniteStateAutomata(){}
    
    /** creation d'un nouvel automate a partir d'un autre 
     *  l'ancien automate est vampiris� et ne contiendra plus d'�tats */
    public FiniteStateAutomata(FiniteStateAutomata fsa){
        merge(fsa);
    }

    /** Ajoute l'�tat � l'automate si necessaire */
    public void addState(State t) {
        if (!states.contains(t))
            states.addState(t);
    }

    /** merge avec l'automate pass� en parametre, ce dernier est laiss�
     * exsangue car on lui pompe tous ces etats */
    public void merge(FiniteStateAutomata fsa){
        states.append(fsa.states);
        fsa.reset();
    }

    /** merge les deux etats 
     *  - le nouvel etat possede les caracteristiques des deux anciens : final et initial
     *  - les transitions partant et arrivant sur les deux etats sont conserv�es
     *
     *  Attention car cette operation peut creer des doublons, lorsqu'un troisieme etat
     *  dispose de deux transitions identiques, l'une vers s1 et l'autre vers s2
     */
    public void merge(State s1, State s2){
        // securite : les deux etats doivent faire partie de l'automate
        if (!(states.contains(s1) && states.contains(s2))) return;

        // l'etat 's1' va servir a regouper les deux �tats
        // Le nouvel etat garde les caract�ristiques 's2' (final & initial)
        if (s2.isFinalState()) s1.setFinalState(true);
        if (s2.isStartState()) s1.setStartState(true);

        // toutes les transitions qui partent de s2 vont dor�navant
        // partir de s1 - attention aux doublons que ca peut creer 
        Transition t2 = null;
        while ((t2=s2.removeFirstTransition())!=null){
            t2.setFromState(s1); // on la rajoute a s1
            t2.bind();
        }

        // toutes les transitions qui arrivent sur s2 vont dor�navant
        // arriver sur s1 - attention aux doublons que ca peut creer
        Transition t3 = null;
        for (StateLinkedListEnumeration e = states.elements();e.hasMoreElements();){
            State s = e.nextElement();
            // recupere toutes les transitions de s qui aboutissent sur s2
            TransitionLinkedListEnumeration te = s.getTransitionsTo(s2);
            while (te.hasMoreElements()){
                t3 = te.nextElement();
                t3.setToState(s1);
            }
        }
        states.remove(states.getStateNode(s2));
    }

    /** retourne une StateLinkedListEnumeration de tous les etats contenus 
     * dans l'automate*/
    public StateLinkedListEnumeration states(){
        return states.elements();
    }

    /**
     * retourne le premier etat initial de l'automate
     * pratique pour les automates dont on sait par construction 
     * qu'ils n'ont qu'un seul etat initial
     */
    public State getFirstStartState(){
        for (StateLinkedListEnumeration e=states.elements();e.hasMoreElements();){
            State s = e.nextElement();
            if (s.isStartState())
                return s;
        }
        return null;
    }

    /** retourne une liste contenant les etats initiaux de l'automate */
    public StateLinkedList getStartStates(){
        StateLinkedList result = new StateLinkedList();
        for (StateLinkedListEnumeration e=states.elements();e.hasMoreElements();){
            State s = e.nextElement();
            if (s.isStartState()) result.addState(s);
        }
        return result;
    }

    /**
     * retourne le premier etat final de l'automate
     * pratique pour les automates dont on sait par construction 
     * qu'ils n'ont qu'un seul etat final
     */
    public State getFirstFinalState(){
        for (StateLinkedListEnumeration e=states.elements();e.hasMoreElements();){
            State s = e.nextElement();
            if (s.isFinalState()) return s;
        }
        return null;
    }

    /** retourne une liste contenant les etats finaux de l'automate */
    public StateLinkedList getFinalStates(){
        StateLinkedList result = new StateLinkedList();
        for (StateLinkedListEnumeration e=states.elements();e.hasMoreElements();){
            State s = e.nextElement();
            if (s.isFinalState())
                result.addState(s);
        }
        return result;
    }
   
    // ----------------------------------------------------------------------------------

    /** est appel� lorsque l'automate est reinitialis� */
     public void reset(){}

	
    
    public String toString(){
        //if (FiniteStateAutomata.debug) System.out.println("tostring fsa");
        String res = "";
        for (StateLinkedListEnumeration e=states.elements();e.hasMoreElements();){
            res += "\n";
            State s = e.nextElement();
            res += s;
        }
        return res;
    }

    // ------------------------------------------------------------

}
