import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Airlines
{
	public static void main(String[] args) {
		Scanner a = new Scanner(System.in);
		//String dataFileName = "FlightData";
		//String requestFileName = "FlightRequest";
		//Dynamic File Input
		System.out.println("Enter the name of your Flight Data file (Database): ");
		String dataFileName = a.nextLine();
		System.out.println("Enter the name of your Flight Request file: ");
		String requestFileName = a.nextLine();

		FlightGraph graph = new FlightGraph(); //Create Graph
		FlightData.parseFlightData(dataFileName, graph); //Extract File Contents
		FlightVerification planner = new FlightVerification(graph); //References Graph
		FlightRequest.parseFlightPlans(requestFileName, planner); //1. Parse 2nd Input File 2. Use DFS & Stack to find Paths
	}
}





/*

            -------------------Parse Data & Output File-------------------------

*/





// Class to hold Flight Path Data --> VertexNode creates adjacency List (Linked List of Linked Lists)
class FlightData {
	String source;
	String destination;
	double cost;
	int time;

	public FlightData() { } //Default Constructor

	public FlightData(String source, String destination, int cost, int time) {
		this.source = source;
		this.destination = destination;
		this.cost = cost;
		this.time = time;
	}

	public static void parseFlightData(String dataFileName, FlightGraph graph) {
		BufferedReader fileReader = null;
		try {
			fileReader = new BufferedReader(new FileReader(dataFileName));
			String entries = fileReader.readLine(); //Read first line of file (# of entries)
			int size = Integer.parseInt(entries);
			for (int i = 0; i < size; i++) {
				String line = fileReader.readLine(); //Parse entire entry
				if (line == null) break; //EOF
				String[] properties = line.split("\\|"); //Parse line with | delimiter
				if (properties.length != 4) { //Check that the entry in file is formatted properly
					System.out.println("Invalid line format: " + line);
					continue;
				}
				String source = properties[0].trim(); //Parse Entries Properties
				String destination = properties[1].trim(); //Trim gets rid of trailing/leading whitespaces
				int cost = Integer.parseInt(properties[2].trim());
				int time = Integer.parseInt(properties[3].trim());
				graph.addFlight(source, destination, cost, time); //Add Vertices & Edges from entry
			}
		} catch (IOException e) {
			System.out.println("Error reading file: " + e.getMessage());
			return;
		}
		if (fileReader != null) {
			try {
				fileReader.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("\n");
	}

	public void printNode() {
		System.out.println("Node: " + this.source + " -> " + this.destination + " | Cost: $" + this.cost + " | Time: " + this.time);
	}
}

// Class to parse Flight Request File
class FlightRequest {
	String source;
	String destination;
	String sortBy;

	public FlightRequest() { }

	public FlightRequest(String source, String destination, String sortBy) {
		this.source = source;
		this.destination = destination;
		this.sortBy = sortBy;
	}

	public static void parseFlightPlans(String requestFileName, FlightVerification planner) {
		BufferedReader fileReader = null;
		try {
			fileReader = new BufferedReader(new FileReader(requestFileName));
			PrintWriter writer = new PrintWriter("OutFile.txt");
			String entries = fileReader.readLine(); //Read first line of file (# of entries)
			int size = Integer.parseInt(entries);
			
			for (int i = 0; i < size; i++) {
				String line = fileReader.readLine(); //Parse entire entry
				if (line == null) break; //EOF
				String[] properties = line.split("\\|"); //Parse line with | delimiter
				
				if (properties.length != 3) { //Check that the entry in file is formatted properly
					System.out.println("Invalid line format: " + line);
					continue;
				}
				String source = properties[0].trim(); //Parse Entries Properties
				String destination = properties[1].trim(); //Trim gets rid of trailing/leading whitespaces
				String sortBy = properties[2].trim();
                
                if (sortBy.equalsIgnoreCase("T")) { //Output Message if T or C sorted
					writer.print("Flight " + (i + 1) + ": (" + source + " | " + destination + ") [Time]:\n");
					System.out.println("Flight Request " + (i + 1) + ": (" + source + " | " + destination + ") [Time]:");
				} else if(sortBy.equalsIgnoreCase("C")) {
					writer.print("Flight " + (i + 1) + ": (" + source + " | " + destination + ") [Cost]:\n");
					System.out.println("Flight Request " + (i + 1) + ": (" + source + " | " + destination + ") [Cost]:");
				}
				
				List<PathState> paths = planner.findAllPaths(source, destination); //Perform DFS
				if (paths.isEmpty()) {
					System.out.println("  No available flight plans...\n"); //If no paths are found
					writer.print("No available flight plans...\n");
					continue;
				}
				if (sortBy.equalsIgnoreCase("T")) { //Check using Comparator
					paths.sort(Comparator.comparingInt(p -> p.totalTime)); //Sort based on Time
				} else if(sortBy.equalsIgnoreCase("C")) {
					paths.sort(Comparator.comparingInt(p -> p.totalCost)); //Sort based on COst
				}
				
				for (int j = 0; j < Math.min(3, paths.size()); j++) { //Print out top 3 Paths if there are at least 3
					PathState path = paths.get(j);                    //Else print all paths
                    writer.print("Path " + (j + 1) + ": ");
					System.out.print("  Path " + (j + 1) + ": ");
					for (int k = 0; k < path.cities.size(); k++) {
						writer.print(path.cities.get(k));
						System.out.print(path.cities.get(k));
						if (k < path.cities.size() - 1) {
							writer.print(" -> ");
							System.out.print(" -> ");
						}
					}
                    writer.printf(". Time: %d Cost: $%.2f%n", path.totalTime, (double) path.totalCost);
					System.out.printf(". Time: %d Cost: $%.2f%n", path.totalTime, (double) path.totalCost);
				}
				System.out.println(); // Blank line between flight requests
				writer.println();
			}
		writer.close();
		} catch (IOException e) { //Exception Handling (Code from earlier asisgnments)
			System.out.println("Error reading file: " + e.getMessage());
			return;
		} catch (NumberFormatException e) {
			System.out.println("Invalid number format in file.");
		}
		if (fileReader != null) {
			try {
				fileReader.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
    
    //Tester Method
	public void printRequest() {
		System.out.println("From: " + this.source + " -> " + this.destination + " | By: " + this.sortBy);
	}
}





/*

                --------------------Graph Construction-------------------------

*/





//This class constructs each Vertex(Node) that will be connected to each unique city through the outer LinkedList
class VertexNode {
	String city; //City
	EdgeNode list; //Linked List that shows all edges for given city (direct flights)
	VertexNode next; //Pointer to next city (master List)

	public VertexNode(String cityName) {
		this.city = cityName;
		this.list = null;
		this.next = null;
	}
}

// This class constructs each edge in the inner linked list (representing a direct flight to another city)
class EdgeNode {
	String destination; //Vertex contains an EdgeNode, so this is the connection between Vertex and the dest Vertex
	int cost; //Weights
	int time;
	EdgeNode next; //Inner Linked List creation

//Constructors
	public EdgeNode() { }

	public EdgeNode(String destination, int cost, int time) {
		this.destination = destination;
		this.cost = cost;
		this.time = time;
		this.next = null;
	}
}

// Graph class that holds head of the outer linked list and provides add/find logic
class FlightGraph {
	private VertexNode head; //head is used as an inline reference for construction

	public FlightGraph() {
		this.head = null;
	}

	//addFlight --> Create vertices source|dest if they do not exist, create an edge between the two if it does not exist
	public void addFlight(String source, String destination, int cost, int time) {
		VertexNode sourceNode = findOrCreateVertex(source);
		VertexNode destNode = findOrCreateVertex(destination);
		addEdge(sourceNode, destination, cost, time);
		addEdge(destNode, source, cost, time);
	}

	//Method to find vertex and insert if it does not exist
	private VertexNode findOrCreateVertex(String city) {
		VertexNode current = head;
		//First, check if city exists
		while (current != null) {
			if (current.city.equals(city)) return current;
			current = current.next;
		}

		// Not found, create and insert at the end
		VertexNode newNode = new VertexNode(city);

		if (head == null) { //If first element
			head = newNode;
		} else { //Otherwise traverse until tail & insert
			VertexNode temp = head;
			while (temp.next != null) {
				temp = temp.next;
			}
			temp.next = newNode;
		}

		return newNode;
	}

	//Method to add an edge between two vertices if it does not exist already
	private void addEdge(VertexNode from, String to, int cost, int time) {
		EdgeNode newEdge = new EdgeNode(to, cost, time);

		if (from.list == null) {
			from.list = newEdge;
		} else {
			EdgeNode temp = from.list;
			while (temp.next != null) {
				temp = temp.next;
			}
			temp.next = newEdge;
		}
	}

	//Method to find a vertex
	public VertexNode findVertex(String city) {
		VertexNode current = head;
		while (current != null) {
			if (current.city.equals(city)) {
				return current;
			}
			current = current.next;
		}
		return null; // City not found in the graph
	}


	//Tester Method to Print the Adjacency List
	public void printGraph() {
		VertexNode current = head;
		while (current != null) {
			System.out.printf("| %-7s | --> ", current.city);
			EdgeNode edge = current.list;
			while (edge != null) {
				System.out.print(edge.destination + " ($" + edge.cost + ", " + edge.time + " min), ");
				edge = edge.next;
			}
			System.out.println();
			current = current.next;
		}
	}
}




/*

                --------------------Iterative Backtracking-------------------------

*/




//This class stores a unique path's properties (Node's visited & weights)
class PathState {
	LinkedList<String> cities; // Linked list of cities in the current path
	int totalCost;
	int totalTime;

	public PathState(LinkedList<String> cities, int cost, int time) {
		this.cities = new LinkedList<>(cities); // Deep copy to preserve path history
		this.totalCost = cost;
		this.totalTime = time;
	}
}

class FlightVerification {
	private FlightGraph graph; //Just a reference to the graph

	public FlightVerification(FlightGraph graph) {
		this.graph = graph;
	}

	public List<PathState> findAllPaths(String start, String end) {
		List<PathState> results = new ArrayList<>(); //Stores all valid paths
		Stack<PathState> stack = new Stack<>(); //Stack that is utilized for each current path

		// Initial path starts at 'start' city
		LinkedList<String> startPath = new LinkedList<>(); //Tracks the initial Vertex and Destination
		startPath.add(start);
		stack.push(new PathState(startPath, 0, 0));

		/*
		    DFS BEGINS HERE
		*/

		while (!stack.isEmpty()) {
			PathState current = stack.pop();
			String lastCity = current.cities.getLast();

			// If we reached the destination, store the full path
			if (lastCity.equals(end)) {
				results.add(current);
				continue;
			}

			// Get VertexNode for the last city
			VertexNode vertex = graph.findVertex(lastCity);
			if (vertex == null) continue; //Additional Measure - If node is not found skip

			// Explore all edges (neighbors)
			EdgeNode edge = vertex.list;
			while (edge != null) {
				if (!current.cities.contains(edge.destination)) {
					// Create a new path with the neighbor added
					LinkedList<String> newPath = new LinkedList<>(current.cities);
					newPath.add(edge.destination);

					PathState newState = new PathState(newPath, current.totalCost + edge.cost, current.totalTime + edge.time);
					stack.push(newState); // Push new partial path to stack
				}
				edge = edge.next;
			}
		}
		return results; // List of all valid paths
	}
}