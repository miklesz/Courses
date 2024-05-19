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

package com.expway.schema.instance;

import gnu.regexp.RE;
import gnu.regexp.REException;
import java.util.*;
import com.expway.schema.SchemaSymbols;

/**
 * DataType.java
 *
 * This class is based on XML Schema datatypes mechanism.
 * It allows some basic datatype mechanism such as checking lexical 
 * representation, possible values, aso.
 *
 * A hierarchy of datatype is allowed. When checking every constraint 
 * of the hierarchy should be checked.
 * 
 * Accepted features :
 *  - Lexical representation is only regexp 
 *  - basetype can be either a primitive or a generated or a user datatype
 *  - <BOLD>to be continued...</BOLD>
 *  
 * <p>TODO IDREFS</p>
 * @author Claude Seyrat & Cedric thienot
 */


// TODO : gerer l'heritage dans la recuperation des facets !

public class DataType extends Type {

    // debugging
    final static public boolean DEBUG = false;

    // the type of this datatype
    final static public short UNDEFINED = -1;
    final static public short PRIMITIVE = 0;
    final static public short GENERATED = 1;
    final static public short USER      = 2;
    protected short type; 
    
    // the baseType (null for primitive)
    DataType myBaseType;
    DataTypeRef myBaseTypeRef;
    DataTypeRef myPrimitiveDataType;
    
    // subtypes possible facets ; only for primitive datatypes
    Collection possibleFacetNames;
    Collection inheritedPossibleFacetNames;
    Collection superDataTypeNames;
    
    // les facets courantes 
    FacetMap currentFacets;
    FacetMap inheritedFacets;

    // the reference
    
    String    refName;

    public boolean isPrimitive(){
        return (type == PRIMITIVE);
    }
    public void setPrimitive(){
        type = PRIMITIVE;
    }
    /**
     * override of name of the element 
     * it could be either the name of the element or the name of the referenced element
     */
    public String getName(){
        if (getRefName()!=null) return getRefName();
        return super.getName();
    }
    DataType refDataType;
    /**
     * Get the value of refElement.
     * @return Value of refElement.
     */
    public DataType getRefDataType() {return refDataType;}
    /**
     * return the reference of the datattype
     */
    public SchemaObject getReference() {return getRefDataType();}
    
    /**
     * Set the value of refElement.
     * @param v  Value to assign to refElement.
     */
    public void setRefDataType(DataType  v) {this.refDataType = v;}
    
    /**
     * Get the value of refName.
     * @return Value of refName.
     */
    public String getRefName() {return refName;}
    
    /**
     * Set the value of refName.
     * @param v  Value to assign to refName.
     */
    public void setRefName(String  v) {
        if (v==null) return;
        isAReference = true;
        this.refName = v;}

    // fin de reference

    /**
     * return true
     */
    public boolean isSimple(){return true;}

    /**
     * return true if is has been defined by the user
     */

    public boolean isUserDefined(){
        return type==USER;
    }

    public void setBaseType(DataType aT){
        myBaseType = aT;
    }

    public DataType getBaseType(){
        if (myBaseType != null) return myBaseType;
        else if (myBaseTypeRef != null) {
            setBaseType((DataType) myBaseTypeRef.getTarget());
            return myBaseType;
        }
        return null;
    }

    final public boolean hasSuperType(){return (getSuperType()!=null);}

    public Type getSuperType(){
        if (myBaseTypeRef != null) return getBaseType(); 
        if (myPrimitiveDataType != null) return (Type)myPrimitiveDataType.getTarget(); 
        return null;
    }
    //TODO
    public void setDerived(String derivedType)throws SchemaException{}
    public String getDerived(){return "";}

    // 
    void addPossibleFacet(String name){
        possibleFacetNames.add(name);
    }

    public Collection getPossibleFacets(){return possibleFacetNames;}
    public void setPossibleFacets(Collection c){ possibleFacetNames = c;}

    // managing the current facets
    void  addFacet(Facet fi)    { currentFacets.putFacet(fi); }
    public void  addFacet(String name,String value) throws SchemaException {
        SimpleFacet sf=(SimpleFacet)Facet.getFacetImpl(name);
        try {
            sf.setValue(value);
        } catch (SchemaException e){
            e.setSource(this);
            throw e;
        }
        addFacet(sf);
    }
    public Facet getFacet(String name) { return currentFacets.getFacet(name); }

    /**
     * Set the value of possibleValues.
     * @param v  Value to assign to possibleValues.
     */

    public void addEnumerationFacet(Collection c) throws SchemaException {
        EnumerationFacet ef=(EnumerationFacet)Facet.getFacetImpl("enumeration");

        Iterator i=c.iterator();
        if (DEBUG) 
            while(i.hasNext()){
                System.out.println(","+i.next());
            }

        ef.setValue(c);
        addFacet(ef);
    }

    /**
     * checks if the current datatype has a enumeration facet (no inheritance)
     */
    public boolean isEnumerated() {
        return currentFacets.containsFacet("enumeration");
    }

    /**
     * get the enumeration facet of the datatype (no inheritance)
     */

    public Collection getEnumerationFacet() {
        EnumerationFacet enum=(EnumerationFacet)getFacet("enumeration");
        return enum.getValues();
    }

    // watch out : datatype must be realized

    /**
     * get the facet "name" of the datatype 
     *   inheritance is taken into account
     */

    public Facet getFacetInh(String name) { return inheritedFacets.getFacet(name); }

    /**
     * checks if the current datatype has a enumeration facet 
     *   inheritance is taken into account
     *   datatype must be realized in order to work properly
     */

    public boolean isEnumeratedInh() {
        return inheritedFacets.containsFacet("enumeration");
    }

    /**
     * get the facet enumeration of the datatype 
     *   inheritance is taken into account
     *   datatype must be realized in order to work properly
     */

