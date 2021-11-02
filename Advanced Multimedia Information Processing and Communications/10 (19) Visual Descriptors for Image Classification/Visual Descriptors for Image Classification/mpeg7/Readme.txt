MPEG-7 Low Level Feature Extraction - Command Line Tool (Windows executable)

by Muhammet Bastan, (c) October 2009
Bilkent University
Department of Computer Engineering
Bilkent, Ankara, TURKIYE

e-mail: mubastan@gmail.com

download page: www.cs.bilkent.edu.tr/~bilmdg/bilvideo-7/Software.html

date: 01.11.2009
update: 15.11.2009
----------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------
Table of Contents:

	Introduction
	License - Liability
	Supported Descriptors
	How to use (running the program for each type of descriptor, parameters, output format, etc.)
		Color Structure Descriptor (CSD)
		Scalable Color Descriptor (SCD)
		Color Layout Descriptor (CLD)
		Dominant Color Descriptor (DCD)
		Homogeneous Texture Descriptor (HTD)
		Edge Histogram Descriptor (EHD)
	Similarity Computation (Matching)

----------------------------------------------------------------------------------------

Introduction

This command line tool for Windows is compiled (C++ and MinGW) using the MPEG-7 Low Level Feature Extraction library (C++)
(which is also planned to be released on the same web site when it is ready to be released), 
which was developed for BilVideo-7 MPEG-7 compatible video indexing and retrieval system:

Muhammet Bastan, Hayati Cam, Ugur Gudukbay and Ozgur Ulusoy, 
"BilVideo-7: An MPEG-7 Compatible Video Indexing and Retrieval System", 
IEEE MultiMedia, vol. 17, no. 3, pp. 62-73, July-September 2010.

