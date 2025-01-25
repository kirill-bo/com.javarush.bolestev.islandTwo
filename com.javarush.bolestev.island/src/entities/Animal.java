package entities;

import entities.creature.animal.herbivore.Herbivore;
import entities.creature.animal.predator.Predator;
import entities.creature.animal.оmnivore.Omnivore;
import entities.сonfig.AnimalFactory;
import entities.сonfig.AnimalConfigLoader;


import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Animal {
    // Глобальная таблица вероятностей поедания
    protected static final Map<Class<? extends Animal>, Map<Class<? extends Animal>, Double>> eatingProbabilities = new HashMap<>();

    // Атрибуты животного
    protected double hungerLevel;
    public final double weight;
    public final int maxOnCell;
    public final int speed;
    public double foodNeed;
    public final String emoji;
    public boolean isAlive = true; // Жив ли организм

    public Cell currentCell; // Текущая клетка, в которой находится животное

    public final List<String> actions; // Лог действий животного

    public Animal(double weight, int maxOnCell, int speed, double foodNeed, String emoji) {
        this.weight = weight;
        this.maxOnCell = maxOnCell;
        this.speed = speed;
        this.foodNeed = foodNeed;
        this.emoji = emoji;
        this.actions = new java.util.ArrayList<>();
    }
    public void reduceHunger() {
        hungerLevel -= hungerLevel * 0.1; // Уменьшение на 10%
        if (hungerLevel <= 0) {
            die(); // Животное умирает при голоде
        }
        System.out.println(emoji + " уменьшил уровень сытости. Текущая сытость: " + hungerLevel);
    }

    public boolean isHungry() {
        return hungerLevel  <= 0; // Например, если сытость ниже 80%, животное голодно
    }
    // Геттеры и сеттеры
    public double getWeight() {
        return weight;
    }

    public int getMaxOnCell() {
        return maxOnCell;
    }

    public int getSpeed() {
        return speed;
    }

    public double getFoodNeed() {
        return foodNeed;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setCurrentCell(Cell currentCell) {
        this.currentCell = currentCell;
    }

    public boolean isAlive() {
        return isAlive;
    }

    // Основные методы поведения
    public void eat() {
        if (!isAlive || currentCell == null) return;

        if (isHungry()) {
            if (this instanceof Predator || this instanceof Omnivore) {
                currentCell.getAnimals().stream()
                        .filter(prey -> prey != this)
                        .findFirst()
                        .ifPresent(prey -> {
                            double chance = AnimalConfigLoader.getEatingProbability(this.getClass().getSimpleName(), prey.getClass().getSimpleName());
                            if (ThreadLocalRandom.current().nextDouble() < chance) {
                                currentCell.removeAnimal(prey);
                                consumeFood(prey.getWeight());
                                System.out.println(getEmoji() + " съел " + prey.getEmoji());
                            }
                        });
            }
            if (this instanceof Herbivore || this instanceof Omnivore) {
                currentCell.getPlants().stream().findFirst().ifPresent(plant -> {
                    double foodEaten = Math.min(foodNeed - hungerLevel, plant.getWeight());
                    consumeFood(foodEaten);
                    plant.reduceWeight(foodEaten);
                    if (plant.getWeight() <= 0) currentCell.removeDeadPlants();
                    System.out.println(getEmoji() + " съел растение на " + foodEaten + " кг");
                });
            }
        }
    }

    public void reproduce() {
        if (currentCell.countAnimalsOfType(this.getClass()) < currentCell.getMaxAnimalsPerType(this.getClass())) {
            Animal offspring = AnimalFactory.createAnimal(this.getClass().getSimpleName());
            offspring.setCurrentCell(currentCell);
            currentCell.addAnimal(offspring);
            System.out.println(getEmoji() + " размножился!");
        }
    }

    public boolean move() {
        if (!isAlive || currentCell == null || speed == 0) return false;

        // Запоминаем начальную клетку
        int originalRow = currentCell.getRow();
        int originalCol = currentCell.getCol();

        for (int attempt = 0; attempt < 10; attempt++) { // Максимум 10 попыток перемещения
            int deltaRow = ThreadLocalRandom.current().nextInt(-speed, speed + 1);
            int deltaCol = ThreadLocalRandom.current().nextInt(-speed, speed + 1);

            // Пропускаем попытку остаться на месте
            if (deltaRow == 0 && deltaCol == 0) continue;

            int newRow = originalRow + deltaRow;
            int newCol = originalCol + deltaCol;

            // Проверяем, находится ли новая клетка в пределах границ
            if (!currentCell.getIsland().isWithinBounds(newRow, newCol)) continue;

            Cell newCell = currentCell.getIsland().getCell(newRow, newCol);

            // Проверяем, что новая клетка может вместить животное
            if (newCell != null && newCell.canAddAnimal(this)) {
                // Успешное перемещение
                currentCell.removeAnimal(this); // Убираем из текущей клетки
                newCell.addAnimal(this);       // Добавляем в новую клетку
                this.setCurrentCell(newCell);  // Обновляем текущую клетку

                System.out.println(getEmoji() + " переместился из клетки [" +
                        originalRow + "][" + originalCol + "] на клетку [" +
                        newRow + "][" + newCol + "]");
                return true;
            }
        }

        // Сообщение о неудаче
        System.out.println(getEmoji() + " остался в клетке [" +
                originalRow + "][" + originalCol + "] и не смог переместиться.");
        return false; // Все попытки перемещения не удались
    }

    public void die() {
        isAlive = false;
        actions.add("Умер");
    }

    public static void addEatingProbabilities(Class<? extends Animal> predator, Map<Class<? extends Animal>, Double> probabilities) {
        eatingProbabilities.put(predator, probabilities);
    }

    public static double getEatingProbability(Class<? extends Animal> predator, Class<? extends Animal> prey) {
        return eatingProbabilities.getOrDefault(predator, Map.of()).getOrDefault(prey, 0.0);
    }
    public void consumeFood(double foodEaten) {
        foodNeed -= foodEaten;
        if (foodNeed <= 0) {
            foodNeed = 0;
        }
    }
    public void performActions() {
        if (!isAlive || currentCell == null) return;

        boolean moved = move(); // Попытка перемещения

        if (moved) {
            eat();       // Питание доступно только после успешного перемещения
            reproduce(); // Размножение доступно только после успешного перемещения
        } else {
            System.out.println(getEmoji() + " остался в клетке [" +
                    currentCell.getRow() + "][" + currentCell.getCol() + "] и пропустил действия.");
        }
    }

    public abstract Animal createNewInstance();
}