    public Collection getEnumerationFacetInh()  {
        EnumerationFacet enum=(EnumerationFacet)getFacetInh("enumeration");
        return enum.getValues();
    }

    boolean isAList =false;
    public boolean isAList(){return isAList;}
    public void setAsAList(){isAList = true;}
    boolean isAUnion =false;
    public boolean isAUnion(){return isAUnion;}
    public void setAsUnion(){isAUnion = true;}
   
   
    // ================================================================================
    //                                  CONSTRUCTORS

    /**
     * Constructor
     */
    public DataType() {
        this(null,null,USER);
    }

    public DataType(String name) {
        this(name,null,USER);
    }
    /**
     * basetype can be list union or an other type
     */
    public DataType(String name,String basetype) {
        this(name,basetype,USER);
    }

    // acces package : reserve pour les types primitifs et generated
    DataType(String name, String basetype, short type) {
        super(name);

        this.type = type;

        //    if (name==null)
        //  throw new RuntimeException("error datatype null");
    

        if (basetype == null)
            myBaseTypeRef = null;
        else
            myBaseTypeRef = new DataTypeRef(basetype);
      
        possibleFacetNames = new HashSet();
        currentFacets      = new FacetMap();

        inheritedPossibleFacetNames = new HashSet();
        inheritedFacets             = new FacetMap();

        superDataTypeNames = new HashSet();
    }



    // ================================================================================
    //                       CHECKING MECHANISM              


    public boolean isSubDataTypeOf(String n){
        return superDataTypeNames.contains(n);
    }

    /**
     * add facets to the facet map with respect to inheritance rules
     * from bottom to top of the hierarchy
     * returns primitive datatype
     */

    private DataType generateInheritance(Schema s, 
                                         FacetMap inh_facets, 
                                         Collection inh_possibleFacets, 
                                         Collection inh_supertypes,
                                         DataType checkedDT) throws SchemaException
    {
        if (DataType.DEBUG) System.out.println("    > generate inheritance of " + getName() + " facet = " + inh_facets);

        // add possible facets
        inh_possibleFacets.addAll(possibleFacetNames);
        // add superdatatype
        inh_supertypes.add(getName());

        if (DataType.DEBUG) System.out.println("    > possible facets of " + this.getName() + " facet = " + inh_possibleFacets);

        // add recursively every facet and merge them if needed
        Iterator it = currentFacets.values().iterator();
        while(it.hasNext()){
            Facet newfacet = (Facet)it.next();
            if (!inh_facets.containsFacet(newfacet.getName()))
                inh_facets.putFacet(newfacet);
            else 
                {
                    Facet f = inh_facets.removeFacet(newfacet.getName());
                    if (DataType.DEBUG) System.out.println("    > facet " 
                                                           + newfacet.getName() 
                                                           + " already exists : check for final !");

                    if (newfacet.getType() == Facet.FINAL) {
                        s.getErrorHandler()
                            .schemaError(new DataTypeException(checkedDT,
                                                               "Facet " + newfacet.getName() + 
                                                               " of " + getName() + 
                                                               " can't be redefined in datatype " + checkedDT.getName()));
                        // put the final one !
                        inh_facets.putFacet(newfacet);
                    } else {
                        // get the sub facet, remove it from the inherited
                        if (DataType.DEBUG) System.out.println("    > facet " 
                                                               + newfacet.getName() 
                                                               + " already exists : request for merge !");

                        // get the merging of the two facets
                        // put it in the inherited facets
                        try { inh_facets.putFacet(f.giveMergedFacet(newfacet)); }
                        catch(SchemaException se){
                            se.setSource(checkedDT);
                            s.getErrorHandler().schemaError(se);
                        }
                    }
                }
        }

        // then call the generateInheritedFacets from the father datatype
        // we dont know if the datatype is realized or not (should be)
         
        if (type!=PRIMITIVE){
            if (myBaseType == null) 
                if (myBaseTypeRef == null) 
                    return this;
                else                           
                    myBaseType = (DataType)myBaseTypeRef.getTarget(s);
            return myBaseType.generateInheritance(s,
                                                  inh_facets,
                                                  inh_possibleFacets,
                                                  inh_supertypes,
                                                  checkedDT);
        }
        else
            return this;
    }

    protected void realizeInheritance(Schema s) throws SchemaException {
        inheritedFacets = new FacetMap();
        inheritedFacets.putAll(currentFacets);
        inheritedPossibleFacetNames = new HashSet();
        inheritedPossibleFacetNames.addAll(possibleFacetNames);
        superDataTypeNames = new HashSet();
        superDataTypeNames.add(getName());

        DataType primitive = null;
        // on pourrait gagner du temps si le calcul d'un
        // datatype sup a deja ete fait : TODO
        // CLAUDE ERROR renvoie un null pointer exception avec bug6
      
        if (type!=PRIMITIVE){
            primitive = getBaseType().generateInheritance(s,
                                                          inheritedFacets,
                                                          inheritedPossibleFacetNames,
                                                          superDataTypeNames,
                                                          this);
         
            myPrimitiveDataType = new DataTypeRef(primitive);
        }
      
        if (DEBUG) System.out.println(this);
      
    }

    // in order if all the facets are possible
    protected void checkPossibleFacets(Schema s) throws SchemaException {
        if (type!=PRIMITIVE && myPrimitiveDataType==null){
            realizeInheritance(s);
        }
        if (myBaseTypeRef != null)
            //{
            // checks
            // CLAUDE error j'ai du rajouter la condition
            if (myPrimitiveDataType != null) {
                DataType myprim = (DataType)myPrimitiveDataType.getTarget();
                Iterator i = inheritedFacets.values().iterator();
                while(i.hasNext()){
                    String n=((Facet)i.next()).getName();
               
                    if (!inheritedPossibleFacetNames.contains(n)){
                        s.getErrorHandler().schemaError(new SchemaException(this,"Facet " + n 
                                                                            + " of " + getNameNonNull() 
                                                                            + " impossible (sub type of " 
                                                                            + myprim.getName() + ")"));
                        
                    }
                }
            }
    }
   
