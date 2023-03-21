function findToken(){
	let urlSearch = new URLSearchParams(location.search);
	let token = urlSearch.get('token')
	if(token!=null && token !== localStorage.getItem('token')){
		localStorage.setItem('token', token);
		emptyUsername(token)
	}
}
function emptyUsername(token){
	$.ajax({
		type: "GET", url: `/find-nickname/` + token, contentType: false, processData: false, success: function(response) {
			username = response;
			localStorage.setItem('wschat.sender', username);
		}
	})
}
$(document).ready(function() {
	//alarmSubscribe();
	findToken();
});