package cardOCR;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CardProcessor {

    private final Map<String, String> mapNumbers = new HashMap<>();
    private final Map<String, String> mapSuits = new HashMap<>();

    /**
     * Fill map with reference values
     */
    CardProcessor() {
        fillReferenceMaps();
    }

    /**
     * Fill maps with reference values for Levenshtein algorithm
     */
    private void fillReferenceMaps() {

        mapNumbers.put("2", Constants.two);
        mapNumbers.put("3", Constants.three);
        mapNumbers.put("4", Constants.four);
        mapNumbers.put("5", Constants.five);
        mapNumbers.put("6", Constants.six);
        mapNumbers.put("7", Constants.seven);
        mapNumbers.put("8", Constants.eight);
        mapNumbers.put("9", Constants.nine);
        mapNumbers.put("10", Constants.ten);
        mapNumbers.put("J", Constants.J);
        mapNumbers.put("Q", Constants.Q);
        mapNumbers.put("K", Constants.K);
        mapNumbers.put("A", Constants.A);

        mapSuits.put("h", Constants.Hearts);
        mapSuits.put("s", Constants.Spades);
        mapSuits.put("d", Constants.Diamonds);
        mapSuits.put("c", Constants.Clubs);
    }

    /**
     * Read image from file and process it to return card values
     *
     * @param folderPath - path to input files folder
     * @param fileName   - name of current file
     * @throws IOException
     */
    void processImage(final String folderPath, final String fileName) throws IOException {

        final BufferedImage image = ImageIO.read(new File(String.format("%s/%s", folderPath, fileName)));
        final StringBuilder outputString = new StringBuilder();

        // find card
        final List<CardInputModel> cardInputModels = readCards(image);

        for (final CardInputModel card : cardInputModels) {

            // initial minimal Levenshtein numbers
            int minNumber = Constants.minimalLevenshteinNumber;
            int minSuite = Constants.minimalLevenshteinNumber;

            // initial found number
            String foundNumber = Constants.emptyString;
            String foundSuite = Constants.emptyString;

            // find card number and append it to output string
            for (Map.Entry<String, String> entry : mapNumbers.entrySet()) {

                final int levenshteinNumber = levenshtein(card.getCardNumberString(), entry.getValue());

                if (levenshteinNumber < minNumber) {
                    minNumber = levenshteinNumber;
                    foundNumber = entry.getKey();
                }
            }

            outputString.append(foundNumber);

            // find card suite and append it to output string
            for (Map.Entry<String, String> entry : mapSuits.entrySet()) {

                final int levenshteinNumber = levenshtein(card.getCardSuiteString(), entry.getValue());

                if (levenshteinNumber < minSuite) {
                    minSuite = levenshteinNumber;
                    foundSuite = entry.getKey();
                }
            }

            outputString.append(foundSuite);
        }

        System.out.println(String.format("%s - %s", fileName, outputString));
    }

    /**
     * Levenshtein algorithm for check strings equality
     *
     * @param targetStr - target string
     * @param sourceStr - source string from string map
     * @return - Levenshtein string equality value
     */
    private int levenshtein(final String targetStr, final String sourceStr) {

        final int m = targetStr.length();
        final int n = sourceStr.length();
        final int[][] delta = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            delta[i][0] = i;
        }

        for (int j = 1; j <= n; j++) {
            delta[0][j] = j;
        }

        for (int j = 1; j <= n; j++) {
            for (int i = 1; i <= m; i++) {
                if (targetStr.charAt(i - 1) == sourceStr.charAt(j - 1)) {
                    delta[i][j] = delta[i - 1][j - 1];
                } else {
                    delta[i][j] = Math.min(delta[i - 1][j] + 1,
                            Math.min(delta[i][j - 1] + 1, delta[i - 1][j - 1] + 1));
                }
            }
        }

        return delta[m][n];
    }

    /**
     * Read cards on table (max 5)
     *
     * @param image - input image
     * @return - list of {@link CardInputModel}
     */
    private List<CardInputModel> readCards(final BufferedImage image) {

        final List<CardInputModel> cardInputModelList = new ArrayList<>();

        int leftOffsetSymbol = Constants.offsetLeftSymbolFirst;
        int leftOffsetNumber = Constants.offsetLeftNumberFirst;

        // read cards (max 5 on table)
        for (int i = 0; i < 5; i++) {

            final CardInputModel cardInputModel = new CardInputModel();

            // read card number string and change left offset
            final String cardNumberString =
                    readCardSymbol(
                            image,
                            Constants.widthNumber,
                            Constants.heightNumber,
                            leftOffsetNumber,
                            Constants.offsetTopNumber);

            cardInputModel.setCardNumberString(cardNumberString);
            leftOffsetNumber -= Constants.leftOffset;

            // read card suite string and change left offset
            final String cardSuiteString =
                    readCardSymbol(
                            image,
                            Constants.widthSymbol,
                            Constants.heightSymbol,
                            leftOffsetSymbol,
                            Constants.offsetTopSymbol);

            cardInputModel.setCardSuiteString(cardSuiteString);
            leftOffsetSymbol -= Constants.leftOffset;

            // check if card model contains only empty card values
            if (!cardInputModel.getCardNumberString().contains(Constants.backgroundSymbol)
                    || !cardInputModel.getCardSuiteString().contains(Constants.backgroundSymbol)) {
                continue;
            }

            cardInputModelList.add(cardInputModel);
        }

        return cardInputModelList;
    }

    /**
     * Read card Symbol from image
     *
     * @param image      - input image
     * @param width      - width
     * @param height     - height
     * @param leftOffset - offset from image left side
     * @param topOffset  - offset from image top side
     * @return - converted string value from image
     */
    private String readCardSymbol(
            final BufferedImage image,
            final int width,
            final int height,
            final int leftOffset,
            final int topOffset) {

        // create new image
        final BufferedImage symbol = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D symbolGraphics = symbol.createGraphics();
        final StringBuilder cardSymbol = new StringBuilder();

        symbolGraphics.drawImage(image, leftOffset, topOffset, null);

        // create string with "*" and " " from image pixels
        for (int y = Constants.initialPixel; y < height; y++) {

            for (int x = Constants.initialPixel; x < width; x++) {

                final int rgb = symbol.getRGB(x, y);

                if (rgb == Constants.whiteBg || rgb == Constants.greyBg) {
                    cardSymbol.append(Constants.backgroundSymbol);
                } else {
                    cardSymbol.append(Constants.symbol);
                }
            }
        }

        return cardSymbol.toString();
    }
}