    public void realize(Schema s) throws SchemaException {
                   
       if (isAlreadyRealized) return;
        isAlreadyRealized = true;
        // realize when the type is a reference
        if (isAReference()){
            Type t = s.getRType(getRefName());
            setRefDataType((DataType)t);
            if (getRefDataType() == null)
             throw new SchemaException(this,
                                          SchemaMessages.getMessages("undefine element"),
                                          new String[] {this.getRefName()});
            if (!t.isSimple()) 
                throw new SchemaException(this,
                                          SchemaMessages.getMessages("onlySimpleType"),
                                          new String[] {"union",this.getRefName()});
            else  {
                getRefDataType().realize(s);
                isCoherent = true; 
                return;
            }
        }

        if (DataType.DEBUG) System.out.println("");
        if (DataType.DEBUG) System.out.print("====> REALIZE " + getName());
        // realize my BaseType
        if (type != PRIMITIVE){

            if (DataType.DEBUG) System.out.println("");

            if (myBaseTypeRef != null){

                if (DataType.DEBUG) System.out.println("....> realize basetype of " + getName() + " : " + myBaseTypeRef.getName());
                try {
                    myBaseTypeRef.realize(s);
                    if (DataType.DEBUG) System.out.println("....> realize Inheritance of " + getName());
                    realizeInheritance(s);
                    if (DataType.DEBUG) System.out.println("....> check facets of " + getName());
                    if (((DataType) myBaseTypeRef.getTarget()).isAList()) setAsAList();
                    if (((DataType) myBaseTypeRef.getTarget()).isAUnion()) setAsUnion();
                    checkPossibleFacets(s);
                }
                catch(SchemaException se){
                    s.getErrorHandler().schemaError(new SchemaException(this,se.getMessage()));
                }
            } else {
                s.getErrorHandler().schemaError(new SchemaException(this,"Base Type not defined for " + getName()));
            }
        } else {
            if (DataType.DEBUG) System.out.println(" : primitive ");
            inheritedFacets = currentFacets;
        }
        if (DataType.DEBUG) System.out.println(this);
        isCoherent = true;
    }

    // verify validity of the parameter value
    public boolean checkValue(String value) throws SchemaException {
        if (inheritedFacets==null)
            throw new DataTypeException(this,"DataType should be realized before being used");

        Iterator i = inheritedFacets.values().iterator();
        while(i.hasNext()){
            try { 
                Facet f=(Facet)i.next();
                f.checkValue(value); 
            }
            catch (SchemaException se){ return false; }
        }
        return true;
    }

    // implementation of RAW schema
    public String getRawPrefix(){
        if (isAReference()){
            return getRefDataType().getRawPrefix();
        }
        return super.getRawPrefix();
    }
 
    void writeRawSchemaWithName(AnonymousTypeRawSchemaConstructor atrsc, String name)
        throws SchemaException{
        if (!isCoherent) 
            throw new SchemaException(this,
                                      SchemaMessages.getMessages("inconsistency"));
        atrsc.write("<" + getRawPrefix()+name + ">");
        writeAbstract(atrsc);
        writePrimitiveType(atrsc);
        writeXSIType(atrsc);
        writeContentToRawSchema(atrsc);
    }

    public void writeRawSchema(AnonymousTypeRawSchemaConstructor atrsc) throws SchemaException{
        writeRawSchemaWithName(atrsc,getName());
    }    

    public void writePrimitiveType(AnonymousTypeRawSchemaConstructor atrsc) throws SchemaException {
        if (myPrimitiveDataType != null && !isAList() && !isAUnion())
                atrsc.write(myPrimitiveDataType.getRawName() );
         else if (isAList())
            atrsc.write("list" );
         else if (isAUnion())
            atrsc.write("union" );
    }


    public void writeOnlyEnumerationToRawSchema(AnonymousTypeRawSchemaConstructor atrsc) throws SchemaException{
        Iterator i = inheritedFacets.values().iterator();
        while(i.hasNext()){
            Facet f=(Facet)i.next();
            if (!(f instanceof Pattern)){
                if (f instanceof EnumerationFacet){
                    Collection c = ((EnumerationFacet)f).getValues();
                    Iterator vi = c.iterator();
                    while(vi.hasNext()){
                        atrsc.write(" \"" + vi.next()+"\"");
                    }
                }
            }
        }
    }

    public DataType getListItem(){
        if (!isAList()) return null;
        return ((DataType) myPrimitiveDataType.getTarget()).getListItem();
        }

    public List getUnionTypes(){
        if (!isAUnion()) return null;
        return ((DataType) myPrimitiveDataType.getTarget()).getUnionTypes();
        }

    void writeFacetToRawSchema(AnonymousTypeRawSchemaConstructor atrsc) throws SchemaException{
        Iterator i = inheritedFacets.values().iterator();
        String s =" ";
        while(i.hasNext()){
            Facet f=(Facet)i.next();
            //System.out.println("facet = " + f);
            if (!(f instanceof Pattern)){
                s += " (" + f.getName();
                if (f instanceof EnumerationFacet){
                    Collection c = ((EnumerationFacet)f).getValues();
                    Iterator vi = c.iterator();
                    while(vi.hasNext()){
                        s += " \"" + vi.next()+"\"";
                    }
                }
                else
                    s += " " + ((SimpleFacet)f).getValue();

                s += ")";
            }
        }
        atrsc.write(s);
    }

    void writeContentToRawSchema(AnonymousTypeRawSchemaConstructor atrsc) throws SchemaException{
        if (myPrimitiveDataType != null)
            if (isAList){
                ((DataType) myPrimitiveDataType.getTarget()).writeContentToRawSchema(atrsc);
                atrsc.write(writeContentToRawSchemaForList());
            } else {
                //                atrsc.write(myPrimitiveDataType.getRawName() );
                //                if (Schema.RWS_NON_REALIZE) writeXSIType(atrsc);
                writeFacetToRawSchema(atrsc);
            }
        else 
            atrsc.write(getRawName()) ;
    }  


