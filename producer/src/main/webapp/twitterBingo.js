var hashtagInput = angular.module('hashtagInput', [])
hashtagInput.controller('myHashtag', function($scope) {
    $scope.Hashtag = "Hash";
});

var myApp = angular.module('myApp', ['ngtweet']);

$(document).ready(function(){
    var hashtag = "love";
    var baseUrl = "http://localhost:8080/producer/api/"
    console.log("Twitter Bingo started");
    console.log("GET /api/GetToken started")

    $.get(baseUrl + "GetToken", function(response){
        var response_json = JSON.parse(response);
        console.log("GET /api/GetToken finished");
        console.log(response_json);
        openTweetStreamConnection(response_json.sessionId, hashtag);
    });

});


// based on https://stackoverflow.com/questions/33635919/xmlhttprequest-chunked-response-only-read-last-response-in-progress
function openTweetStreamConnection(sessionId, hashtag){

    var payload = {
        'sessionId' : sessionId,
        'hashtag' : hashtag
    }
	var last_index = 0;
    var xhr = new XMLHttpRequest()
    xhr.open("POST", "http://localhost:8080/producer/api/TweetStream", true)
	xhr.setRequestHeader('Content-type', 'application/json; charset=UTF-8');
	xhr.send(JSON.stringify(payload));
	console.log("POST /api/TweetStream started");
	console.log(payload);
    xhr.onprogress = function () {
	    var curr_index = xhr.responseText.length;
	    if (last_index == curr_index) return;
	    var s = xhr.responseText.substring(last_index, curr_index);
	    last_index = curr_index;
	    s = s.replace(/\r\n/gm,"");
	    s = JSON.parse(s)
	    $("#content").append("<blockquote class=\"twitter-tweet\"><p dir=\"ltr\">" + s.text + "</p>" + s.rating + "</blockquote>")
	    console.log("POST /api/TweetStream Tweet received");
	    // console.log(s)
	}
}
