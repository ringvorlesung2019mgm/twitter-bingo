import re
from textblob import TextBlob
from textblob_de import TextBlobDE
from langdetect import detect
from langdetect.lang_detect_exception import LangDetectException
from emoji import analyze_emoji_sentimens


def clean_tweet(tweet):
    '''
    Utility function to clean tweet text by removing links, special characters
    using simple regex statements.
    '''
    return ' '.join(re.sub("(@[A-Za-z0-9]+)|([^0-9A-Za-z \t]) | (\w+:\ / \ / \S+)", " ", tweet).split())


def get_tweet_sentiment(tweet, analyze_emojis=True):
    """
    Auto-detect the tweets-language and classify sentiment of passed tweet
    using textblob's sentiment method.
    If the language is neither german nor english (or could not be auto-detected) it returns None.
    If analyze_emojis is True the sentiment of the emojis in the text is also computed. Sentiments of text and emojis is summed (and limited to [-1,1])
    """
    text_sentiment = None
    try:
        lang = detect(clean_tweet(tweet))
        if lang == "de":
            text_sentiment = get_de_tweet_sentiment(tweet)
        elif lang == "en":
            text_sentiment = get_en_tweet_sentiment(tweet)
    except LangDetectException:
        pass

    if not analyze_emojis:
        return text_sentiment

    emoji_sentiment = analyze_emoji_sentimens(tweet)

    if text_sentiment == None:
        return emoji_sentiment

    return max(min(emoji_sentiment+text_sentiment, 1), -1)


def get_en_tweet_sentiment(tweet):
    '''
    Utility function to classify sentiment of passed tweet
    using textblob's sentiment method
    '''
    # create TextBlob object of passed tweet text
    analysis = TextBlob(clean_tweet(tweet))
    # set sentiment
    return analysis.sentiment.polarity


def get_de_tweet_sentiment(tweet):
    '''
    Utility function to classify sentiment of passed tweet
    using textblob's sentiment method
    '''
    # create TextBlob object of passed tweet text
    analysis = TextBlobDE(clean_tweet(tweet))
    # set sentiment
    return analysis.sentiment.polarity


if __name__ == '__main__':
    print('-- en tweets --')
    en_tweets = ['I really like the weather today!', 'This movie was so boring...',
                 'Found today a very interesting book!']
    for t in en_tweets:
        print(t + ' -> ' + get_tweet_sentiment(t))

    print('-- de tweets --')
    de_tweets = ['Das Wetter ist fantastisch heute!',
                 'Der Film war langweilig...',
                 'Gerade ein sehr interessantes Buch entdeckt.',
                 'Scheisse ist das geil.',
                 'Verschissener Kackrotz bei der Bahn!',
                 'Da musste ich am bloeden Knopf rumfingern, das mir der Humpen umgekippt ist! Sapperlot.',
                 'Es erschien ein Engel und zwitscherte ein Lied',
                 'Holla die Waldfee, der Hopfensmoothie laesst mich voll abgehen bei dem Tindergarten. Yolo.',
                 'Ahnma! Die Chixen im Beef sind voll cheedo!',
                 'Unglaublich! Die Frauen im Club sind so toll!']
    for t in de_tweets:
        print(t + ' -> ' + get_de_tweet_sentiment(t))

    # output
    # -- en tweets --
    # I really like the weather today! -> positive
    # This movie was so boring... -> negative
    # Found today a very interesting book! -> positive
    #
    # -- de tweets --
    # Das Wetter ist fantastisch heute! -> positive
    # Der Film war langweilig... -> negative
    # Gerade ein sehr interessantes Buch entdeckt. -> positive
