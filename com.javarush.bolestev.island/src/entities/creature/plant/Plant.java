package entities.creature.plant;

import entities.Cell;

public class Plant {
    private double weight;

    // Конструктор
    public Plant(double initialWeight) {
        this.weight = initialWeight;
    }

    // Увеличение массы растения
    public void addWeight(double additionalWeight) {
        this.weight += additionalWeight;
    }

    // Уменьшение массы растения (например, при поедании)
    public void reduceWeight(double amount) {
        this.weight -= amount;
        if (this.weight < 0) {
            this.weight = 0;
        }
    }
    public void increaseWeight(double weight) {
        this.weight += weight;
    }
    // Получение текущей массы растения
    public double getWeight() {
        return weight;
    }

}
