package entities;

import entities.creature.plant.Plant;
import entities.сonfig.AnimalConfig;
import entities.сonfig.AnimalConfigLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Cell {
    private final int row;
    private final int col;
    private final Island island;
    private final List<Animal> animals = new CopyOnWriteArrayList<>();
    private final List<Plant> plants = new CopyOnWriteArrayList<>();

    // Конструктор
    public Cell(int row, int col, Island island) {
        this.row = row;
        this.col = col;
        this.island = island;
    }

    // Получение строки клетки
    public int getRow() {
        return row;
    }

    // Получение столбца клетки
    public int getCol() {
        return col;
    }

    // Получение острова, к которому относится клетка
    public Island getIsland() {
        return island;
    }

    // Добавление животного в клетку
    public synchronized void addAnimal(Animal animal) {
        animals.add(animal);
    }

    // Добавление растения в клетку
    public void addPlant(Plant plant) {
        plants.add(plant);
    }

    // Удаление животного из клетки
    public synchronized void removeAnimal(Animal animal) {
        animals.remove(animal);
    }

    // Удаление растения из клетки
    public void removeDeadPlants() {
        plants.removeIf(plant -> {
            if (plant.getWeight() <= 0) {
                System.out.println("🌱 Растение удалено из клетки [" + row + "][" + col + "] из-за недостатка веса.");
                return true; // Удаляем растение с нулевым или отрицательным весом
            }
            return false;
        });
    }

    // Удаление всех мёртвых животных из клетки
    public void removeDeadAnimals() {
        animals.removeIf(animal -> {
            if (!animal.isAlive()) {
                System.out.println("❌ " + animal.getEmoji() + " удалён из клетки [" + row + "][" + col + "] из-за смерти.");
                return true; // Удаляем мёртвое животное
            }
            return false;
        });
    }

    public synchronized List<Animal> getAnimals() {
        return new ArrayList<>(animals); // Возвращаем копию для избежания изменений
    }

    public synchronized List<Plant> getPlants() {
        return new ArrayList<>(plants); // Возвращаем копию для избежания изменений
    }

    // Проверка, можно ли добавить животное в клетку
    public boolean canAddAnimal(Animal animal) {
        long count = countAnimalsOfType(animal.getClass());
        return count < animal.getMaxOnCell();
    }

    // Подсчёт количества животных определённого типа
    public long countAnimalsOfType(Class<? extends Animal> type) {
        return animals.stream()
                .filter(type::isInstance) // Проверяем соответствие класса
                .count();
    }

    public int getMaxAnimalsPerType(Class<? extends Animal> animalClass) {
        String type = animalClass.getSimpleName();
        AnimalConfig config = AnimalConfigLoader.getAnimalConfig(type);
        return config != null ? config.getMaxOnCell() : Integer.MAX_VALUE;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        // Добавляем животных
        if (!animals.isEmpty()) {
            animals.forEach(animal -> builder.append(animal.getEmoji()).append(" "));
        }

        // Добавляем растения
        if (!plants.isEmpty()) {
            builder.append("🌱").append(plants.size()).append(" ");
        }

        return builder.toString();
    }

    public void growPlants() {
        plants.forEach(plant -> {
            plant.increaseWeight(0.1); // Увеличиваем вес растения
            System.out.println("🌱 Растение в клетке [" + row + "][" + col + "] выросло на 0.1 кг.");
        });
    }

    public synchronized void processActions() {
        animals.forEach(animal -> {
            if (!animal.isAlive()) {
                System.out.println("❌ " + animal.getEmoji() + " пропускает действия, так как оно мёртвое.");
                return;
            }

            animal.reduceHunger();
            System.out.println(animal.getEmoji() + " уменьшил уровень сытости. Текущая сытость: " + animal.hungerLevel);

            if (animal.isHungry()) {
                System.out.println(animal.getEmoji() + " голодно и ищет еду.");
                animal.eat();
            } else {
                System.out.println(animal.getEmoji() + " не голодно и ищет возможность размножиться.");
                animal.move();
                animal.reproduce();
            }
        });

        // Удаляем мёртвых животных и растений после действий
        removeDeadAnimals();
        removeDeadPlants();
    }
}
