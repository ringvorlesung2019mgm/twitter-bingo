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
var tweetApp = angular.module('tweetApp', ['ngAnimate']);

angular.module('tweetApp').controller('tweetStream', function ($scope) {
	$scope.hashtag = '';
	$scope.state = "IDLE"

	$scope.loadHashtag = function () {
	    if ($scope.hashtag !== "" && $scope.hashtag !== null){
            openTweetStreamConnection($scope.hashtag);
		}else{
		    $scope.tweetArray = [];
		    $scope.state = "IDLE"
		    if ($scope.streamRequest != null){
                $scope.streamRequest.abort()
            }
		}
	};

	// based on https://stackoverflow.com/questions/33635919/xmlhttprequest-chunked-response-only-read-last-response-in-progress
	function openTweetStreamConnection(hashtag) {

		var last_index = 0;

        //if there is already a stream-request running abort it, before replacing it with a new one
		if ($scope.streamRequest != null){
		    $scope.streamRequest.abort()
		}

        $scope.tweetArray = [];
        $scope.state = "WAITING"

		$scope.streamRequest = new XMLHttpRequest()
		$scope.streamRequest.open("POST", "api/TweetStream?q=" + encodeURIComponent(hashtag), true)
		$scope.streamRequest.setRequestHeader('Content-type', 'application/json; charset=UTF-8')
		$scope.streamRequest.onprogress = function () {

		    $scope.state = "STREAMING"

			var curr_index = $scope.streamRequest.responseText.length;
			if (last_index == curr_index) return;
			var s = $scope.streamRequest.responseText.substring(last_index, curr_index);
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
					//var loggingobj = [sumRating, tweetCount, averageRating, lowestRatedTweet, highestRatedTweet, differanceSpan];
					//console.log(loggingobj);
					var rankingDecimal = s.rating.toFixed(1);
					var tweetText = s.text;
					var tweetAuthor = "- " + s.userName;

					var tweetObject = {
					    id: s.id['$numberLong'],
						text: tweetText,
						ranking: rankingDecimal,
						author: tweetAuthor,
					};
					$scope.tweetArray.unshift(tweetObject);

					console.log("POST /api/TweetStream Tweet received");
				}
			}

			// Apply the changes to the UI only after all tweets of the current batch have been processed
            			// Otherwise (when refreshing after every element) we would get a huge performance-problem

            //referencing the window by id seems a bit dirty here, but for the moment it will do fine
			tweetlist = document.getElementById("tweetwindow")
			oldscrolltop = tweetlist.scrollTop
			oldscrollbottom = tweetlist.scrollHeight - tweetlist.scrollTop
			$scope.$apply();
			// If the user had scrolled before we updated the list bring him back to the point he was watching
			if (oldscrolltop > 100){
			    tweetlist.scrollTop = tweetlist.scrollHeight - oldscrollbottom
			}

		}
		$scope.streamRequest.send()
	}
});