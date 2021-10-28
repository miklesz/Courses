I0 = imread( 'Birds.bmp');   
I0grey = im2double(rgb2gray(I0));  
K0= 0.2;
lp = fir1(32,K0);  
lp_2D = ftrans2(lp);  
I_double = im2double(I0grey);
I_lowpass_rep = imfilter(I_double,lp_2D, 'replicate');  
I_lowpass_gray = mat2gray(I_lowpass_rep);
figure();
subplot(2,1,1);
imshow(I0grey); 
title('Przed:');
subplot(2,1,2);
imshow(I_lowpass_gray);
title('Po:');


figure()
freqz2(lp_2D)                  


