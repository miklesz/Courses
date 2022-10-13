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

package com.expway.tools.compression;

import com.expway.binarisation.CodingParameters;
import com.expway.tools.automata.ActivityToken;

import com.expway.tools.io.Chunk;
import com.expway.tools.io.ChunkWriter;

import java.io.FileWriter;
import java.io.IOException;

public class CompressionActivityToken implements ActivityToken {

    static final public boolean DEBUG = false;
    
	
    static int cptid = 0;
    int id = cptid++;

    int bitsOfStructure = 0;
    /** ne marche qu'après avoir créé un chunk */
    public int getBitsOfStructure(){return bitsOfStructure;}

    CptNodeStack     theCptStack = null;
    CptNodeStack     theCptHistory = null;
    CodingNodeStack  theCodingHistory = new CodingNodeStack();
    
    FileWriter fw = null;

    public CompressionActivityToken(){
    	
    }

    private CompressionActivityToken(CompressionActivityToken another){
        if (DEBUG) System.out.println(name() + " new based on " + another.name());

        // on recupere l'historique des coding
        theCodingHistory.bind(another.theCodingHistory);

        // on recupere l'historique des cpts
        if (another.theCptHistory != null){
            theCptHistory = new CptNodeStack(another.theCptHistory);
            //theCptHistory.bind(another.theCptHistory);
        }

        // on recopie tous les compteurs en cours
        if (another.theCptStack != null)
            theCptStack = new CptNodeStack(another.theCptStack);
		
    }

    // ------------------------------------------------------------
    // GESTION DES COMPTEURS EMPILES

    /** appelle la longueur de l'occurrence courante */
    public int getCpt(){return theCptStack.lastValue();}
    
    /** increment le cpt courant */
    public void incrCpt(){ 
        if (DEBUG) System.out.println(name() + " incrCPT.");
        theCptStack.incrLast();
        printTokenState();
        
    }
    
    /** ajoute un compteur, params le nbe de bits necessaire pour son codage : -1 inconnu*/
    public void addCpt(int minoccurs,int nbits){
        if (DEBUG) System.out.println(name() + " addCPT. minvalue= " + minoccurs + " nbits=" + nbits);

        // une pile de compteur (en cours) est necessaire
        if (theCptStack == null)  theCptStack = new CptNodeStack();
        // rajoute un compteur dans la pile des compteurs
        theCptStack.push(new CptNode());

        // une pile de compteur (historique) est necessaire
        if (theCptHistory == null) theCptHistory = new CptNodeStack();
        // rajoute un noeud (empty) à l'histoire des cpts
        theCptHistory.push(new CptNode());

        // rajoute un noeud longueur holder
        theCodingHistory.push(new LengthHolderCodingNode(minoccurs,nbits));

        // Affichage
        printTokenState();
        
    }
    
    /** ote un compteur, le rajoute a l'historique des compteurs */
    public void remCpt(){
        // get the last node in the compteur stack
        CptNode cn = theCptStack.pop();
        // lock it (impossible to modify it) (for checking only)
        cn.lock();

        if (DEBUG) System.out.println(name() + " remCPT. " + cn);

        //if (theCptHistory == null) theCptHistory = new CptNodeStack();
        // a cpt is waiting for you in the history of cpt
        theCptHistory.pushInFirstEmptyCpt(cn);

        printTokenState();
        
    }

    // ------------------------------------------------------------
    // CODAGE

    // if ic.nbits == -1, write se fait en infinite long
    public void encodeInteger(IntegerCoding ic){
        encodeTransition(ic.value,ic.nbits);
    }

    // if nbits == -1, write se fait en infinite long
    public void encodeTransition(int value, int nbits){
        //System.out.println(" CAT no" + id + " " + value +"["+nbits+"]");
        if (value == 0 && nbits == 0) return; // inutile
        theCodingHistory.push(new KeyCodingNode(value,nbits));
    }

    public void encodeType(TypeEncoder te){
        theCodingHistory.push(new ValueCodingNode(te));
    }
    
