from emoji import analyze_emoji_sentimens
from sentimentanalysis import get_tweet_sentiment


def test_analyze_emoji_sentimens():
    assert 3/4.0 == analyze_emoji_sentimens("I 😍 python!")
    assert -3/4.0 == analyze_emoji_sentimens("Perl is 💩")
    assert ((3/4.0)+(-1/4.0)) / \
        2 == analyze_emoji_sentimens("I love testing software 💖😜")


def test_combined_sentiment():
    assert 3/4.0 == get_tweet_sentiment("neutral statement with emoji 😍")
    positive_text = "This is a super great absolutely good text with positive attitude"
    assert get_tweet_sentiment(positive_text) > 0
    positive_text_emoji = positive_text + "😎"
    assert get_tweet_sentiment(
        positive_text_emoji) > get_tweet_sentiment(positive_text)
    assert get_tweet_sentiment(positive_text_emoji) <= 1
    assert get_tweet_sentiment("neutral statement with emoji 😍", False) == 0
