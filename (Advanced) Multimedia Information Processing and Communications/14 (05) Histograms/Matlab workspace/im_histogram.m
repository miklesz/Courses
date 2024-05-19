clc;
clear ;

im1 = imread('jack.jpg'); % reading image

im1gray = rgb2gray(im1);       % converting RGB image to gray scale *


figure('Name','Image Histogram','NumberTitle','on')
    
 subplot(2,2,1),
 imshow(im1);            %Display the image 
 title('image 1');
 
 subplot(2,2,2),
 imshow(im1gray);            %Display the image
 title('image 1 Gray scale');
 

 subplot(2,2,[3,4]),
 imhist(im1gray); % histogram of image1
 title('Histogram')
 


