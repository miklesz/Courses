%FRAME DIFFERING EXAMPLE 2
clc
clear all
close all

VideoReader('bb8.avi');
mmfileinfo('bb8.avi');
obj=VideoReader('bb8.avi');
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

%computing the difference between each frame
for k = numFrames-1:-1:1
diff(:, :, k) = imabsdiff(grey(:, :, k), grey(:, :, k+1));
end
figure(2),imshow(diff(:, :, 1), []);

for k = numFrames-1:-1:1
    bw(:,:,k)= im2bw(diff(:,:,k),graythresh(diff(:,:,k)));
end

figure(3),imshow(bw(:, :, 1));


for k= numFrames-1:-1:1
    s = regionprops(logical(bw(:,:,k)), 'centroid');
    centroids = [s.Centroid];
    xavg = mean(centroids(:,1:2:end));
    yavg = mean(centroids(:,2:2:end));
    position(:,k)=[xavg,yavg];
    if vidFrames(:,:,:,k)==vidFrames(:,:,:,k+1)
        position(:,k)=position(:,k+1);
    end
end



    
figure(4), subplot (2,1,1); title('X');
plot([1:numFrames-1], position(1,:)), ylabel('position x');
subplot (2,1,2); title('Y');
plot([1:numFrames-1], position(2,:)), ylabel('position y');

for k=1:length(position)        %blad-dac 150
    I=mov(k).cdata;
    xpos=int32(position(1,k));
    ypos=int32(position(2,k));
    I(ypos-5:ypos+5,xpos-5:xpos+5,1:2)=150;
    mov(k).cdata=I;
end;
figure(5), movie(mov,1), title ('position');

background = imdilate(grey,ones(1,1,10));
figure(6), imshow(background(:,:,15));