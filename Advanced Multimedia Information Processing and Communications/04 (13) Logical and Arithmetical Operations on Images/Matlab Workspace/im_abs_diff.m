clc;
clear ;

im1 = imread('coins1.jpg'); % reading image
im2 = imread('coins2.jpg'); % reading image


abs_diff_result = imabsdiff(im1,im2); % performing the absolute difference between the two images


figure('Name','Absolute difference operation','NumberTitle','on')
    
 subplot(2,2,1),
 imshow(im1);            %Display the image 
 title('image 1');
 
 subplot(2,2,2),
 imshow(im2);            %Display the image
 title('image 2');
 
 subplot(2,2,[3,4]),
 imshow(abs_diff_result); %Display the result
 title('Operation : Absolute difference')
 