    void writeXSIType(AnonymousTypeRawSchemaConstructor atrsc) throws SchemaException {
        if (!Schema.RWS_NON_REALIZE) return;
        if (type!=PRIMITIVE && getSuperType()!=null){
            atrsc.write(" (RESTRICTION ");
            atrsc.write(getSuperType().getRawName());      
            atrsc.write(")");
        }
    }


    String writeContentToRawSchemaForList() throws SchemaException{
        String s ="";
        Iterator i = currentFacets.values().iterator();
        while(i.hasNext()){
            Facet f=(Facet)i.next();
            //System.out.println("facet = " + f);
            if (!(f instanceof Pattern)){
                    s += " (" + f.getName();
                    if (f instanceof EnumerationFacet){
                        Collection c = ((EnumerationFacet)f).getValues();
                        Iterator vi = c.iterator();
                        while(vi.hasNext()){
                            s += " \"" + vi.next()+"\"";
                        }
                    }
                    else
                        s += " " + ((SimpleFacet)f).getValue();
                    
                    s += ")";
                }
        }
        return s;
    }  

    void writeAbstract(AnonymousTypeRawSchemaConstructor atrsc) throws SchemaException {
        if (isAbstract())
            atrsc.write(" (ABSTRACT) ");
    }

         
 
    //

    // ================================================================================
    //                     EASIER ACCESSING                     

    /**
     * Set the value of baseType.
     * @return Value of baseType.
     */
    public void setBase(String s) {
        myBaseTypeRef = new DataTypeRef(s);
    }// defaultValue;}


    static void generateBuiltInDataTypes(Schema aSchema){
        BasicDataTypeGenerator.generateBuiltInDataTypes(aSchema);
    }




   
}

////////////////////////////////////////////////////////////////////////////////////////
//                            FACETS
////////////////////////////////////////////////////////////////////////////////////////


// abstract class for every kind of facets
abstract class Facet implements NamedObject {

   static final public short DEFAULT = 0; 
   static final public short FINAL   = 1; 

   private short facetType = DEFAULT;

    void setType(short type) { facetType = type; }
   public short getType()           { return facetType; }
   
   //
   
   abstract Facet giveMergedFacet(Facet superf) throws SchemaException;
   abstract void  checkValue(String value) throws SchemaException ;

   //
       
   
   //
   // static methods for mapping classes/instances/key in map
   static final Facet getFacetImpl(String name) throws SchemaException {
      
      // handle special instantiation cases
      // special case when a single class handle many facet
      if (name.equals("minExclusive"))
         return new MinValueFacet(true);
      else if (name.equals("minInclusive"))
         return new MinValueFacet(false);
      if ( name.equals("maxExclusive"))
         return new MaxValueFacet(true);
      else if(name.equals("maxInclusive"))
         return new MaxValueFacet(false);
      else if(name.equals("enumeration"))
         return new EnumerationFacet();

      // default case :  name = class
      try{
         // get the class name
         String classname = getFacetImplClassName(name);
         if (DataType.DEBUG) System.out.println("facet name = " + classname);
         return (Facet)Class.forName(classname).newInstance();
      } 
      catch (ClassNotFoundException cnfe){
         throw new SchemaException(" Facet " + name + " is not defined for this datatype" );
      } 
      catch (Exception e){
         System.out.println("exception : " + e.getMessage());
         e.printStackTrace();
      }
      return null;
   }

   // used as key for facet in facetmap
   static final String getFacetImplClassName(String facetName) {
      String classname = facetName;
      // special case when a single class handle many facet
      if (classname.equals("minExclusive"))
         classname="MinValueFacet";
      else if (classname.equals("minInclusive"))
         classname="MinValueFacet";
      if (classname.equals("maxExclusive"))
         classname="MaxValueFacet";
      else if(classname.equals("maxInclusive"))
         classname="MaxValueFacet";

      classname = "com.expway.schema.instance." + classname.substring(0,1).toUpperCase() + classname.substring(1);
      return classname;
   }
   

}

// not yet implemented
abstract class FundamentalFacet extends Facet {}

// for other facets
abstract class NonFundamentalFacet extends Facet {}

// for enumeration only but nobody knows !
abstract class CompositeFacet extends NonFundamentalFacet {
   abstract public void setValue(Collection c) throws SchemaException;
}

// for every facet whose value is a string
abstract class SimpleFacet extends NonFundamentalFacet {

   private String value;
   private RE regexp = BasicRegexp.EVERYTHING_RE;
   private String regexp_str = BasicRegexp.EVERYTHING;

   // this regexp is for checking the correctness of the 
   // value of the facet (not the value of the datatype)
   final void    setRegexp(String r) {
      try {regexp = new RE(r);regexp_str=r;}
      catch (REException re){
            if (DataType.DEBUG) System.out.println("Exception : " + re.getMessage());
            re.printStackTrace();
         }
      }
   final boolean checkFacetSyntax()  { return regexp.isMatch(value); }
   
   public void   setValue(String v) throws SchemaException {
      if (!regexp.isMatch(v))
         throw new SchemaException("Syntax error in facet " + getName() + " : " + v + " isn't correct");
      value=v;
   }

   public String getValue(){return value;}
       
}

abstract class BoundFacet extends SimpleFacet {
   public BoundFacet(){
      setRegexp(BasicRegexp.REAL);
   }
}

// ENUMERATION

class EnumerationFacet extends CompositeFacet {
   
   private Collection possibleValues;

   public String getName(){return "enumeration";}
   
   public void setValue(Collection c) throws SchemaException {
      possibleValues = c;
   }
   
