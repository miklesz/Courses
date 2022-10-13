clc;
clear ;

im1 = imread('coins1.jpg'); % reading image


subtraction_result = imsubtract(im1,100); % subtract 100 from each pixel value in image1


figure('Name','subtraction operation','NumberTitle','on')
    
 subplot(2,2,[1,2]),
 imshow(im1);            %Display the image 
 title('image 1');

 subplot(2,2,[3,4]),
 imshow(subtraction_result); %Display the result
 title('Operation : subtraction')
 



