package com.learningJava;

import javax.xml.crypto.Data;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {
    private static StringBuilder logger = new StringBuilder();

    public static void main(String[] args) {
        // Task #1
        /*
        В папке Games создайте несколько директорий: src, res, savegames, temp.
        В каталоге src создайте две директории: main, test.
        В подкаталоге main создайте два файла: Main.java, Utils.java.
        В каталог res создайте три директории: drawables, vectors, icons.
        В директории temp создайте файл temp.txt
        */

        File games = new File("Games");
        if (!tryMkdir(games)) {
            System.out.println("Возникли проблемы при создании директории " + games.getAbsolutePath());
            return;
        }
        File src = new File("Games/src");
        File res = new File("Games/res");
        File savegames = new File("Games/savegames");
        File temp = new File("Games/temp");

        File main = new File("Games/src/main");
        File test = new File("Games/src/test");

        File drawables = new File("Games/res/drawables");
        File vectors = new File("Games/res/vectors");
        File icons = new File("Games/res/icons");

        File log = new File(temp, "Games/temp.txt");

        if (tryMkdir(src)) {
            if (tryMkdir(main)) {
                tryCreateNewFile(main, "Main.java");
                tryCreateNewFile(main, "Utils.java");
            }
            tryMkdir(test);
        }
        if (tryMkdir(res)) {
            tryMkdir(drawables);
            tryMkdir(vectors);
            tryMkdir(icons);
        }
        tryMkdir(savegames);
        if (tryMkdir(temp)) {
            if (tryCreateNewFile(temp, "temp.txt")) {
                try (FileWriter fileWriter = new FileWriter("Games/temp/temp.txt", false)) {
                    fileWriter.write(logger.toString());
                    fileWriter.flush();
                } catch (IOException exc) {
                    System.out.println(exc.getMessage());
                }
            }
        }
        // Task #2
        /*
        Создать три экземпляра класса GameProgress.
        Сохранить сериализованные объекты GameProgress в папку savegames из предыдущей задачи.
        Созданные файлы сохранений из папки savegames запаковать в архив zip.
        Удалить файлы сохранений, лежащие вне архива.
        */
        GameProgress[] gameProgresses = {
                new GameProgress(10, 11, 15, 100.15),
                new GameProgress(90, 9, 95, 105.54),
                new GameProgress(1, 1, 99, 11085.56),
        };
        int i = 1;
        List<String> fullFileNames = new ArrayList<>();
        for (GameProgress gameProgress : gameProgresses) {
            String fileNameInZip = "save" + i + ".dat";
            String fullFileName = savegames.getAbsolutePath() + "/" + fileNameInZip;

            if (!saveGame(gameProgress, fullFileName)) {
                return;
            }
            fullFileNames.add(fullFileName);
            ++i;
        }
        String zipPath = savegames.getAbsolutePath() + "/GameProgresses.zip";
        if (zipFiles(zipPath, fullFileNames)) {
            for (String fileName : fullFileNames) {
                File file = new File(fileName);
                file.delete();
            }
        }
        // Task #3
        /*
        Произвести распаковку архива в папке savegames.
        Произвести считывание и десериализацию одного из разархивированных файлов save.dat.
        Вывести в консоль состояние сохранненой игры.
         */
        if (openZip(zipPath, savegames.getAbsolutePath())) {
            System.out.println(openProgress(fullFileNames.get(0)));
        }

    }

    public static GameProgress openProgress(String fullFileName) {
        GameProgress gameProgress = null;
        try(FileInputStream fis = new FileInputStream(fullFileName);
        ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            gameProgress = (GameProgress) ois.readObject();
        } catch (Exception exc) {
            System.out.println(exc.getMessage());
        }
        return gameProgress;
    }

    public static boolean openZip(String pathZip, String pathDir) {
        try (ZipInputStream zin = new ZipInputStream(new FileInputStream(pathZip))) {
            ZipEntry entry;
            String name;
            while((entry = zin.getNextEntry())!= null) {
                name = entry.getName();
                FileOutputStream fout = new FileOutputStream(pathDir + "/" + name);
                for(int c = zin.read(); c != -1; c = zin.read()) {
                    fout.write(c);
                }
                fout.flush();
                zin.closeEntry();
                fout.close();
            }
            return true;
        } catch (IOException exc) {
            System.out.println(exc.getMessage());
            return false;
        }
    }

    public static boolean zipFiles(String fullPath, List<String> fullFileName) {
        try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(fullPath))) {
            int i = 1;
            for (String fileName : fullFileName) {
                String name = "save" + i + ".dat";
                try (FileInputStream fis = new FileInputStream(fileName)) {
                    ZipEntry entry = new ZipEntry(name);
                    zout.putNextEntry(entry);
                    byte[] buffer = new byte[fis.available()];
                    fis.read(buffer);
                    zout.write(buffer);
                    zout.closeEntry();
                    ++i;
                } catch (IOException exc) {
                    System.out.println(exc.getMessage());
                    return false;
                }
            }
            return true;
        } catch (IOException exc) {
            System.out.println(exc.getMessage());
            return false;
        }
    }

    public static boolean saveGame(GameProgress GameProgress, String fullFileName) {
        try (FileOutputStream fos = new FileOutputStream(fullFileName);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(GameProgress);
            return true;
        } catch (IOException exc) {
            System.out.println(exc.getMessage());
            return false;
        }
    }

    public static boolean tryMkdir(File file) {
        logger.append(getDate() + "  ");
        if (file.mkdir()) {
            logger.append("Создана директория " + file.getAbsolutePath());
            logger.append("\n");
            return true;
        }
        logger.append("Не получилось создать директорию " + file.getAbsolutePath());
        logger.append("\n");
        return false;
    }

    public static boolean tryCreateNewFile(File dir, String nameFile) {
        logger.append(getDate() + "  ");
        try {
            new File(dir, nameFile).createNewFile();
        } catch (IOException exc) {
            logger.append("При создании файла " + nameFile + " возникло исключение: " + exc.getMessage());
            logger.append("\n");
            return false;
        }
        logger.append("Создан файл " + nameFile + " в директории " + dir.getAbsolutePath());
        logger.append("\n");
        return true;
    }

    public static String getDate() {
        Calendar calendar = new GregorianCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy H:m:s");
        return dateFormat.format(calendar.getTime());
    }
}