    public void encodeCompatiblePieceHeader(IntegerCoding version, IntegerCoding type){
        theCodingHistory.push(new CompatiblePieceHeader(version,type));
    }
    
    // ------------------------------------------------------------
    
    public void append(CompressionActivityToken cat){
        if (theCptHistory == null)      theCptHistory = new CptNodeStack();
        if (cat.theCptHistory == null)  cat.theCptHistory = new CptNodeStack();
        theCptHistory.append(cat.theCptHistory);
        
        if (theCptStack == null)      theCptStack = new CptNodeStack();
        if (cat.theCptStack == null)  cat.theCptStack = new CptNodeStack();
        theCptStack.append(cat.theCptStack);

        theCodingHistory.append(cat.theCodingHistory);
    }

    /** Compatibility : cette methode doit être celle du CAT de l'attribut 
     * a qui on passe le CAT du content */
    public void appendSimpleTypeInFirstCompatibilitySpace(TypeEncoder te) throws ParsingException {
        CodingNode curr = theCodingHistory.last;
        CodingNode last = curr;
        curr = curr.next;
        while (true){
            while (!(curr instanceof CompatiblePieceHeader)) curr=curr.next;
            if (curr.next == null) break; // the last PCH
            last = curr;
            curr = curr.next;
        } 

        CodingNode teNode = new ValueCodingNode(te);
        CodingNode temp = last.next;
        last.next = teNode;
        teNode.next = temp;
    }

	
    public void appendInCompatibilitySpaces(CompressionActivityToken contentModel) throws ParsingException {
        if (theCodingHistory == null || theCodingHistory.last ==null) 
            throw new ParsingException("CompressionActivityToken is empty");
        if (contentModel.theCodingHistory == null || contentModel.theCodingHistory.last ==null) 
            throw new ParsingException("CompressionActivityToken content Model is empty");

        // avant tout on realize les cpts des deux CAT
        realizeCpt();
        contentModel.realizeCpt();

        // les pointeurs de noeuds
        CodingNode cont = contentModel.theCodingHistory.last;
        CodingNode attr = theCodingHistory.last; 
        CodingNode currentCM = cont;
        

        // les tests spécifiques du début
        if (!(contentModel.theCodingHistory.last instanceof CompatiblePieceHeader)) {
            CodingNode attrTemp = theCodingHistory.last;
            //il y a des noeuds a prendre dans le content model pour les mettre dans les attributs
            // lie debut_attr -> debut_content
            theCodingHistory.last = contentModel.theCodingHistory.last;
            // deplace content vers le prochain CPH
            //System.out.println("A : cont="+cont+" attr="+attr);
            while (!(cont instanceof CompatiblePieceHeader)) {currentCM=cont;cont=cont.next;}
            // puis relier la fin de cette partie au début des attributs (mis de coté dans attr)
            currentCM.next = attrTemp;
        }
        // on cale le pointeur attribut sur le prochain PCH
        //System.out.println("B : cont="+cont+" attr="+attr);
        while (!(attr instanceof CompatiblePieceHeader)) attr=attr.next;

        // ** maintenant attr et cont pointe sur le premier PCH

        // Pour toutes les PCH
        //System.out.println("C : cont="+cont+" attr="+attr);
        while (attr.next != null && cont.next != null){

            if (!(cont.next instanceof CompatiblePieceHeader)) {
                //il y a des noeuds a prendre dans le content model pour les mettre dans les attributs
                CodingNode attrTemp = attr.next;
                // lie debut_attr -> debut_content
                attr.next = cont.next;
                cont = cont.next;
                // deplace content vers le prochain CPH
                while (!(cont instanceof CompatiblePieceHeader)) {currentCM=cont;cont=cont.next;}
                //System.out.println("D : cont="+cont+" attr="+attr);
                // puis relier la fin de cette partie au début des attributs (mis de coté dans attrTemp)
                currentCM.next = attrTemp;
            } 
            else 
                cont = cont.next;
            
            // on cale le pointeur attribut sur le prochain PCH
            attr = attr.next;
            while (!(attr instanceof CompatiblePieceHeader)) attr=attr.next;
            //System.out.println("E : cont="+cont+" attr="+attr);
            }
    }

