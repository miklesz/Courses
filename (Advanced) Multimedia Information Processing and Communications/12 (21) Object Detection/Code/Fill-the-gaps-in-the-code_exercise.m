%Fill the gaps in the code. In code there are empty gaps, where you have to
%input Matlab commends.

%background subtraction
clc
clear all
close all

%*************%                 % Get video
%*************%                 % Read a avi file.
%*************%                 % Create new variable and assign video.
%*************%                 % Read new variable.

%Get the individual frames
numFrames=get(obj, 'numberOfFrames');
for k=1:numFrames
    mov(k).cdata=vidFrames(:,:,:,k);
    mov(k).colormap=[];
end

%Show orginal movie

%*************%                 % Make new figure, and play our movie, add title.


%Changing images from rgb to grey
for k = numFrames:-1:1
grey(:, :, k) = rgb2gray(vidFrames(:, :, :, k));
end
figure(2),imshow(grey(:,:,1));


%Exctracting the background
background = imdilate(grey, ones(1, 1, 5));

%Other way to use the threshold
imshow(background(:,:,1));
d = imabsdiff(grey, background);
thresh = graythresh(d);
bw = (d >= thresh * 255);

%*************%                 % Create new variable centroids which is a matrix of infromation of points of moving object. Tip: Its matrix of zeroes with numers of frames.
%*************%                 % Create loop which goes through every frame (it's already ended)

s = regionprops(logical(bw(:, :, k)), 'area', 'centroid');
area_vector = [s.Area];
[tmp, idx] = max(area_vector);
centroids(k, :) = s(idx(1)).Centroid;
end

figure(3);
subplot(2, 1, 1)
plot(1:numFrames, centroids(:,1)), ylabel('x')
subplot(2, 1, 2)
plot(1:numFrames, centroids(:, 2)), ylabel('y')
xlabel('time (s)')

for k=1:length(centroids)
    I=mov(k).cdata;
    xpos=int32(centroids(k,1));
    ypos=int32(centroids(k,2));
    I(ypos-5:ypos+5,xpos-5:xpos+5,1:2)=255;
    mov(k).cdata=I;
end;
figure(4), movie(mov,1), title ('position');
