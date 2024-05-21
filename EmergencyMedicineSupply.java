import java.util.*;

class Medicine {
    String name;
    double price;

    public Medicine(String name, double price) {
        this.name = name;
        this.price = price;
    }
}

class Pharmacy {
    List<Medicine> medicines;

    public Pharmacy() {
        this.medicines = new ArrayList<>();
        medicines.add(new Medicine("paracetamol", 10.0));
        medicines.add(new Medicine("aspirin", 15.0));
        medicines.add(new Medicine("smc", 8.0));
        medicines.add(new Medicine("pericel", 10.0));
        medicines.add(new Medicine("napa", 15.0));
        medicines.add(new Medicine("metro", 8.0));
        medicines.add(new Medicine("glutacol", 8.0));
        medicines.add(new Medicine("finix", 10.0));
        medicines.add(new Medicine("lorak", 15.0));
        medicines.add(new Medicine("tulsi", 8.0));
    }
}

class EmergencyMedicineSupply {
    Scanner scanner;
    Pharmacy pharmacy;
    double[][] distances;
    Map<String, Integer> nodeIndices;

    public EmergencyMedicineSupply() {
        this.scanner = new Scanner(System.in);
        this.pharmacy = new Pharmacy();
        this.distances = new double[4][4];
        this.nodeIndices = new HashMap<>();
        initializeGraph();
    }

    private void initializeGraph() {
        nodeIndices.put("hospital", 0);
        nodeIndices.put("pharmacy1", 1);
        nodeIndices.put("pharmacy2", 2);
        nodeIndices.put("pharmacy3", 3);
    }

    public void login() {
        System.out.println("Enter username: ");
        String username = scanner.next();
        System.out.println("Enter password: ");
        String password = scanner.next();
        if ("u".equals(username) && "p".equals(password)) {
            System.out.println("Login successful!");
        } else {
            System.out.println("Login failed. Exiting program.");
            System.exit(0);
        }
    }

    public void addEdge(String node1, String node2) {
        int index1 = nodeIndices.get(node1);
        int index2 = nodeIndices.get(node2);
        distances[index1][index2] = 1.0;
        distances[index2][index1] = 1.0;
    }

    public void addDistance(String node1, String node2, double distance) {
        int index1 = nodeIndices.get(node1);
        int index2 = nodeIndices.get(node2);
        distances[index1][index2] = distance;
        distances[index2][index1] = distance;
    }

    private List<String> getNeighbours(String node) {
        int index = nodeIndices.get(node);
        List<String> neighbours = new ArrayList<>();
        for (int i = 0; i < distances[index].length; i++) {
            if (distances[index][i] > 0) {
                neighbours.add(getNodeName(i));
            }
        }
        return neighbours;
    }

