import re
from textblob import TextBlob
from textblob_de import TextBlobDE


def clean_tweet(tweet):
    '''
    Utility function to clean tweet text by removing links, special characters
    using simple regex statements.
    '''
    return ' '.join(re.sub("(@[A-Za-z0-9]+)|([^0-9A-Za-z \t]) | (\w+:\ / \ / \S+)", " ", tweet).split())


def get_tweet_sentiment(tweet):
    '''
    Utility function to classify sentiment of passed tweet
    using textblob's sentiment method
    '''
    # create TextBlob object of passed tweet text
    analysis = TextBlob(clean_tweet(tweet))
    # set sentiment
    if analysis.sentiment.polarity > 0:
        return 'positive'
    elif analysis.sentiment.polarity == 0:
        return 'neutral'
    else:
        return 'negative'

def get_de_tweet_sentiment(tweet):
    '''
    Utility function to classify sentiment of passed tweet
    using textblob's sentiment method
    '''
    # create TextBlob object of passed tweet text
    analysis = TextBlobDE(clean_tweet(tweet))
    # set sentiment
    if analysis.sentiment.polarity > 0:
        return 'positive'
    elif analysis.sentiment.polarity == 0:
        return 'neutral'
    else:
        return 'negative'


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