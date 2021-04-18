package christiansplayer;
import java.util.Stack;
import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    static Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST
    };
    
    static Stack<Direction> moves = new Stack<Direction>();
    
    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};
    static int minersBuilt = 0;
    static int gameStage = 0;
    static int turnCount;
    static int soupRequirement = 75;
    static int[] currentKnowledge = new int[]{0,0,0,0,0,0,42069}; //used to keep track of how many of each building/thing we have, 42069 used to distinguish our messages from the other team in the blockchain
    static Transaction[] compareBlock;

    /* Block-Chain Message Indexes of current knowledge - ID: 222
     * currentKnowledge [0] - # of net guns
     * currentKnowledge [1] - # of 
     * currentKnowledge [2] - # of 
     * currentKnowledge [3] - # of
     * currentKnowledge [4] - # of 
     * currentKnowledge [5] - # of
     * currentKnowledge [6] - # of 
     */
    
    
    
    
    
    
    
    
    
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;
        gameStage = 0;

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You can add the missing ones or rewrite this into your own control structure.
                //System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case HQ:                 runHQ();                break;
                    case MINER:              runMiner();             break;
                    case REFINERY:           runRefinery();          break;
                    case VAPORATOR:          runVaporator();         break;
                    case DESIGN_SCHOOL:      runDesignSchool();      break;
                    case FULFILLMENT_CENTER: runFulfillmentCenter(); break;
                    case LANDSCAPER:         runLandscaper();        break;
                    case DELIVERY_DRONE:     runDeliveryDrone();     break;
                    case NET_GUN:            runNetGun();            break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runHQ() throws GameActionException {
    	//tryBlockchain(gameStage);
    	//Transaction[] compareBlock = rc.getBlock(gameStage);  //If I uncomment this the code does basically nothing
    	
    	//Blockchain Idea:
    	//At each stage check the block chain for messages with our code "42069" at the end (secret code is subject to change)
    	//If we have a match we then compare our currentKnowledge with the new block. If there are any differences we update our currentKnowledge, and continue on normally.
    	//This way all robots should always be on the same page
    	
    	//Problems:
    	//It seems as though whenever we .getBlock the program freezes. 
    	
    	
    	//System.out.println(" Compare Block: " + compareBlock);
    	
        for (Direction dir : directions) {
        	if(minersBuilt <= 4 && tryBuild(RobotType.MINER, dir)) {
            	minersBuilt++;
            	//System.out.println("Current Block: " + rc.getBlock());
           }
        }
    }

    static void runMiner() throws GameActionException {
    	
    	boolean foundSoup = false;
    	boolean hasRefined = false;
    	System.out.println("Im a miner:" + rc.getSoupCarrying());
    	
    	if(rc.getSoupCarrying() < soupRequirement) {
    		for(Direction dir : directions) {
        		if(tryMine(dir)) {
        			foundSoup = true;
        		}
        	}
    		if(foundSoup == false) {
    			if(tryMove(randomDirection())){
    				System.out.println("i moved");
    			}
    		}
    	}
    	
    	for(Direction dir: directions) {
    		if (tryRefine(dir)) {
    			moves.removeAllElements();
    			hasRefined = true;
            	System.out.println("I refined soup! " + rc.getTeamSoup());
            }
    	}
    	
    	if(rc.getSoupCarrying() >= soupRequirement && !hasRefined) {
    		Direction previous_move = moves.pop();
	        
    		if(previous_move == Direction.NORTH) {
    			tryMove(Direction.SOUTH);
    			moves.pop();
    		}
    		if(previous_move == Direction.SOUTH) {
    			tryMove(Direction.NORTH);
    			moves.pop();
    		}
    		if(previous_move == Direction.EAST) {
    			tryMove(Direction.WEST);
    			moves.pop();
    		}
    		if(previous_move == Direction.WEST) {
    			tryMove(Direction.EAST);
    			moves.pop();
    		}
    		if(previous_move == Direction.NORTHWEST) {
    			tryMove(Direction.SOUTHEAST);
    			moves.pop();
    		}
    		if(previous_move == Direction.SOUTHEAST) {
    			tryMove(Direction.NORTHWEST);
    			moves.pop();
    		}
    		if(previous_move == Direction.SOUTHWEST) {
    			tryMove(Direction.NORTHEAST);
    			moves.pop();
    		}
    		if(previous_move == Direction.NORTHEAST) {
    			tryMove(Direction.SOUTHWEST);
    			moves.pop();
    		}
    	}
    	
    	
    	//The goal here is to make 1 netgun total, however without the use of the blockchain, each miner makes 1 netgun themself
    	//In order to make only 1 netgun globally, we have to update the block chain so that all robots can also reflect this change
    	//This code here looks at the current amount of soup, and the current amount of net guns, then it makes one if one doesnt exist
    	//However, I do not know how to then access this, and use it appropriately as when I uncomment getBlock(), it just stops the program
    	
    	if(rc.getTeamSoup() >= 300 && currentKnowledge[0] == 0) {	//if we have 250 soup (+50 for the cost to submit block), and 0 net guns
    		for (Direction dir : directions) {
    			if(tryBuild(RobotType.NET_GUN, dir)) {
    				
    				currentKnowledge[0] = currentKnowledge[0] + 1; //adding 1 to the net gun section
    				rc.submitTransaction(currentKnowledge ,1); //cost, message, ID - Letting everyone know to add 1 to netgun
    				System.out.println("I submitted a transaction!: " + currentKnowledge);
    			}
    		}
    	}
    	
    	
    	
    	
    	
    	
    	
    	/**if(rc.getSoupCarrying() >= soupRequirement) {
    		if(moves.empty()) {
    			System.out.println("Im at the HQ!!!!!");
    			for (Direction dir : directions)
    	            if (tryRefine(dir)) {
    	            	System.out.println("I refined soup! " + rc.getTeamSoup());
    	            }
    		} else {
    			Direction previous_move = moves.pop();
    	    	
    			
    			Direction.NORTH,
    	        Direction.NORTHEAST,
    	        Direction.EAST,
    	        Direction.SOUTHEAST,
    	        Direction.SOUTH,
    	        Direction.SOUTHWEST,
    	        Direction.WEST,
    	        Direction.NORTHWEST
    	        
        		if(previous_move == Direction.NORTH) {
        			tryMove(Direction.SOUTH);
        		}
        		if(previous_move == Direction.SOUTH) {
        			tryMove(Direction.NORTH);
        		}
        		if(previous_move == Direction.EAST) {
        			tryMove(Direction.WEST);
        		}
        		if(previous_move == Direction.WEST) {
        			tryMove(Direction.EAST);
        		}
        		if(previous_move == Direction.NORTHWEST) {
        			tryMove(Direction.SOUTHEAST);
        		}
        		if(previous_move == Direction.SOUTHEAST) {
        			tryMove(Direction.NORTHWEST);
        		}
        		if(previous_move == Direction.SOUTHWEST) {
        			tryMove(Direction.NORTHEAST);
        		}
        		if(previous_move == Direction.NORTHEAST) {
        			tryMove(Direction.SOUTHWEST);
        		}
    		}
    	}
		*/
    	
    	/**
    	if(foundSoup == false) {
    		if(tryMove(randomDirection())) {
    			for (Direction dir : directions)
                    if (tryRefine(dir)) {
                    	System.out.println("I deposited soup");
                    }
    			System.out.println("I moved");
    	    	System.out.println(rc.getSoupCarrying());
    		}else {
    			System.out.println("Im stuck");
    		}
    	}
    	**/
    	/**boolean foundSoup = false;
        for (Direction dir : directions)
            if (tryMine(dir))
                System.out.println("I mined soup! " + rc.getSoupCarrying() + " Location: " + rc.getLocation());
        		foundSoup = true;
        if(foundSoup) {
        	//do nothing
        }else {
        	tryMove(randomDirection());
        }
        ////if (tryMove(randomDirection()))
        // //  System.out.println("I moved!");
        // tryBuild(randomSpawnedByMiner(), randomDirection());
        for (Direction dir : directions)
            tryBuild(RobotType.FULFILLMENT_CENTER, dir);
        for (Direction dir : directions)
            if (tryRefine(dir)) {
            	//System.out.println("I refined soup! " + rc.getTeamSoup());
            }
            **/
    }

    static void runRefinery() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
    }

    static void runVaporator() throws GameActionException {
    	System.out.println("OH MY GOSH IM A VAPORATOR;");
    }

    static void runDesignSchool() throws GameActionException {

    }

    static void runFulfillmentCenter() throws GameActionException {
        for (Direction dir : directions)
            tryBuild(RobotType.DELIVERY_DRONE, dir);
    }

    static void runLandscaper() throws GameActionException {

    }

    static void runDeliveryDrone() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        if (!rc.isCurrentlyHoldingUnit()) {
            // See if there are any enemy robots within capturing range
            RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);

            if (robots.length > 0) {
                // Pick up a first robot within range
                rc.pickUpUnit(robots[0].getID());
                System.out.println("I picked up " + robots[0].getID() + "!");
            }
        } else {
            // No close robots, so search for robots within sight radius
            tryMove(randomDirection());
        }
    }

    static void runNetGun() throws GameActionException {

    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random RobotType spawned by miners.
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnedByMiner() {
        return spawnedByMiner[(int) (Math.random() * spawnedByMiner.length)];
    }

    static boolean tryMove() throws GameActionException {
        for (Direction dir : directions)
            if (tryMove(dir))
                return true;
        return false;
        // MapLocation loc = rc.getLocation();
        // if (loc.x < 10 && loc.x < loc.y)
        //     return tryMove(Direction.EAST);
        // else if (loc.x < 10)
        //     return tryMove(Direction.SOUTH);
        // else if (loc.x > loc.y)
        //     return tryMove(Direction.WEST);
        // else
        //     return tryMove(Direction.NORTH);
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        // System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.isReady() && rc.canMove(dir)) {
            rc.move(dir);
            moves.push(dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to build a given robot in a given direction.
     *
     * @param type The type of the robot to build
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to mine soup in a given direction.
     *
     * @param dir The intended direction of mining
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMineSoup(dir)) {
            rc.mineSoup(dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to refine soup in a given direction.
     *
     * @param dir The intended direction of refining
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryRefine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir)) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        } else return false;
    }


    static void tryBlockchain(int stage) throws GameActionException {
            int[] message = new int[7];
            
   
            
            for (int i = 0; i < 7; i++) {
                message[i] = 123;
            }
            if (rc.canSubmitTransaction(message, 10))
                rc.submitTransaction(message, 10);
        // System.out.println(rc.getRoundMessages(turnCount-1));
    }
}
