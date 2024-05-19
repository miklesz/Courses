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

// TODO : PASSER EN MODELE DATA / CONTROLLER

abstract public class State {

    /** Un compteur statique pour donner un nom unique aux etats */
    private static int cpt = 0;
   
    /** les transitions qui partent de cet état */
    TransitionLinkedList transitions = new TransitionLinkedList();

    /** Le nom de l'état */
    private String name=null;

    /** est ce un état final ? */
    private boolean finalS = false;
    private boolean startS = false;

    /** cet etat accepte t il plusieurs tokens */
    private boolean acceptMultipleActivities = false;
    
    /** l'activité courante = null si inactif*/
    private ActivityTokenLinkedList activity = new ActivityTokenLinkedList();
    private ActivityTokenLinkedList lastActivity = new ActivityTokenLinkedList();
    
    // CONSTRUCTEURS
    /** 
     * constructeur 
     * @param nom : le nom de l'etat
     * @param finalS : true si c'est un état final 
     */
    public State(String nameS, boolean finalS, boolean startS){
        this(nameS,finalS,startS,false);
    }
    
    public State(String nameS, boolean finalS, boolean startS,boolean acceptMultipleActivities){
        name = nameS;
        this.finalS = finalS;
        this.startS = startS;
        this.acceptMultipleActivities = acceptMultipleActivities;
    }
    
    /** 
     * constructeur d'un état
     * un nom unique lui est donné
     * @param finalS : true si c'est un état final 
     */
    public State(boolean finalS,boolean startS)
    {this("q" + cpt++,finalS,startS);}
    
    /** 
     * constructeur d'un état
     * pas final par defaut
     */
    public State(String s){this(s,false,false);}
    
    /** 
     * constructeur d'un état
     * un nom unique lui est donné
     * pas final
     */
    public State(){this(false,false);}
    
    // ACCESSEURS
    /** retourne le nom de l'état*/
    public String getName()         {return name;}
    /** le nom de l'état*/
    public void   setName(String s) {name=s;}
    /** etat final 
     * @deprecated : utiliser setFinalState*/
     public void  setFinal(boolean b)  { setFinalState(finalS);}
    /** etat final */
    public void   setFinalState(boolean b)  { finalS = b;}
    /** etat initial */
    public void   setStartState(boolean b)  { startS = b;}
    /** accepte plusieurs activites ! */
    public void   setAcceptMultipleActivities(boolean b)  { acceptMultipleActivities = b;}
    /** l'etat accepte t il plusieurs activites ? */
    public boolean   getAcceptMultipleActivities()  { return acceptMultipleActivities;}
    /** retourne vrai si actif */
    public boolean isActive() { return !activity.isEmpty();}
    /** retourne vrai si final 
     * @deprecated utiliser isFinalState*/
    public boolean isFinal()  { return isFinalState();}
    /** retourne vrai si final */
    public boolean isFinalState()  { return finalS;}
    /** retourne vrai si etat de depart */
    public boolean isStartState()  { return startS;}
    /** retourne le premier activityToken */
    public ActivityToken getFirstActivityToken(){ return activity.getFirstActivityToken();  }
   
    // LA GESTION DES TRANSITIONS

    /** 
     * contient une transition similaire a tt - utilise la methode equals de la transition tt
     */
    public boolean containsSimilarTransition(Transition tt){
        for(TransitionLinkedListEnumeration e=transitions.elements();e.hasMoreElements();){
            Transition t=e.nextElement();
            if (tt.equals(t))
                return true;
        }
        return false;
    }

    /** 
     * contient une transition similaire a tt qui va vers l'état to - utilise la methode equals de la transition tt
     */
    public boolean containsSimilarTransitionTo(Transition tt,State to){
        for(TransitionLinkedListEnumeration e=transitions.elements();e.hasMoreElements();){
            Transition t=e.nextElement();
            if (tt.equals(t) && t.getToState()==to)
                return true;
        }
        return false;
    }

    /** contient deja la transition tt  */
    public boolean containsTransition(Transition tt){
        return transitions.contains(tt);
    }

    /** ajoute une transition */
    void addTransition(Transition t) {
        if (!transitions.contains(t))
            transitions.addTransition(t);
        else
            throw new RuntimeException("Transition is already here");
    }

    /** enleve la transition */
    void removeTransition(Transition t) {
        transitions.removeTransition(t);
    }
   
