package ru.itis.unfa11503;

import java.util.*;
import java.io.*;

public class BTreeMain {
    private static List<Long> insertTimes = new ArrayList<>();
    private static List<Long> insertOps = new ArrayList<>();
    private static List<Long> searchTimes = new ArrayList<>();
    private static List<Long> searchOps = new ArrayList<>();
    private static List<Long> deleteTimes = new ArrayList<>();
    private static List<Long> deleteOps = new ArrayList<>();

    public static void main(String[] args) {
        int[] randomNumbers = generateRandomArray(10000);

        BTree tree = new BTree(3);

        //Вставка 10000 элементов
        for (int num : randomNumbers) {
            BTree.Stats stats = tree.insertMeasured(num);
            insertTimes.add(stats.nanos);
            insertOps.add(stats.operations);
        }

        //Поиск 100 элементов
        int[] searchElements = selectRandomElements(randomNumbers, 100);
        for (int num : searchElements) {
            BTree.Stats stats = tree.searchMeasured(num);
            searchTimes.add(stats.nanos);
            searchOps.add(stats.operations);
        }

        //Удаление 1000 элементов
        int[] deleteElements = selectRandomElements(randomNumbers, 1000);
        for (int num : deleteElements) {
            BTree.Stats stats = tree.deleteMeasured(num);
            deleteTimes.add(stats.nanos);
            deleteOps.add(stats.operations);
        }

        saveResultsToFiles();
        printAverages();
    }

    private static int[] generateRandomArray(int size) {
        int[] arr = new int[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            arr[i] = random.nextInt(200000);
        }
        return arr;
    }

    private static int[] selectRandomElements(int[] source, int count) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < source.length; i++) {
            indices.add(i);
        }
        Collections.shuffle(indices);

        int[] result = new int[count];
        for (int i = 0; i < count; i++) {
            result[i] = source[indices.get(i)];
        }
        return result;
    }

    private static void printAverages() {
        System.out.println();
        System.out.println("СРЕДНИЕ ЗНАЧЕНИЯ");
        System.out.println("Вставка : время = " + Math.round(average(insertTimes)) + " нс, операции = " + Math.round(average(insertOps)));
        System.out.println("Поиск : время = " + Math.round(average(searchTimes)) + " нс, операции = " + Math.round(average(searchOps)));
        System.out.println("Удаление : время = " + Math.round(average(deleteTimes)) + " нс, операции = " + Math.round(average(deleteOps)));
    }

    private static void saveResultsToFiles() {
        try (PrintWriter writer = new PrintWriter(new File("insert_results.csv"))) {
            writer.println("Номер операции,Время(нс),Количество операций");
            for (int i = 0; i < insertTimes.size(); i++) {
                writer.println((i + 1) + " , " + insertTimes.get(i) + " , " + insertOps.get(i));
            }
            System.out.println("Результаты вставки сохранены в insert_results.csv");
        } catch (FileNotFoundException e) {
            System.err.println("Ошибка сохранения insert_results.csv: " + e.getMessage());
        }

        try (PrintWriter writer = new PrintWriter(new File("search_results.csv"))) {
            writer.println("Номер операции,Время(нс),Количество операций");
            for (int i = 0; i < searchTimes.size(); i++) {
                writer.println((i + 1) + " , " + searchTimes.get(i) + " , " + searchOps.get(i));
            }
            System.out.println("Результаты поиска сохранены в search_results.csv");
        } catch (FileNotFoundException e) {
            System.err.println("Ошибка сохранения search_results.csv: " + e.getMessage());
        }

        try (PrintWriter writer = new PrintWriter(new File("delete_results.csv"))) {
            writer.println("Номер операции,Время(нс),Количество операций");
            for (int i = 0; i < deleteTimes.size(); i++) {
                writer.println((i + 1) + " , " + deleteTimes.get(i) + " , " + deleteOps.get(i));
            }
            System.out.println("Результаты удаления сохранены в delete_results.csv");
        } catch (FileNotFoundException e) {
            System.err.println("Ошибка сохранения delete_results.csv: " + e.getMessage());
        }
    }

    private static double average(List<Long> list) {
        if (list.isEmpty()) return 0;
        long sum = 0;
        for (Long val : list) {
            sum += val;
        }
        return (double) sum / list.size();
    }
}

