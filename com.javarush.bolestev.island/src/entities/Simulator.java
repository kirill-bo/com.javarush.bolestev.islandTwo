package entities;

import entities.creature.animal.herbivore.Herbivore;
import entities.creature.animal.predator.Predator;
import entities.сonfig.ConfigLoader;
import entities.сonfig.IslandConfig;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Simulator {
    private final Island island;
    private final int cycleDuration;
    private final int simulationCycles;
    private final ExecutorService executorService;

    public Simulator(String configPath) {
        IslandConfig config = ConfigLoader.loadIslandConfig(configPath);
        this.island = new Island(configPath); // Инициализация острова
        this.cycleDuration = config.getCycleDuration();
        this.simulationCycles = config.getSimulationCycles();
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    private boolean isSimulationOver() {
        long herbivores = island.getAllCells().stream()
                .flatMap(cell -> cell.getAnimals().stream())
                .filter(animal -> animal instanceof Herbivore)
                .count();

        long plants = island.getAllCells().stream()
                .flatMap(cell -> cell.getPlants().stream())
                .count();

        long predators = island.getAllCells().stream()
                .flatMap(cell -> cell.getAnimals().stream())
                .filter(animal -> animal instanceof Predator)
                .count();

        // Условие окончания симуляции
        return herbivores == 0 || plants == 0 || predators == 0 || (predators > 0 && herbivores == 0);
    }

    public void start() {
        try {
            for (int cycle = 1; !isSimulationOver() && cycle <= simulationCycles; cycle++) {
                System.out.println("Цикл: " + cycle);

                // Обработка клеток
                List<Callable<Void>> tasks = island.getAllCells().stream()
                        .map(cell -> (Callable<Void>) () -> {
                            synchronized (cell) { // Синхронизация на уровне клетки
                                cell.processActions();
                            }
                            Thread.sleep(10000); // Задержка между обработкой клеток
                            return null;
                        }).collect(Collectors.toList());

                executorService.invokeAll(tasks); // Выполнение задач

                // Вывод состояния острова
                island.printIsland();

                // Общая пауза между циклами
                Thread.sleep(cycleDuration);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
            printFinalReport();
        }
    }

    private void printFinalReport() {
        long totalHerbivores = island.getAllCells().stream()
                .flatMap(cell -> cell.getAnimals().stream())
                .filter(animal -> animal instanceof Herbivore)
                .count();

        long totalPlants = island.getAllCells().stream()
                .flatMap(cell -> cell.getPlants().stream())
                .count();

        long totalPredators = island.getAllCells().stream()
                .flatMap(cell -> cell.getAnimals().stream())
                .filter(animal -> animal instanceof Predator)
                .count();

        System.out.println("Финальный отчёт:");
        System.out.println("Травоядные: " + totalHerbivores);
        System.out.println("Хищники: " + totalPredators);
        System.out.println("Растения: " + totalPlants);
    }
}