package com.siemens;

import java.io.IOException;
import java.io.*;
import com.expway.tools.io.ChunkWriter;

public class TimeStreamIO {

 	private byte stream[];
	private int streamPos;
	private int bytePtr=0;
	private byte bufByte;

	
	public TimeStreamIO(int maxCount)
	{
		stream = new byte[maxCount];
	}
	
	public int writeVLInt(String value)
	{
		//ToDo: takes the first numbers or have size to be used?
		long intValue=Integer.parseInt(value);
		int nrOfBits=0;
		long bitCounter=intValue;
		while (bitCounter!=0)
		{
			nrOfBits++;
			bitCounter>>=1;
		}
		if(nrOfBits==0)
			nrOfBits=1;
		int nrOfQuat=(nrOfBits>>2)+((nrOfBits&3)>0?1:0);
		for (int i=0;i<nrOfQuat-1;i++)
		{
			bufByte<<=1;
			bufByte++;
			bytePtr++;
			if (bytePtr==8)
			{
				stream[streamPos]=bufByte;
				bufByte=0;
				streamPos++;
				bytePtr=0;
			}
			
		}
		//last zero to terminate length information
		bufByte<<=1;
		bytePtr++;
		if (bytePtr==8)
		{
			stream[streamPos]=bufByte;
			bufByte=0;
			streamPos++;
			bytePtr=0;
		}
		
		//writing bits to stream starting from MSB

		putBits(intValue,4*nrOfQuat);
		
		return 1;
	}
	
	public int writeVLInt(long intValue)
	{

		int nrOfBits=0;
		long bitCounter=intValue;
		while (bitCounter!=0)
		{
			nrOfBits++;
			bitCounter>>=1;
		}
		if(nrOfBits==0)
			nrOfBits=1;
		int nrOfQuat=(nrOfBits>>2)+((nrOfBits&3)>0?1:0);
		for (int i=0;i<nrOfQuat-1;i++)
		{
			bufByte<<=1;
			bufByte++;
			bytePtr++;
			if (bytePtr==8)
			{
				stream[streamPos]=bufByte;
				bufByte=0;
				streamPos++;
				bytePtr=0;
			}
			
		}
		//last zero to terminate length information
		bufByte<<=1;
		bytePtr++;
		if (bytePtr==8)
		{
			stream[streamPos]=bufByte;
			bufByte=0;
			streamPos++;
			bytePtr=0;
		}
		
		//writing bits to stream starting from MSB

		putBits(intValue,4*nrOfQuat);
		
		return 1;
	}
	
	public int writeRangeInt(String value, long minIncl, long maxIncl)
	{
		//ToDo: takes the first numbers or have size to be used?
		long intValue=Integer.parseInt(value);
		int nrOfBits=0;
		
		if (minIncl<0 || maxIncl<0 || minIncl>maxIncl || intValue<minIncl || intValue>maxIncl) {
            throw new IndexOutOfBoundsException();
		}
		long range=maxIncl-minIncl;
		while (range!=0)
		{
			nrOfBits++;
			range>>=1;
		}
		putBits(intValue-minIncl,nrOfBits);
		
		return 1;
	}
	
	public int writeRangeInt(long intValue, long minIncl, long maxIncl)
	{

		int nrOfBits=0;
		
		if (minIncl<0 || maxIncl<0 || minIncl>maxIncl || intValue<minIncl || intValue>maxIncl) {
            throw new IndexOutOfBoundsException();
		}
		long range=maxIncl-minIncl;
		while (range!=0)
		{
			nrOfBits++;
			range>>=1;
		}
		putBits(intValue-minIncl,nrOfBits);
		
		return 1;
	}
	
	public int writeTimeZone(String value)
	{
		//ToDo: takes the first numbers or have size to be used?
		long intValue=Integer.parseInt(value.substring(1,3));
		if (value.charAt(0)=='-')
			intValue*=-1;
		if (intValue<-11 || intValue>14) {
            throw new IndexOutOfBoundsException();
		}
		intValue+=15;
		
		putBits(intValue,5);		
		value=value.substring(4);
		intValue=Integer.parseInt(value);
		long quarter=intValue/15;
		putBits(quarter,2);	
		
		return 1;	
		
	}

	public int writeToCW(ChunkWriter cw)
	{
		cw.write(stream,0,streamPos);
		cw.writeByte((int)bufByte,bytePtr);
		
		return 1;
	}

	private int putBits(long intValue, int nrOfBits)
	{
		long sensor=1<<(nrOfBits-1);
		for(int i=nrOfBits;i>0;i--)
		{
			bufByte<<=1;
			bufByte+=((intValue&sensor)>0?1:0);
			bytePtr++;
			sensor>>=1;
			if (bytePtr==8)
			{
				stream[streamPos]=bufByte;
				bufByte=0;
				streamPos++;
				bytePtr=0;
			}
		}
	return 1;		
	}
}


