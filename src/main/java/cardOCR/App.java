package cardOCR;

import java.io.File;
import java.io.IOException;

public class App {

    public static void main(final String[] args) {

        if (args.length == 0) {
            throw new RuntimeException("Arguments not present!");
        }

        final String pathToFolder = args[0];

        if (pathToFolder == null || pathToFolder.equals(Constants.emptyString)) {
            throw new RuntimeException("Path to folder not present!");
        }

        final long startTime = System.currentTimeMillis();

        final CardProcessor cardProcessor = new CardProcessor();

        // get list of files in folder
        final File folder = new File(pathToFolder);
        final File[] listOfFiles = folder.listFiles();

        if (listOfFiles == null) {
            throw new RuntimeException("List of files empty!");
        }

        // process each file from folder
        for (final File file : listOfFiles) {
            try {
                cardProcessor.processImage(pathToFolder, file.getName());
            } catch (IOException e) {
                throw new RuntimeException("Can't read file!");
            }
        }

        // calculate work time
        final long endTime = System.currentTimeMillis();
        final double workTime = endTime - startTime;
        final String log = String.format(
                "Work time: %s mills ~ (%s seconds) and process %s images",
                workTime,
                (endTime - startTime) / 1000,
                listOfFiles.length);

        System.out.println(log);
    }
}
