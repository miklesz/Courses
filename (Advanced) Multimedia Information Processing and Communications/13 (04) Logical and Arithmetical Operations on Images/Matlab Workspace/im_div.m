clc;
clear ;

im1 = imread('coins1.jpg'); % reading image


multiplication_result = immultiply(im1,1.0 / 2.5); %divide image by 2.5

figure('Name','Division operation','NumberTitle','on')
    
 subplot(2,2,[1,2]),
 imshow(im1);            %Display the image 
 title('image 1');

 subplot(2,2,[3,4]),
 imshow(multiplication_result); %Display the result
 title('Operation : Division')