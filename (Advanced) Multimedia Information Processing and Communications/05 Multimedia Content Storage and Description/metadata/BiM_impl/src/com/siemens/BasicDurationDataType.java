package com.siemens;

import com.expway.tools.expression.SimpleTypeDefinition;
import com.expway.tools.io.BitToBitDataInputStream;
import com.expway.tools.io.ChunkWriter;
import java.io.IOException;
import java.io.Writer;

public class BasicDurationDataType extends SimpleTypeDefinition {

    long facet=0;
    long represent;

    public BasicDurationDataType(String name,String primitive,long repr){
        super(name,primitive);
        represent=repr;
    }

    public void internalSetFacetValue(String name,String value){}

    public void encodeItInto(String value, ChunkWriter cw){	
        String pattern=new String("");
        pattern="";
        if((represent&512)>0)
            pattern+="\\-?";
        pattern+="P";
        if ((represent&256)>0)
            {
                pattern+="(\\d+D)?";
            }
        if ((represent&4)>0)
            {
                pattern+="(T";
                if ((represent&128)>0)
                    {
                        pattern+="(\\d+H)?";
                    }
                if ((represent&64)>0)
                    {
                        pattern+="(\\d+M)?";
                    }
                if ((represent&32)>0)
                    {
                        pattern+="(\\d+S)?";
                    }
                if ((represent&16)>0)
                    {
                        pattern+="(\\d+N)?";
                    }
                if ((represent&8)>0)
                    {
                        pattern+="(\\d{2}f)?";
                    }
                pattern+=")?";
            }
        if ((represent&2)>0)
            {
                pattern+="(\\d+F)?";
            }

        if ((represent&1)>0)
            pattern+="((\\-|\\+)\\d{2}:\\d{2}Z)?";
			
        //Coding of Representation Flags 
		
        int repFlags=0;
        int nrOfFlags=0;
        int strIndex=0;	
        long days=0;
        long hours=0;
        long minutes=0;
        long seconds=0;
        long fractions=0;
        long df=0;
        long nrOfFract=0;
        int expr_lngth=value.length();

        TimeStreamIO bufferStream=new TimeStreamIO(256);

        int chk=0;
		
        if ((represent&512)>0) 
            { 
                nrOfFlags++;
                if ((strIndex<expr_lngth) && value.charAt(strIndex)=='-')
                    {
                        repFlags+=1;
                        strIndex++;
                    }
            }
        strIndex++; //P within the duration expression
		
        if ((represent&256)>0) 
            { 
                nrOfFlags++;
                chk=strIndex;
                while((strIndex<expr_lngth) && value.charAt(strIndex)>='0' && value.charAt(strIndex)<='9')
                    strIndex++;
                if (value.charAt(strIndex)=='D')
                    {
                        repFlags<<=1;
                        repFlags+=1;
                        strIndex++;
                        days=Integer.parseInt(value.substring(chk,strIndex-1));
                    }
                else
                    {
                        strIndex=chk;
                        repFlags<<=1;
                    }
            }
		
        if ((represent&4)>0 && (strIndex<expr_lngth) && (value.charAt(strIndex)=='T'))
            {
                strIndex++;
                repFlags<<=1;
                repFlags++;
                nrOfFlags++;
                if ((represent&128)>0) 
                    { 
                        nrOfFlags++;
                        chk=strIndex;
                        while((strIndex<expr_lngth) && value.charAt(strIndex)>='0' && value.charAt(strIndex)<='9')
                            strIndex++;
                        if ((strIndex<expr_lngth) && value.charAt(strIndex)=='H')
                            {
                                repFlags<<=1;
                                repFlags+=1;
                                strIndex++;
                                hours=Integer.parseInt(value.substring(chk,strIndex-1));
                            }
                        else
                            {
                                strIndex=chk;
                                repFlags<<=1;
                            }
                    }
                if ((represent&64)>0) 
                    { 
                        nrOfFlags++;
                        chk=strIndex;
                        while((strIndex<expr_lngth) && value.charAt(strIndex)>='0' && value.charAt(strIndex)<='9')
                            strIndex++;
                        if ((strIndex<expr_lngth) && value.charAt(strIndex)=='M')
                            {
                                repFlags<<=1;
                                repFlags+=1;
                                strIndex++;
                                minutes=Integer.parseInt(value.substring(chk,strIndex-1));
                            }
                        else
                            {
                                strIndex=chk;
                                repFlags<<=1;
                            }
                    }
                if ((represent&32)>0) 
                    { 
                        nrOfFlags++;
                        chk=strIndex;
                        while((strIndex<expr_lngth) && value.charAt(strIndex)>='0' && value.charAt(strIndex)<='9')
                            strIndex++;
                        if ((strIndex<expr_lngth) && value.charAt(strIndex)=='S')
                            {
                                repFlags<<=1;
                                repFlags+=1;
                                strIndex++;
                                seconds=Integer.parseInt(value.substring(chk,strIndex-1));
                            }
                        else
                            {
                                strIndex=chk;
                                repFlags<<=1;
                            }
                    }
                if ((represent&16)>0) 
                    { 
                        nrOfFlags++;
                        chk=strIndex;
                        while((strIndex<expr_lngth) && value.charAt(strIndex)>='0' && value.charAt(strIndex)<='9')
                            strIndex++;
                        if ((strIndex<expr_lngth) && value.charAt(strIndex)=='N')
                            {
                                repFlags<<=1;
                                repFlags+=1;
                                strIndex++;
                                fractions=Integer.parseInt(value.substring(chk,strIndex-1));
                            }
                        else
                            {
                                strIndex=chk;
                                repFlags<<=1;
                            }
                    }
                if ((represent&8)>0) 
                    { 
                        nrOfFlags++;
                        chk=strIndex;
                        while((strIndex<expr_lngth) && value.charAt(strIndex)>='0' && value.charAt(strIndex)<='9')
                            strIndex++;
                        if ((strIndex<expr_lngth) && value.charAt(strIndex)=='f')
                            {
                                repFlags<<=1;
                                repFlags+=1;
                                strIndex++;
                                df=Integer.parseInt(value.substring(chk,strIndex-1));
                            }
                        else
                            {
                                strIndex=chk;
                                repFlags<<=1;
                            }
                    }			
            }
        else if((represent&4)>0)
            {
                nrOfFlags++;
                repFlags<<=1;
            }
		
        if ((represent&2)>0) 
            { 
                nrOfFlags++;
                chk=strIndex;
                while((strIndex<expr_lngth) && value.charAt(strIndex)>='0' && value.charAt(strIndex)<='9')
                    strIndex++;
                if ((strIndex<expr_lngth) && value.charAt(strIndex)=='F')
                    {
                        repFlags<<=1;
                        repFlags+=1;
                        strIndex++;
                        nrOfFract=Integer.parseInt(value.substring(chk,strIndex-1));
                    }
                else
                    {
                        strIndex=chk;
                        repFlags<<=1;
                    }
            }		

        //only first value is encoded with variable length
        if ((fractions>nrOfFract)&&(represent>31))
            {
                seconds+=fractions/nrOfFract;
                fractions=fractions%nrOfFract;
            }
        if ((seconds>59)&&(represent>63))
            {
                minutes+=seconds/60;
                seconds=seconds%60;
            }
        if ((minutes>59)&&(represent>127))
            {
                hours=minutes/60;
                minutes=minutes%60;
            }
        if ((hours>23)&&(represent>255))
            {
                days=hours/24;
                hours=hours%24;
            }
		
        int fFlag=0;
        int dfFlag=0;
        int first=1;
        int bitpattern=1<<nrOfFlags;
        bitpattern>>=2;
        if ((represent&256)>0)
            {
                if((repFlags&bitpattern)>0) 
                    { 
                        first=0;
                        bufferStream.writeVLInt(days);
				
                    }
                bitpattern>>=1;
            }
        if ((represent&4)>0)
            {
                if((repFlags&bitpattern)>0) 
                    {
                        bitpattern>>=1; 
                        if ((represent&128)>0)
                            {
                                if((repFlags&bitpattern)>0) 
                                    { 
                                        if (first!=0)
                                            {
                                                first=0;
                                                bufferStream.writeVLInt(hours);
                                            }
                                        else
                                            {
                                                bufferStream.writeRangeInt(hours,0,23);
                                            }
                                    }
                                bitpattern>>=1;
                            }
				
                        if ((represent&64)>0)
                            {
                                if((repFlags&bitpattern)>0) 
                                    { 
                                        if (first!=0)
                                            {
                                                first=0;
                                                bufferStream.writeVLInt(minutes);
                                            }
                                        else
                                            {
                                                bufferStream.writeRangeInt(minutes,0,59);
                                            }
                                    }
                                bitpattern>>=1;
                            }
                        if ((represent&32)>0)
                            {
                                if((repFlags&bitpattern)>0) 
                                    { 
                                        if (first!=0)
                                            {
                                                first=0;
                                                bufferStream.writeVLInt(seconds);
                                            }
                                        else
                                            {
                                                bufferStream.writeRangeInt(seconds,0,59);
                                            }
                                    }
                                bitpattern>>=1;
                            }
                        if ((represent&16)>0)
                            {
                                if((repFlags&bitpattern)>0) 
                                    { 
                                        fFlag=1;
                                    }
                                bitpattern>>=1;
                            }
                        if ((represent&8)>0)
                            {
                                if((repFlags&bitpattern)>0) 
                                    { 
                                        dfFlag=1;
                                    }
                                bitpattern>>=1;
                            }
                    }
                else
                    bitpattern>>=1;
            }
		
        if (((represent&2)>0) && ((repFlags&bitpattern)>0))
            {
                bufferStream.writeVLInt(nrOfFract);
                bitpattern>>=1;
                if (fFlag!=0)
                    {
                        if(first!=0)
                            bufferStream.writeVLInt(fractions);
                        else
                            bufferStream.writeRangeInt(fractions,0,nrOfFract-1);	
                    }
			
                if (dfFlag!=0)
                    {
                        bufferStream.writeRangeInt(df,0,99);
                    }
			
            }
		

        if((represent&1)>0)
            {
                nrOfFlags++;
                //todo: flags have to be reset above!!!
                if ((strIndex<expr_lngth) && ((value.charAt(strIndex)=='-') | (value.charAt(strIndex)=='+')))
                    {
                        repFlags<<=1;
                        repFlags++;
                        bufferStream.writeTimeZone(value.substring(strIndex,strIndex+6));
                    }
                else
                    repFlags<<=1;
            }
		

			
        cw.writeInt(repFlags,nrOfFlags);
        bufferStream.writeToCW(cw);
    }

