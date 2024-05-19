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

package com.expway.util;

import java.util.Hashtable;

/**
 * FactoryHandler.java
 *
 *
 * Created: Fri Aug 11 15:26:07 2000
 *
 * @author 
 * @version
 */

public class HandlerManager  {


    // content DIctionnary
    private static Hashtable handlerManagerMap = new Hashtable();

    // TODO Exception 2 fois une entrée
    public static void register(String handlerClassName, int poolSize, Hashtable classHandlerMap) {
        // on ajoute le classHandler Map
        handlerManagerMap.put(handlerClassName,new HandlerManager(handlerClassName,poolSize,classHandlerMap));
    }
    // TODO erreur si ce n'est pas enregistre
    public static LocalHandler getSonHandler(String father, String son){
        //        System.out.println("on recherche "+son+"for"+father);
        // on recupere le manager du pere
        HandlerManager handlerManager = (HandlerManager) handlerManagerMap.get(father);
        if (handlerManager == null){
                       System.out.println("le handler de "+father+ " n'a pas ete enregistre");
            return  new DefaultLocalHandler();                  //faire une erreur
        }
        if (handlerManager.classHandlerMap == null) 
            return  new DefaultLocalHandler();                  //faire une erreur
            
        else {
            String className = (String)handlerManager.classHandlerMap.get(son);

            if (className == null) {
                System.out.println("on ne connait pas "+son+" pour "+father);
                return  new DefaultLocalHandler();                  // faire un error 
            }else try { 
                HandlerManager sonHandlerManager = (HandlerManager) handlerManagerMap.get(className);
                if (sonHandlerManager == null){
                    System.out.println("le handler pour "+className+" na pas ete enregistre");
                    return  new DefaultLocalHandler();                  // faire un error 
                }else {
                    //                    System.out.println("on imple "+className);
                    // on recupere le manager du fils
                    return sonHandlerManager.newInstance();
                }
            } catch (Exception e){ e.printStackTrace();}   //faire une error
        }
        return null;                  //impossible normalement
      

        
    }

   public  static void release(LocalHandler localHandler){
        String className = localHandler.getClass().getName();
        HandlerManager handlerManager = (HandlerManager) handlerManagerMap.get(className);
        if (handlerManager != null)
            handlerManager.localRelease(localHandler);               
    }
        
    

    // class qui contient la pool, et le dictionnaire des sous handler
    // pour un Handler

    LocalHandler[] localHandlerPool; 
    int indexFreeInstance = -1;
    int poolSize;
    Hashtable classHandlerMap;
    String className;

    HandlerManager (String handlerClassName, int aPoolSize, Hashtable aClassHandlerMap){
        // On construit la pool
        poolSize = aPoolSize;
        localHandlerPool =  new LocalHandler[poolSize];
        classHandlerMap = aClassHandlerMap;
        className = handlerClassName;
    }

    LocalHandler newInstance(){
        if (indexFreeInstance < 0) 
        try{
            //        System.out.println("######## on cree "+className);
            return (LocalHandler)Class.forName(className).newInstance();
        }catch (Exception e){e.printStackTrace();}
        indexFreeInstance --;
        return localHandlerPool[indexFreeInstance + 1];
    }

    void localRelease(LocalHandler localHandler){
        localHandler.reset();
        if (indexFreeInstance < poolSize-1){
            indexFreeInstance++;
            localHandlerPool[indexFreeInstance] = localHandler;
        }
    }


    
}// FactoryHandler
