// localStorage
let token = localStorage.getItem('token');
let nickname = localStorage.getItem('nickname');

$(document).ready(function () {
    let queryString = window.location.search
    if (queryString) {
        saveToken(queryString)
    }
});

function saveToken(queryString) {
    let urlParams = new URLSearchParams(queryString)
    window.localStorage.setItem("token", urlParams.get("token"))
    window.localStorage.setItem("nickname", urlParams.get("name"))
    window.history.replaceState({}, document.title, "/adme");
}