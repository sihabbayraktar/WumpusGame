The Wumpus world
The wumpus world is a grid cave world, as show in the picture below.

wumpus-fullObs.png  


Somewhere in one of the tiles there is a dreadful monster called The Wumpus, which eats anyone that accesses the tile where it is in. Luckily for our agent, the monster does not move (i.e., change tile during the game). But that is not the only threat to our agent's life: each tile can also contain a bottomless pit which will trap our agent forever if it gets there. As the real world however, the cave is not only danger and pain: one of the tile contains a shiny gold chest which will give eternal fame and glory to our brave agent, provided it can get grab it and climb out of the cave. Besides, the agent has a bow and only one arrow available to kill The Wumpus, if it has/wants to.

As explained in Section 2.3 of the book, the game is fully described by the following PEAS specification:

Performance Measure:
Start at 0 points;
-1 point for each action taken;
-10 for using the arrow (additional to the -1 point), -1 for the other times;
-1000 points for falling into a pit or being eaten by The Wumpus;
+1000 for climbing out of the cave with the gold;
The game ends either when the agent dies, when the agent climbs out of the cave, or when the agent’s score goes below -1000.
 
 


The environment:
An NxM grid column x row where 4 ≤ 𝑁, 𝑀 ≤ 10;
The agent always starts in the bottom left square (0, 0), facing to the right;
The locations of the gold and the Wumpus are chosen randomly, with a uniform distribution, from the squares other than the start square;
Each square other than the start can be a pit, with a 20% probability;
The agent dies a miserable death if it enters a square containing a pit or a live Wumpus.
 

Actuators:
The agent can move FORWARD, TURN_LEFT by 90 degrees, or TURN_RIGHT by 90
degrees. The effect of such actions are as expected. You can assume the direction of the agent being one of {N,S,E,W}. If the agent moves forward in a direction where there is a wall, then it does not have any effect (but it still counts as a -1 in the performance measure).
The action GRAB can be used to pick up the gold if it is in the same square as the agent, otherwise it will have no effect.
The action SHOOT can be used to fire an arrow in a straight line in the direction the agent is facing. The arrow continues until it either hits and kills the wumpus or hits a
wall. The agent has only one arrow, so only the first shoot action has any effect.
The action CLIMB can be used to climb out of the cave, but only from square (0, 0).
EVERY ACTION IS ALLOWED IN EVERY STATE, but they possibly do not have any consequence, but still counts as -1 in the performance measure. E.g., shooting the arrow for the second time will not have any effect in the world (as you do not have any arrow left) but it will still give you a -1 in the performance measure.
 

Sensors are not needed for the offline searching agent, as it has fully observability on the environment. They are necessary for implementing the online searching agent in the partially observable cave, and they will be explained later.

Installing the code
Make sure you downloaded Java SE Development Kit 15.0.1 from here (Links to an external site.), or the external libraries needed for the project will not work.
Download and install IntelliJ (Links to an external site.).
Download the project zip file from here and unzip it (it is an Apache Maven (Links to an external site.) project).
Run IntelliJ, select "Open" and choose the folder Wumpus_World (it is an Apache Maven (Links to an external site.) project). Hopefully IntelliJ understands it is maven-based and after some indexing, will set up the project automatically.
Double click on the pom.xml file, click on the small "maven" tab on the right of the window and select Lifecycle -> Install. This compile the project and should finish successfully despite some warnings. You should see something like this:
If not, it might be that IntelliJ is not using the last version of Java you just installed. Right click on the folder Wumpus_World on the left -> Open Module Settings -> Project -> Project SDK should have Java version 15.
The files you have to modify are: SearchAI.java (package fullObservability) for the offline search agent and MyAI (package partialObservability) for the online agent in the partially observable environment.