    /** enleve la premiere transition, efface bien le from */
    Transition removeFirstTransition(){
        TransitionNode tn = transitions.getAndRemoveFirstTransitionNode();
        if (tn == null) return null;
        Transition ret = tn.getTransition();
        ret.brutalUnbind();
        return ret;
    }
   
    /** retourne la premiere transition qui accepte le token passé en parametre */
    public Transition getTransition(Object inputToken){
        for(TransitionLinkedListEnumeration e=transitions.elements();e.hasMoreElements();){
            Transition t=e.nextElement();
            if (t.accept(inputToken))
                return t;
        }
        return null;
    }
   
    private TransitionLinkedList transitionSearchResults = new TransitionLinkedList();
    /** retourne une liste de toutes les transitions qui acceptent le token passé en parametre 
     * si inputToken==null, renvoit la liste des epsilon transitions.*/
    public TransitionLinkedListEnumeration getTransitions(Object inputToken){
        transitionSearchResults.clear();
        for(TransitionLinkedListEnumeration e=transitions.elements();e.hasMoreElements();){
            Transition t=e.nextElement();

            if (inputToken!=null && !t.isEpsilon() && t.accept(inputToken))
                transitionSearchResults.addTransition(t);
            else if (inputToken==null && t.isEpsilon()) 
                transitionSearchResults.addTransition(t);

        }
        return transitionSearchResults.elements();
    }

    /** retourne une enumeration de toutes les transitions qui vont vers l'etat demandé*/
    public TransitionLinkedListEnumeration getTransitionsTo(State toState){
        transitionSearchResults.clear();
        for(TransitionLinkedListEnumeration e=transitions.elements();e.hasMoreElements();){
            Transition t=e.nextElement();
            if (t.getToState()==toState)
                transitionSearchResults.addTransition(t);
        }
        return transitionSearchResults.elements();
    }

    /** retourne une enumeration de toutes les transitions similaire (grace a 
        .equals de transition) qui vont vers l'etat demandé*/
    public TransitionLinkedListEnumeration getSimilarTransitionsTo(Transition tt,State toState){
        transitionSearchResults.clear();
        for(TransitionLinkedListEnumeration e=transitions.elements();e.hasMoreElements();){
            Transition t=e.nextElement();
            if (t.getToState()==toState && t.equals(tt))
                transitionSearchResults.addTransition(t);
        }
        return transitionSearchResults.elements();
    }

    /** retourne la premiere des transitions similaires (grace a 
        .equals de transition) qui vont vers l'etat demandé.
        Utile lorsque par construction on est sur qu'il n'y en a qu'une.*/
    public Transition getFirstSimilarTransitionsTo(Transition tt,State toState){
        for(TransitionLinkedListEnumeration e=transitions.elements();e.hasMoreElements();){
            Transition t=e.nextElement();
            if (t.getToState()==toState && t.equals(tt))
                return t;
        }
        return null;
    }

    /** retourne une enumeration sur toutes les transitions */
    public TransitionLinkedListEnumeration transitions(){
        return transitions.elements();
    }

    /** retourne le vecteur des transitions */
    TransitionLinkedList getTransitions(){
        return transitions;
    }

    /** retourne vrai si le token peut etre consomme par l'etat 
     * ne tient pas compte des epsilon transitions */
    public boolean accept(Object token){
        for(TransitionLinkedListEnumeration e=transitions.elements();e.hasMoreElements();){
            Transition t=e.nextElement();
            if (!t.isEpsilon())
                if (t.accept(token))
                    return true;
        }
        return false;
    }