   public Collection getValues(){
      return possibleValues;
   }
   
   //

   Facet giveMergedFacet(Facet superf) throws SchemaException {
      EnumerationFacet ef=(EnumerationFacet)superf;
      if (ef.possibleValues.containsAll(possibleValues))
         return this;
      else
         throw new DataTypeException("Enumeration values can't be added for a subtype");
   }

   void checkValue(String v) throws SchemaException {
      if (possibleValues.contains(v))
         return;
      else
         throw new DataTypeException("Value is not a possible value of the enumeration facet");
   }
   
}

// MINLENGTH

class MinLength extends SimpleFacet {
   
   public MinLength(){
      setRegexp(BasicRegexp.POSITIVE_INTEGER);
   }

   public String getName(){return "minLength";}
      
   void checkValue(String v) throws SchemaException {
      try {
         if (v.length() > Integer.parseInt(getValue()))
            throw new DataTypeException("Length is not correct and should be greater than " + getValue());
      } catch(NumberFormatException nfe){
         throw new DataTypeException("Impossible to parse the value of the " + getName() + " facet");
      }
   }

   Facet giveMergedFacet(Facet superf) throws SchemaException {
      MinLength sf = (MinLength)superf;
      try {
         if (Integer.parseInt(sf.getValue()) > Integer.parseInt(getValue()))
            throw new DataTypeException("Impossible to decrease the minLength of a sub datatype");
         return this;
      } catch(NumberFormatException nfe){
         throw new DataTypeException("Impossible to parse the value of the " + getName() + " facet");
      }
   }
}

// MAXLENGTH

class MaxLength extends SimpleFacet {

   public MaxLength(){
      setRegexp(BasicRegexp.POSITIVE_INTEGER);
   }

   public String getName(){return "maxLength";}
      
   void checkValue(String v) throws SchemaException {
      try {
         if (v.length() > Integer.parseInt(getValue()))
            throw new DataTypeException("Length is not correct and should be lower than " + getValue());
      } catch(NumberFormatException nfe){
         throw new DataTypeException("Impossible to parse the value of the " + getName() + " facet");
      }
   }

   Facet giveMergedFacet(Facet superf) throws SchemaException {
      MaxLength sf = (MaxLength)superf;
      try {
         if (Integer.parseInt(sf.getValue()) < Integer.parseInt(getValue()))
            throw new DataTypeException("Impossible to increase the maxLength of a sub datatype");
         return this;
      } catch(NumberFormatException nfe){
         throw new DataTypeException("Impossible to parse the value of the " + getName() + " facet");
      }
   }
}

//ENCODING
class Encoding extends SimpleFacet {

   public Encoding(){
      setRegexp(BasicRegexp.HEXORBASE64);
   }

   public String getName(){
      return "encoding";
   }

   Facet giveMergedFacet(Facet superf) throws SchemaException {
      Encoding e = (Encoding)superf;
      if (!e.getValue().equals(getValue()))
         throw new DataTypeException("Impossible to modify the encoding of a sub datatype");
      return this;
   }

   void checkValue(String v) throws SchemaException {}
}
//WHITESPACE
class WhiteSpace extends SimpleFacet {

   public WhiteSpace(){
      setRegexp(BasicRegexp.WHITESPACE);
   }

   public String getName(){
      return "whiteSpace";
   }

   Facet giveMergedFacet(Facet superf) throws SchemaException {
      WhiteSpace e = (WhiteSpace)superf;
      if (e.getValue().equals("collapse"))
          if (getValue().equals("replace")||getValue().equals("preserve"))
              throw new DataTypeException("whiteSpace invalide restriction");
      if (e.getValue().equals("replace"))
          if (getValue().equals("preserve"))
              throw new DataTypeException("whiteSpace invalide restriction");
      return this;
   }

   void checkValue(String v) throws SchemaException {}
}

//PERIOD
// TODO
class Period extends SimpleFacet {

   public Period(){
      setRegexp(BasicRegexp.EVERYTHING);
   }

   public String getName(){
      return "period";
   }

   Facet giveMergedFacet(Facet superf) throws SchemaException {
      return this;
   }

   void checkValue(String v) throws SchemaException {}
}


// LENGTH
class Length extends SimpleFacet {

   public Length(){
      setRegexp(BasicRegexp.POSITIVE_INTEGER);
   }

   public String getName(){
      return "length";
   }
   
   void checkValue(String v) throws SchemaException {
      try {
         if (v.length() != Integer.parseInt(getValue()))
            throw new DataTypeException("Length is not correct and should be equal to " + getValue());
      } catch(NumberFormatException nfe){
         throw new DataTypeException("Impossible to parse the value of the " + getName() + " facet");
      }
   }

   Facet giveMergedFacet(Facet superf) throws SchemaException {
      Length sf = (Length)superf;
      try {
         if (Integer.parseInt(sf.getValue()) != Integer.parseInt(getValue()))
            throw new DataTypeException("Impossible to modify the length of sub datatype");
         return this;
      } catch(NumberFormatException nfe){
         throw new DataTypeException("Impossible to parse the value of the " + getName() + " facet");
      }
   }
}

// MINVALUE
class MinValueFacet extends BoundFacet {

   static final String minInclusive = "minInclusive";
   static final String minExclusive = "minExclusive";

   private boolean exclusive;


   public String getName(){
      if (exclusive)
         return minExclusive;
      else 
         return minInclusive;
   }
   
   public MinValueFacet(boolean exc){exclusive=exc;}
   
