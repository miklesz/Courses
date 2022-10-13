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
import java.io.*;

public class ControlledFiniteStateAutomata extends FiniteStateAutomata implements FiniteStateAutomataInterface {

    static final public boolean COMPUTESTATS = false;
    static final public boolean PRINTSTATS = false;

    /** pour les statistiques internes à l'automate */
    public int numberOfTurns = 0;
    public double avgNumberOfToken = 0.0;
    public long maxToken;
    public long minToken;

    /** statistiques */
    long   currentNumberOfToken = 0;
    int    currentNumberOfActiveStates = 0;
    long   currentMaxToken = 0;
    long   currentMinToken = 0;
    int    currentNumOfToken = 0;
    double currentAvgNumberOfToken = 0.0;
    double avgtok = -666;

    private void resetStatistics(){
        numberOfTurns=0;
        avgNumberOfToken = 0.0;
    }

    private void computeFinalStatistics(){
        avgtok = -666;
        
        if (numberOfTurns != 0)
            avgtok= avgNumberOfToken / numberOfTurns;
        
         if (PRINTSTATS)
             printGlobalStats();
    }

    private void computeStatistics(){
      currentNumberOfToken = 0;
      currentNumberOfActiveStates = 0;
      currentMaxToken = 0;
      currentMinToken = 0;
      currentNumOfToken = 0;
      currentAvgNumberOfToken = 0.0;
      
      numberOfTurns++;
      
      StateLinkedListEnumeration e = activeStates.elements();
      while (e.hasMoreElements()){
          State s=e.nextElement();
          
          int temp = s.countActivities();
          
          currentNumberOfActiveStates++;
          currentNumberOfToken += temp;
          
          currentMaxToken=(currentMaxToken>currentNumOfToken?currentMaxToken:currentNumOfToken);
          currentMinToken=(currentMinToken<currentNumOfToken?currentMinToken:currentNumOfToken);
      }
      
      if (currentNumberOfActiveStates != 0)
          currentAvgNumberOfToken = currentNumberOfToken / currentNumberOfActiveStates;
      else
          currentAvgNumberOfToken = -666;
      
      avgNumberOfToken += currentAvgNumberOfToken;
      maxToken=(maxToken>currentMaxToken?maxToken:currentMaxToken);
      minToken=(minToken<currentMinToken?minToken:currentMinToken);
      
      if (PRINTSTATS)
          printTurnStats();
      
    }
    
    public void printGlobalStats(){
        if (COMPUTESTATS){
             System.out.println("");
             System.out.println("GLOBAL STATISTICS ");
             System.out.println("    Avg Number Of Token             = " + (avgtok==-666?"None":""+avgtok));
             System.out.println("");
             System.out.println("    Max Number of Token             = " + maxToken);
             System.out.println("    Min Number of Token             = " + minToken);
        }
    }

    public void printTurnStats(){
        if (COMPUTESTATS){
            System.out.println("");
            System.out.println("STATISTICS OF TURN " + numberOfTurns + " :" );
            System.out.println("    Current Number Of Token         = " + currentNumberOfToken);
            System.out.println("    Current Number Of Active States = " + currentNumberOfActiveStates);
            System.out.println("    NumOfToken / ActiveStates       = " + (currentAvgNumberOfToken==-666?"None":""+currentAvgNumberOfToken));
            System.out.println("");
            System.out.println("    Max Number of Token             = " + currentMaxToken);
            System.out.println("    Min Number of Token             = " + currentMinToken);
        }
    }


    /** les états actifs */
    StateLinkedList activeStates = new StateLinkedList();
    private StateLinkedList newStates = new StateLinkedList();
    private StateLinkedList epsilonActivatedStates = new StateLinkedList();

    /** reinitialise l'automate */
    public void reset() {

        if (ControlledFiniteStateAutomata.COMPUTESTATS){
            resetStatistics();
        }

        if (ControlledFiniteStateAutomata.debug) System.out.println("reset");
        
        activeStates.clear();
        newStates.clear();
        epsilonActivatedStates.clear();

        for (StateLinkedListEnumeration e=states.elements();e.hasMoreElements();){
            // on reset les états qui eux mêmes reset les transitions
            e.nextElement().internalReset(applicationContext);
        }

        super.reset();
    }

    /** reinitialise l'automate et place le token activite sur l'etat de départ */
    public void reset(ActivityToken at) throws AutomataException {

        if (ControlledFiniteStateAutomata.debug) System.out.println("START RESET with " + at);

        reset();
    
        // on efface la liste des nouveaux etats
        newStates.clear();

        // pour distinguer le premier etat de depart des autres
        boolean first = true; 
        // on rechercher les etats initiaux
        for (StateLinkedListEnumeration e=states.elements();e.hasMoreElements();){
            State s = e.nextElement();
            // si c'est un etat de départ on lui donne un jeton
            if (s.isStartState()){

                if (first)      // on donne le jeton original au premier
                    {
                        s.internalActivate(at,applicationContext);
                        first = false;
                    }
                else            // une copie du jeton original pour tous les autres
                    s.internalActivate(at.cloneIt(),applicationContext);

                // rajoute l'état dans la liste des états actifs
                newStates.addState(s);
            }
        }
        // update de la liste des etats actifs ; propagation le long des epsilons transitions
        updateActiveStates(newStates);
    
        if (ControlledFiniteStateAutomata.debug) System.out.println("END OF RESET :");
        if (ControlledFiniteStateAutomata.debug) System.out.println(" Active states = " + activeStates);
        if (ControlledFiniteStateAutomata.debug) printActiveStates();
    }
   