    /** Propage les jetons sur toutes les transitions "epsilon". 
     * Rajoute a la fin de la liste sll les nouveaux etats activés. 
     * l'etat en cours reste actif.
     * @return true si l'etat meme est encore actif apres : attention aux boucles
     * Le token est detruit si un etat ou une transition le refuse !!
     *
     * TODO : le code est un copié-collé arrangé de celui de internalConsume et les deux meritent d'etre factorisé. 
     */
    boolean internalEpsilonPropagate(StateLinkedList sll,Object applicationContext) throws AutomataException {

        if (ControlledFiniteStateAutomata.debug) System.out.println(getName()+" ---- begin internalEpsilon Propagation");
        if (ControlledFiniteStateAutomata.debug) System.out.println("         lastActivity = " + lastActivity);
        if (ControlledFiniteStateAutomata.debug) System.out.println("         activity     = " + activity);
        if (ControlledFiniteStateAutomata.debug) System.out.println("         sll          = " + sll);

        // est il possible de bouffer ce jeton ? 
        // on propage les jetons qui sont dans la liste activity. 
        // Cette derniere a ete mise a jour par internal consume
        if (activity.isEmpty()) throw new AutomataException(this,"State " + getName() + " is not active");

        // enumeration de toutes les transitions
        TransitionLinkedListEnumeration transitionsEnumeration = transitions();

        // s'il n'y a pas de epsilon transition on peut tout arreter
        if (!transitionsEnumeration.hasMoreElements()) return true;

        // Tous les tokens sont propagés
        // @@ ActivityTokenLinkedListEnumeration tokensEnumeration = activity.elements();

        // @ lastActivity va contenir tous les tokens gardés pour l'état
        lastActivity.clear();

        // pour tous les tokens présent dans la liste chainee 
        // (cas ou l'etat accepte plusieurs tokens)
        while (!activity.isEmpty()){
            // on met de cote le vrai ActivityToken (on le reserve pour la premiere transition)
            // on le met de cote afin de le dupliquer au cas ou il y ait plusieurs transitions possibles
            // on ne le passe pas tout de suite car on ne veut pas qu'il soit modifié
            // par la premiere transition
            //@@ ActivityToken currentToken = tokensEnumeration.nextElement();
            ActivityToken currentToken = activity.getAndRemoveFirstActivityToken();
            if (ControlledFiniteStateAutomata.debug) System.out.println("         activity     = " + activity);

            // pour detecter s'il est possible de desactiver l'etat   
            boolean keepToken = false; // a priori oui sauf s'il existe une transition qui ne soit pas epsilon

            // on parcourt TOUTES les epsilons transitions
            // on duplique le jeton et on le balance a toutes les epsilons transitions
            // le jeton original reste sur place
            while (transitionsEnumeration.hasMoreElements()){

                // on passe a la transition suivante
                Transition t = transitionsEnumeration.nextElement();
                if (!t.isEpsilon()) 
                    keepToken = true;
                else {
                    //@@ BUG KEY -1
                    ActivityToken at = currentToken.cloneIt();
                    if (t.epsilonAccept(at,applicationContext)) {
                        // on clone le jeton
                    
                        // on franchit la transition
                        try 
                            {
                                State tostate = t.crossInternal(null,at,applicationContext);
                                if (tostate != null) // s'il n'etait pas deja activé par un autre jeton qui est arrivé durant le meme tour
                                    sll.appendState(tostate);  // on rajoute l'etat destination a la fin de la liste des nouveaux etats actifs
                            } 
                        catch (RejectionException re){
                            // l'état ou la transition ont refusé de laisser passer cette activité
                            if (ControlledFiniteStateAutomata.debug) System.out.println(getName()+" destroy : mortné");
                            at.destroy(); // mort né
                        } 
                    }
                }
            }

            // si on doit garder le token parcequ'une des transitions n'etaient pas epsilon
            if (keepToken || !allowsDangerousEpsilonPropagationMode()){
                lastActivity.addActivityToken(currentToken);
                if (ControlledFiniteStateAutomata.debug) System.out.println("         jeton garde     = " + activity);
            } else if (allowsDangerousEpsilonPropagationMode()){
                currentToken.destroy();
            }
            
            // reprenons toutes les transitions avec le nouveaux token
            transitionsEnumeration.restart();
        }
        
        if (ControlledFiniteStateAutomata.debug) System.out.println("         avantswitch     = " + activity);
        if (ControlledFiniteStateAutomata.debug) System.out.println("         avantswitch     = " + lastActivity);
       // on replace l'activity avec les jetons conservés
        ActivityTokenLinkedList temp = activity;
        activity = lastActivity;
        lastActivity = temp;
        if (ControlledFiniteStateAutomata.debug) System.out.println("         apresswitch     = " + activity);
        if (ControlledFiniteStateAutomata.debug) System.out.println("         apresswitch     = " + lastActivity);
       
        if (ControlledFiniteStateAutomata.debug) System.out.println(getName()+" > end internalEpsilon Propagation");
        if (ControlledFiniteStateAutomata.debug) System.out.println("         lastActivity = " + lastActivity);
        if (ControlledFiniteStateAutomata.debug) System.out.println("         activity     = " + activity);
        if (ControlledFiniteStateAutomata.debug) System.out.println("         sll          = " + sll);

        // retourne vrai si l'etat est encore actif
        return !activity.isEmpty();
    }

