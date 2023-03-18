function findToken(){
	let urlSearch = new URLSearchParams(location.search);
	let token = urlSearch.get('token')
	if(token!=null && token !== localStorage.getItem('token')){
		localStorage.setItem('token', token);
		emptyUsername(token)
	}
}

function logout(){
	let nickname = localStorage.getItem('wschat.sender');
	$.ajax({
		type: "GET", url: `/user/logout/` + nickname, contentType: false, processData: false, success: function(response) {
			if (response) {
				location.href = "/adme"
			}
		}
	})
}
$(document).ready(function() {
	findToken();
});