    /** appelé lorsque deux jetons se rejoignent TODO */
    public void mergeIt(ActivityToken at){}

    /** appelé lors de l'exécution d'un automate non déterministe
     * lors de plusieurs transitions avec le même arc */
    public ActivityToken cloneIt(){
        CompressionActivityToken cloned = new CompressionActivityToken(this);
        
        return cloned;
    }

    /** appelé lorsque le token disparait parce qu'il ne trouve plus d'arc ou aller */
    public void destroy(){}

   /** Affichage */
    public String name(){
        return "CAT n"+id;
    }

	

    // ------------------------------------------------------------
    // Création d'un chunk à partir de l'encoding

    private void realizeCpt(){
        //
        if (theCptStack != null){
            if (theCptStack.last != null)
                throw new RuntimeException("Impossible to realize a CAT with remaining Cpt");
        }
        //
        if (theCptHistory == null || theCptHistory.last == null) return;

        CodingNode cn = theCodingHistory.last;
        CptNode cpn = theCptHistory.last;
        while (cn!=null) {
            if (cn instanceof LengthHolderCodingNode){
                ((LengthHolderCodingNode)cn).setLengthValue(cpn);
                cpn = cpn.next;
            }
            cn = cn.next;
        }

        theCptHistory = null;
    }

    Chunk generateChunk(){
        if (DEBUG) System.out.println("DEBUT CODAGE");
        if (DEBUG) System.out.println();

        if (DEBUG) System.out.println("    Coding  = " + this);
        if (DEBUG) System.out.println("    Cpt His = " + theCptHistory);
        if (DEBUG) System.out.println("    Cpt Sta = " + theCptStack);

        // realization des CPTs
        realizeCpt();

        if (DEBUG) System.out.println("Après realizeCpt");
        if (DEBUG) System.out.println("    Coding  = " + this);
        if (DEBUG) System.out.println("    Cpt His = " + theCptHistory);
        if (DEBUG) System.out.println("    Cpt Sta = " + theCptStack);

        // l'historique est a l'envers si bien que l'on doit remonter tout au bout d'abord
        CodingNodeStack inverseCoding = new CodingNodeStack();
        CodingNode ccn = theCodingHistory.pop();
        while (ccn!=null){
            inverseCoding.push(ccn);
            ccn = theCodingHistory.pop();
        }

        if (DEBUG) System.out.println();
        if (DEBUG) System.out.println("    Coding          = " + this);
        if (DEBUG) System.out.println("    Cpt History     = " + theCptHistory);
        if (DEBUG) System.out.println("    Inverse Coding  = " + inverseCoding);
        //          if (DEBUG) System.out.println("    Inverse Cpt H.  = " + inverseCpt);
        
        ChunkWriter cw = new ChunkWriter();
        // une fois fait on redeboule tout et on ecrit dans le chunk writer
        ccn = inverseCoding.pop();
        while (ccn!=null){
            if (DEBUG) System.out.println();
            if (DEBUG) System.out.println("Codage de " + ccn);
            bitsOfStructure += ccn.encodeYourself(cw);
            ccn = inverseCoding.pop();
        }

        if (DEBUG) System.out.println("");
        if (DEBUG) System.out.println("    Coding          = " + this);
        if (DEBUG) System.out.println("    Cpt History     = " + theCptHistory);
        if (DEBUG) System.out.println("    Inverse Coding  = " + inverseCoding);
        if (DEBUG) System.out.println("");
        if (DEBUG) System.out.println("FIN CODAGE");

        return cw;
    }

    public void printTokenState(){
        if (DEBUG) System.out.println("    Coding  = " + this);
        if (DEBUG) System.out.println("    Cpt His = " + theCptHistory);
        if (DEBUG) System.out.println("    Cpt Sta = " + theCptStack);
    }

    

}

// ================================================================================
// CODING NODES

abstract class CodingNode {
    static int uidcpt = 0;
    int uid =0;

    protected CodingNode(){
        uid = uidcpt++;
    }

    CodingNode next;

