clc;
clear ;

im1 = imread('coins1.jpg'); % reading image


multiplication_result = immultiply(im1,2.5); %multiply image by 2.5

figure('Name','Multiplication operation','NumberTitle','on')
    
 subplot(2,2,[1,2]),
 imshow(im1);            %Display the image 
 title('image 1');

 subplot(2,2,[3,4]),
 imshow(multiplication_result); %Display the result
 title('Operation : Multiplication')