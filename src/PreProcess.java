import Enums.Type;
import java.awt.*;
import java.util.*;
import java.nio.file.*;
import static Constants.CONST.*;

public class PreProcess {
    public static void loadThemes() {
        Board.themes = new ArrayList<>();

        try {
            Path path = Paths.get(BOARD_THEMES);
            Scanner scanner = new Scanner(path);
            scanner.useDelimiter(",");

            while (scanner.hasNextLine()) {
                int[] dark = Arrays.stream(scanner.next().split(" "))
                        .mapToInt(Integer::parseInt)
                        .toArray();
                scanner.skip(scanner.delimiter());
                int[] light = Arrays.stream(scanner.next().split(" "))
                        .mapToInt(Integer::parseInt)
                        .toArray();
                scanner.skip(scanner.delimiter());
                int[] move = Arrays.stream(scanner.nextLine().split(" "))
                        .mapToInt(Integer::parseInt)
                        .toArray();

                Color darkColor = new Color(dark[0], dark[1], dark[2]);
                Color lightColor = new Color(light[0], light[1], light[2]);
                Color moveColor = new Color(move[0], move[1], move[2]);
                Color[] theme = new Color[] {darkColor, lightColor, moveColor};
                Board.themes.add(theme);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void loadImages() {
        Board.imagePaths = new HashMap<>();

        try {
            Path path = Paths.get(DEFAULT);
            Scanner scanner = new Scanner(path);
            scanner.useDelimiter(System.lineSeparator());
            scanner.useDelimiter(",");

            while (scanner.hasNextLine()) {
                Type pieceType = Type.valueOf(scanner.next());
                scanner.skip(scanner.delimiter());
                Enums.Color pieceColor = Enums.Color.valueOf(scanner.next());
                scanner.skip(scanner.delimiter());
                String imagePath = scanner.nextLine();

                Map<Type, Enums.Color> config = new HashMap<>();
                config.put(pieceType, pieceColor);
                Board.imagePaths.put(config, imagePath);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}