    /** return the number of bits of structure */
    abstract public int encodeYourself(ChunkWriter btb);
}

class CompatiblePieceHeader extends CodingNode {
    
    KeyCodingNode version = null;
    KeyCodingNode type = null;
    
    public CompatiblePieceHeader(IntegerCoding v,IntegerCoding t){
        version = new KeyCodingNode(v.value,v.nbits);
        type = new KeyCodingNode(t.value,t.nbits);
    }

    public int encodeYourself(ChunkWriter btb){
        int s = version.encodeYourself(btb);
        s+= type.encodeYourself(btb);
        return s;
    }    

    public String toString(){
        return "COMPAT("+version.toString()+"|" + type.toString() +")";
     }
 }

class KeyCodingNode extends CodingNode {
    protected int codingValue = -1;
    protected int codingLength = -1;

    public KeyCodingNode(int codingV,int codingL){
        codingValue = codingV;
        codingLength = codingL;
    }

    public int encodeYourself(ChunkWriter btb){
        if (CompressionActivityToken.DEBUG) System.out.println("     Encodage d'une key " + codingValue + " sur " + codingLength + " bits");

        if (codingLength != 0  && codingValue == -1)
            throw new RuntimeException("Value ("+codingValue+") not correct in " + this);

        if (codingLength == -1){

            if (CodingParameters.bWriteStructure)
                return btb.writeInfiniteLong(codingValue);
            else
                return 0;
        }
        else {
            
            if (CodingParameters.bWriteStructure)
                btb.writeInt(codingValue,codingLength);
            return codingLength;
        }
    }

    public String toString(){
        if (codingLength == 0 && codingValue ==0)
            return ".";
        return (codingValue==-1?"?":""+codingValue)
            +"["+(codingLength==-1?"?":""+codingLength)+"]";
    }
}

class LengthHolderCodingNode extends KeyCodingNode {

    // utilisé aussi dans DecompressionContext
    static final public int INFINITE_INTEGER_THRESHOLD = 16;
    int minOccurs = 0;

    public LengthHolderCodingNode(int min,int codingL){
        super(-1, (codingL>INFINITE_INTEGER_THRESHOLD?-1:codingL) );
        minOccurs = min;
    }

    public void setLengthValue(CptNode cn){
        codingValue = cn.getValue() - minOccurs;
    }

    public String toString(){
        return "LH[min="+minOccurs+"](" + super.toString()+")";
    }

}

class ValueCodingNode extends CodingNode { 
    TypeEncoder theTypeEncoder;
    
    public ValueCodingNode(TypeEncoder te){
        theTypeEncoder = te;
    }

    public int encodeYourself(ChunkWriter btb){
        if (CompressionActivityToken.DEBUG) 
            System.out.println("     Encodage de " + theTypeEncoder + " de classe " + theTypeEncoder.getClass().getName());
        
        // on prend le coding et on lui rajoute son contexte	
        Chunk c = theTypeEncoder.getCodingWithContext(true);
        
        if (c!=null) // empty element for instance
            if(CodingParameters.bWriteData||(!(theTypeEncoder instanceof SimpleTypeInstance))) {
                c.writeYourselfInto(btb);
            }
            else
                if (CompressionActivityToken.DEBUG) 
                    System.out.println("     Encodage de " + theTypeEncoder + " = element vide ");
        return 0; // no bits of structure or already computed
    }

    public String toString(){
        return "VALUE("+theTypeEncoder+")";
    }
}


// STACK 

class CodingNodeStack {

    CodingNode last = null;

    /** Attention, les deux Stacks deviennent identiques */
    void append(CodingNodeStack cns){
        if (cns.last==null) return;
        CodingNode cn = cns.last;
        while (cn.next !=null) cn = cn.next;
        cn.next = last;
        last = cns.last;
    }
    
    void bind(CodingNodeStack cns){
        last = cns.last;
    }

    void push(CodingNode c){
        c.next=last;
        last = c;
    }

    CodingNode pop(){
        if (last == null) return null;
        CodingNode c = last;
        last = last.next;
        c.next = null;
        return c;
    }
    
	
}