PART I : Implementing the offline search agent in SearchAI.java
We suggest you get familiar with the game first. Run the file MainSearch.java by creating a run configuration. From the top menu, select: Run -> Edit Configurations -> + -> Application and then selects Java 15 SDK on the right and SearchMain.java as Main class. For this first trial, also write -m in the program arguments box which turns on the manual mode allowing you to play the game interactively by providing the actions to be performed by the agent in the command line. When running this configuration, you will see something like:
========================================================
       .       .      ~.      P.

       .      ~.      s.      ~.

      ~.     Ps.     W~.      s.

      @.     $~.      s.       .

Score: 0
AgentX: 0
AgentY: 0
AgentDir: Right
Last Action: Climbed
Percepts: 
Press 'w' to Move Forward  'a' to 'Turn Left' 'd' to 'Turn Right'
Press 's' to Shoot         'g' to 'Grab'      'c' to 'Climb'
Please input: 
=========================================================

the upper part is a "graphical" representation of the cave, in this case a 4x4 grid where:
@ denotes the position of the agent;
P denotes a pit;
W denotes The Wumpus and
$ denotes the gold chest.

Score is the current score of the agent computed according to the performance measure defined before. The other information is self-explanatory. Percepts are irrelevant as we are now assuming full observability.

If you run the code without the -m option, then it will execute the dummy code in SearchAI.java which returns always the same plan of going forward 8 times; then turn left twice; going forward (which is now backward) ten times and then climb out of the cave, unless it falls into a pit or gets eaten by The Wumpus.

You have to replace the dummy code with a search strategy of your choice in the constructor of SearchAgent class. Please note that you have access the current world configuration as World.Tile[][] board is a parameter of the constructor. Therefore the grid size as well as the position of The Wumpus, golds and pits are known, meaning that the environment is fully observable. So you can devise a plan *offline* before the game start and save it in the LinkedList<Action> plan variable. Once the agent is constructed (and therefore a plan is generated) the actual game starts and the agent will play according to the plan (see method getAction()).

Chapter 3 of the book, and in particular 3.1, explains how to solve a search problem. You have to first build a model of the world (following the PEAS above) or, in other words, what we called a Problem Transition System in class. You need all the components described in 3.1.1 of the book: for example, the state space; but also the actions. You can use the class Action already written in the code. But you would also need the transition model of the world (the RESULT function in the book), as well as the goal test and everything else you need for the search. Once this is done, you can actually implement one of the search algorithms we studied based on trees, so you need data structures for that as well.

You are free to decide on the search algorithm you want to implement but the requirement to pass this part of the project is:
IF the chest is actually reachable (according to the rule of the game) THEN your agent has to find it, go back to tile 0,0 and climb out. (Your code will be tested on one of such configurations to check that the requirement is met.)

If you want, you can also use heuristics to improve the time performance.

Notice the gold is not always reachable because, e.g., it is in a tile with a pit. In that case, the agent has to do something anyway, and it is up to you to devise the optimal plan.

To debug your code, you can use the -d option in the program argument box, which pauses the game after each action and prints the current status.

If you do not specify any other options in the program argument, then it will generate a random 4x4 world and run the agent. But you can also input a text file with the description of a specific world (as well as an output file where to write the results) or you can use -f path_to_folder and then the program will run the agent on all world descriptions found on that folder (the same idea of the competition). How to generate the competition worlds is explained later.

More specifically, the program arguments you can use are (those in [...] are optional):

 [-d]: generates a random world and runs [interactive]
-f folder_to_worlds [output_file]: runs the agent in each world in the folder
-d -f folder_to_worlds: does not work
[-d] file_world [output_file]: runs in the specified world file [and prints the output in output_file] [interactive] output_file works only if the input file is also specified.


PART II : Implementing the online search agent in MyAI.java
Now we are in the setting of partially observable environment. In particular, the agent does not know the size of the world, nor its current configuration in terms of locations of pits, The Wumpus and the gold. The only information available is that the agent starts from 0,0 and that it can rely on its perceptions to survive. The idea is to explore *online* the environment and use a propositional knowledge base to keep track of what it knows and therefore to understand where to move safely.

