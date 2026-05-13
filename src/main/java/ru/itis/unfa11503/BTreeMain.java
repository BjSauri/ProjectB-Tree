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

        for (int num : randomNumbers) {
            BTree.Stats stats = tree.insertMeasured(num);
            insertTimes.add(stats.nanos);
            insertOps.add(stats.operations);
        }

        int[] searchElements = selectRandomElements(randomNumbers, 100);
        for (int num : searchElements) {
            BTree.Stats stats = tree.searchMeasured(num);
            searchTimes.add(stats.nanos);
            searchOps.add(stats.operations);
        }

        int[] deleteElements = selectRandomElements(randomNumbers, 1000);
        for (int num : deleteElements) {
            BTree.Stats stats = tree.deleteMeasured(num);
            deleteTimes.add(stats.nanos);
            deleteOps.add(stats.operations);
        }

        saveResultsToFile();
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
        System.out.println("СРЕДНИЕ ЗНАЧЕНИЯ");
        System.out.println("Вставка : время = " + average(insertTimes) + " нс, операции = " + average(insertOps));
        System.out.println("Поиск : время = " + average(searchTimes) + " нс, операции = " + average(searchOps));
        System.out.println("Удаление : время = " + average(deleteTimes) + "нс, операции = " + average(deleteOps));
    }

    private static void saveResultsToFile() {
        try (PrintWriter writer = new PrintWriter(new File("btree_results.csv"))) {
            writer.println("Тип,Время(нс),Операции");

            for (int i = 0; i < insertTimes.size(); i++) {
                writer.println("Вставка " + insertTimes.get(i) + " " + insertOps.get(i));
            }
            for (int i = 0; i < searchTimes.size(); i++) {
                writer.println("Поиск " + searchTimes.get(i) + " " + searchOps.get(i));
            }
            for (int i = 0; i < deleteTimes.size(); i++) {
                writer.println("Удаление " + deleteTimes.get(i) + " " + deleteOps.get(i));
            }
        } catch (FileNotFoundException e) {
            System.err.println("Ошибка сохранения: " + e.getMessage());
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

