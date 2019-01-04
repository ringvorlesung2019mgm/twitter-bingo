//TweetStream
var tweetApp = angular.module('tweetApp', ['ngAnimate']);

angular.module('tweetApp').controller('tweetStream', function ($scope) {
	$scope.hashtag = '';
	$scope.state = "IDLE"

	$scope.loadHashtag = function () {
		$scope.tweetArray = [];
		$scope.state = "IDLE";
		$scope.tweetCount = 0;
		$scope.averageRating = 0;
		$scope.sumRating = 0;
		$scope.lowestRatedTweet = null;
		$scope.highestRatedTweet = null;
		$scope.numPositive = 0;
		$scope.numNegative = 0;
		$scope.ratingArray =[];

		//Abort currently running request
		if ($scope.streamRequest != null) {
			$scope.streamRequest.abort()
		}

		if ($scope.hashtag !== "" && $scope.hashtag !== null) {
			openTweetStreamConnection($scope.hashtag);
		}
	};

	// based on https://stackoverflow.com/questions/33635919/xmlhttprequest-chunked-response-only-read-last-response-in-progress
	function openTweetStreamConnection(hashtag) {
		var last_index = 0;

		chartArray = [];
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
					var tweetAuthor = "- " + s.userName;

					var tweetObject = {
						id: s.id['$numberLong'],
						text: s.text,
						rating: s.rating *10,
						author: tweetAuthor,
						createdAt: new Date(s.createdAt["$date"])
					};

					$scope.tweetArray.unshift(tweetObject);

					$scope.sumRating += s.rating;
					$scope.tweetCount += 1;
					$scope.averageRating = $scope.sumRating / $scope.tweetCount;
					$scope.averageRating = ($scope.averageRating);
					$scope.ratingArray.push(s.rating);

					if ($scope.lowestRatedTweet == null || tweetObject.rating <= $scope.lowestRatedTweet.rating) {
						$scope.lowestRatedTweet = tweetObject;
					}
					if ($scope.highestRatedTweet == null || tweetObject.rating >= $scope.highestRatedTweet.rating) {
						$scope.highestRatedTweet = tweetObject;
					}

					if (s.rating < 0) {
						$scope.numNegative++
					}

					if (s.rating > 0) {
						$scope.numPositive++
					}
                    var windowRatingTotal = 0;
                    var windowRatingSum = 0;
                    var tempRating = 0;

                    if ($scope.tweetCount >= 5){
                        for (var h = 0; h<5;h++){
                             tempRating = $scope.ratingArray[$scope.tweetCount-h-1];
                             windowRatingSum = windowRatingSum + tempRating
                        }

                        windowRatingTotal= windowRatingSum*1.0/5;

					    var chartObject = {
						    x: new Date(s.createdAt["$date"]),
						    y:  windowRatingTotal
					    }
					    chartArray.push(chartObject);
                    }

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
			if (oldscrolltop > 100) {
				tweetlist.scrollTop = tweetlist.scrollHeight - oldscrollbottom
			}
			loadChart(chartArray);

		}
		$scope.streamRequest.send()
	}

	function loadChart(chartArray) {
		var chart = new CanvasJS.Chart("chartContainer", {
			animationEnabled: false,
			theme: "light2",
			title: {
				text: "Time Analysis"

			},
			axisY: {
				stripLines: [{ value: 0 }]
			},
			axisX: {
				title: "Time"
			},
			data: [{
				type: "line",
				xValueType: "dateTime",
				xValueFormatString: "DD MMM hh:mm TT",
				dataPoints: chartArray
			}]
		});
		chart.render();
	}
});