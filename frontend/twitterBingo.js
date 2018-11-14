var hashtagInput = angular.module('hashtagInput', [])
hashtagInput.controller('myHashtag', function($scope) {
    $scope.Hashtag = "Hash";
});

var myApp = angular.module('myApp', ['ngtweet']);