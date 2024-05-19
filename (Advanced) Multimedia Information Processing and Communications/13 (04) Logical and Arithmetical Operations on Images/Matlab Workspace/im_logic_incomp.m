clc;
clear ;

im1 = imread('rose1.jpg'); % reading image

im1_bw = im2bw(im1);

incomplement_result_1 = imcomplement(im1); %Invert the RGB image
incomplement_result_2 = imcomplement(im1_bw);  %Invert the B&W image

figure('Name','Logic operation "Incomplemet"','NumberTitle','on')
    
 subplot(2,2,1),
 imshow(im1);            %Display the image 
 title('image 1');

 subplot(2,2,3),
 imshow(incomplement_result_1); %Display the result
 title('Operation : Incomplemet') 
 
  subplot(2,2,2),
 imshow(im1_bw);            %Display the image 
 title('image 1 B&W');

 subplot(2,2,4),
 imshow(incomplement_result_2); %Display the result
 title('Operation : Incomplemet')