   Facet giveMergedFacet(Facet superf) throws SchemaException {
      if (DataType.DEBUG) System.out.println("try to merge facets f = " + this 
                         + " and superf = " + superf);
      MinValueFacet msuperf = (MinValueFacet)superf;
      try{
         if (
             // new min < super min
             (Double.parseDouble(msuperf.getValue()) > Double.parseDouble(getValue())) 
             || 
             // new min == super min && exclu/inclu 
             (Double.parseDouble(msuperf.getValue()) == Double.parseDouble(getValue()) 
              && msuperf.exclusive && !exclusive)
             )
            throw new DataTypeException("Impossible to decrease the lower bound of a sub datatype");
         else 
            return this;
      } catch (NumberFormatException nfe){
         throw new DataTypeException("Impossible to parse the value of the " + getName() + " facet");
      }
   }
   
   void checkValue(String v) throws SchemaException {
      try {
         double doubleValue = Double.parseDouble(v);
         double minValue = Double.parseDouble(getValue());
         if ((doubleValue < minValue) || ((doubleValue == minValue) && exclusive))
            throw new DataTypeException("Value is lower than the minimum possible value");
      }
      catch (NumberFormatException nfe){
         throw new DataTypeException("Facet " + getName() + " can't parse the value facet");
      }
   }
}

// MAXVALUE

class MaxValueFacet extends BoundFacet {

   static final String maxInclusive = "maxInclusive";
   static final String maxExclusive = "maxExclusive";
   
   private boolean exclusive;

   public String getName(){
      if (exclusive)
         return maxExclusive;
      else 
         return maxInclusive;
   }
   
   public MaxValueFacet(boolean exc){exclusive=exc;}
   
   Facet giveMergedFacet(Facet superf) throws SchemaException {
      if (DataType.DEBUG) System.out.println("try to merge facets " + this + " and " + superf);
      MaxValueFacet msuperf = (MaxValueFacet)superf;
      try{
         if (
             // new max > super max
             (Double.parseDouble(msuperf.getValue()) < Double.parseDouble(getValue())) 
             || 
             // new max == super max && exclu/inclu 
             (Double.parseDouble(msuperf.getValue()) == Double.parseDouble(getValue()) &&
              msuperf.exclusive && !exclusive) 
             )
            throw new DataTypeException("Impossible to increase the upper bound of a sub datatype");
         else 
            return this;
      } catch (NumberFormatException nfe){
         throw new DataTypeException("Impossible to parse the value of the " + getName() + " facet");
      }
   }

   void checkValue(String v) throws SchemaException {
      try {
         double doubleValue = Double.parseDouble(v);
         double minValue = Double.parseDouble(getValue());
         if ((doubleValue > minValue) || ((doubleValue == minValue) && exclusive))
            throw new DataTypeException("Value is lower than the minimum possible value");
      }
      catch (NumberFormatException nfe){
         throw new DataTypeException("Facet " + getName() + " can't parse the value facet");
      }
   }
}

// PATTERN

class Pattern extends SimpleFacet {

   private RE valueRE = null;

   public String getName(){ return "pattern"; }

   Facet giveMergedFacet(Facet superf) throws SchemaException {
      return this;
   }

   public void setValue(String value) throws SchemaException {
      super.setValue(value);
      try {
         valueRE = new RE(getValue());
      } catch (REException re){
         throw new SchemaException("Invalid regexp in facet "+this.getName());
      }
   }
   
   void checkValue(String v) throws SchemaException {
      if (!valueRE.isMatch(v))
         throw new DataTypeException("Value is not lexically correct");
   }
}

// SCALE
// ##TODO

class Scale extends SimpleFacet {

   public Scale(){
      setRegexp(BasicRegexp.POSITIVE_INTEGER);
   }
   

   public String getName(){return "scale";}
   
   Facet giveMergedFacet(Facet superf) throws SchemaException {
      return this;
   }

   void checkValue(String v) throws SchemaException {}
   
}

// PRECISION
// ##TODO

class Precision extends SimpleFacet {

   public Precision(){
      setRegexp(BasicRegexp.POSITIVE_INTEGER);
   }

   public String getName() {return "precision";}
   
   Facet giveMergedFacet(Facet superf) throws SchemaException {
      return this;
   }
   
   void checkValue(String v) throws SchemaException {}
}

class Base extends SimpleFacet {

   public Base(){
       setRegexp(BasicRegexp.EVERYTHING);
   }

   public String getName() {return "base";}
   
   Facet giveMergedFacet(Facet superf) throws SchemaException {
       return this;
   }
   
   void checkValue(String v) throws SchemaException {}
}


//////////////////////////////////////////////////////////////

// facets are indexed by their class name
// a inverted index gives class names for particular MPEG7 facets

class FacetMap extends HashMap {
   
   Facet getFacet(String name){
      return (Facet)get(Facet.getFacetImplClassName(name));
   }

   Facet removeFacet(String name){
      return (Facet)remove(Facet.getFacetImplClassName(name));
   }
   
   void putFacet(Facet fi){ 
      put(Facet.getFacetImplClassName(fi.getName()),fi); 
   }
   
   boolean containsFacet(String name){
      /*
        if (DataType.DEBUG) System.out.print("contains " + name + " ? ");
        if (DataType.DEBUG) System.out.println(containsKey(name)?"yes":"no");
      */
      return containsKey(Facet.getFacetImplClassName(name));
   }

       
}

//////////////////////////////////////////////////////////////
// INIT OF PRIMITIVES DATATYPES

class BasicRegexp {
   final static public String EVERYTHING = ".*";
   final static public String INTEGER = "(\\+|\\-)?\\d+(E(\\+)?\\d+)?";
   final static public String POSITIVE_INTEGER = "\\d+(E(\\+)?\\d+)?";
   final static public String NEGATIVE_INTEGER = "\\-\\d+(E(\\+)?\\d+)?";
   final static public String REAL = "(\\+|\\-)?\\d+(\\.\\d+)?";
   final static public String RFC1766 = ".*"; //TODO
   final static public String ISO8601 = ".*"; //TODO
   final static public String LEFTTRUNCATED_ISO8601 = ".*"; //TODO
   final static public String URI = "((http|ftp)://|mailto:).[^ /]*";
   final static public String HEXORBASE64 = "(hex|base64)";
   final static public String WHITESPACE = "(replace|preserve|collapse)";