Agents sensors work as follows:

In the square containing The Wumpus and in the directly (not diagonally) adjacent squares, the agent will perceive a STENCH;
In the squares directly adjacent to a pit, the agent will perceive a BREEZE.
In the square where the gold is, the agent will perceive a GLITTER.
When an agent walks into a wall, it will perceive a BUMP.
When The Wumpus is killed, it emits a woeful SCREAM that can be perceived anywhere in the cave. This percept will only be sensed on the turn immediately after The Wumpus's death.
Please read Section 7.2, 7.3 and 7.4 of the book to get an in-depth understanding of the setting and an idea on how to structure your solution. You can also go through 7.5 and 7.6 that are related. However:

SECTION 7.7 PROVIDES A SOLUTION OF A LOGICAL AGENT THAT INVOLVES KEEPING TRACK OF TIME, WHICH IS NOT ACTUALLY NEEDED HERE.

Therefore, you can have a look at that as well but your solution **MUST NOT** involve time, nor fluents (which makes it much easier). Read 7.7.2 and 7.7.3, because you have to implement something like the hybrid-wumpus-agent in Figure 7.20.

OBS: Do *NOT* go through Section 7.7.4, because that approach is different from what you have to do.

To implement the agent, you have to modify the file MyAI.java. Differently from before, now the constructor does not have the current configuration as a parameter. But the method getAction() has the current perceptions as parameters, which can be used to understand what is happening, and in particular, if the information it has collected so far is enough to derive information on the location of gold, The Wumpus and the pits. Recall that now the game will be played *online*, so, differently from before, you will be mainly modify the getAction() method which is called at each step of the game.

You DO NOT have to implement any algorithm for propositional reasoning. You just need to: model the world in a similar way of how it is done in Section 7.7.1 using propositional logic formulas; build a knowledge base with those formulas and update it with the perceptions at each step. To build and query the knowledge base you can use the external library Tweety, that is already linked to the project. Tweety actually provides a set of libraries for reasoning in different logics, but what we need here is the package org.tweetyproject.logics.pl which is for propositional reasoning. The API of library can be found here (Links to an external site.): but look only at the classes in org.tweetyproject.logics.pl, and in particular org.tweetyproject.logics.pl.syntax for building the formulae; org.tweetyproject.logics.pl.syntax.PlBeliefSet for the knowledge base and org.tweetyproject.logics.pl.reasoner for performing the reasoning tasks.

You must re-use the search algorithm developed in Part I to implement the hybrid-wumpus-agent. To do that, you can perform the search in a public static method in SearchAI and then call it from MyAI. Notice that this search must include only SAFE tiles on the plan (see Algorithm hybrid-wumpus-agent).

You pass this part of the project if your agent finds the gold and climbs out of the cave in configurations where it is safe to do that. In other words, every time it is possible to infer that there is a safe path to the gold, then the agent should find a way to reach it and climb out from the cave. (Your code will be tested on one of such configurations to check that the requirement is met.)

To test the agent in MyAI.java, you have to run Main.java in the PartialObservability package. The command line argument are similar to those in Part I.


How to generate worlds and competitions
If you would like to make your own world file, you can create a text file according to the following format:

[column dimension][tab][row dimension]

[wumpus column] [tab] [wumpus row]

[gold column][tab][gold row]

[number of pits]
[pit1 column][tab][pit1 row]

[pit2 column][tab][pit2 row] 
.
.

.
[pitN column][tab][pitN row]

 

and pass the path to the file as the program argument.


In the project zip file you can find also the folder Wumpus_World_generator, which requires an installation of python to work. From you shell/terminal, go inside that folder and write "make" to create a set of worlds of a specific size. Or write "make tournamentSet" to generate a folder with a set of random worlds of different sizes. The path to this folder can be passed (with option -f) to the main files as program arguments to simulate a competition.
