Number.prototype.between = function (a, b) {
	var min = Math.min.apply(Math, [a, b]),
		max = Math.max.apply(Math, [a, b]);
	return this > min && this < max;
};

var receivedTweets = [];
var lowestRatedTweet = null;
var highestRatedTweet = null;
var averageRating;
var sumRating = 0;
var tweetCount = 0;
var differanceSpan = 0;
const lowCriteria = {
	low: -1,
	high: -0.5
}
const highCriteria = {
	low: 0.5,
	high: 1
};
var lowRatedTweets = [];
var highRatedTweet = []

//TweetStream
var tweetApp = angular.module('tweetApp', []);

tweetApp.controller('tweetStream', function ($scope) {
	$scope.hashtag = '';

	$scope.loadHashtag = function () {
		$scope.tweetArray = [];
		var baseUrl = "http://localhost:8080/producer/api/"
		openTweetStreamConnection($scope.hashtag);
	};

	// based on https://stackoverflow.com/questions/33635919/xmlhttprequest-chunked-response-only-read-last-response-in-progress
	function openTweetStreamConnection(hashtag) {

		var last_index = 0;
		var xhr = new XMLHttpRequest()
		xhr.open("POST", "http://localhost:8080/producer/api/TweetStream?q=" + encodeURIComponent(hashtag), true)
		xhr.setRequestHeader('Content-type', 'application/json; charset=UTF-8')
		xhr.send()
		xhr.onprogress = function () {
			var curr_index = xhr.responseText.length;
			if (last_index == curr_index) return;
			var s = xhr.responseText.substring(last_index, curr_index);
			last_index = curr_index;
			ms = s.split("\r\n");
			for (i = 0; i < ms.length; i++) {
				if (ms[i].length > 0) {
					s = JSON.parse(ms[i])
					receivedTweets.push(s);
					sumRating += s.rating;
					tweetCount += 1;
					averageRating = sumRating / tweetCount;
					if (lowestRatedTweet == null || s.rating <= lowestRatedTweet.rating) {
						lowestRatedTweet = s;
					}
					if (highestRatedTweet == null || s.rating >= highestRatedTweet.rating) {
						highestRatedTweet = s;
					}
					// simple implementation, to be improved by statistics
					if (s.rating.between(lowCriteria.low, lowCriteria.high)) {
						lowRatedTweets.push(s);
					}
					if (s.rating.between(highCriteria.low, highCriteria.high)) {
						highRatedTweet.push(s);
					}
					if (lowestRatedTweet != null && highestRatedTweet != null) {
						// dynamic criteria generation
						// differanceSpan = highestRatedTweet.rating - lowestRatedTweet.rating;
						// lowest20 = lowestRatedTweet.rating + differanceSpan*0.2;
						// highest20 = highestRatedTweet.rating - differanceSpan*0.2;

						// 
					}
					var loggingobj = [sumRating, tweetCount, averageRating, lowestRatedTweet, highestRatedTweet, differanceSpan];
					console.log(loggingobj);
					var rankingDecimal = s.rating.toFixed(1);
					var tweetText = s.text;
					var tweetAuthor = "- " + s.userName;

					var tweetObject = {
						text: tweetText,
						ranking: rankingDecimal,
						author: tweetAuthor,
					};
					$scope.tweetArray.push(tweetObject);

					$scope.$apply();
					//$("#content").append("<blockquote class=\"twitter-tweet\"><p dir=\"ltr\">" + s.text + "</p></blockquote><div class=\"sentiment-number\">"+ rating +"</div>")
					console.log("POST /api/TweetStream Tweet received");
				}
			}
			// console.log(s)
		}
	}
});