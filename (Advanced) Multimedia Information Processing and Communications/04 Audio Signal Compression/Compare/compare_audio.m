clear all;
%hmfr = dsp.AudioFileReader('vqegMM2_C01_Aorig.wav');
%hmfr = audioread('vqegMM2_C01_Aorig.wav');
%src=step(hmfr);
%while ~isDone(hmfr)
%    src = [src; step(hmfr)];
%end
%release(hmfr); % release the input file
%hmfr = dsp.AudioFileReader('064.mp3');
%hmfr = dsp.AudioFileReader('vqegMM2_C01_Aorig.wav');
%pas=step(hmfr);
%while ~isDone(hmfr)
%    pas = [pas; step(hmfr)];
%end
%release(hmfr); % release the input file

src = audioread('vqegMM2_C01_Aorig.wav');
pas = audioread('vqegMM2_C01_Aorig.mp3');
msrc=mean(src,2);
mpas=mean(pas,2);
d=size(mpas,1);
mcal=msrc(1:d);
e=mpas-mcal;
ae=abs(e);
mae=mean(ae)