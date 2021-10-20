clc;
clear ;

im1 = imread('jack.jpg');         %read the image
im1grayscale = rgb2gray(im1);     %convert the image from RGB to Gray scale  
p = 0:255;                        % histogram length ( 0 to 255)
histeqValue = histeq(im1grayscale,p); %Performing the histogram equalization 


figure('Name','Equalized Histogram','NumberTitle','on')

subplot(2,2,1),
imshow(im1grayscale);             %display gray scale image
title('image1 gray scale');
 
subplot(2,2,2),
imhist(im1grayscale);           %disply histogram of gray scale image
title('gray scale Hist ');
 
subplot(2,2,3),
imshow(histeqValue);            %display the image after equalizing its histogram
title('image1 equalized');
 
subplot(2,2,4)
imhist(histeqValue);             % display equalized histogram  
title('Equalized Hist');

figure('Name','RGB Image','NumberTitle','on')
imshow(im1);                      %display the original RGB image
title('RGB Image');
