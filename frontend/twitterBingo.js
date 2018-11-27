var hashtagInput = angular.module('hashtagInput', [])
hashtagInput.controller('myHashtag', function($scope) {
    $scope.Hashtag = "Hash";
});

var myApp = angular.module('myApp', ['ngtweet']);

    $(document).ready(function(){
        console.log("Started");


	var id = "demo";
	var hashtag = "love";

	// based on https://stackoverflow.com/questions/33635919/xmlhttprequest-chunked-response-only-read-last-response-in-progress

	var last_index = 0;
        var xhr = new XMLHttpRequest()
        xhr.open("POST", "http://localhost:8080/producer/TweetStream", true)
	xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
	xhr.send("id=" + id + "&hashtag=" + hashtag);
        xhr.onprogress = function () {
	    var curr_index = xhr.responseText.length;
	    if (last_index == curr_index) return; 
	    var s = xhr.responseText.substring(last_index, curr_index);
	    last_index = curr_index;
	    s = s.replace(/\r\n/gm,"");
	    	if( s != ""){
			console.log(s);
	    		console.log("PROGRESS:", s); 
			s = JSON.parse(s)  
	    		$("#content").append("<blockquote class=\"twitter-tweet\"><p dir=\"ltr\">" + s.text + "</p>" + s.rating + "</blockquote>");
		}else{
			console.log("KeepAlive Received");
		}

	}

    });