    /** fait un reset de toutes les transitions */
    void internalReset(Object applicationContext){
        reset(applicationContext);
        activity.clear();
        lastActivity.clear();
        for (TransitionLinkedListEnumeration e=transitions.elements();e.hasMoreElements();)
            e.nextElement().reset(applicationContext);
    }

    /** previent l'etat qu'une nouvelle activité arrive 
     * throws AutomataException lorsque l'etat a refusé le jeton */
    boolean internalActivate(ActivityToken at,Object applicationContext) throws RejectionException {
        if (ControlledFiniteStateAutomata.debug) System.out.println(getName()+"> Activation of state ");
        activate(at,applicationContext); // ici l'etat peut rejeter le jeton
        return setActivity(at);
    }

    /** 
     * met a jour l'activite de l'état et les fusionne si necessaire. 
     * La fusion a lieu si l'état n'accepte qu'une seule activité et 
     * s'il contient deja une activité
     *
     * retourne true si l'etat etait deja actif car cela signifie qu'il
     * est deja enregistre dans la liste des etats actifs
     */
    boolean setActivity(ActivityToken at){
        if (ControlledFiniteStateAutomata.debug) System.out.println(getName()+"> setActivity with " + at);

        boolean ret = !activity.isEmpty();
        
        // plusieurs activités sont acceptées
        if (acceptMultipleActivities) {
            activity.addActivityToken(at);
        } else {
            // deja une activité mais on a droit qu'a une activite
            if (ret)
                activity.getFirstActivityToken().mergeIt(at);
            else 
                activity.addActivityToken(at);
        }
        //
        return ret;
    }

    /** compte le nbe d'activités dans l'etat */
    public int countActivities(){
        int i= 0;
        ActivityTokenLinkedListEnumeration tokensEnumeration = activity.elements();
        while (tokensEnumeration.hasMoreElements()){i++;tokensEnumeration.nextElement();}
        return i;
    }

    /** prepare le prochain tour : l'activite courante devient derniere activite
     * afin d'éviter les conflits lors du tour avec les jetons entrants */
    void prepareNewTurn(Object applicationContext){
        if (ControlledFiniteStateAutomata.debug) System.out.println(getName()+"> prepare New Turn : " + getName());
        if (isActive()){
            ActivityTokenLinkedList temp = lastActivity;
            lastActivity = activity;
            activity = temp;
            activity.clear(); // on renvoit tous les nodes a la maison mere
        }
        newTurn(applicationContext);
    }

