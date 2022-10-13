close all;
clear all;
clc;

file_name = 'vqegMM2_C01_Aorig'; % here you must specify the file name (without extension) located in the directory.
ext = 'ogg'; % here you must specify the compressed file name extension.
%% NOTE: FILES SHOULD NOT HAVE NAME EXTENSIONS!

%% load wave and compressed file

[y,Fs_y] = audioread([file_name '.wav']); % load wave file

[z,Fs_z] = audioread([file_name '.' ext]); % load compressed file

%% calculations for wave file

T_y = 1/Fs_y; % sampling period
size_y = size(y); 
t_y = 0:T_y:(size_y(1)/Fs_y-T_y);

%% calculations for ext file

T_z = 1/Fs_z; % sampling period
size_z = size(z);
t_z = 0:T_z:(size_z(1)/Fs_z-T_z);

y = y(((size(y)-size(z))/2):(size_z(1)+((size(y)-size(z))/2)-1),:);

t_y = t_z;

%% waveform graph of sound - a comparison of the two channels

figure('Name','Comparison of the signal')
subplot(2,1,1)
plot(t_y',y(:,1),t_z',z(:,1)   )% ,t_y',y(:,1)-z(:,1))
axis([0 10 -max(abs(y(:,1))) max(abs(y(:,1)))]); % determine the display area
xlabel('t[s]')
ylabel('amplitude')
title('channel 1')
legend('wave',ext);
subplot(2,1,2)
plot(t_y',y(:,2),t_z',z(:,2)   )% ,t_y',y(:,1)-z(:,1))
axis([0 10 -max(abs(y(:,2))) max(abs(y(:,2)))]); % determine the display area
xlabel('t[s]')
ylabel('amplitude')
title('channel 2')
legend('wave',ext);

%% fourier transform of the wave file

Y1 = fft(y(:,1),1024*16); % the length of the vector of FFT = 1024
Y2 = fft(y(:,2),1024*16); % the length of the vector of FFT = 1024
% fft function analyzes the number of samples equal to the length of the vector of fft
Pyy1 = Y1.* conj(Y1) / (1024*16); % connection of the complex and real parts
Pyy2 = Y2.* conj(Y2) / (1024*16); % connection of the complex and real parts
f_y = Fs_y*(0:512*16)/(1024*16); % auxiliary vector of frequency needed to chart

%% fourier transform of the compressed file

Z1 = fft(z(:,1),1024*16); % the length of the vector of FFT = 1024
Z2 = fft(z(:,2),1024*16); % the length of the vector of FFT = 1024
% fft function analyzes the number of samples equal to the length of the vector of fft
Pzz1 = Z1.* conj(Z1) / (1024*16); % connection of the complex and real parts
Pzz2 = Z2.* conj(Z2) / (1024*16); % connection of the complex and real parts
f_z = Fs_z*(0:512*16)/(1024*16); % auxiliary vector of frequency needed to chart

%% charts of fft

figure('Name','FFT')
subplot(2,1,1)
plot(f_y,Pyy1(1:512*16+1),f_z,Pzz1(1:512*16+1),f_y,Pyy1(1:512*16+1)-Pzz1(1:512*16+1)) 
axis([0 max(f_y) min(Pyy1(1:512*16+1))-0.1*max(Pyy1(1:512*16+1)) ...
    max(Pyy1(1:512*16+1))]); % determine the display area
xlabel('f[Hz]')
legend('wave',ext,'difference');
title('channel 1')
subplot(2,1,2)
plot(f_y,Pyy2(1:512*16+1),f_z,Pzz2(1:512*16+1),f_y,Pyy2(1:512*16+1)-Pzz2(1:512*16+1))
axis([0 max(f_y) min(Pyy2(1:512*16+1))-0.1*max(Pyy2(1:512*16+1)) ...
    max(Pyy2(1:512*16+1))]); % determine the display area
xlabel('f[Hz]')
legend('wave',ext,'difference');
title('channel 2')

%% SPECGRAM

figure('Name','SPECGRAM')
subplot(2,2,1)
specgram(y(:,1))
title('wave channel 1')
subplot(2,2,2)
specgram(y(:,2))
title('wave channel 2')
subplot(2,2,3)
specgram(z(:,1))
title([ext ' channel 1'])
subplot(2,2,4)
specgram(z(:,2))
title([ext ' channel 2'])

