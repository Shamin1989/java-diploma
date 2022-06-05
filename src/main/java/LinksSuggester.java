import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LinksSuggester {

    private final File file;

    public LinksSuggester(File file) throws FileNotFoundException {
        if (file.isFile()) {
            this.file = file;
        } else {
            throw new FileNotFoundException("File not found");
        }
    }

    public List<Suggest> suggest(String text) {
        List<Suggest> allSuggests = getAllSuggests();
        List<Suggest> findSuggests = new ArrayList<>();
        for (Suggest sug : allSuggests) {
            if (text.toLowerCase().contains(sug.getKeyWord().toLowerCase())) {
                findSuggests.add(sug);
            }
        }
        return findSuggests;
    }
    public List<Suggest> getAllSuggests() {
        List<Suggest> allSuggests = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(file);
            if (!scanner.hasNextLine()) {
                throw new WrongLinksFormatException("Файл пустой");
            }
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] strings = line.split("\t");
                if (strings.length != 3) {
                    throw new WrongLinksFormatException("Не все строки в config файле состоят из 3х частей");
                }
                Suggest suggest = new Suggest(strings[0], strings[1], strings[2]);
                allSuggests.add(suggest);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Не могу прочитать файл : " + e);
        }
        return allSuggests;
    }
}
