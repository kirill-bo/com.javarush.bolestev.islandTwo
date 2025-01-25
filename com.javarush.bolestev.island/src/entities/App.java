package entities;

import entities.сonfig.AnimalConfigLoader;

public class App {
    public static void main(String[] args) {
        // Путь к файлам конфигурации
        String islandConfigPath = "com.javarush.bolestev.island/src/entities/resources/islandConfig.json";
        String animalsConfigPath = "com.javarush.bolestev.island/src/entities/resources/animals.json";

        try {
            // Загружаем конфигурации животных
            AnimalConfigLoader.loadAnimalConfigs(animalsConfigPath);

            // Создаём симулятор
            Simulator simulator = new Simulator(islandConfigPath);

            // Запускаем симуляцию
            simulator.start();
        } catch (Exception e) {
            // Обработка ошибок
            System.err.println("Ошибка при запуске симуляции: " + e.getMessage());
            e.printStackTrace();
        }
    }
}