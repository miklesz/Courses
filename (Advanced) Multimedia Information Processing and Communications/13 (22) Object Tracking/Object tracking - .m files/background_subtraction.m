%%BACKGROUND SUBTRACTION EXAMPLE 1
clc
clear all
close all

VideoReader('sun.avi');
mmfileinfo('sun.avi');
obj=VideoReader('sun.avi');
vidFrames=read(obj);

numFrames=get(obj, 'numberOfFrames');
%get the individual frames
for k=1:numFrames
    mov(k).cdata=vidFrames(:,:,:,k);
    mov(k).colormap=[];
end

figure(1), movie(mov,1,obj.FrameRate),title('Original movie');

%changing images from rgb to grey
for k = numFrames:-1:1
grey(:, :, k) = rgb2gray(vidFrames(:, :, :, k));
end
figure(3),imshow(grey(:,:,10));

%exctracting the background
background = imdilate(grey, ones(1, 1, 5));

%other way to use the threshold
imshow(background(:,:,1));
d = imabsdiff(grey, background);
thresh = graythresh(d);
bw = (d >= thresh * 255);
centroids = zeros(numFrames, 2);
for k = 1:numFrames
s = regionprops(logical(bw(:, :, k)), 'area', 'centroid');
area_vector = [s.Area];
[tmp, idx] = max(area_vector);
centroids(k, :) = s(idx(1)).Centroid;
end
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