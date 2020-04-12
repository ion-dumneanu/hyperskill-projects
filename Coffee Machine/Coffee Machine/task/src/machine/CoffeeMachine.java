package machine;

import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.System.*;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static machine.CoffeeMachine.CoffeeTypeInfoHolder.*;


public class CoffeeMachine {

    private int milk;
    private int water;
    private int beans;
    private int cups;

    private int money;

    private ICoffeeMachineState coffeeMachineState;

    private CoffeeMachine(int water, int milk, int beans, int cups, int money) {

        this.milk = milk;
        this.water = water;
        this.beans = beans;
        this.cups = cups;
        this.money = money;

        this.coffeeMachineState = new ChooseAction(this);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(in);
        final CoffeeMachine coffeeMachine = new CoffeeMachine(400, 540, 120, 9, 550);

        while (true) {
            String outputState = coffeeMachine.getOutputState();
            if (outputState != null) {
                out.println();
                out.println(outputState);
            }
            coffeeMachine.execute(scanner.next());
        }

    }

    public int getMilk() {
        return milk;
    }

    public int getWater() {
        return water;
    }

    public int getBeans() {
        return beans;
    }

    public int getCups() {
        return cups;
    }

    public int getMoney() {
        return money;
    }

    public void execute(String input) {
        this.coffeeMachineState = this.coffeeMachineState.execute(input);
    }

    private String getOutputState() {
        return this.coffeeMachineState.getOutputState();
    }

    private String canNotProduce(CoffeeTypeInfoHolder coffeeTypeInfoHolder) {

        if (!canProduceWithAvailableWater(coffeeTypeInfoHolder)) {
            return "water";
        }

        if (!canProduceWithAvailableMilk(coffeeTypeInfoHolder)) {
            return "milk";
        }

        if (!canProduceWithAvailableBeans(coffeeTypeInfoHolder)) {
            return "beans";
        }

        if (this.cups == 0) {
            return "cups";
        }

        return null;
    }

    private boolean canProduceWithAvailableBeans(CoffeeTypeInfoHolder coffeeTypeInfoHolder) {
        return this.beans > coffeeTypeInfoHolder.getBeans();
    }

    private boolean canProduceWithAvailableWater(CoffeeTypeInfoHolder coffeeTypeInfoHolder) {
        return this.water > coffeeTypeInfoHolder.getWater();
    }

    private boolean canProduceWithAvailableMilk(CoffeeTypeInfoHolder coffeeTypeInfoHolder) {
        final boolean[] result = {true};
        coffeeTypeInfoHolder.withMilk().ifPresent(coffeeTypeInfoHolder1 -> {
            result[0] = this.milk > coffeeTypeInfoHolder1.getMilk();
        });
        return result[0];
    }

    public void buy(CoffeeTypeInfoHolder coffeeTypeInfoHolder) {
        this.water -= coffeeTypeInfoHolder.getWater();
        this.milk -= coffeeTypeInfoHolder.getMilk();
        this.beans -= coffeeTypeInfoHolder.getBeans();
        this.cups -= 1;
        this.money += coffeeTypeInfoHolder.getPrice();
    }

    public void take() {
        this.money = 0;
    }

    private void addCups(int nextInt) {
        this.cups += nextInt;
    }

    private void addBeans(int nextInt) {
        this.beans += nextInt;
    }

    private void addMilk(int nextInt) {
        this.milk += nextInt;
    }

    private void addWatter(int nextInt) {
        this.water += nextInt;
    }

    enum CoffeeTypeInfoHolder {

        ESSPRESSO(250, 0, 16, 4),
        LATTE(350, 75, 20, 7),
        CAPPUCINO(200, 100, 12, 6);

        private final int water;
        private final int milk;
        private final int beans;
        private final int price;

        CoffeeTypeInfoHolder(int water, int milk, int beans, int price) {
            this.water = water;
            this.milk = milk;
            this.beans = beans;
            this.price = price;
        }

        public int getWater() {
            return water;
        }

        public int getMilk() {
            return milk;
        }

        public int getBeans() {
            return beans;
        }

        public int getPrice() {
            return price;
        }

        public Optional<CoffeeTypeInfoHolder> withMilk() {
            if (milk == 0) {
                return empty();
            }
            ;
            return of(this);
        }
    }

    interface ICoffeeMachineState {

        ICoffeeMachineState execute(String input);

        String getOutputState();
    }

    interface IFillCoffeeElement {

        void add(IFillElementCoffeeMachineState fillElementChain);

    }

    interface IFillElementCoffeeMachineState extends ICoffeeMachineState, IFillCoffeeElement {
    }

    public static class ChooseAction implements ICoffeeMachineState {
        private CoffeeMachine instance;

        public ChooseAction(CoffeeMachine instance) {
            this.instance = instance;
        }

        @Override
        public ICoffeeMachineState execute(String action) {
            switch (action) {
                case "exit":
                    exit(0);
                    break;
                case "remaining":
                    instance.printState();
                    return new ChooseAction(instance);
                case "buy":
                    return new ChooseCoffeeType(instance);
                case "take":
                    int cashAvailable = instance.getMoney();
                    instance.take();
                    out.printf("I gave you $%d\n", cashAvailable);
                    return new ChooseAction(instance);
                case "fill":
                    IFillElementCoffeeMachineState fillElementChain = new FillCoffeeElement("Write how many ml of water do you want to add:", instance::addWatter);
                    fillElementChain.add(new FillCoffeeElement("Write how many ml of milk do you want to add:", instance::addMilk));
                    fillElementChain.add(new FillCoffeeElement("Write how many grams of coffee beans do you want to add:", instance::addBeans));
                    fillElementChain.add(new FillCoffeeElement("Write how many disposable cups of coffee do you want to add:", instance::addCups));
                    fillElementChain.add(new BackToActionFillElement(new ChooseAction(instance)));

                    return fillElementChain;
                default:
                    out.println();
                    out.printf("'%s' is not a valid selection!\n", action);
            }
            return new ChooseCoffeeType(instance);
        }