    /** dévore le token, splitte les états si nécessaire, etc.. 
     * met a jour la liste des etats nouvellement activé.
     * Le token est detruit si un etat ou une transition le refuse !!
     */
    /*void internalConsume(StateLinkedList sll,Object token,Object applicationContext) throws AutomataException {
      
        if (ControlledFiniteStateAutomata.debug) System.out.println(getName()+" > begin internalConsume ");
        if (ControlledFiniteStateAutomata.debug) System.out.println("         lastActivity = " + lastActivity);
        if (ControlledFiniteStateAutomata.debug) System.out.println("         activity     = " + activity);
        if (ControlledFiniteStateAutomata.debug) System.out.println("         sll          = " + sll);

        // appel du callback
        consume(token,applicationContext); // peut emettre une rejection exception pour refuser le jeton

        // est il possible de bouffer ce jeton ? 
        if (lastActivity.isEmpty()) throw new AutomataException(this,"State " + getName() + " is not active");

        // recherche les transitions qui l'interesse
        // TODO : optimiser en faisant le test dans la boucle, dans notre cas on parcourt deux fois la liste des transitions
        TransitionLinkedListEnumeration transitionsEnumeration = getTransitions(token);
        // tous les tokens sont propagés
        ActivityTokenLinkedListEnumeration tokensEnumeration = lastActivity.elements();

        // pour tous les tokens présent dans la liste chainee 
        // (cas ou l'etat accepte plusieurs tokens)
        while (tokensEnumeration.hasMoreElements()){
            // y a t il au moins une transition sortante
            if (transitionsEnumeration.hasMoreElements()){
                // on met de cote le vrai ActivityToken (on le reserve pour la premiere transition)
                // on le met de cote afin de le dupliquer au cas ou il y ait plusieurs transitions possibles
                // on ne le passe pas tout de suite car on ne veut pas qu'il soit modifié
                // par la premiere transition
                ActivityToken anActivityToken = tokensEnumeration.nextElement();

                // et puis toutes les autres
                while (transitionsEnumeration.hasMoreElements()){

                    // on passe a la transition suivante
                    Transition t = transitionsEnumeration.nextElement();

                    System.out.println("TESTACCEPT");
                    // on teste qu'à priori la transition va laisser passer le jeton
                    // évite de générer une quantité hallucinante de jetons mort-nés
                    if (t.accept(anActivityToken,token,applicationContext)){
                        ActivityToken at = anActivityToken.cloneIt();
                        
                        // on tente de franchir la transition
                        // en dernier ressort la transition peut refuser par l'emission d'une exception
                        try 
                            {
                                State tostate = t.crossInternal(token,at,applicationContext);
                                if (tostate != null) // s'il n'etait pas deja activé par un autre jeton qui est arrivé durant le meme tour
                                    sll.addState(tostate);  // on rajoute l'etat destination a la liste des nouveaux etats actifs
                            } 
                        catch (RejectionException re){
                            // l'état ou la transition ont refusé de laisser passer cette activité
                            if (ControlledFiniteStateAutomata.debug) System.out.println(getName()+" destroy : mortné");
                            at.destroy(); // mort né
                        } 
                    }
                
                }
            }
            else //            if (transitionsEnumeration.hasMoreElements())
                {
                    // pas de transitions possibles pour notre povre activité
                    // on la kill
                    if (ControlledFiniteStateAutomata.debug) System.out.println(getName()+" destroy : 2");
                    tokensEnumeration.nextElement().destroy();
                }
        
        
        // reprenons toutes les transitions avec le nouveaux token
            transitionsEnumeration.restart();
        }
        
        // l'ancienne activité de l'etat est annulé
        lastActivity.clear();
        
        if (ControlledFiniteStateAutomata.debug) System.out.println(getName()+" > end internalConsume ");
        if (ControlledFiniteStateAutomata.debug) System.out.println("         lastActivity = " + lastActivity);
        if (ControlledFiniteStateAutomata.debug) System.out.println("         activity     = " + activity);
        if (ControlledFiniteStateAutomata.debug) System.out.println("         sll          = " + sll);

    }*/