Bibtex entry:
@article{BilVideo7-MM2010,
	author = {Muhammet Ba\c{s}tan and Hayati \c{C}am and U\v{g}ur G\"{u}d\"{u}kbay and \"{O}zg\"{u}r Ulusoy},
	title = {{BilVideo-7: An MPEG-7-Compatible Video Indexing and Retrieval System}},
	journal ={IEEE MultiMedia},
	volume = {17},
	number = {3},
	pages = {62--73},
	year = {2009},
	doi = {http://doi.ieeecomputersociety.org/10.1109/MMUL.2009.74},
	publisher = {IEEE Computer Society},
	address = {Los Alamitos, CA, USA},
}

Please cite this paper if you use this tool.
Please feel free to contact the author (bastan@cs..) in case of any bugs in the program (which is of course possible),
or valuable suggestions.

---------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------
License - Liability: 
(GPL. You are free to use/distribute the tool including this Readme.txt and all the other files as is)
In no event and under no legal theory,
      
whether in tort (including negligence), contract, or otherwise,      
unless required by applicable law (such as deliberate and grossly      
negligent acts) or agreed to in writing, shall any Contributor be      
liable to You for damages, including any direct, indirect, special,      
incidental, or consequential damages of any character arising as a      
result of this License or out of the use or inability to use the      
Work (including but not limited to damages for loss of goodwill,      
work stoppage, computer failure or malfunction, or any and all      
other commercial damages or losses), even if such Contributor      
has been advised of the possibility of such damages.
----------------------------------------------------------------------------------------

The tool contains the functionality of the MPEG-7 Low Level Feature Extraction library only partially, 
and it will be expanded as time allows.
The library is adapted from the MPEG-7 XM Reference Software available at 
http://standards.iso.org/ittf/PubliclyAvailableStandards/index.html
The XM reference software contains some bugs and does not work properly.
Moreover, it is not meant to be used in applications directly, therefore, 
there is no decent interface to use it easily, not mentioning the horrible coding, 
which makes everybody run away at the first glance!

----------------------------------------------------------------------------------------

Supported descriptors in this package :

 + Color Structure Descriptor (CSD)
 + Scalable Color Descriptor (SCD)
 + Color Layout Descriptor (CLD)
 + Dominant Color Descriptor (DCD)
 + Homogeneous Texture Descriptor (HTD)
 + Edge Histogram Descriptor (EHD)

Please see http://www.chiariglione.org/mpeg/standards/mpeg-7/mpeg-7.htm for a brief description of each.
For more details: 
B. S. Manjunath, P. Salembier, and T. Sikora, Eds., Introduction to MPEG-7: Multimedia Content Description Interface. England: WILEY, 2002.

In this release, extraction only from whole images is supported.
Extraction from image regions (not supported in this package) requires region masks,
therefore, it is better to use the library directly (will hopefully be released soon) instead of a command line tool..

----------------------------------------------------------------------------------------

How to use:

The library & executable uses OpenCV 2.0 (Open Computer Vision Library) for image/video handling, therefore the runtime DLLs
are required to be in the same directory, or they should be on the PATH..
The package includes the require OpenCV DLLs for convenience, therefore you do not need to install OpenCV 2.0 to run the executable.

You should run the program from command line, supplying the necessary input arguments; and the descriptors will be written 
to the output file you specified.
The program extracts one descriptor at a time. (It would be faster if it could extract a specified subset of them)

The program is run as follows:

MPEG7Fex.exe featureType featureParameters imageListFile outputFile

For instance, to extract Color Structure Descriptor from a set of images, run the following command:

MPEG7Fex.exe CSD 64 imageList.txt CSD.txt

This command reads the image filenames from imageList.txt file (full/relative path of images, one image per line),
extracts CSD of size 64 from each image, and saves the descriptor values to CSD.txt, one CSD descriptor on one line for each image.

If imageList.txt is as follows (see imageList.txt in this directory)

images/Ordekler.jpg
D:\research\code\sandbox\MPEG7Fex\images\ari-cicek2.jpg
D:\research\code\sandbox\MPEG7Fex\images\agustos_bocegi.jpg

(The images can be specified by their full paths or relative paths, relative to the MPEG7Fex.exe executable)

The output file, CSD.txt (see descriptors/ directory), is generated as follows, one descriptor on each line.

imageFile    descriptorValues (64 values for CSD of size 64)
----------   -----------------------------------------------
Ordekler.jpg 61 0 0 0 3 0 0 0 86 75 2 21 0 0 2 0 181 96 0 0 0 0 5 0 119 144 96 76 98 175 23 0 113 204 59 2 31 26 0 0 140 114 47 15 131 87 18 3 112 94 20 3 59 42 3 3 117 149 70 34 6 0 2 6
ari-cicek2.jpg 4 44 0 0 0 0 0 0 19 11 114 84 98 4 0 0 0 0 0 0 109 108 39 8 121 164 124 51 46 121 92 0 8 18 27 0 69 161 168 78 104 118 60 11 86 94 62 0 35 56 56 0 59 88 74 4 65 75 71 76 63 16 0 0
agustos_bocegi.jpg 124 163 79 0 0 0 0 5 130 68 121 46 96 42 0 0 0 0 0 0 0 0 14 24 141 106 62 53 89 49 27 16 6 13 5 0 37 59 43 16 77 45 31 60 102 35 33 51 33 19 9 29 35 46 22 20 61 38 25 10 12 16 18 162

If the input arguments are specified wrongly, or the parameter ranges are wrong and informative error message is printed on the screen,
showing how to extract each descriptor, etc.


In the following, we give the details of how to run the program to extract each descriptor.


1. Color Structure Descriptor (CSD)
-----------------------------------

Run, using one of the following (in the second case, default parameters are used)

MPEG7Fex.exe CSD 64 imageList.txt CSD.txt
or
MPEG7Fex.exe CSD imageList.txt CSD.txt

Default descriptor size is 64, allowable descriptor size values are 32, 64, 128, 256.

The output, DCD.txt (see descriptors/DCD.txt), is shown above.


2. Scalable Color Descriptor (SCD)
----------------------------------

Run, using one of the following (in the second case, default parameters are used)

MPEG7Fex.exe SCD 128 imageList.txt SCD.txt
or 
MPEG7Fex.exe SCD imageList.txt SCD.txt

Default descriptor size is 128, allowable descriptor size values are 16, 32, 64, 128, 256.

The output, SCD.txt (see descriptors/SCD.txt), is as follows (descriptor size=32):

Ordekler.jpg -148 75 0 49 -14 -12 9 21 -16 -13 -2 14 9 14 19 22 -2 5 -3 6 -1 5 0 0 -2 2 2 0 -3 5 1 -4
ari-cicek2.jpg -182 71 18 57 -3 3 17 21 -5 2 6 14 9 14 19 22 3 -2 -1 2 -1 5 0 0 -2 2 2 0 -3 5 1 -4
agustos_bocegi.jpg -167 77 5 52 -7 -7 14 23 -9 -8 3 16 9 14 19 22 3 -4 0 2 -1 5 0 0 -2 2 2 0 -3 5 1 -4


3. Color Layout Descriptor (CLD)
----------------------------------

Run, using one of the following (in the second case, default parameters are used)

MPEG7Fex.exe CLD numberOfYCoeff numberOfCCoeff images.txt CLD.txt

MPEG7Fex.exe CLD 64 28 imageList.txt CLD.txt
or
MPEG7Fex.exe CLD imageList.txt CLD.txt

Defaults, numberOfYCoeff = 64,  numberOfCCoeff = 28.
Allowable parameters (numberOfYCoeff, numberOfCCoeff) are 3, 6, 10, 15, 21, 28, 64

The output file (CLD.txt) contains the CLD descriptors, assuming numberOfYCoeff = 6,  numberOfCCoeff = 3, 
one per line, in the following order:

imageFile YCoeff_DC(1) YCoeff_AC(5) CbCoeff_DC(1) CbCoeff_AC(2) CrCoeff_DC(1) CrCoeff_AC(2)    (vector of length 12)

First the DC coefficient, then the AC coefficients for 3 channels (Y, Cb, Cr) are written consecutively.

Example (see descriptors/CLD.txt, run as MPEG7Fex.exe CLD 6 3 imageList.txt descriptors/CLD.txt)

Ordekler.jpg 15 18 17 13 16 11 35 19 11 22 12 16
ari-cicek2.jpg 26 13 17 18 17 20 23 11 7 33 10 8
agustos_bocegi.jpg 47 25 30 25 9 25 15 13 28 29 17 21


4. Dominant Color Descriptor (DCD)
----------------------------------

Run, using one of the following

MPEG7Fex.exe DCD normalizationFlag varianceFlag spatialFlag numBin1 numBin2 numBin3 images.txt DCD.txt

MPEG7Fex.exe DCD 1 0 1 32 32 32 imageList.txt DCD.txt
or
MPEG7Fex.exe DCD 1 0 1 images.txt DCD.txt
or
MPEG7Fex.exe DCD images.txt DCD.txt

Parameters:
+ normalizationFlag: if 1, normalize the values to MPEG-7  ranges (color:0-32, variance:0,1, weight:0-32)
		     if 0, do not normalize (color:RGB values[0-255], variance:as computed, weight:0-100 percent)
		     default: 1
+ varianceFlag: if 1, compute and write variance; if 0, do not compute/write variance, default: 1.
+ spatialFlag: if 1, compute and write spatial coherence; if 0, do not compute/write spatial coherence; default: 1
+ numBin1, numBin2, numBin3: number of bins for color quantization, defaults: 32, 32, 32.

Output file (DCD.txt) contains the DCD descriptors, one per line for each image, in the following order.

imageFile numberOfDominantColors spatialCoherency percentage_1 centroid_1_channel1 centroid_1_channel2 centroid_1_channel3
          variance_1_channel1 variance_1_channel2 variance_1_channel3 percentage_2 centroid_2_channel1 centroid_2_channel2 centroid_2_channel3 ...

Example (see descriptors/DCD.txt)

Ordekler.jpg 7 14 8 9 15 19 0 0 0 4 3 4 4 0 0 0 4 9 12 9 0 0 0 2 24 22 19 1 0 1 3 8 11 13 0 0 0 5 8 8 7 0 1 0 2 11 17 17 0 0 1
ari-cicek2.jpg 8 4 2 4 4 3 1 0 1 10 12 18 10 0 0 0 4 16 9 15 1 0 0 2 13 13 6 1 0 1 2 12 11 11 0 0 1 4 22 16 22 0 0 1 2 22 21 13 1 0 1 2 17 17 16 1 0 1
agustos_bocegi.jpg 8 9 3 3 4 3 1 0 0 14 31 31 31 0 0 0 2 10 15 5 1 0 1 1 23 12 7 1 1 1 2 18 29 5 0 0 1 2 7 9 4 1 0 1 2 14 9 8 1 1 1 2 13 20 5 1 1 1

 
There might be up to 8 dominant colors in an image, or only one! Hence the descriptors for each image may differ in size.
That is why "the number of dominant colors" is saved as the first value for the ease of reading..



5. Homogeneous Texture Descriptor (HTD)
---------------------------------------

Run, using one of the following

MPEG7Fex.exe HTD layerFlag images.txt HTD.txt

MPEG7Fex.exe HTD 1 images.txt HTD.txt
or
MPEG7Fex.exe HTD images.txt HTD.txt

*** NOTE: the input images should be at least 128x128 in size, otherwise they are skipped!

layerFlag: if 1, full layer (compute & write  both energy and deviation); if 0, base layer (compute & write energy, but not energy deviation)
           default: 1

Output file (HTD.txt) contains the HTD descriptors, one per line for each image, in the following order.

imageFile mean (1) std(1) energy (30) energyDeviation(30)	(a vector of length 62 if full layer)


Example (see descriptors/HTD.txt)
Ordekler.jpg 86 61 201 179 163 142 169 195 194 150 135 121 137 157 156 125 103 105 94 121 119 73 58 85 58 58 96 16 0 54 0 5 201 181 165 137 170 192 187 148 133 118 125 146 146 119 94 92 85 116 117 58 56 73 55 53 104 8 0 48 0 1
agustos_bocegi.jpg 176 195 238 239 235 235 202 191 216 200 202 206 208 208 183 193 159 191 161 190 153 161 156 167 161 176 149 141 136 142 132 141 237 239 233 232 197 185 206 192 202 202 204 204 166 186 141 177 144 185 141 134 153 149 157 176 141 117 111 137 102 123



6. Edge Histogram Descriptor (EHD)
---------------------------------------

Run using,

MPEG7Fex.exe EHD images.txt EHD.txt

The output file (EHD.txt) contains EHD descriptors of length 80 for each image.

Example (see descriptors/HTD.txt)

Ordekler.jpg 0 6 2 5 0 1 3 5 2 0 1 4 2 4 0 0 1 1 1 0 0 6 2 2 3 2 4 5 3 2 1 4 5 6 4 1 6 5 5 1 1 7 2 3 5 2 4 7 6 4 3 4 5 6 5 2 5 2 4 3 0 6 0 1 1 1 7 1 3 1 2 6 3 5 4 1 7 2 6 2
ari-cicek2.jpg 0 0 0 0 0 2 3 5 4 1 0 5 1 1 2 0 2 1 1 0 2 0 2 2 1 4 4 6 6 4 2 4 7 5 3 3 3 6 7 0 3 1 5 5 2 5 2 6 6 5 4 3 7 6 4 6 2 7 4 3 2 1 3 4 2 6 1 5 6 5 6 1 5 6 4 6 1 6 5 4
agustos_bocegi.jpg 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 3 1 2 1 5 4 2 5 3 3 3 5 5 0 4 0 1 0 1 3 6 0 2 3 2 6 4 7 3 5 3 6 4 2 2 2 6 2 2 4 6 2 4 5 0 4 7 5 1 1 1 7 3 1 2 1 7 4



------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------

Similarity Computation (Matching):
For the distance metrics proposed by MPEG-7, see the MPEG-7 book:
B. S. Manjunath, P. Salembier, and T. Sikora, Eds., Introduction to MPEG-7: Multimedia Content Description Interface. England: WILEY, 2002.

or the following papers:

Muhammet Bastan, Hayati Cam, Ugur Gudukbay and Ozgur Ulusoy, 
"BilVideo-7: An MPEG-7 Compatible Video Indexing and Retrieval System", 
IEEE MultiMedia, vol. 17, no. 3, pp. 62-73, July-September 2010.

Muhammet Bastan, Hayati Cam, Ugur Gudukbay, Ozgur Ulusoy, 
“An MPEG-7 Compatible Video Retrieval System with Integrated Support for Complex Multimodal Queries,”
Bilkent University, Technical Report (BU-CE-0905), 2009.
Available at: http://www.cs.bilkent.edu.tr/tech-reports/2009/BU-CE-0905.pdf

H. Eidenberger, “Distance measures for MPEG-7-based retrieval,” 
in Proceedings of the 5th ACM SIGMM International Workshop on Multimedia Information Retrieval (MIR ’03), 
2003, pp. 130–137.

For instance, you may use L1 distance for DCD, SCD..

