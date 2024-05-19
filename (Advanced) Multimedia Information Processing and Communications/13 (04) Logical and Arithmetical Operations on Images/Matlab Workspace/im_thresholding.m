clc;
clear ;

im1 = imread('coins3.jpg'); % Reading image

thre_im1 = im2bw(im1,0.5);  % Perform thresholding

figure('Name','Thresholding','NumberTitle','on')
subplot(1,2,1)
imshow(im1);                %show original image
title('image 1')

subplot(1,2,2)
imshow(thre_im1);           %show image after thresholding 
title('threshold')





 