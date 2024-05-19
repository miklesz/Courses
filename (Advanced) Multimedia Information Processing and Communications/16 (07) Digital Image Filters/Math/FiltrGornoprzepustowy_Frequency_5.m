I0 = imread( 'Birds.bmp');   
I0grey = im2double(rgb2gray(I0));  
ff = fft2(I0grey);
K0= 0.2;    
[N,M] = size(I0grey); 
dx = 1; 
dy = 1; 
KX0 = (mod(1/2 + (0:(M-1))/M, 1) - 1/2); 
KX1 = KX0 * (2*pi/dx); 
KY0 = (mod(1/2 + (0:(N-1))/N, 1) - 1/2); 
KY1 = KY0 * (2*pi/dx); 
[KX,KY] = meshgrid(KX1,KY1);
lpf = (KX.*KX + KY.*KY < K0^2); 
hpf = 1-lpf;
rec = ifft2(hpf.*ff);
figure(1);
F1=fftshift(log(abs(hpf)+1));    
mesh(F1);
figure(2)
subplot(2,1,1)
imshow(I0grey); 
title('Przed filtracj¹:');
subplot(2,1,2)
imshow(rec);
title('Po filtracji:');