    /** un nouveau token arrive. l'automate doit le consommer */
    public void consume(Object o) throws AutomataException {
        if (ControlledFiniteStateAutomata.debug) System.out.println(" NEW TURN");
        if (ControlledFiniteStateAutomata.debug) System.out.println(" Active states = " + activeStates);

        newTurn(applicationContext);

        // on efface la liste des nouveaux etats
        newStates.clear();

        if (ControlledFiniteStateAutomata.debug) System.out.println("    - prepare new turn");

        // on prepare le tour pour tous les états
        //          StateLinkedListEnumeration e = activeStates.elements();     // dorénavant on avertit tout le monde
        StateLinkedListEnumeration e = states.elements();
        while(e.hasMoreElements()){
            e.nextElement().prepareNewTurn(applicationContext);
        }
      
        // tous les etats actifs
        e = activeStates.elements();     // dorénavant on avertit tout le monde        e.restart();

         if (ControlledFiniteStateAutomata.debug) System.out.println("    - consume token");

       // on parcourt la liste de tous les etats actifs
        while (e.hasMoreElements()){
            State s=e.nextElement();
            //if (s.accept(o)){
            // l'etat avale le jeton et retourne la liste des nouveaux etats actifs
            // cette liste est ajoutée a la liste des nouveaux états
            s.internalConsume(newStates,o,applicationContext);
            //}
        }
      
         if (ControlledFiniteStateAutomata.debug) System.out.println("    - epsilons propagations");

        // update de la liste des etats actifs ; propagation le long des epsilons transitions
        updateActiveStates(newStates);
        
        // des statistiques sur le nombre de jetons
        if (ControlledFiniteStateAutomata.COMPUTESTATS){
            computeStatistics();
        }
        
        endTurn(applicationContext);

        // test final pour savoir s'il reste des jetons...
        // s'il n'en reste pas alors probleme
        e = activeStates.elements();
        if (!e.hasMoreElements()){
            throw new AutomataException("Transition impossible");
        }

        if (ControlledFiniteStateAutomata.debug) System.out.println(" TURN END");
        if (ControlledFiniteStateAutomata.debug) System.out.println(" Active states = " + activeStates);

        //          // test pour savoir si un etat final est actif
        //          while(e.hasMoreElements()){
        //              State s = e.nextElement();
        //              if (s.isFinalState()) return true;
        //          }
        // return false;
    }

    void updateActiveStates(StateLinkedList newStates) throws AutomataException {

         if (ControlledFiniteStateAutomata.debug) System.out.println("        - update active states");

        // on efface la liste des nouveaux etats
        activeStates.clear();

        // on parcourt la liste de tous les nouveaux etats pour la closure des epsilons transitions
        //  - on propage tous les jetons le long des transitions epsilon
        //  - les nouveaux etats actifs sont placés en fin de la liste 'newStates'
        //  - la liste 'newStates' est traitee jusqu'à ce qu'il n'y ait plus d'états non vu
        while(!newStates.isEmpty()){
            if (ControlledFiniteStateAutomata.debug) System.out.println("    New States " + newStates);
            StateNode sn = newStates.getAndRemoveFirstStateNode();
            boolean toKeep = sn.getState().internalEpsilonPropagate(newStates,applicationContext);
            if (toKeep)
                activeStates.addStateNode(sn);
        }
        if (ControlledFiniteStateAutomata.debug) System.out.println("        - FIN update active states ");
    }

    /** La fin du flux d'entree - retourne le premier jeton rencontré qui a satisfait l'automata */
    public ActivityToken end() throws FinalStateNotReachedException {
        if (ControlledFiniteStateAutomata.debug) System.out.println("end");
        
        if (ControlledFiniteStateAutomata.COMPUTESTATS){
            computeFinalStatistics();
        }

        ActivityToken at = getFinalActivityToken();
        if (at!=null) return at;
        throw new FinalStateNotReachedException(this,"Final State Not Reached");
    }

    public ActivityToken getFinalActivityToken(){
        StateLinkedListEnumeration e = activeStates.elements();
        while (e.hasMoreElements()){
            State s=e.nextElement();
            if (s.isFinalState()) {
                return s.getFirstActivityToken();
            }
        }
        return null;
    }


    // ----------------------------------------------------------------------------------
    // CALLBACKS
    
    /** appele au debut de chaque tour */
    public void newTurn(Object applicationContext){}

    /** appele a la fin de chaque tour */
    public void endTurn(Object applicationContext){}

    // ------------------------------------------------------------

    public void printStats(){
        int nstates = 0, ntransitions = 0;
        float transitionsPerStateAvg = 0.0f;

        for (StateLinkedListEnumeration e = states();e.hasMoreElements();){
            State fs = (State)e.nextElement();
            nstates ++;
            for (TransitionLinkedListEnumeration te = fs.transitions();te.hasMoreElements();) {
                Transition tr = (Transition)te.nextElement();
                ntransitions ++;
            }
        }

        System.out.println(" NBE ETATS             = " + nstates);
        System.out.println(" NBE TRANSITIONS       = " + ntransitions);
        System.out.println(" NBE TRANSITIONS/ETATS = " + (float)ntransitions/nstates);
    }
     
    public void printActiveStates(){
        int nstates = 0, ntransitions = 0;
        float transitionsPerStateAvg = 0.0f;

        for (StateLinkedListEnumeration e = activeStates.elements();e.hasMoreElements();){
            State fs = (State)e.nextElement();
            System.out.println ("   - " + fs);
        }
    }   
    
    
}





