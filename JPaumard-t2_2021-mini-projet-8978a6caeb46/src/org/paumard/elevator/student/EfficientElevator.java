// Hela DHIFLI && Makerem BEN CHEIKH
package org.paumard.elevator.student;

import org.paumard.elevator.Building;
import org.paumard.elevator.Elevator;
import org.paumard.elevator.model.Person;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EfficientElevator implements Elevator {
    private static final int ANGER_LIMIT_THRESHOLD = 180;
    private int currentFloor = 1;
    private final int capacity;
    
    	//liste des personnes qui attendent a chaque etages
	private List<List<Person>> peopleByFloor = List.of();
	
		//liste des personnes dans ascenceurs
	private List<Person> people = new ArrayList<>();
	
		//permet de savoir l'heure a un moment donné
	private LocalTime time;
	private List<Integer> destinations = new ArrayList<>();

    public EfficientElevator(int capacity) {
		this.capacity = capacity;
    }

    @Override
    public void startsAtFloor(LocalTime time, int initialFloor) {
		this.time = time;
    }

    @Override
    public void peopleWaiting(List<List<Person>> peopleByFloor) {
    	this.peopleByFloor = peopleByFloor;
    }

    @Override
    public List<Integer> chooseNextFloors() {
    	
    	//Quand L'elevator arrive a un etage, il 
    	//ajoute les dest des gens qui attendent a cet etage pour qu'ils
    	//montent dans ascenceur
    	 addDestinationOfCurrentFloor();
		
		
		 if (!this.destinations.isEmpty()) {
	    		return this.destinations;
	     }
    	
		 //si ma table de dest est vide
		 int numberOfPeopleWaiting = countWaitingPeople();
		 
		 if (numberOfPeopleWaiting > 0) {
    		//priorite : les gens qui attendent plus que 180 sec
    		List<Integer> destinations = destinationsToPickUpAngryPeople();
    		if (!destinations.isEmpty()) {
    			this.destinations = destinations;
    			return this.destinations;
    		}
    		//je cherche les etages non vides 
    		List<Integer> nonEmptyFloors = findNonEmptyFloor();
    		int nonEmptyFloor = nonEmptyFloors.get(0);
    		if (nonEmptyFloor != this.currentFloor) {
    			return List.of(nonEmptyFloor);
    		} else {
    			int indexOfCurrentFloor = this.currentFloor - 1;
				List<Person> waitingListForCurrentFloor = 
						this.peopleByFloor.get(indexOfCurrentFloor);
				
				List<Integer> destinationFloorsForCurrentFloor = 
						findDestinationFloors(waitingListForCurrentFloor);
				this.destinations  = destinationFloorsForCurrentFloor;
				return this.destinations;
    		}
    	}
    	
    	//pas de gens qui attendent => retour au 1
    	return List.of(1);
    }

	private void addDestinationOfCurrentFloor() {
		int indexCurrentFloor = this.currentFloor - 1;
		 if (this.peopleByFloor.get(indexCurrentFloor).size() != 0) {
				List<Integer> destinationFloorsForCurrentFloor = 
						findDestinationFloors(this.peopleByFloor.get(indexCurrentFloor));
			 this.destinations.addAll(destinationFloorsForCurrentFloor);
			 this.destinations = this.destinations.stream().distinct()
					 			.sorted().collect(Collectors.toList());
		 }
	}

    	//retourne etage et dest de la personne qui a attendu le plus
	private List<Integer> destinationsToPickUpAngryPeople() {
		
		for (int indexFloor = 0 ; indexFloor < Building.MAX_FLOOR ; indexFloor++) {
			List<Person> waitingList = this.peopleByFloor.get(indexFloor);
			if (!waitingList.isEmpty()) {
				Person mostPatientPerson = waitingList.get(0);
				LocalTime arrivalTime = mostPatientPerson.getArrivalTime();
				Duration waitingTime = Duration.between(arrivalTime, this.time); 
				long waitingTimeInSeconds = waitingTime.toSeconds();
				if (waitingTimeInSeconds >= ANGER_LIMIT_THRESHOLD) {
					List<Integer> result = List.of(indexFloor + 1, mostPatientPerson.getDestinationFloor());
					return new ArrayList<>(result);
				}
			}
		}
		return List.of();
	}

		//renvoit la liste des destinationFloor des gens de l'etage dans lequel je suis
	private List<Integer> findDestinationFloors(List<Person> waitingListForCurrentFloor) {
		return waitingListForCurrentFloor.stream()
			.map(person -> person.getDestinationFloor())
			.distinct()
			.sorted()
			.collect(Collectors.toList());
	}

		//retourne l'etage ou il y a quelqu'un
	private List<Integer> findNonEmptyFloor() {
		for (int indexFloor = 0 ; indexFloor < Building.MAX_FLOOR ; indexFloor++) {
			if (!peopleByFloor.get(indexFloor).isEmpty()) {
				return List.of(indexFloor + 1);
			}
		}
		return List.of(-1);
	}

	private int countWaitingPeople() {
		return peopleByFloor.stream()
			.mapToInt(list -> list.size())
			.sum();
	}

    @Override
    public void arriveAtFloor(int floor) {
    	if (!this.destinations.isEmpty()) {
    		this.destinations.remove(0);
    	}
    	this.currentFloor = floor;
    }

    @Override
    public void loadPeople(List<Person> people) {
    	this.people.addAll(people);
    	int indexFloor = this.currentFloor - 1;
    	this.peopleByFloor.get(indexFloor).removeAll(people);
    }

    @Override
    public void unload(List<Person> people) {
    	this.people.removeAll(people);
    }

    @Override
    public void newPersonWaitingAtFloor(int floor, Person person) {
    	int indexFloor = floor - 1;
    	this.peopleByFloor.get(indexFloor).add(person);
    }

    @Override
    public void lastPersonArrived() {
    }

    @Override
    public void timeIs(LocalTime time) {
    	this.time = time;
    }

    @Override
    public void standByAtFloor(int currentFloor) {
    }


}