    void internalConsume(StateLinkedList sll,Object token,Object applicationContext) throws AutomataException {
      
        if (ControlledFiniteStateAutomata.debug) System.out.println(getName()+" > begin internalConsume ");
        if (ControlledFiniteStateAutomata.debug) System.out.println("         lastActivity = " + lastActivity);
        if (ControlledFiniteStateAutomata.debug) System.out.println("         activity     = " + activity);
        if (ControlledFiniteStateAutomata.debug) System.out.println("         sll          = " + sll);

        // appel du callback
        consume(token,applicationContext); // peut emettre une rejection exception pour refuser le jeton

        // est il possible de bouffer ce jeton ? 
        if (lastActivity.isEmpty()) throw new AutomataException(this,"State " + getName() + " is not active");

        // recherche les transitions qui l'interesse
        // TODO : optimiser en faisant le test dans la boucle, dans notre cas on parcourt deux fois la liste des transitions
        TransitionLinkedListEnumeration transitionsEnumeration = getTransitions(token);
        // tous les tokens sont propagés
        ActivityTokenLinkedListEnumeration tokensEnumeration = lastActivity.elements();

        // pour tous les tokens présent dans la liste chainee 
        // (cas ou l'etat accepte plusieurs tokens)
        while (tokensEnumeration.hasMoreElements()){
            // y a t il au moins une transition sortante
            if (transitionsEnumeration.hasMoreElements()){
                // on met de cote le vrai ActivityToken (on le reserve pour la premiere transition)
                // on le met de cote afin de le dupliquer au cas ou il y ait plusieurs transitions possibles
                // on ne le passe pas tout de suite car on ne veut pas qu'il soit modifié
                // par la premiere transition
                Transition firstTransition = transitionsEnumeration.nextElement();
                ActivityToken anActivityToken = tokensEnumeration.nextElement();

                // et puis toutes les autres
                while (transitionsEnumeration.hasMoreElements()){

                    // on passe a la transition suivante
                    Transition t = transitionsEnumeration.nextElement();

                    // on teste qu'à priori la transition va laisser passer le jeton
                    // évite de générer une quantité hallucinante de jetons mort-nés
                    if (t.accept(anActivityToken,token,applicationContext)){
                        ActivityToken at = anActivityToken.cloneIt();
                        
                        // on tente de franchir la transition
                        // en dernier ressort la transition peut refuser par l'emission d'une exception
                        try 
                            {
                                State tostate = t.crossInternal(token,at,applicationContext);
                                if (tostate != null) // s'il n'etait pas deja activé par un autre jeton qui est arrivé durant le meme tour
                                    sll.addState(tostate);  // on rajoute l'etat destination a la liste des nouveaux etats actifs
                            } 
                        catch (RejectionException re){
                            // l'état ou la transition ont refusé de laisser passer cette activité
                            if (ControlledFiniteStateAutomata.debug) System.out.println(getName()+" destroy : mortné");
                            at.destroy(); // mort né
                        } 
                    }
                
                }
                
                // Enfin le premier recoit ce qu'il attendait : le vrai jeton
                try 
                    {
                        if (firstTransition.accept(anActivityToken,token,applicationContext)){
                            State tostate = firstTransition.crossInternal(token, anActivityToken,applicationContext);
                            // s'il n'etait pas deja actif (parce qu'une activite lui serait parvenu au meme moment par un chemin detourne)
                            if (tostate != null) 
                                sll.addState(tostate); // ajout a la liste des etats actives
                        }
                    } catch (RejectionException re){
                        // l'état ou la transition ont refusé de laisser passer cette activité
                        if (ControlledFiniteStateAutomata.debug) System.out.println(getName()+" destroy : 1");
                        anActivityToken.destroy();
                    } 
            }
            else //            if (transitionsEnumeration.hasMoreElements())
                {
                    // pas de transitions possibles pour notre povre activité
                    // on la kill
                    if (ControlledFiniteStateAutomata.debug) System.out.println(getName()+" destroy : 2");
                    tokensEnumeration.nextElement().destroy();
                }
            
            // reprenons toutes les transitions avec le nouveaux token
            transitionsEnumeration.restart();
        }

        // l'ancienne activité de l'etat est annulé
        lastActivity.clear();

        if (ControlledFiniteStateAutomata.debug) System.out.println(getName()+" > end internalConsume ");
        if (ControlledFiniteStateAutomata.debug) System.out.println("         lastActivity = " + lastActivity);
        if (ControlledFiniteStateAutomata.debug) System.out.println("         activity     = " + activity);
        if (ControlledFiniteStateAutomata.debug) System.out.println("         sll          = " + sll);

    }

    /** affichage */
    public String toString(){
        String res = "";
        res += getName()+ " final="+finalS+" start=" + startS;
        int i=1;
        for (TransitionLinkedListEnumeration e=transitions.elements();e.hasMoreElements();){
            res += "\n";
            res += "   " + (i++) + ". " + e.nextElement();
        }
        return res;
    }

    abstract public String getRank();

    // ------------------------------------------------------------
    // CALLBACKS

    /** appelé pour demander à l'etat s'il accepte de voir disparaitre (si possible) les activités qu'il
     * contient lors de la propagation des epsilons transitions. Attention si cette méthode retourne false
     * l'automate fonctionnera correctement mais risque de dupliquer inutilement des activités. Si elle
     * retourne vrai il peut que l'automate entre en boucle... a manipuler avec precautions uniquement
     * dans des cas bien maitrises */
    public boolean allowsDangerousEpsilonPropagationMode(){ return false;}
    /** appelé avant chaque nouveau consume */
    abstract public void newTurn(Object applicationContext);
    /** appelé lorsqu'un nouveau token a avaler arrive 
     *  l'etat peut refuser d'avaler le token en launchant une exception  */
    abstract public void consume(Object inputToken,Object applicationContext) throws RejectionException;
    /** appelé avant que l'état soit activé. 
     * l'état peut refuser le jeton en lancant une exception */
    abstract public void activate(ActivityToken at, Object applicationContext) throws RejectionException;
    /** appelé pour une remise au propre de l'état */
    abstract public void reset(Object applicationContext);
   
    // PROPRIETES
    // public boolean isDeterministic();

}