    private String getNodeName(int index) {
        for (Map.Entry<String, Integer> entry : nodeIndices.entrySet()) {
            if (entry.getValue() == index) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("Invalid node index: " + index);
    }

    private List<String> dfsShortestPath(String start, String end, Set<String> visited, List<String> currentPath,
            List<String> shortestPath) {
        visited.add(start);
        currentPath.add(start);
        if (start.equals(end)) {
            if (shortestPath.isEmpty() || currentPath.size() < shortestPath.size()) {
                shortestPath.clear();
                shortestPath.addAll(currentPath);
            }
        } else {
            for (String neighbor : getNeighbours(start)) {
                if (!visited.contains(neighbor)) {
                    dfsShortestPath(neighbor, end, visited, currentPath, shortestPath);
                }
            }
        }
        visited.remove(start);
        currentPath.remove(currentPath.size() - 1);
        return shortestPath;
    }

    public void findShortestRoute() {
        System.out.println("Enter the destination (e.g., pharmacy1, pharmacy2, pharmacy3):");
        String destination = scanner.next();
        System.out.print("Shortest route from hospital to " + destination + ": ");
        Set<String> visited = new HashSet<>();
        List<String> shortestPath = new ArrayList<>();
        dfsShortestPath("hospital", destination, visited, new ArrayList<>(), shortestPath);
        System.out.println();
        System.out.print("Path: ");
        double totalDistance = 0.0;
        for (String node : shortestPath) {
            System.out.print(node + " -> ");
        }
        System.out.println(destination);
        for (int i = 0; i < shortestPath.size() - 1; i++) {
            String currentNode = shortestPath.get(i);
            String nextNode = shortestPath.get(i + 1);
            totalDistance += distances[nodeIndices.get(currentNode)][nodeIndices.get(nextNode)];
        }
        System.out.println("Total Distance: " + totalDistance + " units");
    }

    public void run() {
        while (true) {
            System.out.println("1. Buy Medicine");
            System.out.println("2. Generate Bill");
            System.out.println("3. Find Shortest Route");
            System.out.println("4. Exit");
            int option = scanner.nextInt();
            switch (option) {
                case 1:
                    buyMedicine();
                    break;
                case 2:
                    generateBill();
                    break;
                case 3:
                    findShortestRoute();
                    break;
                case 4:
                    System.out.println("Exiting program.");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    public void buyMedicine() {
        System.out.println("1. Show Medicines");
        System.out.println("2. Search Medicine");
        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                showMedicines();
                break;
            case 2:
                searchMedicine();
                break;
            default:
                System.out.println("Invalid choice");
        }
    }

    public void showMedicines() {
        mergeSort(pharmacy.medicines, Comparator.comparing(medicine -> medicine.name));
        System.out.println("Medicines available:");
        for (int i = 0; i < Math.min(20, pharmacy.medicines.size()); i++) {
            Medicine medicine = pharmacy.medicines.get(i);
            System.out.println((i + 1) + ". " + medicine.name + " - $" + medicine.price);
        }
    }

    private void mergeSort(List<Medicine> list, Comparator<Medicine> comparator) {
        if (list.size() > 1) {
            int mid = list.size() / 2;
            List<Medicine> left = new ArrayList<>(list.subList(0, mid));
            List<Medicine> right = new ArrayList<>(list.subList(mid, list.size()));
            mergeSort(left, comparator);
            mergeSort(right, comparator);
            merge(list, left, right, comparator);
        }
    }

    private void merge(List<Medicine> result, List<Medicine> left, List<Medicine> right,
            Comparator<Medicine> comparator) {
        int i = 0, j = 0, k = 0;
        while (i < left.size() && j < right.size()) {
            if (comparator.compare(left.get(i), right.get(j)) <= 0) {
                result.set(k++, left.get(i++));
            } else {
                result.set(k++, right.get(j++));
            }
        }
        while (i < left.size()) {
            result.set(k++, left.get(i++));
        }
        while (j < right.size()) {
            result.set(k++, right.get(j++));
        }
    }

    private int lcsSearch(List<Medicine> medicines, String searchName) {
        int maxLength = -1;
        int index = -1;

        for (int i = 0; i < medicines.size(); i++) {
            String medicineName = medicines.get(i).name;
            int[][] dp = new int[medicineName.length() + 1][searchName.length() + 1];

            for (int j = 1; j <= medicineName.length(); j++) {
                for (int k = 1; k <= searchName.length(); k++) {
                    if (medicineName.charAt(j - 1) == searchName.charAt(k - 1)) {
                        dp[j][k] = dp[j - 1][k - 1] + 1;
                    } else {
                        dp[j][k] = Math.max(dp[j - 1][k], dp[j][k - 1]);
                    }
                }
            }

            if (dp[medicineName.length()][searchName.length()] > maxLength
                    && dp[medicineName.length()][searchName.length()] == searchName.length()) {
                maxLength = dp[medicineName.length()][searchName.length()];
                index = i;
            }
        }

        return (maxLength == 0) ? -1 : index;
    }

    public void searchMedicine() {
        mergeSort(pharmacy.medicines, Comparator.comparing(medicine -> medicine.name));
        System.out.println("Enter the medicine name to search:");
        String searchName = scanner.next();
        int index = lcsSearch(pharmacy.medicines, searchName);
        if (index != -1) {
            Medicine medicine = pharmacy.medicines.get(index);
            System.out.println("Medicine found: " + medicine.name + " - $" + medicine.price);
        } else {
            System.out.println("Medicine not found.");
        }
    }

    public void generateBill() {
        System.out.println("Select the medicine by number:");
        for (int i = 0; i < Math.min(20, pharmacy.medicines.size()); i++) {
            Medicine medicine = pharmacy.medicines.get(i);
            System.out.println((i + 1) + ". " + medicine.name + " - $" + medicine.price);
        }
        List<Medicine> selectedMedicines = new ArrayList<>();
        System.out.println("Enter the medicine numbers (separated by commas):");
        String[] selectedNumbers = scanner.next().split(",");
        for (String number : selectedNumbers) {
            int index = Integer.parseInt(number) - 1;
            selectedMedicines.add(pharmacy.medicines.get(index));
        }
        double totalBill = selectedMedicines.stream().mapToDouble(medicine -> medicine.price).sum();
        System.out.println("Total Bill: $" + totalBill);
    }

    public static void main(String[] args) {
        EmergencyMedicineSupply ems = new EmergencyMedicineSupply();
        ems.login();
        ems.addEdge("hospital", "pharmacy1");
        ems.addEdge("hospital", "pharmacy2");

        ems.addEdge("hospital", "pharmacy3");
        ems.addDistance("hospital", "pharmacy1", 6.0);
        ems.addDistance("hospital", "pharmacy2", 7.0);
        ems.addDistance("hospital", "pharmacy3", 8.0);
        ems.addDistance("pharmacy1", "pharmacy2", 5.5);
        ems.addDistance("pharmacy1", "pharmacy3", 6.0);
        ems.addDistance("pharmacy2", "pharmacy3", 9.0);
        ems.addDistance("hospital", "pharmacy1", 6.5);
        ems.addDistance("hospital", "pharmacy2", -20.0);
        ems.addDistance("hospital", "pharmacy3", 6.5);
        ems.addDistance("hospital", "pharmacy1", 5.0);
        ems.run();
    }
}