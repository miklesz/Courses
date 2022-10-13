x = imread('Birds.bmp'); 
x = rgb2gray(x); 
N = [9 9]; 
sig = 2; 
h = fspecial('gaussian',N,sig);
xf = imfilter(double(x),h); 
xf = uint8(xf); 
H = freqz2(h); 
figure
subplot(121), imagesc(x), colormap(gray),
title('obraz oryginalny')
subplot(122), imagesc(xf), colormap(gray),
title('obraz po filtracji f. Gaussa')
figure
subplot(121), imagesc([-1 1],[-1 1],abs(freqz2(H))), axis('square')
colormap(cool), colorbar
title('Wzmocnienie |H(f_x,f_y)| filtra'), xlabel('f_x'), ylabel('f_y')
subplot(122), freqz2(h), zlabel('|H(f_x,f_y)|')