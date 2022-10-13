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

package com.expway.tools.expression;

import java.util.*;
import java.io.*;

// Classe de base de gestion d'un noeud d'arbre.

abstract public class BasicTreeNode { 
    // Variables d'instance

    static final boolean WARNING = false;

    private List            children = new ArrayList();
    private BasicTreeNode   parent = null; 
    
    //------------------------------------------------------------
    // CONSTRUCTEURS

    /** constructeur */
    public BasicTreeNode(){}

    //------------------------------------------------------------
    // ACCESSEURS

    // Les parents

    /** rattache le noeud de son pere
     * methode privée afin de maintenir la cohérence des liens croisés fils-pere*/
    private   void  setParent(BasicTreeNode tn) {parent = tn;}

    /** detache le noeud de son pere */
    protected void  clearParent(){
        // viré de chez ses parents s'il y est encore à son age !
        if (parent != null)  parent.getChildren().remove(this);
        parent=null;
    }
    public BasicTreeNode getParent()            {return parent;}
    
    // Les enfants

    public boolean  isLeaf()               {return children.size()==0;}

    public void     addChild(BasicTreeNode tn) {
        children.add(tn);
        if (tn != null) tn.setParent(this);
        if (tn == null && WARNING){
            System.out.println("[Warning] Adopt null to BasicTreeNode");
            Thread.dumpStack();
        }
    }

    void addChildAt(int i,BasicTreeNode tn) {
        children.add(i,tn);
        if (tn != null) tn.setParent(this);
        if (tn == null && WARNING){
            System.out.println("[Warning] Adopt null to BasicTreeNode");
            Thread.dumpStack();
        }
    }
    
    /** adoptChild => contrairement a addChild la relation fils -> pere n'est pas mise a jour */
    void adoptAsChild(BasicTreeNode tn)          {
        children.add(tn);
        if (tn==null && WARNING){
            System.out.println("[Warning] Adopt null to BasicTreeNode");
            Thread.dumpStack();
        }
    }

    void adoptAsChildAt(int i,BasicTreeNode tn)  {
        children.add(i,tn);
        if (tn==null && WARNING){
            System.out.println("[Warning] Adopt null to BasicTreeNode");
            Thread.dumpStack();
        }
    }

    void adoptChildrenOf(BasicTreeNode btn){
        Iterator i = btn.children();
        while (i.hasNext())
            adoptAsChild((BasicTreeNode)i.next());
    }


    public BasicTreeNode getFirstChild()        {return (BasicTreeNode)children.get(0);}
    public BasicTreeNode getChild(int n)        {return (BasicTreeNode)children.get(n);}
    
    public Iterator children()             {return children.iterator();}
    protected List getChildren()           {return children;}

    public int size()                      {return children.size();}

    //------------------------------------------------------------
    // Abstraites
    
    // Retourne un nom unique
    abstract public String getUName();
    
    
}

