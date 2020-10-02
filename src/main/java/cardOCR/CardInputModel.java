package cardOCR;

/**
 * Input model of card number string and card suite string
 */
class CardInputModel {

    private String cardNumberString;
    private String cardSuiteString;

    String getCardNumberString() {
        return cardNumberString;
    }

    void setCardNumberString(final String cardNumberString) {
        this.cardNumberString = cardNumberString;
    }

    String getCardSuiteString() {
        return cardSuiteString;
    }

    void setCardSuiteString(final String cardSuiteString) {
        this.cardSuiteString = cardSuiteString;
    }
}
