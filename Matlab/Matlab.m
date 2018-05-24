clear();
clc();
desktop = com.mathworks.mde.desk.MLDesktop.getInstance;
myGroup = desktop.addGroup('YR');
desktop.setGroupDocked('YR', 0);
path = '/Users/gabrieledragotto/Documents/Eclipse_wspace/OP_2Rot/Matlab/';
matfiles = dir(fullfile(strcat(path,'colors/'),'YR_*.txt'));
nfiles = length(matfiles);
myDim   = java.awt.Dimension(4, 3);
% 1: Maximized, 2: Tiled, 3: Floating
desktop.setDocumentArrangement('myGroup', 2, myDim)
figH    = gobjects(1, nfiles);
bakWarn = warning('off','MATLAB:HandleGraphics:ObsoletedProperty:JavaFrame');
data  = cell(nfiles);
ADJ = importdata("ADJ.txt","\t",0);


for i = 1 : nfiles
    iFig = i;
	%figH(iFig) = figure('WindowStyle', 'docked', ...
    %  'Name', sprintf('Figure %d', iFig), 'NumberTitle', 'off');
	drawnow;
    figH(iFig) = figure('rend','painters','pos',[10 10 900 600]);
	pause(0.02);  % Magic, reduces rendering errors
	set(get(handle(figH(iFig)), 'javaframe'), 'GroupName', 'YR');
    Labels = importdata(strcat(strcat(path,'labels/'),strrep(matfiles(i).name,'YR','Labels')),"\t",0);
    YR = importdata(strcat(strcat(path,'colors/'),matfiles(i).name),"\t",0);
    add = ones(size(YR),'like',YR);
    YR=YR+add;
    n=size(YR,1)*size(YR,2)+1;
    G=graph(ADJ);
    h=plot(G);
    highlight(h, YR(1,:),'NodeColor','y');
    highlight(h, YR(2,:),'NodeColor','r');
    highlight(h,YR(1,:) );
    highlight(h,YR(2,:) );
    labelnode(h,2:n,Labels);
    labelnode(h,1,'Inf');
    title(strrep((strrep(strrep(matfiles(i).name,'YR_',''),'_','-')),'.txt',''));
    saveas(gcf,[path '/out/' strrep(strrep(matfiles(i).name,'YR',''),'.txt','') '.png']);
end