%% energy enthropy

win = Fs_y/10*2;
step = win/2;
blocks = win/441;
EE_y1 = Energy_Entropy_Block(y(:,1), win, step, blocks);
EE_z1 = Energy_Entropy_Block(z(:,1), win, step, blocks);
EE_y2 = Energy_Entropy_Block(y(:,2), win, step, blocks);
EE_z2 = Energy_Entropy_Block(z(:,2), win, step, blocks);

size_EE = size(EE_z1);

t_EE = 0:.2:((size_EE(2)/5)-.2);

figure('Name','Energy entropy block')
subplot(2,1,1)
plot(t_EE,EE_y1,t_EE,EE_z1)
legend('wave',ext);
title('channel 1')
subplot(2,1,2)
plot(t_EE,EE_y2,t_EE,EE_z2)
legend('wave',ext);
title('channel 2')

%% spectral entropy

win = Fs_y/10*2;
step = win/2;
TH = 0.9;

SE_y1 = SpectralEntropy(y(:,1), win, step, 512, 8);
SE_z1 = SpectralEntropy(z(:,1), win, step, 512, 8);

SE_y2 = SpectralEntropy(y(:,2), win, step, 512, 8);
SE_z2 = SpectralEntropy(z(:,2), win, step, 512, 8);

size_SE = size(SE_z1);

t_SE = 0:.2:((size_SE(1)/5)-.2);

figure('Name','Spectral entropy')
subplot(2,1,1)
plot(t_SE,SE_y1,t_SE,SE_z1)
legend('wave',ext);
title('channel 1')
subplot(2,1,2)
plot(t_SE,SE_y2,t_SE,SE_z2)
legend('wave',ext);
title('channel 2')

%% spectral roll off

win = Fs_y/10*2;
step = win/2;
TH = 0.9;

SE_y1 = SpectralRollOff(y(:,1), win, step, TH, win/40)*100;
SE_z1 = SpectralRollOff(z(:,1), win, step, TH, win/40)*100;

SE_y2 = SpectralRollOff(y(:,2), win, step, TH, win/40)*100;
SE_z2 = SpectralRollOff(z(:,2), win, step, TH, win/40)*100;

size_SE = size(SE_z1);

t_SE = 0:.2:((size_SE(2)/5)-.2);

figure('Name','Spectral roll off')
subplot(2,1,1)
plot(t_SE,SE_y1,t_SE,SE_z1)
legend('wave',ext);
title('channel 1')
subplot(2,1,2)
plot(t_SE,SE_y2,t_SE,SE_z2)
legend('wave',ext);
title('channel 2')

%% short time energy

win = Fs_y/10*2;
step = win/2;
TH = 0.9;

STE_y1 = ShortTimeEnergy(y(:,1), win, step);
STE_z1 = ShortTimeEnergy(z(:,1), win, step);

STE_y2 = ShortTimeEnergy(y(:,2), win, step);
STE_z2 = ShortTimeEnergy(z(:,2), win, step);

size_STE = size(STE_z1);

t_STE = 0:.2:((size_STE(1)/5)-.2);

figure('Name','Short time energy')
subplot(2,1,1)
plot(t_STE,STE_y1,t_STE,STE_z1)
legend('wave',ext);
title('channel 1')
subplot(2,1,2)
plot(t_STE,STE_y2,t_STE,STE_z2)
legend('wave',ext);
title('channel 2')

%% Zero crossing rate

win = Fs_y/10*2;
step = win/2;
TH = 0.9;

zcr_y1 = zcr(y(:,1), win, step, Fs_y);
zcr_z1 = zcr(z(:,1), win, step, Fs_z);

zcr_y2 = zcr(y(:,2), win, step, Fs_y);
zcr_z2 = zcr(z(:,2), win, step, Fs_z);

size_zcr = size(zcr_z1);

t_zcr = 0:.2:((size_zcr(1)/5)-.2);

figure('Name','Zero crossing rate')
subplot(2,1,1)
plot(t_zcr,zcr_y1,t_zcr,zcr_z1)
legend('wave',ext);
title('channel 1')
subplot(2,1,2)
plot(t_zcr,zcr_y2,t_zcr,zcr_z2)
legend('wave',ext);
title('channel 2')
