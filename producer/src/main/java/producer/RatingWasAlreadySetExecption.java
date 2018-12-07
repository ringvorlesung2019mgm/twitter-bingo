package producer;

public class RatingWasAlreadySetExecption extends Throwable {

    TwingoTweet twingoTweet;
    double rating;

    public RatingWasAlreadySetExecption(String message, TwingoTweet twingoTweet, double rating) {
        super(message);
        this.twingoTweet = twingoTweet;
        this.rating = rating;
    }
}