   static public RE EVERYTHING_RE = null;
   static public RE INTEGER_RE = null;
   static public RE REAL_RE = null;
   static public RE TIME_RE = null;
   static public RE URI_RE = null;

   static {
      try{
         EVERYTHING_RE = new RE(EVERYTHING);
         INTEGER_RE = new RE(INTEGER);
         REAL_RE = new RE(REAL);
         RE TIME_RE = new RE(ISO8601);
         RE URI_RE = new RE(URI);
      } catch(REException rex){
         System.out.println("error : " + rex.getMessage());
         rex.printStackTrace();
      }
   }
   
}


class BasicDataTypeGenerator extends BasicRegexp {

    

   static final String[][] compositeFacetClasses = 
      {
         {"enumeration"} //TODO
      };
   

   // possible facets for every datatype
   static final String[][] primitiveDatatypes = 
      {
          {"anySimpleType",
          "pattern","whiteSpace", "minExclusive", "maxExclusive","minInclusive","maxInclusive","enumeration","scale","precision"},

         
         { "list",             
           "minLength","maxLength", "length", "base"},

         { "string",             
           "pattern", "whiteSpace", "minLength","maxLength", "length", "enumeration","minExclusive", "maxExclusive","minInclusive","maxInclusive"},
         // in XML Schema 17 Dec : minInclusive, etc...

         { "token",             
           "pattern", "whiteSpace", "minLength","maxLength", "length", "enumeration"},

         { "base64Binary",             
           "pattern", "whiteSpace", "minLength","maxLength", "length", "enumeration"},
         
         { "boolean",
           "pattern" },
         
         { "float",         
           "pattern", "minExclusive", "whiteSpace", "maxExclusive","minInclusive","maxInclusive","enumeration"},
         // in XML Schema 17 dec : possible values are -INF INF NAN
         // base 32
         
         { "double",         
           "pattern", "whiteSpace","minExclusive", "maxExclusive","minInclusive","maxInclusive","enumeration"},
          // in XML Schema 17 dec : possible values are -INF INF NAN
         // base 64
        
         { "decimal",
          "pattern", "whiteSpace", "minExclusive", "maxExclusive","minInclusive","maxInclusive","enumeration","scale","precision","fractionDigits"},

         { "integer",
           "pattern", "whiteSpace", "minExclusive", "maxExclusive","minInclusive","maxInclusive","enumeration"},
         // todo : normalement ce n'est pas un primitive mais c'est plus simple comme ca

         { "timeDuration" , 
           "pattern","enumeration" },
         // todo

         { "recurringDuration" , 
           "pattern", "enumeration","period" },
         // todo

         {  "binary",                
            "pattern", "minLength", "maxLength", "length","enumeration","encoding"},

         { "uriReference" , 
           "pattern","enumeration","minLength", "maxLength", "length", "whiteSpace"},
         
          // conforme a la recommendation
         { "anyURI" , 
           "pattern","enumeration","minLength", "maxLength", "length", "whiteSpace"},

         { "dateTime" , 
           "pattern","enumeration","whiteSpace", "minExclusive", "maxExclusive","minInclusive","maxInclusive" },

         { "duration" , 
           "pattern","enumeration","whiteSpace", "minExclusive", "maxExclusive","minInclusive","maxInclusive" },


         { "hexBinary" , 
           "pattern","enumeration","minLength", "maxLength", "length", "whiteSpace"},

          // For Joerg
//            { "basicTimePointTypeHook",             
//             "pattern", "whiteSpace", "minLength","maxLength", "length", "enumeration","minExclusive", "maxExclusive","minInclusive","maxInclusive"},
          
//            { "basicDurationTypeHook",             
//             "pattern", "whiteSpace", "minLength","maxLength", "length", "enumeration","minExclusive", "maxExclusive","minInclusive","maxInclusive"},

      };


   static final String[][] generatedDatatypes = {
      
      { "date"                 , "recurringDuration"   },
      { "time"                 , "recurringDuration"   },

      { "language"             , "string"   },
      
      //{ "integer"              , "decimal"  }, // non conforme a XML Schema mais pratique pour nous
      
      {"nonNegativeInteger"   , "integer"  },
      {"nonPositiveInteger"   , "integer"  },
      {"int"   , "integer"  },
      {"positiveInteger"      , "nonNegativeInteger"  },
      {"negativeInteger"      , "nonPositiveInteger"  },
      {"unsignedLong"         , "nonNegativeInteger"  },
      {"unsignedInt"          , "unsignedLong"},
      {"unsignedShort"          , "unsignedInt"},
      {"unsignedByte"          , "unsignedShort"},
      // TODO : PAS CONFORME A XML SCHEMA MAIS CONSIDERE COMME UN STRING : UTILE !!
      { "ID"                   , "string"   },
      { "IDREF"                , "string"   },
      { "hexBinary"            , "string"   },
      
      { "IDREFS"               , "string"   },
      //        { "NMTOKEN"              , "string"   },
      { "Name"                 , "string"   },
      //        { "NCName"               , "Name"     },
      //        { "language"             , "string"   }
      //TODO add by cedric a verifier
      { "QName"                 , "string"   },
      { "NMTOKENS"                 , "string"   },
      { "NMTOKEN"                 , "string"   },
      { "NCName"                 , "string"   },
      { "token"                 , "string"   },
      { "base64Binary"                 , "string"   },


      /* not yet done
        , NMTOKENS
        IDREFS, ENTITY, ENTITIES
        NOTATION (facet = enumeration)
      */

      { "normalizedString"                 , "string"   },

   };
   