    public void decode(BitToBitDataInputStream bis, Writer w){
        // par defaut une string
        try {

            long repFlags=0;
            String timeExpr=new String("");
		
		
            if ((represent&512)>0) 
                { 
                    repFlags+=(bis.readBoolean())?512:0;
                }
		
            if ((represent&256)>0) 
                { 
                    repFlags+=(bis.readBoolean())?256:0;
                }
					
            if ((represent&4)>0)
                {
                    repFlags+=(bis.readBoolean())?4:0;
                    if ((repFlags&4)>0)
                        {
                            if ((represent&128)>0)
                                {
                                    repFlags+=(bis.readBoolean())?128:0;
                                }
				
                            if ((represent&64)>0)
                                {
                                    repFlags+=(bis.readBoolean())?64:0;
                                }
				
                            if ((represent&32)>0)
                                {
                                    repFlags+=(bis.readBoolean())?32:0;
                                }
			
                            if ((represent&16)>0)
                                {
                                    repFlags+=(bis.readBoolean())?16:0;
                                }
			
                            if ((represent&8)>0)
                                {
                                    repFlags+=(bis.readBoolean())?8:0;
                                }
                        }

                }
		
		
		
            if((represent&2)>0)
                {
                    repFlags+=(bis.readBoolean())?2:0;
                }
			
		
            if((represent&1)>0)
                {
                    repFlags+=(bis.readBoolean())?1:0;
                }		
		
		
		
            long intValue;
            long intFract=0;
            boolean first=true;
		
            if ((repFlags&512)>0) 
                { 
                    timeExpr+="-";
                }
		
            timeExpr+="P";
					
            if ((repFlags&256)>0)
                {
                    intValue=bis.readInfiniteLong();
                    timeExpr+=intValue+"D";
                    first=false;
                }
			

            if ((repFlags&4)>0)
                {
                    timeExpr+="T";
                    if ((repFlags&128)>0)
                        {
                            if (first)
                                {
                                    intValue=bis.readInfiniteLong();
                                }
                            else
                                intValue=bis.readInt(5);
                            timeExpr+=intValue+"H";
                            first=false;
                        }
			
                    if ((repFlags&64)>0)
                        {
                            if (first)
                                {
                                    intValue=bis.readInfiniteLong();
                                }
                            else
                                intValue=bis.readInt(6);
                            timeExpr+=intValue+"M";
                            first=false;
                        }	
			
                    if ((repFlags&32)>0)
                        {
                            if (first)
                                {
                                    intValue=bis.readInfiniteLong();
                                }
                            else
                                intValue=bis.readInt(6);
                            timeExpr+=intValue+"S";
                            first=false;
                        }

                }
		
            if ((repFlags&2)>0)
                {
                    intFract=bis.readInfiniteLong();
                    intValue=intFract;
                    int nrOfBits=0;
                    while (intValue!=0)
                        {
                            nrOfBits++;
                            intValue>>=1;
                        }
	
                    if (((repFlags&16)>0) && ((repFlags&4)>0))
                        {
                            if(first)
                                intValue=bis.readInfiniteLong();
                            else
                                intValue=bis.readInt(nrOfBits);
					
                            timeExpr+=intValue+"N";
                        }
                }
            if (((repFlags&8)>0) && ((repFlags&4)>0))
                {
                    intValue=bis.readInt(7);
                    timeExpr+=((intValue<10)?"0":"")+intValue+"f";
                }
		
            if ((repFlags&2)>0)
                {
                    timeExpr+=intFract+"F";
                }
		
            if ((repFlags&1)>0)
                {
                    intValue=bis.readInt(5);
                    intValue-=15;
                    if(intValue>=0)
                        timeExpr+="+"+((intValue<10)?"0":"")+intValue;
                    else
                        timeExpr+="-"+((intValue>-10)?"0":"")+(intValue*-1);
                    intValue=bis.readInt(2)*15;
                    timeExpr+=":"+((intValue<10)?"0":"")+intValue+"Z";	
                }


		

            w.write(timeExpr);

        } catch(IOException utf){
            throw new RuntimeException("IOERROR");
        }
    }
    
}
