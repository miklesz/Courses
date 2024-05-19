clc;
clear ;

im1 = imread('coins1.jpg'); % reading image
im2 = imread('coins2.jpg'); % reading image

%im1_gray = rgb2gray(im1); % converting RGB image to gray scale *
%im2_gray = rgb2gray(im2); % converting RGB image to gray scale

%im1_bw= im2bw(im1);       % converting RGB image to black and white **
%im2_bw = im2bw(im1);      % converting RGB image to black and white

addition_result = imadd(im1,im2); % performing the addition on the two images


figure('Name','Addition operation','NumberTitle','on')
    
 subplot(2,2,1),
 imshow(im1);            %Display the image 
 title('image 1');
 
 subplot(2,2,2),
 imshow(im2);            %Display the image
 title('image 2');
 
 subplot(2,2,[3,4]),
 imshow(addition_result); %Display the result
 title('Operation : addition')
 