   // add some simple facets to datatypes 
   static final String[][] defaultFacets =  {

       // zarebi
         {"list"               , "base"      , "string"         , "DEFAULT"},

         // language
         //         {"language"           , "enumeration"      , "JP,AB,OM,AA,AF,SQ,AM,AR,HY,AS,AY,AZ,BA,EU,BN,DZ,BH,BI,BR,BG,MY,BE,KM,CA,ZH,CO,HR,CS,DA,NL,EN,EO,ET,FO,FJ,FI,FR,FY,GL,KA,DE,EL,KL,GN,GU,HA,HE,HI,HU,IS,ID,IA,IE,IU,IK,GA,IT,JA,JV,KN,KS,KK,RW,KY,RN,KO,KU,LO,LA,LV,LN,LT,MK,MG,MS,ML,MT,MI,MR,MO,MN,NA,NE,NO,OC,OR,PS,FA,PL,PT,PA,QU,RM,RO,RU,SM,SG,SA,GD,SR,SH,ST,TN,SN,SD,SI,SS,SK,SL,SO,ES,SU,SW,SV,TL,TG,TA,TT,TE,TH,BO,TI,TO,TS,TR,TK,TW,UG,UK,UR,UZ,VI,VO,CY,WO,XH,YI,YO,ZA,ZU" , "FINAL"},
  

         // normal
         {"binary"               , "pattern"      , "(0|1)*"         , "FINAL"},
         {"boolean"              , "pattern"      , "(true|false)"   , "FINAL"},
         {"float"                , "pattern"      , REAL             , "DEFAULT"},
         {"double"               , "pattern"      , REAL             , "DEFAULT"},

         {"integer"              , "pattern"      , INTEGER          , "DEFAULT"},
         //{"integer"              , "scale"        , "0"          , "DEFAULT"},

         {"nonNegativeInteger" , "minInclusive" , "0"              , "DEFAULT" },
         {"nonNegativeInteger" , "pattern"      , POSITIVE_INTEGER , "FINAL"},

         {"nonPositiveInteger" , "maxInclusive" , "0"              , "DEFAULT" },
         {"nonPositiveInteger" , "pattern"      , NEGATIVE_INTEGER , "FINAL"},

         {"positiveInteger"     , "minExclusive" , "0"              , "DEFAULT" },
         {"negativeInteger"     , "maxExclusive" , "0"              , "DEFAULT" },
         {"unsignedLong"        , "maxExclusive" , "18446744073709551615"              , "DEFAULT" },
         {"unsignedInt"         , "maxExclusive" , "4294967295"     , "DEFAULT" },
         {"unsignedShort"       , "maxExclusive" , "65535"          , "DEFAULT" },
         {"unsignedByte"        , "maxExclusive" , "255"            , "DEFAULT" },
         {"int"        , "maxExclusive" , "2147483647"            , "DEFAULT" },
         {"int"     , "minExclusive" , "-2147483648"              , "DEFAULT" },


         //         {"anyURI"        , "pattern"      , URI              , "FINAL" },
            
         // todo
         {"timeDuration"         , "pattern"      , ISO8601               , "FINAL"},
         {"recurringDuration"    , "pattern"      , LEFTTRUNCATED_ISO8601 , "FINAL"},
         //{"language"             , "pattern"      , RFC1766               , "FINAL"}


      };
   
   static final void generateBuiltInDataTypes(Schema aSchema){

      String s[][];

      // creation of primitive datatypes
      s = primitiveDatatypes;

      for (int i=0;i<s.length;i++){
         DataType dth = new DataType(s[i][0],null,DataType.PRIMITIVE);
         for (int j=1;j<s[i].length;j++){
            if (DataType.DEBUG) System.out.println(" ajoute possible facet : " + s[i][j]);
            dth.addPossibleFacet(s[i][j]);
            if (DataType.DEBUG) System.out.println(dth);
         }
         try { aSchema.addType(dth); }
         catch(SchemaException se){
            System.out.println("Duplicate datatype while primitive datatypes generation");
         }
      }

      // creation of generated datatypes
      s= generatedDatatypes;
      for (int i=0;i<s.length;i++){
         DataType dth = new DataType(s[i][0],SchemaSymbols.URI_SCHEMAFORSCHEMA+":"+s[i][1],DataType.GENERATED);
         for (int j=2;j<s[i].length;j++)
            dth.addPossibleFacet(s[i][j]);
         try{ aSchema.addType(dth);}
         catch(SchemaException se){
            System.out.println("Duplicate datatype ("+dth+") while generated datatypes generation");
         }
      }

      // add singleValued facets
      s = defaultFacets;
      for (int i=0;i<s.length;i++){
          //System.out.println("getDatatype of " + s[i][0]);
         DataType dth = (DataType)aSchema.getType(s[i][0]);
         if (DataType.DEBUG)
            System.out.println("looking for the datatype : " + s[i][0]);
         for (int j=1;j<s[i].length;j+=3)
            {
               try {
                  //System.out.println("getFacetImpl of " + s[i][j]);
                   if (s[i][j].equals("enumeration")){
                       StringTokenizer stn = new StringTokenizer(s[i][j+1],",");
                       Collection c = new TreeSet();
                       while(stn.hasMoreTokens()) {
                           c.add(stn.nextToken());
                       }
                       dth.addEnumerationFacet(c);
                   } else {
                       SimpleFacet f = (SimpleFacet)Facet.getFacetImpl(s[i][j]);
                       // set the value
                       String value = s[i][j+1];
                       f.setValue(s[i][j+1]);
                       // select the inheritance behaviour
                       String type = s[i][j+2];
                       if (type.equals("FINAL"))
                           f.setType(Facet.FINAL);
                       else
                           f.setType(Facet.DEFAULT);
                       // finally we add it
                       dth.addFacet(f);
                   }
               }
               catch (Exception e){
                  System.out.println("exception while generating datatypes");
                  System.out.println(e.getMessage());
                  e.printStackTrace();
                  System.exit(0);
               }
               if (DataType.DEBUG) System.out.println("Datatype added " + dth);
            }
      }

      //
      
   }
}


