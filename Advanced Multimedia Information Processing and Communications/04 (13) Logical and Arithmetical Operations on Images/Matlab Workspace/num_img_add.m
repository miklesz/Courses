clc;
clear ;

im1 = imread('coins1.jpg'); % reading image


addition_result = imadd(im1,100); % Add 100 to each pixel value in image1


figure('Name','Addition operation','NumberTitle','on')
    
 subplot(2,2,[1,2]),
 imshow(im1);            %Display the image 
 title('image 1');

 subplot(2,2,[3,4]),
 imshow(addition_result); %Display the result
 title('Operation : addition')
 



