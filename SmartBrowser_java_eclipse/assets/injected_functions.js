var selectedObj;
var indexSet='';
function listenerClick(e){
	setBorder(this);
	e.stopPropagation();
	e.returnValue = false;
	window.sbrowser.showBtnset();
}
function setBorder(el){
	if(typeof el != 'undefined' && typeof el.style != 'undefined'){
		var temp = el;
		var left = 0;
		var top = 0;
		if(temp.offsetParent){
			do{
				left += temp.offsetLeft;
				top += temp.offsetTop;
			}while(temp=temp.offsetParent);
		}
		selectedCover.style.width = (el.offsetWidth-4)+'px';
		selectedCover.style.height = (el.offsetHeight-4)+'px';
		selectedCover.style.left = left+'px';
		selectedCover.style.top = top+'px';
		selectedCover.style.display = 'block';
		selectedObj = el;
	}
}
function expand(){
	if(typeof selectedObj.parentNode.tagName != 'undefined'){
		var beforeW = selectedObj.offsetWidth;
		var beforeH = selectedObj.offsetHeight;
		setBorder(selectedObj.parentNode);
		if(beforeW*1.2 > selectedObj.offsetWidth && beforeH*1.2 > selectedObj.offsetHeight)
			expand();
	}
}
function clip(){
	hide(document.getElementsByTagName('body')[0].childNodes);
	showParent(selectedObj);
	showChild(selectedObj.childNodes);
	window.sbrowser.saveValues(indexSet,window.innerWidth,window.innerHeight,selectedObj.offsetWidth,selectedObj.offsetHeight);
	window.scrollTo(0,0);
}
function hide(el){
	for(var i=0; i<el.length; i++){
		if(typeof el[i].style != 'undefined')
			el[i].style.display = 'none';
		if(el[i].childNodes instanceof NodeList){
			hide(el[i].childNodes);
		}
	}
}
function showChild(el){
	for(var i=0; i<el.length; i++){
		if(typeof el[i].style != 'undefined')
			el[i].style.display = '';
		if(el[i].childNodes instanceof NodeList)
			showChild(el[i].childNodes);
	}
}
function showParent(el){
	if(typeof el.style != 'undefined')
		el.style.display = '';
	if((el instanceof HTMLBodyElement) == false && el.parentNode && typeof el.parentNode != 'undefined'){
		indexSet = getIndex(el) + ',' + indexSet;
		showParent(el.parentNode);
	}
}
function getIndex(el){
	for(var i=0; i<el.parentNode.childNodes.length; i++)
		if(el.parentNode.childNodes[i] == el) return i;
}
function resetClick(el){
	for(var i=0; i<el.length; i++){
		if((el[i] instanceof Text) == false && (el[i] instanceof HTMLButtonElement) == false){
			el[i].onclick = listenerClick;
			if(el[i].childNodes instanceof NodeList)
				resetClick(el[i].childNodes);
		}
	}
}
function redo(idxSet){
	var currObj = document.getElementsByTagName('body')[0];
	hide(currObj.childNodes);
	var arr = idxSet.split(',');
	for(var i=0; i<arr.length-1; i++){
		if(typeof(currObj.childNodes[arr[i]].style) != 'undefined') currObj = currObj.childNodes[arr[i]];
		else if(arr[i] > 0 && typeof(currObj.childNodes[arr[i]-1].style) != 'undefined') currObj = currObj.childNodes[arr[i]-1];
		else if(typeof(currObj.childNodes[arr[i]+1].style) != 'undefined') currObj = currObj.childNodes[arr[i]+1];
		currObj.style.display='';
	}
	showChild(currObj.childNodes);
}
function extractSource(){
	window.sbrowser.showSource(document.getElementsByTagName('html')[0].outerHTML);
}
function saveSource(){
	window.sbrowser.saveSource(document.getElementsByTagName('html')[0].outerHTML);
}
var mainContEl = null;
var mainContSize = 0;
function extractMainContEl(el){
	var contSize = el.innerText.length;
	if(typeof el.childNodes != 'undefined'){
		for(var i=0; i<el.childNodes.length; i++){
			if(isAvailableElement(el.childNodes[i])){
				contSize -= el.childNodes[i].innerText.length;
				extractMainContEl(el.childNodes[i]);
			}
		}
	}
	if(contSize > mainContSize){
		mainContEl = el;
		mainContSize = contSize;
	}
}
function isAvailableElement(el){
	if(typeof el.innerText != 'undefined' && el instanceof HTMLScriptElement == false 
		&& el.style.display != 'none' && el.offsetWidth > 0)
		return true;
	else return false;
}
function extractContent(){
	extractMainContEl(document.getElementsByTagName('body')[0]);
	var title = document.getElementsByTagName('title')[0].innerText;
	for(var i=0; i<5; i++){
		mainContEl = mainContEl.parentNode;
		if(mainContEl.innerText.length > 1000) break;
	}
	var content = mainContEl.innerHTML;
	window.sbrowser.showContent(title+'<br/><br/>'+content);
}
var selectedCover = document.createElement('div');
selectedCover.setAttribute('style','position:absolute;background-color:rgba(208,80,49,0.3);border:2px solid #d05031;display:none;z-index:99999;');
document.getElementsByTagName('body')[0].appendChild(selectedCover);