        @Override
        public String getOutputState() {
            return "Write action (buy, fill, take, remaining, exit):";
        }
    }

    public void printState() {
        out.println();
        out.println(format("The coffee machine has:\n" +
                "%d of water\n" +
                "%d of milk\n" +
                "%d of coffee beans\n" +
                "%d of disposable cups\n" +
                "$%d of money\n", this.water, this.milk, this.beans, this.cups, this.money));

    }

    public static class ChooseCoffeeType implements ICoffeeMachineState {
        private CoffeeMachine instance;

        ChooseCoffeeType(CoffeeMachine instance) {
            this.instance = instance;
        }

        static CoffeeTypeInfoHolder getCoffeeByBuyIndex(String buyIndex) {
            switch (buyIndex) {
                case "1":
                    return ESSPRESSO;
                case "2":
                    return LATTE;
                case "3":
                    return CAPPUCINO;
            }
            return null;
        }

        @Override
        public ICoffeeMachineState execute(String input) {
            if ("back".equals(input)) {
                return new ChooseAction(instance);
            }
            CoffeeTypeInfoHolder coffeeTypeInfoHolder = getCoffeeByBuyIndex(input);
            if (coffeeTypeInfoHolder == null) {
                return this;
            }

            final String canNotProduceCause = instance.canNotProduce(coffeeTypeInfoHolder);
            if (canNotProduceCause == null) {
                out.println("I have enough resources, making you a coffee!");
                instance.buy(coffeeTypeInfoHolder);
            } else {
                out.printf("Sorry, not enough %s!\n", canNotProduceCause);
            }
            return new ChooseAction(instance);
        }

        @Override
        public String getOutputState() {
            return "What do you want to buy? 1 - espresso, 2 - latte, 3 - cappuccino, back - to main menu:";
        }
    }

    public static class ProduceCoffeeState implements ICoffeeMachineState {
        private final CoffeeMachine instance;
        private final CoffeeTypeInfoHolder coffeeTypeInfoHolder;

        public ProduceCoffeeState(CoffeeMachine instance, CoffeeTypeInfoHolder coffeeTypeInfoHolder) {
            this.instance = instance;
            this.coffeeTypeInfoHolder = coffeeTypeInfoHolder;
        }

        @Override
        public ICoffeeMachineState execute(String input) {
            if (instance.canNotProduce(coffeeTypeInfoHolder) == null) {
                instance.buy(coffeeTypeInfoHolder);
            }
            return new ChooseAction(instance);
        }

        @Override
        public String getOutputState() {
            final String canProduceStatus = instance.canNotProduce(coffeeTypeInfoHolder);

            if (canProduceStatus != null) {
                return canProduceStatus;
            }
            return "I have enough resources, making you a coffee!";
        }
    }

    static class BackToActionFillElement implements IFillElementCoffeeMachineState {

        private final ICoffeeMachineState coffeeMachineState;

        BackToActionFillElement(ICoffeeMachineState coffeeMachineState) {
            this.coffeeMachineState = coffeeMachineState;
        }

        @Override
        public ICoffeeMachineState execute(String input) {
            return coffeeMachineState.execute(input);
        }

        @Override
        public String getOutputState() {
            return coffeeMachineState.getOutputState();
        }

        @Override
        public void add(IFillElementCoffeeMachineState fillElementChain) {

        }
    }

    static class FillCoffeeElement implements IFillElementCoffeeMachineState {

        private IFillElementCoffeeMachineState next;

        private String outputStream;
        private Consumer<Integer> consumer;

        public FillCoffeeElement(String outputStream, Consumer<Integer> consumer) {
            this.outputStream = outputStream;
            this.consumer = consumer;
        }

        public void add(IFillElementCoffeeMachineState fillElementChain) {
            if (this.next == null) {
                this.next = fillElementChain;
                return;
            }
            next.add(fillElementChain);
        }

        @Override
        public ICoffeeMachineState execute(String input) {
            consumer.accept(parseInt(input));
            return next;
        }

        @Override
        public String getOutputState() {
            return outputStream;
        }

    }

    static class FillCoffeeMachine implements ICoffeeMachineState {
        private final CoffeeMachine instance;

        FillCoffeeMachine(CoffeeMachine instance) {
            this.instance = instance;
        }

        @Override
        public ICoffeeMachineState execute(String input) {
            Scanner scanner = new Scanner(in);
            out.println();
            out.println("Write how many ml of water do you want to add:");
            instance.addWatter(scanner.nextInt());
            out.println("Write how many ml of milk do you want to add:");
            instance.addMilk(scanner.nextInt());
            out.println("Write how many grams of coffee beans do you want to add:");
            instance.addBeans(scanner.nextInt());
            out.println("Write how many disposable cups of coffee do you want to add:");
            instance.addCups(scanner.nextInt());

            return new ChooseAction(instance);
        }

        @Override
        public String getOutputState() {
            return null;
        }
    }


}
